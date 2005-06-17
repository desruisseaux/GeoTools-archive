/*
 * Geotools dependancies:
 *     main-2.1.x.jar
 * 
 * Other dependancies:
 *     geoapi-2.0.jar
 *     units-0.01.jar
 *     vecmath-1.3.jar
 * 
 */
package org.geotools.demo.referencing;

//J2SE dependancies
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

/**
 * @author rschulz
 *
 * Code for the geotools coordinate transformation services tutorial. These examples
 * cover the following topics:
 * <ul>
 *   <li>creating coordinate reference systems (CRS) by hand</li>
 *   <li>creating CRS's from well known text (WKT) strings</li>
 *   <li> </li>
 *   <li> </li>
 *   <li> </li>
 *   <li> </li> 
 * </ul>
 */
public class CTSTutorial {
	
	private CRSFactory crsFactory = null;
	private MathTransformFactory mtFactory = null;
	private FactoryGroup factories = null;
	
	private CoordinateReferenceSystem geocentricCRS = null;
	private CoordinateReferenceSystem airyCRS = null;
	
	CTSTutorial() {
		try {
			crsFactory = FactoryFinder.getCRSFactory(null);
			mtFactory = FactoryFinder.getMathTransformFactory(null);
			factories = new FactoryGroup();
			creatCRSFromWKT();
			createCRSByHand1();
			createCRSByHand2();
			createAndUseMathTransform();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * An example of creating a CRS from a WKT string. Additonal examples of WKT strings
	 * can be found ________________
	 * 
	 * Brief description of what a CRS is (and what it is composed of)
	 * 
	 * @throws Exception
	 */
	void creatCRSFromWKT() throws Exception {
		System.out.println("------------------------------------------"); 
		System.out.println("Creating a CRS from a WKT string:");

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
		System.out.println("  CRS: " + crs.toWKT());
		
		System.out.println("------------------------------------------"); 
	}
	
	/**
	 * 
	 * 
	 * @throws Exception
	 */
	void createCRSByHand1() throws Exception {
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
		
		// factories in the group will be created from defaults

		Map properties = Collections.singletonMap("name", "WGS 84 / UTM Zone 12N");
		ProjectedCRS projCRS = factories.createProjectedCRS(properties, geoCRS, null, parameters,
            cartCS);
    
		System.out.println("  Projected CRS: " + projCRS.toWKT());
		System.out.println("------------------------------------------"); 
		
	}
	
	/**
	 * Creating a custom geographic coordinate system by hand without using 
	 * any of the GT2 APIs (except FactoryFinder to get things started). 
	 * The following example creates a CRS to represent the Airy 1830 
	 * ellipsoid with the incoming data in the order of (long,lat,height)
	 * @throws FactoryException
	 */
	void createCRSByHand2() throws FactoryException {
		//CoordinateOperationFactory transformFactory = FactoryFinder.getCoordinateOperationFactory();
		DatumFactory               datumFactory     = FactoryFinder.getDatumFactory(null);
		CSFactory                  csFactory        = FactoryFinder.getCSFactory(null);
		
		Map map = new HashMap();
		map.put("name", "WGS84 Ellispoidal height");
		VerticalDatum ellipsoidVertical =
			datumFactory.createVerticalDatum(map, VerticalDatumType.ELLIPSOIDAL);
		
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
		GeodeticDatum wgs84EllipsoidHD = 
			datumFactory.createGeodeticDatum(map, wgs84Ellipsoid, greenwichMeridian);
		
		//			 Create a collection of axes for each of the coordinate systems.
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
		map.put("name", "Geodetic North axis");
		CoordinateSystemAxis northAxis = csFactory.createCoordinateSystemAxis(map, "N",AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
		
		map.clear();
		map.put("name", "Geodetic East axis");
		CoordinateSystemAxis eastAxis = csFactory.createCoordinateSystemAxis(map, "E", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
		
		map.clear();
		map.put("name", "Geodetic Height axis");
		CoordinateSystemAxis heightAxis = csFactory.createCoordinateSystemAxis(map, "Up", AxisDirection.UP, SI.METER);
		
		//			 Now, the coordinate reference system that we'd use for output - eg to a 3D renderer
		map.clear();
		map.put("name", "Rendered Cartesian CS");

		CartesianCS worldCS = csFactory.createCartesianCS(map, xAxis, yAxis, zAxis);
		
		map.clear();
		map.put("name", "Output Cartesian CS");
		
		geocentricCRS = crsFactory.createGeocentricCRS(map, wgs84EllipsoidHD, worldCS);
		System.out.println("Geocentric CRS: " + geocentricCRS.toWKT());
		
		//			 Now set up the CRS representing the Airy 1830 ellipsoid
		map.clear();
		map.put("name", "Airy 1830");
		Ellipsoid airyEllipse = 
			datumFactory.createFlattenedSphere(map, 6377563.396, 299.3249646, SI.METER);
		
		map.clear();
		map.put("name", "<long>,<lat> Airy 1830 geodetic");
		EllipsoidalCS airyCS = csFactory.createEllipsoidalCS(map, eastAxis, northAxis, heightAxis);
	
		airyCRS = crsFactory.createGeographicCRS(map, wgs84EllipsoidHD, airyCS);
//TODO crs.toWKT() throws exceptions here (.toString() works)
		System.out.println("Geographic CRS: " + airyCRS.toString());

//			 Finally, create the projection from the Airy datum to the WGS84 ellipsoid CRS.
			//CoordinateOperation cst = transformFactory.createOperation(airyCRS, outputCRS);
			//MathTransform mt = cst.getMathTrahsnform();

//			 now go convert those points


	}
	
	/*void createCRSfromEPSG () {
		sourceCRS = CRS.decode("EPSG:4978"); //WGS84 geocentrique
		targetCRS = CRS.decode("EPSG:4979"); //WGS84 geographique 3D


		 MathTransform math =CRS.transform( sourceCRS, targetCRS );
	     DirectPosition pt1 = new GeneralDirectPosition(new Double(x),new Double(y) ,new Double(z) );        
	     math.transform( pt1, null );
	}*/
	
	
	/**
	 * A low level "operation", essentially just an equation (with some parameters)
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
	
	void createMathTransformBetweenCRSs() {
		CoordinateOperationFactory coFactory = FactoryFinder.getCoordinateOperationFactory(null);

		//CoordinateReferenceSystem sourceCRS = ...
		//CoordinateReferenceSystem targetCRS = ...

		//CoordinateOperation op = coFactory.createOperation(sourceCRS, targetCRS);
		//MathTransform trans = op.getMathTransform();
		
	}
	
	void identifiedObject() {
		//I have a "scratch" class that does this somewhere...
		
	}
        
   /**
     * Print out information about the identified object
     */
   /* void printIdentifierStuff(IdentifiedObject identObj) {
        System.out.println ("  getName(): code, authority: " + identObj.getName().getCode() + ", " + identObj.getName().getAuthority());
        
        GenericName[] aliases = identObj.getAlias();
        if (aliases.length == 0) {
               System.out.println("    no aliases");
        } else {
            for (int i=0; i<aliases.length; i++) {
                System.out.println("    Alias (" + i + "): " + aliases[i]);
            }
        }
        
        Identifier[] idents = identObj.getIdentifiers();
        if (idents.length == 0) {
            System.out.println("    no extra identifiers");
        } else {
             for (int i=0; i<idents.length; i++) {
                 System.out.println("    Extra Identifier (" + i + "): code, authority: " + idents[i].getCode() + ", " + idents[i].getAuthority());
             }
        }
    }*/

	public static void main(String[] args) {
		new CTSTutorial();
	}
}
