package it.geosolutions.hdf.object.h4;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdflib.HDFCompInfo;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a General Raster Image.
 * 
 * @author Daniele Romagnoli
 */
public class H4GRImage extends H4Variable implements IH4ReferencedObject {

	/** predefined attributes */
	// TODO: To be checked
	public static String PREDEF_ATTR_FILL_VALUE = "FILL_VALUE";

	private int[] mutex = new int[] { 1 };

	/**
	 * The list of {@link H4Palette} available for this image
	 * 
	 * @uml.property name="palettes"
	 */
	private List palettes;

	/**
	 * the interlace mode associated to this image <BR>
	 * Available values are:<BR>
	 * HDFConstants.MFGR_INTERLACE_PIXEL<BR>
	 * HDFConstants.MFGR_INTERLACE_LINE<BR>
	 * HDFConstants.MFGR_INTERLACE_COMPONENT<BR>
	 * 
	 * @uml.property name="interlaceMode"
	 */
	private int interlaceMode;

	/**
	 * the pixel datatype of this image
	 * 
	 * @uml.property name="datatype"
	 */

	private int datatype;

	/**
	 * The number of components in this image
	 * 
	 * @uml.property name="numComponents"
	 */
	private int numComponents;

	/**
	 * the number of palettes available for this image
	 * 
	 * @uml.property name="numPalettes"
	 */
	private int numPalettes;

	/**
	 * the dimension sizes of this image
	 * 
	 * @uml.property name="dimSizes"
	 */
	private int[] dimSizes = new int[2];

	/**
	 * the reference of this image
	 * 
	 * @uml.property name="reference"
	 */
	private H4ReferencedObject reference;

	/**
	 * The index of this image.
	 * 
	 * @uml.property name="index"
	 */
	private int index;

	/**
	 * The list of data object label annotations related to this Image.
	 */
	private List labelAnnotations = null;

	/**
	 * The number of data object label annotations related to this Image.
	 * 
	 * @uml.property name="nLabels"
	 */
	private int nLabels = -1;

	/**
	 * The list of data object description annotations related to this Image.
	 */
	private List descAnnotations = null;

	/**
	 * The number of data object description annotations related to this Image.
	 * 
	 * @uml.property name="nDescriptions"
	 */
	private int nDescriptions = -1;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Getter of the property <code>reference</code>
	 * 
	 * @return the reference of this image
	 * @uml.property name="reference"
	 */
	public int getReference() {
		return reference.getReference();
	}

	/**
	 * Getter of the property <code>datatype</code>
	 * 
	 * @return the pixel datatype of this image
	 * @uml.property name="datatype"
	 */
	public int getDatatype() {
		return datatype;
	}

	/**
	 * Getter of the property <code>dimSizes</code>
	 * 
	 * @return the dimension sizes of this image
	 * @uml.property name="dimSizes"
	 */
	public int[] getDimSizes() {
		return dimSizes;
	}

	/**
	 * Getter of the property <code>interlaceMode</code>
	 * 
	 * @return the interlace mode associated to this image.
	 * @uml.property name="interlaceMode"
	 */
	public int getInterlaceMode() {
		return interlaceMode;
	}

	/**
	 * Getter of the property <code>numComponents</code>
	 * 
	 * @return the number of components in this image
	 * @uml.property name="numComponents"
	 */
	public int getNumComponents() {
		return numComponents;
	}

	/**
	 * Getter of the property <code>numPalettes</code>
	 * 
	 * @return the number of palettes available for this image
	 * @uml.property name="numPalettes"
	 */
	public int getNumPalettes() {
		return numPalettes;
	}

	/**
	 * Getter of the property <code>index</code>
	 * 
	 * @return the index of this image
	 * @uml.property name="index"
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Getter of the property <code>nDescriptions</code>
	 * 
	 * @return number of data object description annotations related to this
	 *         Image.
	 * @uml.property name="nDescriptions"
	 */
	public int getNDescriptions() {
		synchronized (mutex) {
			if (nDescriptions == -1)
				try {
					getAnnotations(HDFConstants.AN_DATA_DESC);
				} catch (HDFException e) {
					nDescriptions = 0;
				}
			return nDescriptions;
		}
	}

