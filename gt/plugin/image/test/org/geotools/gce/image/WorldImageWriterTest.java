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
import org.geotools.geometry.*;
import org.geotools.referencing.crs.*;
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


    public void testWrite() throws IOException, IllegalArgumentException {
        TestData testData= new TestData();
        URL url=null;
        File file=null;
        InputStream in=null;
        //checking test data directory for all kind of inputs
        File test_data_dir=testData.file(this,".");
        String[] fileList=test_data_dir.list(new MyFileFilter());
        for(int i=0;i<fileList.length;i++)
        {

            //url
            url= new URL("file:" + test_data_dir.getAbsolutePath()+"/"+ fileList[i]);
            this.write(url);

            //file
            file= new File(test_data_dir.getAbsolutePath()+"/"+fileList[i]);
            this.write(file);


            //inputstream
            in=new FileInputStream(test_data_dir.getAbsolutePath()+"/"+fileList[i]);
            this.write(in);

        }

        //checking an http link
        url= new URL("http://www.sun.com/im/homepage-powered_by_sun.gif");
        this.write(url);

        in=new URL("http://www.sun.com/im/homepage-powered_by_sun.gif").openStream();
        this.write(in);

    }

    /**
     * write
     *
     * @param source Object
     */
    private void write(Object source) throws java.io.IOException,
            IllegalArgumentException {



        wiReader= new WorldImageReader(source);
        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();
        //setting crs

        GridCoverage2D coverage = (GridCoverage2D) wiReader.read(null);
        //(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());


        //writing png
        File tempFile= File.createTempFile("temp",".tiff");

        //writer
        wiWriter= new WorldImageWriter(tempFile);

        //writing parameters for png
        Format writerParams=wiWriter.getFormat();
        writerParams.getWriteParameters().parameter("Format").setValue("tiff");

        //writing
        wiWriter.write(coverage,null);


        //reading again
        wiReader= new WorldImageReader(tempFile);
        coverage=(GridCoverage2D) wiReader.read(null);


        //displaying
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(((PlanarImage) coverage.
                getRenderedImage()).getAsBufferedImage()));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();

    }

}





