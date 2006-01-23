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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.xml.gml.GMLSchema;

/**
 * A basic implementation of FeatureType.
 *
 * @author Ian Schneider
 * @source $URL$
 * @version $Id$
 */
public class DefaultFeatureType implements FeatureType {
    /** The name of this FeatureType. */
    private final String typeName;

    /** The namespace to uniquely identify this FeatureType. */
    private final URI namespace;

    /** The array of types that this FeatureType can have as attributes. */
    private final AttributeType[] types;

    /** The FeatureTypes this is descended from. */
    private final FeatureType[] ancestors;

    /** The default geometry AttributeType. */
    private final GeometryAttributeType defaultGeom;
    
    private final int hashCode;

    /** The position of the default Geometry 
     *  Leave as package protected for use by DefaultFeature
     */
    final int defaultGeomIdx;
    
    /** An feature type with no attributes */
    public static final FeatureType EMPTY = new DefaultFeatureType();
    
    /** attname:string -> position:int */
    private final java.util.Map attLookup;
    
    private final static URI toURI( String namespace ) throws SchemaException {
        try {
            return new URI( namespace );
        } catch (URISyntaxException badNamespace ) {
           throw new SchemaException( badNamespace );
        }
    }
    public DefaultFeatureType(String typeName, String namespace,
            Collection types, Collection superTypes, GeometryAttributeType defaultGeom)
            throws SchemaException, NullPointerException {
        this( typeName, toURI(namespace ), types, superTypes, defaultGeom );
    }
    /**
     * Constructs a new DefaultFeatureType.
     *
     * <p>
     * Attributes from the superTypes will be copied to the list of attributes
     * for this feature type.  
     *
     * @param typeName The name to give this FeatureType.
     * @param namespace The namespace of the new FeatureType.
     * @param types The attributeTypes to use for validation.
     * @param superTypes The ancestors of this FeatureType.
     * @param defaultGeom The attributeType to set as the defaultGeometry.
     *
     * @throws SchemaException For problems making the FeatureType.
     * @throws NullPointerException If typeName is null.
     */
    public DefaultFeatureType(String typeName, URI namespace,
        Collection types, Collection superTypes, GeometryAttributeType defaultGeom)
        throws NullPointerException {
        if (typeName == null) {
            throw new NullPointerException(typeName);
        }

        this.typeName = typeName;
        this.namespace = namespace == null ? GMLSchema.NAMESPACE : namespace;
        this.ancestors = (FeatureType[]) superTypes.toArray(new FeatureType[superTypes
                .size()]);

        Collection attributes = new java.util.ArrayList( types );
        for (int i = 0, ii = ancestors.length; i < ii; i++) {
            FeatureType ancestor = ancestors[i];
            for (int j = 0, jj = ancestor.getAttributeCount(); j < jj; j++) {
                attributes.add(ancestor.getAttributeType(j));
            }
        }
        if(attributes.size()!=0)
            this.types = (AttributeType[]) attributes.toArray(new AttributeType[attributes.size()]);
        else
            this.types = new AttributeType[0];

        this.defaultGeom = defaultGeom;
        
        attLookup = new java.util.HashMap(this.types.length);
        for (int i = 0, ii = this.types.length; i < ii; i++) {
            attLookup.put(this.types[i].getName(),new Integer(i));
        }
        
        this.defaultGeomIdx = find(defaultGeom);
            
        hashCode = computeHash();
    }
    
    /**
     * Builds an empty feature type, useful for testing
     * @throws SchemaException
     */
    private DefaultFeatureType() {
        this.typeName = "emptyFeatureType";
            namespace = GMLSchema.NAMESPACE;
        this.types = new AttributeType[0];
        this.ancestors = new FeatureType[0];
        this.defaultGeomIdx = -1;
        this.defaultGeom = null;
        hashCode = computeHash();
        attLookup = java.util.Collections.EMPTY_MAP;
    }

