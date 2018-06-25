package org.hua.ast;

public class IdentifierExpression extends Expression {

    private Expression expression;
    private String identifier;

    public IdentifierExpression(Expression expression,String identifier) {
        this.expression = expression;
        this.identifier = identifier;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
