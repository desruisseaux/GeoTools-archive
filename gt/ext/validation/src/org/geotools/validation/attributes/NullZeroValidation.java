/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004 TOPP - www.openplans.org
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
 */
package org.geotools.validation.attributes;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;


/**
 * NullZeroFeatureValidation purpose.
 * 
 * <p>
 * Description of NullZeroFeatureValidation ...
 * </p>
 * 
 * <p>
 * Capabilities:
 * 
 * <ul>
 * <li>
 * Tests for null/0 atribute values.
 * </li>
 * </ul>
 * 
 * Example Use:
 * <pre><code>
 * NullZeroFeatureValidation x = new NullZeroFeatureValidation(...);
 * </code></pre>
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class NullZeroValidation extends DefaultFeatureValidation {
	/** XPATH expression for attribtue */
    private String attribute;

    public NullZeroValidation() {
        super();
    }

    /**
     * Implement validate.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param feature Provides the attributes to test.
     * @param type not used.
     * @param results a reference for returning error codes.
     *
     * @return false when null or 0 values are found in the attribute.
     *
     * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature,
     *      org.geotools.feature.FeatureType,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Feature feature, FeatureType type,
        ValidationResults results) { // throws Exception {

    	//if attribute not set, just pass
    	if (attribute == null)
    		return true;
    	
        Object obj = feature.getAttribute(attribute);

        if (obj == null) {
            results.error(feature, attribute + " is Empty");

            return false;
        }

        if (obj instanceof Number) {
            Number number = (Number) obj;

            if (number.intValue() == 0) {
                results.error(feature, attribute + " is Zero");

                return false;
            }
        }

		if (obj instanceof String) {
			String string = (String) obj;

			if ("".equals(string.trim())) {
				results.error(feature, attribute + " is \"\"");

				return false;
			}
		}

        return true;
    }

    /**
     * Implement getPriority.
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Implementation of getTypeNames.
     *
     * @return Array of typeNames, or empty array for all, null for disabled
     *
     * @see org.geotools.validation.Validation#getTypeRefs()
     */
    public String[] getTypeRefs() {
        if (getTypeRef() == null) {
            return null;
        }

        if (getTypeRef().equals("*")) {
            return ALL;
        }

        return new String[] { getTypeRef(), };
    }

    /**
     * Access attributeName property.
     *
     * @return the path being stored for validation
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * set AttributeName to xpath expression.
     *
     * @param xpath A String
     */
    public void setAttribute(String xpath) {
        attribute = xpath;
    }
}
