package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.geotools.xml.Binding;
import org.picocontainer.MutablePicoContainer;

/**
 * Factory used to create binding objects. 
 * 
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 * 
 */
public interface BindingFactory {

	/**
	 * Loads a binding with a specifc QName into a context.
	 * 
	 * @param type The qualified name of the type of the binding object.
	 * @param context The context which is to contain the binding.
	 * 
	 * @return The binding object of the associated type, otherwise null if 
	 * no such binding could be created.
	 * 
	 */
	Binding loadBinding(QName type, MutablePicoContainer context);
	
	/**
	 * Returns the class of the strategy object used to parse the type with the 
	 * specified qualified name.
	 * 
	 * @param type The qualified name of the type of the strategy.
	 * 
	 * @return The strategy class, or null if no such class exists.
	 */
	Class getBinding(QName type);
	
	/**
	 * @return The container which houses the strategy objects.
	 */
	MutablePicoContainer getContainer();
}
