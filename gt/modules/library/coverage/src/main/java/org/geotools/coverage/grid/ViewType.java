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
package org.geotools.coverage.grid;

// J2SE and JAI dependencies
import java.io.Serializable;
import java.awt.image.ColorModel;                // For javadoc
import java.awt.image.IndexColorModel;           // For javadoc
import javax.media.jai.JAI;                      // For javadoc
import javax.media.jai.InterpolationNearest;     // For javadoc
import javax.media.jai.InterpolationBilinear;    // For javadoc
import javax.media.jai.InterpolationBicubic;     // For javadoc
import javax.media.jai.operator.ScaleDescriptor; // For javadoc


/**
 * Enumerates different "views" over a given coverage. Coverage views represent the same data
 * in different ways. Some views are more appropriate than others depending of the kind of work
 * to be performed. For example numerical computations on meteorological or oceanographical data
 * should be performed on the {@linkplain #GEOPHYSICS geophysics} view, while renderings are
 * better performed with the {@linkplain #DISPLAYABLE displayable} view.
 * <p>
 * Different views are sometime synonymous for a given coverage. For example the
 * {@linkplain #NATIVE native} and {@linkplain #DISPLAYABLE displayable} views are identical
 * when the coverage values are unsigned 8 or 16 bits integers, but distincts if the native
 * values are <em>signed</em> integers. This is because in the later case, the negative values
 * can not be processed directly by an {@linkplain IndexColorModel index color model}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Should be an enum when we will be allowed to compile for J2SE 1.5.
 */
