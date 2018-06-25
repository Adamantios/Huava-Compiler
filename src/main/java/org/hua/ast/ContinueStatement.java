package org.hua.ast;

public class ContinueStatement extends Statement {

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
