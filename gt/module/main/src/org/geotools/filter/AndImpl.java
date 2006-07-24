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
package org.geotools.filter;

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.opengis.filter.And;
import org.opengis.filter.FilterVisitor;

/**
 * Direct implementation of And filter.
 *
 * @author jdeolive
 */
public class AndImpl extends LogicFilterImpl implements And {
	
	protected AndImpl(FilterFactory factory, List/*<Filter>*/ children) {
		super(factory, children );
		
		//backwards compatability with old type system
		this.filterType = LOGIC_AND;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Filter filter = (Filter)itr.next();
			if( !filter.evaluate( feature )) return false;
		}
		return true;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
}

