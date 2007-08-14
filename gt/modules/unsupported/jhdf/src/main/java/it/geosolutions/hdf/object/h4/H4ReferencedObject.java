package it.geosolutions.hdf.object.h4;

/**
 * @author Romagnoli Daniele
 */
public class H4ReferencedObject implements IH4ReferencedObject {

	/**
	 * the reference of this object
	 * 
	 * @uml.property name="reference"
	 */
	private int reference;

	public H4ReferencedObject(int ref) {
		reference = ref;
	}

	/**
	 * Getter of the property <code>reference</code>
	 * 
	 * @return the reference of this object.
	 * @uml.property name="reference"
	 */
	public int getReference() {
		return reference;
	}

}
