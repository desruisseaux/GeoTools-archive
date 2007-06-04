/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.image.io.netcdf;

// NetCDF dependencies
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.EnhanceScaleMissing; // For javadoc

// Geotools dependencies
import org.geotools.resources.XArray;


/**
 * Parses the offset, scale factor, minimum, maximum and fill values from a variable. This class
 * duplicate UCAR's {@code EnhanceScaleMissingImpl} functionality, but we have to do that because:
 * <p>
 * <ul>
 *   <li>I have not been able to find any method giving me directly the offset and scale factor.
 *       We can use some trick with {@link EnhanceScaleMissing#convertScaleOffsetMissing}, but
 *       they are subject to rounding errors and there is no efficient way I can see to take
 *       missing values in account.</li>
 *   <li>The {@link EnhanceScaleMissing} methods are available only if the variable is enhanced.
 *       Our variable is not, because we want raw data.</li>
 * </ul>
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class VariableMetadata {
    /**
     * The scale and and offset values, or {@link Double#NaN NaN} if none.
     */
    public final double scale, offset;

    /**
     * The minimal and maximal valid values in geophysics units, or {@link Double#NaN NaN} if none.
     * They are converted from the packed values if needed, as UCAR does.
     */
    public final double minimum, maximum;

    /**
     * The fill and missing values in <strong>packed</strong> units, or {@code null} if none.
     * Note that this is different from UCAR, who converts to geophysics values. We keep packed
     * values in order to avoir rounding error. This array contains both the fill value and the
     * missing values, without duplicated values.
     */
    public final double[] missingValues;

    /**
     * Extracts metadata from the specified variable.
     */
    public VariableMetadata(final Variable variable) {
        scale   = attribute(variable, "scale_factor");
        offset  = attribute(variable, "add_offset");
        /*
         * Gets minimum and maximum. If a "valid_range" attribute is presents, it as precedence
         * over "valid_min" and "valid_max" as specified in UCAR documentation.
         */
        double minimum = Double.NaN;
        double maximum = Double.NaN;
        Attribute attribute = variable.findAttribute("valid_range");
        final DataType dataType = variable.getDataType();
        DataType rangeType = dataType;
        if (attribute != null) {
            rangeType = widest(attribute, rangeType);
            Number value = attribute.getNumericValue(0);
            if (value != null) {
                minimum = value.doubleValue();
            }
            value = attribute.getNumericValue(1);
            if (value != null) {
                maximum = value.doubleValue();
            }
        }
        if (Double.isNaN(minimum)) {
            // TODO: update 'rangeType'
            minimum = attribute(variable, "valid_min");
        }
        if (Double.isNaN(maximum)) {
            maximum = attribute(variable, "valid_max");
        }
        this.minimum = Double.isNaN(minimum) ? Double.NEGATIVE_INFINITY : minimum;
        this.maximum = Double.isNaN(maximum) ? Double.POSITIVE_INFINITY : maximum;
        /*
         * Gets fill and missing values. According UCAR documentation, they are
         * always in packed units. We keep them "as-is" (as opposed to UCAR who
         * converts them to geophysics units), in order to avoid rounding errors.
         * Note that we merge missing and fill values in a single array, without
         * duplicated values.
         */
        attribute = variable.findAttribute("missing_value");
        final double fillValue    = attribute(variable, "_FillValue");
        final int    fillCount    = Double.isNaN(fillValue) ? 0 : 1;
        final int    missingCount = (attribute != null) ? attribute.getLength() : 0;
        final double[] missings   = new double[fillCount + missingCount];
        if (fillCount != 0) {
            missings[0] = fillValue;
        }
        int count = fillCount;
scan:   for (int i=0; i<missingCount; i++) {
            final Number number = attribute.getNumericValue(i);
            if (number != null) {
                final double value = number.doubleValue();
                if (!Double.isNaN(value)) {
                    for (int j=0; j<count; j++) {
                        if (value == missings[j]) {
                            // Current value duplicates a previous one.
                            continue scan;
                        }
                    }
                    missings[count++] = value;
                }
            }
        }
        missingValues = (count != 0) ? XArray.resize(missings, count) : null;
    }

    /**
     * Returns the attribute value as a {@code double}.
     */
    private static double attribute(final Variable variable, final String name) {
        final Attribute attribute = variable.findAttribute(name);
        if (attribute != null) {
            final Number value = attribute.getNumericValue();
            if (value != null) {
                return value.doubleValue();
            }
        }
        return Double.NaN;
    }

    /**
     * Returns the widest of two data type.
     */
    private static DataType widest(final Attribute attribute, final DataType type2) {
        final DataType type1 = attribute.getDataType();
        if (type1 == null) return type2;
        if (type2 == null) return type1;
        final int size1 = type1.getSize();
        final int size2 = type2.getSize();
        if (size1 > size2) return type1;
        if (size1 < size2) return type2;
        if (DataType.FLOAT.equals(type2) || DataType.DOUBLE.equals(type2)) {
            return type2;
        }
        return type1;
    }
}
