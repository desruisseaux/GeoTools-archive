/*
 *    GeoTools - The Open Source Java GIS Toolkit
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

import org.opengis.util.ProgressListener;

/**
 * Used to process inputs and is reported using a ProgressListener.
 * Results are available after being run.
 *
 * @author Graham Davis
 */
public interface Process {
	/**
	 * 
	 * @param monitor
	 * @return Map of results (see factory getResultParameters for details), or null if canceled
	 */
	public Map<String,Object> process(ProgressListener monitor);
	public ProcessFactory getFactory();
}
