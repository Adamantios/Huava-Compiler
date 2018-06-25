package org.hua.ast;

public class IfElseStatement extends Statement {

    private Expression expression;
    private Statement statement1;
    private Statement statement2;

    public IfElseStatement(Expression expression, Statement statement1, Statement statement2) {
        this.expression = expression;
        this.statement1 = statement1;
        this.statement2 = statement2;
    }

    public Statement getStatement1() {
        return statement1;
    }

    public Statement getStatement2() {
        return statement2;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
