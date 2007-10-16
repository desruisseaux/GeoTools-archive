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

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xsd.XSDTypeDefinition;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.geotools.util.Converters;


/**
 * Base class for complex bindings which map to an EMF model class.
 * <p>
 * Provides implementations for:
 * <ul>
 *         <li>{@link ComplexBinding#getProperty(Object, QName)}.
 * </ul>
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractComplexEMFBinding extends AbstractComplexBinding {
    /**
     * Factory used to create model objects
     */
    EFactory factory;

    /**
     * Default constructor.
     * <p>
     * Creatign the binding with this constructor will force it to perform a
     * noop in the {@link #parse(ElementInstance, Node, Object)} method.
     * </p>
     */
    public AbstractComplexEMFBinding() {
    }

    /**
     * Constructs the binding with an efactory.
     *
     * @param factory Factory used to create model objects.
     */
    public AbstractComplexEMFBinding(EFactory factory) {
        this.factory = factory;
    }

    /**
     * Dynamically tries to determine the type of the object using emf naming
     * conventions and the name returned by {@link Binding#getTarget()}.
     * <p>
     * This implementation is a heuristic and is not guarenteed to work. Subclasses
     * may override to provide the type explicity.
     * </p>
     */
    public Class getType() {
        //try to build up a class name 
        String pkg = factory.getClass().getPackage().getName();

        if (pkg.endsWith(".impl")) {
            pkg = pkg.substring(0, pkg.length() - 5);
        }

        String className = getTarget().getLocalPart();

        try {
            return Class.forName(pkg + "." + className);
        } catch (ClassNotFoundException e) {
            //do an underscore check
            if (className.startsWith("_")) {
                className = className.substring(1) + "Type";
            }

            try {
                return Class.forName(pkg + "." + className);
            } catch (ClassNotFoundException e1) {
                //try appending a Type
            }
        }

        return null;
    }

    /**
     * Uses EMF reflection to create an instance of the EMF model object this
     * binding maps to. The properties of the resulting object are set using
     * the the contents of <param>node</param>
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //does this binding actually map to an eObject?
        if (EObject.class.isAssignableFrom(getType()) && (factory != null)) {
            //yes, try and use the factory to dynamically create a new instance

            //get the classname
            String className = getType().getName();
            int index = className.lastIndexOf('.');

            if (index != -1) {
                className = className.substring(index + 1);
            }

            //find the proper create method
            Method create = factory.getClass().getMethod("create" + className, null);

            if (create == null) {
                //no dice
                return value;
            }

            //create the instance
            EObject eObject = (EObject) create.invoke(factory, null);

            //reflectivley set the properties of it
            for (Iterator c = node.getChildren().iterator(); c.hasNext();) {
                Node child = (Node) c.next();
                String property = child.getComponent().getName();
                setProperty(eObject, property, child.getValue());
            }

            for (Iterator a = node.getAttributes().iterator(); a.hasNext();) {
                Node att = (Node) a.next();
                String property = att.getComponent().getName();
                setProperty(eObject, property, att.getValue());
            }

            //check for a complex type with simpleContent, in this case use 
            // the string value (if any) to set the value property
            if (instance.getElementDeclaration().getTypeDefinition().getBaseType() instanceof XSDTypeDefinition) {
                if ((value != null) && EMFUtils.has(eObject, "value")) {
                    setProperty(eObject, "value", value);
                }
            }

            return eObject;
        }

        //could not do it, just return whatever was passed in
        return value;
    }

    /**
     * Internal method for reflectively setting the property of an eobject.
     * <p>
     * Subclasses may override.
     * </p>
     */
    protected void setProperty(EObject eObject, String property, Object value) {
        if (EMFUtils.has(eObject, property)) {
            try {
                if (EMFUtils.isCollection(eObject, property)) {
                    EMFUtils.add(eObject, property, value);
                } else {
                    EMFUtils.set(eObject, property, value);
                }
            } catch (ClassCastException e) {
                //convert to the correct type
                EStructuralFeature feature = EMFUtils.feature(eObject, property);
                Class target = feature.getEType().getInstanceClass();

                if ((value != null) && !value.getClass().isAssignableFrom(target)) {
                    //TODO: log this
                    value = Converters.convert(value, target);
                }

                if (value == null) {
                    //just throw the oringinal exception
                    throw e;
                }
            }
        }
    }

    /**
     * Uses EMF reflection dynamically return the property with the specified
     * name.
     */
    public Object getProperty(Object object, QName name)
        throws Exception {
        if (object instanceof EObject) {
            EObject eObject = (EObject) object;

            if (EMFUtils.has(eObject, name.getLocalPart())) {
                return EMFUtils.get(eObject, name.getLocalPart());
            }
        }

        return null;
    }
}
