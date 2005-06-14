package org.geotools.renderer.shape;

import junit.framework.TestCase;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryFilter;
import org.geotools.map.DefaultMapContext;
import org.geotools.renderer.lite.RenderListener;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Envelope;

public class QueryTest extends TestCase {
	private static final boolean INTERACTIVE = true;
	private FeatureSource source;
	private Style style;
	private DefaultMapContext map;
	Envelope bounds=new Envelope(-5, 5, -5, 5);
	protected void setUp() throws Exception {
		source=TestUtilites.getDataStore("theme1.shp").getFeatureSource();
		style=TestUtilites.createTestStyle("theme1", null);
		map=new DefaultMapContext();
		map.addLayer(source, style);
	}
	
	public void testFidFilter() throws Exception{
		Query q=new DefaultQuery("theme1", TestUtilites.filterFactory.createFidFilter("theme1.2"));
		map.getLayer(0).setQuery(q);
		ShapefileRenderer renderer=new ShapefileRenderer(map);
		renderer.addRenderListener(new RenderListener(){

			public void featureRenderer(Feature feature) {
				assertEquals("theme1.2", feature.getID());
			}

			public void errorOccurred(Exception e) {
				throw new RuntimeException(e);
			}
			
		});
		TestUtilites.INTERACTIVE=INTERACTIVE;
		TestUtilites.showRender("testFidFilter", renderer, 1000, bounds, 1);
	}

	
	public void dtestBBOXFilter() throws Exception{
		BBoxExpression bbox = TestUtilites.filterFactory.createBBoxExpression(new Envelope(-4,-2,0,-3));
		String geom = source.getSchema().getDefaultGeometry().getName();
		AttributeExpression geomExpr = TestUtilites.filterFactory.createAttributeExpression(source.getSchema(), geom);

		
		
		GeometryFilter filter = TestUtilites.filterFactory.createGeometryFilter(Filter.GEOMETRY_INTERSECTS);
		filter.addRightGeometry(bbox);
		filter.addLeftGeometry(geomExpr);
		
		Query q=new DefaultQuery("theme1", filter);
		map.getLayer(0).setQuery(q);
		ShapefileRenderer renderer=new ShapefileRenderer(map);
		renderer.addRenderListener(new RenderListener(){

			public void featureRenderer(Feature feature) {
				assertEquals("theme1.1", feature.getID());
			}

			public void errorOccurred(Exception e) {
				throw new RuntimeException(e);
			}
			
		});
		TestUtilites.showRender("testFidFilter", renderer, 1000, bounds, 1);
	}
}
