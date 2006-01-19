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
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;         // For javadoc
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;        // For javadoc
import org.opengis.referencing.operation.CoordinateOperationFactory; // For javadoc
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.AllAuthoritiesFactory;       // For javadoc
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;
import org.geotools.util.GenericName;

// JTS dependencies (to be removed)
import com.vividsolutions.jts.geom.Coordinate;


/**
 * Simple utility class for making use of the {@link CoordinateReferenceSystem}
 * and associated {@link org.opengis.referencing.Factory} implementations.
 * <p>
 * This utility class is made up of static final functions. This class is
 * not a Factory or a Builder. It makes use of the GeoAPI Factory interfaces
 * provided by {@link FactoryFinder} in the most direct manner possible.
 * <p>
 * The following methods may be added in a future version:
 * <ul>
 *   <li>CoordinateReferenceSystem parseXML( String )</li>
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
     * Implement this method to visit each available {@link CoordinateOperationFactory}
     * known to {@link FactoryFinder}.
     *
     * @since 2.1
     * @version $Id$
     * @author Jody Garnett (Refractions Research)
     *
     * @deprecated No public API uses this interface at this time. If a particular
     *             {@link CoordinateOperationFactory} implementation is wanted, try
     *             to provide a {@link Hints#COORDINATE_OPERATION_FACTORY} hint to the
     *             {@link FactoryFinder#getCoordinateOperationFactory} method instead.
     *             In a future version, this interface will be removed or expanded if
     *             the hints way is not suffisient.
     */
    public interface OperationVisitor {
        /**
         * Implement this method to visit each available CoordinateOperationFactory
         * known to FactoryFinder.
         * <p>
         * You may register additional Factories using META-INF/serivces
         * please see  
         * </p>
         * @param factory
         * @return Value created using the Factory, visit returns a list of these
         */
        public Object  factory( CoordinateOperationFactory factory ) throws FactoryException;
    }

    /**
     * Do not allow instantiation of this class.
     */
    private CRS() {
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
        return org.geotools.resources.CRSUtilities.equalsIgnoreMetadata(object1, object2);
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
     * <code>{@linkplain #transform(CoordinateReferenceSystem, CoordinateReferenceSystem, boolean)
     * transform(CoordinateReferenceSystem,CoordinateReferenceSystem)}(sourceCRS, targetCRS)</code>,
     * except that it can optionally tolerate <cite>lenient datum shift</cite>. If the
     * {@code lenient} argument is {@code true}, then this method will not throw a
     * "<cite>Bursa-Wolf parameters required</cite>" exception during datum shifts if the
     * Bursa-Wolf paramaters are not specified. Instead it will assume a no datum shift.
     * 
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @param  lenient {@code true} if the math transform should be created even when there is
     *         no information available for a datum shift. The default value is {@code false}.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
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
    	Set result = Collections.EMPTY_SET;
        boolean isSetCopied = false;
    	for (final Iterator i=FactoryFinder.getCRSAuthorityFactories().iterator(); i.hasNext();) {
            final CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();
            if (Citations.identifierMatches(factory.getAuthority(), authority)) {
                final Set codes;
                try {
                    codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
                } catch (Exception exception) {
                    /*
                     * Failed to fetch the codes either because of a database connection problem
                     * (FactoryException), or because we are using a simple factory that doesn't
                     * support this operation (UnsupportedOperationException), or any unexpected
                     * reason. No codes from this factory will be added to the set.
                     */
                    Utilities.unexpectedException("org.geotools.referencing", "CRS",
                                                  "getSupportedCodes", exception);
                    continue;
                }
                if (codes!=null && !codes.isEmpty()) {
                    if (result.isEmpty()) {
                        result = codes;
                    } else {
                        if (!isSetCopied) {
                            result = new LinkedHashSet(result);
                            isSetCopied = true;
                        }
                        result.addAll(codes);
                    }
                }
			}
    	}
        return result;
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
     * If there is more than one factory implementation for the same authority, then this
     * method tries all of them in their iteration order and returns the first successfully
     * created CRS.
     * <p>
     * <strong>NOTE:</strong> The {@link AllAuthoritiesFactory} class provides similar functionality
     * as an {@link CRSAuthorityFactory} implementation. It allows the same client code to work with
     * either a specific authority, or all available authorities, at user choice. A call to this
     * {@code decode(code)} method maps approximatively to the following code:
     *
     * <blockquote><code>
     * {@linkplain AllAuthoritiesFactory#DEFAULT}.createCoordinateReferenceSystem(code)
     * </code></blockquote>
     *
     * The main difference is that {@code CRSAuthorityFactory} uses only the "preferred"
     * implementation for each authority, while this {@code decode} method tries every
     * implementations. Using only the preferred implementation way save class loading
     * and database connections if more than one EPSG database are availables (for example
     * an {@linkplain org.geotools.referencing.factory.epsg.AccessDataSource Access} and a
     * {@linkplain org.geotools.referencing.factory.epsg.PostgreDataSource PostgreSQL} ones)
     * and their content are expected identical.
     *
     * @param  code The Coordinate Reference System authority code.
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     *
     * @see #getSupportedCodes
     * @see AllAuthoritiesFactory#createCoordinateReferenceSystem
     */ 
    public static CoordinateReferenceSystem decode(String code) throws NoSuchAuthorityCodeException  {
        /*
         * Gets the authority name, in upper case mostly for consistency
         *  with a previous version of this method.
         */
        code = code.trim().toUpperCase();
        final int split = code.indexOf(GenericName.DEFAULT_SEPARATOR);
        if (split < 0) {
            throw new NoSuchAuthorityCodeException(Errors.format(ErrorKeys.MISSING_AUTHORITY_$1, code),
                                                   Vocabulary.format(VocabularyKeys.UNKNOW), code);
        }
        final String authority = code.substring(0, split).trim();
        /*
         * Tries all implementations in their iteration order. This first failures reasons are
         * stored and will be rethrown at the end of this method, if no CRS were constructed.
         */
        Exception trouble = null;
        NoSuchAuthorityCodeException notFound = null;
        for (Iterator i=FactoryFinder.getCRSAuthorityFactories().iterator(); i.hasNext();) {
            final CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();
            if (!Citations.identifierMatches(factory.getAuthority(), authority)) {
                continue;
            }
            final CoordinateReferenceSystem crs;
            try {
                crs = factory.createCoordinateReferenceSystem(code);
            } catch (NoSuchAuthorityCodeException e) {
                if (notFound == null) {
                    notFound = e;
                }
                continue;
            } catch (Exception e) {
                // Catchs FactoryException as well as UnsupportedOperationException.
                if (trouble == null) {
                    trouble = e;
                }
                continue;
            }
            if (crs != null) {
                if (trouble != null) {
                    /*
                     * We have been able to construct the CRS, but using some secondary factory.
                     * the preferred factory failed. Reports this failure as a warning.
                     */
                    Utilities.unexpectedException("org.geotools.referencing", "CRS", "decode", trouble);
                }
                return crs;
            }
        }
        if (notFound == null) {
            notFound = new NoSuchAuthorityCodeException(
                           Errors.format(ErrorKeys.UNKNOW_AUTHORITY_$1, authority), authority, code);
            notFound.initCause(trouble); // trouble may be null.
        }
        throw notFound;
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
     * the given CRS.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The envelope, or {@code null} if none.
     *
     * @since 2.2
     */
    public static Envelope getEnvelope(final CoordinateReferenceSystem crs) {
        // TODO: Copy the implementation from CRSUtilities there.
        return org.geotools.resources.CRSUtilities.getEnvelope(crs);
    }

    /**
     * ESTIMATE the distance between the two points.
     *    1. transforms both points to lat/lon
     *    2. find the distance between the two points
     * 
     *  NOTE: we're using ellipsoid calculations.
     * 
     * @param p1   first point
     * @param p2   second point
     * @param crs  reference system the two points are in
     * @return approximate distance between the two points, in meters
     *
     * @deprecated Moved to {@link org.geotools.geometry.jts.JTS#orthodromicDistance} (in the
     *             main module) in order to avoid JTS dependency from the referencing module.
     *             In addition, the new method should be slightly more efficient and accurate,
     *             and the method signature (including the exception clause) is more specific.
     */
    public static double distance(Coordinate p1, Coordinate p2, CoordinateReferenceSystem crs) throws Exception
	{
    	GeodeticCalculator gc = new GeodeticCalculator() ;  // WGS84
    	
    	double[] cs        = new double[4];
    	double[] csLatLong = new double[4];
    	cs[0] = p1.x;
    	cs[1] = p1.y;
    	cs[2] = p2.x;
    	cs[3] = p2.y;    	 
         
    	MathTransform transform = distanceOperationFactory.createOperation(crs,DefaultGeographicCRS.WGS84).getMathTransform();
    	transform.transform(cs, 0, csLatLong, 0, 2);
    	   //these could be backwards depending on what WSG84 you use
    	gc.setAnchorPoint(csLatLong[0],csLatLong[1]);
    	gc.setDestinationPoint(csLatLong[2],csLatLong[3]);
    
    	return gc.getOrthodromicDistance();
    }

    /**
     * @deprecated To be deleted once {@link #distance} will be removed from this method.
     */
    private final static CoordinateOperationFactory distanceOperationFactory;
    static {
        Hints hints=new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        distanceOperationFactory=FactoryFinder.getCoordinateOperationFactory(hints);
    }
}
