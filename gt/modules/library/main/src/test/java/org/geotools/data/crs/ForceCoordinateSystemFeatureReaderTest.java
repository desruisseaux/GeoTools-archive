package org.geotools.data.crs;

import junit.framework.TestCase;

import org.geotools.data.FeatureReader;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class ForceCoordinateSystemFeatureReaderTest extends TestCase {

    private static final String FEATURE_TYPE_NAME = "testType";

    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * create a datastore with 1 feature in it.
     * @param crs the CRS of the featuretype
     * @param p the point to add, should be same CRS as crs
     * @return
     * @throws Exception
     */
    private MemoryDataStore createDatastore(CoordinateReferenceSystem crs, Point p) throws Exception{
        
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(FEATURE_TYPE_NAME);
        builder.add("geom", Point.class );
        
        SimpleFeatureType ft = builder.buildFeatureType();
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(ft);
        b.setType( ft );
        b.add( p );
        
        SimpleFeature[] features=new SimpleFeature[]{
           b.buildFeature(null) 
        };
        
        return new MemoryDataStore(features);
    }
    
    public void testSameCRS() throws Exception {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GeometryFactory fac=new GeometryFactory();
        Point p = fac.createPoint(new Coordinate(10,10) );
        
        MemoryDataStore ds = createDatastore(crs, p);
        
        FeatureReader original = ds.getFeatureReader(FEATURE_TYPE_NAME);
        
        ForceCoordinateSystemFeatureReader modified = new ForceCoordinateSystemFeatureReader(ds.getFeatureReader(FEATURE_TYPE_NAME), crs);
        
        SimpleFeature f1=original.next();
        SimpleFeature f2=modified.next();
        
        assertEquals(f1,f2);
        
        assertFalse( original.hasNext() );
        assertFalse( modified.hasNext() );
        
        assertNull(modified.schema);
    }
    
    public void testDifferentCRS() throws Exception {
        CoordinateReferenceSystem srcCRS = DefaultGeographicCRS.WGS84;
        GeometryFactory fac=new GeometryFactory();
        Point p = fac.createPoint(new Coordinate(10,10) );
        
        MemoryDataStore ds = createDatastore(srcCRS, p);
        
        FeatureReader original = ds.getFeatureReader(FEATURE_TYPE_NAME);
        
        CoordinateReferenceSystem destCRS=DefaultEngineeringCRS.CARTESIAN_2D;
        ForceCoordinateSystemFeatureReader modified = new ForceCoordinateSystemFeatureReader(
                ds.getFeatureReader(FEATURE_TYPE_NAME), destCRS);
        
        SimpleFeature f1=original.next();
        SimpleFeature f2=modified.next();
        
        assertEquals(((Geometry)f1.getDefaultGeometry()).getCoordinate(),((Geometry)f2.getDefaultGeometry()).getCoordinate());
        assertFalse( f1.getFeatureType().getCRS().equals(f2.getFeatureType().getCRS()) );
        assertEquals( srcCRS, f1.getFeatureType().getCRS());
        assertEquals( destCRS, f2.getFeatureType().getCRS());
        
        assertFalse( original.hasNext() );
        assertFalse( modified.hasNext() );
        
        assertNotNull(modified.schema);
    }
    
    public void testNullDestination() throws Exception {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GeometryFactory fac=new GeometryFactory();
        Point p = fac.createPoint(new Coordinate(10,10) );
        
        MemoryDataStore ds = createDatastore(crs, p);
        
        try{
            new ForceCoordinateSystemFeatureReader(
                ds.getFeatureReader(FEATURE_TYPE_NAME), (CoordinateReferenceSystem)null);
            fail(); // should throw a nullpointer exception.
        }catch(NullPointerException e){
            // good
        }
        
    }
    
    public void testNullSource() throws Exception {
        CoordinateReferenceSystem srcCRS = null;
        GeometryFactory fac=new GeometryFactory();
        Point p = fac.createPoint(new Coordinate(10,10) );
        
        MemoryDataStore ds = createDatastore(srcCRS, p);
        
        FeatureReader original = ds.getFeatureReader(FEATURE_TYPE_NAME);
        
        CoordinateReferenceSystem destCRS=DefaultEngineeringCRS.CARTESIAN_2D;
        ForceCoordinateSystemFeatureReader modified = new ForceCoordinateSystemFeatureReader(
                ds.getFeatureReader(FEATURE_TYPE_NAME), destCRS);
        
        SimpleFeature f1=original.next();
        SimpleFeature f2=modified.next();
        
        assertEquals(((Geometry)f1.getDefaultGeometry()).getCoordinate(),((Geometry)f2.getDefaultGeometry()).getCoordinate());
        assertFalse( f2.getFeatureType().getCRS().equals(f1.getFeatureType().getCRS()) );
        assertEquals( srcCRS, f1.getFeatureType().getCRS());
        assertEquals( destCRS, f2.getFeatureType().getCRS());
        
        assertFalse( original.hasNext() );
        assertFalse( modified.hasNext() );
        
        assertNotNull(modified.schema);
    }
}
