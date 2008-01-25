/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.wfs.protocol.HttpMethod.GET;
import static org.geotools.wfs.protocol.HttpMethod.POST;
import static org.geotools.wfs.protocol.WFSOperationType.GET_FEATURE;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows.DCPType;
import net.opengis.ows.KeywordsType;
import net.opengis.ows.OnlineResourceType;
import net.opengis.ows.OperationType;
import net.opengis.ows.OperationsMetadataType;
import net.opengis.ows.RequestMethodType;
import net.opengis.ows.ServiceProviderType;
import net.opengis.ows.WGS84BoundingBoxType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.ResultTypeType;
import net.opengis.wfs.WFSCapabilitiesType;
import net.opengis.wfs.WfsFactory;

import org.apache.xml.serialize.OutputFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.wfs.protocol.HttpMethod;
import org.geotools.wfs.protocol.Version;
import org.geotools.wfs.protocol.WFSConnectionFactory;
import org.geotools.wfs.protocol.WFSOperationType;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

public class WFS110ProtocolHandler extends WFSConnectionFactory {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * WFS 1.1 configuration used for XML parsing and encoding
     */
    private static final WFSConfiguration configuration = new WFSConfiguration();

    /**
     * The WFS GetCapabilities document. Final by now, as we're not handling
     * updatesequence, so will not ask the server for an updated capabilities
     * during the life-time of this datastore.
     */
    private final WFSCapabilitiesType capabilities;

    /**
     * Per featuretype name Map of capabilities feature type information. Not to
     * be used directly but through {@link #getFeatureTypeInfo(String)}
     */
    private final Map<String, FeatureTypeType> typeInfos;

    /**
     * Creates the protocol handler by parsing the capabilities document from
     * the provided input stream.
     * 
     * @param capabilitiesReader
     * @param tryGzip
     * @param auth
     * @param encoding
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public WFS110ProtocolHandler(InputStream capabilitiesReader, boolean tryGzip,
            Authenticator auth, String encoding) throws IOException {
        super(Version.v1_1_0, tryGzip, auth, encoding);
        this.capabilities = parseCapabilities(capabilitiesReader);
        this.typeInfos = new HashMap<String, FeatureTypeType>();

        final List<FeatureTypeType> ftypes = capabilities.getFeatureTypeList().getFeatureType();
        QName typeName;
        for (FeatureTypeType ftype : ftypes) {
            typeName = ftype.getName();
            assert !("".equals(typeName.getPrefix()));
            String prefixedTypeName = typeName.getPrefix() + ":" + typeName.getLocalPart();
            typeInfos.put(prefixedTypeName, ftype);
        }
    }

    private WFSCapabilitiesType parseCapabilities(InputStream capabilitiesReader)
            throws IOException {
        final Parser parser = new Parser(configuration);
        final Object parsed;
        try {
            parsed = parser.parse(capabilitiesReader);
        } catch (SAXException e) {
            throw new DataSourceException("Exception parsing WFS 1.1.0 capabilities", e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException("WFS 1.1.0 parsing configuration error", e);
        }
        if (parsed == null) {
            throw new DataSourceException("WFS 1.1.0 capabilities was not parsed");
        }
        if (!(parsed instanceof WFSCapabilitiesType)) {
            throw new DataSourceException("Expected WFS Capabilities, got " + parsed);
        }
        return (WFSCapabilitiesType) parsed;
    }

    /**
     * Returns the URL representing the service entry point for the required WFS
     * operation and HTTP method.
     */
    @SuppressWarnings("unchecked")
    @Override
    public URL getOperationURL(final WFSOperationType operation, final HttpMethod method)
            throws UnsupportedOperationException {
        final OperationsMetadataType operationsMetadata = capabilities.getOperationsMetadata();
        final List<OperationType> operations = operationsMetadata.getOperation();
        for (OperationType operationType : operations) {
            String operationName = operationType.getName();
            if (operation.getName().equalsIgnoreCase(operationName)) {
                List<DCPType> dcps = operationType.getDCP();
                for (DCPType dcp : dcps) {
                    List<RequestMethodType> requests;
                    if (GET == method) {
                        requests = dcp.getHTTP().getGet();
                    } else {
                        requests = dcp.getHTTP().getPost();
                    }
                    for (RequestMethodType req : requests) {
                        String href = req.getHref();
                        if (href != null) {
                            try {
                                return new URL(href);
                            } catch (MalformedURLException e) {
                                // Log error and let the search continue
                                LOGGER.log(Level.INFO, "Malformed " + method + " URL for "
                                        + operationName, e);
                            }
                        }
                    }
                }
            }
        }
        throw new UnsupportedOperationException("Operation metadata not found for " + operation
                + " with HTTP " + method + " method");
    }

