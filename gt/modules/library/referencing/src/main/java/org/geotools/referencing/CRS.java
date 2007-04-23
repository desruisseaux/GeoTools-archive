/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.BoundingPolygon;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.util.Version;
import org.geotools.util.Logging;
import org.geotools.util.UnsupportedImplementationException;


/**
 * Simple utility class for making use of the {@linkplain CoordinateReferenceSystem
 * coordinate reference system} and associated {@linkplain org.opengis.referencing.Factory}
 * implementations. This utility class is made up of static final functions. This class is
 * not a factory or a builder. It makes use of the GeoAPI factory interfaces provided by
 * {@link ReferencingFactoryFinder}.
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
 * @author Andrea Aime
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


    //////////////////////////////////////////////////////////////
    ////                                                      ////
    ////        FACTORIES, CRS CREATION AND INSPECTION        ////
    ////                                                      ////
    //////////////////////////////////////////////////////////////

    /**
     * Returns the CRS authority factory used by the {@link #decode(String,boolean) decode} methods.
     * This factory is {@linkplain org.geotools.referencing.factory.BufferedAuthorityFactory buffered},
     * scans over {@linkplain org.geotools.referencing.factory.AllAuthoritiesFactory all factories} and
     * uses additional factories as {@linkplain org.geotools.referencing.factory.FallbackAuthorityFactory
     * fallbacks} if there is more than one {@linkplain ReferencingFactoryFinder#getCRSAuthorityFactories
     * registered factory} for the same authority.
     * <p>
     * This factory can be used as a kind of <cite>system-wide</cite> factory for all authorities.
     * However for more determinist behavior, consider using a more specific factory (as returned
     * by {@link ReferencingFactoryFinder#getCRSAuthorityFactory} when the authority in known.
     *
     * @param  longitudeFirst {@code true} if axis order should be forced to
     *         (<var>longitude</var>,<var>latitude</var>). Note that {@code false} means
     *         "<cite>use default</cite>", <strong>not</strong> "<cite>latitude first</cite>".
     * @return The CRS authority factory.
     * @throws FactoryRegistryException if the factory can't be created.
     *
     * @since 2.3
     */
    public static synchronized CRSAuthorityFactory getAuthorityFactory(final boolean longitudeFirst)
            throws FactoryRegistryException
    {
        if (ReferencingFactoryFinder.updated) {
            ReferencingFactoryFinder.updated = false;
            defaultFactory = xyFactory = null;
        }
        CRSAuthorityFactory factory = (longitudeFirst) ? xyFactory : defaultFactory;
        if (factory == null) try {
            final Hints hints = GeoTools.getDefaultHints();
            if (longitudeFirst) {
                hints.put(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
            } else {
                /*
                 * Do NOT set the hint to false. If 'longitudeFirst' is not set, this means
                 * "use the system default", not "latitude first". The longitude may or may
                 * not be first depending the value of "org.geotools.referencing.forcexy"
                 * system property. This state is included in GeoTools.getDefaultHints().
                 * If we don't behave that way, the 'decode(String)' method will fails.
                 */
            }
            factory = new DefaultAuthorityFactory(hints);
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
     * Returns the version number of the specified authority database, or {@code null} if
     * not available.
     *
     * @param  authority The authority name (typically {@code "EPSG"}).
     * @return The version number of the authority database, or {@code null} if unknown.
     * @throws FactoryRegistryException if no {@link CRSAuthorityFactory} implementation
     *         was found for the specified authority.
     *
     * @since 2.4
     */
    public static Version getVersion(final String authority) throws FactoryRegistryException {
        Object factory = ReferencingFactoryFinder.getCRSAuthorityFactory(authority, null);
        final Set guard = new HashSet(); // Safety against never-ending recursivity.
        while (factory instanceof Factory && guard.add(factory)) {
            final Map hints = ((Factory) factory).getImplementationHints();
            final Object version = hints.get(Hints.VERSION);
            if (version instanceof Version) {
                return (Version) version;
            }
            factory = hints.get(Hints.CRS_AUTHORITY_FACTORY);
        }
        return null;
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
     * ReferencingFactoryFinder#getCRSAuthorityFactory getCRSAuthorityFactory}(authority, null);<br>
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
     * Returns the set of the authority identifiers supported by registered authority factories.
     * This method search only for {@linkplain CRSAuthorityFactory CRS authority factories}.
     *
     * @param  returnAliases If {@code true}, the set will contain all identifiers for each
     *         authority. If {@code false}, only the first one
     * @return The set of supported authorities. May be empty, but never null.
     *
     * @since 2.3.1
     */
    public static Set/*<String>*/ getSupportedAuthorities(final boolean returnAliases) {
        return DefaultAuthorityFactory.getSupportedAuthorities(returnAliases);
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
        /*
         * Do not use Boolean.getBoolean(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER).
         * The boolean argument should be 'false', which means "use system default"
         * (not "latitude first").
         */
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
     *   <th>{@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER Hint} value</th>
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
     *         (<var>longitude</var>,<var>latitude</var>). Note that {@code false} means
     *         "<cite>use default</cite>", <strong>not</strong> "<cite>latitude first</cite>".
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
     * FactoryFinder.{@linkplain ReferencingFactoryFinder#getCRSFactory getCRSFactory}(null).{@linkplain
     * org.opengis.referencing.crs.CRSFactory#createFromWKT createFromWKT}(wkt);
     * </code></blockquote>
     */
    public static CoordinateReferenceSystem parseWKT(final String wkt) throws FactoryException {
    	return ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(wkt);
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
            final CoordinateReferenceSystem sourceCRS = envelope.getCoordinateReferenceSystem();
            if (sourceCRS != null) try {
                crs = CRSUtilities.getCRS2D(crs);
                if (!equalsIgnoreMetadata(sourceCRS, crs)) {
                    final GeneralEnvelope e;
                    e = transform(findMathTransform(sourceCRS, crs, true), envelope);
                    e.setCoordinateReferenceSystem(crs);
                    envelope = e;
                }
            } catch (FactoryException exception) {
                /*
                 * No transformation path was found for the specified CRS. Logs a warning and
                 * returns null, since it is a legal return value according this method contract.
                 */
                envelope = null;
                Logging.unexpectedException("org.geotools.referencing", CRS.class,
                                            "getEnvelope", exception);
            } catch (TransformException exception) {
                /*
                 * The envelope is probably outside the range of validity for this CRS.
                 * It should not occurs, since the envelope is supposed to describe the
                 * CRS area of validity. Logs a warning and returns null, since it is a
                 * legal return value according this method contract.
                 */
                envelope = null;
                Logging.unexpectedException("org.geotools.referencing", CRS.class,
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
        GeneralEnvelope envelope = null;
        if (crs != null) {
            final Extent validArea = crs.getValidArea();
            if (validArea != null) {
                for (final Iterator it=validArea.getGeographicElements().iterator(); it.hasNext();) {
                    final GeographicExtent geo = (GeographicExtent) it.next();
                    final GeneralEnvelope candidate;
                    if (geo instanceof GeographicBoundingBox) {
                        final GeographicBoundingBox bounds = (GeographicBoundingBox) geo;
                        final Boolean inclusion = bounds.getInclusion();
                        if (inclusion == null) {
                            // Status unknow; ignore this bounding box.
                            continue;
                        }
                        if (!inclusion.booleanValue()) {
                            // TODO: we could uses Envelope.substract if such
                            //       a method is defined in a future version.
                            continue;
                        }
                        candidate = new GeneralEnvelope(new double[] {bounds.getWestBoundLongitude(),
                                                                      bounds.getSouthBoundLatitude()},
                                                        new double[] {bounds.getEastBoundLongitude(),
                                                                      bounds.getNorthBoundLatitude()});
                        candidate.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
                    } else if (geo instanceof BoundingPolygon) {
                        // TODO: iterates through all polygons and invoke Polygon.getEnvelope();
                        continue;
                    } else {
                        continue;
                    }
                    if (envelope == null) {
                        envelope = candidate;
                    } else {
                        envelope.add(candidate);
                    }
                }
            }
        }
        return envelope;
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
            Logging.unexpectedException("org.geotools.referencing", CRS.class,
                                        "getGeographicBoundingBox", exception);
        }
        return null;
    }

    /**
     * Returns the first horizontal coordinate reference system found in the given CRS,
     * or {@code null} if there is none. A horizontal CRS is usually a two-dimensional
     * {@linkplain GeographicCRS geographic} or {@linkplain ProjectedCRS projected} CRS.
     *
     * @since 2.4
     */
    public static SingleCRS getHorizontalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof SingleCRS && crs.getCoordinateSystem().getDimension()==2) {
            CoordinateReferenceSystem base = crs;
            while (base instanceof GeneralDerivedCRS) {
                base = ((GeneralDerivedCRS) base).getBaseCRS();
            }
            // No need to test for ProjectedCRS, since the code above unwrap it.
            if (base instanceof GeographicCRS) {
                return (SingleCRS) crs; // Really returns 'crs', not 'base'.
            }
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ c=
                    ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (final Iterator it=c.iterator(); it.hasNext();) {
                final SingleCRS candidate = getHorizontalCRS((CoordinateReferenceSystem) it.next());
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first projected coordinate reference system found in a the given CRS,
     * or {@code null} if there is none.
     *
     * @since 2.4
     */
    public static ProjectedCRS getProjectedCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof ProjectedCRS) {
            return (ProjectedCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ c =
                    ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (final Iterator it=c.iterator(); it.hasNext();) {
                final ProjectedCRS candidate = getProjectedCRS((CoordinateReferenceSystem) it.next());
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first vertical coordinate reference system found in a the given CRS,
     * or {@code null} if there is none.
     *
     * @since 2.4
     */
    public static VerticalCRS getVerticalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof VerticalCRS) {
            return (VerticalCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ c =
                    ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (final Iterator it=c.iterator(); it.hasNext();) {
                final VerticalCRS candidate = getVerticalCRS((CoordinateReferenceSystem) it.next());
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first temporal coordinate reference system found in the given CRS,
     * or {@code null} if there is none.
     *
     * @since 2.4
     */
    public static TemporalCRS getTemporalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof TemporalCRS) {
            return (TemporalCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ c =
                    ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (final Iterator it=c.iterator(); it.hasNext();) {
                final TemporalCRS candidate = getTemporalCRS((CoordinateReferenceSystem) it.next());
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first ellipsoid found in a coordinate reference system,
     * or {@code null} if there is none.
     *
     * @since 2.4
     */
    public static Ellipsoid getEllipsoid(final CoordinateReferenceSystem crs) {
        final Datum datum = CRSUtilities.getDatum(crs);
        if (datum instanceof GeodeticDatum) {
            return ((GeodeticDatum) datum).getEllipsoid();
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ c =
                    ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (final Iterator it=c.iterator(); it.hasNext();) {
                final Ellipsoid candidate = getEllipsoid((CoordinateReferenceSystem) it.next());
                if (candidate != null) {
                    return candidate;
                }
            }
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
        if (object1 == object2) {
            return true;
        }
        if (object1 instanceof AbstractIdentifiedObject &&
            object2 instanceof AbstractIdentifiedObject)
        {
            return ((AbstractIdentifiedObject) object1).equals(
                   ((AbstractIdentifiedObject) object2), false);
        }
        return object1!=null && object1.equals(object2);
    }

    
    /**
     * Looks up an identifier for the specified coordinate reference system.
     * 
     * 
     * @param crs the coordinate reference system looked up.
     * @param authorities the authority that we should look up the identifier into. 
     *         If {@code null} the search will to be performed against all authorities.
     * @param fullScan if {@code true}, an exhaustive full scan against all registered CRS
     *         will be performed (may be slow). Otherwise only a fast lookup based on embedded
     *         identifiers and names will be performed.
     * @return The identifier, or {@code null} if not found.
     *
     * @since 2.3.1
     */
    public static String lookupIdentifier(final CoordinateReferenceSystem crs,
                                          Set/*<String>*/ authorities,
                                          final boolean fullScan)
    {
        // gather the authorities we're considering
        if (authorities == null) {
            authorities = getSupportedAuthorities(false);
        }
        // first check if one of the identifiers can be used to spot directly
        // a CRS (and check it's actually equal to one in the db)
        for (Iterator it = crs.getIdentifiers().iterator(); it.hasNext();) {
            final Identifier id = (Identifier) it.next();
            final CoordinateReferenceSystem candidate;
            try {
                candidate = CRS.decode(id.toString());
            } catch (FactoryException e) {
                // the identifier was not recognized, no problem, let's go on
                continue;
            }
            if (equalsIgnoreMetadata(candidate, crs)) {
                String identifier = getSRSFromCRS(candidate, authorities);
                if (identifier != null) {
                    return identifier;
                }
            }
        }
        
        // try a quick name lookup
        try {
            CoordinateReferenceSystem candidate = CRS.decode(crs.getName().toString());
            if (equalsIgnoreMetadata(candidate, crs)) {
                String identifier = getSRSFromCRS(candidate, authorities);
                if (identifier != null) {
                    return identifier;
                }
            }
        } catch (Exception e) {
            // the name was not recognized, no problem, let's go on
        }
        
        // here we exhausted the quick paths, bail out if the user does not want a full scan
        if (!fullScan) {
            return null;
        }
        // a direct lookup did not work, let's try a full scan of known CRS then
        // TODO: implement a smarter method in the actual EPSG authorities, which may
        // well be this same loop if they do have no other search capabilities
        for (Iterator itAuth = authorities.iterator(); itAuth.hasNext();) {
            String authority = (String) itAuth.next();
            Set codes = CRS.getSupportedCodes(authority);
            for (Iterator itCodes = codes.iterator(); itCodes.hasNext();) {
                String code = (String) itCodes.next();
                try {
                    final CoordinateReferenceSystem candidate;
                    if (code.indexOf(':') == -1) {
                        candidate = CRS.decode(authority + ':' + code);
                    } else {
                        candidate = CRS.decode(code);
                    }
                    if (CRS.equalsIgnoreMetadata(candidate, crs)) {
                        return getSRSFromCRS(candidate, Collections.singleton(authority));
                    }
                } catch (Exception e) {
                    // some CRS cannot be decoded properly
                }
            }
        }
        return null;
    }

    /**
     * Scans the identifiers list looking for an EPSG id
     * @param crs
     * @return
     */
    private static String getSRSFromCRS(final CoordinateReferenceSystem crs, final Set authorities) {
        for (Iterator itAuth = authorities.iterator(); itAuth.hasNext();) {
            final String authority = (String) itAuth.next();
            final String prefix = authority + ":";
            for (Iterator itIdent = crs.getIdentifiers().iterator(); itIdent.hasNext();) {
                NamedIdentifier id = (NamedIdentifier) itIdent.next();
                String idName = id.toString();
                if(idName.startsWith(prefix))
                    return idName;
            }
        }
        return null;
    } 


    /////////////////////////////////////////////////
    ////                                         ////
    ////          COORDINATE OPERATIONS          ////
    ////                                         ////
    /////////////////////////////////////////////////

    /**
     * Grab a transform between two Coordinate Reference Systems. This convenience method is a
     * shorthand for the following:
     *
     * <blockquote><code>FactoryFinder.{@linkplain ReferencingFactoryFinder#getCoordinateOperationFactory
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
     * {@linkplain MathTransform} transform = CRS.findMathTransform(
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
    public static MathTransform findMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                  final CoordinateReferenceSystem targetCRS)
            throws FactoryException
    {
    	return findMathTransform(sourceCRS, targetCRS, false);
    }

    /**
     * Grab a transform between two Coordinate Reference Systems. This method is similar to
     * <code>{@linkplain #findMathTransform(CoordinateReferenceSystem, CoordinateReferenceSystem)
     * findMathTransform}(sourceCRS, targetCRS)</code>, except that it can optionally tolerate
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
    public static MathTransform findMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                  final CoordinateReferenceSystem targetCRS,
                                                  boolean lenient)
            throws FactoryException
    {
        final CoordinateOperationFactory factory =
                ReferencingFactoryFinder.getCoordinateOperationFactory(lenient ? LENIENT : null);
        return factory.createOperation(sourceCRS, targetCRS).getMathTransform();
    }

    /**
     * Transforms an envelope. The transformation is only approximative. Note that the returned
     * envelope may not have the same number of dimensions than the original envelope.
     *
     * @param  transform The transform to use.
     * @param  envelope Envelope to transform, or {@code null}. This envelope will not be modified.
     * @return The transformed envelope, or {@code null} if {@code envelope} was null.
     * @throws TransformException if a transform failed.
     *
     * @since 2.4
     */
    public static GeneralEnvelope transform(final MathTransform transform, final Envelope envelope)
            throws TransformException
    {
        if (envelope == null) {
            return null;
        }
        if (transform.isIdentity()) {
            /*
             * Slight optimisation: Just copy the envelope. Note that we need to set the CRS
             * to null because we don't know what the target CRS was supposed to be. Even if
             * an identity transform often imply that the target CRS is the same one than the
             * source CRS, it is not always the case. The metadata may be differents, or the
             * transform may be a datum shift without Bursa-Wolf parameters, etc.
             */
            final GeneralEnvelope e = new GeneralEnvelope(envelope);
            e.setCoordinateReferenceSystem(null);
            return e;
        }
        final int sourceDim = transform.getSourceDimensions();
        final int targetDim = transform.getTargetDimensions();
        if (envelope.getDimension() != sourceDim) {
            throw new MismatchedDimensionException(Errors.format(
                      ErrorKeys.MISMATCHED_DIMENSION_$2,
                      new Integer(sourceDim), new Integer(envelope.getDimension())));
        }
        int          coordinateNumber = 0;
        GeneralEnvelope   transformed = null;
        final GeneralDirectPosition sourcePt = new GeneralDirectPosition(sourceDim);
        final GeneralDirectPosition targetPt = new GeneralDirectPosition(targetDim);
        for (int i=sourceDim; --i>=0;) {
            sourcePt.setOrdinate(i, envelope.getMinimum(i));
        }
  loop: while (true) {
            // Transform a point and add the transformed point to the destination envelope.
            if (targetPt != transform.transform(sourcePt, targetPt)) {
                throw new UnsupportedImplementationException(transform.getClass());
            }
            if (transformed != null) {
                transformed.add(targetPt);
            } else {
                transformed = new GeneralEnvelope(targetPt, targetPt);
            }
            // Get the next point's coordinate.   The 'coordinateNumber' variable should
            // be seen as a number in base 3 where the number of digits is equals to the
            // number of dimensions. For example, a 4-D space would have numbers ranging
            // from "0000" to "2222". The digits are then translated into minimal, central
            // or maximal ordinates.
            int n = ++coordinateNumber;
            for (int i=sourceDim; --i>=0;) {
                switch (n % 3) {
                    case 0:  sourcePt.setOrdinate(i, envelope.getMinimum(i)); n/=3; break;
                    case 1:  sourcePt.setOrdinate(i, envelope.getCenter (i)); continue loop;
                    case 2:  sourcePt.setOrdinate(i, envelope.getMaximum(i)); continue loop;
                    default: throw new AssertionError(n); // Should never happen
                }
            }
            break;
        }
        return transformed;
    }
    
    /**
     * Transforms a rectangular envelope. The transformation is only approximative.
     * Invoking this method is equivalent to invoking the following:
     * <p>
     * <pre>transform(transform, new GeneralEnvelope(source)).toRectangle2D()</pre>
     *
     * @param  transform The transform to use. Source and target dimension must be 2.
     * @param  source The rectangle to transform (may be {@code null}).
     * @param  dest The destination rectangle (may be {@code source}).
     *         If {@code null}, a new rectangle will be created and returned.
     * @return {@code dest}, or a new rectangle if {@code dest} was non-null
     *         and {@code source} was null.
     * @throws TransformException if a transform failed.
     *
     * @todo Move this method as a static method in {@link org.geotools.referencing.CRS}.
     */
    public static Rectangle2D transform(final MathTransform2D transform,
                                        final Rectangle2D     source,
                                        final Rectangle2D     dest)
            throws TransformException
    {
        if (source == null) {
            return null;
        }
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        final Point2D.Double point = new Point2D.Double();
        for (int i=0; i<8; i++) {
            /*
             *   (0)----(5)----(1)
             *    |             |
             *   (4)           (7)
             *    |             |
             *   (2)----(6)----(3)
             */
            point.x = (i&1)==0 ? source.getMinX() : source.getMaxX();
            point.y = (i&2)==0 ? source.getMinY() : source.getMaxY();
            switch (i) {
                case 5: // fallthrough
                case 6: point.x=source.getCenterX(); break;
                case 7: // fallthrough
                case 4: point.y=source.getCenterY(); break;
            }
            transform.transform(point, point);
            if (point.x < xmin) xmin = point.x;
            if (point.x > xmax) xmax = point.x;
            if (point.y < ymin) ymin = point.y;
            if (point.y > ymax) ymax = point.y;
        }
        if (dest != null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return XRectangle2D.createFromExtremums(xmin, ymin, xmax, ymax);
    }

    /**
     * Prints to the {@linkplain System#out standard output stream} some information about
     * {@linkplain CoordinateReferenceSystem coordinate reference systems} specified by their
     * authority codes. This method can be invoked from the command line in order to test the
     * {@linkplain #getAuthorityFactory authority factory}�content for some specific CRS.
     * <p>
     * By default, this method prints all enumerated objects as <cite>Well Known Text</cite>.
     * However this method can prints different kind of information if an option such as
     * {@code -factories}, {@code -codes} or {@code -bursawolfs} is provided.
     * <p>
     * <b>Usage:</b> {@code java org.geotools.referencing.CRS [options] [codes]}<br>
     * <b>Options:</b>
     *
     * <blockquote>
     *   <p><b>{@code -authority}=<var>name</var></b><br>
     *       Uses the specified authority factory, for example {@code "EPSG"}. The authority
     *       name can be any of the authorities listed by the {@code -factories} option. If
     *       this option is not specified, then the default is all factories.</p>
     *
     *   <p><b>{@code -bursawolfs} <var>codes</var></b><br>
     *       Lists the Bursa-Wolf parameters for the specified CRS ou datum objects. For some
     *       transformations, there is more than one set of Bursa-Wolf parameters available.
     *       The standard <cite>Well Known Text</cite> format prints only what look like the
     *       "main" one. This option display all Bursa-Wolf parameters in a table for a given
     *       object.</p>
     *
     *   <p><b>{@code -codes}</b><br>
     *       Lists all available authority codes. Use the {@code -authority} option if the
     *       list should be restricted to a single authority.</p>
     *
     *   <p><b>{@code -colors}</b><br>
     *       Enable syntax coloring on <A HREF="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI
     *       X3.64</A> compatible (aka ECMA-48 and ISO/IEC 6429) terminal. This option tries to
     *       highlight most of the elements relevant to the {@link #equalsIgnoreMetadata
     *       equalsIgnoreMetadata} method, with the addition of Bursa-Wolf parameters.</p>
     *
     *   <p><b>{@code -encoding}=<var>charset</var></b><br>
     *       Sets the console encoding for this application output. This value has no impact
     *       on data, but may improve the output quality. This is not needed on Linux terminal
     *       using UTF-8 encoding (tip: the <cite>terminus font</cite> gives good results).
     *       Windows users may need to set this encoding to the value returned by the
     *       {@code chcp} command line. This parameter need to be specified only once.</p>
     *
     *   <p><b>{@code -factories}</b><br>
     *       Lists all availables CRS authority factories.</p>
     *
     *   <p><b>{@code -help}</b><br>
     *       Prints the list of options.</p>
     *
     *   <p><b>{@code -locale}=<var>name</var></b><br>
     *       Formats texts in the specified {@linkplain java.util.Locale locale}.</p>
     *
     *   <p><b>{@code -operations} <var>sourceCRS</var> <var>targetCRS</var></b><br>
     *       Prints all available coordinate operations between a pair of CRS. This option
     *       prints only the operations explicitly defined in a database like EPSG. There
     *       is sometime many such operations, and sometime none (in which case this option
     *       prints nothing - it doesn't try to find an operation by itself).</p>
     *
     *   <p><b>{@code -transform} <var>sourceCRS</var> <var>targetCRS</var></b><br>
     *       Prints the preferred math transform between a pair of CRS. At the difference of
     *       the {@code "-operations"} option, this option pick up only one operation (usually
     *       the most accurate one), inferring it if none were explicitly specified in the
     *       database.</p>
     * </blockquote>
     *
     * <strong>Examples</strong> (assuming that {@code "CRS"} is a shortcut for 
     * {@code "java org.geotools.referencing.CRS"}):
     *
     * <blockquote>
     *   <p><b>{@code CRS EPSG:4181 EPSG:4326 CRS:84 AUTO:42001,30,0}</b><br>
     *       Prints the "Luxembourg 1930" CRS, the "WGS 84" CRS (from EPSG database),
     *       the ""WGS84" CRS (from the <cite>Web Map Service</cite> specification) and a UTM
     *       projection in WKT format.</p>
     *
     *   <p><b>{@code CRS -authority=EPSG 4181 4326}</b><br>
     *       Prints the "Luxembourg 1930" and "WGS 84" CRS, looking only in the EPSG
     *       database (so there is no need to prefix the codes with {@code "EPSG"}).</p>
     *
     *   <p><b>{@code CRS -colors EPSG:7411}</b><br>
     *       Prints the "NTF (Paris) / Lambert zone II + NGF Lallemand" CRS with syntax
     *       coloring enabled.</p>
     *
     *   <p><b>{@code CRS -bursawolfs EPSG:4230}</b><br>
     *       Prints three set of Bursa-Wolf parameters for a CRS based on
     *       "European Datum 1950".</p>
     *
     *   <p><b>{@code CRS -authority=EPSG -operations 4230 4326}</b><br>
     *       Prints all operations declared in the EPSG database from "ED50" to "WGS 84"
     *       geographic CRS. Note that for this particular pair of CRS, there is close
     *       to 40 operations declared in the EPSG database. This method prints only the
     *       ones that Geotools can handle.</p>
     *
     *   <p><b>{@code CRS -transform EPSG:4230 EPSG:4326}</b><br>
     *       Prints the math transform that Geotools would use by default for coordinate
     *       transformation from "ED50" to "WGS 84".</p>
     * </blockquote>
     *
     * @param args Options and list of object codes to display.
     *
     * @since 2.4
     */
    public static void main(final String[] args) {
        Command.execute(args);
    }
}
