package org.hua.ast;

/**
 * Abstract syntax tree visitor.
 */
public interface ASTVisitor {

    void visit(CompUnit node) throws ASTVisitorException;

    void visit(AssignmentStatement node) throws ASTVisitorException;

    void visit(WriteStatement node) throws ASTVisitorException;

    void visit(CompoundStatement node) throws ASTVisitorException;

    void visit(BinaryExpression node) throws ASTVisitorException;

    void visit(UnaryExpression node) throws ASTVisitorException;

    void visit(IdentifierExpression node) throws ASTVisitorException;

    void visit(FloatLiteralExpression node) throws ASTVisitorException;

    void visit(IntegerLiteralExpression node) throws ASTVisitorException;

    void visit(StringLiteralExpression node) throws ASTVisitorException;

    void visit(ParenthesisExpression node) throws ASTVisitorException;

    void visit(WhileStatement node) throws ASTVisitorException;
    
    void visit(IfStatement node) throws ASTVisitorException;
    
    void visit(IfElseStatement node) throws ASTVisitorException;
    
    void visit(BreakStatement node) throws ASTVisitorException;
    
    void visit(ContinueStatement node) throws ASTVisitorException;
    
    void visit(ReturnStatement node) throws ASTVisitorException;
    
    void visit(ThisExpression node) throws ASTVisitorException; 
    
    void visit(NullExpression node) throws ASTVisitorException; 
    
    void visit(FunctionCallExpression node) throws ASTVisitorException;
    
    void visit(FieldDefinition node) throws ASTVisitorException;
    
    void visit(StorageSpecifier node) throws ASTVisitorException;
    
    void visit(SimpleFunctionCallExpression node) throws ASTVisitorException;
    
    void visit(MethodIdentifier node) throws ASTVisitorException;
    
    void visit(SimpleMethodIdentifier node) throws ASTVisitorException;
    
    void visit(SimpleConstructorIdentifier node) throws ASTVisitorException;

    void visit(ConstructorIdentifier node) throws ASTVisitorException;
    
    void visit(SimpleIdentifierExpression node) throws ASTVisitorException;
    
    void visit(SimpleReturnStatement node) throws ASTVisitorException;
            
    void visit(SimpleStatement node) throws ASTVisitorException;
    
    void visit(ParameterDeclaration node) throws ASTVisitorException;
    
    void visit(FunctionDefinition node) throws ASTVisitorException;
    
    void visit(ClassDefinition node) throws ASTVisitorException;
    
}
