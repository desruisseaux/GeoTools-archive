package org.geotools.data.gml;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractDataStore;
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
public class GMLDataStore extends AbstractDataStore {
    
    /*
     * should not be used
     */
    private GMLDataStore(){}
    
    // contains the data repository location
    private File dir;
    
    private Map featureReaders; // un-used featureReaders
    private Map featureTypes; // cached featureTypes
    
    /**
     * Creates a dataStore for the directory specified.
     * 
     * @param dir
     */
    protected GMLDataStore(File dir){
        featureReaders = new HashMap();
        featureTypes = new HashMap();
        this.dir = dir; // this is a dir if it came from the factory
    }

    /**
     * 
     * NOTE: This returns an abbriged directory listing of .gml files ... 
     * the typenames returned may not be the gml typename. 
     * 
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        return dir.list(new GMLFileNameFilter(dir));
    }
    
    private static class GMLFileNameFilter implements FilenameFilter{
        
        /*
         * Should not be used
         */
        private GMLFileNameFilter(){}
        
        // do not want child directories ... so keep track of the parent
        private File dir;
        
        /**
         * 
         * @param dir parent directory ... to avoid nested directories
         */
        public GMLFileNameFilter(File dir){
            this.dir = dir;
        }

        /**
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File dir, String name) {
            return this.dir.equals(dir) && name.endsWith(".gml");
        }
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String arg0) throws IOException {
        if(featureTypes.get(arg0)!=null)
            return (FeatureType)featureTypes.get(arg0);
        FCBuffer fcBuffer = (FCBuffer)getFeatureSource(arg0);
        FeatureType ft = fcBuffer.getFeatureType();
        featureReaders.put(arg0,fcBuffer);
        featureTypes.put(arg0,ft);
        return ft;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(String arg0) throws IOException {
        if(featureReaders.containsKey(arg0))
            return (FeatureReader)featureReaders.remove(arg0);
        
        File f = new File(dir,arg0);
        return FCBuffer.getFeatureReader(f.toURI(),100);
    }

}
