package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
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
		
		List properties = new ArrayList();
		
		//first get all the properties that can be infered from teh schema
		List children = encoder.getSchemaIndex().getChildElementParticles( element );
		
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
		
		//second, get the properties which cannot be infereed from the schema
		GetPropertiesExecutor executor = new GetPropertiesExecutor( object );
		encoder.getBindingWalker().walk( element, executor, context );
		
		if ( !executor.getProperties().isEmpty() ) {
			//group into a map of name, list
			MultiHashMap map = new MultiHashMap();
			for ( Iterator p = executor.getProperties().iterator(); p.hasNext(); ) {
				Object[] property = (Object[]) p.next();
				map.put( property[ 0 ], property[ 1 ] );
			}
			
			//turn each map entry into a particle
			HashMap particles = new HashMap();
			for ( Iterator e = map.entrySet().iterator(); e.hasNext(); ) {
				Map.Entry entry = (Map.Entry) e.next();
				QName name = (QName) entry.getKey();
				Collection values = (Collection) entry.getValue();
				
				//find hte element 
				XSDElementDeclaration elementDecl = 
					encoder.getSchemaIndex().getElementDeclaration( name );
				if ( elementDecl == null ) {
					//TODO: resolving like this will return an element no 
					// matter what, modifying the underlying schema, this might
					// be dangerous. What we shold do is force the schema to 
					// resolve all of it simports when the encoder starts
					elementDecl = 
						encoder.getSchema().resolveElementDeclaration( name.getNamespaceURI(), name.getLocalPart() );
				}
				
				//wrap it in a particle
				XSDParticle particle = XSDFactory.eINSTANCE.createXSDParticle();
				particle.setContent( elementDecl );
				
				if ( values.size() > 1 ) {
					//make a multi property
					particle.setMaxOccurs( -1 );
				}
				else {
					//single property
					particle.setMaxOccurs( 1 );
				}
				
				particles.put( name, particle );
			}
			
			//process the particles in order in which we got the properties
			for ( Iterator p = executor.getProperties().iterator(); p.hasNext(); ) {
				Object[] property = (Object[]) p.next();
				QName name = (QName) property[ 0 ];
				
				XSDParticle particle = (XSDParticle) particles.get( name );
				if ( particle == null ) {
					continue;	//already processed, must be a multi property
				}
				
				Collection values = (Collection) map.get( name );
				if ( values.size() > 1 ) {
					//add as is, the encoder will unwrap
					properties.add( new Object[] { particle, values });
				}
				else {
					//unwrap it
					properties.add( new Object[] { particle, values.iterator().next() } ); 
				}
				
				//done with this particle
				particles.remove( name );
			}
			
		}
		
		
		return properties;
	}

}
