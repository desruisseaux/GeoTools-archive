/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.File;
import java.io.IOException;

import org.geotools.gce.image.*;
import junit.framework.TestCase;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import javax.swing.JScrollPane;
import org.geotools.coverage.grid.GridCoverage2D;
import javax.media.jai.PlanarImage;
import org.geotools.resources.TestData;
import java.net.URL;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.ParameterValueGroup;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.GeographicCRS;
import org.opengis.parameter.GeneralParameterValue;
import java.awt.BorderLayout;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
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
                wiReader = new WorldImageReader(new URL("http://java.sun.com/im/logo_java.gif"));

        }

        /**
         * Constructor for WorldImageReaderTest.
         * @param arg0
         */
        public WorldImageReaderTest(String arg0) {
                super(arg0);
        }

        public void testRead() throws IOException {
            Format readerFormat=wiReader.getFormat();
            paramsRead = readerFormat.getReadParameters();
            //setting crs
            paramsRead.parameter("crs").setValue(GeographicCRS.WGS84);
            //setting envelope
            paramsRead.parameter("envelope").setValue(new GeneralEnvelope(
                    new double[] {10, 42}, new double[] {11, 43}));

            GridCoverage2D coverage = (GridCoverage2D)wiReader.read(null);
                      //(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
            assertNotNull(coverage);
            assertNotNull(((GridCoverage2D)coverage).getRenderedImage());
            assertNotNull(coverage.getEnvelope());

            JFrame frame = new JFrame();
            JLabel label = new JLabel(new ImageIcon( ((PlanarImage)coverage.getRenderedImage()).getAsBufferedImage()));
            frame.getContentPane().add(label, BorderLayout.CENTER);
            frame.pack();
            frame.show();


        }
}
