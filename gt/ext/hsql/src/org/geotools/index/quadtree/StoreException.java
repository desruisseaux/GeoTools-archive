/*
 * Created on 19-ago-2004
 */
package org.geotools.index.quadtree;


/**
 * @author Tommaso Nolli
 */
public class StoreException extends Exception {

    /**
     * 
     */
    public StoreException() {
        super();
    }

    /**
     * @param message
     */
    public StoreException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public StoreException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
