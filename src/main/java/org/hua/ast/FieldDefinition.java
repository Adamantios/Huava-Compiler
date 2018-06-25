package org.hua.ast;

import org.objectweb.asm.Type;

public class FieldDefinition extends FieldOrFunctionDefinition {
    
    private boolean isStatic;
    private Type type;
    private String identifier;

    public FieldDefinition(boolean isStatic, Type type, String id) {
        this.isStatic = isStatic;
        this.type = type;
        this.identifier = id;
    }

    public boolean isStatic() {
        return isStatic;
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
