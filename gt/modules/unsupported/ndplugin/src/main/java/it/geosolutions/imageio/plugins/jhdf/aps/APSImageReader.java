package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

/**
 * Specific Implementation of the <code>AbstractHDFImageReader</code> needed to
 * work on HDF produced by the Navy's APS (Automated Processing System)
 * 
 * @author Romagnoli Daniele
 */
public class APSImageReader extends AbstractHDFImageReader {

	public APSImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/** The Products Dataset List contained within the APS File */
	private String[] productList;

	private APSImageMetadata imageMetadata;

	private APSStreamMetadata streamMetadata;

	private void checkImageIndex(int imageIndex) {
		// TODO: Implements the imageIndex coherency check

		// if (imageIndex < 0
		// || (!hasSubDatasets && imageIndex > 0)
		// || (hasSubDatasets && ((nSubdatasets == 0 && imageIndex > 0) ||
		// (nSubdatasets != 0 && (imageIndex > nSubdatasets))))) {
		//
		// // The specified imageIndex is not valid.
		// // Retrieving the valid image index range.
		// final int validImageIndex = hasSubDatasets ? nSubdatasets
		// : 0;
		// StringBuffer sb = new StringBuffer(
		// "Illegal imageIndex specified = ").append(imageIndex)
		// .append(", while the valid imageIndex");
		// if (validImageIndex > 0)
		// // There are N Subdatasets.
		// sb.append(" range should be (0,").append(validImageIndex - 1)
		// .append(")!!");
		// else
		// // Only the imageIndex 0 is valid.
		// sb.append(" should be only 0!");
		// throw new IndexOutOfBoundsException(sb.toString());
		// }
	}

	/**
	 * Retrieve APS main information.
	 * 
	 * @param root
	 * 
	 * @throws Exception
	 */
	protected void initializeProfile() throws Exception {
		
		final int nSDS = h4SDSCollection.getNumSDS();
		
		int subdatasetsNum = 0;
		final H4Attribute attrib = h4SDSCollection.getAttribute("prodList");
		if (attrib!=null){
			Object attribValue = attrib.getValues();
			byte[] bb = (byte[]) attribValue;
			final int size = bb.length;
			StringBuffer sb = new StringBuffer(size);
			for (int i = 0; i < size && bb[i] != 0; i++) {
				sb.append(new String(bb, i, 1));
			}
			final String values = sb.toString();
			String products[] = values.split(",");
			productList = refineProductList(products);
			subdatasetsNum = productList.length;
		}
				
		subDatasetsMap = new LinkedHashMap(subdatasetsNum);
		sourceStructure = new SourceStructure(subdatasetsNum);

		// Scanning all the datasets
		for (int i = 0; i < nSDS; i++) {
			final H4SDS sds = h4SDSCollection.getH4SDS(i);
			
				final String name = sds.getName();
				boolean added = false;
				for (int j = 0; j < subdatasetsNum; j++) {

					// Checking if the actual dataset is a product.
					if (name.equals(productList[j])) {
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
						sourceStructure.setSubDatasetSize(j, datasetSize);
						sourceStructure.setSubDatasetInfo(j, dsInfo);
						added=true;
						break;
					}
				}
				
				if(!added)
					sds.close();
		}
	}

	/**
	 * Reduces the product's list by removing not interesting ones. As an
	 * instance the dataset containing l2_flags will be not presented.
	 * 
	 * @param products
	 *            The originating <code>String</code> array containing the
	 *            list of products to be checked.
	 * @return A <code>String</code> array containing a refined list of
	 *         products
	 */
	private String[] refineProductList(String[] products) {
		final int inputProducts = products.length;
		int j = 0;
		final boolean[] accepted = new boolean[inputProducts];

		for (int i = 0; i < inputProducts; i++)
			if (isAcceptedItem(products[i])) {
				accepted[i] = true;
				j++;
			} else
				accepted[i] = false;
		if (j == inputProducts)
			return products;
		final String[] returnedProductsList = new String[j];
		j = 0;
		for (int i = 0; i < inputProducts; i++) {
			if (accepted[i])
				returnedProductsList[j++] = products[i];
		}
		return returnedProductsList;
	}

	protected boolean isAcceptedItem(String productName) {
		// if (attribName.endsWith("_flags"))
		// return false;
		if (APSProperties.apsProducts.getHDFProduct(productName) != null)
			return true;
		return false;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		if (imageMetadata == null)
			imageMetadata = new APSImageMetadata(sdInfo);
		return imageMetadata;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		return sourceStructure.getNSubdatasets();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (streamMetadata == null)
			streamMetadata = new APSStreamMetadata(h4SDSCollection);
		return streamMetadata;
	}

	public void dispose() {
		super.dispose();
		synchronized (mutex) {
			final Set set = subDatasetsMap.keySet();
			final Iterator setIt = set.iterator();

			// Cleaning HashMap
			while (setIt.hasNext()) {
//				Dataset ds = (Dataset) subDatasetsMap.get(setIt.next());
				// TODO:Restore original properties?
				// TODO: Close datasets
			}
			subDatasetsMap.clear();
		}
	}

	public void reset() {
		super.reset();
		streamMetadata = null;
		imageMetadata = null;
		productList = null;
	}

	protected int getBandNumberFromProduct(String productName) {
		return APSProperties.apsProducts.getHDFProduct(productName).getNBands();
	}

}
