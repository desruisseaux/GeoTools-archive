package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

	/** The Dataset List contained within this APS File */

	private String[] productList;

	private HashMap subDatasets;

	private APSImageMetadata imageMetadata;
	private APSStreamMetadata streamMetadata;
	
	
	/**
	 * Retrieve APS main information
	 * 
	 * @param root
	 * @throws Exception
	 */
	private void initializeAPS(HObject root) throws Exception {

		// Getting the Member List from the provided root
		final List membersList = ((Group) root).getMemberList();
		final Iterator metadataIt = root.getMetadata().iterator();

		int subdatasetsNum=0;
		while (metadataIt.hasNext()) {
			// get the attribute
			final Attribute att = (Attribute) metadataIt.next();

			// Checking if the attribute is related to the products list
			if (att.getName().equalsIgnoreCase("prodList")) {
				Object valuesList = att.getValue();
				final String[] values = (String[]) valuesList;
				final String products[] = values[0].split(",");
				productList = products;
				subdatasetsNum = products.length;
				subDatasets = new HashMap(subdatasetsNum);
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
					if (name.equals(productList[j])) {
						subDatasets.put(name, member);
						final int rank = ((Dataset)member).getRank();
						final long[] dims = ((Dataset)member).getDims();
						final long[] chunkSize = ((Dataset)member).getChunkSize();
						
						final long[] subDatasetDims = new long[rank];
						final long[] subDatasetChunkSize = new long[rank];
						long datasetSize = 1;
						for (int k = 0; k < rank; k++) {
							subDatasetDims[k]=dims[k];
							subDatasetChunkSize[k]=chunkSize[k];
							if(k>=2)
								datasetSize *= dims[k];
						}
						SubDatasetInfo dsInfo= new SubDatasetInfo(name,rank,subDatasetDims,subDatasetChunkSize);
						//TODO: Need to set Bands!
						sourceStructure.setSubDatasetInfo(j, dsInfo);
					}
				}
			}
		}
		if (subdatasetsNum > 1)
			sourceStructure.setHasSubDatasets(true);
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

}
