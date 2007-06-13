package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;

import org.w3c.dom.Node;

public class APSHDFImageMetadata extends BaseHDFImageMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	protected final int[] mapMutex = new int[1];
	
	/**
	 * private fields for metadata node building
	 */

	protected Map attributesMap = Collections.synchronizedMap(new HashMap(19));

	public Node getAsTree(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return getNativeTree();
		} else {
			throw new IllegalArgumentException("Not a recognized format!");
		}
	}

	private Node getNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);
		/**
		 * Setting Dataset Properties common to any sub-format
		 */
		
		root.appendChild(getCommonDatasetNode());
		
		synchronized (mapMutex) {
			
			/**
			 * Setting Product Dataset Attributes
			 */
	
			IIOMetadataNode pdsaNode = new IIOMetadataNode(
					"ProductDataSetAttributes");
			pdsaNode.setAttribute(APSAttributes.PDSA_CREATESOFTWARE, 
					attributesMap.containsKey(APSAttributes.PDSA_CREATESOFTWARE)?
					(String) attributesMap.get(APSAttributes.PDSA_CREATESOFTWARE):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_CREATETIME, 
					attributesMap.containsKey(APSAttributes.PDSA_CREATETIME)?
					(String) attributesMap.get(APSAttributes.PDSA_CREATETIME):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_CREATEPLATFORM, 
					attributesMap.containsKey(APSAttributes.PDSA_CREATEPLATFORM)?
							(String) attributesMap.get(APSAttributes.PDSA_CREATEPLATFORM):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTNAME, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTNAME)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTNAME):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTALGORITHM, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTALGORITHM)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTALGORITHM):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTUNITS, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTUNITS)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTUNITS):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTVERSION, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTVERSION)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTVERSION):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTTYPE,
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTTYPE)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTTYPE):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_ADDITIONALUNITS,
					attributesMap.containsKey(APSAttributes.PDSA_ADDITIONALUNITS)?
							(String) attributesMap.get(APSAttributes.PDSA_ADDITIONALUNITS):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTSTATUS, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTSTATUS)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTSTATUS):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_VALIDRANGE, 
					attributesMap.containsKey(APSAttributes.PDSA_VALIDRANGE)?
							(String) attributesMap.get(APSAttributes.PDSA_VALIDRANGE):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_INVALID, 
					attributesMap.containsKey(APSAttributes.PDSA_INVALID)?
							(String) attributesMap.get(APSAttributes.PDSA_INVALID):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTSCALING, 
					attributesMap.containsKey(APSAttributes.PDSA_PRODUCTSCALING)?
							(String) attributesMap.get(APSAttributes.PDSA_PRODUCTSCALING):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_SCALINGSLOPE, 
					attributesMap.containsKey(APSAttributes.PDSA_SCALINGSLOPE)?
							(String) attributesMap.get(APSAttributes.PDSA_SCALINGSLOPE):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_SCALINGINTERCEPT, 
					attributesMap.containsKey(APSAttributes.PDSA_SCALINGINTERCEPT)?
							(String) attributesMap.get(APSAttributes.PDSA_SCALINGINTERCEPT):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_BROWSEFUNC, 
					attributesMap.containsKey(APSAttributes.PDSA_BROWSEFUNC)?
							(String) attributesMap.get(APSAttributes.PDSA_BROWSEFUNC):"");
			
			pdsaNode.setAttribute(APSAttributes.PDSA_BROWSERANGES, 
					attributesMap.containsKey(APSAttributes.PDSA_BROWSERANGES)?
							(String) attributesMap.get(APSAttributes.PDSA_BROWSERANGES):"");
			
			root.appendChild(pdsaNode);
		}

		return root;
	}

	public APSHDFImageMetadata(Dataset dataset) {
		this();
		initializeFromDataset(dataset);
	}

	public APSHDFImageMetadata() {
		super(
				false,
				nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadataFormat",
				null, null);
	}

	/**
	 * Initialize Metadata from a raster
	 * 
	 * @param raster
	 *            the <code>SwanRaster</code> from which retrieve data
	 * @param imageIndex
	 *            the imageIndex relying the required subdataset
	 */
	private void initializeFromDataset(final Dataset dataset) {
		if (dataset == null)
			return;

		//Initializing common properties to each HDF dataset.
		initializeCommonDatasetProperties(dataset);
		
		// TODO: Add syncronization
		Iterator metadataIt;
		try {
			metadataIt = dataset.getMetadata().iterator();
			
			//number of supported attributes
			final int attribNumber = APSAttributes.PDSA_ATTRIB.length;
			synchronized (mapMutex) {
				while (metadataIt.hasNext()) {
	
					// get Attributes
					final Attribute att = (Attribute) metadataIt.next();
	
					// get Attribute Name
					final String attribName = att.getName();
					
					// checks if the attribute name matches one of the supported
					// attributes
					for (int k=0; k<attribNumber;k++){
						//if matched
						if (attribName.equals(APSAttributes.PDSA_ATTRIB[k])){
							
							final String attribValue=APSAttributes.buildAttributeString(att);
							//putting the <attribut Name, attribute value> couple
							//in the map
							attributesMap.put((String)attribName, attribValue);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}

	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// TODO Auto-generated method stub

	}

	public void reset() {
		// TODO Auto-generated method stub

	}

}
