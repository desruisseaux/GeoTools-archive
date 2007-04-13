/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.filter.text.cql2;

import org.geotools.filter.text.cql2.ParseException;
import org.geotools.filter.text.cql2.Token;


/**
 * This exception is produced when the cql input string has sintax errors
 *
 * @since 2.4
 * @author Mauricio Pazos - Axios Engineering
 * 
 * @version $Id$
 *
 */
public class CQLException extends ParseException {
    /** for interoperability */
    private static final long serialVersionUID = 8873756073711225699L;
    protected Throwable cause;

    /**
     * New instance of CQLException
     *
     * @param message
     * @param token
     * @param cause
     */
    public CQLException(String message, Token token, Throwable cause) {
        this.currentToken = token;
        this.cause = cause;
    }

    /**
     * New instance of CQLException
     *
     * @param message
     */
    public CQLException(String message) {
        this(message, null, null);
    }

    /**
     * New instance of CQLException
     *
     * @param message
     * @param token
     */
    public CQLException(String message, Token token) {
        this(message, token, null);
    }

    /**
     * returns the cause
     *
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Returns the exception message
     *
     * @return a message
     */
    public String getMessage() {
        if (currentToken == null) {
            return this.getMessage();
        }

        return super.getMessage() + ", Current Token : " + currentToken.image;
    }
}
