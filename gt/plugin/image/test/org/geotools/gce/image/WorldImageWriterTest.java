/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.*;
import java.net.*;

import junit.framework.*;
import org.geotools.coverage.grid.*;
import org.geotools.resources.*;
import org.opengis.coverage.grid.*;
import org.opengis.parameter.*;
import javax.media.jai.PlanarImage;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageWriterTest extends TestCase {

	
    WorldImageReader wiReader;
    ParameterValueGroup paramsRead = null,paramsWrite=null;
    WorldImageWriter wiWriter;
    Object destination;

    public WorldImageWriterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();


    }


    public void testWrite() throws MalformedURLException, IOException  {

        URL url=null;
        File file=null;
        InputStream in=null;
        //checking test data directory for all kind of inputs
        File test_data_dir=null;
		try {
			test_data_dir = TestData.file(this,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] fileList=test_data_dir.list(new MyFileFilter());
        for(int i=0;i<fileList.length;i++)//i<fileList.length
        {

        	
            //url
            url= TestData.getResource(this,fileList[i]);
            assertTrue(url!=null);
            this.write(url);

            try {
				//file
				file= TestData.file(this,fileList[i]);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
			}
            assertTrue(file!=null);
            this.write(file);


            try {
				//inputstream
				in=new FileInputStream(TestData.file(this,fileList[i]));
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace(System.err);
			}
            this.write(in);
        	
        

        }

        try {
			//checking an http link
			url= new URL("http://www.sun.com/im/homepage-powered_by_sun.gif");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(System.err);
		}
        this.write(url);

        try {
			in=new URL("http://www.sun.com/im/homepage-powered_by_sun.gif").openStream();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace(System.err);
		}
        this.write(in);

    }

    /**
     * write
     *
     * @param source Object
     */
    private void write(Object source)  {


        wiReader= new WorldImageReader(source);
        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();
        //setting crs

        GridCoverage2D coverage=null;
		try {
			coverage = (GridCoverage2D) wiReader.read(null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		//(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());


        //writing png
        File tempFile=null;
		try {
			tempFile = TestData.temp(this,"temp");
			System.err.println(tempFile.getAbsolutePath());
			assertTrue(tempFile.exists());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(System.err);
			assertTrue(false);
		}

        //writer
        wiWriter= new WorldImageWriter(tempFile);

        //writing parameters for png
        Format writerParams=wiWriter.getFormat();
        writerParams.getWriteParameters().parameter("Format").setValue("gif");

        try {
			//writing
			wiWriter.write(coverage.geophysics(false),null);
		} catch (IllegalArgumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace(System.err);
			assertTrue(false);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace(System.err);
			assertTrue(false);
		}


        //reading again
		assertTrue(tempFile.exists());
       /* wiReader= new WorldImageReader(tempFile);
        try {
			coverage=(GridCoverage2D) wiReader.read(null);
		} catch (IllegalArgumentException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace(System.err);
			assertTrue(false);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace(System.err);
			assertTrue(false);
		}
*/

        //displaying
    /*    JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(((PlanarImage) coverage.
                getRenderedImage()).getAsBufferedImage()));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();*/
    }

}





