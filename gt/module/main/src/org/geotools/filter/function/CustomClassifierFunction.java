/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.function;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.Feature;

/**
 * This is a sort of mock-classifier.  It contains pre-defined ranges/explicit values for the classification function, and masquerades as a hybrid of the Explicit and Ranged classifiers. 
 * 
 * @author Cory Horner, Refractions Research Inc.
 */
public class CustomClassifierFunction extends ClassificationFunction {
	List explicit = new ArrayList();
	List min = new ArrayList();
	List max = new ArrayList();
	
	public String getName() {
        return "Custom";
	}

	public void setRangedValues(int index, Object min, Object max) {
		this.min.add(index, min);
		this.max.add(index, max);
	}
	
	public void setExplicitValues(int index, Object value) {
		this.explicit.add(index, value);
	}
	
	public boolean hasRanged(int index) {
		try {
			if (min.get(index) != null) 
				if (max.get(index) != null)
					return true;
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}
	
	public boolean hasExplicit(int index) {
		try {
			if (explicit.get(index) != null)
				return true;
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}
	
	public Object evaluate(Feature feature) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Returns the explicit value(s), or null if not defined.
	 */
	public Object getValue(int index) {
		return explicit.get(index);
	}
	
	/**
	 * Returns the ranged value minimum, or null if not defined.
	 * @param index
	 */
	public Object getMin(int index) {
		return min.get(index);
	}

	/**
	 * Returns the ranged value maximum, or null if not defined.
	 * @param index
	 */
	public Object getMax(int index) {
		return max.get(index);
	}
}
