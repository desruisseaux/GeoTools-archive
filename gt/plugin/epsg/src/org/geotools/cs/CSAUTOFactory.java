/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.cs;

// JAI dependencies
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.ParameterList;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.data.crs.CRSAuthoritySpi;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.pt.CoordinatePoint;


/**
 * Generate Automatic Projections (dynamic projections) based on code and location.
 * Automatic Projections are defined in Annex E of OGC-01-068r3.
 *
 * <ul>
 *   <li>AUTO projection codes are in the range 42000-42499</li>
 *   <li><var>lon0</var> and <var>lat0</var> are centeral point of the projection</li>
 * </ul>
 * <p>
 * The lon0/lat0 are provided by the SRS parameter of the map request
 * (see Section 6.5.5.2 of OGC 01-068r3)
 * </p>
 * <p>
 * This is a first-attempt CoordinateSystemAuthority to me and is not up
 * to the usual high standards of the rest of this package. Please aid in
 * improving this class with bug reports etc...
 * </p> 
 * @version $Id$
 * @author Jody Garnett
 */
public class CSAUTOFactory extends CoordinateSystemAuthorityFactory implements CRSAuthoritySpi {
	/**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    private static CSAUTOFactory DEFAULT;
    
    /**
     * Map of Factlets by Integer code (from AUTO:code)
     *
     * @task TODO: Replace this with full FactorySPI system.
     */
    private final Map facts = new HashMap();
	 
    public CSAUTOFactory(){
        this( CoordinateSystemFactory.getDefault() );
    }
    /**
     * Construct a authority factory backed by the specified factory.
     *
     * @param factory The underlying factory used for objects creation.
     */
    public CSAUTOFactory(final CoordinateSystemFactory factory) {
        super(factory);
        facts.put( new Integer(42001), new Auto42001() );
        facts.put( new Integer(42002), new Auto42002() );
        facts.put( new Integer(42003), new Auto42003() );
        facts.put( new Integer(42004), new Auto42004() );		    
    }

    /**
     * Returns a default coordinate system factory backed by the EPSG property file.
     * 
     * @return The default factory.
     */
    public synchronized static CoordinateSystemAuthorityFactory getDefault() {
        if (DEFAULT == null) {        	
            DEFAULT = new CSAUTOFactory( CoordinateSystemFactory.getDefault() );
        }            
        return DEFAULT;
    }
    /**
     * Returns the authority name, which is known as "Automatic".
     * <p>
     * I assume this is the "display name" presented to end users?
     * </p>
     * @task REVISIT: "AUTO" is not really an authority name. Which organisation
     *                is the author of annex E in OGC-01-068r3?
     */
    public String getAuthority() {
        return "AUTO";
    }

    /**
     * Returns the <code>Factlet</code> for the given code.
     *
     * @param code The code.
     */
    private Factlet factFinder(final Code code) throws NoSuchAuthorityCodeException{
        final Integer key = new Integer( code.code );
        final Factlet fac = (Factlet) facts.get(key);
        if (fac == null) {
            throw new NoSuchAuthorityCodeException( code.classname, code.toString() );
        }
        return fac;
    }

    /**
     * {@inheritDoc}
     */
    public Unit createUnit(final String code) throws FactoryException {
        final Code c = new Code(code, "Unit");
        return factFinder(c).unit();        
    }

    /**
     * {@inheritDoc}
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        final Code c = new Code(code, "Ellipsoid");
        return factFinder(c).ellipsoid();
    }

    /**
     * {@inheritDoc}
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        final Code c = new Code(code, "PrimeMeridian");
	return factFinder(c).primeMeridian();
    }

    /**
     * {@inheritDoc}
     */
    public Datum createDatum(String code) throws FactoryException {
        final Code c = new Code(code, "Datum");
	return factFinder(c).datum();
    }

