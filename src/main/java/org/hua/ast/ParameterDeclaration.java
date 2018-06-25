package org.hua.ast;

import org.objectweb.asm.Type;

public class ParameterDeclaration extends ASTNode{

    private Type type;
    private String identifier;

    public ParameterDeclaration(Type t, String id) {
        this.type = t;
        this.identifier = id;
    }
    
    public Type getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
