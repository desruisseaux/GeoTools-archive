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
package org.geotools.gce.arcgrid;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
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
import org.geotools.data.DataSourceException;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.image.CoverageUtilities;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

/**
 * ArcGridWriter Supports writing of an ArcGrid GridCoverage to a Desination
 * object
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public class ArcGridWriter implements GridCoverageWriter {

	private ImageWriter mWriter;

	/** Small number for comparaisons. */
	private static final double EPS = 1E-6;

	/** the destination object where we will do the writing */
	private Object destination;

	/** Format for this wirter. */
	private Format format = (new ArcGridFormatFactory()).createFormat();

	private boolean disposed = false;

	/**
	 * Takes either a URL or a String that points to an ArcGridCoverage file and
	 * converts it to a URL that can then be written to.
	 * 
	 * @param destination
	 *            the URL or String pointing to the file to load the ArcGrid
	 */
	public ArcGridWriter(Object destination) {
		this.destination = destination;
	}

	/**
	 * Implementation of getMetadataNames. Currently unimplemented because it
	 * has not been specified where to retrieve the metadata
	 * 
	 * @return null
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		return null;
	}

	/**
	 * Creates a Format object describing the Arc Grid Format
	 * 
	 * @return the format of the data source we will write to. (ArcGridFormat in
	 *         this case)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Returns the destination object passed to it by the GridCoverageExchange
	 * 
	 * @return the destination that this writer is configured to write to.
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
	 */
	public Object getDestination() {
		return destination;
	}

	/**
	 * Sets up the object's environment based on the Parameters passed to it by
	 * the client.
	 * 
	 * @throws InvalidParameterNameException
	 * @throws InvalidParameterValueException
	 * @throws IOException
	 */

	private void setEnvironment(GeneralParameterValue[] parameters)
			throws InvalidParameterNameException,
			InvalidParameterValueException, IOException {
		if (parameters == null) {
			format.getWriteParameters().parameter("GRASS").setValue(false);
			format.getWriteParameters().parameter("Compressed").setValue(false);
		} else {
			format.getWriteParameters().parameter("GRASS").setValue(
					((ParameterValue) parameters[0]).booleanValue());
			format.getWriteParameters().parameter("Compressed").setValue(
					((ParameterValue) parameters[1]).booleanValue());
		}
	}

	/**
	 * This method was copied from ArcGridData source
	 * 
	 * @param gc
	 *            the grid coverage that will be written to the destination
	 * 
	 * @throws DataSourceException
	 *             indicates an unexpected exception
	 */
	private void writeGridCoverage(GridCoverage2D gc)
			throws DataSourceException {
		try {
			// /////////////////////////////////////////////////////////////////
			//
			// Getting CRS and envelope information
			//
			// /////////////////////////////////////////////////////////////////
			final CoordinateReferenceSystem crs = gc
					.getCoordinateReferenceSystem();
			final CoordinateSystem cs = CRSUtilities.getCRS2D(crs)
					.getCoordinateSystem();
			final boolean lonFirst = !GridGeometry2D.swapXY(cs);
			final Envelope oldEnv = gc.getEnvelope();
			final boolean GRASS = format.getWriteParameters()
					.parameter("GRASS").booleanValue();

			// /////////////////////////////////////////////////////////////////
			//
			// getting visible band
			//
			// /////////////////////////////////////////////////////////////////
			final int visibleBand = CoverageUtilities.getVisibleBand(gc);
			final int numBands = gc.getSampleDimensions().length;
			if (numBands > 1)
				gc = (GridCoverage2D) Operations.DEFAULT.selectSampleDimension(
						gc, new int[] { visibleBand });

			// /////////////////////////////////////////////////////////////////
			//
			// checking if the coverage needs to be resampled in order to have
			// square pixels for the esri format
			//
			// /////////////////////////////////////////////////////////////////
			if (!GRASS)
				gc = reShapeData(((GridCoverage2D) gc), oldEnv
						.getLength(lonFirst ? 0 : 1), // W
						oldEnv.getLength(lonFirst ? 1 : 0) // H
				);

			// /////////////////////////////////////////////////////////////////
			//
			// Preparing to write header information
			//
			// /////////////////////////////////////////////////////////////////
			// getting the new envelope after the reshaping
			final Envelope newEnv = gc.getEnvelope();

			// trying to prepare the header
			final AffineTransform gridToWorld = (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem();
			final double xl = lonFirst ? newEnv.getLowerCorner().getOrdinate(0)
					: newEnv.getLowerCorner().getOrdinate(1);
			final double yl = (!lonFirst) ? newEnv.getLowerCorner()
					.getOrdinate(0) : newEnv.getLowerCorner().getOrdinate(1);
			final double cellsizeX = Math.abs(lonFirst ? gridToWorld.getScaleX()
					: gridToWorld.getShearY());
			final double cellsizeY = Math.abs(lonFirst ? gridToWorld.getScaleY()
					: gridToWorld.getShearX());

			// /////////////////////////////////////////////////////////////////
			//
			// Preparing source image and metadata
			//
			// /////////////////////////////////////////////////////////////////
			final PlanarImage source = (PlanarImage) ((GridCoverage2D) gc)
					.getRenderedImage();
			final int cols = source.getWidth();
			final int rows = source.getHeight();

			// Preparing main parameters for JAI imageWrite Operation
			final ParameterBlockJAI pbjImageWrite = buildImageWriteParameters(
					cols, rows, cellsizeX,cellsizeY, xl, yl, GRASS, gc);

			// Setting the source for the image write operation
			pbjImageWrite.addSource(source);

			// writing crs info
			writeCRSInfo(crs);

			// /////////////////////////////////////////////////////////////////
			//
			// Creating the imageWrite Operation
			//
			// /////////////////////////////////////////////////////////////////
			final RenderedOp image = JAI.create("ImageWrite", pbjImageWrite);
			mWriter = (AsciiGridsImageWriter) image
					.getProperty("JAI.ImageWriter");
			mWriter.dispose();// TODO: Auto-dispose. Maybe I need to allow a
			// manual dispose call?

		} catch (IOException ioe) {
			throw new DataSourceException("IOError writing", ioe);
		} catch (TransformException e) {
			throw new DataSourceException("IOError writing", e);
		}
	}

	/**
	 * A simple method which prepares imageWrite parameters and set metadata
	 * 
	 * @param cols
	 * @param rows
	 * @param cellsize
	 * @param xl
	 * @param yl
	 * @param yl2 
	 * @param GRASS
	 * @param gc
	 */
	private ParameterBlockJAI buildImageWriteParameters(final int cols,
			final int rows, final double cellsizeX,final double cellsizeY, final double xl,
			final double yl,  final boolean GRASS, GridCoverage2D gc) {

		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		// //
		// Setting Output
		// //
		pbjImageWrite.setParameter("Output", destination);

		// //
		// Setting Compression parameter if needed
		// //
		if (format.getWriteParameters().parameter("Compressed").booleanValue()) {
			Iterator it = ImageIO.getImageWritersByFormatName("arcGrid");
			ImageWriter writer;

			if (it.hasNext()) {
				writer = (ImageWriter) it.next();

				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
				pbjImageWrite.setParameter("WriteParam", param);
			}
		}
		
		// //
		// no data management
		// //
		final GridSampleDimension sd = (GridSampleDimension) gc
				.getSampleDimension(0);
		final List categories = sd.getCategories();
		final Iterator it = categories.iterator();
		Category candidate;
		double inNoData = Double.NaN;
		while (it.hasNext()) {
			candidate = (Category) it.next();
			if (candidate.getName().toString().equalsIgnoreCase(
					Vocabulary.formatInternational(VocabularyKeys.NODATA)
							.toString())) {
				inNoData = candidate.getRange().getMaximum();
			}
		}
		
		// //
		// Construct a proper asciiGridRaster which supports metadata setting
		// //
		pbjImageWrite.setParameter("ImageMetadata",
				new AsciiGridsImageMetadata(cols, rows, cellsizeX, cellsizeY, xl,
						yl, true, GRASS, inNoData));
		return pbjImageWrite;
	}

	/**
	 * Resampling the raster in order to have square pixels instead of
	 * rectangular which are not suitable for an Esrii ascii grid.
	 * 
	 * @param gc
	 *            Input coverage.
	 * @param W
	 *            Geospatial width of this coverage.
	 * @param H
	 *            Geospatial height of this coverage.
	 * 
	 * @return A new coverage with square pixels.
	 */
	private GridCoverage2D reShapeData(GridCoverage2D gc, double W, double H) {
		// resampling the image if needed
		final RenderedImage image = gc.getRenderedImage();
		int Nx = image.getWidth();
		int Ny = image.getHeight();
		final double dx = W / Nx;
		final double dy = H / Ny;

		if (Math.abs(dx - dy) <= ArcGridWriter.EPS) {
			return gc;
		}

		final double _Nx;
		final double _Ny;

		if ((dx - dy) > ArcGridWriter.EPS) {
			/**
			 * we have higher resolution on the Y axis we have to increase it on
			 * the X axis as well.
			 */

			// new number of columns
			_Nx = W / dy;
			Nx = (int) Math.round(_Nx);
		} else {
			/**
			 * we have higher resolution on the X axis we have to increase it on
			 * the Y axis as well.
			 */

			// new number of rows
			_Ny = H / dx;
			Ny = (int) Math.round(_Ny);
		}

		// new grid range
		final GeneralGridRange newGridrange = new GeneralGridRange(new int[] {
				0, 0 }, new int[] { Nx, Ny });
		final GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
				new GeneralEnvelope(gc.getEnvelope()));
		return (GridCoverage2D) Operations.DEFAULT.resample(gc, gc
				.getCoordinateReferenceSystem(), newGridGeometry, Interpolation
				.getInstance(Interpolation.INTERP_NEAREST));
	}

	/**
	 * Writing coordinate reference system WKT representation on a prj file.
	 * 
	 * @param crs
	 * 
	 * @throws IOException
	 */
	private void writeCRSInfo(CoordinateReferenceSystem crs) throws IOException {
		// is it null?
		if (crs == null) {
			// default gcs wgs84
			crs = ArcGridFormat.getDefaultCRS();
		}

		// get the destination path
		// getting the path of this object and the name
		URL url = null;
		String pathname = null;
		String name = null;

		if (this.destination instanceof String) {
			url = (new File((String) this.destination)).toURL();
			pathname = url.getPath().substring(0,
					url.getPath().lastIndexOf("/") + 1);
			name = url.getPath().substring(url.getPath().lastIndexOf("/") + 1,
					url.getPath().length());
		} else if (this.destination instanceof File) {
			url = ((File) this.destination).toURL();
			pathname = url.getPath().substring(0,
					url.getPath().lastIndexOf("/") + 1);
			name = url.getPath().substring(url.getPath().lastIndexOf("/") + 1,
					url.getPath().length());
		} else if (this.destination instanceof URL) {
			url = (URL) this.destination;
			pathname = url.getPath().substring(0,
					url.getPath().lastIndexOf("/") + 1);
			name = url.getPath().substring(url.getPath().lastIndexOf("/") + 1,
					url.getPath().length());
		} else {
			// do nothing for the moment
			return;
		}

		// build up the name
		name = new StringBuffer(pathname).append(
				((name.indexOf(".") > 0) ? name.substring(0, name.indexOf("."))
						: name)).append(".prj").toString();

		// create the file
		final BufferedWriter fileWriter = new BufferedWriter(new FileWriter(
				name));

		// write information on crs
		fileWriter.write(crs.toWKT());
		fileWriter.close();
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMetadataValue(String name, String value) throws IOException,
			MetadataNameNotFoundException {
		// Metadata has not been handled at this point ie there is not spec on
		// where it should be written
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	public void setCurrentSubname(String name) throws IOException {
	}

	/**
	 * Note: The geotools GridCoverage class does not implement the geoAPI
	 * GridCoverage Interface so this method shows an error. All other methods
	 * are using the geotools GridCoverage class
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage,
	 *      org.opengis.parameter.GeneralParameterValue[])
	 */
	public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
			throws IllegalArgumentException, IOException {
		if (parameters != null) {
			setEnvironment(parameters);
		}

		writeGridCoverage((GridCoverage2D) coverage);

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public synchronized void dispose() throws IOException {
		if (disposed)
			return;
		if (mWriter != null) {
			mWriter.dispose();
			mWriter = null;
			disposed = true;
		}
	}
}
