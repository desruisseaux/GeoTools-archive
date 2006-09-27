package org.geotools.demo.introduction;

import java.io.IOException;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.styling.Style;

import junit.framework.TestCase;

public class DemoTest extends TestCase {


    DemoBase demoBase;
    
    protected void setUp() throws Exception {
        demoBase = new DemoBase();
    }
    
    public void testCreateFeatureSourceFromScratch() throws Exception {
        FeatureSource featureSource = demoBase.createFeatureSourceFromScratchNext();
        assertNotNull( featureSource );
        
        assertFalse( featureSource.getFeatures().isEmpty() );
    }
    
    public void testLoadShapefileIntoCatalog() throws IOException {
        demoBase.loadShapefileIntoCatalog();
        
        Catalog catalog = demoBase.demoData.getLocalCatalog();
        assertEquals( 1, catalog.members( null ).size() );
        
        Service service = (Service) catalog.members( null ).get( 0 );
        assertTrue( service.canResolve( ShapefileDataStore.class ) );
    }
    
    public void testLoadShapefileFeatureSource() throws IOException {
        demoBase.loadShapefileIntoCatalog();
        
        FeatureSource featureSource = demoBase.getFeatureSourceForShapefile();
        assertNotNull( featureSource );
        
        assertFalse( featureSource.getFeatures().isEmpty() );
    }
    
    public void testCreateStyleFromScratch() throws Exception {
        Style style = demoBase.createStyleFromScratch();
        assertNotNull( style );
    
        assertEquals( 1, style.getFeatureTypeStyles().length );
    }
    
    public void testCreateStyleFromFile() throws Exception {
        Style style = demoBase.createStyleFromFile();
        assertNotNull( style );
        
        assertEquals( 1, style.getFeatureTypeStyles().length ); 
    }
	
}
