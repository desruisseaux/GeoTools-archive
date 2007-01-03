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
package org.geotools.data.postgis;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * A feature reader for versioned features. It assumes the internal reader has
 * already been configured not to return versioning columns, so simply handles
 * FID mutation.
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class VersionedFeatureReader implements FeatureReader {

    private FeatureReader wrapped;

    private VersionedFIDMapper fidMapper;

    public VersionedFeatureReader(FeatureReader wrapped, VersionedFIDMapper fidMapper) {
        this.wrapped = wrapped;
        this.fidMapper = fidMapper;
    }

    public void close() throws IOException {
        wrapped.close();
    }

    public FeatureType getFeatureType() {
        return wrapped.getFeatureType();
    }

    public boolean hasNext() throws IOException {
        return wrapped.hasNext();
    }

    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        Feature feature = wrapped.next();
        FeatureType featureType = wrapped.getFeatureType();
        String id = feature.getID();

        return featureType.create(feature
                .getAttributes(new Object[featureType.getAttributeCount()]), fidMapper
                .getUnversionedFid(id));
    }
}
