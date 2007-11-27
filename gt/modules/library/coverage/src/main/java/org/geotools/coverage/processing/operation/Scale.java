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
 * @todo Consider refactoring as a {@code OperationJAI} subclass. We could get ride of the
 *       {@code ScaledGridCoverage2D} class. The main feature to add is the
 *       copy of interpolation and border extender parameters to the hints.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 * @since 2.3
 *
 * @see javax.media.jai.operator.ScaleDescriptor
 */
public class Scale extends Operation2D {
	/**
	 * Serial number for cross-version compatibility.
	 */
	private static final long serialVersionUID = -3212656385631097713L;

	/**
	 * The X scale factor.
	 */
	public static final ParameterDescriptor xScale = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "xScale", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			1f,   // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The Y scale factor.
	 */
	public static final ParameterDescriptor yScale = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "yScale", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			1f,   // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The X translation.
	 */
	public static final ParameterDescriptor xTrans = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "xTrans", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			0f,   // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The Y translation.
	 */
	public static final ParameterDescriptor yTrans = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "yTrans", Float.class, // Value class
			// (mandatory)
			null, // Array of valid values
			0f,   // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * The interpolation method for resampling.
	 */
	public static final ParameterDescriptor Interpolation = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "Interpolation", Interpolation.class, // Value
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
			Citations.GEOTOOLS, "BorderExtender", BorderExtender.class, // Value
			// class
			// (mandatory)
			null, // Array of valid values
			javax.media.jai.BorderExtender.createInstance(javax.media.jai.BorderExtender.BORDER_ZERO), // Default
			// value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * Default constructor.
	 */
	public Scale() {
		super(new DefaultParameterDescriptorGroup(Citations.GEOTOOLS, "Scale",
				new ParameterDescriptor[] { SOURCE_0, xScale, yScale, xTrans,
						yTrans, Interpolation, BorderExtender }));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.coverage.processing.AbstractOperation#doOperation(org.opengis.parameter.ParameterValueGroup,
	 *      org.geotools.factory.Hints)
	 */
	public Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
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
