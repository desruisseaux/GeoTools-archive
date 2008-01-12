/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.io.WFSConnectionFactory;
import org.geotools.wfs.v_1_0_0.data.WFSDataStore;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * 
 * @author dzwiers
 * @author Gabriel Roldan (TOPP)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/WFSDataStoreFactory.java $
 */
@SuppressWarnings("unchecked")
public class WFSDataStoreFactory extends AbstractDataStoreFactory {
    private static final Logger logger = Logging.getLogger("org.geotools.data.wfs");

    /**
     * A {@link Param} subclass that allows to provide a default value to the
     * lookUp method.
     * 
     * @author Gabriel Roldan
     * @version $Id$
     * @since 2.5.x
     * @URL $URL$
     */
    public static class WFSFactoryParam<T> extends Param {
        private T defaultValue;

        /**
         * Creates a required parameter
         * 
         * @param key
         * @param type
         * @param description
         */
        public WFSFactoryParam(String key, Class type, String description) {
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
        public WFSFactoryParam(String key, Class type, String description, T defaultValue) {
            super(key, type, description, false);
            this.defaultValue = defaultValue;
        }

        public T lookUp(final Map params) throws IOException {
            T parameter = (T) super.lookUp(params);
            return parameter == null ? defaultValue : parameter;
        }
    }

    /**
     * A simple user/password authenticator
     * 
     * @author Gabriel Roldan
     * @version $Id$
     * @since 2.5.x
     * @URL $URL$
     */
    private static class WFSAuthenticator extends Authenticator {
        private java.net.PasswordAuthentication pa;

        /**
         * 
         * @param user
         * @param pass
         */
        public WFSAuthenticator(String user, String pass) {
            pa = new java.net.PasswordAuthentication(user, pass.toCharArray());
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return pa;
        }
    }

    private static final WFSFactoryParam[] parametersInfo = new WFSFactoryParam[9];
    static {
        String name;
        Class clazz;
        String description;

        name = "WFSDataStoreFactory:GET_CAPABILITIES_URL";
        clazz = URL.class;
        description = "Represents a URL to the getCapabilities document or a server instance.";
        parametersInfo[0] = new WFSFactoryParam(name, clazz, description);

        name = "WFSDataStoreFactory:PROTOCOL";
        clazz = Boolean.class;
        description = "Sets a preference for the HTTP protocol to use when requesting "
                + "WFS functionality. Set this value to Boolean.TRUE for POST, Boolean.FALSE "
                + "for GET or NULL for AUTO";
        parametersInfo[1] = new WFSFactoryParam(name, clazz, description, (Boolean) null);

        name = "WFSDataStoreFactory:USERNAME";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not "
                + "be used without the PASSWORD param.";
        parametersInfo[2] = new WFSFactoryParam(name, clazz, description, (String) null);

        name = "WFSDataStoreFactory:PASSWORD";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not"
                + " be used without the USERNAME param.";
        parametersInfo[3] = new WFSFactoryParam(name, clazz, description, (String) null);

        name = "WFSDataStoreFactory:ENCODING";
        clazz = String.class;
        description = "This allows the user to specify the character encoding of the "
                + "XML-Requests sent to the Server. Defaults to UTF-8";
        parametersInfo[4] = new WFSFactoryParam(name, clazz, description, "UTF-8");

        name = "WFSDataStoreFactory:TIMEOUT";
        clazz = Integer.class;
        description = "This allows the user to specify a timeout in milliseconds. This param"
                + " has a default value of 3000ms.";
        parametersInfo[5] = new WFSFactoryParam(name, clazz, description, Integer.valueOf(3000));

        name = "WFSDataStoreFactory:BUFFER_SIZE";
        clazz = Integer.class;
        description = "This allows the user to specify a buffer size in features. This param "
                + "has a default value of 10 features.";
        parametersInfo[6] = new WFSFactoryParam(name, clazz, description, Integer.valueOf(10));

        name = "WFSDataStoreFactory:TRY_GZIP";
        clazz = Boolean.class;
        description = "Indicates that datastore should use gzip to transfer data if the server "
                + "supports it. Default is true";
        parametersInfo[7] = new WFSFactoryParam(name, clazz, description, Boolean.TRUE);

        name = "WFSDataStoreFactory:LENIENT";
        clazz = Boolean.class;
        description = "Indicates that datastore should do its best to create features from the "
                + "provided data even if it does not accurately match the schema.  Errors will "
                + "be logged but the parsing will continue if this is true.  Default is false";
        parametersInfo[8] = new WFSFactoryParam(name, clazz, description, Boolean.FALSE);
    }

    /**
     * Mandatory DataStore parameter indicating the URL for the WFS
     * GetCapabilities document.
     */
    public static final WFSFactoryParam<URL> URL = parametersInfo[0];

    /**
     * Optional {@code Boolean} DataStore parameter acting as a hint for the
     * HTTP protocol to use preferably against the WFS instance, with the
     * following semantics:
     * <ul>
     * <li>{@code null} (not supplied): use "AUTO", let the DataStore decide.
     * <li>{@code Boolean.TRUE} use HTTP POST preferably.
     * <li> {@code Boolean.FALSE} use HTTP GET preferably.
     * </ul>
     */
    public static final WFSFactoryParam<Boolean> PROTOCOL = parametersInfo[1];

    /**
     * Optional {@code String} DataStore parameter supplying the user name to
     * use when the server requires HTTP authentication
     * <p>
     * Shall be used together with {@link #PASSWORD} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final WFSFactoryParam<String> USERNAME = parametersInfo[2];

    /**
     * Optional {@code String} DataStore parameter supplying the password to use
     * when the server requires HTTP authentication
     * <p>
     * Shall be used together with {@link #USERNAME} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final WFSFactoryParam<String> PASSWORD = parametersInfo[3];

    /**
     * Optional {@code String} DataStore parameter supplying a JVM supported
     * {@link Charset charset} name to use as the character encoding for XML
     * requests sent to the server.
     */
    public static final WFSFactoryParam<String> ENCODING = parametersInfo[4];

