package org.geotools.demo.introduction;

import java.io.IOException;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.styling.Style;

import junit.framework.TestCase;

public class DemoTest extends TestCase {

	Demo demo;
	
	protected void setUp() throws Exception {
		demo = new Demo();
	}
	
	public void testCreateFeatureSourceFromScratch() throws Exception {
		FeatureSource featureSource = demo.createFeatureSourceFromScratch();
		assertNotNull( featureSource );
		
		assertFalse( featureSource.getFeatures().isEmpty() );
	}
	
	public void testLoadShapefileIntoCatalog() throws IOException {
		demo.loadShapefileIntoCatalog();
		
		Catalog catalog = demo.getCatalog();
		assertEquals( 1, catalog.members( null ).size() );
		
		Service service = (Service) catalog.members( null ).get( 0 );
		assertTrue( service.canResolve( ShapefileDataStore.class ) );
	}
	
	public void testLoadShapefileFeatureSource() throws IOException {
		demo.loadShapefileIntoCatalog();
		
		FeatureSource featureSource = demo.loadShapefileFeatureSource();
		assertNotNull( featureSource );
		
		assertFalse( featureSource.getFeatures().isEmpty() );
	}
	
	public void testCreateStyleFromScratch() throws Exception {
		Style style = demo.createStyleFromScratch();
		assertNotNull( style );
	
		assertEquals( 1, style.getFeatureTypeStyles().length );
	}
	
	public void testCreateStyleFromFile() throws Exception {
		Style style = demo.createStyleFromFile();
		assertNotNull( style );
		
		assertEquals( 1, style.getFeatureTypeStyles().length ); 
	}
	
}
