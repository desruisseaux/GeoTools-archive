/**
 * This tutorial code was developed and tested with the geotools 2.1.0
 * release jars.
 * 
 * Geotools dependancies:
 *     gt2-main.jar
 *     gt2-epsg-wkt.jar or gt2-epsg-hsql.jar or gt2-epsg-access
 * 
 * Other dependancies:
 *     geoapi-2.0.jar
 *     units-0.01.jar
 *     vecmath-1.3.jar
 *     hsqldb-1.7.3.jar (if using gt2-epsg-hsql.jar)
 * 
 */
package org.geotools.demo.referencing;

//J2SE dependancies
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.units.NonSI;
import javax.units.SI; 

//GeoAPI dependencies
import org.opengis.referencing.FactoryException;
//import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.operation.*;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
//import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.metadata.Identifier;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.BursaWolfParameters;

/**
 *
 * Code for the geotools coordinate transformation services tutorial:
 * http://docs.codehaus.org/display/GEOTOOLS/Coordinate+Transformation+Services+for+Geotools+2.1
 *
 * The following code is made up of many, short methods used for discussion in the tutorial,
 * and does not create a coherent program. 
 * 
 * These examples cover the following topics:
 * <ul>
 *   <li>creating coordinate reference systems (CRS) by hand</li>
 *   <li>creating CRS's from well known text (WKT) strings</li>
 *   <li>creating CRS's from authority codes </li>
 *   <li>creating math transforms between CRS's </li>
 *   <li>creating math transforms by hand </li>
 *   <li> </li> 
 * </ul>
 *
 * START SNIPPET and END SNIPPET comments are used by the wiki to display code snippets from svn.
 *
 * @author Rueben Schulz
 *
 * TODO using the auto crs factory
 *      utility fuctions in CRS class (such as decode)
 */
public class CTSTutorial {
	
	private CRSFactory crsFactory = null;
	private MathTransformFactory mtFactory = null;
	private FactoryGroup factories = null;
	
	private CoordinateReferenceSystem geocentricCRS = null;
	private CoordinateReferenceSystem airyCRS = null;
	
