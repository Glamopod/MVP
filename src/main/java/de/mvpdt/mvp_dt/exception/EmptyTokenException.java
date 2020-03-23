package de.mvpdt.mvp_dt.exception;

import java.io.Serializable;

public class EmptyTokenException extends Exception implements Serializable {
    private static final long serialVersionUID = 9122648889150308889L;
    public EmptyTokenException() { super(); }
    public EmptyTokenException(final String message) { super(message); }
    public EmptyTokenException(final String message, final Throwable cause) { super(message, cause); }
}
