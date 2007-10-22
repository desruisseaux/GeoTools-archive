package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4DecoratedObject;
import it.geosolutions.hdf.object.h4.H4File;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;

import ncsa.hdf.hdflib.HDFException;

public class APSImageReaderSpi extends AbstractHDFImageReaderSpi {

	/**
	 * The list of the required attributes was built in compliance with the
	 * information available at:
	 * http://www7333.nrlssc.navy.mil/docs/aps_v3.4/user/aps/ch06.html
	 * 
	 */
	private final static String[] requiredAPSAttributes = { "file",
			"fileClassification", "fileStatus", "fileTitle", "fileVersion",
			"createAgency", "createSoftware", "createPlatform", "createTime",
	/* "createUser" */}; // TODO: Add more attributes??

	static final String[] suffixes = { "hdf" };

	static final String[] formatNames = { "HDF", "HDF4", "HDF5" };

	static final String[] mimeTypes = { "image/hdf" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.aps.APSImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = { null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFStreamMetadata_1.0";

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = null;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public APSImageReaderSpi() {
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
								H4Attribute attrib = sdscoll
										.getAttribute(APSProperties.STD_FA_CREATESOFTWARE);
								if (attrib != null) {
									final Object attValue = attrib.getValues();
									byte[] bb = (byte[]) attValue;
									final int size = bb.length;
									StringBuffer sb = new StringBuffer(size);
									for (int i = 0; i < size && bb[i] != 0; i++) {
										sb.append(new String(bb, i, 1));
									}
									final String value = sb.toString();
									if (value.startsWith("APS")) {
										found = true;
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

	/**
	 * Returns an instance of the APSImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new APSImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return new StringBuffer("APS Compliant HDF Image Reader, version ")
				.append(version).toString();
	}

}