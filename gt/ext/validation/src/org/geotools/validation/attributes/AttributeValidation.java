/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on Jan 24, 2004
 */
package org.geotools.validation.attributes;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;


/**
 * PointCoveredByLineValidation purpose.
 * 
 * <p>
 * Completes the specified attribute comparison.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class AttributeValidation extends DefaultFeatureValidation {
    public static final int LESS_THAN = -1;
    public static final int EQUALITY = 0;
    public static final int GREATER_THAN = 1;
    private String attributeComparisonValue;
    private String attributeName;
    private int attributeComparisonType;

    /**
     * PointCoveredByLineValidation constructor.
     * 
     * <p>
     * Super
     * </p>
     */
    public AttributeValidation() {
        super();
    }

    /**
     * Completes the specified comparison.
     *
     * @param feature Feature to be Validated
     * @param type FeatureTypeInfo schema of feature
     * @param results coallate results information
     *
     * @return
     *
     * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature,
     *      org.geotools.feature.FeatureType,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Feature feature, FeatureType type,
        ValidationResults results) {
    	int surface = ((Integer) feature.getAttribute("surface")).intValue();
    	int speed = ((Integer) feature.getAttribute("speed")).intValue();
    	if( surface == 1 && speed > 110 ){
    		results.error( feature, "speed over 110");
    		return false;
    	}
    	if( surface == 2 && speed > 110 ){
    		results.error( feature, "speed over 70");
    		return false;
    	}
        return true;
    }

    /**
     * Access attributeComparisonType property.
     *
     * @return Returns the attributeComparisonType.
     */
    public int getAttributeComparisonType() {
        return attributeComparisonType;
    }

    /**
     * Set attributeComparisonType to attributeComparisonType.
     *
     * @param attributeComparisonType The attributeComparisonType to set.
     */
    public void setAttributeComparisonType(int attributeComparisonType) {
        this.attributeComparisonType = attributeComparisonType;
    }

    /**
     * Access attributeComparisonValue property.
     *
     * @return Returns the attributeComparisonValue.
     */
    public String getAttributeComparisonValue() {
        return attributeComparisonValue;
    }

    /**
     * Set attributeComparisonValue to attributeComparisonValue.
     *
     * @param attributeComparisonValue The attributeComparisonValue to set.
     */
    public void setAttributeComparisonValue(String attributeComparisonValue) {
        this.attributeComparisonValue = attributeComparisonValue;
    }

    /**
     * Access attributeName property.
     *
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Set attributeName to attributeName.
     *
     * @param attributeName The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
