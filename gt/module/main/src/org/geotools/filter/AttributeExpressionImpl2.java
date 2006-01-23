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
package org.geotools.filter;

import java.util.logging.Logger;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;


/**
 * Defines a complex filter (could also be called logical filter). This filter
 * holds one or more filters together and relates them logically in an
 * internally defined manner.
 *
 * @author Rob Hranac, TOPP
 * @source $URL$
 * @version $Id$
 */
public class AttributeExpressionImpl2 extends DefaultExpression
    implements AttributeExpression {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** Holds all sub filters of this filter. */
    protected String attPath;

    /** Holds all sub filters of this filter. */
    protected AttributeType at = null;

    /**
     * Constructor with the schema for this attribute.
     *
     * @param schema The schema for this attribute.
     */
    protected AttributeExpressionImpl2(AttributeType at) {
        this.at = at;
        this.expressionType = ATTRIBUTE;
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attPath The initial (required) sub filter.
     *
     * @throws IllegalFilterException If the attribute path is not in the
     *         schema.
     */
    public void setAttributePath(String attPath) throws IllegalFilterException {
        
                throw new IllegalFilterException(
                    "Attribute: " +attPath+ " is not in stated schema "+".");
    }

    /**
     * Gets the path to the attribute to be evaluated by this expression.
     *
     * @return the path.
     */
    public String getAttributePath() {
        return attPath;
    }

    /**
     * Gets the value of this attribute from the passed feature.
     *
     * @param feature Feature from which to extract attribute value.
     *
     * @return DOCUMENT ME!
     */
    public Object getValue(Feature feature) {
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
            AttributeExpressionImpl2 expAttr = (AttributeExpressionImpl2) obj;

            boolean isEqual = (expAttr.getType() == this.expressionType);
            isEqual = (expAttr.attPath != null)
                ? (isEqual && expAttr.attPath.equals(this.attPath))
                : (isEqual && (this.attPath == null));

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
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}
