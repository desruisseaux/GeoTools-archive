package org.MaBaUtils.exceptions;

/** Thrown when it was tried to write onto a read-only object or object property.
 * @author  Matthias Basler
 */
public class NotImplementedException extends java.lang.RuntimeException{
    private static final long serialVersionUID = 3258689901401551673L;
    public NotImplementedException() {super();}
    public NotImplementedException(String msg) {super(msg);}
    public NotImplementedException(String msg, Class cl) {super(msg + " " + (cl.getName()));} //$NON-NLS-1$
    public NotImplementedException(Class cl) {super(cl.getName());}
    
}
