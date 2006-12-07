package org.geotools.xml.impl;

import java.util.logging.Logger;

import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.util.Converters;
import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.SimpleBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class ElementEncodeExecutor implements BindingWalker.Visitor {

	/** the object being encoded **/
	Object object;
	
	/** the element being encoded **/
	XSDElementDeclaration element;
	
	/** the encoded value **/
	Element encoding;
	
	/** the document / factory **/
	Document document;
	
	/** logger */
	Logger logger;
	
	public ElementEncodeExecutor(
		Object object, XSDElementDeclaration element, Document document, Logger logger
	) {
		this.object = object;
		this.element = element;
		this.document = document;
		this.logger = logger;
		
//		if ( element.getTargetNamespace() != null ) {
			encoding = document.createElementNS(element.getTargetNamespace(),element.getName());	
//		}
//		else {
//			encoding = document.createElementNS( 
//				element.getSchema().getTargetNamespace(), element.getName() 
//			);
//		}
		
	}
	
	public Element getEncodedElement() {
		return encoding;
	}
	
	public void visit(Binding binding) {
		//ensure that the type of the object being encoded matches the type 
		// of the binding
		if ( binding.getType() == null ) {
			logger.warning( "Binding: " + binding.getTarget() + " does not declare a target type" );
			return;
		}
		
		if ( !binding.getType().isAssignableFrom( object.getClass() ) ) {
			//try to convert 
			Object converted = Converters.convert( object, binding.getType() );
			if ( converted != null ) {
				object = converted;
			}
			else {
				logger.warning( object + "[ " + object.getClass() + " ] is not of type " + binding.getType() );	
				return;
			}
		}
			
		if (binding instanceof ComplexBinding) {
			ComplexBinding complex = (ComplexBinding)binding;
			
			try {
				Element element = complex.encode(object,document,encoding);
				if ( element != null ) {
					encoding = element;
				}
			}
			catch (Throwable t) {
				String msg = "Encode failed for " + element.getName() +
					". Cause: " + t.getLocalizedMessage();
				throw new RuntimeException(msg,t);
			}
		}
		else {
			SimpleBinding simple = (SimpleBinding)binding;
			
			//figure out if the node has any text
			Text text = null;
			for (int i = 0; i < encoding.getChildNodes().getLength(); i++) {
				Node node = (Node)encoding.getChildNodes().item(i);
				if (node instanceof Text) {
					text = (Text) node;
					break;
				}
			}
			
			try {
				String value = 
					simple.encode(object,text != null ? text.getData() : null);
				
				if (value != null) {
					//set the text of the node
					if (text == null) {
						text = document.createTextNode(value);
						encoding.appendChild(text);
					}
					else {
						text.setData(value);
					}
				}
			} 
			catch (Throwable t) {
				String msg = "Encode failed for " + element.getName() +
					". Cause: " + t.getLocalizedMessage();
				throw new RuntimeException(msg,t);
			}
			
		}
	}
	
}
