/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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

import java.awt.Shape;
import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.HistogramDescriptor;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractStatisticsOperationJAI;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.parameter.ImagingParameterDescriptorsDecorator;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.InternationalString;

/**
 * * This operation simply wraps JAI Extrema operations described by
 * {@link HistogramDescriptor} inside a GeoTools operation in order to make it
 * spatial-aware.
 * 
 * <p>
 * For the moment this is a very simple wrap. Plans on the 2.4 and successive
 * versions of this operation are to add the ability to to use spatial ROIs and
 * to specific Spatial subsampling. As of now, ROI has to be a Java2D
 * {@link Shape} subclass and the parameters to control x and y subsamplings got
 * to be Integer, which means pixel-aware.
 * 
 * <p>
 * For more information on how the underlying {@link JAI} operators works you
 * can have a look here: <a
 * href="http://download.java.net/media/jai/javadoc/1.1.3/jai-apidocs/javax/media/jai/operator/HistogramDescriptor.html">HistogramDescriptor</a>
 * <a
 * href="http://download.java.net/media/jai/javadoc/1.1.3/jai-apidocs/javax/media/jai/Histogram.html>Histogram</a>
 * 
 * <p>
 * <strong>How to use this operation</strong> Here is a very simple example on
 * how to use this operation in order to get the
 * {@link javax.media.jai.Histogram} of the source coverage.
 * 
 * <code>
 * final OperationJAI op=new OperationJAI("Histogram");
 * ParameterValueGroup params = op.getParameters();
 * params.parameter("Source").setValue(coverage);
 * coverage=(GridCoverage2D) op.doOperation(params,null);
 * System.out.println(((double[])coverage.getProperty("histogram")));
 * </code>
 * 
 * @author Simone Giannecchini
 * @since 2.4
 * @see javax.media.jai.Histogram
 * 
 */
public class Histogram extends AbstractStatisticsOperationJAI {

	public Histogram(OperationDescriptor operationDescriptor, ImagingParameterDescriptorsDecorator decorator) {
		super(operationDescriptor, decorator);
	}

	public Histogram(OperationDescriptor operation) {
		super(operation);
	}

	public Histogram(String name) {
		super(name);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4256576399698278701L;

	/**
	 * {@link String} key for getting the {@link javax.media.jai.Histogram}
	 * object.
	 */
	public final static String GT_SYNTHETIC_PROPERTY_HISTOGRAM = "histogram";

	/**
	 * Default constructor for the {@link Histogram} operation.
	 * 
	 * @throws OperationNotFoundException
	 */
	public Histogram() throws OperationNotFoundException {
		super(getOperationDescriptor("Histogram"), prepareParams("Histogram"));
	}

	/**
	 * This operation MUST be performed on the geophysics data for this
	 * {@link GridCoverage2D}.
	 * 
	 * @param parameters
	 *            {@link ParameterValueGroup} that describes this operation
	 * @return always true.
	 */
	protected boolean computeOnGeophysicsValues(ParameterValueGroup parameters) {
		return true;
	}

	/**
	 * Prepare the {@link javax.media.jai.Histogram} property for this extream
	 * operation.
	 * 
	 * <p>
	 * See <a
	 * href="http://download.java.net/media/jai/javadoc/1.1.3/jai-apidocs/javax/media/jai/operator/ExtremaDescriptor.html">ExtremaDescriptor</a>
	 * for more info.
	 * 
	 * @see OperationJAI#getProperties(RenderedImage, CoordinateReferenceSystem,
	 *      InternationalString, MathTransform, GridCoverage2D[],
	 *      org.geotools.coverage.processing.OperationJAI.Parameters),
	 */
	protected Map getProperties(RenderedImage data,
			CoordinateReferenceSystem crs, InternationalString name,
			MathTransform toCRS, GridCoverage2D[] sources, Parameters parameters) {
		// /////////////////////////////////////////////////////////////////////
		//
		// If and only if data is a RenderedOp we prepare the properties for
		// minimum and maximum as the output of the extrema operation.
		//
		// /////////////////////////////////////////////////////////////////////
		if (data instanceof RenderedOp) {
			// XXX remove me with 1.5
			final RenderedOp result = (RenderedOp) data;

			// get the properties
			final javax.media.jai.Histogram hist = (javax.media.jai.Histogram) result
					.getProperty(GT_SYNTHETIC_PROPERTY_HISTOGRAM);

			// return the map
			final Map synthProp = new HashMap(2);
			synthProp.put(GT_SYNTHETIC_PROPERTY_HISTOGRAM, hist);
			return Collections.unmodifiableMap(synthProp);

		}
		return super.getProperties(data, crs, name, toCRS, sources, parameters);
	}

}