	/**
	 * Getter of the property <code>nLabels</code>
	 * 
	 * @return the number of data object label annotations related to this
	 *         Image.
	 * @uml.property name="nLabels"
	 */
	public int getNLabels() {
		synchronized (mutex) {
			if (nLabels == -1)
				try {
					getAnnotations(HDFConstants.AN_DATA_LABEL);
				} catch (HDFException e) {
					nLabels = 0;
				}
			return nLabels;
		}
	}

	/**
	 * Getter of the property <code>h4GRImageCollectionOwner</code>
	 * 
	 * @return the {@link H4GRImageCollection} to which this {@link H4GRImage}
	 *         belongs.
	 * @uml.property name="h4GRImageCollectionOwner"
	 */
	public H4GRImageCollection getH4GRImageCollectionOwner() {
		return h4GRImageCollectionOwner;
	}

	/**
	 * Returns compression info related to this Image
	 * 
	 * @return the type of compression.
	 * @throws HDFException
	 */
	public int getCompression() throws HDFException {
		final HDFCompInfo compInfo = new HDFCompInfo();
		HDFLibrary.GRgetcompress(identifier, compInfo);
		return compInfo.ctype;
	}

	/**
	 * The {@link H4GRImageCollection} to which this {@link H4GRImage} belongs.
	 * 
	 * @uml.property name="h4GRImageCollectionOwner"
	 * @uml.associationEnd inverse="grImages:it.geosolutions.hdf.object.h4.H4GRImageCollection"
	 */
	private H4GRImageCollection h4GRImageCollectionOwner = null;