    /**
     * {@inheritDoc}
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        final Code c = new Code(code, "CoordinateSystem");
	return factFinder(c).create(c);
    }
	
    /**
     * A code parsed by the {@link CoordinateSystemAUTOFactory#parseCode} method.
     * The expected format is <code>code,lon0,lat0</code>.
     *
     * @version $Id$
     * @author Jody Garnett
     * @author Martin Desruisseaux
     */
    private static class Code {
        /**
         * The code number.
         */
        public int code;

        /**
         * The central longitude.
         */
        public double longitude = Double.NaN;

        /**
         * The central latitude.
         */
        public double latitude = Double.NaN;

        /**
         * The short class name of the class to be constructed (e.g. "Ellipsoid").
         * Used only in case of failure for constructing an error message.
         */
        public final String classname;
		
        /**
         * Parse the code string to retrive the code number and central longitude / latitude.
         * Assumed format is <code>AUTO:code,lon0,lat0</code>.
         *
         * @param  text The code in the <code>AUTO:code,lon0,lat0</code> format.
         * @param  classname The short class name of the class to be constructed (e.g. "Ellipsoid").
         *         Used only in case of failure for constructing an error message.
         * @throws NoSuchAuthorityCodeException if the specified code can't be parsed.
         */
        public Code(final String text, final String classname) throws NoSuchAuthorityCodeException {
            this.classname = classname;
            int startField = -1;
    parse:  for (int i=0; ; i++) {
                int endField = text.indexOf(',', ++startField);
                if (endField < 0) {
                    endField = text.length();
                }
                if (endField <= startField) {
                    // A required field was not found.
                    throw new NoSuchAuthorityCodeException( classname, text );
                }
                final String field = text.substring(startField, endField).trim();
                try {
                    switch (i) {
                        case 0:  code      = Integer.parseInt  (field); break;
                        case 1:  longitude = Double.parseDouble(field); break;
                        case 2:  latitude  = Double.parseDouble(field); break parse;
                        // Add case statements here if the is more fields to parse.
                        default: break parse;
                    }
                } catch (NumberFormatException exception) {
                    // If a number can't be parsed, then this is an invalid authority code.
                    NoSuchAuthorityCodeException e = new NoSuchAuthorityCodeException( classname, text);
                    e.initCause(exception);
                    throw e;
                }
                startField = endField;
            }
            if (!(longitude>=Longitude.MIN_VALUE && longitude<=Longitude.MAX_VALUE &&
                  latitude >= Latitude.MIN_VALUE && latitude <= Latitude.MAX_VALUE))
            {
                // A longitude or latitude is out of range, or was not present
                // (i.e. the field still has a NaN value).
                throw new NoSuchAuthorityCodeException( classname, text );
            }
        }
        public String toString(){
			return "AUTO:"+code+","+longitude+","+latitude;        	
        }
    }

    /**
     * Mini Plug-In API because I can't handle switch statements.
     */
    private static interface Factlet {
        public boolean match( Code code );
        public Unit unit();
        public Ellipsoid ellipsoid();
        public PrimeMeridian primeMeridian();
        public Datum datum();
        public CoordinateSystem create( Code code ) throws FactoryException;
    }

