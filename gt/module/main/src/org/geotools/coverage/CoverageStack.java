/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.coverage;

// J2SE dependencies
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOReadProgressListener;
import java.lang.reflect.UndeclaredThrowableException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.GridCoverageProcessor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.image.io.IIOListeners;
import org.geotools.image.io.IIOReadProgressAdapter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.util.NumberRange;


/**
 * Wraps a stack of {@linkplain Coverage coverages} as an extra dimension. For example this class
 * can wraps an array of {@link org.geotools.coverage.grid.GridCoverage2D} on the same geographic
 * area, but where each {@code GridCoverage2D} is for a different date. This {@code CoverageStack}
 * manages the two-dimensional coverages as if the whole set was a huge three-dimensional coverage.
 * <br><br>
 * Each {@linkplain Element coverage element} in the stack usually covers the same
 * {@linkplain Coverage#getEnvelope geographic area} with the same
 * {@linkplain CoordinateReferenceSystem coordinate reference system},
 * but this is not a requirement. Coverage elements are often two-dimensional, but this is not
 * a requirement neither; this stack will simply append one more dimension to the coverage
 * element's CRS dimensions. Coverage elements may be other {@code CoverateStack} objects,
 * thus allowing construction of coverages with four or more dimensions.
 * <br><br>
 * {@code GridCoverage2D} objects tend to be big. In order to keep memory usage raisonable, this
 * implementation doesn't requires all {@code GridCoverage} objects at once. Instead, it requires
 * an array of {@link Element} objects, which will load the coverage content only when first
 * needed. This {@code CoverageStack} implementation remember the last coverage elements used;
 * it will not trig new data loading as long as consecutive calls to {@code evaluate(...)}
 * methods require the same coverage elements. Apart from this very simple caching mechanism,
 * caching is the responsability of {@link Element} implementations. Note that this simple
 * caching mechanism is suffisient if {@code evaluate(...)} methods are invoked with increasing
 * <var>z</var> values.
 * <br><br>
 * Each coverage element is expected to extends over a range of <var>z</var> values (the new
 * dimensions appended by this {@code CoverageStack}). If an {@code evaluate(...)} method is
 * invoked with a <var>z</var> value not falling in the middle of a coverage element, a linear
 * interpolation is applied.
 * <br><br>
 * <strong>Note:</strong> This implementation is thread-safe.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class CoverageStack extends AbstractCoverage {
    /**
     * Implementation hints for factories (none for now).
     */
    private static Hints HINTS = null;

    /**
     * An element in a {@linkplain CoverageStack coverage stack}. Each element is expected to
     * extends over a range of <var>z</var> values (the new dimensions appended by the
     * {@code CoverageStack} container). Implementations should be capable to returns
     * {@linkplain #getZRange z value range} without loading the coverage data. If an expensive
     * loading is required, it should be performed only when {@link #getCoverage} is invoked.
     * If {@code getCoverage} is invoked more than once, caching (if desirable) is implementor
     * responsability.
     * <br><br>
     * All methods declares {@link IOException} in their throws cause in case I/O operations are
     * required. Subclasses of {@code IOException} include {@link javax.imageio.IIOException} for
     * I/O operations that apply to images, or {@link java.rmi.RemoteOperation} for I/O operations
     * that apply to remote method invocation (which may be useful for large images database backed
     * by a distant server).
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static interface Element {
        /**
         * Returns a name for the coverage. This method should not load a large
         * amount of data, since it may be invoked soon.
         */
        String getName() throws IOException;
        
        /**
         * Returns the minimum and maximum <var>z</var> value for the coverage.
         * This information is mandatory. This method should not load a large
         * amount of data, since it may be invoked soon. Note that this method
         * may be invoked often, so it should be efficient.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        NumberRange getZRange() throws IOException;

        /**
         * The coverage envelope, or {@code null} if this information is too expensive to compute.
         * The envelope may or may not contains an extra dimension for the {@linkplain #getZRange
         * range of z values}, since the {@link CoverageStack} class is tolerant in this regard.
         * This method should not load a large amount of data, since it may be invoked soon.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        Envelope getEnvelope() throws IOException;

        /**
         * The coverage coordinate reference system, or {@code null} if this information is too
         * expensive to compute. The CRS may or may not contains an extra dimension for the
         * {@linkplain #getZRange range of z values}, since the {@link CoverageStack} class
         * is tolerant in this regard. This method should not load a large amount of data,
         * since it may be invoked soon.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        CoordinateReferenceSystem getCoordinateReferenceSystem() throws IOException;

        /**
         * The coverage grid geometry, or {@code null} if this information is too expensive to
         * compute. This method should not load a large amount of data, since it may be invoked
         * soon.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        GridGeometry getGridGeometry() throws IOException;

        /**
         * The sample dimension for the coverage, or {@code null} if this information is too
         * expensive to compute. This method should not load a large amount of data, since it
         * may be invoked soon.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        SampleDimension[] getSampleDimensions() throws IOException;

        /**
         * Returns the coverage, loading the data if needed. Implementations should invokes the
         * {@link IIOListeners#addListenersTo(ImageReader)} method if they use an image reader
         * for loading data. Caching (if desired) is implementor's responsability. The default
         * {@link CoverageStack} implementation caches only the last coverage used.
         *
         * @param  listeners Listeners to register to the {@linkplain ImageReader image I/O reader},
         *         if such a reader is going to be used.
         * @throws IOException if a data loading was required but failed.
         */
        Coverage getCoverage(IIOListeners listeners) throws IOException;
    }

    /**
     * Small number for floating point comparaisons.
     */
    private static final double EPS = 1E-6;
    
    /**
     * Coverage elements in this stack. Elements may be shared by more than one
     * instances of {@code CoverageStack}.
     */
    private final Element[] elements;
    
    /**
     * The sample dimensions for this coverage, or {@code null} if unknown.
     */
    private final SampleDimension[] sampleDimensions;
    
    /**
     * The envelope for this coverage. This is the union of all elements envelopes.
     *
     * @see #getEnvelope
     */
    private final Envelope envelope;

    /**
     * The dimension of the <var>z</var> ordinate (the last value in coordinate points).
     * This is always the CRS dimension minus 1.
     */
    private final int zDimension;
    
    /**
     * <code>true</code> if interpolations are allowed.
     */
    private boolean interpolationEnabled = true;
    
    /**
     * Maximal interval between the upper z-value of a coverage and the lower z-value of the next
     * one. If a greater difference is found, we will consider that there is a hole in the data
     * and {@code evaluate(...)} methods will returns NaN for <var>z</var> values in this hole.
     */
    private final double lagTolerance = 0;
    
    /**
     * List of objects to inform when image loading are trigged.
     */
    private final IIOListeners listeners = new IIOListeners();
    
    /**
     * Internal listener for logging image loading.
     */
    private transient Listeners readListener;
    
    /**
     * Coverage with a minimum z-value lower than or equals to the requested <var>z</var> value.
     * If possible, this class will tries to select a coverage with a middle value (not just the
     * minimum value) lower than the requested <var>z</var> value.
     */
    private transient Coverage lower;
    
    /**
     * Coverage with a maximum z-value higher than or equals to the requested <var>z</var> value.
     * If possible, this class will tries to select a coverage with a middle value (not just the
     * maximum value) higher than the requested <var>z</var> value.
     */
    private transient Coverage upper;
    
    /**
     * The coverage interpolated by the last call to {@link #getGridCoverage}. This coverage is
     * retained in order to avoid to constructs it many time if the same coverage is requested
     * more than once for the same <var>z</var> value.
     */
    private transient GridCoverage interpolated;
    
    /**
     * <var>Z</var> values in the middle of {@link #lower} and {@link #upper} envelope.
     */
    private transient double lowerZ=Double.POSITIVE_INFINITY, upperZ=Double.NEGATIVE_INFINITY;
    
    /**
     * <var>Z</var> value for the {@link #interpolated} coverage.
     */
    private transient double interpolatedZ = Double.NaN;
    
    /**
     * Range for {@link #lower} and {@link #upper}.
     */
    private transient NumberRange lowerRange, upperRange;
    
    /**
     * The grid coverage processor to uses for interpolations.
     * Will be created only when first needed.
     */
    private transient GridCoverageProcessor processor;
    
    /**
     * Initialize fields after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lowerZ        = Double.POSITIVE_INFINITY;
        upperZ        = Double.NEGATIVE_INFINITY;
        interpolatedZ = Double.NaN;
    }

    /**
     * Constructs a new coverage stack with all the supplied elements.
     *
     * @param  name     The name for this coverage.
     * @param  crs      The coordinate reference system for this coverage.
     * @param  elements All coverage {@link Element Element}s for this stack.
     * @throws IOException if an I/O operation was required and failed.
     */
    public CoverageStack(final CharSequence name, final CoordinateReferenceSystem crs,
                         final Set elements) throws IOException
    {
        super(name, crs, null, null);
        this.elements = (Element[]) elements.toArray(new Element[elements.size()]);
        try {
            Arrays.sort(this.elements, COMPARATOR);
        } catch (UndeclaredThrowableException exception) {
            throw rethrow(exception);
        }
        zDimension = crs.getCoordinateSystem().getDimension() - 1;
        boolean      sampleDimensionMismatch = false;
        SampleDimension[]   sampleDimensions = null;
        GeneralEnvelope            envelope  = null;
        CoordinateOperation        transform = null;
        CoordinateOperationFactory factory   = null;
        for (int j=0; j<this.elements.length; j++) {
            final Element element = this.elements[j];
            if (true) {
                /*
                 * Ensures that all coverages uses the same number of sample dimension.
                 * To be strict, we should ensure that all sample dimensions are identical.
                 * However, this is not needed for proper working of this class, so we will
                 * ensure this condition only in 'getSampleDimension' method.
                 */
                final SampleDimension[] candidate = element.getSampleDimensions();
                if (candidate != null) {
                    if (sampleDimensions == null) {
                        sampleDimensions = candidate;
                    } else {
                        if (sampleDimensions.length != candidate.length) {
                            throw new IllegalArgumentException( // TODO: localize
                                        "Inconsistent number of sample dimensions.");
                        }
                        if (!Arrays.equals(sampleDimensions, candidate)) {
                            sampleDimensionMismatch = true;
                        }
                    }
                }
            }
            /*
             * Computes an envelope for all coverage elements. If a coordinate reference system
             * information is bundled with the envelope, it will be used in order to reproject
             * the envelope on the fly (if needed). Otherwise, CRS are assumed the same than the
             * one specified at construction time.
             */
            Envelope candidate = element.getEnvelope();
            if (candidate == null) {
                continue;
            }
            final CoordinateReferenceSystem sourceCRS = element.getCoordinateReferenceSystem();
            if (sourceCRS != null) {
                final CoordinateReferenceSystem targetCRS = CRSUtilities.getSubCRS(crs, 0,
                                                sourceCRS.getCoordinateSystem().getDimension());
                if (targetCRS == null) {
                    // TODO: localize
                    throw new IllegalArgumentException("An element uses an incompatible CRS");
                }
                if (!CRSUtilities.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
                    /*
                     * The envelope needs to be reprojected. Gets the transform
                     * if it was not already done, and applies the reprojection.
                     */
                    if (transform == null ||
                        !CRSUtilities.equalsIgnoreMetadata(transform.getSourceCRS(), sourceCRS))
                    {
                        if (factory == null) {
                            factory = FactoryFinder.getCoordinateOperationFactory(HINTS);
                        }
                        try {
                            transform = factory.createOperation(sourceCRS, targetCRS);
                        } catch (FactoryException exception) {
                            final IllegalArgumentException e = // TODO: localize the message below.
                                    new IllegalArgumentException("An element uses an incompatible CRS");
                            e.initCause(exception); // TODO: uses J2SE 1.5 constructor instead.
                            throw e;
                        }
                    }
                    try {
                        candidate = CRSUtilities.transform(transform.getMathTransform(), candidate);
                    } catch (TransformException exception) {
                        final IllegalArgumentException e = // TODO: localize the message below.
                                new IllegalArgumentException("An element uses an incompatible CRS");
                        e.initCause(exception); // TODO: uses J2SE 1.5 constructor instead.
                        throw e;
                    }
                }
            }
            /*
             * Increase the envelope in order to contains 'candidate'.
             * The range of z-values will be included in the envelope.
             */
            final boolean set = (envelope == null);
            if (set) {
                envelope = new GeneralEnvelope(zDimension + 1);
            }
            final int dim = candidate.getDimension();
            for (int i=0; i<=zDimension; i++) {
                double min = envelope.getMinimum(i);
                double max = envelope.getMaximum(i);
                final double minimum, maximum;
                if (i < dim) {
                    minimum = candidate.getMinimum(i);
                    maximum = candidate.getMaximum(i);
                } else if (i == zDimension) {
                    final NumberRange range = element.getZRange();
                    minimum = range.getMinimum();
                    maximum = range.getMaximum();
                } else {
                    minimum = Double.NEGATIVE_INFINITY;
                    maximum = Double.POSITIVE_INFINITY;
                }
                if (set || minimum<min) min=minimum;
                if (set || maximum>max) max=maximum;
                envelope.setRange(i, min, max);
            }
        }
        this.sampleDimensions = sampleDimensionMismatch ? null : sampleDimensions;
        this.envelope = (envelope!=null) ? envelope : CRSUtilities.getEnvelope(crs);
    }
    
    /**
     * Constructs a new coverage using the same elements than the specified coverage stack.
     */
    protected CoverageStack(final CharSequence name, final CoverageStack source) {
        super(name, source);
        elements             = source.elements;
        sampleDimensions     = source.sampleDimensions;
        envelope             = source.envelope;
        zDimension           = source.zDimension;
        interpolationEnabled = source.interpolationEnabled;
    }
    
    /**
     * A comparator for {@link Element} sorting and binary search. This comparator uses the
     * middle <var>z</var> value as criterion. It must accepts {@link Double} objects as well
     * as {@link Element}, because binary search will mix those two kinds of object.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object entry1, final Object entry2) {
            try {
                return Double.compare(zFromObject(entry1),
                                      zFromObject(entry2));
            } catch (IOException exception) {
                throw new UndeclaredThrowableException(exception);
                // Will be catch and rethrown as IOException
                // by all methods using this comparator.
            }
        }
    };
    
    /**
     * Returns the <var>z</var> value of the specified object. The specified
     * object may be a {@link Double} or an {@link Element} instance.
     *
     * @param  object The object to sort.
     * @return The z-value of the specified object.
     * @throws IOException if an I/O operation was required but failed.
     * @throws ClassCastException if {@code object} is not an instance of {@link Double}
     *         or {@link Element}.
     */
    private static double zFromObject(final Object object) throws IOException, ClassCastException {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        return getZ((Element) object);
    }
    
    /**
     * Returns the middle <var>z</var> value. If the element has no <var>z</var> value
     * (for example if the <var>z</var> value is the time and the coverage is constant
     * over the time), then this method returns {@link Double#NaN}.
     */
    private static double getZ(final Element entry) throws IOException {
        return getZ(entry.getZRange());
    }
    
    /**
     * Returns the <var>z</var> value in the middle of the specified range.
     * If the range is null, then this method returns {@link Double#NaN}.
     */
    private static double getZ(final NumberRange range) {
        if (range != null) {
            final Number lower = (Number) range.getMinValue();
            final Number upper = (Number) range.getMaxValue();
            if (lower != null) {
                if (upper != null) {
                    return 0.5 * (lower.doubleValue() + upper.doubleValue());
                } else {
                    return lower.doubleValue();
                }
            } else if (upper != null) {
                return upper.doubleValue();
            }
        }
        return Double.NaN;
    }
    
    /**
     * Returns the bounding box for the coverage domain in coordinate system coordinates.
     */
    public Envelope getEnvelope() {
        return (Envelope) ((Cloneable) envelope).clone();
    }
    
    /**
     * Returns the number of sample dimension in this coverage.
     */
    public int getNumSampleDimensions() {
        if (sampleDimensions != null) {
            return sampleDimensions.length;
        } else {
            // TODO: provides a localized message.
            throw new IllegalStateException("Sample dimensions are undetermined.");
        }
    }
    
    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension.
     */
    public SampleDimension getSampleDimension(final int index) {
        if (sampleDimensions != null) {
            return sampleDimensions[index];
        } else {
            // TODO: provides a localized message.
            throw new IllegalStateException("Sample dimensions are undetermined.");
        }
    }
    
    /**
     * Check if the given coordinate reference system is compatible with this coverage's
     * {@link #crs}. This method is used for assertions.
     *
     * @param  sourceCRS The coordinate reference system to test.
     * @return <code>true</code> if the specified crs is compatible.
     *
     * @todo Inspects the axis information provided in GridGeometry2D.
     */
    private boolean isCompatibleCRS(final CoordinateReferenceSystem sourceCRS) {
        return CRSUtilities.equalsIgnoreMetadata(sourceCRS,
               CRSUtilities.getSubCRS(crs, 0, sourceCRS.getCoordinateSystem().getDimension()));
    }
    
    /**
     * Snaps the specified coordinate point to the coordinate of the nearest voxel available in
     * this coverage. First, this method locate the {@linkplain Element coverage element} at or
     * near the last ordinate value (the <var>z</var> value). If no coverage is available at the
     * specified <var>z</var> value, then the nearest one is selected. Next, this method locate
     * the pixel under the {@code point} coordinate in the coverage element. The {@code point}
     * is then set to the pixel center coordinate and to the <var>z</var> value of the selected
     * coverage element. Consequently, calling any {@code evaluate(...)} method with snapped
     * coordinates will returns non-interpolated values.
     *
     * @param  point The point to snap.
     * @throws IOException if an I/O operation was required but failed.
     */
    public void snap(final DirectPosition point) throws IOException { // No synchronization needed.
        double z = point.getOrdinate(zDimension);
        int index;
        try {
            index = Arrays.binarySearch(elements, new Double(z), COMPARATOR);
        } catch (UndeclaredThrowableException exception) {
            throw rethrow(exception);
        }
        if (index < 0) {
            /*
             * There is no exact match for the z value.
             * Snap it to the closest coverage element.
             */
            index = ~index;
            if (index == elements.length) {
                if (index == 0) {
                    return; // No elements in this coverage
                }
                z = getZ(elements[--index]);
            } else if (index == 0) {
                z = getZ(elements[index]);
            } else {
                final double lowerZ = getZ(elements[index-1]);
                final double upperZ = getZ(elements[index  ]);
                assert !(z<=lowerZ || z>=upperZ) : z; // Use !(...) in order to accept NaN values.
                if (Double.isNaN(upperZ) || z-lowerZ < upperZ-z) {
                    index--;
                    z = lowerZ;
                } else {
                    z = upperZ;
                }
            }
            point.setOrdinate(zDimension, z);
        }
        /*
         * Now that we know the coverage element,
         * snap the spatial coordinate point.
         */
        try {
            final Element element = elements[index];
            final CoordinateReferenceSystem sourceCRS = element.getCoordinateReferenceSystem();
            if (!CRSUtilities.equalsIgnoreMetadata(crs, sourceCRS)) {
                // TODO: Implement coordinate operation.
                throw new CannotEvaluateException("Incompatible CRS.");
            }
            final GridGeometry  geometry  = element .getGridGeometry();
            final GridRange     range     = geometry.getGridRange();
            final MathTransform transform = geometry.getGridToCoordinateSystem();
            final int           dimension = transform.getSourceDimensions();
            DirectPosition position = new GeneralDirectPosition(dimension);
            for (int i=dimension; --i>=0;) {
                // Copy only the first dimensions (may not be up to crs.dimension)
                position.setOrdinate(i, point.getOrdinate(i));
            }
            position = transform.inverse().transform(position, position);
            for (int i=dimension; --i>=0;) {
                position.setOrdinate(i, Math.max(range.getLower(i),
                                        Math.min(range.getUpper(i)-1,
                                   (int)Math.rint(position.getOrdinate(i)))));
            }
            position = transform.transform(position, position);
            for (int i=Math.min(dimension, zDimension); --i>=0;) {
                // Do not touch the z-value, copy the other ordinates.
                point.setOrdinate(i, position.getOrdinate(i));
            }
        } catch (TransformException exception) {
            throw new CannotEvaluateException(cannotEvaluate(point), exception);
        }
    }
    
    /**
     * Returns a message for exception.
     *
     * @todo provides a better formatting of the point coordinate.
     */
    private static String cannotEvaluate(final DirectPosition point) {
        return Resources.format(ResourceKeys.ERROR_CANT_EVALUATE_$1, point);
    }
    
    /**
     * Loads a single coverage for the specified element. All {@code evaluate(...)} methods
     * ultimately loads their coverages through this method. It provides a single place where
     * to add post-loading processing, if needed.
     *
     * @param  element The coverage to load.
     * @return The loaded coverage.
     * @throws IOException if an error occured while loading image.
     */
    private Coverage load(final Element element) throws IOException {
        return element.getCoverage(listeners);
    }
    
    /**
     * Loads a single image at the given index.
     *
     * @param  index Index in {@link #elements} for the image to load.
     * @throws IOException if an error occured while loading image.
     */
    private void load(final int index) throws IOException {
        final Element    element = elements[index];
        final NumberRange zRange = element.getZRange();
        logLoading(ResourceKeys.LOADING_IMAGE_$1, new String[]{element.getName()});
        lower      = upper      = load(element);
        lowerZ     = upperZ     = getZ(zRange);
        lowerRange = upperRange = zRange;
    }
    
    /**
     * Loads images for the given elements.
     *
     * @throws IOException if an error occured while loading images.
     */
    private void load(final Element lowerElement, final Element upperElement) throws IOException {
        logLoading(ResourceKeys.LOADING_IMAGES_$2, new String[]{lowerElement.getName(),
                                                                upperElement.getName()});
        final NumberRange lowerRange = lowerElement.getZRange();
        final NumberRange upperRange = upperElement.getZRange();
        final Coverage lower = load(lowerElement);
        final Coverage upper = load(upperElement);
        
        this.lower      = lower; // Set only when BOTH images are OK.
        this.upper      = upper;
        this.lowerZ     = getZ(lowerRange);
        this.upperZ     = getZ(upperRange);
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }
    
    /**
     * Loads coverages required for a linear interpolation at the specified <var>z</var> value.
     * The loaded coverages will be stored in {@link #lower} and {@link #upper} fields. It is
     * possible that the same coverage is given to those two fields, if this method determine
     * that no interpolation is necessary.
     *
     * @param  z The z value.
     * @return <code>true</code> if data were found.
     * @throws PointOutsideCoverageException if the <var>z</var> value is outside the allowed range.
     * @throws CannotEvaluateException if the operation failed for some other reason.
     */
