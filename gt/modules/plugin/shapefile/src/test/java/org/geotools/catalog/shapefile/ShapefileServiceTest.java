/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.catalog.shapefile;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceInfo;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.TestCaseSupport;

public class ShapefileServiceTest extends TestCaseSupport {
    final static String STATE_POP = "shapes/statepop.shp";

    /**
     * Test that shapefile geo resource throws a FileNotFoundException when the
     * file does not exist.
     * 
     */
    public void testShapefileNotExists() throws Exception {
        URI uri = new URI("file:///home/nouser/nofile.shp");
        HashMap params = new HashMap();
        params.put(ShapefileDataStoreFactory.URLP.key, uri.toURL());

        Service service = new ShapefileService(null, uri, params);
        try {
            service.getInfo(null);
            fail("Expected a filenot found exception");
        } catch (FileNotFoundException e) {
            // ok
        }
    }

    //
    public void testShapefileExists() throws Exception {
        File file = copyShapefiles(STATE_POP);
        URL url = file.toURL();

        HashMap params = new HashMap();
        params.put(ShapefileDataStoreFactory.URLP.key, url);

        Service service = new ShapefileService(null, new URI(url.toString()),
                params);

        // show we can connect
        ServiceInfo info = service.getInfo(null);
        assertNotNull(info);

        List members = service.members(null);
        assertEquals(1, members.size());

        ShapefileGeoResource resource = (ShapefileGeoResource) members.get(0);
        resource.getInfo(null);
    }
}
