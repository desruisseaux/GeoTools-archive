/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gce.geotiff.IIOMetadataAdpaters;

import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.GeoTiffConstants;
import org.geotools.util.KeySortedList;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.GeoTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFTag;

public class GeoTiffIIOMetadataEncoder {

	private int numModelTiePoints;

	private TiePoint[] modelTiePoints;

	private double[] modelPixelScale;

	private double[] modelTransformation;

	private int numGeoKeyEntries;

	private KeySortedList geoTiffEntries;

	private int numGeoTiffDoubleParams;

	private double[] geoDoubleParams;

	private int numGeoAsciiParams;

	private StringBuffer geoAsciiParams;

	public GeoTiffIIOMetadataEncoder() {
		this(GeoTiffConstants.DEFAULT_GEOTIFF_VERSION,
				GeoTiffConstants.DEFAULT_KEY_REVISION_MAJOR,
				GeoTiffConstants.DEFAULT_KEY_REVISION_MINOR);
	}

	public GeoTiffIIOMetadataEncoder(final int geoTIFFVersion,
			final int keyRevisionMajor, final int keyRevisionMinor) {
		geoTiffEntries = new KeySortedList();
		geoDoubleParams = new double[GeoTiffConstants.ARRAY_ELEM_INCREMENT];
		geoAsciiParams = new StringBuffer();
		modelTiePoints = new TiePoint[GeoTiffConstants.ARRAY_ELEM_INCREMENT];
		modelPixelScale = new double[3];
		modelTransformation = new double[16];
		setModelPixelScale(1.0, 1.0);
		addGeoKeyEntry(geoTIFFVersion, keyRevisionMajor, keyRevisionMinor, 0);
	}

	public static boolean isTiffUShort(final int value) {
		return (value >= GeoTiffConstants.USHORT_MIN)
				&& (value <= GeoTiffConstants.USHORT_MAX);
	}

	public int getGeoTIFFVersion() {
		return getGeoKeyEntryAt(0).values[0];
	}

	public void setGeoTIFFVersion(int version) {
		getGeoKeyEntryAt(0).values[0] = version;
	}

	public int getKeyRevisionMajor() {
		return getGeoKeyEntryAt(0).values[1];
	}

	public int getKeyRevisionMinor() {
		return getGeoKeyEntryAt(0).values[2];
	}

	public void setKeyRevision(int major, int minor) {
		getGeoKeyEntryAt(0).values[1] = major;
		getGeoKeyEntryAt(0).values[2] = minor;
	}

	public double getModelPixelScaleX() {
		return modelPixelScale[0];
	}

	public double getModelPixelScaleY() {
		return modelPixelScale[1];
	}

	public double getModelPixelScaleZ() {
		return modelPixelScale[2];
	}

	public void setModelPixelScale(double x, double y) {
		setModelPixelScale(x, y, 0.0);
	}

	public void setModelPixelScale(double x, double y, double z) {
		modelPixelScale[0] = x;
		modelPixelScale[1] = y;
		modelPixelScale[2] = z;
	}

	public int getNumModelTiePoints() {
		return numModelTiePoints;
	}

	public TiePoint getModelTiePoint() {
		return getModelTiePointAt(0);
	}

	public TiePoint getModelTiePointAt(int index) {
		return modelTiePoints[index];
	}

	public void setModelTiePoint(double i, double j, double x, double y) {
		setModelTiePoint(i, j, 0.0, x, y, 0.0);
	}

	public void setModelTiePoint(double i, double j, double k, double x,
			double y, double z) {
		if (getNumModelTiePoints() > 0) {
			getModelTiePointAt(0).set(i, j, k, x, y, z);
		} else {
			addModelTiePoint(i, j, k, x, y, z);
		}
	}

	public void addModelTiePoint(double i, double j, double x, double y) {
		addModelTiePoint(i, j, 0.0, x, y, 0.0);
	}

	public void addModelTiePoint(double i, double j, double k, double x,
			double y, double z) {
		final int numTiePoints = numModelTiePoints;

		if (numTiePoints >= (modelTiePoints.length - 1)) {
			final TiePoint[] tiePoints = new TiePoint[numTiePoints
					+ GeoTiffConstants.ARRAY_ELEM_INCREMENT];
			System.arraycopy(modelTiePoints, 0, tiePoints, 0, numTiePoints);
			modelTiePoints = tiePoints;
		}

		modelTiePoints[numTiePoints] = new TiePoint(i, j, k, x, y, z);
		numModelTiePoints++;
	}

	public int getNumGeoKeyEntries() {
		return numGeoKeyEntries;
	}

