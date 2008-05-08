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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.opengis.parameter.ParameterDescriptor;

/**
 * A Format to allow discovery of Readers/Writers for raster images that support
 * world files containing information about the image. Supports gif+gfw,
 * jpg/jpeg+jgw, tif/tiff+tfw and png+pgw. wld may be used in place of
 * the format specific extension (jpg+wld, etc) Designed to be used with
 * GridCoverageExchange.
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/image/src/org/geotools/gce/image/WorldImageFormat.java $
 * @author Simone Giannecchini
 */
public final class WorldImageFormat extends AbstractGridFormat implements
		Format {

	/** {@link Set} of supported extensions for png world files. */
	private final static Set PNG_WFILE_EXT;

	/** {@link Set} of supported extensions for tiff world files. */
	private final static Set TIFF_WFILE_EXT;

	/** {@link Set} of supported extensions for jpeg world files. */
	private final static Set JPG_WFILE_EXT;

	/** {@link Set} of supported extensions for gif world files. */
	private final static Set GIF_WFILE_EXT;

	/** {@link Set} of supported extensions for bmp world files. */
	private final static Set BMP_WFILE_EXT;

	static {
		// png
		Set tempSet = new HashSet(2);
		tempSet.add(".pgw");
		tempSet.add(".pngw");
		PNG_WFILE_EXT = Collections.unmodifiableSet(tempSet);

		// jpeg
		tempSet = new HashSet(3);
		tempSet.add(".jpw");
		tempSet.add(".jgw");
		tempSet.add(".jpgw");
		tempSet.add(".jpegw");
		JPG_WFILE_EXT = Collections.unmodifiableSet(tempSet);

		// gif
		tempSet = new HashSet(2);
		tempSet.add(".gifw");
		tempSet.add(".gfw");
		GIF_WFILE_EXT = Collections.unmodifiableSet(tempSet);

		// png
		tempSet = new HashSet(2);
		tempSet.add(".tfw");
		tempSet.add(".tiffw");
		TIFF_WFILE_EXT = Collections.unmodifiableSet(tempSet);

		// bmp
		tempSet = new HashSet(2);
		tempSet.add(".bmw");
		tempSet.add(".bmpw");
		BMP_WFILE_EXT = Collections.unmodifiableSet(tempSet);

	}

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.image");

	/**
	 * Format writing parameter. When writing a world image we need to provide
	 * an output format in which we want to encode the image itself. PNG is
	 * default output format.
	 */
	public static final ParameterDescriptor FORMAT = new DefaultParameterDescriptor(
			"Format", "Indicates the output format for this image", "png", true);

	/**
	 * WorldImageFormat
	 */
	public WorldImageFormat() {
		setInfo();
	}

	private void setInfo() {
		// information for this format
		HashMap info = new HashMap();

		info.put("name", "WorldImage");
		info.put("description",
				"A raster file accompanied by a spatial data file");
		info.put("vendor", "Geotools");
		info.put("docURL", "http://www.geotools.org/WorldImageReader+formats");
		info.put("version", "1.0");
		mInfo = info;

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(
						mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, }));

		// writing parameters
		writeParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(mInfo,
						new GeneralParameterDescriptor[] { FORMAT }));
	}

	/**
	 * Retrieves a {@link WorldImageReader} in case the providede
	 * <code>source</code> can be accepted as a valid source for a world
	 * image. The method returns null otherwise.
	 * 
	 * @param source
	 *            The source object to read a WorldImage from
	 * 
	 * @return a new WorldImageReader for the source
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);
	}

	/**
	 * Call the accepts() method before asking for a writer to determine if the
	 * current object is supported.
	 * 
	 * @param destination
	 *            the destination object to write a WorldImage to
	 * 
	 * @return a new WorldImageWriter for the destination
	 */
	public GridCoverageWriter getWriter(Object destination) {
		return new WorldImageWriter(destination);
	}

	/**
	 * Call the accepts() method before asking for a writer to determine if the
	 * current object is supported.
	 * 
	 * @param destination
	 *            the destination object to write a WorldImage to
	 * 
	 * @return a new WorldImageWriter for the destination
	 */
	public GridCoverageWriter getWriter(Object destination, Hints hints) {
		return new WorldImageWriter(destination, hints);
	}

	/**
	 * Takes the input and determines if it is a class that we can understand
	 * and then futher checks the format of the class to make sure we can
	 * read/write to it.
	 * 
	 * @param input
	 *            The object to check for acceptance.
	 * 
	 * @return true if the input is acceptable, false otherwise
	 */
	public boolean accepts(Object input) {
		String pathname = "";

		if (input instanceof URL) {
			final URL url = (URL) input;
			final String protocol = url.getProtocol();
			if (protocol.equalsIgnoreCase("file"))
				pathname = url.getFile();
			else {
				if (protocol.equalsIgnoreCase("http")) {
					final String query;
					try {
						query = java.net.URLDecoder.decode(url.getQuery()
								.intern(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						if (LOGGER.isLoggable(Level.FINE))
							LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
						return false;
					}

					// should we proceed? Let's look for a getmap WMS request
					// we do a very basic check we should make this stronger!
					// @todo
					if (query.toLowerCase().intern().indexOf("getmap") == -1)
						return false;
					return true;

				}
			}
		} else if (input instanceof File) {
			File file = (File) input;

			pathname = file.getAbsolutePath();
		} else if (input instanceof String)
			pathname = (String) input;
		// else if (input instanceof InputStream
		// || input instanceof ImageInputStream)
		// return true;// @ask TODO is this right?????
		else
			return false;
		// check if we can decode this file
		if (!(pathname.endsWith(".gif") || pathname.endsWith(".jpg")
				|| pathname.endsWith(".jpeg") || pathname.endsWith(".tif")
				|| pathname.endsWith(".tiff") || pathname.endsWith(".png") || pathname
				.endsWith(".bmp"))) {
			return false;
		}

		// check the presence of the world file
		final File source = new File(pathname);
		if (!source.exists())
			return false;
		String suffix;
		String fileName;

		boolean answer = false;
		final File parentDir = source.getParentFile();
		if (parentDir != null) {
			final int dotIndex = pathname.lastIndexOf('.');
			if (dotIndex != -1) {
				fileName = pathname.substring(0, dotIndex);
				suffix = pathname.substring(dotIndex + 1, pathname.length());
				final Set suffixes = WorldImageFormat.getWorldExtension(suffix);
				final Iterator it = suffixes.iterator();
				StringBuffer buff = new StringBuffer(fileName);
				do {
					answer = new File(buff.append((String)it.next()).toString()).exists();
					buff = new StringBuffer(fileName);
				} while (!answer && it.hasNext());
				if (!answer) {
					buff.setLength(0);
					buff.append(fileName);
					buff.append(".wld");
					answer = new File(buff.toString()).exists();
				}
				if (!answer) {
					buff.setLength(0);
					buff.append(fileName);
					buff.append(".meta");
					answer = new File(buff.toString()).exists();
				}
			}

		}
		return answer;
	}

	/**
	 * Takes an image file extension (such as .gif, including the '.') and
	 * returns it's corresponding world file extension (such as .gfw).
	 * 
	 * @param fileExtension
	 *            an image file extension, including the '.'
	 * 
	 * @return a corresponding {@link Set} of world file extensions, including
	 *         the '.'
	 */
	public static Set getWorldExtension(String fileExtension) {
		if (fileExtension == null) {
			throw new NullPointerException("Provided input is null");
		}

		if (fileExtension.equalsIgnoreCase("png")) {
			return PNG_WFILE_EXT;
		}

		if (fileExtension.equals("gif")) {
			return GIF_WFILE_EXT;
		}

		if (fileExtension.equalsIgnoreCase("jpg")
				|| fileExtension.equalsIgnoreCase("jpeg")) {
			return JPG_WFILE_EXT;
		}

		if (fileExtension.equalsIgnoreCase("tif")
				|| fileExtension.equalsIgnoreCase("tiff")) {
			return TIFF_WFILE_EXT;
		}

		if (fileExtension.equalsIgnoreCase("bmp")) {
			return BMP_WFILE_EXT;
		}

		throw new IllegalArgumentException("Unsupported file format");
	}

	/**
	 * Retrieves a {@link WorldImageReader} in case the providede
	 * <code>source</code> can be accepted as a valid source for a world
	 * image. The method returns null otherwise.
	 * 
	 * @param source
	 *            The source object to read a WorldImage from
	 * @param hints
	 *            {@link Hints} to control the provided {@link WorldImageReader}.
	 * @return a new WorldImageReader for the source
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {
			return new WorldImageReader(source, hints);
		} catch (DataSourceException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Always returns null since for the moment there are no
	 * {@link GeoToolsWriteParams} availaible for this format.
	 * 
	 * @return always null.
	 */
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		return null;
	}
}
