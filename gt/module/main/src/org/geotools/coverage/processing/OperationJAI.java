/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.coverage.processing;

// J2SE dependencies and extensions
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.io.Serializable;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import javax.units.Unit;

// JAI dependencies
import javax.media.jai.ImageLayout;
import javax.media.jai.IntegerSequence;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.util.AbstractInternationalString;
import org.geotools.parameter.ImagingParameterDescriptors;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.image.ImageUtilities;


/**
 * Wraps a JAI's {@link OperationDescriptor} for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</A>.
 * This class help to leverage the rich set of JAI operators in an GeoAPI framework.
 * {@code OperationJAI} inherits operation name and argument types from {@link OperationDescriptor},
 * except the source argument type (usually <code>{@linkplain RenderedImage}.class</code>) which is
 * set to <code>{@linkplain GridCoverage2D}.class</code>. If there is only one source argument, it
 * will be renamed {@code "source"} for better compliance with OpenGIS usage.
 * <p>
 * The entry point for applying an operation is the usual {@link #doOperation doOperation} method.
 * The default implementation forward the call to other methods for different bits of tasks,
 * resulting in the following chain of calls:
 * <p>
 * <ol>
 *   <li>{@link #doOperation doOperation} (the entry point)</li>
 *   <li>{@link #resampleToCommonGeometry resampleToCommonGeometry}
 *       (reproject all source to the same coordinate reference system)</li>
 *   <li>{@link #deriveGridCoverage deriveGridCoverage} (gets the destination properties)</li>
 *   <li>{@link #deriveSampleDimension} (gets the destination sample dimensions)</li>
 *   <li>{@link #deriveCategory} (gets the destination categories)</li>
 *   <li>{@link #deriveUnit} (gets the destination units)</li>
 *   <li>{@link #createRenderedImage} (the actual call to {@link JAI#createNS JAI.createNS})</li>
 * </ol>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class OperationJAI extends Operation2D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5974520239347639965L;

    /**
     * The rendered mode for JAI operation.
     */
    private static final String RENDERED_MODE = RenderedRegistryMode.MODE_NAME;

    /**
     * Index of the source {@link GridCoverage2D} to use as a model. The destination grid coverage
     * will reuse the same coordinate reference system, envelope and qualitative categories than
     * this primary source.
     * <p>
     * For operations expecting only one source, there is no ambiguity. But for operations
     * expecting more than one source, the choice of a primary source is somewhat arbitrary.
     * This constant is used merely as a flag for spotting those places in the code.
     */
    private static final int PRIMARY_SOURCE_INDEX = 0;

    /**
     * The mapping from JAI's source type to Grid Coverage source type.
     */
    private static final Map SOURCE_TYPE_MAP =
            Collections.singletonMap(RenderedImage.class, GridCoverage2D.class);

    /**
     * The JAI's operation descriptor.
     */
    protected final OperationDescriptor operation;

    /**
     * Constructs a grid coverage operation from a JAI operation name. This convenience
     * constructor fetch the {@link OperationDescriptor} from the specified operation
     * name using the default {@link JAI} instance.
     *
     * @param operationName JAI operation name (e.g. {@code "GradientMagnitude"}).
     * @throws OperationNotFoundException if no JAI descriptor was found for the given name.
     */
    public OperationJAI(final String operationName) throws OperationNotFoundException {
        this(getOperationDescriptor(operationName));
    }

    /**
     * Constructs a grid coverage operation backed by a JAI operation. The operation descriptor
     * must supports the {@code "rendered"} mode (which is the case for most JAI operations).
     *
     * @param operation The operation descriptor.
     */
    public OperationJAI(final OperationDescriptor operation) {
        this(operation, 
             new ImagingParameterDescriptors(operation, SOURCE_TYPE_MAP, RENDERED_MODE));
    }

    /**
     * Constructs a grid coverage operation backed by a JAI operation. The operation descriptor
     * must supports the {@code "rendered"} mode (which is the case for most JAI operations).
     *
     * @param operation The operation descriptor.
     * @param parameters The parameters descriptor.
     */
    protected OperationJAI(final OperationDescriptor      operation,
                           final ParameterDescriptorGroup parameters)
    {
        super(parameters);
        this.operation = operation;
        /*
         * Check argument validity.
         */
        ensureRenderedImage(operation.getDestClass(RENDERED_MODE));
        final Class[] sourceClasses = operation.getSourceClasses(RENDERED_MODE);
        if (sourceClasses != null) {
            for (int i=0; i<sourceClasses.length; i++) {
                ensureRenderedImage(sourceClasses[i]);
            }
        }
        assert super.getNumSources() == operation.getNumSources();
    }

    /**
     * Returns the operation descriptor for the specified JAI operation name. This method
     * uses the default {@link JAI} instance and looks for the {@code "rendered"} mode.
     *
     * @param  name The operation name.
     * @return The operation descriptor for the given name.
     * @throws OperationNotFoundException if no JAI descriptor was found for the given name.
     *
     * @todo Should be inlined in the constructor if only Sun was to fix RFE #4093999
     *       ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static OperationDescriptor getOperationDescriptor(final String name)
            throws OperationNotFoundException
    {
        final OperationDescriptor operation = (OperationDescriptor) JAI.getDefaultInstance().
                                    getOperationRegistry().getDescriptor(RENDERED_MODE, name);
        if (operation == null) {
            throw new OperationNotFoundException(Resources.format(
                      ResourceKeys.ERROR_OPERATION_NOT_FOUND_$1, name));
        }
        return operation;
    }

    /**
     * Ensures that the specified class is assignable to {@link RenderedImage}.
     */
    private static final void ensureRenderedImage(final Class classe)
            throws IllegalArgumentException
    {
        if (!RenderedImage.class.isAssignableFrom(classe)) {
            // TODO: provide localized message
            throw new IllegalArgumentException(classe.getName());
        }
    }

    /**
     * Returns <code>true</code> if grid coverage should be transformed from sample values
     * to geophysics value before to apply an operation.
     */
    boolean computeOnGeophysicsValues() {
        return true;
    }

