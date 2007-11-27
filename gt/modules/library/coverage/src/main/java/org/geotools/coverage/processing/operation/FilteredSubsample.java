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

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;

import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * TODO: Need documentation
 *
 * @todo Consider refactoring as a {@code OperationJAI} subclass. We could get ride of the
 *       {@code FilteredSubsampledGridCoverage2D} class. The main feature to add is the
 *       copy of interpolation and border extender parameters to the hints.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 * @since 2.3
 *
 * @see javax.media.jai.operator.FilteredSubsampleDescriptor
 */
public class FilteredSubsample extends Operation2D {
	/**
	 * Serial number for cross-version compatibility.
	 */
	private static final long serialVersionUID = 652535074064952517L;

	/**
	 * 
	 */
	public static final ParameterDescriptor scaleX = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "scaleX", Integer.class, // Value class
			// (mandatory)
			null, // Array of valid values
			2,    // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	/**
	 * 
	 */
	public static final ParameterDescriptor scaleY = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "scaleY", Integer.class, // Value class
			// (mandatory)
			null, // Array of valid values
			2,    // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	/**
	 * 
	 */
	public static final ParameterDescriptor qsFilter = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "qsFilterArray", float[].class, // Value class
			// (mandatory)
			null, // Array of valid values
			new float[]{1}, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	

	
	/**
	 * 
	 */
	public static final ParameterDescriptor Interpolation = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "Interpolation", Interpolation.class, // Value class (mandatory)
			null, // Array of valid values
			new InterpolationNearest(), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	
	/**
	 * 
	 */
	public static final ParameterDescriptor BorderExtender = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "BorderExtender", BorderExtender.class, // Value class (mandatory)
			null, // Array of valid values
			BorderExtenderCopy.createInstance(BorderExtenderCopy.BORDER_COPY), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * 
	 */
	public FilteredSubsample() {
		super(new DefaultParameterDescriptorGroup(Citations.GEOTOOLS,
				"FilteredSubsample", new ParameterDescriptor[] { SOURCE_0,
				scaleX ,scaleY,qsFilter,Interpolation,BorderExtender}));
	}

	/* (non-Javadoc)
	 * @see org.geotools.coverage.processing.AbstractOperation#doOperation(org.opengis.parameter.ParameterValueGroup, org.geotools.factory.Hints)
	 */
	public Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
		return FilteredSubsampledGridCoverage2D
		.create(parameters,
				(hints instanceof Hints) ? (Hints) hints
						: new Hints(hints));
	}
}
