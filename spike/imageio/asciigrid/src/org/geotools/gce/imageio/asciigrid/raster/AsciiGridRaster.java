package org.geotools.gce.imageio.asciigrid.raster;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.TileFactory;
import javax.media.jai.iterator.RectIter;

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader;

/**
 * Abstract base class
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 */
public abstract class AsciiGridRaster {

	private final static Logger LOGGER = Logger.getLogger(AsciiGridRaster.class
			.toString());

	static final String newline = System.getProperty("line.separator");

	protected final int[] tileTreeMutex = new int[1];

	protected final int[] abortMutex = new int[1];

	/** max value found in the file */
	protected double maxValue = Float.MIN_VALUE;

	/** min value found in the file */
	protected double minValue = Float.MAX_VALUE;

	protected double xllCellCoordinate = Double.NaN;

	protected double yllCellCoordinate = Double.NaN;

	/** horizontal subsampling */
	protected int sourceXSubsampling = 1;

	/** vertical subsampling */
	protected int sourceYSubsampling = 1;

	/**
	 * The grid's origin (the lower left corner of the grid) can be specified
	 * using the coordinates of either its lower-left corner (by providing
	 * XLLCORNER and YLLCORNER) or the center of the lower-left grid cell
	 * (XLLCENTER and YLLCENTER)
	 */
	/**
	 * If isCorner is true then xllCellCoordinate is the coordinate of the
	 * lower-left corner of the grid. If isCorner is false then
	 * xllCellCoordinate is the coordinate of the center of the lower-left
	 * gridcell of the grid.
	 */
	protected boolean isCorner;

	protected double cellSizeX = Double.NaN;

	protected double cellSizeY = Double.NaN;

	protected int nCols = -1;

	protected int nRows = -1;

	protected boolean compress;

	/**
	 * A kind of Bookmark that point at the first byte in the stream after the
	 * header
	 */
	protected long dataStartAt = -1;

	/**
	 * A TreeMap used to Skip spaces-count operation when the image is tiled. If
	 * I need to load data values to fill the last tile, I need to skip a lot of
	 * samples before finding useful data values. This TreeMap couples 'number
	 * of spaces' to 'positions in the stream'. Thus, this search operation is
	 * accelerated.
	 */
	protected TreeMap tileMarker = new TreeMap();

	protected int tileWidth = -1;

	protected int tileHeight = -1;

	/** ImageInputStream used to read the source that contain data */
	protected ImageInputStream imageIS = null;

	/**
	 * ImageOutputStream used to write the raster to the device
	 * (file,stream,...)
	 */
	protected ImageOutputStream imageOS = null;

	protected double noData = Double.NaN;

	protected AsciiGridsImageReader reader;

	protected AsciiGridRaster(ImageInputStream iis) {
		imageIS = iis;
		abortMutex[0] = 0;

	}

	protected AsciiGridRaster(ImageInputStream iis, AsciiGridsImageReader reader) {
		this(iis);
		this.reader = reader;

	}

	protected AsciiGridRaster(ImageOutputStream ios) {
		imageOS = ios;
		abortMutex[0] = 0;
	}

	public abstract void parseHeader() throws IOException;

	public double getNoData() {
		return noData;
	}

	public abstract String getNoDataMarker();

	public abstract void writeHeader(String columns, String rows, String xll,
			String yll, String cellsizeX, String cellsizeY,
			String rasterSpaceType, String noDataValue) throws IOException;

	/**
	 * Max value.
	 * 
	 * @return the max value contained in the data file
	 */
	final public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Min value.
	 * 
	 * @return the min value contained in the data file
	 */
	final public double getMinValue() {
		return minValue;
	}

	/**
	 * Returns the number of rows contained in the file.
	 * 
	 * @return number of rows
	 */
	final public int getNRows() {
		return nRows;
	}

	/**
	 * Returns the number of columns contained in the file.
	 * 
	 * @return number of columns
	 */
	final public int getNCols() {
		return nCols;
	}

