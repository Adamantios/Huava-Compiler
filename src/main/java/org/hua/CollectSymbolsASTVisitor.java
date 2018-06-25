package org.hua;

import org.objectweb.asm.Type;
import org.hua.symbol.LocalIndexPool;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;

import org.hua.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect all symbols such as variables, methods, etc in symbol table.
 */
class CollectSymbolsASTVisitor implements ASTVisitor {

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        int index = pool.getLocalIndex();
        st.put("write" + "function", new SymTableEntry("write", Type.VOID_TYPE, index));

        for (ClassDefinition c : node.getClassDefinitionList()) {
            c.accept(this);
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatementList()) {
            st.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (node.getStatement() != null)
            node.getStatement().accept(this);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (node.getStatement1() != null)
            node.getStatement1().accept(this);
        if (node.getStatement2() != null)
            node.getStatement2().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (node.getStatement() != null)
            node.getStatement().accept(this);
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        for (Expression e : node.getExpressionList()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(StorageSpecifier node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(SimpleFunctionCallExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(MethodIdentifier node) throws ASTVisitorException {
        for (Expression e : node.getExpressionList()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(SimpleMethodIdentifier node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(SimpleConstructorIdentifier node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ConstructorIdentifier node) throws ASTVisitorException {
        for (Expression e : node.getExpressionList()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(SimpleIdentifierExpression node) throws ASTVisitorException {
        // nothing here
    }

    @Override
    public void visit(SimpleReturnStatement node) throws ASTVisitorException {
        // nothing here
    }

    @Override
    public void visit(SimpleStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        // nothing here.
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        List<Type> parameterTypes = new ArrayList<Type>();
        int parameterIndex = 0;

        if (node.getCompoundStatement() != null) {
            node.getCompoundStatement().accept(this);

            // FIND CS SYMBOL TABLE
            SymTable<SymTableEntry> csST = ASTUtils.getSafeEnv(node.getCompoundStatement());
            for (ParameterDeclaration p : node.getParameterDeclarationList())
                if (csST.lookupOnlyInTop(p.getIdentifier() + "var") != null)
                    ASTUtils.error(node, "Redefined variable found inside function!");
        }
        if (!node.isStatic())
            parameterIndex++;
        for (ParameterDeclaration p : node.getParameterDeclarationList()) {
            SymTableEntry symbol = new SymTableEntry(p.getIdentifier(), p.getType(), parameterIndex++);
            SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(p);
            st.put(p.getIdentifier() + "var", symbol);

            parameterTypes.add(p.getType());
        }

        Type[] a = new Type[parameterTypes.size()];
        int i = 0;
        for (Type t : parameterTypes)
            a[i++] = t;
        Type functionType = Type.getMethodType(node.getReturnType(), a);

        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);

        if (st.lookupOnlyInTop(node.getIdentifier()) != null)
            ASTUtils.error(node, "Function redefined!");
        else {
            LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
            int index = pool.getLocalIndex();
            SymTableEntry symbol = new SymTableEntry(node.getIdentifier(), index);
            symbol.setStatic(node.isStatic());
            symbol.setType(functionType);
            symbol.setParameterTypes(parameterTypes);
            st.put(node.getIdentifier() + "function", symbol);
        }
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        // test if class exists
        Type t = Type.getType("Lorg/hua/" + node.getIdentifier() + ";");
        if (Registry.getInstance().getClasses().containsKey(t))
            ASTUtils.error(node, "Redefined Class!");

        Registry.getInstance().getClasses().put(t, ASTUtils.getSafeEnv(node));

        for (FieldOrFunctionDefinition f : node.getFieldOrFunctionDefinitionList()) {
            f.accept(this);
        }
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);

        if (st.lookupOnlyInTop(node.getIdentifier() + "var") != null)
            ASTUtils.error(node, "Redefined variable!");

        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        int index = pool.getLocalIndex(node.getType());
        SymTableEntry symbol = new SymTableEntry(node.getIdentifier(), node.getType(), index, node.isStatic());
        st.put(node.getIdentifier() + "var", symbol);
    }
}
