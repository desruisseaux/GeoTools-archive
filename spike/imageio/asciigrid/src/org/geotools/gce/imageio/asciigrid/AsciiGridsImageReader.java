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
package org.geotools.gce.imageio.asciigrid;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RasterFactory;

import org.geotools.gce.imageio.asciigrid.raster.AsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.EsriAsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.GrassAsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.spi.AsciiGridsImageReaderSpi;

/**
 * Class used for reading ASCII ArcInfo Grid Format (ArcGrid) and ASCII GRASS
 * Grid Format and to create {@link RenderedImage}s and {@link Raster}s.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class AsciiGridsImageReader extends ImageReader {
	/** Logger. */
	private static final Logger logger = Logger
			.getLogger("org.geotools.gce.imageio.asciigrid");

	private static final int MIN_SIZE_NEED_TILING = 5242880; // 5 MByte

	private static final int DEFAULT_TILE_SIZE = 1048576 / 2; // 1 MByte

	private boolean GRASS = false;

	/** Image Dimensions */
	private int width = -1;

	/** Image Dimensions */
	private int height = -1;

	/** Image Size */
	private int imageSize = -1;

	// if the imageSize is bigger than MIN_SIZE_NEED_TILING
	// we proceed to image tiling
	private boolean isTiled = false;

	/**
	 * Tile width for the underlying raster.
	 */
	private int tileWidth = -1;

	/**
	 * Tile height for the underlying raster.
	 */
	private int tileHeight = -1;

	/**
	 * The thread-safe {@link AsciiGridRaster} to read rasters out of an ascii
	 * grid file.
	 * 
	 * <p>
	 * Every {@link AsciiGridsImageReader} will cache this raster-reader between
	 * differet reads because it will internally save information about the
	 * positions of the tiles on disk.
	 * 
	 */
	private AsciiGridRaster rasterReader = null;

	/**
	 * The Color model for an {@link AsciiGridsImageReader}.
	 * 
	 * The color model is always the same, moreover a {@link ColorModel} in java
	 * is an immutable, therefore it is possible to create it just once for all
	 * the possible {@link AsciiGridsImageReader}.
	 * 
	 * 
	 */
	private final static ComponentColorModel cm = RasterFactory
			.createComponentColorModel(DataBuffer.TYPE_FLOAT,
			// dataType
					ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency;

	/** The {@link SampleModel} associated to this reader. */
	private SampleModel sm;

	/** The {@link ImageTypeSpecifier} associated to this reader. */
	private ImageTypeSpecifier imageType;

	/** Default {@link ImageReadParam} for this reader. */
	private final ImageReadParam imageReadParam = getDefaultReadParam();

	/** The {@link imageInputStream} associated to this reader. */
	private ImageInputStream imageInputStream = null;

	/** The {@link AsciiGridsImageMetadata} associated to this reader. */
	private AsciiGridsImageMetadata metadata;

	/**
	 * Constructor.
	 * 
	 * It builts up an {@link AsciiGridsImageReader} by providing an
	 * {@link AsciiGridsImageReaderSpi}
	 * 
	 * @param originatingProvider
	 */
	public AsciiGridsImageReader(AsciiGridsImageReaderSpi originatingProvider) {
		super(originatingProvider);

	}

	/**
	 * this method sets the input for the AsciiGridsImageReader.
	 * 
	 * @param input
	 *            Source the AsciiGridsImageReader will read from
	 * 
	 * <strong>NOTE: Constrain on GZipped InputStream</strong> If we want to
	 * provide explicitly an InputStream (instead of a File) for a GZipped
	 * source, we MUST provide a proper previously created GZIPInputStream
	 * instead of a simple InputStream.
	 * 
	 * Thus, you need to use Code A) instead of Code B): <blockquote> //as an
	 * instance: File file = new File("example.asc.gz"); //A GZipped Source ...
	 * //Code A) GZIPInputStream stream = new GZIPInputStream(new
	 * FileInputStream(file));
	 * 
	 * //Code B) //InputStream stream = new FileInputStream(file);
	 * 
	 * </blockquote> Otherwise, when calling
	 * {@code ImageIO.getImageReaders(stream)}, (directly or indirectly by a
	 * Jai ImageRead Operation), the proper SPI can't correctly try to read the
	 * Header in order to decode the input.
	 * 
	 */
	public void setInput(Object input) {

		// //
		//
		// Reset the state of this reader
		//
		// Prior to set a new input, I need to do a pre-emptive reset in order
		// to clear any value-object related to the previous input.
		// //
		if (this.imageInputStream != null) {
			reset();
		}
		
		if (logger.isLoggable(Level.FINE))
			logger.fine("Setting Input");


	
		if (input instanceof ImageInputStream)
			imageInputStream = (ImageInputStream) input;

		else {
// try {

				imageInputStream = ImageIO.createImageInputStream((File) input);
// } catch (Exception e) {
// throw new RuntimeException("Not a Valid Input", e);
// }

		}



		if(imageInputStream==NULL) {
			// XXXXX
			throw new IllegalArgumentException(
					"Unsupported object provided as input!");
		}

		// /////////////////////////////////////////////////////////////////////
		// Now, I have an ImageInputStream and I can try to see if input can
		// be decoded by doing header parsing
		// /////////////////////////////////////////////////////////////////////
		try {
			imageInputStream.reset();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		imageInputStream.mark();

		try {

			// Header Parsing to check if it is an EsriAsciiGridRaster
			rasterReader = new EsriAsciiGridRaster(imageInputStream, this);
			rasterReader.parseHeader();
		} catch (IOException e) {
			try {

				// Header Parsing to know if it is a GrassAsciiGridRaster
				rasterReader = new GrassAsciiGridRaster(imageInputStream, this);
				rasterReader.parseHeader();
			} catch (IOException e1) {
				// Input cannot be decoded
				throw new RuntimeException(
						"Unable to parse the header for the provided input", e1);
			}
		}

		// setting input on superclass
		super.setInput(input, true, false);

		// reading information
		initializeReader();

	}

	/**
	 * This method initializes the {@link AsciiGridsImageReader} (if it has
	 * already decoded an input source) by setting some fields, like the
	 * imageInputStream, the {@link ColorModel} and the {@link SampleModel},
	 * the image dimensions and so on.
	 * 
	 */
	private void initializeReader() {

		GRASS = rasterReader instanceof GrassAsciiGridRaster;

		if (LoggerController.enableLoggerReader) {
			logger.info("Data Initializing");
			logger.info("\tImageInputStream: \t" + imageInputStream.toString());
			logger.info("\tGrass:\t\t\t " + GRASS);
		}

		// Image dimensions initialization
		width = rasterReader.getNCols();
		height = rasterReader.getNRows();

		sm = cm.createCompatibleSampleModel(width, height);

		// calculating the imageSize. Its value is given by
		// nRows*nCols*sampleSizeByte (if DataType is Float
		// the size of each sample is 32 bit = 4 Byte)
		final int dataType = sm.getDataType();
		final int sampleSizeBit = DataBuffer.getDataTypeSize(dataType);
		final int sampleSizeByte = (sampleSizeBit + 7) / 8;

		imageSize = width * height * sampleSizeByte;

		/**
		 * Setting Tile Dimensions (If Tiling is supported)
		 */

		// if the Image Size is greater than a certain dimension
		// (MIN_SIZE_NEED_TILING), the image needs to be tiled
		if (imageSize >= MIN_SIZE_NEED_TILING) {
			isTiled = true;

			// This implementation supposes that tileWidth is equal to the width
			// of the whole image
			tileWidth = width;

			// actually (need improvements) tileHeight is given by
			// the default tile size divided by the tileWidth multiplied by the
			// sample size (in byte)
			tileHeight = DEFAULT_TILE_SIZE / (tileWidth * sampleSizeByte);

			// if computed tileHeight is zero, it is setted to 1 as precaution
			if (tileHeight < 1) {
				tileHeight = 1;
			}

			rasterReader.setTilesData(tileWidth, tileHeight);
		} else {
			// If no Tiling needed, I set the tile sizes equal to the image
			// sizes
			tileWidth = width;
			tileHeight = height;
		}

		// image type specifier
		imageType = new ImageTypeSpecifier(cm, sm);
	}

	/**
	 * This method check if the ImageIndex indicated is valid In the
	 * AsciiGridsImageReader, the only legal imageIndex value is 0
	 * 
	 * @param imageIndex
	 *            the specified imageIndex
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if imageIndex is greater than 0.
	 */
	private void checkImageIndex(final int imageIndex) {
		/* AsciiGrid file format can "contain" only 1 image */
		if (imageIndex != 0) {
			throw new IndexOutOfBoundsException("illegal Index");
		}
	}

	/**
	 * Returns the height in pixels of the image
	 * 
	 * @param imageIndex
	 *            the specified imageIndex
	 * 
	 * @see javax.imageio.ImageReader#getHeight(int)
	 */
	public int getHeight(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return height;
	}

	/**
	 * Returns the width in pixels of the image
	 * 
	 * @param imageIndex
	 *            the specified imageIndex
	 * 
	 * @see javax.imageio.ImageReader#getWidth(int)
	 */
	public int getWidth(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return width;
	}

	/**
	 * Returns the number of images available from the current input source.
	 * AsciiGrid input source contains data for a single image. So this method
	 * return "1"
	 * 
	 * @see javax.imageio.ImageReader#getNumImages(boolean)
	 */
	public int getNumImages(final boolean allowSearch) throws IOException {
		return 1;
	}

	/**
	 * this method provides suggestions for possible image types that will be
	 * used to decode the image. In this case, we are suggesting using a 32 bit
	 * grayscale image with no alpha component.
	 * 
	 * @param imageIndex
	 *            arcGrid handle only one image.
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public Iterator getImageTypes(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		final List l = new java.util.ArrayList();

		if (imageType == null) {
			imageType = new ImageTypeSpecifier(cm, sm);
		}

		l.add(imageType);

		return l.iterator();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		logger.info("getStreamMetadata");
		return null;
	}

	public IIOMetadata getImageMetadata(final int imageIndex)
			throws IOException {
		checkImageIndex(imageIndex);

		if (metadata == null)
			metadata = new AsciiGridsImageMetadata(this.rasterReader);
		return metadata;

	}

	/**
	 * Reads the raster and return a BufferedImage
	 * 
	 * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
	 */
	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger.info("read(final int imageIndex, ImageReadParam param)");
		}

		checkImageIndex((imageIndex));
		processImageStarted(imageIndex);
		return new BufferedImage(cm, (WritableRaster) readRaster(imageIndex,
				param), false, null);

		// // Initialize the destination image
		// final Iterator imageTypes = getImageTypes(0);
		// imageTypes
		// .next();
		//
		// final BufferedImage theImage = getDestination(param, imageTypes,
		// width,
		// height);
		//
		// final Rectangle srcRegion = new Rectangle(0, 0, 0, 0);
		// final Rectangle destRegion = new Rectangle(0, 0, 0, 0);
		//
		// computeRegions(imageReadParam, width, height, theImage, srcRegion,
		// destRegion);
		//
		// // Creating a color model if not present
		// synchronized (mutex) {
		//			
		// }
		// if ((sm == null) || (cm == null)) {
		// cm = RasterFactory.createComponentColorModel(DataBuffer.TYPE_FLOAT,
		// // dataType
		// ColorSpace
		// .getInstance(ColorSpace.CS_GRAY), // color space
		// false, // has alpha
		// false, // is alphaPremultiplied
		// Transparency.OPAQUE); // transparency
		// }
		//
		// processImageComplete();
		// return new BufferedImage(cm, rasterTiled, false, null);
	}

	public int getTileGridXOffset(final int imageIndex) throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger.info("getTileGridXOffset(final int imageIndex)");
		}

		return 0;
	}

	public int getTileGridYOffset(final int imageIndex) throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger.info("getTileGridYOffset(final int imageIndex)");
		}

		return 0;
	}

	public int getTileHeight(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return tileHeight;

		// If the image is not Tiled, tile Height = Whole Image Height
	}

	public int getTileWidth(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return tileWidth;

		// If the image is not Tiled, tile Width = Whole Image Width
	}

	public boolean isImageTiled(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return isTiled;
	}

	public boolean isRandomAccessEasy(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		return false;
	}

	public boolean isSeekForwardOnly() {
		return true;
	}

	public BufferedImage read(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);

		if (LoggerController.enableLoggerReader) {
			logger.info("read(imageIndex");
		}

		return read(imageIndex, null);
	}

	public RenderedImage readAsRenderedImage(final int imageIndex,
			ImageReadParam param) throws IOException {
		return read(imageIndex, param);
	}

	public boolean readerSupportsThumbnails() {
		return false;
	}

	public Raster readRaster(final int imageIndex, ImageReadParam param)
			throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger
					.info("readRaster(final int imageIndex, ImageReadParam param)");
		}
		if (param == null) {
			param = getDefaultReadParam();
		}
		return rasterReader.readRaster(param);
	}

	public BufferedImage readTile(final int imageIndex, final int tileX,
			final int tileY) throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger
					.info("readTile(final int imageIndex, final int tileX, final int tileY)");
		}

		checkImageIndex(imageIndex);

		final int w = getWidth(imageIndex);
		final int h = getHeight(imageIndex);
		int tw = getTileWidth(imageIndex);
		int th = getTileHeight(imageIndex);

		int x = tw * tileX;
		int y = th * tileY;

		if ((tileX < 0) || (tileY < 0) || (x >= w) || (y >= h)) {
			throw new IllegalArgumentException(
					"Tile indices are out of bounds!");
		}

		// if tile overcomes the rightern image bound
		// tile will be resized
		if ((x + tw) > w) {
			tw = w - x;
		}

		// if tile overcomes the vertical image bound
		// tile will be resized
		if ((y + th) > h) {
			th = h - y;
		}

		ImageReadParam param = getDefaultReadParam();
		Rectangle tileRect = new Rectangle(x, y, tw, th);
		param.setSourceRegion(tileRect);

		return read(imageIndex, param);
	}

	public Raster readTileRaster(final int imageIndex, int tileX, int tileY)
			throws IOException {
		if (LoggerController.enableLoggerReader) {
			logger
					.info("readTileRaster(final int imageIndex, int tileX, int tileY)");
		}
		return readTile(imageIndex, tileX, tileY).getRaster();
	}

	public boolean canReadRaster() {
		return true;
	}

	public int getMinIndex() {
		return 0;
	}

	public int getNumThumbnails(final int imageIndex) throws IOException {
		return 0;
	}

	public boolean hasThumbnails(final int imageIndex) throws IOException {
		return false;
	}

	/**
	 * Returns <code>true</code> if the current input source has been marked
	 * as allowing metadata to be ignored by passing <code>true</code> as the
	 * <code>ignoreMetadata</code> argument to the
	 * {@link AsciiGridsImageReader#setInput} method.
	 * 
	 * @return <code>true</code> if the metadata may be ignored.
	 */
	public boolean isIgnoringMetadata() {
		return ignoreMetadata;
	}

	/**
	 * A simple method which returns the proper AsciiGridRaster used to perform
	 * reading operations
	 * 
	 * @return Returns the rasterReader.
	 */
	public AsciiGridRaster getRasterReader() {
		return rasterReader;
	}

	/**
	 * A simple method which returns the imageInputStream used to perform
	 * reading operations
	 * 
	 * @return Returns the imageInputStream.
	 */
	public ImageInputStream getCurrentImageInputStream() {
		return imageInputStream;
	}

	/**
	 * Cleans this {@link AsciiGridsImageReader} up.
	 */
	public void dispose() {
		if (imageInputStream != null)
			try {
				imageInputStream.close();
			} catch (IOException ioe) {

			}

		imageInputStream = null;
		super.dispose();
	}

	/**
	 * Resets this {@link AsciiGridsImageReader}.
	 * 
	 * @see javax.imageio.ImageReader#reset()
	 */
	public void reset() {
		dispose();
		super.setInput(null, false, false);
		rasterReader = null;
		tileHeight = tileWidth = -1;
		width = height = -1;
		sm = null;
		isTiled = false;
		imageType = null;
		imageSize = -1;
		GRASS = false;
		metadata = null;

	}

	public synchronized void abort() {
		super.abort();
	}

	protected synchronized boolean abortRequested() {
		return super.abortRequested();
	}

	protected synchronized void clearAbortRequest() {

		super.clearAbortRequest();
	}

	public void processImageProgress(float percentageDone) {
		// TODO Auto-generated method stub
		super.processImageProgress(percentageDone);
	}
}
