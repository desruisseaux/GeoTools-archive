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

import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.MathTransform;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.crs.CRSService;
import org.geotools.data.crs.ForceCoordinateSystemFeatureReader;
import org.geotools.data.ows.FeatureSetDescription;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wfs.WFSFilterVisitor.WFSBBoxFilterVisitor;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.gml.GMLComplexTypes;
import org.geotools.xml.ogc.FilterSchema;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.wfs.WFSSchema;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
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
import java.net.URI;
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

    protected WFSDataStore(URL host, Boolean get, Boolean post, String username,
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

    protected static URL createGetCapabilitiesRequest(URL host) {
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
        
        // TODO sanity check for request with capabilities obj

        FeatureType t = null;
        SAXException sax = null;
        if (((protos & POST_FIRST) == POST_FIRST) && (t == null)) {
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                sax = e;
            }
        }

        if (((protos & GET_FIRST) == GET_FIRST) && (t == null)) {
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                sax = e;
            }
        }

        if (((protos & POST_OK) == POST_OK) && (t == null)) {
            try {
                t = getSchemaPost(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                sax = e;
            }
        }

        if (((protos & GET_OK) == GET_OK) && (t == null)) {
            try {
                t = getSchemaGet(typeName);
            } catch (SAXException e) {
                logger.warning(e.toString());
                sax = e;
            }
        }
        if(t == null && sax!=null)
            throw new IOException(sax.toString());
        
        //set crs?
        FeatureSetDescription fsd = getFSD(typeName);
        String crsName = null;
        String ftName = null;
        crsName = fsd.getSRS();
        ftName = fsd.getName();
        
        CoordinateReferenceSystem crs;
        try {
            if(crsName!=null){
                crs = getCRSService().createCRS(crsName);
            	t = CRSService.transform(t,crs);
            }
        } catch (FactoryException e) {
            logger.warning(e.getMessage());
        } catch (SchemaException e) {
            logger.warning(e.getMessage());
        }
        
        if(ftName!=null){
            try {
                t = FeatureTypeFactory.newFeatureType(t.getAttributeTypes(),ftName,t.getNamespaceURI(),t.isAbstract(),t.getAncestors(),t.getDefaultGeometry());
            } catch (FactoryConfigurationError e1) {
                logger.warning(e1.getMessage());
            } catch (SchemaException e1) {
                logger.warning(e1.getMessage());
            }
        }

        if (t != null) {
            featureTypeCache.put(typeName, t);
        }

        return t;
    }
    
    private FeatureSetDescription getFSD(String typename){
        List l = capabilities.getFeatureTypes();
        Iterator i = l.iterator();
        String crsName = null;
        String ftName = null;

        while (i.hasNext() && crsName==null) {
                FeatureSetDescription fsd = (FeatureSetDescription) i.next();
                if (typename.equals(fsd.getName()) || (fsd.getName()!=null && typename.equals(fsd.getName().substring(typename.indexOf(':')+1)))) {
                    return fsd;
                }
        }
        return null;
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
System.out.println("SCHMEMA GET "+url);
        getUrl = new URL(url);

        HttpURLConnection hc = (HttpURLConnection) getUrl.openConnection();
        hc.setRequestMethod("GET");

        InputStream is = getInputStream(hc, auth);
        Schema schema = SchemaFactory.getInstance(null, is);
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
        List l = capabilities.getFeatureTypes();
        Iterator it = l.iterator();
        URI uri = null;
        while(it.hasNext() && uri == null){
            FeatureSetDescription fsd = (FeatureSetDescription)it.next();
            if(typeName.equals(fsd.getName()))
                uri = fsd.getNamespace();
        }
        if(uri!=null)
            hints.put(DocumentWriter.SCHEMA_ORDER, new String[]{WFSSchema.NAMESPACE.toString(), uri.toString()});

//        try {
//            DocumentWriter.writeDocument(new String[] { typeName },
//                WFSSchema.getInstance(), new OutputStreamWriter(System.out), hints);
//        } catch (OperationNotSupportedException e) {
//            logger.warning(e.toString());
//            throw new SAXException(e);
//        }
        
        try {
            DocumentWriter.writeDocument(new String[] { typeName },
                WFSSchema.getInstance(), w, hints);
        } catch (OperationNotSupportedException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
        os.flush();

        InputStream is = getInputStream(hc, auth);
        Schema schema = SchemaFactory.getInstance(null, is);
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

        os.close();
        is.close();

        return ft;
    }
    private CRSService crs;
    private CRSService getCRSService(){
        if(crs == null)
            crs = new CRSService();
        return crs;
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
                    String bb = printBBoxGet(((GeometryFilter) request.getFilter()),request.getTypeName());
                    if(bb!=null)
                        url += ("&BBOX=" + bb);
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

System.out.println("GET URL = "+url); // url to request
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
                timeout, ts, getSchema(request.getTypeName()));

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

    private String printBBoxGet(GeometryFilter gf,String typename) throws IOException {
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
        
        if(e == null || e.isNull())
            return null;
        
        // find layer's bbox
        Envelope lbb = null;
        if(capabilities != null && capabilities.getFeatureTypes() != null && typename!=null && !"".equals(typename)){
            List fts = capabilities.getFeatureTypes();
            if(!fts.isEmpty()){
                for(Iterator i=fts.iterator();i.hasNext() && lbb == null;){
                    FeatureSetDescription fsd = (FeatureSetDescription)i.next();
                    if(fsd!=null && typename.equals(fsd.getName())){
                        lbb = fsd.getLatLongBoundingBox();
                    }
                }
            }
        }
        if(lbb == null || lbb.contains(e))
            return e.getMinX() + "," + e.getMinY() + "," + e.getMaxX() + ","
            + e.getMaxY();
        return null;
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
                timeout, ts, getSchema(query.getTypeName()));

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
        FeatureReader t = null;
        SAXException sax = null;
        IOException io = null;
        Filter[] filters = splitFilters(query,transaction); // [server][post] 
        
        query = new DefaultQuery(query);
        // TODO modify bbox requests here
        FeatureSetDescription fsd = getFSD(query.getTypeName());
        
        Envelope maxbbox = fsd.getLatLongBoundingBox();
        if(fsd.getSRS()!=null){
            // reproject this
            try {
                CoordinateReferenceSystem crs = getCRSService().createCRS(fsd.getSRS());
                MathTransform mt = getCRSService().reproject(getCRSService().GEOGRAPHIC,crs,false);
                maxbbox = getCRSService().transform(maxbbox,mt);
            } catch (FactoryException e) {
                e.printStackTrace();maxbbox = null;
            } catch (CannotCreateTransformException e) {
                e.printStackTrace();maxbbox = null;
            } catch (MismatchedDimensionException e) {
                e.printStackTrace();maxbbox = null;
            } catch (TransformException e) {
                e.printStackTrace();maxbbox = null;
            }
        }
        if(maxbbox!=null){
            WFSBBoxFilterVisitor bfv = new WFSBBoxFilterVisitor(maxbbox);
            filters[0].accept(bfv);
        }
        ((DefaultQuery)query).setFilter(filters[0]);
        if (((protos & POST_FIRST) == POST_FIRST) && (t == null)) {
            try {
                t = getFeatureReaderPost(query, transaction);
                if(t!=null)
                    t.hasNext(); // throws spot
            } catch (SAXException e) {
                t = null;
                logger.warning(e.toString());
                sax = e;
            } catch (IOException e) {
                t = null;
                logger.warning(e.toString());
                io = e;
            }
        }

        if (((protos & GET_FIRST) == GET_FIRST) && (t == null)) {
            try {
                t = getFeatureReaderGet(query, transaction);
                if(t!=null)
                    t.hasNext(); // throws spot
            } catch (SAXException e) {
                t = null;
                logger.warning(e.toString());
                sax = e;
            } catch (IOException e) {
                t = null;
                logger.warning(e.toString());
                io = e;
            }
        }

        if (((protos & POST_OK) == POST_OK) && (t == null)) {
            try {
                t = getFeatureReaderPost(query, transaction);
                if(t!=null)
                    t.hasNext(); // throws spot
            } catch (SAXException e) {
                t = null;
                logger.warning(e.toString());
                sax = e;
            } catch (IOException e) {
                t = null;
                logger.warning(e.toString());
                io = e;
            }
        }

        if (((protos & GET_OK) == GET_OK) && (t == null)) {
            try {
                t = getFeatureReaderGet(query, transaction);
                if(t!=null)
                    t.hasNext(); // throws spot
            } catch (SAXException e) {
                t = null;
                logger.warning(e.toString());
                sax = e;
            } catch (IOException e) {
                t = null;
                logger.warning(e.toString());
                io = e;
            }
        }

        if(t==null && sax!=null)
            throw new IOException(sax.toString());
        if(t==null && io!=null)
            throw io;

        if (t.hasNext()) { // opportunity to throw exception

            if (t.getFeatureType() != null) {
//                if(query.getCoordinateSystemReproject()!=null && t.getFeatureType().getDefaultGeometry()!=null && t.getFeatureType().getDefaultGeometry().getCoordinateSystem()!=null){
//                    FeatureReader tmp = t;
//                    try {
//                        t = new ReprojectFeatureReader(t,query.getCoordinateSystemReproject());
//                    } catch (CannotCreateTransformException e1) {
//                        e1.printStackTrace();
//                        t = tmp;
//                    } catch (SchemaException e1) {
//                        e1.printStackTrace();
//                        t = tmp;
//                    }
//                }
                if (!filters[1].equals( Filter.NONE ) ) {
                    t = new FilteringFeatureReader(t, filters[1]);
                }
                if (query.getCoordinateSystem()!=null){
                    FeatureReader tmp = t;
                    try {
                        t = new ForceCoordinateSystemFeatureReader(t,query.getCoordinateSystem());
                    } catch (SchemaException e) {
                        e.printStackTrace();
                        t = tmp;
                    }
                }
            	return t;
            }
            throw new IOException(
                "There are features but no feature type ... odd");
        }

        return new EmptyFeatureReader(getSchema(query.getTypeName()));
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
