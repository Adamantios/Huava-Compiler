package org.hua.ast;

import java.util.List;

public class ClassDefinition extends ASTNode {

    private String identifier;
    private List<FieldOrFunctionDefinition> fieldOrFunctionDefinitionList;

    public ClassDefinition(String id, List<FieldOrFunctionDefinition> l) {
        this.identifier = id;
        this.fieldOrFunctionDefinitionList = l;
    }

    public List<FieldOrFunctionDefinition> getFieldOrFunctionDefinitionList() {
        return fieldOrFunctionDefinitionList;
    }

    public String getIdentifier() {
        return identifier;
    }
   
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
