package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;

import java.util.Map;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a SDS Dimension.
 * 
 * @author Daniele Romagnoli
 */
public class H4Dimension extends H4Variable implements IHObject {

	/** predefined attributes */
	public static String PREDEF_ATTR_LABEL = "long_name";

	public static String PREDEF_ATTR_UNIT = "units";

	public static String PREDEF_ATTR_FORMAT = "format";

	/**
	 * <code>true</code> if a dimension scale is set for this dimension.
	 * <code>false</code> otherwise
	 */
	private boolean hasDimensionScaleSet = false;

	private int[] mutex = new int[] { 1 };

	/**
	 * The {@link H4SDS} to which this dimensions is related.
	 * 
	 * @uml.property name="h4SDS"
	 * @uml.associationEnd inverse="dimensionsList:it.geosolutions.hdf.object.h4.H4SDS"
	 */
	private H4SDS h4SDS;

	/**
	 * the ID of the SDS representing the dimension scale (if present) set for
	 * this dimension.
	 */
	private int sdsDimensionScaleID = HDFConstants.FAIL;

	/**
	 * the datatype of the dimension scale, if a dimension scale is set for this
	 * dimension. Otherwise, datatype is zero.
	 * 
	 * @uml.property name="datatype"
	 */
	private int datatype;

	/**
	 * The index of this dimension along the owner Scientific Dataset.
	 * 
	 * @uml.property name="index"
	 */
	private int index;

	/**
	 * The size of this dimension.
	 * 
	 * @uml.property name="size"
	 */
	private int size;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Getter of the property <code>index</code>
	 * 
	 * @return the index of this dimension.
	 * @uml.property name="index"
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Getter of the property <code>size</code>
	 * 
	 * @return the size of this dimension.
	 * @uml.property name="size"
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Getter of the property <code>datatype</code>.
	 * 
	 * @return the datatype. If a dimension scale is set for this dimension,
	 *         <code>datatype</code> is the data type of the dimension scale.
	 *         Otherwise, datatype is zero.
	 * @uml.property name="datatype"
	 */
	public int getDatatype() {
		return datatype;
	}

	/**
	 * Getter of the property <code>hasDimensionScaleSet</code>.
	 * 
	 * @return <code>true</code> if a dimension scale is set for this
	 *         dimension. <code>false</code> otherwise
	 * @uml.property name="hasDimensionScaleSet"
	 */
	public final boolean isHasDimensionScaleSet() {
		return hasDimensionScaleSet;
	}

	/**
	 * Getter of the property <code>h4SDS</code>.
	 * 
	 * @return the {@link H4SDS} to which this dimensions is related.
	 * @uml.property name="h4SDS"
	 */
	public H4SDS getH4SDS() {
		return h4SDS;
	}

	/**
	 * Builds a {@link H4Dimension} given the SDS to which the dimension
	 * belongs, and the index of the dimension within the SDS.
	 * 
	 * @param sds
	 *            the {@link H4SDS} to which this dimension belongs.
	 * @param dimensionIndex
	 *            the index of the dimension within the SDS.
	 */
	public H4Dimension(H4SDS sds, final int dimensionIndex) {
		index = dimensionIndex;
		h4SDS = sds;
		final int sdsID = sds.getIdentifier();
		try {
			// get the id of the required dimension of the specified dataset
			identifier = HDFLibrary.SDgetdimid(sdsID, dimensionIndex);
			if (identifier != HDFConstants.FAIL) {

				// retrieving dimension information
				final String[] dimName = { "" };
				final int[] dimInfo = { 0, 0, 0 };
				HDFLibrary.SDdiminfo(identifier, dimName, dimInfo);
				name = dimName[0];
				size = dimInfo[0];
				datatype = dimInfo[1] & (~HDFConstants.DFNT_LITEND);
				numAttributes = dimInfo[2];
				initDecorated();

				// Retrieving dimension scale
				final int interfaceID = sds.getH4SDSCollectionOwner()
						.getIdentifier();

				// If set, the dimension scale has the same name of the
				// dimension
				final int dimensionScaleIndex = HDFLibrary.SDnametoindex(
						interfaceID, name);
				if (dimensionScaleIndex != HDFConstants.FAIL) {
					sdsDimensionScaleID = HDFLibrary.SDselect(interfaceID,
							dimensionScaleIndex);
					hasDimensionScaleSet = HDFLibrary
							.SDiscoordvar(sdsDimensionScaleID);
					if (hasDimensionScaleSet && datatype == 0) {
						// //////////////
						//
						// Sometimes, although dimension scale values exist,
						// returned datatype is zero. Then, I attempt to
						// retrieve the datatype from the SDS containing
						// dimension scale values.
						//
						// //////////////
						final int dummyInfo[] = { 0, 0, 0 };
						final String dummyName[] = { "" };
						final int dummyDimSizes[] = new int[HDFConstants.MAX_VAR_DIMS];
						if (HDFLibrary.SDgetinfo(sdsDimensionScaleID,
								dummyName, dummyDimSizes, dummyInfo)) {
							datatype = dummyInfo[1];
						}
					}
					//
					// final boolean isDimensionScale = HDFLibrary
					// .SDiscoordvar(sdsDimensionScaleID);
					// if (isDimensionScale) {
					// dimensionScaleID = HDFLibrary.SDgetdimid(
					// sdsDimensionScaleID, 0);
					// hasDimensionScaleSet = true;
					// }
				}
			} else {
				// XXX
			}

		} catch (HDFException e) {
			throw new RuntimeException ("HDFException occurred while creating a new H4Dimension", e);
		}
	}

