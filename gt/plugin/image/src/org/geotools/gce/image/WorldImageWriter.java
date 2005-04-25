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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.parameter.Parameter;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.spatialschema.geometry.Envelope;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.media.jai.ColorCube;
import javax.media.jai.IHSColorSpace;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;


/**
 * Writes a GridCoverage to a raster image file and an accompanying world file.
 * The destination specified must point to the location of the raster file to
 * write to, as this is how the format is determined. The directory that file
 * is located in must also already exist.
 *
 * @author simone giannecchini
 * @author rgould
 * @author alessio fabiani
 */
public class WorldImageWriter implements GridCoverageWriter {
    /**format for this writer*/
    private Format format = new WorldImageFormat();

    /** Destination to write to */
    private Object destination;

    /**
     * Destination must be a File. The directory it resides in must already
     * exist. It must point to where the raster image is to be located. The
     * world image will be derived from there.
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
     * Returns the format supported by this WorldImageWriter, a new
     * WorldImageFormat
     *
     * @return DOCUMENT ME!
     */
    public Format getFormat() {
        return format;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#getDestination()
     */

    /**
     * Returns the location of the raster that the GridCoverage will be written
     * to.
     *
     * @return DOCUMENT ME!
     */
    public Object getDestination() {
        return destination;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#getMetadataNames()
     */

    /**
     * Metadata is not supported. Returns null.
     *
     * @return DOCUMENT ME!
     */
    public String[] getMetadataNames() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String, java.lang.String)
     */

    /**
     * Metadata not supported, does nothing.
     *
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws MetadataNameNotFoundException DOCUMENT ME!
     */
    public void setMetadataValue(String name, String value)
        throws IOException, MetadataNameNotFoundException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
     */

    /**
     * Raster images don't support names. Does nothing.
     *
     * @param name DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void setCurrentSubname(String name) throws IOException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#write(org.geotools.gc.GridCoverage, org.opengis.parameter.GeneralParameterValue[])
     */

    /**
     * Takes a GridCoverage and writes the image to the destination file. It
     * then reads the format of the file and writes an accompanying world
     * file. It will throw a FileFormatNotCompatibleWithGridCoverageException
     * if Destination is not a File (URL is a read-only format!).
     *
     * @param coverage the GridCoverage to write.
     * @param parameters no parameters are accepted. Currently ignored.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
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
        } else if (this.destination instanceof URL) {
            destination = new File(((URL) destination).getPath());
        } else
        //OUTPUT STREAM HANDLING
        if (destination instanceof OutputStream) {
            this.encode((GridCoverage2D) coverage, (OutputStream) destination);
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

            this.encode((GridCoverage2D) coverage, outBuf);
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageWriter#dispose()
     */

    /**
     * Cleans up the writer. Currently does nothing.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void dispose() throws IOException {
    }

    /**
     * Encode.
     *
     * @param sourceCoverage DOCUMENT ME!
     * @param output OutputStream
     *
     * @throws IOException
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void encode(GridCoverage2D sourceCoverage, OutputStream output)
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
             * ARE WE DEALING WITH A GRAYSCALE IMAGE?
             */
            if (sourceCoverage.getSampleDimension(0).getColorInterpretation()
                                  .name().equals("GRAY_INDEX")) {
                //getting rendered image
                surrogateImage = ((PlanarImage) ((GridCoverage2D) sourceCoverage).geophysics(false)
                                                 .getRenderedImage());
            } else {
                /**
                 * WORKING ON A COLORED IMAGE
                 */
                surrogateImage = (PlanarImage) ((GridCoverage2D) sourceCoverage)
                    .getRenderedImage();

                //trying to write a GIF
                if (surrogateImage.getColorModel() instanceof ComponentColorModel
                        && (((String) (this.format.getWriteParameters()
                                                      .parameter("format")
                                                      .getValue()))
                        .compareToIgnoreCase("gif") == 0)) {
                    surrogateImage = componentColorModel2GIF(surrogateImage);
                }
            }

            /**
             * WRITE TO OUTPUTBUFER
             */
            ImageIO.write(surrogateImage,
                (String) (this.format.getWriteParameters().parameter("format")
                                     .getValue()), output);
        } catch (Exception e) {
            System.err.println(e.getMessage());

            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Convert the image to a GIF-compliant image.  This method has been
     * created in order to convert the input image to  a form that is
     * compatible with the GIF model.  It first remove the information about
     * transparency since the error diffusion and the error dither operations
     * are unable to process images with more than 3 bands.  Sfterwards the
     * image is processed with an error diffusion operator in order to reduce
     * the number of bands from 3 to 1 and the number of color to 216.  A
     * suitable layout is used for the final image via the RenderingHints in
     * order to take into account the different layout model for the final
     * image.
     *
     * @param surrogateImage image to convert
     *
     * @return PlanarImage image converted
     */
    private PlanarImage componentColorModel2GIF(PlanarImage surrogateImage) {
        {
            //parameter block
            ParameterBlock pb = new ParameterBlock();
            RenderedOp bandSelect = null;

            //check the number of bands looking for alpha band
            if (surrogateImage.getSampleModel().getNumBands() > 3) {
                bandSelect = JAI.create("bandSelect", surrogateImage,
                        new int[] { 0, 1, 2 });
                surrogateImage = bandSelect.createInstance();
            }

            //removing alpha band
            int w = surrogateImage.getWidth();
            int h = surrogateImage.getHeight();
            KernelJAI ditherMask = KernelJAI.ERROR_FILTER_FLOYD_STEINBERG; //KernelJAI.DITHER_MASK_443;
            ColorCube colorMap = ColorCube.BYTE_496;

            //PARAMETER BLOCK
            pb.removeParameters();
            pb.removeSources();

            //color map
            pb.addSource(surrogateImage);
            pb.add(colorMap);
            pb.add(ditherMask);

            //building final color model
            //     int bitsNum = 8;
            //      ColorModel cm = new IndexColorModel(bitsNum,
            //              colorMap.getByteData()[0].length,
            ////              colorMap.getByteData()[0], colorMap.getByteData()[1],
            //              colorMap.getByteData()[2], Transparency.OPAQUE);
            // PlanarImage op = PlanarImage.wrapRenderedImage(new BufferedImage(
            //           w, h, BufferedImage.TYPE_BYTE_INDEXED,
            //    (IndexColorModel) cm));
            //layout for the final image
            //      ImageLayout layout = new ImageLayout();
            ////  layout.setMinX(op.getMinX());
            //  layout.setMinY(op.getMinY());
            //  layout.setHeight(op.getHeight());
            //  layout.setWidth(op.getWidth());
            //   layout.setTileWidth(surrogateImage.getTileWidth());
            //   layout.setTileHeight(surrogateImage.getTileHeight());
            //   layout.setTileGridXOffset(surrogateImage.getTileGridXOffset());
            //  layout.setTileGridYOffset(surrogateImage.getTileGridYOffset());
            //  layout.setColorModel(cm);
            //  layout.setSampleModel(op.getSampleModel());
            //        RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            RenderedOp op1 = JAI.create("errordiffusion", pb, null);
            surrogateImage = (PlanarImage) op1.getRendering();
        }

        return surrogateImage;
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
