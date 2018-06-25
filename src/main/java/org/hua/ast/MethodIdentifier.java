package org.hua.ast;

import java.util.List;

public class MethodIdentifier extends Expression{

    private String identifier;
    private List<Expression> expressionList;
    
    public MethodIdentifier(String id,List<Expression> expressions) {
        this.identifier = id;
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
