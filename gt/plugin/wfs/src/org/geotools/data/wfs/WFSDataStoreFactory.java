
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceMetadataEnity;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStoreFactory implements DataStoreFactorySpi{

    // note one of the two is required
    public static final Param GET_CAPABILITIES_URL = new Param("WFSDataStoreFactory:GET_CAPABILITIES_URL",URL.class,"Represents a URL to the getCapabilities document. This URL does not need to be altered in any way. GET_CAPABILITIES_URL and SERVER_URL are mutually exclusive. One of the two is required.",false);
    public static final Param SERVER_URL = new Param("WFSDataStoreFactory:SERVER_URL",URL.class,"Represents a URL to the wfs server. This URL represents the server bases url, and should have the capability request post-pended. GET_CAPABILITIES_URL and SERVER_URL are mutually exclusive. One of the two is required.");
    
    // note may not have both, when neither is specified will prefer post
    public static final Param USE_POST = new Param("WFSDataStoreFactory:USE_POST",Boolean.class,"This specifies whether to use the POST portions of the getCapabilities document. When false the POST portion of the document should be ignored. When true, the POST portion should be used first. If this attribute is missing, and GET is specified, POST requests will be attempted when GET requests are not supported and POST requests are. If neither USE_POST or USE_GET are included, post will be prefered. USE_POST and USE_GET are muttually exclusive.",false);
    public static final Param USE_GET = new Param("WFSDataStoreFactory:USE_GET",Boolean.class,"This specifies whether to use the GET portions of the getCapabilities document. When false the GET portion of the document should be ignored. When true, the GET portion should be used first. If this attribute is missing, and POST is specified, GET requests will be attempted when POST requests are not supported and GET requests are. If neither USE_POST or USE_GET are included, post will be prefered. USE_POST and USE_GET are muttually exclusive.",false);
    
    // password stuff -- see java.net.Authentication
    // either both or neither
    public static final Param USERNAME = new Param("WFSDataStoreFactory:USERNAME",String.class,"This allows the user to specify a username. This param should not be used without the PASSWORD param.",false);
    public static final Param PASSWORD = new Param("WFSDataStoreFactory:PASSWORD",String.class,"This allows the user to specify a username. This param should not be used without the USERNAME param.",false);
    
    private Map cache = new HashMap();
    private Logger logger = Logger.getLogger("org.geotools.data.wfs"); 
    
    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        // TODO check that we can use hashcodes in this manner -- think it's ok, particularily for regular usage
        if(cache.containsKey(params))
            return (DataStore)cache.get(params);
        return createNewDataStore(params);
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createMetadata(java.util.Map)
     */
    public DataSourceMetadataEnity createMetadata(Map params) throws IOException {
        URL host = null;
        if(params.containsKey(SERVER_URL)){
            host = (URL)params.get(SERVER_URL);
        }else{
            host = ((URL)params.get(GET_CAPABILITIES_URL));
        }
        return new DataSourceMetadataEnity(host.toString(), getDisplayName(), getDescription() );
    }

    /**
     * @throws 
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        URL host = null;
        if(params.containsKey(SERVER_URL)){
            host = (URL)params.get(SERVER_URL);
            host = WFSDataStore.createGetCapabilitiesRequest(host);
        }else{
            host = ((URL)params.get(GET_CAPABILITIES_URL));
        }
        
        Boolean get,post;get = post = null;
        if(params.containsKey(USE_GET.key))
            get = (Boolean)params.get(USE_GET.key);
        if(params.containsKey(USE_POST.key))
            get = (Boolean)params.get(USE_POST.key);
        if(get != null && post != null)
            throw new IOException("Cannot define both get and post");
        
        String user,pass; user = pass = null;
        if(params.containsKey(USERNAME.key))
            user = (String)params.get(USERNAME.key);
        if(params.containsKey(PASSWORD.key))
            pass = (String)params.get(PASSWORD.key);
        if((user == null && pass!=null) || (pass == null && user!=null))
            throw new IOException("Cannot define only one of USERNAME or PASSWORD, muct define both or neither");
        
        DataStore ds = null;
        try {
            ds = new WFSDataStore(host,get,post,user,pass);
            cache.put(params,ds);
        } catch (SAXException e) {
            logger.warning(e.toString());
            throw new IOException(e.toString());
        }
        
        return ds;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "The WFSDataStore represents a connection to a Web Feature Server. This connection provides access to the Features published by the server, and the ability to perform transactions on the server (when supported / allowed).";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[]{GET_CAPABILITIES_URL,SERVER_URL,USE_POST,USE_GET,USERNAME,PASSWORD};
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        if (params == null) {
            return false;
        }
        
        // TODO check types here?
        
        // check url
        if(params.containsKey(GET_CAPABILITIES_URL.key)){
            if(params.containsKey(SERVER_URL.key))
                return false;	// cannot have both
        }else{
            if(!params.containsKey(SERVER_URL.key))
                return false;	// must have atleast one
        }
        
        // check post / get
        if(params.containsKey(USE_POST.key)){
            if(params.containsKey(USE_GET.key))
                return false;	// cannot have both
        } // may have neither
        
        // check password / username
        if(params.containsKey(USERNAME.key)){
            if(!params.containsKey(PASSWORD.key))
                return false;	// must have both
        }else{
            if(params.containsKey(PASSWORD.key))
                return false;	// must have both
        }
        return true;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDisplayName()
     */
    public String getDisplayName() {
        return "WFSDataStore";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }
}
