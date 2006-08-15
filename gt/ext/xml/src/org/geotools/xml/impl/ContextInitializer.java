package org.geotools.xml.impl;

import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.geotools.xml.impl.BindingWalker.Visitor;
import org.picocontainer.MutablePicoContainer;

public class ContextInitializer implements Visitor {

	ElementInstance instance;
	Node node;
	MutablePicoContainer context;
	
	public ContextInitializer(
		ElementInstance instance, Node node, MutablePicoContainer context
	) {
		this.instance = instance;
		this.node = node;
		this.context = context;
	}
	
	public void visit(Binding strategy) {
		if (strategy instanceof ComplexBinding) {
			ComplexBinding cStrategy = (ComplexBinding)strategy;
			cStrategy.initialize(instance,node,context);
		}
	}
}
