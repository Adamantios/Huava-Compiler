package org.hua.ast;

public class SimpleFunctionCallExpression extends Expression{

    private Expression expression;
    private String identifier;

    public SimpleFunctionCallExpression(Expression expression,String identifier) {
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
