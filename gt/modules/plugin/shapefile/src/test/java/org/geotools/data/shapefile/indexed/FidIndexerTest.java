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
package org.geotools.data.shapefile.indexed;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.shapefile.indexed.FidIndexer;
import org.geotools.data.shapefile.indexed.IndexedFidReader;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import java.net.URL;


public class FidIndexerTest extends FIDTestCase {
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.geotools.index.fid.FidIndexer.generate(URL)'
     */
    public void testGenerate() throws Exception {
        URL url = FidIndexer.generate(backshp.toURL());

        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(backshp
                .toURL(), null, false, false, (byte) 0);

        FeatureSource fs = ds.getFeatureSource();
        int features = fs.getCount(Query.ALL);

        
		IndexedFidReader reader = new IndexedFidReader(TYPE_NAME,
                FidIndexer.getReadChannel(url));

        try {
            assertEquals(features, reader.getCount());

            int i = 1;

            while (reader.hasNext()) {
                assertEquals(TYPE_NAME+"." + i, reader.next());
                assertEquals(TYPE_NAME+"."+i, i-1, reader.currentIndex());
                i++;
            }

            assertEquals(features, i-1);
        } finally {
            reader.close();
        }
    }
}
