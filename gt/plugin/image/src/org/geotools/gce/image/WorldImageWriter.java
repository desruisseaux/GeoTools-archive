/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.parameter.Parameter;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.FileFormatNotCompatibleWithGridCoverageException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.spatialschema.geometry.Envelope;

import javax.imageio.ImageIO;

import javax.media.jai.IHSColorSpace;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.URL;

/**
 * @author rgould
 * @author alessio fabiani (alessio.fabiani@gmail.com)
 * @author simone giannecchini (simboss_ml@tiscali.it)
 *
 * Writes a GridCoverage to a raster image file and an accompanying world file.
 * The destination specified must point to the location of the raster file to
 * write to, as this is how the format is determined. The directory that file is
 * located in must also already exist.
 */
public class WorldImageWriter implements GridCoverageWriter {
    /*format for this writer*/
    private Format format = new WorldImageFormat();

    /**Destination to write to*/
    private Object destination;

    /**
     * Destination must be a File. The directory it resides in must already exist.
     * It must point to where the raster image is to be located. The world image will
     * be derived from there.
     *
     * @param destination
     */
    public WorldImageWriter(Object destination) {
        this.destination = destination;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#getFormat()
     */

    /**
     * Returns the format supported by this WorldImageWriter, a new WorldImageFormat
     */
    public Format getFormat() {
        return format;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#getDestination()
     */

    /**
     * Returns the location of the raster that the GridCoverage will be written to.
     */
    public Object getDestination() {
        return destination;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#getMetadataNames()
     */

    /**
     * Metadata is not supported. Returns null.
     */
    public String[] getMetadataNames() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String, java.lang.String)
     */

    /**
     * Metadata not supported, does nothing.
     */
    public void setMetadataValue(String name, String value)
        throws IOException, MetadataNameNotFoundException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
     */

    /**
     * Raster images don't support names. Does nothing.
     */
    public void setCurrentSubname(String name)
        throws IOException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#write(org.geotools.gc.GridCoverage, org.opengis.parameter.GeneralParameterValue[])
     */

    /**
     * Takes a GridCoverage and writes the image to the destination file.
     * It then reads the format of the file and writes an accompanying world file.
     * It will throw a FileFormatNotCompatibleWithGridCoverageException if Destination
     * is not a File (URL is a read-only format!).
     *
     * @param coverage the GridCoverage to write.
     * @param parameters no parameters are accepted. Currently ignored.
     */
    public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
        throws IllegalArgumentException, IOException {
        //checking parameters
        //if provided we have to use them
        //specifically this is one of the way we can provide an output format
        if (parameters != null) {
            this.format.getWriteParameters().parameter("format").setValue(((Parameter) parameters[0])
                .stringValue());
        }

        //convert everything into a file when possible
        //we have to separate the handling of a file from the handling of an
        //output stream due to the fact that the latter requires no world file.
        if (this.destination instanceof String) {
            destination = new File((String) destination);
        }
        else if (this.destination instanceof URL) {
            destination = new File(((URL) destination).getPath());
        }
        else
        //OUTPUT STREAM HANDLING
        if (destination instanceof OutputStream) {
            this.encode(coverage, (OutputStream) destination);
        }

        if (destination instanceof File) {
            //WRITING TO A FILE
            RenderedImage image = ((PlanarImage) ((GridCoverage2D) coverage)
                .getRenderedImage()).getAsBufferedImage();
            Envelope env = coverage.getEnvelope();
            double xMin = env.getMinimum(0);
            double yMin = env.getMinimum(1);
            double xMax = env.getMaximum(0);
            double yMax = env.getMaximum(1);

            double xPixelSize = (xMax - xMin) / image.getWidth();
            double rotation1 = 0;
            double rotation2 = 0;
            double yPixelSize = (yMax - yMin) / image.getHeight();
            double xLoc = xMin;
            double yLoc = yMax;

            //files destinations
            File imageFile = (File) destination;
            String path = imageFile.getAbsolutePath();
            int index = path.lastIndexOf(".");
            String baseFile = path.substring(0, index);
            File worldFile = new File(baseFile
                    + WorldImageFormat.getWorldExtension(
                        format.getWriteParameters().parameter("format")
                              .stringValue()));

            //create new files
            imageFile = new File(baseFile + "."
                    + format.getWriteParameters().parameter("format")
                            .stringValue());
            imageFile.createNewFile();
            worldFile.createNewFile();

            //writing world file
            PrintWriter out = new PrintWriter(new FileOutputStream(worldFile));

            out.println(xPixelSize);
            out.println(rotation1);
            out.println(rotation2);
            out.println("-" + yPixelSize);
            out.println(xLoc);
            out.println(yLoc);
            out.close();

            BufferedOutputStream outBuf = new BufferedOutputStream(new FileOutputStream(
                        imageFile));

            this.encode(coverage, outBuf);
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#dispose()
     */

    /**
     * Cleans up the writer. Currently does nothing.
     */
    public void dispose() throws IOException {
    }

    /**Encode.
     *
     * @param output OutputStream
     * @throws IOException
     */
    private void encode(GridCoverage sourceCoverage, OutputStream output)
        throws IOException {
        //do we have a source coverage?
        if (sourceCoverage == null) {
            throw new IllegalArgumentException(
                "A coverage must be provided in order for write to succeed!");
        }

        //trying to perform a rendering for this image
        try {
            PlanarImage surrogateImage = null;

            /**
             * ARE WE DEALING WITH A GRAYSCALE IMAGE? LET'S RESCALE AND GO TO
             * BYTE DATABUFFER FOR DISPLAYING.
             */
            if (sourceCoverage.getSampleDimension(0).getColorInterpretation()
                              .name().equals("GRAY_INDEX")) {
                //getting rendered image
                RenderedImage image = ((GridCoverage2D) sourceCoverage)
                    .getRenderedImage();

                //image dimensions
                int width = image.getWidth();
                int height = image.getHeight();

                double[] dpixel = new double[image.getSampleModel().getNumBands()];

                // Which are the max and min of the image ? We need to know to create the
                // surrogate image.
                // Let's use the extrema operator to get them.
                ParameterBlock pbMaxMin = new ParameterBlock();

                pbMaxMin.addSource(image);

                RenderedOp extrema = JAI.create("extrema", pbMaxMin);

                // Must get the extrema of all bands !
                double[] allMins = (double[]) extrema.getProperty("minimum");
                double[] allMaxs = (double[]) extrema.getProperty("maximum");
                double minValue = Double.MAX_VALUE;
                double maxValue = Double.MIN_VALUE;

                //looking for the minimum sample
                //TODO convert this code into a cycle
                if (!Double.isNaN(allMins[0])) {
                    minValue = allMins[0];
                }
                else {
                    Double[] buffer = null;

                    if (image.getData().getDataBuffer() instanceof DataBufferFloat) {
                        float[] tmp = ((DataBufferFloat) image.getData()
                                                              .getDataBuffer())
                            .getData();

                        buffer = new Double[tmp.length];

                        for (int i = 0; i < tmp.length; i++) {
                            buffer[i] = new Double(tmp[i]);
                        }
                    }
                    else if (image.getData().getDataBuffer() instanceof DataBufferDouble) {
                        double[] tmp = ((DataBufferDouble) image.getData()
                                                                .getDataBuffer())
                            .getData();

                        buffer = new Double[tmp.length];

                        for (int i = 0; i < tmp.length; i++) {
                            buffer[i] = new Double(tmp[i]);
                        }
                    }

                    for (int i = 0; i < buffer.length; i++) {
                        if (minValue > buffer[i].doubleValue()) {
                            minValue = buffer[i].doubleValue();
                        }
                    }
                }

                //looking for the maximum sample
                //TODO convert this code into a cycle
                if (!Double.isNaN(allMaxs[0])) {
                    maxValue = allMaxs[0];
                }
                else {
                    Double[] buffer = null;

                    if (image.getData().getDataBuffer() instanceof DataBufferFloat) {
                        float[] tmp = ((DataBufferFloat) image.getData()
                                                              .getDataBuffer())
                            .getData();

                        buffer = new Double[tmp.length];

                        for (int i = 0; i < tmp.length; i++) {
                            buffer[i] = new Double(tmp[i]);
                        }
                    }
                    else if (image.getData().getDataBuffer() instanceof DataBufferDouble) {
                        double[] tmp = ((DataBufferDouble) image.getData()
                                                                .getDataBuffer())
                            .getData();

                        buffer = new Double[tmp.length];

                        for (int i = 0; i < tmp.length; i++) {
                            buffer[i] = new Double(tmp[i]);
                        }
                    }

                    for (int i = 0; i < buffer.length; i++) {
                        if (maxValue < buffer[i].doubleValue()) {
                            maxValue = buffer[i].doubleValue();
                        }
                    }
                }

                //looking for the max and the min
                //TODO use max function
                for (int v = 1; v < allMins.length; v++) {
                    if (allMins[v] < minValue) {
                        minValue = allMins[v];
                    }

                    if (allMaxs[v] > maxValue) {
                        maxValue = allMaxs[v];
                    }
                }

                //    double minValue = sourceCoverage.getSampleDimension(0).getMinimumValue();
                //    double maxValue = sourceCoverage.getSampleDimension(0).getMinimumValue();

                /**
                 * RESCALING SOURCE IMAGE
                 */
                double[] subtract = new double[1];

                subtract[0] = minValue;

                double[] divide = new double[1];

                divide[0] = 255.0 / (maxValue - minValue);

                // Now we can rescale the pixels gray levels:
                ParameterBlock pbRescale = new ParameterBlock();

                pbRescale.add(divide);
                pbRescale.add(subtract);
                pbRescale.addSource(image);
                surrogateImage = (PlanarImage) JAI.create("rescale", pbRescale,
                        null);

                // Let's convert the data type for displaying.
                ParameterBlock pbConvert = new ParameterBlock();

                pbConvert.addSource(surrogateImage);
                pbConvert.add(DataBuffer.TYPE_BYTE);
                surrogateImage = JAI.create("format", pbConvert);

                //TODO check this if it is needed
                surrogateImage = JAI.create("invert", surrogateImage);
            }
            else {
                /**
                 * WORKING ON A COLORED IMAGE
                 */
                surrogateImage = (PlanarImage) ((GridCoverage2D) sourceCoverage)
                    .getRenderedImage();
            }

            /** WRITE TO OUTPUTBUFER */
            ImageIO.write(surrogateImage,
                (String) (this.format.getWriteParameters().parameter("format")
                                     .getValue()), output);
            output.flush();
            output.close();
        }
        catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    private RenderedImage highlightImage(RenderedImage stillImg) {
        RenderedImage dispImg = null;

        // Highlight the human pixels (detection results img)
        // Create a constant image
        Byte[] bandValues = new Byte[1];

        bandValues[0] = new Byte("65"); //32 -- orangeish, 65 -- greenish

        ParameterBlock pbConstant = new ParameterBlock();

        pbConstant.add(new Float(stillImg.getWidth())); // The width

        pbConstant.add(new Float(stillImg.getHeight())); // The height

        pbConstant.add(bandValues); // The band values

        PlanarImage imgConstant = (PlanarImage) JAI.create("constant",
                pbConstant);

        //System.out.println("Making multiply image");
        // Multiply the mask by 255 so the values are 0 or 255
        ParameterBlock pbMultiply = new ParameterBlock();

        pbMultiply.addSource((PlanarImage) stillImg);

        double[] multiplyArray = new double[] { 255.0 };

        pbMultiply.add(multiplyArray);

        PlanarImage imgMask = (PlanarImage) JAI.create("multiplyconst",
                pbMultiply);

        //System.out.println("Making IHS image");
        // Create a Intensity, Hue, Saturation image
        ParameterBlock pbIHS = new ParameterBlock();

        pbIHS.setSource(stillImg, 0); //still img is the intensity

        pbIHS.setSource(imgConstant, 1); //constant img is the hue

        pbIHS.setSource(imgMask, 2); //mask is the saturation

        //create rendering hint for IHS image to specify the color model
        ComponentColorModel IHS_model = new ComponentColorModel(IHSColorSpace
                .getInstance(), new int[] { 8, 8, 8 }, false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

        ImageLayout layout = new ImageLayout();

        layout.setColorModel(IHS_model);

        RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);

        PlanarImage IHSImg = JAI.create("bandmerge", pbIHS, rh);

        //System.out.println("Making RGB image");
        // Convert IHS image to a RGB image
        ParameterBlock pbRGB = new ParameterBlock();

        //create rendering hint for RGB image to specify the color model
        ComponentColorModel RGB_model = new ComponentColorModel(ColorSpace
                .getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 }, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

        pbRGB.addSource(IHSImg);

        pbRGB.add(RGB_model);

        dispImg = JAI.create("colorconvert", pbRGB);

        return dispImg;
    }
}
