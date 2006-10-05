/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter;

import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.opengis.filter.expression.ExpressionVisitor;


/**
 * Defines a complex filter (could also be called logical filter). This filter
 * holds one or more filters together and relates them logically in an
 * internally defined manner.
 *
 * @author Rob Hranac, TOPP
 * @source $URL$
 * @version $Id$
 */
public class AttributeExpressionImpl extends DefaultExpression
    implements AttributeExpression {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** Holds all sub filters of this filter. */
    protected String attPath;

    /** Holds all sub filters of this filter. */
    protected FeatureType schema = null;

    /**
     * Constructor with the schema for this attribute.
     *
     * @param schema The schema for this attribute.
     */
    protected AttributeExpressionImpl(FeatureType schema) {
        this.schema = schema;
        this.expressionType = ATTRIBUTE;
    }

    /**
     * Constructor with schema and path to the attribute.
     *
     * @param schema The initial (required) sub filter.
     * @param attPath the xpath to the attribute.
     */
    protected AttributeExpressionImpl( String xpath ){
    	attPath = xpath;
    	schema = null;
    	this.expressionType = ATTRIBUTE;    	
    }    
    /**
     * Constructor with schema and path to the attribute.
     *
     * @param schema The initial (required) sub filter.
     * @param attPath the xpath to the attribute.
     *
     * @throws IllegalFilterException If the attribute path is not in the
     *         schema.
     */
    protected AttributeExpressionImpl(FeatureType schema, String attPath)
        throws IllegalFilterException {
        this.schema = schema;
        this.expressionType = ATTRIBUTE;
        setAttributePath(attPath);
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attPath The initial (required) sub filter.
     *
     * @throws IllegalFilterException If the attribute path is not in the
     *         schema.
     *         
     *  @deprecated use {@link #setPropertyName(String)}
     */
    public final void setAttributePath(String attPath) throws IllegalFilterException {
       setPropertyName(attPath);
    }

    /**
     * This method calls {@link #getPropertyName()}.
     * 
     * @deprecated use {@link #getPropertyName()}
     */
    public final String getAttributePath() {
      	return getPropertyName();
    }

    /**
     * Gets the path to the attribute to be evaluated by this expression.
     *
     * {@link org.opengis.filter.expression.PropertyName#getPropertyName()}
     */
   public String getPropertyName() {
		return attPath;
   }	
   
   public void setPropertyName(String attPath) {
	   LOGGER.entering("ExpressionAttribute", "setAttributePath", attPath);
       LOGGER.finest("schema: " + schema + "\n\nattribute: " + attPath);

       if (schema != null) {
           if (schema.hasAttributeType(attPath)) {
               this.attPath = attPath;
           } else {
        	   
        	   throw new IllegalFilterException(
                   "Attribute: " +attPath+ " is not in stated schema "+schema.getTypeName()+"."
               );	   
        	   
           }
       } else {
           this.attPath = attPath;
       }
    }	
    
   /**
      * Gets the value of this attribute from the passed feature.
      *
      * @param feature Feature from which to extract attribute value.
      */
    public Object evaluate(Feature feature) {
    	return feature.getAttribute(attPath);
    }
    
     /**
     * Return this expression as a string.
     *
     * @return String representation of this attribute expression.
     */
    public String toString() {
        return attPath;
    }

    /**
     * Compares this filter to the specified object.  Returns true  if the
     * passed in object is the same as this expression.  Checks  to make sure
     * the expression types are the same as well as  the attribute paths and
     * schemas.
     *
     * @param obj - the object to compare this ExpressionAttribute against.
     *
     * @return true if specified object is equal to this filter; else false
     */
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            AttributeExpressionImpl expAttr = (AttributeExpressionImpl) obj;

            boolean isEqual = (expAttr.getType() == this.expressionType);
            LOGGER.finest("expression type match:" + isEqual + "; in:"
                + expAttr.getType() + "; out:" + this.expressionType);
            isEqual = (expAttr.attPath != null)
                ? (isEqual && expAttr.attPath.equals(this.attPath))
                : (isEqual && (this.attPath == null));
            LOGGER.finest("attribute match:" + isEqual + "; in:"
                + expAttr.getAttributePath() + "; out:" + this.attPath);
            isEqual = (expAttr.schema != null)
                ? (isEqual && expAttr.schema.equals(this.schema))
                : (isEqual && (this.schema == null));
            LOGGER.finest("schema match:" + isEqual + "; in:" + expAttr.schema
                + "; out:" + this.schema);

            return isEqual;
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return a code to hash this object by.
     */
    public int hashCode() {
        int result = 17;
        result = (37 * result) + (attPath == null ? 0 : attPath.hashCode());
        result = (37 * result) + (schema == null ? 0 : schema.hashCode());
        return result;
    }

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the  parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
    public Object accept(ExpressionVisitor visitor, Object extraData) {
    	return visitor.visit(this,extraData);
    }
}
