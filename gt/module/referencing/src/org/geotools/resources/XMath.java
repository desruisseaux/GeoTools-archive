/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le D�veloppement
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
package org.geotools.resources;

// J2SE dependencies
import java.text.ChoiceFormat;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Simple mathematical functions. Some of these functions will be removed if JavaSoft provide a
 * standard implementation or fix some issues in Bug Parade:
 * <p>
 * <ul>
 *   <li><a href="http://developer.java.sun.com/developer/bugParade/bugs/4074599.html">Implement log10 (base 10 logarithm)</a></li>
 *   <li><a href="http://developer.java.sun.com/developer/bugParade/bugs/4358794.html">implement pow10 (power of 10) with optimization for integer powers</a>/li>
 *   <li><a href="http://developer.java.sun.com/developer/bugParade/bugs/4461243.html">Math.acos is very slow</a></li>
 * </ul>
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class XMath {
    /**
     * Natural logarithm of 10.
     * Approximately equal to 2.302585.
     */
    public static final double LN10 = 2.3025850929940456840179914546844;
    
    /**
     * Table of some integer powers of 10. Used
     * for fast computation of {@link #pow10(int)}.
     */
    private static final double[] POW10 = {
        1E+00, 1E+01, 1E+02, 1E+03, 1E+04, 1E+05, 1E+06, 1E+07, 1E+08, 1E+09,
        1E+10, 1E+11, 1E+12, 1E+13, 1E+14, 1E+15, 1E+16, 1E+17, 1E+18, 1E+19,
        1E+20, 1E+21, 1E+22
    };

    /**
     * Do not allow instantiation of this class.
     */
    private XMath() {
    }

    /**
     * Combute the cubic root of the specified value. This is method will be removed if
     * <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4633024.html">RFE
     * 4633024</A> is implemented.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static double cbrt(final double x) {
        return Math.pow(x, 1.0/3);
    }
    
    /**
     * Compute the hypotenuse (<code>sqrt(x�+y�)</code>).
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static double hypot(final double x, final double y) {
        return Math.sqrt(x*x + y*y);
    }

    /**
     * Compute the logarithm in base 10. See
     * http://developer.java.sun.com/developer/bugParade/bugs/4074599.html.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static double log10(final double x) {
        return Math.log(x) / LN10;
    }
    
    /**
     * Compute 10 power <var>x</var>.
     */
    public static double pow10(final double x) {
        final int ix = (int) x;
        if (ix == x) {
            return pow10(ix);
        } else {
            return Math.pow(10, x);
        }
    }
    
    /**
     * Compute <var>x</var> to the power of 10. This computation is very fast
     * for small power of 10 but has some rounding error issues (see
     * http://developer.java.sun.com/developer/bugParade/bugs/4358794.html).
     */
    public static strictfp double pow10(final int x) {
        if (x >= 0) {
            if (x < POW10.length) {
                return POW10[x];
            }
        } else if (x != Integer.MIN_VALUE) {
            final int nx = -x;
            if (nx < POW10.length) {
                return 1 / POW10[nx];
            }
        }
        try {
            /*
             * Note: Method 'Math.pow(10,x)' has rounding errors: it doesn't
             *       always return the closest IEEE floating point
             *       representation. Method 'Double.parseDouble("1E"+x)' gives
             *       as good or better numbers for ALL integer powers, but is
             *       much slower.  The difference is usually negligible, but
             *       powers of 10 are a special case since they are often
             *       used for scaling axes or formatting human-readable output.
             *       We hope that the current workaround is only temporary.
             *       (see http://developer.java.sun.com/developer/bugParade/bugs/4358794.html).
             */
            return Double.parseDouble("1E"+x);
        } catch (NumberFormatException exception) {
            return StrictMath.pow(10, x);
        }
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null or {@code NaN} and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static int sgn(final double x) {
        if (x>0) return +1;
        if (x<0) return -1;
        else     return  0;
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null or {@code NaN} and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static int sgn(final float x) {
        if (x>0) return +1;
        if (x<0) return -1;
        else     return  0;
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static int sgn(long x) {
        if (x>0) return +1;
        if (x<0) return -1;
        else     return  0;
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static int sgn(int x) {
        if (x>0) return +1;
        if (x<0) return -1;
        else     return  0;
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static short sgn(short x) {
        if (x>0) return (short) +1;
        if (x<0) return (short) -1;
        else     return (short)  0;
    }

    /**
     * Returns the sign of <var>x</var>. This method returns
     *    -1 if <var>x</var> is negative,
     *     0 if <var>x</var> is null and
     *    +1 if <var>x</var> is positive.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static byte sgn(byte x) {
        if (x>0) return (byte) +1;
        if (x<0) return (byte) -1;
        else     return (byte)  0;
    }

    /**
     * Round the specified value, providing that the difference between the original value and
     * the rounded value is not greater than the specified amount of floating point units. This
     * method can be used for hiding floating point error likes 2.9999999996.
     *
     * @param  value The value to round.
     * @param  flu The amount of floating point units.
     * @return The rounded value, of {@code value} if it was not close enough to an integer.
     */
    public static double round(final double value, int flu) {
        final double target = Math.rint(value);
        if (value != target) {
            final boolean pos = (value < target);
            double candidate = value;
            while (--flu >= 0) {
                candidate = pos ? next(candidate) : previous(candidate);
                if (candidate == target) {
                    return target;
                }
            }
        }
        return value;
    }

    /**
     * Try to remove at least {@code n} fraction digits in the string representation of
     * the specified value. This method try small changes to {@code value}, by adding or
     * substracting a maximum of 4 ulps. If there is no small change that remove at least
     * {@code n} fraction digits, then the value is returned unchanged. This method is
     * used for hiding rounding errors, like in conversions from radians to degrees.
     *
     * <P>Example: {@code XMath.fixRoundingError(-61.500000000000014, 12)} returns
     * {@code -61.5}.
     *
     * @param  value The value to fix.
     * @param  n The minimum amount of fraction digits.
     * @return The fixed value, or the unchanged {@code value} if there is no small change
     *         that remove at least {@code n} fraction digits.
     */
    public static double fixRoundingError(final double value, int n) {
        double lower = value;
        double upper = value;
        n = countFractionDigits(value) - n;
        if (n > 0) {
            for (int i=0; i<4; i++) {
                if (countFractionDigits(lower = previous(lower)) <= n) return lower;
                if (countFractionDigits(upper = next    (upper)) <= n) return upper;
            }
        }
        return value;
    }

    /**
     * Count the fraction digits in the string representation of
     * the specified value. This method is equivalent to a call to
     * <code>{@linkplain Double#toString(double) Double#toString}(value)</code>
     * and counting the number of digits after the decimal separator.
     */
    public static int countFractionDigits(final double value) {
        final String asText = Double.toString(value);
        final int exp = asText.indexOf('E');
        int upper, power;
        if (exp >= 0) {
            upper = exp;
            power = Integer.parseInt(asText.substring(exp+1));
        } else {
            upper = asText.length();
            power = 0;
        }
        while ((asText.charAt(--upper)) == '0');
        return Math.max(upper - asText.indexOf('.') - power, 0);
    }

    /**
     * Finds the least float greater than d (if positive == true),
     * or the greatest float less than d (if positive == false).
     * If NaN, returns same value. This code is an adaptation of
     * {@link java.text.ChoiceFormat#nextDouble}.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    private static float next(final float f, final boolean positive) {
        final int SIGN             = 0x80000000;
        final int POSITIVEINFINITY = 0x7F800000;

        // Filter out NaN's
        if (Float.isNaN(f)) {
            return f;
        }

        // Zero's are also a special case
        if (f == 0f) {
            final float smallestPositiveFloat = Float.intBitsToFloat(1);
            return (positive) ? smallestPositiveFloat : -smallestPositiveFloat;
        }

        // If entering here, d is a nonzero value.
        // Hold all bits in a int for later use.
        final int bits = Float.floatToIntBits(f);

        // Strip off the sign bit.
        int magnitude = bits & ~SIGN;

        // If next float away from zero, increase magnitude.
        // Else decrease magnitude
        if ((bits > 0) == positive) {
            if (magnitude != POSITIVEINFINITY) {
                magnitude++;
            }
        } else {
            magnitude--;
        }

        // Restore sign bit and return.
        final int signbit = bits & SIGN;
        return Float.intBitsToFloat(magnitude | signbit);
    }

    /**
     * Finds the least float greater than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static float next(final float f) {
        return next(f, true);
    }

    /**
     * Finds the greatest float less than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static float previous(final float f) {
        return next(f, false);
    }

    /**
     * Finds the least double greater than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @see java.text.ChoiceFormat#nextDouble
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static double next(final double f) {
        return ChoiceFormat.nextDouble(f);
    }

    /**
     * Finds the greatest double less than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @see java.text.ChoiceFormat#previousDouble
     *
     * @todo Remove this method when we will be allowed to use J2SE 1.5.
     */
    public static double previous(final double f) {
        return ChoiceFormat.previousDouble(f);
    }

    /**
     * Returns the next or previous representable number. If {@code amount} is equals to
     * {@code 0}, then this method returns the {@code value} unchanged. Otherwise,
     * The operation performed depends on the specified {@code type}:
     * <ul>
     *   <li><p>If the {@code type} is {@link Double}, then this method is
     *       equivalent to invoking   {@link #previous(double)} if {@code amount} is equals to
     *       {@code -1}, or invoking {@link #next(double)} if {@code amount} is equals to
     *       {@code +1}. If {@code amount} is smaller than {@code -1} or greater
     *       than {@code +1}, then this method invokes {@link #previous(double)} or
     *       {@link #next(double)} in a loop for {@code abs(amount)} times.</p></li>
     *
     *   <li><p>If the {@code type} is {@link Float}, then this method is
     *       equivalent to invoking   {@link #previous(float)} if {@code amount} is equals to
     *       {@code -1}, or invoking {@link #next(float)} if {@code amount} is equals to
     *       {@code +1}. If {@code amount} is smaller than {@code -1} or greater
     *       than {@code +1}, then this method invokes {@link #previous(float)} or
     *       {@link #next(float)} in a loop for {@code abs(amount)} times.</p></li>
     *
     *   <li><p>If the {@code type} is an {@linkplain #isInteger integer}, then invoking
     *       this method is equivalent to computing {@code value + amount}.</p></li>
     * </ul>
     *
     * @param type    The type. Should be the class of {@link Double}, {@link Float},
     *                {@link Long}, {@link Integer}, {@link Short} or {@link Byte}.
     * @param value   The number to rool.
     * @param amount  -1 to return the previous representable number,
     *                +1 to return the next representable number, or
     *                 0 to return the number with no change.
     * @return One of previous or next representable number as a {@code double}.
     * @throws IllegalArgumentException if {@code type} is not one of supported types.
     */
    public static double rool(final Class type, double value, int amount)
            throws IllegalArgumentException
    {
        if (Double.class.isAssignableFrom(type)) {
            if (amount<0) {
                do {
                    value = previous(value);
                } while (++amount != 0);
            } else if (amount!=0) {
                do {
                    value = next(value);
                } while (--amount != 0);
            }
            return value;
        }
        if (Float.class.isAssignableFrom(type)) {
            float vf = (float)value;
            if (amount<0) {
                do {
                    vf = previous(vf);
                } while (++amount != 0);
            } else if (amount!=0) {
                do {
                    vf = next(vf);
                } while (--amount != 0);
            }
            return vf;
        }
        if (isInteger(type)) {
            return value + amount;
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNSUPPORTED_DATA_TYPE_$1,
                                                         Utilities.getShortName(type)));
    }

    /**
     * Returns {@code true} if the specified {@code type} is one of real
     * number types. Real number types includes {@link Float} and {@link Double}.
     *
     * @param  type The type to test (may be {@code null}).
     * @return {@code true} if {@code type} is the class {@link Float} or {@link Double}.
     */
    public static boolean isReal(final Class type) {
        return type!=null &&
               Double.class.isAssignableFrom(type) ||
                Float.class.isAssignableFrom(type);
    }

    /**
     * Returns {@code true} if the specified {@code type} is one of integer types.
     * Integer types includes {@link Long}, {@link Integer}, {@link Short} and {@link Byte}.
     *
     * @param  type The type to test (may be {@code null}).
     * @return {@code true} if {@code type} is the class {@link Long}, {@link Integer},
     *         {@link Short} or {@link Byte}.
     */
    public static boolean isInteger(final Class type) {
        return type!=null &&
               Long.class.isAssignableFrom(type) ||
            Integer.class.isAssignableFrom(type) ||
              Short.class.isAssignableFrom(type) ||
               Byte.class.isAssignableFrom(type);
    }

    /**
     * Returns the number of bits used by number of the specified type.
     *
     * @param  type The type (may be {@code null}).
     * @return The number of bits, or 0 if unknow.
     *
     * @todo Use the predefined constants when we will be allowed to use J2SE 1.5.
     */
    public static int getBitCount(final Class type) {
        if (Double   .class.isAssignableFrom(type)) return 64;
        if (Float    .class.isAssignableFrom(type)) return 32;
        if (Long     .class.isAssignableFrom(type)) return 64;
        if (Integer  .class.isAssignableFrom(type)) return 32;
        if (Short    .class.isAssignableFrom(type)) return 16;
        if (Byte     .class.isAssignableFrom(type)) return  8;
        if (Character.class.isAssignableFrom(type)) return 16;
        if (Boolean  .class.isAssignableFrom(type)) return  1;
        return 0;
    }

    /**
     * Change a primitive class to its wrapper (e.g. {@code double} to {@link Double}).
     * If the specified class is not a primitive type, then it is returned unchanged.
     *
     * @param  type The primitive type (may be {@code null}).
     * @return The type as a wrapper.
     */
    public static Class primitiveToWrapper(final Class type) {
        if (Character.TYPE.equals(type)) return Character.class;
        if (Boolean  .TYPE.equals(type)) return Boolean  .class;
        if (Byte     .TYPE.equals(type)) return Byte     .class;
        if (Short    .TYPE.equals(type)) return Short    .class;
        if (Integer  .TYPE.equals(type)) return Integer  .class;
        if (Long     .TYPE.equals(type)) return Long     .class;
        if (Float    .TYPE.equals(type)) return Float    .class;
        if (Double   .TYPE.equals(type)) return Double   .class;
        return type;
    }
}
