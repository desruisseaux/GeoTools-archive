package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4File;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReaderSpi;
import it.geosolutions.imageio.plugins.jhdf.HDFUtilities;
import it.geosolutions.imageio.plugins.jhdf.aps.APSProperties;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageReader;

import ncsa.hdf.hdflib.HDFException;

public class TOVSImageReaderSpi extends AbstractHDFImageReaderSpi {

	static final String[] suffixes = { "hdf" };

	static final String[] formatNames = { "HDF", "HDF4", "HDF5" };

	static final String[] mimeTypes = { "image/hdf" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = { null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSStreamMetadata_1.0";

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = null;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public TOVSImageReaderSpi() {
		super(
				vendorName,
				version,
				formatNames,
				suffixes,
				mimeTypes,
				readerCN, // readerClassName
				STANDARD_INPUT_TYPE,
				wSN, // writer Spi Names
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

	public boolean canDecodeInput(Object input) throws IOException {
		synchronized (spiMutex) {
			boolean found = false;
			if (input instanceof FileImageInputStreamExtImpl) {
				input = ((FileImageInputStreamExtImpl) input).getFile();
			}
			if (input instanceof File) {
				try {
					final String filepath = ((File) input).getPath();
					final H4File h4File = new H4File(filepath);
					if (h4File != null) {
						final H4SDSCollection sdscoll = h4File
								.getH4SdsCollection();
						if (sdscoll != null) {
							final int numAttributes = sdscoll
									.getNumAttributes();
							if (numAttributes != 0) {
								for (int i = 0; i < numAttributes; i++) {
									H4Attribute attrib = sdscoll
											.getAttribute(i);
									if (attrib != null) {
										final String attName = attrib.getName();
										if (attName
												.startsWith("File Description")) {
											final String value = HDFUtilities
													.buildAttributeString(attrib);
											if (value
													.contains("TOVS PATHFINDER")) {
												found = true;
												break;
											}
										}

									}
								}

							}

						}
						h4File.close();
					}
				} catch (HDFException e) {
					found = false;
				}
			}
			return found;
		}
	}

	public ImageReader createReaderInstance(Object source) throws IOException {
		return new TOVSImageReader(this);
	}

	public String getDescription(Locale locale) {
		return new StringBuffer("TOVS Compliant HDF Image Reader, version ")
				.append(version).toString();
	}

}
