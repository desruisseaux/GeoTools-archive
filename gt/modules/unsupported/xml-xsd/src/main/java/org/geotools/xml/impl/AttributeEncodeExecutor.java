package org.geotools.xml.impl;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.SimpleBinding;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class AttributeEncodeExecutor implements BindingWalker.Visitor {

	/** the object being encoded **/
	Object object;
	
	/** the attribute being encoded **/
	XSDAttributeDeclaration attribute;
	
	/** the encoded value **/
	Attr encoding;
	
	/** the document / factory **/
	Document document;
	
	public AttributeEncodeExecutor(
		Object object, XSDAttributeDeclaration attribute, Document document
	) {
		this.object = object;
		this.attribute = attribute;
		this.document = document;
		
		encoding = document
			.createAttributeNS(attribute.getTargetNamespace(), attribute.getName());
			
	}
	
	public Attr getEncodedAttribute() {
		return encoding;
	}
	
	public void visit(Binding binding) {
		if (binding instanceof SimpleBinding) {
			SimpleBinding simple = (SimpleBinding)binding;
			
			try {
				encoding.setValue(simple.encode(object,encoding.getValue()));
			} 
			catch (Throwable t) {
				String msg = "Encode failed for " + attribute.getName() +
					". Cause: " + t.getLocalizedMessage();
				throw new RuntimeException(msg,t);
			}
			
		}
	}
	
}
