package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.geotools.xml.Binding;
import org.geotools.xml.BindingFactory;
import org.picocontainer.MutablePicoContainer;

public class BindingFactoryImpl implements BindingFactory {

	MutablePicoContainer context;
	BindingLoader loader;
	
	public BindingFactoryImpl( BindingLoader loader ) {
		this.loader = loader;
	}
	
	public Binding createBinding( QName name ) {
		return loader.loadBinding( name, context );
	}
	
	public void setContext(MutablePicoContainer context) {
		this.context = context;
	} 

}