public final class ViewType implements Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -1926667155583006688L;

    /**
     * The name for this enum.
     */
    private final String name;

    /**
     * {@code true} if interpolations other than {@linkplain InterpolationNearest
     * nearest neighbor} are allowed.
     */
    private final boolean interpolationAllowed;

    /**
     * {@code true} if the replacement of {@linkplain IndexColorModel index color model}
     * is allowed.
     *
     * @see JAI#KEY_REPLACE_INDEX_COLOR_MODEL
     */
    private final boolean colorSpaceConversionAllowed;

    /**
     * Coverage data come directly from some source (typically a file) and are unprocessed.
     * This view doesn't have any of the restrictions imposed by other views: values may be
     * integers or floating point values, negative values are allowed, and missing data may
     * be represented by "pad values" like -9999. This view is generally not suitable for
     * renderings or numerical computations. However in some special cases, this view may
     * be identical to an other view (see those other views for a more exhaustive list of
     * their conditions):
     * <p>
     * <ul>
     *   <li>If the values are stored as unsigned integers, then the native view may
     *       be identical to the {@linkplain #DISPLAYABLE displayable} view.</li>
     *   <li>If all missing values are represented by {@linkplain Float#isNaN some kind of
     *       NaN values}, then the native view may be identical to the
     *       {@linkplain #GEOPHYSICS geophysics} view.</li>
     * </ul>
     * <p>
     * Interpolations other than {@linkplain InterpolationNearest nearest neighbor} are
     * not allowed. Conversions to the RGB color space are not allowed neither, for the
     * same reasons than the {@linkplain #DISPLAYABLE displayable} view.
     */
    public static final ViewType NATIVE = new ViewType("NATIVE", false, false);

    /**
     * Coverage data are compatible with common Java2D {@linkplain ColorModel color models}.
     * This usually imply that values are restricted to unsigned integers. This view is often
     * identical to the {@linkplain #NATIVE native} view if the values on the originating
     * device were already unsigned.
     * <p>
     * Conversions to the RGB color space are not allowed, because the data are often related
     * to {@linkplain #GEOPHYSICS geophysics} values in some way. For example the coverage may
     * contains <cite>Sea Surface Temperature</cite> (SST) data packed as 8 bits integers and
     * convertible to degrees Celsius using the following formula: <var>temperature</var> =
     * <var>pixel_value</var> &times; 0.15 - 3. A conversion to RGB space would lose this
     * relationship, and any oceanographical calculation accidentaly performed on this space
     * would produce wrong results.
     * <p>
     * Interpolations other than {@linkplain InterpolationNearest nearest neighbor} are not
     * allowed, because some special values are often used as pad values for missing data. An
     * interpolation between a "real" value (for example a value convertible to the above-cited
     * SST) and "pad" value would produce a wrong result.
     */
    public static final ViewType DISPLAYABLE = new ViewType("DISPLAYABLE", false, false);

    /**
     * Coverage data are the values of some geophysics phenomenon, for example an elevation
     * in metres or a temperature in Celsius degrees. Values are typically floating point
     * numbers ({@code float} or {@code double} primitive type), but this is not mandatory
     * if there is never fractional parts or missing values in a particular coverage.
     * <p>
     * If the coverage contains some "no data" values, then those missing values
     * <strong>must</strong> be represented by {@link Float#NaN} or {@link Double#NaN}
     * constant, or any other value in the NaN range as {@linkplain Float#intBitsToFloat
     * explained there}. Real numbers used as "pad values" like {@code -9999} are
     * <strong>not</strong> allowed.
     * <p>
     * Interpolations ({@linkplain InterpolationBilinear bilinear},
     * {@linkplain InterpolationBicubic bicubic}, <cite>etc.</cite>) are allowed.
     * If there is some missing values around the interpolation point, then the
     * result is a {@code NaN} value.
     * <p>
     * Conversions to RGB color space is not allowed. All computations (including
     * interpolations) must be performed in this geophysics space.
     */
    public static final ViewType GEOPHYSICS = new ViewType("GEOPHYSICS", true, false);

    /**
     * Coverage data have no meaning other than visual color. It is not an elevation map for
     * example (in which case the coverage would rather be described as {@linkplain #GEOPHYSICS
     * geophysics}).
     * <p>
     * Conversions to the RGB color space are allowed. Because the coverage has no geophysics
     * meaning other than visual color, there is no significant data lose in the replacement
     * of {@linkplain IndexColorModel index color model}.
     * <p>
     * Interpolation are not allowed on indexed values. They must be performed on the RGB
     * or similar color space instead.
     */
    public static final ViewType PHOTOGRAPHIC = new ViewType("PHOTOGRAPHIC", false, true);

    /**
     * Creates a new instance of {@code ViewType}.
     */
    private ViewType(final String  name,
                     final boolean interpolationAllowed,
                     final boolean colorSpaceConversionAllowed)
    {
        this.name                        = name;
        this.interpolationAllowed        = interpolationAllowed;
        this.colorSpaceConversionAllowed = colorSpaceConversionAllowed;
    }

    /**
     * Returns {@code true} if interpolations other than {@linkplain InterpolationNearest
     * nearest neighbor} are allowed. Those interpolations require the following conditions:
     * <p>
     * <ul>
     *   <li>Values are either {@linkplain #GEOPHYSICS geophysics} values, or related to
     *       geophysics values through a linear relationship over all the range of possible
     *       values (including "no data" values).</li>
     *   <li>There is no "pad values". Missing values, if any, are represented by some
     *       {@link Float#NaN NaN} values}.</li>
     * </ul>
     * <p>
     * This method may conservatively returns {@code false} if unsure. If interpolations
     * are wanted but not allowed, then users should try to convert the coverage to the
     * {@linkplain #GEOPHYSICS geophysics} space, which supports interpolations. If no
     * geophysics view is available, then users may convert the image to the RGB space
     * if {@linkplain #isColorSpaceConversionAllowed color space conversion is allowed}.
     * Interpolations in the RGB space produce nice-looking images, but the pixel values
     * lose all geophysical meaning. If the color space conversion is not allowed, then
     * then users should stick with {@linkplain InterpolationNearest nearest neighbor}
     * interpolation.
     */
    public boolean isInterpolationAllowed() {
        return interpolationAllowed;
    }

    /**
     * Returns {@code true} if the replacement of {@linkplain IndexColorModel index color model}
     * is allowed. Such replacements may occurs during some operations requirying interpolations,
     * like {@linkplain ScaleDescriptor scale}, in order to produce images that look nicer.
     * However such replacements should be attempted only in last resort (interpolations in the
     * {@linkplain #GEOPHYSICS geophysics} space should be preferred) and only if the coverage
     * data don't have any meaning other than visual color, as in {@linkplain #PHOTOGRAPHIC
     * photographic} images.
     *
     * @see JAI#KEY_REPLACE_INDEX_COLOR_MODEL
     */
    public boolean isColorSpaceConversionAllowed() {
        return colorSpaceConversionAllowed;
    }

    /**
     * Returns a hash value for this enum.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ name.hashCode();
    }

    /**
     * Compares this enum with the specified object for equality.
     */
    public boolean equals(final Object object) {
        return (object instanceof ViewType) && name.equals(((ViewType) object).name);
    }

    /**
     * Returns a string representation of this enum.
     */
    public String toString() {
        return name;
    }
}
