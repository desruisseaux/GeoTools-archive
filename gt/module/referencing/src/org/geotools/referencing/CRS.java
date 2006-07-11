/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Set;
import java.util.NoSuchElementException;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.Factory;                              // For javadoc
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperation;        // For javadoc
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.referencing.crs.DefaultGeographicCRS;            // For javadoc
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;


/**
 * Simple utility class for making use of the {@linkplain CoordinateReferenceSystem
 * coordinate reference system} and associated {@linkplain Factory factory} implementations.
 * <p>
 * This utility class is made up of static final functions. This class is not a factory or a
 * builder. It makes use of the GeoAPI factory interfaces provided by {@link FactoryFinder}.
 * <p>
 * The following methods may be added in a future version:
 * <ul>
 *   <li>{@code CoordinateReferenceSystem parseXML(String)}</li>
 * </ul>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 *
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/Coordinate+Transformation+Services+for+Geotools+2.1
 */
public final class CRS {
    /**
     * A set of hints used in order to fetch lenient coordinate operation factory.
     */
    private static final Hints LENIENT = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

    /**
     * A factory for CRS creation with (<var>latitude</var>, <var>longitude</var>) axis order
     * (unless otherwise specified in system property). Will be created only when first needed.
     */
    private static CRSAuthorityFactory defaultFactory;

    /**
     * A factory for CRS creation with (<var>longitude</var>, <var>latitude</var>) axis order.
     * Will be created only when first needed.
     */
    private static CRSAuthorityFactory xyFactory;

    /**
     * Do not allow instantiation of this class.
     */
    private CRS() {
    }

    /**
     * Grab a transform between two Coordinate Reference Systems. This convenience method is a
     * shorthand for the following:
     *
     * <blockquote><code>FactoryFinder.{@linkplain FactoryFinder#getCoordinateOperationFactory
     * getCoordinateOperationFactory}(null).{@linkplain CoordinateOperationFactory#createOperation
     * createOperation}(sourceCRS, targetCRS).{@linkplain CoordinateOperation#getMathTransform
     * getMathTransform}();</code></blockquote>
     *
     * Note that some metadata like {@linkplain CoordinateOperation#getPositionalAccuracy
     * positional accuracy} are lost by this method. If those metadata are wanted, use the
     * {@linkplain CoordinateOperationFactory coordinate operation factory} directly.
     * <p>
     * Sample use:
     * <blockquote><code>
     * {@linkplain MathTransform} transform = CRS.transform(
     * CRS.{@linkplain #decode decode}("EPSG:42102"),
     * CRS.{@linkplain #decode decode}("EPSG:4326") );
     * </blockquote></code>
     * 
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
     */
    public static MathTransform transform(final CoordinateReferenceSystem sourceCRS,
                                          final CoordinateReferenceSystem targetCRS)
            throws FactoryException
    {
        return transform(sourceCRS, targetCRS, false);
    }

    /**
     * Grab a transform between two Coordinate Reference Systems. This method is similar to
     * <code>{@linkplain #transform(CoordinateReferenceSystem, CoordinateReferenceSystem)
     * transform}(sourceCRS, targetCRS)</code>, except that it can optionally tolerate
     * <cite>lenient datum shift</cite>. If the {@code lenient} argument is {@code true},
     * then this method will not throw a "<cite>Bursa-Wolf parameters required</cite>"
     * exception during datum shifts if the Bursa-Wolf paramaters are not specified.
     * Instead it will assume a no datum shift.
     * 
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @param  lenient {@code true} if the math transform should be created even when there is
     *         no information available for a datum shift. The default value is {@code false}.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
     *
     * @see Hints#LENIENT_DATUM_SHIFT
     */
    public static MathTransform transform(final CoordinateReferenceSystem sourceCRS,
                                          final CoordinateReferenceSystem targetCRS,
                                          boolean lenient)
            throws FactoryException
    {
        final CoordinateOperationFactory factory =
                FactoryFinder.getCoordinateOperationFactory(lenient ? LENIENT : null);
        return factory.createOperation(sourceCRS, targetCRS).getMathTransform();
    }

