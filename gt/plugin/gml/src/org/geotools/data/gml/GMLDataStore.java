package org.geotools.data.gml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.geotools.xml.gml.FCBuffer;

/**
 * <p> 
 * GML DataStore read-only implementation. 
 * </p>
 * @author dzwiers
 *
 */
public class GMLDataStore extends AbstractFileDataStore {
    
    /*
     * should not be used
     */
    private GMLDataStore(){}
    
    // contains the data repository location
    private URI uri;

    private FCBuffer fcBuffer;
    
    /**
     * Creates a dataStore for the directory specified.
     * 
     * @param dir
     * @throws URISyntaxException
     */
    protected GMLDataStore(URL url) throws URISyntaxException{
        this.uri = new URI(url.toExternalForm()); // this is a url if it came from the factory
    }

    /**
     * 
     * NOTE: This returns an abbriged directory listing of .gml files ... 
     * the typenames returned may not be the gml typename. 
     * 
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        if(fcBuffer == null)
            fcBuffer = (FCBuffer)FCBuffer.getFeatureReader(uri,100);
        FeatureType ft = fcBuffer.getFeatureType();
        return new String[] {ft.getTypeName(),};
    }
    
       /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String arg0) throws IOException {
        return getSchema();
    }
    
    /**
  * @see org.geotools.data.DataStore#getSchema(java.lang.String)
  */
 public FeatureType getSchema() throws IOException {
     if(fcBuffer == null)
         fcBuffer = (FCBuffer)FCBuffer.getFeatureReader(uri,100);
     FeatureType ft = fcBuffer.getFeatureType();
     return ft;
 }

 /**
  * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
  */
 protected FeatureReader getFeatureReader(String arg0) throws IOException {
     return getFeatureReader();
 }
 /**
  * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
  */
 protected FeatureReader getFeatureReader() throws IOException {
     if(fcBuffer == null)
         return FCBuffer.getFeatureReader(uri,100);
     FCBuffer r = fcBuffer;
     fcBuffer = null;
     return r;
 }

}
