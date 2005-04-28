package org.geotools.data.gml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FileDataStoreFactorySpi;

/**
 * <p> 
 * This creates GML DataStores based for the directory provided. By 
 * convention the name of the file x.gml represents the data type x.
 * </p>
 * @author dzwiers
 */
public class GMLDataStoreFactory implements FileDataStoreFactorySpi {

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        URL url = (URL) URLP.lookUp( params ); // try early error        
        if( testURL(url)){  
            try{
                return new GMLDataStore( new URI(url.toString()) );
            }catch(URISyntaxException e){
                throw new IOException(e.toString());
            }
        }
        throw new IOException( "Provided file was not valid");       
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getDisplayName() {
        return "GML";
    }
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Read only data store for validating gml 2.x data";
    }
//    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
//        URL url = (URL) URLP.lookUp( params );
//        if( "file".equals(url.getProtocol())){
//            String parent = url.getPath();
//            String name = url.getFile();
//            return new DataSourceMetadataEnity( parent, name, "Access to GML file "+url.toString());
//        }
//        return new DataSourceMetadataEnity( url.getHost(), url.getFile(),  "Access to GML "+url.toString());
//    }
//    public static final Param DIRECTORY = new Param("directory", File.class,
//            "Directory containing gml files", true);

    public static final Param URLP = new Param("url", URL.class,
        "url to a gml file");
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { URLP, };
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        return (params != null && params.containsKey("url")
        && params.get("url") instanceof URL 
        && canProcess((URL)params.get("url")));
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getFileExtensions()
     */
    public String[] getFileExtensions() {
        return new String[] {".xml",".gml"};
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#canProcess(java.net.URL)
     */
    public boolean canProcess(URL f) {
        try {
            return testURL( f );
        } catch (IOException e) {
            return false;
        }
    }
    public boolean testURL( URL f ) throws IOException {
        if("file".equals(f.getProtocol())){        
            if(f.getFile().toUpperCase().endsWith(".XML")){
                return true;
            }
            if(f.getFile().toUpperCase().endsWith(".GML")){
                return true;            
            }
            throw new IOException("*.xml or *.gml file required");
        }
        if("http".equals(f.getProtocol())){
            URLConnection conn = f.openConnection();
            if( "text/xml".equals( conn.getContentType() )){
                return true;
            }
            if( "application/gml".equals( conn.getContentType() )){
                return true;
            }
            throw new IOException("text/xml or application/gml mime type required");
        }
        return false;
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#createDataStore(java.net.URL)
     */
    public DataStore createDataStore(URL url) throws IOException {        
        if(canProcess(url))
            try{
            return new GMLDataStore(new URI(url.toString()));

            }catch(URISyntaxException e){
                throw new IOException(e.toString());
            }
        return null;
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getTypeName(java.net.URL)
     */
    public String getTypeName(URL url) throws IOException {
        DataStore ds = createDataStore(url);
        String[] names = ds.getTypeNames();
        return ((names==null || names.length==0)?null:names[0]);
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
