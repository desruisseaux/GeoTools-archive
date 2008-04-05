/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.processfactory;

import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Parameter;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * A simple process showing how to interact with a couple of geometry literals.
 * 
 * @author Graham Davis
 */
public class IntersectsFactory implements ProcessFactory {

	public Process create(Map<String, Object> parameters)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public InternationalString getDescription() {
		return new SimpleInternationalString("Intersection between two literal geometry");
	}

	public Parameter[] getParameterInfo() {
	    return null;
	}
	
	public Parameter[] getResultInfo(Map<String, Object> parameters)
			throws IllegalArgumentException {
		return null;
	}

	public InternationalString getTitle() {
	    // please note that this is a title for display purposes only
	    // finding an specific implementation by name is not possible
	    //
	    return new SimpleInternationalString("Intersection");
	}

}
