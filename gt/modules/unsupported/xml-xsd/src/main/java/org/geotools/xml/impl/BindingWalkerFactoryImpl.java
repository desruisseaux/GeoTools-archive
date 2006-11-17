package org.geotools.xml.impl;

import org.geotools.xml.BindingWalkerFactory;
import org.picocontainer.MutablePicoContainer;

public class BindingWalkerFactoryImpl implements BindingWalkerFactory {

	BindingLoader bindingLoader;
	MutablePicoContainer context;
	
	public BindingWalkerFactoryImpl( BindingLoader bindingLoader, MutablePicoContainer context ) {
		this.bindingLoader = bindingLoader;
		this.context = context;
	}
	
	public BindingWalker createBindingWalker() {
		return new BindingWalker( bindingLoader, context );
	}
	
	public void setContext(MutablePicoContainer context) {
		this.context = context;
	}
	

}
