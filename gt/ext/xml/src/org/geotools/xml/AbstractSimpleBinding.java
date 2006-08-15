package org.geotools.xml;


/**
 * Base class for complex bindings.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractSimpleBinding implements SimpleBinding {

	/**
	 * This implementation returns {@link Binding#AFTER}.
	 * <p>
	 * Subclasses should override to change this behaviour.
	 * </p>
	 */
	public int getExecutionMode() {
		return AFTER;
	}

}
