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
 *
 */
/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.data.gtopo30;

import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.Filter;
import org.geotools.gc.GridCoverage;

import com.vividsolutions.jts.geom.Envelope;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @author James Macgill
 */
public class GT30DemTest extends TestCaseSupport {
    public GT30DemTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(GT30DemTest.class));
    }

    public void testDem() throws Exception {
        // read dem      
        URL demURL = getTestResource("test.dem");
        GTopo30DataSource ds = new GTopo30DataSource(demURL);
        FeatureCollection fc = FeatureCollections.newCollection();
        ds.getFeatures(fc, Query.ALL);
        Envelope ex = ds.getBounds();

        // get the image out of the grid coverage
        Feature f = fc.features().next();
        GridCoverage gc = (GridCoverage) f.getAttribute("grid");
        RenderedImage image = gc.geophysics(false).getRenderedImage();
        
        // write to disk
        FileOutputStream out = new FileOutputStream(getFile("demImage.png"));
        ImageIO.write(image, "PNG", out);
    }
    
    public void testCrop() throws Exception {
        // read dem      
        URL demURL = getTestResource("test.dem");
        GTopo30DataSource ds = new GTopo30DataSource(demURL);
        ds.setCropEnvelope(new Envelope(0, 40, 70, 90));
        FeatureCollection fc = FeatureCollections.newCollection();
        ds.getFeatures(fc, Query.ALL);
        System.out.println(ds.getBounds());

        // get the image out of the grid coverage
        Feature f = fc.features().next();
        GridCoverage gc = (GridCoverage) f.getAttribute("grid");
        RenderedImage image = gc.geophysics(false).getRenderedImage();

        // write to disk
        FileOutputStream out = new FileOutputStream(getFile("emptyImage.png"));
    }
}
