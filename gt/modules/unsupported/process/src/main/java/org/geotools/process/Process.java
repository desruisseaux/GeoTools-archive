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
 * @author gdavis
 */
public interface Process {
	/**
	 * Execute this process with the provided interfaces.
	 * NOTE:  This method should only ever be called once
	 * @param monitor listener for handling the progress of the process
	 * @param monitor
	 * @return Map of results (@see factory.getResultParameters for details), or null if canceled
	 */
	public void process(ProgressListener monitor);
	
	/**
	 * ProcessFactory that created this process (useful if you want to check the process title etc..).
	 * 
	 * @return ProcessFactory that created this process.
	 */
	public ProcessFactory getFactory();
	
	/**
	 * Set the input parameters for this process
	 * @param input map of inputs
	 */
	public void setInput(Map<String,Object> input);
	
	/**
	 * Get the output map of results.  User can monitor when a process is complete and
	 * the results are ready through the ProgressListener passed to the process() method.
	 * @param input Map for output
	 */
	public Map<String,Object> getResult();	
	
}
