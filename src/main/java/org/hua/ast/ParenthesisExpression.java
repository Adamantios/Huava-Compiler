package org.hua.ast;

public class ParenthesisExpression extends Expression {

    private Expression expression;

    public ParenthesisExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
