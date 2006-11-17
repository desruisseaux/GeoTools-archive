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
package org.geotools.gce.arcgrid;

import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.media.jai.Interpolation;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.spatialschema.geometry.Envelope;

/**
 * ArcGridWriter Supports writing of an ArcGrid GridCoverage to an Desination
 * object, provided the desination object can be converted to a PrintWriter with
 * the IOExchange
 * 
 * @author jeichar
 * @author Simone Giannecchini (simboss)
 * @source $URL$
 */
public class ArcGridWriter implements GridCoverageWriter {
	/** Small number for comparaisons. */
	private static final double EPS = 1E-6;

	/** the destination object where we will do the writing */
	private Object destination;

	transient ArcGridRaster arcGridRaster;

	transient PrintWriter mWriter;

	/**
	 * an IOExchange used to figure out the the Writer class to retrieve from
	 * destination
	 */
	private IOExchange ioexchange;

	/** Format for this wirter. */
	private Format format = (new ArcGridFormatFactory()).createFormat();

	/**
	 * Takes either a URL or a String that points to an ArcGridCoverage file and
	 * converts it to a URL that can then be written to.
	 * 
	 * @param destination
	 *            the URL or String pointing to the file to load the ArcGrid
	 */
	public ArcGridWriter(Object destination) {
		this.destination = destination;
		ioexchange = IOExchange.getIOExchange();
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

	boolean parseBoolean(ParameterValueGroup params, String name) {
		if (params == null) {
			throw new InvalidParameterValueException(
					"A Parameter group was expected", null, null);
		}

		ParameterValue targetInfo = params.parameter(name);

		if (targetInfo == null) {
			throw new InvalidParameterNameException(name,
					"Not a ArcGrid paramerter");
		}

		org.opengis.parameter.ParameterValue target = params.parameter(name);

		if (target == null) {
			throw new InvalidParameterValueException("Parameter " + name
					+ "is requried", null, null);
		}

		return target.booleanValue();
	}

	private void setEnvironment(GeneralParameterValue[] parameters)
			throws InvalidParameterNameException,
			InvalidParameterValueException, IOException {
		// this.name = name;
		if (parameters == null) {
			format.getWriteParameters().parameter("GRASS").setValue(false);
			format.getWriteParameters().parameter("Compressed").setValue(false);
		} else {
			format.getWriteParameters().parameter("Compressed").setValue(
					((ParameterValue) parameters[0]).booleanValue());
			format.getWriteParameters().parameter("GRASS").setValue(
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
	 * 
	 * 
	 */
	private void writeGridCoverage(GridCoverage gc) throws DataSourceException {
		try {
			// getting crs from gc
			final CoordinateReferenceSystem crs = gc
					.getCoordinateReferenceSystem();

			final Envelope oldEnv = gc.getEnvelope();
			// check if the coverage needs to be resampled in order to have
			// square pixels
			
			AffineTransform gridToWorld = (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem();
			final boolean lonFirst = (XAffineTransform.getSwapXY(gridToWorld) != -1);
			gc = reShapeData(((GridCoverage2D) gc), oldEnv.getLength(lonFirst?0:1), // W
					oldEnv.getLength(lonFirst?1:0) // H
			);

			// getting the underlying raster
			final Raster data = ((GridCoverage2D) gc).getRenderedImage()
					.getData();
			// getting the new envelope
			final Envelope newEnv = gc.getEnvelope();

			// trying to prepare the header
			gridToWorld = (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem();

			final double xl = lonFirst ? newEnv.getLowerCorner().getOrdinate(0)
					: newEnv.getLowerCorner().getOrdinate(1);
			final double yl = !lonFirst ? newEnv.getLowerCorner().getOrdinate(0)
					: newEnv.getLowerCorner().getOrdinate(1);

			final double cellsize = XAffineTransform.getScaleX0(gridToWorld);

			// writing crs info
			writeCRSInfo(crs);

			if (format.getWriteParameters().parameter("GRASS").booleanValue()) {
				arcGridRaster = new GRASSArcGridRaster(mWriter);
			} else {
				arcGridRaster = new ArcGridRaster(mWriter);
			}

			arcGridRaster.writeRaster(data, xl, yl, cellsize, format
					.getWriteParameters().parameter("Compressed")
					.booleanValue());

		} catch (Exception ioe) {
			throw new DataSourceException("IOError writing", ioe);
		}
	}

	/**
	 * Resaping the raster in order to have square pixels instead of rectangular
	 * which are not suitable for an ascii grid.
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
	 * @throws IOException
	 * @throws org.opengis.referencing.NoSuchAuthorityCodeException
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
		name = pathname
				+ ((name.indexOf(".") > 0) ? name.substring(0, name
						.indexOf(".")) : name) + ".prj";

		// create the file
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(name));

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

		if (format.getWriteParameters().parameter("Compressed").booleanValue()) {
			mWriter = ioexchange.getGZIPPrintWriter(destination);
		} else {
			mWriter = ioexchange.getPrintWriter(destination);
		}

		writeGridCoverage(coverage);
		mWriter.close();
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public void dispose() throws IOException {
		if (mWriter != null) {
			mWriter.close();
		}
	}
}
