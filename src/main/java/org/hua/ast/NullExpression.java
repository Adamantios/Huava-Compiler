package org.hua.ast;

public class NullExpression extends Expression {

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