    /**
     * Optional {@code Integer} DataStore parameter indicating a timeout in
     * milliseconds for the HTTP connections.
     * 
     * @TODO: specify if its just a connection timeout or also a read timeout
     */
    public static final WFSFactoryParam<Integer> TIMEOUT = parametersInfo[5];

    /**
     * Optional {@code Integer} parameter stating how many Feature instances to
     * buffer at once. Only implemented for WFS 1.1.0 support.
     */
    public static final WFSFactoryParam<Integer> BUFFER_SIZE = parametersInfo[6];

    /**
     * Optional {@code Boolean} data store parameter indicating whether to set
     * the accept GZip encoding on the HTTP request headers sent to the server
     */
    public static final WFSFactoryParam<Boolean> TRY_GZIP = parametersInfo[7];

    /**
     * Optional {@code Boolean} DataStore parameter indicating whether to be
     * lenient about parsing bad data
     */
    public static final WFSFactoryParam<Boolean> LENIENT = parametersInfo[8];

    protected Map<Map, DataStore> perParameterSetDataStoreCache = new HashMap();

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(final Map params) throws IOException {
        // TODO check that we can use hashcodes in this manner -- think it's ok,
        // particularily for regular usage
        if (perParameterSetDataStoreCache.containsKey(params)) {
            return (DataStore) perParameterSetDataStoreCache.get(params);
        }
        final URL host = URL.lookUp(params);
        final Boolean protocol = PROTOCOL.lookUp(params);
        final String user = USERNAME.lookUp(params);
        final String pass = PASSWORD.lookUp(params);
        final int timeout = TIMEOUT.lookUp(params);
        final int buffer = BUFFER_SIZE.lookUp(params);
        final boolean tryGZIP = TRY_GZIP.lookUp(params);
        final boolean lenient = LENIENT.lookUp(params);
        final String encoding = ENCODING.lookUp(params);

        if (((user == null) && (pass != null)) || ((pass == null) && (user != null))) {
            throw new IOException(
                    "Cannot define only one of USERNAME or PASSWORD, must define both or neither");
        }

        final Authenticator auth;
        if (user != null && pass != null) {
            auth = new WFSAuthenticator(user, pass);
        }else{
            auth = null;
        }

        final WFSCapabilities capabilities = findCapabilities(host, tryGZIP, auth);
        DataStore ds = null;
        final WFSConnectionFactory connectionFac = new WFSConnectionFactory(capabilities, tryGZIP,
                auth, encoding);

        try {
            ds = new WFSDataStore(capabilities, protocol, connectionFac, timeout, buffer, lenient);
            perParameterSetDataStoreCache.put(new HashMap(params), ds);
        } catch (SAXException e) {
            logger.warning(e.toString());
            throw new IOException(e.toString());
        }

        return ds;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(final Map params) throws IOException {
        throw new UnsupportedOperationException("Operation not applicable to a WFS service");
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
        int length = parametersInfo.length;
        Param[] params = new Param[length];
        System.arraycopy(parametersInfo, 0, params, 0, length);
        return params;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        if (params == null) {
            return false;
        }

        // check url
        if (!params.containsKey(URL.key)) {
            return false; // cannot have both
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

        // check for type
        if (params.containsKey(PROTOCOL.key)) {
            try {
                PROTOCOL.lookUp(params);
            } catch (IOException e) {
                return false;
            }
        }

        // check for type
        if (params.containsKey(TIMEOUT.key)) {
            try {
                TIMEOUT.lookUp(params);
            } catch (IOException e) {
                return false;
            }
        }

        if (params.containsKey(BUFFER_SIZE.key)) {
            try {
                BUFFER_SIZE.lookUp(params);
            } catch (IOException e) {
                return false;
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
     * @return {@code true}, no extra or external requisites for datastore
     *         availability.
     * 
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * 
     * @param host
     * @return
     */
    public static URL createGetCapabilitiesRequest(URL host) {
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
            WFSDataStore.LOGGER.warning(e.toString());

            return host;
        }
    }

    private WFSCapabilities findCapabilities(URL host, boolean tryGZIP, Authenticator auth)
            throws IOException {

        // TODO support using POST for getCapabilities

        Object t = null;
        Map hints = new HashMap();
        hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);
        try {
            // try and complete the url first
            URL capabilitiesUrl = createGetCapabilitiesRequest(host);
            HttpURLConnection hc = WFSConnectionFactory.getConnection(capabilitiesUrl, tryGZIP,
                    false, auth);
            InputStream is = WFSConnectionFactory.getInputStream(hc, tryGZIP);
            t = DocumentFactory.getInstance(is, hints, logger.getLevel());
        } catch (Throwable e) {
            // try the url as given second
            HttpURLConnection hc = WFSConnectionFactory.getConnection(host, tryGZIP, false, auth);
            InputStream is = WFSConnectionFactory.getInputStream(hc, tryGZIP);
            try {
                t = DocumentFactory.getInstance(is, hints, logger.getLevel());
            } catch (SAXException saxEx) {
                throw new IOException("Parsing exception: " + saxEx.getMessage(), saxEx);
            }
        }
        if (t instanceof WFSCapabilities) {
            return (WFSCapabilities) t;
        } else {
            throw new IllegalStateException(
                    "The specified URL Should have returned a 'WFSCapabilities' object. Returned a "
                            + ((t == null) ? "null value."
                                    : (t.getClass().getName() + " instance.")));
        }
    }
}
