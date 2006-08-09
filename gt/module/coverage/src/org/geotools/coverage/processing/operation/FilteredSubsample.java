/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * @author Simone Giannecchini
 *
 */
public class FilteredSubsample extends Operation2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = 652535074064952517L;

	/**
	 * 
	 */
	public static final ParameterDescriptor scaleX = new DefaultParameterDescriptor(
			Citations.OGC, "scaleX", Integer.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Integer(2), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	/**
	 * 
	 */
	public static final ParameterDescriptor scaleY = new DefaultParameterDescriptor(
			Citations.OGC, "scaleY", Integer.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Integer(2), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	
	/**
	 * 
	 */
	public static final ParameterDescriptor qsFilter = new DefaultParameterDescriptor(
			Citations.OGC, "qsFilterArray", float[].class, // Value class
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
			Citations.OGC, "Interpolation", Interpolation.class, // Value class
			// (mandatory)
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
			Citations.OGC, "BorderExtender", BorderExtender.class, // Value class
			// (mandatory)
			null, // Array of valid values
			BorderExtenderCopy.createInstance(BorderExtenderCopy.BORDER_ZERO), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional
	/**
	 * @param descriptor
	 */
	public FilteredSubsample() {
		super(new DefaultParameterDescriptorGroup(Citations.OGC,
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
