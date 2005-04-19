/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage;

// J2SE dependencies and extensions
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.coverage.ColorInterpretation;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.util.AbstractInternationalString;
import org.geotools.util.SimpleInternationalString;


/**
 * Utility methods for choosing a {@linkplain SampleModel sample model} or a
 * {@linkplain ColorModel color model} on the basis of a range of values.
 * This class provides also some methods for mapping {@link SampleDimensionType}
 * to {@link DataBuffer} types.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class TypeMap {
    /**
     * The mapping of {@link SampleDimensionType} to {@link DataBuffer} types.
     */
    private static final TypeMap[] MAP = new TypeMap[SampleDimensionType.values().length];
    static {
        // The constructor will register automatically those objects in the above array.
        new TypeMap(SampleDimensionType. UNSIGNED_1BIT,  DataBuffer.TYPE_BYTE,   (byte) 1, false, false);
        new TypeMap(SampleDimensionType. UNSIGNED_2BITS, DataBuffer.TYPE_BYTE,   (byte) 2, false, false);
        new TypeMap(SampleDimensionType. UNSIGNED_4BITS, DataBuffer.TYPE_BYTE,   (byte) 4, false, false);
        new TypeMap(SampleDimensionType. UNSIGNED_8BITS, DataBuffer.TYPE_BYTE,   (byte) 8, false, false);
        new TypeMap(SampleDimensionType.   SIGNED_8BITS, DataBuffer.TYPE_BYTE,   (byte) 8, true,  false);
        new TypeMap(SampleDimensionType.UNSIGNED_16BITS, DataBuffer.TYPE_USHORT, (byte)16, false, false);
        new TypeMap(SampleDimensionType.  SIGNED_16BITS, DataBuffer.TYPE_SHORT,  (byte)16, true,  false);
        new TypeMap(SampleDimensionType.UNSIGNED_32BITS, DataBuffer.TYPE_INT,    (byte)32, false, false);
        new TypeMap(SampleDimensionType.  SIGNED_32BITS, DataBuffer.TYPE_INT,    (byte)32, true,  false);
        new TypeMap(SampleDimensionType.    REAL_32BITS, DataBuffer.TYPE_FLOAT,  (byte)32, true,  true );
        new TypeMap(SampleDimensionType.    REAL_64BITS, DataBuffer.TYPE_DOUBLE, (byte)64, true,  true );
    };

    /**
     * One of {@link SampleDimensionType} code list.
     */
    private final SampleDimensionType code;

    /**
     * The {@link DataBuffer} type. Must be one of the following constants:
     * {@link DataBuffer#TYPE_BYTE},  {@link DataBuffer#TYPE_USHORT},
     * {@link DataBuffer#TYPE_SHORT}, {@link DataBuffer#TYPE_INT},
     * {@link DataBuffer#TYPE_FLOAT}, {@link DataBuffer#TYPE_DOUBLE}.
     */
    private final int type;

    /**
     * The size in bits. The value range from 1 to 64. This is different than
     * {@link DataBuffer#getDataTypeSize}, which have values ranging from 8 to 64.
     */
    private final byte size;

    /**
     * {@code true} for signed sample type.
     */
    private final boolean signed;

    /**
     * {@code true} for floating-point data type.
     */
    private final boolean real;

    /**
     * The name as an international string.
     */
    private final InternationalString name = new AbstractInternationalString() {
        public String toString(final Locale locale) {
            return Resources.getResources(locale).getString(ResourceKeys.DATA_TYPE_$2,
                    new Integer(real ? 2 : signed ? 1 : 0), new Integer(size));
        }
    };

    /**
     * Constructs a new mapping with the specified value.
     */
    private TypeMap(final SampleDimensionType code,
                    final int     type,   final byte    size,
                    final boolean signed, final boolean real)
    {
        this.code   = code;
        this.type   = type;
        this.size   = size;
        this.signed = signed;
        this.real   = real;
        final int ordinal = code.ordinal();
        assert MAP[ordinal] == null : code;
        MAP[ordinal] = this;
    }

    /**
     * Returns the smallest sample dimension type capable to hold the specified range of values.
     *
     * @param  range The range of values.
     * @return The smallest sample dimension type for the specified range.
     */
    public static SampleDimensionType getSampleDimensionType(final Range range) {
        final Class type = range.getElementClass();
        if (Double.class.isAssignableFrom(type)) {
            return SampleDimensionType.REAL_64BITS;
        }
        if (Float.class.isAssignableFrom(type)) {
            return SampleDimensionType.REAL_32BITS;
        }
        long min = ((Number) range.getMinValue()).longValue();
        long max = ((Number) range.getMaxValue()).longValue();
        if (!range.isMinIncluded()) min++;
        if (!range.isMaxIncluded()) max--;
        return getSampleDimensionType(min, max);
    }

    /**
     * Returns the smallest sample dimension type capable to hold the specified range of values.
     * An heuristic approach is used for non-integer values.
     *
     * @param  min The lower value, inclusive.
     * @param  max The upper value, <strong>inclusive</strong> as well.
     * @return The smallest sample dimension type for the specified range.
     */
    public static SampleDimensionType getSampleDimensionType(double min, double max) {
        final long lgMin = (long) min;
        if (lgMin == min) {
            final long lgMax = (long) max;
            if (lgMax == max) {
                return getSampleDimensionType(lgMin, lgMax);
            }
        }
        min = Math.abs(min);
        max = Math.abs(max);
        if (Math.min(min,max)>=Float.MIN_VALUE && Math.max(min,max)<=Float.MAX_VALUE) {
            return SampleDimensionType.REAL_32BITS;
        }
        return SampleDimensionType.REAL_64BITS;
    }

    /**
     * Returns the smallest sample dimension type capable to hold the specified range of values.
     *
     * @param  min The lower value, inclusive.
     * @param  max The upper value, <strong>inclusive</strong> as well.
     * @return The smallest sample dimension type for the specified range.
     */
    public static SampleDimensionType getSampleDimensionType(final long min, final long max) {
        if (min >= 0) {
            if (max < (1L <<  1)) return SampleDimensionType.UNSIGNED_1BIT;
            if (max < (1L <<  2)) return SampleDimensionType.UNSIGNED_2BITS;
            if (max < (1L <<  4)) return SampleDimensionType.UNSIGNED_4BITS;
            if (max < (1L <<  8)) return SampleDimensionType.UNSIGNED_8BITS;
            if (max < (1L << 16)) return SampleDimensionType.UNSIGNED_16BITS;
            if (max < (1L << 32)) return SampleDimensionType.UNSIGNED_32BITS;
        } else {
            if (min>=Byte   .MIN_VALUE && max<=Byte   .MAX_VALUE) return SampleDimensionType.SIGNED_8BITS;
            if (min>=Short  .MIN_VALUE && max<=Short  .MAX_VALUE) return SampleDimensionType.SIGNED_16BITS;
            if (min>=Integer.MIN_VALUE && max<=Integer.MAX_VALUE) return SampleDimensionType.SIGNED_32BITS;
        }
        return SampleDimensionType.REAL_32BITS;
    }

    /**
     * Return the sample dimension type for the specified sample model and band number. If
     * the sample model use an undefined data type, then this method returns {@code null}.
     *
     * @param  model The sample model.
     * @param  band  The band to query.
     * @return The sample dimension type for the specified sample model and band number.
     * @throws IllegalArgumentException if the band number is not in the valid range.
     */
    public static SampleDimensionType getSampleDimensionType(final SampleModel model, final int band)
            throws IllegalArgumentException
    {
        if (band<0 || band>=model.getNumBands()) {
            throw new IllegalArgumentException(
                    Resources.format(ResourceKeys.ERROR_BAD_BAND_NUMBER_$1, new Integer(band)));
        }
        boolean signed = true;
        switch (model.getDataType()) {
            case DataBuffer.TYPE_DOUBLE: return SampleDimensionType.REAL_64BITS;
            case DataBuffer.TYPE_FLOAT:  return SampleDimensionType.REAL_32BITS;
            case DataBuffer.TYPE_USHORT: // Fall through
            case DataBuffer.TYPE_BYTE:   signed=false; // Fall through
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_SHORT: {
                switch (model.getSampleSize(band)) {
                    case  1: return SampleDimensionType.UNSIGNED_1BIT;
                    case  2: return SampleDimensionType.UNSIGNED_2BITS;
                    case  4: return SampleDimensionType.UNSIGNED_4BITS;
                    case  8: return signed ? SampleDimensionType.SIGNED_8BITS  : SampleDimensionType.UNSIGNED_8BITS;
                    case 16: return signed ? SampleDimensionType.SIGNED_16BITS : SampleDimensionType.UNSIGNED_16BITS;
                    case 32: return signed ? SampleDimensionType.SIGNED_32BITS : SampleDimensionType.UNSIGNED_32BITS;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link DataBuffer} type. This is one of the following constants:
     * {@link DataBuffer#TYPE_BYTE   TYPE_BYTE},
     * {@link DataBuffer#TYPE_USHORT TYPE_USHORT},
     * {@link DataBuffer#TYPE_SHORT  TYPE_SHORT},
     * {@link DataBuffer#TYPE_INT    TYPE_INT},
     * {@link DataBuffer#TYPE_FLOAT  TYPE_FLOAT},
     * {@link DataBuffer#TYPE_DOUBLE TYPE_DOUBLE} or
     * {@link DataBuffer#TYPE_UNDEFINED} if the type is unrecognized.
     */
    public static int getDataBufferType(final SampleDimensionType type) {
        if (type != null) {
            final int ordinal = type.ordinal();
            if (ordinal>=0 && ordinal<MAP.length) {
                return MAP[ordinal].type;
            }
        }
        return DataBuffer.TYPE_UNDEFINED;
    }

    /**
     * Returns the sample dimension type name as an international string. For example, the localized
     * name for {@link SampleDimensionType#UNSIGNED_16BITS} is "<cite>16 bits unsigned integer</cite>"
     * in English and "<cite>Entier non-signé sur 16 bits</cite>" in French.
     */
    public static InternationalString getName(final SampleDimensionType type) {
        final int ordinal = type.ordinal();
        if (ordinal>=0 && ordinal<MAP.length) {
            return MAP[ordinal].name;
        }
        return new SimpleInternationalString(type.name());
    }

    /**
     * Returns the size in bits. The value range from 1 to 64. This is similar, but
     * different than {@link DataBuffer#getDataTypeSize}, which have values ranging
     * from 8 to 64.
     */
    public static int getSize(final SampleDimensionType type) {
        return map(type).size;
    }

    /**
     * Returns {@code true} for signed sample type.
     */
    public static boolean isSigned(final SampleDimensionType type) {
        return map(type).signed;
    }

    /**
     * Returns {@code true} for floating-point data type.
     */
    public static boolean isFloatingPoint(final SampleDimensionType type) {
        return map(type).real;
    }

    /**
     * Returns the mapper for the specified sample dimension type. If no map is found for the
     * specified sample dimension type, then an exception is thrown.
     */
    private static TypeMap map(final SampleDimensionType type) throws IllegalArgumentException {
        if (type != null) {
            final int ordinal = type.ordinal();
            if (ordinal>=0 && ordinal<MAP.length) {
                final TypeMap map = MAP[ordinal];
                if (map != null) {
                    return map;
                }
            }
        }
        throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                  org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "type", type));
    }

    /**
     * Wraps the specified value into a number of the specified data type. If the
     * value can't fit in the specified type, then a wider type is choosen unless
     * {@code allowWidening} is {@code false}.
     *
     * @param  value The value to wrap in a {@link Number} object.
     * @param  type A constant from the {@link SampleDimensionType} code list.
     * @param  allowWidening {@code true} if this method is allowed to returns
     *         a wider type than the usual one for the specified {@code type}.
     * @return The value as a {@link Number}.
     * @throws IllegalArgumentException if {@code type} is not a recognized constant.
     * @throws IllegalArgumentException if {@code allowWidening} is {@code false}
     *         and the specified {@code value} can't fit in the specified sample type.
     */
    public static Number wrapSample(final double             value,
                                    final SampleDimensionType type,
                                    final boolean    allowWidening)
            throws IllegalArgumentException
    {
        /*
         * Note about 'ordinal' computation: We would like to switch on SampleDimensionType
         * ordinal values. But the compiler requires constant values, and doesn't recognize
         * SampleDimensionType ordinal as such. As a workaround, we use the sample size (in
         * bits) with the following convention: negative value if signed, and offset by 16
         * bits if floating point numbers.
         */
        final TypeMap map = map(type);
        int ordinal = map.size;
        if (map.real) {
            ordinal <<= 16;
        } else if (map.signed) {
            ordinal = -ordinal;
        }
        switch (ordinal) {
            case  1: // Fall through
            case  2: // Fall through
            case  4: // Fall through
            case -8: {
                final byte candidate = (byte) value;
                if (candidate == value) {
                    return new Byte(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case   8: // Fall through
            case -16: {
                final short candidate = (short) value;
                if (candidate == value) {
                    return new Short(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case  16: // Fall through
            case -32: {
                final int candidate = (int) value;
                if (candidate == value) {
                    return new Integer(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case 32: {
                final long candidate = (long) value;
                if (candidate == value) {
                    return new Long(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case (32 << 16): {
                if (!allowWidening || Math.abs(value) <= Float.MAX_VALUE) {
                    return new Float((float) value);
                }
                // Fall through
            }
            case (64 << 16): {
                return new Double(value);
            }
            default: {
                throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                          org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                          "type", type));
            }
        }
        throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                  org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                  "value", new Double(value)));
    }
    
    /**
     * Return the color interpretation code for the specified color model and band number.
     *
     * @param  model The color model.
     * @param  band  The band to query.
     * @return The code for the specified color model and band number.
     * @throws IllegalArgumentException if the band number is not in the valid range.
     */
    public static ColorInterpretation getColorInterpretation(final ColorModel model, final int band)
            throws IllegalArgumentException
    {
        if (band<0 || band>=model.getNumComponents()) {
            throw new IllegalArgumentException(
                    Resources.format(ResourceKeys.ERROR_BAD_BAND_NUMBER_$1, new Integer(band)));
        }
        if (model instanceof IndexColorModel) {
            return ColorInterpretation.PALETTE_INDEX;
        }
        switch (model.getColorSpace().getType()) {
            case ColorSpace.TYPE_GRAY: {
                switch (band) {
                    case  0: return ColorInterpretation.GRAY_INDEX;
                    default: return ColorInterpretation.UNDEFINED;
                }
            }
            case ColorSpace.TYPE_RGB: {
                switch (band) {
                    case  0: return ColorInterpretation.RED_BAND;
                    case  1: return ColorInterpretation.GREEN_BAND;
                    case  2: return ColorInterpretation.BLUE_BAND;
                    case  3: return ColorInterpretation.ALPHA_BAND;
                    default: return ColorInterpretation.UNDEFINED;
                }
            }
            case ColorSpace.TYPE_HSV: {
                switch (band) {
                    case  0: return ColorInterpretation.HUE_BAND;
                    case  1: return ColorInterpretation.SATURATION_BAND;
                    case  2: return ColorInterpretation.LIGHTNESS_BAND;
                    default: return ColorInterpretation.UNDEFINED;
                }
            }
            case ColorSpace.TYPE_CMY:
            case ColorSpace.TYPE_CMYK: {
                switch (band) {
                    case  0: return ColorInterpretation.CYAN_BAND;
                    case  1: return ColorInterpretation.MAGENTA_BAND;
                    case  2: return ColorInterpretation.YELLOW_BAND;
                    case  3: return ColorInterpretation.BLACK_BAND;
                    default: return ColorInterpretation.UNDEFINED;
                }
            }
            default: return ColorInterpretation.UNDEFINED;
        }
    }
}
