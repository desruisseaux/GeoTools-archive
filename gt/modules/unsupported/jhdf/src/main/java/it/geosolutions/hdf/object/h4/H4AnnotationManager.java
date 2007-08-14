package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF annotations. It is worth to point out that it
 * does not internally store any instance of {@link H4Annotation} but simply
 * allows to build them when required.
 * 
 * @author Romagnoli Daniele
 */
public class H4AnnotationManager extends AbstractHObject implements IHObject {
	//XXX: Add Synchronization?
	
	
	/**
	 * The number of file label annotations <br>
	 * 
	 * @uml.property name="nFileLabels"
	 */
	private int nFileLabels = -1;

	/**
	 * The number of file description annotations <br>
	 * 
	 * @uml.property name="nFileDescriptions"
	 */
	private int nFileDescriptions = -1;

	/**
	 * The number of total data object label annotations <br>
	 * 
	 * @uml.property name="nDataObjectLabels"
	 */
	private int nDataObjectLabels = -1;

	/**
	 * The number of total data object description annotations <br>
	 * 
	 * @uml.property name="nDataObjectDescriptions"
	 */
	private int nDataObjectDescriptions = -1;

	/**
	 * the {@link H4File} to which this collection is attached
	 * 
	 * @uml.property name="h4File"
	 * @uml.associationEnd inverse="H4AnnotationManager:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Getter of the property <code>nDataObjectDescriptions</code>
	 * 
	 * @return Returns the nDataObjectDescriptions.
	 * @uml.property name="nDataObjectDescriptions"
	 */
	public int getNDataObjectDescriptions() {
		return nDataObjectDescriptions;
	}

	/**
	 * Getter of the property <code>nDataObjectLabels</code>
	 * 
	 * @return Returns the nDataObjectLabels.
	 * @uml.property name="nDataObjectLabels"
	 */
	public int getNDataObjectLabels() {
		return nDataObjectLabels;
	}

	/**
	 * Getter of the property <code>nFileDescriptions</code>
	 * 
	 * @return Returns the nFileDescriptions.
	 * @uml.property name="nFileDescriptions"
	 */
	public int getNFileDescriptions() {
		return nFileDescriptions;
	}

	/**
	 * Getter of the property <code>nFileLabels</code>
	 * 
	 * @return Returns the nFileLabels.
	 * @uml.property name="nFileLabels"
	 */
	public int getNFileLabels() {
		return nFileLabels;
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
	 * Main constructor which builds a <code>H4AnnotationManager</code> given
	 * an input {@link H4File}.
	 * 
	 * @param h4file
	 *            the input {@link H4File}
	 */
	public H4AnnotationManager(H4File h4file) {
		// set the parent H4File and retrieve its identifier
		h4File = h4file;
		final int fileID = h4File.getIdentifier();
		try {
			// Open the Annotation Interface and set its identifier
			identifier = HDFLibrary.ANstart(fileID);
			if (identifier != HDFConstants.FAIL) {
				// Retrieve basic annotations properties.
				int annotationsInfo[] = new int[] { 0, 0, 0, 0 };
				HDFLibrary.ANfileinfo(identifier, annotationsInfo);
				nFileLabels = annotationsInfo[0];
				nFileDescriptions = annotationsInfo[1];
				nDataObjectLabels = annotationsInfo[2];
				nDataObjectDescriptions = annotationsInfo[3];
			} else {
				// XXX
			}

		} catch (HDFException e) {
			throw new RuntimeException ("HDFException occurred while accessing to annotation routines ", e);
		}
	}

	/**
	 * Builds an returns a <code>List</code> of {@link H4Annotation}s
	 * available for a specific data object, given the required type of
	 * annotation, and the TAG and reference of the data object.<BR>
	 * If you are looking for file annotations, you have to use
	 * {@link H4AnnotationManager#getH4Annotations(int)} instead.
	 * 
	 * @param annotationType
	 *            the required type of annotation. Supported values are
	 *            <code>HDFConstants.AN_DATA_DESC</code> for data object
	 *            descriptions and <code>HDFConstants.AN_DATA_LABEL</code> for
	 *            data object labels. File annotations will not retrieved by
	 *            this method since specifying TAG and Reference does not have
	 *            sense for file. Anyway, using this method for requesting file
	 *            annotations will return <code>null</code>.
	 * @param requiredTag
	 *            the TAG of the required object.
	 * @param requiredReference
	 *            the reference of the required object.
	 * @return a <code>List</code> of {@link H4Annotation}s.
	 * @throws HDFException
	 */
	public List getH4Annotations(final int annotationType,
			final short requiredTag, final short requiredReference)
			throws HDFException {
		List annotations = null;
		switch (annotationType) {
		case HDFConstants.AN_DATA_DESC:
		case HDFConstants.AN_DATA_LABEL:
			final int numTag = HDFLibrary.ANnumann(identifier, annotationType,
					requiredTag, requiredReference);
			if (numTag > 0) {
				final int annIDs[] = new int[numTag];
				HDFLibrary.ANannlist(identifier, annotationType, requiredTag,
						requiredReference, annIDs);
				annotations = new ArrayList(numTag);
				for (int k = 0; k < numTag; k++) {
					H4Annotation annotation = new H4Annotation(annIDs[k]);
					annotations.add(k, annotation);
				}
			}
		}
		return annotations;
	}

	/**
	 * Use this method to retrieve the <code>List</code> of
	 * {@link H4Annotation}s available for the file represented by the
	 * {@link H4File} owner of this <code>H4AnnotationManager</code>. If you
	 * ar looking for data object annotations, you have to use
	 * {@link H4AnnotationManager#getH4Annotations(int, short, short)} instead,
	 * since data object annotations are related to a specific Object identified
	 * by a couple <TAG,reference>.
	 * 
	 * @param annotationType
	 *            the required type of annotation. Supported values are
	 *            <code>HDFConstants.AN_FILE_DESC</code> and
	 *            <code>HDFConstants.AN_FILE_LABEL</code>. Anyway, using this
	 *            method for requesting data object annotations will return
	 *            <code>null</code>.
	 * @return a <code>List</code> of {@link H4Annotation}s.
	 * @throws HDFException
	 */
	public List getH4Annotations(final int annotationType) throws HDFException {
		List annotations = null;
		switch (annotationType) {
		case HDFConstants.AN_FILE_LABEL:
			if (nFileLabels != 0) {
				annotations = new ArrayList(nFileLabels);
				for (int i = 0; i < nFileLabels; i++) {
					final int annID = HDFLibrary.ANselect(identifier, i,
							HDFConstants.AN_FILE_LABEL);
					H4Annotation annotation = new H4Annotation(annID);
					annotations.add(i, annotation);
				}
			}
			break;
		case HDFConstants.AN_FILE_DESC:
			if (nFileDescriptions != 0) {
				annotations = new ArrayList(nFileDescriptions);
				for (int i = 0; i < nFileDescriptions; i++) {
					final int annID = HDFLibrary.ANselect(identifier, i,
							HDFConstants.AN_FILE_DESC);
					H4Annotation annotation = new H4Annotation(annID);
					annotations.add(i, annotation);
				}
			}
			break;
		}
		return annotations;
	}

	/**
	 * End access to the underlying annotation routine interface.
	 */
	public void close() {
		try {
			if (identifier != HDFConstants.FAIL){
				HDFLibrary.ANend(identifier);
				identifier=HDFConstants.FAIL;
			}
				
		} catch (HDFException e) {
			// XXX
		}
	}
}
