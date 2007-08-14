package it.geosolutions.hdf.object;

import ncsa.hdf.hdflib.HDFConstants;

/**
 * Main abstract class representing a HDF Object.
 */
public abstract class AbstractHObject implements IHObject{

	/**
	 * The numeric identifier associated to this <code>AbstractHObject</code>
	 * 
	 * @uml.property name="identifier"
	 */
	protected int identifier = HDFConstants.FAIL;

	/**
	 *  Getter of the property <code>identifier</code>
	 * 
	 * @return the numeric identifier associated to this
	 *         <code>AbstractHObject</code>
	 * @uml.property name="identifier"
	 */
	public int getIdentifier() {
		return identifier;
	}
}
