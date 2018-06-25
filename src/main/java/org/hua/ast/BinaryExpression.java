package org.hua.ast;

public class BinaryExpression extends Expression {

    private Operator operator;
    private Expression expression1;
    private Expression expression2;

    public BinaryExpression(Operator operator, Expression expression) {
        this.operator = operator;
        this.expression1 = expression;
    }
    
    public BinaryExpression(Operator operator, Expression expression1, Expression expression2) {
        this.operator = operator;
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getExpression1() {
        return expression1;
    }

    public Expression getExpression2() {
        return expression2;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
