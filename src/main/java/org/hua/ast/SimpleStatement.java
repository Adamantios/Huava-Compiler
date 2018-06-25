package org.hua.ast;

public class SimpleStatement extends Statement{

    private Expression expression;

    public SimpleStatement(Expression e) {
        this.expression = e;
    }

    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
