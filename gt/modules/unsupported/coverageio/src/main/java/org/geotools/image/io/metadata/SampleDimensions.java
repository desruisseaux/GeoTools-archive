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

import org.geotools.util.NumberRange;
import org.w3c.dom.Node;


/**
 * Provides convenience methods for encoding and decoding information about sample dimensions.
 * A sample dimension should be {@linkplain #select selected} before any {@code get} or {@code
 * set} method is invoked. Valid sample dimension index range from 0 inclusive to
 * {@link #elementCount elementCount()} exclusive.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleDimensions extends MetadataAccessor {
    /**
     * Creates a parser for sample dimensions.
     *
     * @param  metadata The metadata node.
     */
    public SampleDimensions(final Node metadata) {
        super(metadata, "SampleDimensions", "SampleDimension");
    }

    /**
     * Returns the name for {@linkplain #select selected} sample dimension,
     * or {@code null} if none.
     */
    public String getName() {
        return getString("name");
    }

    /**
     * Sets the name for the {@linkplain #select selected} sample dimension.
     *
     * @param name The sample dimension name, or {@code null} if none.
     */
    public void setName(final String name) {
        setString("name", name);
    }

    /**
     * Returns the range of valid values for the {@linkplain #select selected} sample dimension.
     * The range use the {@link Integer} type if possible, or the {@link Double} type otherwise.
     * Note that range {@linkplain NumberRange#getMinValue minimum value},
     * {@linkplain NumberRange#getMaxValue maximum value} or both may be null if no
     * {@code "minValue"} or {@code "maxValue"} attribute were found for the
     * {@code "SampleDimensions/SampleDimension"} element.
     */
    public NumberRange getValidRange() {
        Number minimum = getInteger("minValue");
        Number maximum = getInteger("maxValue");
        final Class type;
        if (minimum == null || maximum == null) {
            minimum = getDouble("minValue");
            maximum = getDouble("maxValue");
            type = Double.class;
        } else {
            type = Integer.class;
        }
        // Note: minimum and/or maximum may be null, in which case the range in unbounded.
        return new NumberRange(type, minimum, true, maximum, true);
    }

    /**
     * Set the range of valid values. The values should be integers most of the time since
     * they are packed values (often index in a color palette). But floating point values
     * are allowed too.
     *
     * @param minValue  The minimal valid <em>packed</em> value,
     *                  or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maxValue  The maximal valid <em>packed</em> value,
     *                  or {@link Double#POSITIVE_INFINITY} if none.
     */
    public void setValidRange(final double minValue, final double maxValue) {
        final int minIndex = (int) minValue;
        final int maxIndex = (int) maxValue;
        if (minIndex == minValue && maxIndex == maxValue) {
            setInteger("minValue", minIndex);
            setInteger("maxValue", maxIndex);
        } else {
            setDouble("minValue", minValue);
            setDouble("maxValue", maxValue);
        }
    }

    /**
     * Returns the fill values for the {@linkplain #select selected} sample dimension,
     * or {@code null} if none.
     */
    public double[] getFillValues() {
        return getDoubles("fillValues", true);
    }

    /**
     * Sets the fill values for the {@linkplain #select selected} sample dimension.
     * This method formats all fill values as integers if possible, or all values as
     * floating points otherwise. We apply a "all or nothing" rule for consistency.
     *
     * @param fillValues The packed values used for missing data, or {@code null} if none.
     */
    public void setFillValues(final double[] fillValues) {
        if (fillValues != null) {
            int[] asIntegers = new int[fillValues.length];
            for (int i=0; i<fillValues.length; i++) {
                final double value = fillValues[i];
                if ((asIntegers[i] = (int) value) != value) {
                    asIntegers = null; // Not integers; stop the check.
                    break;
                }
            }
            if (asIntegers != null) {
                setIntegers("fillValues", asIntegers);
                return;
            }
        }
        setDoubles("fillValues", fillValues);
    }

    /**
     * Returns the scale factor for the {@linkplain #select selected} sample dimension.
     */
    public Double getScale() {
        return getDouble("scale");
    }

    /**
     * Sets the scale factor for the {@linkplain #select selected} sample dimension.
     *
     * @param scale The scale from packed to geophysics values, or {@code 1} if none.
     */
    public void setScale(final double scale) {
        setDouble("scale", scale);
    }

    /**
     * Returns the offset for the {@linkplain #select selected} sample dimension.
     */
    public Double getOffset() {
        return getDouble("offset");
    }

    /**
     * Sets the offset for the {@linkplain #select selected} sample dimension.
     *
     * @param offset The offset from packed to geophysics values, or {@code 0} if none.
     */
    public void setOffset(final double offset) {
        setDouble("offset", offset);
    }
}
