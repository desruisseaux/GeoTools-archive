/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io.metadata;

import java.util.List;
import org.w3c.dom.Element;
import org.geotools.util.NumberRange;


/**
 * Provides convenience methods for decoding sample dimension information.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleDimensions extends MetadataAccessor {
    /**
     * The elements for the {@code "SampleDimensions/SampleDimension"} node.
     */
    private final List/*<Element>*/ sampleDimensions;

    /**
     * Creates a parser for sample dimensions.
     *
     * @param parent The parent metadata parser.
     */
    public SampleDimensions(final GeographicMetadataParser parent) {
        sampleDimensions = parent.getElements("SampleDimensions/SampleDimension");
    }

    /**
     * Returns the number of {@code "SampleDimensions/SampleDimension"} nodes.
     */
    public int getCount() {
        return sampleDimensions.size();
    }

    /**
     * Returns the element for the {@code "SampleDimensions/SampleDimension"} node at the
     * specified band.
     *
     * @param  band The sample dimension number.
     * @return The node for the specified sample dimension.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    private Element getSampleDimension(final int band) throws IndexOutOfBoundsException {
        return (Element) sampleDimensions.get(band);
    }

    /**
     * Returns the name for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @return The sample dimension name, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public String getName(final int band) throws IndexOutOfBoundsException {
        return getString(getSampleDimension(band), "name");
    }

    /**
     * Sets the name for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @param  name The sample dimension name, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public void setName(final int band, final String name) throws IndexOutOfBoundsException {
        GeographicMetadata.setAttribute(getSampleDimension(band), "name", name);
    }

    /**
     * Returns the range of valid values for the specified sample dimension. The range use the
     * {@link Integer} type if possible, or the {@link Double} type otherwise. Note that range
     * {@linkplain NumberRange#getMinValue minimum value}, {@linkplain NumberRange#getMaxValue
     * maximum value} or both may be null if no {@code "minValue"} or {@code "maxValue"}
     * attribute were found for the {@code "SampleDimensions/SampleDimension"} node.
     *
     * @param  band The sample dimension number.
     * @return The range of valid values for the specified sample dimension.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public NumberRange getValidRange(final int band) throws IndexOutOfBoundsException {
        final Element element = getSampleDimension(band);
        Number minimum = getInteger(element, "minValue");
        Number maximum = getInteger(element, "maxValue");
        final Class type;
        if (minimum == null || maximum == null) {
            minimum = getDouble(element, "minValue");
            maximum = getDouble(element, "maxValue");
            type = Double.class;
        } else {
            type = Integer.class;
        }
        // Note: minimum and/or maximum may be null, in which case the range in unbounded.
        return new NumberRange(type, minimum, true, maximum, true);
    }

    /**
     * Returns the fill values for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @return The fill values for the specified sample dimension, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public double[] getFillValues(final int band) throws IndexOutOfBoundsException {
        return getDoubles(getSampleDimension(band), "fillValues", true);
    }

    /**
     * Sets the fill values for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @param  fillValues The fill values for the specified sample dimension, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public void setFillValues(final int band, final double[] fillValues) throws IndexOutOfBoundsException {
        GeographicMetadata.setAttribute(getSampleDimension(band), "fillValues", fillValues);
    }

    /**
     * Returns the scale factor for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @return The scale factor for the specified sample dimension, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public Double getScale(final int band) throws IndexOutOfBoundsException {
        return getDouble(getSampleDimension(band), "scale");
    }

    /**
     * Sets the scale factor for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @param  scale The scale factor for the specified sample dimension, or {@code NaN} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public void setScale(final int band, final double scale) throws IndexOutOfBoundsException {
        GeographicMetadata.setAttribute(getSampleDimension(band), "scale", scale);
    }

    /**
     * Returns the offset for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @return The offset for the specified sample dimension, or {@code null} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public Double getOffset(final int band) throws IndexOutOfBoundsException {
        return getDouble(getSampleDimension(band), "offset");
    }

    /**
     * Sets the offset for the specified sample dimension.
     *
     * @param  band The sample dimension number.
     * @param  offset The offset for the specified sample dimension, or {@code NaN} if none.
     * @throws IndexOutOfBoundsException if the specified sample dimension is out of range.
     */
    public void setOffset(final int band, final double offset) throws IndexOutOfBoundsException {
        GeographicMetadata.setAttribute(getSampleDimension(band), "offset", offset);
    }
}
