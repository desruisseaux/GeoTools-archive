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
package org.geotools.gce.ecw;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.plugins.ecw.ECWImageReaderSpi;

import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * A simple implementation of the ECW Grid Format.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public class ECWFormat extends AbstractGridFormat implements Format {
	/**
	 * Logger.
	 * 
	 */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.ecw");


	/** Caching the {@link ECWImageReaderSpi} factory. */
	private final ECWImageReaderSpi spi = new ECWImageReaderSpi();

	/**
	 * Creates an instance and sets the metadata.
	 */
	public ECWFormat() {
		setInfo();
	}

	/**
	 * Sets the metadata information.
	 */
	private void setInfo() {
		HashMap info = new HashMap();
		info.put("name", "ECW");
		info.put("description", "ECW Grid Coverage Format");
		info.put("vendor", "Geotools");
		info.put("docURL", "http://www.ermapper.com/ecw/");
		info.put("version", "1.0");
		mInfo = info;

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
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
	 *      input)
	 */
	public boolean accepts(Object input) {
		try {
			return spi.canDecodeInput(input);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return false;
		}
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getName()
	 */
	public String getName() {
		return (String) this.mInfo.get("name");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDescription()
	 */
	public String getDescription() {
		return (String) this.mInfo.get("description");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVendor()
	 */
	public String getVendor() {
		return (String) this.mInfo.get("vendor");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDocURL()
	 */
	public String getDocURL() {
		return (String) this.mInfo.get("docURL");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVersion()
	 */
	public String getVersion() {
		return (String) this.mInfo.get("version");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getReadParameters()
	 */
	public ParameterValueGroup getReadParameters() {
		return readParameters;
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getWriteParameters()
	 */
	public ParameterValueGroup getWriteParameters() {
		return writeParameters;

	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object,
	 *      Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {
			return new ECWReader(source, hints);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		}
	}
}
