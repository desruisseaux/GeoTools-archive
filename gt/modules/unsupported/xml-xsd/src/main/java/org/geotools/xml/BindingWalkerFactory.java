package org.geotools.xml;

import org.geotools.xml.impl.BindingWalker;

/**
 * Factory used by bindings to create a new binding walker.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface BindingWalkerFactory {

	/**
	 * Creates a new binding walker.
	 * 
	 * @return A new binding walker.
	 */
	BindingWalker createBindingWalker();
}
