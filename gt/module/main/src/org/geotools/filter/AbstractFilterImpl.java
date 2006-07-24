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
 *    
 *    Created on 23 October 2002, 17:19
 */
package org.geotools.filter;


/**
 * Abstract filter implementation provides or and and methods for child filters
 * to use.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public abstract class AbstractFilterImpl
    extends org.geotools.filter.AbstractFilter {
   
	protected AbstractFilterImpl(FilterFactory factory) {
		super(factory);
	}

	/**
     * Default implementation for OR - should be sufficient for most filters.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     *
     * @return ORed filter.
     */
    public Filter or(Filter filter) {
        try {
        	return factory.createLogicFilter(this,filter,LOGIC_OR);
        } catch (IllegalFilterException ife) {
            return filter;
        }
    }

    /**
     * Default implementation for AND - should be sufficient for most filters.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     *
     * @return ANDed filter.
     */
    public Filter and(Filter filter) {
        try {
            return factory.createLogicFilter(this,filter,LOGIC_AND);
        } catch (IllegalFilterException ife) {
            return filter;
        }
    }

    /**
     * Default implementation for NOT - should be sufficient for most filters.
     *
     * @return NOTed filter.
     */
    public Filter not() {
        try {
            return factory.createLogicFilter(this,LOGIC_NOT);
        } catch (IllegalFilterException ife) {
            return this;
        }
    }
}
