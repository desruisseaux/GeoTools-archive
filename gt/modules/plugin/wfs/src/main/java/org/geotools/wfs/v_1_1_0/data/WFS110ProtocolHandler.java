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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import net.opengis.wfs.WFSCapabilitiesType;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.wfs.protocol.HttpMethod;
import org.geotools.wfs.protocol.Version;
import org.geotools.wfs.protocol.WFSConnectionFactory;
import org.geotools.wfs.protocol.WFSOperationType;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
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
        FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
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

    @SuppressWarnings("unchecked")
    public SimpleFeatureType parseDescribeFeatureType(final String typeName) throws IOException {
        if(!typeInfos.containsKey(typeName)){
            throw new DataSourceException("Type name not found: " + typeName);
        }
        final FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
        final QName featureDescriptorName = featureTypeInfo.getName();

        final URL describeUrl = getDescribeFeatureTypeURLGet(typeName);

        SimpleFeatureType featureType;
        CoordinateReferenceSystem crs = getFeatureTypeCRS(typeName);
        featureType = EmfAppSchemaParser.parse(featureDescriptorName, describeUrl, crs);
        return featureType;
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
        FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
        return featureTypeInfo.getTitle();
    }

    public String getFeatureTypeAbstract(final String typeName) {
        FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
        return featureTypeInfo.getAbstract();
    }

    @SuppressWarnings("unchecked")
    public ReferencedEnvelope getFeatureTypeBounds(final String typeName) {
        FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
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
        FeatureTypeType featureTypeInfo = typeInfos.get(typeName);
        String defaultSRS = featureTypeInfo.getDefaultSRS();
        try {
            return CRS.decode(defaultSRS);
        } catch (NoSuchAuthorityCodeException e) {
            LOGGER.info("Authority not found for " + typeName + " CRS: " + defaultSRS);
        } catch (FactoryException e) {
            LOGGER.log(Level.INFO, "Error creating CRS " + typeName + ": " + defaultSRS, e);
        }
        return null;
    }

    public ReferencedEnvelope getBounds(Query query) {
        return null;
    }

    public int getCount(Query query) {
        return 0;
    }

    public FeatureReader getFeatureReader(SimpleFeatureType contentType, Query geomQuery,
            Transaction autoCommit) {
        return null;
    }

}