    /**
     * Creates a new feature, with a generated unique featureID.  This is less
     * than ideal, as a FeatureID should be persistant over time, generally
     * created by a datasource.  This method is more for testing that doesn't
     * need featureID.
     *
     * @param attributes the array of attribute values
     *
     * @return The created feature with this as its feature type.
     *
     * @throws IllegalAttributeException if this FeatureType does not validate
     *         the attributes.
     */
    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return create(attributes, null);
    }

    /**
     * Creates a new feature, with the proper featureID, using this
     * FeatureType.
     *
     * @param attributes the array of attribute values.
     * @param featureID the feature ID.
     *
     * @return the created feature.
     *
     * @throws IllegalAttributeException if this FeatureType does not validate
     *         the attributes.
     */
    public Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException {
        return new DefaultFeature(this, attributes, featureID);
    }

    public Feature duplicate(Feature original) throws IllegalAttributeException{
        if( original == null ) return null;
        FeatureType featureType = original.getFeatureType();
        if (!featureType.equals(this)) { 
        throw new IllegalAttributeException("Feature type " + featureType
                        + " does not match " + this);
        }
        String id = original.getID();
        int numAtts = featureType.getAttributeCount();
        Object attributes[] = new Object[numAtts];
        for (int i = 0; i < numAtts; i++) {
        AttributeType curAttType = getAttributeType(i);
            attributes[i] = curAttType.duplicate(original.getAttribute(i));
        }
        return featureType.create(attributes, id );
    }
    /**
     * Gets the default geometry AttributeType.  If the FeatureType has more
     * one geometry it is up to the implementor to determine which geometry is
     * the default.  If working with multiple geometries it is best to get the
     * attributeTypes and iterate through them, checking isGeometry on each.
     * This should just be used a convenience method when it is known that the
     * features are flat.
     *
     * @return The attribute type of the default geometry, which will contain
     *         the position.
     */
    public GeometryAttributeType getDefaultGeometry() {
        return defaultGeom;
    }

    /**
     * Gets the attributeType at this xPath, if the specified attributeType
     * does not exist then null is returned.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return True if attribute exists.
     */
    public AttributeType getAttributeType(String xPath) {        
        AttributeType attType = null;
        int idx = find(xPath);
        if (idx >= 0)
            attType = types[idx];
        return attType;
    }

    /**
     * Find the position of a given AttributeType.
     *
     * @param type The type to search for.
     *
     * @return -1 if not found, a zero-based index if found.
     */
    public int find(AttributeType type) {
        if (type == null) return -1;
        int idx = find(type.getName());
        if (idx < 0 || !types[idx].equals(type))
            idx = -1;
        return idx;
    }
    
    /**
     * Find the position of an AttributeType which matches the given String.
     * @param attName the name to look for
     * @return -1 if not found, zero-based index otherwise
     */
    public int find(String attName) {
        Integer idx = (Integer) attLookup.get(attName);
        return idx == null ? -1 : idx.intValue();
    }

    /**
     * Gets the attributeType at the specified index.
     *
     * @param position the position of the attribute to check.
     *
     * @return The attribute type at the specified position.
     */
    public AttributeType getAttributeType(int position) {
        return types[position];
    }

    public AttributeType[] getAttributeTypes() {
        return (AttributeType[]) types.clone();
    }

    /**
     * Gets the global schema namespace.
     *
     * @return Namespace of schema.
     */
    public URI getNamespace() {
        return namespace;
    }

    /**
     * Gets the type name for this schema.
     *
     * @return Namespace of schema.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * This is only used twice in the whole geotools code base, and  one of
     * those is for a test, so we're removing it from the interface. If
     * getAttributeType does not have the AttributeType it will just return
     * null.  Gets the number of occurrences of this attribute.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return Number of occurrences.
     */
    public boolean hasAttributeType(String xPath) {
        return getAttributeType(xPath) != null;
    }

    /**
     * Returns the number of attributes at the first 'level' of the schema.
     *
     * @return the total number of first level attributes.
     */
    public int getAttributeCount() {
        return types.length;
    }

    public boolean equals(FeatureType other) {
        if(other == this)
            return true;
        
        if (other == null) {
            return false;
        }

        if ((typeName == null) && (other.getTypeName() != null)) {
            return false;
        } else if (!typeName.equals(other.getTypeName())) {
            return false;
        }

        if ((namespace == null) && (other.getNamespace() != null)) {
            return false;
        } else if (!namespace.equals(other.getNamespace())) {
            return false;
        }

        if (types.length != other.getAttributeCount()) {
            return false;
        }

        for (int i = 0, ii = types.length; i < ii; i++) {
            if (!types[i].equals(other.getAttributeType(i))) {
                return false;
            }
        }

        return true;
    }
    
    private int computeHash() {
        int hash = typeName.hashCode() ^ namespace.hashCode();
        for (int i = 0, ii = types.length; i < ii; i++) {
            hash ^= types[i].hashCode();
        }
        return hash;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        String info = "name=" + typeName;
        info += (" , namespace=" + namespace);
        info += (" , abstract=" + isAbstract());

        String types1 = "types=(";

        for (int i = 0, ii = this.types.length; i < ii; i++) {
            types1 += this.types[i].toString();

            if (i < ii) {
                types1 += ",";
            }
        }

        types1 += ")";
        info += (" , " + types1);

        return "DefaultFeatureType [" + info + "]";
    }

    public boolean equals(Object other) {
        if (other instanceof FeatureType) 
        	return equals((FeatureType) other);
       	return false;
    }

    /**
     * Obtain an array of this FeatureTypes ancestors. Implementors should
     * return a non-null array (may be of length 0).
     *
     * @return An array of ancestors.
     */
    public FeatureType[] getAncestors() {
        return ancestors;
    }

    /**
     * Is this FeatureType an abstract type?
     *
     * @return true if abstract, false otherwise.
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * A convenience method for calling<br>
     * <code> FeatureType f1; FeatureType f2;
     * f1.isDescendedFrom(f2.getNamespace(),f2.getName()); </code>
     *
     * @param type The type to compare to.
     *
     * @return true if descendant, false otherwise.
     */
    public boolean isDescendedFrom(FeatureType type) {
        return isDescendedFrom(type.getNamespace(), type.getTypeName());
    }

    /**
     * Test to determine whether this FeatureType is descended from the given
     * FeatureType. Think of this relationship likes the "extends"
     * relationship in java.
     *
     * @param nsURI The namespace URI to use.
     * @param typeName1 The typeName.
     *
     * @return true if descendant, false otherwise.
     *
     * @task HACK: if nsURI is null only typeName is tested.
     */
    public boolean isDescendedFrom(URI nsURI, String typeName1) {
        for (int i = 0, ii = ancestors.length; i < ii; i++) {
            if (((nsURI == null)
                    || ancestors[i].getNamespace().equals(nsURI))
                    && ancestors[i].getTypeName().equals(typeName1)) {
                return true;
            }
        }        
        return false;
    }

    static final class Abstract extends DefaultFeatureType {
        public Abstract(String typeName, URI namespace, Collection types,
            Collection superTypes, GeometryAttributeType defaultGeom)
            throws SchemaException {
            super(typeName, namespace, types, superTypes, defaultGeom);

            Iterator st = superTypes.iterator();

            while (st.hasNext()) {
                FeatureType ft = (FeatureType) st.next();

                if (!ft.isAbstract()) {
                    throw new SchemaException(
                        "Abstract type cannot descend from no abstract type : "
                        + ft);
                }
            }
        }

        public final boolean isAbstract() {
            return true;
        }

        public Feature create(Object[] atts) throws IllegalAttributeException {
            throw new UnsupportedOperationException("Abstract Type");
        }

        public Feature create(Object[] atts, String id)
            throws IllegalAttributeException {
            throw new UnsupportedOperationException("Abstract Type");
        }

    }
    
}