	CTSTutorial() {
            try {
                
                // set up some factories used below
                crsFactory = FactoryFinder.getCRSFactory(null);
                mtFactory = FactoryFinder.getMathTransformFactory(null);
                // factories in the group will be created from defaults
                factories = new FactoryGroup();
                
                creatCRSFromWKT();
                createFromEPSGCode("26910");
                //createFromEPSGCode("EPSG:4326");
                createCRSByHand1();
                createCRSByHand2();
                createCRSByHand3();
                createAndUseMathTransform();
                createMathTransformBetweenCRSs(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
	}
        
        
        /**
         * A method with some examples of premade static objects. 
         */
        void premadeObjects() {
            // START SNIPPET: premadeObjects
            GeographicCRS geoCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
            GeodeticDatum wgs84Datum = org.geotools.referencing.datum.DefaultGeodeticDatum.WGS84;
            PrimeMeridian greenwichMeridian = org.geotools.referencing.datum.PrimeMeridian.GREENWICH;
            CartesianCS cartCS = org.geotools.referencing.cs.DefaultCartesianCS.GENERIC_2D;
            CoordinateSystemAxis latAxis = org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LATITUDE;
            // END SNIPPET: premadeObjects
        }
	
	/**
	 * An example of creating a CRS from a WKT string. Additonal examples of WKT strings
	 * can be found in
         * http://svn.geotools.org/geotools/trunk/gt/module/referencing/test/org/geotools/referencing/test-data/
	 * 
	 * TODO Brief description of what a CRS is (and what it is composed of)
	 * 
	 * @throws Exception
	 */
	void creatCRSFromWKT() throws Exception {
		System.out.println("------------------------------------------"); 
		System.out.println("Creating a CRS from a WKT string:");
                // START SNIPPET: crsFromWKT
		String wkt = "PROJCS[\"UTM_Zone_10N\", "
		               + "GEOGCS[\"WGS84\", "
		                   + "DATUM[\"WGS84\", "
		                   + "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "
		                   + "PRIMEM[\"Greenwich\", 0.0], "
		                   + "UNIT[\"degree\",0.017453292519943295], "
		                   + "AXIS[\"Longitude\",EAST], "
		                   + "AXIS[\"Latitude\",NORTH]], "
		               + "PROJECTION[\"Transverse_Mercator\"], "
		               + "PARAMETER[\"semi_major\", 6378137.0], "
		               + "PARAMETER[\"semi_minor\", 6356752.314245179], "
		               + "PARAMETER[\"central_meridian\", -123.0], "
		               + "PARAMETER[\"latitude_of_origin\", 0.0], "
		               + "PARAMETER[\"scale_factor\", 0.9996], "
		               + "PARAMETER[\"false_easting\", 500000.0], "
		               + "PARAMETER[\"false_northing\", 0.0], "
		               + "UNIT[\"metre\",1.0], "
		               + "AXIS[\"x\",EAST], "
		               + "AXIS[\"y\",NORTH]]";

		CoordinateReferenceSystem crs = crsFactory.createFromWKT(wkt);
                // END SNIPPET: crsFromWKT
		System.out.println("  CRS: " + crs.toWKT());
                System.out.println("Identified CRS object:");
		printIdentifierStuff(crs);
                System.out.println("Identified Datum object:");
                printIdentifierStuff(((ProjectedCRS)crs).getDatum());
		System.out.println("------------------------------------------"); 
	}
        
        
        /**
         * Creates a CRS from an EPSG code. There are a few different EPSG authority factories in 
         * geotools that do roughly the same thing:
         *
         * <ul>
         *   <li>gt2-epsg-access.jar is backed by the official EPSG MS Access database 
         *     (only works on MS Windows, therefore I have not shown how to configure it here).</li>
         *   <li>gt2-epsg-hsql.jar provides an embeded hsql database created from the EPSG SQL scripts. This contains the
         *     same information as the MS Arcess database.</li>
         *   <li>other factories allow the EPSG information to be in an external database (postgresql, mysql, oracle)</li>
         *   <li>gt2-epsg-wkt.jar is a simple properties file with WKT descriptions for EPSG defined CRS codes. This file does not 
         * derive directly from the official EPSG database, so its should be used with caution. It 
         * provides a very simple method of creating a new authority factory and named objects.</li>
         * </ul>
         *
         * The specific authority factory returned by getCRSAuthorityFactory is dependent on the different 
         * factories on your classpath (ie WKT or Access or HSQL) and the hints you provide. By default the "better"
         * authority factory should be used if more than one is available.
         *
         * TODO check on the use of hints
         * TODO expand on how to use EPSG data in a postgres db (this may be a 2.2 feature, but FactoryUsingANSISQL may work)
         *
         */
        void createFromEPSGCode(String code) throws Exception {
            System.out.println("------------------------------------------"); 
            System.out.println("Creating a CRS from a factory:");
            // START SNIPPET: crsFromCode
            CoordinateReferenceSystem crs = FactoryFinder.getCRSAuthorityFactory("EPSG",null).createCoordinateReferenceSystem(code);
            // END SNIPPET: crsFromCode
            System.out.println("  CRS: " + crs.toWKT());
            System.out.println("Identified CRS object:");
            printIdentifierStuff(crs);
            System.out.println("------------------------------------------"); 
        }
        
	/**
	 * Creates a WGS 84/UTM Zone 10N CRS mostly (uses some premade objects) by hand.
         * Uses the higher level FactoryGroup instead of the lower level MathTransformFactory
         * (commented out).
	 * 
	 * @throws Exception
	 */
	CoordinateReferenceSystem createCRSByHand1() throws Exception {
		System.out.println("------------------------------------------"); 
		System.out.println("Creating a CRS by hand:");
		MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);

		GeographicCRS geoCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
		CartesianCS cartCS = org.geotools.referencing.cs.DefaultCartesianCS.GENERIC_2D;

		ParameterValueGroup parameters = mtFactory.getDefaultParameters("Transverse_Mercator");
		parameters.parameter("central_meridian").setValue(-111.0);
		parameters.parameter("latitude_of_origin").setValue(0.0);
		parameters.parameter("scale_factor").setValue(0.9996);
		parameters.parameter("false_easting").setValue(500000.0);
		parameters.parameter("false_northing").setValue(0.0);
		//parameters.parameter("semi_major").setValue(((GeodeticDatum)geoCRS.getDatum()).getEllipsoid().getSemiMajorAxis());
		//parameters.parameter("semi_minor").setValue(((GeodeticDatum)geoCRS.getDatum()).getEllipsoid().getSemiMinorAxis());

		//MathTransform trans = mtFactory.createParameterizedTransform(parameters);
		//ProjectedCRS projCRS = crsFactory.createProjectedCRS(
		//	Collections.singletonMap("name", "WGS 84 / UTM Zone 12N"), 
		//    new org.geotools.referencing.operation.OperationMethod(trans),
		//    geoCRS, trans, cartCS);

		Map properties = Collections.singletonMap("name", "WGS 84 / UTM Zone 12N");
		ProjectedCRS projCRS = factories.createProjectedCRS(properties, geoCRS, null, parameters, cartCS);
    
		System.out.println("  Projected CRS: " + projCRS.toWKT());
		System.out.println("------------------------------------------"); 
                
                return projCRS;
		
	}
        
        /**
         * Creates a NAD 27 geographic CRS. Notice that the datum factory automatically
         * adds aliase names to the datum (because "North American Datum 1927" has an entry in
         * http://svn.geotools.org/geotools/trunk/gt/module/referencing/src/org/geotools/referencing/factory/DatumAliasesTable.txt
         * ). Also notice that toWGS84 information (used in a datum transform) was also added to the datum.
         *
         */
        CoordinateReferenceSystem createCRSByHand2() throws Exception {
            System.out.println("------------------------------------------"); 
            System.out.println("Creating a CRS by hand:");
            // START SNIPPET: nad27crsByHand
            DatumFactory               datumFactory     = FactoryFinder.getDatumFactory(null);
            CSFactory                  csFactory        = FactoryFinder.getCSFactory(null);
            
            Map map = new HashMap();
            map.put("name", "Clarke 1866");
            
            Ellipsoid clark1866ellipse = 
			datumFactory.createFlattenedSphere(map, 6378206.4, 294.978698213901, SI.METER);
            
            PrimeMeridian greenwichMeridian = org.geotools.referencing.datum.PrimeMeridian.GREENWICH;
            
            final BursaWolfParameters toWGS84 = new BursaWolfParameters(DefaultGeodeticDatum.WGS84);
            toWGS84.dx = -3.0;
            toWGS84.dy = 142;
            toWGS84.dz = 183;
            
            map.clear();
            map.put("name", "North American Datum 1927");
            map.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, toWGS84);
            
            GeodeticDatum clark1866datum = datumFactory.createGeodeticDatum(map, clark1866ellipse, greenwichMeridian);
            System.out.println(clark1866datum.toWKT());
  // notice all of the lovely datum aliases (used to determine if two datums are the same)
            System.out.println("Identified Datum object:");
            printIdentifierStuff(clark1866datum);
            
            //EllipsoidalCS ellipsCS = org.geotools.referencing.cs.DefaultEllipsoidalCS.GEODETIC_2D;
            map.clear();
            map.put("name", "<lat>, <long>");
            CoordinateSystemAxis latAxis = org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LATITUDE;
            CoordinateSystemAxis longAxis = org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LONGITUDE;
            EllipsoidalCS ellipsCS = csFactory.createEllipsoidalCS(map, latAxis, longAxis);
            
            map.clear();
            map.put("name", "NAD 27");
            map.put("authority","9999");
  // TODO add an authority code here
            GeographicCRS nad27CRS = crsFactory.createGeographicCRS(map, clark1866datum,ellipsCS);
            System.out.println(nad27CRS.toWKT());
            // END SNIPPET: nad27crsByHand
            System.out.println("Identified CRS object:");
            printIdentifierStuff(nad27CRS);
            
            System.out.println("------------------------------------------"); 
            return nad27CRS;
        }
	
