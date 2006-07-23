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

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReaderSpi;
import org.geotools.gce.imageio.asciigrid.LoggerController;

/**
 * Class used to handle an ASCII ArcGrid format source.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 */
public final class EsriAsciiGridRaster extends AsciiGridRaster {
	private static final Logger logger = Logger
			.getLogger(EsriAsciiGridRaster.class.toString());

	public static final String NO_DATA_MARKER = "-9999";

	/** Column number tag in the header file */
	public static final String NCOLS = "NCOLS";

	/** Row number tag in the header file */
	public static final String NROWS = "NROWS";

	/** xll corner coordinate tag in the header file */
	public static final String XLLCORNER = "XLLCORNER";

	/** yll corner coordinate tag in the header file */
	public static final String YLLCORNER = "YLLCORNER";

	/** xll center coordinate tag in the header file */
	public static final String XLLCENTER = "XLLCENTER";

	/** yll center coordinate tag in the header file */
	public static final String YLLCENTER = "YLLCENTER";

	/** cell size tag in the header file */
	public static final String CELLSIZE = "CELLSIZE";

	/** no data tag in the header file */
	public static final String NODATA_VALUE = "NODATA_VALUE";

	private String noDataMarker;

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 * @param spi 
	 */
	public EsriAsciiGridRaster(ImageInputStream iis, AsciiGridsImageReaderSpi spi) {
		super(iis);
	}

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public EsriAsciiGridRaster(ImageInputStream ios) {
		super(ios);
	}
	public EsriAsciiGridRaster(ImageInputStream ios,AsciiGridsImageReader reader) {
		super(ios,reader);
	}
	/**
	 * Parses the header for the known properties.
	 * 
	 * @throws IOException
	 *             for reading errors
	 */
	public void parseHeader() throws IOException {
		///////////////////////////////////////////////////////////////////////
		//
		//  This is the ArcInfo ASCII Grid Format
		//  nrows XX
		//  ncols XX
		//  xllcorner | xllcenter XX
		//  yllcorner | yllcenter XX
		//  cellsize XX
		//  NODATA_value XX (Optional)
		//  XX XX XX XX... (DATA VALUES)
		//
		///////////////////////////////////////////////////////////////////////
		if (LoggerController.enableLoggerAsciiGridRaster) {
			logger.info("Header Parsed: ");
		}

		boolean keepParsing = true;
		imageIS.mark();

		int i = 0;
		int requiredFields = 0;
		long headerEnd = -1;
		String s = null;
		String sKey = null;
		boolean cornerInitialized = false;

		// if in the header there is a field (like ncols, nrows) not followed by
		// numbers, parseInt or parseDouble throws a NumberFormatException

		///////////////////////////////////////////////////////////////////////
		// Parsing the header
		///////////////////////////////////////////////////////////////////////
		while (keepParsing && ((s = imageIS.readLine()) != null)) {
			i = s.indexOf(" ");

			if (i != -1) {
				sKey = s.substring(0, i);
				s = (s.substring(i)).trim();

				if (NCOLS.equalsIgnoreCase(sKey)) {
					nCols = Integer.parseInt(s);
					requiredFields++;

					continue;
				}

				if (NROWS.equalsIgnoreCase(sKey)) {
					nRows = Integer.parseInt(s);
					requiredFields++;

					continue;
				}

				if (XLLCORNER.equalsIgnoreCase(sKey)) {
					xllCellCoordinate = Double.parseDouble(s);

					if (!cornerInitialized) {
						isCorner = true;
						cornerInitialized = true;
					}

					requiredFields++;

					continue;
				}

				if (YLLCORNER.equalsIgnoreCase(sKey)) {
					yllCellCoordinate = Double.parseDouble(s);
					requiredFields++;

					continue;
				}

				if (XLLCENTER.equalsIgnoreCase(sKey)) {
					xllCellCoordinate = Double.parseDouble(s);

					if (!cornerInitialized) {
						isCorner = false;
						cornerInitialized = true;
					}
					requiredFields++;

					continue;
				}

				if (YLLCENTER.equalsIgnoreCase(sKey)) {
					yllCellCoordinate = Double.parseDouble(s);
					requiredFields++;

					continue;
				}

				if (CELLSIZE.equalsIgnoreCase(sKey)) {
					cellSizeX = cellSizeY = Double.parseDouble(s);
					requiredFields++;

					// NODATA_VALUE is optional. If not present, Data Values
					// could start from the next line
					// (This happens when there is not an empty line between
					// header and data values)
					// So, I get current stream position as precaution
					headerEnd = imageIS.getStreamPosition();

					continue;
				}

				if (NODATA_VALUE.equalsIgnoreCase(sKey)) {

					noData = Double.parseDouble(s);
					requiredFields++;

					// if optional NODATA_VALUE is present, I'm sure that only
					// data value can exists after it... So, no more header
					// fields can exist.
					dataStartAt = imageIS.getStreamPosition();
					keepParsing = false;
				} else {
					keepParsing = false;
				}
			} else {
				// probably, is a new empty line. Maybe, next data could be
				// valid data values.
				// (for example, optional field NODATA_VALUE is not present)
				// so, I get current stream position as precaution
				headerEnd = imageIS.getStreamPosition();
				keepParsing = false;
			}
		}

		///////////////////////////////////////////////////////////////////////
		//
		// Checking if each required header field was found
		//
		///////////////////////////////////////////////////////////////////////
		if (requiredFields < 5) {
			// The Header is not compliant with the ArcInfo ASCII format.
			// Before checking if the Header is compliant to GRASS format, i
			// need to reset the stream
			imageIS.reset();
			throw new IOException("Header not identified");
		} else {
			if (dataStartAt == -1) {
				dataStartAt = headerEnd;
			} else {
				headerEnd = dataStartAt;
			}

			// Next char can be a kind of whitespace or the first digit of a
			// data value. Reading methods need to know where useful data
			// value begins. I move the stream position to the first useful
			// data value.
			imageIS.reset();
			imageIS.mark();
			imageIS.seek(dataStartAt);

			// stop reading when character is not a white space
			int character = imageIS.read();
			while ((character == 32) || (character == 9) || (character == 10)
					|| (character == 13)) {
				headerEnd++;
				character = imageIS.read();
			}
			dataStartAt = headerEnd;
		}

		if (LoggerController.enableLoggerAsciiGridRaster) {
			logger.info("\tnCols:" + nCols);
			logger.info("\tnRows:" + nRows);
		}

		imageIS.reset();
	}

