package org.geotools.xml;

import javax.xml.namespace.QName;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  A strategy for parsing elements in an instance document which are of 
 *  complex type.
 *
 *	<p>
 *	Complex types contain child elements, and attributes. A complex strategy 
 *	has the ability to  
 *	<p>
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface ComplexBinding extends Binding {

	/**
	 * Initializes the context to be used while parsing child elements of the  
	 * complex type.
	 * 
	 * <p>
	 * This method is called when the leading edge of the associated element is
	 * reached. It is used to create context in which child elements will be 
	 * parsed in. The context is in the form of a pico container. For types that
	 * do not need to create context for children this method should do nothing.
	 * </p>
	 * 
	 * @param instance The element being parsed.
	 * @param node The node in the parse tree representing the element being 
	 * parsed. It is important to note that at the time this method is called
	 * the node contains no child element nodes, only child attribute nodes.
	 * @param context The container to be used as context for child strategies. 
	 *  
	 */
	void initialize(ElementInstance instance, Node node, MutablePicoContainer context);
	
	/**
	 * Parses a complex element from an instance document into an object 
	 * representation.
	 * 
	 * <p>
	 * This method is called when the trailing edge of the associated element is 
	 * reached.
	 * </p>
	 *
	 * @param instance The element being parsed.
	 * @param node The node in the parse tree representing the element being 
	 * parsed.
	 * @param value The result of the parse from another strategy in the type 
	 * hierarchy. Could be null if this is the first strategy being executed.
	 * 
	 * @return The parsed object, or null if the component could not be parsed.
	 *
	 * @throws Exception  Strategy objects should not attempt to handle any exceptions.
	 */
	Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception;
	
	/**
	 * Performs the encoding of the object into its xml representation.
	 * <p>
	 * Complex objects are encoded as elements in a document, any attributes 
	 * on the element must be created within this method. Child elements may 
	 * also be created withing this method.
	 * </p>
	 * <p>
	 * The document containing the element may be used to create child
	 * nodes for the element (elements or attributes).
	 * </p>
	 * 
	 * @param object The object being encoded.
	 * @param element The element representing the encoded object.
	 * @param document The document containing the encoded element.
	 * 
	 */
	//void encode(Object object, Element element, Document document) 
	//	throws Exception;
	
	/**
	 * Returns a child object which matches the specified qualified name.
	 * 
	 * <p>This method should just return null in the event that the object being 
	 * encoded is an leaf in its object model.</p>
	 * 
	 * <p>This method should return an array in the event that the qualified 
	 * name mapps to multiple children</p>
	 * 
	 * @param object The object being encoded.
	 * @param name The name of the child.
	 * 
	 * @return The childn to be encoded or null if no such child exists.
	 */
	//Object getChild(Object object, QName name);
}
