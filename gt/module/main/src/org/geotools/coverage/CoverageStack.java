/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le D�veloppement
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

// Utilities
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.media.jai.util.Range;
import java.lang.reflect.UndeclaredThrowableException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// I/O
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.imageio.ImageReader;
import javax.swing.event.EventListenerList;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOReadProgressListener;
//import org.geotools.io.image.IIOReadProgressAdapter;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.grid.GridRange;
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
import org.geotools.coverage.SampleDimensionGT;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.GridCoverageProcessor2D;
import org.geotools.referencing.FactoryFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.CRSUtilities;


/**
 * Wraps a stack of {@linkplain Coverage coverages} as an extra dimension. For example this class
 * can wraps an array of {@link org.geotools.coverage.grid.GridCoverage2D} on the same geographic
 * area, but where each {@code GridCoverage2D} is for a different date. This {@code CoverageStack}
 * manages the two-dimensional coverages as if the whole set was a huge three-dimensional coverage.
 * <br><br>
 * Each {@linkplain Element coverage element} in the stack usually covers the same
 * {@linkplain Coverage#getEnvelope geographic area}, but this is not a requirement.
 * However, current implementation requires that every coverage element uses the same
 * {@linkplain CoordinateReferenceSystem coordinate reference system}. Coverage elements
 * are often two-dimensional, but this is not a requirement neither; this stack will simply
 * append one more dimension to the coverage element's CRS dimensions. Coverage elements may
 * be other {@code CoverateStack} objects, thus allowing construction of coverages with four
 * or more dimensions.
 * <br><br>
 * {@code GridCoverage2D} objects tend to be big. In order to keep memory usage raisonable, this
 * implementation doesn't requires all {@code GridCoverage} objects at once. Instead, it requires
 * an array of {@link Element} objects, which will load the coverage content only when first
 * needed. This {@code CoverageStack} implementation remember the last coverage elements used;
 * it will not trig new data loading as long as consecutive calls to {@code evaluate(...)}
 * methods require the same coverage elements. Apart from this very simple caching mechanism,
 * caching is the responsability of {@link Element} implementations.
 * <br><br>
 * Each coverage element is expected to extends over a range of <var>z</var> values (the new
 * dimensions appended by this {@code CoverageStack}). If an {@code evaluate(...)} method is
 * invoked with a <var>z</var> value not falling in the middle of a coverage element, a linear
 * interpolation is applied.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class CoverageStack extends AbstractCoverage {
    /**
     * An element in a {@linkplain CoverageStack coverage stack}. Each element is expected to
     * extends over a range of <var>z</var> values (the new dimensions appended by the
     * {@code CoverageStack} container). Implementations should be capable to returns
     * {@linkplain #getMinimum minimum} and {@link #getMaximum maximum} <var>z</var> values
     * without loading the coverage data. If an expensive loading is required, it should be
     * performed only when {@link #getCoverage} is invoked. If this method is invoked more
     * than once, caching (if desirable) is implementor responsability.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static interface Element {
        /**
         * Returns the minimum <var>z</var> value for the coverage.
         */
        double getMinimum();

        /**
         * Returns the maximum <var>z</var> value for the coverage.
         */
        double getMaximum();

        /**
         * Returns the coverage.
         *
         * @throws IOException if a loading was required and failed.
         */
        Coverage getCoverage() throws IOException;
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
     * The sample dimensions for this coverage.
     */
    private final SampleDimension[] bands;
    
    /**
     * The envelope for this coverage. This is the union of all elements envelopes.
     *
     * @see #getEnvelope
     */
    private final Envelope envelope;
    
    /**
     * <code>true</code> if interpolations are allowed.
     */
    private boolean interpolationAllowed = true;
    
    /**
     * Maximal interval between the {@linkplain Element#getMaximum upper bound} of a coverage and
     * the {@linkplain Element#getMinimum lower bound} of the next one. If a greater difference
     * if found, we will consider that there is a hole in the data and {@code evaluate(...)}
     * methods will returns NaN for <var>z</var> values in this hole.
     */
    private final long lagTolerance = 0;
    
    /**
     * List of objects to inform when image loading are trigged.
     */
    private final EventListenerList listeners = new EventListenerList();
    
    /**
     * Internal listener for logging image loading.
     */
