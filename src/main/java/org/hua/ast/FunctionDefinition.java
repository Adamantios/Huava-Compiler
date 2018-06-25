package org.hua.ast;

import org.objectweb.asm.Type;

import java.util.List;

public class FunctionDefinition extends FieldOrFunctionDefinition{
    
    private boolean staticFlag;
    private String identifier;
    private Type returnType;
    private List<ParameterDeclaration> parameterDeclarationList;
    private CompoundStatement compoundStatement;

    public FunctionDefinition(boolean staticFlag, String id, Type returnType, List<ParameterDeclaration> pl, CompoundStatement cs) {
        this.staticFlag = staticFlag;
        this.identifier = id;
        this.returnType = returnType;
        this.parameterDeclarationList = pl;
        this.compoundStatement = cs;
    }

    public boolean isStatic() {
        return staticFlag;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<ParameterDeclaration> getParameterDeclarationList() {
        return parameterDeclarationList;
    }

    public CompoundStatement getCompoundStatement() {
        return compoundStatement;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
