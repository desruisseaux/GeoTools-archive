/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.shapefile;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 *
 * @source $URL$
 * @version $Id$
 * @author  Ian Schneider
 */
public class ShapefileDataStoreTest extends TestCaseSupport {
    
    final static String STATE_POP = "shapes/statepop.shp";
    final static String STREAM    = "shapes/stream.shp";
    final static String DANISH    = "shapes/danish_point.shp";
    final static String CHINESE   = "shapes/chinese_poly.shp";
    
    public ShapefileDataStoreTest(String testName) throws IOException {
        super(testName);
    }
    
    protected FeatureCollection loadFeatures(String resource, Query query) throws Exception {
        assertNotNull( query );
        
        URL url = TestData.url(resource);
        ShapefileDataStore s = new ShapefileDataStore(url);
        FeatureSource fs = s.getFeatureSource(s.getTypeNames()[0]);
        return fs.getFeatures(query);
    }
    protected FeatureCollection loadLocalFeaturesM2() throws IOException {
        String target = "jar:file:/C:/Documents and Settings/jgarnett/.m2/repository/org/geotools/gt2-sample-data/2.4-SNAPSHOT/gt2-sample-data-2.4-SNAPSHOT.jar!/org/geotools/test-data/shapes/statepop.shp";
        URL url = new URL( target );
        ShapefileDataStore s = new ShapefileDataStore(url);
        FeatureSource fs = s.getFeatureSource(s.getTypeNames()[0]);
        return fs.getFeatures();
    }
    protected FeatureCollection loadFeatures(String resource, Charset charset, Query q) throws Exception {
        if (q == null) q = new DefaultQuery();
        URL url = TestData.url(resource);
        ShapefileDataStore s = new ShapefileDataStore(url, false, charset);
        FeatureSource fs = s.getFeatureSource(s.getTypeNames()[0]);
        return fs.getFeatures(q);
    }
    
    protected FeatureCollection loadFeatures(ShapefileDataStore s) throws Exception {
        return s.getFeatureSource(s.getTypeNames()[0]).getFeatures();
    }
    
    public void testLoad() throws Exception {
        loadFeatures(STATE_POP, Query.ALL );
    }
    
    public void testLoadDanishChars() throws Exception {
        FeatureCollection fc = loadFeatures(DANISH, Query.ALL);
        SimpleFeature first = fc.features().next();
        
        // Charl�tte, if you can read it with your OS charset
        assertEquals("Charl\u00F8tte", first.getAttribute("TEKST1"));
    }
    
    public void testLoadChineseChars() throws Exception {
        FeatureCollection fc = loadFeatures(CHINESE, Charset.forName("GB18030"), null);
        SimpleFeature first = fc.features().next();
        String s = (String) first.getAttribute("NAME");
        assertEquals("\u9ed1\u9f99\u6c5f\u7701", first.getAttribute("NAME"));
    }
    
    public void testNamespace() throws Exception {
		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		Map map = new HashMap();
		
		URI namespace = new URI("http://jesse.com");
		
		map.put(ShapefileDataStoreFactory.NAMESPACEP.key, namespace);
		map.put(ShapefileDataStoreFactory.URLP.key, TestData.url(STATE_POP));
		
		ShapefileDataStore store = factory.createDataStore(map);
		String typeName = STATE_POP.substring(STATE_POP.lastIndexOf('/')+1,
                STATE_POP.lastIndexOf('.'));
		FeatureType schema = store.getSchema(); 
		assertEquals(namespace.toString(), schema.getName().getNamespaceURI());
	}
    
    public void testSchema() throws Exception {
        URL url = TestData.url(STATE_POP);
        ShapefileDataStore shapeDataStore = new ShapefileDataStore(url);
        String typeName = shapeDataStore.getTypeNames()[0];
        SimpleFeatureType schema = shapeDataStore.getSchema(typeName);
        List<AttributeDescriptor> attributes = schema.getAttributes();
        assertEquals("Number of Attributes",253,attributes.size() );
    }
    
    public void testSpacesInPath() throws Exception {
        URL u = TestData.url(this, "folder with spaces/pointtest.shp");
        File f = new File(URLDecoder.decode(u.getFile(),"UTF-8"));
        assertTrue(f.exists());
        ShapefileDataStore s = new ShapefileDataStore(u);
        loadFeatures(s);
    }
    
