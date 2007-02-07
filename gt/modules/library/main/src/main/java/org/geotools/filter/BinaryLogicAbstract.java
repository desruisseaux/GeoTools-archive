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

import java.util.List;

import org.opengis.filter.BinaryLogicOperator;

/**
 * @author jdeolive
 */
public abstract class BinaryLogicAbstract extends AbstractFilter implements BinaryLogicOperator {
	protected List/*<Filter>*/ children;
	
	protected BinaryLogicAbstract(FilterFactory factory, List/*<Filter>*/ children ) {
		super(factory);
		this.children = children;
	}
	
	public List/*<Filter>*/ getChildren() {
		return children;
	}
	
	public void setChildren(List children) {
		this.children = children;
	}

	public Filter and(org.opengis.filter.Filter filter) {
		return factory.and((Filter)this,(Filter)filter);
	}

	public Filter or(org.opengis.filter.Filter filter) {
		return factory.or((Filter)this,(Filter)filter);
	}

	public Filter not() {
		return factory.not(this);
	}
}
