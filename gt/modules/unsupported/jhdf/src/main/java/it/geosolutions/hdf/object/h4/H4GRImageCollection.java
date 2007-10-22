package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF General Raster Images.
 * 
 * @author Romagnoli Daniele
 */
public class H4GRImageCollection extends H4DecoratedObject implements IHObject {

	private int[] mutex = new int[1];

	/**
	 * The list of {@link H4GRImage}s available by mean of this image
	 * collection
	 */
	private List grImagesList;

	private Map grImagesNamesToIndexes;

	/**
	 * The number of images available by means of this image collection
	 * 
	 * @uml.property name="numImages"
	 */
	private int numImages;

	/**
	 * the {@link H4File} to which this collection is attached
	 * 
	 * @uml.property name="h4File"
	 * @uml.associationEnd inverse="h4GrImageCollection:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Getter of the property <code>numImages</code>
	 * 
	 * @return the number of images available by means of this image collection
	 * @uml.property name="numImages"
	 */
	public int getNumImages() {
		return numImages;
	}

	/**
	 * Getter of the property <code>h4File</code>
	 * 
	 * @return the {@link H4File} to which this collection is attached
	 * @uml.property name="h4File"
	 */
	public H4File getH4File() {
		return h4File;
	}

	/**
	 * Constructor which builds and initialize a
	 * <code>H4GRImageCollection</code> given an input {@link H4File}.
	 * 
	 * @param h4file
	 *            the input {@link H4File}
	 */
	public H4GRImageCollection(H4File h4file) {
		h4File = h4file;
		final int fileID = h4File.getIdentifier();
		try {
			identifier = HDFLibrary.GRstart(fileID);
			if (identifier != HDFConstants.FAIL) {
				final int[] grFileInfo = new int[2];

				// Retrieving Information
				if (HDFLibrary.GRfileinfo(identifier, grFileInfo)) {
					numAttributes = grFileInfo[1];
					initDecorated();
					numImages = grFileInfo[0];
					grImagesList = new ArrayList(numImages);
					grImagesNamesToIndexes = Collections
							.synchronizedMap(new HashMap(numImages));
					for (int i = 0; i < numImages; i++) {
						// Initializing image list
						H4GRImage grImage = new H4GRImage(this, i);
						grImagesList.add(i, grImage);
						final String name = grImage.getName();
						grImagesNamesToIndexes.put(name, new Integer(i));
						// grImage.close();
					}
				} else {
					// the GRFileInfo operation has failed
					numImages = 0;
					grImagesList = null;
				}
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new RuntimeException(
					"HDFException occurred while accessing General Raster Images routines with file "
							+ h4file.getFilePath(), e);
		}
	}

	/**
	 * close this {@link H4GRImageCollection} and dispose allocated objects.
	 */
	public void dispose() {
		super.dispose();
		if (grImagesNamesToIndexes != null)
			grImagesNamesToIndexes.clear();
		close();

	}

	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * End access to the underlying general raster interface and end access to
	 * the owned {@link AbstractHObject}s
	 */
	public void close() {
		synchronized (mutex) {
			try {
				if (grImagesList != null) {
					for (int i = 0; i < numImages; i++) {
						H4GRImage h4grImage = (H4GRImage) grImagesList.get(i);
						h4grImage.dispose();
					}
					grImagesList.clear();
				}
				if (identifier != HDFConstants.FAIL) {
					HDFLibrary.GRend(identifier);
					identifier = HDFConstants.FAIL;
				}
			} catch (HDFException e) {
				// XXX
			}
		}
	}

	/**
	 * Returns the {@link H4GRImage} related to the index-TH image. Prior to
	 * call this method, be sure that some images are available from this
	 * {@link H4GRImageCollection} by querying the
	 * {@link H4GRImageCollection#getNumImages()} method.
	 * 
	 * @param index
	 *            the index of the requested image.
	 * @return the requested {@link H4GRImage}
	 */
	public H4GRImage getH4GRImage(final int index) {
		if (index > numImages || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than "
							+ numImages);
		H4GRImage image = (H4GRImage) grImagesList.get(index);
		// image.open();
		return image;
	}

	/**
	 * Returns the {@link H4GRImage} having the name specified as input
	 * parameter, and open it. Prior to call this method, be sure that some
	 * images are available from this {@link H4GRImageCollection} by querying
	 * the {@link H4GRImageCollection#getNumImages()} method.
	 * 
	 * @param sName
	 *            the name of the requested image.
	 * @return the requested {@link H4GRImage} or <code>null</code> if the
	 *         specified image does not exist
	 */
	public H4GRImage getH4GRImage(final String sName) {
		H4GRImage grImage = null;
		if (grImagesNamesToIndexes.containsKey(sName)) {
			grImage = (H4GRImage) grImagesList
					.get(((Integer) grImagesNamesToIndexes.get(sName))
							.intValue());
			// grImage.open();
		}
		return grImage;
	}

	// /**
	// * returns a <code>Map</code> containing all file attributes associated to
	// * this image collection
	// *
	// * @return the map of attributes.
	// * @throws HDFException
	// */
	// public Map getAttributes() throws HDFException {
	// synchronized (mutex) {
	// if (attributes != null && attributes.size() < numAttributes) {
	// final String[] fileAttrName = new String[1];
	// final int[] fileAttrInfo = { 0, 0 };
	// boolean attrOk = false;
	// for (int ii = 0; ii < numAttributes; ii++) {
	// fileAttrName[0] = "";
	// // get various info about this attribute
	// attrOk = HDFLibrary.GRattrinfo(identifier, ii,
	// fileAttrName, fileAttrInfo);
	// if (attrOk) {
	// final String attrName = fileAttrName[0];
	// H4Attribute attrib = new H4Attribute(this, ii,
	// attrName, fileAttrInfo);
	// if (attrib != null)
	// attributes.put(attrName, attrib);
	// }
	// }
	// }
	// return attributes;
	// }
	// }

	// /**
	// * Returns a specific attribute of this GRImage collection, given its
	// name.
	// *
	// *
	// * @param name
	// * the name of the required attribute
	// * @return the {@link H4Attribute} related to the specified name.
	// * @throws HDFException
	// */
	// public H4Attribute getAttribute(final String attributeName)
	// throws HDFException {
	// H4Attribute attribute = null;
	// synchronized (mutex) {
	// getAttributes();
	// if (attributes != null && attributes.containsKey(attributeName))
	// attribute = (H4Attribute) attributes.get(attributeName);
	// return attribute;
	// }
	// }
}