	/**
	 * Returns the x coordinate of the lower left grid corner or grid center.
	 * 
	 * @return x coordinate of the lower left grid corner or grid center.
	 */
	final public double getXllCellCoordinate() {
		return xllCellCoordinate;
	}

	/**
	 * Returns the y coordinate of the lower left grid corner or grid center.
	 * 
	 * @return y coordinate of the lower left grid corner or grid center.
	 */
	final public double getYllCellCoordinate() {
		return yllCellCoordinate;
	}

	/**
	 * This Method creates a TreeMap and initialize tile sizes
	 * 
	 * @param tileWidth
	 *            the tileWidth dimension
	 * @param tileHeight
	 *            the tileHeight dimension
	 */
	final public void setTilesData(int tileWidth, int tileHeight) {
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;

	}

	/**
	 * Returns the tileHeight
	 * 
	 * @return Returns the tileHeight.
	 */
	final public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * Returns the tileWidth
	 * 
	 * @return Returns the tileWidth.
	 */
	final public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Returns true if the Coordinates of the Lower-Left cell of the grid
	 * represent the coordinates of the lower-left corner. Returns false if
	 * coordinates are those of the center.
	 * 
	 * @return Returns isCorner.
	 */
	final public boolean isCorner() {
		return isCorner;
	}

	/**
	 * Returns the stream position where useful data starts (just after the end
	 * of the header)
	 * 
	 * @return dataStartAt
	 */
	final long getDataStartAt() {
		return dataStartAt;
	}

