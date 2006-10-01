/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureSource;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureType;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * {@link AbstractGridFormat} sublass for controlling {@link ImageMosaicReader}
 * creation.
 * 
 * @author Simone Giannecchini (simboss)
 * @since 2.3
 */
public final class ImageMosaicFormat extends AbstractGridFormat implements
		Format {

	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.imagemosaic");

	public static final DefaultParameterDescriptor FINAL_ALPHA = new DefaultParameterDescriptor(
			"FinalAlpha", Boolean.class, null, Boolean.FALSE);

	public static final DefaultParameterDescriptor ALPHA_THRESHOLD = new DefaultParameterDescriptor(
			"AlphaThreshold", Double.class, null, new Double(1));

	public static final DefaultParameterDescriptor INPUT_IMAGE_ROI = new DefaultParameterDescriptor(
			"InputImageROI", Boolean.class, null, Boolean.FALSE);

	public static final DefaultParameterDescriptor INPUT_IMAGE_ROI_THRESHOLD = new DefaultParameterDescriptor(
			"InputImageROIThreshold", Integer.class, null, new Integer(1));

	/**
	 * Creates an instance and sets the metadata.
	 */
	public ImageMosaicFormat() {
		setInfo();
	}

	/**
	 * Sets the metadata information.
	 */
	private void setInfo() {
		HashMap info = new HashMap();

		info.put("name", "ImageMosaic");
		info.put("description", "Image mosaicking plugin");
		info.put("vendor", "Geotools");
		info.put("docURL", "");
		info.put("version", "1.0");
		mInfo = info;

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
								FINAL_ALPHA, ALPHA_THRESHOLD, INPUT_IMAGE_ROI,
								INPUT_IMAGE_ROI_THRESHOLD }));

		// reading parameters
		writeParameters = null;
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object)
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);
	}

	/**
	 * 
	 */
	public GridCoverageWriter getWriter(Object destination) {
		throw new UnsupportedOperationException(
				"This plugin does not support writing.");
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
	 *      input)
	 */
	public boolean accepts(Object source) {
		try {

			URL sourceURL;
			// /////////////////////////////////////////////////////////////////////
			//
			// Check source
			//
			// /////////////////////////////////////////////////////////////////////
			if (source instanceof File)
				sourceURL = ((File) source).toURL();
			else if (source instanceof URL)
				sourceURL = (URL) source;
			else if (source instanceof String) {
				final File tempFile = new File((String) source);
				if (tempFile.exists()) {
					sourceURL = tempFile.toURL();
				} else
					try {
						sourceURL = new URL(URLDecoder.decode((String) source,
								"UTF8"));
						if (sourceURL.getProtocol() != "file") {
							return false;

						}
					} catch (MalformedURLException e) {
						if (LOGGER.isLoggable(Level.WARNING))
							LOGGER.log(Level.WARNING, e.getLocalizedMessage(),
									e);
						return false;

					} catch (UnsupportedEncodingException e) {
						if (LOGGER.isLoggable(Level.WARNING))
							LOGGER.log(Level.WARNING, e.getLocalizedMessage(),
									e);
						return false;

					}

			} else
				return false;
			// /////////////////////////////////////////////////////////////////////
			//
			// Load tiles informations, especially the bounds, which will be
			// reused
			//
			// /////////////////////////////////////////////////////////////////////
			final ShapefileDataStore tileIndexStore = new ShapefileDataStore(
					sourceURL);
			final String[] typeNames = tileIndexStore.getTypeNames();
			if (typeNames.length <= 0)
				return false;
			final String typeName = typeNames[0];
			final FeatureSource featureSource = tileIndexStore
					.getFeatureSource(typeName);
			final FeatureType schema = featureSource.getSchema();
			if (schema.getAttributeType("location") == null)
				return false;// looking
			// for the
			// location
			// attribute

			return true;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return false;

		}

	}

	/**
	 * @see AbstractGridFormat#getReader(Object, Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {

			return new ImageMosaicReader(source, hints);
		} catch (MalformedURLException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		}
	}

}
