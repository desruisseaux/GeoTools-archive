/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.coverage.processing.operation;

import org.geotools.coverage.processing.BaseScaleOperationJAI;


/**
 * This operation is simply a wrapper for the JAI SubsampleAverage operation which allows
 * me to arbitrarly scale a rendered image while smoothing it out.

 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @version $Id: SubsampleAverage.java 23157 2006-12-01 01:29:53Z desruisseaSubsampleAveragedCoverageator.SubsampleAverageDescriptor
 * @since 2.3
 * @see javax.media.jai.operator.SubsampleAverageDescriptor
 * @source $URL$
 */
public class SubsampleAverage extends BaseScaleOperationJAI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SubsampleAverage() {
		super("SubsampleAverage");
	}



}