//    private transient Listeners readListener;
    
    /**
     * Donn�es dont la date de d�but est inf�rieure ou �gale � la date demand�e.
     * Autant que possible, on essaiera de faire en sorte que la date du milieu
     * soit inf�rieure ou �gale � la date demand�e (mais ce second aspect n'est
     * pas garantie).
     */
//    private transient GridCoverage2D lower;
    
    /**
     * Donn�es dont la date de fin  est sup�rieure ou �gale � la date demand�e.
     * Autant que possible, on essaiera de faire en sorte que la date du milieu
     * soit sup�rieure ou �gale � la date demand�e (mais ce second aspect n'est
     * pas garantie).
     */
//    private transient GridCoverage2D upper;
    
    /**
     * L'image interpol�e lors du dernier appel de {@link #getGridCoverage2D}. M�moris�e ici
     * afin d'�viter de reconstruire cette image plusieurs fois lors d'appels successifs de
     * {@link #getGridCoverage2D} avec la m�me date.
     */
//    private transient GridCoverage2D interpolated;
    
    /**
     * Date et heure du milieu des donn�es {@link #lower} et {@link #upper},
     * en nombre de millisecondes �coul�es depuis le 1er janvier 1970 UTC.
     */
//    private transient long lowerTime=Long.MAX_VALUE, upperTime=Long.MIN_VALUE;
    
    /**
     * La date et heure (en nombre de millisecondes depuis le 1er janvier 1970 UTC)
     * � laquelle a �t� interpol�e l'image {@link #interpolated}.
     */
//    private transient long timeInterpolated = Long.MIN_VALUE;
    
    /**
     * Plage de temps des donn�es {@link #lower} et {@link #upper}.
     */
//    private transient Range lowerTimeRange, upperTimeRange;
    
    /**
     * L'objet � utiliser pour effectuer des op�rations sur les images
     * (notamment modifier les interpolations). Ne sera construit que
     * la premi�re fois o� il sera n�cessaire.
     */
//    private transient GridCoverageProcessor2D processor;
    
    /**
     * Initialize fields after deserialization.
     */
//    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
//        lowerTime = Long.MAX_VALUE;
//        upperTime = Long.MIN_VALUE;
//        timeInterpolated = Long.MIN_VALUE;
//    }
    
    /**
     * Construit une couverture � partir des donn�es de la table sp�cifi�e.
     * Les entr�es {@link Element} seront m�moris�es immediatement.
     * Toute modification faite � la table apr�s la construction de cet objet
     * <code>GridCoverage3D</code> (incluant la fermeture de la table) n'auront
     * aucun effet sur cet objet.
     *
     * @param  table Table d'o� proviennent les donn�es.
     * @throws RemoteException si l'interrogation du catalogue a �chou�e.
     * @throws TransformException si une transformation de coordonn�es �tait n�cessaire et a �chou�.
     */
//    public CoverageStack(final CoverageTable table) throws RemoteException, TransformException {
//        this(table, table.getCoordinateReferenceSystem());
//    }
    
    /**
     * Construit une couverture � partir des donn�es de la table sp�cifi�e et utilisant
     * le syst�me de r�f�rence des coordonn�es sp�cifi�.
     *
     * @param  table Table d'o� proviennent les donn�es.
     * @param  crs Le syst�me de r�f�rence des coordonn�es � utiliser pour cet obet {@link Coverage}.
     *         Ce syst�me doit obligatoirement comprendre un axe temporel.
     * @throws RemoteException si l'interrogation du catalogue a �chou�e.
     * @throws TransformException si une transformation de coordonn�es �tait n�cessaire et a �chou�.
     */
