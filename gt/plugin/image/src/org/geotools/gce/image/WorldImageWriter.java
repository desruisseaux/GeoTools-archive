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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
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
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.parameter.Parameter;
import org.opengis.coverage.MetadataNameNotFoundException;
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
            this.format.getWriteParameters().parameter("format").setValue(((Parameter) parameters[0]).stringValue());
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
            RenderedImage image = ((PlanarImage) ((GridCoverage2D) coverage).getRenderedImage());
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
            image = null;

            //files destinations
            File imageFile = (File) destination;
            String path = imageFile.getAbsolutePath();
            int index = path.lastIndexOf(".");
            String baseFile = path.substring(0, index);
            File worldFile = new File(baseFile +
                    WorldImageFormat.getWorldExtension(
                        format.getWriteParameters().parameter("format")
                              .stringValue()));

            //create new files
            imageFile = new File(baseFile + "." +
                    format.getWriteParameters().parameter("format").stringValue());
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

            /**
             * Getting the non geophysics view of this grid coverage. the
             * geophysiscs view usually comes with an index color model
             * for 3 bands, since sometimes I get some problems with JAI
             * encoders I select only  the first band, which by the way is
             * the only band we use.
             */
            surrogateImage = ((PlanarImage) (sourceCoverage).geophysics(false)
                                             .getRenderedImage());

			//surrogateImage=cleanIndexColorModel(surrogateImage);
			//removing unused bands from this non geophysics view
            //they might cause prblems with jai encoders
            if (surrogateImage.getColorModel() instanceof IndexColorModel &&
                    (surrogateImage.getSampleModel().getNumBands() > 1)) {
                surrogateImage = JAI.create("bandSelect", surrogateImage,
                        new int[] { 0 });
            }

            //            }

			if (surrogateImage.getColorModel() instanceof DirectColorModel ) {
		        surrogateImage = direct2ComponentColorModel(surrogateImage);


            }				
            /**
             * ADJUSTMENTS FOR VARIOUS FILE FORMATS
             */

            //------------------------GIF-----------------------------------
            if ((((String) (this.format.getWriteParameters().parameter("format")
                                           .getValue())).compareToIgnoreCase(
                        "gif") == 0)) {
				/**
				 * For the moment we do not work with DirectColorModel but instead we switch to 
				 * component color model which is really easier to handle even if it much more memory expensive.
				 * Once we are in component color model is really easy to go to Gif and similar.
				 * 
				 */
				if (surrogateImage.getColorModel() instanceof DirectColorModel ) {
	                surrogateImage = this.reformatColorModel2ComponentColorModel(surrogateImage);
	            }				
				/**
				 * IndexColorModel with more than 8 bits for sample might be a problem because GIF allows only 8 bits based palette 
				 * therefore I prefere switching to component color model in order to handle this properly.
				 * 
				 * NOTE. The only transfert types avalaible for IndexColorModel are byte and ushort.
				 */
				if (surrogateImage.getColorModel() instanceof IndexColorModel &&
                        (surrogateImage.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE)) {
                    surrogateImage = this.reformatColorModel2ComponentColorModel(surrogateImage);
                }				
                /**
                 * component color model is not well digested by the gif
                 * encoder we need to go to indecolor model somehow.  This
                 * code for the moment remove transparency, but I am confident
                 * I will  find a way to add that.
                 */
                if (surrogateImage.getColorModel() instanceof ComponentColorModel) {
                    surrogateImage = componentColorModel2IndexColorModel4GIF(surrogateImage);
                } else
                /**
                 * IndexColorModel with full transparency support is not
                 * suitable for gif images we need to go to bitmask loosing
                 * some informations. we have only one full transparent color.
                 */
                if (surrogateImage.getColorModel() instanceof IndexColorModel
                      
						) {
                    surrogateImage = convertIndexColorModelAlpha4GIF(surrogateImage);
                }
            } else
            //-----------------TIFF--------------------------------------

            /**
             * TIFF file format.  We need just a couple of correction for this
             * format. It seems that the encoder does not work fine with
             * IndexColorModel therefore in such a case we need the reformat
             * the input image to a ComponentColorModel.
             */
            if ((((String) (this.format.getWriteParameters().parameter("format")
                                           .getValue())).compareToIgnoreCase(
                        "tiff") == 0) ||
                    (((String) (this.format.getWriteParameters()
                                               .parameter("format").getValue())).compareToIgnoreCase(
                        "tif") == 0)) {
                //Are we dealing with IndexColorModel? If so we need to go back to ComponentColorModel
                if (surrogateImage.getColorModel() instanceof IndexColorModel) {
                    surrogateImage = reformatColorModel2ComponentColorModel(surrogateImage);
                }
            }

            /**
             * write using JAI encoders
             */
            ImageIO.write(surrogateImage,
                (String) (this.format.getWriteParameters().parameter("format")
                                     .getValue()), output);
        } catch (Exception e) {
            throw new IOException("Error when writing world image: "+e.getMessage());
        }
    }

	/**
	 * 
	 * @param surrogateImage
	 * @return
	 */
