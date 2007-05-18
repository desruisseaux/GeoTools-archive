/*
 * (C) 2006, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.geotools.image.io.netcdf;

// J2SE dependencies
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A class for testing the reading in a netCDF format file, and to display an 
 * {@link RenderedImage} from these data.
 *
 * @author Cédric Briançon
 */
public class Test {
    /**
     * The file to read.
     */
    private static final File FILE = new File("C:\\OA_20050518.nc");
    
    /**
     * The default reader spi for the "temperature" file.
     */
    private static TemperatureReaderSpi temperatureSpi = new TemperatureReaderSpi();
    
    /**
     * The default constructor for testing the reading and the displaying of an image 
     * generated from a netCDF file.
     */
    public Test() throws IOException {
        final JFrame frame = new JFrame();
        // Creation time
        final RenderedImage image = getRenderedImage();
        // Include the {@code RenderedImage} into a {@code Panel} allowing to scroll if the image
        // is bigger than the frame.
        final ScrollingImagePanel scroll = new ScrollingImagePanel(image, 800, 600);
        System.out.println("Image netCDF : " + FILE.getName());
        System.out.println("Largeur : " + image.getWidth());
        System.out.println("Hauteur : " + image.getHeight());
        frame.add(scroll);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
//    /**
//     * Get a rendered image for the netCDF file chosen.
//    
//     * @return The rendered image.
//     * @throws IOException
//     */
//    public static RenderedImage getRenderedImage() throws IOException {
//        final ImageReader reader = temperatureSpi.createReaderInstance(null);
//        final ImageInputStream inStream = ImageIO.createImageInputStream(FILE);
//        reader.setInput(inStream, true);
//        Integer imageChoice = new Integer(0);
//        final ImageReadParam readP = new ImageReadParam();
//        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
//        GeneralEnvelope requestedEnvelope = new GeneralEnvelope(crs);
//        /*try {
//            imageChoice = AbstractGridCoverage2DReader.setReadParams(readP, requestedEnvelope, new Rectangle());
//        } catch (TransformException e) {
//            new DataSourceException(e);
//        }*/
//        //readP.setSourceRenderSize(new Dimension(800,600));
//        final ParameterBlock pbjRead = new ParameterBlock();
//        pbjRead.add(inStream);
//        pbjRead.add(imageChoice);
//        pbjRead.add(Boolean.FALSE);
//        pbjRead.add(Boolean.FALSE);
//        pbjRead.add(Boolean.FALSE);
//        pbjRead.add(null);
//        pbjRead.add(null);
//        pbjRead.add(readP);
//        pbjRead.add(reader);
//        
//        RenderedImage image = JAI.create("ImageRead", pbjRead);
//        return image;
//    }
    
    /**
     * Get a rendered image for the netCDF file chosen.
     *
     * @return The rendered image.
     * @throws IOException
     */
    public static RenderedImage getRenderedImage() throws IOException {
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GeneralEnvelope requestedEnvelope = new GeneralEnvelope(crs);
        final ImageReader reader = temperatureSpi.createReaderInstance(null);
        //final ImageInputStream inStream = ImageIO.createImageInputStream(FILE);
        //reader.setInput(inStream, true);
        reader.setInput(FILE);
        RenderedImage image = reader.read(0);
        return image;
    }
    
    /**
     * This implementation will create a rendered image from a netCDF file. This image will
     * be displayed, using the {@code color palette} found in the {@code resources} folder.
     * 
     * @param args Not used in this implementation.
     */
    public static void main(String[] args) throws IOException {
        new Test();
    }
    
}