//    public CoverageStack(final CoverageTable table, final CoordinateReferenceSystem crs) 
//            throws RemoteException, TransformException 
//    {
//        super(table.getSeries().getName(), crs);
//        /*
//         * Obtient la liste des images en ordre chronologiques, et
//         * v�rifie au passage qu'elles ont toutes les m�mes bandes.
//         */
//        final List<Element> entryList = table.getEntries();
//        this.elements = entryList.toArray(new Element[entryList.size()]);
//        this.bands   = (elements.length!=0) ? elements[0].getSampleDimensions() : new SampleDimensionGT[0];
//        for (int i=1; i<elements.length; i++) {
//            if (!Arrays.equals(bands, elements[i].getSampleDimensions())) {
//                throw new CatalogException(Resources.format(ResourceKeys.ERROR_CATEGORIES_MITMATCH));
//            }
//        }
//        try {
//            Arrays.sort(elements, COMPARATOR);
//        } catch (UndeclaredThrowableException exception) {
//            rethrow(exception);
//        }
//        /*
//         * Calcule l'enveloppe englobant celles de toutes les images.
//         * Les coordonn�es seront transform�es si n�cessaires.
//         */
//        Envelope                        envelope    = null;
//        CoordinateOperation             transform   = null;
//        CoordinateOperationFactory      factory     = null;
//        for (int i=0; i<elements.length; i++) {
//            Element               entry       = elements[i];
//            Envelope                    candidate   = entry.getEnvelope();
//            CoordinateReferenceSystem   sourceCRS   = entry.getCoordinateReferenceSystem();
//            if (!CRSUtilities.equalsIgnoreMetadata(crs, sourceCRS)) {
//                if (transform==null || !CRSUtilities.equalsIgnoreMetadata(transform.getSourceCRS(), sourceCRS)) {
//                    if (factory == null) {
//                        factory = FactoryFinder.getCoordinateOperationFactory(null);
//                    }
//                    try {
//                        transform = factory.createOperation(sourceCRS, crs);
//                    } catch (FactoryException exception) {
//                        throw new TransformException(exception.getLocalizedMessage(), exception);
//                    }
//                }
//                candidate = CRSUtilities.transform(transform.getMathTransform(), candidate);
//            }
//            if (envelope == null) {
//                envelope = candidate;
//            } else {
//                final GeneralEnvelope ge = wrap(envelope);
//                ge.add(wrap(candidate));
//                envelope = ge;
//            }
//        }
//        if (envelope == null) {
//            envelope = CRSUtilities.getEnvelope(crs);
//        }
//        this.envelope = envelope;
//    }

    /**
     * If the specified object is already a {@code GeneralEnvelope}, returns it unchanged.
     * Otherwise, converts it into a {@code GeneralEnvelope}. This method is usefull for
     * accessing Geotools-specific methods.
     */
    private static GeneralEnvelope wrap(final Envelope envelope) {
        if (envelope instanceof GeneralEnvelope) {
            return (GeneralEnvelope) envelope;
        }
        return new GeneralEnvelope(envelope);
    }
    
    /**
     * Construit une couverture utilisant les m�mes param�tres que la couverture sp�cifi�e.
     */
    protected CoverageStack(final CharSequence name, final CoverageStack source) {
        super(name, source);
        elements             = source.elements;
        bands                = source.bands;
        envelope             = source.envelope;
        interpolationAllowed = source.interpolationAllowed;
    }
    
    /**
     * Comparateur � utiliser pour classer les images et effectuer des recherches rapides.
     * Ce comparateur utilise la date du milieu comme crit�re. Il doit accepter aussi bien
     * des objets {@link Date} que {@link Element} �tant donn� que les recherches
     * combineront ces deux types d'objets.
     */
//    private static final Comparator<Object> COMPARATOR = new Comparator<Object>() {
//        public int compare(final Object entry1, final Object entry2) {
//            try {
//                final long time1 = getTime(entry1);
//                final long time2 = getTime(entry2);
//                if (time1 < time2) return -1;
//                if (time1 > time2) return +1;
//                return 0;
//            } catch (RemoteException exception) {
//                // Will be catch are rethrow as RemoteException in caller block.
//                throw new UndeclaredThrowableException(exception);
//            }
//        }
//    };
    
    /**
     * Rethrows the exception in {@link #COMPARATOR} as a {@link RemoteException}.
     */
