package org.geotools.xml;

import org.eclipse.xsd.XSDFeature;
import org.geotools.xml.impl.BindingWalker;

/**
 * Factory made available to bindings to walk over a bindign execution chain.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 * TODO: rename, this isnt really a factory!!
 */
public interface BindingWalkerFactory {

	/**
	 * Walks over the bindings for a particular xml component.
	 * 
	 */
	void walk( XSDFeature component, BindingWalker.Visitor visitor );
}
