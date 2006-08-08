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

import java.awt.geom.NoninvertibleTransformException;

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;

import org.geotools.coverage.processing.CannotScaleException;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * This operation is simply a wrapper for the JAI scale operation which allows
 * me to arbitrarly scale and translate a rendered image.
 *  
 * @author Simone Giannecchini
 */
public class Scale extends Operation2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3212656385631097713L;

	/**
	 * The X scale factor.
	 */
	public static final ParameterDescriptor xScale = new DefaultParameterDescriptor(
			Citations.OGC, "xScale", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Float(1), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The Y scale factor.
	 */
	public static final ParameterDescriptor yScale = new DefaultParameterDescriptor(
			Citations.OGC, "yScale", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Float(1), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The X translation.
	 */
	public static final ParameterDescriptor xTrans = new DefaultParameterDescriptor(
			Citations.OGC, "xTrans", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Float(0), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The Y translation.
	 */
	public static final ParameterDescriptor yTrans = new DefaultParameterDescriptor(
			Citations.OGC, "yTrans", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Float(0), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The interpolation method for resampling.
	 */
	public static final ParameterDescriptor Interpolation = new DefaultParameterDescriptor(
			Citations.OGC, "Interpolation", Interpolation.class, // Value
			// class
			// (mandatory)
			null, // Array of valid values
			new InterpolationNearest(), // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The BorderExtender used wth high oerder interpolation methods.
	 */
	public static final ParameterDescriptor BorderExtender = new DefaultParameterDescriptor(
			Citations.OGC, "BorderExtender", BorderExtender.class, // Value
			// class
			// (mandatory)
			null, // Array of valid values
			BorderExtenderCopy.createInstance(BorderExtenderCopy.BORDER_ZERO), // Default
			// value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * Default constructor.
	 */
	public Scale() {
		super(new DefaultParameterDescriptorGroup(Citations.OGC, "Scale",
				new ParameterDescriptor[] { SOURCE_0, xScale, yScale, xTrans,
						yTrans, Interpolation, BorderExtender }));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.coverage.processing.AbstractOperation#doOperation(org.opengis.parameter.ParameterValueGroup,
	 *      org.geotools.factory.Hints)
	 */
	protected Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
		try {
			return ScaledGridCoverage2D
					.create(parameters,
							(hints instanceof Hints) ? (Hints) hints
									: new Hints(hints));
		} catch (NoninvertibleTransformException e) {
			throw new CannotScaleException(Errors
					.format(ErrorKeys.NONINVERTIBLE_SCALING_TRANSFORM), e);
		}
	}
}
