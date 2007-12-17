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
 * This exception is produced when the cql input string has sintax errors.
 *
 * @author Mauricio Pazos - Axios Engineering
 * 
 * @version $Id$
 * @since 2.4
 */
public class CQLException extends ParseException {
    /** for interoperability */
    private static final long serialVersionUID = 8873756073711225699L;

    protected Throwable cause = null;
    private String cqlSource = null;

    /**
     * New instance of CQLException
     *
     * @param message   exception description
     * @param token     current token
     * @param cause     the cause
     * @param cqlSource string analized
     */
    public CQLException(String message, Token token, Throwable cause, String cqlSource) {
        super(message);
        
        assert message != null;

        super.currentToken = token;

        this.cause = cause;
        this.cqlSource = cqlSource;
    }

    /**
     * New instance of CQLException
     * 
     * @param message   exception description
     * @param token     current token
     * @param cqlSource analized string
     */
    public CQLException(final String message, final Token token, final String cqlSource) {
        this(message, token, null, cqlSource);
    }

    /**
     * New instance of CQLException
     *
     * @param message   exception description
     */
    public CQLException(String message) {
        this(message, null, null, null);
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

    /**
     * Returns the syntax error presents in the last sequence of characters analyzed.
     * 
     * @return the syntax error
     */
    public String getSyntaxError() {

        // builds two lines the first has the source string, the second has
        // the pointer to syntax error.
        
        // First Line
        StringBuffer msg = new StringBuffer(this.cqlSource);
        msg.append('\n');

        // Second Line
        // searches the last token recognized 
        Token curToken = this.currentToken;

        while (curToken.next != null)
            curToken = curToken.next;

        // add the pointer to error
        int column = curToken.beginColumn - 1;

        for (int i = 0; i < column; i++) {
            msg.append(' ');
        }

        msg.append('^').append('\n');
        msg.append(this.getMessage());

        return msg.toString();
    }
}
