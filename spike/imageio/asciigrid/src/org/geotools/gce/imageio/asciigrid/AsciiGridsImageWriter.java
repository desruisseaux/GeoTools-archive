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

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.geotools.gce.imageio.asciigrid.raster.AsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.EsriAsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.GrassAsciiGridRaster;
import org.w3c.dom.Node;

/**
 * class used for writing ASCII ArcGrid Format and ASCII GRASS Grid Format
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public final  class AsciiGridsImageWriter extends ImageWriter {
	private static final Logger logger = Logger
			.getLogger(AsciiGridsImageWriter.class.toString());

	static final String newline = System.getProperty("line.separator");

	private static final double EPS = 1E-6;

	private AsciiGridsImageMetadata imageMetadata = null;

	private ImageOutputStream imageOutputStream;

	private AsciiGridRaster rasterWriter = null;

	/** The input source RenderedImage */
	private PlanarImage inputRenderedImage;

	private boolean GRASS = false;

	private int nColumns;

	private int nRows;

	private double cellsizeX;

	private double cellsizeY;

	private String rasterSpaceTypeString;

	private String noDataValueString;

	private double xll;

	private double yll;

	public ImageWriteParam getDefaultWriteParam() {
		return new AsciiGridsImageWriteParam(getLocale());
	}

	public AsciiGridsImageWriter(ImageWriterSpi originatingProvider) {
		super(originatingProvider);
	}

	/**
	 * This method set the Output where to write to.
	 */
	public void setOutput(Object output) {
		super.setOutput(output); // validates output
		if (output != null) {
			// if (!(output instanceof FileImageOutputStreamExtImpl)
			// && !(output instanceof GZIPImageOutputStream)
			// && !(output instanceof GZIPFilterImageOutputStreamExt)) {
			if (!(output instanceof ImageOutputStream)) {
				throw new IllegalArgumentException(
						"Not a valid type of Output ");
			}
			imageOutputStream = (ImageOutputStreamImpl) output;

		} else {
			imageOutputStream = null;
			throw new IllegalArgumentException("Not a valid type of Output ");
		}
		if (LoggerController.enableLoggerWriter) {
			logger.info("Setting Output");
		}
	}

	/**
	 * @see javax.imageio.ImageWriter#getDefaultStreamMetadata(javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata getDefaultStreamMetadata(ImageWriteParam arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method provides the writing of the image. This operations include
	 * the loading of the metadata, the writing of the header and the writing of
	 * data values.
	 * 
	 * @see javax.imageio.ImageWriter#write(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.IIOImage, javax.imageio.ImageWriteParam)
	 */
	public void write(IIOMetadata streamMetadata, IIOImage image,
			ImageWriteParam param) throws IOException {

		// Getting the source
		inputRenderedImage = PlanarImage.wrapRenderedImage(image
				.getRenderedImage());
		// Getting metadata to write the file header.
		imageMetadata = (AsciiGridsImageMetadata) image.getMetadata();
		// TODO: METADATA MANAGEMENT IF NO METADATA PROVIDED

		final Node root = imageMetadata
				.getAsTree(AsciiGridsImageMetadata.nativeMetadataFormatName);

		// check to have square cell Size
		// CON IL GRASS NON SI DEVE FARE!!!
		checkMetadata(root);

		// Checking if the compression is needed.
//		final int compression;
//		if (param == null) {
//			param = getDefaultWriteParam();
//			compression = ImageWriteParam.MODE_DISABLED;
//		} else {
//			compression = param.getCompressionMode();
//			// if compression is needed (ImageWriteParam.MODE_DEFAULT
//			// I need to use a GZIPImageOutputStreamExt
//			if (compression == ImageWriteParam.MODE_DEFAULT) {
//				throw new UnsupportedOperationException(
//						"Compression is not supported for the moment by this plugin!");
//
//				// imageOutputStream = new
//				// GZIPImageOutputStream(imageOutputStream);
//
//			}
//		}

		// Writing out the Header
		writeHeader(root);

		// writing the raster
		writeRaster();

	}

	private void checkMetadata(Node root) throws IOException {

		// //
		//
		// Grass
		//
		// //
		final Node formatDescriptorNode = root.getFirstChild();
		GRASS = (Boolean.valueOf(formatDescriptorNode.getAttributes()
				.getNamedItem("GRASS").getNodeValue())).booleanValue();

		// //
		//
		// Grid description
		//
		// //
		final Node gridDescriptorNode = formatDescriptorNode.getNextSibling();
		nColumns = Integer.parseInt(gridDescriptorNode.getAttributes()
				.getNamedItem("nColumns").getNodeValue());
		nRows = Integer.parseInt(gridDescriptorNode.getAttributes()
				.getNamedItem("nRows").getNodeValue());
		rasterSpaceTypeString = gridDescriptorNode.getAttributes()
				.getNamedItem("rasterSpaceType").getNodeValue();
		noDataValueString = null;// remember the no data value can be
		// optional
		if (!GRASS) {
			Node dummyNode = gridDescriptorNode.getAttributes().getNamedItem(
					"noDataValue");

			if (dummyNode != null) {
				noDataValueString = dummyNode.getNodeValue();
			}
		}

		// //
		//
		// Spatial dimensions
		//
		// //
		final Node envelopDescriptorNode = gridDescriptorNode.getNextSibling();
		cellsizeX = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("cellsizeX").getNodeValue());
		cellsizeY = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("cellsizeY").getNodeValue());
		xll = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("xll").getNodeValue());
		yll = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("yll").getNodeValue());

		// //
		//
		// Checking if the dimensions of the current image are different from
		// the original dimensions as provided by the metadata. This might
		// happen if we scale on reading or if we make a mistake when providing
		// the metadata.
		//
		// As an alternative for ImageReadOp images with source subsampling we
		// could look for the image read params.
		//
		// //
		final int actualWidth = this.inputRenderedImage.getWidth();
		final int actualHeight = this.inputRenderedImage.getHeight();
		cellsizeX *= nColumns / actualWidth;
		cellsizeY *= nRows / actualHeight;
		if (!GRASS)
			if (Math.abs(cellsizeX - cellsizeY) > EPS)
				throw new IOException(
						"The provided metadata are illegal!CellSizeX!=CellSizeY.");

	}

	/**
	 * @throws IOException
	 */
	private void writeRaster() throws IOException {
		// we need to cobble rasters of the same row together in order to
		// respect the way our writer works.

		final RectIter iterator = RectIterFactory.create(inputRenderedImage,
				null);
		// writing
		final Double noDataDouble = new Double(rasterWriter.getNoData());
		final String noDataMarker = rasterWriter.getNoDataMarker();
		rasterWriter.writeRaster(iterator, noDataDouble, noDataMarker);

	}

	/**
	 * This Method provides the loading of values needed to write the header by
	 * requesting these values from metadata. Then it calls the proper Header
	 * Writer (ArcGrid or GRASS)
	 * 
	 * @param root
	 * 
	 * @throws IOException
	 */
	private void writeHeader(Node root) throws IOException {
		if (GRASS) {
			rasterWriter = new GrassAsciiGridRaster(imageOutputStream);
		} else {
			rasterWriter = new EsriAsciiGridRaster(imageOutputStream);
		}

		rasterWriter.writeHeader(Integer.toString(nColumns), Integer
				.toString(nRows), Double.toString(xll), Double.toString(yll),
				Double.toString(cellsizeX), Double.toString(cellsizeY),
				rasterSpaceTypeString, noDataValueString);
	}

	/**
	 * @see javax.imageio.ImageWriter#getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier,
	 *      javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier its,
			ImageWriteParam param) {
		return null;
	}

	/**
	 * @see javax.imageio.ImageWriter#convertStreamMetadata(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata convertStreamMetadata(IIOMetadata md,
			ImageWriteParam param) {
		// TODO Check THIS FUNCTION
		return null;
	}

	/**
	 * @see javax.imageio.ImageWriter#convertImageMetadata(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata convertImageMetadata(IIOMetadata md,
			ImageTypeSpecifier its, ImageWriteParam param) {
		// TODO Check THIS FUNCTION
		return md;
	}

	final class AsciiGridsImageWriteParam extends ImageWriteParam {

		AsciiGridsImageWriteParam(Locale locale) {
			super(locale);
			compressionMode = MODE_DISABLED;
			canWriteCompressed = true;
		}

		public void setCompressionMode(int mode) {
			if (mode == MODE_EXPLICIT || mode == MODE_COPY_FROM_METADATA) {
				throw new UnsupportedOperationException(
						"mode == MODE_EXPLICIT || mode == MODE_COPY_FROM_METADATA");
			}

			super.setCompressionMode(mode); // This sets the instance variable.
		}

		public void unsetCompression() {
			super.unsetCompression(); // Performs checks.
		}
	}

	public void dispose() {
		if (imageOutputStream != null)
			try {
				imageOutputStream.flush();
				imageOutputStream.close();
			} catch (IOException ioe) {

			}
		imageOutputStream = null;
		super.dispose();
	}

}