//    private boolean seek(final double z) throws CannotEvaluateException {
//        /*
//         * Check if currently loaded coverages
//         * are valid for the requested z value.
//         */
//        if ((z>=lowerZ && z<=upperZ) || (Double.isNaN(z) && Double.isNaN(lowerZ) && Double.isNaN(upperZ))) {
//            return true;
//        }
//        /*
//         * Currently loaded coverages are not valid for the requested z value.
//         * Search for the coverage to use as upper bounds ({@link #upper}).
//         */
//        int index;
//        try {
//            index = Arrays.binarySearch(elements, new Double(z), COMPARATOR);
//        } catch (UndeclaredThrowableException exception) {
//            // TODO: localize
//            throw new CannotEvaluateException("Can't fetch coverage properties.", rethrow(exception));
//        }
//        try {
//            if (index >= 0) {
//                /*
//                 * An exact match has been found.
//                 * Load only this coverage and exit.
//                 */
//                load(index);
//                return true;
//            }
//            index = ~index; // Insertion point (note: ~ is NOT the minus sign).
//            if (index == elements.length) {
//                if (--index>=0) { // Does this coverage has at least 1 image?
//                    /*
//                     * The requested date is after the last image's central time.
//                     * Maybe it is not after the last image's *end* time. Check...
//                     */
//                    if (elements[index].getTimeRange().contains(date)) {
//                        load(index);
//                        return true;
//                    }
//                }
//                // fall through the exception at this method's end.
//            } else if (index == 0) {
//                /*
//                 * The requested date is before the first image's central time.
//                 * Maybe it is not before the first image's *start* time. Check...
//                 */
//                if (elements[index].getTimeRange().contains(date)) {
//                    load(index);
//                    return true;
//                }
//                // fall through the exception at this method's end.
//            } else {
//                /*
//                 * An interpolation between two image seems possible.
//                 * Checks if there is not a time lag between both.
//                 */
//                final Element lowerEntry = elements[index-1];
//                final Element upperEntry = elements[index  ];
//                final Range lowerRange = lowerEntry.getTimeRange();
//                final Range upperRange = upperEntry.getTimeRange();
//                final long  lowerEnd   = zFromObject(lowerRange.getMaxValue());
//                final long  upperStart = zFromObject(upperRange.getMinValue())-1; // MIN_VALUE-1 == MAX_VALUE
//                if (lowerEnd+lagTolerance >= upperStart) {
//                    if (interpolationEnabled) {
//                        load(lowerEntry, upperEntry);
//                    } else {
//                        if (Math.abs(getZ(upperRange)-time) > Math.abs(time-getZ(lowerRange))) {
//                            index--;
//                        }
//                        load(index);
//                    }
//                    return true;
//                }
//                if (lowerRange.contains(date)) {
//                    load(index-1);
//                    return true;
//                }
//                if (upperRange.contains(date)) {
//                    load(index);
//                    return true;
//                }
//                return false; // Missing data.
//            }
//        } catch (IOException exception) {
//            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
//        }
//        throw new PointOutsideCoverageException(Resources.format(ResourceKeys.ERROR_DATE_OUTSIDE_COVERAGE_$1, date));
//    }
    
    /**
     * Returns a 2 dimensional grid coverage for the given date.
     *
     * @param  time The date where to evaluate.
     * @return The grid coverage at the specified time, or <code>null</code>
     *         if the requested date fall in a hole in the data.
     * @throws PointOutsideCoverageException if <code>time</code> is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
//    public synchronized GridCoverage2D getGridCoverage(final Date time) throws CannotEvaluateException {
//        if (!seek(time)) {
//            // Missing data
//            return null;
//        }
//        if (lower == upper) {
//            // No interpolation needed.
//            return lower;
//        }
//        assert isCompatibleCRS(lower.getCoordinateReferenceSystem()) : lower;
//        assert isCompatibleCRS(upper.getCoordinateReferenceSystem()) : upper;
//        
//        final long timeMillis = time.getTime();
//        assert (timeMillis>=lowerTime && timeMillis<=upperTime) : time;
//        if (timeMillis==timeInterpolated && interpolated!=null) {
//            return interpolated;
//        }
//        final double ratio = (double)(timeMillis-lowerTime) / (double)(upperTime-lowerTime);
//        if (Math.abs(  ratio) <= EPS) return lower;
//        if (Math.abs(1-ratio) <= EPS) return upper;
//        if (interpolationEnabled) {
//            final GridCoverageProcessor processor = getGridCoverageProcessor2D();
//            final Operation operation = processor.getOperation("Combine");
//            final ParameterValueGroup param = operation.getParameters();
//            param.parameter("source0").setValue(lower);
//            param.parameter("source1").setValue(upper);
//            param.parameter("matrix").setValue(new double[][]{{1-ratio, ratio, 0}});
//            interpolated = (GridCoverage2D) processor.doOperation(operation, param);
//            timeInterpolated = timeMillis; // Set only if previous line has been successfull.
//            return interpolated;
//        } else {
//            return (ratio <= 0.5) ? lower : upper;
//        }
//    }
    
    /**
     * Rethrows the exception in {@link #COMPARATOR} as a {@link RuntimeException}.
     * It gives an opportunity for implementations of {@link Element} to uses some
     * checked exception like {@link IOException}.
     */
    private static IOException rethrow(final UndeclaredThrowableException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof IOException) {
            return (IOException) cause;
        }
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw exception;
    }
    
    /**
     * Projète un point du système de coordonnées de cette couverture vers le système
     * de l'image spécifiée. Cette méthode doit être utilisée avant d'appeller une
     * méthode <code>evaluate(...)</code> sur la couverture spécifiée.
     *
     * @param  point Le point à transformer. Ce point ne sera jamais modifié.
     * @return Le point transformé.
     * @throws CannotEvaluateException si la transformation n'a pas pu être faites.
     */
