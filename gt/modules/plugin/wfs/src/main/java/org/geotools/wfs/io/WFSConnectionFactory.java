package org.geotools.wfs.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.geotools.data.ows.OperationType;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.v_1_0_0.data.LogInputStream;

/**
 * Handles seting up connections to a WFS taking care of GZIP and authentication
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public class WFSConnectionFactory {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.wfs.io");

    private final WFSCapabilities capabilities;

    private final boolean tryGzip;

    private final Authenticator auth;

    private String encoding;

    public WFSConnectionFactory(final WFSCapabilities capabilities, final boolean tryGzip,
            final Authenticator auth, String encoding) {
        this.capabilities = capabilities;
        this.tryGzip = tryGzip;
        this.auth = auth;
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public HttpURLConnection createGetCapabilitiesConnection(final boolean doPost)
            throws IOException {
        OperationType getCapabilities = capabilities.getGetCapabilities();
        return getConnection(getCapabilities, doPost);
    }

    public HttpURLConnection createDescribeFeatureTypeConnection(final String typeName,
            final boolean doPost) throws IOException {
        URL query;
        if (doPost) {
            query = capabilities.getDescribeFeatureType().getPost();
        } else {
            query = getDescribeFeatureTypeURLGet(typeName);
        }
        if (query == null) {
            return null;
        }
        return getConnection(query, doPost);
    }

    public HttpURLConnection getConnection(URL query, boolean doPost) throws IOException {
        return getConnection(query, tryGzip, doPost, auth);
    }

    public URL getDescribeFeatureTypeURLGet(String typeName) throws MalformedURLException {
        URL getUrl = capabilities.getDescribeFeatureType().getGet();
        Logging.getLogger("org.geotools.data.communication").fine("Output: " + getUrl);

        if (getUrl == null) {
            return null;
        }

        String query = getUrl.getQuery();
        query = query == null ? null : query.toUpperCase();
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
            url += "&VERSION=" + capabilities.getVersion();
        }

        if ((query == null) || (query.indexOf("REQUEST") == -1)) {
            url += "&REQUEST=DescribeFeatureType";
        }

        url += ("&TYPENAME=" + typeName);

        getUrl = new URL(url);
        return getUrl;
    }

    private HttpURLConnection getConnection(final OperationType operation, final boolean isPost)
            throws IOException {
        final URL url;
        if (isPost) {
            url = operation.getPost();
        } else {
            url = operation.getGet();
        }

        HttpURLConnection connection = getConnection(url, tryGzip, isPost, auth);

        return connection;
    }

    public static HttpURLConnection getConnection(final URL url, final boolean tryGzip,
            final boolean isPost, final Authenticator auth) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (isPost) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "text/xml, application/xml");
        } else {
            connection.setRequestMethod("GET");
        }
        connection.setDoInput(true);
        /*
         * FIXME this could breaks uDig. Not quite sure what to do otherwise.
         * Maybe have a mechanism that would allow an authenticator to ask the
         * datastore itself for a previously supplied user/pass.
         */
        if (auth != null) {
            synchronized (Authenticator.class) {
                Authenticator.setDefault(auth);
                connection.connect();
                Authenticator.setDefault(null);
            }
        }

        if (tryGzip) {
            connection.addRequestProperty("Accept-Encoding", "gzip");
        }

        return connection;
    }

    public InputStream getInputStream(HttpURLConnection hc) throws IOException {
        return getInputStream(hc, tryGzip);
    }

    /**
     * If the field useGZIP is true Adds gzip to the connection accept-encoding
     * property and creates a gzip inputstream (if server supports it).
     * Otherwise returns a normal buffered input stream.
     * 
     * @param hc
     *            the connection to use to create the stream
     * @return an input steam from the provided connection
     */
    public static InputStream getInputStream(final HttpURLConnection hc, final boolean tryGZIP)
            throws IOException {
        InputStream is = hc.getInputStream();

        if (tryGZIP) {
            if (hc.getContentEncoding() != null && hc.getContentEncoding().indexOf("gzip") != -1) {
                is = new GZIPInputStream(is);
            }
        }
        is = new BufferedInputStream(is);
        if (LOGGER.isLoggable(Level.FINE)) {
            is = new LogInputStream(is, LOGGER, Level.FINE);
        }
        // special logger for communication information only.
        Logger logger = Logging.getLogger("org.geotools.data.communication");
        if (logger.isLoggable(Level.FINE)) {
            is = new LogInputStream(is, logger, Level.FINE);
        }
        return is;
    }

}
