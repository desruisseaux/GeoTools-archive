/*
 * (c) 2004 Mike Nidel
 *
 * Take, Modify, Distribute freely
 * Buy, Sell, Pass it off as your own
 *
 * Use this code at your own risk, the author makes no guarantee
 * of performance and retains no liability for the failure of this
 * software.
 *
 * If you feel like it, send any suggestions for improvement or
 * bug fixes, or modified source code to mike@gelbin.org
 *
 * Do not taunt Happy Fun Ball.
 *
 */
package org.geotools.gce.geotiff.IIOMetadataAdpaters;

import java.awt.geom.AffineTransform;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.GeoTiffConstants;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.codes.GeoTiffGCSCodes;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.media.imageio.plugins.tiff.GeoTIFFTagSet;

/**
 * This class provides an abstraction from the details of TIFF data access for
 * the purpose of retrieving GeoTIFFWritingUtilities metadata from an image.
 * <p>
 * All of the GeoKey values are included here as constants, and the portions of
 * the GeoTIFFWritingUtilities specification pertaining to each have been copied
 * for easy access.
 * </p>
 * <p>
 * The majority of the possible GeoKey values and their meanings are NOT
 * reproduced here. Only the most important GeoKey code values have been copied,
 * for others see the specification.
 * </p>
 * <p>
 * Convenience methods have been included to retrieve the various TIFFFields
 * that are not part of the GeoKey directory, such as the Model Transformation
 * and Model TiePoints. Retrieving a GeoKey from the GeoKey directory is a bit
 * more specialized and requires knowledge of the correct key code.
 * </p>
 * <p>
 * Making use of the geographic metadata still requires some basic understanding
 * of the GeoKey values that is not provided here.
 * </p>
 * <p>
 * For more information see the GeoTIFFWritingUtilities specification at
 * http://www.remotesensing.org/geotiff/spec/geotiffhome.html
 * </p>
 * 
 * @author Mike Nidel
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/src/org/geotools/gce/geotiff/IIOMetadataAdpaters/GeoTiffIIOMetadataDecoder.java $
 */
public final class GeoTiffIIOMetadataDecoder {

	/** The root of the metadata DOM tree */
	private IIOMetadataNode rootNode = null;

	private IIOMetadataNode geoKeyDir = null;

	private NodeList geoKeyDirEntries = null;

	private int geoKeyDirEntriesNum = 0;

	private IIOMetadataNode tiffTagsEntries;

	private int numTiffTasEntries;

	private int geoKeyDirVersion;

	private int geoKeyRevision;

	private int geoKeyMinorRevision;

	private int geoKeyDirTagsNum;

	private IIOMetadataNode geoKeyDoubleParams;

	private IIOMetadataNode geoKeyAsciiParams;

	/**
	 * The constructor builds a metadata adapter for the image metadata root
	 * IIOMetadataNode.
	 * 
	 * @param imageMetadata
	 *            The image metadata
	 */
	public GeoTiffIIOMetadataDecoder(final IIOMetadata imageMetadata) {
		// getting the image metadata root node.
		rootNode = (IIOMetadataNode) imageMetadata.getAsTree(imageMetadata
				.getNativeMetadataFormatName());

		tiffTagsEntries = (IIOMetadataNode) rootNode.getFirstChild()
				.getChildNodes();
		numTiffTasEntries = tiffTagsEntries.getLength();
		// getting the geokey ddirectory
		geoKeyDir = getTiffField(GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY);
		if (geoKeyDir == null) {
			throw new UnsupportedOperationException(
					"GeoKey directory does not exist");
		}
		if (rootNode == null) {
			throw new UnsupportedOperationException(
					"Unable to retrieve metadata");
		}

		// getting all the entries and its nunber
		geoKeyDirEntries = geoKeyDir.getFirstChild().getChildNodes();
		// GeoKeyDirVersion and the other parameters
		geoKeyDirVersion = getTiffShort(geoKeyDir,
				GeoTiffGCSCodes.GEO_KEY_DIRECTORY_VERSION_INDEX);
		geoKeyRevision = getTiffShort(geoKeyDir,
				GeoTiffGCSCodes.GEO_KEY_REVISION_INDEX);
		if (geoKeyRevision != 1) {
			throw new UnsupportedOperationException("Unsupported revision");
		}
		geoKeyMinorRevision = getTiffShort(geoKeyDir,
				GeoTiffGCSCodes.GEO_KEY_MINOR_REVISION_INDEX);
		// loading the number of geokeys inside the geokeydirectory
		geoKeyDirTagsNum = getTiffShort(geoKeyDir,
				GeoTiffGCSCodes.GEO_KEY_NUM_KEYS_INDEX);
		// each geokey has 4 entries
		geoKeyDirEntriesNum = geoKeyDirEntries.getLength();

		geoKeyDoubleParams = getTiffField(GeoTIFFTagSet.TAG_GEO_DOUBLE_PARAMS);
		geoKeyAsciiParams = getTiffField(GeoTIFFTagSet.TAG_GEO_ASCII_PARAMS);

	}

