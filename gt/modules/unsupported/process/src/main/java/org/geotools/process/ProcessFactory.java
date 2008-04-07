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
package org.geotools.process;

import java.util.Map;

import org.opengis.util.InternationalString;

/**
 * Used to describe the parameters needed for a Process, and for creating a Process to use.
 *
 * @author Graham Davis
 */
public interface ProcessFactory {
    /** Human readable title suitable for display.
     * <p>
     * Please note that this title is *not* stable across locale; if you want
     * to remember a ProcessFactory between runs please use the classname
     * (as we are not providing a name or uri)
     */
	public InternationalString getTitle();
	
	/**
	 * Human readable description of this process.
	 * @return
	 */
	public InternationalString getDescription();
	
	/**
	 * Description of the Map parameter to use when executing.
	 * @return
	 */
	public Map<String,Parameter<?>> getParameterInfo();
	
	public Process create(Map<String, Object> parameters) throws IllegalArgumentException;
	
	public Map<String,Parameter<?>> getResultInfo(Map<String, Object> parameters) throws IllegalArgumentException;
}