/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004 Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.crs;

// J2SE dependencies and extensions
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Generate Automatic Projections (dynamic projections) based on code and location.
 * Automatic Projections are defined in Annex E of OGC-01-068r3 
 * (Web Map Service Implementation Specification).
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
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 *
 * @deprecated Moved to {@link org.geotools.referencing.factory.wms.AutoCRSFactory}.
 */
//how is this going to work with the factory finder
public class AUTOCRSAuthorityFactory implements CRSAuthorityFactory {
    /**
     * A tuple of {@link CRSFactory} and the {@link MathTransformFactory}. All other
     * factories are left to null. A factory group provides convenience methods that
     * can't appears in GeoAPI factories, because they involve more than one of them.
     * For example there is a {@link FactoryGroup#createProjectedCRS} that creates
     * itself a math transform from a given set of parameters. Because this method
     * involves both a math transform factory and a CRS factory, it can't be a
     * {@link MathTransformFactory} or a {@link CRSFactory} method.
     */
    private final FactoryGroup factories;
    
    /**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    private static AUTOCRSAuthorityFactory DEFAULT;
    
    /**
     * Map of Factlets by Integer code (from AUTO:code)
     *
     * @task TODO: Replace this with full FactorySPI system.
     */
    private final Map facts = new HashMap();
	 
    /**
     * Construct <code>CRSAUTOFactory</code>.
     */
    public AUTOCRSAuthorityFactory(){
        this(FactoryFinder.getCRSFactory(null));
    }
    
    /**
     * Construct a authority factory backed by the specified factory.
     *
     * @param factory The underlying factory used for objects creation.
     */
    public AUTOCRSAuthorityFactory(final CRSFactory factory) {
        factories = new FactoryGroup(null, null, factory, null);
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
    public synchronized static CRSAuthorityFactory getDefault() {
        if (DEFAULT == null) {        	
            DEFAULT = new AUTOCRSAuthorityFactory();
        }            
        return DEFAULT;
    }
        
    public CoordinateReferenceSystem createCoordinateReferenceSystem(String code) throws FactoryException {
        final Code c = new Code(code, "CoordinateReferenceSystem");
	return factFinder(c).create(c);
    }
        
    public IdentifiedObject createObject(String code) throws FactoryException {
        return createCoordinateReferenceSystem(code);
    }
    
    public ProjectedCRS createProjectedCRS(String code) throws FactoryException {
        return (ProjectedCRS) createCoordinateReferenceSystem(code);
    }
     
    public Citation getAuthority() {
        return Citations.AUTO;
    }
    
    /**
     * Provide a complete set of the known codes provided by this authority.
     * <p>
     * Note. this implementation should provide a leading "AUTO:" prefix, but
     * the result provided does work with the EPSG assumption maintained by
     * CRSService. The result may provide this prefix in the future. 
     * </p>
     * @return Set of know codes.
     */
    public Set getAuthorityCodes(Class clazz) throws FactoryException {
//wrong, want to filter codes based on clazz
        Set set = new TreeSet();
        for( Iterator i=facts.keySet().iterator(); i.hasNext(); ) {
            Integer code = (Integer) i.next();
            set.add( "AUTO:"+code );
        }
        return set;
    }

    /**
     * @deprecated This method was required by old GeoAPI interfaces, but is not required anymore.
     */
    public org.opengis.referencing.ObjectFactory getObjectFactory() {
        return factories.getCRSFactory();
    }
    
    public Citation getVendor() {
        return Citations.GEOTOOLS;
    }
    
    public org.opengis.util.InternationalString getDescriptionText(String str) throws FactoryException {
//implement this (return the crs name)        
        throw new FactoryException("Not implemented");
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
            throw new NoSuchAuthorityCodeException(code.toString(), "AUTO",  code.classname);
        }
        return fac;
    }
    
