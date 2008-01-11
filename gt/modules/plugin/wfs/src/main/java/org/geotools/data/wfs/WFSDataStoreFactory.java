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
import java.net.Authenticator;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.v_1_0_0.data.WFSDataStore;
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
     * Default value for whether to set the accepts gzip HTTP header value on
     * requests sent to the server
     */
    private static final boolean DEFAULT_TRY_GZIP = true;

    /**
     * Default value for the LENIENT parameter
     */
    private static final boolean DEFAULT_LENIENT_MODE = true;

    /**
     * Default value for the encoding parameter.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final Param[] parametersInfo = new Param[9];
    static {
        String name;
        Class clazz;
        String description;
        boolean mandatory;
        Object sampleValue;

        name = "WFSDataStoreFactory:GET_CAPABILITIES_URL";
        clazz = URL.class;
        description = "Represents a URL to the getCapabilities document or a server instance.";
        mandatory = true;
        parametersInfo[0] = new Param(name, clazz, description, mandatory);

        name = "WFSDataStoreFactory:PROTOCOL";
        clazz = Boolean.class;
        description = "Sets a preference for the HTTP protocol to use when requesting "
                + "WFS functionality. Set this value to Boolean.TRUE for POST, Boolean.FALSE "
                + "for GET or NULL for AUTO";
        mandatory = false;
        parametersInfo[1] = new Param(name, clazz, description, mandatory);

        name = "WFSDataStoreFactory:USERNAME";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not "
                + "be used without the PASSWORD param.";
        mandatory = false;
        parametersInfo[2] = new Param(name, clazz, description, mandatory);

        name = "WFSDataStoreFactory:PASSWORD";
        clazz = String.class;
        description = "This allows the user to specify a username. This param should not"
                + " be used without the USERNAME param.";
        mandatory = false;
        parametersInfo[3] = new Param(name, clazz, description, mandatory);

        name = "WFSDataStoreFactory:ENCODING";
        clazz = String.class;
        description = "This allows the user to specify the character encoding of the "
                + "XML-Requests sent to the Server.";
        mandatory = false;
        sampleValue = DEFAULT_ENCODING;
        parametersInfo[4] = new Param(name, clazz, description, mandatory, sampleValue);

        name = "WFSDataStoreFactory:TIMEOUT";
        clazz = Integer.class;
        description = "This allows the user to specify a timeout in milliseconds. This param"
                + " has a default value of 3000ms.";
        mandatory = false;
        sampleValue = Integer.valueOf(3000);
        parametersInfo[5] = new Param(name, clazz, description, mandatory, sampleValue);

        name = "WFSDataStoreFactory:BUFFER_SIZE";
        clazz = Integer.class;
        description = "This allows the user to specify a buffer size in features. This param "
                + "has a default value of 10 features.";
        mandatory = false;
        parametersInfo[6] = new Param(name, clazz, description, mandatory, sampleValue);

        name = "WFSDataStoreFactory:TRY_GZIP";
        clazz = Boolean.class;
        description = "Indicates that datastore should use gzip to transfer data if the server "
                + "supports it. Default is true";
        mandatory = false;
        parametersInfo[7] = new Param(name, clazz, description, mandatory, sampleValue);

        name = "WFSDataStoreFactory:LENIENT";
        clazz = Boolean.class;
        description = "Indicates that datastore should do its best to create features from the "
                + "provided data even if it does not accurately match the schema.  Errors will "
                + "be logged but the parsing will continue if this is true.  Default is false";
        mandatory = false;
        parametersInfo[8] = new Param(name, clazz, description, mandatory, sampleValue);
    }

    /**
     * Mandatory DataStore parameter indicating the URL for the WFS
     * GetCapabilities document.
     */
    public static final Param URL = parametersInfo[0];

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
    public static final Param PROTOCOL = parametersInfo[1];

    /**
     * Optional {@code String} DataStore parameter supplying the user name to
     * use when the server requires HTTP authentication
     * <p>
     * Shall be used together with {@link #PASSWORD} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final Param USERNAME = parametersInfo[2];

    /**
     * Optional {@code String} DataStore parameter supplying the password to use
     * when the server requires HTTP authentication
     * <p>
     * Shall be used together with {@link #USERNAME} or not used at all.
     * </p>
     * 
     * @see Authenticator
     */
    public static final Param PASSWORD = parametersInfo[3];

    /**
     * Optional {@code String} DataStore parameter supplying a JVM supported
     * {@link Charset charset} name to use as the character encoding for XML
     * requests sent to the server.
     */
    public static final Param ENCODING = parametersInfo[4];

    /**
     * Optional {@code Integer} DataStore parameter indicating a timeout in
     * milliseconds for the HTTP connections.
     * 
     * @TODO: specify if its just a connection timeout or also a read timeout
     */
    public static final Param TIMEOUT = parametersInfo[5];

    /**
     * Optional {@code Integer} parameter stating how many Feature instances to
     * buffer at once. Only implemented for WFS 1.1.0 support.
     */
    public static final Param BUFFER_SIZE = parametersInfo[6];

    /**
     * Optional {@code Boolean} data store parameter indicating whether to set
     * the accept GZip encoding on the HTTP request headers sent to the server
     */
    public static final Param TRY_GZIP = parametersInfo[7];

    /**
     * Optional {@code Boolean} DataStore parameter indicating whether to be
     * lenient about parsing bad data
     */
    public static final Param LENIENT = parametersInfo[8];

    protected Map cache = new HashMap();

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(final Map params) throws IOException {
        // TODO check that we can use hashcodes in this manner -- think it's ok,
        // particularily for regular usage
        if (cache.containsKey(params)) {
            return (DataStore) cache.get(params);
        }
        URL host = null;

        if (params.containsKey(URL.key)) {
            host = (URL) URL.lookUp(params);
        }

        Boolean protocol = null;
        if (params.containsKey(PROTOCOL.key)) {
            protocol = (Boolean) PROTOCOL.lookUp(params);
        }

        String user, pass;
        user = pass = null;

        int timeout = 3000;
        int buffer = 10;
        boolean tryGZIP = DEFAULT_TRY_GZIP;
        boolean lenient = DEFAULT_LENIENT_MODE;
        String encoding = null;

        if (params.containsKey(TIMEOUT.key)) {
            Integer i = (Integer) TIMEOUT.lookUp(params);
            if (i != null)
                timeout = i.intValue();
        }

        if (params.containsKey(BUFFER_SIZE.key)) {
            Integer i = (Integer) BUFFER_SIZE.lookUp(params);
            if (i != null)
                buffer = i.intValue();
        }

        if (params.containsKey(TRY_GZIP.key)) {
            Boolean b = (Boolean) TRY_GZIP.lookUp(params);
            if (b != null)
                tryGZIP = b.booleanValue();
        }

        if (params.containsKey(LENIENT.key)) {
            Boolean b = (Boolean) LENIENT.lookUp(params);
            if (b != null)
                lenient = b.booleanValue();
        }

        if (params.containsKey(USERNAME.key)) {
            user = (String) USERNAME.lookUp(params);
        }

        if (params.containsKey(PASSWORD.key)) {
            pass = (String) PASSWORD.lookUp(params);
        }

        if (params.containsKey(ENCODING.key)) {
            encoding = (String) ENCODING.lookUp(params);
        }

        if (((user == null) && (pass != null)) || ((pass == null) && (user != null))) {
            throw new IOException(
                    "Cannot define only one of USERNAME or PASSWORD, muct define both or neither");
        }

        DataStore ds = null;

        try {
            ds = new WFSDataStore(host, protocol, user, pass, timeout, buffer, tryGZIP, lenient,
                    encoding);
            cache.put(params, ds);
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
}
