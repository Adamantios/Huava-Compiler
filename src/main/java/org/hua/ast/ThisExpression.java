package org.hua.ast;

public class ThisExpression extends Expression {

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
