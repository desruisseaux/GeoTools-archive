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

import it.geosolutions.imageio.plugins.arcgrid.spi.AsciiGridsImageReaderSpi;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * An implementation a {@link Format} for the ASCII grid ESRI and GRASS format.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public final class ArcGridFormat extends AbstractGridFormat implements Format {
	/**
	 * Logger.
	 * 
	 */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.arcgrid");

	/** Indicates whether the arcgrid data must be written in GRASS format */
	public static final DefaultParameterDescriptor GRASS = new DefaultParameterDescriptor(
			"GRASS", "Indicates whether the arcgrid data has to be written in GRASS format",
			Boolean.FALSE, true);
	
	/** Indicates whether we ask the plugin to resample the coverage to have dx==dy */
	public static final DefaultParameterDescriptor FORCE_CELLSIZE = new DefaultParameterDescriptor(
			"FORCE_CELLSIZE", "Indicates whether the input coverage has to be resampled to have dx==dyt",
			Boolean.FALSE, false);
	

	/** Caching the {@link AsciiGridsImageReaderSpi} factory. */
	private final AsciiGridsImageReaderSpi spi = new AsciiGridsImageReaderSpi();

	/**
	 * Creates an instance and sets the metadata.
	 */
	public ArcGridFormat() {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Creating a new ArcGriFormat.");
		setInfo();
	}

	/**
	 * Sets the metadata information.
	 */
	private void setInfo() {
		HashMap info = new HashMap();

		info.put("name", "ArcGrid");
		info.put("description", "Arc Grid Coverage Format");
		info.put("vendor", "Geotools");
		info.put("docURL", "http://gdal.velocet.ca/projects/aigrid/index.html");
		info.put("version", "1.0");
		mInfo = info;

		// writing parameters
		writeParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(mInfo,
						new GeneralParameterDescriptor[] { GRASS,
								GEOTOOLS_WRITE_PARAMS,FORCE_CELLSIZE }));

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(
						mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object
	 *      source)
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
	 *      destination)
	 */
	public GridCoverageWriter getWriter(Object destination) {
		try {
			return new ArcGridWriter(destination);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
	 *      destination,Hints hints)
	 */
	public GridCoverageWriter getWriter(Object destination, Hints hints) {
		try {
			return new ArcGridWriter(destination, hints);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
	 *      input)
	 */
	public boolean accepts(Object input) {
		try {
			return spi.canDecodeInput(input);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			return false;
		}
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object,
	 *      Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {
			return new ArcGridReader(source, hints);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves the default instance for the {@link ArcGridFormat} of the
	 * {@link GeoToolsWriteParams} to control the writing process.
	 * 
	 * @return a default instance for the {@link ArcGridFormat} of the
	 *         {@link GeoToolsWriteParams} to control the writing process.
	 */
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {

		return new ArcGridWriteParams();
	}
}