	public KeyEntry getGeoKeyEntryAt(int index) {
		// got to retrieve the eleme at a certain index
		final Object it = this.geoTiffEntries.get(index);
		if (it != null)
			return (KeyEntry) (it);
		return null;
	}

	public KeyEntry getGeoKeyEntry(int keyID) {
		KeyEntry retVal = null;
		final Object o = geoTiffEntries.first(new Integer(keyID));
		if (o!=null)
			retVal = (KeyEntry) o;

		return retVal;
	}

	public boolean hasGeoKeyEntry(int keyID) {
		return getGeoKeyEntry(keyID) != null;
	}

	public int getGeoShortParam(int keyID) {
		final KeyEntry entry = getNonNullKeyEntry(keyID);
		final int[] data = entry.getValues();
		final int tag = data[1];
		// final int count = data[2]; // ignored here
		final int value = data[3];
		checkParamTag(tag, 0);

		return value;
	}

	public double getGeoDoubleParam(int keyID) {
		final KeyEntry entry = getNonNullKeyEntry(keyID);
		final int[] data = entry.getValues();
		final int tag = data[1];
		// final int count = data[2]; // ignored here
		final int offset = data[3];
		checkParamTag(tag, getGeoDoubleParamsTag().getNumber());

		return geoDoubleParams[offset];
	}

	public double[] getGeoDoubleParams(int keyID) {
		return getGeoDoubleParams(keyID, null);
	}

	public double[] getGeoDoubleParams(int keyID, double[] values) {
		final KeyEntry entry = getNonNullKeyEntry(keyID);
		final int[] data = entry.getValues();
		final int tag = data[1];
		final int count = data[2];
		final int offset = data[3];
		checkParamTag(tag, getGeoDoubleParamsTag().getNumber());

		if (values == null) {
			values = new double[count];
		}

		System.arraycopy(geoDoubleParams, offset, values, 0, count);

		return values;
	}

	public String getGeoAsciiParam(int keyID) {
		final KeyEntry entry = getNonNullKeyEntry(keyID);
		final int[] data = entry.getValues();
		final int tag = data[1];
		final int count = data[2];
		final int offset = data[3];
		checkParamTag(tag, getGeoAsciiParamsTag().getNumber());

		return geoAsciiParams.substring(offset, (offset + count) - 1);
	}

	public void addGeoShortParam(int keyID, int value) {
		addGeoKeyEntry(keyID, 0, 1, value);
	}

	public void addGeoDoubleParam(int keyID, double value) {
		addGeoDoubleParamsRef(keyID, 1);
		addDoubleParam(value);
	}

	public void addGeoDoubleParams(int keyID, double[] values) {
		addGeoDoubleParamsRef(keyID, values.length);
		final int length = values.length;
		for (int i = 0; i < length; i++) {
			addDoubleParam(values[i]);
		}
	}

	public void addGeoAscii(int keyID, String value) {
		addGeoAsciiParamsRef(keyID, value.length() + 1); // +1 for the '|'
		// character to be
		// appended
		addAsciiParam(value);
	}

	private void addGeoKeyEntry(int keyID, int tag, int count, int offset) {
		if (!isTiffUShort(keyID)) {
			throw new IllegalArgumentException("keyID is not a TIFF USHORT");
		}

		if (!isTiffUShort(tag)) {
			throw new IllegalArgumentException("tag is not a TIFF USHORT");
		}

		if (!isTiffUShort(count)) {
			throw new IllegalArgumentException("count is not a TIFF USHORT");
		}

		if (!isTiffUShort(offset)) {
			throw new IllegalArgumentException("offset is not a TIFF USHORT");
		}

		final int numKeyEntries = numGeoKeyEntries;
		geoTiffEntries.add(new Integer(keyID), new KeyEntry(keyID, tag,
				count, offset));
		getGeoKeyEntryAt(0).values[3] = numKeyEntries;
		numGeoKeyEntries++;
	}

	public void assignTo(Element element) {
		if (!element.getName().equals(
				GeoTiffConstants.GEOTIFF_IIO_ROOT_ELEMENT_NAME)) {
			throw new IllegalArgumentException("root not found: "
					+ GeoTiffConstants.GEOTIFF_IIO_ROOT_ELEMENT_NAME);
		}

		final Element ifd1 = element.getChild(GeoTiffConstants.GEOTIFF_IFD_TAG);

		if (ifd1 == null) {
			throw new IllegalArgumentException("child not found: "
					+ GeoTiffConstants.GEOTIFF_IFD_TAG);
		}

		final Element ifd2 = createIFD();
		ifd1.setAttribute(GeoTiffConstants.GEOTIFF_TAGSETS_ATT_NAME, ifd2
				.getAttributeValue(GeoTiffConstants.GEOTIFF_TAGSETS_ATT_NAME));

		final Element[] childElems = (Element[]) ifd2.getChildren().toArray(
				new Element[0]);
		final int length = childElems.length;
		for (int i = 0; i < length; i++) {
			Element child = childElems[i];
			ifd2.removeContent(child);
			ifd1.addContent(child);
		}
	}