    /**
     * Get the list of the codes that are supported by the given authority. For example
     * {@code getSupportedCodes("EPSG")} may returns {@code "EPSG:2000"}, {@code "EPSG:2001"},
     * {@code "EPSG:2002"}, <cite>etc</cite>. It may also returns {@code "2000"}, {@code "2001"},
     * {@code "2002"}, <cite>etc.</cite> without the {@code "EPSG:"} prefix. Whatever the authority
     * name is prefixed or not is factory implementation dependent.
     * <p>
     * If there is more than one factory for the given authority, then this method merges the
     * code set of all of them. If a factory fails to provide a set of supported code, then
     * this particular factory is ignored. Please be aware of the following potential issues:
     * <p>
     * <ul>
     *   <li>If there is more than one EPSG databases (for example an 
     *       {@linkplain org.geotools.referencing.factory.epsg.AccessDataSource Access} and a
     *       {@linkplain org.geotools.referencing.factory.epsg.PostgreDataSource PostgreSQL} ones),
     *       then this method will connect to all of them even if their content are identical.</li>
     *
     *   <li>If two factories format their codes differently (e.g. {@code "4326"} and
     *       {@code "EPSG:4326"}), then the returned set will contain a lot of synonymous
     *       codes.</li>
     *
     *   <li>For any code <var>c</var> in the returned set, there is no warranty that
     *       <code>{@linkplain #decode decode}(c)</code> will use the same authority
     *       factory than the one that formatted <var>c</var>.</li>
     *   
     *   <li>This method doesn't report connection problems since it doesn't throw any exception.
     *       {@link FactoryException}s are logged as warnings and otherwise ignored.</li>
     * </ul>
     * <p>
     * If a more determinist behavior is wanted, consider the code below instead.
     * The following code exploit only one factory, the "preferred" one.
     *
     * <blockquote><code>
     * {@linkplain CRSAuthorityFactory} factory = FactoryFinder.{@linkplain
     * FactoryFinder#getCRSAuthorityFactory getCRSAuthorityFactory}(authority, null);<br>
     * Set&lt;String&gt; codes = factory.{@linkplain CRSAuthorityFactory#getAuthorityCodes
     * getAuthorityCodes}(CoordinateReferenceSystem.class);<br>
     * String code = <cite>...choose a code here...</cite><br>
     * {@linkplain CoordinateReferenceSystem} crs = factory.createCoordinateReferenceSystem(code);
     * </code></blockquote>
     *
     * @param  authority The authority name (for example {@code "EPSG"}).
     * @return The set of supported codes. May be empty, but never null.
     */
    public static Set/*<String>*/ getSupportedCodes(final String authority) {
        return DefaultAuthorityFactory.getSupportedCodes(authority);
    }

    /**
     * Return a Coordinate Reference System for the specified code.
     * Note that the code needs to mention the authority. Examples:
     *
     * <blockquote><pre>
     * EPSG:1234
     * AUTO:42001, ..., ..., ...
     * </pre></blockquote>
     *
     * If there is more than one factory implementation for the same authority, then all additional
     * factories are {@linkplain org.geotools.referencing.factory.FallbackAuthorityFactory fallbacks}
     * to be used only when the first acceptable factory failed to create the requested CRS object.
     * <p>
     * CRS objects created by previous calls to this method are
     * {@linkplain org.geotools.referencing.factory.BufferedAuthorityFactory cached in a buffer}
     * using {@linkplain java.lang.ref.WeakReference weak references}. Subsequent calls to this
     * method with the same authority code should be fast, unless the CRS object has been garbage
     * collected.
     *
     * @param  code The Coordinate Reference System authority code.
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     *
     * @see #getSupportedCodes
     * @see org.geotools.referencing.factory.AllAuthoritiesFactory#createCoordinateReferenceSystem
     */ 
    public static CoordinateReferenceSystem decode(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        return decode(code, false);
    }