    /**
     * Test envelope versus old DataSource
     */
    public void testEnvelope() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP, Query.ALL);
        ShapefileDataStore s = new ShapefileDataStore(TestData.url(STATE_POP));
        String typeName = s.getTypeNames()[0];
        FeatureCollection all = s.getFeatureSource( typeName ).getFeatures();
        
        assertEquals(features.getBounds(), all.getBounds() );
    }
    
    public void testLoadAndVerify() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP, Query.ALL);
        //FeatureCollection features = loadFeaturesM2();
        int count = features.size();
        
        assertTrue( "Have features", count > 0 );        
        //assertEquals("Number of Features loaded",49,features.size()); // FILE (correct value)
        //assertEquals("Number of Features loaded",3, count);           // JAR
        
        SimpleFeature firstFeature = firstFeature(features);
        SimpleFeatureType schema = firstFeature.getFeatureType();
        assertNotNull(schema.getDefaultGeometry());
        assertEquals("Number of Attributes",253, schema.getAttributeCount());
        assertEquals("Value of statename is wrong","Illinois", firstFeature.getAttribute("STATE_NAME"));
        assertEquals("Value of land area is wrong",143986.61,((Double)firstFeature.getAttribute("LAND_KM")).doubleValue(),0.001);
    }
    
    public void testLoadAndCheckParentTypeIsPolygon() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP,Query.ALL);
        SimpleFeatureType schema = firstFeature(features).getFeatureType();
        
        assertTrue( FeatureTypes.isDecendedFrom( schema, BasicFeatureTypes.POLYGON ) ); 
        assertTrue( FeatureTypes.isDecendedFrom( schema, BasicFeatureTypes.POLYGON ) );
        assertTrue( FeatureTypes.isDecendedFrom( schema, FeatureTypes.DEFAULT_NAMESPACE, "polygonFeature"));
    }
    
    public void testCreateSchema() throws Exception {
        File file = new File( "test.shp" );
        URL toURL = file.toURL();
        ShapefileDataStore ds=new ShapefileDataStore(toURL);
        ds.createSchema( DataUtilities.createType("test", "geom:MultiPolygon") );
        
        // ds = new ShapefileDataStore(toURL); this is not needed?        
        assertEquals("test", ds.getSchema().getTypeName());
        
        file.deleteOnExit();        
        file=new File("test.dbf");
        file.deleteOnExit();
        file=new File("test.shp");
        file.deleteOnExit();
        
        file=new File("test.prj");
        if( file.exists() ) file.deleteOnExit();

        file=new File("test.shx");
        if( file.exists() ) file.deleteOnExit();
    }
    public void testForceCRS() throws Exception {
        File file = new File( "test.shp" );
        URL toURL = file.toURL();
        
        ShapefileDataStore ds=new ShapefileDataStore(toURL);
        ds.createSchema( DataUtilities.createType("test", "geom:MultiPolygon") );
        FeatureType before = ds.getSchema();
        
        ds.forceSchemaCRS( CRS.decode("EPSG:3005") );
        FeatureType after = ds.getSchema();
        
        assertNotSame( before, after );        
        assertNull( "4326", before.getCRS() );
        assertEquals( "NAD83 / BC Albers", after.getCRS().getName().getCode() );
                
        file.deleteOnExit();        
        file=new File("test.dbf");
        file.deleteOnExit();
        file=new File("test.shp");
        file.deleteOnExit();
        
        file=new File("test.prj");
        
        if( file.exists() ) file.deleteOnExit();
        
        file=new File("test.shx");
        if( file.exists() ) file.deleteOnExit();
    }
    
    private ShapefileDataStore createDataStore(File f) throws Exception {
        FeatureCollection fc = createFeatureCollection();
        ShapefileDataStore sds = new ShapefileDataStore(f.toURL());
        writeFeatures(sds, fc);
        return sds;
    }
    
    private ShapefileDataStore createDataStore() throws Exception {
        return createDataStore(getTempFile());
    }
    
    /**
     * Create a set of features, then remove every other one, updating the 
     * remaining. Test for removal and proper update after reloading...
     */
    public void testUpdating() throws Throwable {
        try {
            ShapefileDataStore sds = createDataStore();
            loadFeatures(sds);
            
            FeatureWriter writer = null;
            try {
                writer = sds.getFeatureWriter(sds.getTypeNames()[0],Filter.INCLUDE, Transaction.AUTO_COMMIT);
                while (writer.hasNext()) {
                    SimpleFeature feat = writer.next();
                    Byte b = (Byte) feat.getAttribute(1);
                    if (b.byteValue() % 2 == 0) {
                        writer.remove();
                    } else {
                        feat.setAttribute(1,new Byte( (byte) -1));
                    }
                }
            } finally {
                if( writer != null ) writer.close();
            }
            FeatureCollection fc = loadFeatures(sds);
            
            assertEquals(10,fc.size());
            for (FeatureIterator i = fc.features();i.hasNext();) {
                assertEquals(-1, ((Byte) i.next().getAttribute(1)).byteValue());
            }
        }
        catch (Throwable t ){
            if( System.getProperty("os.name").startsWith("Windows")) {
                System.out.println("Ignore "+t+" because you are on windows");
                return;
            }        
            else {
                throw t;
            }
        }
    }
    
    /**
     * Create a test file, then continue removing the first entry until
     * there are no features left.
     */ 
    public void testRemoveFromFrontAndClose() throws Throwable {
        try {
            ShapefileDataStore sds = createDataStore();
            
            int idx = loadFeatures(sds).size();
            
            while (idx > 0) {
                FeatureWriter writer = null;
                
                try {
                    writer = sds.getFeatureWriter(sds.getTypeNames()[0],Filter.INCLUDE, Transaction.AUTO_COMMIT);                
                    writer.next();
                    writer.remove();
                }
                finally {
                    if( writer != null ){
                        writer.close();
                        writer = null;
                    }
                }
                assertEquals(--idx,loadFeatures(sds).size());
            }
        }
        catch (Throwable t ){
            if( System.getProperty("os.name").startsWith("Windows")){
                System.out.println("Ignore "+t+" because you are on windows");
                return;
            }        
            else {
                throw t;
            }
        }
        
    }
    
    /**
     * Create a test file, then continue removing the last entry until
     * there are no features left.
     */ 
    public void testRemoveFromBackAndClose() throws Throwable {
        try {
            ShapefileDataStore sds = createDataStore();
            
            int idx = loadFeatures(sds).size();
            
            while (idx > 0) {
                FeatureWriter writer = null;
                try {
                    writer =  sds.getFeatureWriter(sds.getTypeNames()[0],Filter.INCLUDE, Transaction.AUTO_COMMIT);
                    while (writer.hasNext()) {
                        writer.next();
                    }
                    writer.remove();
                }
                finally {
                    if( writer != null ){
                        writer.close();
                        writer = null;
                    }
                }
                assertEquals(--idx,loadFeatures(sds).size());
            }
        }
        catch (Throwable t ){
            if( System.getProperty("os.name").startsWith("Windows")){
                System.out.println("Ignore "+t+" because you are on windows");
                return;
            }        
            else {
                throw t;
            }
        }
    }

    public void testWriteShapefileWithNoRecords() throws Exception {
        SimpleFeatureType featureType = DataUtilities.createType("whatever", "a:Polygon,b:String" );
        
        File tempFile = getTempFile();
        ShapefileDataStore shapefileDataStore = new ShapefileDataStore(tempFile.toURL());
        shapefileDataStore.createSchema(featureType);
        
        FeatureWriter featureWriter = shapefileDataStore.getFeatureWriter(shapefileDataStore.getTypeNames()[0], Transaction.AUTO_COMMIT);

        //don't add any features to the data store....

        //this should create a shapefile with no records. Not sure about the semantics of this,
        //but it's meant to be used in the context of a FeatureCollection iteration,
        //where the FeatureCollection has nothing in it.
        featureWriter.close();
    }

    /**
     * Creates feature collection with all the stuff we care about from simple types, to Geometry and date.
     * <p>
     * As we care about supporting more stuff please add on to the end of this list...
     * @return FeatureCollection For use in testing.
     * @throws Exception
     */
    private FeatureCollection createFeatureCollection() throws Exception {
        SimpleFeatureType featureType = createExampleSchema();
        SimpleFeatureBuilder build = new SimpleFeatureBuilder(featureType);
        
        FeatureCollection features = FeatureCollections.newCollection();
        for (int i = 0, ii = 20; i < ii; i++) {
            
            build.add(new GeometryFactory().createPoint(new Coordinate(1,-1)));
            build.add(new Byte( (byte) i ) );
            build.add(new Short( (short) i));
            build.add(new Double( i ));
            build.add(new Float( i ));
            build.add(new String( i + " " ));
            build.add(new Date( i ));
            build.add(new Boolean( true ));
            build.add(new Integer(22));
            build.add(new Long(1234567890123456789L));
            build.add(new BigDecimal(new BigInteger("12345678901234567890123456789"), 2));
            build.add(new BigInteger("12345678901234567890123456789"));
            
            SimpleFeature feature = build.buildFeature(null); 
            features.add( feature );
        }
        return features;
    }

    private SimpleFeatureType createExampleSchema() {
        SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
        build.setName("junk");
        build.add("a", Point.class);
        build.add("b",Byte.class);
        build.add("c",Short.class);
        build.add("d",Double.class);
        build.add("e",Float.class);
        build.add("f",String.class);
        build.add("g",Date.class);
        build.add("h",Boolean.class);
        build.add("i",Number.class);
        build.add("j",Long.class);
        build.add("k",BigDecimal.class);
        build.add("l",BigInteger.class);

        return build.buildFeatureType();
    }
    
    public void testAttributesWriting() throws Exception {
        FeatureCollection features = createFeatureCollection();
        File tmpFile = getTempFile();
        tmpFile.createNewFile();
        ShapefileDataStore s = new ShapefileDataStore(tmpFile.toURL());
        writeFeatures(s, features);
    }
    
    public void testWriteReadBigNumbers() throws Exception {
    	// create feature type
        SimpleFeatureType type = DataUtilities.createType("junk", "a:Point,b:java.math.BigDecimal,c:java.math.BigInteger");
        FeatureCollection features = FeatureCollections.newCollection();
		
        BigInteger bigInteger = new BigInteger("1234567890123456789");
		BigDecimal bigDecimal = new BigDecimal( bigInteger, 2);
		
		SimpleFeatureBuilder build = new SimpleFeatureBuilder(type);
		build.add( new GeometryFactory().createPoint(new Coordinate(1, -1)) );
		build.add( bigDecimal );
		build.add( bigInteger );
		
		SimpleFeature feature = build.buildFeature(null);
		features.add( feature );
		
		// store features
		File tmpFile = getTempFile();
		tmpFile.createNewFile();
		ShapefileDataStore s = new ShapefileDataStore(tmpFile.toURL());
		writeFeatures(s, features);

		// read them back
		FeatureReader reader = s.getFeatureReader("junk");
		try {
    		SimpleFeature f = reader.next();
    		
    		assertEquals( "big decimal", bigDecimal.doubleValue(), ((Number)f.getAttribute("b")).doubleValue(), 0.00001 );
            assertEquals( "big integer", bigInteger.longValue(), ((Number)f.getAttribute("c")).longValue(), 0.00001 );
		}
		finally {
		    reader.close();
		}
	}
    
    public void testGeometriesWriting() throws Exception {
        
        
        String[] wktResources = new String[] {
            "point",
            "multipoint",
            "line",
            "multiline",
            "polygon",
            "multipolygon"
        };
        
        PrecisionModel pm = new PrecisionModel();
        for (int i = 0; i < wktResources.length; i++) {
            Geometry geom = readGeometry(wktResources[i]);
            String testName = wktResources[i];
            try {
                
                 runWriteReadTest(geom,false);
                make3D(geom);
                testName += "3d";
                runWriteReadTest(geom,true);
            } catch (Throwable e) {
            	e.printStackTrace();
                throw new Exception("Error in " + testName,e);
            }
            
        }
        
    }
    
    private void make3D(Geometry g) {
        Coordinate[] c = g.getCoordinates();
        for (int i = 0, ii = c.length; i < ii; i++) {
            c[i].z = 42 + i;
        }
    }
    
    private void writeFeatures(ShapefileDataStore s,FeatureCollection fc) throws Exception {
        s.createSchema(fc.features().next().getFeatureType());
        FeatureWriter fw = s.getFeatureWriter(s.getTypeNames()[0],Transaction.AUTO_COMMIT);
        FeatureIterator it = fc.features();
        while (it.hasNext()) {
            SimpleFeature feature = it.next();
            SimpleFeature newFeature = fw.next();
            newFeature.setAttributes( feature.getAttributes() );
            
            fw.write();
        }
        fw.close();
    }
    
    private void runWriteReadTest(Geometry geom,boolean d3) throws Exception {
        // make features
        
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("Junk");
        ftb.add("a", geom.getClass());
        SimpleFeatureType type = ftb.buildFeatureType();

        FeatureCollection features = FeatureCollections.newCollection();
        SimpleFeatureBuilder  build = new SimpleFeatureBuilder(type);        
        for (int i = 0, ii = 20; i < ii; i++) {
            build.set(0, (Geometry) geom.clone() );
            SimpleFeature feature = build.buildFeature( null );
            
            features.add( feature );
        }
        
        // set up file
        File tmpFile = getTempFile();
        tmpFile.delete();
        
        // write features
        ShapefileDataStore shapeDataStore = new ShapefileDataStore(tmpFile.toURL());
        shapeDataStore.createSchema(type);
        writeFeatures(shapeDataStore, features);

        // read features
        shapeDataStore = new ShapefileDataStore(tmpFile.toURL());
        FeatureCollection fc = loadFeatures(shapeDataStore);
        FeatureIterator fci = fc.features();
        // verify
        while (fci.hasNext()) {
            SimpleFeature f = fci.next();
            Geometry fromShape = (Geometry) f.getDefaultGeometry();
            
            if (fromShape instanceof GeometryCollection) {
                if ( ! (geom instanceof GeometryCollection) ) {
                    fromShape = ((GeometryCollection)fromShape).getGeometryN(0);
                }
            }
            try {
                Coordinate[] c1 = geom.getCoordinates();
                Coordinate[] c2 = fromShape.getCoordinates();
                for (int cc = 0, ccc = c1.length; cc < ccc; cc++) {
                    if (d3)
                        assertTrue(c1[cc].equals3D(c2[cc]));
                    else
                        assertTrue(c1[cc].equals2D(c2[cc]));
                }
            } catch (Throwable t) {
                fail("Bogus : " + Arrays.asList(geom.getCoordinates()) + " : " + Arrays.asList(fromShape.getCoordinates()));
            }
        }
        tmpFile.delete();
    }
    
    public void testGetCount() throws Exception {
        assertTrue(copyShapefiles(STREAM).canRead()); // The following test seems to fail in the URL point into the JAR file.
        ShapefileDataStore store=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(TestData.url(this, STREAM));
        int count = 0;
        FeatureReader reader = store.getFeatureReader();
        try {
            while (reader.hasNext()) {
                count++;
                reader.next();
            }
            assertEquals(count, store.getCount(Query.ALL));
        } finally {
            reader.close();
        }
    }
    /**
     * Checks if feature reading optimizations still allow to execute the queries or not
     * @throws Exception
     */
    public void testGetReaderOptimizations() throws Exception {
    	URL url = TestData.url(STATE_POP);
        ShapefileDataStore s = new ShapefileDataStore(url);
        
        // attributes other than geometry can be ignored here
        Query query = new DefaultQuery(s.getSchema().getTypeName(), Filter.INCLUDE, new String[] {"the_geom"});
        FeatureReader reader = s.getFeatureReader(s.getSchema().getTypeName(), query);
        assertEquals(1, reader.getFeatureType().getAttributeCount());
        assertEquals("the_geom", reader.getFeatureType().getAttribute(0).getLocalName());
        
        // here too, the filter is using the geometry only
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFactory gc = new GeometryFactory();
        LinearRing ring = gc.createLinearRing(new Coordinate[] {new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,10), new Coordinate(0,10), new Coordinate(0,0)});
        Polygon polygon = gc.createPolygon(ring, null);
        GeometryFilter gf = ff.createGeometryFilter(Filter.GEOMETRY_BBOX);
        gf.addLeftGeometry(ff.createAttributeExpression("the_geom"));
        gf.addRightGeometry(ff.createLiteralExpression(polygon));
        query = new DefaultQuery(s.getSchema().getTypeName(), gf, new String[] {"the_geom"});
        reader = s.getFeatureReader( s.getSchema().getTypeName(), query);
        assertEquals(1, reader.getFeatureType().getAttributeCount());
        assertEquals("the_geom", reader.getFeatureType().getAttribute(0).getLocalName());
        
        // here not, we need state_name in the feature type, so open the dbf file please
        CompareFilter cf = ff.createCompareFilter(Filter.COMPARE_EQUALS);
        cf.addLeftValue(ff.createAttributeExpression("STATE_NAME"));
        cf.addRightValue(ff.createLiteralExpression("Illinois"));
        query = new DefaultQuery(s.getSchema().getTypeName(), cf, new String[] {"the_geom"});
        reader = s.getFeatureReader(s.getSchema().getTypeName(), query);
        assertEquals(s.getSchema(), reader.getFeatureType());
    }

    
     /**
      * This is useful to dump a UTF16 character to an UT16 escape sequence, basically
      * the only way to represent the chars we don't have on the keyboard (such as chinese ones :))
      * @param c
      * @return
      */
     static public String charToHex(char c) {
        // Returns hex String representation of char c
        byte hi = (byte) (c >>> 8);
        byte lo = (byte) (c & 0xff);
        return byteToHex(hi) + byteToHex(lo);
     }
     
     static public String byteToHex(byte b) {
         // Returns hex String representation of byte b
         char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
         };
         char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
         return new String(array);
      }
}