//    private static void rethrow(final UndeclaredThrowableException exception) throws RemoteException {
//        final Throwable cause = exception.getCause();
//        if (cause instanceof RemoteException) {
//            throw (RemoteException) cause;
//        }
//        if (cause instanceof RuntimeException) {
//            throw (RuntimeException) cause;
//        }
//        throw exception;
//    }
    
    /**
     * Retourne la date de l'objet sp�cifi�e. Si l'argument est un objet
     * {@link Date}, alors la date sera extraite avec {@link #getTime}.
     */
//    private static long getTime(final Object object) throws RemoteException {
//        if (object instanceof Date) {
//            return ((Date) object).getTime();
//        }
//        if (object instanceof Element) {
//            return getTime((Element) object);
//        }
//        return Long.MIN_VALUE;
//    }
    
    /**
     * Retourne la date du milieu de l'image sp�cifi�e.  Si l'image ne couvre aucune
     * plage de temps (par exemple s'il s'agit de donn�es qui ne varient pas avec le
     * temps, comme la bathym�trie), alors cette m�thode retourne {@link Long#MIN_VALUE}.
     */
//    private static long getTime(final Element entry) throws RemoteException {
//        return getTime(entry.getTimeRange());
//    }
    
    /**
     * Retourne la date du milieu de la plage sp�cifi�e. Si la plage de temps
     * est nul, alors cette m�thode retourne {@link Long#MIN_VALUE}.
     */
//    private static long getTime(final Range timeRange) {
//        if (timeRange != null) {
//            final Date startTime = (Date) timeRange.getMinValue();
//            final Date   endTime = (Date) timeRange.getMaxValue();
//            if (startTime != null) {
//                if (endTime != null) {
//                    return (endTime.getTime()+startTime.getTime())/2;
//                } else {
//                    return startTime.getTime();
//                }
//            } else if (endTime != null) {
//                return endTime.getTime();
//            }
//        }
//        return Long.MIN_VALUE;
//    }
    
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
        return bands.length;
    }
    
    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension.
     */
    public SampleDimension getSampleDimension(final int index) {
        return bands[index];
    }
    
    /**
     * Check if the given coordinate reference system is compatible with this coverage's
     * {@link #crs}. This method is used for assertions.
     *
     * @param crs The coordinate reference system to test.
     * @return <code>true</code> if the specified crs is compatible.
     *
     * @todo Inspects the axis information provided in GridGeometry2D.
     */
    private boolean isCompatibleCRS(final CoordinateReferenceSystem crs) {
        return CRSUtilities.equalsIgnoreMetadata(this.crs,
               CRSUtilities.getSubCRS(crs, 0, crs.getCoordinateSystem().getDimension()));
    }
    
    /**
     * Snap the specified coordinate point and date to the closest point available in
     * this coverage. First, this method locate the image at or near the specified date
     * (if no image was available at the specified date, the closest one is selected).
     * The <code>date</code> argument is then set to this date. Next, this method locate
     * the pixel under the <code>point</code> coordinate on this image. The <code>point</code>
     * argument is then set to this pixel center. Consequently, calling any <code>evaluate</code>
     * method with snapped coordinates will returns non-interpolated values.
     *
     * @param point The point to snap (may be null).
     * @param date  The date to snap (can not be null, since we need to
     *              know the image's date before to snap the point).
     */
