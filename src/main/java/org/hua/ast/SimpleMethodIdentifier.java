package org.hua.ast;

public class SimpleMethodIdentifier extends Expression{

    private String identifier;
    
    public SimpleMethodIdentifier(String id) {
        this.identifier = id;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
