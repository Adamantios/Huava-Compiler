package org.hua.ast;

import java.util.ArrayList;
import java.util.List;

public class CompoundStatement extends Statement {

    private List<Statement> statementList;

    public CompoundStatement() {
        this.statementList = new ArrayList<Statement>();
    }

    public CompoundStatement(List<Statement> statements) {
        this.statementList = statements;
    }

    public List<Statement> getStatementList() {
        return statementList;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