	/**
	 * This method reads data values from the ImageInputStream and returns a
	 * raster having these data values as samples. When image is tiled or
	 * reading is executed only on a specific part of the ASCII source, I need
	 * to determine which values must be loaded and which must be skipped.
	 * Within an ASCII source, I can't know how many digits compose a value.
	 * Thus, I need to scan and check every byte stored on the input source and
	 * retrieve the value as well as I need to skip values if they are useless.
	 * 
	 * @param param
	 *            an ImageReadParam which specifies source region properties as
	 *            width, height, x and y offsets.
	 * 
	 * @return WritableRaster the Raster composed by reading data values
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 *             TODO we ignore destination region, destinationOffset etc...
	 */
	public WritableRaster readRaster(ImageReadParam param) throws IOException {
		final WritableRaster raster;
//		System.out.println("");
//		System.out.println(Thread.currentThread().getName());
//		System.out.println(param.getSourceXSubsampling());
//		System.out.println(param.getSourceYSubsampling());
//		System.out.println(param.getSourceRegion().x);
//		System.out.println(param.getSourceRegion().y);
//		System.out.println(param.getSourceRegion().width);
//		System.out.println(param.getSourceRegion().height);
		
		// int perc=0;
		// int iPerc=1;
		int dstWidth = -1;
		int dstHeight = -1;
		int srcRegionWidth = -1;
		int srcRegionHeight = -1;
		int srcRegionXOffset = -1;
		int srcRegionYOffset = -1;
		int xSubsamplingFactor = -1;
		int ySubsamplingFactor = -1;
		boolean doSubsampling = false;

		// //////////////////////////////////////////////////////////////////////
		//
		//
		// STEP 1
		//
		// Retrieving Information about Source Region and doing
		// additional intialization operations.
		//
		//
		//
		// /////////////////////////////////////////////////////////////////////
		Rectangle srcRegion = param.getSourceRegion();
		if (srcRegion != null) {
			srcRegionWidth = (int) srcRegion.getWidth();
			srcRegionHeight = (int) srcRegion.getHeight();
			srcRegionXOffset = (int) srcRegion.getX();
			srcRegionYOffset = (int) srcRegion.getY();

			// //
			//
			// Minimum correction for wrong source regions
			//
			// When you do subsampling or source subsetting it might happen that
			// the given source region in the read param is uncorrect, which
			// means it can be or a bit larger than the original file or can
			// begin a bit before original limits.
			//
			// We got to be prepared to handle such case in order to avoid
			// generating ArrayIndexOutOFboundsException later in the code.
			//
			// //
			if (srcRegionXOffset < 0)
				srcRegionXOffset = 0;
			if (srcRegionYOffset < 0)
				srcRegionYOffset = 0;
			if ((srcRegionXOffset + srcRegionWidth) > nCols) {
				srcRegionWidth = nCols - srcRegionXOffset;
			}
			// initializing destWidth
			dstWidth = srcRegionWidth;

			if ((srcRegionYOffset + srcRegionHeight) > nRows) {
				srcRegionHeight = nRows - srcRegionYOffset;
			}
			// initializing dstHeight
			dstHeight = srcRegionHeight;

		} else {
			// Source Region not specified.
			// Assuming Source Region Dimension equal to Source Image Dimension
			dstWidth = nCols;
			dstHeight = nRows;
			srcRegionXOffset = srcRegionYOffset = 0;
			srcRegionWidth = nCols;
			srcRegionHeight = nRows;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();
		if ((xSubsamplingFactor > nCols) || (ySubsamplingFactor > nRows)) {
			throw new IOException(
					"The subSamplingFactor cannot be greater than image size!");
		}
		if (xSubsamplingFactor > 1 || ySubsamplingFactor > 1)
			doSubsampling = true;
		// ////////////////////////////////////////////////////////////////////////////
		//
		// I'm loading data to create a Raster needed for a Tile.
		// Thus, if the samples needed for the tile are not located immediatly
		// after the header, I need to find (and count) a defined number of
		// whitespaces (a withespace could be one of
		// {' ' , '\n' , '\r' , '\t' , "\r\n"})
		//
		// ////////////////////////////////////////////////////////////////////////////
		// total samples to scan
		// TODO srcRegionHeight*tileWidth - srcRegionXOffset???;
		final int samplesToLoad = srcRegionHeight * nCols;

		// //
		//
		// Updating the destination size in compliance with
		// the subSampling parameters
		//
		// //
		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		// Number of spaces to count before I find useful data
		final int samplesToThrowAwayBeforeFirstValidSample = (nCols * srcRegionYOffset);

		// Parameters needed to handle setSourceRegion Operations
		// final int srcDifference=srcWidth - srcRegionWidth;
		// final int spacesBetweenUsefulData =
		// (srcDifference>0)?srcDifference:0;
		// final boolean reducedWidth = (spacesBetweenUsefulData != 0) ? true:
		// false;
		final TileFactory factory = (TileFactory) JAI.getDefaultInstance()
				.getRenderingHint(JAI.KEY_TILE_FACTORY);
		if (factory != null)
			raster = factory.createTile(RasterFactory.createBandedSampleModel(
					java.awt.image.DataBuffer.TYPE_FLOAT, dstWidth, dstHeight,
					1), new Point(0, 0));
		else
			raster = RasterFactory.createBandedRaster(
					java.awt.image.DataBuffer.TYPE_FLOAT, dstWidth, dstHeight,
					1, null);

		int ch = -1;
		int prevCh = -1;
		int samplesCounted = 0;
		long streamPosition = 0;
		// /////////////////////////////////////////////////////////////////////
		//
		//
		//
		// STEP 2
		//
		// Searching Start of useful (for this Tile) data Values
		//
		//
		//
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// 2.A: Looking at a tile marker in order to accelerate the search
		//
		// //
		// I check if there is an useful entry in the TreeMap which
		// retrieves a stream position
		synchronized (tileTreeMutex) {
			Long markedPos = (Long) tileMarker.get(new Long(
					samplesToThrowAwayBeforeFirstValidSample));

			// Case 1: Exact key
			if (markedPos != null) {
		
				imageIS.seek(markedPos.intValue());
				samplesCounted = samplesToThrowAwayBeforeFirstValidSample;

				// I have found a stream Position associated to the number
				// of spaces that I need to count before finding useful data
				// values. Thus, I dont need to search furthermore

			} else {
				// Case 2: Nearest(Lower) Key
				SortedMap sm = tileMarker.headMap(new Long(
						samplesToThrowAwayBeforeFirstValidSample));

				if (!sm.entrySet().isEmpty()) {
					// searching the nearest key (belower)
					final Long key = (Long) sm.lastKey();

					// returning the stream position
					markedPos = (Long) tileMarker.get(key);

					if (markedPos != null) {
						// I have found a stream position related to a
						// number of white spaces smaller than the requested
						// number. Thus, I need to manually count the
						// remaining number of spaces.
						
						imageIS.seek( markedPos.intValue());
						samplesCounted = key.intValue();
					}
				} else {
					// positioning on the first data byte
					imageIS.seek(dataStartAt);
					samplesCounted = 0;// reinforcing
				}
			}

		}
		streamPosition=imageIS.getStreamPosition();
		// //
		//
		// Check abort request
		//
		// //
		synchronized (abortMutex) {
			if (abortMutex[0] == 1)
				return raster;

		}

		// //
		//
		// 2.B: Maybe I need to count some white space before reaching the
		// first useful data value or the tileMarker is empty
		//
		// //

		// If I dont need to search the first useful data value,
		// I Skip these operations.
		if (samplesCounted < samplesToThrowAwayBeforeFirstValidSample) {
			final int tileH = getTileHeight();
			final int tileW = getTileWidth();
			Long key;
			Long val;
			while (samplesCounted < samplesToThrowAwayBeforeFirstValidSample) {

				ch = imageIS.read(); // Filling the Buffer
				if (ch == -1)
					// send error on end of file
					throw new EOFException(
							"EOF found while looking for valid input");
				streamPosition++;

				// if(ch==-1) TODO check me
				// The nested "if" check, groups consecutive
				// whitespaces.
				// example: 3______4 => only 1 whitespace, not 6
				// (in the previous example, you have to substitute
				// underscores with spaces)
				// example: 3 /r/n /r/n 4 => only 1 whitespace, not 7
				if ((ch != 32) && (ch != 10) && (ch != 13) && (ch != 9)) {
					if ((prevCh == 32) || (prevCh == 10) || (prevCh == 13)
							|| (prevCh == 9)) {
						samplesCounted++;

						// Another space was counted. If the number of
						// spaces counted is multiple of the Size of
						// tile (tile Heigth*tile Width), I store a new
						// couple in the TreeMap. This is useful when I
						// dont load tiles in order. If, par example, I
						// load in advance data values which are related
						// to the last tile, I need to scan the whole
						// file and I need to counts a great number of
						// spaces. Thus, during this counting process, it is
						// useful to annotate stream positions in the
						// tileMarker
						if ((samplesCounted % (tileH * tileW)) == 0) {
							key = new Long(samplesCounted);
							val = new Long(streamPosition);
							synchronized (tileTreeMutex) {

								if (!tileMarker.containsKey(key)) {
									tileMarker.put(key, val);
								}
							}
						}

					}
				}

				prevCh = ch;

				// //
				//
				// Check abort request at every 10%
				//
				// //
				// perc=(int) (((samplesCounted * 1.0f) /
				// samplesToThrowAwayBeforeFirstValidSample) * 100);
				// if( (perc % (10*iPerc) == 0)&&(int)perc>0)
				// synchronized (abortMutex) {
				// if (abortMutex[0] == 1)
				// return raster;
				// iPerc++;
				//						
				//
				// }

			}

		
		}

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// STEP 3
		//
		//
		// Here starts the real samples loading. It's Time to read data
		// and convert Ascii bytes in floating numbers.
		//
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// 3.B: Variables initialization
		//
		// //
		// so far I have read samplesToThrowAwayBeforeValidArea samples, either
		// directly or using the cached
		// stream positions
		samplesCounted = 0;
		prevCh = -1;
		ch = -1;

		// I load values only when readRow is true
		// boolean readRow = true;

		// variables for arithmetic operations
		double value = 0.0;
		int valSign = 1;
		double exponent = 0;
		int expSign = 1;
		double multiplier = 0.1;

		// variables to prevent illegal numbers
		int expChar = 0; // count the E symbols within the same number
		int decimalPoint = 0; // count the '.' within the same number
		int digits = 0;
		int decimalDigits = 0;
		int expDigits = 0;

		// variables for raster setting
		int rasterX = 0;
		int rasterY = 0;

		int tempCol = 0, tempRow = 0;

		// If I need to load 10 samples, I need to count 9 spaces
		while (samplesCounted < samplesToLoad) {

			ch = imageIS.read();
			streamPosition++;

			// check if we read a white space or similar
			if ((ch != 32) && (ch != 10) && (ch != 13) && (ch != 9)
					&& (ch != 0)) {
				if ((prevCh == 32) // ' '
						|| (prevCh == 10) // '\r'
						|| (prevCh == 13) // '\n'
						|| (prevCh == 9) // '\t'
						|| (prevCh == 0)) {
					// //
					//
					// End of whitespaces. I need to convert read bytes
					// in a double value and I set it as a sample of the
					// raster, if subsampling allows it.
					//
					// //

					// //
					//
					// Checks on the read value
					//
					// //
					if (((decimalPoint == 1) && (decimalDigits == 0))
							|| ((expChar == 1) && (expDigits == 0))
							|| ((digits == 0) && (decimalDigits == 0) && (expDigits > 0))) {
						// Illegal numbers. Example: 14.E8 ****
						// 12.5E **** E10
						throw new NumberFormatException(
								"An Illegal number was found:\nDigits = "
										+ digits + "\nDecimalPoints = "
										+ decimalPoint + "\nDecimalDigits = "
										+ decimalDigits + "\nE Symbols = "
										+ expChar
										+ "\nDigits after E Symbol = "
										+ expDigits + "\n");
					}

					// //
					//
					// Does subsampling allow to add this value?
					//
					// //
					tempCol = samplesCounted % nCols;
					tempRow = samplesCounted / nCols;

					if (!((tempCol < srcRegionXOffset || tempCol >= srcRegionXOffset
							+ srcRegionWidth))) {

						if ((!doSubsampling)
								|| (doSubsampling && (((tempRow)
										% ySubsamplingFactor == 0) && ((tempCol)
										% xSubsamplingFactor == 0)))) {
							// If there is an exponent, I update the value
							if (exponent != 0) {
								value *= Math.pow(10.0, exponent * expSign);
							}

							// Applying the proper sign.
							value = value * valSign;

							// no data management
							if ((value != getNoData()) && !Double.isNaN(value)
									&& !Double.isInfinite(value)) {
								synchronized (tileTreeMutex) {
									minValue = Math.min(minValue, value);
									maxValue = Math.max(maxValue, value);
								}

							}

							// //
							//
							// We found a value, let's give it to the raster.
							//
							// //
							rasterY = (tempRow) / ySubsamplingFactor;
							rasterX = (tempCol - srcRegionXOffset)
									/ xSubsamplingFactor;
							raster
									.setSample(rasterX, rasterY, 0,
											(float) value);

						}
					}
					// sample found
					samplesCounted++;

					// //
					//
					// Check abort request at every 10%
					//
					// //
					// perc=(int) (((samplesCounted * 1.0f) /
					// samplesToThrowAwayBeforeFirstValidSample) * 100);
					// if( (perc % (10*iPerc) == 0)&&(int)perc>0)
					// synchronized (abortMutex) {
					// if (abortMutex[0] == 1)
					// return raster;
					// reader.processImageProgress(perc);
					// iPerc++;
					//							
					//
					// }

					// Resetting Values
					value = 0;
					valSign = 1;
					exponent = 0;
					expChar = 0;
					expDigits = 0;
					decimalPoint = 0;
					decimalDigits = 0;
					digits = 0;
				}

				// //
				//
				// Analysis of current byte for next value
				//
				// //
				switch (ch) {
				case 48: // '0'
				case 49: // '1'
				case 50: // '2'
				case 51: // '3'
				case 52: // '4'
				case 53: // '5'
				case 54: // '6'
				case 55: // '7'
				case 56: // '8'
				case 57: // '9'

					if ((decimalPoint == 0) && (expChar == 0)) {
						value = (value * 10) + (ch - 48);
						digits++;
					} else if (expChar == 1) {
						exponent = (exponent * 10) + (ch - 48);
						expDigits++;
					} else {
						value += ((ch - 48) * multiplier);
						multiplier /= 10.0;
						decimalDigits++;
					}

					break;

				case 46: // '.'

					if (expChar > 0) {
						throw new NumberFormatException(
								"A Decimal point can't exists after the E symbol within the same number\n");
					}

					decimalPoint++; // The "++" prevents invalid number

					if (decimalPoint > 1) {
						throw new NumberFormatException(
								"The number contains more than 1 decimal point!\n");
					}

					// Illegal Number handled example:
					// 12.3.45
					multiplier = 0.1;

					break;

				case 45: // '-'

					if ((prevCh == 69) || (prevCh == 101)) {
						expSign = -1;
					} else {
						valSign = -1;
					}

					break;

				case 43: // '+'

					if ((prevCh == 69) || (prevCh == 101)) {
						expSign = 1;
					} else {
						valSign = 1;
					}

					break;

				case 42: // '*' NoData in GRASS Format
					value = Double.NaN;

					break;

				case 69: // 'E'
				case 101: // 'e'
					expChar++; // The "++" prevents invalid number

					if (expChar > 1) {
						throw new NumberFormatException(
								"The number contains more than one 'E' symbol !\n");
					}

					// Illegal Number handled example:
					// 12.6E23E45
					exponent = 0;
					expSign = 1;
					expDigits = 0;

					break;

				case -1:
					if ((samplesCounted != samplesToLoad))
						// send error on end of file
						throw new EOFException(
								"EOF found while looking for valid input");
					break;

				default:
					throw new NumberFormatException(new StringBuffer(
							"Invalid data value was found. ASCII CODE : ")
							.append(ch).toString());
				}

			}

			prevCh = ch; // store this byte for some checks

		}

		synchronized (tileTreeMutex) {
			// The image support Tiling.
			// I put a couple in tileMarker: <spaces Counted>,<stream Position>.
			// <stream position> says how many bytes I have to read in the
			// stream before I have counted <spaces counted> spaces.
			// I can use this information to skip useless space searches
			// operations when I load Tiles not located at the beginning of the
			// stream
			// spacesMarker += ((bufferLoop > 0) ? ((bufferLoop * BUFFER_SIZE) +
			// i)
			// : i);
			tileMarker.put(new Long(samplesToLoad
					+ samplesToThrowAwayBeforeFirstValidSample), new Long(
					streamPosition));
		}

		return raster;
	}

	/**
	 * This method provides to write the raster
	 * 
	 * @param iterator
	 * @param noDataDouble
	 * @param noDataMarker
	 * @throws IOException
	 */

	public void writeRaster(RectIter iterator, Double noDataDouble,
			String noDataMarker) throws IOException {
		// iterator.startBands();
		// iterator.startLines();
		// iterator.startPixels();
		double sample;

		while (!iterator.finishedLines()) {
			while (!iterator.finishedPixels()) {
				sample = iterator.getSampleDouble();

				// writing the sample
				if ((noDataDouble.compareTo(new Double(sample)) != 0)
						&& !Double.isNaN(sample) && !Double.isInfinite(sample)) {
					imageOS.writeBytes(Double.toString(sample));
				} else {
					imageOS.writeBytes(noDataMarker);
				}

				imageOS.writeBytes(" ");

				iterator.nextPixel();
			}

			imageOS.writeBytes(newline);
			iterator.nextLine();
			iterator.startPixels();
		}
	}

	public int getSourceXSubsampling() {
		return sourceXSubsampling;
	}

	public int getSourceYSubsampling() {
		return sourceYSubsampling;
	}

	public final double getCellSizeX() {
		return cellSizeX;
	}

	public final double getCellSizeY() {
		return cellSizeY;
	}

	public void abort() {
		synchronized (abortMutex) {
			abortMutex[0] = 1;
		}

	}

	public boolean isAborting() {
		synchronized (abortMutex) {
			return abortMutex[0] == 1;
		}

	}

	public void clearAbort() {
		synchronized (abortMutex) {
			abortMutex[0] = 0;
		}

	}
}