//    public void snap(final Point2D point, final Date date) { // No synchronization needed.
//        try {
//            int index;
//            try {
//                index = Arrays.binarySearch(elements, date, COMPARATOR);
//            } catch (UndeclaredThrowableException exception) {
//                rethrow(exception);
//                return;
//            }
//            if (index < 0) {
//                /*
//                 * There is no exact match for the date.
//                 * Snap the date to the closest image.
//                 */
//                index = ~index;
//                long time;
//                if (index == elements.length) {
//                    if (index == 0) {
//                        return; // No elements in this coverage
//                    }
//                    time = getTime(elements[--index]);
//                } else if (index>=1) {
//                    time = date.getTime();
//                    final long lowerTime = getTime(elements[index-1]);
//                    final long upperTime = getTime(elements[index])-1; // Long.MIN_VALUE-1 == Long.MAX_VALUE
//                    assert (time>lowerTime && time<upperTime);
//                    if (time-lowerTime < upperTime-time) {
//                        index--;
//                        time = lowerTime;
//                    } else {
//                        time = upperTime+1;
//                    }
//                } else {
//                    time = getTime(elements[index]);
//                }
//                if (time!=Long.MIN_VALUE && time!=Long.MAX_VALUE) {
//                    date.setTime(time);
//                }
//            }
//            /*
//             * Now that we know the image entry,
//             * snap the spatial coordinate point.
//             */
//            if (point != null) try {
//                final Element  entry = elements[index];
//                final CoordinateReferenceSystem sourceCRS = entry.getCoordinateReferenceSystem();
//                DirectPosition coordinate = getGeneralDirectPosition(point, date);
//                if (!CRSUtilities.equalsIgnoreMetadata(crs, sourceCRS)) {
//                    // TODO: impl�menter la transformation de coordonn�es.
//                    throw new CannotEvaluateException("Syst�me de coordonn�es incompatibles.");
//                }
//                final GridGeometry2D    geometry    = entry.getGridGeometry2D();
//                final GridRange         range       = geometry.getGridRange();
//                final MathTransform     transform   = geometry.getGridToCoordinateSystem();
//                coordinate = transform.inverse().transform(coordinate, coordinate);
//                for (int i=coordinate.getDimension(); --i>=0;) {
//                    coordinate.setOrdinate(i, 
//                                Math.max(range.getLower(i),
//                                Math.min(range.getUpper(i)-1,
//                                (int)Math.rint(coordinate.getOrdinate(i)))));
//                }
//                coordinate = transform.transform(coordinate, coordinate);
//                point.setLocation(coordinate.getOrdinate(0), coordinate.getOrdinate(1));
//            } catch (TransformException exception) {
//                throw new CannotEvaluateException(cannotEvaluate(point), exception);
//            }
//        } catch (RemoteException exception) {
//            throw new CannotEvaluateException(cannotEvaluate(point), exception);
//        }
//    }
    
    /**
     * Returns a message for exception.
     */
    private static String cannotEvaluate(final Point2D point) {
        return org.geotools.resources.gcs.Resources.format(
                org.geotools.resources.gcs.ResourceKeys.ERROR_CANT_EVALUATE_$1,
                point); // TODO: provides a better formatting here.
    }
    
    /**
     * Returns the grid coverage processor to use for applying operations.
     * The grid coverage processor will be created when first needed.
     */
//    private GridCoverageProcessor2D getGridCoverageProcessor2D() {
//        if (processor == null) {
//            processor = GridCoverageProcessor2D.getDefault();
//        }
//        return processor;
//    }
    
    /**
     * Loads a single image for the specified image entry.
     *
     * @param  entry The image to load.
     * @return The loaded image.
     * @throws IOException if an error occured while loading image.
     */
//    private GridCoverage2D load(final Element entry) throws IOException {
//        GridCoverage2D coverage = entry.getGridCoverage2D(listeners);
//        if (!interpolationAllowed) {
//            final GridCoverageProcessor2D processor = getGridCoverageProcessor2D();
//            coverage = (GridCoverage2D) processor.doOperation("Interpolate", coverage, "Type", "NearestNeighbor");
//        }
//        return coverage;
//    }
    
    /**
     * Loads a single image at the given index.
     *
     * @param  index Index in {@link #elements} for the image to load.
     * @throws IOException if an error occured while loading image.
     */