	/**
	 * This method provides the header writing
	 * 
	 * @param columns
	 *            A String representing the number of columns
	 * @param rows
	 *            A String representing the number of rows
	 * @param xll
	 *            A String representing the xllCellCoordinate of the Bounding
	 *            Box
	 * @param yll
	 *            A String representing the yllCellCoordinate of the Bounding
	 *            Box
	 * @param cellsizeX
	 *            A String representing the x size of the grid cell
	 * @param cellsizeY
	 *            A String representing the Y size of the grid cell
	 * @param rasterSpaceType
	 *            A string representing if xll is xllCorner or xllCenter
	 * @param noDataValue
	 *            A String representing the optional NoData value
	 * @throws IOException
	 *             if a writing error occurs
	 */
	public void writeHeader(String columns, String rows, String xll,
			String yll, String cellsizeX, String cellsizeY,
			String rasterSpaceType, String noDataValue) throws IOException {
		imageOS.writeBytes(new StringBuffer(NCOLS).append(" ").append(columns)
				.append(newline).toString());
		imageOS.writeBytes(new StringBuffer(NROWS).append(" ").append(rows)
				.append(newline).toString());

		if (rasterSpaceType.equalsIgnoreCase("pixelIsArea")) {
			imageOS.writeBytes(new StringBuffer(XLLCENTER).append(" ").append(
					xll).append(newline).toString());
			imageOS.writeBytes(new StringBuffer(YLLCENTER).append(" ").append(
					yll).append(newline).toString());
		} else {
			imageOS.writeBytes(new StringBuffer(XLLCORNER).append(" ").append(
					xll).append(newline).toString());
			imageOS.writeBytes(new StringBuffer(YLLCORNER).append(" ").append(
					yll).append(newline).toString());
		}

		imageOS.writeBytes(new StringBuffer(CELLSIZE).append(" ").append(
				cellsizeX).append(newline).toString());

		// remember the no data value is optional
		if (noDataValue != null) {

			// we need to extend the reader to read Nan and Inf
			imageOS
					.writeBytes(new StringBuffer(NODATA_VALUE)
							.append(" ")
							.append(
									noDataValue.equalsIgnoreCase("nan") ? NO_DATA_MARKER
											: noDataValue).append(newline)
							.toString());
			// setting the no data value
			noData = Double.parseDouble(noDataValue);
		}
	}
	
	/**
	 * This method returns the noDataMarker
	 * returns 
	 * 		the noDataMarker
	 */

	public String getNoDataMarker() {
		if (noDataMarker == null) {

			noDataMarker = !Double.isNaN(noData) ? Double.toString(noData)
					: NO_DATA_MARKER;
		}
		return noDataMarker;
	}
}
