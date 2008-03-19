/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind;

import java.io.PrintWriter;

public class JAXBException extends Exception {
    private String errorCode;
    private Throwable linkedException;
    static final long serialVersionUID = -5621384651494307979L;
    public JAXBException(String message) { this( message, null, null ); }
    public JAXBException(String message, String errorCode) { this( message, errorCode, null ); }
    public JAXBException(Throwable exception) { this( null, null, exception ); }
    public JAXBException(String message, Throwable exception) { this( message, null, exception ); }
    public JAXBException(String message, String errorCode, Throwable exception) {
        super( message );
        this.errorCode = errorCode;
        this.linkedException = exception;
    }
    public String getErrorCode() { return this.errorCode; }
    public Throwable getLinkedException() { return linkedException; }
    public synchronized void setLinkedException( Throwable exception ) { this.linkedException = exception; }
    public String toString() {
        return linkedException == null ? 
            super.toString() :
            super.toString() + "\n - with linked exception:\n[" +
                                linkedException.toString()+ "]";
    }
    public void printStackTrace( java.io.PrintStream s ) { super.printStackTrace(s); }
    public void printStackTrace() { super.printStackTrace(); }
    public void printStackTrace(PrintWriter s) { super.printStackTrace(s); }
    @Override
    public Throwable getCause() { return linkedException; }
}