//    private void load(final int index) throws IOException {
//        final Element entry = elements[index];
//        final Range timeRange = entry.getTimeRange();
//        log(ResourceKeys.LOADING_IMAGE_$1, new Object[]{entry});
//        lower          = upper          = load(entry);
//        lowerTime      = upperTime      = getTime(timeRange);
//        lowerTimeRange = upperTimeRange = timeRange;
//    }
    
    /**
     * Loads images for the given elements.
     *
     * @throws IOException if an error occured while loading images.
     */
//    private void load(final Element lowerEntry, final Element upperEntry)
//            throws IOException
//    {
//        log(ResourceKeys.LOADING_IMAGES_$2, new Object[]{lowerEntry, upperEntry});
//        final Range lowerTimeRange = lowerEntry.getTimeRange();
//        final Range upperTimeRange = upperEntry.getTimeRange();
//        final GridCoverage2D lower = load(lowerEntry);
//        final GridCoverage2D upper = load(upperEntry);
//        
//        this.lower          = lower; // Set only when BOTH images are OK.
//        this.upper          = upper;
//        this.lowerTime      = getTime(lowerTimeRange);
//        this.upperTime      = getTime(upperTimeRange);
//        this.lowerTimeRange = lowerTimeRange;
//        this.upperTimeRange = upperTimeRange;
//    }
    
    /**
     * Proc�de � la lecture des images n�cessaires � l'interpolation des donn�es � la date
     * sp�cifi�e. Les images lues seront point�es par {@link #lower} et {@link #upper}. Il
     * est possible que la m�me image soit affect�e � ces deux champs, si cette m�thode
     * d�termine qu'il n'y a pas d'interpolation � faire.
     *
     * @param  date La date demand�e.
     * @return <code>true</code> si les donn�es sont pr�sentes.
     * @throws PointOutsideCoverageException si la date sp�cifi�e est
     *         en dehors de la plage de temps des donn�es disponibles.
     * @throws CannotEvaluateException Si l'op�ration a �chou�e pour
     *         une autre raison.
     */
//    private boolean seek(final Date date) throws CannotEvaluateException {
//        /*
//         * Check if images currently loaded
//         * are valid for the requested date.
//         */
//        final long time = date.getTime();
//        if (time>=lowerTime && time<=upperTime) {
//            return true;
//        }
//        /*
//         * Currently loaded images are not valid for the
//         * requested date. Search for the image to use
//         * as upper bounds ({@link #upper}).
//         */
//        try {
//            int index;
//            try {
//                index = Arrays.binarySearch(elements, date, COMPARATOR);
//            } catch (UndeclaredThrowableException exception) {
//                rethrow(exception);
//                return false;
//            }
//            if (index >= 0) {
//                /*
//                 * An exact match has been found.
//                 * Load only this image and exit.
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
//                final long  lowerEnd   = getTime(lowerRange.getMaxValue());
//                final long  upperStart = getTime(upperRange.getMinValue())-1; // MIN_VALUE-1 == MAX_VALUE
//                if (lowerEnd+lagTolerance >= upperStart) {
//                    if (interpolationAllowed) {
//                        load(lowerEntry, upperEntry);
//                    } else {
//                        if (Math.abs(getTime(upperRange)-time) > Math.abs(time-getTime(lowerRange))) {
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
//        if (interpolationAllowed) {
//            final GridCoverageProcessor2D processor = getGridCoverageProcessor2D();
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
     * Proj�te un point du syst�me de coordonn�es de cette couverture vers le syst�me
     * de l'image sp�cifi�e. Cette m�thode doit �tre utilis�e avant d'appeller une
     * m�thode <code>evaluate(...)</code> sur la couverture sp�cifi�e.
     *
     * @param  point Le point � transformer. Ce point ne sera jamais modifi�.
     * @return Le point transform�.
     * @throws CannotEvaluateException si la transformation n'a pas pu �tre faites.
     */