    /**
     * Auto Universal Transverse Mercator (AUTO:42001)
     * <p>
     * From the OGC 01-068r3 Annex E:
     * <pre><code>
     * PROJCS["WGS 84 / Auto UTM",
     *   [GEOGCS["WGS 84",
     *       DATUM["WGS_1984",
     *           SPHEROID["WGS_1984", 6378137, 298.257223564]
     *       ],
     *       PRIMEM["Greenwich",0],
     *       UNIT["Decimal_Degree", 0.0174532925199433]
     *   ],
     *   PROJECTON["Transverse_Mercator"],
     *   PARAMETER["Central_Meridian", $centralMeridian ],
     *   PARAMETER["Latitude_of_Origion", 0 ],
     *   PARAMETER["False_Easting", 500000 ],
     *   PARAMETER["False_Northing", $falseNorthing ],
     *   PARAMETER["Scale_Factor", 0.9996 ],
     *   UNIT["Meter",1],
     * ]
     * </code></pre>
     * </p>
     * <p>
     * Where:
     * <ul>
     * <li>$centralMeridian = -183 + $zone * 6
     * <li>$zone = min( floor( $lon0 + 180.0)/6)+1, 60 )
     * <li>$falseNorthing = $lat0 >= 0 ? 0 : 10000000;
     * </ul>
     * </p>
     */    
    private class Auto42001 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42001;
        }
        public Unit unit() {
            return Unit.METRE;
        }
        public Ellipsoid ellipsoid() {
            return Ellipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return PrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return HorizontalDatum.WGS84;
        }
        public CoordinateSystem create(final Code code) throws FactoryException {
            final double   falseNorthing   = code.latitude >= 0.0 ? 0.0 : 10000000.0;
            final double   zone            = Math.min(Math.floor((code.longitude + 180.0)/6.0)+1, 60);
            final double   centralMeridian = -183.0 + zone*6.0;
            final String   classification  = "Transverse_Mercator";
            final ParameterList parameters = factory.createProjectionParameterList(classification);
            parameters.setParameter("central_meridian", centralMeridian);
            parameters.setParameter("false_northing",   falseNorthing);
            final Projection projection = factory.createProjection("Auto UTM", classification, parameters);
            return factory.createProjectedCoordinateSystem("WGS 84 / Auto UTM",
                                                           GeographicCoordinateSystem.WGS84,
                                                           projection);
        }
    }

    /**
     * Auto Transverse Mercator (AUTO:42002)
     * <p>
     * From the OGC 01-068r3 Annex E:
     * <pre><code>
     * PROJCS["WGS 84 / Auto Tr. Mercator",
     *   GEOGCS["WGS 84",
     *     DATUM["WGS_1984",
     *     SPHEROID["WGS_1984", 6378137, 298.257223563]
     *   ],
     *   PRIMEM["Greenwich", 0],
     *   UNIT["Decimal_Degree", 0.0174532925199433]],
     *   PROJECTION["Transverse_Mercator"],
     *   PARAMETER["Central_Meridian", centralMeridian}],
     *   PARAMETER["Latitude_of_Origin", 0],
     *   PARAMETER["False_Easting", 500000],
     *   PARAMETER["False_Northing", falseNorthing}],PARAMETER["Scale_Factor", 0.9996],
     *   UNIT["Meter", 1]
     * ]
     * </code></pre>
     * </p>
     * <p>
     * Where:
     * <ul>
     * <li>centralMeridian = $lon0
     * <li>falseNorthing = ($lat0 >= 0.0) ? 0.0 : 10000000.0
     * </ul>
     */   
    private class Auto42002 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42002;
        }		
        public Unit unit(){
            return Unit.METRE;
        }
        public Ellipsoid ellipsoid() {
            return Ellipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return PrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return HorizontalDatum.WGS84;
        }
        public CoordinateSystem create(final Code code) throws FactoryException {
            final double   centralMeridian = code.longitude;
            final double   falseNorthing   = code.latitude >= 0.0 ? 0.0 : 10000000.0;
            final String   classification  = "Transverse_Mercator";
            final ParameterList parameters = factory.createProjectionParameterList(classification);
            parameters.setParameter("central_meridian", centralMeridian);
            parameters.setParameter("false_northing",   falseNorthing);
            final Projection projection = factory.createProjection("Auto Tr. Mercator", classification, parameters);
            return factory.createProjectedCoordinateSystem("WGS 84 / Auto Tr. Mercator",
                                                           GeographicCoordinateSystem.WGS84,
                                                           projection);
        }		
    }

    /**
     * Auto Orthographic (AUTO:42003)
     *
     * <p>
     * From the OGC 01-068r3 Annex E:
     * <pre><code>
     * PROJCS["WGS 84 / Auto Orthographic",
     *   GEOGCS["WGS 84",
     *     DATUM["WGS_1984",
     *       SPHEROID["WGS_1984", 6378137, 298.257223563]
     *     ],
     *   PRIMEM["Greenwich", 0],
     *   UNIT["Decimal_Degree", 0.0174532925199433]],
     *   PROJECTION["Orthographic"],
     *   PARAMETER["Central_Meridian", centralMeridian}],
     *   PARAMETER["Latitude_of_Origin", latitudeOfOrigin}],
     *   UNIT["Meter", 1]
     * ]
     * </code></pre>
     * </p>
     * <p>
     * Where:
     * <ul>
     * <li>centralMeridian = $lon0
     * <li>latitudeOfOrigin = $lat0
     * </ul>
     */
    private class Auto42003 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42003;
        }		
        public Unit unit(){
            return Unit.METRE;
        }
        public Ellipsoid ellipsoid() {
            return Ellipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return PrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return HorizontalDatum.WGS84;
        }
        public CoordinateSystem create(final Code code) throws FactoryException {
            final double   centralMeridian  = code.longitude;
            final double   latitudeOfOrigin = code.latitude;
            final double   falseNorthing    = code.latitude >= 0.0 ? 0.0 : 10000000.0;
            final String   classification   = "Orthographic";		
            final ParameterList parameters  = factory.createProjectionParameterList(classification);
            parameters.setParameter("central_meridian", centralMeridian);
            parameters.setParameter("latitude_of_orgion", latitudeOfOrigin );
            final Projection projection = factory.createProjection("Auto Orthographic", classification, parameters);
            return factory.createProjectedCoordinateSystem("WGS 84 / Auto Orthographic",
                                                           GeographicCoordinateSystem.WGS84,
                                                           projection);
        }
    }
    /**
     * Auto Equirectangular (AUTO:42004)
     * <p>
     * From the OGC 01-068r3 Annex E:
     * <pre><code>
     * PROJCS["WGS 84 / Auto Equirectangular",
     *   GEOGCS["WGS 84",
     *     DATUM["WGS_1984",
     *       SPHEROID["WGS_1984", 6378137, 298.257223563]
     *     ],
     *     PRIMEM["Greenwich", 0],
     *     UNIT["Decimal_Degree", 0.0174532925199433]
     *   ],
     *   PROJECTION["Equirectangular"],
     *   PARAMETER["Central_Meridian", centralMeridian],
     *   PARAMETER["Latitude_of_Origin", 0],
     *   PARAMETER["Standard_Parallel_1", standardParallel],
     *   UNIT["Meter", 1]
     * ]
     * </code></pre>
     * </p>
     * <p>
     * Where:
     * <ul>
     * <li>centralMeridian = $lon0
     * <li>standard_parallel  = $lat0
     * </ul>
     */	
    private class Auto42004 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42004;
        }		
        public Unit unit(){
            return Unit.METRE;
        }
        public Ellipsoid ellipsoid() {
            return Ellipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return PrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return HorizontalDatum.WGS84;
        }
        public CoordinateSystem create(final Code code) throws FactoryException {
            final double   centralMeridian   = code.longitude;
            final double   standardParallel1 = code.latitude;
            final String   classification    = "Equirectangular";
            final ParameterList parameters   = factory.createProjectionParameterList(classification);
            parameters.setParameter("central_meridian", centralMeridian);
            parameters.setParameter("latitude_of_orgion", 0.0 );
            parameters.setParameter("standard_parallel", standardParallel1);
            final Projection projection = factory.createProjection("Auto Equirectangular", classification, parameters);
            return factory.createProjectedCoordinateSystem("WGS 84 / Auto Equirectangular",
                                                           GeographicCoordinateSystem.WGS84,
                                                           projection);
        }
    }
}