//    /**
//     * Apply a process operation to a grid coverage.
//     * The default implementation performs the following steps:
//     *
//     * <ul>
//     *   <li>Convert source grid coverages to their <cite>geophysics</cite> view, using
//     *       <code>{@link GridCoverage2D#geophysics GridCoverage2D.geophysics}(true)</code>.
//     *       This allow to performs all computation on geophysics values instead of encoded
//     *       samples.</li>
//     *   <li>Ensure that every source <code>GridCoverage2D</code>s use the same coordinate
//     *       system and have the same envelope.</li>
//     *   <li>Invoke {@link #deriveGridCoverage}.
//     *       The sources in the <code>ParameterBlock</code> are {@link RenderedImage} objects
//     *       obtained from {@link GridCoverage2D#getRenderedImage()}.</li>
//     *   <li>If the source <code>GridCoverage2D</code>s was not a geophysics view, convert the
//     *       result back to the same type with
//     *       <code>{@link GridCoverage2D#geophysics GridCoverage2D.geophysics}(false)</code>.</li>
//     * </ul>
//     *
//     * @param  parameters List of name value pairs for the parameters required for the operation.
//     * @param  hints A set of rendering hints, or {@code null} if none.
//     * @return The result as a grid coverage.
//     *
//     * @see #deriveGridCoverage
//     */
//    protected GridCoverage2D doOperation(final ParameterList  parameters,
//                                       final RenderingHints hints)
//    {
//        /*
//         * Copy parameter values from the ParameterList to the ParameterBlockJAI.
//         * The sources GridCoverages are extracted in the process and the source
//         * RenderedImage are set in the ParameterBlockJAI. The first array of
//         * range specifiers, if any, is treated especialy.
//         */
//        RangeSpecifier[]        ranges = null;
//        Boolean  requireGeophysicsType = null;
//        final ParameterBlockJAI  block = new ParameterBlockJAI(operation, RENDERED_MODE);
//        final String[]      paramNames = parameters.getParameterListDescriptor().getParamNames();
//        final String[] blockParamNames = block.getParameterListDescriptor().getParamNames();
//        final String[]     sourceNames = getSourceNames(parameters);
//        final GridCoverage2D[]   sources = new GridCoverage2D[length(sourceNames)];
//        for (int srcCount=0,i=0; i<paramNames.length; i++) {
//            final String name  = paramNames[i];
//            final Object value = parameters.getObjectParameter(name);
//            if (contains(sourceNames, name)) {
//                GridCoverage2D source = (GridCoverage2D) value;
//                if (computeOnGeophysicsValues()) {
//                    final GridCoverage2D old = source;
//                    source = source.geophysics(true);
//                    if (srcCount == PRIMARY_SOURCE_INDEX) {
//                        requireGeophysicsType = Boolean.valueOf(old==source);
//                    }
//                }
//                sources[srcCount++] = source;
//                continue;
//            }
//            if (!contains(blockParamNames, name)) {
//                if (value == null) {
//                    continue;
//                }
//                if (value instanceof RangeSpecifier) {
//                    ranges = new RangeSpecifier[] {(RangeSpecifier)value};
//                    continue;
//                }
//                if (value instanceof RangeSpecifier[]) {
//                    ranges = (RangeSpecifier[]) value;
//                    continue;
//                }
//            }
//            setParameter(block, name, value);
//        }
//        /*
//         * Ensures that all coverages use the same coordinate system and has the same envelope.
//         * After the projection, the method still checks all CS in case the user overrided the
//         * {@link #resampleToCommonGeometry} method.
//         */
//        resampleToCommonGeometry(sources, null, null, hints);
//        GridCoverage2D          coverage = sources[PRIMARY_SOURCE_INDEX];
//        final CoordinateReferenceSystem cs = coverage.getCoordinateReferenceSystem2D();
//        final MathTransform2D gridToCS = coverage.getGridGeometry().getGridToCoordinateSystem2D();
//        for (int i=0; i<sources.length; i++) {
//            final GridCoverage2D source = sources[i];
//            if (!cs.equals(getCoordinateSystem(source), false) ||
//                !gridToCS.equals(source.getGridGeometry().getGridToCoordinateSystem2D()))
//            {
//                throw new IllegalArgumentException(Resources.format(
//                        ResourceKeys.ERROR_INCOMPATIBLE_GRID_GEOMETRY));
//            }
//            block.addSource(source.getRenderedImage());
//        }
//        /*
//         * Apply the operation. This delegates the work to the chain of 'deriveXXX' methods.
//         */
//        coverage = deriveGridCoverage(sources, new Parameters(cs, gridToCS, block, hints, ranges));
//        if (requireGeophysicsType != null) {
//            coverage = coverage.geophysics(requireGeophysicsType.booleanValue());
//        }
//        return coverage;
//    }

    /**
     * Returns a sub-coordinate reference system for the specified dimension range.
     * This method is for internal use by {@link #resampleToCommonGeometry}.
     *
     * @param  crs   The coordinate reference system to decompose.
     * @param  lower The first dimension to keep, inclusive.
     * @param  upper The last  dimension to keep, exclusive.
     * @return The sub-coordinate system, or {@code null} if {@code lower} is equals to
     *         {@code upper}.
     */
    private static CoordinateReferenceSystem getSubCRS(final CoordinateReferenceSystem crs,
                                                       final int lower, final int upper)
    {
        if (lower == upper) {
            return null;
        }
        final CoordinateReferenceSystem candidate = CRSUtilities.getSubCRS(crs, lower, upper);
        if (candidate == null) {
            throw new InvalidGridGeometryException("Unsupported CRS: "+crs.getName().getCode());
        }
        return candidate;
    }

    /**
     * Resamples all sources grid coverages to the same {@linkplain GridGeometry2D two-dimensional
     * geometry} before to apply the {@linkplain #operation}. Only the two-dimensional part is
     * reprojected; extra dimension (if any) are left unchanged. Extra dimensions are typically
     * a time axis or a depth. Subclasses should override this method if they want to specify a
     * target {@linkplain GridGeometry2D grid geometry} and {@linkplain CoordinateReferenceSystem
     * coordinate reference system} different than the default one.
     *
     * @param  sources     The source grid coverages to resample. This array is updated in-place as
     *                     needed (for example if a grid coverage is replaced by a projected one).
     * @param  crs2D       The target coordinate reference system to use, or {@code null} for a
     *                     default one.
     * @param  gridToCrs2D The target "grid to coordinate reference system" transform, or
     *                     {@code null} for a default one.
     * @param  hints       The rendering hints, or {@code null} if none.
     *
     * @throws InvalidGridGeometryException if a source coverage has an unsupported grid geometry.
     * @throws CannotReprojectException if a grid coverage can't be resampled for some other reason.
     */
    protected void resampleToCommonGeometry(final GridCoverage2D[]  sources,
                                            CoordinateReferenceSystem crs2D,
                                            MathTransform2D     gridToCrs2D,
                                            final Hints               hints)
            throws InvalidGridGeometryException, CannotReprojectException
    {
        if (sources==null || sources.length==0) {
            return; // Nothing to reproject.
        }
        /*
         * Ensures that the target CRS is two-dimensional. If no target CRS were specified,
         * uses the CRS of the primary source. The math transform must be 2D too, but this
         * is ensured by the interface type (MathTransform2D).
         */
        final GridCoverage2D primarySource = sources[PRIMARY_SOURCE_INDEX];
        if (crs2D == null) {
            if (gridToCrs2D==null && sources.length==1) {
                return; // No need to reproject.
            }
            crs2D = primarySource.getCoordinateReferenceSystem2D();
        } else try {
            crs2D = CRSUtilities.getCRS2D(crs2D);
        } catch (TransformException exception) {
            // TODO: localize
            throw new CannotReprojectException("Unsupported CRS: "+crs2D.getName().getCode());
        }
        if (gridToCrs2D == null) {
            // TODO: Remove cast when we will be allowed to compile for J2SE 1.5.
            gridToCrs2D = ((GridGeometry2D) primarySource.getGridGeometry()).getGridToCoordinateSystem2D();
        }
        /*
         * 'crs2D' is the two dimensional part of the target CRS. Now for each source coverages,
         * substitute their two-dimensional CRS by this 'crs2D'. A source may have more than two
         * dimensions. For example it may have a time or a depth axis. In such case, their "head"
         * and "tail" CRS will be preserved before and after 'crs2D'.
         */
        final GridCoverageProcessor2D processor = getGridCoverageProcessor(hints);
        for (int i=0; i<sources.length; i++) {
            final GridCoverage2D            source    = sources[i];
            final GridGeometry2D            geometry  = (GridGeometry2D) source.getGridGeometry();
            final CoordinateReferenceSystem srcCrs2D  = source.getCoordinateReferenceSystem2D();
            final CoordinateReferenceSystem sourceCRS = source.getCoordinateReferenceSystem();
            final CoordinateReferenceSystem targetCRS;
            if (CRSUtilities.equalsIgnoreMetadata(crs2D, srcCrs2D)) {
                targetCRS = sourceCRS; // No reprojection needed for this source coverage.
            } else {
                /*
                 * Replaces the 2D part in the source CRS, while preserving the leading and
                 * trailing CRS (if any). Leading and trailing CRS are typically time axis or
                 * depth axis. Current implementation requires that the 2D part appears in two
                 * consecutive dimensions. Those dimensions are (0,1) in the majority of cases.
                 */
                final int  lowerDim = Math.min(geometry.axisDimensionX, geometry.axisDimensionY);
                final int  upperDim = Math.max(geometry.axisDimensionX, geometry.axisDimensionY)+1;
                final int sourceDim = sourceCRS.getCoordinateSystem().getDimension();
                if (upperDim-lowerDim != srcCrs2D.getCoordinateSystem().getDimension()) {
                    // TODO: localize
                    throw new InvalidGridGeometryException("Unsupported CRS: "+sourceCRS.getName().getCode());
                }
                final CoordinateReferenceSystem headCRS = getSubCRS(sourceCRS, 0, lowerDim);
                final CoordinateReferenceSystem tailCRS = getSubCRS(sourceCRS, upperDim, sourceDim);
                CoordinateReferenceSystem[]  components = new CoordinateReferenceSystem[3];
                int count = 0;
                if (headCRS != null) components[count++] = headCRS;
                                     components[count++] = crs2D;
                if (tailCRS != null) components[count++] = tailCRS;
                components = (CoordinateReferenceSystem[]) XArray.resize(components, count);
                if (count == 1) {
                    targetCRS = components[0];
                } else try {
                    targetCRS = FactoryFinder.getCRSFactory(hints).createCompoundCRS(
                                Collections.singletonMap(IdentifiedObject.NAME_KEY,
                                crs2D.getName().getCode()), components);
                } catch (FactoryException exception) {
                    throw new CannotReprojectException(exception.getLocalizedMessage(), exception);
                }
            }
            final MathTransform toSource2D = geometry.getGridToCoordinateSystem2D();
            final MathTransform toSource   = geometry.getGridToCoordinateSystem();
            final MathTransform toTarget;
            if (CRSUtilities.equalsIgnoreMetadata(gridToCrs2D, toSource2D)) {
                toTarget  = toSource;
            } else {
                /*
                 * Constructs the 'gridToCRS' transform in the same way than the CRS:
                 * leading and trailing dimensions (if any) are preserved.
                 */
                MathTransformFactory factory = FactoryFinder.getMathTransformFactory(hints);
//                try {
//                    MathTransform transform = source.getGridGeometry().getGridToCoordinateSystem();
//                    IntegerSequence headDim = JAIUtilities.createSequence(0, dimension-1);
//                    IntegerSequence tailDim = JAIUtilities.createSequence(dimension, sourceDim-1);
//                    MathTransform   headTr  = factory.createSubTransform(transform, headDim, null);
//                    MathTransform   tailTr  = factory.createSubTransform(transform, tailDim, null);
//                    if (!headTr.equals(gridToCS)) {
//                        headTr = factory.createPassThroughTransform(0, headTr, sourceDim-dimension);
//                        tailTr = factory.createPassThroughTransform(dimension, tailTr, 0);
//                        transform = factory.createConcatenatedTransform(headTr, tailTr);
//                    }
//                    targetGeom = new GridGeometry(null, transform);
//                } catch (FactoryException exception) {
//                    throw new CannotReprojectException(Resources.format(
//                            ResourceKeys.ERROR_CANT_REPROJECT_$1,
//                            source.getName(null)), exception);
//                }
            }
            sources[i] = (GridCoverage2D) processor.doOperation(
                            "Resample",                  source,
//                          "GridGeometry",              targetGeom,
                            "CoordinateReferenceSystem", targetCRS);
        }
    }

    /**
     * Applies a JAI operation to a grid coverage. This method is invoked automatically by
     * {@link #doOperation}. The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Gets the {@link SampleDimension}s for the target images by invoking the
     *       {@link #deriveSampleDimension deriveSampleDimension(...)} method.</li>
     *   <li>Applied the JAI operation using {@link #createRenderedImage}.</li>
     *   <li>Wraps the result in a {@link GridCoverage2D} object.</li>
     * </ul>
     *
     * @param  sources The source coverages.
     * @param  parameters Parameters, rendering hints and coordinate reference system to use.
     * @return The result as a grid coverage.
     *
     * @see #doOperation
     * @see #deriveSampleDimension
     * @see JAI#createNS
     */
    protected GridCoverage2D deriveGridCoverage(final GridCoverage2D[] sources,
                                                final Parameters    parameters)
    {
        GridCoverage2D primarySource = sources[PRIMARY_SOURCE_INDEX];
        /*
         * Gets the target SampleDimensions. If they are identical to the SampleDimensions of
         * one of the source GridCoverage2D, then this GridCoverage2D will be used at the primary
         * source. It will affect the target GridCoverage2D's name and the visible band. Then,
         * a new color model will be constructed from the new SampleDimensions, taking in
         * account the visible band.
         */
        final GridSampleDimension[][] list = new GridSampleDimension[sources.length][];
        for (int i=0; i<list.length; i++) {
            list[i] = sources[i].getSampleDimensions();
        }
        final GridSampleDimension[] sampleDims = deriveSampleDimension(list, parameters);
        int primarySourceIndex = -1;
        for (int i=0; i<list.length; i++) {
            if (Arrays.equals(sampleDims, list[i])) {
                primarySource = sources[i];
                primarySourceIndex = i;
                break;
            }
        }
        /*
         * Set the rendering hints image layout. Only the following properties will be set:
         *
         *     - Color model
         *     - Tile width
         *     - Tile height
         */
        RenderingHints hints = ImageUtilities.getRenderingHints(parameters.getSource());
        ImageLayout   layout = (hints!=null) ? (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT) : null;
        if (layout==null || !layout.isValid(ImageLayout.COLOR_MODEL_MASK)) {
            if (sampleDims!=null && sampleDims.length!=0) {
                if (layout == null) {
                    layout = new ImageLayout();
                }
                int visibleBand = GCSUtilities.getVisibleBand(primarySource.getRenderedImage());
                if (visibleBand >= sampleDims.length) {
                    visibleBand = 0;
                }
                final ColorModel colors;
                colors = sampleDims[visibleBand].getColorModel(visibleBand, sampleDims.length);
                layout = layout.setColorModel(colors);
            }
        }
        if (layout != null) {
            if (hints == null) {
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            } else {
                hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        if (parameters.hints != null) {
            if (hints != null) {
                hints.add(parameters.hints); // May overwrite the image layout we have just set.
            } else {
                hints = parameters.hints;
            }
        }
        /*
         * Performs the operation using JAI and construct the new grid coverage.
         * Uses the coordinate system from the main source coverage in order to
         * preserve the extra dimensions (if any). The first two dimensions should
         * be equal to the coordinate system set in the 'parameters' block.
         */
        final InternationalString      name = deriveName(sources, primarySourceIndex, parameters);
        final CoordinateReferenceSystem crs = primarySource.getCoordinateReferenceSystem();
        final MathTransform           toCRS = primarySource.getGridGeometry().getGridToCoordinateSystem();
        final RenderedImage            data = createRenderedImage(parameters.parameters, hints);
        return new GridCoverage2D(name,        // The grid coverage name
                                  data,        // The underlying data
                                  crs,         // The coordinate system (may not be 2D).
                                  toCRS,       // The grid transform (may not be 2D).
                                  sampleDims,  // The sample dimensions
                                  sources,     // The source grid coverages.
                                  null);       // Properties
    }

    /**
     * Returns the index of the quantitative category, providing that there is one and only one
     * quantitative category. If {@code categories} contains 0, 2 or more quantative category,
     * then this method returns {@code -1}.
     */
    private static int getQuantitative(final Category[] categories) {
        int index = -1;
        for (int i=0; i<categories.length; i++) {
            if (categories[i].isQuantitative()) {
                if (index >= 0) {
                    return -1;
                }
                index = i;
            }
        }
        return index;
    }

    /**
     * Returns the {@link SampleDimension}s for the target {@linkplain GridCoverage2D grid coverage}.
     * This method is invoked automatically by {@link #deriveGridCoverage deriveGridCoverage} with
     * a {@code bandLists} argument initialized as below:
     * <p>
     * <ul>
     *   <li>The {@code bandLists} array length is equals to the number of source coverages.</li>
     *   <li>The <code>bandLists[<var>i</var>]</code> array length is equals to the number of
     *       sample dimensions in the source coverage <var>i</var>.</li>
     *   <li>The sample dimension for a band at index <var>band</var> in the source at index
     *       <var>source</var> is {@code bandLists[source][band]}.</li>
     * </ul>
     * <p>
     * This method shall returns an array with a length equals to the number of bands in the target
     * image. If the sample dimensions can't be determined, then this method is allowed to returns
     * {@code null}.
     * <p>
     * The default implementation iterates among all bands and invokes the {@link #deriveCategory
     * deriveCategory} and {@link #deriveUnit deriveUnit} methods for each of them. Subclasses
     * should override this method if they know a more accurate algorithm for determining sample
     * dimensions.
     *
     * @param  bandLists The set of {@link SampleDimension}s for each source {@link GridCoverage2D}s.
     * @param  parameters Parameters, rendering hints and coordinate reference system to use.
     * @return The sample dimensions for each band in the destination image, or {@code null}
     *         if unknow.
     *
     * @see #deriveCategory
     * @see #deriveUnit
     */
    protected GridSampleDimension[] deriveSampleDimension(final GridSampleDimension[][] bandLists,
                                                          final Parameters              parameters)
    {
        /*
         * Computes the number of bands. Sources with only 1 band are treated as a special case:
         * their unique band is applied to all bands in other sources. If sources don't have the
         * same number of bands, then this method returns {@code null} since we don't know how to
         * handle those cases.
         */
        int numBands = 1;
        for (int i=0; i<bandLists.length; i++) {
            final int nb = bandLists[i].length;
            if (nb != 1) {
                if (numBands!=1 && nb!=numBands) {
                    return null;
                }
                numBands = nb;
            }
        }
        /*
         * Iterate among all bands. The 'result' array will contains SampleDimensions created
         * during the iteration for each individual band. The 'XS' suffix designate temporary
         * arrays of categories and units accross all sources for one particular band.
         */
        final GridSampleDimension[] result = new GridSampleDimension[numBands];
        final Category[]        categoryXS = new Category[bandLists.length];
        final Unit[]                unitXS = new Unit[bandLists.length];
        while (--numBands >= 0) {
            GridSampleDimension sampleDim = null;
            Category[]      categoryArray = null;
            int       indexOfQuantitative = 0;
            assert PRIMARY_SOURCE_INDEX == 0; // See comment below.
            for (int i=bandLists.length; --i>=0;) {
                /*
                 * Iterates among all sources (i) for the current band. We iterate
                 * sources in reverse order because the primary source MUST be the
                 * last one iterated, in order to have proper values for variables
                 * 'sampleDim', 'categoryArray' and 'indexOfQuantitative' after the
                 * loop.
                 */
                final GridSampleDimension[] allBands = bandLists[i];
                sampleDim           = allBands[allBands.length==1 ? 0 : numBands];
                categoryArray       = (Category[]) sampleDim.getCategories().toArray();
                indexOfQuantitative = getQuantitative(categoryArray);
                if (indexOfQuantitative < 0) {
                    return null;
                }
                unitXS    [i] = sampleDim.getUnits();
                categoryXS[i] = categoryArray[indexOfQuantitative];
            }
            final Category oldCategory = categoryArray[indexOfQuantitative];
            final Unit     oldUnit     = sampleDim.getUnits();
            final Category newCategory = deriveCategory(categoryXS, parameters);
            final Unit     newUnit     = deriveUnit(unitXS, parameters);
            if (newCategory == null) {
                return null;
            }
            if (!oldCategory.equals(newCategory) || !Utilities.equals(oldUnit, newUnit)) {
                categoryArray[indexOfQuantitative] = newCategory;
                result[numBands] = new GridSampleDimension(categoryArray, newUnit);
            } else {
                // Reuse the category list from the primary source.
                result[numBands] = sampleDim;
            }
        }
        return result;
    }

    /**
     * Returns the quantitative category for a single {@linkplain SampleDimension sample dimension}
     * in the target {@linkplain GridCoverage2D grid coverage}. This method is invoked automatically
     * by the {@link #deriveSampleDimension deriveSampleDimension} method for each band in the
     * target image. Subclasses should override this method in order to compute the target
     * {@link Category} from the source categories. For example, the {@code "add"} operation
     * may implements this method as below:
     *
     * <blockquote><pre>
     * NumberRange r0 = categories[0].getRange();
     * NumberRange r1 = categories[0].getRange();
     * double min = r0.getMinimum() + r1.getMinimum();
     * double min = r0.getMaximum() + r1.getMaximum();
     * NumberRange newRange = new NumberRange(min, max);
     * return new Category("My category", null, r0, newRange);
     * </pre></blockquote>
     *
     * @param  categories The quantitative categories from every sources. For unary operations
     *         like {@code "GradientMagnitude"}, this array has a length of 1. For binary
     *         operations like {@code "add"} and {@code "multiply"}, this array has a length of 2.
     * @param  parameters Parameters, rendering hints and coordinate reference system to use.
     * @return The quantative category to use in the destination image,
     *         or {@code null} if unknow.
     */
    protected Category deriveCategory(final Category[] categories, final Parameters parameters) {
        return null;
    }

    /**
     * Returns the unit of data for a single {@linkplain SampleDimension sample dimension} in the
     * target {@linkplain GridCoverage2D grid coverage}. This method is invoked automatically by
     * the {@link #deriveSampleDimension deriveSampleDimension} method for each band in the target
     * image. Subclasses should override this method in order to compute the target units from the
     * source units. For example a {@code "multiply"} operation may implement this method as below:
     *
     * <blockquote><pre>
     * if (units[0]!=null && units[1]!=null) {
     *     return units[0].{@link Unit#multiply(Unit) multiply}(units[1]);
     * } else {
     *     return super.deriveUnit(units, cs, parameters);
     * }
     * </pre></blockquote>
     *
     * @param  units The units from every sources. For unary operations like
     *         {@code "GradientMagnitude"}, this array has a length of 1. For binary operations
     *         like {@code "add"} and {@code "multiply"}, this array has a length of 2.
     * @param  parameters Parameters, rendering hints and coordinate reference system to use.
     * @return The unit of data in the destination image, or {@code null} if unknow.
     */
    protected Unit deriveUnit(final Unit[] units, final Parameters parameters) {
        return null;
    }

    /**
     * Returns a name for the target {@linkplain GridCoverage2D grid coverage} based on the given
     * sources. This method is invoked once by the {@link #deriveGridCoverage deriveGridCoverage}
     * method. The default implementation returns the operation name followed by the source name
     * between parenthesis, for example "<cite>GradientMagnitude(Sea Surface Temperature)</cite>".
     *
     * @param  sources The sources grid coverage.
     * @param  primarySourceIndex The index of what seems to be the primary source, or {@code -1}
     *         if none of unknow.
     * @param  parameters Parameters, rendering hints and coordinate reference system to use.
     * @return A name for the target grid coverage.
     */
    protected InternationalString deriveName(final GridCoverage2D[] sources,
                                             final int              primarySourceIndex,
                                             final Parameters       parameters)
    {
        final InternationalString[] names;
        if (primarySourceIndex >= 0) {
            names = new InternationalString[] {sources[primarySourceIndex].getName()};
        } else {
            names = new InternationalString[sources.length];
            for (int i=0; i<names.length; i++) {
                names[i] = sources[i].getName();
            }
        }
        return new Name(getName(), null);
    }

    /**
     * A localized name for the default implementation of {@link OperationJAI#deriveName}.
     */
    private static final class Name extends AbstractInternationalString implements Serializable {
        /** Serial number for cross-versions compatibility. */
        private static final long serialVersionUID = -8096255331549347383L;

        /** The operation name. */
        private final String operation;

        /** Names of source grid coverages. */
        private final InternationalString[] sources;

        /** Constructs a name from the given source names. */
        public Name(final String operation, final InternationalString[] sources) {
            this.operation = operation;
            this.sources   = sources;
        }

        /** Returns a string localized in the given locale. */
        public String toString(final Locale locale) {
            final StringBuffer buffer = new StringBuffer(operation);
            buffer.append('(');
            for (int i=0; i<sources.length; i++) {
                if (i != 0) {
                    buffer.append(", ");
                }
                buffer.append(sources[i].toString(locale));
            }
            buffer.append(')');
            return buffer.toString();
        }
    }

    /**
     * Applies the JAI operation. The operation name can be fetch from {@link #operation}.
     * The JAI instance to use can be fetch from {@link #getJAI}. The default implementation
     * returns the following:
     *
     * <blockquote><pre>
     * {@linkplain #getJAI getJAI}(hints).{@linkplain JAI#createNS createNS}({@linkplain #operation}.getName(), parameters, hints)
     * </pre></blockquote></li>
     *
     * Subclasses may override this method in order to invokes a different JAI operation
     * according the parameters.
     *
     * @param parameters The parameters to be given to JAI.
     * @param hints The rendering hints to be given to JAI.
     */
    protected RenderedImage createRenderedImage(final ParameterBlockJAI parameters,
                                                final RenderingHints    hints)
    {
        return getJAI(hints).createNS(operation.getName(), parameters, hints);
    }

    /**
     * Returns the {@link JAI} instance to use for operations on {@link RenderedImage}.
     * If no JAI instance is defined for the {@link Hints#JAI_INSTANCE} key, then the
     * default instance is returned.
     *
     * @param  hints The rendering hints, or {@code null} if none.
     * @return The JAI instance to use (never {@code null}).
     */
    protected static JAI getJAI(final RenderingHints hints) {
        if (hints != null) {
            final Object value = hints.get(Hints.JAI_INSTANCE);
            if (value instanceof JAI) {
                return (JAI) value;
            }
        }
        return JAI.getDefaultInstance();
    }

    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimisation
            return true;
        }
        if (super.equals(object)) {
            final OperationJAI that = (OperationJAI) object;
            return Utilities.equals(this.operation, that.operation);
        }
        return false;
    }

    /**
     * A block of parameters for a {@link GridCoverage2D} processed by a {@link OperationJAI}.
     * This parameter is given to the following methods:
     *
     * <ul>
     *   <li>{@link OperationJAI#deriveSampleDimension}</li>
     *   <li>{@link OperationJAI#deriveCategory}</li>
     *   <li>{@link OperationJAI#deriveUnit}</li>
     * </ul>
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    protected static final class Parameters {
        /**
         * The two dimensional coordinate reference system for all sources and the
         * destination {@link GridCoverage2D}. Sources coverages will be projected in
         * this CRS as needed.
         */
        public final CoordinateReferenceSystem crs;

        /**
         * The "grid to coordinate reference system" transform common to all source grid
         * coverages.
         */
        public final MathTransform2D gridToCRS;

        /**
         * The parameters to be given to the {@link JAI#createNS} method.
         */
        public final ParameterBlockJAI parameters;

        /**
         * The rendering hints to be given to the {@link JAI#createNS} method.
         * The {@link JAI} instance to use for the {@code createNS} call will
         * be fetch from the {@link Hints#JAI_INSTANCE} key.
         */
        public final RenderingHints hints;

        /**
         * The range, colors and units of the main quantitative {@link Category} to be created.
         * If non-null, then this array length matches the number of sources.
         */
        final RangeSpecifier[] rangeSpecifiers;

        /**
         * Construct a new parameter block with the specified values.
         */
        Parameters(final CoordinateReferenceSystem crs,
                   final MathTransform2D   gridToCRS,
                   final ParameterBlockJAI parameters,
                   final RenderingHints    hints,
                   final RangeSpecifier[]  rangeSpecifiers)
        {
            this.crs             = crs;
            this.gridToCRS       = gridToCRS;
            this.parameters      = parameters;
            this.hints           = hints;
            this.rangeSpecifiers = rangeSpecifiers;
        }

        /**
         * Returns the range specifier for the first source, or {@code null} if none.
         */
        final RangeSpecifier getRangeSpecifier() {
            return (rangeSpecifiers!=null && rangeSpecifiers.length!=0) ? rangeSpecifiers[0] : null;
        }

        /**
         * Returns the first source image, or {@code null} if none.
         */
        final RenderedImage getSource() {
            final int n = parameters.getNumSources();
            for (int i=0; i<n; i++) {
                final Object source = parameters.getSource(i);
                if (source instanceof RenderedImage) {
                    return (RenderedImage) source;
                }
            }
            return null;
        }
    }
}
