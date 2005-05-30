/*
 * StylingTest.java JUnit based test Created on April 12, 2002, 1:18 PM
 */

package org.geotools.renderer.shape;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.JTS;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RenderListener;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.UserLayer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * @author jamesm
 */
public class Rendering2DTest extends TestCase {
    /** path for test data */
    // private java.net.URL base = getClass().getResource("/testData/");
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    public static boolean INTERACTIVE=false;
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Object transform;
    static final String LINE = "linefeature";
    static final String POLYGON = "polygonfeature";
    static final String POINT = "pointfeature";
    static final String RING = "ringfeature";
    static final String COLLECTION = "collfeature";

    public Rendering2DTest( java.lang.String testName ) {
        super(testName);
    }

    public static void main( java.lang.String[] args ) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(Rendering2DTest.class);
        return suite;
    }
     
    Style loadTestStyle() throws IOException {
        StyleFactory factory = StyleFactory.createStyleFactory();

        java.net.URL surl = TestData.getResource(this, "test-sld.xml");
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();

        UserLayer layer = (UserLayer)sld.getStyledLayers()[0];

        Style style = layer.getUserStyles()[0];
        return style;
    }
    
    Style createTestStyle() throws IllegalFilterException {
    	return createTestStyle(null, null);
    }
    
    public static Style createTestStyle(String polyName, String lineName) throws IllegalFilterException {
    	if( polyName==null )
    		polyName="lakes";
    	if( lineName==null )
    		lineName="streams";
        StyleFactory sFac = StyleFactory.createStyleFactory();
        // The following is complex, and should be built from
        // an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
        polysym.setStroke(myStroke);
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        fts.setFeatureTypeName(polyName);

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[]{rule2});
        fts2.setFeatureTypeName(lineName);

        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("pointfeature");

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts, fts2, fts3});

        return style;
    }

    public void testSimpleRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        DataStore ds=getPolygons();
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env
                .getMaxY());
        showRender("testSimpleRender", renderer, 1000, env);

    }
    
    public void testSimpleLineRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getLines());
        MapContext map=renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(), map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        map.setAreaOfInterest(env);
        showRender("testSimpleLineRender", renderer, 3000, env);

    }
   
    public void testSimplePolygonRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getPolygons());
        MapContext map=renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(), map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        map.setAreaOfInterest(env);
        showRender("testSimpleLineRender", renderer, 3000, env);

    }   
    public void testSimplePolygonRenderZoomedOut() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getPolygons());
        MapContext map=renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(), map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 200000, env.getMaxX() + 200000, env.getMinY() - 200000, env
                .getMaxY() + 200000);
        map.setAreaOfInterest(env);
        showRender("testSimpleLineRender", renderer, 3000, env);

    }
    
    public void testSimpleLineRenderLargebbox() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getLines());
        MapContext map=renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(), map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - env.getWidth(), env.getMaxX() + env.getWidth(), 
        		env.getMinY() - env.getHeight(), env.getMaxY() + env.getHeight());
        map.setAreaOfInterest(env);
        showRender("testSimpleLineRender", renderer, 3000, env);

    }

    public void disabledtestSimplePointRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        DataStore ds=getPoints();
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        showRender("testSimplePointRender", renderer, 1000, env);

    }

    public void testReprojection() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        
        DataStore ds=getPolygons();
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        ShapeRenderer renderer = new ShapeRenderer(map);
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX(), bounds.getMaxX(),
                bounds.getMinY(), bounds.getMaxY());
        showRender("testReprojection", renderer, 1000, env);

        // System.in.read();

    }
    
    public void testLineReprojection() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getLines());
        Envelope env=renderer.getContext().getAreaOfInterest();
//        INTERACTIVE=true;
        showRender("testSimpleLineRender", renderer, 3000, env);

    }

    public void testNullCRSPoly() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        
        ShapefileDataStore ds=getPolygons("smallMultiPoly.shp");
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle(ds.getSchema().getTypeName(), null);

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(env.getMinX()-1, env.getMaxX()+1,
        		env.getMinY()-1, env.getMaxY()+1);
        showRender("testReprojection", renderer, 1000, env);

        // System.in.read();

    }
    
    public void testNullCRSLine() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        
        ShapeRenderer renderer=createLineRenderer(getLines("lineNoCRS.shp"));
        Envelope env=renderer.getContext().getAreaOfInterest();
