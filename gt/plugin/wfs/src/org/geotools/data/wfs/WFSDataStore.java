/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.wfs;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.ows.FeatureSetDescription;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.feature.FeatureType;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.gml.GMLComplexTypes;
import org.geotools.xml.ogc.FilterSchema;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.wfs.WFSSchema;
import org.xml.sax.SAXException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSDataStore extends AbstractDataStore {
    protected static final Logger logger = Logger.getLogger(
            "org.geotools.data.wfs");
    protected static final int POST_FIRST = 1;
    protected static final int GET_FIRST = 2;
    protected static final int POST_OK = 4;
    protected static final int GET_OK = 8;
    protected WFSCapabilities capabilities = null;
    protected int protos = 0;
    protected Authenticator auth = null;
    private int bufferSize = 10;
    private int timeout = 3000;
    private Map featureTypeCache = new HashMap();

    private WFSDataStore() {
    	// not called
    }

    WFSDataStore(URL host, Boolean get, Boolean post, String username,
        String password, int timeout, int buffer)
        throws SAXException, IOException {
        super(false); // TODO update when writeable
        logger.setLevel(Level.WARNING);

        // TODO find a better way of adding functionality to the factory ... perhaps putting in your own RootHandler?
        new WFSSchemaFactory();

        if ((username != null) && (password != null)) {
            auth = new WFSAuthenticator(username, password, host);
        }

        // TODO support using POST for getCapabilities
        HttpURLConnection hc = (HttpURLConnection) host.openConnection();
        InputStream is = getInputStream(hc, auth);
        Object t = DocumentFactory.getInstance(is, null, logger.getLevel());

        if (t instanceof WFSCapabilities) {
            capabilities = (WFSCapabilities) t;
        } else {
            throw new SAXException(
                "The specified URL Should have returned a 'WFSCapabilities' object. Returned a "
                + ((t == null) ? "null value."
                               : (t.getClass().getName() + " instance.")));
        }

        if (get == null) {
            protos = GET_OK;
        } else {
            protos = get.booleanValue() ? (GET_FIRST + GET_OK) : 0;
        }

        if (post == null) {
            protos = protos | POST_OK;
        } else {
            protos = post.booleanValue() ? (POST_FIRST + POST_OK)
                                         : (protos | protos);
        }

        this.timeout = timeout;
        this.bufferSize = buffer;
    }

    protected static InputStream getInputStream(HttpURLConnection url,
        Authenticator auth) throws IOException {
        // TODO ensure that we can sync using the class loader and not have concurent thread issues
        //
        // should be ok, as we would only be playing with the classloader's allocated space
        InputStream result = null;

        synchronized (Authenticator.class) {
            Authenticator.setDefault(auth);

            try {
                result = url.getInputStream();
            } catch (MalformedURLException e) {
                logger.warning(e.toString());
                throw e;
            }

            url.connect();

            Authenticator.setDefault(null);
        }

        return new BufferedInputStream(result);
    }

    protected static OutputStream getOutputStream(HttpURLConnection url,
        Authenticator auth) throws IOException {
        // TODO ensure that we can sync using the class loader and not have concurent thread issues
        //
        // should be ok, as we would only be playing with the classloader's allocated space
        OutputStream result = null;

        synchronized (Authenticator.class) {
            Authenticator.setDefault(auth);
            url.setDoOutput(true);

            // 	      url.connect();
            result = url.getOutputStream();

            Authenticator.setDefault(null);
        }

        return new BufferedOutputStream(result);
    }

    static URL createGetCapabilitiesRequest(URL host) {
        if (host == null) {
            return null;
        }

        String url = host.toString();

        if (host.getQuery() == null) {
            url += "?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetCapabilities";
        } else {
            String t = host.getQuery().toUpperCase();

            if (t.indexOf("SERVICE") == -1) {
                url += "&SERVICE=WFS";
            }

            if (t.indexOf("VERSION") == -1) {
                url += "&VERSION=1.0.0";
            }

            if (t.indexOf("REQUEST") == -1) {
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

        for (int i = 0; i < l.size(); i++) {
            result[i] = ((FeatureSetDescription) l.get(i)).getName();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        if (featureTypeCache.containsKey(typeName)) {
            return (FeatureType) featureTypeCache.get(typeName);
        }

        FeatureType t = null;

        if (((protos & POST_FIRST) == POST_FIRST) && (t == null)) {
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & GET_FIRST) == GET_FIRST) && (t == null)) {
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & POST_OK) == POST_OK) && (t == null)) {
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & GET_OK) == GET_OK) && (t == null)) {
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (t != null) {
            featureTypeCache.put(typeName, t);
        }

        return t;
    }

    private FeatureType getSchemaGet(String typeName)
        throws SAXException, IOException {
        URL getUrl = capabilities.getDescribeFeatureType().getGet();

        //System.out.println("getSchemaGet -- get "+getUrl);
        if (getUrl == null) {
            return null;
        }

        String query = getUrl.getQuery().toUpperCase();
        String url = getUrl.toString();

        if ((query == null) || "".equals(query)) {
            if ((url == null) || !url.endsWith("?")) {
                url += "?";
            }

            url += "SERVICE=WFS";
        } else {
            if (query.indexOf("SERVICE=WFS") == -1) {
                url += "&SERVICE=WFS";
            }
        }

        if ((query == null) || (query.indexOf("VERSION") == -1)) {
            url += "&VERSION=1.0.0";
        }

        if ((query == null) || (query.indexOf("REQUEST") == -1)) {
            url += "&REQUEST=DescribeFeatureType";
        }

        url += ("&TYPENAME=" + typeName);
        getUrl = new URL(url);

        HttpURLConnection hc = (HttpURLConnection) getUrl.openConnection();
        hc.setRequestMethod("GET");

        InputStream is = getInputStream(hc, auth);
        Schema schema = WFSSchemaFactory.getInstance(null, is);
        Element[] elements = schema.getElements();
        Element element = null;

        String ttname = typeName.substring(typeName.indexOf(":") + 1);

        for (int i = 0; (i < elements.length) && (element == null); i++)

            // HACK -- namspace related -- should be checking ns as opposed to removing prefix
            if (typeName.equals(elements[i].getName())
                    || ttname.equals(elements[i].getName())) {
                element = elements[i];
            }

        if (element == null) {
            return null;
        }

        FeatureType ft = GMLComplexTypes.createFeatureType(element);

        return ft;
    }

    private FeatureType getSchemaPost(String typeName)
        throws IOException, SAXException {
        URL postUrl = capabilities.getDescribeFeatureType().getPost();

        //System.out.println("getSchemaPost -- post "+postUrl);
        if (postUrl == null) {
            return null;
        }

        //System.out.println(postUrl);
        HttpURLConnection hc = (HttpURLConnection) postUrl.openConnection();
        hc.setRequestMethod("POST");
        hc.setDoInput(true);

        OutputStream os = getOutputStream(hc, auth);

        // write request
        Writer w = new OutputStreamWriter(os);
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,
            WFSSchema.getInstance().getElements()[1]); // DescribeFeatureType

        try {
            DocumentWriter.writeDocument(new String[] { typeName },
                WFSSchema.getInstance(), w, hints);
        } catch (OperationNotSupportedException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        os.flush();
        os.close();

        InputStream is = getInputStream(hc, auth);
        Schema schema = WFSSchemaFactory.getInstance(null, is);
        Element[] elements = schema.getElements();

        if (elements == null) {
            return null; // not found
        }

        Element element = null;

        String ttname = typeName.substring(typeName.indexOf(":") + 1);

        for (int i = 0; (i < elements.length) && (element == null); i++) {
            // HACK -- namspace related -- should be checking ns as opposed to removing prefix
            if (typeName.equals(elements[i].getName())
                    || ttname.equals(elements[i].getName())) {
                element = elements[i];
            }
        }

        if (element == null) {
            return null;
        }

        FeatureType ft = GMLComplexTypes.createFeatureType(element);
        is.close();

        return ft;
    }
    
    private WFSFeatureReader getFeatureReaderGet(Query request,
        Transaction transaction) throws UnsupportedEncodingException, IOException, SAXException{
        URL getUrl = capabilities.getGetFeature().getGet();

        if (getUrl == null) {
            return null;
        }

        String query = getUrl.getQuery().toUpperCase();
        String url = getUrl.toString();

        if ((query == null) || "".equals(query)) {
            if ((url == null) || !url.endsWith("?")) {
                url += "?";
            }

            url += "SERVICE=WFS";
        } else {
            if (query.indexOf("SERVICE=WFS") == -1) {
                url += "&SERVICE=WFS";
            }
        }

        if ((query == null) || (query.indexOf("VERSION") == -1)) {
            url += "&VERSION=1.0.0";
        }

        if ((query == null) || (query.indexOf("REQUEST") == -1)) {
            url += "&REQUEST=GetFeature";
        }

        url += ("&TYPENAME=" + request.getTypeName());

        if (request != null) {
            if (request.getMaxFeatures() != Query.DEFAULT_MAX) {
                url += ("&MAXFEATURES=" + request.getMaxFeatures());
            }

            if (request.getFilter() != null) {
                if (request.getFilter().getFilterType() == FilterType.GEOMETRY_BBOX) {
                    url += ("&BBOX="
                    + printBBoxGet(((GeometryFilter) request.getFilter())));
                } else {
                    if (request.getFilter().getFilterType() == FilterType.FID) {
                        FidFilter ff = (FidFilter) request.getFilter();

                        if ((ff.getFids() != null) && (ff.getFids().length > 0)) {
                            url += ("&FEATUREID=" + ff.getFids()[0]);

                            for (int i = 1; i < ff.getFids().length; i++) {
                                url += ("," + ff.getFids()[i]);
                            }
                        }
                    } else {
                        // rest
                        if (request.getFilter() != Filter.NONE) {
                            url += URLEncoder.encode("&FILTER="
                                + printFilter(request.getFilter()), "UTF-8");
                        }
                    }
                }
            }
        }

        //System.out.println(url); // url to request
        getUrl = new URL(url);

        HttpURLConnection hc = (HttpURLConnection) getUrl.openConnection();
        hc.setRequestMethod("GET");

        InputStream is = getInputStream(hc, auth);

        WFSTransactionState ts = null;

        if (!(transaction == Transaction.AUTO_COMMIT)) {
            ts = (WFSTransactionState) transaction.getState(this);

            if (ts == null) {
                ts = new WFSTransactionState(this);
                transaction.putState(this, ts);
            }
        }

        WFSFeatureReader ft = WFSFeatureReader.getFeatureReader(is, bufferSize,
                timeout, ts);

        return ft;
    }

    private String printFilter(Filter f) throws IOException, SAXException {
        // ogc filter
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,
            FilterSchema.getInstance().getElements()[2]); // Filter

        StringWriter w = new StringWriter();

        try {
            DocumentWriter.writeFragment(f, FilterSchema.getInstance(), w, hints);
        } catch (OperationNotSupportedException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        return w.toString();
    }

    private String printBBoxGet(GeometryFilter gf) throws IOException {
        Envelope e = null;

        if (gf.getLeftGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
            e = ((Geometry) ((LiteralExpression) gf.getLeftGeometry())
                .getLiteral()).getEnvelopeInternal();
        } else {
            if (gf.getRightGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
                e = ((Geometry) ((LiteralExpression) gf.getRightGeometry())
                    .getLiteral()).getEnvelopeInternal();
            } else {
                throw new IOException("Cannot encode BBOX:" + gf);
            }
        }

        return e.getMinX() + "," + e.getMinY() + "," + e.getMaxX() + ","
        + e.getMaxY();
    }

    private WFSFeatureReader getFeatureReaderPost(Query query,
        Transaction transaction) throws SAXException, IOException {
        URL postUrl = capabilities.getGetFeature().getPost();

        if (postUrl == null) {
            return null;
        }

        HttpURLConnection hc = (HttpURLConnection) postUrl.openConnection();
        hc.setRequestMethod("POST");

        OutputStream os = getOutputStream(hc, auth);

        // write request
        Writer w = new OutputStreamWriter(os);
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,
            WFSSchema.getInstance().getElements()[2]); // GetFeature

