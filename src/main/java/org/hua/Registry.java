package org.hua;

import org.hua.ast.ASTNode;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Type;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;

/**
 * Global registry (Singleton pattern)
 */
public class Registry {

    private ASTNode root;
    private Map<Type, SymTable<SymTableEntry>> classes;

    private Registry() {
        root = null;
        classes = new HashMap<Type, SymTable<SymTableEntry>>();
    }

    private static class SingletonHolder {

        static final Registry instance = new Registry();

    }

    public static Registry getInstance() {
        return SingletonHolder.instance;
    }

    public ASTNode getRoot() {
        return root;
    }

    void setRoot(ASTNode root) {
        this.root = root;
    }

    public Map<Type, SymTable<SymTableEntry>> getClasses() {
        return classes;
    }

    public void setClasses(Map<Type, SymTable<SymTableEntry>> classes) {
        this.classes = classes;
    }

}
