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
 * Base class for complex bindings.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractComplexBinding implements ComplexBinding {
    /**
     * Does nothing, subclasses should override this method.
     */
    public void initialize(ElementInstance instance, Node node,
        MutablePicoContainer context) {
        //does nothing, subclasses should override
    }

    /**
     * This implementation returns {@link Binding#OVERRIDE}.
     * <p>
     * Subclasses should override to change this behaviour.
     * </p>
     */
    public int getExecutionMode() {
        return OVERRIDE;
    }
    
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
     * @param document The document containing the encoded element.
     *
     */
    public Element encode(Object object, Document document, Element value) 
    	throws Exception {
    	
    	return value;
    }

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
    public Object getChild(Object object, QName name) {
    	//do nothing, subclasses should override
    	return null;
    }
}
