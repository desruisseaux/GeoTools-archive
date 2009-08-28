/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.ws;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.ws.protocol.http.HTTPProtocol;
import org.geotools.data.ws.protocol.http.HTTPResponse;
import org.geotools.data.ws.protocol.http.SimpleHttpProtocol;
import org.geotools.data.ws.protocol.ws.Version;
import org.geotools.data.ws.protocol.ws.WSProtocol;
import org.geotools.util.logging.Logging;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @author rpetty
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/app-schema/webservice/
 *         src/main/java/org/geotools /data/wfs/WSDataStoreFactory.java $
 * @see XmlDataStore
 * @see WSProtocol
 * @see WSStrategy
 */
@SuppressWarnings( { "unchecked", "nls" })
public class WSDataStoreFactory extends AbstractDataStoreFactory {
    private static final Logger logger = Logging.getLogger("org.geotools.data.ws");

    public static class WSFactoryParam<T> extends Param {
        private T defaultValue;

        /**
         * Creates a required parameter
         * 
         * @param key
         * @param type
         * @param description
         */
        public WSFactoryParam(String key, Class type, String description) {
            super(key, type, description, true);
        }

        /**
         * Creates an optional parameter with the supplied default value
         * 
         * @param key
         * @param type
         * @param description
         * @param required
         */
        public WSFactoryParam(String key, Class type, String description, T defaultValue) {
            super(key, type, description, false);
            this.defaultValue = defaultValue;
        }

        public T lookUp(final Map params) throws IOException {
            T parameter = (T) super.lookUp(params);
            return parameter == null ? defaultValue : parameter;
        }
    }

    private static final WSFactoryParam[] parametersInfo = new WSFactoryParam[9];
    static {
        String name;
        Class clazz;
        String description;

        name = "WSDataStoreFactory:GET_CONNECTION_URL";
        clazz = URL.class;
        description = "Represents a URL to the getCapabilities document or a server instance.";
        parametersInfo[0] = new WSFactoryParam(name, clazz, description);

        name = "WSDataStoreFactory:USERNAME";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not "
                + "be used without the PASSWORD param.";
        parametersInfo[1] = new WSFactoryParam(name, clazz, description, (String) null);

        name = "WSDataStoreFactory:PASSWORD";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not"
                + " be used without the USERNAME param.";
        parametersInfo[2] = new WSFactoryParam(name, clazz, description, (String) null);

        name = "WSDataStoreFactory:TIMEOUT";
        clazz = Integer.class;
        description = "This allows the user to specify a timeout in milliseconds. This param"
                + " has a default value of 3000ms.";
        parametersInfo[3] = new WSFactoryParam(name, clazz, description, Integer.valueOf(3000));

        name = "WSDataStoreFactory:TRY_GZIP";
        clazz = Boolean.class;
        description = "Indicates that datastore should use gzip to transfer data if the server "
                + "supports it. Default is true";
        parametersInfo[4] = new WSFactoryParam(name, clazz, description, Boolean.TRUE);

        name = "WSDataStoreFactory:MAXFEATURES";
        clazz = Integer.class;
        description = "Positive integer used as a hard limit for the amount of Features to retrieve"
                + " for each FeatureType. A value of zero or not providing this parameter means no limit.";
        parametersInfo[5] = new WSFactoryParam(name, clazz, description, Integer.valueOf(0));

        name = "WSDataStoreFactory:TEMPLATE_NAME";
        clazz = String.class;
        description = "File name of the template used to create the XML request";
        parametersInfo[6] = new WSFactoryParam(name, clazz, description);

        name = "WSDataStoreFactory:TEMPLATE_DIRECTORY";
        clazz = String.class;
        description = "Directory where the template used to create the XML request has been put";
        parametersInfo[7] = new WSFactoryParam(name, clazz, description);

        name = "WSDataStoreFactory:CAPABILITIES_FILE_LOCATION";
        clazz = String.class;
        description = "The location of the capabilities file";
        parametersInfo[8] = new WSFactoryParam(name, clazz, description);
    }

    /**
     * Mandatory DataStore parameter indicating the URL for the WS GetCapabilities document.
     */
    public static final WSFactoryParam<URL> GET_CONNECTION_URL = parametersInfo[0];

    /**
     * Optional {@code String} DataStore parameter supplying the user name to use when the server
     * requires HTTP authentication
     * <p>
     * Shall be used together with {@link #PASSWORD} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final WSFactoryParam<String> USERNAME = parametersInfo[1];

    /**
     * Optional {@code String} DataStore parameter supplying the password to use when the server
     * requires HTTP authentication
     * <p>
     * Shall be used together with {@link #USERNAME} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final WSFactoryParam<String> PASSWORD = parametersInfo[2];

    /**
     * Optional {@code Integer} DataStore parameter indicating a timeout in milliseconds for the
     * HTTP connections.
     * 
     * @TODO: specify if its just a connection timeout or also a read timeout
     */
    public static final WSFactoryParam<Integer> TIMEOUT = parametersInfo[3];

