package org.hua.ast;

import java.util.List;

public class FunctionCallExpression extends Expression{

    private Expression expression;
    private String identifier;
    private List<Expression> expressionList;
    
    public FunctionCallExpression(Expression e, String id,List<Expression> el) {
        this.expression = e;
        this.identifier = id;
        this.expressionList = el;
    }

    public Expression getExpression() {
        return expression;
    }
    
    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
