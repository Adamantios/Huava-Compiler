package org.hua.ast;

import java.util.List;

public class ConstructorIdentifier extends Expression{

    private String identifier;
    private List<Expression> expressionList;
    
    public ConstructorIdentifier(String identifier,List<Expression> expressions) {
        this.identifier = identifier;
        this.expressionList = expressions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