//    private Point2D project(final Point2D point, final GridCoverage2D coverage) throws CannotEvaluateException {
//        // TODO: On ne prend que les deux première dimensions parce que, pour une raison non
//        //       élucidée, l'opération "NodataFilter" retourne un système de coordonnées 2D.
//        try {
//            final CoordinateReferenceSystem targetCS = CRSUtilities.getCRS2D(coverage.getCoordinateReferenceSystem());
//            if (CRSUtilities.equalsIgnoreMetadata(CRSUtilities.getCRS2D(crs), targetCS)) {
//                return point;
//            }
//            // TODO: Implémenter la transformation de coordonnées.
//            throw new CannotEvaluateException("Système de coordonnées incompatibles.");
//        } catch (TransformException exception) {
//            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
//        }
//    }
    
    /**
     * Returns a sequence of integer values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * inherited from {@link CoverageTable}:  usually bicubic for spatial axis, and
     * linear for temporal axis.
     *
     * @param  point The coordinate point where to evaluate.
     * @param  time  The date where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to create a new array.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws PointOutsideCoverageException if <code>point</code> or <code>time</code> is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
//    public synchronized int[] evaluate(final Point2D point, final Date time, int[] dest)
//            throws CannotEvaluateException
//    {
//        if (!seek(time)) {
//            // Missing data
//            if (dest == null) {
//                dest = new int[bands.length];
//            }
//            Arrays.fill(dest, 0, bands.length, 0);
//            return dest;
//        }
//        if (lower == upper) {
//            return lower.evaluate(point, dest);
//        }
//        int[] last=null;
//        last = upper.evaluate(project(point, upper), last);
//        dest = lower.evaluate(project(point, lower), dest);
//        final long timeMillis = time.getTime();
//        assert (timeMillis>=lowerTime && timeMillis<=upperTime) : time;
//        final double ratio = (double)(timeMillis-lowerTime) / (double)(upperTime-lowerTime);
//        for (int i=0; i<last.length; i++) {
//            dest[i] = (int)Math.round(dest[i] + ratio*(last[i]-dest[i]));
//        }
//        return dest;
//    }
    
    /**
     * Returns a sequence of float values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * inherited from {@link CoverageTable}:  usually bicubic for spatial axis, and
     * linear for temporal axis.
     *
     * @param  point The coordinate point where to evaluate.
     * @param  time  The date where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to create a new array.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws PointOutsideCoverageException if <code>point</code> or <code>time</code> is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
//    public synchronized float[] evaluate(final Point2D point, final Date time, float[] dest)
//            throws CannotEvaluateException 
//    {
//        if (!seek(time)) {
//            // Missing data
//            if (dest == null) {
//                dest = new float[bands.length];
//            }
//            Arrays.fill(dest, 0, bands.length, Float.NaN);
//            return dest;
//        }
//        if (lower == upper) {
//            return lower.evaluate(point, dest);
//        }
//        float[] last=null;
//        last = upper.evaluate(project(point, upper), last);
//        dest = lower.evaluate(project(point, lower), dest);
//        final long timeMillis = time.getTime();
//        assert (timeMillis>=lowerTime && timeMillis<=upperTime) : time;
//        final double ratio = (double)(timeMillis-lowerTime) / (double)(upperTime-lowerTime);
//        for (int i=0; i<last.length; i++) {
//            final float lower = dest[i];
//            final float upper = last[i];
//            float value = (float)(lower + ratio*(upper-lower));
//            if (Float.isNaN(value)) {
//                if (!Float.isNaN(lower)) {
//                    assert Float.isNaN(upper) : upper;
//                    if (lowerTimeRange.contains(time)) {
//                        value = lower;
//                    }
//                } else if (!Float.isNaN(upper)) {
//                    assert Float.isNaN(lower) : lower;
//                    if (upperTimeRange.contains(time)) {
//                        value = upper;
//                    }
//                }
//            }
//            dest[i] = value;
//        }
//        return dest;
//    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * inherited from {@link CoverageTable}:  usually bicubic for spatial axis, and
     * linear for temporal axis.
     *
     * @param  point The coordinate point where to evaluate.
     * @param  time  The date where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to create a new array.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws PointOutsideCoverageException if <code>point</code> or <code>time</code> is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
//    public synchronized double[] evaluate(final Point2D point, final Date time, double[] dest)
//            throws CannotEvaluateException 
//    {
//        if (!seek(time)) {
//            // Missing data
//            if (dest == null) {
//                dest = new double[bands.length];
//            }
//            Arrays.fill(dest, 0, bands.length, Double.NaN);
//            return dest;
//        }
//        if (lower == upper) {
//            return lower.evaluate(point, dest);
//        }
//        double[] last=null;
//        last = upper.evaluate(project(point, upper), last);
//        dest = lower.evaluate(project(point, lower), dest);
//        final long timeMillis = time.getTime();
//        assert (timeMillis>=lowerTime && timeMillis<=upperTime) : time;
//        final double ratio = (double)(timeMillis-lowerTime) / (double)(upperTime-lowerTime);
//        for (int i=0; i<last.length; i++) {
//            final double lower = dest[i];
//            final double upper = last[i];
//            double value = (lower + ratio*(upper-lower));
//            if (Double.isNaN(value)) {
//                if (!Double.isNaN(lower)) {
//                    assert Double.isNaN(upper) : upper;
//                    if (lowerTimeRange.contains(time)) {
//                        value = lower;
//                    }
//                } else if (!Double.isNaN(upper)) {
//                    assert Double.isNaN(lower) : lower;
//                    if (upperTimeRange.contains(time)) {
//                        value = upper;
//                    }
//                }
//            }
//            dest[i] = value;
//        }
//        return dest;
//    }
    
    /**
     * Returns <code>true</code> if interpolation are enabled in the <var>z</var> value dimension.
     * Interpolations are enabled by default.
     */
    public boolean isInterpolationEnabled() {
        return interpolationEnabled;
    }
    
    /**
     * Enable or disable interpolations in the <var>z</var> value dimension.
     */
    public synchronized void setInterpolationEnabled(final boolean flag) {
        lower                = null;
        upper                = null;
        interpolated         = null;
        lowerZ               = Double.POSITIVE_INFINITY;
        upperZ               = Double.NEGATIVE_INFINITY;
        interpolatedZ        = Double.NaN;
        interpolationEnabled = flag;
    }
    
    /**
     * Adds an {@link IIOReadWarningListener} to the list of registered warning listeners.
     */
    public void addIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.addIIOReadWarningListener(listener);
    }
    
    /**
     * Removes an {@link IIOReadWarningListener} from the list of registered warning listeners.
     */
    public void removeIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.removeIIOReadWarningListener(listener);
    }
    
    /**
     * Adds an {@link IIOReadProgressListener} to the list of registered progress listeners.
     */
    public void addIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.addIIOReadProgressListener(listener);
    }
    
    /**
     * Removes an {@link IIOReadProgressListener} from the list of registered progress listeners.
     */
    public void removeIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.removeIIOReadProgressListener(listener);
    }
    
    /**
     * Invoked automatically when an image is about to be loaded. The default implementation
     * logs the message in the {@code "org.geotools.coverage"} logger. Subclasses can override
     * this method if they wants a different logging.
     *
     * @param record The log record. The message contains information about the images to load.
     */
    protected void logLoading(final LogRecord record) {
        Logger.getLogger("org.geotools.coverage").log(record);
    }
    
    /**
     * Prepares a log record about an image to be loaded, and put the log record in a stack.
     * The record will be effectively logged only when image loading really beging.
     */
    private void logLoading(final int key, final Object[] parameters) {
        final Locale locale = null;
        final LogRecord record = Resources.getResources(locale).getLogRecord(Level.INFO, key);
        record.setSourceClassName("CoverageStack");
        record.setSourceMethodName("evaluate");
        record.setParameters(parameters);
        if (readListener == null) {
            readListener = new Listeners();
            addIIOReadProgressListener(readListener);
        }
        readListener.record = record;
    }
    
    /**
     * A listener for monitoring image loading. The purpose for this listener is to
     * log a message when an image is about to be loaded.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class Listeners extends IIOReadProgressAdapter {
        /**
         * The record to log.
         */
        public LogRecord record;
        
        /**
         * Reports that an image read operation is beginning.
         */
        public void imageStarted(ImageReader source, int imageIndex) {
            if (record != null) {
                logLoading(record);
                source.removeIIOReadProgressListener(this);
                record = null;
            }
        }
    }
}
