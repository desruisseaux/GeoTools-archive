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
 */
package org.geotools.data.shapefile.shp;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 *
 * @source $URL$
 */
public class createTestData {
    public static URL createLineData(final Dimension d)
        throws Exception {
        File file = new File("test_lines.shp");

        if (file.exists()) {
            file.delete();
        }

        IndexedShapefileDataStoreFactory factory = new IndexedShapefileDataStoreFactory();
        DataStore datastore = factory.createDataStore(file.toURL());
        FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance(
                "test_lines");
        builder.setDefaultGeometry((GeometryAttributeType) AttributeTypeFactory
            .newAttributeType("geom", LineString.class));
        builder.addType(AttributeTypeFactory.newAttributeType("x", Integer.class));
        builder.addType(AttributeTypeFactory.newAttributeType("y", Integer.class));

        final FeatureType featureType = builder.getFeatureType();
        datastore.createSchema(featureType);

        FeatureStore store = (FeatureStore) datastore.getFeatureSource(
                "test_lines");
        store.addFeatures(new FeatureReader() {
                public FeatureType getFeatureType() {
                    return featureType;
                }

                GeometryFactory factory = new GeometryFactory();
                int x = 0;
                int y = 0;

                public Feature next()
                    throws IOException, IllegalAttributeException, 
                        NoSuchElementException {
                    LineString geom = factory.createLineString(new Coordinate[] {
                                new Coordinate(x + 0.0, y + 0.0),
                                new Coordinate(x + .9, y + 0.9)
                            });
                    Feature feature = featureType.create(new Object[] {
                                geom, new Integer(x), new Integer(y)
                            });

                    if (x == (d.width - 1)) {
                        y++;
                        x = 0;
                    } else {
                        x++;
                    }

                    return feature;
                }

                public boolean hasNext() throws IOException {
                    return y < d.height;
                }

                public void close() throws IOException {
                    // TODO Auto-generated method stub
                }
            });

        return file.toURL();
    }

    /**
     * DOCUMENT ME!
     *
     * @param args
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println(createLineData(new Dimension(512, 512)));
    }
}