//    private Point2D project(final Point2D point, final GridCoverage2D coverage) throws CannotEvaluateException {
//        // TODO: On ne prend que les deux premi�re dimensions parce que, pour une raison non
//        //       �lucid�e, l'op�ration "NodataFilter" retourne un syst�me de coordonn�es 2D.
//        try {
//            final CoordinateReferenceSystem targetCS = CRSUtilities.getCRS2D(coverage.getCoordinateReferenceSystem());
//            if (CRSUtilities.equalsIgnoreMetadata(CRSUtilities.getCRS2D(crs), targetCS)) {
//                return point;
//            }
//            // TODO: Impl�menter la transformation de coordonn�es.
//            throw new CannotEvaluateException("Syst�me de coordonn�es incompatibles.");
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
//        assert isCompatibleCRS(lower.getCoordinateReferenceSystem()) : lower;
//        assert isCompatibleCRS(upper.getCoordinateReferenceSystem()) : upper;
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
//        assert isCompatibleCRS(lower.getCoordinateReferenceSystem()) : lower;
//        assert isCompatibleCRS(upper.getCoordinateReferenceSystem()) : upper;
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
//        assert isCompatibleCRS(lower.getCoordinateReferenceSystem()) : lower;
//        assert isCompatibleCRS(upper.getCoordinateReferenceSystem()) : upper;
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
     * Indique si cet objet est autoris� � interpoller dans l'espace et dans le temps.
     * La valeur par d�faut est <code>true</code>.
     */
//    public boolean isInterpolationAllowed() {
//        return interpolationAllowed;
//    }
    
    /**
     * Sp�cifie si cet objet est autoris� � interpoller dans l'espace et dans le temps.
     * La valeur par d�faut est <code>true</code>.
     */
//    public synchronized void setInterpolationAllowed(final boolean flag) {
//        lower     = null;
//        upper     = null;
//        lowerTime = Long.MAX_VALUE;
//        upperTime = Long.MIN_VALUE;
//        interpolationAllowed = flag;
//    }
    
    /**
     * Adds an {@link IIOReadWarningListener} to
     * the list of registered warning listeners.
     */
    public void addIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.add(IIOReadWarningListener.class, listener);
    }
    
    /**
     * Removes an {@link IIOReadWarningListener} from
     * the list of registered warning listeners.
     */
    public void removeIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.remove(IIOReadWarningListener.class, listener);
    }
    
    /**
     * Adds an {@link IIOReadProgressListener} to
     * the list of registered progress listeners.
     */
    public void addIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.add(IIOReadProgressListener.class, listener);
    }
    
    /**
     * Removes an {@link IIOReadProgressListener} from
     * the list of registered progress listeners.
     */
    public void removeIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.remove(IIOReadProgressListener.class, listener);
    }
    
    /**
     * Enregistre un message vers le journal des �v�nements.
     */
//    protected void log(final LogRecord record) {
//        CoverageDataBase.LOGGER.log(record);
//    }
    
    /**
     * Pr�pare un enregistrement pour le journal.
     */
//    private void log(final int cl�, final Object[] parameters) {
//        final Locale locale = null;
//        final LogRecord record = Resources.getResources(locale).getLogRecord(Level.INFO, cl�);
//        record.setSourceClassName("GridCoverage3D");
//        record.setSourceMethodName("evaluate");
//        record.setParameters(parameters);
//        if (readListener == null) {
//            readListener = new Listeners();
//            addIIOReadProgressListener(readListener);
//        }
//        readListener.record = record;
//    }
    
    /**
     * Objet ayant la charge de suivre le chargement d'une image. Cet objet sert
     * surtout � enregistrer dans le journal un enregistrement indiquant que la
     * lecture d'une image a commenc�.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
//    private final class Listeners extends IIOReadProgressAdapter {
//        /**
//         * The record to log.
//         */
//        public LogRecord record;
//        
//        /**
//         * Reports that an image read operation is beginning.
//         */
//        public void imageStarted(ImageReader source, int imageIndex) {
//            if (record != null) {
//                log(record);
//                source.removeIIOReadProgressListener(this);
//                record = null;
//            }
//        }
//    }
}