//        INTERACTIVE=true;
        showRender("testSimpleLineRender", renderer, 3000, env);

    }
    public void disabledtestPointReprojection() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        GeometryFactory geomFac = new GeometryFactory(
                PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        DataStore ds=getPoints();
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        ShapeRenderer renderer = new ShapeRenderer(map);
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX() - 2000000, bounds.getMaxX() + 2000000,
                bounds.getMinY() - 2000000, bounds.getMaxY() + 2000000);
        showRender("testPointReprojection", renderer, 1000, env);

        // System.in.read();

    }

    
    /**
     * bounds may be null
     */
    public static void showRender( String testName, Object renderer, long timeOut, Envelope bounds )
            throws Exception {

        int w = 300, h = 300;
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        render(renderer, g, new Rectangle(w, h), bounds);
        if ( (System.getProperty("java.awt.headless") == null
                || !System.getProperty("java.awt.headless").equals("true"))
                && INTERACTIVE) {
            Frame frame = new Frame(testName);
            frame.addWindowListener(new WindowAdapter(){
                public void windowClosing( WindowEvent e ) {
                    e.getWindow().dispose();
                }
            });

            Panel p = new Panel(){
                /** <code>serialVersionUID</code> field */
                private static final long serialVersionUID = 1L;

                public void paint( Graphics g ) {
                    g.drawImage(image, 0, 0, this);
                }
            };
            frame.add(p);
            frame.setSize(w, h);
            frame.setVisible(true);

            Thread.sleep(timeOut);
            frame.dispose();
        }

        boolean hasData=false; //All I can seem to check reliably.

        for( int y=0; y<h;y++ ){
            for( int x=0; x<w;x++ ){
            	if( image.getRGB(x,y)!=-1 ){
            		hasData=true;
            	}
            }
        }
        assertTrue("image is blank and should not be", hasData);

    }

    /**
     * responsible for actually rendering.
     * 
     * @param g
     * @param bounds
     */
    private static void render( Object obj, Graphics g, Rectangle rect, Envelope bounds ) {
        if (obj instanceof ShapeRenderer) {
            ShapeRenderer renderer = (ShapeRenderer) obj;
            renderer.paint((Graphics2D) g, rect, bounds);
        }
    }

    
    private Style createDefQueryTestStyle() throws IllegalFilterException {
        StyleFactory sFac = StyleFactory.createStyleFactory();

        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());
        pointsym.setGeometryPropertyName("point");

        Rule rulep = sFac.createRule();
        rulep.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle ftsP = sFac.createFeatureTypeStyle();
        ftsP.setRules(new Rule[]{rulep});
        ftsP.setFeatureTypeName("querytest");

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        linesym.setGeometryPropertyName("line");

        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(3)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle ftsL = sFac.createFeatureTypeStyle();
        ftsL.setRules(new Rule[]{rule2});
        ftsL.setFeatureTypeName("querytest");

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        polysym.setGeometryPropertyName("polygon");
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle ftsPoly = sFac.createFeatureTypeStyle(new Rule[]{rule});
        // ftsPoly.setRules(new Rule[]{rule});
        ftsPoly.setFeatureTypeName("querytest");

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{ftsPoly, ftsL, ftsP});

        return style;
    }
    static ShapefileDataStore getLines() throws IOException{
    	return getLines("streams.shp");
    }
    
    public static ShapefileDataStore getLines(String filename) throws IOException{
    	URL url=TestData.getResource(Rendering2DTest.class, filename);
    	ShapefileDataStoreFactory factory=new ShapefileDataStoreFactory();
    	return (ShapefileDataStore) factory.createDataStore(url);
    }
    static ShapefileDataStore getPoints() throws IOException{
    	URL url=TestData.getResource(Rendering2DTest.class, "Points.shp");
    	ShapefileDataStoreFactory factory=new ShapefileDataStoreFactory();
    	return (ShapefileDataStore) factory.createDataStore(url);
    }
    static ShapefileDataStore getPolygons() throws IOException{
    	return getPolygons("lakes.shp");
    }
    static ShapefileDataStore getPolygons(String filename) throws IOException{
    	URL url=TestData.getResource(Rendering2DTest.class, filename);
    	ShapefileDataStoreFactory factory=new ShapefileDataStoreFactory();
    	return (ShapefileDataStore) factory.createDataStore(url);
    }
    
    public void testEnvelopePerformance() throws Exception{
    	ShapeRenderer renderer=createLineRenderer(getLines());
    	MapContext context=renderer.getContext();
    	
    	context.setAreaOfInterest(context.getLayerBounds());
    	CountingRenderListener l1=new CountingRenderListener();
    	renderer.addRenderListener(l1);
    	BufferedImage image=new BufferedImage(300,300,BufferedImage.TYPE_3BYTE_BGR);
    	renderer.paint(image.createGraphics(),new Rectangle(300,300), context.getAreaOfInterest());
    	renderer.removeRenderListener(l1);
    	
    	CountingRenderListener l2=new CountingRenderListener();
    	renderer.addRenderListener(l2);
    	Envelope old=context.getAreaOfInterest();
    	Envelope env=new Envelope(old.getMinX()+old.getWidth()/2,
    			old.getMaxX()-old.getWidth()/2,
				old.getMinY()+old.getHeight()/2,
				old.getMaxY()-old.getHeight()/2);
    	renderer.paint(image.createGraphics(),new Rectangle(300,300), env);
    	assertTrue( l1.i>l2.i);
    }

    private class CountingRenderListener implements RenderListener{

    	public int i=0;
    	
		/* (non-Javadoc)
		 * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
		 */
		public void featureRenderer(Feature feature) {
			i++;
		}

		/* (non-Javadoc)
		 * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
		 */
		public void errorOccurred(Exception e) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
	/**
	 * @return
	 * @throws Exception
	 */
	private ShapeRenderer createLineRenderer(ShapefileDataStore ds) throws Exception {

        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle(null, ds.getTypeNames()[0]);

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env
                .getMaxY());
        map.setAreaOfInterest(env);
		return renderer;
	}
    
}