	public Element createRootTree() {
		final Element rootElement = new Element(
				GeoTiffConstants.GEOTIFF_IIO_ROOT_ELEMENT_NAME);
		rootElement.addContent(createIFD());

		return rootElement;
	}



	protected static TIFFTag getGeoKeyDirectoryTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY);
	}

	protected static TIFFTag getGeoDoubleParamsTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_GEO_DOUBLE_PARAMS);
	}

	protected static TIFFTag getGeoAsciiParamsTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_GEO_ASCII_PARAMS);
	}

	protected static TIFFTag getModelPixelScaleTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE);
	}

	protected static TIFFTag getModelTiePointTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_MODEL_TIE_POINT);
	}

	protected static TIFFTag getModelTransformationTag() {
		return GeoTIFFTagSet.getInstance().getTag(
				GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Private Implementation Helpers
	private KeyEntry getNonNullKeyEntry(int keyID) {
		final KeyEntry entry = getGeoKeyEntry(keyID);

		if (entry == null) {
			throw new IllegalArgumentException("entry not found for geo key "
					+ keyID);
		}

		return entry;
	}

	private void checkParamTag(final int tag, final int expectedTag) {
		if (tag != expectedTag) {
			if (expectedTag == 0) {
				throw new IllegalArgumentException(
						"invalid key access, not a GeoTIFF SHORT parameter");
			} else if (expectedTag == getGeoDoubleParamsTag().getNumber()) {
				throw new IllegalArgumentException(
						"invalid key access, not a GeoTIFF DOUBLE parameter");
			} else if (expectedTag == getGeoAsciiParamsTag().getNumber()) {
				throw new IllegalArgumentException(
						"invalid key access, not a GeoTIFF ASCII parameter");
			} else {
				throw new IllegalStateException();
			}
		}
	}

	private void addDoubleParam(double param) {
		final int numDoubleParams = numGeoTiffDoubleParams;

		if (numDoubleParams >= (geoDoubleParams.length - 1)) {
			final double[] doubleParams = new double[numDoubleParams
					+ GeoTiffConstants.ARRAY_ELEM_INCREMENT];
			System.arraycopy(geoDoubleParams, 0, doubleParams, 0,
					numDoubleParams);
			geoDoubleParams = doubleParams;
		}

		geoDoubleParams[numDoubleParams] = param;
		numGeoTiffDoubleParams++;
	}

	private void addAsciiParam(String param) {
		geoAsciiParams.append(param);
		geoAsciiParams.append('|');
		numGeoAsciiParams++;
	}

	private void addGeoDoubleParamsRef(int keyID, int count) {
		addGeoKeyEntry(keyID, getGeoDoubleParamsTag().getNumber(), count,
				getCurrentGeoDoublesOffset());
	}

	private void addGeoAsciiParamsRef(int keyID, int length) {
		addGeoKeyEntry(keyID, getGeoAsciiParamsTag().getNumber(), length,
				getCurrentGeoAsciisOffset());
	}

	private int getCurrentGeoDoublesOffset() {
		return numGeoTiffDoubleParams;
	}

	private int getCurrentGeoAsciisOffset() {
		return geoAsciiParams.length();
	}

	private Element createIFD() {
		Element ifd = new Element(GeoTiffConstants.GEOTIFF_IFD_TAG);
		ifd.setAttribute(GeoTiffConstants.GEOTIFF_TAGSETS_ATT_NAME,
				BaselineTIFFTagSet.class.getName() + ","
						+ GeoTIFFTagSet.class.getName());

		if (isModelPixelScaleSet()) {
			ifd.addContent(createModelPixelScaleElement());
		}

		if (hasModelTiePoints()) {
			ifd.addContent(createModelTiePointsElement());
		}

		if (isModelTransformationSet()) {
			ifd.addContent(createModelTransformationElement());
		}

		if (getNumGeoKeyEntries() > 1) {
			ifd.addContent(createGeoKeyDirectoryElement());
		}

		if (numGeoTiffDoubleParams > 0) {
			ifd.addContent(createGeoDoubleParamsElement());
		}

		if (numGeoAsciiParams > 0) {
			ifd.addContent(createGeoAsciiParamsElement());
		}

		return ifd;
	}

	private boolean isModelPixelScaleSet() {
		final int length = modelPixelScale.length;
		for (int i = 0; i < length; i++) {
			if ((modelPixelScale[i] != 0.0) && (modelPixelScale[i] != 1.0)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasModelTiePoints() {
		return numModelTiePoints > 0;
	}

	private boolean isModelTransformationSet() {
		final int length = modelTransformation.length;
		for (int i = 0; i < length; i++) {
			if (modelTransformation[i] != 0.0) {
				return true;
			}
		}

		return false;
	}

	private Element createGeoKeyDirectoryElement() {
		Element field = createFieldElement(getGeoKeyDirectoryTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_SHORTS_TAG);
		field.addContent(data);

		for (int i = 0; i < numGeoKeyEntries; i++) {
			final int[] values = getGeoKeyEntryAt(i).values;
			final int lenght = values.length;
			for (int j = 0; j < lenght; j++) {
				Element keyEntry = createShortElement(values[j]);
				data.addContent(keyEntry);
			}
		}

		return field;
	}

	private Element createGeoDoubleParamsElement() {
		Element field = createFieldElement(getGeoDoubleParamsTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_DOUBLES_TAG);
		field.addContent(data);

		for (int i = 0; i < numGeoTiffDoubleParams; i++) {
			Element param = createDoubleElement(geoDoubleParams[i]);
			data.addContent(param);
		}

		return field;
	}

	private Element createGeoAsciiParamsElement() {
		Element field = createFieldElement(getGeoAsciiParamsTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_ASCIIS_TAG);
		field.addContent(data);
		data.addContent(createAsciiElement(geoAsciiParams.toString()));

		return field;
	}

	private Element createModelPixelScaleElement() {
		Element field = createFieldElement(getModelPixelScaleTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_DOUBLES_TAG);
		field.addContent(data);
		addDoubleElements(data, modelPixelScale);

		return field;
	}

	private Element createModelTransformationElement() {
		Element field = createFieldElement(getModelTransformationTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_DOUBLES_TAG);
		field.addContent(data);
		addDoubleElements(data, modelTransformation);

		return field;
	}

	private Element createModelTiePointsElement() {
		Element field = createFieldElement(getModelTiePointTag());
		Element data = new Element(GeoTiffConstants.GEOTIFF_DOUBLES_TAG);
		field.addContent(data);

		for (int i = 0; i < numModelTiePoints; i++) {
			addDoubleElements(data, modelTiePoints[i].values);
		}

		return field;
	}

	private Element createFieldElement(final TIFFTag tag) {
		Element field = new Element(GeoTiffConstants.GEOTIFF_FIELD_TAG);
		field.setAttribute(GeoTiffConstants.NUMBER_ATTR, String.valueOf(tag
				.getNumber()));
		field.setAttribute(GeoTiffConstants.NAME_ATTR, tag.getName());

		return field;
	}

	private Element createShortElement(final int value) {
		Element keyEntry = new Element(GeoTiffConstants.GEOTIFF_SHORT_TAG);
		keyEntry.setAttribute(GeoTiffConstants.VALUE_ATTR, String
				.valueOf(value));

		return keyEntry;
	}

	private Element createDoubleElement(final double value) {
		Element param = new Element(GeoTiffConstants.GEOTIFF_DOUBLE_TAG);
		param.setAttribute(GeoTiffConstants.VALUE_ATTR, String.valueOf(value));

		return param;
	}

	private Element createAsciiElement(final String value) {
		Element param = new Element(GeoTiffConstants.GEOTIFF_ASCII_TAG);
		param.setAttribute(GeoTiffConstants.VALUE_ATTR, String.valueOf(value));

		return param;
	}

	private void addDoubleElements(Element data, final double[] values) {
		final int length = values.length;
		for (int j = 0; j < length; j++) {
			Element keyEntry = createDoubleElement(values[j]);
			data.addContent(keyEntry);
		}
	}

	private final static class KeyEntry {
		public int[] values;

		private KeyEntry(int keyID, int tag, int count, int offset) {
			values = new int[4];
			set(keyID, tag, count, offset);
		}

		private void set(int keyID, int tag, int count, int offset) {
			values[0] = keyID;
			values[1] = tag;
			values[2] = count;
			values[3] = offset;
		}

		public int[] getValues() {
			return values;
		}
	}

	private final static class TiePoint {
		private double[] values;

		private TiePoint(double i, double j, double k, double x, double y,
				double z) {
			values = new double[6];
			set(i, j, k, x, y, z);
		}

		private void set(double i, double j, double k, double x, double y,
				double z) {
			values[0] = i;
			values[1] = j;
			values[2] = k;
			values[3] = x;
			values[4] = y;
			values[5] = z;
		}

		public double[] getData() {
			return values;
		}
	}
}