//	private PlanarImage cleanIndexColorModel(PlanarImage surrogateImage) {
//		final ColorModel cm=surrogateImage.getColorModel();
//		  if(cm instanceof IndexColorModel){
//			final boolean hasAlpha=cm.hasAlpha();
//			final int transparency=cm.getTransparency();
//			
//			//bitmask
//			if
//		  }
//		return surrogateImage;
//	}

	/**
	 * This method allows me to go from DirectColorModel to ComponentColorModel
	 * which seems to be well acepted from PNGEncoder and TIFFEncoder.
	 * 
	 * @param surrogateImage
	 * @return
	 */
	private PlanarImage direct2ComponentColorModel(PlanarImage surrogateImage) {
		ParameterBlockJAI pb = new ParameterBlockJAI("ColorConvert");
		pb.addSource(surrogateImage);
		int numBits=8;
		if(DataBuffer.TYPE_INT==surrogateImage.getSampleModel().getTransferType())
			numBits=32;
		else
			if(DataBuffer.TYPE_USHORT==surrogateImage.getSampleModel().getTransferType()||
					DataBuffer.TYPE_SHORT==surrogateImage.getSampleModel().getTransferType())
				numBits=16;
			else
				if(DataBuffer.TYPE_FLOAT==surrogateImage.getSampleModel().getTransferType())
					numBits=32;
				else
					if(DataBuffer.TYPE_DOUBLE==surrogateImage.getSampleModel().getTransferType())
						numBits=64;
		ComponentColorModel colorModel = new ComponentColorModel(surrogateImage.getColorModel().getColorSpace(),
		                                                         new int[] { 
																		numBits,
																		numBits,
																		numBits,
																		numBits 
																 },
		                                                         false,
		                                                         surrogateImage.getColorModel().hasAlpha(),
																 surrogateImage.getColorModel().getTransparency(),
																 surrogateImage.getSampleModel().getTransferType());
		pb.setParameter("colormodel", colorModel);
		ImageLayout layout = new ImageLayout();
		layout.setColorModel(colorModel);
		layout.setSampleModel(colorModel.createCompatibleSampleModel(surrogateImage.getWidth(), surrogateImage.getHeight()));
		RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
		surrogateImage = JAI.create("ColorConvert", pb, hints).createInstance();
        pb.removeParameters();
        pb.removeSources();
		
		return surrogateImage;
	}



	/**
     * GIF does not support full alpha channel we need to reduce it in order to
     * provide a simple transparency index to a unique fully transparent
     * color.
     *
     * @param surrogateImage
     *
     * @return
     */
    private PlanarImage convertIndexColorModelAlpha4GIF(
        PlanarImage surrogateImage) {
		//doing nothing if the input color model is correct
        final IndexColorModel cm = (IndexColorModel) surrogateImage.getColorModel();
		if(cm.getTransparency()==Transparency.OPAQUE)
			return surrogateImage;
			

		
        byte[][] rgba = new byte[4][256]; //WE MIGHT USE LESS THAN 256 COLORS
 
		//getting all the colors
		cm.getReds(rgba[0]);
		cm.getGreens(rgba[1]);
		cm.getBlues(rgba[2]);
		


        //get the data (actually a copy of them) and prepare to rewrite them
        WritableRaster raster = surrogateImage.copyData();

        /**
         * Now we are going to use the first transparent color as it were the
         * transparent color and we point all the tranpsarent pixel to this
         * color in the color map.
         *
         *
         * NOTE Assuming we have just one band.
         */
        int transparencyIndex = -1;
        int index = -1;
		final int H=raster.getHeight();
		final int W=raster.getWidth();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                //index in the color map is given by a value in the raster.
                index = raster.getSample(j, i, 0);

                //check for transparency
                if ((cm.getAlpha(index)&0xff) == 0) {
                    //FULLY TRANSPARENT PIXEL
                    if (transparencyIndex == -1) {
                        //setting transparent color to this one
                        //the other tranpsarent bits will point to this one
                        transparencyIndex = cm.getAlpha(index);
  
                        //                      setting sample in the raster that corresponds to an index in the
                        //color map
                        raster.setSample(j, i, 0, transparencyIndex++);
                    } else //we alredy set the transparent color we will reuse that one
                     {
                        //basically do nothing here
                        //we do not need to add a new color because we are reusing the old on
                        //we already set
                        //                      setting sample in the raster that corresponds to an index in the
                        //color map
                        raster.setSample(j, i, 0, transparencyIndex);
                    }
                } else //NON FULLY TRANSPARENT PIXEL
                 {

                    //setting sample in the raster that corresponds to an index in the
                    //color map                    
                    //raster.setSample(j, i, 0, colorIndex++);
                }
            }
        }

        /**
         * Now all the color are opaque except one and the color map has been
         * rebuilt loosing all the tranpsarent colors except the first one.
         * The raster has been rebuilt as well, in order to make it point to the
         * right color in the color map.  We have to create the new image
         * to be returned.
         */
        IndexColorModel cm1 =transparencyIndex==-1? new IndexColorModel(
				cm.getComponentSize(0),
				256,
				rgba[0],
				rgba[1],
				rgba[2]): new IndexColorModel(
						cm.getComponentSize(0),
						256,
						rgba[0],
						rgba[1],
						rgba[2],
						transparencyIndex);
		
		BufferedImage image= new BufferedImage(raster.getWidth(),
				raster.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED,
				cm1);
        image.setData(raster);

        //disposing old image
        surrogateImage.dispose();

        return PlanarImage.wrapRenderedImage(image);
    }

    /**
     * Reformat the index color model to a component color model preserving
     * transparency.
     * Code from jai-interests archive.
     * @param surrogateImage
     *
     * @return
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private PlanarImage reformatColorModel2ComponentColorModel(
        PlanarImage surrogateImage) throws IllegalArgumentException {
        // Format the image to be expanded from IndexColorModel to
        // ComponentColorModel
        ParameterBlock pbFormat = new ParameterBlock();
        pbFormat.addSource(surrogateImage);
        pbFormat.add(surrogateImage.getSampleModel().getTransferType());

        ImageLayout layout = new ImageLayout();
        ColorModel cm1 = null;
        final int numBits;

        switch (surrogateImage.getSampleModel().getTransferType()) {
        case DataBuffer.TYPE_BYTE:
            numBits = 8;
            break;

        case DataBuffer.TYPE_USHORT:
            numBits = 16;
            break;
		case DataBuffer.TYPE_SHORT:
            numBits = 16;
            break;

        case DataBuffer.TYPE_INT:
            numBits = 32;
            break;			
		case DataBuffer.TYPE_FLOAT:
            numBits = 32;
            break;
		case DataBuffer.TYPE_DOUBLE:
			numBits=64;
			break;

        default:
            throw new IllegalArgumentException(
                "Unsupported data type for an index color model!");
        }

        //do we need alpha?
		final int transparency=surrogateImage.getColorModel().getTransparency();
		final int transpPixel=((IndexColorModel)surrogateImage.getColorModel()).getTransparentPixel();
        if (transparency!=Transparency.OPAQUE) {
            cm1 = new ComponentColorModel(ColorSpace.getInstance(
                        ColorSpace.CS_sRGB),
                    new int[] { numBits, numBits, numBits, numBits }, true,
                    false,transparency,
                    surrogateImage.getSampleModel().getTransferType());
        } else {
            cm1 = new ComponentColorModel(ColorSpace.getInstance(
                        ColorSpace.CS_sRGB),
                    new int[] { numBits, numBits, numBits }, false, false,
					transparency,
                    surrogateImage.getSampleModel().getTransferType());
        }

        layout.setColorModel(cm1);
        layout.setSampleModel(cm1.createCompatibleSampleModel(
                surrogateImage.getWidth(), surrogateImage.getHeight()));

        RenderingHints hint = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        RenderedOp dst = JAI.create("format", pbFormat, hint);
        surrogateImage = dst.createSnapshot();
		pbFormat.removeParameters();
		pbFormat.removeSources();
		dst.dispose();
		
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
    private PlanarImage componentColorModel2IndexColorModel4GIF(
        PlanarImage surrogateImage) {
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
             * BAND MERGE  If we do not have 3 bands we have no way to go to
             * index color model in a simple way using jai. Therefore we add
             * the bands we need in order to get there. This trick works fine
             * with gray scale images. ATTENTION, if the initial image had no
             * alpha channel we proceed without  doing anything since it seems
             * that GIF encoder in such a case works fine.
             */
            if ((surrogateImage.getSampleModel().getNumBands() == 1) &&
                    (alphaChannel != null)) {
                int numBands = surrogateImage.getSampleModel().getNumBands();

                //getting first band
                RenderedImage firstBand = JAI.create("bandSelect",
                        surrogateImage, new int[] { 0 });

                //adding to the image
                for (int i = 0; i < (3 - numBands); i++) {
                    pb.removeParameters();
                    pb.removeSources();

                    pb.addSource(surrogateImage);
                    pb.addSource(firstBand);
                    surrogateImage = JAI.create("bandmerge", pb);
					
                    pb.removeParameters();
                    pb.removeSources();					
                }
            }

            /**
             * ERROR DIFFUSION   we create a single banded image with index
             * color model.
             *
             */
            if (surrogateImage.getSampleModel().getNumBands() == 3) {
                surrogateImage = reduction2IndexColorModel(surrogateImage, pb);
            }
		
		
			
            /**
             * TRANSPARENCY  Adding transparency if needed, which means using
             * the alpha channel to build a new color model
             *
             */
            if (alphaChannel != null) {
                surrogateImage = addTransparency2IndexColorModel(surrogateImage,
                        alphaChannel, pb);
            }
        }
	  
        return surrogateImage;
    }

    /**
     * This method is used to add transparency to a preexisting image whose
     * color model is  indexcolormodel.  There are quite a few step to perform
     * here. 1>Creating a new IndexColorModel which supports transparency,
     * using the given image's colormodel  2>creating a suitable sample model
     * 3>copying the old sample model to the new sample model.  4>looping
     * through the alphaChannel and setting the corresponding pixels in the
     * new sample model to the index for transparency  5>creating a
     * bufferedimage  6>creating a planar image to be returned
     *
     * @param surrogateImage
     * @param alphaChannel
     * @param pb
     *
     * @return
     */
    private PlanarImage addTransparency2IndexColorModel(
        final PlanarImage surrogateImage, final RenderedImage alphaChannel,
        ParameterBlock pb) {
        //getting original color model 
        //in order to have the rgba vector
        final IndexColorModel cm = (IndexColorModel) surrogateImage.getColorModel();

        //get the r g b a components
        final int transparencyIndex = 255;
		
        byte[][] rgba = new byte[3][256]; //WE MIGHT USE LESS THAN 256 COLORS
		//cm.getRGBs(rgba);
		cm.getReds(rgba[0]);
		cm.getGreens(rgba[1]);
		cm.getBlues(rgba[2]);
		//setting color
		rgba[0][transparencyIndex]=0;
		rgba[1][transparencyIndex]=0;
		rgba[2][transparencyIndex]=0;
		
        //get the data (actually a copy of them) and prepare to rewrite them
        WritableRaster rasterGIF = surrogateImage.copyData();
		Raster rasterAlpha=((PlanarImage)alphaChannel).copyData();
		((PlanarImage)alphaChannel).dispose();
		
        /**
         * Now we are going to use the first transparent color as it were the
         * transparent color and we point all the tranpsarent pixel to this
         * color in the color map.
         *
         *
         * NOTE Assuming we have just one band.
         */

        boolean foundFullyTransparent=false;
        
		final int minX=rasterGIF.getMinX();
		final int minY=rasterGIF.getMinY();
		final int W=rasterGIF.getWidth();
		final int H=rasterGIF.getHeight();
        for (int i = minY; i <H ; i++) {
            for (int j = minX; j < W; j++) {

                //check for transparency
                if (rasterAlpha.getSample(j,i,0)== 0) {
                    //FULLY TRANSPARENT PIXEL
					foundFullyTransparent=true;
                    rasterGIF.setSample(j, i, 0,transparencyIndex );
                }
            }
        }

        /**
         * Now all the color are opaque except one and the color map has been
         * rebuilt loosing all the tranpsarent colors except the first one.
         * The raster has been rebuilt as well, in order to make it point to the
         * right color in the color map.  We have to create the new image
         * to be returned.
         */
        IndexColorModel cm1 = new IndexColorModel(
				cm.getPixelSize(),
				256,
				rgba[0],
				rgba[1],
				rgba[2],
				255);
				

		BufferedImage image= new BufferedImage(rasterGIF.getWidth(),
				rasterGIF.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED,
				cm1);
        image.setData(rasterGIF);

        //disposing old image
        surrogateImage.dispose();
		
		PlanarImage retImage=PlanarImage.wrapRenderedImage(image);
		image=null;
		rasterGIF=null;
		rasterAlpha=null;
        return retImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param surrogateImage
     * @param pb
     *
     * @return
     */
    private PlanarImage reduction2IndexColorModel(PlanarImage surrogateImage,
        ParameterBlock pb) {
        //error dither
        final KernelJAI ditherMask = KernelJAI.ERROR_FILTER_STUCKI; //KernelJAI.DITHER_MASK_443;
        final ColorCube colorMap = ColorCube.BYTE_496;

        //PARAMETER BLOCK
        pb.removeParameters();
        pb.removeSources();

        //color map
        pb.addSource(surrogateImage);
        pb.add(colorMap);
        pb.add(ditherMask);

        RenderedOp op1 = JAI.create("errordiffusion", pb, null);
        pb.removeParameters();
        pb.removeSources();
		
        return op1.createSnapshot();
    }

    /**
     * Remove the alpha band and keeps the others.
     *
     * @param surrogateImage
     * @param numBands
     *
     * @return
     */
    private PlanarImage getBandsFromImage(PlanarImage surrogateImage,
        int numBands) {
        switch (numBands - 1) {
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

		final int length=extrema[0].length;
        for (int i = 0; i < length; i++) {
            scale[i] = 255 / (extrema[1][i] - extrema[0][i]);
            offset[i] = -((255 * extrema[0][i]) / (extrema[1][i] -
                extrema[0][i]));
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
        pb.removeSources();
        pb.removeParameters();
		surrogateImage.dispose();
		
        return image2return;
    }

   
}
