package org.geotools.xml;

import org.picocontainer.MutablePicoContainer;

/**
 * Base class for complex bindings.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractComplexBinding implements ComplexBinding {

	/**
	 * Does nothing, subclasses should override this method.
	 */
	public void initialize(ElementInstance instance, Node node,
			MutablePicoContainer context) {

		//does nothing, subclasses should override
	}

	/**
	 * This implementation returns {@link Binding#OVERRIDE}.
	 * <p>
	 * Subclasses should override to change this behaviour.
	 * </p>
	 */
	public int getExecutionMode() {
		return OVERRIDE;
	}

}
