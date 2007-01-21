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
    public void initializeChildContext(ElementInstance childInstance, Node node,
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
     * Subclasses should ovverride this method if need be, the default implementation 
     * returns <param>value</param>.
     * 
     * @see ComplexBinding#encode(Object, Document, Element).
     */
    public Element encode(Object object, Document document, Element value) 
    	throws Exception {
    	
    	return value;
    }

    /**
     * Subclasses should override this method if need be, the default implementation 
     * returns <code>null</code>.
     * 
     * @see ComplexBinding#getProperty(Object, QName)
     */
    public Object getProperty(Object object, QName name) throws Exception {
    	//do nothing, subclasses should override
    	return null;
    }
}
