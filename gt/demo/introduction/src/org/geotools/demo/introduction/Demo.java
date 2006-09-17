package org.geotools.demo.introduction;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.GeoResource;
import org.geotools.catalog.Service;
import org.geotools.catalog.defaults.DefaultCatalog;
import org.geotools.catalog.defaults.DefaultServiceFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.demo.mappane.MapViewer;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * A demo class illustrating some of the various parts of the geotools api.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class Demo {

	/**
	 * Name of the test shapefile
	 */
	static final String shapefileName = "/countries.shp";
	/**
	 * Name of the test sld for the test shapefile
	 */
	static final String shapefileSldName = "/countries.sld";
	/**
	 * Url of wfs server
	 */
	static final String wfsServerUrl = "http://www.refractions.net:8080/geoserver/wfs";
	/**
	 * Url of postgis server
	 */
	static final String postgisServerUrl = "www.refractions.net";
	static final String postgisUsername = "postgres";
	static final String postgisDatabase = "geotools";
	
	/**
	 * the catalog
	 */
	Catalog catalog;
	
	/**
	 * Creates the demo class and an underlying catalog for storing data.
	 */
	public Demo() {
		catalog = new DefaultCatalog();
	}
	
	/**
	 * @return The underlying catalog used to store data.
	 */
	public Catalog getCatalog() {
		return catalog;
	}
	
	/**
	 * Creates an instanceof {@link FeatureSource} from scratch.
	 * <p>
	 * The feature source is created by first creating a {@link MemoryDataStore}.
	 * </p>
	 * @return A feature source.
	 * 
	 * @throws IOException Any datastore or feature source creation errors.
	 */
	public FeatureSource createFeatureSourceFromScratch() throws IOException {
		 // Wikipedia gives London as:  51?? 30.4167??? N 0?? 7.65??? W 
	    // NOTE: in Gt 2.2 axis order is Long/Lat throughout; in 2.3 the CRS rules
	    Coordinate ptc = new Coordinate(0.1275d,51.507d);
	    GeometryFactory geomFac = new GeometryFactory();
	    Point ptG = geomFac.createPoint(ptc);
	    
	    /* Name Attribute */
	    String name = "London";
	    
	    /* Population Attribute */
	    Integer pop = new Integer(7500000);
	
	    /* AttributeTypes, starting with Geometry using pre-made CRS */
	    GeometryAttributeType ptGA = 
	            (GeometryAttributeType) AttributeTypeFactory.newAttributeType(
	                                                      "the_geom", 
	                                                      ptG.getClass(), 
	                                                      true, 
	                                                      1, 
	                                                      null,
	           org.geotools.referencing.crs.DefaultGeographicCRS.WGS84);    
	    AttributeType cityAT = 
	        AttributeTypeFactory.newAttributeType(
	                                      "CITYNAME", 
	                                      String.class, 
	                                      true, 
	                                      48, 
	                                      null);
	    AttributeType popAT = 
	        AttributeTypeFactory.newAttributeType(
	                                      "CITYPOP", 
	                                      Integer.class, 
	                                      true, 
	                                      48, 
	                                      null);
	    
	    
	    /* FeatureType */
	    AttributeType[] ptATs = new AttributeType[3];
        ptATs[0] = ptGA;
        ptATs[1] = cityAT;
        ptATs[2] = popAT;

        FeatureType ptFT = null;
	    try{
           ptFT = FeatureTypes.newFeatureType(ptATs, "Metropolis");
	    } 
	    catch (SchemaException schex){
	    	String msg = "SchemaException on FeatureType creation: "+ schex;
	    	new IOException( msg ).initCause( schex );
	    }
	    
	    
	    /* Feature */
	    Object [] ptElems = { ptG, name, pop };
	    
	    Feature ptF = null;
	    try {
            ptF = ptFT.create(ptElems);
	    } 
	    catch (IllegalAttributeException iaex){
	    	String msg = "IllegalAttributeException on Feature creation: " + iaex;
            throw (IOException) new IOException( msg ).initCause( iaex );
	    }
	    
	    //TODO: figure out default GEomsee above
	    // System.out.println("DefaultGeom is: "+ptF.getDefaultGeometry());
	    
	    /* DataStore and its FeatureSource */ 
	    Feature [] ptFetArray = new Feature [] {ptF};
	    MemoryDataStore memds = new MemoryDataStore();
	    memds.addFeatures(ptFetArray);
	    
        return memds.getFeatureSource("Metropolis");
	}
	
	/**
	 * Loads a shapefile service into the catalog.
	 * 
	 * @throws IOException Any I/O errors loading into the catalog.
	 */
	public void loadShapefileIntoCatalog() throws IOException {
		
		//create shapefile datastore parameters
		URL shapefileURL = getClass().getResource( shapefileName );
		Map params = new HashMap();
		params.put( ShapefileDataStoreFactory.URLP.key, shapefileURL );
		
		//load the services, there should be only one service
		DefaultServiceFinder finder = new DefaultServiceFinder( catalog );
		List services = finder.aquire( params );
		
		//add the service to the catalog
		catalog.add( (Service) services.get( 0 ) );
	}
	
	/**
	 * Loads a shapefile feature source from the catalog.
	 * <p>
	 * This method <b>must</b> be called after {@link #loadShapefileIntoCatalog()}.
	 * </p>
	 * @return The shapefile feature source.
	 * 
	 * @throws IOException Any I/O errors that occur accessing the shapefile resource.
	 */
	public FeatureSource loadShapefileFeatureSource() throws IOException {
		
		//create the uri to lookup
		URI uri = null;
		try {
			uri =  new URI( getClass().getResource( shapefileName ).toString() );
		} 
		catch ( URISyntaxException e ) {
			throw (IOException) new IOException( "Unable to create shapefile uri").initCause( e );
		}
		
		//lookup service, should be only one
		List services = catalog.find( uri, null );
		Service service = (Service) services.get( 0 );
		
		//shapefiles only contain a single resource
		List resources = service.members( null );
		GeoResource resource = (GeoResource) resources.get( 0 );
		
		return (FeatureSource) resource.resolve( FeatureSource.class, null );
	}

	/**
	 * Loads a web feature service into the catalog.
	 * 
	 * @throws IOException Any I/O errors loading into the catalog.
	 */
	public void loadWebFeatureServiceIntoCatalog() throws IOException {
	
		//create wfs datastore parameters
		URL wfsURL = new URL( wfsServerUrl );
		Map params = new HashMap();
		params.put( WFSDataStoreFactory.URL.key, wfsURL );
		
		//load the service, there should be only one
		DefaultServiceFinder finder = new DefaultServiceFinder( catalog );
		List services = finder.aquire( params );
		
		//add the service to the catalog
		catalog.add( (Service) services.get( 0 ) );
	}
	
	/**
	 * Loads all the wfs feature sources from the wfs service.
	 * <p>
	 * This method <b>must</b> be called 
	 * </p>
	 * @return
	 * @throws IOException
	 */
	public List loadWebFeatureServiceFeatureSources() throws IOException {
		
		//create the uri to lookup
		URI uri = null;
		try {
			uri =  new URI( wfsServerUrl );
		} 
		catch ( URISyntaxException e ) {
			throw (IOException) new IOException( "Unable to create wfs uri").initCause( e );
		}
		
		//lookup service, should only be one
		List services = catalog.find( uri, null );
		Service service = (Service) services.get( 0 );
		
		//wfs contains many resources
		List resources = service.members( null );
		List featureSources = new ArrayList();
		
		for ( Iterator r = resources.iterator(); r.hasNext(); ) {
			GeoResource resource = (GeoResource) r.next();
			if ( resource.canResolve( FeatureSource.class ) ) {
				FeatureSource featureSource = 
					(FeatureSource) resource.resolve( FeatureSource.class, null );
				featureSources.add( featureSource );
			}
		}
		
		return featureSources;
	}

	/**
	 * Loads a postgis database into the catalog.
	 * 
	 * @throws IOException Any I/O errors loading into the catalog.
	 */
	public void loadPostGISintoCatalog() throws IOException {
		
		//set up connection parameters
		URL postgisURL = new URL( postgisServerUrl );
		Map params = new HashMap();
		params.put( PostgisDataStoreFactory.HOST.key, postgisURL );
		params.put( PostgisDataStoreFactory.USER.key, postgisUsername );
		params.put( PostgisDataStoreFactory.DATABASE.key, postgisDatabase );
		
		//load the service, there should be only one
		DefaultServiceFinder finder = new DefaultServiceFinder( catalog );
		List services = finder.aquire( params );
		
		//add the service to the catalog
		catalog.add( (Service) services.get( 0 ) );
		
	}
	
	
	
	public Style createStyleFromFile() throws IOException {
    
		// Make the sldURL from the sldName 
        URL sldURL = MapViewer.class.getResource( shapefileSldName );
        
    	// Create the shapefile Style, uses StyleFactory and an SLD URL
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        SLDParser stylereader = null;
        try {
        	stylereader = new SLDParser(sf,sldURL);
        } 
        catch (IOException ioex){
            System.out.println("IOException on SLDfile read: " + ioex);
        }
        Style[] shpStylArr = stylereader.readXML();
        Style shpStyle = shpStylArr[0];
        
        return shpStyle;
	    
	}
	
	public Style createStyleFromScratch() {
		/* Point style from scratch */
        StyleBuilder builder = new StyleBuilder();
        Mark mark    = builder.createMark("circle", Color.RED);
        Graphic g    = builder.createGraphic(null,mark,null);
        Symbolizer s = builder.createPointSymbolizer(g);
        
        Style memStyle = builder.createStyle( s );
        return memStyle;
    }
    
}
