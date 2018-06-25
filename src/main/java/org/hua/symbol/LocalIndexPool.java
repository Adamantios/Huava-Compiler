package org.hua.symbol;

import org.hua.Registry;
import org.hua.types.TypeUtils;
import org.objectweb.asm.Type;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helper class to maintain a pool of used-free local variables.
 */
public class LocalIndexPool {

    private final SortedSet<Integer> used;
    private int max;
    private int maxUsed;

    public LocalIndexPool() {
        this(Integer.MAX_VALUE);
    }

    private LocalIndexPool(int max) {
        this.used = new TreeSet<Integer>();
        this.max = max;
        this.maxUsed = 0;
    }

    public int getLocalIndex(Type type) {
        boolean classTypeFound = false;
        if (Registry.getInstance().getClasses().containsKey(type))
            classTypeFound = true;

        if (type.equals(Type.DOUBLE_TYPE))
            return getDoubleLocalIndex();
        else if (type.equals(Type.INT_TYPE)
                || type.equals(Type.FLOAT_TYPE)
                || type.equals(TypeUtils.STRING_TYPE)
                || classTypeFound)
            return getLocalIndex();
        else
            throw new IllegalArgumentException("Not supported type " + type);
    }

    public void freeLocalIndex(int i, Type type) {
        boolean classTypeFound = false;
        if (Registry.getInstance().getClasses().containsKey(type))
            classTypeFound = true;

        if (type.equals(Type.FLOAT_TYPE)) {
            freeDoubleLocalIndex(i);
        } else if (type.equals(Type.INT_TYPE) || type.equals(TypeUtils.STRING_TYPE) || classTypeFound) {
            freeLocalIndex(i);
        } else {
            throw new IllegalArgumentException("Not supported type " + type);
        }
    }

    public int getLocalIndex() {
        for (int i = 0; i < max; i++) {
            if (!used.contains(i)) {
                used.add(i);
                if (i > maxUsed) {
                    maxUsed = i;
                }
                return i;
            }
        }
        throw new RuntimeException("Pool cannot contain more temporaries.");
    }

    public void freeLocalIndex(int t) {
        used.remove(t);
    }

    private int getDoubleLocalIndex() {
        for (int i = 0; i < max; i++) {
            if (!used.contains(i) && !used.contains(i + 1)) {
                used.add(i);
                used.add(i + 1);
                if (i + 1 > maxUsed) {
                    maxUsed = i + 1;
                }
                return i;
            }
        }
        throw new RuntimeException("Pool cannot contain more temporaries.");
    }

    private void freeDoubleLocalIndex(int t) {
        used.remove(t);
        used.remove(t + 1);
    }

    public int getMaxLocals() {
        return maxUsed;
    }

}