	/**
	 * Gets the version of the GeoKey directory. This is typically a value of 1
	 * and can be used to check that the data is of a valid format.
	 * 
	 * @return DOCUMENT ME!
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public int getGeoKeyDirectoryVersion() {

		// now get the value from the correct TIFFShort location
		return geoKeyDirVersion;

	}

	/**
	 * Gets the revision number of the GeoKeys in this metadata.
	 * 
	 * @return DOCUMENT ME!
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public int getGeoKeyRevision() {

		// Get the value from the correct TIFFShort
		return geoKeyRevision;
	}

	/**
	 * Gets the minor revision number of the GeoKeys in this metadata.
	 * 
	 * @return DOCUMENT ME!
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public int getGeoKeyMinorRevision() {

		// Get the value from the correct TIFFShort
		return geoKeyMinorRevision;
	}

	/**
	 * Gets the number of GeoKeys in the geokeys directory.
	 * 
	 * @return DOCUMENT ME!
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public int getNumGeoKeys() {

		return geoKeyDirTagsNum;
	}

	/**
	 * Gets a GeoKey value as a String. This implementation should be
	 * &quotquiet&quot in the sense that it should not throw any exceptions but
	 * only return null in the event that the data organization is not as
	 * expected.
	 * 
	 * @param keyID
	 *            The numeric ID of the GeoKey
	 * @return A string representing the value, or null if the key was not
	 *         found.
	 */
	public String getGeoKey(final int keyID) {

		final GeoKeyEntry rec = getGeoKeyRecord(keyID);
		if (rec == null)
			return null;
		if (rec.getTiffTagLocation() == 0)
			// value is stored directly in the GeoKey record
			return String.valueOf(rec.getValueOffset());

		// value is stored externally
		// get the TIFF field where the data is actually stored
		final IIOMetadataNode field = getTiffField(rec.getTiffTagLocation());

		if (field != null) {
			final Node sequence = field.getFirstChild();

			if (sequence != null) {
				if (sequence.getNodeName().equals(
						GeoTiffConstants.GEOTIFF_ASCIIS_TAG)) {
					// TIFFAscii values are handled specially
					return getTiffAscii((IIOMetadataNode) sequence, rec
							.getValueOffset(), rec.getCount());
				} else {
					// value is numeric
					return getValueAttribute(sequence.getChildNodes().item(
							rec.getValueOffset()));
				}
			}
		}

		return null;
	}