//        Writer sw = new StringWriter();
//        try{
//            DocumentWriter.writeDocument(query,WFSSchema.getInstance(),sw,hints);
//        }catch(OperationNotSupportedException e){
//            logger.warning(e.toString());
//            throw new SAXException(e);
//        }
//        System.out.println("WFS FILTER START");
//        System.out.println(sw);
//        System.out.println("WFS FILTER END");
//        System.out.println("FILTER WAS "+query.getFilter());
        
        try {
            DocumentWriter.writeDocument(query, WFSSchema.getInstance(), w,
                hints);
        } catch (OperationNotSupportedException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        os.flush();
        os.close();

        InputStream is = getInputStream(hc, auth);

        // 	    System.out.println("ready?"+is.available());
        WFSTransactionState ts = null;

        if (!(transaction == Transaction.AUTO_COMMIT)) {
            ts = (WFSTransactionState) transaction.getState(this);

            if (ts == null) {
                ts = new WFSTransactionState(this);
                transaction.putState(this, ts);
            }
        }

        WFSFeatureReader ft = WFSFeatureReader.getFeatureReader(is, bufferSize,
                timeout, ts);

        return ft;
    }

    protected FeatureReader getFeatureReader(String typeName)
        throws IOException {
        return getFeatureReader(typeName, new DefaultQuery(typeName));
    }

    protected FeatureReader getFeatureReader(String typeName, Query query)
        throws IOException {
        if ((query.getTypeName() == null)
                || !query.getTypeName().equals(typeName)) {
            Query q = new DefaultQuery(typeName, query.getNamespace(),
                    query.getFilter(), query.getMaxFeatures(),
                    query.getPropertyNames(), query.getHandle());

            return getFeatureReader(q, Transaction.AUTO_COMMIT);
        }

        return getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query, org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction)
        throws IOException {
        WFSFeatureReader t = null;
        Filter[] filters = splitFilters(query,transaction); // [server][post] 
        
        query = new DefaultQuery(query);
        ((DefaultQuery)query).setFilter(filters[0]);
        if (((protos & POST_FIRST) == POST_FIRST) && (t == null)) {
            try {
                t = getFeatureReaderPost(query, transaction);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & GET_FIRST) == GET_FIRST) && (t == null)) {
            try {
                t = getFeatureReaderGet(query, transaction);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & POST_OK) == POST_OK) && (t == null)) {
            try {
                t = getFeatureReaderPost(query, transaction);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (((protos & GET_OK) == GET_OK) && (t == null)) {
            try {
                t = getFeatureReaderGet(query, transaction);
            } catch (SAXException e) {
                logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        if (t.hasNext()) { // opportunity to throw exception

            if (t.getFeatureType() != null) {
            	if (!filters[1].equals( Filter.NONE ) ) {
            		return new FilteringFeatureReader(t, filters[1]);
                }
            	return t;
            }

            throw new IOException(
                "There are features but no feature type ... odd");
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getBounds(org.geotools.data.Query)
     */
    protected Envelope getBounds(Query query) throws IOException {
        if ((query == null) || (query.getTypeName() == null)) {
            return super.getBounds(query);
        }

        List fts = capabilities.getFeatureTypes(); // FeatureSetDescription
        Iterator i = fts.iterator();
        String queryName = query.getTypeName().substring(query.getTypeName()
                                                              .indexOf(":") + 1);

        while (i.hasNext()) {
            FeatureSetDescription fsd = (FeatureSetDescription) i.next();
            String fsdName = (fsd.getName() == null) ? null
                                                     : fsd.getName().substring(fsd.getName()
                                                                                  .indexOf(":")
                    + 1);

            if (queryName.equals(fsdName)) {
                return fsd.getLatLongBoundingBox();
            }
        }

        return super.getBounds(query);
    }

    private Filter[] splitFilters(Query q, Transaction t) throws IOException{
    	// have to figure out which part of the request the server is capable of after removing the parts in the update / delete actions
    	// [server][post]
    	if(q.getFilter() == null)
    		return new Filter[]{Filter.NONE,Filter.NONE};
    	if(q.getTypeName() == null || t == null)
    		return new Filter[]{Filter.NONE,q.getFilter()};
    	
    	FeatureType ft = getSchema(q.getTypeName());
    	
    	List fts = capabilities.getFeatureTypes(); //FeatureSetDescription
        boolean found = false;
        for (int i = 0; i < fts.size(); i++)
            if (fts.get(i) != null) {
                FeatureSetDescription fsd = (FeatureSetDescription) fts.get(i);
                if (ft.getTypeName().equals(fsd.getName())) {
                    found = true;
                } else {
                    String fsdName = (fsd.getName() == null) ? null
                        : fsd.getName().substring(fsd.getName()
                        .indexOf(":") + 1);
                    if (ft.getTypeName().equals(fsdName)) {
                        found = true;
                    }
                }
            }

        if (!found) {
            logger.warning("Could not find typeName: " + ft.getTypeName());
            return new Filter[]{Filter.NONE,q.getFilter()};
        }
        WFSTransactionState state = (t == Transaction.AUTO_COMMIT)?null:(WFSTransactionState)t.getState(this);
        WFSFilterVisitor wfsfv = new WFSFilterVisitor(capabilities
                .getFilterCapabilities(), ft, state);

//System.out.println("STARTING FILTER "+q.getFilter());
        q.getFilter().accept(wfsfv);

        Filter[] f = new Filter[2]; 
        f[0] = wfsfv.getFilterPre(); // server
        f[1] = wfsfv.getFilterPost();

        return f;
    }
    
    /**
     * @see org.geotools.data.AbstractDataStore#getUnsupportedFilter(java.lang.String,
     *      org.geotools.filter.Filter)
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter) {
        try {
			return splitFilters(new DefaultQuery(typeName,filter),Transaction.AUTO_COMMIT)[1];
		} catch (IOException e) {
			return filter;
		}
    }

    /**
     * 
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName)
        throws IOException {
        if (capabilities.getTransaction() != null) {
            //			if(capabilities.getLockFeature()!=null){
            //				return new WFSFeatureLocking(this,getSchema(typeName));
            //			}
            return new WFSFeatureStore(this, getSchema(typeName));
        }

        return new WFSFeatureSource(this, getSchema(typeName));
    }

    private static class WFSAuthenticator extends Authenticator {
        private PasswordAuthentication pa;
        private URL host; // this is the getCapabilities url

        private WFSAuthenticator() {
        	// not called
        }

        /**
         * 
         * @param user
         * @param pass
         * @param host
         */
        public WFSAuthenticator(String user, String pass, URL host) {
            pa = new PasswordAuthentication(user, pass.toCharArray());
            this.host = host;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            // check protocol
            if ((host.getProtocol() != null)
                    && (!host.getProtocol().equals(getRequestingProtocol()))) {
                return null;
            }

            // check host
            if ((host.getHost() != null)
                    && (!host.getHost().equals(getRequestingHost()))) {
                return null;
            }

            // check port
            // TODO probably should add more ports here ://
            if ((host.getPort() != 0)
                    && (host.getPort() != getRequestingPort())) {
                return null;
            }

            // TODO add more checks by someone who knows more
            return pa;
        }
    }
}
