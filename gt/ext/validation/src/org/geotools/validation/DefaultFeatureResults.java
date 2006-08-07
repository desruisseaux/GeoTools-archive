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
 *    Created on 18-Jun-2004
 */
package org.geotools.validation;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.Feature;


/**
 * @source $URL$
 */
public final class DefaultFeatureResults implements ValidationResults {
	Validation trial;
	public List error = new ArrayList();
	public List warning = new ArrayList();
	public void setValidation(Validation validation) {
		trial = validation;									
	}
	public void error(Feature feature, String message) {
		String where = feature != null ? feature.getID() : "all"; 
		error.add( where + ":"+ message );
		System.err.println( where + ":"+ message );
	}
	public void warning(Feature feature, String message) {
		String where = feature != null ? feature.getID() : "all";
		warning.add( where + ":"+ message );
		System.out.println( where + ":"+ message );
	}
}
