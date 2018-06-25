package org.hua.types;

public class TypeException extends Exception {

    private static final long serialVersionUID = 1L;

    public TypeException() {
        super();
    }

    TypeException(String msg) {
        super(msg);
    }

    public TypeException(String msg, Throwable t) {
        super(msg, t);
    }

    public TypeException(Throwable t) {
        super(t);
    }

}