	/**
	 * Gets a record containing the four TIFFShort values for a geokey entry.
	 * For more information see the GeoTIFFWritingUtilities specification.
	 * 
	 * @param keyID
	 *            DOCUMENT ME!
	 * @return the record with the given keyID, or null if none is found
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public GeoKeyEntry getGeoKeyRecord(int keyID) {

		int thisKeyID = 0;
		// embed the exit condition in the for loop
		for (int i = 4; i < geoKeyDirEntriesNum; i += 4) {

			thisKeyID = getIntValueAttribute(geoKeyDirEntries.item(i));// key

			if (thisKeyID == keyID) {
				// we've found the right GeoKey, now build it
				return new GeoKeyEntry(thisKeyID,
						getIntValueAttribute(geoKeyDirEntries.item(i + 1)),// location
						getIntValueAttribute(geoKeyDirEntries.item(i + 2)),// count
						getIntValueAttribute(geoKeyDirEntries.item(i + 3)));// offset
			}
		}

		return null;
	}

	/**
	 * Gets a record containing the four TIFFShort values for a geokey entry.
	 * For more information see the GeoTIFFWritingUtilities specification.
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * @return the record with the given keyID, or null if none is found
	 * @throws UnsupportedOperationException
	 *             DOCUMENT ME!
	 */
	public GeoKeyEntry getGeoKeyRecordByIndex(int index) {
		index *= 4;
		return new GeoKeyEntry(getIntValueAttribute(geoKeyDirEntries
				.item(index)), getIntValueAttribute(geoKeyDirEntries
				.item(index + 1)), getIntValueAttribute(geoKeyDirEntries
				.item(index + 2)), getIntValueAttribute(geoKeyDirEntries
				.item(index + 3)));

	}

	/**
	 * Gets the model pixel scales from the correct TIFFField
	 * 
	 */
	public PixelScale getModelPixelScales() {
		final double[] pixScales = getTiffDoubles(getTiffField(GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE));
		final int length = pixScales.length;
		final PixelScale retVal = new PixelScale();
		for (int i = 0; i < length; i++)
			switch (i) {
			case 0:
				retVal.setScaleX(pixScales[i]);
				break;
			case 1:
				retVal.setScaleY(pixScales[i]);
				break;
			case 2:
				retVal.setScaleZ(pixScales[i]);
				break;
			}
		return retVal;

	}

