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
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.units.Unit;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.coverage.ColorInterpretation;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.PaletteInterpretation;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.resources.ClassChanger;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.XMath;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.util.NumberRange;


/**
 * Describes the data values for a coverage. For a grid coverage a sample dimension is a band.
 * Sample values in a band may be organized in categories.  This <code>SampleDimension</code>
 * implementation is capable to differenciate <em>qualitative</em> and <em>quantitative</em>
 * categories. For example an image of sea surface temperature (SST) could very well defines
 * the following categories:
 *
 * <blockquote><pre>
 *   [0]       : no data
 *   [1]       : cloud
 *   [2]       : land
 *   [10..210] : temperature to be converted into Celsius degrees through a linear equation
 * </pre></blockquote>
 *
 * In this example, sample values in range <code>[10..210]</code> defines a quantitative category,
 * while all others categories are qualitative. The difference between those two kinds of category
 * is that the {@link Category#getSampleToGeophysics} method returns a non-null transform if and
 * only if the category is quantitative.
 *
 * @version $Id$
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 */
public class SampleDimension implements org.opengis.coverage.SampleDimension, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6026936545776852758L;

    /**
     * An empty array of metadata names.
     */
    private static final String[] EMPTY_METADATA = new String[0];

    /**
     * Ordinal values of {@link SampleDimensionType}. Used for a "switch" statement.
     * Note: with J2SE 1.5, it may no longer be necessary.
     */
    private static final int UNSIGNED_1BIT   =  0,
                             UNSIGNED_2BITS  =  1,
                             UNSIGNED_4BITS  =  2,
                             UNSIGNED_8BITS  =  3,
                             SIGNED_8BITS    =  4,
                             UNSIGNED_16BITS =  5,
                             SIGNED_16BITS   =  6,
                             UNSIGNED_32BITS =  7,
                             SIGNED_32BITS   =  8,
                             REAL_32BITS     =  9,
                             REAL_64BITS     = 10;

    /**
     * A sample dimension wrapping the list of categories <code>CategoryList.inverse</code>.
     * This object is constructed and returned by {@link #geophysics}. Constructed when first
     * needed, but serialized anyway because it may be a user-supplied object.
     */
    private SampleDimension inverse;

    /**
     * The category list for this sample dimension, or <code>null</code> if this sample
     * dimension has no category. This field is read by <code>SampleTranscoder</code> only.
     */
    final CategoryList categories;

    /**
     * <code>true</code> if all categories in this sample dimension have been already scaled
     * to geophysics ranges. If <code>true</code>, then the {@link #getSampleToGeophysics()}
     * method should returns an identity transform. Note that the opposite do not always hold:
     * an identity transform doesn't means that all categories are geophysics. For example,
     * some qualitative categories may map to some values differents than <code>NaN</code>.
     * <br><br>
     * Assertions:
     *  <ul>
     *    <li><code>isGeophysics</code> == <code>categories.isScaled(true)</code>.</li>
     *    <li><code>isGeophysics</code> != <code>categories.isScaled(false)</code>, except
     *        if <code>categories.geophysics(true) == categories.geophysics(false)</code></li>
     * </ul>
     */
    private final boolean isGeophysics;

    /**
     * <code>true</code> if this sample dimension has at least one qualitative category.
     * An arbitrary number of qualitative categories is allowed, providing their sample
     * value ranges do not overlap. A sample dimension can have both qualitative and
     * quantitative categories.
     */
    private final boolean hasQualitative;

    /**
     * <code>true</code> if this sample dimension has at least one quantitative category.
     * An arbitrary number of quantitative categories is allowed, providing their sample
     * value ranges do not overlap.
     * <br><br>
     * If <code>sampleToGeophysics</code> is non-null, then <code>hasQuantitative</code>
     * <strong>must</strong> be true.  However, the opposite do not hold in all cases: a
     * <code>true</code> value doesn't means that <code>sampleToGeophysics</code> should
     * be non-null.
     */
    private final boolean hasQuantitative;

    /**
     * The {@link Category#getSampleToGeophysics sampleToGeophysics} transform used by every
     * quantitative {@link Category}, or <code>null</code>. This field may be null for two
     * reasons:
     *
     * <ul>
     *   <li>There is no quantitative category in this sample dimension.</li>
     *   <li>There is more than one quantitative category, and all of them
     *       don't use the same {@link Category#getSampleToGeophysics
     *       sampleToGeophysics} transform.</li>
     * </ul>
     *
     * This field is used by {@link #getOffset} and {@link #getScale}. The
     * {@link #getSampleToGeophysics} method may also returns directly this
     * value in some conditions.
     */
    private final MathTransform1D sampleToGeophysics;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;

    /**
     * Construct a sample dimension with no category.
     */
    public SampleDimension() {
        this((CategoryList) null);
    }
    
    /**
     * Constructs a sample dimension with a set of qualitative categories only.
     * This constructor expects only a sequence of category names for the values
     * contained in a sample dimension. This allows for names to be assigned to
     * numerical values. The first entry in the sequence relates to a cell value
     * of zero. For example: [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     * The created sample dimension will have no unit and a default set of colors.
     *
     * @param names Sequence of category names for the values contained in a sample dimension,
     *              as {@link String} or {@link InternationalString} objects.
     */
    public SampleDimension(final CharSequence[] names) {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(names));
    }
    
    /** Constructs a list of categories. Used by constructors only. */
    private static CategoryList list(final CharSequence[] names) {
        final Color[] colors = new Color[names.length];
        final double scale = 255.0/colors.length;
        for (int i=0; i<colors.length; i++) {
            final int r = (int)Math.round(scale*i);
            colors[i] = new Color(r,r,r);
        }
        return list(names, colors);
    }
    
    /**
     * Constructs a sample dimension with a set of qualitative categories and colors.
     * This constructor expects a sequence of category names for the values
     * contained in a sample dimension. This allows for names to be assigned to
     * numerical values. The first entry in the sequence relates to a cell value
     * of zero. For example: [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     * The created sample dimension will have no unit and a default set of colors.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension,
     *               as {@link String} or {@link InternationalString} objects.
     * @param colors Color to assign to each category. This array must have the same
     *               length than <code>names</code>.
     */
    public SampleDimension(final CharSequence[] names, final Color[] colors) {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(names, colors));
    }
    
    /** Constructs a list of categories. Used by constructors only. */
    private static CategoryList list(final CharSequence[] names, final Color[] colors) {
        if (names.length != colors.length) {
            throw new IllegalArgumentException(
                    Resources.format(ResourceKeys.ERROR_MISMATCHED_ARRAY_LENGTH));
        }
        final Category[] categories = new Category[names.length];
        for (int i=0; i<categories.length; i++) {
            categories[i] = new Category(names[i], colors[i], i);
        }
        return list(categories, null);
    }

    /**
     * Constructs a sample dimension with the specified properties. For convenience, any argument
     * which is not a {@code double} primitive can be {@code null}, and any {@linkplain CharSequence
     * char sequence} can be either a {@link String} or {@link InternationalString} object.
     * 
     * This constructor allows the construction of a {@code SampleDimension} without explicit
     * construction of {@link Category} objects. An heuristic approach is used for dispatching
     * the informations into a set of {@link Category} objects. However, this constructor still
     * less general and provides less fine-grain control than the constructor expecting an array
     * of {@link Category} objects.
     *
     * @param  description The sample dimension title or description, or {@code null} if none.
     *         This is the value to be returned by {@link #getDescription}.
     * @param  type The grid value data type (which indicate the number of bits for the data type),
     *         or {@code null} for computing it automatically from the range
     *         {@code [minimum..maximum]}. This is the value to be returned by
     *         {@link #getSampleDimensionType}.
     * @param  color The color interpretation, or {@code null} for a default value (usually
     *         {@link ColorInterpretation#PALETTE_INDEX PALETTE_INDEX}). This is the value to be
     *         returned by {@link #getColorInterpretation}.
     * @param  palette The color palette associated with the sample dimension, or {@code null}
     *         for a default color palette (usually grayscale). If {@code categories} is
     *         non-null, then both arrays usually have the same length. However, this constructor
     *         is tolerant on this array length. This is the value to be returned (indirectly) by
     *         {@link #getColorModel}.
     * @param  categories A sequence of category names for the values contained in the sample
     *         dimension, or {@code null} if none. This is the values to be returned by
     *         {@link #getCategoryNames}.
     * @param  nodata the values to indicate "no data", or {@code null} if none. This is the
     *         values to be returned by {@link #getNoDataValues}.
     * @param  minimum The lower value, inclusive. The {@code [minimum..maximum]} range may or
     *         may not includes the {@code nodata} values; the range will be adjusted as
     *         needed. If {@code categories} was non-null, then {@code minimum} is
     *         usually 0. This is the value to be returned by {@link #getMinimumValue}.
     * @param  maximum The upper value, <strong>inclusive</strong> as well. The
     *         {@code [minimum..maximum]} range may or may not includes the {@code nodata}
     *         values; the range will be adjusted as needed. If {@code categories} was non-null,
     *         then {@code maximum} is usually equals to {@code categories.length-1}. This
     *         is the value to be returned by {@link #getMaximumValue}.
     * @param  scale The value which is multiplied to grid values, or 1 if none. This is the value
     *         to be returned by {@link #getScale}.
     * @param  offset The value to add to grid values, or 0 if none. This is the value to be
     *         returned by {@link #getOffset}.
     * @param  unit The unit information for this sample dimension, or {@code null} if none.
     *         This is the value to be returned by {@link #getUnits}.
     *
     * @throws IllegalArgumentException if the range {@code [minimum..maximum]} is not valid.
     */
    public SampleDimension(final CharSequence  description,
                           final SampleDimensionType  type,
                           final ColorInterpretation color,
                           final Color[]           palette,
                           final CharSequence[] categories,
                           final double[]           nodata,
                           final double            minimum,
                           final double            maximum,
                           final double              scale,
                           final double             offset,
                           final Unit                 unit)
    {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").        
        this(list(description, type, color, palette, categories, nodata,
                  minimum, maximum, scale, offset, unit));
    }

    /** Constructs a list of categories. Used by constructors only. */
    private static CategoryList list(final CharSequence  description,
                                           SampleDimensionType  type,
                                           ColorInterpretation color,
                                     final Color[]           palette,
                                     final CharSequence[] categories,
                                     final double[]           nodata,
                                           double            minimum,
                                           double            maximum,
                                     final double              scale,
                                     final double             offset,
                                     final Unit                 unit)
    {
        if (Double.isInfinite(minimum) || Double.isInfinite(maximum) || !(minimum < maximum)) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_RANGE_$2,
                                               new Double(minimum), new Double(maximum)));
        }
        if (Double.isNaN(scale) || Double.isInfinite(scale) || scale==0) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                               "scale", new Double(scale)));
        }
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                               "offset", new Double(offset)));
        }
        if (type == null) {
            type = getSampleDimensionType(minimum, maximum);
        }
        if (color == null) {
            color = ColorInterpretation.PALETTE_INDEX;
        }
        final int     nameCount = (categories!=null) ? categories.length : 0;
        final int   nodataCount = (nodata    !=null) ?     nodata.length : 0;
        final List categoryList = new ArrayList(nameCount + nodataCount + 2);
        /*
         * STEP 1 - Add a qualitative category for each 'nodata' value.
         *          NAME:  Fetched from 'categories' if available, otherwise default to the value.
         *          COLOR: Fetched from 'palette' if available, otherwise use Category default.
         */
        for (int i=0; i<nodataCount; i++) {
            CharSequence name = null;
            final double padValue = nodata[i];
            final int    intValue = (int) Math.floor(padValue);
            if (intValue>=0 && intValue<nameCount) {
                if (intValue == padValue) {
                    // This category will be added in step 2 below.
                    continue;
                }
                name = categories[intValue];
            }
            final Number value = wrapSample(padValue, type, false);
            if (name == null) {
                name = value.toString();
            }
            final NumberRange range = new NumberRange(value.getClass(), value, value);
            final Color[] colors = ColorUtilities.subarray(palette, intValue, intValue+1);
            categoryList.add(new Category(name, colors, range, (MathTransform1D)null));
        }
        /*
         * STEP 2 - Add a qualitative category for each category name.
         *          RANGE: Fetched from the index (position) in the 'categories' array.
         *          COLOR: Fetched from 'palette' if available, otherwise use Category default.
         */
        if (nameCount != 0) {
            int lower = 0;
            for (int upper=1; upper<=categories.length; upper++) {
                if (upper!=categories.length &&
                        categories[lower].toString().trim().equalsIgnoreCase(
                        categories[upper].toString().trim()))
                {
                    // If there is a suite of categories with identical name,  create only one
                    // category with range [lower..upper] instead of one new category for each
                    // sample value.
                    continue;
                }
                final CharSequence name = categories[lower];
                Number min = wrapSample(lower,   type, false);
                Number max = wrapSample(upper-1, type, false);
                final Class classe;
                if (min.equals(max)) {
                    min = max;
                    classe = max.getClass();
                } else {
                    classe = ClassChanger.getWidestClass(min, max);
                    min = ClassChanger.cast(min, classe);
                    max = ClassChanger.cast(max, classe);
                }
                final NumberRange range = new NumberRange(classe, min, max);
                final Color[] colors = ColorUtilities.subarray(palette, lower, upper);
                categoryList.add(new Category(name, colors, range, (MathTransform1D)null));
                lower = upper;
            }
        }
        /*
         * STEP 3 - Changes some qualitative categories into quantitative ones.  The hard questions
         *          is: do we want to mark a category as "quantitative"?   OpenGIS has no notion of
         *          "qualitative" versus "quantitative" category. As an heuristic approach, we will
         *          look for quantitative category if:
         *
         *          - 'scale' and 'offset' do not map to an identity transform. Those
         *            coefficients can be stored in quantitative category only.
         *
         *          - 'nodata' were specified. If the user wants to declare "nodata" values,
         *            then we can reasonably assume that he have real values somewhere else.
         *
         *          - Only 1 category were created so far. A classified raster with only one
         *            category is useless. Consequently, it is probably a numeric raster instead.
         */
        boolean needQuantitative = false;
        if (scale!=1 || offset!=0 || nodataCount!=0 || categoryList.size()<=1) {
            needQuantitative = true;
            for (int i=categoryList.size(); --i>=0;) {
                Category category = (Category) categoryList.get(i);
                if (!category.isQuantitative()) {
                    final NumberRange range = category.getRange();
                    final Comparable min = range.getMinValue();
                    final Comparable max = range.getMaxValue();
                    if (min.compareTo(max) != 0) {
                        final double xmin = ((Number)min).doubleValue();
                        final double xmax = ((Number)max).doubleValue();
                        if (!rangeContains(xmin, xmax, nodata)) {
                            final InternationalString name = category.getName();
                            final Color[] colors = category.getColors();
                            category = new Category(name, colors, range, scale, offset);
                            categoryList.set(i, category);
                            needQuantitative = false;
                        }
                    }
                }
            }
        }
        /*
         * STEP 4 - Create at most one quantitative category for the remaining sample values.
         *          The new category will range from 'minimum' to 'maximum' inclusive, minus
         *          all ranges used by previous categories.  If there is no range left, then
         *          no new category will be created.  This step will be executed only if the
         *          information provided by the user seem to be incomplete.
         *
         *          Note that substractions way break a range into many smaller ranges.
         *          The naive algorithm used here try to keep the widest range.
         */
        if (needQuantitative) {
            boolean minIncluded = true;
            boolean maxIncluded = true;
            for (int i=categoryList.size(); --i>=0;) {
                final NumberRange range = ((Category) categoryList.get(i)).getRange();
                final double  min = range.getMinimum();
                final double  max = range.getMaximum();
                if (max-minimum < maximum-min) {
                    if (max >= minimum) {
                        // We are loosing some sample values in
                        // the lower range because of nodata values.
                        minimum = max;
                        minIncluded = !range.isMaxIncluded();
                    }
                } else {
                    if (min <= maximum) {
                        // We are loosing some sample values in
                        // the upper range because of nodata values.
                        maximum = min;
                        maxIncluded = !range.isMinIncluded();
                    }
                }
            }
            // If the remaining range is wide enough, add the category.
            if (maximum-minimum > (minIncluded && maxIncluded ? 0 : 1)) {
                Number min = wrapSample(minimum, type, false);
                Number max = wrapSample(maximum, type, false);
                final Class classe = ClassChanger.getWidestClass(min, max);
                min = ClassChanger.cast(min, classe);
                max = ClassChanger.cast(max, classe);
                final NumberRange range = new NumberRange(classe, min, minIncluded,
                                                                  max, maxIncluded);
                final Color[] colors = ColorUtilities.subarray(palette,
                                                     (int)Math.ceil (minimum),
                                                     (int)Math.floor(maximum));
                categoryList.add(new Category((description!=null) ? description : "(automatic)",
                                              colors, range, scale, offset));
                needQuantitative = false;
            }
        }
        /*
         * STEP 5 - Now, the list of categories should be complete. Construct a
         *          sample dimension appropriate for the type of palette used.
         */
        final Category[] cl = (Category[]) categoryList.toArray(new Category[categoryList.size()]);
        if (ColorInterpretation.PALETTE_INDEX.equals(color) ||
            ColorInterpretation.GRAY_INDEX.equals(color))
        {
            return list(cl, unit);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Constructs a sample dimension with an arbitrary set of categories, which may be both
     * quantitative and qualitative.   It is possible to specify more than one quantitative
     * categories, providing that their sample value ranges do not overlap.    Quantitative
     * categories can map sample values to geophysics values using arbitrary relation (not
     * necessarly linear).
     *
     * @param  categories The list of categories.
     * @param  units      The unit information for this sample dimension.
     *                    May be <code>null</code> if no category has units.
     *                    This unit apply to values obtained after the
     *                    {@link #getSampleToGeophysics sampleToGeophysics} transformation.
     * @throws IllegalArgumentException if <code>categories</code> contains incompatible
     *         categories. If may be the case for example if two or more categories have
     *         overlapping ranges of sample values.
     */
    public SampleDimension(Category[] categories, Unit units) throws IllegalArgumentException {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(categories, units));
    }

    /** Construct a list of categories. Used by constructors only. */
    private static CategoryList list(final Category[] categories, final Unit units) {
        if (categories == null) {
            return null;
        }
        CategoryList list = new CategoryList(categories, units);
        list = (CategoryList) Category.pool.canonicalize(list);
        if (CategoryList.isScaled(categories, false)) return list;
        if (CategoryList.isScaled(categories, true )) return list.inverse;
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_MIXED_CATEGORIES));
    }

    /**
     * Constructs a sample dimension with the specified list of categories.
     *
     * @param list The list of categories, or <code>null</code>.
     */
    private SampleDimension(final CategoryList list) {
        MathTransform1D main = null;
        boolean  isMainValid = true;
        boolean  qualitative = false;
        if (list != null) {
            for (int i=list.size(); --i>=0;) {
                final MathTransform1D candidate = ((Category)list.get(i)).getSampleToGeophysics();
                if (candidate == null) {
                    qualitative = true;
                    continue;
                }
                if (main != null) {
                    isMainValid &= main.equals(candidate);
                }
                main = candidate;
            }
            this.isGeophysics = list.isScaled(true);
        } else {
            this.isGeophysics = false;
        }
        this.categories         = list;
        this.hasQualitative     = qualitative;
        this.hasQuantitative    = (main != null);
        this.sampleToGeophysics = isMainValid ? main : null;
    }

    /**
     * Constructs a new sample dimension with the same categories and
     * units than the specified sample dimension.
     *
     * @param other The other sample dimension, or <code>null</code>.
     */
    protected SampleDimension(final SampleDimension other) {
        if (other != null) {
            inverse            = other.inverse;
            categories         = other.categories;
            isGeophysics       = other.isGeophysics;
            hasQualitative     = other.hasQualitative;
            hasQuantitative    = other.hasQuantitative;
            sampleToGeophysics = other.sampleToGeophysics;
        } else {
            // 'inverse' will be set when needed.
            categories         = null;
            isGeophysics       = false;
            hasQualitative     = false;
            hasQuantitative    = false;
            sampleToGeophysics = null;
        }
    }

    /**
     * Wrap the specified OpenGIS's sample dimension into a Geotools's implementation
     * of {@code SampleDimension}.
     *
     * @param sd The sample dimension to wrap into a Geotools implementation.
     */
    public static SampleDimension wrap(final org.opengis.coverage.SampleDimension sd) {
        if (sd instanceof SampleDimension) {
            return (SampleDimension) sd;
        }
        final int[][] palette = sd.getPalette();
        final Color[] colors;
        if (palette != null) {
            colors = new Color[palette.length];
            for (int i=0; i<colors.length; i++) {
                // Assuming RGB. It will be checked in the constructor.
                final int[] color = palette[i];
                colors[i] = new Color(color[0], color[1], color[2]);
            }
        } else {
            colors = null;
        }
        return new SampleDimension(sd.getDescription(),
                                   sd.getSampleDimensionType(),
                                   sd.getColorInterpretation(),
                                   colors,
                                   sd.getCategoryNames(),
                                   sd.getNoDataValues(),
                                   sd.getMinimumValue(),
                                   sd.getMaximumValue(),
                                   sd.getScale(),
                                   sd.getOffset(),
                                   sd.getUnits());
    }

    /**
     * Returns a code value indicating grid value data type.
     * This will also indicate the number of bits for the data type.
     *
     * @return a code value indicating grid value data type.
     */
    public SampleDimensionType getSampleDimensionType() {
        final NumberRange range = getRange();
        if (range == null) {
            return SampleDimensionType.REAL_32BITS;
        }
        return getSampleDimensionType(range);
    }

    /**
     * Returns the enum for the smallest type capable to hold the specified range of values.
     *
     * @param  range The range of values.
     * @return The enum for the specified range.
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
     * Returns the enum for a type capable to hold the specified range of values.
     * An heuristic approach is used for non-integer values.
     *
     * @param  min  The lower value, inclusive.
     * @param  max  The upper value, <strong>inclusive</strong> as well.
     * @return The enum for the specified range.
     */
    private static SampleDimensionType getSampleDimensionType(double min, double max) {
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
     * Returns the enum for the smallest type capable to hold the specified range of values.
     *
     * @param  min  The lower value, inclusive.
     * @param  max  The upper value, <strong>inclusive</strong> as well.
     * @return The enum for the specified range.
     */
    private static SampleDimensionType getSampleDimensionType(final long min, final long max) {
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
     * Return the enum for the specified sample model and band number.
     * If the sample model use an undefined data type, then this method
     * returns <code>null</code>.
     *
     * @param  model The sample model.
     * @param  band  The band to query.
     * @return The enum for the specified sample model and band number.
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
            case DataBuffer.TYPE_USHORT: signed=false; // Fall through
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_BYTE: {
                switch (model.getSampleSize(band)) {
                    case  1: return SampleDimensionType.UNSIGNED_1BIT;
                    case  2: return SampleDimensionType.UNSIGNED_2BITS;
                    case  4: return SampleDimensionType.UNSIGNED_4BITS;
                    case  8: return signed ? SampleDimensionType.SIGNED_8BITS : SampleDimensionType.UNSIGNED_8BITS;
                    case 16: return signed ? SampleDimensionType.SIGNED_16BITS : SampleDimensionType.UNSIGNED_16BITS;
                    case 32: return signed ? SampleDimensionType.SIGNED_32BITS : SampleDimensionType.UNSIGNED_32BITS;
                }
            }
        }
        return null;
    }

    /**
     * Wrap the specified value into a number of the specified data type. If the
     * value can't fit in the specified type, then a wider type is choosen unless
     * <code>allowWidening</code> is <code>false</code>.
     *
     * @param  value The value to wrap in a {@link Number} object.
     * @param  type A constant from the {@link SampleDimensionType} code list.
     * @param  allowWidening <code>true</code> if this method is allowed to returns
     *         a wider type than the usual one for the specified <code>type</code>.
     * @return The value as a {@link Number}.
     * @throws IllegalArgumentException if <code>type</code> is not a recognized constant.
     * @throws IllegalArgumentException if <code>allowWidening</code> is <code>false</code>
     *         and the specified <code>value</code> can't fit in the specified sample type.
     */
    private static Number wrapSample(final double value,
                                     final SampleDimensionType type,
                                     final boolean allowWidening)
        throws IllegalArgumentException
    {
        switch (type.ordinal()) {
            case UNSIGNED_1BIT:  // Fall through
            case UNSIGNED_2BITS: // Fall through
            case UNSIGNED_4BITS: // Fall through
            case   SIGNED_8BITS: {
                final byte candidate = (byte) value;
                if (candidate == value) {
                    return new Byte(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case UNSIGNED_8BITS: // Fall through
            case  SIGNED_16BITS: {
                final short candidate = (short) value;
                if (candidate == value) {
                    return new Short(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case UNSIGNED_16BITS: // Fall through
            case   SIGNED_32BITS: {
                final int candidate = (int) value;
                if (candidate == value) {
                    return new Integer(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case UNSIGNED_32BITS: {
                final long candidate = (long) value;
                if (candidate == value) {
                    return new Long(candidate);
                }
                if (!allowWidening) break;
                // Fall through
            }
            case REAL_32BITS: {
                if (!allowWidening || Math.abs(value) <= Float.MAX_VALUE) {
                    return new Float((float) value);
                }
                // Fall through
            }
            case REAL_64BITS: {
                return new Double(value);
            }
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }

    /**
     * Get the sample dimension title or description.
     * This string may be {@code null} if no description is present.
     */
    public InternationalString getDescription() {
        return (categories!=null) ? categories.getName() : null;
    }
    
    /**
     * @deprecated Use {@link #getDescription()} instead.
     */
    public String getDescription(final Locale locale) {
        return (categories!=null) ? categories.getName().toString(locale) : null;
    }

    /**
     * Returns a sequence of category names for the values contained in this sample dimension.
     * This allows for names to be assigned to numerical values. The first entry in the sequence
     * relates to a cell value of zero. For example:
     *
     *  <blockquote><pre>
     *    [0] Background
     *    [1] Water
     *    [2] Forest
     *    [3] Urban
     *  </pre></blockquote>
     *
     * @return The sequence of category names for the values contained in this sample dimension,
     *         or {@code null} if there is no category in this sample dimension.
     * @throws IllegalStateException if a sequence can't be mapped because some category use
     *         negative or non-integer sample values.
     *
     * @see #getCategories
     * @see #getCategory
     */
    public InternationalString[] getCategoryNames() throws IllegalStateException {
        if (categories == null) {
            return null;
        }
        if (categories.isEmpty()) {
            return new InternationalString[0];
        }
        InternationalString[] names = null;
        for (int i=categories.size(); --i>=0;) {
            final Category category = (Category) categories.get(i);
            final int lower = (int) category.minimum;
            final int upper = (int) category.maximum;
            if (lower!=category.minimum || lower<0 ||
                upper!=category.maximum || upper<0)
            {
                throw new IllegalStateException(Resources.format(
                        ResourceKeys.ERROR_NON_INTEGER_CATEGORY));
            }
            if (names == null) {
                names = new InternationalString[upper+1];
            }
            Arrays.fill(names, lower, upper+1, category.getName());
        }
        return names;
    }

    /**
     * Returns a sequence of category names for the values contained in this sample dimension.
     *
     * @deprecated Use {@link #getCategoryNames()} instead.
     */
    public final String[] getCategoryNames(final Locale locale) throws IllegalStateException {
        final InternationalString[] inter = getCategoryNames();
        final String[] names = new String[inter.length];
        for (int i=0; i<names.length; i++) {
            names[i] = inter[i].toString(locale);
        }
        return names;
    }
    
    /**
     * Returns all categories in this sample dimension. Note that a {@link Category} object may
     * apply to an arbitrary range of sample values.    Consequently, the first element in this
     * collection may not be directly related to the sample value <code>0</code>.
     *
     * @return The list of categories in this sample dimension, or <code>null</code> if none.
     *
     * @see #getCategoryNames
     * @see #getCategory
     */
    public List getCategories() {
        return categories;
    }
    
    /**
     * Returns the category for the specified sample value. If this method can't maps
     * a category to the specified value, then it returns <code>null</code>.
     *
     * @param  sample The value (can be one of <code>NaN</code> values).
     * @return The category for the supplied value, or <code>null</code> if none.
     *
     * @see #getCategories
     * @see #getCategoryNames
     */
    public Category getCategory(final double sample) {
        return (categories!=null) ? categories.getCategory(sample) : null;
    }

    /**
     * Returns a default category to use for background. A background category is used
     * when an image is <A HREF="../gp/package-summary.html#Resample">resampled</A> (for
     * example reprojected in an other coordinate system) and the resampled image do not
     * fit in a rectangular area. It can also be used in various situation where a raisonable
     * "no data" category is needed. The default implementation try to returns one
     * of the {@linkplain #getNoDataValues no data values}. If no suitable category is found,
     * then a {@linkplain Category#NODATA default} one is returned.
     *
     * @return A category to use as background for the "Resample" operation.
     *         Never <code>null</code>.
     */
    public Category getBackground() {
        return (categories!=null) ? categories.nodata : Category.NODATA;
    }

    /**
     * Returns the values to indicate "no data" for this sample dimension.  The default
     * implementation deduces the "no data" values from the list of categories supplied
     * at construction time. The rules are:
     *
     * <ul>
     *   <li>If {@link #getSampleToGeophysics} returns <code>null</code>, then
     *       <code>getNoDataValues()</code> returns <code>null</code> as well.
     *       This means that this sample dimension contains no category or contains
     *       only qualitative categories (e.g. a band from a classified image).</li>
     *
     *   <li>If {@link #getSampleToGeophysics} returns an identity transform,
     *       then <code>getNoDataValues()</code> returns <code>null</code>.
     *       This means that sample value in this sample dimension are already
     *       expressed in geophysics values and that all "no data" values (if any)
     *       have already been converted into <code>NaN</code> values.</li>
     *
     *   <li>Otherwise, if there is at least one quantitative category, returns the sample values
     *       of all non-quantitative categories. For example if "Temperature" is a quantitative
     *       category and "Land" and "Cloud" are two qualitative categories, then sample values
     *       for "Land" and "Cloud" will be considered as "no data" values. "No data" values
     *       that are already <code>NaN</code> will be ignored.</li>
     * </ul>
     *
     * Together with {@link #getOffset()} and {@link #getScale()}, this method provides a limited
     * way to transform sample values into geophysics values. However, the recommended way is to
     * use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead, which is more
     * general and take care of converting automatically "no data" values into <code>NaN</code>.
     *
     * @return The values to indicate no data values for this sample dimension,
     *         or <code>null</code> if not applicable.
     * @throws IllegalStateException if some qualitative categories use a range of
     *         non-integer values.
     *
     * @see #getSampleToGeophysics
     */
    public double[] getNoDataValues() throws IllegalStateException {
        if (!hasQuantitative) {
            return null;
        }
        int count = 0;
        double[] padValues = null;
        final int size = categories.size();
        for (int i=0; i<size; i++) {
            final Category category = (Category) categories.get(i);
            if (!category.isQuantitative()) {
                final double min = category.minimum;
                final double max = category.maximum;
                if (!Double.isNaN(min) || !Double.isNaN(max)) {
                    if (padValues == null) {
                        padValues = new double[size-i];
                    }
                    if (count >= padValues.length) {
                        padValues = XArray.resize(padValues, count*2);
                    }
                    padValues[count++] = min;
                    /*
                     * The "no data" value has been extracted. Now, check if we have a range
                     * of "no data" values instead of a single one for this category.  If we
                     * have a single value, it can be of any type. But if we have a range,
                     * then it must be a range of integers (otherwise we can't expand it).
                     */
                    if (max != min) {
                        int lower = (int) min;
                        int upper = (int) max;
                        if (lower!=min || upper!=max ||
                            !XMath.isInteger(category.getRange().getElementClass()))
                        {
                            throw new IllegalStateException(Resources.format(
                                    ResourceKeys.ERROR_NON_INTEGER_CATEGORY));
                        }
                        final int requiredLength = count + (upper-lower);
                        if (requiredLength > padValues.length) {
                            padValues = XArray.resize(padValues, requiredLength*2);
                        }
                        while (++lower <= upper) {
                            padValues[count++] = lower;
                        }
                    }
                }
            }
        }
        if (padValues != null) {
            padValues = XArray.resize(padValues, count);
        }
        return padValues;
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     * The default implementation fetch this value from the categories supplied at
     * construction time. If the minimum value can't be computed, then this method
     * returns {@link Double#NEGATIVE_INFINITY}.
     *
     * @see #getRange
     */
    public double getMinimumValue() {
        if (categories!=null && !categories.isEmpty()) {
            final double value = ((Category) categories.get(0)).minimum;
            if (!Double.isNaN(value)) {
                return value;
            }
        }
        return Double.NEGATIVE_INFINITY;
    }
    
    /**
     * Returns the maximum value occurring in this sample dimension.
     * The default implementation fetch this value from the categories supplied at
     * construction time. If the maximum value can't be computed, then this method
     * returns {@link Double#POSITIVE_INFINITY}.
     *
     * @see #getRange
     */
    public double getMaximumValue() {
        if (categories!=null) {
            for (int i=categories.size(); --i>=0;) {
                final double value = ((Category) categories.get(i)).maximum;
                if (!Double.isNaN(value)) {
                    return value;
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }
    
    /**
     * Returns the range of values in this sample dimension. This is the union of the range of
     * values of every categories, excluding <code>NaN</code> values. A {@link NumberRange} object
     * gives more informations than {@link #getMinimumValue} and {@link #getMaximumValue} methods
     * since it contains also the data type (integer, float, etc.) and inclusion/exclusion
     * informations.
     *
     * @return The range of values. May be <code>null</code> if this sample dimension has no
     *         quantitative category.
     *
     * @see Category#getRange
     * @see #getMinimumValue
     * @see #getMaximumValue
     *
     * @todo We should do a better job in {@code CategoryList.getRange()} when selecting
     *       the appropriate data type. {@code getSampleDimensionType(Range)} may be of
     *       some help.
     */
    public NumberRange getRange() {
        return (categories!=null) ? categories.getRange() : null;
    }

    /**
     * Returns <code>true</code> if at least one value of <code>values</code> is
     * in the range <code>lower</code> inclusive to <code>upper</code> exclusive.
     */
    private static boolean rangeContains(final double   lower,
                                         final double   upper,
                                         final double[] values)
    {
        if (values != null) {
            for (int i=0; i<values.length; i++) {
                final double v = values[i];
                if (v>=lower && v<upper) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns a string representation of a sample value. This method try to returns
     * a representation of the geophysics value; the transformation is automatically
     * applied when necessary. More specifically:
     *
     * <ul>
     *   <li>If <code>value</code> maps a qualitative category, then the
     *       category name is returned as of {@link Category#getName}.</li>
     *
     *   <li>Otherwise, if <code>value</code> maps a quantitative category, then the value is
     *       transformed into a geophysics value as with the {@link #getSampleToGeophysics()
     *       sampleToGeophysics} transform, the result is formatted as a number and the unit
     *       symbol is appened.</li>
     * </ul>
     *
     * @param  value  The sample value (can be one of <code>NaN</code> values).
     * @param  locale Locale to use for formatting, or <code>null</code> for the default locale.
     * @return A string representation of the geophysics value, or <code>null</code> if there is
     *         none.
     *
     * @task REVISIT: What should we do when the value can't be formatted?
     *                <code>SampleDimension</code> returns <code>null</code> if there is no
     *                category or if an exception is thrown, but <code>CategoryList</code>
     *                returns "Untitled" if the value is an unknow NaN, and try to format
     *                the number anyway in other cases.
     */
    public String getLabel(final double value, final Locale locale) {
        if (categories != null) {
            if (isGeophysics) {
                return categories.format(value, locale);
            } else try {
                return categories.inverse.format(categories.transform(value), locale);
            } catch (TransformException exception) {
                // Value probably don't match a category. Ignore...
            }
        }
        return null;
    }
    
    /**
     * Returns the unit information for this sample dimension.
     * May returns <code>null</code> if this dimension has no units.
     * This unit apply to values obtained after the {@link #getSampleToGeophysics
     * sampleToGeophysics} transformation.
     *
     * @see #getSampleToGeophysics
     */
    public Unit getUnits() {
        return (categories!=null) ? categories.geophysics(true).getUnits() : null;
    }

    /**
     * Returns the value to add to grid values for this sample dimension.
     * This attribute is typically used when the sample dimension represents
     * elevation data. The transformation equation is:
     *
     * <blockquote><pre>offset + scale*sample</pre></blockquote>
     *
     * Together with {@link #getScale()} and {@link #getNoDataValues()}, this method provides a
     * limited way to transform sample values into geophysics values. However, the recommended
     * way is to use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead,
     * which is more general and take care of converting automatically "no data" values
     * into <code>NaN</code>.
     *
     * @return The offset to add to grid values.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see #getSampleToGeophysics
     * @see #rescale
     */
    public double getOffset() throws IllegalStateException {
        return getCoefficient(0);
    }

    /**
     * Returns the value which is multiplied to grid values for this sample dimension.
     * This attribute is typically used when the sample dimension represents elevation
     * data. The transformation equation is:
     *
     * <blockquote><pre>offset + scale*sample</pre></blockquote>
     *
     * Together with {@link #getOffset()} and {@link #getNoDataValues()}, this method provides a
     * limited way to transform sample values into geophysics values. However, the recommended
     * way is to use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead,
     * which is more general and take care of converting automatically "no data" values
     * into <code>NaN</code>.
     *
     * @return The scale to multiply to grid value.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see #getSampleToGeophysics
     * @see #rescale
     */
    public double getScale() {
        return getCoefficient(1);
    }

    /**
     * Returns a coefficient of the linear transform from sample to geophysics values.
     *
     * @param  order The coefficient order (0 for the offset, or 1 for the scale factor,
     *         2 if we were going to implement quadratic relation, 3 for cubic, etc.).
     * @return The coefficient.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     */
    private double getCoefficient(final int order) throws IllegalStateException {
        if (!hasQuantitative) {
            // Default value for "offset" is 0; default value for "scale" is 1.
            // This is equal to the order if 0 <= order <= 1.
            return order;
        }
        Exception cause = null;
        if (sampleToGeophysics != null) try {
            final double value;
            switch (order) {
                case 0:  value = sampleToGeophysics.transform(0); break;
                case 1:  value = sampleToGeophysics.derivative(Double.NaN); break;
                default: throw new AssertionError(order); // Should not happen
            }
            if (!Double.isNaN(value)) {
                return value;
            }
        } catch (TransformException exception) {
            cause = exception;
        }
        IllegalStateException exception = new IllegalStateException(Resources.format(
                                              ResourceKeys.ERROR_NON_LINEAR_RELATION));
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Returns a transform from sample values to geophysics values. If this sample dimension
     * has no category, then this method returns <code>null</code>. If all sample values are
     * already geophysics values (including <code>NaN</code> for "no data" values), then this
     * method returns an identity transform. Otherwise, this method returns a transform expecting
     * sample values as input and computing geophysics value as output. This transform will take
     * care of converting all "{@linkplain #getNoDataValues() no data values}" into
     * <code>NaN</code> values.
     * The <code>sampleToGeophysics.{@linkplain MathTransform1D#inverse() inverse()}</code>
     * transform is capable to differenciate <code>NaN</code> values to get back the original
     * sample value.
     *
     * @return The transform from sample to geophysics values, or <code>null</code> if this
     *         sample dimension do not defines any transform (which is not the same that
     *         defining an identity transform).
     *
     * @see #getScale
     * @see #getOffset
     * @see #getNoDataValues
     * @see #rescale
     */
    public MathTransform1D getSampleToGeophysics() {
        if (isGeophysics) {
            return LinearTransform1D.IDENTITY;
        }
        if (!hasQualitative && sampleToGeophysics!=null) {
            // If there is only quantitative categories and they all use the same transform,
            // then we don't need the indirection level provided by CategoryList.
            return sampleToGeophysics;
        }
        // CategoryList is a MathTransform1D.
        return categories;
    }

    /**
     * If <code>true</code>, returns the geophysics companion of this sample dimension. By
     * definition, a <cite>geophysics sample dimension</cite> is a sample dimension with a
     * {@linkplain #getRange range of sample values} transformed in such a way that the
     * {@link #getSampleToGeophysics sampleToGeophysics} transform is always the identity
     * transform, or <code>null</code> if no such transform existed in the first place. In
     * other words, the range of sample values in all category maps directly the "real world"
     * values without the need for any transformation.
     * <br><br>
     * <code>SampleDimension</code> objects live by pair: a <cite>geophysics</cite> one (used for
     * computation) and a <cite>non-geophysics</cite> one (used for packing data, usually as
     * integers). The <code>geo</code> argument specifies which object from the pair is wanted,
     * regardless if this method is invoked on the geophysics or non-geophysics instance of the
     * pair. In other words, the result of <code>geophysics(b1).geophysics(b2).geophysics(b3)</code>
     * depends only on the value in the last call (<code>b3</code>).
     *
     * @param  geo <code>true</code> to get a sample dimension with an identity
     *         {@linkplain #getSampleToGeophysics transform} and a {@linkplain #getRange range of
     *         sample values} matching the geophysics values, or <code>false</code> to get back the
     *         original sample dimension.
     * @return The sample dimension. Never <code>null</code>, but may be <code>this</code>.
     *
     * @see Category#geophysics
     * @see org.geotools.coverage.grid.GridCoverage#geophysics
     */
    public SampleDimension geophysics(final boolean geo) {
        if (geo == isGeophysics) {
            return this;
        }
        if (inverse == null) {
            if (categories != null) {
                inverse = new SampleDimension(categories.inverse);
                inverse.inverse = this;
            } else {
                /*
                 * If there is no categories, then there is no real difference between
                 * "geophysics" and "indexed" sample dimensions.  Both kinds of sample
                 * dimensions would be identical objects, so we are better to just
                 * returns 'this'.
                 */
                inverse = this;
            }
        }
        return inverse;
    }

    /**
     * Color palette associated with the sample dimension.
     * A color palette can have any number of colors.
     * See palette interpretation for meaning of the palette entries.
     * If the grid coverage has no color palette, <code>null</code> will be returned.
     *
     * @return The color palette associated with the sample dimension.
     *
     * @see #getPaletteInterpretation
     * @see #getColorInterpretation
     * @see IndexColorModel
     */
    public int[][] getPalette() {
        final ColorModel color = getColorModel();
        if (color instanceof IndexColorModel) {
            final IndexColorModel cm = (IndexColorModel) color;
            final int[][] colors = new int[cm.getMapSize()][];
            for (int i=0; i<colors.length; i++) {
                colors[i] = new int[] {cm.getRed(i), cm.getGreen(i), cm.getBlue(i)};
            }
            return colors;
        }
        return null;
    }

    /**
     * Indicates the type of color palette entry for sample dimensions which have a
     * palette. If a sample dimension has a palette, the color interpretation must
     * be {@link ColorInterpretation#GRAY_INDEX GRAY_INDEX}
     * or {@link ColorInterpretation#PALETTE_INDEX PALETTE_INDEX}.
     * A palette entry type can be Gray, RGB, CMYK or HLS.
     *
     * @return The type of color palette entry for sample dimensions which have a palette.
     */
    public PaletteInterpretation getPaletteInterpretation() {
        return PaletteInterpretation.RGB;
    }
    
    /**
     * Returns the color interpretation of the sample dimension.
     * A sample dimension can be an index into a color palette or be a color model
     * component. If the sample dimension is not assigned a color interpretation
     * the value is {@link ColorInterpretation#UNDEFINED}.
     */
    public ColorInterpretation getColorInterpretation() {
        // The 'GridSampleDimension' class overrides this method
        // with better values for 'band' and 'numBands' constants.
        final int band     = 0;
        final int numBands = 1;
        return getColorInterpretation(getColorModel(band, numBands), band);
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

    /**
     * Returns a color model for this sample dimension. The default implementation create a color
     * model with 1 band using each category's colors as returned by {@link Category#getColors}.
     * The returned color model will typically use data type {@link DataBuffer#TYPE_FLOAT} if this
     * <code>SampleDimension</code> instance is "geophysics", or an integer data type otherwise.
     * <br><br>
     * Note that {@link org.geotools.coverage.grid.GridCoverage2D#getSampleDimension} returns
     * special implementations of {@code SampleDimension}. In this particular case, the color model
     * created by this {@code getColorModel()} method will have the same number of bands
     * than the grid coverage's {@link RenderedImage}.
     *
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link #getRange}</code> range. May be <code>null</code> if this
     *         sample dimension has no category.
     */
    public ColorModel getColorModel() {
        // The 'GridSampleDimension' class overrides this method
        // with better values for 'band' and 'numBands' constants.
        final int band     = 0;
        final int numBands = 1;
        return getColorModel(band, numBands);
    }
    
    /**
     * Returns a color model for this sample dimension. The default implementation create the
     * color model using each category's colors as returned by {@link Category#getColors}. The
     * returned color model will typically use data type {@link DataBuffer#TYPE_FLOAT} if this
     * <code>SampleDimension</code> instance is "geophysics", or an integer data type otherwise.
     *
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the <code>visibleBand</code> and ignore the others, but
     *         the existence of all <code>numBands</code> will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link #getRange}</code> range. May be <code>null</code> if this
     *         sample dimension has no category.
     *
     * @task REVISIT: This method may be deprecated in a future version. It it strange to use
     *                only one <code>SampleDimension</code>  for creating a multi-bands color
     *                model. Logically, we would expect as many <code>SampleDimension</code>s
     *                as bands.
     */
    public ColorModel getColorModel(final int visibleBand, final int numBands) {
        if (categories != null) {
            return categories.getColorModel(visibleBand, numBands);
        }
        return null;
    }
    
    /**
     * Returns a color model for this sample dimension. The default implementation create the
     * color model using each category's colors as returned by {@link Category#getColors}. 
     *
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the <code>visibleBand</code> and ignore the others, but
     *         the existence of all <code>numBands</code> will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @param  type The data type that has to be used for the sample model
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link #getRange}</code> range. May be <code>null</code> if this
     *         sample dimension has no category.
     *
     * @task REVISIT: This method may be deprecated in a future version. It it strange to use
     *                only one <code>SampleDimension</code>  for creating a multi-bands color
     *                model. Logically, we would expect as many <code>SampleDimension</code>s
     *                as bands.
     */
    public ColorModel getColorModel(final int visibleBand, final int numBands, final int type) {
        if (categories != null) {
            return categories.getColorModel(visibleBand, numBands, type);
        }
        return null;
    }

    /**
     * Returns a sample dimension using new {@link #getScale scale} and {@link #getOffset offset}
     * coefficients. Other properties like the {@linkplain #getRange sample value range},
     * {@linkplain #getNoDataValues no data values} and {@linkplain #getColorModel colors}
     * are unchanged.
     *
     * @param scale  The value which is multiplied to grid values for the new sample dimension.
     * @param offset The value to add to grid values for the new sample dimension.
     *
     * @see #getScale
     * @see #getOffset
     * @see Category#rescale
     */
    public SampleDimension rescale(final double scale, final double offset) {
        final MathTransform1D sampleToGeophysics = Category.createLinearTransform(scale, offset);
        final Category[] categories = (Category[]) getCategories().toArray();
        final Category[] reference  = (Category[]) categories.clone();
        for (int i=0; i<categories.length; i++) {
            if (categories[i].isQuantitative()) {
                categories[i] = categories[i].rescale(sampleToGeophysics);
            }
            categories[i] = categories[i].geophysics(isGeophysics);
        }
        if (Arrays.equals(categories, reference)) {
            return this;
        }
        return new SampleDimension(categories, getUnits());
    }

    /**
     * The list of metadata keywords for a sample dimension.
     * If no metadata is available, the sequence will be empty.
     *
     * @return The list of metadata keywords for a sample dimension.
     *
     * @see #getMetadataValue
     * @see javax.media.jai.PropertySource#getPropertyNames
     */
    public String[] getMetaDataNames() {
        return EMPTY_METADATA;
    }    

    /**
     * Retrieve the metadata value for a given metadata name.
     *
     * @param  name Metadata keyword for which to retrieve metadata.
     * @return The metadata value for a given metadata name.
     * @throws MetadataNameNotFoundException if there is no value for the specified metadata name.
     *
     * @see #getMetaDataNames
     * @see javax.media.jai.PropertySource#getProperty
     */
    public String getMetadataValue(String name) throws MetadataNameNotFoundException {
        throw new MetadataNameNotFoundException();
    }
    
    /**
     * Returns a hash value for this sample dimension.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return (categories!=null) ? categories.hashCode() : 23491;
    }
    
    /**
     * Compares the specified object with this sample dimension for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (object instanceof SampleDimension) {
            final SampleDimension that = (SampleDimension) object;
            return Utilities.equals(this.categories, that.categories);
            // Since everything is deduced from CategoryList, two sample dimensions
            // should be equal if they have the same list of categories.
        }
        return false;
    }
    
    /**
     * Returns a string representation of this sample dimension.
     * This string is for debugging purpose only and may change
     * in future version. The default implementation format the
     * sample value range, then the list of categories. A "*"
     * mark is put in front of what seems the "main" category.
     */
    public String toString() {
        if (categories != null) {
            return categories.toString(this);
        } else {
            return Utilities.getShortClassName(this);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    ////////                                                                 ////////
    ////////        REGISTRATION OF "SampleTranscode" IMAGE OPERATION        ////////
    ////////                                                                 ////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Register the "SampleTranscode" image operation.
     * Registration is done when the class is first loaded.
     *
     * @todo This static initializer will imply immediate class loading of a lot of
     *       JAI dependencies.  This is a pretty high overhead if JAI is not wanted
     *       right now. The correct approach is to declare the image operation into
     *       the {@code META-INF/registryFile.jai} file, which is automatically
     *       parsed during JAI initialization. Unfortunatly, it can't access private
     *       classes and we don't want to make our registration classes public. We
     *       can't move our registration classes into a hidden "resources" package
     *       neither because we need package-private access to {@code CategoryList}.
     *       For now, we assume that people using the GC package probably want to work
     *       with {@link org.geotools.coverage.grid.GridCoverage2D}, which make extensive
     *       use of JAI. Peoples just working with {@link org.geotools.coverage.Coverage} are
     *       stuck with the overhead. Note that we register the image operation here because
     *       the only operation's argument is of type {@code SampleDimension[]}.
     *       Consequently, the image operation may be invoked at any time after class
     *       loading of {@link SampleDimension}.
     *       <br><br>
     *       Additional note: moving the initialization into the
     *       {@code META-INF/registryFile.jai} file may not be the best idea neithter,
     *       since peoples using JAI without the GCS module may be stuck with the overhead
     *       of loading GC classes.
     */
    static {
        SampleTranscoder.register(JAI.getDefaultInstance());
    }
}
