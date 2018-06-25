package org.hua.ast;

import java.util.ArrayList;
import java.util.List;

public class CompUnit extends ASTNode {

    private List<ClassDefinition> classDefinition;

    public CompUnit() {
        classDefinition = new ArrayList<ClassDefinition>();
    }

    public CompUnit(List<ClassDefinition> classDefinition) {
        this.classDefinition = classDefinition;
    }

    public List<ClassDefinition> getClassDefinitionList() {
        return classDefinition;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
