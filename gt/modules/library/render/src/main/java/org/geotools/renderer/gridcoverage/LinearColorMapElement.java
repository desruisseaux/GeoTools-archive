/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.renderer.gridcoverage;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.referencing.piecewise.DefaultLinearPiecewiseTransform1DElement;
import org.geotools.referencing.piecewise.DomainElement1D;
import org.geotools.referencing.piecewise.PiecewiseTransform1DElement;
import org.geotools.renderer.gridcoverage.LinearColorMap.LinearColorMapType;
import org.geotools.renderer.i18n.ErrorKeys;
import org.geotools.renderer.i18n.Errors;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform1D;

/**
 * This     {@link LinearColorMapElement}     is a special implementation of both    {@link PiecewiseTransform1DElement}     and     {@link ColorMapTransformElement}     which can be used to do various types of classifications on raster. Specifically the supported types of classifications are unique values, classified and color ramps. <p> The supported types of classifications are     {@link LinearColorMapType#TYPE_RAMP}    ,    {@link LinearColorMapType#TYPE_VALUES}     and     {@link LinearColorMapType#TYPE_INTERVALS}    .
 * @see LinearColorMap
 * @see LinearColorMap.LinearColorMapType
 * @author     Simone Giannecchini, GeoSolutions
 */
public class LinearColorMapElement extends DefaultLinearPiecewiseTransform1DElement
		implements PiecewiseTransform1DElement, ColorMapTransformElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2216106857184603629L;


	/**
	 * {@link Color}        s associated to this         {@link ColorMapTransformElement}        .
	 * @uml.property  name="colors"
	 */
	private Color[] colors;

	public static LinearColorMapElement create(final CharSequence name,
			final Color[] colors, final NumberRange valueRange,
			final NumberRange sampleRange) throws IllegalArgumentException {
		return new LinearColorMapElement(name, colors, valueRange, sampleRange);
	}
	public static LinearColorMapElement create(CharSequence name, final Color color,
			final NumberRange inRange, final int outVal)
			throws IllegalArgumentException {
		return new ConstantColorMapElement(name, color, inRange, outVal);
	}
	
	/**
	 * @see LinearColorMapElement#ClassificationCategory(CharSequence,
	 *      Color[], NumberRange, NumberRange)
	 */
	public static LinearColorMapElement create(final CharSequence name,
			final Color color, final short value, final int sample)
			throws IllegalArgumentException {
		return new ConstantColorMapElement(name, color, value, sample);
	}
	/**
	 * @see LinearColorMapElement#ClassificationCategory(CharSequence,
	 *      Color[], NumberRange, NumberRange)
	 */
	public static LinearColorMapElement create(final CharSequence name,
			final Color color, final int value, final int sample)
			throws IllegalArgumentException {
		return new ConstantColorMapElement(name, color, value, sample);
	}
	/**
	 * @see LinearColorMapElement#ClassificationCategory(CharSequence,
	 *      Color[], NumberRange, NumberRange)
	 */
	public static LinearColorMapElement create(final CharSequence name,
			final Color color, final float value, final int sample)
			throws IllegalArgumentException {
		return new ConstantColorMapElement(name, color, value, sample);
	}

	
	/**
	 * @see LinearColorMapElement#ClassificationCategory(CharSequence,
	 *      Color[], NumberRange, NumberRange)
	 */
	public static LinearColorMapElement create(final CharSequence name,
			final Color color, final double value, final int sample)
			throws IllegalArgumentException {
		return new ConstantColorMapElement(name, color, value, sample);
	}
	
	
	/**
	 * Constructor for a {@link LinearColorMapElement}. It allows users
	 * to build a category which is able to map values into integer sample
	 * values for further rendering using and {@link IndexColorModel}.
	 * 
	 * <strong>NOTE</strong> Due to the limitations of the
	 * {@link IndexColorModel} we can accept as valid ranges only those that
	 * fit between 0 -65535.
	 * 
	 * @param name
	 *            for this {@link DomainElement1D}.
	 * @param colors
	 *            to use when rendering values belonging to this
	 *            {@link DomainElement1D}
	 * @param valueRange
	 *            the input range for this category.
	 * @param sampleRange
	 *            the sample range for this category. It will be used as indexes
	 *            for the final color map.
	 * @throws IllegalArgumentException
	 *             in case the output range does not respect
	 *             {@link IndexColorModel} limitations.
	 */
	LinearColorMapElement(final CharSequence name,
			final Color[] colors, final NumberRange valueRange,
			final NumberRange sampleRange) throws IllegalArgumentException {
		super(name, valueRange, checkSampleRange(sampleRange));
//		//@todo check this test
//		final int inEquals = ColorMapUtilities.compare(getInputMaximum(), getInputMinimum());
//		final int outEquals = ColorMapUtilities.compare(getOutputMaximum(), getOutputMinimum());
//		if (inEquals == 0 && outEquals == 0)
//			this.type = LinearColorMap.LinearColorMapType.TYPE_VALUES;
//		else if (outEquals == 0)
//			this.type = LinearColorMap.LinearColorMapType.TYPE_INTERVALS;
//		else {
//			if (isIdentity())
//
//				this.type = LinearColorMap.LinearColorMapType.TYPE_VALUES;
//			else
//				this.type = LinearColorMap.LinearColorMapType.TYPE_RAMP;
//		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Initialise fields for visualization
		//
		// /////////////////////////////////////////////////////////////////////
		this.colors = new Color[colors.length];
		System.arraycopy(colors, 0, this.colors, 0, colors.length);

	}


	/**
	 * This method is responsible for performing a few checks on the provided
	 * range in order to make sure we are talking about a valid range for
	 * building an {@link IndexColorModel}.
	 * 
	 * @param numberRange
	 *            the range to use for mapping values to colors.
	 * @return the input {@link NumberRange} if everything goes well.
	 * @see IndexColorModel
	 */
	private static NumberRange checkSampleRange(NumberRange numberRange) {
		if (numberRange == null)
			throw new IllegalArgumentException();
		final Class elementClass = numberRange.getElementClass();
		if (!elementClass.equals(Integer.class)
				&& !elementClass.equals(Byte.class)
				&& !elementClass.equals(Short.class))
			throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, numberRange));
		if (numberRange.getMinimum() < 0 || numberRange.getMaximum() > 65535)
			throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, numberRange));
		return numberRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.lite.gridcoverage2d.Category#equals(java.lang.Object)
	 */
	public boolean equals(final Object object) {
		boolean retVal = super.equals(object);
		if (retVal) {
			final LinearColorMapElement that = (LinearColorMapElement) object;
			retVal &= Arrays.equals(this.getColors(), that.getColors());
		}
		return retVal;
	}

	/**
	 * Returns the set of colors for this category. Change to the returned array will not affect this category.
	 * @see  GridSampleDimension#getColorModel
	 * @uml.property  name="colors"
	 */
	public Color[] getColors() {
		return (Color[]) colors.clone();
	}
	
	/**
	 * Gives access to the internal {@link MathTransform1D}.
	 * 
	 * @return the internal {@link MathTransform1D}.
	 */
	MathTransform1D accessTransform() {
		return getTransform();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geotools.referencing.piecewise.DefaultLinearPiecewiseTransform1DElement#toString()
	 */
	public String toString() {
		final StringBuffer buffer= new StringBuffer(super.toString());
		buffer.append("\n").append("colors=");
		for(int i=0;(colors !=null)&&i<colors.length;i++)
		{
			buffer.append(colors[i]);
			if(i+1<colors.length)
				buffer.append(",");
		}

		return buffer.toString();
	}

}
