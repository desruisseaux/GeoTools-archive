package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

/**
 * Specific Implementation of the <code>BaseHDFImageReader</code> needed to
 * work on HDF produced by the Navy's APS (Automated Processing System)
 * 
 * @author Romagnoli Daniele
 */
public class APSImageReader extends BaseHDFImageReader {

	protected APSImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/** The Products Dataset List contained within the APS File */
	private String[] productList;

	private LinkedHashMap subDatasetsMap;

	private APSImageMetadata imageMetadata;

	private APSStreamMetadata streamMetadata;

	/**
	 * Given a specified datasetIndex, returns the proper Dataset.
	 * 
	 * @param datasetIndex
	 *            an index specifying required coverage(subDataset).
	 * 
	 */
	protected Dataset retrieveDataset(int datasetIndex) {
		checkImageIndex(datasetIndex);
		return getDataset(datasetIndex);

	}

	private Dataset getDataset(int datasetIndex) {
		synchronized (mutex) {
			Set set = subDatasetsMap.keySet();
			Iterator it = set.iterator();
			for (int j = 0; j < datasetIndex; j++)
				it.next();
			return (Dataset) subDatasetsMap.get((String) it.next());
		}
	}

	private void checkImageIndex(int imageIndex) {
		//TODO: Implements the imageIndex coherency check
		
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
	private void initializeAPS(HObject root) throws Exception {
		//TODO: Add synchronization

		// Getting the Member List from the provided root
		final List membersList = ((Group) root).getMemberList();
		final Iterator metadataIt = root.getMetadata().iterator();

		int subdatasetsNum = 0;
		while (metadataIt.hasNext()) {
			// get the attribute
			final Attribute attrib = (Attribute) metadataIt.next();

			// Checking if the attribute is related to the products list
			if (attrib.getName().equalsIgnoreCase("prodList")) {
				Object valuesList = attrib.getValue();
				final String[] values = (String[]) valuesList;
				String products[] = values[0].split(",");
				products = refineProductList(products);
				productList = products;
				subdatasetsNum = products.length;
				subDatasetsMap = new LinkedHashMap(subdatasetsNum);
				break;
			}
		}

		final int listSize = membersList.size();
		sourceStructure = new SourceStructure(subdatasetsNum);

		// Scanning all the datasets
		for (int i = 0; i < listSize; i++) {
			final HObject member = (HObject) membersList.get(i);
			if (member instanceof ScalarDS) {
				final String name = member.getName();
				for (int j = 0; j < subdatasetsNum; j++) {

					// Checking if the actual dataset is a product.
					if (name.equals(productList[j])) {
						// Updating the subDatasetsMap map
						subDatasetsMap.put(name, member);

						// retrieving subDataset main properties
						// (Rank, dims, chunkSize)
						final int rank = ((Dataset) member).getRank();
						final long[] dims = ((Dataset) member).getDims();
						final long[] chunkSize = ((Dataset) member)
								.getChunkSize();

						final long[] subDatasetDims = new long[rank];
						final long[] subDatasetChunkSize = new long[rank];
						long datasetSize = 1;

						// copying values.
						for (int k = 0; k < rank; k++) {
							subDatasetDims[k] = dims[k];
							subDatasetChunkSize[k] = chunkSize[k];
							if (k >= 2)
								datasetSize *= dims[k];
						}

						// instantiating a SubDatasetInfo
						SubDatasetInfo dsInfo = new SubDatasetInfo(name, rank,
								subDatasetDims, subDatasetChunkSize);
						sourceStructure.setSubDatasetSize(j, datasetSize);
						sourceStructure.setSubDatasetInfo(j, dsInfo);
					}
				}
			}
		}
		if (subdatasetsNum > 1)
			sourceStructure.setHasSubDatasets(true);
	}

	/**
	 * Delete unrequired products.
	 * 
	 * @param products
	 *            <code>String</code>'s array containing products within the
	 *            APS source.
	 * @return
	 */
	private String[] refineProductList(String[] products) {
		final int productsNum = products.length;
		int i =0, j=0;
		for (;i<productsNum;i++)
			if (!products[i].endsWith("_flags"))
				j++;
		if (j==productsNum)
			return products;
		else{
			final String[] refinedList = new String[j];
			for (i=0,j=0;i<productsNum;i++)
				if(!products[i].endsWith("_flags"))
					refinedList[j++]=products[i];
			return refinedList;
		}
		
	}
	

	protected void initialize() throws IOException {
		super.initialize();
		try {
			initializeAPS(root);
		} catch (Exception e) {
			IOException ioe = new IOException(
					"Unable to Initialize data. Provided Input is not valid"
							+ e);
			ioe.initCause(e);
			throw ioe;
		}
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		if (imageMetadata == null)
			imageMetadata = new APSImageMetadata(retrieveDataset(imageIndex));
		return imageMetadata;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		return sourceStructure.getNSubdatasets();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (streamMetadata == null)
			streamMetadata = new APSStreamMetadata(root);
		return streamMetadata;
	}

	public void dispose() {
		super.dispose();
		final Set set = subDatasetsMap.keySet();
		final Iterator setIt = set.iterator();
		// Cleaning HashMap
		while (setIt.hasNext()) {
			Dataset ds = (Dataset) subDatasetsMap.get(setIt.next());
			// TODO:Restore original properties?
		}
		subDatasetsMap.clear();
	}

	public void reset() {
		super.reset();
		streamMetadata = null;
		imageMetadata = null;
		productList = null;
	}

}
