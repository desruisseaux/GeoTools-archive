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
package org.geotools.data.gtopo30;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;


/**
 * DOCUMENT ME!
 *
 * @author giannecchini 
 */
public class GT30ReaderWriterTest extends TestCaseSupport {
    private String fileName = "W020N90";

    /**
     * Constructor for GT30ReaderTest.
     *
     * @param arg0
     */
    public GT30ReaderWriterTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        File file = this.getFile(this.fileName + ".zip");
        assertTrue(file.exists());

        String outPath = getFile(".").getAbsolutePath();
        this.unzipFile(file.getAbsolutePath(),
            outPath.substring(0, outPath.length() - 1));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteAll();
        super.tearDown();
    }

    /**
     *	Deleting all the file we created during tests.
     *	Since gtopo files are big we try to save space on the disk!!!
     */
    private void deleteAll() {
        File testDir = getFile("");
        File[] fileList = testDir.listFiles();

        for (int i = 0; i < fileList.length; i++)
            if (!fileList[i].getName().endsWith("zip")
                    && !fileList[i].getName().endsWith("ZIP")
                    && !fileList[i].isDirectory()) {
                fileList[i].delete();
            }
    }

    /**
     * Testin reader and writer for gtopo.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void testReaderWriter() throws IllegalArgumentException, IOException {
    	//getting a resource to test
        URL statURL = getTestResource(this.fileName + ".DEM");
        AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory()
            .createFormat();

        if (format.accepts(statURL)) {
            //get a reader
            GridCoverageReader reader = format.getReader(statURL);

            //get a grid coverage
            GridCoverage2D gc = ((GridCoverage2D) reader.read(null)); //.geophysics(false);
            //show the coverage
            ImageIcon icon = new ImageIcon(((PlanarImage) gc.geophysics(false).getRenderedImage())
                    .getAsBufferedImage());
            JLabel label = new JLabel(icon);
            JFrame frame = new JFrame();
            frame.setTitle(statURL.toString());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JScrollPane(label));
            frame.pack();
            frame.show();
            //delete all files and write them again to test
            deleteAll();

            //write it
            File testDir = getFile("");
            GridCoverageWriter writer = format.getWriter(testDir);
            writer.write(gc, null);

            //read it again
            reader = format.getReader(statURL);

            //get a grid coverage
            gc = ((GridCoverage2D) reader.read(null)).geophysics(false);

            //show the coverage again
            icon = new ImageIcon(((PlanarImage) gc.getRenderedImage())
                    .getAsBufferedImage());
            label = new JLabel(icon);
            frame = new JFrame();
            frame.setTitle(statURL.toString());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JScrollPane(label));
            frame.pack();
            frame.show();
        }
    }
}
