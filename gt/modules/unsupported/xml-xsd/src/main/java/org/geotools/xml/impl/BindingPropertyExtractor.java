package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import org.geotools.xml.Encoder;
import org.geotools.xml.PropertyExtractor;
import org.geotools.xml.Schemas;
import org.picocontainer.MutablePicoContainer;

/**
 * Uses {@link org.geotools.xml.ComplexBinding#getProperty(Object, QName)} to obtain 
 * properties from the objecet being encoded.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class BindingPropertyExtractor implements PropertyExtractor {

	Encoder encoder;
	MutablePicoContainer context;
	
	
	public BindingPropertyExtractor ( Encoder encoder, MutablePicoContainer context ) {
		this.encoder = encoder;
		this.context = context;
	}
	
	public boolean canHandle(Object object) {
		return true;
	}

	public void setContext(MutablePicoContainer context) {
		this.context = context;
	}
	
	public List properties(Object object, XSDElementDeclaration element) {
		
		List children = encoder.getSchemaIndex().getChildElementParticles( element );
		
		List properties = new ArrayList();
	O:	for (Iterator itr = children.iterator(); itr.hasNext();) {
			XSDParticle particle = (XSDParticle) itr.next();
			XSDElementDeclaration child = (XSDElementDeclaration) particle.getContent();
			if ( child.isElementDeclarationReference() ) {
				child = child.getResolvedElementDeclaration();
			}
			
			//get the object(s) for this element 
			GetPropertyExecutor executor = 
				new GetPropertyExecutor(object,child);
			
			encoder.getBindingWalker().walk(element,executor,context);
			
			if (executor.getChildObject() != null) {
				properties.add( new Object[] { particle, executor.getChildObject() });
			}
		}
		
		return properties;
	}

}
