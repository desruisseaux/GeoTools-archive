package org.geotools.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.xml.impl.AttributeEncodeExecutor;
import org.geotools.xml.impl.BindingFactoryImpl;
import org.geotools.xml.impl.BindingLoader;
import org.geotools.xml.impl.BindingPropertyExtractor;
import org.geotools.xml.impl.BindingWalker;
import org.geotools.xml.impl.BindingWalkerFactoryImpl;
import org.geotools.xml.impl.ElementEncodeExecutor;
import org.geotools.xml.impl.ElementEncoder;
import org.geotools.xml.impl.GetPropertyExecutor;
import org.geotools.xml.impl.SchemaIndexImpl;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Xml Encoder, taking objects and encoding them as xml.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class Encoder {

	/** the schema + index **/
	private XSDSchema schema;
	private SchemaIndex index;
	
	/** binding factory + context **/
	private BindingLoader bindingLoader;
	private MutablePicoContainer context;
	
	/** element encoder */
	private ElementEncoder encoder;
	
	/** factory for creating nodes **/
	private Document doc;
	/** namespaces */
	private NamespaceSupport namespaces;
	 
	/** document serializer **/
	private XMLSerializer serializer;
	
	/** schema location */
	private HashMap schemaLocations;
	
	/**
	 * Logger logger;
	 */
	private Logger logger;
	
	public Encoder(Configuration configuration, XSDSchema schema) {
		this.schema = schema;
		
		index = new SchemaIndexImpl(new XSDSchema[]{schema});
		
		bindingLoader = new BindingLoader();
		bindingLoader.setContainer(
			configuration.setupBindings( bindingLoader.getContainer() ) 
		);
		
		//create the context
		context = new DefaultPicoContainer();
		
		//register hte binding factory in the context
		BindingFactory bindingFactory = new BindingFactoryImpl( bindingLoader );
        context.registerComponentInstance( bindingFactory );
        
        //register the element encoder in the context
        encoder = new ElementEncoder( bindingLoader, context );
        context.registerComponentInstance( encoder );
        
        //register the schema index
        context.registerComponentInstance( index );
        
        //register a default property extractor
        BindingPropertyExtractor extractor = new BindingPropertyExtractor( bindingLoader, context );        
        context.registerComponentInstance( extractor );
        
        BindingWalkerFactoryImpl bwFactory = new BindingWalkerFactoryImpl( bindingLoader, context );
        context.registerComponentInstance( bwFactory );
        
        //pass the context off to the configuration
		context = configuration.setupContext(context);
		encoder.setContext( context );
		extractor.setContext( context );
		bwFactory.setContext( context );
		
		//schema location setup
		schemaLocations = new HashMap();
		
		 // get a logger from the context
        logger = (Logger) context.getComponentInstanceOfType(Logger.class);

        if (logger == null) {
            //create a default
            logger = Logger.getLogger("org.geotools.xml");
            context.registerComponentInstance(logger);
        }

        encoder.setLogger( logger );
	}
	
	/**
	 * Sets the schema location for a particular namespace uri.
	 * <p>
	 * Registering a schema location will include it on the "schemaLocation" attribute of the 
	 * root element of the encoding.
	 * </p>
	 * @param namespaceURI A namespace uri.
	 * @param location A schema location.
	 *
	 */
	public void setSchemaLocation( String namespaceURI, String location ) {
		schemaLocations.put( namespaceURI, location );
	}
	
	public void write(Object object, QName name, OutputStream out) 
		throws IOException, SAXException {
		
		//create the document seriaizer
		OutputFormat format = new OutputFormat();
		format.setIndent(2);
		format.setIndenting(true);
		
		serializer = new XMLSerializer(out,format);
		serializer.setNamespaces( true );
		serializer.startDocument();
		
		//write out all the namespace prefix value mappings
		namespaces = new NamespaceSupport();
		for (Iterator itr = schema.getQNamePrefixToNamespaceMap()
			.entrySet().iterator(); itr.hasNext();) {
			
			Map.Entry entry = (Map.Entry)itr.next();
			String pre = (String) entry.getKey();
			String ns = (String) entry.getValue();
			
			if( XSDUtil.SCHEMA_FOR_SCHEMA_URI_2001.equals( ns ) ) 
				continue;
			
			serializer.startPrefixMapping(pre,ns);
			serializer.endPrefixMapping(pre);
			
			namespaces.declarePrefix( pre != null ? pre : "" , ns );
		}
		
		//create the document
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
		try {
			doc = docFactory.newDocumentBuilder().newDocument();
		} 
		catch (ParserConfigurationException e) {
			new IOException().initCause(e);
		}
		//maintain a stack of (encoding,element declaration pairs)
		Stack encoded = new Stack();
		encoded.add(new EncodingEntry(object,index.getElementDeclaration(name)));
		
		while(!encoded.isEmpty()) {
			EncodingEntry entry = (EncodingEntry)encoded.peek();
			
			if (entry.encoding != null) {
				
				//element has been started, get the next child
				if (!entry.children.isEmpty()) {
					Object[] child = (Object[])entry.children.get(0);
					XSDElementDeclaration element = (XSDElementDeclaration) child[0];
					Iterator itr = (Iterator) child[1];
					
					if (itr.hasNext()) {
						//add the next object to be encoded to the stack
						encoded.push(new EncodingEntry(itr.next(),element));
					}
					else {
						//this child is done, remove from child list
						entry.children.remove(0);
					}
				}
				else {
					// no more children, finish the element
					end(entry.encoding);
					encoded.pop();
				}
				
			}
			else {
				//start the encoding of the entry
				
				//first make sure the element is not abstract
				if ( entry.element.isAbstract() ) {
					//look for a non abstract substitute
					List sub = entry.element.getSubstitutionGroup();
					if ( sub.size() > 0 ) {
						//match up by type
						List matches = new ArrayList();
						for ( Iterator s = sub.iterator(); s.hasNext(); ) {
							XSDElementDeclaration e = (XSDElementDeclaration) s.next();
							if ( e.equals( entry.element ) ) 
								continue;
							
							if ( e.getName() == null ) 
								continue;
							
							//look up hte binding
							Binding binding = bindingLoader.loadBinding( 
								new QName( e.getTargetNamespace(), e.getName() ), context 
							);
							if ( binding == null ) {
								//try the type
								XSDTypeDefinition type = e.getType();
								if ( type.getName() == null ) 
									continue;
								
								binding = bindingLoader.loadBinding( 
									new QName( type.getTargetNamespace(), type.getName() ), context 
								);
							}
							
							if ( binding == null ) 
								continue;
							
							//match up the type
							if ( binding.getType().isAssignableFrom( entry.object.getClass() ) ) {
								//we have a match
								matches.add( e );
							}
						}
						
						//if one, we are gold
						if ( matches.size() == 1 ) {
							entry.element = (XSDElementDeclaration) matches.get( 0 );	
						}
						//if multiple we have a problem, jsut die
						else if ( matches.size() > 0 ) {
							String msg = "Found multiple non-abstract bindings for " + entry.element.getName();
							throw new IllegalStateException( msg );
						}
						
						//if zero, just use the absttract element
					}
				}
				
				if ( entry.element.isAbstract() ) {
					logger.warning( entry.element.getName() + " is abstract" );
				}
				
				entry.encoding = (Element) encode(entry.object,entry.element);
				
				//add any more attributes
				List attributes = Schemas.getAttributeDeclarations(entry.element);
				for (Iterator itr = attributes.iterator(); itr.hasNext();) {
					XSDAttributeDeclaration attribute = 
						(XSDAttributeDeclaration)itr.next();
					
					//do not encode the attribute if it has already been 
					// encoded by the parent
					String ns = attribute.getTargetNamespace();
					String local = attribute.getName();
					if (entry.encoding.getAttributeNS(ns,local) != null 
						&& !"".equals(entry.encoding.getAttributeNS(ns,local)))
						continue;
					
					//get the object(s) for this attribute
					GetPropertyExecutor executor = 
						new GetPropertyExecutor(entry.object,attribute);
					
					new BindingWalker(bindingLoader,context).walk(entry.element,executor);
					
					if (executor.getChildObject() != null) {
						//encode the attribute
						Attr attr = (Attr) encode(executor.getChildObject(),attribute);
						if (attr != null) {
							entry.encoding.setAttributeNodeNS(attr);
						}
					}
				}
				
				//write out the leading edge of the element
				if ( schemaLocations != null ) {
					//root element, add schema locations if set
					if ( !schemaLocations.isEmpty() ) {
						//declare the schema instance mapping
						serializer.startPrefixMapping( "xsi", XSDUtil.SCHEMA_INSTANCE_URI_2001 );
						serializer.endPrefixMapping( "xsi" );
						namespaces.declarePrefix( "xsi", XSDUtil.SCHEMA_INSTANCE_URI_2001 );
						
						StringBuffer schemaLocation = new StringBuffer();
						for ( Iterator e = schemaLocations.entrySet().iterator(); e.hasNext(); ) {
							Map.Entry tuple = (Map.Entry) e.next();
							String namespaceURI = (String) tuple.getKey();
							String location = (String) tuple.getValue();
							
							schemaLocation.append( namespaceURI + " " + location );
							if ( e.hasNext() ) {
								schemaLocation.append( " " );
							}
						}
						
						entry.encoding.setAttributeNS( 
							XSDUtil.SCHEMA_INSTANCE_URI_2001, "xsi:schemaLocation", schemaLocation.toString() 
						);
					}
					
					schemaLocations = null;
				}
				
				start(entry.encoding);
				
				//get children by processing propertyExtractor "extension point"
				List propertyExtractors = 
					Schemas.getComponentInstancesOfType( context, PropertyExtractor.class ); 
					
				for ( Iterator pe = propertyExtractors.iterator(); pe.hasNext(); ) {
					PropertyExtractor propertyExtractor = (PropertyExtractor) pe.next();
					if ( propertyExtractor.canHandle( entry.object ) ) {
						List extracted = propertyExtractor.properties( entry.object, entry.element );
					O:	for ( Iterator e = extracted.iterator(); e.hasNext(); ) {
							Object[] tuple = (Object[]) e.next();
							XSDParticle particle = (XSDParticle) tuple[ 0 ];
							XSDElementDeclaration child = (XSDElementDeclaration) particle.getContent();
							if ( child.isElementDeclarationReference() ) {
								child = child.getResolvedElementDeclaration();
							}
							
							//do not encode the element if the parent has already 
							// been encoded by the parent
							String ns = child.getTargetNamespace();
							String local = child.getName();
							for (int i = 0; i < entry.encoding.getChildNodes().getLength(); i++) {
								Node node = entry.encoding.getChildNodes().item(i);
								if (node instanceof Element) {
									if (ns != null) {
										if (ns.equals(node.getNamespaceURI()) && 
											local.equals(node.getLocalName())) {
											continue O;
										}
									}
									else {
										if (local.equals(node.getLocalName()))
											continue O;
									}
								}
							}
							
							Object obj = tuple[ 1 ];
							
							if ( obj == null ) {
								if ( particle.getMinOccurs() == 0 ) {
									//cool
								}
								else {
									//log an error
									logger.warning( 
										"Property " + ns + ":"  + local + " not found but minoccurs > 0 "
									);
								}
								
								//skip this regardless
								continue;
							}
							
							if ( particle.getMaxOccurs() == -1 || particle.getMaxOccurs() > 1 ) {
								//may have a collection or array, unwrap it
								Iterator iterator = null;
								if ( obj.getClass().isArray() ) {
									Object[] array = (Object[]) obj;
									iterator = Arrays.asList( array ).iterator();
								}
								else if ( obj instanceof Collection ) {
									Collection collection = (Collection) obj;
									iterator = collection.iterator();
								}
								else {
									iterator = new SingleIterator( obj );
								}
								
								entry.children.add( new Object[]{ child,iterator} );
							}
							else {
								//only one, just add the object
								entry.children.add(
									new Object[]{
										child,
										new SingleIterator( obj )
									}
								);
							}
						}
					}
				}
				
			}
		}
		
		serializer.endDocument();
	}		
		
	protected Node encode(Object object,XSDNamedComponent component) {
		
		if (component instanceof XSDElementDeclaration) {
			XSDElementDeclaration element = (XSDElementDeclaration)component;
			
			return encoder.encode( object, element, doc );
		}
		else if (component instanceof XSDAttributeDeclaration) {
			XSDAttributeDeclaration attribute = 
				(XSDAttributeDeclaration)component;
			
			AttributeEncodeExecutor encoder = 
				new AttributeEncodeExecutor(object,attribute,doc);
			
			new BindingWalker(bindingLoader,bindingLoader.getContainer())
				.walk(attribute,encoder);
			
			return encoder.getEncodedAttribute();
		}
		
		return null;
	}
	
	protected void start(Element element) throws SAXException {
		
		String uri = element.getNamespaceURI();
		String local = element.getLocalName();
		
		String qName = element.getLocalName();
		
		uri = uri != null ? uri : namespaces.getURI( "" );
		qName = namespaces.getPrefix( uri ) + ":" + qName;
		
		DOMAttributes atts = new DOMAttributes(element.getAttributes(), namespaces );
		serializer.startElement(uri,local,qName,atts);
		
		//write out any text
		for (int i = 0; i < element.getChildNodes().getLength(); i++) {
			Node node = (Node)element.getChildNodes().item(i);
			if (node instanceof Text) {
				char[] ch = ((Text)node).getData().toCharArray();
				serializer.characters(ch,0,ch.length);
			}
		}
		
		//write out any child elements
		for (int i = 0; i < element.getChildNodes().getLength();i++) {
			Node node = (Node)element.getChildNodes().item(i);
			if (node instanceof Element) {
				start((Element)node);
				end((Element)node);
			}
		}
		
		//push a new context for children, declaring the default prefix to be the one of this 
		// element
		namespaces.pushContext();
		if ( uri != null ) {
			namespaces.declarePrefix( "", uri );	
		}
	}
	
	protected void end(Element element) throws SAXException {
		//push off last context
		namespaces.popContext();
		
		String uri = element.getNamespaceURI();
		String local = element.getLocalName();
		
		String qName = element.getLocalName();
		if (element.getPrefix() != null && !"".equals(element.getPrefix()))
			qName = element.getPrefix() + ":" + qName; 
		
		serializer.endElement(uri,local,qName);	
	}
	
	private static class NullIterator implements Iterator {

		public void remove() {
			// do nothing
		}
	
		public boolean hasNext() {
			return false;
		}
	
		public Object next() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class SingleIterator implements Iterator {
		Object object;
		boolean more;
		
		public SingleIterator(Object object) {
			this.object = object;
			more = true;
		}

		public void remove() {
			//unsupported
		}

		public boolean hasNext() {
			return more;
		}

		public Object next() {
			more = false;
			return object;
		}
	}
	
	/**
	 * Encoding stack entries.
	 */
	private static class EncodingEntry {
		
		public Object object;
		
		public XSDElementDeclaration element;
		public Element encoding;
		
		public List children; //list of (element,iterator) tuples
		 
		public EncodingEntry(Object object, XSDElementDeclaration element) {
			this.object = object;
			this.element = element;
			
			children = new ArrayList();
		}
		
	}
	
	private static class DOMAttributes implements Attributes {

		NamedNodeMap atts;
		NamespaceSupport namespaces;
		
		public DOMAttributes(NamedNodeMap atts, NamespaceSupport namespaces) {
			this.atts = atts;
			this.namespaces = namespaces;
		}
		
		public int getLength() {
			return atts.getLength();
		}

		public String getLocalName(int index) {
			return atts.item(index).getLocalName();
		}

		public String getQName(int index) {
			Node n = atts.item(index);
			String uri = n.getNamespaceURI();
			String prefix = uri != null ? namespaces.getPrefix( uri ) : null;
			
			if ( prefix != null ) {
				return prefix + ":" + n.getLocalName();
			}
			
			return n.getLocalName();
		}

		public String getType(int index) {
			return "CDATA"; //TODO: this properly
		}

		public String getURI(int index) {
			return atts.item(index).getNamespaceURI();
		}

		public String getValue(int index) {
			return atts.item(index).getNodeValue();
		}

		public int getIndex(String qName) {
			String pre = null;
			String local = null;
			
			if (qName.lastIndexOf(':') != -1) {
				String[] split = qName.split(":");
				pre = split[0];
				local = split[1];
			}
			else {
				pre = "";
				local = qName;
			}
			
			for (int i = 0; i < atts.getLength(); i++) {
				Node att = (Node)atts.item(i);
				if (att.getLocalName().equals(local)) {
					String apre = att.getPrefix();
					if (apre == null)
						apre = "";
							
					if (pre.equals(apre))
						return i;
				}
			}
			
			return -1;
		}

		public String getType(String qName) {
			return getType(getIndex(qName));
		}

		public String getValue(String qName) {
			return getValue(getIndex(qName));
		}

		public int getIndex(String uri, String localName) {
			if (uri == null || uri.equals(""))
				return getIndex(localName);
			
			return getIndex(uri + ":" + localName);
		}

		public String getType(String uri, String localName) {
			return getType(getIndex(uri,localName));
		}

		public String getValue(String uri, String localName) {
			return getValue(getIndex(uri,localName));
		}
		
	}
}

