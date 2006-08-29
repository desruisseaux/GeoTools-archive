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
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import javax.xml.namespace.QName;


public class BindingFactoryImpl implements BindingFactory {
    MutablePicoContainer container;

    public BindingFactoryImpl() {
        container = new DefaultPicoContainer();
    }

    public Binding loadBinding(QName qName, MutablePicoContainer context) {
        Class bindingClass = getBinding(qName);

        if (bindingClass == null) {
            return null;
        }

        try {
            context.registerComponentImplementation(bindingClass);
        } catch (DuplicateComponentKeyRegistrationException e) {
            //ok, just means that we have already registerd the class
        }

        return (Binding) context.getComponentInstanceOfType(bindingClass);
    }

    public Class getBinding(QName type) {
        ComponentAdapter adapter = container.getComponentAdapter(type);

        if (adapter == null) {
            return null;
        }

        return adapter.getComponentImplementation();
    }

    public MutablePicoContainer getContainer() {
        return container;
    }
}
