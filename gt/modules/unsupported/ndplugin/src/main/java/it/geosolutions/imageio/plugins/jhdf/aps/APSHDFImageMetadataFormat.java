package it.geosolutions.imageio.plugins.jhdf.aps;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class APSHDFImageMetadataFormat extends IIOMetadataFormatImpl {

	public APSHDFImageMetadataFormat() {
		super(APSImageMetadata.nativeMetadataFormatName,
				IIOMetadataFormatImpl.CHILD_POLICY_ALL);
		
		addElement("DatasetProperties",
					APSImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
		addAttribute("DatasetProperties", "Name", DATATYPE_STRING, true, null);
		addAttribute("DatasetProperties", "Rank", DATATYPE_STRING, true, null);
		addAttribute("DatasetProperties", "Dims", DATATYPE_STRING, true, null);
		addAttribute("DatasetProperties", "ChunkSize", DATATYPE_STRING, true, null);

		addElement("ProductDataSetAttributes",
				APSImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
		//TODO: Should Set to false attributes required?
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_CREATESOFTWARE, DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_CREATETIME , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_CREATEPLATFORM , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTNAME , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTALGORITHM , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTUNITS , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTVERSION , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTTYPE  , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_ADDITIONALUNITS , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTSTATUS , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_VALIDRANGE  , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_INVALID , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_PRODUCTSCALING , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_SCALINGSLOPE , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_SCALINGINTERCEPT , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_BROWSEFUNC , DATATYPE_STRING, true, null);
        addAttribute("ProductDataSetAttributes", APSAttributes.PDSA_BROWSERANGES , DATATYPE_STRING, true, null);

        addElement("AdditionalAttributes",
        		APSImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("AdditionalAttributes", "additionals" , DATATYPE_STRING, false, null);
	}

	private static IIOMetadataFormat instance = null;

	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		// @todo @task TODO
		return true;
	}

}
