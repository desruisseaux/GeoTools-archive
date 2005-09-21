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
package org.geotools.gce.gtopo30;

import org.geotools.coverage.grid.GridCoverage2D;

import org.geotools.data.coverage.grid.AbstractGridFormat;

import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;

import com.sun.media.jai.widget.DisplayJAI;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;

import java.util.zip.ZipOutputStream;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import tilecachetool.*;
/**
 * DOCUMENT ME!
 *
 * @author giannecchini
 */
public class GT30ReaderWriterTest extends TestCaseSupport {
    private String fileName = "W020N90";

    /**
     * Constructor for GT30ReaderTest.
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

        
    }

	/**
	 * @throws Exception
	 */
	private void unpackGTOPO() throws Exception {
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
     * Deleting all the file we created during tests.     Since gtopo files are
     * big we try to save space on the disk!!!
     */
    private void deleteAll() {
        final File testDir = getFile("");
        final File[] fileList = testDir.listFiles();
        final int length=fileList.length;
        for (int i = 0; i <length ; i++)
            if (!fileList[i].getName().endsWith("zip") &&
                    !fileList[i].getName().endsWith("ZIP") &&
                    !fileList[i].isDirectory()) {
                fileList[i].delete();
            }
    }

    /**
     * Testing reader and writer for gtopo.
     * @throws Exception 
     */
    public void testReaderWriter() throws Exception {
        //getting a resource to test
		unpackGTOPO();
        URL statURL = getTestResource(this.fileName + ".DEM");
        AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory().createFormat();

        TileCache defaultInstance=JAI.getDefaultInstance().getTileCache();
        defaultInstance.setMemoryCapacity(1024*1024*64);
        defaultInstance.setMemoryThreshold(1.0f);
        
     //   TCTool tctool = new TCTool( );
        if (format.accepts(statURL)) {
            //get a reader
            GridCoverageReader reader = format.getReader(statURL);

            //get a grid coverage
            GridCoverage2D gc = ((GridCoverage2D) reader.read(null)); 

            //show the coverage
            DisplayJAI display= new DisplayJAI();
            display.set(gc.geophysics(false)
                    .getRenderedImage());
      
  
            JFrame frame = new JFrame();

            frame.setTitle(statURL.toString());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JScrollPane(display));
            frame.pack();
            frame.show();
//            
//            RenderedImage image=gc.getRenderedImage();
//            if(image instanceof PlanarImage){
//            	
//            }
            	
            

            //delete all files and write them again to test
//            deleteAll();

            //write it
//            File testDir = getFile("");
//            GridCoverageWriter writer = format.getWriter(testDir);
//            writer.write(gc, null);

//            //read it again
//            reader = format.getReader(statURL);
//
//            //get a grid coverage
//            gc = ((GridCoverage2D) reader.read(null)).geophysics(false);
//
//            //show the coverage again
//            display.set(gc.getRenderedImage());
//            frame = new JFrame();
//            frame.setTitle(statURL.toString());
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.getContentPane().add(new JScrollPane(display));
//            frame.pack();
//            frame.show();
//			 deleteAll();
        }
    }
/*
    public void testReaderWriterZip()
        throws Exception {
		this.unpackGTOPO();
        URL statURL x getTestResource(this.fileName + ".DEM");
        AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory().createFormat();

        if (format.accepts(statURL)) {
            //    	get a reader
            GridCoverageReader reader = format.getReader(statURL);

            //get a grid coverage
            GridCoverage2D gc = ((GridCoverage2D) reader.read(null));

            File zipFile = getFile("test.zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                        zipFile));

            GridCoverageWriter writer = format.getWriter(out);
            writer.write(gc, null);
            out.flush();
            out.close();
        }
		deleteAll();
    }*/
    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(GT30ReaderWriterTest.class));
    }
}
