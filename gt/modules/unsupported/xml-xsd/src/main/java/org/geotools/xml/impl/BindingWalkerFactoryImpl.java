package org.geotools.xml.impl;

import org.eclipse.xsd.XSDFeature;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.impl.BindingWalker.Visitor;
import org.picocontainer.MutablePicoContainer;

public class BindingWalkerFactoryImpl implements BindingWalkerFactory {

	BindingLoader bindingLoader;
	MutablePicoContainer context;
	
	public BindingWalkerFactoryImpl( BindingLoader bindingLoader, MutablePicoContainer context ) {
		this.bindingLoader = bindingLoader;
		this.context = context;
	}
	
	public void walk( XSDFeature component, Visitor visitor ) {
		new BindingWalker( bindingLoader ).walk( component, visitor, context );
	}
	
	public void setContext(MutablePicoContainer context) {
		this.context = context;
	}
}
