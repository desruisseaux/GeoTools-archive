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

import java.awt.geom.AffineTransform;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverageWriter;
import org.geotools.factory.Hints;
import org.geotools.image.ImageWorker;
import org.geotools.parameter.Parameter;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * Writes a GridCoverage to a raster image file and an accompanying world file.
 * The destination specified must point to the location of the raster file to
 * write to, as this is how the format is determined. The directory that file is
 * located in must also already exist.
 * 
 * @author Simone Giannecchini
 * @author rgould
 * @author alessio fabiani
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/image/src/org/geotools/gce/image/WorldImageWriter.java $
 */
public final class WorldImageWriter extends AbstractGridCoverageWriter implements GridCoverageWriter {
	/** format for this writer */
	private Format format = new WorldImageFormat();

	/** Destination to write to */
	private Object destination;

	/**
	 * Format chosen for this writer.
	 * 
	 * The default format is png.
	 */
	private String extension = "png";

	/**
	 * Destination must be a File. The directory it resides in must already
	 * exist. It must point to where the raster image is to be located. The
	 * world image will be derived from there.
	 * 
	 * @param destination
	 */
	public WorldImageWriter(Object destination) {
		this(destination,null);
	}
	/**
	 * Destination must be a File. The directory it resides in must already
	 * exist. It must point to where the raster image is to be located. The
	 * world image will be derived from there.
	 * 
	 * @param destination
	 */
	public WorldImageWriter(Object destination,Hints hints) {
		this.destination = destination;

		// convert everything into a file when possible
		// we have to separate the handling of a file from the handling of an
		// output stream due to the fact that the latter requires no world file.
		if (this.destination instanceof String) {
			destination = new File((String) destination);
		} else if (this.destination instanceof URL) {
			final URL url = ((URL) destination);
			if (url.getProtocol().equalsIgnoreCase("file"))
				destination = new File(url.getPath());
			else
				throw new RuntimeException(
						"WorldImageWriter::write:It is not possible writing to an URL!");
		} /*else if (!(destination instanceof ImageOutputStream)
				&& !(destination instanceof File))
			throw new RuntimeException(
					"WorldImageWriter::write:It is not possible writing to an URL!");*/
		// //
		//
		// managing hints
		//
		// //
		if (hints != null) {
			if (super.hints == null)
				this.hints = new Hints(null);
			hints.add(hints);
		}
	}
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
	 */
	public Object getDestination() {
		return destination;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		return null;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMetadataValue(String name, String value) throws IOException,
			MetadataNameNotFoundException {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	public void setCurrentSubname(String name) throws IOException {
	}

	/**
	 * Takes a GridCoverage and writes the image to the destination file. It
	 * then reads the format of the file and writes an accompanying world file.
	 * It will throw a FileFormatNotCompatibleWithGridCoverageException if
	 * Destination is not a File (URL is a read-only format!).
	 * 
	 * @param coverage
	 *            the GridCoverage to write.
	 * @param parameters
	 *            no parameters are accepted. Currently ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.geotools.gc.GridCoverage,
	 *      org.opengis.parameter.GeneralParameterValue[])
	 */
	public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
			throws IllegalArgumentException, IOException {
		final GridCoverage2D gc = (GridCoverage2D) coverage;
		final ImageOutputStream outstream;
		// checking parameters
		// if provided we have to use them
		// specifically this is one of the way we can provide an output format
		if (parameters != null) {
			this.extension = ((Parameter) parameters[0]).stringValue();
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// WorldFile and projection file.
		//
		// ////////////////////////////////////////////////////////////////////
		if (destination instanceof File) {
			// files destinations
			File imageFile = (File) destination;
			final String path = imageFile.getAbsolutePath();
			final int index = path.lastIndexOf(".");
			final String baseFile = index >= 0 ? path.substring(0, index)
					: path;

			// envelope and image
			final RenderedImage image = gc.getRenderedImage();

			// world file
			try {
				createWorldFile(coverage, image, baseFile);
			} catch (TransformException e) {
				final IOException ex = new IOException();
				ex.initCause(e);
				throw ex;
			}

			// projection file
			createProjectionFile(baseFile, coverage
					.getCoordinateReferenceSystem());

		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Encoding of the original coverage
		//
		// ////////////////////////////////////////////////////////////////////
		outstream = (destination instanceof ImageOutputStream) ? (ImageOutputStream) destination
				: ImageIO.createImageOutputStream(destination);
		if (outstream == null)
			throw new IOException(
					"WorldImageWriter::write:No image output stream avalaible for the provided destination");
		this.encode(gc, outstream);

	}

	/**
	 * This method is responsible for creating a projection file using the WKT
	 * representation of this coverage's coordinate reference system. We can
	 * reuse this file in order to rebuild later the crs.
	 * 
	 * 
	 * @param baseFile
	 * @param coordinateReferenceSystem
	 * @throws IOException
	 */
	private void createProjectionFile(final String baseFile,
			final CoordinateReferenceSystem coordinateReferenceSystem)
			throws IOException {
		final File prjFile = new File(new StringBuffer(baseFile).append(".prj")
				.toString());
		BufferedWriter out = new BufferedWriter(new FileWriter(prjFile));
		out.write(coordinateReferenceSystem.toWKT());
		out.close();

	}

	/**
	 * This method is responsible fro creating a world file to georeference an
	 * image given the image bounding box and the image geometry. The name of
	 * the file is composed by the name of the image file with a proper
	 * extension, depending on the format (see WorldImageFormat). The projection
	 * is in the world file.
	 * 
	 * @param gc
	 *            Envelope of this image.
	 * @param image
	 *            Image to be used.
	 * @param baseFile
	 *            Basename and path for this image.
	 * @throws IOException
	 *             In case we cannot create the world file.
	 * @throws TransformException
	 * @throws TransformException
	 */
	private void createWorldFile(GridCoverage gc, final RenderedImage image,
			final String baseFile) throws IOException, TransformException {
		// /////////////////////////////////////////////////////////////////////
		//
		// CRS information
		//
		// ////////////////////////////////////////////////////////////////////
		final CoordinateReferenceSystem crs = CRSUtilities.getCRS2D(gc
				.getCoordinateReferenceSystem());
		final CoordinateSystem cs = crs.getCoordinateSystem();
		final boolean lonFirst = !GridGeometry2D.swapXY(cs);
		final AffineTransform gridToWorld = (AffineTransform) gc
				.getGridGeometry().getGridToCoordinateSystem();

		// /////////////////////////////////////////////////////////////////////
		//
		// World File values
		// It is worthwhile to note that we have to keep into account the fact
		// that the axis could be swapped (LAT,lon) therefore when getting
		// xPixSize and yPixSize we need to look for it a the right place
		// inside the grid to world transform.
		//
		// ////////////////////////////////////////////////////////////////////
		final double xPixelSize = (lonFirst) ? gridToWorld.getScaleX()
				: gridToWorld.getShearY();
		final double rotation1 = (lonFirst) ? gridToWorld.getShearX()
				: gridToWorld.getScaleX();
		final double rotation2 = (lonFirst) ? gridToWorld.getShearY()
				: gridToWorld.getScaleY();
		final double yPixelSize = (lonFirst) ? gridToWorld.getScaleY()
				: gridToWorld.getShearX();
		final double xLoc = gridToWorld.getTranslateX();
		final double yLoc = gridToWorld.getTranslateY();

		// /////////////////////////////////////////////////////////////////////
		//
		// writing world file
		//
		// ////////////////////////////////////////////////////////////////////
		final StringBuffer buff = new StringBuffer(baseFile);
		buff.append(WorldImageFormat.getWorldExtension(format
				.getWriteParameters().parameter("format").stringValue()));
		final File worldFile = new File(buff.toString());
		final PrintWriter out = new PrintWriter(new FileOutputStream(worldFile));
		out.println(xPixelSize);
		out.println(rotation1);
		out.println(rotation2);
		out.println(yPixelSize);
		out.println(xLoc);
		out.println(yLoc);
		out.flush();
		out.close();

	}

	/**
	 * Cleans up the writer. Currently does nothing.
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void dispose() throws IOException {
	}

	/**
	 * Encode the given coverage to the requsted output format.
	 * 
	 * @param sourceCoverage
	 *            the coverage to be encoded.s
	 * @param outstream
	 *            OutputStream
	 * @throws IOException
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	private void encode(final GridCoverage2D sourceCoverage,
			final ImageOutputStream outstream) throws IOException {

		// do we have a source coverage?
		if (sourceCoverage == null) {
			throw new IllegalArgumentException(
					"A coverage must be provided in order for write to succeed!");
		}

		/**
		 * Getting the non geophysics view of this grid coverage. the
		 * geophysiscs view usually comes with an index color model for 3 bands,
		 * since sometimes I get some problems with JAI encoders I select only
		 * the first band, which by the way is the only band we use.
		 */
		RenderedImage image = (sourceCoverage).geophysics(false)
				.getRenderedImage();
		final ImageWorker worker = new ImageWorker(image);

		// /////////////////////////////////////////////////////////////////////
		//
		// With index color model we want just the first band
		//
		// /////////////////////////////////////////////////////////////////////
		if (image.getColorModel() instanceof IndexColorModel
				&& (image.getSampleModel().getNumBands() > 1)) {
			worker.retainBands(1);
			image = worker.getRenderedImage();
		}

		/**
		 * For the moment we do not work with DirectColorModel but instead we
		 * switch to component color model which is really easier to handle even
		 * if it much more memory expensive. Once we are in component color
		 * model is really easy to go to Gif and similar.
		 */
		if (image.getColorModel() instanceof DirectColorModel) {
			worker.forceComponentColorModel();
			image = worker.getRenderedImage();
		}

		/**
		 * ADJUSTMENTS FOR VARIOUS FILE FORMATS
		 */

		// ------------------------GIF-----------------------------------
		if (extension.compareToIgnoreCase("gif") == 0) {

			/**
			 * IndexColorModel with more than 8 bits for sample might be a
			 * problem because GIF allows only 8 bits based palette therefore I
			 * prefere switching to component color model in order to handle
			 * this properly. NOTE. The only transfert types avalaible for
			 * IndexColorModel are byte and ushort.
			 */
			if (image.getColorModel() instanceof IndexColorModel
					&& (image.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE)) {
				worker.forceComponentColorModel();
				image = worker.getRenderedImage();
			}

			/**
			 * component color model is not well digested by the gif encoder we
			 * need to go to indecolor model somehow. This code for the moment
			 * remove transparency, but I am confident I will find a way to add
			 * that.
			 */
			if (image.getColorModel() instanceof ComponentColorModel) {
				worker.forceIndexColorModelForGIF();
				image = worker.getRenderedImage();
			} else
			/**
			 * IndexColorModel with full transparency support is not suitable
			 * for gif images we need to go to bitmask loosing some
			 * informations. we have only one full transparent color.
			 */
			if (image.getColorModel() instanceof IndexColorModel) {
				worker.forceIndexColorModelForGIF();
				image = worker.getRenderedImage();
			}
		}
		// else
		// -----------------TIFF--------------------------------------

		// /**
		// * TIFF file format. We need just a couple of correction for this
		// * format. It seems that the encoder does not work fine with
		// * IndexColorModel therefore in such a case we need the reformat the
		// * input image to a ComponentColorModel.
		// */
		// if (extension.compareToIgnoreCase("tiff") == 0
		// || extension.compareToIgnoreCase("tif") == 0) {
		// // Are we dealing with IndexColorModel? If so we need to go back
		// // to ComponentColorModel
		// if (image.getColorModel() instanceof IndexColorModel) {
		// surrogateImage = ImageUtilities
		// .reformatColorModel2ComponentColorModel(surrogateImage);
		// }
		// }

		/**
		 * write using JAI encoders
		 */
		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		pbjImageWrite.addSource(image);
		pbjImageWrite.setParameter("Output", outstream);
		pbjImageWrite.setParameter("VerifyOutput", Boolean.FALSE);
		pbjImageWrite.setParameter("Format", extension);
		JAI.create("ImageWrite", pbjImageWrite);
		outstream.flush();
		outstream.close();

	}
}
