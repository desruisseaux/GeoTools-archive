/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given. 
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.GeoTiffIIOMetadataDecoder;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

/**
 * Provides basic information about the GeoTIFF format IO. This is currently an
 * extension of the Geotools AbstractGridFormat because the stream and file GCEs
 * will pick it up if it extends AbstractGridFormat.
 * 
 * @author Bryce Nordgren, USDA Forest Service
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/src/org/geotools/gce/geotiff/GeoTiffFormat.java $
 */
public final class GeoTiffFormat extends AbstractGridFormat implements Format {
	private final static TIFFImageReaderSpi spi = new TIFFImageReaderSpi();

	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.geotiff");

	/**
	 * Creates a new instance of GeoTiffFormat
	 */
	public GeoTiffFormat() {
		writeParameters = null;
		mInfo = new HashMap();
		mInfo.put("name", "GeoTIFF");
		mInfo.put("description",
				"Tagged Image File Format with Geographic information");
		mInfo.put("vendor", "Geotools");
		mInfo.put("version", "1.1");
		mInfo
				.put("docURL",
						"http://www.remotesensing.org:16080/websites/geotiff/geotiff.html");

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(
						mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));

	}

	/**
	 * Currently, we only accept files, and we open the file to verify that it
	 * has a GeoKeyDirectory tag. If anything more subtle is wrong with the
	 * file, we deal with that when we try and read it.
	 * 
	 * @param o
	 *            the source object to test for compatibility with this format.
	 * 
	 * 
	 * @return true if "o" is a File or a URL that points to a GeoTiff with a
	 *         GeoTiff file as a resource.
	 */
	public boolean accepts(Object o) {

		//
		// if (o instanceof CatalogEntry) {
		// o = ((CatalogEntry) o).resource();
		// }

		try {
			if (o instanceof URL) {
				// /////////////////////////////////////////////////////////////
				//
				// URL management
				// In case the URL points to a file we need to get to the fie
				// directly and avoid caching. In case it points to http or ftp
				// or it is an opnen stream we have very small to do and we need
				// to enable caching.
				//
				// /////////////////////////////////////////////////////////////
				final URL url = (URL) o;
				if (url.getProtocol().equalsIgnoreCase("file"))
					o = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
				else {
					if (url.getProtocol().equalsIgnoreCase("http")
							|| url.getProtocol().equalsIgnoreCase("ftp")) {
						o = ((URL) o).openStream();

					} else
						return false;
				}

			}
			// get a stream
			final ImageInputStream inputStream = ImageIO
					.createImageInputStream(o);
			if (inputStream == null) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.warning("Unable to get an ImageInputStream");
				return false;
			}

			// get a reader
			final ImageReader reader = spi.createReaderInstance();
			reader.setInput(inputStream);
			final IIOMetadata metadata = reader.getImageMetadata(0);
			try {
				final GeoTiffIIOMetadataDecoder metadataAdapter = new GeoTiffIIOMetadataDecoder(
						metadata);

				if (metadataAdapter.getGeoKeyRevision() != 1) {
					return false;
				}
			} catch (UnsupportedOperationException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				return false;
			}
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return false;
		}
		return true;

	}

	/**
	 * If <CODE>source</CODE> is a file, this will return a reader object.
	 * This file does not use hints in the construction of the geotiff reader.
	 * 
	 * @param source
	 *            must be a GeoTiff File
	 * 
	 * @return a GeoTiffReader object initialized to the specified File.
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);

	}

	/**
	 * If <CODE>source</CODE> is a file, this will return a reader object.
	 * This file does not use hints in the construction of the geotiff reader.
	 * 
	 * @param source
	 *            must be a GeoTiff File
	 * @param hints
	 *            Hints to pass the hypothetic {@link GridCoverageReader} to
	 *            control its behaviour.
	 * 
	 * @return a GeoTiffReader object initialized to the specified File.
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		// if (source instanceof CatalogEntry) {
		// source = ((CatalogEntry) source).resource();
		// }

		if (source instanceof URL) {
			URL url = (URL) source;

			try {
				final String pathname = URLDecoder.decode(url.getFile(),
						"UTF-8");

				return new GeoTiffReader(new File(pathname), hints);
			} catch (UnsupportedEncodingException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				return null;
			} catch (DataSourceException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				return null;
			}
		}
		try {
			return new GeoTiffReader(source, null);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		}

	}

	/**
	 * Retrieves a {@link GeoTiffWriter} or <code>null</code> if the provided
	 * <code>destination</code> is suitable.
	 * 
	 * This file does not use hints in the construction of the geotiff reader.
	 * 
	 * @param source
	 *            must be a GeoTiff File
	 * @param hints
	 *            Hints to pass the hypothetic {@link GridCoverageReader} to
	 *            control its behaviour.
	 * 
	 * @return a GeoTiffReader object initialized to the specified File.
	 */
	public GridCoverageWriter getWriter(Object destination) {
		try {
			return new GeoTiffWriter(destination);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		}
	}
}