    public org.opengis.referencing.crs.CompoundCRS createCompoundCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.DerivedCRS createDerivedCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.EngineeringCRS createEngineeringCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.GeocentricCRS createGeocentricCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.GeographicCRS createGeographicCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.ImageCRS createImageCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.TemporalCRS createTemporalCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.VerticalCRS createVerticalCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
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
        /** The authority - should usually be AUTO */
        public String authority;
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
                    throw new NoSuchAuthorityCodeException(classname, "AUTO",  text);
                }
                final String field = text.substring(startField, endField).trim();
                try {
                    switch (i) {
                        case 0:  int split = field.indexOf(':');
                                 authority = field.substring(0,split);
                                 code      = Integer.parseInt  (field.substring(split+1)); break;
                        case 1:  longitude = Double.parseDouble(field); break;
                        case 2:  latitude  = Double.parseDouble(field); break parse;
                        // Add case statements here if the is more fields to parse.
                        default: break parse;
                    }
                } catch (NumberFormatException exception) {
                    // If a number can't be parsed, then this is an invalid authority code.
                    NoSuchAuthorityCodeException e = new NoSuchAuthorityCodeException(classname, "AUTO",  text);
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
                throw new NoSuchAuthorityCodeException(classname, "AUTO",  text);
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
        public CoordinateReferenceSystem create( Code code ) throws FactoryException;
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
     *   PARAMETER["Latitude_of_Origin", 0 ],
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
            return SI.METER;
        }
        public Ellipsoid ellipsoid() {
            return DefaultEllipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return DefaultPrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return DefaultGeodeticDatum.WGS84;
        }
        public CoordinateReferenceSystem create(final Code code) throws FactoryException {
            GeographicCRS geoCRS = DefaultGeographicCRS.WGS84;
            CartesianCS cartCS = DefaultCartesianCS.PROJECTED;
            
            final double   falseNorthing   = code.latitude >= 0.0 ? 0.0 : 10000000.0;
            final double   zone            = Math.min(Math.floor((code.longitude + 180.0)/6.0)+1, 60);
            final double   centralMeridian = -183.0 + zone*6.0;
            final String   classification  = "Transverse_Mercator";

            ParameterValueGroup parameters = factories.getMathTransformFactory().getDefaultParameters(classification);
            parameters.parameter("central_meridian").setValue(centralMeridian);
            parameters.parameter("latitude_of_origin").setValue(0.0);
            parameters.parameter("scale_factor").setValue(0.9996);
            parameters.parameter("false_easting").setValue(500000.0);
            parameters.parameter("false_northing").setValue(falseNorthing);
            
            return factories.createProjectedCRS(Collections.singletonMap("name", "WGS 84 / Auto UTM"), 
                    geoCRS, null, parameters, cartCS);
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
     *   PARAMETER["False_Northing", falseNorthing}],
     *   PARAMETER["Scale_Factor", 0.9996],
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
            return SI.METER;
        }
        public Ellipsoid ellipsoid() {
            return DefaultEllipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return DefaultPrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return DefaultGeodeticDatum.WGS84;
        }
        public CoordinateReferenceSystem create(final Code code) throws FactoryException {
            GeographicCRS geoCRS = DefaultGeographicCRS.WGS84;
            CartesianCS cartCS = DefaultCartesianCS.PROJECTED;
          
            final double   centralMeridian = code.longitude;
            final double   falseNorthing   = code.latitude >= 0.0 ? 0.0 : 10000000.0;
            final String   classification  = "Transverse_Mercator";
            
            ParameterValueGroup parameters = factories.getMathTransformFactory().getDefaultParameters(classification);
            parameters.parameter("central_meridian").setValue(centralMeridian);
            parameters.parameter("latitude_of_origin").setValue(0.0);
            parameters.parameter("scale_factor").setValue(0.9996);
            parameters.parameter("false_easting").setValue(500000.0);
            parameters.parameter("false_northing").setValue(falseNorthing);
            
            return factories.createProjectedCRS(Collections.singletonMap("name", "WGS 84 / Auto Tr. Mercator"), 
                    geoCRS, null, parameters, cartCS);  
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
    //this will fail; we do not support ellipsoidal formulas for the orthographic
    private class Auto42003 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42003;
        }
        public Unit unit(){
            return SI.METER;
        }
        public Ellipsoid ellipsoid() {
            return DefaultEllipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return DefaultPrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return DefaultGeodeticDatum.WGS84;
        }
        public CoordinateReferenceSystem create(final Code code) throws FactoryException {
            GeographicCRS geoCRS = DefaultGeographicCRS.WGS84;
            CartesianCS cartCS = DefaultCartesianCS.PROJECTED;
          
            final double   centralMeridian  = code.longitude;
            final double   latitudeOfOrigin = code.latitude;
            final String   classification   = "Orthographic";
            
            ParameterValueGroup parameters = factories.getMathTransformFactory().getDefaultParameters(classification);
            parameters.parameter("central_meridian").setValue(centralMeridian);
            parameters.parameter("latitude_of_origin").setValue(latitudeOfOrigin);
            parameters.parameter("scale_factor").setValue(1.0);
            parameters.parameter("false_easting").setValue(0.0);
            parameters.parameter("false_northing").setValue(0.0);
            
            return factories.createProjectedCRS(Collections.singletonMap("name", "WGS 84 / Auto Orthographic"), 
                    geoCRS, null, parameters, cartCS);  
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
    //this will fail; projection not yet supported
    private class Auto42004 implements Factlet {
        public boolean match(Code code) {
            return code.code == 42004;
        }	
        public Unit unit(){
            return SI.METER;
        }
        public Ellipsoid ellipsoid() {
            return DefaultEllipsoid.WGS84;
        }
        public PrimeMeridian primeMeridian() {
            return DefaultPrimeMeridian.GREENWICH;
        }
        public Datum datum() {
            return DefaultGeodeticDatum.WGS84;
        }
        public CoordinateReferenceSystem create(final Code code) throws FactoryException {
            GeographicCRS geoCRS = DefaultGeographicCRS.WGS84;
            CartesianCS cartCS = DefaultCartesianCS.PROJECTED;
        
            final double   centralMeridian   = code.longitude;
            final double   standardParallel1 = code.latitude;
            final String   classification    = "Equirectangular";
            
            ParameterValueGroup parameters = factories.getMathTransformFactory().getDefaultParameters(classification);
            parameters.parameter("central_meridian").setValue(centralMeridian);
            parameters.parameter("latitude_of_origin").setValue(0.0);
            parameters.parameter("standard_parallel_1").setValue(standardParallel1);
            parameters.parameter("scale_factor").setValue(1.0);
            parameters.parameter("false_easting").setValue(0.0);
            parameters.parameter("false_northing").setValue(0.0);
            
            return factories.createProjectedCRS(Collections.singletonMap("name", "WGS 84 / Auto Orthographic"), 
                    geoCRS, null, parameters, cartCS);  
        }
    }
    
}
