/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.grid;

import java.awt.RenderingHints;
import java.awt.image.*; // Numerous imports here.
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.LogRecord;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;

import org.opengis.util.InternationalString;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.NoninvertibleTransformException;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.resources.XArray;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;


/**
 * Holds the different views of a {@link GridCoverage2D}. Those views are handled in a separated
 * class because the same instance may be shared by more than one {@link GridCoverage2D}. Because
 * views are associated with potentially big images, sharing them when possible is a big memory
 * and CPU saver.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridCoverageViews {
    /**
     * Slight number for rounding errors in floating point comparaison.
     */
    private static final float EPS = 1E-5f;

    /**
     * {@code true} if we should apply a conservative policy for the "piecewise" operation.
     * The conservative policy is to apply "piecewise" only if there is no ambiguity about what
     * the user wants.
     */
    private static final boolean CONSERVATIVE_PIECEWISE = true;

    /**
     * The coverage that created this {@code GridCoverageViews}.
     */
    private final GridCoverage2D original;

    /**
     * The views. At least one value must be the {@link #original} instance.
     */
    private final Map<ViewType,GridCoverage2D> views;

    /**
     * Constructs a initially empty map of views.
     *
     * @param coverage The coverage that created this {@code GridCoverageViews}.
     */
    public GridCoverageViews(final GridCoverage2D coverage) {
        original = coverage;
        views = new EnumMap<ViewType,GridCoverage2D>(ViewType.class);
        boolean geophysics   = true; // 'true' only if all bands are geophysics.
        boolean photographic = true; // 'true' only if no band have category.
        final int numBands = coverage.getNumSampleDimensions();
        for (int i=0; i<numBands; i++) {
            final GridSampleDimension band = coverage.getSampleDimension(i);
            if (band != null) {
                final List<Category> categories = band.getCategories();
                if (categories == null || categories.isEmpty()) {
                    // No category. The image is treated as photographic.
                    continue;
                }
                photographic = false;
                if (band != band.view(ViewType.GEOPHYSICS)) {
                    geophysics = false;
                }
            }
        }
        final ViewType type;
        if (geophysics) {
            type = ViewType.GEOPHYSICS;
        } else if (photographic) {
            type = ViewType.PHOTOGRAPHIC;
        } else {
            type = ViewType.NATIVE;
        }
        views.put(type, coverage);
    }

    /**
     * Returns the specified view.
     */
    public synchronized GridCoverage2D get(final ViewType type) {
        GridCoverage2D coverage = views.get(type);
        if (coverage != null) {
            return coverage;
        }
        coverage = create(original, type);
        views.put(type, coverage);
        return coverage;
    }

    /**
     * Invoked by {@link #get} when a view needs to be created.
     *
     * @todo IndexColorModel seems to badly choose its sample model. As of JDK 1.4-rc1, it
     *       construct a ComponentSampleModel, which is drawn very slowly to the screen. A
     *       much faster sample model is PixelInterleavedSampleModel,  which is the sample
     *       model used by BufferedImage for TYPE_BYTE_INDEXED. We should check if this is
     *       fixed in future J2SE release.
     *
     * @todo The "Piecewise" operation is disabled because javac 1.4.1_01 generate illegal
     *       bytecode. This bug is fixed in javac 1.4.2-beta. However, we still have an
     *       ArrayIndexOutOfBoundsException in JAI code...
     */
    static GridCoverage2D create(final GridCoverage2D coverage, final ViewType type) {
        /*
         * STEP 1 - Gets the source image and prepare the target bands (sample dimensions).
         *          As a slight optimisation, we skip the "Null" operation since such image
         *          may be the result of some operation (e.g. "Colormap").
         */
        RenderedImage image = coverage.getRenderedImage();
        while (image instanceof NullOpImage) {
            final NullOpImage op = (NullOpImage) image;
            if (op.getNumSources() != 1) {
                break;
            }
            image = op.getSourceImage(0);
        }
        final SampleModel           sourceModel = image.getSampleModel();
        final int                      numBands = sourceModel.getNumBands();
        final GridSampleDimension[] sourceBands = coverage.getSampleDimensions();
        final GridSampleDimension[] targetBands = sourceBands.clone();
        assert targetBands.length == numBands : targetBands.length;
        for (int i=0; i<targetBands.length; i++) {
            targetBands[i] = targetBands[i].view(type);
        }
        /*
         * If the target bands are equal to the source bands, then there is nothing to do.
         * Otherwise, we call "nativeBands" the ones that are not geophysics (i.e. the ones
         * that may contain a MathTransform1D different than the identity transform).
         */
        if (Arrays.equals(sourceBands, targetBands)) {
            return coverage;
        }
        final int visibleBand = CoverageUtilities.getVisibleBand(image);
        final boolean toGeophysics = type.equals(ViewType.GEOPHYSICS);
        final GridSampleDimension[] nativeBands = toGeophysics ? sourceBands : targetBands;
        /*
         * Computes immediately the "geophysics to native" transforms.  If all transforms are the
         * identity one, then we will return the coverage unchanged. The transforms that can't be
         * obtained will be set to null, which is understood by LookupTableFactory.create(...) as
         * "Lookup operation not allowed".
         */
        boolean isIdentity = true;
        MathTransform1D[] transforms = new MathTransform1D[numBands];
        for (int i=0; i<numBands; i++) {
            MathTransform1D transform = nativeBands[i].getSampleToGeophysics();
            if (transform!=null && !toGeophysics) try {
                transform = transform.inverse(); // We want the geophysics to native transform.
            } catch (NoninvertibleTransformException e) {
                transform = null;
                isIdentity = false;
            }
            transforms[i] = transform;
            isIdentity &= transform.isIdentity();
        }
        if (isIdentity) {
            return coverage;
        }
        /*
         * STEP 2 - Computes the layout for the destination RenderedImage. We will use the same
         *          layout than the parent image, except for tile size if the parent image had
         *          only one big tile, and for the color model and sample model  (since we are
         *          reformating data in the process of this operation).
         */
        ImageLayout layout      = ImageUtilities.getImageLayout(image);
        ColorModel  colors      = targetBands[visibleBand].getColorModel(visibleBand, numBands);
        SampleModel targetModel = colors.createCompatibleSampleModel(
                layout.getTileWidth(image), layout.getTileHeight(image));
        if (colors instanceof IndexColorModel && targetModel.getClass().equals(ComponentSampleModel.class)) {
            // TODO: There is the 'IndexColorModel' hack (see method description).
            // Consider removing this hack when we will target Java 6.
            final int w = targetModel.getWidth();
            final int h = targetModel.getHeight();
            targetModel = new PixelInterleavedSampleModel(colors.getTransferType(), w,h,1,w, new int[1]);
        }
        layout = layout.setSampleModel(targetModel).setColorModel(colors);
        ParameterBlock param = new ParameterBlock().addSource(image);
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        hints.put(JAI.KEY_TRANSFORM_ON_COLORMAP,     Boolean.FALSE);
        String operation = null; // Will be set in step 3 or 4.
        /*
         * STEP 3 - Checks if the transcoding could be done with the JAI's "Lookup" operation. This
         *          is probably the fastest operation available for going to the geophysics view.
         *          Note that the transforms array may contains null elements, which will cause
         *          LookupTableFactory.create(...) to returns null.
         */
        if (transforms != null) try {
            final int sourceType = sourceModel.getDataType();
            final int targetType = targetModel.getDataType();
            LookupTableJAI table = LookupTableFactory.create(sourceType, targetType, transforms);
            if (table != null) {
                operation = "Lookup";
                param = param.add(table);
            }
        } catch (TransformException exception) {
            /*
             * A value can't be transformed. Fallback on "Rescale" or "Piecewise" operations. We
             * don't log yet because the more general operations are likely to fail for the same
             * reason and we don't want to log the same TransformException twice.
             */
        }
        /*
         * STEP 4 - Check if the transcoding could be done with a JAI's "Rescale" or "Piecewise"
         *          operations. The "Rescale" operation requires a completly linear relationship
         *          between the source and the destination sample values. The "Piecewise" operation
         *          is less strict: piecewise breakpoints are very similar to categories, but the
         *          transformation for all categories still have to be linear.
         */
        if (operation == null) try {
            boolean     canRescale   = true; // 'true' if the "Rescale"   operation can be applied.
            boolean     canPiecewise = true; // 'true' if the "Piecewise" operation can be applied.
            boolean     conditional  = false;// 'true' if isZeroExcluded(...) needs to be invoked.
            double[]    scales       = null; // The first  argument for "Rescale".
            double[]    offsets      = null; // The second argument for "Rescale".
            float[][][] breakpoints  = null; // The only   argument for "Piecewise".
testLinear: for (int i=0; i<numBands; i++) {
                final List<Category> sources = sourceBands[i].getCategories();
                final List<Category> natives = nativeBands[i].getCategories();
                final int      numCategories = sources.size();
                float[]    sourceBreakpoints = null;
                float[]    targetBreakpoints = null;
                double        expectedSource = Double.NaN;
                double        expectedTarget = Double.NaN;
                int jbp = 0; // Break point index (vary with j)
                for (int j=0; j<numCategories; j++) {
                    final Category sourceCategory = sources.get(j);
                    final Category nativeCategory = natives.get(j);
                    MathTransform1D transform = nativeCategory.getSampleToGeophysics();
                    if (transform == null) {
                        /*
                         * A qualitative category was found. Those categories maps NaN values,
                         * which need the special processing performed by our "SampleTranscode"
                         * operation. So "Piecewise" is eliminated, but as a special case the
                         * "Rescale" operation could continue to work if the NaN value maps to 0.
                         */
                        canPiecewise = false;
                        if (!toGeophysics) {
                            assert !nativeCategory.equals(sourceCategory) : nativeCategory;
                            final NumberRange range = nativeCategory.getRange();
                            if (range.getMinimum(true) == 0 && range.getMaximum(true) == 0) {
                                assert Double.isNaN(sourceCategory.getRange().getMinimum()) : sourceCategory;
                                conditional = true;
                                continue;
                            }
                        }
                        canRescale = false;
                        break testLinear;
                    }
                    if (!toGeophysics) {
                        // We are going to convert geophysics values to native one.
                        transform = transform.inverse();
                    }
                    final double offset = transform.transform(0);
                    final double scale  = transform.derivative(Double.NaN);
                    if (Double.isNaN(scale) || Double.isNaN(offset)) {
                        // One category doesn't use a linear transformation. We can't deal with
                        // that with "Rescale" or "Piecewise". Fallback on our "SampleTranscode".
                        canRescale   = false;
                        canPiecewise = false;
                        break testLinear;
                    }
                    // Allocates arrays the first time the loop is run up to this point.
                    // Store scale and offset, and check if they still the same.
                    if (j == 0) {
                        if (i == 0) {
                            scales      = new double[numBands];
                            offsets     = new double[numBands];
                            breakpoints = new float [numBands][][];
                        }
                        sourceBreakpoints = new float[numCategories * 2];
                        targetBreakpoints = new float[numCategories * 2];
                        breakpoints[i] = new float[][] {sourceBreakpoints, targetBreakpoints};
                        offsets    [i] = offset;
                        scales     [i] = scale;
                    }
                    if (offset!=offsets[i] || scale!=scales[i]) {
                        canRescale = false;
                    }
                    // Compute breakpoints.
                    final NumberRange range = sourceCategory.getRange();
                    final double    minimum = range.getMinimum(true);
                    final double    maximum = range.getMaximum(true);
                    final float   sourceMin = (float) minimum;
                    final float   sourceMax = (float) maximum;
                    final float   targetMin = (float)(minimum * scale + offset);
                    final float   targetMax = (float)(maximum * scale + offset);
                    assert sourceMin <= sourceMax : range;
                    if (Math.abs(minimum - expectedSource) <= EPS) {
                        if (Math.abs(targetMin - expectedTarget) <= EPS) {
                            // This breakpoint is identical to the previous one. Do not
                            // duplicate; overwrites the previous one since this one is
                            // likely to be more accurate.
                            jbp--;
                        } else {
                            // Found a discontinuity!!! The "piecewise" operation is not really
                            // designed for such case. The behavior between the last breakpoint
                            // and the current one may not be what the user expected.
                            assert sourceBreakpoints[jbp-1] < sourceMin : expectedSource;
                            if (CONSERVATIVE_PIECEWISE) {
                                canPiecewise = false;
                            }
                        }
                    } else if (j != 0) {
                        // Found a gap between the last category and the current one. The
                        // "piecewise" operation may not behave as the user expected  for
                        // sample values falling in this gap.
                        assert !(expectedSource > sourceMin) : expectedSource;
                        if (CONSERVATIVE_PIECEWISE) {
                            canPiecewise = false;
                        }
                    }
                    sourceBreakpoints[jbp  ] = sourceMin;
                    sourceBreakpoints[jbp+1] = sourceMax;
                    targetBreakpoints[jbp  ] = targetMin;
                    targetBreakpoints[jbp+1] = targetMax;
                    jbp += 2;
                    expectedSource = range.getMaximum(false);
                    expectedTarget = expectedSource * scale + offset;
                }
                if (false) {
                    // HACK: temporarily disabled because 'javac' 1.4.1_02 produces invalid
                    //       bytecode. This bug is fixed in 'java' 1.4.2-beta. Furthermore,
                    //       the "piecewise" operation throws an ArrayIndexOutOfBoundsException
                    //       in JAI code for an unknow reason...
                    breakpoints[i][0] = sourceBreakpoints = XArray.resize(sourceBreakpoints, jbp);
                    breakpoints[i][1] = targetBreakpoints = XArray.resize(targetBreakpoints, jbp);
                    assert XArray.isSorted(sourceBreakpoints);
                } else {
                    canPiecewise = false;
                }
            }
            if (canRescale && scales!=null && (!conditional || isZeroExcluded(image, scales, offsets))) {
                operation = "Rescale";
                param = param.add(scales).add(offsets);
            } else if (canPiecewise && breakpoints!=null) {
                operation = "Piecewise";
                param = param.add(breakpoints);
            }
        } catch (TransformException exception) {
            /*
             * At least one category doesn't use a linear relation. Ignores the exception and
             * fallback on the next case. We log a message at Level.FINE rather than WARNING
             * because this exception may be normal. We pretend that the log come from
             * GridCoverage2D.view, which is the public method that invoked this one.
             */
            Logging.recoverableException(GridCoverage2D.class, "view", exception);
        }
        /*
         * STEP 5 - Transcode the image sample values. The "SampleTranscode" operation is
         *          registered in the org.geotools.coverage package in the GridSampleDimension
         *          class.
         */
        if (operation == null) {
            param = param.add(sourceBands);
            operation = "org.geotools.SampleTranscode";
        }
        final InternationalString name = coverage.getName();
        if (GridCoverage2D.LOGGER.isLoggable(AbstractProcessor.OPERATION)) {
            // Logs a message using the same level than grid coverage processor.
            final String shortName = operation.substring(operation.lastIndexOf('.') + 1);
            final Locale    locale = coverage.getLocale();
            final LogRecord record = Loggings.getResources(locale).getLogRecord(
                    AbstractProcessor.OPERATION, LoggingKeys.SAMPLE_TRANSCODE_$3, new Object[] {
                        name.toString(locale), Integer.valueOf(toGeophysics ? 1 : 0), shortName
                    });
            record.setSourceClassName(GridCoverage2D.class.getName());
            record.setSourceMethodName("geophysics");
            GridCoverage2D.LOGGER.log(record);
        }
        final PlanarImage    view    = JAI.create(operation, param, hints);
        final GridCoverage[] sources = new GridCoverage[] {coverage};
        return new GridCoverage2D(name, view, coverage.gridGeometry, targetBands, sources, null);
    }

    /**
     * Returns {@code true} if rescaling every pixels in the specified image (excluding NaN) would
     * not produce zero value. In case of doubt, this method conservatively returns {@code false}.
     * <p>
     * <b>Why this method exists</b><br>
     * When a {@link SampleDimension} describes exactly one linear relationship with one NaN value
     * mapping exactly to the index value 0, then the "<cite>geophysics to native</cite>" transform
     * can be optimized to the {@code "Rescale"} operation because {@link Float#NaN} casted to the
     * {@code int} primitive type equals 0. This case is very common, which make this optimization
     * a usefull one. Unfortunatly there is nothing in {@code "Rescale"} preventing some real number
     * (not NaN) to maps to 0 through the normal linear relationship. We need to make sure that the
     * range of transformed values doesn't contains 0.
     */
    private static boolean isZeroExcluded(final RenderedImage image,
            final double[] scales, final double[] offsets)
    {
        /*
         * We can't do any garantee if pixel values are modifiable.
         */
        if (image instanceof WritableRenderedImage) {
            return false;
        }
        /*
         * If an "Extrema" operation is used somewhere in the image chain, ensure that it was
         * applied on an image with the same pixel values than the image we want to analyze.
         * Ensure also that no ROI was defined for the "Extrema" operation.
         */
        Object parent = image;
        while (parent instanceof PlanarImage) {
            final PlanarImage planar = (PlanarImage) image;
            if (parent instanceof RenderedOp) {
                final RenderedOp op = (RenderedOp) parent;
                final String name = op.getOperationName();
                if (name.equalsIgnoreCase("Extrema")) {
                    final int n = op.getNumParameters();
                    for (int i=0; i<n; i++) {
                        if (op.getObjectParameter(i) instanceof ROI) {
                            return false;
                        }
                    }
                    break;
                }
                if (!name.equalsIgnoreCase("Null") && !name.equalsIgnoreCase("Histogram")) {
                    return false;
                }
            }
            final int n = planar.getNumSources();
            if (n >= 2) return false;
            if (n == 0) break;
            parent = planar.getSourceObject(0);
        }
        /*
         * Apparently, there is nothing preventing us to query the "extrema" property. Note that
         * the above test did not garantee that this property is defined - only that if defined,
         * it looks like suitable. Now ensure that the range after conversion does not includes 0.
         */
        final Object property = image.getProperty("extrema");
        if (!(property instanceof double[][])) {
            return false;
        }
        final double[][] extrema = (double[][]) property;
        if (extrema.length != 2) {
            return false;
        }
        for (int i=0; i<scales.length; i++) {
            final double scale  = scales [i];
            final double offset = offsets[i];
            double maximum = extrema[0][i] * scale + offset;
            double minimum = extrema[1][i] * scale + offset;
            if (minimum > maximum) {
                final double tmp = minimum;
                minimum = maximum;
                maximum = tmp;
            }
            if (!(minimum > 0 || maximum < 0)) { // Use '!' for catching NaN.
                return false;
            }
        }
        return true;
    }
}