    /**
     * Return a Coordinate Reference System for the specified code, maybe forcing the
     * axis order to (<var>longitude</var>,<var>latitude</var>). This method is similar
     * to <code>{@linkplain #decode(String) decode}(code)</code>, except that the
     * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER}
     * hint is set to {@link Boolean#TRUE TRUE} if and only if the {@code longitudeFirst}
     * argument value is {@code true}.
     * <p>
     * If the {@code longitudeFirst} argument value is {@code false} (which is the default value),
     * then the {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER}
     * hint is left unset. This is <strong>not</strong> equivalent to setting the above-cited hint
     * to {@link Boolean#FALSE FALSE}. The following table explain the different meanings:
     * <p>
     * <table border='1'>
     * <tr>
     *   <th>This method argument</th>
     *   <th>{@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER
     *       Corresponding hint}</th>
     *   <th>Meaning</th>
     * </tr>
     * <tr>
     *   <td>{@code true}</td>
     *   <td>{@link Boolean#TRUE TRUE}</td>
     *   <td>All coordinate reference systems are forced to
     *       (<var>longitude</var>,<var>latitude</var>) axis order.</td>
     * </tr>
     * <tr>
     *   <td>{@code false}</td>
     *   <td>{@code null}</td>
     *   <td>Coordinate reference systems may or may not be forced to
     *       (<var>longitude</var>,<var>latitude</var>) axis order. The behavior depends on user
     *       setting, for example the value of the <code>{@value
     *       org.geotools.referencing.factory.epsg.LongitudeFirstFactory#SYSTEM_DEFAULT_KEY}</code>
     *       system property.</td>
     * </tr>
     * <tr>
     *   <td></td>
     *   <td>{@link Boolean#FALSE FALSE}</td>
     *   <td>Forcing (<var>longitude</var>,<var>latitude</var>) axis order is not allowed,
     *       no matter the value of the <code>{@value
     *       org.geotools.referencing.factory.epsg.LongitudeFirstFactory#SYSTEM_DEFAULT_KEY}</code>
     *       system property.</td>
     * </tr>
     * </table>
     *
     * @param  code The Coordinate Reference System authority code.
     * @param  longitudeFirst {@code true} if axis order should be forced to
     *         (<var>longitude</var>,<var>latitude</var>).
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     *
     * @see #getSupportedCodes
     * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     *
     * @since 2.3
     */
    public static CoordinateReferenceSystem decode(String code, final boolean longitudeFirst)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        // Note: Use upper case mostly for consistency with a previous version of this method.
        code = code.trim().toUpperCase();
        return getAuthorityFactory(longitudeFirst).createCoordinateReferenceSystem(code);
    }

    /**
     * Parses a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite></A> (WKT) into a CRS object. This convenience method is a
     * shorthand for the following:
     *
     * <blockquote><code>
     * FactoryFinder.{@linkplain FactoryFinder#getCRSFactory getCRSFactory}(null).{@linkplain
     * org.opengis.referencing.crs.CRSFactory#createFromWKT createFromWKT}(wkt);
     * </code></blockquote>
     */
    public static CoordinateReferenceSystem parseWKT(final String wkt) throws FactoryException {
    	return FactoryFinder.getCRSFactory(null).createFromWKT(wkt);
    }

    /**
     * Returns the CRS authority factory used by the {@link #decode(String,boolean) decode} methods.
     * This factory is
     * {@linkplain org.geotools.referencing.factory.BufferedAuthorityFactory buffered}, scans over
     * {@linkplain org.geotools.referencing.factory.AllAuthoritiesFactory all factories} and uses
     * additional factories as {@linkplain org.geotools.referencing.factory.FallbackAuthorityFactory
     * fallbacks} if there is more than one {@linkplain FactoryFinder#getCRSAuthorityFactories
     * registered factory} for the same authority.
     * <p>
     * This factory can be used as a kind of <cite>system-wide</cite> factory for all authorities.
     * However for more determinist behavior, consider using a more specific factory (as returned
     * by {@link FactoryFinder#getCRSAuthorityFactory} when the authority in known.
     *
     * @param  longitudeFirst {@code true} if axis order should be forced to
     *         (<var>longitude</var>,<var>latitude</var>).
     * @return The CRS authority factory.
     * @throws FactoryRegistryException if the factory can't be created.
     *
     * @since 2.3
     */
    public static synchronized CRSAuthorityFactory getAuthorityFactory(final boolean longitudeFirst)
            throws FactoryRegistryException
    {
        if (FactoryFinder.updated) {
            FactoryFinder.updated = false;
            defaultFactory = xyFactory = null;
        }
        CRSAuthorityFactory factory = (longitudeFirst) ? xyFactory : defaultFactory;
        if (factory == null) try {
            factory = new DefaultAuthorityFactory(longitudeFirst);
            if (longitudeFirst) {
                xyFactory = factory;
            } else {
                defaultFactory = factory;
            }
        } catch (NoSuchElementException exception) {
            // No factory registered in FactoryFinder.
            throw new FactoryNotFoundException(null, exception);
        }
        return factory;
    }

    /**
     * Returns the valid area bounding box for the specified coordinate reference system, or
     * {@code null} if unknown. This method search in the metadata informations associated with
     * the given CRS. The returned envelope is expressed in terms of the specified CRS.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The envelope in terms of the specified CRS, or {@code null} if none.
     *
     * @since 2.2
     */
    public static Envelope getEnvelope(CoordinateReferenceSystem crs) {
        Envelope envelope = getGeographicEnvelope(crs);
        if (envelope != null) {
            // TODO: Use a more direct method if we provide such method in a future GeoAPI version.
            final CoordinateReferenceSystem sourceCRS;
            sourceCRS = envelope.getUpperCorner().getCoordinateReferenceSystem();
            if (sourceCRS != null) try {
                crs = CRSUtilities.getCRS2D(crs);
                if (!equalsIgnoreMetadata(sourceCRS, crs)) {
                    final GeneralEnvelope e;
                    e = CRSUtilities.transform(transform(sourceCRS, crs, true), envelope);
                    e.setCoordinateReferenceSystem(crs);
                    envelope = e;
                }
            } catch (FactoryException exception) {
                /*
                 * No transformation path was found for the specified CRS. Logs a warning and
                 * returns null, since it is a legal return value according this method contract.
                 */
                envelope = null;
                Utilities.unexpectedException("org.geotools.referencing", "CRS",
                                              "getEnvelope", exception);
            } catch (TransformException exception) {
                /*
                 * The envelope is probably outside the range of validity for this CRS.
                 * It should not occurs, since the envelope is supposed to describe the
                 * CRS area of validity. Logs a warning and returns null, since it is a
                 * legal return value according this method contract.
                 */
                envelope = null;
                Utilities.unexpectedException("org.geotools.referencing", "CRS",
                                              "getEnvelope", exception);
            }        
        }
        return envelope;
    }

    /**
     * Returns the valid area bounding box for the specified coordinate reference system, or
     * {@code null} if unknown. This method search in the metadata informations associated with
     * the given CRS. The returned envelope is always expressed in terms of the
     * {@linkplain DefaultGeographicCRS#WGS_84 WGS 84} CRS.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The envelope, or {@code null} if none.
     */
    private static Envelope getGeographicEnvelope(final CoordinateReferenceSystem crs) {
        // TODO: Copy the implementation from CRSUtilities there.
        return CRSUtilities.getEnvelope(crs);
    }

    /**
     * Returns the valid geographic area for the specified coordinate reference system, or
     * {@code null} if unknown. This method search in the metadata informations associated
     * with the given CRS.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The geographic area, or {@code null} if none.
     *
     * @since 2.3
     */
    public static GeographicBoundingBox getGeographicBoundingBox(final CoordinateReferenceSystem crs) {
        final Envelope envelope = getGeographicEnvelope(crs);
        if (envelope != null) try {
            return new GeographicBoundingBoxImpl(envelope);
        } catch (TransformException exception) {
            /*
             * Should not occurs, since envelopes are usually already in geographic coordinates.
             * If it occurs anyway, returns null since it is allowed by this method contract.
             */
            Utilities.unexpectedException("org.geotools.referencing", "CRS",
                                          "getGeographicBoundingBox", exception);
        }
        return null;
    }

    /**
     * Compares the specified objects for equality. If both objects are Geotools
     * implementations of class {@link AbstractIdentifiedObject}, then this method
     * will ignore the metadata during the comparaison.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return {@code true} if both objects are equals.
     *
     * @since 2.2
     */
    public static boolean equalsIgnoreMetadata(final Object object1, final Object object2) {
        // TODO: Copy the implementation from CRSUtilities there.
        return CRSUtilities.equalsIgnoreMetadata(object1, object2);
    }
}
