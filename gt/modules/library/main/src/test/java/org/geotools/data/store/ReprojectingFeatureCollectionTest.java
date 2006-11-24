package org.geotools.data.store;

import java.util.Iterator;


import org.geotools.feature.Feature;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class ReprojectingFeatureCollectionTest extends
		FeatureCollectionWrapperTestSupport {

	CoordinateReferenceSystem target;
	GeometryCoordinateSequenceTransformer transformer; 
	
	protected void setUp() throws Exception {
		super.setUp();
	
		target = CRS.parseWKT(
			"PROJCS[\"BC_Albers\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS_1980\",6378137,298.257222101],TOWGS84[0,0,0]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Albers_Conic_Equal_Area\"],PARAMETER[\"False_Easting\",1000000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-126],PARAMETER[\"Standard_Parallel_1\",50],PARAMETER[\"Standard_Parallel_2\",58.5],PARAMETER[\"Latitude_Of_Origin\",45],UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"42102\"]]"	
		);
		
		MathTransform2D tx = (MathTransform2D) FactoryFinder.getCoordinateOperationFactory(null)
		.createOperation(crs,target).getMathTransform();
		transformer = new GeometryCoordinateSequenceTransformer();
		transformer.setMathTransform( tx );
	}
	
	public void testNormal() throws Exception {
		
		Iterator reproject = new ReprojectingFeatureCollection( delegate, target ).iterator();
		Iterator reader = delegate.iterator();
		
		while( reader.hasNext() ) {
			Feature normal = (Feature) reader.next();
			Feature reprojected = (Feature) reproject.next();
			
			Point p1 = (Point) normal.getAttribute( "defaultGeom" );
			p1 = (Point) transformer.transform( p1 );
			
			Point p2 = (Point) reprojected.getAttribute( "defaultGeom" );
			assertTrue( p1.equals( p2 ) );
			
			LineString l1 = (LineString) normal.getAttribute( "otherGeom" );
			l1 = (LineString) transformer.transform( l1 );
			
			LineString l2 = (LineString) reprojected.getAttribute( "otherGeom" );
			assertTrue( l1.equals( l2 ) );
		}
		
	}
}
