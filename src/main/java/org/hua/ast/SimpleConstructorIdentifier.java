package org.hua.ast;

public class SimpleConstructorIdentifier extends Expression{

    private String identifier;
    
    public SimpleConstructorIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
