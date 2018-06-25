package org.hua;

import org.hua.ast.*;

import org.hua.helpers.Category;
import org.hua.helpers.TerminalColors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.objectweb.asm.Type;

class PrintASTVisitor implements ASTVisitor {

    /**
     * The tabs needed for the code formation.
     */
    private int tabs = 0;

    /**
     * Flag which shows if a semicolon needs to be added.
     */
    private boolean needsSemicolon = false;

    /**
     * Flag which shows if we are inside a write function.
     */
    private boolean insideWrite = false;

    /**
     * Prints the given String with a color depending on its category.
     *
     * @param s        the String to be printed.
     * @param category the category of the String.
     */
    private void ColourfulPrint(String s, Category category) {
        switch (category) {
            case TYPE:
                System.out.print(TerminalColors.ANSI_YELLOW + s + TerminalColors.ANSI_RESET);
                break;
            case STATEMENT:
                System.out.print(TerminalColors.ANSI_BLUE + s + TerminalColors.ANSI_RESET);
                break;
            case METHOD:
                System.out.print(TerminalColors.ANSI_GREEN + s + TerminalColors.ANSI_RESET);
                break;
            case KEYWORD:
                System.out.print(TerminalColors.ANSI_BLUE + s + TerminalColors.ANSI_RESET);
                break;
            case IDENTIFIER:
                System.out.print(TerminalColors.ANSI_WHITE + s + TerminalColors.ANSI_RESET);
                break;
            case NUMBER:
                System.out.print(TerminalColors.ANSI_CYAN + s + TerminalColors.ANSI_RESET);
                break;
            case STRING:
                System.out.print(TerminalColors.ANSI_RED + s + TerminalColors.ANSI_RESET);
                break;
            case OPERATOR:
                System.out.print(TerminalColors.ANSI_PURPLE + s + TerminalColors.ANSI_RESET);
                break;
            case AFTERDOT:
                System.out.print(TerminalColors.ANSI_WHITE + s + TerminalColors.ANSI_RESET);
                break;
            default:
                System.out.print(s);
        }
    }

    /**
     * Prints as many tabs as the value of the variable tabs.
     */
    private void printTabs() {
        for (int i = 0; i < tabs; i++)
            System.out.print("    ");
    }

    /**
     * Filters a type and removes unwanted characters.
     * @param type the type to be filtered.
     * @return String
     */
    private String filterType(String type) {
        return type.substring(type.lastIndexOf("/") + 1).replace(";", "");
    }

    /**
     * Returns the Type in a beautified String.
     * @param exprType the type to be beautified.
     * @return String
     */
    private String beautifyType(Type exprType) {
        if (exprType.equals(Type.VOID_TYPE))
            return  "void";
        else if (exprType.equals(Type.INT_TYPE))
            return  "int";
        else if (exprType.equals(Type.FLOAT_TYPE))
            return  "float";
        else
            return filterType(exprType.toString());
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (ClassDefinition s : node.getClassDefinitionList()) {
            s.accept(this);
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        needsSemicolon = false;

        printTabs();
        node.getExpression1().accept(this);
        ColourfulPrint(" = ", Category.OPERATOR);
        node.getExpression2().accept(this);

        if (needsSemicolon)
            System.out.println(";");
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        insideWrite = true;
        printTabs();
        ColourfulPrint("write", Category.METHOD);
        System.out.print("(");
        node.getExpression().accept(this);
        System.out.println(");");
        needsSemicolon = false;
        insideWrite = false;
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("while", Category.STATEMENT);
        System.out.print("(");
        node.getExpression().accept(this);
        System.out.print(") ");
        if (node.getStatement() != null)
            node.getStatement().accept(this);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" ");
        ColourfulPrint(node.getOperator().toString(), Category.OPERATOR);
        System.out.print(" ");
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ColourfulPrint(node.getOperator().toString(), Category.OPERATOR);
        System.out.print(" ");
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ColourfulPrint(".", Category.OPERATOR);
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        needsSemicolon = true;
        ColourfulPrint(node.getLiteral().toString(), Category.NUMBER);
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        needsSemicolon = true;
        ColourfulPrint(node.getLiteral().toString(), Category.NUMBER);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        needsSemicolon = true;
        System.out.print("\"");
        ColourfulPrint(StringEscapeUtils.escapeJava(node.getLiteral()), Category.STRING);
        System.out.print("\"");
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        System.out.print("( ");
        node.getExpression().accept(this);
        System.out.print(" )");
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        tabs++;
        System.out.println("{");

        for (Statement st : node.getStatementList()) {
            st.accept(this);
        }

        tabs--;
        printTabs();
        System.out.println("}");
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("if", Category.STATEMENT);
        System.out.print("(");
        node.getExpression().accept(this);
        System.out.print(") ");
        if (node.getStatement1() != null)
            node.getStatement1().accept(this);
        else {
            System.out.println("{");
            printTabs();
            System.out.println("}");
        }
        printTabs();
        ColourfulPrint("else ", Category.STATEMENT);
        if (node.getStatement2() != null)
            node.getStatement2().accept(this);
        else {
            System.out.println("{");
            printTabs();
            System.out.println("}");
        }
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("if", Category.STATEMENT);
        System.out.print("(");
        node.getExpression().accept(this);
        System.out.print(") ");
        if (node.getStatement() != null)
            node.getStatement().accept(this);
        else {
            System.out.print("{");
            printTabs();
            System.out.print("}");
        }
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("break", Category.KEYWORD);
        System.out.println(";");
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("continue", Category.KEYWORD);
        System.out.println(";");
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        printTabs();
        needsSemicolon = false;

        ColourfulPrint("return ", Category.KEYWORD);
        node.getExpression().accept(this);

        if (needsSemicolon)
            System.out.println(";");
    }

    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        ColourfulPrint("this", Category.KEYWORD);
        System.out.println(";");
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        ColourfulPrint("null", Category.KEYWORD);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ColourfulPrint(".", Category.OPERATOR);
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.print("(");

