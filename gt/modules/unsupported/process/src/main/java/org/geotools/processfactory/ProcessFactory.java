/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package org.geotools.processfactory;

import java.util.Map;

import org.geotools.process.Process;
import org.geotools.processparameter.ProcessParameter;
import org.opengis.util.InternationalString;

/**
 * Used to describe the parameters needed for a Process, and for creating a Process to use.
 *
 * @author Graham Davis
 */
public interface ProcessFactory {

		public InternationalString getTitle();
		public InternationalString getDescription();
		public ProcessParameter[] getParameterInfo();
		
		public Process create(Map<String, Object> parameters) throws IllegalArgumentException;
		public ProcessParameter[] getResultInfo(Map<String, Object> parameters) throws IllegalArgumentException;
}