    /**
     * Checks whether the WFS capabilities provides a service entry point for
     * the given operation and HTTP method.
     */
    @Override
    public boolean supports(WFSOperationType operation, HttpMethod method) {
        try {
            getOperationURL(operation, method);
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public String getServiceAbstract() {
        return capabilities.getServiceIdentification().getAbstract();
    }

    /**
     * @return service metadata keywords
     */
    @SuppressWarnings("unchecked")
    public Set<String> getKeywords() {
        List<KeywordsType> capsKeywords = capabilities.getServiceIdentification().getKeywords();
        return getKeyWords(capsKeywords);
    }

    /**
     * @param typeName
     *            type name to return the keyword list for
     * @return the keywords of {@code typeName} in the capabilities document
     */
    @SuppressWarnings("unchecked")
    public Set<String> getKeywords(final String typeName) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        List<KeywordsType> ftKeywords = featureTypeInfo.getKeywords();
        return getKeyWords(ftKeywords);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getKeyWords(List<KeywordsType> keywordsList) {
        Set<String> keywords = new HashSet<String>();
        for (KeywordsType keys : keywordsList) {
            keywords.addAll(keys.getKeyword());
        }
        return keywords;
    }

    public URI getServiceProviderUri() {
        ServiceProviderType serviceProvider = capabilities.getServiceProvider();
        if (serviceProvider == null) {
            return null;
        }
        OnlineResourceType providerSite = serviceProvider.getProviderSite();
        if (providerSite == null) {
            return null;
        }
        String href = providerSite.getHref();
        if (href == null) {
            return null;
        }
        try {
            return new URI(href);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String getServiceTitle() {
        return capabilities.getServiceIdentification().getTitle();
    }

    /**
     * Makes a {@code DescribeFeatureType} request for {@code typeName} feature
     * type, parses the server response into a {@link SimpleFeatureType} and
     * returns it.
     * <p>
     * Due to a current limitation widely spread through the GeoTools library,
     * the parsed FeatureType will be adapted to share the same name than the
     * Features produced for it. For example, if the actual feature type name is
     * {@code Streams_Type} and the features name (i.e. which is the FeatureType
     * name as stated in the WFS capabilities document) is {@code Stream}, the
     * returned feature type name will also be {@code Stream}.
     * </p>
     * 
     * @param typeName
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public SimpleFeatureType parseDescribeFeatureType(final String typeName) throws IOException {
        final FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        final QName featureDescriptorName = featureTypeInfo.getName();

        final URL describeUrl = getDescribeFeatureTypeURLGet(typeName);

        final SimpleFeatureType featureType;
        CoordinateReferenceSystem crs = getFeatureTypeCRS(typeName);
        featureType = EmfAppSchemaParser.parse(configuration, featureDescriptorName, describeUrl, crs);

        // adapt the feature type name
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init(featureType);
        builder.setName(typeName);
        builder.setNamespaceURI(featureTypeInfo.getName().getNamespaceURI());
        GeometryDescriptor defaultGeometry = featureType.getDefaultGeometry();
        if (defaultGeometry != null) {
            builder.setDefaultGeometry(defaultGeometry.getLocalName());
            builder.setCRS(defaultGeometry.getCRS());
        }
        final SimpleFeatureType adaptedFeatureType = builder.buildFeatureType();
        return adaptedFeatureType;
    }

    @Override
    public URL getDescribeFeatureTypeURLGet(final String typeName) throws MalformedURLException {
        URL v100StyleUrl = super.getDescribeFeatureTypeURLGet(typeName);
        FeatureTypeType typeInfo = getFeatureTypeInfo(typeName);
        QName name = typeInfo.getName();
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix())) {
            return v100StyleUrl;
        }
        String raw = v100StyleUrl.toExternalForm();
        String nsUri;
        String outputFormat;
        try {
            nsUri = URLEncoder.encode(name.getNamespaceURI(), "UTF-8");
            outputFormat = URLEncoder.encode("text/xml; subtype=gml/3.1.1", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        raw += "&NAMESPACE=xmlns(" + name.getPrefix() + "=" + nsUri + ")";
        raw += "&OUTPUTFORMAT=" + outputFormat;
        URL v110StyleUrl = new URL(raw);
        return v110StyleUrl;
    }

    private Object parse(final URL url, final HttpMethod method) throws IOException {

        final HttpURLConnection connection = getConnection(url, method);
        String contentEncoding = connection.getContentEncoding();
        Charset charset = Charset.forName("UTF-8"); // TODO: un-hardcode
        if (null != contentEncoding) {
            try {
                charset = Charset.forName(contentEncoding);
            } catch (UnsupportedCharsetException e) {
                LOGGER.warning("Can't handle response encoding: " + contentEncoding
                        + ". Trying with default");
            }
        }
        Parser parser = new Parser(configuration);
        InputStream in = getInputStream(connection);
        Reader reader = new InputStreamReader(in, charset);
        Object parsed;
        try {
            parsed = parser.parse(reader);
        } catch (SAXException e) {
            throw new DataSourceException(e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException(e);
        } finally {
            reader.close();
        }
        return parsed;
    }

    public String[] getCapabilitiesTypeNames() {
        List<String> typeNames = new ArrayList<String>(typeInfos.keySet());
        Collections.sort(typeNames);
        return typeNames.toArray(new String[typeNames.size()]);
    }

    public String getFeatureTypeTitle(final String typeName) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getTitle();
    }

    public String getFeatureTypeAbstract(final String typeName) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getAbstract();
    }

    @SuppressWarnings("unchecked")
    public ReferencedEnvelope getFeatureTypeBounds(final String typeName) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        List<WGS84BoundingBoxType> bboxList = featureTypeInfo.getWGS84BoundingBox();
        if (bboxList != null && bboxList.size() > 0) {
            WGS84BoundingBoxType bboxType = bboxList.get(0);
            List lowerCorner = bboxType.getLowerCorner();
            List upperCorner = bboxType.getUpperCorner();
            double minx = (Double) lowerCorner.get(0);
            double miny = (Double) lowerCorner.get(1);
            double maxx = (Double) upperCorner.get(0);
            double maxy = (Double) upperCorner.get(1);
            CoordinateReferenceSystem crs = getFeatureTypeCRS(typeName);
            ReferencedEnvelope typeBounds = new ReferencedEnvelope(minx, maxx, miny, maxy, crs);
            return typeBounds;
        }
        return new ReferencedEnvelope();
    }

    public CoordinateReferenceSystem getFeatureTypeCRS(final String typeName) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        String defaultSRS = featureTypeInfo.getDefaultSRS();
        try {
            return CRS.decode(defaultSRS);
        } catch (NoSuchAuthorityCodeException e) {
            LOGGER.info("Authority not found for " + typeName + " CRS: " + defaultSRS);
            // HACK HACK HACK!: remove when
            // http://jira.codehaus.org/browse/GEOT-1659 is fixed
            if (defaultSRS.toUpperCase().startsWith("URN")) {
                String code = defaultSRS.substring(defaultSRS.lastIndexOf(":") + 1);
                try {
                    return CRS.decode("EPSG:" + code);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            }
        } catch (FactoryException e) {
            LOGGER.log(Level.INFO, "Error creating CRS " + typeName + ": " + defaultSRS, e);
        }
        return null;
    }

    /**
     * 
     * @param query
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate or any errors occur.
     */
    public ReferencedEnvelope getBounds(final Query query) throws IOException {
        if (!Filter.INCLUDE.equals(query.getFilter())) {
            return null;
        }
        String typeName = query.getTypeName();
        return getFeatureTypeBounds(typeName);
    }

    /**
     * TODO: implement using GetFeature with {@code resultType=hits}
     * 
     * @param query
     * @return
     */
    public int getCount(final Query query) throws IOException {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public FeatureReader getFeatureReader(final SimpleFeatureType contentType, final Query query,
            final Transaction transaction) throws IOException {
        // by now encode the full query to be sent to the server.
        // TODO: implement filter splitting for server supported/unsupported

        final String typeName = query.getTypeName();
        final FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);

        Filter filter = query.getFilter();
        if (Filter.EXCLUDE.equals(filter)) {
            return new EmptyFeatureReader(contentType);
        }

        final InputStream responseStream;
        // TODO: enable POST
        if (false && supports(GET_FEATURE, POST)) {
            QueryType wfsQuery = createWfsQuery(typeName, filter);
            GetFeatureType wfsRequest = createGetFeature(wfsQuery, false);
            URL getFeaturePostUrl = getOperationURL(GET_FEATURE, POST);
            responseStream = sendPost(getFeaturePostUrl, wfsRequest, WFS.GetFeature);
        } else {
            URL getFeatureGetUrl;
            String[] propNames = query.getPropertyNames();
            List<String> propertyNames;
            if (propNames == null || propNames.length == 0) {
                propertyNames = Collections.emptyList();
            } else {
                propertyNames = Arrays.asList(propNames);
            }
            int maxFeatures = query.getMaxFeatures();
            List<SortBy> sortBy = (List<SortBy>) (query.getSortBy() == null ? Collections
                    .emptyList() : Arrays.asList(query.getSortBy()));
            getFeatureGetUrl = createGetFeatureGet(typeName, propertyNames, filter, maxFeatures,
                    sortBy, false);
            responseStream = sendGet(getFeatureGetUrl);
        }
        final QName name = featureTypeInfo.getName();
        GetFeatureParser parser = new StreamingParserFeatureReader(configuration, responseStream,
                name);
        WFSFeatureReader reader = new WFSFeatureReader(contentType, parser);
        return reader;
    }

    /**
     * 
     * @param typeName
     * @param propertyNames
     * @param filter
     *            the filter to apply to the request, shall not be
     *            {@code Filter.EXCLUDE}
     * @param maxFeatures
     * @param sortBy
     * @return
     * @throws MalformedURLException
     */
    @SuppressWarnings("unchecked")
    private URL createGetFeatureGet(final String typeName, final List<String> propertyNames,
            final Filter filter, final int maxFeatures, final List<SortBy> sortBy, boolean hits)
            throws MalformedURLException {
        final URL getFeatureGetUrl = getOperationURL(GET_FEATURE, GET);
        Map<String, String> kvpMap = new LinkedHashMap<String, String>();
        {
            String query = getFeatureGetUrl.getQuery();
            if (query != null) {
                String[] split = query.split("&");
                for (String kvp : split) {
                    String[] keyAndValue = kvp.split("=");
                    String key = keyAndValue[0];
                    if ("".equals(key)) {
                        continue;
                    }
                    String value = keyAndValue.length == 1 ? null : keyAndValue[1];
                    kvpMap.put(key, value);
                }
            }
        }

        kvpMap.put("SERVICE", "WFS");
        kvpMap.put("VERSION", "1.1.0");
        kvpMap.put("REQUEST", "GetFeature");
        kvpMap.put("TYPENAME", typeName);
        // TODO: consider other output formats
        // try {
        // kvpMap.put("OUTPUTFORMAT",
        // URLEncoder.encode("text/xml;subtype=gml/3.1.1", "UTF-8"));
        // } catch (UnsupportedEncodingException e) {
        // throw new RuntimeException(e);
        // }

        if (hits) {
            kvpMap.put("RESULTTYPE", "hits");
        }

        if (Integer.MAX_VALUE != maxFeatures) {
            kvpMap.put("MAXFEATURES", String.valueOf(maxFeatures));
        }

        if (propertyNames.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<String> it = propertyNames.iterator(); it.hasNext();) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            kvpMap.put("PROPERTYNAME", sb.toString());
        }

        if (Filter.INCLUDE != filter) {
            if (filter instanceof BBOX) {

            } else if (filter instanceof Id) {
                final Set<Identifier> identifiers = ((Id) filter).getIdentifiers();
                StringBuffer idValues = new StringBuffer();
                for (Iterator<Identifier> it = identifiers.iterator(); it.hasNext();) {
                    Object id = it.next().getID();
                    // REVISIT: should URL encode the id?
                    idValues.append(String.valueOf(id));
                    if (it.hasNext()) {
                        idValues.append(",");
                    }
                }
                kvpMap.put("FEATUREID", idValues.toString());
            } else {
                String xmlEncodedFilter = encodeGetFeatureGetFilter(filter);
                String urlEncodedFilter;
                try {
                    urlEncodedFilter = URLEncoder.encode(xmlEncodedFilter, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                StringBuffer wfsParamDelimitedFilter = new StringBuffer("(");
                wfsParamDelimitedFilter.append(urlEncodedFilter);
                wfsParamDelimitedFilter.append(")");
                kvpMap.put("FILTER", wfsParamDelimitedFilter.toString());
            }
        }

        if (sortBy.size() > 0) {
            StringBuffer sb = new StringBuffer();
            SortBy next;
            PropertyName propertyName;
            String sortOrder;
            for (Iterator<SortBy> it = sortBy.iterator(); it.hasNext();) {
                next = it.next();
                propertyName = next.getPropertyName();
                sortOrder = SortOrder.ASCENDING == next.getSortOrder() ? "A" : "D";
                sb.append(propertyName.getPropertyName());
                sb.append(" ");
                sb.append(sortOrder);
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            kvpMap.put("SORTBY", sb.toString());
        }

        // TODO: support SRSNAME parameter

        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<String, String> kvp : kvpMap.entrySet()) {
            queryString.append(kvp.getKey());
            queryString.append("=");
            queryString.append(kvp.getValue());
            queryString.append("&");
        }

        String entryPoint = getFeatureGetUrl.toExternalForm();
        String getFeatureQueryString = queryString.toString();

        if (!entryPoint.endsWith("&") && !entryPoint.endsWith("?")) {
            entryPoint += "?";
        }
        String url = entryPoint + getFeatureQueryString;
        final URL getFeatureRequest = new URL(url);
        return getFeatureRequest;
    }

    private String encodeGetFeatureGetFilter(Filter filter) {
        return null;
    }

    /**
     * Sends a GET request represented by {@code fullQuery} and returns an input
     * stream from which to get the server response.
     * 
     * @param fullQuery
     * @return
     * @throws IOException
     */
    private InputStream sendGet(final URL fullQuery) throws IOException {
        HttpURLConnection connection = getConnection(fullQuery, GET);
        InputStream responseStream = getInputStream(connection);
        return responseStream;
    }

    /**
     * Sends a POST request to {@code destination} whose content is the XML
     * encoded representation of {@code object} and returns an input stream from
     * which to get the server response.
     * 
     * @param destination
     * @param object
     * @param name
     * @return
     * @throws IOException
     */
    private InputStream sendPost(final URL destination, final Object object, QName name)
            throws IOException {
        Encoder encoder = new Encoder(configuration);
        encoder.setNamespaceAware(true);

        // TODO: outputformat should be somehow hidden as its xerces specific
        // API
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setIndenting(true);
        outputFormat.setIndent(2);
        encoder.setOutputFormat(outputFormat);

        HttpURLConnection connection = getConnection(destination, POST);
        OutputStream outputStream = connection.getOutputStream();
        try {
            encoder.encode(object, name, outputStream);
        } finally {
            outputStream.close();
        }
        InputStream responseStream = getInputStream(connection);
        return responseStream;
    }

    @SuppressWarnings("unchecked")
    private GetFeatureType createGetFeature(QueryType wfsQuery, boolean hits) {
        String outputFormat = null;
        String typeName = (String) wfsQuery.getTypeName().get(0);

        GetFeatureType request = WfsFactory.eINSTANCE.createGetFeatureType();
        request.setHandle("geotools-wfs-client " + typeName);
        request.setOutputFormat(outputFormat);
        request.setResultType(hits ? ResultTypeType.HITS_LITERAL : ResultTypeType.RESULTS_LITERAL);
        request.setService("WFS");// TODO: un-hardcode
        request.setVersion("1.1.0");// TODO: un-hardcode
        request.getQuery().add(wfsQuery);
        return request;
    }

    private QueryType createWfsQuery(String typeName, Filter filter) {
        return null;
    }

    /**
     * Returns the feature type metadata object parsed from the capabilities
     * document for the given {@code typeName}
     * 
     * @param typeName
     *            the typeName as stated in the capabilities
     *            {@code FeatureTypeList} to get the info for
     * @return the WFS capabilities metadata {@link FeatureTypeType metadata}
     *         for {@code typeName}
     * @throws IllegalArgumentException
     *             if {@code typeName} is not the name of a FeatureType stated
     *             in the capabilities document.
     */
    private FeatureTypeType getFeatureTypeInfo(final String typeName) {
        if (!typeInfos.containsKey(typeName)) {
            throw new IllegalArgumentException("Type name not found: " + typeName);
        }
        return typeInfos.get(typeName);
    }

}
