
package org.geotools.data.wfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ows.FeatureSetDescription;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.gml.GMLComplexTypes;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.wfs.WFSSchema;
import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStore extends AbstractDataStore{

    private WFSCapabilities capabilities = null;
    private static Logger logger = Logger.getLogger("org.geotools.data.wfs"); 
 	
    private int protos = 0;
 	private static final int POST_FIRST = 1;
 	private static final int GET_FIRST = 2;
 	private static final int POST_OK = 4;
 	private static final int GET_OK = 8;
 	
 	private Authenticator auth = null;
 	
 	private int bufferSize = 10;
 	private int timeout = 1000;
 	
 	private WFSDataStore(){}
 	
 	WFSDataStore(URL host, Boolean get, Boolean post, String username, String password, int timeout, int buffer) throws SAXException, IOException{
 	    super(false); // TODO update when writeable
 	    logger.setLevel(Level.WARNING);
 	    
 	    // TODO find a better way of adding functionality to the factory ... perhaps putting in your own RootHandler?
 	    new WFSSchemaFactory();
 	    
 	    if(username!=null && password!=null)
 	        auth = new WFSAuthenticator(username,password,host);
 	    
 	    // TODO support using POST for getCapabilities
 	    HttpURLConnection hc = (HttpURLConnection)host.openConnection();
 	    InputStream is = getInputStream(hc,auth);
 	    Object t = DocumentFactory.getInstance(is,null,logger.getLevel());
 	    if(t instanceof WFSCapabilities)
 	        capabilities = (WFSCapabilities)t;
 	    else
 	        throw new SAXException("The specified URL Should have returned a 'WFSCapabilities' object. Returned a "+(t==null?"null value.":t.getClass().getName()+" instance."));
 	   
 	    if(get == null){
 	        protos = GET_OK;
 	    }else{
 	        protos = get.booleanValue()?GET_FIRST+GET_OK:0;
 	    }
 	    if(post == null){
 	        protos = protos | POST_OK;
 	    }else{
 	        protos = post.booleanValue()?(POST_FIRST+POST_OK):protos|protos;
 	    }
 	    this.timeout = timeout;
 	    this.bufferSize = buffer;
 	}
 	
 	private static InputStream getInputStream(HttpURLConnection url, Authenticator auth) throws IOException{
 	   // TODO ensure that we can sync using the class loader and not have concurent thread issues
 	   //
 	   // should be ok, as we would only be playing with the classloader's allocated space
 	   InputStream result = null;
 	   synchronized(Authenticator.class){
 	      Authenticator.setDefault(auth);

 	      result = url.getInputStream();
 	      url.connect();
 	      
 	      Authenticator.setDefault(null);
 	   }
 	   return new BufferedInputStream(result);
 	}
 	
 	private static OutputStream getOutputStream(HttpURLConnection url, Authenticator auth) throws IOException{
 	   // TODO ensure that we can sync using the class loader and not have concurent thread issues
 	   //
 	   // should be ok, as we would only be playing with the classloader's allocated space
 	   OutputStream result = null;
 	   synchronized(Authenticator.class){
 	      Authenticator.setDefault(auth);
 	      url.setDoOutput(true);
// 	      url.connect();
 	      result = url.getOutputStream();
 	      
 	      Authenticator.setDefault(null);
 	   }
 	   return new BufferedOutputStream(result);
 	}
 	
 	static URL createGetCapabilitiesRequest(URL host){
 	    if(host == null)
 	        return null;
 	    String url = host.toString();
 	    if(host.getQuery() == null){
 	        url += "?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetCapabilities";
 	    }else{
 	        String t = host.getQuery().toUpperCase();
 	        if(t.indexOf("SERVICE")==-1){
 	           url += "&SERVICE=WFS";
 	        }
 	        if(t.indexOf("VERSION")==-1){
 	           url += "&VERSION=1.0.0";
 	        }
 	        if(t.indexOf("REQUEST")==-1){
 	           url += "&REQUEST=GetCapabilities";
 	        }
 	    }
 	    try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.warning(e.toString());
            return host;
        }
 	}

    /**
     * @see org.geotools.data.AbstractDataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        List l = capabilities.getFeatureTypes();
        String[] result = new String[l.size()];
        for(int i=0;i<l.size();i++){
            result[i] = ((FeatureSetDescription)l.get(i)).getName();
        }
        return result;
    }

    private Map featureTypeCache = new HashMap();
    /**
     * @throws IOException
     * @throws 
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        if(featureTypeCache.containsKey(typeName))
            return (FeatureType)featureTypeCache.get(typeName);

        FeatureType t = null;
        if((protos & POST_FIRST) == POST_FIRST && t == null){
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if((protos & GET_FIRST) == GET_FIRST && t == null)
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        
        if((protos & POST_OK) == POST_OK && t == null)
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }

        if((protos & GET_OK) == GET_OK && t == null)
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }

        if(t!=null)
            featureTypeCache.put(typeName,t);
        return t;
    }
    
    private FeatureType getSchemaGet(String typeName) throws SAXException, IOException {
        URL getUrl = capabilities.getDescribeFeatureType().getGet();
//System.out.println("getSchemaGet -- get "+getUrl);
		if(getUrl == null)
		    return null;
        
        String query = getUrl.getQuery().toUpperCase();
        String url = getUrl.toString();
        if(query == null){
            url += "?SERVICE=WFS";
        }else{
            if(query.indexOf("SERVICE")==-1){
                url += "&SERVICE=WFS";
 	        }
        }
        if(query.indexOf("VERSION")==-1){
            url += "&VERSION=1.0.0";
	    }
        if(query.indexOf("REQUEST")==-1){
            url += "&REQUEST=DescribeFeatureType";
	    }
        url += "&TYPENAME="+typeName;
        
        getUrl = new URL(url);
//System.out.println(getUrl);
        HttpURLConnection hc = (HttpURLConnection)getUrl.openConnection();
        hc.setRequestMethod("GET");

 	    InputStream is = getInputStream(hc,auth);
        Schema schema = WFSSchemaFactory.getInstance(null,is);
        Element[] elements = schema.getElements();
        Element element = null;
        
        String ttname = typeName.substring(typeName.indexOf(":")+1);
        for(int i = 0;i<elements.length && element == null;i++)
            // HACK -- namspace related -- should be checking ns as opposed to removing prefix
            if(typeName.equals(elements[i].getName()) || ttname.equals(elements[i].getName()))
                element = elements[i];
        
        if(element == null)
            return null;
        FeatureType ft = GMLComplexTypes.createFeatureType(element);
        return ft;
    }
    
    private FeatureType getSchemaPost(String typeName) throws IOException, SAXException {
        URL postUrl = capabilities.getDescribeFeatureType().getPost();
//System.out.println("getSchemaPost -- post "+postUrl);
		if(postUrl == null)
		    return null;
//System.out.println(postUrl);
        
        HttpURLConnection hc = (HttpURLConnection)postUrl.openConnection();
        hc.setRequestMethod("POST");
	    hc.setDoInput(true);
        OutputStream os = getOutputStream(hc,auth);
        // write request
        
        Writer w = new OutputStreamWriter(os);
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[1]); // DescribeFeatureType
        try{
            DocumentWriter.writeDocument(new String[]{typeName},WFSSchema.getInstance(),w,hints);
        }catch(OperationNotSupportedException e){
            logger.warning(e.toString());
            throw new SAXException(e);
        }
        os.flush();
        os.close();
 	    InputStream is = getInputStream(hc,auth);
        Schema schema = WFSSchemaFactory.getInstance(null,is);
        Element[] elements = schema.getElements();
        if (elements == null){
            return null; // not found
        }
        Element element = null;
        
        String ttname = typeName.substring(typeName.indexOf(":")+1);
        for(int i = 0;i<elements.length && element == null;i++){
            // HACK -- namspace related -- should be checking ns as opposed to removing prefix
            if(typeName.equals(elements[i].getName()) || ttname.equals(elements[i].getName()))
                element = elements[i];
        }
        
        if(element == null)
            return null;
        FeatureType ft = GMLComplexTypes.createFeatureType(element);
        is.close();
        return ft;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        WFSFeatureReader t = null;
        if((protos & POST_FIRST) == POST_FIRST && t == null){
            try {
                t = getFeatureReaderPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if((protos & GET_FIRST) == GET_FIRST && t == null)
            try {
                t = getFeatureReaderGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        
        if((protos & POST_OK) == POST_OK && t == null)
            try {
                t = getFeatureReaderPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }

        if((protos & GET_OK) == GET_OK && t == null)
            try {
                t = getFeatureReaderGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
            
        if(t.hasNext()){ // opportunity to throw exception
            if(t.getFeatureType()!=null)
                return t;
            throw new IOException("There are features but no feature type ... odd");
        }
        return null;
    }
    
    private WFSFeatureReader getFeatureReaderGet(String typeName) throws SAXException, IOException{
        URL getUrl = capabilities.getGetFeature().getGet();

		if(getUrl == null)
		    return null;
		
        String query = getUrl.getQuery().toUpperCase();
        String url = getUrl.toString();
        if(query == null){
            url += "?SERVICE=WFS";
        }else{
            if(query.indexOf("SERVICE")==-1){
                url += "&SERVICE=WFS";
 	        }
        }
        if(query.indexOf("VERSION")==-1){
            url += "&VERSION=1.0.0";
	    }
        if(query.indexOf("REQUEST")==-1){
            url += "&REQUEST=GetFeature";
	    }
        url += "&TYPENAME="+typeName;
        
        // TODO maxFeatures?
        
        getUrl = new URL(url);
        HttpURLConnection hc = (HttpURLConnection)getUrl.openConnection();
        hc.setRequestMethod("GET");

 	    InputStream is = getInputStream(hc,auth);
        
 	   WFSFeatureReader ft = WFSFeatureReader.getFeatureReader(is,bufferSize,timeout);
        return ft;
    }
    
    private WFSFeatureReader getFeatureReaderPost(String typeName) throws SAXException, IOException{
        URL postUrl = capabilities.getGetFeature().getPost();

		if(postUrl == null)
		    return null;
		
        HttpURLConnection hc = (HttpURLConnection)postUrl.openConnection();
        hc.setRequestMethod("POST");
        OutputStream os = getOutputStream(hc,auth);
        // write request
        
        Writer w = new OutputStreamWriter(os);
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[2]); // GetFeature
        try{
            Query query = new DefaultQuery(typeName);
            DocumentWriter.writeDocument(query,WFSSchema.getInstance(),w,hints);
        }catch(OperationNotSupportedException e){
            logger.warning(e.toString());
            throw new SAXException(e);
        }
        os.flush();
 	    os.close();
 	    
 	    InputStream is = getInputStream(hc,auth);
// 	    System.out.println("ready?"+is.available());

 	    WFSFeatureReader ft = WFSFeatureReader.getFeatureReader(is,bufferSize,timeout);
 	    
        return ft;
    }
    
    /**
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String, org.geotools.data.Query)
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
            throws IOException {
        // TODO Auto-generated method stub
        return super.getFeatureReader(typeName, query);
    }
    /**
     * @see org.geotools.data.AbstractDataStore#getUnsupportedFilter(java.lang.String, org.geotools.filter.Filter)
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter) {
        // TODO Auto-generated method stub
        return super.getUnsupportedFilter(typeName, filter);
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
