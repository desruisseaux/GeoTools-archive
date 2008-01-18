/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.WFSCapabilitiesType;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.wfs.io.WFSConnectionFactory;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.xml.sax.SAXException;

/**
 * A WFS 1.1 DataStore implementation.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0/data/WFSDataStore.java $
 */
public final class WFS_1_1_0_DataStore implements WFSDataStore {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.wfs.v_1_1_0");

    private static WFSConfiguration configuration;

    /**
     * The WFS GetCapabilities document. Final by now, as we're not handling
     * updatesequence, so will not ask the server for an updated capabilities
     * during the life-time of this datastore.
     */
    private final WFSCapabilitiesType capabilities;

    private final Map<String, FeatureTypeType> typeInfos;

    private final Map<String, SimpleFeatureType> featureTypeCache;

    private WFSConnectionFactory connectionFactory;

    /**
     * The WFS capabilities document.
     * 
     * @param capabilities
     */
    @SuppressWarnings("unchecked")
    public WFS_1_1_0_DataStore(final WFSCapabilitiesType capabilities,
            final WFSConnectionFactory connectionFactory) {
        this.capabilities = capabilities;
        this.connectionFactory = connectionFactory;
        this.typeInfos = new HashMap<String, FeatureTypeType>();
        this.featureTypeCache = new HashMap<String, SimpleFeatureType>();
        synchronized (WFS_1_1_0_DataStore.class) {
            if (configuration == null) {
                configuration = new WFSConfiguration();
            }
        }
        final List<FeatureTypeType> ftypes = capabilities.getFeatureTypeList().getFeatureType();
        String prefixedTypeName;
        for (FeatureTypeType ftype : ftypes) {
            prefixedTypeName = ftype.toString();
            typeInfos.put(prefixedTypeName, ftype);
        }
    }

    public ServiceInfo getInfo() {
        return new ServiceInfo(){
            public String getDescription() {
                return getAbstract();
            }

            public Icon getIcon() {
                return null; // talk to Eclesia the icons are in renderer?
            }
            public Set<String> getKeywords() {
                return getKeywords();
            }

            public URI getPublisher() {
                return null; // help?
            }

            public URI getSchema() {
                return null; // WFS 1.0.0 uri here
            }

            public URI getSource() {
                throw new UnsupportedOperationException("Not implemented yet");
            }

            public String getTitle() {
                throw new UnsupportedOperationException("Not implemented yet");
            }            
        };
    }
    
    /**
     * @see WFSDataStore#getTitle()
     */
    public String getTitle() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see WFSDataStore#getAbstract()
     */
    public String getAbstract() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see WFSDataStore#getKeywords()
     */
    public List<String> getKeywords() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see WFSDataStore#getTitle(String)
     */
    public String getTitle(String typeName) throws NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see WFSDataStore#getAbstract(String)
     */
    public String getAbstract(String typeName) throws NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see WFSDataStore#getLatLonBoundingBox(String)
     */
    public ReferencedEnvelope getLatLonBoundingBox(String typeName) throws NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @param
     * @return the GeoTools FeatureType for the {@code typeName} as stated on
     *         the capabilities document.
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public SimpleFeatureType getSchema(final String typeName) throws IOException {
        if (featureTypeCache.containsKey(typeName)) {
            return featureTypeCache.get(typeName);
        }
        final URL describeUrl = connectionFactory.getDescribeFeatureTypeURLGet(typeName);
        final HttpURLConnection connection = connectionFactory.getConnection(describeUrl, false);
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
        InputStream in = connectionFactory.getInputStream(connection);
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
        SimpleFeatureType ftype = (SimpleFeatureType) parsed;
        synchronized (featureTypeCache) {
            featureTypeCache.put(typeName, ftype);
        }
        return ftype;
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName) throws IOException {
        return null;
    }

    /**
     * @return {@code null}, no lock support so far
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     * @see DefaultView
     */
    public FeatureSource getView(final Query query) throws IOException, SchemaException {
        final String typeName = query.getTypeName();
        final FeatureSource featureSource = this.getFeatureSource(typeName);
        return new DefaultView(featureSource, query);
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS DataStore does not supports updateSchema");
    }

    /**
     * @see org.geotools.data.DataStore#createSchema(org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS DataStore does not support createSchema");
    }
}