	/**
	 * Returns a proper <code>Object</code> containing the values of the
	 * dimension scale set for this dimension. The type of the returned object
	 * depends on the datatype of the dimension scale. As an instance, for a
	 * dimension scale having <code>HDFConstants.DFNT_INT32</code> as
	 * datatype, returned object is an <code>int</code> array. See
	 * {@link H4DatatypeUtilities#allocateArray(int, int)} to retrieve
	 * information about the returned type.
	 * 
	 * @return an <code>Object</code> containing dimension scale values if
	 *         this dimension has a dimension scale set. <code>null</code>
	 *         otherwise.
	 * @throws HDFException
	 */

	public Object getDimensionScaleValues() throws HDFException {
		Object dataValues = null;
		if (hasDimensionScaleSet && identifier != HDFConstants.FAIL) {
			dataValues = H4DatatypeUtilities.allocateArray(datatype, size);
			if (dataValues != null)
				HDFLibrary.SDgetdimscale(identifier, dataValues);
		}
		return dataValues;
	}

	/**
	 * close this {@link H4Dimension} and dispose allocated objects.
	 */
	public void dispose() {
		super.dispose();
		close();
	}

	/**
	 * if a Dimension Scale is available for this Dimension, I need to close
	 * access to the SDS containing dimension scale values
	 */
	public void close() {
		if (hasDimensionScaleSet) {
			try {
				// end access to the SDS representing the dimension
				if (sdsDimensionScaleID != HDFConstants.FAIL) {
					HDFLibrary.SDendaccess(sdsDimensionScaleID);
					sdsDimensionScaleID = HDFConstants.FAIL;
				}
			} catch (HDFException e) {
				// XXX
			}
		}
	}

	/**
	 * returns a <code>Map</code> containing all attributes associated to this
	 * Dimension
	 * 
	 * @return the map of attributes.
	 * @throws HDFException
	 */
	public Map getAttributes() throws HDFException {
		synchronized (mutex) {

			// Checking if I need to initialize attributes map
			if (attributes != null && attributes.size() < numAttributes) {

				// user defined attributes
				final String[] dimAttrName = new String[1];
				final int[] dimAttrInfo = { 0, 0 };
				int nAttr = 0;
				for (int i = 0; i < numAttributes; i++) {
					dimAttrName[0] = "";
					// get various info about this attribute
					HDFLibrary.SDattrinfo(identifier, i, dimAttrName,
							dimAttrInfo);
					final String attrName = dimAttrName[0];

					// //
					//
					// The HDF user guide explicitly states that
					// Predefined Attributes for Dimensions need to be
					// read using specialized routines (SDgetdimstrs).
					// So, I will skip a found attribute if it is a
					// predefined one.
					//
					// //
					boolean isPredef = false;
					if (attrName.equals(PREDEF_ATTR_LABEL)
							|| attrName.equals(PREDEF_ATTR_UNIT)
							|| attrName.equals(PREDEF_ATTR_FORMAT))
						isPredef = true;
					if (!isPredef) {
						H4Attribute attrib = new H4Attribute(this, nAttr,
								attrName, dimAttrInfo);
						if (attrib != null) {
							attributes.put(attrName, attrib);
							indexToAttributesMap.put(Integer.valueOf(nAttr),
									attrib);
							nAttr++;
						}
					}
				}

				// retrieving predefined attributes
				final String predefAttributesValues[] = { "NONE", "NONE",
						"NONE" };
				HDFLibrary.SDgetdimstrs(identifier, predefAttributesValues,
						HDFConstants.DFS_MAXLEN);
				final String predefinedStrings[] = { PREDEF_ATTR_LABEL,
						PREDEF_ATTR_UNIT, PREDEF_ATTR_FORMAT };

				for (int k = 0; k < 3; k++) {
					// getting predefined attribute value
					final String value = predefAttributesValues[k];
					if (value != null && value.trim().length() != 0) {

						// predefined attribute found. Building a new
						// H4Attribute
						H4Attribute attrib = new H4Attribute(this, nAttr,
								predefinedStrings[k],
								new int[] { HDFConstants.DFNT_CHAR8,
										value.length() }, value.getBytes());
						if (attrib != null) {
							attributes.put(predefinedStrings[k], attrib);
							indexToAttributesMap.put(Integer.valueOf(nAttr),
									attrib);
							nAttr++;
						}
					}
				}
			}
			return attributes;
		}
	}

	/**
	 * Returns a specific attribute of this Dimension, given its name.
	 * 
	 * @param index
	 *            the index of the required attribute
	 * @return the {@link H4Attribute} related to the specified index.
	 * @throws HDFException
	 */
	public H4Attribute getAttribute(final String attributeName)
			throws HDFException {
		H4Attribute attribute = null;
		synchronized (mutex) {
			getAttributes();
			if (attributes != null && attributes.containsKey(attributeName))
				attribute = (H4Attribute) attributes.get(attributeName);
			return attribute;
		}
	}

	/**
	 * Returns a specific attribute of this Dimension, given its index.
	 * 
	 * @param name
	 *            the name of the required attribute
	 * @return the {@link H4Attribute} related to the specified index.
	 * @throws HDFException
	 */
	public H4Attribute getAttribute(final int attributeIndex)
			throws HDFException {
		H4Attribute attribute = null;
		synchronized (mutex) {
			getAttributes();
			if (indexToAttributesMap != null
					&& indexToAttributesMap.containsKey(Integer
							.valueOf(attributeIndex)))
				attribute = (H4Attribute) indexToAttributesMap.get(Integer
						.valueOf(attributeIndex));

		}
		return attribute;
	}

	protected void finalize() throws Throwable {
		dispose();
	}
}
