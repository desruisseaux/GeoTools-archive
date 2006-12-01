/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml;

import javax.xml.namespace.QName;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *  A strategy for parsing elements in an instance document which are of
 *  complex type.
 *
 *        <p>
 *        Complex types contain child elements, and attributes. A complex strategy
 *        has the ability to
 *        <p>
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
    void initialize(ElementInstance instance, Node node,
        MutablePicoContainer context);

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
     *
     * <p>
     * Complex objects are encoded as elements in a document. The <param>value</param>
     * parameter is the encoded element, created by the parent binding. For the 
     * first binding in the execution chain this is just an empty element ( no 
     * children or attributes ). The binding has the choice to return <param>value</param>
     * or to create a new element to return.
     * </p>
     *
     *	<p>
     * This method may choose to create child elements and attributes for the element. 
     * Or as an alternative return the object values for these contructs in 
     * {@link #getProperty(Object, QName)}. 
     *	</p>
     *
     * @param object The object being encoded.
     * @param document The document containing the encoded element.
     * @param value The object as encoded by the parent binding.
     * 
     * @return The element for the objcet being encoded, or <code>null</code>
     *
     */
    Element encode(Object object, Document document, Element value) 
    	throws Exception;

    /**
     * Returns a property of a particular object which corresponds to the 
     * specified name.
     *
     * </p>
     * <p>This method should just return null in the event that the object being
     * encoded is an leaf in its object model.</p>
     *
     * <p>
     * For multi-values properties ( maxOccurs > 0 ), this method may return an 
     * instance of {@link java.util.Collection}, {@link java.util.Iterator}, or 
     * an array.
     * </p>
     *
     * @param object The object being encoded.
     * @param name The name of the property to obtain.
     *
     * @return The value of the property, or <code>null</code>.
     */
    Object getProperty(Object object, QName name) throws Exception;
}
