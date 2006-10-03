/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.gtopo30;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.coverage.grid.AbstractGridCoverageWriter;
import org.geotools.factory.Hints;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.image.CoverageUtilities;
import org.geotools.util.NumberRange;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * Class useful for writing GTopo30 file format from a GridCoverage2D.
 * 
 * @author jeichar
 * @author Simone Giannecchini
 * @author mkraemer
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/gtopo30/src/org/geotools/gce/gtopo30/GTopo30Writer.java $
 */
final public class GTopo30Writer extends AbstractGridCoverageWriter implements GridCoverageWriter {

	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.gtopo30");

	static {
		// register new JAI operation
		NoDataReplacerOpImage.register(JAI.getDefaultInstance());
	}

	/** Standard width for the GIF image. */
	private final static int GIF_WIDTH = 640;

	/** Standard height for the GIF image. */
	private final static int GIF_HEIGHT = 480;

	/**
	 * The destination (can be a File (a directory actually), an URL to a
	 * directory, a ZipOutputStream or a String representing a directory or an
	 * URL to a directory.)
	 */
	private Object destination;

	/**
	 * Creates a GTopo30Writer.
	 * 
	 * @param dest
	 *            The destination object can be a File (a directory actually),
	 *            an URL to a directory, a ZipOutputStream or a String
	 *            representing a directory or an URL to a directory.
	 */
	public GTopo30Writer(final Object dest) {
		this(dest,null);
	}/**
	 * Creates a GTopo30Writer.
	 * 
	 * @param dest
	 *            The destination object can be a File (a directory actually),
	 *            an URL to a directory, a ZipOutputStream or a String
	 *            representing a directory or an URL to a directory.
	 */
	public GTopo30Writer(final Object dest,final Hints hints) {
		destination = dest;

		if (dest == null) {
			return;
		}

		final File temp;
		final URL url;

		try {
			// we only accept a directory as a path
			if (dest instanceof String) {
				temp = new File((String) dest);

				// if it exists and it is not a directory that 's not good
				if ((temp.exists() && !temp.isDirectory()) || !temp.exists()) {
					destination = null; // we cannot write
				} else if (!temp.exists()) {
					// well let's create it!
					if (!temp.mkdir()) {
						destination = null;
					} else {
						destination = temp.getAbsolutePath();
					}
				}
			} else if (dest instanceof File) {
				temp = (File) dest;

				if (temp.exists() && !temp.isDirectory()) {
					this.destination = null;
				} else if (!temp.exists()) {
					// let's create it
					if (temp.mkdir()) {
						destination = temp.getAbsolutePath();
					} else {
						destination = null;
					}
				}
			} else if (dest instanceof URL) {
				url = (URL) dest;

				if (url.getProtocol().compareToIgnoreCase("file") != 0) {
					destination = null;
				}

				temp = new File(url.getFile());

				if (temp.exists() && !temp.isDirectory()) {
					destination = null;
				} else if (!temp.exists()) {
					// let's create it
					if (temp.mkdir()) {
						destination = temp.getAbsolutePath();
					} else {
						destination = null;
					}
				}
			} else if (dest instanceof ZipOutputStream) {
				this.destination = (ZipOutputStream) dest;
				((ZipOutputStream) destination)
						.setMethod(ZipOutputStream.DEFLATED);
				((ZipOutputStream) destination)
						.setLevel(Deflater.BEST_COMPRESSION);
			}
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			destination = null;
		}
		
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
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
	 */
	public Format getFormat() {
		return null;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
	 */
	public Object getDestination() {
		return null;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		return null;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMetadataValue(final String name, final String value)
			throws MetadataNameNotFoundException {
		if ((name != null) && (value != null)) {
			// unreferenced parameter: name
			// unreferenced parameter: value
		}
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	public void setCurrentSubname(final String name) {
		if (name != null) {
			// unreferenced parameter: name
		}
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage,
	 *      org.opengis.parameter.GeneralParameterValue[])
	 */
	public void write(final GridCoverage coverage,
			final GeneralParameterValue[] parameters)
			throws java.lang.IllegalArgumentException, java.io.IOException {
		final GridCoverage2D gc2D = (GridCoverage2D) coverage;

		if (parameters != null) {
			// unreferenced parameter: parameters
		}

		// destination file name
		String fileName = gc2D.getName().toString();

		// destination
		Object dest = this.destination;

		/**
		 * TODO At this place a format operation on the GridCoverage2D should be
		 * performed since the internal rendered image should of data type
		 * short. Moreover we should substitute NaN values with -9999.
		 */
		final PlanarImage reFormattedData2Short = reFormatCoverageImage(gc2D,
				DataBuffer.TYPE_SHORT);

		// write DEM
		if (this.destination instanceof File) {
			fileName = ((File) this.destination).getAbsolutePath() + "/"
					+ fileName;
			dest = new File(fileName + ".DEM");
		}

		this.writeDEM(reFormattedData2Short, fileName, dest);
		// write statistics
		if (this.destination instanceof File) {
			dest = new File(fileName + ".STX");
		}

		this.writeStats(reFormattedData2Short, dest, gc2D);
		// we won't use this image anymore let's release the resources.

		// write world file
		if (this.destination instanceof File) {
			dest = new File(fileName + ".DMW");
		}

		this.writeWorldFile(gc2D, dest);

		// write projection
		if (this.destination instanceof File) {
			dest = new File(fileName + ".PRJ");
		}

		this.writePRJ(gc2D, dest);

		// write HDR
		if (this.destination instanceof File) {
			dest = new File(fileName + ".HDR");
		}

		this.writeHDR(gc2D, dest);

		// write gif
		if (this.destination instanceof File) {
			dest = new File(fileName + ".GIF");
		}

		this.writeGIF(gc2D, dest);

		// write src
		if (this.destination instanceof File) {
			dest = new File(fileName + ".SRC");
		}

		this.writeSRC(gc2D, dest);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param gc2D
	 * @param dataType
	 *            DOCUMENT ME!
	 * 
	 * @return
	 */
	private PlanarImage reFormatCoverageImage(final GridCoverage2D gc2D,
			final int dataType) {
		// internal image
		PlanarImage image = (PlanarImage) gc2D.getRenderedImage();

		// sample dimension type
		final int origDataType = image.getSampleModel().getDataType();

		// short?
		if (dataType == origDataType) {
			return image;
		}

		//
		final int visibleBand = CoverageUtilities.getVisibleBand(gc2D);
		final GridSampleDimension visibleSD = ((GridSampleDimension) gc2D
				.getSampleDimension(visibleBand)).geophysics(true);

		// getting categories
		final List oldCategories = visibleSD.getCategories();

		// removing old nodata category
		// candidate
		Category candidate = null;
		NumberRange candidateRange = null;
		final Iterator it = oldCategories.iterator();

		while (it.hasNext()) {
			candidate = (Category) it.next();

			// removing candidate for NaN
			if (candidate.getName().toString().equalsIgnoreCase("no data")) {
				candidateRange = candidate.getRange();

				break;
			}
		}

		// new no data category
		final double oldNoData = candidateRange.getMinimum();

		final ParameterBlockJAI pbjM = new ParameterBlockJAI(
				"org.geotools.gce.gtopo30.NoDataReplacer");
		pbjM.addSource(image);
		pbjM.setParameter("oldNoData", oldNoData);
		image = JAI.create("org.geotools.gce.gtopo30.NoDataReplacer", pbjM,
				null);

		// //format
		// final ParameterBlockJAI pbjF= new ParameterBlockJAI("Format");
		// pbjF.addSource(image);
		// pbjF.setParameter("dataType",DataBuffer.TYPE_SHORT);
		//		
		// image=JAI.create("Format",pbjF,new
		// RenderingHints(JAI.KEY_IMAGE_LAYOUT,new ImageLayout(image)));
		return image;
	}

	/**
	 * Writing down the header file for the gtopo30 format:
	 * 
	 * @param coverage
	 *            The GridCoverage to write
	 * @param file
	 *            The destination object (can be a File or ZipOutputStream)
	 * 
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writeHDR(final GridCoverage2D gc, final Object file)
			throws IOException {

		// final GeneralEnvelope envelope = (GeneralEnvelope) gc.getEnvelope();
		final double noData = -9999.0;

		try {
			// checking the directions of the axes.
			// we need to understand how the axes of this gridcoverage are
			// specified
			final CoordinateReferenceSystem crs = CRSUtilities.getCRS2D(gc
					.getCoordinateReferenceSystem());
			boolean lonFirst = !GridGeometry2D
					.swapXY(crs.getCoordinateSystem());

			final AffineTransform gridToWorld = (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem();

			final double geospatialDx = Math.abs((lonFirst) ? gridToWorld
					.getScaleX() : gridToWorld.getShearY());
			final double geospatialDy = Math.abs((lonFirst) ? gridToWorld
					.getScaleY() : gridToWorld.getShearX());

			// getting corner coordinates of the left upper corner
			final double xUpperLeft = lonFirst ? gridToWorld.getTranslateX()
					: gridToWorld.getTranslateY();
			final double yUpperLeft = lonFirst ? gridToWorld.getTranslateY()
					: gridToWorld.getTranslateX();

			// calculating the physical resolution over x and y.
			final int geometryWidth = gc.getGridGeometry().getGridRange()
					.getLength(0);
			final int geometryHeight = gc.getGridGeometry().getGridRange()
					.getLength(1);

			if (file instanceof File) {
				final PrintWriter out = new PrintWriter(new FileOutputStream(
						(File) file));

				// output header and assign header fields
				out.print("BYTEORDER");
				out.print(" ");
				out.println("M");

				out.print("LAYOUT");
				out.print(" ");
				out.println("BIL");

				out.print("NROWS");
				out.print(" ");
				out.println(geometryHeight);

				out.print("NCOLS");
				out.print(" ");
				out.println(geometryWidth);

				out.print("NBANDS");
				out.print(" ");
				out.println("1");

				out.print("NBITS");
				out.print(" ");
				out.println("16");

				out.print("BANDROWBYTES");
				out.print(" ");
				out.println(geometryWidth * 2);

				out.print("TOTALROWBYTES");
				out.print(" ");
				out.println(geometryWidth * 2);

				out.print("BANDGAPBYTES");
				out.print(" ");
				out.println(0);

				out.print("NODATA");
				out.print(" ");
				out.println((int) noData);

				out.print("ULXMAP");
				out.print(" ");
				out.println(xUpperLeft);

				out.print("ULYMAP");
				out.print(" ");
				out.println(yUpperLeft);

				out.print("XDIM");
				out.print(" ");
				out.println(geospatialDx);

				out.print("YDIM");
				out.print(" ");
				out.println(geospatialDy);
				out.flush();
				out.close();
			} else {
				final ZipOutputStream outZ = (ZipOutputStream) file;
				final ZipEntry e = new ZipEntry(gc.getName().toString()
						+ ".HDR");
				outZ.putNextEntry(e);

				// writing world file
				outZ.write("BYTEORDER".getBytes());
				outZ.write(" ".getBytes());
				outZ.write("M".getBytes());
				outZ.write("\n".getBytes());

				outZ.write("LAYoutZ".getBytes());
				outZ.write(" ".getBytes());
				outZ.write("BIL".getBytes());
				outZ.write("\n".getBytes());

				outZ.write("NROWS".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Integer.toString(geometryHeight).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("NCOLS".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Integer.toString(geometryWidth).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("NBANDS".getBytes());
				outZ.write(" ".getBytes());
				outZ.write("1".getBytes());
				outZ.write("\n".getBytes());

				outZ.write("NBITS".getBytes());
				outZ.write(" ".getBytes());
				outZ.write("16".getBytes());
				outZ.write("\n".getBytes());

				outZ.write("BANDROWBYTES".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Integer.toString(geometryWidth * 2).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("TOTALROWBYTES".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Integer.toString(geometryWidth * 2).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("BANDGAPBYTES".getBytes());
				outZ.write(" ".getBytes());
				outZ.write("0".getBytes());
				outZ.write("\n".getBytes());

				outZ.write("NODATA".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Integer.toString((int) noData).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("ULXMAP".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Double.toString(xUpperLeft + (geospatialDx / 2))
						.getBytes());
				outZ.write("\n".getBytes());

				outZ.write("ULYMAP".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Double.toString(yUpperLeft - (geospatialDy / 2))
						.getBytes());
				outZ.write("\n".getBytes());

				outZ.write("XDIM".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Double.toString(geospatialDx).getBytes());
				outZ.write("\n".getBytes());

				outZ.write("YDIM".getBytes());
				outZ.write(" ".getBytes());
				outZ.write(Double.toString(geospatialDy).toString().getBytes());
				outZ.write("\n".getBytes());

				outZ.closeEntry();

				((ZipOutputStream) file).closeEntry();
			}
		} catch (TransformException e) {
			final IOException ioe = new IOException(
					"Unable to write world file");
			ioe.initCause(e);
			throw ioe;
		}
	}

	/**
	 * Writes the source file (.SRC). The default byte order is BIG_ENDIAN.
	 * 
	 * @param gc
	 *            The GridCoverage to write
	 * @param file
	 *            The destination object (can be a File or ZipOutputStream)
	 * 
	 * @throws FileNotFoundException
	 *             If the destination file could not be found
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writeSRC(GridCoverage2D gc, final Object file)
			throws FileNotFoundException, IOException {

		// /////////////////////////////////////////////////////////////////////
		// TODO @task @todo
		// Here I am making the assumption that the non geophysiscs view is 8
		// bit but it can also be 16. I should do something more general like a
		// clamp plus a format but for the moment this is enough.
		//
		// We need also to get the one visible band
		//
		// /////////////////////////////////////////////////////////////////////
		gc = gc.geophysics(false);
		ImageOutputStreamImpl out = null;

		if (file instanceof File) {
			out = new FileImageOutputStream((File) file);
		} else {
			final ZipOutputStream outZ = (ZipOutputStream) file;
			final ZipEntry e = new ZipEntry(gc.getName().toString() + ".SRC");
			outZ.putNextEntry(e);

			out = new FileCacheImageOutputStream(outZ, null);
		}

		// setting byte order
		out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);

		// /////////////////////////////////////////////////////////////////////
		//
		// Prepare to write
		//
		// /////////////////////////////////////////////////////////////////////
		RenderedImage image = gc.getRenderedImage();
		image = untileImage(image);

		final ParameterBlockJAI pbj = new ParameterBlockJAI("imagewrite");
		pbj.addSource(image);
		pbj.setParameter("Format", "raw");
		pbj.setParameter("Output", out);
		final RenderedOp wOp = JAI.create("ImageWrite", pbj);

		// /////////////////////////////////////////////////////////////////////
		//
		// Dispose things
		//
		// /////////////////////////////////////////////////////////////////////
		final Object o = wOp
				.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
		if (o instanceof ImageWriter)
			((ImageWriter) o).dispose();

		if (!(file instanceof File)) {
			((ZipOutputStream) file).closeEntry();
		}
		out.flush();
		out.close();

	}

	/**
	 * Writing a gif file as an overview for this GTopo30.
	 * 
	 * @param gc
	 *            The GridCoverage to write
	 * @param file
	 *            The destination object (can be a File or ZipOutputStream)
	 * 
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writeGIF(final GridCoverage2D gc, final Object file)
			throws IOException {
		ImageOutputStreamImpl out = null;

		if (file instanceof File) {
			// writing gif image
			out = new FileImageOutputStream((File) file);
		} else {
			final ZipOutputStream outZ = (ZipOutputStream) file;
			final ZipEntry e = new ZipEntry(gc.getName().toString() + ".GIF");
			outZ.putNextEntry(e);

			out = new FileCacheImageOutputStream(outZ, null);
		}

		// rescaling to a smaller resolution in order to save space on storage
		final GridCoverage2D gc1 = rescaleCoverage(gc);

		// get the non-geophysiscs view
		final GridCoverage2D gc2 = gc1.geophysics(false);

		// get the underlying image
		final RenderedImage image = gc2.getRenderedImage();

		// write it down as a gif
		final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageWrite");
		pbj.addSource(image);
		pbj.setParameter("Output", out);
		pbj.setParameter("Format", "gif");
		JAI.create("ImageWrite", pbj, new RenderingHints(JAI.KEY_TILE_CACHE,
				null));

		if (file instanceof File) {
			out.close();
		} else {
			((ZipOutputStream) file).closeEntry();
		}

		// disposing the old unused coverages
		gc1.dispose();
	}

	/**
	 * Purpose of this method is rescaling the original coverage in order to
	 * create an overview for the shaded relief gif image to be put inside the
	 * gtopo30 set of files.
	 * 
	 * @param gc
	 *            Supplied coverage.
	 * 
	 * @return rescaled coverage.
	 */
	private GridCoverage2D rescaleCoverage(GridCoverage2D gc) {
		final RenderedImage image = gc.getRenderedImage();
		final int width = image.getWidth();
		final int height = image.getHeight();

		if ((height < GIF_HEIGHT) && (width < GIF_WIDTH)) {
			return gc;
		}

		// new grid range
		final GeneralGridRange newGridrange = new GeneralGridRange(new int[] {
				0, 0 }, new int[] { GIF_WIDTH, GIF_HEIGHT });

		final GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
				gc.getEnvelope());

		// resample this coverage
		return (GridCoverage2D) Operations.DEFAULT.resample(gc, gc
				.getCoordinateReferenceSystem(), newGridGeometry, Interpolation
				.getInstance(Interpolation.INTERP_NEAREST));
	}

	/**
	 * Write a projection file (.PRJ) using wkt
	 * 
	 * @param gc
	 *            The GridCoverage to write
	 * @param file
	 *            The destination object (can be a File or ZipOutputStream)
	 * 
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writePRJ(final GridCoverage2D gc, Object file)
			throws IOException {
		if (file instanceof File) {
			// create the file
			final BufferedWriter fileWriter = new BufferedWriter(
					new FileWriter((File) file));

			// write information on crs
			fileWriter.write(gc.getCoordinateReferenceSystem().toWKT());
			fileWriter.close();
		} else {
			final ZipOutputStream out = (ZipOutputStream) file;
			final ZipEntry e = new ZipEntry(new StringBuffer(gc.getName()
					.toString()).append(".PRJ").toString());
			out.putNextEntry(e);
			out.write(gc.getCoordinateReferenceSystem().toWKT().getBytes());
			out.closeEntry();
		}
	}

	/**
	 * Writes the stats file (.STX).
	 * 
	 * @param image
	 *            The GridCoverage to write
	 * @param file
	 *            The destination object (can be a File or ZipOutputStream)
	 * @param gc
	 *            DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writeStats(final PlanarImage image, final Object file,
			final GridCoverage2D gc) throws IOException {
		ParameterBlock pb = new ParameterBlock();

		// /////////////////////////////////////////////////////////////////////
		//
		// we need to evaluate stats first using jai
		//
		// /////////////////////////////////////////////////////////////////////
		final int visibleBand = CoverageUtilities.getVisibleBand(image);
		final GridSampleDimension sd = (GridSampleDimension) gc
				.getSampleDimension(visibleBand);
		final double[] Max = new double[] { sd.getMaximumValue() };
		final double[] Min = new double[] { -9999.0 };

		// histogram
		pb.addSource(image);
		pb.add(null); // no roi
		pb.add(1);
		pb.add(1);
		pb.add(new int[] { (int) (Max[0] - Min[0] + 1) });
		pb.add(Min);
		pb.add(Max);
		pb.add(1);

		// /////////////////////////////////////////////////////////////////////
		//
		// Create the histogram
		//
		// /////////////////////////////////////////////////////////////////////
		final PlanarImage histogramImage = JAI.create("histogram", pb,
				new RenderingHints(JAI.KEY_TILE_CACHE, null));
		final Histogram hist = (Histogram) histogramImage
				.getProperty("histogram");
		pb.removeParameters();
		pb.removeSources();

		// /////////////////////////////////////////////////////////////////////
		//
		// Write things our
		//
		// /////////////////////////////////////////////////////////////////////
		if (file instanceof File) {
			// files destinations
			if (!((File) file).exists()) {
				((File) file).createNewFile();
			}

			// writing world file
			final PrintWriter out = new PrintWriter(new FileOutputStream(
					((File) file)));
			out.print(1);
			out.print(" ");
			out.print((int) Min[0]);
			out.print(" ");
			out.print((int) Max[0]);
			out.print(" ");
			out.print(hist.getMean()[0]);
			out.print(" ");
			out.print(hist.getStandardDeviation()[0]);
			out.close();
		} else {
			final ZipOutputStream outZ = (ZipOutputStream) file;
			final ZipEntry e = new ZipEntry(gc.getName().toString() + ".STX");
			outZ.putNextEntry(e);

			// writing world file
			outZ.write("1".getBytes());
			outZ.write(" ".getBytes());
			outZ.write(new Integer((int) Min[0]).toString().getBytes());
			outZ.write(" ".getBytes());
			outZ.write(new Integer((int) Max[0]).toString().getBytes());
			outZ.write(" ".getBytes());
			outZ.write(new Double(hist.getMean()[0]).toString().getBytes());
			outZ.write(" ".getBytes());
			outZ.write(new Double(hist.getStandardDeviation()[0]).toString()
					.getBytes());
			((ZipOutputStream) file).closeEntry();
		}

		histogramImage.dispose();
	}

	/**
	 * Writes the world file (.DMW)
	 * 
	 * @param gc
	 *            The GridCoverage to write
	 * @param worldFile
	 *            The destination world file (can be a file or a
	 *            ZipOutputStream)
	 * 
	 * @throws IOException
	 *             if the file could not be written
	 */
	private void writeWorldFile(final GridCoverage2D gc, Object worldFile)
			throws IOException {
		// final RenderedImage image = (PlanarImage) gc.getRenderedImage();

		/**
		 * It is worth to point out that here I build the values using the axes
		 * order specified in the CRs which can be either LAT,LON or LON,LAT.
		 * This is important since we ned to know how to assocaite the
		 * underlying raster dimensions with the envelope which is in CRS
		 * values.
		 */

		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// trying to understand the direction of the first axis in order to
			// understand how to associate the value to the crs.
			//
			// /////////////////////////////////////////////////////////////////////
			final CoordinateReferenceSystem crs = CRSUtilities.getCRS2D(gc
					.getCoordinateReferenceSystem());
			boolean lonFirst = !GridGeometry2D
					.swapXY(crs.getCoordinateSystem());
			final AffineTransform gridToWorld = (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem();

			// /////////////////////////////////////////////////////////////////////
			//
			// associate value to crs
			//
			// /////////////////////////////////////////////////////////////////////
			final double xPixelSize = (lonFirst) ? gridToWorld.getScaleX()
					: gridToWorld.getShearY();
			final double rotation1 = (lonFirst) ? gridToWorld.getShearX()
					: gridToWorld.getScaleY();
			final double rotation2 = (lonFirst) ? gridToWorld.getShearY()
					: gridToWorld.getScaleX();
			final double yPixelSize = (lonFirst) ? gridToWorld.getScaleY()
					: gridToWorld.getShearX();
			final double xLoc = lonFirst ? gridToWorld.getTranslateX()
					: gridToWorld.getTranslateY();
			final double yLoc = lonFirst ? gridToWorld.getTranslateY()
					: gridToWorld.getTranslateX();
			if (worldFile instanceof File) {
				// files destinations
				if (!((File) worldFile).exists()) {
					((File) worldFile).createNewFile();
				}

				// writing world file
				final PrintWriter out = new PrintWriter(new FileOutputStream(
						(File) worldFile));
				out.println(xPixelSize);
				out.println(rotation1);
				out.println(rotation2);
				out.println(yPixelSize);
				out.println(xLoc);
				out.println(yLoc);
				out.close();
			} else {
				final ZipOutputStream outZ = (ZipOutputStream) worldFile;
				final ZipEntry e = new ZipEntry(gc.getName().toString()
						+ ".DMW");
				outZ.putNextEntry(e);

				// writing world file
				outZ.write(Double.toString(xPixelSize).getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(rotation1).toString().getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(rotation2).toString().getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(xPixelSize).toString().getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(yPixelSize).toString().getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(xLoc).toString().getBytes());
				outZ.write("\n".getBytes());
				outZ.write(Double.toString(yLoc).toString().getBytes());
				outZ.write("\n".getBytes());
				((ZipOutputStream) worldFile).closeEntry();
			}
		} catch (TransformException e) {
			final IOException ioe = new IOException(
					"Unable to write world file");
			ioe.initCause(e);
			throw ioe;
		}
	}

	/**
	 * Writes the digital elevation model file (.DEM). The default byte order is
	 * BIG_ENDIAN.
	 * 
	 * @param image
	 *            The GridCoverage object to write
	 * @param name
	 *            DOCUMENT ME!
	 * @param dest
	 *            The destination object (can be a File or a ZipOutputStream)
	 * 
	 * @throws FileNotFoundException
	 *             If the destination file could not be found
	 * @throws IOException
	 *             If the file could not be written
	 */
	private void writeDEM(PlanarImage image, final String name,
			final Object dest) throws FileNotFoundException, IOException {
		ImageOutputStreamImpl out;

		if (dest instanceof File) {
			out = new FileImageOutputStream((File) dest);
		} else {
			final ZipOutputStream outZ = (ZipOutputStream) dest;
			final ZipEntry e = new ZipEntry(name + ".DEM");
			outZ.putNextEntry(e);
			out = new FileCacheImageOutputStream(outZ, null);
		}

		out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);

		// untile the image in case it is tiled
		// otherwise we could add tiles which are unexistant in the original
		// data
		// generating failures when reading back the data again.
		image = untileImage(image);

		// requesting an imageio writer
		// setting tile parameters in order to tile the image on the disk
		final ParameterBlockJAI pbjW = new ParameterBlockJAI("ImageWrite");
		pbjW.addSource(image);
		pbjW.setParameter("Format", "raw");
		pbjW.setParameter("Output", out);
		JAI.create("ImageWrite", pbjW);

		if (dest instanceof File) {
			out.flush();
			out.close();
		} else {
			((ZipOutputStream) dest).closeEntry();
		}
	}

	/**
	 * This method has the objective of untiling the final image to write on
	 * disk since we do not want to have tiles added to the file on disk causing
	 * failures when reading it back into memory.
	 * 
	 * @param image
	 *            Image to untile.
	 * 
	 * @return Untiled image.
	 */
	private PlanarImage untileImage(RenderedImage image) {
		final ParameterBlockJAI pbj = new ParameterBlockJAI("format");
		pbj.addSource(image);
		pbj.setParameter("dataType", image.getSampleModel().getTransferType());

		final ImageLayout layout = new ImageLayout(image);
		layout.unsetTileLayout();
		layout.setTileGridXOffset(0);
		layout.setTileGridYOffset(0);
		layout.setTileHeight(image.getHeight());
		layout.setTileWidth(image.getWidth());
		layout.setValid(ImageLayout.TILE_GRID_X_OFFSET_MASK
				| ImageLayout.TILE_GRID_Y_OFFSET_MASK
				| ImageLayout.TILE_HEIGHT_MASK | ImageLayout.TILE_WIDTH_MASK);

		final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
				layout);
		hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null)); // avoid
		// caching
		// this
		// image

		return JAI.create("format", pbj, hints);
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public void dispose() {
	}
}
