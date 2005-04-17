/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.media.jai.*;
import javax.swing.*;

import org.geotools.coverage.grid.*;
import org.geotools.resources.*;
import org.opengis.coverage.grid.*;
import org.opengis.parameter.*;
import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini</a>
 * @author <a href="mailto:alessio.fabiani@gmail.com">Alessio Fabiani</a>
 * @author rgould
 * <p>
 * @TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageReaderTest extends TestCase {

	WorldImageReader wiReader;
        ParameterValueGroup paramsRead=null;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

	}

	/**
	 * Constructor for WorldImageReaderTest.
	 * @param arg0
	 */
	public WorldImageReaderTest(String arg0) {
		super(arg0);
	}

	public void testRead() throws IOException {
            TestData testData= new TestData();
            URL url=null;
            File file=null;
            InputStream in=null;
            //checking test data directory for all kind of inputs
            File test_data_dir=TestData.file(this,null);
            String[] fileList=test_data_dir.list(new MyFileFilter());
            for(int i=0;i<fileList.length;i++)
            {

                //url
                url= new URL("file:" + test_data_dir.getAbsolutePath()+"/"+ fileList[i]);
                this.read(url);

                //file
                file= new File(test_data_dir.getAbsolutePath()+"/"+fileList[i]);
                this.read(file);


                //inputstream
                in=new FileInputStream(test_data_dir.getAbsolutePath()+"/"+fileList[i]);
                this.read(in);

            }

            //checking an http link
            url= new URL("http://www.sun.com/im/homepage-powered_by_sun.gif");
            this.read(url);

            in=new URL("http://www.sun.com/im/homepage-powered_by_sun.gif").openStream();
            this.read(in);

	}

    /**
     * read
     *
     * @param source Object
     */
    private void read(Object source) throws FileNotFoundException, IOException,
            IllegalArgumentException {
        wiReader = new WorldImageReader(source);
        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();
        GridCoverage2D coverage = (GridCoverage2D) wiReader.read(null);
        //(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());

        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(((PlanarImage) coverage.
                getRenderedImage()).getAsBufferedImage()));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();

    }

}

