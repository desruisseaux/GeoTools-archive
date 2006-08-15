package org.geotools.xml.impl;

import org.picocontainer.MutablePicoContainer;

public abstract class HandlerImpl implements Handler {

	
	MutablePicoContainer context;
	
	public MutablePicoContainer getContext() {
		return context;
	}

	public void setContext(MutablePicoContainer context) {
		this.context = context;
	}
}
