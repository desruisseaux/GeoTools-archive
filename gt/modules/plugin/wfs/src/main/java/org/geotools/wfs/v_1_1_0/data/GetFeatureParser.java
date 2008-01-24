package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;

public interface GetFeatureParser {

    /**
     * @return the next feature in the stream or {@code null} if there are no
     *         more features to parse.
     */
    SimpleFeature parse() throws IOException;

    void close() throws IOException;
}
