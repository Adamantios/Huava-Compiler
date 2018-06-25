package org.hua.ast;

public class FloatLiteralExpression extends Expression {

    private Float literal;

    public FloatLiteralExpression(Float literal) {
        this.literal = literal;
    }

    public Float getLiteral() {
        return literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
