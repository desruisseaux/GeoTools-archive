package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;



/**
 * Abstract class representing a HDF variable
 * 
 * @author Daniele Romagnoli
 */
public abstract class H4Variable extends H4DecoratedObject implements IHObject {

	/**
	 * The name of this Variable
	 * 
	 * @uml.property name="name"
	 */
	protected String name = "";

	/**
	 * Getter of the property <code>name</code>
	 * 
	 * @return the name of this Variable.
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}
}
