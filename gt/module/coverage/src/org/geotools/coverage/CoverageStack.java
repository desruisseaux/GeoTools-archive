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

// J2SE and JAI dependencies
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
import javax.media.jai.InterpolationNearest;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.image.io.IIOListeners;
import org.geotools.image.io.IIOReadProgressAdapter;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.Interpolator2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.NumberRange;


/**
 * Wraps a stack of {@linkplain Coverage coverages} as an extra dimension. For example this class
 * can wraps an array of {@link org.geotools.coverage.grid.GridCoverage2D} on the same geographic
 * area, but where each {@code GridCoverage2D} is for a different date. This {@code CoverageStack}
 * manages the two-dimensional coverages as if the whole set was a huge three-dimensional coverage.
 * <p>
 * Each {@linkplain Element coverage element} in the stack usually covers the same
 * {@linkplain Coverage#getEnvelope geographic area}, but this is not a requirement. However,
 * they must use the same {@linkplain CoordinateReferenceSystem coordinate reference system}.
 * For performance reason, the later condition will not be checked except at construction time
 * if the CRS is provided in the envelope, and at evaluation time if Java assertion are enabled.
 * If the CRS of coverage elements is uncertain, consider wrapping them in a
 * {@link TransformedCoverage} object.
 * <p>
 * Coverage elements are often two-dimensional, but this is not a requirement. This stack will
 * simply append one more dimension to the coverage element's CRS dimensions. Coverage elements
 * may be other {@code CoverateStack} objects, thus allowing construction of coverages with four
 * or more dimensions.
 * <p>
 * {@code GridCoverage2D} objects tend to be big. In order to keep memory usage raisonable, this
 * implementation doesn't requires all {@code GridCoverage} objects at once. Instead, it requires
 * an array of {@link Element} objects, which will load the coverage content only when first
 * needed. This {@code CoverageStack} implementation remember the last coverage elements used;
 * it will not trig new data loading as long as consecutive calls to {@code evaluate(...)}
 * methods require the same coverage elements. Apart from this very simple caching mechanism,
 * caching is the responsability of {@link Element} implementations. Note that this simple
 * caching mechanism is suffisient if {@code evaluate(...)} methods are invoked with increasing
 * <var>z</var> values.
 * <p>
 * Each coverage element is expected to extends over a range of <var>z</var> values (the new
 * dimensions appended by this {@code CoverageStack}). If an {@code evaluate(...)} method is
 * invoked with a <var>z</var> value not falling in the middle of a coverage element, a linear
 * interpolation is applied.
 * <p>
 * <strong>Note:</strong> This implementation is thread-safe.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageStack extends AbstractCoverage {
    /**
     * An element in a {@linkplain CoverageStack coverage stack}. Each element is expected to
     * extents over a range of <var>z</var> values (the new dimensions appended by the
     * {@code CoverageStack} container). Implementations should be capable to returns coverage's
     * {@linkplain #getZRange range of z-values} without loading the coverage's data. If an
     * expensive loading is required, it should be delayed until the {@link #getCoverage} method
     * is invoked. If {@code getCoverage} is invoked more than once, caching (if desirable) is
     * implementor's responsability.
     * <p>
     * All methods declares {@link IOException} in their throws cause in case I/O operations are
     * required. Subclasses of {@code IOException} include {@link javax.imageio.IIOException} for
     * image I/O operations, or {@link java.rmi.RemoteOperation} for remote method invocation
     * (which may be useful for large images database backed by a distant server).
     *
     * @since 2.1
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static interface Element {
        /**
         * Returns a name for the coverage. This method should not load a large amount of data,
         * since it may be invoked soon. This method is invoked just before {@link #getCoverage}
         * in order to log a "Loading data..." message.
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
         * Returns the coverage envelope, or {@code null} if this information is too expensive to
         * compute. The envelope may or may not contains an extra dimension for the
         * {@linkplain #getZRange range of z values}, since the {@link CoverageStack} class is
         * tolerant in this regard. This method should not load a large amount of data, since it
         * may be invoked soon.
         *
         * @throws IOException if an I/O operation was required but failed.
         */
        Envelope getEnvelope() throws IOException;

        /**
         * The coverage grid geometry, or {@code null} if this information do not applies or is too
         * expensive to compute. This method should not load a large amount of data, since it may be
         * invoked soon.
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
     * A convenience adapter class for wrapping a pre-loaded {@link Coverage} into an
     * {@link Element} object. This adapter provides basic implementation for all methods,
     * but they a require a fully constructed {@link Coverage} object. Subclasses are strongly
     * encouraged to provides alternative implementation loading only the minimum amount of data
     * required for each method.
     *
     * @since 2.1
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Adapter implements Element {
        /**
         * The wrapped coverage, or {@code null} if not yet loaded.
         * If null, the loading must be performed by the {@link #getCoverage} method.
         */
        protected Coverage coverage;

        /**
         * Minimum and maximum <var>z</var> values for this element, or {@code null} if not yet
         * determined. If {@code null}, the range must be computed by the {@link #getZRange} method.
         */
        protected NumberRange range;

        /**
         * Constructs a new adapter for the specified coverage and <var>z</var> values.
         *
         * @param coverage The coverage to wrap. Can be {@code null} only if this constructor
         *                 is invoked from a sub-class constructor.
         * @param range    The minimum and maximum <var>z</var> values for this element, or
         *                 {@code null} to infers it from the last dimension in the coverage's
         *                 envelope.
         */
        public Adapter(final Coverage coverage, final NumberRange range) {
            this.coverage = coverage;
            this.range    = range;
            if (getClass() == Adapter.class) {
                if (coverage == null) {
                    // TODO: provides a localized message.
                    throw new IllegalArgumentException("coverage");
                }
            }
        }

        /**
         * Returns the coverage name. The default implementation delegates to the
         * {@linkplain #getCoverage underlying coverage} if it is an instance of
         * {@link AbstractCoverage}.
         */
        public String getName() throws IOException {
            Object coverage = getCoverage(null);
            if (coverage instanceof AbstractCoverage)  {
                coverage = ((AbstractCoverage) coverage).getName();
            }
            return coverage.toString();
        }
        
        /**
         * Returns the minimum and maximum <var>z</var> values for the coverage. If the range was
         * not explicitly specified to the constructor, then the default implementation infers it
         * from the last dimension in the coverage's envelope.
         */
        public NumberRange getZRange() throws IOException {
            if (range == null) {
                final Envelope envelope = getEnvelope();
                final int zDimension = envelope.getDimension() - 1;
                range = new NumberRange(envelope.getMinimum(zDimension),
                                        envelope.getMaximum(zDimension));
            }
            return range;
        }

        /**
         * Returns the coverage envelope. The default implementation delegates to the
         * {@linkplain #getCoverage underlying coverage}.
         */
        public Envelope getEnvelope() throws IOException {
            return getCoverage(null).getEnvelope();
        }

        /**
         * Returns the coverage grid geometry. The default implementation delegates to the
         * {@linkplain #getCoverage underlying coverage} if it is an instance of
         * {@link GridCoverage}.
         */
        public GridGeometry getGridGeometry() throws IOException {
            final Coverage coverage = getCoverage(null);
            return (coverage instanceof GridCoverage) ?
                    ((GridCoverage) coverage).getGridGeometry() : null;
        }

        /**
         * Returns the sample dimension for the coverage. The default implementation delegates to the
         * {@linkplain #getCoverage underlying coverage}.
         */
        public SampleDimension[] getSampleDimensions() throws IOException {
            final Coverage coverage = getCoverage(null);
            final SampleDimension[] sd = new SampleDimension[coverage.getNumSampleDimensions()];
            for (int i=0; i<sd.length; i++) {
                sd[i] = coverage.getSampleDimension(i);
            }
            return sd;
        }

        /**
         * Returns the coverage. Implementors can overrides this method if they want to load
         * {@link #coverage} only when first needed. However, they are strongly encouraged to
         * override all other methods as well in order to load the minimum amount of data,
         * since all default implementations invoke {@code getCoverage(null)}.
         */
        public Coverage getCoverage(final IIOListeners listeners) throws IOException {
            return coverage;
        }
    }
    
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
     * The number of sample dimensions for this coverage, or 0 is unknow.
     * Note: this attribute may be non-null even if {@link #sampleDimensions} is null.
     */
    private final int numSampleDimensions;
    
    /**
     * The envelope for this coverage. This is the union of all elements envelopes.
     *
     * @see #getEnvelope
     */
    private final GeneralEnvelope envelope;

    /**
     * A direct position with {@link #zDimension} dimensions. will be created only
     * when first needed.
     */
    private transient GeneralDirectPosition reducedPosition;

    /**
     * The dimension of the <var>z</var> ordinate (the last value in coordinate points).
     * This is always the {@linkplain #getCoordinateReferenceSystem() coordinate reference
     * system} dimension minus 1.
     *
     * @since 2.3
     */
    public final int zDimension;
    
    /**
     * {@code true} if interpolations are allowed.
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
     * <var>Z</var> values in the middle of {@link #lower} and {@link #upper} envelope.
     */
    private transient double lowerZ=Double.POSITIVE_INFINITY, upperZ=Double.NEGATIVE_INFINITY;
    
    /**
     * Range for {@link #lower} and {@link #upper}.
     */
    private transient NumberRange lowerRange, upperRange;

    /**
     * Sample byte values. Allocated when first needed, in order to avoid allocating
     * thel again everytime an {@code evaluate(...)} method is invoked.
     */
    private transient byte[] byteBuffer;

    /**
     * Sample integer values. Allocated when first needed, in order to avoid allocating
     * thel again everytime an {@code evaluate(...)} method is invoked.
     */
    private transient int[] intBuffer;

    /**
     * Sample float values. Allocated when first needed, in order to avoid allocating
     * thel again everytime an {@code evaluate(...)} method is invoked.
     */
    private transient float[] floatBuffer;

    /**
     * Sample double values. Allocated when first needed, in order to avoid allocating
     * thel again everytime an {@code evaluate(...)} method is invoked.
     */
    private transient double[] doubleBuffer;
    
    /**
     * Initialize fields after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lowerZ = Double.POSITIVE_INFINITY;
        upperZ = Double.NEGATIVE_INFINITY;
    }

    /**
     * Constructs a new coverage stack with all the supplied elements. All coverages must uses the
     * same coordinate reference system. Additionnaly, all coverages must specify their <var>z</var>
     * value in the last dimension of their envelope. The example below constructs two dimensional
     * grid coverages (to be given as the {@code coverages} argument) for the same area, but at
     * different times:
     *
     * <blockquote><pre>
     * GridCoverageFactory     factory = ...;
     * CoordinateReferenceSystem crs2D = ...;  // Yours horizontal CRS.
     * TemporalCRS             timeCRS = ...;  // Yours CRS for time measurement.
     * CoordinateReferenceSystem crs3D = new CompoundCRS(crs3D, timeCRS);
     *
     * List&lt;Coverage&gt; coverages = new ArrayList&lt;Coverage&gt;();
     * GeneralEnvelope envelope = new GeneralEnvelope(3); // A <strong>3-dimensional</strong> envelope.
     * envelope.setRange(...);                            // Set the horizontal part.
     * for (int i=0; i<...; i++) {
     *     envelope.setRange(2, startTime, endTime);
     *     coverages.add(factory.create(..., crs, envelope, ...);
     * }
     * </pre></blockquote>
     * 
     * This convenience constructor wraps all coverage intos a {@link Adapter Adapter} object.
     * Users with a significant amount of data are encouraged to uses the constructor expecting
     * {@link Element Element} objects instead, in order to provides their own implementation
     * loading data only when needed.
     *
     * @param  name      The name for this coverage.
     * @param  coverages All {@link Coverage} elements for this stack.
     * @throws IOException if an I/O operation was required and failed.
     */
    public CoverageStack(final CharSequence name,
                         final Collection/*<Coverage>*/ coverages) throws IOException
    {
        this(name, getCoordinateReferenceSystem(coverages), toElements(coverages));
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private static CoordinateReferenceSystem getCoordinateReferenceSystem(final Collection coverages) {
        CoordinateReferenceSystem crs = null;
        for (final Iterator it=coverages.iterator(); it.hasNext();) {
            final CoordinateReferenceSystem candidate = ((Coverage) it.next()).getCoordinateReferenceSystem();
            if (crs == null) {
                crs = candidate;
            } else if (!crs.equals(candidate)) {
                // TODO: localize
                throw new IllegalArgumentException("Inconsistent coordinate reference system");
            }
        }
        return crs;
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private static Collection/*<Element>*/ toElements(final Collection/*<Coverage>*/ coverages) {
        final List elements = new ArrayList(coverages.size());
        for (final Iterator it=coverages.iterator(); it.hasNext();) {
            elements.add(new Adapter((Coverage) it.next(), null));
        }
        return elements;
    }

    /**
     * Constructs a new coverage stack with all the supplied elements.
     *
     * @param  name     The name for this coverage.
     * @param  crs      The coordinate reference system for this coverage.
     * @param  elements All coverage {@link Element Element}s for this stack.
     * @throws IOException if an I/O operation was required and failed.
     */
    public CoverageStack(final CharSequence             name,
                         final CoordinateReferenceSystem crs,
                         final Collection/*<Element>*/ elements) throws IOException
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
            final Envelope candidate = element.getEnvelope();
            if (candidate == null) {
                continue;
            }
            final CoordinateReferenceSystem sourceCRS;
            // TODO: use a more direct way if we add an accessor for that in GeoAPI.
            sourceCRS = candidate.getLowerCorner().getCoordinateReferenceSystem();
            if (sourceCRS != null) {
                final int dim = sourceCRS.getCoordinateSystem().getDimension();
                if (dim<zDimension || dim>zDimension+1) {
                    // TODO: localize
                    throw new MismatchedDimensionException("An element uses an incompatible CRS");
                }
                final CoordinateReferenceSystem targetCRS = CRSUtilities.getSubCRS(crs, 0, dim);
                if (!CRSUtilities.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
                    // TODO: localize
                    throw new IllegalArgumentException("An element uses an incompatible CRS");
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
        this.numSampleDimensions = (sampleDimensions!=null) ? sampleDimensions.length : 0;
        this.sampleDimensions = sampleDimensionMismatch ? null : sampleDimensions;
        if (envelope != null) {
            this.envelope = envelope;
            envelope.setCoordinateReferenceSystem(crs);
        } else {
            assert this.elements.length == 0;
            this.envelope = new GeneralEnvelope(CRSUtilities.getEnvelope(crs));
        }
    }
    
    /**
     * Constructs a new coverage using the same elements than the specified coverage stack.
     */
    protected CoverageStack(final CharSequence name, final CoverageStack source) {
        super(name, source);
        elements             = source.elements;
        sampleDimensions     = source.sampleDimensions;
        numSampleDimensions  = source.numSampleDimensions;
        envelope             = source.envelope;
        zDimension           = source.zDimension;
        interpolationEnabled = source.interpolationEnabled;
    }
    
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
     * Returns {@code true} if the specified z-value is inside the specified range.
     */
    private static boolean contains(final NumberRange range, final double z) {
        return z>=range.getMinimum() && z<=range.getMaximum();
    }
    
    /**
     * Returns the bounding box for the coverage domain in coordinate system coordinates.
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }
    
    /**
     * Returns the number of sample dimension in this coverage.
     */
    public int getNumSampleDimensions() {
        if (numSampleDimensions != 0) {
            return numSampleDimensions;
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
        final Element element = elements[index];
        final GridGeometry geometry = element.getGridGeometry();
        if (geometry != null) {
            final GridRange     range     = geometry.getGridRange();
            final MathTransform transform = geometry.getGridToCoordinateSystem();
            final int           dimension = transform.getSourceDimensions();
            DirectPosition position = new GeneralDirectPosition(dimension);
            for (int i=dimension; --i>=0;) {
                // Copy only the first dimensions (may not be up to crs.dimension)
                position.setOrdinate(i, point.getOrdinate(i));
            }
            try {
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
    }
    
    /**
     * Returns a message for exception.
     *
     * @todo provides a better formatting of the point coordinate.
     */
    private static String cannotEvaluate(final DirectPosition point) {
        return Errors.format(ErrorKeys.CANT_EVALUATE_$1, point);
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
        Coverage coverage = element.getCoverage(listeners);
        if (coverage instanceof GridCoverage2D) {
            final GridCoverage2D coverage2D = (GridCoverage2D) coverage;
            if (interpolationEnabled) {
                if (coverage2D.getInterpolation() instanceof InterpolationNearest) {
                    coverage = Interpolator2D.create(coverage2D);
                }
            }
        }
        /*
         * CRS assertions (for debugging purpose).
         */
        final CoordinateReferenceSystem sourceCRS;
        assert CRSUtilities.equalsIgnoreMetadata((sourceCRS=coverage.getCoordinateReferenceSystem()),
               CRSUtilities.getSubCRS(crs, 0, sourceCRS.getCoordinateSystem().getDimension())) :
               sourceCRS + "\n\n" + crs;
        assert coverage.getNumSampleDimensions() == numSampleDimensions : coverage;
        return coverage;
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
        logLoading(VocabularyKeys.LOADING_IMAGE_$1, new String[] {element.getName()});
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
        logLoading(VocabularyKeys.LOADING_IMAGES_$2, new String[] {lowerElement.getName(),
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
     * @return {@code true} if data were found.
     * @throws PointOutsideCoverageException if the <var>z</var> value is outside the allowed range.
     * @throws CannotEvaluateException if the operation failed for some other reason.
     */
    private boolean seek(final double z) throws CannotEvaluateException {
        assert Thread.holdsLock(this);
        /*
         * Check if currently loaded coverages
         * are valid for the requested z value.
         */
        if ((z>=lowerZ && z<=upperZ) || (Double.isNaN(z) && Double.isNaN(lowerZ) && Double.isNaN(upperZ))) {
            return true;
        }
        /*
         * Currently loaded coverages are not valid for the requested z value.
         * Search for the coverage to use as upper bounds ({@link #upper}).
         */
        final Number Z = new Double(z);
        int index;
        try {
            index = Arrays.binarySearch(elements, Z, COMPARATOR);
        } catch (UndeclaredThrowableException exception) {
            // TODO: localize
            throw new CannotEvaluateException("Can't fetch coverage properties.", rethrow(exception));
        }
        try {
            if (index >= 0) {
                /*
                 * An exact match has been found.
                 * Load only this coverage and exit.
                 */
                load(index);
                return true;
            }
            index = ~index; // Insertion point (note: ~ is NOT the minus sign).
            if (index == elements.length) {
                if (--index >= 0) { // Does this stack has at least 1 coverage?
                    /*
                     * The requested z is after the last coverage's central z.
                     * Maybe it is not after the last coverage's upper z. Check...
                     */
                    if (elements[index].getZRange().contains(Z)) {
                        load(index);
                        return true;
                    }
                }
                // fall through the exception at this method's end.
            } else if (index == 0) {
                /*
                 * The requested z is before the first coverage's central z.
                 * Maybe it is not before the first coverage's lower z. Check...
                 */
                if (elements[index].getZRange().contains(Z)) {
                    load(index);
                    return true;
                }
                // fall through the exception at this method's end.
            } else {
                /*
                 * An interpolation between two coverages seems possible.
                 * Checks if there is not a z lag between both.
                 */
                final Element     lowerElement = elements[index-1];
                final Element     upperElement = elements[index  ];
                final NumberRange lowerRange   = lowerElement.getZRange();
                final NumberRange upperRange   = upperElement.getZRange();
                final double      lowerEnd     = lowerRange.getMaximum();
                final double      upperStart   = upperRange.getMinimum();
                if (lowerEnd+lagTolerance >= upperStart) {
                    if (interpolationEnabled) {
                        load(lowerElement, upperElement);
                    } else {
                        if (Math.abs(getZ(upperRange)-z) > Math.abs(z-getZ(lowerRange))) {
                            index--;
                        }
                        load(index);
                    }
                    return true;
                }
                if (lowerRange.contains(Z)) {
                    load(index-1);
                    return true;
                }
                if (upperRange.contains(Z)) {
                    load(index);
                    return true;
                }
                return false; // Missing data.
            }
        } catch (IOException exception) {
            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
        }
        throw new OrdinateOutsideCoverageException(Errors.format(
                  ErrorKeys.ZVALUE_OUTSIDE_COVERAGE_$2, getName(), Z), zDimension);
    }
    
    /**
     * Returns a point with the same number of dimensions than the specified coverage.
     * The number of dimensions must be {@link #zDimensions} or {@code zDimensions+1}.
     */
    private final DirectPosition reduce(DirectPosition coord, final Coverage coverage) {
        final CoordinateReferenceSystem targetCRS = coverage.getCoordinateReferenceSystem();
        final int dimension = targetCRS.getCoordinateSystem().getDimension();
        if (dimension == zDimension) {
            if (reducedPosition == null) {
                reducedPosition = new GeneralDirectPosition(zDimension);
            }
            for (int i=0; i<dimension; i++) {
                reducedPosition.ordinates[i] = coord.getOrdinate(i);
            }
            coord = reducedPosition;
        } else {
            assert CRSUtilities.equalsIgnoreMetadata(crs, targetCRS) : targetCRS;
        }
        return coord;
    }
    
    /**
     * Returns a sequence of values for a given point in the coverage. The default implementation
     * delegates to the {@link #evaluate(DirectPosition, double[])} method.
     *
     * @param  coord The coordinate point where to evaluate.
     * @return The value at the specified point.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public Object evaluate(final DirectPosition coord)
            throws CannotEvaluateException
    {
        return evaluate(coord, (double[]) null);
    }
    
    /**
     * Returns a sequence of boolean values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public synchronized boolean[] evaluate(final DirectPosition coord, boolean[] dest)
            throws CannotEvaluateException
    {
        final double z = coord.getOrdinate(zDimension);
        if (!seek(z)) {
            // Missing data
            if (dest == null) {
                dest = new boolean[numSampleDimensions];
            } else {
                Arrays.fill(dest, 0, numSampleDimensions, false);
            }
            return dest;
        }
        if (lower == upper) {
            return lower.evaluate(reduce(coord, lower), dest);
        }
        assert !(z<lowerZ || z>upperZ) : z;   // Uses !(...) in order to accepts NaN.
        final Coverage coverage = (z >= 0.5*(lowerZ+upperZ)) ? upper : lower;
        return coverage.evaluate(reduce(coord, coverage), dest);
    }
    
    /**
     * Returns a sequence of byte values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public synchronized byte[] evaluate(final DirectPosition coord, byte[] dest)
            throws CannotEvaluateException
    {
        final double z = coord.getOrdinate(zDimension);
        if (!seek(z)) {
            // Missing data
            if (dest == null) {
                dest = new byte[numSampleDimensions];
            } else {
                Arrays.fill(dest, 0, numSampleDimensions, (byte)0);
            }
            return dest;
        }
        if (lower == upper) {
            return lower.evaluate(reduce(coord, lower), dest);
        }
        byteBuffer = upper.evaluate(reduce(coord, upper), byteBuffer);
        dest       = lower.evaluate(reduce(coord, lower), dest);
        assert !(z<lowerZ || z>upperZ) : z;   // Uses !(...) in order to accepts NaN.
        final double ratio = (z-lowerZ) / (upperZ-lowerZ);
        for (int i=0; i<byteBuffer.length; i++) {
            dest[i] = (byte)Math.round(dest[i] + ratio*(byteBuffer[i]-dest[i]));
        }
        return dest;
    }
    
    /**
     * Returns a sequence of integer values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public synchronized int[] evaluate(final DirectPosition coord, int[] dest)
            throws CannotEvaluateException
    {
        final double z = coord.getOrdinate(zDimension);
        if (!seek(z)) {
            // Missing data
            if (dest == null) {
                dest = new int[numSampleDimensions];
            } else {
                Arrays.fill(dest, 0, numSampleDimensions, 0);
            }
            return dest;
        }
        if (lower == upper) {
            return lower.evaluate(reduce(coord, lower), dest);
        }
        intBuffer = upper.evaluate(reduce(coord, upper), intBuffer);
        dest      = lower.evaluate(reduce(coord, lower), dest);
        assert !(z<lowerZ || z>upperZ) : z;   // Uses !(...) in order to accepts NaN.
        final double ratio = (z-lowerZ) / (upperZ-lowerZ);
        for (int i=0; i<intBuffer.length; i++) {
            dest[i] = (int)Math.round(dest[i] + ratio*(intBuffer[i]-dest[i]));
        }
        return dest;
    }
    
    /**
     * Returns a sequence of float values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public synchronized float[] evaluate(final DirectPosition coord, float[] dest)
            throws CannotEvaluateException
    {
        final double z = coord.getOrdinate(zDimension);
        if (!seek(z)) {
            // Missing data
            if (dest == null) {
                dest = new float[numSampleDimensions];
            }
            Arrays.fill(dest, 0, numSampleDimensions, Float.NaN);
            return dest;
        }
        if (lower == upper) {
            return lower.evaluate(reduce(coord, lower), dest);
        }
        floatBuffer = upper.evaluate(reduce(coord, upper), floatBuffer);
        dest        = lower.evaluate(reduce(coord, lower), dest);
        assert !(z<lowerZ || z>upperZ) : z;   // Uses !(...) in order to accepts NaN.
        final double ratio = (z-lowerZ) / (upperZ-lowerZ);
        for (int i=0; i<floatBuffer.length; i++) {
            final float lower = dest[i];
            final float upper = floatBuffer[i];
            float value = (float)(lower + ratio*(upper-lower));
            if (Float.isNaN(value)) {
                if (!Float.isNaN(lower)) {
                    assert Float.isNaN(upper) : upper;
                    if (contains(lowerRange, z)) {
                        value = lower;
                    }
                } else if (!Float.isNaN(upper)) {
                    assert Float.isNaN(lower) : lower;
                    if (contains(upperRange, z)) {
                        value = upper;
                    }
                }
            }
            dest[i] = value;
        }
        return dest;
    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws PointOutsideCoverageException if {@code coord} is outside coverage.
     * @throws CannotEvaluateException if the computation failed for some other reason.
     */
    public synchronized double[] evaluate(final DirectPosition coord, double[] dest)
            throws CannotEvaluateException
    {
        final double z = coord.getOrdinate(zDimension);
        if (!seek(z)) {
            // Missing data
            if (dest == null) {
                dest = new double[numSampleDimensions];
            }
            Arrays.fill(dest, 0, numSampleDimensions, Double.NaN);
            return dest;
        }
        if (lower == upper) {
            return lower.evaluate(reduce(coord, lower), dest);
        }
        doubleBuffer = upper.evaluate(reduce(coord, upper), doubleBuffer);
        dest         = lower.evaluate(reduce(coord, lower), dest);
        assert !(z<lowerZ || z>upperZ) : z;   // Uses !(...) in order to accepts NaN.
        final double ratio = (z-lowerZ) / (upperZ-lowerZ);
        for (int i=0; i<doubleBuffer.length; i++) {
            final double lower = dest[i];
            final double upper = doubleBuffer[i];
            double value = lower + ratio*(upper-lower);
            if (Double.isNaN(value)) {
                if (!Double.isNaN(lower)) {
                    assert Double.isNaN(upper) : upper;
                    if (contains(lowerRange, z)) {
                        value = lower;
                    }
                } else if (!Double.isNaN(upper)) {
                    assert Double.isNaN(lower) : lower;
                    if (contains(upperRange, z)) {
                        value = upper;
                    }
                }
            }
            dest[i] = value;
        }
        return dest;
    }
    
    /**
     * Returns the coverages to be used for the specified <var>z</var> value. Special cases:
     * <p>
     * <ul>
     *   <li>If there is no coverage available for the specified <var>z</var> value, returns
     *       an {@linkplain Collections#EMPTY_LIST empty list}.</li>
     *   <li>If there is only one coverage available, or if the specified <var>z</var> value
     *       falls exactly in the middle of the {@linkplain Element#getZRange range value}
     *       (i.e. no interpolation are needed), or if {@linkplain #setInterpolationEnabled
     *       interpolations are disabled}, then this method returns a
     *       {@linkplain Collections#singletonList singleton}.</li>
     *   <li>Otherwise, this method returns a list containing at least 2 coverages, one before
     *       and one after the specified <var>z</var> value.</li>
     * </ul>
     *
     * @param z The z value for the coverages to be returned.
     * @return  The coverages for the specified values. May contains 0, 1 or 2 elements.
     *
     * @since 2.3
     */
    public synchronized List/*<Coverage>*/ coveragesAt(final double z) {
        if (!seek(z)) {
            return Collections.EMPTY_LIST;
        }
        if (lower == upper) {
            return Collections.singletonList(lower);
        }
        return Arrays.asList(new Coverage[] {lower, upper});
    }
    
    /**
     * Returns {@code true} if interpolation are enabled in the <var>z</var> value dimension.
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
        lowerZ               = Double.POSITIVE_INFINITY;
        upperZ               = Double.NEGATIVE_INFINITY;
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
        final LogRecord record = Vocabulary.getResources(locale).getLogRecord(Level.INFO, key);
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
