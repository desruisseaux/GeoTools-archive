/*
 * Created on May 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Encoder;
import com.vividsolutions.xdo.Node;
import com.vividsolutions.xdo.PluginFinder;
import com.vividsolutions.xdo.Strategy;
import com.vividsolutions.xdo.StrategyBuilder;
import com.vividsolutions.xdo.xsi.Import;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GTStrategyBuilder extends Converter implements StrategyBuilder {

	/* (non-Javadoc)
	 * @see com.vividsolutions.xdo.StrategyBuilder#build(com.vividsolutions.xdo.xsi.Element, java.util.Map)
	 */
	public Strategy build(com.vividsolutions.xdo.xsi.Element type, Map hints) {
		Schema s = SchemaFactory.getInstance(type.getNamespace());
		
		// find element
		if(s.getElements()!=null){
			for(int i=0;i<s.getElements().length;i++)
				if(s.getElements()[i] != null && (type == s.getElements()[i] || (type.getName()!=null && type.getName().equals(s.getElements()[i].getName()))))
					return new WrappedStrategy(s.getElements()[i].getType());
		}
		
		return null;
	}

	private static class WrappedStrategy extends Strategy{

		/**
		 * @param name
		 * @param id
		 * @param namespace
		 * @param target
		 */
		public WrappedStrategy(Type t) {
			super(t.getName(), null, t.getNamespace(),t.getInstanceType() );
			type = t;
		}
		private Type type = null;

		/* (non-Javadoc)
		 * @see com.vividsolutions.xdo.Strategy#canDecode(com.vividsolutions.xdo.xsi.Element, java.util.Map)
		 */
		public boolean canDecode(com.vividsolutions.xdo.xsi.Element element, Map hints) {
			return element!=null && element.getType()!=null && type.equals(element.getType());
		}

		/* (non-Javadoc)
		 * @see com.vividsolutions.xdo.Strategy#decode(com.vividsolutions.xdo.xsi.Element, com.vividsolutions.xdo.Node[], org.xml.sax.Attributes, java.util.Map)
		 */
		public Object decode(com.vividsolutions.xdo.xsi.Element element, final Node[] children, Attributes attrs, Map hints) throws SAXException, OperationNotSupportedException {
			 ElementValue[] value = new  ElementValue[children == null?0:children.length];
			 for(int i=0;i<value.length;i++){
			 	final int k = i;
			 	value[i] = new ElementValue(){
					public Element getElement() {
						return convert(children[k].element);
					}

					public Object getValue() {
						return children[k].value;
					}
			 	
			 	};
			 }
			return type.getValue(convert(element),value,attrs,hints);
		}

		/* (non-Javadoc)
		 * @see com.vividsolutions.xdo.Strategy#cache(com.vividsolutions.xdo.xsi.Element, java.util.Map)
		 */
		public boolean cache(com.vividsolutions.xdo.xsi.Element element, Map hints) {
			if(canDecode(element,hints) && type instanceof ComplexType)
				return ((ComplexType)type).cache(convert(element),hints);
			return true;
		}

		/* (non-Javadoc)
		 * @see com.vividsolutions.xdo.Strategy#canEncode(com.vividsolutions.xdo.xsi.Element, java.util.Map)
		 */
		public boolean canEncode(com.vividsolutions.xdo.xsi.Element element, Map hints) {
			return type.canEncode(convert(element),null,hints);
		}

		/* (non-Javadoc)
		 * @see com.vividsolutions.xdo.Strategy#encode(com.vividsolutions.xdo.Node, com.vividsolutions.xdo.Encoder, java.util.Map)
		 */
		public void encode(Node value, Encoder output, Map hints) throws IOException, OperationNotSupportedException {
			type.encode(convert(value.element),value.value,new WrappedPrintHandler(output,value.element.getNamespace()),hints);
		}
	}
	
	private static class WrappedPrintHandler implements PrintHandler{

		Encoder out;
		com.vividsolutions.xdo.xsi.Schema schema;
		public WrappedPrintHandler(Encoder output, URI ns){
			this.out = output;
			schema = PluginFinder.getInstance().getSchemaBuilder().build(ns);
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#startElement(java.net.URI, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(URI namespaceURI, String localName, Attributes attributes) throws IOException {
			out.startElement(lookup(namespaceURI,localName),attributes);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#element(java.net.URI, java.lang.String, org.xml.sax.Attributes)
		 */
		public void element(URI namespaceURI, String localName, Attributes attributes) throws IOException {
			out.element(lookup(namespaceURI,localName),attributes);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#endElement(java.net.URI, java.lang.String)
		 */
		public void endElement(URI namespaceURI, String localName) throws IOException {
			out.endElement(lookup(namespaceURI,localName));
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#characters(char[], int, int)
		 */
		public void characters(char[] arg0, int arg1, int arg2) throws IOException {
			out.characters(arg0,arg1,arg2);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#characters(java.lang.String)
		 */
		public void characters(String s) throws IOException {
			out.characters(s);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#ignorableWhitespace(char[], int, int)
		 */
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws IOException {
			out.ignorableWhitespace(arg0,arg1,arg2);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#startDocument()
		 */
		public void startDocument() throws IOException {
			out.startDocument();
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#endDocument()
		 */
		public void endDocument() throws IOException {
			out.endDocument();
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#getDocumentSchema()
		 */
		public Schema getDocumentSchema() {
			return convert(schema);
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#findElement(java.lang.Object)
		 */
		public Element findElement(Object value) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#findElement(java.lang.String)
		 */
		public Element findElement(String name) {
			return convert(lookup(null,name));
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.PrintHandler#getHint(java.lang.Object)
		 */
		public Object getHint(Object key) {
			return null;
		}
		
		private com.vividsolutions.xdo.xsi.Element lookup(URI ns, String name){
			com.vividsolutions.xdo.xsi.Schema s = null;
			if(ns == null || ns.equals(schema.getTargetNamespace())){
				s = schema;
			}else{
				Import[] imp = schema.getImports();
				if(imp != null)
					for(int i=0;i<imp.length && s == null;i++){
						if(imp[i] != null && imp[i].getSchema()!=null && ns.equals(imp[i].getSchema().getTargetNamespace()))
							s = imp[i].getSchema();
					}
			}
			// s loaded
			if(s!=null){
				com.vividsolutions.xdo.xsi.Element[] elems = s.getElements();
				if(elems != null)
					for(int i=0;i<elems.length;i++){
						if(elems[i]!=null && name.equals(elems[i].getName()))
							return elems[i];
					}
			}
			return null;
		}
	}
}
