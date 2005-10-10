/*
 * EnvironmentVariable.java
 *
 * Created on 07 December 2004, 16:27
 */

package org.geotools.filter;

import org.opengis.feature.Attribute;

/**
 *
 * @author James
 */
public interface EnvironmentVariable  extends Expression {
    
    /**
     * Gets the attribute value at the path held by this expression from the
     * feature.
     *
     * @param feature the feature to get this attribute from.
     *
     * @return the value of the attribute found by this expression.
     */
    Object getValue(Attribute att);
    
}
