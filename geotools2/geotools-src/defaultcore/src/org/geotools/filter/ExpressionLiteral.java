/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.filter;

import com.vividsolutions.jts.geom.*;

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines an expression that holds a literal for return.
 *
 * @version $Id: ExpressionLiteral.java,v 1.4 2002/07/22 20:22:03 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
 */
public class ExpressionLiteral extends ExpressionDefault {


    /** Holds a reference to the literal. */
    protected Object literal = null;


    /**
     * Constructor with literal.
     */
    public ExpressionLiteral () {
    }


    /**
     * Constructor with literal.
     *
     * @param literal The literal to store inside this expression.
     * @throws IllegalFilterException This literal type is not in scope.
     */
    public ExpressionLiteral (Object literal)
        throws IllegalFilterException {
        
        this.setLiteral(literal);
    }


    /**
     * Returns the literal type.
     */
    public short getType() {
        return expressionType;
    }

    /**
     * Sets the literal.
     *
     * @param literal The literal to store inside this expression.
     * @throws IllegalFilterException This literal type is not in scope.
     */
    public void setLiteral(Object literal)
        throws IllegalFilterException {
        
        if (literal instanceof Double) {
            expressionType = LITERAL_DOUBLE;
        }
        else if (literal instanceof Integer) {
            expressionType = LITERAL_INTEGER;
        }
        else if (literal instanceof String) {
            expressionType = LITERAL_STRING;
        }
        else if (literal instanceof Geometry) {
            expressionType = LITERAL_GEOMETRY;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to add a literal with non-supported type " +
                 "(ie. not Double, Integer, String).");
        }

        this.literal = literal;
    }


    /**
     * Gets the value of this literal.
     *
     * @param feature Required by the interface but not used.
     * @throws IllegalArgumentException Feature does not match declared schema.
     */
    public Object getValue(Feature feature)
        throws IllegalArgumentException {

        return literal;
    }
        
    public String toString() {
        return literal.toString();        
    }
    
    /** Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
}
