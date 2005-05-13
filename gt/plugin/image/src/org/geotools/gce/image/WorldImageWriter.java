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

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
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
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.parameter.Parameter;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.spatialschema.geometry.Envelope;


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
    /** format for this writer */
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
     * Encode the given coverage to the requsted output format.
     *
     * @param sourceCoverage the coverage to be encoded.s
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

            /** do we need to go to the geophysic view for this data? */

            //let's check if we have a sampledimension which carried geophysics information
            //in such a case we have to get the visual representation for this data
            //by calling geophysiscs(false)
            
            SampleDimensionType sampleDimensionType=sourceCoverage.getSampleDimension(0).getSampleDimensionType();
           if(sampleDimensionType== SampleDimensionType.UNSIGNED_16BITS||
           		sampleDimensionType== SampleDimensionType.UNSIGNED_8BITS||
           		sampleDimensionType== SampleDimensionType.SIGNED_8BITS){
                /**
                 * GEOPHYSICS(TRUE)?
                 * Are we dealing with a real image and not with the non geophysics representation of an image that 
                 * we built before.
                 */         
            	 surrogateImage = (PlanarImage) (sourceCoverage)
                 .getRenderedImage();
           }else
           	if(sampleDimensionType== SampleDimensionType.SIGNED_16BITS||
           			sampleDimensionType== SampleDimensionType.SIGNED_32BITS||
           			sampleDimensionType== SampleDimensionType.SIGNED_16BITS||
           			sampleDimensionType==SampleDimensionType.REAL_32BITS||
           			sampleDimensionType== SampleDimensionType.REAL_64BITS){
            	/**
            	 * Getting the geophysics view of this grid coverage.
            	 * the geophysiscs view usually comes with an index color model for 3 bands,
            	 * since sometimes I get some problems with JAI encoders I select onyl 
            	 * the first band, which by the way is the only band we use.
            	 * 
            	 */                	
                surrogateImage = ((PlanarImage) (sourceCoverage).geophysics(false)
                                                 .getRenderedImage());

                //removing unused bands from this non geophysics view
                //they might cause prblems with jai encoders
                surrogateImage = JAI.create("bandSelect", surrogateImage,
        	            new int[] { 0 });                    
                 	
            }
            
        
            
            
            /**
             * 
             * 
             * ADJUSTMENTS FOR VARIOUS FILE FORMATS
             * 
             * 
             * 
             * 
             */
            //------------------------GIF-----------------------------------
            if ((((String) (this.format.getWriteParameters().parameter("format")
                                           .getValue())).compareToIgnoreCase(
                        "gif") == 0)) {
            	/**
            	 * component color model is not well digested by the gif encoder
            	 * we need to go to indecolor model somehow.
            	 * 
            	 * This code for the moment remove transparency, but I am confident I will 
            	 * find a way to add that.
            	 * 
            	 */
                if (surrogateImage.getColorModel() instanceof ComponentColorModel) {
                    surrogateImage = componentColorModel2GIF(surrogateImage);
                }

            }else
            	//-----------------TIFF--------------------------------------
            	/**
            	 * TIFF file format.
            	 * 
            	 * We need just a couple of correction for this format. It seems that the encoder does not
            	 * work fine with IndexColorModel therefore in such a case we need the reformat the inpit image to a ComponentColorModel.
            	 */
            	if(((String) (this.format.getWriteParameters().parameter("format")
                        .getValue())).compareToIgnoreCase(
                        "tiff") == 0
                        ||
                        ((String) (this.format.getWriteParameters().parameter("format")
                                .getValue())).compareToIgnoreCase(
                                "tif") == 0) {
            		//Are we dealing with IndexColorModel? If so we need to go back to ComponentColorModel
            		if(surrogateImage.getColorModel() instanceof IndexColorModel){
            	      surrogateImage = reformatFromIndexColorModel2ComponentColorModel(surrogateImage);
            		}
            	}
            

            /**
             * write using JAI encoders
             */
            ImageIO.write(surrogateImage,
                (String) (this.format.getWriteParameters().parameter("format")
                                     .getValue()), output);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

	/**Reformat the index color model to a component color model preserving transparency.
	 * 
	 * @param surrogateImage
	 * @return
	 */
	private PlanarImage reformatFromIndexColorModel2ComponentColorModel(PlanarImage surrogateImage) 
	throws IllegalArgumentException{
		// Format the image to be expanded from IndexColorModel to
		// ComponentColorModel
		ParameterBlock pbFormat = new ParameterBlock();
		pbFormat.addSource(surrogateImage);
		pbFormat.add(surrogateImage.getSampleModel().getTransferType());
		ImageLayout layout = new ImageLayout();
		ColorModel cm1 =null;
		int numBits=0;
		switch(surrogateImage.getSampleModel().getTransferType()){
		case DataBuffer.TYPE_BYTE:
			numBits=8;
			break;
		case DataBuffer.TYPE_USHORT:
			numBits=16;
			break;
		default:
			throw new IllegalArgumentException("Unsupported data type for an index color model!");
		}

		//do we need alpha?
		if(surrogateImage.getColorModel().hasAlpha())
			cm1=new ComponentColorModel(ColorSpace.getInstance(
		            ColorSpace.CS_sRGB), new int[] { numBits, numBits, numBits, numBits }, true, false,
		        Transparency.TRANSLUCENT, surrogateImage.getSampleModel().getTransferType());
		
		else
			cm1=new ComponentColorModel(ColorSpace.getInstance(
		            ColorSpace.CS_sRGB), new int[] { numBits, numBits, numBits}, false, false,
		        Transparency.OPAQUE, surrogateImage.getSampleModel().getTransferType());
		layout.setColorModel(cm1);
		layout.setSampleModel(cm1.createCompatibleSampleModel(surrogateImage.getWidth(),surrogateImage.getHeight()));
		RenderingHints hint = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
		RenderedOp dst = JAI.create("format", pbFormat, hint);
		surrogateImage=dst.createSnapshot();
		return surrogateImage;
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
            RenderedImage alphaChannel = null;

            /**
             * AMPLITUDE RESCALING
             */

            //I might also need to reformat the image in order to get it to 8 bits
            //samples
            if (surrogateImage.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE) {
                surrogateImage = rescale2Byte(surrogateImage);
            }

            /**
             * ALPHA CHANNEL  getting the alpha channel and separating from the
             * others bands.
             */
            if (surrogateImage.getColorModel().hasAlpha()) {
                int numBands = surrogateImage.getSampleModel().getNumBands();

                //getting alpha channel
                alphaChannel = JAI.create("bandSelect", surrogateImage,
                        new int[] { numBands - 1 });
                //getting needed bands
                surrogateImage = getBandsFromImage(surrogateImage, numBands);
            }
            
            /**
             * BAND MERGE
             * 
             * If we do not have 3 bands we have no way to go to
             * index color model in a simple way using jai. Therefore we add 
             * the bands we need in order to get there. This trick works fine with gray
             * scale images. ATTENTION, if the initial image had no alpha channel we proceed without 
             * doing anything since it seems that GIF encoder in such a case works fine.
             * 
             */

            if(surrogateImage.getSampleModel().getNumBands() == 1
            		&&alphaChannel!=null){
            	int numBands = surrogateImage.getSampleModel().getNumBands();
            	//getting first band
            	RenderedImage firstBand = JAI.create("bandSelect", surrogateImage,
                        new int[] {0 });
            	
            	
            	//adding to the image
            	for(int i=0;i<3-numBands;i++){
            			pb.removeParameters();
            			pb.removeSources();
            			
            			pb.addSource(surrogateImage);
            			pb.addSource(firstBand);
            			surrogateImage=JAI.create("bandmerge",pb);
            		
            	}
            }
            

            /**
             * ERROR DIFFUSION 
             * 
             * we create a single banded image with index color model.
             */
            if (surrogateImage.getSampleModel().getNumBands() == 3) {
                surrogateImage = reduction2IndexColorModel(surrogateImage, pb);
            
            }

            /**
             * TRANSPARENCY
             * 
             * Adding transparency if needed, which means using the alpha channel to build a new color
             * model 
             */
            if(alphaChannel!=null){
            	surrogateImage = addTransparency2IndexXolorModel(surrogateImage,alphaChannel, pb);
            }
        }

        return surrogateImage;
    }

	/**
	 * This method is used to add transparency to a preexisting image whose color model is 
	 * indexcolormodel.
	 * 
	 * There are quite a few step to perform here.
	 * 1>Creating a new IndexColorModel which supports transparency, using the given image's colormodel
	 * 
	 * 2>creating a suitable sample model 
	 * 
	 * 3>copying the old sample model to the new sample model.
	 * 
	 * 4>looping through the alphaChannel and setting the corresponding pixels
	 * in the new sample model to the index for transparency
	 * 
	 * 5>creating a bufferedimage
	 * 
	 * 6>creating a planar image
	 * @param surrogateImage
	 * @param alphaChannel
	 * @param pb
	 * @return
	 */
	private PlanarImage addTransparency2IndexXolorModel(PlanarImage surrogateImage, RenderedImage alphaChannel, ParameterBlock pb) {
		// TODO Auto-generated method stub
		return surrogateImage;
	}

	/**
	 * @param surrogateImage
	 * @param pb
	 * @return
	 */
	private PlanarImage reduction2IndexColorModel(PlanarImage surrogateImage, ParameterBlock pb) {
		//error dither
		KernelJAI ditherMask = KernelJAI.ERROR_FILTER_STUCKI; //KernelJAI.DITHER_MASK_443;
		ColorCube colorMap = ColorCube.BYTE_496;
		//PARAMETER BLOCK
		pb.removeParameters();
		pb.removeSources();
		//color map
		pb.addSource(surrogateImage);
		pb.add(colorMap);
		pb.add(ditherMask);
		RenderedOp op1 = JAI.create("errordiffusion", pb, null);
		surrogateImage = op1.createSnapshot();
		return surrogateImage;
	}

	/**Remove the alpha band and keeps the others.
	 * @param surrogateImage
	 * @param numBands
	 * @return
	 */
	private PlanarImage getBandsFromImage(PlanarImage surrogateImage, int numBands) {
		switch (numBands-1) {
		case 1:
		    surrogateImage = JAI.create("bandSelect", surrogateImage,
		            new int[] { 0 });

		    break;

		case 3:
		    surrogateImage = JAI.create("bandSelect", surrogateImage,
		            new int[] { 0, 1, 2 });

		    break;
		}
		return surrogateImage;
	}

    /**
     * DOCUMENT ME!
     *
     * @param surrogateImage
     *
     * @return
     */
    private PlanarImage rescale2Byte(PlanarImage surrogateImage) {
        //rescale the initial image in order
        //to expand the dynamic

        /** EXTREMA */

        // Set up the parameter block for the source image and
        // the constants
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(surrogateImage); // The source image
        pb.add(null); // The region of the image to scan
        pb.add(1); // The horizontal sampling rate
        pb.add(1); // The vertical sampling rate

        // Perform the extrema operation on the source image
        // Retrieve both the maximum and minimum pixel value
        double[][] extrema = (double[][]) JAI.create("extrema", pb).getProperty("extrema");

        /**
         * RESCSALE
         */
        pb.removeSources();
        pb.removeParameters();

        //set the levels for the dynamic
        pb.addSource(surrogateImage);

        //rescaling each band to 8 bits
        double[] scale = new double[extrema[0].length];
        double[] offset = new double[extrema[0].length];

        for (int i = 0; i < extrema[0].length; i++) {
            scale[i] = 255 / (extrema[1][i] - extrema[0][i]);
            offset[i] = -((255 * extrema[0][i]) / (extrema[1][i]
                - extrema[0][i]));
        }

        pb.add(scale);
        pb.add(offset);

        RenderedOp image2return = JAI.create("rescale", pb);

        //setting up the right layout for this image
        ImageLayout layout = new ImageLayout(image2return);
        pb.removeParameters();
        pb.removeSources();
        pb.addSource(image2return);
        pb.add(DataBuffer.TYPE_BYTE);

        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        image2return = JAI.create("format", pb, hints);

        return image2return;
    }

    //	private RenderedImage highlightImage(RenderedImage stillImg) {
    //        RenderedImage dispImg = null;
    //
    //        // Highlight the human pixels (detection results img)
    //        // Create a constant image
    //        Byte[] bandValues = new Byte[1];
    //
    //        bandValues[0] = new Byte("65"); //32 -- orangeish, 65 -- greenish
    //
    //        ParameterBlock pbConstant = new ParameterBlock();
    //
    //        pbConstant.add(new Float(stillImg.getWidth())); // The width
    //
    //        pbConstant.add(new Float(stillImg.getHeight())); // The height
    //
    //        pbConstant.add(bandValues); // The band values
    //
    //        PlanarImage imgConstant = (PlanarImage) JAI.create("constant",
    //                pbConstant);
    //
    //        //System.out.println("Making multiply image");
    //        // Multiply the mask by 255 so the values are 0 or 255
    //        ParameterBlock pbMultiply = new ParameterBlock();
    //
    //        pbMultiply.addSource((PlanarImage) stillImg);
    //
    //        double[] multiplyArray = new double[] { 255.0 };
    //
    //        pbMultiply.add(multiplyArray);
    //
    //        PlanarImage imgMask = (PlanarImage) JAI.create("multiplyconst",
    //                pbMultiply);
    //
    //        //System.out.println("Making IHS image");
    //        // Create a Intensity, Hue, Saturation image
    //        ParameterBlock pbIHS = new ParameterBlock();
    //
    //        pbIHS.setSource(stillImg, 0); //still img is the intensity
    //
    //        pbIHS.setSource(imgConstant, 1); //constant img is the hue
    //
    //        pbIHS.setSource(imgMask, 2); //mask is the saturation
    //
    //        //create rendering hint for IHS image to specify the color model
    //        ComponentColorModel IHS_model = new ComponentColorModel(IHSColorSpace
    //                .getInstance(), new int[] { 8, 8, 8 }, false, false,
    //                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    //
    //        ImageLayout layout = new ImageLayout();
    //
    //        layout.setColorModel(IHS_model);
    //
    //        RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    //
    //        PlanarImage IHSImg = JAI.create("bandmerge", pbIHS, rh);
    //
    //        //System.out.println("Making RGB image");
    //        // Convert IHS image to a RGB image
    //        ParameterBlock pbRGB = new ParameterBlock();
    //
    //        //create rendering hint for RGB image to specify the color model
    //        ComponentColorModel RGB_model = new ComponentColorModel(ColorSpace
    //                .getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 }, false,
    //                false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    //
    //        pbRGB.addSource(IHSImg);
    //
    //        pbRGB.add(RGB_model);
    //
    //        dispImg = JAI.create("colorconvert", pbRGB);
    //
    //        return dispImg;
    //    }
}
