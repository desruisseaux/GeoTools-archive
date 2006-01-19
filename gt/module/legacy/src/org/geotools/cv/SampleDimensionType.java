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
package org.geotools.cv;

// J2SE dependencies
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.media.jai.EnumeratedParameter;
import javax.media.jai.util.Range;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.opengis.cv.CV_SampleDimensionType;


/**
 * Contains information for an individual sample dimension of coverage.
 * This interface is applicable to any coverage type.
 * For grid coverages, the sample dimension refers to an individual band.
 *
 * @source $URL$
 * @version $Id$
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_SampleDimensionType
 *
 * @deprecated Replaced by {@link org.opengis.coverage.SampleDimensionType} in the
 *             <code>org.opengis.coverage</code> package.
 */
public final class SampleDimensionType extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8172733477873830772L;

    /**
     * 1 bit integers.
     *
     * @see CV_SampleDimensionType#CV_1BIT
     * @see DataBuffer#TYPE_BYTE
     */
    public static final SampleDimensionType BIT = new SampleDimensionType("BIT",
            CV_SampleDimensionType.CV_1BIT, DataBuffer.TYPE_BYTE, (byte)1, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_1BIT);

    /**
     * 2 bits integers.
     *
     * @see CV_SampleDimensionType#CV_2BIT
     * @see DataBuffer#TYPE_BYTE
     */
    public static final SampleDimensionType DOUBLET = new SampleDimensionType("DOUBLET",
            CV_SampleDimensionType.CV_2BIT, DataBuffer.TYPE_BYTE, (byte)2, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_2BITS);

    /**
     * 4 bits integers (also called "quartet" or "half-byte").
     *
     * @see CV_SampleDimensionType#CV_4BIT
     * @see DataBuffer#TYPE_BYTE
     */
    public static final SampleDimensionType NIBBLE = new SampleDimensionType("NIBBLE",
            CV_SampleDimensionType.CV_4BIT, DataBuffer.TYPE_BYTE, (byte)4, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_4BITS);

    /**
     * Unsigned 8 bits integers.
     *
     * @see CV_SampleDimensionType#CV_8BIT_U
     * @see DataBuffer#TYPE_BYTE
     */
    public static final SampleDimensionType UBYTE = new SampleDimensionType("UBYTE",
            CV_SampleDimensionType.CV_8BIT_U, DataBuffer.TYPE_BYTE, (byte)8, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_8BITS);

    /**
     * Signed 8 bits integers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_BYTE}.
     *
     * @see CV_SampleDimensionType#CV_8BIT_S
     * @see DataBuffer#TYPE_BYTE
     */
    public static final SampleDimensionType BYTE = new SampleDimensionType("BYTE",
            CV_SampleDimensionType.CV_8BIT_S, DataBuffer.TYPE_BYTE, (byte)8, true, false,
            org.opengis.coverage.SampleDimensionType.SIGNED_8BITS);

    /**
     * Unsigned 16 bits integers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_USHORT}.
     *
     * @see CV_SampleDimensionType#CV_16BIT_U
     * @see DataBuffer#TYPE_USHORT
     */
    public static final SampleDimensionType USHORT = new SampleDimensionType("USHORT",
            CV_SampleDimensionType.CV_16BIT_U, DataBuffer.TYPE_USHORT, (byte)16, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_16BITS);

    /**
     * Signed 16 bits integers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_SHORT}.
     *
     * @see CV_SampleDimensionType#CV_16BIT_S
     * @see DataBuffer#TYPE_SHORT
     */
    public static final SampleDimensionType SHORT = new SampleDimensionType("SHORT",
            CV_SampleDimensionType.CV_16BIT_S, DataBuffer.TYPE_SHORT, (byte)16, true, false,
            org.opengis.coverage.SampleDimensionType.SIGNED_16BITS);

    /**
     * Unsigned 32 bits integers.
     *
     * @see CV_SampleDimensionType#CV_32BIT_U
     * @see DataBuffer#TYPE_INT
     */
    public static final SampleDimensionType UINT = new SampleDimensionType("UINT",
            CV_SampleDimensionType.CV_32BIT_U, DataBuffer.TYPE_INT, (byte)32, false, false,
            org.opengis.coverage.SampleDimensionType.UNSIGNED_32BITS);

    /**
     * Signed 32 bits integers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_INT}.
     *
     * @see CV_SampleDimensionType#CV_32BIT_S
     * @see DataBuffer#TYPE_INT
     */
    public static final SampleDimensionType INT = new SampleDimensionType("INT",
            CV_SampleDimensionType.CV_32BIT_S, DataBuffer.TYPE_INT, (byte)32, true, false,
            org.opengis.coverage.SampleDimensionType.SIGNED_32BITS);

    /**
     * Simple precision floating point numbers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_FLOAT}.
     *
     * @see CV_SampleDimensionType#CV_32BIT_REAL
     * @see DataBuffer#TYPE_FLOAT
     */
    public static final SampleDimensionType FLOAT = new SampleDimensionType("FLOAT",
            CV_SampleDimensionType.CV_32BIT_REAL, DataBuffer.TYPE_FLOAT, (byte)32, true, true,
            org.opengis.coverage.SampleDimensionType.REAL_32BITS);

    /**
     * Double precision floating point numbers.
     * The equivalent Java data type is {@link DataBuffer#TYPE_DOUBLE}.
     *
     * @see CV_SampleDimensionType#CV_64BIT_REAL
     * @see DataBuffer#TYPE_DOUBLE
     */
    public static final SampleDimensionType DOUBLE = new SampleDimensionType("DOUBLE",
            CV_SampleDimensionType.CV_64BIT_REAL, DataBuffer.TYPE_DOUBLE, (byte)64, true, true,
            org.opengis.coverage.SampleDimensionType.REAL_64BITS);
    
    /**
     * Color interpretation by value. Used to
     * canonicalize after deserialization.
     */
    private static final SampleDimensionType[] ENUMS = {
        BIT, DOUBLET, NIBBLE, UBYTE, BYTE,
        USHORT, SHORT, UINT, INT, FLOAT, DOUBLE
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue() != i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }

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
     * <code>true</code> for signed sample type.
     */
    private final boolean signed;

    /**
     * <code>true</code> for floating-point data type.
     */
    private final boolean real;

    /**
     * The GeoAPI code.
     */
    private final org.opengis.coverage.SampleDimensionType geoAPI;

    /**
     * Construct a new enum with the specified value.
     */
    private SampleDimensionType(final String  name,   final int     value,
                                final int     type,   final byte    size,
                                final boolean signed, final boolean real,
                                org.opengis.coverage.SampleDimensionType geoAPI)
    {
        super(name, value);
        this.type   = type;
        this.size   = size;
        this.signed = signed;
        this.real   = real;
        this.geoAPI = geoAPI;
    }
    
    /**
     * Return the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.cv.CV_SampleDimensionType}.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static SampleDimensionType getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Return the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.coverage.SampleDimensionType}.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static SampleDimensionType getEnum(final org.opengis.coverage.SampleDimensionType value)
            throws NoSuchElementException
    {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].geoAPI.equals(value)) {
                return ENUMS[i];
            }
        }
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Returns the enum for the smallest type capable to hold the specified range of values.
     *
     * @param  range The range of values.
     * @return The enum for the specified range.
     */
    public static SampleDimensionType getEnum(final Range range) {
        final Class type = range.getElementClass();
        if (Double.class.isAssignableFrom(type)) {
            return DOUBLE;
        }
        if (Float.class.isAssignableFrom(type)) {
            return FLOAT;
        }
        long min = ((Number) range.getMinValue()).longValue();
        long max = ((Number) range.getMaxValue()).longValue();
        if (!range.isMinIncluded()) min++;
        if (!range.isMaxIncluded()) max--;
        return getEnum(min, max);
    }

    /**
     * Returns the enum for a type capable to hold the specified range of values.
     * An heuristic approach is used for non-integer values.
     *
     * @param  min  The lower value, inclusive.
     * @param  max  The upper value, <strong>inclusive</strong> as well.
     * @return The enum for the specified range.
     */
    static SampleDimensionType getEnum(double min, double max) {
        final long lgMin = (long) min;
        if (lgMin == min) {
            final long lgMax = (long) max;
            if (lgMax == max) {
                return getEnum(lgMin, lgMax);
            }
        }
        min = Math.abs(min);
        max = Math.abs(max);
        if (Math.min(min,max)>=Float.MIN_VALUE && Math.max(min,max)<=Float.MAX_VALUE) {
            return FLOAT;
        }
        return DOUBLE;
    }

    /**
     * Returns the enum for the smallest type capable to hold the specified range of values.
     *
     * @param  min  The lower value, inclusive.
     * @param  max  The upper value, <strong>inclusive</strong> as well.
     * @return The enum for the specified range.
     */
    static SampleDimensionType getEnum(final long min, final long max) {
        if (min >= 0) {
            if (max < (1L <<  1)) return BIT;
            if (max < (1L <<  2)) return DOUBLET;
            if (max < (1L <<  4)) return NIBBLE;
            if (max < (1L <<  8)) return UBYTE;
            if (max < (1L << 16)) return USHORT;
            if (max < (1L << 32)) return UINT;
        } else {
            if (min>=Byte   .MIN_VALUE && max<=Byte   .MAX_VALUE) return BYTE;
            if (min>=Short  .MIN_VALUE && max<=Short  .MAX_VALUE) return SHORT;
            if (min>=Integer.MIN_VALUE && max<=Integer.MAX_VALUE) return INT;
        }
        return FLOAT;
    }

    /**
     * Return the enum for the specified sample model and band number.
     * If the sample model use an undefined data type, then this method
     * returns <code>null</code>.
     *
     * @param  model The sample model.
     * @param  band  The band to query.
     * @return The enum for the specified sample model and band number.
     * @throws IllegalArgumentException if the band number is not in the valid range.
     */
    public static SampleDimensionType getEnum(final SampleModel model, final int band)
        throws IllegalArgumentException
    {
        if (band<0 || band>=model.getNumBands()) {
            throw new IllegalArgumentException(
                    Errors.format(ErrorKeys.BAD_BAND_NUMBER_$1, new Integer(band)));
        }
        boolean signed = true;
        switch (model.getDataType()) {
            case DataBuffer.TYPE_DOUBLE: return DOUBLE;
            case DataBuffer.TYPE_FLOAT:  return FLOAT;
            case DataBuffer.TYPE_USHORT: signed=false; // Fall through
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_BYTE: {
                switch (model.getSampleSize(band)) {
                    case  1: return BIT;
                    case  2: return DOUBLET;
                    case  4: return NIBBLE;
                    case  8: return signed ? BYTE  : UBYTE;
                    case 16: return signed ? SHORT : USHORT;
                    case 32: return signed ? INT   : UINT;
                }
            }
        }
        return null;
    }

    /**
     * Returns this enum's name in the specified locale. If no name is available for
     * the specified locale, a default one will be used.  For example, the localized
     * name for {@link #SHORT} is "<cite>16 bits unsigned integer</cite>" in English
     * and "<cite>Entier non-signé sur 16 bits</cite>" in French.
     *
     * @param  locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Vocabulary.getResources(locale).getString(VocabularyKeys.DATA_TYPE_$2,
                new Integer(real ? 2 : signed ? 1 : 0), new Integer(size));
    }

    /**
     * Returns the {@link DataBuffer} type. This is one of the following constants:
     * {@link DataBuffer#TYPE_BYTE},  {@link DataBuffer#TYPE_USHORT},
     * {@link DataBuffer#TYPE_SHORT}, {@link DataBuffer#TYPE_INT},
     * {@link DataBuffer#TYPE_FLOAT}, {@link DataBuffer#TYPE_DOUBLE}.
     */
    public int getDataBufferType() {
        return type;
    }

    /**
     * Returns the size in bits. The value range from 1 to 64. This is similar, but
     * different than {@link DataBuffer#getDataTypeSize}, which have values ranging
     * from 8 to 64.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns <code>true</code> for signed sample type.
     * <code>true</code> is returned for {@link #BYTE}, {@link #SHORT},
     * {@link #INT}, {@link #FLOAT} and {@link #DOUBLE}.
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * Returns <code>true</code> for floating-point data type.
     * <code>true</code> is returned for {@link #FLOAT} and {@link #DOUBLE}.
     */
    public boolean isFloatingPoint() {
        return real;
    }

    /**
     * Wrap the specified value into a number of this sample data type. If the
     * value can't fit in a {@link Number} object of this sample type, then a wider
     * type is choosen unless <code>allowWidening</code> is <code>false</code>.
     *
     * @param  value The value to wrap in a {@link Number} object.
     * @param  allowWidening <code>true</code> if this method is allowed to returns
     *         a wider type than the usual one for this sample type.
     * @return The value as a {@link Number}.
     * @throws IllegalArgumentException if <code>allowWidening</code> is <code>false</code>
     *         and the specified <code>value</code> can't fit in a {@link Number} of this
     *         sample type.
     */
    Number wrapSample(double value, boolean allowWidening) throws IllegalArgumentException {
        return wrapSample(value, getValue(), allowWidening);
    }

    /**
     * Wrap the specified value into a number of the specified data type.  The type
     * must be one constant of the {@link CV_SampleDimensionType} interface. If the
     * value can't fit in the specified type, then a wider type is choosen unless
     * <code>allowWidening</code> is <code>false</code>.
     *
     * @param  value The value to wrap in a {@link Number} object.
     * @param  type A constant from the {@link CV_SampleDimensionType} interface.
     * @param  allowWidening <code>true</code> if this method is allowed to returns
     *         a wider type than the usual one for the specified <code>type</code>.
     * @return The value as a {@link Number}.
     * @throws IllegalArgumentException if <code>type</code> is not a recognized constant.
     * @throws IllegalArgumentException if <code>allowWidening</code> is <code>false</code>
     *         and the specified <code>value</code> can't fit in the specified sample type.
     */
    static Number wrapSample(final double value, final int type, final boolean allowWidening)
        throws IllegalArgumentException
    {
        switch (type) {
            case CV_SampleDimensionType.CV_1BIT: // Fall through
            case CV_SampleDimensionType.CV_2BIT: // Fall through
            case CV_SampleDimensionType.CV_4BIT: // Fall through
            case CV_SampleDimensionType.CV_8BIT_S: {
                final byte candidate = (byte) value;
                if (candidate == value) {
                    return new Byte(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case CV_SampleDimensionType.CV_8BIT_U: // Fall through
            case CV_SampleDimensionType.CV_16BIT_S: {
                final short candidate = (short) value;
                if (candidate == value) {
                    return new Short(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case CV_SampleDimensionType.CV_16BIT_U: // Fall through
            case CV_SampleDimensionType.CV_32BIT_S: {
                final int candidate = (int) value;
                if (candidate == value) {
                    return new Integer(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case CV_SampleDimensionType.CV_32BIT_U: {
                final long candidate = (long) value;
                if (candidate == value) {
                    return new Long(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case CV_SampleDimensionType.CV_32BIT_REAL: {
                if (!allowWidening || Math.abs(value) <= Float.MAX_VALUE) {
                    return new Float((float) value);
                }
                // Fall through
            }
            case CV_SampleDimensionType.CV_64BIT_REAL: {
                return new Double(value);
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(type));
            }
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }

    /**
     * Use a single instance of {@link SampleDimensionType} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        try {
            return getEnum(getValue());
        } catch (NoSuchElementException cause) {
            InvalidObjectException e = new InvalidObjectException("Unknow enum");
            e.initCause(cause);
            throw e;
        }
    }
}
