/*
 * ShapefileDataStoreTest.java
 *
 * Created on November 5, 2003, 2:20 PM
 */

package org.geotools.data.shapefile;


import org.geotools.data.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;

import junit.framework.*;
import org.geotools.data.shapefile.*;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.filter.Filter;

/**
 *
 * @author  Ian Schneider
 */
public class ShapefileDataStoreTest extends TestCaseSupport {
    
    final static String STATE_POP = "statepop.shp";
    final static String STREAM = "stream.shp";
    
    public ShapefileDataStoreTest(java.lang.String testName) {
        super(testName);
    }
    
    protected FeatureCollection loadFeatures(String resource,Query q) throws Exception {
        if (q == null) q = new DefaultQuery();
        URL url = getTestResource(resource);
        ShapefileDataStore s = new ShapefileDataStore(url);
        FeatureSource fs = s.getFeatureSource(s.getTypeNames()[0]);
        return fs.getFeatures(q).collection();
    }
    
    protected FeatureCollection loadFeatures(ShapefileDataStore s) throws Exception {
        return s.getFeatureSource(s.getTypeNames()[0]).getFeatures().collection();
    }
    
    public void testLoad() throws Exception {
        loadFeatures(STATE_POP,null);
    }
    
    public void testSchema() throws Exception {
        URL url = getTestResource(STATE_POP);
        ShapefileDataStore s = new ShapefileDataStore(url);
        FeatureType schema = s.getSchema(s.getTypeNames()[0]);
        AttributeType[] types = schema.getAttributeTypes();
        assertEquals("Number of Attributes",253,types.length);
    }
    
    public void testSpacesInPath() throws Exception {
        URL u = getTestResource("legacy folder/pointtest.shp");
        File f = new File(URLDecoder.decode(u.getFile(),"UTF-8"));
        assertTrue(f.exists());
        ShapefileDataStore s = new ShapefileDataStore(u);
        loadFeatures(s);
    }
    
    /**
     * Test envelope versus old DataSource
     */
    public void testEnvelope() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP, null);
        ShapefileDataStore s = new ShapefileDataStore(getTestResource(STATE_POP));
        String typeName = s.getTypeNames()[0];
        FeatureResults all = s.getFeatureSource( typeName ).getFeatures();
        
        assertEquals(features.getBounds(), all.getBounds() );
    }
    
    public void testLoadAndVerify() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP,null);
        
        assertEquals("Number of Features loaded",49,features.size());
        
        FeatureType schema = firstFeature(features).getFeatureType();
        assertNotNull(schema.getDefaultGeometry());
        assertEquals("Number of Attributes",253,schema.getAttributeTypes().length);
        assertEquals("Value of statename is wrong",firstFeature(features).getAttribute("STATE_NAME"),"Illinois");
        assertEquals("Value of land area is wrong",((Double)firstFeature(features).getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
    }
    
    public void testLoadAndCheckParentTypeIsPolygon() throws Exception {
        FeatureCollection features = loadFeatures(STATE_POP,null);
        
        
        
        FeatureType schema = firstFeature(features).getFeatureType();
        assertTrue(schema.isDescendedFrom(BasicFeatureTypes.POLYGON));
        assertTrue(schema.isDescendedFrom(null,"polygonFeature"));
    }
    
    private ShapefileDataStore createDataStore(File f) throws Exception {
        FeatureCollection fc = createFeatureCollection();
        f.createNewFile();
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
                writer = sds.getFeatureWriter(sds.getTypeNames()[0],Filter.NONE, Transaction.AUTO_COMMIT);
                while (writer.hasNext()) {
                    Feature feat = writer.next();
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
                    writer = sds.getFeatureWriter(sds.getTypeNames()[0],Filter.NONE, Transaction.AUTO_COMMIT);                
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
                    writer =  sds.getFeatureWriter(sds.getTypeNames()[0],Filter.NONE, Transaction.AUTO_COMMIT);
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
        //create a FeatureType
        AttributeType thePolygon = AttributeTypeFactory.newAttributeType("a", Polygon.class);
        AttributeType attributeB = AttributeTypeFactory.newAttributeType("b", String.class);
 
        FeatureType featureType = FeatureTypeFactory.newFeatureType(new AttributeType[]{thePolygon, attributeB}, "whatever");

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


    
    private FeatureCollection createFeatureCollection() throws Exception {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
        factory.addType(AttributeTypeFactory.newAttributeType("a",Geometry.class));
        factory.addType(AttributeTypeFactory.newAttributeType("b",Byte.class));
        factory.addType(AttributeTypeFactory.newAttributeType("c",Short.class));
        factory.addType(AttributeTypeFactory.newAttributeType("d",Double.class));
        factory.addType(AttributeTypeFactory.newAttributeType("e",Float.class));
        factory.addType(AttributeTypeFactory.newAttributeType("f",String.class));
        factory.addType(AttributeTypeFactory.newAttributeType("g",Date.class));
        factory.addType(AttributeTypeFactory.newAttributeType("h",Boolean.class));
        factory.addType(AttributeTypeFactory.newAttributeType("i",Number.class));
        factory.addType(AttributeTypeFactory.newAttributeType("j",Long.class));
        FeatureType type = factory.getFeatureType();
        FeatureCollection features = FeatureCollections.newCollection();
        for (int i = 0, ii = 20; i < ii; i++) {
            features.add(type.create(new Object[] {
                new GeometryFactory().createPoint(new Coordinate(1,-1)),
                new Byte( (byte) i ),
                new Short( (short) i),
                new Double( i ),
                new Float( i ),
                new String( i + " " ),
                new Date( i ),
                new Boolean( true ),
                new Integer(22),
                new Long(1234567890123456789L)
            }));
        }
        return features;
    }
    
    public void testAttributesWriting() throws Exception {
        FeatureCollection features = createFeatureCollection();
        File tmpFile = getTempFile();
        tmpFile.createNewFile();
        ShapefileDataStore s = new ShapefileDataStore(tmpFile.toURL());
        writeFeatures(s, features);
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
            fw.next().setAttributes(it.next().getAttributes(null));
            fw.write();
        }
        fw.close();
    }
    
    private void runWriteReadTest(Geometry geom,boolean d3) throws Exception {
        // make features
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
        factory.addType(AttributeTypeFactory.newAttributeType("a",Geometry.class));
        FeatureType type = factory.getFeatureType();
        FeatureCollection features = FeatureCollections.newCollection();
        for (int i = 0, ii = 20; i < ii; i++) {
            features.add(type.create(new Object[] {
                geom.clone()
            }));
        }
        
        // set up file
        File tmpFile = getTempFile();
        tmpFile.delete();
        
        // write features
        ShapefileDataStore s = new ShapefileDataStore(tmpFile.toURL());
        s.createSchema(type);
        writeFeatures(s, features);

        // read features
        s = new ShapefileDataStore(tmpFile.toURL());
        FeatureCollection fc = loadFeatures(s);
        FeatureIterator fci = fc.features();
        // verify
        while (fci.hasNext()) {
            Feature f = fci.next();
            Geometry fromShape = f.getDefaultGeometry();
            
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
    
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(ShapefileDataStoreTest.class));
    }
    
}