	/**
	 * Creates two coordinate reference system by hand without using 
	 * any of the GT2 APIs (except FactoryFinder to get things started). It 
         * does not use any of the static objects available in geotools implementations.
	 * The following example creates a CRS to represent the Airy 1830 
	 * ellipsoid with the incoming data in the order of (long,lat,height) and 
         * a geocentric CRS with (x,y,z) axises.
         *
         * TODO the Airy CRS described below is actually wgs84, FIX this.
         *
	 * @throws FactoryException
	 */
	void createCRSByHand3() throws FactoryException {
                System.out.println("------------------------------------------"); 
		System.out.println("Creating two CRSs by hand:");
		DatumFactory               datumFactory     = FactoryFinder.getDatumFactory(null);
		CSFactory                  csFactory        = FactoryFinder.getCSFactory(null);
		Map map = new HashMap();
                
		//
                // Create a datum used for each CRS
                //
		map.clear();
		map.put("name", "Greenwich Meridian");
		PrimeMeridian greenwichMeridian =
			datumFactory.createPrimeMeridian(map, 0, NonSI.DEGREE_ANGLE);
		
		map.clear();
		map.put("name", "WGS 84 Ellipsoid Datum");
		Ellipsoid wgs84Ellipsoid = 
			datumFactory.createFlattenedSphere(map, 6378137, 298.257223563, SI.METER);
		
		map.clear();
		map.put("name", "WGS84 Height Datum");
		GeodeticDatum wgs84Datum = 
			datumFactory.createGeodeticDatum(map, wgs84Ellipsoid, greenwichMeridian);
		
                //
                //Create a geocentric CRS
                //
		// Create a collection of axes for the coordinate system.
		map.clear();
		map.put("name", "Cartesian X axis");
		CoordinateSystemAxis xAxis =
			csFactory.createCoordinateSystemAxis(map, "X", AxisDirection.GEOCENTRIC_X, SI.METER);
		
		map.clear();
		map.put("name", "Cartesian Y axis");
		CoordinateSystemAxis yAxis =
			csFactory.createCoordinateSystemAxis(map, "Y", AxisDirection.GEOCENTRIC_Y, SI.METER);
		
		map.clear();
		map.put("name", "Cartesian Z axis");
		CoordinateSystemAxis zAxis =
			csFactory.createCoordinateSystemAxis(map, "Z", AxisDirection.GEOCENTRIC_Z, SI.METER);
		
                map.clear();
		map.put("name", "Rendered Cartesian CS");
		CartesianCS worldCS = csFactory.createCartesianCS(map, xAxis, yAxis, zAxis);
                
		// Now, the geocentric coordinate reference system that we'd use for output - eg to a 3D renderer
		map.clear();
		map.put("name", "Output Cartesian CS");
		geocentricCRS = crsFactory.createGeocentricCRS(map, wgs84Datum, worldCS);
		System.out.println("Geocentric CRS: " + geocentricCRS.toWKT());
		
                //
		// Create a geograyhic CRS for the Airy 1830 ellipsoid
		//map.clear();
		//map.put("name", "Airy 1830");
		//Ellipsoid airyEllipse = 
		//	datumFactory.createFlattenedSphere(map, 6377563.396, 299.3249646, SI.METER);
		
                map.clear();
		map.put("name", "Geodetic North axis");
		CoordinateSystemAxis northAxis = csFactory.createCoordinateSystemAxis(map, "N",AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
		
		map.clear();
		map.put("name", "Geodetic East axis");
		CoordinateSystemAxis eastAxis = csFactory.createCoordinateSystemAxis(map, "E", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
		
		map.clear();
		map.put("name", "Geodetic Height axis");
		CoordinateSystemAxis heightAxis = csFactory.createCoordinateSystemAxis(map, "Up", AxisDirection.UP, SI.METER);
                
		map.clear();
		map.put("name", "<long>,<lat> Airy 1830 geodetic");
		EllipsoidalCS airyCS = csFactory.createEllipsoidalCS(map, eastAxis, northAxis, heightAxis);
	
                // finally create the source geographic CRS
		airyCRS = crsFactory.createGeographicCRS(map, wgs84Datum, airyCS);
//TODO crs.toWKT() throws exceptions here (.toString() works)
		System.out.println("Geographic CRS: " + airyCRS.toString());
                
                System.out.println("Identified CRS object:");
                printIdentifierStuff(airyCRS);
                System.out.println("Identified Datum object:");
                printIdentifierStuff(((GeographicCRS)airyCRS).getDatum());
                
                // you could now use these two CRS's to create a transform between them
                // as done below in createMathTransformBetweenCRSs(). The transform can 
                // be used to convert points from lat,long to geocentric x,y,z.
                System.out.println("------------------------------------------"); 
                
	}
	
	/*void createCRSfromEPSG () {
         *
		sourceCRS = CRS.decode("EPSG:4978"); //WGS84 geocentrique
		targetCRS = CRS.decode("EPSG:4979"); //WGS84 geographique 3D


		 MathTransform math =CRS.transform( sourceCRS, targetCRS );
	     DirectPosition pt1 = new GeneralDirectPosition(new Double(x),new Double(y) ,new Double(z) );        
	     math.transform( pt1, null );
	}*/
	
	
	/**
	 * Creates a low level "operation" by hand. This is essentially just an equation (with some parameters)
	 * used to transform input to output points.
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 */
	void createAndUseMathTransform() throws FactoryException, MismatchedDimensionException, TransformException {
		System.out.println("------------------------------------------"); 
		System.out.println("Creating a math transform by hand:");
		
		ParameterValueGroup params = mtFactory.getDefaultParameters("Hotine_Oblique_Mercator");
		params.parameter("semi_major").setValue(6377298.556);
		params.parameter("semi_minor").setValue(6356097.5503009);
		params.parameter("longitude_of_center").setValue(115.0);
		params.parameter("latitude_of_center").setValue(4.0);
		params.parameter("azimuth").setValue(53.315820472222200);
		params.parameter("rectified_grid_angle").setValue(53.130102361111100);
		params.parameter("scale_factor").setValue(0.99984);
		params.parameter("false_easting").setValue(0.0);
		params.parameter("false_northing").setValue(0.0);
		MathTransform trans = mtFactory.createParameterizedTransform(params);
		System.out.println("Math Transform: " + trans.toWKT());
		
		//transform some points
		DirectPosition pt = new GeneralDirectPosition(120.0,6.0);        
		System.out.println("Input point: " + pt);
                pt = trans.transform(pt, null); 
                System.out.println("Output point: " + pt);
                System.out.println("Inverse of output point: " + trans.inverse().transform(pt,null));
		System.out.println("------------------------------------------"); 
	}
	
	void createMathTransformBetweenCRSs() throws Exception {
                System.out.println("------------------------------------------"); 
		System.out.println("Creating a math transform between two CRSs:");
		CoordinateOperationFactory coFactory = FactoryFinder.getCoordinateOperationFactory(null);

                // Nad 27 geographic (lat,long)
		CoordinateReferenceSystem sourceCRS =  createCRSByHand2();
                // UTM Zone 10N, WGS 84 (x,y)
		CoordinateReferenceSystem targetCRS =  createCRSByHand1();

		CoordinateOperation op = coFactory.createOperation(sourceCRS, targetCRS);
		MathTransform trans = op.getMathTransform();
		System.out.println("Math Transform: " + trans.toWKT());
                
                //transform some points
		DirectPosition pt = new GeneralDirectPosition(45.1,-120.0);        
		System.out.println("Input point: " + pt);
                pt = trans.transform(pt, null); 
                System.out.println("Output point: " + pt);
                System.out.println("Inverse of output point: " + trans.inverse().transform(pt,null));
		System.out.println("------------------------------------------"); 
	}
        
        /*
         * An example of using a hint to turn off datum shifts
         *
         // Source CRS: Belge 1972 / Belge Lambert 72
        String sourceCode = "31300";
        // Target CRS: NTF (Paris) / Nord France
        String targetCode = "27591";
         *> Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
         *This instructs Geotools to be tolerant to missing Bursa-Wolf parameters. 
            However, you may get one kilometer error in such case if a datum shift 
            is applied without such parameters.
       > CRSAuthorityFactory crsFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", hints);
       > CoordinateReferenceSystem sourceCRS = crsFactory.createCoordinateReferenceSystem(sourceCode);
       > CoordinateReferenceSystem targetCRS = crsFactory.createCoordinateReferenceSystem(targetCode);

        CoordinateOperationFactory opFactory = 
            Factoryfinder.getCoordinateOperationFactory(hints);

        > MathTransform mt = opFactory.createCoordinateOperation(sourceCRS, targetCRS);
        */
        
        //also want to play with  the new operation authority factory (especially the nad shift case)
        void createOperationFromAuthorityCode() {
            //This is only on head, not in geotools 2.1rc
            //CoordinateOperationAuthorityFactory coaf = FactoryFinder.getCoordinateOperationAuthorityFactory("EPSG",null);
            
        }
        

        /**
         * Print out information about an identified object
         */
        void printIdentifierStuff(IdentifiedObject identObj) {
            // START SNIPPET: identifiedObject
            System.out.println("  getName().getCode() - " + identObj.getName().getCode());
            System.out.println("  getName().getAuthority() - " + identObj.getName().getAuthority());
            System.out.println("  getRemarks() - " + identObj.getRemarks());
            System.out.println("  getAliases():");
            //GenericName[]
            Iterator aliases = identObj.getAlias().iterator();
            if (! aliases.hasNext()) {
                System.out.println("    no aliases");
            } else {
                for (int i=0; aliases.hasNext(); i++) {
                    System.out.println("    alias(" + i + "): " + (GenericName) aliases.next());
                }
            }
            
            System.out.println("  getIdentifiers():");
            //Identifier[]
            Iterator idents = identObj.getIdentifiers().iterator();
            if (! idents.hasNext()) {
                System.out.println("    no extra identifiers");
            } else {
                for (int i=0; idents.hasNext(); i++) {
                    Identifier ident = (Identifier)idents.next();
                    System.out.println("    identifier(" + i + ").getCode() - " + ident.getCode());
                    System.out.println("    identifier(" + i + ").getAuthority() - " + ident.getAuthority());
                }
            }
            // END SNIPPET: identifiedObject
        }

	public static void main(String[] args) {
		new CTSTutorial();
	}
}
