package org.hua;

import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;

import org.hua.types.TypeUtils;

import org.hua.ast.*;

import org.objectweb.asm.Type;
import org.hua.types.TypeException;

import java.util.List;

/**
 * Compute possible types for each node.
 */
class CollectTypesASTVisitor implements ASTVisitor {

    private int whileCounter;
    private boolean insideMain;
    private boolean moreFilesExpected;
    private boolean insideStaticFunction;
    private boolean afterNull;
    private Type currentFunctionReturnType;
    private Type currentClass;

    CollectTypesASTVisitor(boolean moreFilesExpected) {
        this.whileCounter = 0;
        this.insideStaticFunction = false;
        this.insideMain = false;
        this.moreFilesExpected = moreFilesExpected;
        this.afterNull = false;
    }

    private boolean typeExists(Type type) {
        boolean typeFound = false;

        if (type == Type.VOID_TYPE ||
                type == Type.INT_TYPE ||
                type == Type.FLOAT_TYPE ||
                type.equals(Type.getType("Ljava/lang/String;")))
            typeFound = true;

        if (!typeFound)
            for (Type t : Registry.getInstance().getClasses().keySet())
                if (type.equals(t)) {
                    typeFound = true;
                    break;
                }

        return typeFound;
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (ClassDefinition c : node.getClassDefinitionList())
            c.accept(this);

        ASTUtils.setType(node, Type.VOID_TYPE);

        int mainCounter = 0;
        for (Type t : Registry.getInstance().getClasses().keySet()) {
            for (SymTableEntry s : Registry.getInstance().getClasses().get(t).getSymbols()) {
                if (s.getIdentifier().equals("main")) {
                    mainCounter++;
                    if (mainCounter > 1)
                        ASTUtils.error(node, "There has to be strictly one main in your program!");
                    if (!s.isStatic())
                        ASTUtils.error(node, "'main' has to be static!");
                    if (!s.getType().equals(Type.getType("()V")))
                        ASTUtils.error(node, "'main' must have void return type!");
                }
            }
        }

        if (mainCounter == 0 && !moreFilesExpected)
            ASTUtils.error(node, "There has to be one main in your program!");
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        afterNull = false;
        node.getExpression1().accept(this);
        Type t1 = ASTUtils.getSafeType(node.getExpression1());
        node.getExpression2().accept(this);
        Type t2 = ASTUtils.getSafeType(node.getExpression2());

        if (t1.equals(Type.VOID_TYPE) || t1.equals(Type.INT_TYPE) || t1.equals(Type.FLOAT_TYPE))
            if (afterNull)
                ASTUtils.error(node, "Incompatible types!");

        if (TypeUtils.isAssignable(t1, t2)) {
            ASTUtils.setType(node, TypeUtils.maxType(t1, t2));
        } else
            ASTUtils.error(node, "Not Assignable!");
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatementList()) {
            st.accept(this);
        }

        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);

        try {
            ASTUtils.setType(node, TypeUtils.applyBinary(node.getOperator(),
                    ASTUtils.getSafeType(node.getExpression1()),
                    ASTUtils.getSafeType(node.getExpression2())));

        } catch (TypeException e) {
            ASTUtils.error(node, e.getMessage());
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        try {
            ASTUtils.setType(node, TypeUtils.applyUnary(node.getOperator(), ASTUtils.getSafeType(node.getExpression())));
        } catch (TypeException e) {
            ASTUtils.error(node, e.getMessage());
        }
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node.getExpression());
        if (st == null)
            ASTUtils.error(node, node.getIdentifier() + " called on an unknown instance!");

        Type expressionType = ASTUtils.getSafeType(node.getExpression());
        boolean found = false;
        for (SymTableEntry symTableEntry : Registry.getInstance().getClasses().get(expressionType).getSymbols()) {
            if (symTableEntry.getIdentifier().equals(node.getIdentifier())) {
                found = true;
                ASTUtils.setType(node, symTableEntry.getType());
            }
        }

        if (!found)
            ASTUtils.error(node, "Undefined reference: identifier " + node.getIdentifier() + "!");

        SymTableEntry s = st.lookup(node.getExpression() + "var");
        if (s != null) {
            if (insideStaticFunction && !insideMain)
                if (!s.isStatic())
                    ASTUtils.error(node, "Access of non static variable is not allowed inside static function!");
        } else {
            SymTable<SymTableEntry> ClassSt = Registry.getInstance().getClasses().get(expressionType);
            SymTableEntry ClassEntry = ClassSt.lookup(node.getIdentifier() + "var");
            if (insideStaticFunction && !insideMain)
                if (!ClassEntry.isStatic())
                    ASTUtils.error(node, "Access of non static variable is not allowed inside static function!");
        }

    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.INT_TYPE);
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.FLOAT_TYPE);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.getType(String.class));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        whileCounter++;
        node.getExpression().accept(this);

        Type t = ASTUtils.getSafeType(node.getExpression());

        if (!t.equals(Type.BOOLEAN_TYPE) && !t.equals(Type.INT_TYPE)) {
            ASTUtils.error(node, "Boolean type expected!" + "Found [" + t + "] instead!");
        }

        if (node.getStatement() != null)
            node.getStatement().accept(this);

        ASTUtils.setType(node, Type.VOID_TYPE);
        whileCounter--;
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        if (whileCounter == 0) {
            ASTUtils.error(node, "Break outside of a while loop detected!");
        }

        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        if (whileCounter == 0) {
            ASTUtils.error(node, "Continue outside of a while loop detected!");
        }

        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type t = ASTUtils.getSafeType(node.getExpression());

        if (!t.equals(Type.BOOLEAN_TYPE)) {
            ASTUtils.error(node, "Boolean type expected!" + "Found [" + t + "] instead!");
        }

        if (node.getStatement1() != null)
            node.getStatement1().accept(this);
        if (node.getStatement2() != null)
            node.getStatement2().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type t = ASTUtils.getSafeType(node.getExpression());

        if (!t.equals(Type.BOOLEAN_TYPE))
            ASTUtils.error(node, "Boolean type expected!" + "Found [" + t + "] instead!");

        if (node.getStatement() != null)
            node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));

        Type t1 = ASTUtils.getSafeType(node.getExpression());
        if (!TypeUtils.isAssignable(currentFunctionReturnType, t1))
            ASTUtils.error(node, "Return type must be the same with the function's return type!");
    }

    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        if (insideStaticFunction)
            ASTUtils.error(node, "'this' keyword is not allowed inside static function!");

        ASTUtils.setType(node, currentClass);
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        afterNull = true;
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        // get type of node.getExpression()
        Type t = ASTUtils.getSafeType(node.getExpression());
        // if class does not exist error
        if (!Registry.getInstance().getClasses().containsKey(t))
            ASTUtils.error(node, "Type not found!");

        // registry - lookup class and symbol table
        // lookup function in symbol table
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");

        Type methodType;
        List<Type> types;
        if (s != null) {
            methodType = s.getType();

            if (!Registry.getInstance().getClasses().get(t).getSymbols().contains(s))
                ASTUtils.error(node, "There is no such method in this class!");

//            if (insideStaticFunction && !insideMain)
//                if (!s.isStatic())
//                    ASTUtils.error(node, "Access of non static function is not allowed inside static function!");

            if (node.getExpressionList().size() != s.getParameterTypes().size())
                ASTUtils.error(node, "Wrong number of parameters!");

            types = s.getParameterTypes();
        } else {
            // Find expression type
            Type exprType = ASTUtils.getSafeType(node.getExpression());
            SymTable<SymTableEntry> classSt = Registry.getInstance().getClasses().get(exprType);
            SymTableEntry classEntry = classSt.lookup(node.getIdentifier() + "function");
            methodType = classEntry.getType();

            if (!Registry.getInstance().getClasses().get(t).getSymbols().contains(classEntry))
                ASTUtils.error(node, "There is no such method in this class!");

//            if (insideStaticFunction && !insideMain)
//                if (!classEntry.isStatic())
//                    ASTUtils.error(node, "Access of non static function is not allowed inside static function!");

            if (node.getExpressionList().size() != classEntry.getParameterTypes().size())
                ASTUtils.error(node, "Wrong number of parameters!");

            types = classEntry.getParameterTypes();
        }

        ASTUtils.setType(node, methodType.getReturnType());

        for (Expression e : node.getExpressionList()) {
            e.accept(this);
            Type type = ASTUtils.getSafeType(e);

            // check for each parameter if it is assignable
            for (Type ty : types)
                if (!TypeUtils.isAssignable(ty, type))
                    ASTUtils.error(node, "Wrong parameter type found!");
        }
    }

    @Override
    public void visit(StorageSpecifier node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(SimpleFunctionCallExpression node) throws ASTVisitorException {

        node.getExpression().accept(this);
        // get type of node.getExpression()
        Type t = ASTUtils.getSafeType(node.getExpression());
        // if not class error
        if (!Registry.getInstance().getClasses().containsKey(t))
            ASTUtils.error(node, "There is no class with that name!");
        // registry - lookup class and symbol table
        // lookup function in symbol table
        SymTable<SymTableEntry> st = Registry.getInstance().getClasses().get(ASTUtils.getSafeType(node.getExpression()));
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");

        Type methodType;
        if (s != null) {
            methodType = s.getType();

            if (!Registry.getInstance().getClasses().get(t).getSymbols().contains(s))
                ASTUtils.error(node, "There is no such method in this class!");

//            if (insideStaticFunction && !insideMain)
//                if (!s.isStatic())
//                    ASTUtils.error(node, "Access of non static function is not allowed inside static function!");
        } else {
            // Find expression type
            Type exprType = ASTUtils.getSafeType(node.getExpression());
            SymTable<SymTableEntry> classSt = Registry.getInstance().getClasses().get(exprType);
            SymTableEntry classEntry = classSt.lookup(node.getIdentifier() + "function");
            methodType = classEntry.getType();

            if (!Registry.getInstance().getClasses().get(t).getSymbols().contains(classEntry))
                ASTUtils.error(node, "There is no such method in this class!");

//            if (insideStaticFunction)
//                if (!classEntry.isStatic())
//                    ASTUtils.error(node, "Access of non static function is not allowed inside static function!");
        }

        ASTUtils.setType(node, methodType.getReturnType());
    }

    @Override
    public void visit(MethodIdentifier node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);

        SymTableEntry s = st.lookup(node.getIdentifier() + "function");
        if (s == null)
            ASTUtils.error(node, "Undefined reference method call " + node.getIdentifier() + "!");

        if (insideStaticFunction)
            if (!s.isStatic())
                ASTUtils.error(node, "Access of non static function is not allowed inside static function!");

        if (node.getIdentifier().equals("write"))
            if (node.getExpressionList().size() != 1)
                ASTUtils.error(node, "'write' must have one parameter!");

        List<Type> types = s.getParameterTypes();
        if (types.size() != node.getExpressionList().size())
            ASTUtils.error(node, "Wrong number of parameters!");

        // check for each parameter if it is assignable
        int i = 0;
        //noinspection Duplicates
        for (Expression e : node.getExpressionList()) {
            e.accept(this);
            Type type = ASTUtils.getSafeType(e);

            if (!TypeUtils.isAssignable(types.get(i++), type))
                ASTUtils.error(node, "Wrong parameter type found!");
        }

        Type functionType = s.getType();
        ASTUtils.setType(node, functionType.getReturnType());
    }

    @Override
    public void visit(SimpleMethodIdentifier node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);

        SymTableEntry s = st.lookup(node.getIdentifier() + "function");
        if (s == null)
            ASTUtils.error(node, "Undefined reference method call " + node.getIdentifier() + "!");

        if (node.getIdentifier().equals("write"))
            ASTUtils.error(node, "'write' must have one parameter!");

        if (insideStaticFunction)
            if (!s.isStatic())
                ASTUtils.error(node, "Access of non static function is not allowed inside static function!");

        Type functionType = s.getType();
        ASTUtils.setType(node, functionType.getReturnType());
    }

    @Override
    public void visit(SimpleConstructorIdentifier node) throws ASTVisitorException {
        // construct Type for class
        Type t = Type.getType("Lorg/hua/" + node.getIdentifier() + ";");
        // check exists in Registry
        if (!Registry.getInstance().getClasses().containsKey(t))
            ASTUtils.error(node, "Constructor does not exist!");

        ASTUtils.setType(node, t);
    }

    @Override
    public void visit(ConstructorIdentifier node) throws ASTVisitorException {
        // construct Type for class
        Type t = Type.getType("Lorg/hua/" + node.getIdentifier() + ";");
        // check exists in Registry
        if (!Registry.getInstance().getClasses().containsKey(t))
            ASTUtils.error(node, "Constructor does not exist!");

        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");

        List<Type> types = s.getParameterTypes();
        if (types.size() != node.getExpressionList().size())
            ASTUtils.error(node, "Wrong number of parameters!");

        // check for each parameter if it is assignable
        int i = 0;
        //noinspection Duplicates
        for (Expression e : node.getExpressionList()) {
            e.accept(this);
            Type type = ASTUtils.getSafeType(e);

            if (!TypeUtils.isAssignable(types.get(i++), type))
                ASTUtils.error(node, "Wrong parameter type found!");
        }

        ASTUtils.setType(node, t);
    }

    @Override
    public void visit(SimpleIdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);

        SymTableEntry s = st.lookup(node.getIdentifier() + "var");
        if (s == null)
            ASTUtils.error(node, "Undefined reference identifier " + node.getIdentifier() + "!");

        if (insideStaticFunction)
            if (!s.isStatic() && !s.isDefinedInStaticFunction())
                ASTUtils.error(node, "Access of non static variable is not allowed inside static function!");

        ASTUtils.setType(node, s.getType());
    }

    @Override
    public void visit(SimpleReturnStatement node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(SimpleStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        Type type = node.getType();

        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "var");

        if (insideStaticFunction)
            s.setStatic(true);

        if (!typeExists(type))
            ASTUtils.error(node, "Type not found!");

        ASTUtils.setType(node, type);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        if (node.isStatic())
            insideStaticFunction = true;

        currentFunctionReturnType = node.getReturnType();
        if (!typeExists(currentFunctionReturnType))
            ASTUtils.error(node, "Type not found!");
        ASTUtils.setType(node, currentFunctionReturnType);

        if (node.getIdentifier().equals("main"))
            insideMain = true;

        if (node.getIdentifier().equals("write"))
            ASTUtils.error(node, "Write function already exists!");

        for (ParameterDeclaration p : node.getParameterDeclarationList())
            p.accept(this);

        if (node.getCompoundStatement() != null)
            node.getCompoundStatement().accept(this);

        currentFunctionReturnType = null;
        insideStaticFunction = false;
        insideMain = false;
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        for (FieldOrFunctionDefinition f : node.getFieldOrFunctionDefinitionList()) {
            f.accept(this);
        }

        currentClass = Type.getType("Lorg/hua/" + node.getIdentifier() + ";");
        ASTUtils.setType(node, currentClass);
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        Type type = node.getType();

        if (insideMain)
            if (node.isStatic())
                ASTUtils.error(node, "Modifier static is not allowed here!");

        if (!typeExists(type))
            ASTUtils.error(node, "Type not found!");

        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "var");

        if (insideStaticFunction)
            s.setDefinedInStaticFunction(true);

        ASTUtils.setType(node, type);
    }
}
