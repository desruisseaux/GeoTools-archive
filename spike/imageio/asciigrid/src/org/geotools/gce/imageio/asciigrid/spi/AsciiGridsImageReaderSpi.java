package org.geotools.gce.imageio.asciigrid.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader;
import org.geotools.gce.imageio.asciigrid.LoggerController;
import org.geotools.gce.imageio.asciigrid.raster.AsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.EsriAsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.GrassAsciiGridRaster;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates an AsciiGridsImageReader if it is able to decode the input
 * provided.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public class AsciiGridsImageReaderSpi extends ImageReaderSpi {
	private static final Logger logger = Logger
			.getLogger(AsciiGridsImageReaderSpi.class.toString());

	static final String[] suffixes = { "asc", "arx" };

	static final String[] formatNames = { "Ascii ArcInfo", "Ascii GRASS" };

	static final String[] MIMETypes = { "image/asc", "image/arx" };

	static final String version = "1.0";

	static final String readerCN = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader";

	static final String vendorName = " TODO";

	// writerSpiNames
	static final String[] wSN = { "org.geotools.gce.imageio.asciigrid.AsciiGridsImageWriterSpi" };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = true;

	static final String nativeImageMetadataFormatName = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadataFormat";

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public AsciiGridsImageReaderSpi() {
		super(vendorName, version, formatNames, suffixes, MIMETypes, readerCN, // readerClassName
				STANDARD_INPUT_TYPE, wSN, // writer Spi Names
				supportsStandardStreamMetadataFormat,
				nativeStreamMetadataFormatName,
				nativeStreamMetadataFormatClassName,
				extraStreamMetadataFormatNames,
				extraStreamMetadataFormatClassNames,
				supportsStandardImageMetadataFormat,
				nativeImageMetadataFormatName,
				nativeImageMetadataFormatClassName,
				extraImageMetadataFormatNames,
				extraImageMetadataFormatClassNames);

		if (LoggerController.enableLoggerSpi) {
			logger.info("Creating a SPI");
		}
	}

	/**
	 * This method check if the input source can be decoded by the reader
	 * provided by this specific subclass of ImageReaderSpi. Return true if the
	 * check was successfully passed input source type accepted and handled are
	 * String, File, Url, InputStream and ImageInputStream.
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object)
	 */
	public boolean canDecodeInput(Object input) throws IOException {
		if (LoggerController.enableLoggerSpi) {
			logger.info("canDecodeInput?");
		}
		if (input instanceof ImageInputStream)
			((ImageInputStream) input).mark();

		// /////////////////////////////////////////////////////////////////////
		// temp vars
		// /////////////////////////////////////////////////////////////////////
		AsciiGridRaster asciiRaster;
		ImageInputStream spiImageInputStream;
		boolean closeMe = false;// if the stream is opened here we need to close
		// it before leaving
		/**
		 * Checking input source types and creating an ImageInputStream from
		 * them, needed for header parsing
		 */

		// if input source is a string,
		// convert input from String to File
		if (input instanceof String) {
			input = new File((String) input);
			closeMe = true;
		}

		// if input source is an URL,
		// open an InputStream
		if (input instanceof URL) {
			final URL tempURL = (URL) input;
			if (tempURL.getProtocol().equalsIgnoreCase("file"))
				input = new File(URLDecoder.decode(tempURL.getFile(), "UTF8"));
			else
				input = ((URL) input).openStream();
			closeMe = true;

		}

		// if input source is a File,
		// convert input from File to FileInputStream
		if (input instanceof File) {
			input = ImageIO.createImageInputStream(input);
			closeMe = true;

		}

		// input source is it an InputStream?
		if (input instanceof InputStream) {

			/**
			 * ISSUE on the GZipped test.
			 * 
			 * Example A) final File f = new File("file.asc.gz"); final
			 * InputStream is = new FileInputStream(f); Iterator it =
			 * ImageIO.getImageReaders(stream);
			 * 
			 * Example B) final File f = new File("file.asc.gz"); final
			 * GZIPInputStream stream = new GZIPInputStream( new
			 * FileInputStream(f)); Iterator it =
			 * ImageIO.getImageReaders(stream);
			 * 
			 * if we use the code of the Example A instead of that of Example B,
			 * the parseHeader method (which works with ImageInputStreams) is
			 * unable to try to read the header because the stream is GZipped.
			 * 
			 * Thus, if we want to provide explicitly an InputStream (instead of
			 * a File) for a GZipped File, we need to use the code of the
			 * Example B which make use of the GZIPInputStream Subclass.
			 * 
			 */

			// creating an ImageInputStream from the InputStream
			// input = ImageInputStreamAdapter.getStream((InputStream) input);
		}

		// input source is it an ImageInputStream?
		if (input instanceof ImageInputStream) {
			((ImageInputStream) input).mark();
			// marking the initial stream
			// casting from object to ImageInputStream and setting
			// the imageInputStream
			spiImageInputStream = (ImageInputStream) input;
			// if (!(spiImageInputStream instanceof GZIPImageInputStream)) {
			//
			// try {
			// final ImageInputStream temp = new GZIPImageInputStream(
			// spiImageInputStream);
			// spiImageInputStream = temp;
			// } catch (IOException e) {
			// spiImageInputStream.reset();
			//
			// }
			// }
		} else {

			return false;
		}

		// /////////////////////////////////////////////////////////////////////
		// Now, I have an ImageInputStream and I can try to see if input can be
		// decoded by doing header parsing
		// /////////////////////////////////////////////////////////////////////
		try {

			// Header Parsing to check if it is an EsriAsciiGridRaster
			asciiRaster = new EsriAsciiGridRaster(spiImageInputStream);
			asciiRaster.parseHeader();
		} catch (IOException e) {
			try {

				// Header Parsing to know if it is a GrassAsciiGridRaster
				asciiRaster = new GrassAsciiGridRaster(spiImageInputStream);
				asciiRaster.parseHeader();
			} catch (IOException e2) {
				// Input cannot be decoded
				if (LoggerController.enableLoggerSpi) {
					logger
							.info("Unable to parse Header Succesfully. Format not recognized");
				}

				// /////////////////////////////////////////////////////////////////////
				// reset the stream
				// /////////////////////////////////////////////////////////////////////
				((ImageInputStream) input).reset();
				if (closeMe)
					spiImageInputStream.close();
				return false;
			}
		}
		// /////////////////////////////////////////////////////////////////////
		// reset the stream and close it if needed
		// /////////////////////////////////////////////////////////////////////
		if (input instanceof ImageInputStream)
			((ImageInputStream) input).reset();
		if (closeMe)
			spiImageInputStream.close();
		return true;
	}

	/**
	 * Returns an instance of the AsciiGridsImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new AsciiGridsImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "AsciiGrids Image Reader, version " + version;
	}

	public void dispose() {

	}
}