	/**
	 * Constructor which builds a new <code>H4GRImage</code> given its index
	 * in the collection.<BR>
	 * 
	 * @param h4GrImageCollection
	 *            the parent collection
	 * @param grIndex
	 *            the index of the required image
	 * 
	 */
	public H4GRImage(H4GRImageCollection h4GrImageCollection, final int grIndex) {
		h4GRImageCollectionOwner = h4GrImageCollection;
		final int interfaceID = h4GRImageCollectionOwner.getIdentifier();
		try {
			identifier = HDFLibrary.GRselect(interfaceID, grIndex);
			if (identifier != HDFConstants.FAIL) {
				reference = new H4ReferencedObject(HDFLibrary
						.GRidtoref(identifier));

				// retrieving general raster info
				final int grInfo[] = { 0, 0, 0, 0 };
				String grName[] = { "" };
				HDFLibrary.GRgetiminfo(identifier, grName, grInfo, dimSizes);
				this.name = grName[0];
				numComponents = grInfo[0];
				datatype = grInfo[1] & (~HDFConstants.DFNT_LITEND);
				interlaceMode = grInfo[2];
				numAttributes = grInfo[3];
				initDecorated();
				numPalettes = HDFLibrary.GRgetnluts(identifier);
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new RuntimeException ("HDFException occurred while creating a new H4GRImage", e);
		}
	}

	protected void finalize() throws Throwable {
		dispose();
	}
	
	/**
	 * close this {@link H4GRImage} and dispose allocated objects.
	 */
	public void dispose() {
		synchronized (mutex) {
			
			//Disposing object holds by H4DecoratedObject superclass
			super.dispose();
			
			//closing annotations
			if (descAnnotations != null) {
				for (int i = 0; i < nDescriptions; i++) {
					H4Annotation ann = (H4Annotation) descAnnotations.get(i);
					ann.close();
				}
				descAnnotations.clear();
			}
			if (labelAnnotations != null) {
				for (int i = 0; i < nLabels; i++) {
					H4Annotation ann = (H4Annotation) labelAnnotations.get(i);
					ann.close();
				}
				labelAnnotations.clear();
			}
			close();
		}
	}

	/**
	 * Terminate access to this Image
	 */
	public void close() {
		try {
			if (identifier != HDFConstants.FAIL) {
				HDFLibrary.GRendaccess(identifier);
				identifier = HDFConstants.FAIL;
			}
		} catch (HDFException e) {
			// XXX
		}
	}

	/**
	 * Open a H4GRImage. It is worth to point out that each open operations
	 * provides a different identifier.
	 */
	public void open() {
		if (identifier != HDFConstants.FAIL) {
			try {
				identifier = HDFLibrary.GRselect(h4GRImageCollectionOwner
						.getIdentifier(), index);
			} catch (HDFException e) {
				throw new RuntimeException("Error while opening the H4GRImage",
						e);
			}
		}
	}

	/**
	 * Returns a <code>List</code> containing available palettes for this
	 * image.
	 * 
	 * @return a <code>List</code> of {@link H4Palette}s
	 */
	public List getPalettes() {
		synchronized (mutex) {
			if (palettes == null) {
				palettes = new ArrayList(numPalettes);
				for (int pal = 0; pal < numPalettes; pal++) {
					H4Palette palette = new H4Palette(this, pal);
					palettes.add(pal, palette);
				}
			}
			return palettes;
		}
	}

	/**
	 * Returns the {@link H4Palette} related to the index-TH palette available
	 * for this image. Prior to call this method, be sure that some palettes are
	 * available from this {@link H4GRImage} by querying the
	 * {@link H4GRImage#getNumPalettes()} method.
	 * 
	 * @param index
	 *            the index of the requested palette.
	 * @return the requested {@link H4Palette}
	 */
	public H4Palette getPalette(final int index) {
		return (H4Palette) getPalettes().get(index);
	}

	/**
	 * Read a required zone of data, given a set of input parameters.
	 * 
	 * @param selectedStart
	 *            int array indicating the start point of each dimension
	 * @param selectedStride
	 *            int array indicating the required stride of each dimension
	 * @param selectedSizes
	 *            int array indicating the required size along each dimension
	 * @return an array of the proper type containing read data
	 * @throws HDFException
	 */
	public Object read(final int selectedStart[], final int selectedStride[],
			final int selectedSizes[]) throws HDFException {

		Object theData = null;
		int datasize = 1;

		// NOTE: Images are always 2D. So, rank is always 2
		int[] size = new int[2];
		int[] start = new int[2];
		int[] stride = null;
		for (int i = 0; i < 2; i++) {
			datasize *= selectedSizes[i];
			size[i] = selectedSizes[i];
			start[i] = selectedStart[i];
		}
		if (selectedStride != null) {
			stride = new int[2];
			for (int i = 0; i < 2; i++)
				stride[i] = selectedStride[i];
		}

		// allocate the required data array where data read will be stored.
		// TODO: Attempts to read some Banded GRImages (with numComponents>0)
		// Available examples in the guide are unworking
		theData = H4DatatypeUtilities.allocateArray(datatype, datasize);

		// //
		//
		// Read operation
		//
		// //
		if (theData != null) {
			// prepare the proper interlaceMode for next read operation.
			HDFLibrary.GRreqimageil(identifier, interlaceMode);
			HDFLibrary.GRreadimage(identifier, start, stride, size, theData);
		}
		return theData;
	}

	/**
	 * Returns a <code>List</code> of annotations available for this Image,
	 * given the required type of annotations.
	 * 
	 * @param annotationType
	 *            the required annotation type. One of:<BR>
	 *            <code>HDFConstants.AN_DATA_LABEL</code> for label
	 *            annotations<BR>
	 *            <code>HDFConstants.AN_DATA_DESC</code> for description
	 *            annotations
	 * @return the <code>List</code> of annotations available for this Image
	 * @throws HDFException
	 */
	public List getAnnotations(final int annotationType) throws HDFException {
		synchronized (mutex) {
			H4AnnotationManager annotationManager = h4GRImageCollectionOwner
					.getH4File().getH4AnnotationManager();
			switch (annotationType) {
			case HDFConstants.AN_DATA_LABEL:
				if (nLabels == -1) {
					// Searching data object label annotations related to this
					// SDS using the new TAG.
					List listLabels = annotationManager.getH4Annotations(
							HDFConstants.AN_DATA_LABEL,
							(short) HDFConstants.DFTAG_RIG, (short) reference
									.getReference());
					if (listLabels == null || listLabels.size() == 0) {
						nLabels = 0;
					} else
						nLabels = listLabels.size();
					labelAnnotations = listLabels;
				}
				return labelAnnotations;
			case HDFConstants.AN_DATA_DESC:
				if (nDescriptions == -1) {
					// Searching data object label annotations related to this
					// SDS using the new TAG.
					List listDescriptions = annotationManager.getH4Annotations(
							HDFConstants.AN_DATA_DESC,
							(short) HDFConstants.DFTAG_RIG, (short) reference
									.getReference());
					if (listDescriptions == null
							|| listDescriptions.size() == 0) {
						nDescriptions = 0;
					} else
						nDescriptions = listDescriptions.size();
					descAnnotations = listDescriptions;
				}
				return descAnnotations;
			default:
				return null;
			}
		}
	}
}
