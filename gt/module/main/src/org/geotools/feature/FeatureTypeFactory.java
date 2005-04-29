/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.feature;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.xml.gml.GMLSchema;


/**
 * Replaced with use of FeatureTypeBuilder to follow standard pattern
 * naming conventions.
 * 
 * @deprecated Please use FeatureTypeBuilder
 * @author Ian Schneider
 * @version $Id: FeatureTypeFactory.java,v 1.12 2004/02/21 10:16:50 aaime Exp $
 */
public abstract class FeatureTypeFactory extends FeatureTypeBuilder {
    /**
     * An empty public constructor. Subclasses should not provide a
     * constructor.
     */
    public FeatureTypeFactory() {
    	// no op constructor
    }
    
    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit
     *        from. Currently, all types inherit from feature in the opengis
     *        namespace.
     *
     * @return A new FeatureType created from the given arguments.
     *
     * @throws FactoryConfigurationError If there are problems creating a
     *         factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in
     *         some way.
     */
    public static FeatureType newFeatureType(AttributeType[] types,
        String name, URI ns, boolean isAbstract, FeatureType[] superTypes) 
        throws FactoryConfigurationError, SchemaException { 
            return newFeatureType(types, name, ns, isAbstract, superTypes, null);
    }
        
    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit
     *        from. Currently, all types inherit from feature in the opengis
     *        namespace.
     *
     * @return A new FeatureType created from the given arguments.
     *
     * @throws FactoryConfigurationError If there are problems creating a
     *         factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in
     *         some way.
     */
    public static FeatureType newFeatureType(AttributeType[] types,
        String name, URI ns, boolean isAbstract, FeatureType[] superTypes, AttributeType defaultGeometry)
        throws FactoryConfigurationError, SchemaException {
        FeatureTypeFactory factory = newInstance(name);
        factory.addTypes(types);
        factory.setNamespace(ns);
        factory.setAbstract(isAbstract);
        if(defaultGeometry != null)
            factory.setDefaultGeometry((GeometryAttributeType) defaultGeometry);

        if (superTypes != null) {
            factory.setSuperTypes(Arrays.asList(superTypes));
        }

        return factory.getFeatureType();
    }
    
    /**
         * The most specific way to create a new FeatureType.
         *
         * @param types The AttributeTypes to create the FeatureType with.
         * @param name The typeName of the FeatureType. Required, may not be null.
         * @param ns The namespace of the FeatureType. Optional, may be null.
         * @param isAbstract True if this created type should be abstract.
         * @param superTypes A Collection of types the FeatureType will inherit
         *        from. Currently, all types inherit from feature in the opengis
         *        namespace.
         *
         * @return A new FeatureType created from the given arguments.
         *
         * @throws FactoryConfigurationError If there are problems creating a
         *         factory.
         * @throws SchemaException If the AttributeTypes provided are invalid in
         *         some way.
         */
        public static FeatureType newFeatureType(AttributeType[] types,
            String name, URI ns, boolean isAbstract, FeatureType[] superTypes, GeometryAttributeType defaultGeometry)
            throws FactoryConfigurationError, SchemaException {
            FeatureTypeFactory factory = newInstance(name);
            factory.addTypes(types);
            factory.setNamespace(ns);
            factory.setAbstract(isAbstract);

            if (superTypes != null) {
                factory.setSuperTypes(Arrays.asList(superTypes));
            }
            
            if(defaultGeometry != null) {
                factory.setDefaultGeometry(defaultGeometry);
            }

            return factory.getFeatureType();
        }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for
     * calling <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     *
     * @return A new FeatureType created from the given arguments.
     *
     * @throws FactoryConfigurationError If there are problems creating a
     *         factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in
     *         some way.
     */
    public static FeatureType newFeatureType(AttributeType[] types,
        String name, URI ns, boolean isAbstract)
        throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, ns, isAbstract, null);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for
     * calling <code>newFeatureType(types,name,ns,false,null)</code>.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     *
     * @return A new FeatureType created from the given arguments.
     *
     * @throws FactoryConfigurationError If there are problems creating a
     *         factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in
     *         some way.
     */
    public static FeatureType newFeatureType(AttributeType[] types,
        String name, URI ns)
        throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, ns, false);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for
     * calling <code>newFeatureType(types,name,null,false,null)</code>. Useful
     * for test cases or datasources which may not allow a namespace.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     *
     * @return A new FeatureType created from the given arguments.
     *
     * @throws FactoryConfigurationError If there are problems creating a
     *         factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in
     *         some way.
     */
    public static FeatureType newFeatureType(AttributeType[] types, String name)
        throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, GMLSchema.NAMESPACE, false);
    }

    /**
     * Create a new FeatureTypeFactory with the given typeName.
     *
     * @param name The typeName of the feature to create.
     *
     * @return A new FeatureTypeFactory instance.
     *
     * @throws FactoryConfigurationError If there exists a configuration error.
     */
    public static FeatureTypeFactory newInstance(String name)
        throws FactoryConfigurationError {
        FeatureTypeFactory factory = (FeatureTypeFactory) FactoryFinder
            .findFactory("org.geotools.feature.FeatureTypeFactory",
                "org.geotools.feature.DefaultFeatureTypeFactory");
        factory.setName(name);

        return factory;
    }

    /**
     * Create a FeatureTypeFactory which contains all of the AttributeTypes
     * from the given FeatureType. This is simply a convenience method for<br>
     * <code><pre>
     * FeatureTypeFactory factory = FeatureTypeFactory.newInstace();
     * factory.importType(yourTypeHere); 
     * factory.setName(original.getName());
     * factory.setNamespace(original.getNamespace());
     * factory.setNillable(original.isNillable());
     * factory.setDefaultGeometry(original.getDefaultGeometry()); 
     * </pre></code>
     *
     * @param original The FeatureType to obtain information from.
     *
     * @return A new FeatureTypeFactory which is initialized with the state of
     *         the original FeatureType.
     *
     * @throws FactoryConfigurationError If a FeatureTypeFactory cannot be
     *         found.
     */
    public static FeatureTypeFactory createTemplate(FeatureType original)
        throws FactoryConfigurationError {
    	
    	FeatureTypeFactory builder = FeatureTypeFactory.newInstance(original.getTypeName());
        builder.importType(original);
        builder.setNamespace(original.getNamespace());
        builder.setDefaultGeometry(original.getDefaultGeometry());

        FeatureType[] ancestors = original.getAncestors();

        if (ancestors != null) {
        	builder.setSuperTypes(Arrays.asList(ancestors));
        }

        return builder;
    } 

    /**
     * Returns a string representation of this factory.
     *
     * @return The string representing this factory.
     */
    public String toString() {
        String types = "";

        for (int i = 0, ii = getAttributeCount(); i < ii; i++) {
            types += get(i);

            if (i < ii) {
                types += " , ";
            }
        }

        return "FeatureTypeFactory(" + getClass().getName() + ") [ " + types
        + " ]";
    }  

}
