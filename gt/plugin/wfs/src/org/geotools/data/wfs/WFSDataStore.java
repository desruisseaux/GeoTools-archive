
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Logger;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.feature.FeatureType;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStore extends AbstractDataStore{
//    private URL host = null; // getCap document url
    private WFSCapabilities capabilities = null;
    private Logger logger = Logger.getLogger("org.geotools.data.wfs"); 
 	private Boolean get = null;
 	private Boolean post = null;
 	private Authenticator auth = null;
// 	private String username = null;
// 	private String password = null;
 	
 	private WFSDataStore(){}
 	
 	WFSDataStore(URL host, Boolean get, Boolean post, String username, String password) throws SAXException{
 	    super(false); // TODO update when writeable
 	    
 	    if(username!=null && password!=null)
 	        auth = new WFSAuthenticator(username,password,host);
 	    
 	    InputStream is = getInputStream(host,auth);
 	    Object t = DocumentFactory.getInstance(is,null,logger.getLevel());
 	    if(t instanceof WFSCapabilities)
 	        capabilities = (WFSCapabilities)t;
 	    else
 	        throw new SAXException("The specified URL Should have returned a 'WFSCapabilities' object. Returned a "+(t==null?"null value.":t.getClass().getName()+" instance."));
 	   
 	    this.get = get;
 	    this.post = post;
 	}
 	
 	private static synchronized InputStream getInputStream(URL url, Authenticator auth){
 	    return null;
 	}
 	
 	static URL createGetCapabilitiesRequest(URL host){
 	    // TODO fill me in
 	    return null;
 	}

    /**
     * @see org.geotools.data.AbstractDataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static class WFSAuthenticator extends Authenticator{
        private PasswordAuthentication pa;
        private URL host; // this is the getCapabilities url
        private WFSAuthenticator(){}
        public WFSAuthenticator(String user, String pass, URL host){
            pa = new PasswordAuthentication(user,pass.toCharArray());
            this.host = host;
        }
        protected PasswordAuthentication getPasswordAuthentication(){
            // check protocol
            if(host.getProtocol()!=null && (!host.getProtocol().equals(getRequestingProtocol())))
            	return null;
            	
            // check host
            if(host.getHost()!=null && (!host.getHost().equals(getRequestingHost())))
            	return null;
            	
            // check port
            // TODO probably should add more ports here ://
            if(host.getPort()!=0 && (host.getPort()!=getRequestingPort()))
            	return null;
            	
            // TODO add more checks by someone who knows more
            	
            return pa;
        }
    }
}
