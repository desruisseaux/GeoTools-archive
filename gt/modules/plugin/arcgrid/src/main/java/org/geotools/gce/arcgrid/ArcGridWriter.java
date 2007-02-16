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

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.coverage.processing.operation.SelectSampleDimension;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageWriter;
import org.geotools.gce.imageio.asciigrid.spi.AsciiGridsImageWriterSpi;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.image.CoverageUtilities;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;

/**
 * {@link ArcGridWriter} supports writing of an ArcGrid GridCoverage to a
 * Desination object
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public final class ArcGridWriter extends AbstractGridCoverageWriter implements
		GridCoverageWriter {
	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.arcgrid");

	/** Imageio {@link AsciiGridsImageWriter} we will use to write out. */
	private AsciiGridsImageWriter mWriter = new AsciiGridsImageWriter(
			new AsciiGridsImageWriterSpi());

	/** Default {@link ParameterValueGroup} for doing a bandselect. */
	private final static ParameterValueGroup bandSelectParams;

	/** Default {@link ParameterValueGroup} for doing a reshape. */
	private final static ParameterValueGroup reShapeParams;

	/** Caching a {@link Resample} operation. */
	private static final Resample resampleFactory = new Resample();

	/** Caching a {@link SelectSampleDimension} operation. */
	private static final SelectSampleDimension bandSelectFactory = new SelectSampleDimension();
	static {
		DefaultProcessor processor = new DefaultProcessor(new Hints(
				Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
		bandSelectParams = (ParameterValueGroup) processor.getOperation(
				"SelectSampleDimension").getParameters();

		reShapeParams = (ParameterValueGroup) processor
				.getOperation("Resample").getParameters();
	}

	/** Small number for comparaisons. */
	private static final double EPS = 1E-6;

	/** Are we going to write using a GRASS header or not?. */
	private boolean grass = false;

	/** The band of the provided coverage that we want to write down. */
	private int writeBand = -1;

	/**
	 * Takes either a URL or a String that points to an ArcGridCoverage file and
	 * converts it to a URL that can then be written to.
	 * 
	 * @param destination
	 *            the URL or String pointing to the file to load the ArcGrid
	 * @throws DataSourceException
	 */
	public ArcGridWriter(Object destination) throws DataSourceException {
		this(destination, null);
	}

	/**
	 * Takes either a URL or a String that points to an ArcGridCoverage file and
	 * converts it to a URL that can then be written to.
	 * 
	 * @param destination
	 *            the URL or String pointing to the file to load the ArcGrid
	 * @throws DataSourceException
	 */
	public ArcGridWriter(Object destination, Hints hints)
			throws DataSourceException {
		this.destination = destination;
		if (destination instanceof File)
			try {
				super.outStream = ImageIO.createImageOutputStream(destination);
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw new DataSourceException(e);
			}
		else if (destination instanceof URL) {
			final URL dest = (URL) destination;
			if (dest.getProtocol().equalsIgnoreCase("file")) {
				File destFile;
				try {
					destFile = new File(URLDecoder.decode(dest.getFile(),
							"UTF8"));
				} catch (UnsupportedEncodingException e) {
					if (LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					throw new DataSourceException(e);
				}
				try {
					super.outStream = ImageIO.createImageOutputStream(destFile);
				} catch (IOException e) {
					if (LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					throw new DataSourceException(e);
				}
			}

		} else if (destination instanceof OutputStream) {

			try {
				super.outStream = ImageIO
						.createImageOutputStream((OutputStream) destination);
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw new DataSourceException(e);
			}

		} else if (destination instanceof ImageOutputStream)
			this.destination = outStream = (ImageOutputStream) destination;
		else
			throw new DataSourceException(
					"The provided destination cannot be used!");
		// //
		//
		// managing hints
		//
		// //
		if (hints != null) {
			if (this.hints == null)
			{
				this.hints = new Hints(Hints.LENIENT_DATUM_SHIFT,Boolean.TRUE);
				this.hints.add(new RenderingHints(JAI.KEY_TILE_CACHE,null));
			}
			this.hints.add(hints);
		}
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
		return new ArcGridFormat();
	}

	/**
	 * Sets up the object's environment based on the Parameters passed to it by
	 * the client.
	 * 
	 * 
	 * @param parameters
	 * @param gc2D
	 * @throws InvalidParameterNameException
	 * @throws InvalidParameterValueException
	 * @throws IOException
	 */
	private void setEnvironment(GeneralParameterValue[] parameters,
			GridCoverage2D gc2D) throws InvalidParameterNameException,
			InvalidParameterValueException, IOException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Checking writing params
		//
		// /////////////////////////////////////////////////////////////////////
		GeoToolsWriteParams gtParams = null;
		if (parameters != null) {
			Parameter param;
			String name;
			final int length = parameters.length;
			for (int i = 0; i < length; i++) {
				param = (Parameter) parameters[i];
				name = param.getDescriptor().getName().toString();
				if (param.getDescriptor().getName().getCode().equals(
						AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
								.toString())) {
					gtParams = (GeoToolsWriteParams) param.getValue();
				}
				if (name.equalsIgnoreCase("GRASS"))
					grass = param.booleanValue();
			}
		}
		if(gtParams==null)
			gtParams= new ArcGridWriteParams();
		// write band
		int[] writeBands = gtParams.getSourceBands();
		writeBand = CoverageUtilities.getVisibleBand(gc2D.getRenderedImage());
		if ((writeBands == null || writeBands.length == 0 || writeBands.length > 1)
				&& (writeBand < 0 || writeBand > gc2D.getNumSampleDimensions()))
			throw new IllegalArgumentException(
					"You need to supply a valid index for deciding which band to write.");
		if (!((writeBands == null || writeBands.length == 0 || writeBands.length > 1)))
			writeBand = writeBands[0];

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
			final Envelope oldEnv = gc.getEnvelope();

			// /////////////////////////////////////////////////////////////////
			//
			// getting visible band
			// TODO make the band we write parametric, meaning that the user can
			// choose it he wants.
			// /////////////////////////////////////////////////////////////////
			final int numBands = gc.getNumSampleDimensions();
			if (numBands > 1) {
				final int visibleBand;
				if (writeBand > 0 && writeBand < numBands)
					visibleBand = writeBand;
				else
					visibleBand = CoverageUtilities.getVisibleBand(gc);

				final ParameterValueGroup param = (ParameterValueGroup) ArcGridWriter.bandSelectParams
						.clone();
				param.parameter("source").setValue(gc);
				param.parameter("SampleDimensions").setValue(
						new int[] { visibleBand });
				gc = (GridCoverage2D) bandSelectFactory
						.doOperation(param, null);
			}
			// /////////////////////////////////////////////////////////////////
			//
			// checking if the coverage needs to be resampled in order to have
			// square pixels for the esri format
			//
			// /////////////////////////////////////////////////////////////////
			if (!grass)
				gc = reShapeData(gc, oldEnv.getLength(0), // W
						oldEnv.getLength(1)// H
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
			final double xl = newEnv.getLowerCorner().getOrdinate(0);
			final double yl = newEnv.getLowerCorner().getOrdinate(0);
			final double cellsizeX = Math.abs(gridToWorld.getScaleX());
			final double cellsizeY = Math.abs(gridToWorld.getScaleY());

			// /////////////////////////////////////////////////////////////////
			//	
			// Preparing source image and metadata
			//
			// /////////////////////////////////////////////////////////////////
			final RenderedImage source = gc.getRenderedImage();
			final int cols = source.getWidth();
			final int rows = source.getHeight();

			// Preparing main parameters for JAI imageWrite Operation
			// //
			// Setting Output
			// //
			mWriter.setOutput(outStream);

			// //
			// no data management
			// //
			double inNoData = getCandidateNoData(gc);

			// //
			// Construct a proper asciiGridRaster which supports metadata
			// setting
			// //

			// Setting the source for the image write operation
			mWriter.write(null, new IIOImage(source, null,
					new AsciiGridsImageMetadata(cols, rows, cellsizeX,
							cellsizeY, xl, yl, true, grass, inNoData)), null);

			// writing crs info
			writeCRSInfo(crs);

			// /////////////////////////////////////////////////////////////////
			//
			// Creating the imageWrite Operation
			//
			// /////////////////////////////////////////////////////////////////
			mWriter.dispose();
			// TODO: Auto-dispose. Maybe I need to allow a manual dispose call?
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		}
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
		final ParameterValueGroup param = (ParameterValueGroup) reShapeParams
				.clone();
		param.parameter("source").setValue(gc);
		param.parameter("CoordinateReferenceSystem").setValue(
				gc.getCoordinateReferenceSystem2D());
		param.parameter("GridGeometry").setValue(newGridGeometry);
		param.parameter("InterpolationType").setValue(
				Interpolation.getInstance(Interpolation.INTERP_NEAREST));
		return (GridCoverage2D) resampleFactory.doOperation(param, hints);
	}

	/**
	 * Writing {@link CoordinateReferenceSystem} WKT representation on a prj
	 * file.
	 * 
	 * @param crs
	 *            the {@link CoordinateReferenceSystem} to be written out.
	 * 
	 * @throws IOException
	 */
	private void writeCRSInfo(CoordinateReferenceSystem crs) throws IOException {
		// is it null?
		if (crs == null) {
			throw new IllegalArgumentException("CRS cannot be null!");
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
			setEnvironment(parameters,(GridCoverage2D) coverage);
		}

		writeGridCoverage((GridCoverage2D) coverage);

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public void dispose() {

		if (mWriter != null) {
			mWriter.dispose();
			mWriter = null;

		}

	}

	static double getCandidateNoData(GridCoverage2D gc) {
		// no data management
		final GridSampleDimension sd = (GridSampleDimension) gc
				.getSampleDimension(0);
		final List categories = sd.getCategories();
		final Iterator it = categories.iterator();
		Category candidate;
		double inNoData = Double.NaN;
        final String noDataName = Vocabulary.format(VocabularyKeys.NODATA);
		while (it.hasNext()) {
			candidate = (Category) it.next();
            final String name = candidate.getName().toString();
			if (name.equalsIgnoreCase("No Data") || name.equalsIgnoreCase(noDataName)) {
				inNoData = candidate.getRange().getMaximum();
			}
		}

		return inNoData;
	}

}
