package org.hua.ast;

import java.util.ArrayList;
import java.util.List;
import org.hua.symbol.LocalIndexPool;
import org.objectweb.asm.Type;

import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * Class with static helper methods for AST handling
 */
public class ASTUtils {

    private static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
    private static final String LOCAL_INDEX_POOL_PROPERTY = "LOCAL_INDEX_POOL_PROPERTY";
    private static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
    private static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    private static final String NEXT_LIST_PROPERTY = "NEXT_LIST_PROPERTY";
    private static final String BREAK_LIST_PROPERTY = "BREAK_LIST_PROPERTY";
    private static final String CONTINUE_LIST_PROPERTY = "CONTINUE_LIST_PROPERTY";
    private static final String TRUE_LIST_PROPERTY = "TRUE_LIST_PROPERTY";
    private static final String FALSE_LIST_PROPERTY = "FALSE_LIST_PROPERTY";

    private ASTUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getEnv(ASTNode node) {
        return (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getSafeEnv(ASTNode node)
            throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = (SymTable<SymTableEntry>) node
                .getProperty(SYMTABLE_PROPERTY);
        if (symTable == null) {
            ASTUtils.error(node, "Symbol table not found!");
        }
        return symTable;
    }

    public static void setEnv(ASTNode node, SymTable<SymTableEntry> env) {
        node.setProperty(SYMTABLE_PROPERTY, env);
    }

    public static void setLocalIndexPool(ASTNode node, LocalIndexPool pool) {
        node.setProperty(LOCAL_INDEX_POOL_PROPERTY, pool);
    }

    @SuppressWarnings("unchecked")
    public static LocalIndexPool getSafeLocalIndexPool(ASTNode node)
            throws ASTVisitorException {
        LocalIndexPool lip = (LocalIndexPool) node.getProperty(LOCAL_INDEX_POOL_PROPERTY);
        if (lip == null) {
            ASTUtils.error(node, "Local index pool not found!");
        }
        return lip;
    }

    public static boolean isBooleanExpression(Expression node) {
        Boolean b = (Boolean) node.getProperty(IS_BOOLEAN_EXPR_PROPERTY);
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void setBooleanExpression(Expression node, boolean value) {
        node.setProperty(IS_BOOLEAN_EXPR_PROPERTY, value);
    }

    public static Type getType(ASTNode node) {
        return (Type) node.getProperty(TYPE_PROPERTY);
    }

    public static Type getSafeType(ASTNode node) throws ASTVisitorException {
        Type type = (Type) node.getProperty(TYPE_PROPERTY);
        if (type == null) {
            ASTUtils.error(node, "Type not found!");
        }
        return type;
    }

    public static void setType(ASTNode node, Type type) {
        node.setProperty(TYPE_PROPERTY, type);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getTrueList(Expression node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(TRUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(TRUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setTrueList(Expression node, List<JumpInsnNode> list) {
        node.setProperty(TRUE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getFalseList(Expression node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(FALSE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(FALSE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setFalseList(Expression node, List<JumpInsnNode> list) {
        node.setProperty(FALSE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getNextList(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(NEXT_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(NEXT_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setNextList(Statement node, List<JumpInsnNode> list) {
        node.setProperty(NEXT_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getBreakList(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(BREAK_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(BREAK_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setBreakList(Statement node, List<JumpInsnNode> list) {
        node.setProperty(BREAK_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getContinueList(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(CONTINUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(CONTINUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setContinueList(Statement node, List<JumpInsnNode> list) {
        node.setProperty(CONTINUE_LIST_PROPERTY, list);
    }

    public static void error(ASTNode node, String message)
            throws ASTVisitorException {
        throw new ASTVisitorException(node.getLine() + ":" + node.getColumn()
                + ": " + message);
    }

}
