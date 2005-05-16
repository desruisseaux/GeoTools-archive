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
package org.geotools.gce.image;

import junit.framework.*;
import org.geotools.coverage.grid.*;
import org.geotools.resources.*;
import org.opengis.coverage.grid.*;
import org.opengis.parameter.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;
import java.net.*;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * Test class for WorldImageWriter. This test tries to read, writer and re-read successive images
 * checking for errors.
 *
 * @author giannecchini
 * @author rgould
 */
public class WorldImageWriterTest extends TestCase {
    WorldImageReader wiReader;
    ParameterValueGroup paramsRead = null;
    ParameterValueGroup paramsWrite = null;
    WorldImageWriter wiWriter;
    Object destination;

    public WorldImageWriterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        File testDir = TestData.file(this, "");
        String[] files = testDir.list();
//
//        //removing unnecessary files
//        if (files != null) {
//            for (int i = 0; i < files.length; i++)
//                if (!files[i].startsWith("etopo")
//                        && !files[i].startsWith("usa")) {
//                    (new File(testDir.getAbsolutePath() + "/" + files[i]))
//                    .delete();
//                }
//        }

        super.tearDown();
    }

    public void testWrite() throws MalformedURLException, IOException {
        URL url = null;
        File file = null;
        InputStream in = null;

        //checking test data directory for all kind of inputs
        File test_data_dir = null;
        test_data_dir = TestData.file(this, null);
        String[] fileList = test_data_dir.list(new MyFileFilter());

        for (int i = 0; i < fileList.length; i++) {
            //url
            url = TestData.getResource(this, fileList[i]);
            assertTrue(url != null);
            this.write(url);

//            //getting file
//            file = TestData.file(this, fileList[i]);
//
//            assertTrue(file != null);
//
//            //starting write test
//            this.write(file);
//
//            //inputstream
//            in = new FileInputStream(TestData.file(this, fileList[i]));
//
//            this.write(in);
        }

        //checking an http link
//        url = new URL("http://www.sun.com/im/homepage-powered_by_sun.gif");
//
//        this.write(url);
//
//        in = new URL("http://www.sun.com/im/homepage-powered_by_sun.gif")
//            .openStream();
//
//        this.write(in);
    }

    /**
     * write
     *
     * @param source Object
     *
     * @throws IOException
     */
    private void write(Object source) throws IOException {
        wiReader = new WorldImageReader(source);

        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();

        //setting crs
        GridCoverage2D coverage = null;

        coverage = (GridCoverage2D) wiReader.read(null);

        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());

        //writing png
        File tempFile = null;

        //remember to provide a valid name, it wil be mde unique by the helper function
        //temp
        tempFile = File.createTempFile("temp", ".gif");
        tempFile.deleteOnExit();
        assertTrue(tempFile.exists());

        //writer
        wiWriter = new WorldImageWriter(tempFile);

        //writing parameters for png
        Format writerParams = wiWriter.getFormat();
        writerParams.getWriteParameters().parameter("Format").setValue("gif");

        //writing
        wiWriter.write(coverage, null);
		
	    
//
//        //reading again
//        assertTrue(tempFile.exists());
//        wiReader = new WorldImageReader(tempFile);
//
//        coverage = (GridCoverage2D) wiReader.read(null);
		
		

    }
}
