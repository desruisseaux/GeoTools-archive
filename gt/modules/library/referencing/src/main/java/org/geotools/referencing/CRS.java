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
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.BoundingPolygon;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.Factory;                              // For javadoc
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.referencing.crs.DefaultGeographicCRS;            // For javadoc
import org.geotools.util.UnsupportedImplementationException;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.util.Logging;


/**
 * Simple utility class for making use of the {@linkplain CoordinateReferenceSystem
 * coordinate reference system} and associated {@linkplain Factory factory} implementations.
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


    //////////////////////////////////////////////////////////////
    ////                                                      ////
    ////        FACTORIES, CRS CREATION AND INSPECTION        ////
    ////                                                      ////
    //////////////////////////////////////////////////////////////

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
                Logging.unexpectedException("org.geotools.referencing", "CRS",
                                            "getEnvelope", exception);
            } catch (TransformException exception) {
                /*
                 * The envelope is probably outside the range of validity for this CRS.
                 * It should not occurs, since the envelope is supposed to describe the
                 * CRS area of validity. Logs a warning and returns null, since it is a
                 * legal return value according this method contract.
                 */
                envelope = null;
                Logging.unexpectedException("org.geotools.referencing", "CRS",
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
                        if (!bounds.getInclusion()) {
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
            Logging.unexpectedException("org.geotools.referencing", "CRS",
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


    /////////////////////////////////////////////////
    ////                                         ////
    ////          COORDINATE OPERATIONS          ////
    ////                                         ////
    /////////////////////////////////////////////////

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
    public static MathTransform findMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                  final CoordinateReferenceSystem targetCRS,
                                                  boolean lenient)
            throws FactoryException
    {
        final CoordinateOperationFactory factory =
                FactoryFinder.getCoordinateOperationFactory(lenient ? LENIENT : null);
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
}
