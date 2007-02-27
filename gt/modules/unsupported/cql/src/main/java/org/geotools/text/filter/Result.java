/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.text.filter;

import org.geotools.text.filter.Token;

/**
 * Maintains the result of building process. 
 * 
 * @since 2.4
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL$
 */
final class Result {

    private int nodeType = 0;

    private Object built = null;

    private Token token = null;

    Result(Object built, Token token, int nodeType) {

        this.built = built;
        this.token = token;
        this.nodeType = nodeType;
    }

    public String toString(){
        assert this.token != null;
        
        return "Result [TOKEN("+ this.token.toString()+");"+ "BUILT("+ built +"); NODE_TYPE("+ new Integer(nodeType)+") ]"; 
        
    }
    public final Object getBuilt() {
        return this.built;
    }

    public final int getNodeType() {
        return this.nodeType;
    }

    public final Token getToken() {
        return this.token;
    }
}
