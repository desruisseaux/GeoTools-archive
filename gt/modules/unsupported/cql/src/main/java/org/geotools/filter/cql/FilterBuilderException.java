/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
 *    Created on October 15, 2003, 1:57 PM
 */

package org.geotools.filter.cql;


/**
 *
 * @author  Ian Schneider
 * @source $URL: http://gtsvn.refractions.net/geotools/trunk/gt/module/main/src/org/geotools/filter/parser/FilterBuilderException.java $
 */
public class FilterBuilderException extends ParseException {
    
    /**
     * generated serial version uid 
     */
    private static final long serialVersionUID = -8027243686579409436L;
    
    Throwable cause;
    
    public FilterBuilderException(String message) {
        this(message,null,null);
    }

    public FilterBuilderException(String message,Token token) {
        this(message,token,null);
    }
    
    public FilterBuilderException(String message,Token token,Throwable cause) {
        super(message);
        
        this.currentToken = token;
        this.cause = cause;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getMessage() {
        if (currentToken == null) return super.getMessage();
        
        return super.getMessage() + ", Current Token : " + currentToken.image;
    }
}
