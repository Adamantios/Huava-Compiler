package org.hua.ast;

public class ReturnStatement extends Statement {
    
    private Expression expression;
    
    public ReturnStatement(Expression e) {
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
