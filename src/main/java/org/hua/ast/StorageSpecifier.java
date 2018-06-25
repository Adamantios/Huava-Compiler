package org.hua.ast;

public class StorageSpecifier extends ASTNode {

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}