        boolean firstVisitDone = false;
        //noinspection Duplicates
        for (Expression e : node.getExpressionList()) {

            if (firstVisitDone)
                ColourfulPrint(", ", Category.AFTERDOT);

            e.accept(this);
            firstVisitDone = true;
        }

        if (needsSemicolon && !insideWrite)
            System.out.println(");");
        needsSemicolon = false;
    }

    @Override
    public void visit(StorageSpecifier node) throws ASTVisitorException {
        ColourfulPrint("static ", Category.KEYWORD);
    }

    @Override
    public void visit(SimpleFunctionCallExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ColourfulPrint(".", Category.OPERATOR);
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.println("();");
        needsSemicolon = false;
    }

    @Override
    public void visit(MethodIdentifier node) throws ASTVisitorException {
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.print("(");

        boolean firstVisitDone = false;
        //noinspection Duplicates
        for (Expression e : node.getExpressionList()) {

            if (firstVisitDone)
                ColourfulPrint(", ", Category.AFTERDOT);

            e.accept(this);
            firstVisitDone = true;
        }

        if (needsSemicolon)
            System.out.println(");");
        needsSemicolon = false;
    }

    @Override
    public void visit(SimpleMethodIdentifier node) throws ASTVisitorException {
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.print("() ");
    }

    @Override
    public void visit(SimpleConstructorIdentifier node) throws ASTVisitorException {
        ColourfulPrint("new ", Category.KEYWORD);
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.println("();");
        needsSemicolon = false;
    }

    @Override
    public void visit(ConstructorIdentifier node) throws ASTVisitorException {
        ColourfulPrint("new ", Category.KEYWORD);
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.print("(");

        boolean firstVisitDone = false;
        for (Expression e : node.getExpressionList()) {

            if (firstVisitDone)
                System.out.print(", ");

            e.accept(this);
            firstVisitDone = true;
        }

        if (needsSemicolon)
            System.out.println(");");
        needsSemicolon = false;
    }

    @Override
    public void visit(SimpleIdentifierExpression node) throws ASTVisitorException {
        needsSemicolon = true;
        ColourfulPrint(node.getIdentifier(), Category.IDENTIFIER);
    }

    @Override
    public void visit(SimpleReturnStatement node) throws ASTVisitorException {
        printTabs();
        ColourfulPrint("return", Category.KEYWORD);
        System.out.println(";");
    }

    @Override
    public void visit(SimpleStatement node) throws ASTVisitorException {
        printTabs();
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        Type exprType = node.getType();
        String finalType = beautifyType(exprType);
        ColourfulPrint(finalType, Category.TYPE);
        System.out.print(" ");
        ColourfulPrint(node.getIdentifier(), Category.IDENTIFIER);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        printTabs();
        if (node.isStatic())
            ColourfulPrint("static ", Category.KEYWORD);

        Type exprType = node.getReturnType();
        String finalType = beautifyType(exprType);
        ColourfulPrint(finalType, Category.TYPE);
        System.out.print(" ");
        ColourfulPrint(node.getIdentifier(), Category.METHOD);
        System.out.print("(");

        boolean firstVisitDone = false;
        for (ParameterDeclaration p : node.getParameterDeclarationList()) {

            if (firstVisitDone)
                System.out.print(", ");

            p.accept(this);
            firstVisitDone = true;
        }

        System.out.print(") ");

        if (node.getCompoundStatement() != null)
            node.getCompoundStatement().accept(this);
        else {
            System.out.println("{}");
        }
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        ColourfulPrint("class ", Category.KEYWORD);

        ColourfulPrint(node.getIdentifier(), Category.IDENTIFIER);
        System.out.println(" {");

        tabs++;
        for (FieldOrFunctionDefinition f : node.getFieldOrFunctionDefinitionList())
            f.accept(this);
        tabs--;

        System.out.println("}\n");
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        printTabs();

        if (node.isStatic())
            ColourfulPrint("static ", Category.KEYWORD);

        Type exprType = node.getType();
        String finalType = beautifyType(exprType);
        ColourfulPrint(finalType, Category.TYPE);
        System.out.print(" ");
        ColourfulPrint(node.getIdentifier(), Category.IDENTIFIER);
        System.out.println(";");
    }
}
