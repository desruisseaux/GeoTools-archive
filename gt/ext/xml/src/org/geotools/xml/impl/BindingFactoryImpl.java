package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.geotools.xml.Binding;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

public class BindingFactoryImpl implements BindingFactory {

	MutablePicoContainer container;
	
	public BindingFactoryImpl() {
		container = new DefaultPicoContainer();
	}
	
	public Binding loadBinding(QName qName, MutablePicoContainer context) {
		Class bindingClass = getBinding(qName);
		if (bindingClass == null)
			return null;
		
		try {
			context.registerComponentImplementation(bindingClass);	
		}
		catch(DuplicateComponentKeyRegistrationException e) {
			//ok, just means that we have already registerd the class
		}
		
		return (Binding) context.getComponentInstanceOfType(bindingClass);
	}
	
	public Class getBinding(QName type) {
		ComponentAdapter adapter = container.getComponentAdapter(type);
		if (adapter == null)
			return null;
		
		return adapter.getComponentImplementation();
	}
	
	public MutablePicoContainer getContainer() {
		return container;
	}

}