	/**
	 * Gets the model tie points from the appropriate TIFFField
	 * 
	 * @return the tie points, or null if not found
	 */
	public TiePoint[] getModelTiePoints() {

		final double tiePoints[] = getTiffDoubles(getTiffField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT));
		final int numTiePoints = tiePoints.length / 6;
		final TiePoint retVal[] = new TiePoint[numTiePoints];
		int initialIndex = 0;
		for (int i = 0; i < numTiePoints; i++) {
			initialIndex = i * 6;
			retVal[i] = new TiePoint(tiePoints[initialIndex],
					tiePoints[initialIndex + 1], tiePoints[initialIndex + 2],
					tiePoints[initialIndex + 3], tiePoints[initialIndex + 4],
					tiePoints[initialIndex + 5]);
		}
		return retVal;

	}

	/**
	 * Gets the model tie points from the appropriate TIFFField
	 * <p>
	 * Attention, for the moment we support only 2D baseline transformations.
	 * 
	 * @return the transformation, or null if not found
	 */
	public AffineTransform getModelTransformation() {

		final double[] modelTransformation = getTiffDoubles(getTiffField(GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION));
		if (modelTransformation == null)
			return null;
		final AffineTransform transform = new AffineTransform(
				modelTransformation[0], modelTransformation[4],
				modelTransformation[1], modelTransformation[5],
				modelTransformation[6], modelTransformation[7]);
		return transform;

	}

	// private utility methods

	/**
	 * Gets the value attribute of the given Node.
	 * 
	 * @param node
	 *            A Node containing a value attribute, for example the node
	 *            &ltTIFFShort value=&quot123&quot&gt
	 * @return A String containing the text from the value attribute. In the
	 *         above example, the string would be 123
	 */
	private String getValueAttribute(Node node) {
		return node.getAttributes().getNamedItem(GeoTiffConstants.VALUE_ATTR)
				.getNodeValue();
	}

	/**
	 * Gets the value attribute's contents and parses it as an int
	 * 
	 * @param node
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private int getIntValueAttribute(Node node) {
		return Integer.parseInt(getValueAttribute(node));
	}

	/**
	 * Gets a TIFFField node with the given tag number. This is done by
	 * searching for a TIFFField with attribute number whose value is the
	 * specified tag value.
	 * 
	 * @param tag
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private IIOMetadataNode getTiffField(final int tag) {
		if (tag == GeoTIFFTagSet.TAG_GEO_ASCII_PARAMS
				&& this.geoKeyAsciiParams != null)
			return this.geoKeyAsciiParams;
		if (tag == GeoTIFFTagSet.TAG_GEO_DOUBLE_PARAMS
				&& this.geoKeyDoubleParams != null)
			return this.geoKeyDoubleParams;
		if (tag == GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY
				&& this.geoKeyDir != null)
			return this.geoKeyDir;

		// embed the exit condition in the for loop
		Node child = null;
		Node number = null;

		for (int i = 0; i < numTiffTasEntries; i++) {
			// search through all the TIFF fields to find the one with the
			// given tag value
			child = tiffTagsEntries.item(i);
			number = child.getAttributes().getNamedItem(
					GeoTiffConstants.NUMBER_ATTR);

			if (number != null) {
				if (tag == Integer.parseInt(number.getNodeValue()))
					return (IIOMetadataNode) child;

			}
		}

		return null;
	}

	/**
	 * Gets a single TIFFShort value at the given index.
	 * 
	 * @param tiffField
	 *            An IIOMetadataNode pointing to a TIFFField element that
	 *            contains a TIFFShorts element.
	 * @param index
	 *            The 0-based index of the desired short value
	 * @return DOCUMENT ME!
	 */
	private int getTiffShort(final IIOMetadataNode tiffField, final int index) {

		return getIntValueAttribute(((IIOMetadataNode) tiffField
				.getFirstChild()).getElementsByTagName(
				GeoTiffConstants.GEOTIFF_SHORT_TAG).item(index));

	}

	/**
	 * Gets an array of double values from a TIFFDoubles TIFFField.
	 * 
	 * @param tiffField
	 *            An IIOMetadataNode pointing to a TIFFField element that
	 *            contains a TIFFDoubles element.
	 * @return DOCUMENT ME!
	 */
	private double[] getTiffDoubles(final IIOMetadataNode tiffField) {

		if (tiffField == null)
			return null;
		final NodeList doubles = ((IIOMetadataNode) tiffField.getFirstChild())
				.getElementsByTagName(GeoTiffConstants.GEOTIFF_DOUBLE_TAG);
		final int length = doubles.getLength();
		final double[] result = new double[length];
		for (int i = 0; i < length; i++) {
			result[i] = Double.parseDouble(getValueAttribute(doubles.item(i)));
		}

		return result;
	}

	/**
	 * Gets a portion of a TIFFAscii string with the specified start character
	 * and length;
	 * 
	 * @param tiffField
	 *            An IIOMetadataNode pointing to a TIFFField element that
	 *            contains a TIFFAsciis element. This element should contain a
	 *            single TiffAscii element.
	 * @param start
	 *            DOCUMENT ME!
	 * @param length
	 *            DOCUMENT ME!
	 * @return A substring of the value contained in the TIFFAscii node, with
	 *         the final '|' character removed.
	 */
	private String getTiffAscii(final IIOMetadataNode tiffField,
			final int start, final int length) {

		// there should be only one, so get the first
		// GeoTIFFWritingUtilities specification places a vertical bar '|' in
		// place of \0
		// null delimiters so drop off the vertical bar for Java Strings
		return getValueAttribute(
				((IIOMetadataNode) tiffField.getFirstChild())
						.getElementsByTagName(
								GeoTiffConstants.GEOTIFF_ASCII_TAG).item(0))
				.substring(start, start + length - 1);

	}

	/**
	 * Retrieves the root node of the tiff metadata we are parsing.
	 * 
	 * @return The root node of the tiff metadata we are parsing.
	 */
	public IIOMetadataNode getRootNode() {
		return rootNode;
	}
} // end of class GeoTiffIIOMetadataDecoder
