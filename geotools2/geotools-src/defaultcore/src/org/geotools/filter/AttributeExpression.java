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

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.feature.FeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalFeatureException;

/**
 * Defines a complex filter (could also be called logical filter).
 *
 * This filter holds one or more filters together and relates
 * them logically in an internally defined manner.
 *
 * @version $Id: AttributeExpression.java,v 1.2 2002/10/23 16:52:40 ianturton Exp $
 * @author Rob Hranac, Vision for New York
 */
public class AttributeExpression extends DefaultExpression {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** Holds all sub filters of this filter. */
    protected String attributePath = new String();

    /** Holds all sub filters of this filter. */
    protected FeatureType schema = null;


    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param schema The schema for this attribute.
     */
    public AttributeExpression (FeatureType schema) {
        this.schema = schema;
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attributePath The initial (required) sub filter.
     */
    public AttributeExpression (FeatureType schema, String attributePath)
        throws IllegalFilterException {

        this.schema = schema;
        this.expressionType = ATTRIBUTE;
        setAttributePath(attributePath);
    }


    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attributePath The initial (required) sub filter.
     */
    public void setAttributePath(String attributePath)
        throws IllegalFilterException {
        LOGGER.entering("ExpressionAttribute", "setAttributePath", attributePath);
        LOGGER.finest("schema: " + schema + "\n\nattribute: " + attributePath);
        if (schema != null){
            if (schema.hasAttributeType(attributePath)) {
                this.attributePath = attributePath;
            } 
            else {
                throw new IllegalFilterException("Attribute does not conform to stated schema.");
            }
        } 
        else {
            this.attributePath = attributePath;
        }
    }
    
    public String getAttributePath(){
        return attributePath;
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param expressionType The final relation between all sub filters.
     */
    /*public void setExpressionType(short expressionType) {
        this.expressionType = expressionType;
        }*/

    /**
     * Gets the value of this attribute from the passed feature.
     *
     * @param feature Feature from which to extract attribute value.
     */
    public Object getValue(Feature feature) {

        Object tempAttribute = null;
        try {
            tempAttribute = feature.getAttribute(attributePath);
        }
        catch (IllegalFeatureException e) {            
            LOGGER.fine("Feature does not match declared schema: " + schema.toString());
        }
        return tempAttribute;
    }
        

    public String toString() {
        return attributePath;
    }
   

      /** 
     * Compares this filter to the specified object.  Returns true 
     * if the passed in object is the same as this expression.  Checks 
     * to make sure the expression types are the same as well as 
     * the attribute paths and schemas.
     *
     * @param obj - the object to compare this ExpressionAttribute against.
     * @return true if specified object is equal to this filter; false otherwise.
     */
    public boolean equals(Object obj) {
	if (obj.getClass() == this.getClass()){
	    AttributeExpression expAttr = (AttributeExpression)obj;
	    return (expAttr.getType() == this.expressionType &&
		    expAttr.getAttributePath().equals(this.attributePath) &&
		    expAttr.schema.toString().equals(this.schema.toString()));
		} else {
	    return false;
	}
    }
    
}