    /**
     * Optional {@code Boolean} data store parameter indicating whether to set the accept GZip
     * encoding on the HTTP request headers sent to the server
     */
    public static final WSFactoryParam<Boolean> TRY_GZIP = parametersInfo[4];

    /**
     * Optional positive {@code Integer} used as a hard limit for the amount of Features to retrieve
     * for each FeatureType. A value of zero or not providing this parameter means no limit.
     */
    public static final WSFactoryParam<Integer> MAXFEATURES = parametersInfo[5];

    public static final WSFactoryParam<String> TEMPLATE_NAME = parametersInfo[6];

    public static final WSFactoryParam<String> TEMPLATE_DIRECTORY = parametersInfo[7];

    public static final WSFactoryParam<String> CAPABILITIES_FILE_LOCATION = parametersInfo[8];

    protected Map<Map, XmlDataStore> perParameterSetDataStoreCache = new HashMap();

    /**
     * Requests the WS Capabilities document from the {@link WSDataStoreFactory#URL url} parameter
     * in {@code params} and returns a {@link XmlDataStore} according to the version of the
     * GetCapabilities document returned.
     * <p>
     * Note the {@code URL} provided as parameter must refer to the actual {@code GetCapabilities}
     * request. If you need to specify a preferred version or want the GetCapabilities request to be
     * generated from a base URL build the URL with the
     * {@link #createGetCapabilitiesRequest(URL, Version)} first.
     * </p>
     * 
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public XmlDataStore createDataStore(final Map params) throws IOException {

        if (perParameterSetDataStoreCache.containsKey(params)) {
            return perParameterSetDataStoreCache.get(params);
        }
        final URL getQueryRequest = (URL) GET_CONNECTION_URL.lookUp(params);
        final String user = (String) USERNAME.lookUp(params);
        final String pass = (String) PASSWORD.lookUp(params);
        final int timeoutMillis = (Integer) TIMEOUT.lookUp(params);
        final boolean tryGZIP = (Boolean) TRY_GZIP.lookUp(params);
        final Integer maxFeatures = (Integer) MAXFEATURES.lookUp(params);
        final String templateName = (String) TEMPLATE_NAME.lookUp(params);
        final String templateDirectory = (String) TEMPLATE_DIRECTORY.lookUp(params);
        final String capabilitiesDirectory = (String) CAPABILITIES_FILE_LOCATION.lookUp(params);

        if (((user == null) && (pass != null)) || ((pass == null) && (user != null))) {
            throw new IOException(
                    "Cannot define only one of USERNAME or PASSWORD, must define both or neither");
        }

        final HTTPProtocol http = new SimpleHttpProtocol();
        http.setTryGzip(tryGZIP);
        http.setAuth(user, pass);
        http.setTimeoutMillis(timeoutMillis);

        InputStream capsIn = new FileInputStream(new File(capabilitiesDirectory));

        WSStrategy strategy = determineCorrectStrategy(templateDirectory, templateName);
        WS_Protocol ws = new WS_Protocol(capsIn, strategy, getQueryRequest, http);
        final XmlDataStore dataStore = new WS_DataStore(ws);
        dataStore.setMaxFeatures(maxFeatures);

        perParameterSetDataStoreCache.put(new HashMap(params), dataStore);
        return dataStore;
    }

    static WSStrategy determineCorrectStrategy(String templateDirectory, String templateName) {
        WSStrategy strategy = new DefaultWSStrategy(templateDirectory, templateName);

        logger.info("Using WS Strategy: " + strategy.getClass().getName());
        return strategy;
    }

    /**
     * Unsupported operation, can't create a WS service.
     * 
     * @throws UnsupportedOperationException
     *             always, as this operation is not applicable to WS.
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(final Map params) throws IOException {
        throw new UnsupportedOperationException("Operation not applicable to a WS service");
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "The XmlDataStore represents a connection to a Web Feature Server. This connection provides access to the Features published by the server, and the ability to perform transactions on the server (when supported / allowed).";
    }

    /**
     * Returns the set of parameter descriptors needed to connect to a WS.
     * 
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     * @see #URL
     * @see #PROTOCOL
     * @see #USERNAME
     * @see #PASSWORD
     * @see #TIMEOUT
     * @see #BUFFER_SIZE
     * @see #TRY_GZIP
     * @see #LENIENT
     * @see #ENCODING
     */
    public Param[] getParametersInfo() {
        int length = parametersInfo.length;
        Param[] params = new Param[length];
        System.arraycopy(parametersInfo, 0, params, 0, length);
        return params;
    }

