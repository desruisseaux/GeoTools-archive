/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.File;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import javax.media.jai.PlanarImage;
import org.geotools.referencing.crs.GeographicCRS;
import java.awt.BorderLayout;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.parameter.ParameterValueGroup;
import javax.swing.ImageIcon;
import org.opengis.coverage.grid.Format;
import java.net.URL;
import org.geotools.geometry.GeneralEnvelope;
import javax.swing.JLabel;
import javax.swing.JFrame;

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

        destination =  TestData.temp(this, "worldimage.aaa");

        wiWriter = new WorldImageWriter(destination);
        wiReader = new WorldImageReader(new URL(
                "http://java.sun.com/im/logo_java.gif"));
    }


    public void testWrite() throws IOException, IllegalArgumentException {
        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();
        //setting crs
        paramsRead.parameter("crs").setValue(GeographicCRS.WGS84);
        //setting envelope
        paramsRead.parameter("envelope").setValue(new GeneralEnvelope(
                new double[] {10, 42}, new double[] {11, 43}));

        GridCoverage2D coverage = (GridCoverage2D) wiReader.read(null);
        //(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());



        //writing
        paramsWrite=wiWriter.getFormat().getWriteParameters();
        paramsWrite.parameter("format").setValue("tiff");

        wiWriter.write(coverage,null);

    }

}
