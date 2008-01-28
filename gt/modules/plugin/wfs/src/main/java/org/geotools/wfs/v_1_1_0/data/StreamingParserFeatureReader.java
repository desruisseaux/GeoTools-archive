package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.DataSourceException;
import org.geotools.xml.Configuration;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

/**
 * {@link GetFeatureParser} for {@link WFSFeatureReader} that uses the geotools
 * {@link StreamingParser} to fetch Features out of a WFS GetFeature response.
 * 
 * @author Gabriel Roldan
 * @version $Id: StreamingParserFeatureReader.java 28937 2008-01-25 10:52:22Z
 *          desruisseaux $
 * @since 2.5.x
 * @source $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0/data/StreamingParserFeatureReader.java $
 */
class StreamingParserFeatureReader implements GetFeatureParser {

    private StreamingParser parser;

    private InputStream inputStream;

    public StreamingParserFeatureReader(Configuration configuration, InputStream input,
            QName featureName) throws DataSourceException {
        this.inputStream = input;
        try {
            this.parser = new StreamingParser(configuration, input, featureName);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException(e);
        } catch (SAXException e) {
            if (e.getCause() == null && e.getException() != null) {
                e.initCause(e.getException());
            }
            throw new DataSourceException(e);
        }
    }

    /**
     * @see GetFeatureParser#close()
     */
    public void close() throws IOException {
        if (inputStream != null) {
            try {
                inputStream.close();
            } finally {
                inputStream = null;
                parser = null;
            }
        }
    }

    /**
     * @see GetFeatureParser#parse()
     */
    public SimpleFeature parse() throws IOException {
        Object parsed = parser.parse();
        SimpleFeature feature = (SimpleFeature) parsed;
        if (feature != null)
            System.out.println(feature.getAttribute("the_geom"));// TODO:REMOVE!!
        return feature;
    }

}
