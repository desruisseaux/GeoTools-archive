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
package org.geotools.xml.impl;

import org.geotools.xml.Binding;
import org.picocontainer.MutablePicoContainer;
import javax.xml.namespace.QName;


/**
 * Factory used to create binding objects.
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface BindingFactory {
    /**
     * Loads a binding with a specifc QName into a context.
     *
     * @param type The qualified name of the type of the binding object.
     * @param context The context which is to contain the binding.
     *
     * @return The binding object of the associated type, otherwise null if
     * no such binding could be created.
     *
     */
    Binding loadBinding(QName type, MutablePicoContainer context);

    /**
     * Returns the class of the binding  used to parse the type with the
     * specified qualified name.
     *
     * @param type The qualified name of the type of the binding.
     *
     * @return The binding class, or null if no such class exists.
     */
    Class getBinding(QName type);

    /**
     * @return The container which houses the bindings.
     */
    MutablePicoContainer getContainer();
    
    /**
     * Sets the container which houses bindings.
     * 
     */
    void setContainer( MutablePicoContainer container );
}
