package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.HDFUtilities;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

public class TOVSImageReader extends AbstractHDFImageReader {
	private TOVSImageMetadata imageMetadata;

	public TOVSImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	protected int getBandNumberFromProduct(String productName) {
		return TOVSPathAProperties.tovsProducts.getHDFProduct(productName)
				.getNBands();
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		if (imageMetadata == null)
			imageMetadata = new TOVSImageMetadata(sdInfo);
		return imageMetadata;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		return sourceStructure.getNSubdatasets();
	}

	protected boolean isAcceptedItem(String productName) {
		if (TOVSPathAProperties.tovsProducts.getHDFProduct(productName) != null)
			return true;
		return false;
	}

	/**
	 * Initializing TOVS datasets properties.
	 * 
	 * @param root
	 * 
	 * @throws Exception
	 */
	protected void initializeProfile() throws Exception {

		// Getting the Member List from the provided root
		final int nSDS = h4SDSCollection.getNumSDS();
		int subDatasets = 0;

		// Mutex on the subDatasetMap and sourceStructure initialization
		subDatasetsMap = new LinkedHashMap(8);
		sourceStructure = new SourceStructure();

		// Scanning all the datasets
		for (int i = 0; i < nSDS; i++) {
			final H4SDS sds = h4SDSCollection.getH4SDS(i);
			H4Attribute attrib = sds.getAttribute("long_name");
			if (attrib != null) {
				final String name = HDFUtilities.buildAttributeString(attrib);

				if (isAcceptedItem(name)) {

					subDatasets++;
					// Updating the subDatasetsMap map
					subDatasetsMap.put(name, sds);

					// retrieving subDataset main properties
					// (Rank, dims, chunkSize)
					final int rank = sds.getRank();
					final int[] dims = sds.getDimSizes();
					final int[] chunkSize = sds.getChunkSizes();

					final int[] subDatasetDims = new int[rank];
					final int[] subDatasetChunkSize;
					int datasetSize = 1;

					// copying values to avoid altering dataset
					// fields.
					for (int k = 0; k < rank; k++) {
						subDatasetDims[k] = dims[k];

						// when rank > 2, X and Y are the last
						// 2 coordinates. As an instance, for a
						// 3D subdatasets, 3rd dimension has
						// index 0.
						if (k < rank - 2)
							datasetSize *= dims[k];
					}
					if (chunkSize != null) {
						subDatasetChunkSize = new int[rank];
						for (int k = 0; k < rank; k++)
							subDatasetChunkSize[k] = chunkSize[k];
					} else
						subDatasetChunkSize = null;
					final int dt = sds.getDatatype();

					// instantiating a SubDatasetInfo
					SubDatasetInfo dsInfo = new SubDatasetInfo(name, rank,
							subDatasetDims, subDatasetChunkSize, dt);
					sourceStructure
							.addSubDatasetProperties(dsInfo, datasetSize);

				}
			}
		}
	}
}
