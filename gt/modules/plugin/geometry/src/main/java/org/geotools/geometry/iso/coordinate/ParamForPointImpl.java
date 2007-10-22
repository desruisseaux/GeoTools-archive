/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
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

package org.geotools.geometry.iso.coordinate;

import java.util.ArrayList;

import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.GenericCurve;
import org.opengis.geometry.coordinate.ParamForPoint;


public class ParamForPointImpl extends ArrayList<Double> implements
		ParamForPoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1875728979337736219L;
	
	private GenericCurve genericCurve;

	/**
	 * @param genericCurve
	 * 
	 */
	public ParamForPointImpl(GenericCurve genericCurve) {
		this.genericCurve = genericCurve;
	}

	public double getDistance() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	public DirectPosition getPosition() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation		
		return null;
	}

}
