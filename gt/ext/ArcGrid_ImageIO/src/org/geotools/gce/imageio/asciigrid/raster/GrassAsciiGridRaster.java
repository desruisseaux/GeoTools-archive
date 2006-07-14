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
package org.geotools.gce.imageio.asciigrid.raster;

import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.geotools.gce.imageio.asciigrid.LoggerController;

/**
 * Class used to handle an ASCII GRASS format source.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 */
public final class GrassAsciiGridRaster extends AsciiGridRaster {
	private static final Logger logger = Logger
			.getLogger(GrassAsciiGridRaster.class.toString());

	/** Column number tag in the header file */
	public static final String COLS = "COLS:";

	/** Row number tag in the header file */
	public static final String ROWS = "ROWS:";

	/** x corner coordinate tag in the header file */
	public static final String NORTH = "NORTH:";

	/** y corner coordinate tag in the header file */
	public static final String SOUTH = "SOUTH:";

	/** y corner coordinate tag in the header file */
	public static final String EAST = "EAST:";

	/** y corner coordinate tag in the header file */
	public static final String WEST = "WEST:";

	public static final String NO_DATA_MARKER = "*";

	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 */
	public GrassAsciiGridRaster(ImageInputStream iis) {
		super(iis);
	}

	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public GrassAsciiGridRaster(ImageOutputStream ios) {
		super(ios);
	}

	/**
	 * Parses the header for the known properties.
	 * 
	 * @throws IOException
	 *             for reading errors
	 */
	public void parseHeader() throws IOException {
		// /**
		// * This is the GRASS ASCII Grid Format
		// * NORTH: XX
		// * SOUTH: XX
		// * EAST: XX
		// * WEST: XX
		// * ROWS: XX
		// * COLS: XX
		// * XX XX XX XX XX XX... (DATA VALUES)
		// */
		double north = 0;
		double south = 0;
		double east = 0;
		double west = 0;
		if (LoggerController.enableLoggerAsciiGridRaster)
			logger.info("Header Parsed: ");

		boolean keepParsing = true;
		imageIS.mark();

		int i = 0;
		int requiredFields = 0;
		long headerEnd = -1;
		String s = null;
		String sKey = null;

		/**
		 * Parsing the header
		 */
		// if in the header there is a field (like cols, rows) not followed by
		// numbers, parseInt or parseDouble throws a NumberFormatException
		while (keepParsing && ((s = imageIS.readLine()) != null)) {
			i = s.indexOf(" ");

			if (i != -1) {
				sKey = s.substring(0, i);
				s = (s.substring(i)).trim();

				if (NORTH.equalsIgnoreCase(sKey)) {
					north = Double.parseDouble(s);
					requiredFields++;

					continue;
				}

				if (SOUTH.equalsIgnoreCase(sKey)) {
					south = Double.parseDouble(s);
					requiredFields++;

					continue;
				}

				if (EAST.equalsIgnoreCase(sKey)) {
					// east = Double.parseDouble(s);
					requiredFields++;
					east = Double.parseDouble(s);
					continue;
				}

				if (WEST.equalsIgnoreCase(sKey)) {
					west = Double.parseDouble(s);
					requiredFields++;

					continue;
				}

				if (ROWS.equalsIgnoreCase(sKey)) {
					nRows = Integer.parseInt(s);
					requiredFields++;

					continue;
				}

				if (COLS.equalsIgnoreCase(sKey)) {
					nCols = Integer.parseInt(s);
					requiredFields++;
					dataStartAt = imageIS.getStreamPosition();
					keepParsing = false;
				} else {
					keepParsing = false;
				}
			} else {
				// probably, is a new empty line. Maybe, next data could be
				// valid data values.
				// so, I get current stream position as precaution
				keepParsing = false;
			}
		}

		if (requiredFields < 6) {
			// The Header is not compliant with the GRASS ASCII format.
			// I reset the stream
			imageIS.reset();
			throw new IOException("Header not identified");
		} else {
			headerEnd = dataStartAt;

			// Next char can be a kind of whitespace or the first digit of a
			// data value. Reading methods need to know where useful data
			// value begins. I move the stream position to the first useful
			// data value.

			imageIS.reset();
			imageIS.mark();
			imageIS.seek(dataStartAt);

			// stop reading when character is not a white space
			int character = imageIS.read();
			while ((character == 32) || (character == 9) || (character == 13)
					|| (character == 10)) {
				headerEnd++;
				character = imageIS.read();
			}

			dataStartAt = headerEnd;
		}
		if (LoggerController.enableLoggerAsciiGridRaster) {
			logger.info("\n\tnCols:" + nCols);
			logger.info("\tnRows:" + nRows);
		}
		imageIS.reset();

		// Preparing data.
		xllCellCoordinate = west;
		yllCellCoordinate = south;
		isCorner = true;
		cellSizeY = (north - south) / nRows;
		cellSizeX = (east - west) / nCols;
	}

	/**
	 * This method provides the header writing
	 * 
	 * @param columnsString
	 *            A String representing the number of columns
	 * @param rowsString
	 *            A String representing the number of rows
	 * @param xllString
	 *            A String representing the xllCorner of the Bounding Box
	 * @param yllString
	 *            A String representing the yllCorner of the Bounding Box
	 * @param cellsizeString
	 *            A String representing the size of the grid cell
	 * @param rasterSpaceType
	 *            Not interesting in GRASS rasters
	 * @param noDataValue
	 *            Not interesting in GRASS rasters
	 * 
	 * @throws IOException
	 *             if a writing error occurs
	 */
	public void writeHeader(String columnsString, String rowsString,
			String xllString, String yllString, String cellsizeStringX, String cellsizeStringY,
			String rasterSpaceType, String noDataValue) throws IOException {
		nCols = Integer.parseInt(columnsString);
		nRows = Integer.parseInt(rowsString);
		cellSizeX = Double.parseDouble(cellsizeStringX);
		cellSizeY = Double.parseDouble(cellsizeStringY);
		xllCellCoordinate = Double.parseDouble(xllString);
		yllCellCoordinate = Double.parseDouble(yllString);

		final double west;
		final double east;
		final double south;
		final double north;
		west = xllCellCoordinate;
		south = yllCellCoordinate;
		north = south + (cellSizeY * nRows);
		east = west + (cellSizeX * nCols);

		imageOS.writeBytes(new StringBuffer(NORTH).append(" ").append(
				Double.toString(north)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(SOUTH).append(" ").append(
				Double.toString(south)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(EAST).append(" ").append(
				Double.toString(east)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(WEST).append(" ").append(
				Double.toString(west)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(ROWS).append(" ")
				.append(rowsString).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(COLS).append(" ").append(
				columnsString).append(newline).toString());

	}

	public String getNoDataMarker() {
		return NO_DATA_MARKER;
	}
}