    /**
     * Checks whether {@code params} contains a valid set of parameters to connecto to a WS.
     * <p>
     * Rules are:
     * <ul>
     * <li>the mandatory {@link #URL} is provided.
     * <li>whether both {@link #USERNAME} and {@link #PASSWORD} are provided, or none.
     * </ul>
     * Availability of the other optional parameters is not checked for existence.
     * </p>
     * 
     * @param params
     *            non null map of datastore parameters.
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(final Map params) {
        if (params == null) {
            throw new NullPointerException("params");
        }
        try {
            // manditory fields
            GET_CONNECTION_URL.lookUp(params);
            TEMPLATE_NAME.lookUp(params);
            TEMPLATE_DIRECTORY.lookUp(params);
            CAPABILITIES_FILE_LOCATION.lookUp(params);
        } catch (Exception e) {
            return false;
        }

        // check password / username
        if (params.containsKey(USERNAME.key)) {
            if (!params.containsKey(PASSWORD.key)) {
                return false; // must have both
            }
        } else {
            if (params.containsKey(PASSWORD.key)) {
                return false; // must have both
            }
        }
        return true;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDisplayName()
     */
    public String getDisplayName() {
        return "Web Feature Server";
    }

    /**
     * @return {@code true}, no extra or external requisites for datastore availability.
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Creates a HTTP GET Method based WS {@code GetCapabilities} request for the given protocol
     * version.
     * <p>
     * If the query string in the {@code host} URL already contains a VERSION number, that version
     * is <b>discarded</b>.
     * </p>
     * 
     * @param host
     *            non null URL from which to construct the WS {@code GetCapabilities} request by
     *            discarding the query string, if any, and appending the propper query string.
     * @return
     */
    public static URL createGetCapabilitiesRequest(URL host, Version version) {
        if (host == null) {
            throw new NullPointerException("null url");
        }
        if (version == null) {
            throw new NullPointerException("version");
        }
        HTTPProtocol httpUtils = new SimpleHttpProtocol();
        Map<String, String> getCapsKvp = new HashMap<String, String>();
        getCapsKvp.put("SERVICE", "WS");
        getCapsKvp.put("REQUEST", "GetCapabilities");
        getCapsKvp.put("VERSION", version.toString());
        URL getcapsUrl;
        try {
            getcapsUrl = httpUtils.createUrl(host, getCapsKvp);
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Can't create GetCapabilities request from " + host, e);
            throw new RuntimeException(e);
        }

        return getcapsUrl;
    }

    /**
     * Creates a HTTP GET Method based WS {@code GetCapabilities} request.
     * <p>
     * If the query string in the {@code host} URL already contains a VERSION number, that version
     * is used, otherwise the queried version will be 1.0.0.
     * </p>
     * <p>
     * <b>NOTE</b> the default version will be 1.0.0 until the support for 1.1.0 gets stable enough
     * for general use. If you want to use a 1.1.0 WS you'll have to explicitly provide the
     * VERSION=1.1.0 parameter in the GetCapabilities request meanwhile.
     * </p>
     * 
     * @param host
     *            non null URL pointing either to a base WS service access point, or to a full
     *            {@code GetCapabilities} request.
     * @return
     */
    public static URL createGetCapabilitiesRequest(final URL host) {
        if (host == null) {
            throw new NullPointerException("url");
        }

        String queryString = host.getQuery();
        queryString = queryString == null || "".equals(queryString.trim()) ? "" : queryString
                .toUpperCase();

        final Version defaultVersion = Version.highest();
        // final Version defaultVersion = Version.v1_0_0;
        // which version to use
        Version requestVersion = defaultVersion;

        if (queryString.length() > 0) {

            Map<String, String> params = new HashMap<String, String>();
            String[] split = queryString.split("&");
            for (String kvp : split) {
                int index = kvp.indexOf('=');
                String key = index > 0 ? kvp.substring(0, index) : kvp;
                String value = index > 0 ? kvp.substring(index + 1) : null;
                params.put(key, value);
            }

            String request = params.get("REQUEST");
            if ("GETCAPABILITIES".equals(request)) {
                String version = params.get("VERSION");
                if (version != null) {
                    requestVersion = Version.find(version);
                    if (requestVersion == null) {
                        requestVersion = defaultVersion;
                    }
                }
            }
        }
        return createGetCapabilitiesRequest(host, requestVersion);
    }

    /**
     * Package visible to be overridden by unit test.
     * 
     * @param capabilitiesUrl
     * @param tryGZIP
     * @param auth
     * @return
     * @throws IOException
     */
    byte[] loadCapabilities(final URL capabilitiesUrl, HTTPProtocol http) throws IOException {
        byte[] wfsCapabilitiesRawData;

        HTTPResponse httpResponse = http.issueGet(capabilitiesUrl, Collections.EMPTY_MAP);
        InputStream inputStream = httpResponse.getResponseStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int readCount;
        while ((readCount = inputStream.read(buff)) != -1) {
            out.write(buff, 0, readCount);
        }
        wfsCapabilitiesRawData = out.toByteArray();
        return wfsCapabilitiesRawData;
    }

    static Document parseCapabilities(InputStream inputStream) throws IOException,
            DataSourceException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document;
        try {
            document = documentBuilder.parse(inputStream);
        } catch (SAXException e) {
            throw new DataSourceException("Error parsing capabilities document", e);
        }
        return document;
    }
}
