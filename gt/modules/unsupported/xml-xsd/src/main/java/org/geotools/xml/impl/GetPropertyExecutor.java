package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;

/**
 * Gets children from a parent object, visiting bindings in teh 
 * hierachy until one is found.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GetPropertyExecutor implements BindingWalker.Visitor {

	/** parent + child objects **/
	Object parent;
	Object child;
	
	/** declaration (element or attribute) + qualified name**/
	QName name;
	
	public GetPropertyExecutor(Object parent, XSDNamedComponent content) {
		this.parent = parent;
		
		name = new QName(content.getTargetNamespace(),content.getName());
	}
	
	public Object getChildObject() {
		return child;
	}
	
	public void visit(Binding binding) {
		//TODO: visit should return a boolena to signify wether to continue
		if (child != null)
			return;
		
		if (binding instanceof ComplexBinding) {
			ComplexBinding complex = (ComplexBinding)binding;
			
			child = complex.getProperty(parent, name);
		}
	}

}