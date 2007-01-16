package org.geotools.referencing.operation.builder;

import org.opengis.referencing.FactoryException;
/**
 * Thrown when a required operation can't be performed because some information is missing or isn't set up properly.
 * @author jezekjan
 *
 */
public class MissingInfoException extends FactoryException {

    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3128525157353302290L;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param  message The cause for this exception. The cause is saved
     *         for later retrieval by the {@link #getCause()} method.
     */
    public MissingInfoException(String message) {
        super(message);
    }

/**
     * Constructs an exception with no detail message.
     */
    public MissingInfoException() {
        super();
    }
}

