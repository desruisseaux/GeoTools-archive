package org.geotools.wfs.io;

import static org.geotools.data.wfs.HttpMethod.GET;
import static org.geotools.data.wfs.HttpMethod.POST;
import static org.geotools.data.wfs.WFSOperationType.DESCRIBE_FEATURETYPE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.geotools.data.wfs.HttpMethod;
import org.geotools.data.wfs.Version;
import org.geotools.data.wfs.WFSOperationType;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.v_1_0_0.data.LogInputStream;

/**
 * Handles setting up connections to a WFS based on a WFS capabilities document,
 * taking care of GZIP and authentication.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/io/WFSConnectionFactory.java $
 */
public abstract class WFSConnectionFactory {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.wfs.io");

    private Version wfsVersion;
    
    private final boolean tryGzip;

    private final Authenticator auth;

    private String encoding;

    public WFSConnectionFactory(final Version version, final boolean tryGzip, final Authenticator auth, String encoding) {
        this.wfsVersion = version;
        this.tryGzip = tryGzip;
        this.auth = auth;
        this.encoding = encoding;
    }

    public Version getVersion(){
        return wfsVersion;
    }

    /**
     * Returns whether the service supports the given operation for the given
     * HTTP method.
     * 
     * @param operation
     * @param method
     * @return
     */
    public abstract boolean supports(WFSOperationType operation, HttpMethod method);

    /**
     * 
     * @param operation
     * @param method
     * @return The URL access point for the given operation and method
     * @throws UnsupportedOperationException
     *             if the combination operation/method is not supported by the
     *             service
     * @see #supports(WFSOperationType, HttpMethod)
     */
    public abstract URL getOperationURL(WFSOperationType operation, HttpMethod method)
            throws UnsupportedOperationException;

    /**
     * Returns the preferred character encoding name to encode requests in
     * 
     * @return
     */
    public String getEncoding() {
        return encoding;
    }

    public HttpURLConnection createDescribeFeatureTypeConnection(final String typeName,
            HttpMethod method) throws IOException, IllegalArgumentException {
        URL query;
        if (HttpMethod.POST == method) {
            query = getOperationURL(WFSOperationType.DESCRIBE_FEATURETYPE, HttpMethod.POST);
        } else {
            query = getDescribeFeatureTypeURLGet(typeName);
        }
        if (query == null) {
            return null;
        }
        return getConnection(query, method);
    }

    public HttpURLConnection getConnection(URL query, HttpMethod method) throws IOException {
        return getConnection(query, tryGzip, method, auth);
    }

    public URL getDescribeFeatureTypeURLGet(final String typeName) throws MalformedURLException {
        URL getUrl = getOperationURL(DESCRIBE_FEATURETYPE, GET);
        Logging.getLogger("org.geotools.data.communication").fine("Output: " + getUrl);

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
            url += "&VERSION=" + getVersion();
        }

        if ((query == null) || (query.indexOf("REQUEST") == -1)) {
            url += "&REQUEST=DescribeFeatureType";
        }

        url += ("&TYPENAME=" + typeName);

        getUrl = new URL(url);
        return getUrl;
    }

    public static HttpURLConnection getConnection(final URL url, final boolean tryGzip,
            final HttpMethod method, final Authenticator auth) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (POST == method) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.wfs.io.WFSProtocolManager#getInputStream(java.net.HttpURLConnection)
     */
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
