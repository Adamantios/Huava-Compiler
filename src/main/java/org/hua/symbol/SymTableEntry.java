package org.hua.symbol;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

public class SymTableEntry {

    private String identifier;
    private Type type;
    private Integer index;
    private boolean isStatic;
    private boolean definedInStaticFunction;
    private List<Type> parameterTypes;

    public SymTableEntry(String id, Integer index) {
        this.identifier = id;
        this.index = index;
        this.parameterTypes = new ArrayList<Type>();
    }

    public SymTableEntry(String id, Type type, Integer index) {
        this.identifier = id;
        this.type = type;
        this.index = index;
        this.parameterTypes = new ArrayList<Type>();
    }

    public SymTableEntry(String id, Type type, Integer index, boolean isStatic) {
        this.identifier = id;
        this.type = type;
        this.index = index;
        this.isStatic = isStatic;
        this.parameterTypes = new ArrayList<Type>();
    }

    public Integer getIndex() {
        return index;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isDefinedInStaticFunction() {
        return definedInStaticFunction;
    }

    public void setDefinedInStaticFunction(boolean definedInStaticFunction) {
        this.definedInStaticFunction = definedInStaticFunction;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<Type> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
        hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 41 * hash + (this.isStatic ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SymTableEntry other = (SymTableEntry) obj;
        return (this.identifier == null) ? other.identifier == null : this.identifier.equals(other.identifier)
                && !(this.type != other.type
                && (this.type == null || !this.type.equals(other.type)))
                && this.isStatic == other.isStatic;
    }

}
