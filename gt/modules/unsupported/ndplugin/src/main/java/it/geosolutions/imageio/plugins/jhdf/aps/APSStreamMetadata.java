package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFStreamMetadata;
import it.geosolutions.imageio.plugins.jhdf.HDFUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import ncsa.hdf.hdflib.HDFConstants;

import org.w3c.dom.Node;

public class APSStreamMetadata extends AbstractHDFStreamMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSStreamMetadata_1.0";

	// TODO: Provides to build a proper structure to get CP_Pixels, CP_Lines,
	// CP_Latitudes, CP_Longitudes information

	protected Map stdFileAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(11));

	protected Map stdTimeAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(9));

	protected Map stdSensorAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(13));

	protected Map fileInputParamAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(6));

	protected Map fileNavAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(7));

	protected Map fileInGeoCovAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(8));

	protected Map genericAttribMap = Collections
			.synchronizedMap(new LinkedHashMap(15));

	protected Map projectionMap = null;

	protected Map productsMap = null;

	protected String[] prodList = null;

	private final int[] mapMutex = new int[] { 0 };

	private int prodListNum;

	private String projectionDatasetName;

	public APSStreamMetadata() {
		super(false, nativeMetadataFormatName, null, null, null);
	}

	public APSStreamMetadata(H4SDSCollection root) {
		this();
		buildMetadata(root);
	}

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

		synchronized (mapMutex) {

			// /////////////////////////////////////////////////////////////
			// 
			// Attributes Node
			//
			// /////////////////////////////////////////////////////////////
			IIOMetadataNode attribNode = new IIOMetadataNode("Attributes");

			// /////////////////////////////////////////////////////////////
			// Standard APS Attributes
			// /////////////////////////////////////////////////////////////
			IIOMetadataNode stdNode = new IIOMetadataNode(
					"StandardAPSAttributes");

			// File Attributes
			IIOMetadataNode stdFaNode = buildAttributesNodeFromMap(
					stdFileAttribMap, "FileAttributes");
			stdNode.appendChild(stdFaNode);

			// Time Attributes
			IIOMetadataNode stdTaNode = buildAttributesNodeFromMap(
					stdTimeAttribMap, "TimeAttributes");
			stdNode.appendChild(stdTaNode);

			// Sensor Attributes
			IIOMetadataNode stdSaNode = buildAttributesNodeFromMap(
					stdSensorAttribMap, "SensorAttributes");
			stdNode.appendChild(stdSaNode);

			attribNode.appendChild(stdNode);

			// ////////////////////////////////////////////////////////////////
			// File Products Attributes
			// ////////////////////////////////////////////////////////////////
			IIOMetadataNode fpaNode = new IIOMetadataNode(
					"FileProductsAttributes");

			// Input Parameter Attributes
			IIOMetadataNode fpIpaNode = buildAttributesNodeFromMap(
					fileInputParamAttribMap, "InputParameterAttributes");
			fpaNode.appendChild(fpIpaNode);

			// Navigation Attributes
			IIOMetadataNode fpNaNode = buildAttributesNodeFromMap(
					fileNavAttribMap, "NavigationAttributes");
			fpaNode.appendChild(fpNaNode);

			// Input Geographical Coverage Attributes
			IIOMetadataNode fpIgcaNode = buildAttributesNodeFromMap(
					fileInGeoCovAttribMap,
					"InputGeographicalCoverageAttributes");
			fpaNode.appendChild(fpIgcaNode);

			attribNode.appendChild(fpaNode);

			// ////////////////////////////////////////////////////////////////
			// Generic Attributes
			// ////////////////////////////////////////////////////////////////
			IIOMetadataNode genericNode = buildAttributesNodeFromMap(
					genericAttribMap, "GenericAttributes");
			attribNode.appendChild(genericNode);
			root.appendChild(attribNode);

			IIOMetadataNode productsNode = new IIOMetadataNode("Products");
			productsNode.setAttribute("numberOfProducts", Integer
					.toString(prodListNum));

			final Set set = productsMap.keySet();
			final Iterator productsIt = set.iterator();

			while (productsIt.hasNext()) {
				IIOMetadataNode productNode = new IIOMetadataNode("Product");
				final String name = (String) productsIt.next();
				productNode.setAttribute("name", name);
				final ArrayList attribs = (ArrayList) productsMap.get(name);

				final Map pdsaAttribMap = (LinkedHashMap) attribs.get(0);
				IIOMetadataNode pdsaAttribNode = buildAttributesNodeFromMap(
						pdsaAttribMap, "ProductDatasetAttributes");
				productNode.appendChild(pdsaAttribNode);

				final Map genericAttribMap = (LinkedHashMap) attribs.get(1);
				IIOMetadataNode genericPdsaNode = buildAttributesNodeFromMap(
						genericAttribMap, "ProductGenericAttributes");
				productNode.appendChild(genericPdsaNode);

				productsNode.appendChild(productNode);
			}

			root.appendChild(productsNode);
			IIOMetadataNode referencingNode = new IIOMetadataNode("Referencing");
			IIOMetadataNode projectionNode = buildAttributesNodeFromMap(
					projectionMap, "Projection");

			referencingNode.appendChild(projectionNode);
			root.appendChild(referencingNode);
		}
		return root;
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

	public void buildMetadata(H4SDSCollection root) {
		try {
			final int numAttributes= root.getNumAttributes();

			// number of supported attributes
			final int nStdFileAttribMap = APSProperties.STD_FA_ATTRIB.length;
			final int nStdTimeAttribMap = APSProperties.STD_TA_ATTRIB.length;
			final int nStdSensorAttribMap = APSProperties.STD_SA_ATTRIB.length;
			final int nFileInputParamAttribMap = APSProperties.PFA_IPA_ATTRIB.length;
			final int nFileNavAttribMap = APSProperties.PFA_NA_ATTRIB.length;
			final int nFileInGeoCovAttribMap = APSProperties.PFA_IGCA_ATTRIB.length;

			synchronized (mapMutex) {
				for (int i=0;i<numAttributes;i++){
					// get Attributes
					final H4Attribute att = (H4Attribute) root.getAttribute(i);

					// get Attribute Name
					final String attribName = att.getName();
					final String attribValue = HDFUtilities
							.buildAttributeString(att);

					// checks if the attribute name matches one of the supported
					// attributes

					boolean found = false;
					for (int k = 0; k < nStdFileAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.STD_FA_ATTRIB[k])) {
							stdFileAttribMap.put((String) attribName,
									attribValue);
							found = true;
						}
					}

					for (int k = 0; k < nStdTimeAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.STD_TA_ATTRIB[k])) {
							stdTimeAttribMap.put((String) attribName,
									attribValue);
							found = true;
						}
					}

					for (int k = 0; k < nStdSensorAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.STD_SA_ATTRIB[k])) {
							stdSensorAttribMap.put((String) attribName,
									attribValue);
							found = true;
						}
					}

					for (int k = 0; k < nFileInputParamAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.PFA_IPA_ATTRIB[k])) {
							fileInputParamAttribMap.put((String) attribName,
									attribValue);
							if (attribName
									.equals(APSProperties.PFA_IPA_PRODLIST))
								prodList = attribValue.split(",");
							found = true;
						}
					}

					for (int k = 0; k < nFileNavAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.PFA_NA_ATTRIB[k])) {
							fileNavAttribMap.put((String) attribName,
									attribValue);
							if (attribName
									.equals(APSProperties.PFA_NA_MAPPROJECTION))
								projectionDatasetName = attribValue;
							found = true;
						}
					}

					for (int k = 0; k < nFileInGeoCovAttribMap && !found; k++) {
						// if matched
						if (attribName.equals(APSProperties.PFA_IGCA_ATTRIB[k])) {
							fileInGeoCovAttribMap.put((String) attribName,
									attribValue);
							found = true;
						}
					}

					if (!found)
						genericAttribMap.put((String) attribName, attribValue);
				}

				// //////////////////////////////////
				// Retrieving products metadata
				// //////////////////////////////////

				prodListNum = prodList.length;
				if (productsMap == null)
					productsMap = Collections
							.synchronizedMap(new LinkedHashMap(prodListNum));

				final int numSDS = root.getNumSDS();
				for (int i = 0; i < numSDS; i++) {
					H4SDS sds = root.getH4SDS(i);
					final String name = sds.getName();
					boolean productFound = false;
					for (int j = 0; j < prodListNum && !productFound; j++) {
						if (name.equals(prodList[j])) {
							productFound = true;

							final int sdsAttributes = sds.getNumAttributes();
							if (sdsAttributes != 0) {
								

								final int nPdsAttrib = APSProperties.PDSA_ATTRIB.length;
								final LinkedHashMap pdsAttribMap = new LinkedHashMap(
										nPdsAttrib);
								final LinkedHashMap pdsGenericAttribMap = new LinkedHashMap(
										10);

								for (int indexAttr=0;indexAttr<sdsAttributes;indexAttr++) {

									// get Attributes
									final H4Attribute att = (H4Attribute) sds.getAttribute(indexAttr);

									// get Attribute Name
									final String attribName = att.getName();

									// get Attribute Value
									final String attribValue = HDFUtilities
											.buildAttributeString(att);
									boolean attributeFound = false;
									for (int k = 0; k < nPdsAttrib
											&& !attributeFound; k++) {
										// if matched
										if (attribName
												.equals(APSProperties.PDSA_ATTRIB[k])) {
											// putting the <attribut Name,
											// attribute value>
											// couple in the map
											pdsAttribMap.put(
													(String) attribName,
													attribValue);

											attributeFound = true;
										}
									}

									if (!attributeFound) {
										// putting the <attribut Name, attribute
										// value>
										// couple in the map
										pdsGenericAttribMap.put(
												(String) attribName,
												attribValue);

									}

								}

								final ArrayList productAttribs = new ArrayList(
										2);
								// putting pdsaAttribMap and genericAttribMap
								// in the arrayList
								productAttribs.add(pdsAttribMap);
								productAttribs.add(pdsGenericAttribMap);
								productsMap.put(name, productAttribs);
							}
						}
					}

					if (!productFound && name.equals(projectionDatasetName)) {
						// TODO: All projection share the same dataset
						// structure??
						Object data = sds.read();
						final int datatype = sds
								.getDatatype();
						if (projectionMap == null)
							projectionMap = buildMedeastProjectionAttributesMap(
									data, datatype);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block

		}
	}

	private Map buildMedeastProjectionAttributesMap(final Object data,
			int datatype) {
		final Map projMap = Collections.synchronizedMap(new LinkedHashMap(29));

		if (datatype == HDFConstants.DFNT_FLOAT64) {
			double[] values = (double[]) data;
			// synchronized (projMap) {
			// TODO: I need to build a parser or a formatter to properly
			// interprete
			// these settings
			projMap.put("Code", Double.toString(values[0]));
			projMap.put("Projection", Double.toString(values[1]));
			projMap.put("Zone", Double.toString(values[2]));
			projMap.put("Datum", Double.toString(values[3]));
			projMap.put("Param0", Double.toString(values[4]));
			projMap.put("Param1", Double.toString(values[5]));
			projMap.put("Param2", Double.toString(values[6]));
			projMap.put("Param3", Double.toString(values[7]));
			projMap.put("Param4", Double.toString(values[8]));
			projMap.put("Param5", Double.toString(values[9]));
			projMap.put("Param6", Double.toString(values[10]));
			projMap.put("Param7", Double.toString(values[11]));
			projMap.put("Param8", Double.toString(values[12]));
			projMap.put("Param9", Double.toString(values[13]));
			projMap.put("Param10", Double.toString(values[14]));
			projMap.put("Param11", Double.toString(values[15]));
			projMap.put("Param12", Double.toString(values[16]));
			projMap.put("Param13", Double.toString(values[17]));
			projMap.put("Param14", Double.toString(values[18]));
			projMap.put("Width", Double.toString(values[19]));
			projMap.put("Height", Double.toString(values[20]));
			projMap.put("Longitude_1", Double.toString(values[21]));
			projMap.put("Latitude_1", Double.toString(values[22]));
			projMap.put("Pixel_1", Double.toString(values[23]));
			projMap.put("Line_1", Double.toString(values[24]));
			projMap.put("Longitude_2", Double.toString(values[25]));
			projMap.put("Latitude_2", Double.toString(values[26]));
			projMap.put("Delta", Double.toString(values[27]));
			projMap.put("Aspect", Double.toString(values[28]));
			// }
		}
		return projMap;
	}
}
