package org.geotools.gce.imageio.asciigrid.spi;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageWriter;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates an AsciiGridsImageReader if it is able to decode the input
 * provided.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class AsciiGridsImageWriterSpi extends ImageWriterSpi {
	static final String[] suffixes = { "asc", "gz" };

	static final String[] formatNames = { "Ascii ArcInfo", "Ascii GRASS",
			"arcGrid" };

	static final String[] MIMETypes = { "image/asc" };

	static final String version = "1.0";

	static final String writerCN = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageWriter";

	static final String vendorName = "TODO";

	// ReaderSpiNames
	static final String[] rSN = { "org.geotools.gce.imageio.asciigrid.AsciiGridsImageReaderSpi" };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadataFormat";

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	/**
	 * 
	 */
	public AsciiGridsImageWriterSpi() {
		super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN, // writer
				// class
				// name
				STANDARD_OUTPUT_TYPE, rSN, // reader spi names
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

	}

	/**
	 * 
	 * @see javax.imageio.spi.ImageWriterSpi#canEncodeImage(javax.imageio.ImageTypeSpecifier)
	 */
	public boolean canEncodeImage(ImageTypeSpecifier its) {
		// int dataType = its.getSampleModel().getDataType();

		// if (dataType != DataBuffer.TYPE_FLOAT) {
		// return false;
		// }
		//
		// int bands = its.getNumBands();
		//
		// return bands == 1;
		return true;
	}

	/**
	 * 
	 * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
	 */
	public ImageWriter createWriterInstance(Object extension)
			throws IOException {
		return new AsciiGridsImageWriter(this);
	}

	/**
	 * 
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "SPI for AsciiIMageWriter";
	}
}
