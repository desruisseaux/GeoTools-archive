/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.data.wms.test;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.referencing.CRS;
import org.geotools.resources.TestData;
import org.opengis.spatialschema.geometry.Envelope;

public class Geot553Test extends TestCase {
    
    public void testGeot553 () throws Exception {
       // File getCaps = TestData.file(this, "geot553capabilities.xml");
        
        URL getCapsURL = TestData.getResource(this, "geot553capabilities.xml");
        
        WebMapServer wms = new WebMapServer(getCapsURL);
        Layer layer = wms.getCapabilities().getLayer().getChildren()[2];
        
        Envelope env = wms.getEnvelope(layer, CRS.decode("EPSG:3005"));
        
        assertNotNull(env);
    }
}
