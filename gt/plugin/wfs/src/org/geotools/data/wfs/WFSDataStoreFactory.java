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

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSDataStoreFactory extends AbstractDataStoreFactory { //implements DataStoreFactorySpi{

    // note one of the two is required
    /**
     * url
     */
    public static final Param GET_CAPABILITIES_URL = new Param("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            URL.class,
            "Represents a URL to the getCapabilities document. This URL does not need to be altered in any way. GET_CAPABILITIES_URL and SERVER_URL are mutually exclusive. One of the two is required.",
            false);
    /**
     * url
     */
    public static final Param SERVER_URL = new Param("WFSDataStoreFactory:SERVER_URL",
            URL.class,
            "Represents a URL to the wfs server. This URL represents the server bases url, and should have the capability request post-pended. GET_CAPABILITIES_URL and SERVER_URL are mutually exclusive. One of the two is required.");

    // note may not have both, when neither is specified will prefer post
    /**
     * boolean
     */
    public static final Param USE_POST = new Param("WFSDataStoreFactory:USE_POST",
            Boolean.class,
            "This specifies whether to use the POST portions of the getCapabilities document. When false the POST portion of the document should be ignored. When true, the POST portion should be used first. If this attribute is missing, and GET is specified, POST requests will be attempted when GET requests are not supported and POST requests are. If neither USE_POST or USE_GET are included, post will be prefered. USE_POST and USE_GET are muttually exclusive.",
            false);
    /**
     * boolean
     */
    public static final Param USE_GET = new Param("WFSDataStoreFactory:USE_GET",
            Boolean.class,
            "This specifies whether to use the GET portions of the getCapabilities document. When false the GET portion of the document should be ignored. When true, the GET portion should be used first. If this attribute is missing, and POST is specified, GET requests will be attempted when POST requests are not supported and GET requests are. If neither USE_POST or USE_GET are included, post will be prefered. USE_POST and USE_GET are muttually exclusive.",
            false);

    // password stuff -- see java.net.Authentication
    // either both or neither
    /**
     * String
     */
    public static final Param USERNAME = new Param("WFSDataStoreFactory:USERNAME",
            String.class,
            "This allows the user to specify a username. This param should not be used without the PASSWORD param.",
            false);
    /**
     * String
     */
    public static final Param PASSWORD = new Param("WFSDataStoreFactory:PASSWORD",
            String.class,
            "This allows the user to specify a username. This param should not be used without the USERNAME param.",
            false);

    // timeout -- optional
    /**
     * Integer
     */
    public static final Param TIMEOUT = new Param("WFSDataStoreFactory:TIMEOUT",
            Integer.class,
            "This allows the user to specify a timeout in milliseconds. This param has a default value of 3000ms.",
            false);

    // buffer size -- optional
    /**
     * Integer
     */
    public static final Param BUFFER_SIZE = new Param("WFSDataStoreFactory:BUFFER_SIZE",
            Integer.class,
            "This allows the user to specify a buffer size in features. This param has a default value of 10 features.",
            false);
    private Map cache = new HashMap();
    private Logger logger = Logger.getLogger("org.geotools.data.wfs");

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        // TODO check that we can use hashcodes in this manner -- think it's ok, particularily for regular usage
        if (cache.containsKey(params)) {
            return (DataStore) cache.get(params);
        }

        return createNewDataStore(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        URL host = null;

        if (params.containsKey(SERVER_URL.key)) {
            host = (URL) SERVER_URL.lookUp(params);
            host = WFSDataStore.createGetCapabilitiesRequest(host);
        } else {
            host = ((URL) GET_CAPABILITIES_URL.lookUp(params));
        }

        Boolean get;
        Boolean post;
        get = post = null;

        if (params.containsKey(USE_GET.key)) {
            get = (Boolean) USE_GET.lookUp(params);
        }

        if (params.containsKey(USE_POST.key)) {
            post = (Boolean) USE_POST.lookUp(params);
        }

        // sHould be for true only ... TODO fix this up
        //        if(get != null && post != null)
        //            throw new IOException("Cannot define both get and post");
        String user;

        // sHould be for true only ... TODO fix this up
        //        if(get != null && post != null)
        //            throw new IOException("Cannot define both get and post");
        String pass;
        user = pass = null;

        int timeout = 3000;
        int buffer = 10;

        if (params.containsKey(TIMEOUT.key)) {
            Integer i = (Integer) TIMEOUT.lookUp(params);
            if(i!=null)
                timeout = i.intValue();
        }

        if (params.containsKey(BUFFER_SIZE.key)) {
            Integer i = (Integer) BUFFER_SIZE.lookUp(params);
            if(i!=null)
                buffer = i.intValue();
        }

        if (params.containsKey(USERNAME.key)) {
            user = (String) USERNAME.lookUp(params);
        }

        if (params.containsKey(PASSWORD.key)) {
            pass = (String) PASSWORD.lookUp(params);
        }

        if (((user == null) && (pass != null))
                || ((pass == null) && (user != null))) {
            throw new IOException(
                "Cannot define only one of USERNAME or PASSWORD, muct define both or neither");
        }

        DataStore ds = null;

        try {
            ds = new WFSDataStore(host, get, post, user, pass, timeout, buffer);
            cache.put(params, ds);
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
        return new Param[] {
            GET_CAPABILITIES_URL, SERVER_URL, USE_POST, USE_GET, USERNAME,
            PASSWORD
        };
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
        if (params.containsKey(GET_CAPABILITIES_URL.key)) {
            if (params.containsKey(SERVER_URL.key)) {
                return false; // cannot have both
            }
        } else {
            if (!params.containsKey(SERVER_URL.key)) {
                return false; // must have atleast one
            }
        }

        // check post / get
        Boolean get;

        // check post / get
        Boolean post;
        post = get = null;

        if (params.containsKey(USE_POST.key)) {
            try {
                post = (Boolean) USE_POST.lookUp(params);
            } catch (IOException e) {
                return false;
            }
        }

        if (params.containsKey(USE_GET.key)) {
            try {
                get = (Boolean) USE_GET.lookUp(params);
            } catch (IOException e) {
                return false;
            }
        }

        if (((post != null) && post.booleanValue() && (get != null)
                && get.booleanValue())
                || ((post != null) && !post.booleanValue() && (get != null)
                && !get.booleanValue())) {
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
        return "WFSDataStore";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }
}
