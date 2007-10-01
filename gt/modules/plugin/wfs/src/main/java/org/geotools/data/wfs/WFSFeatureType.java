/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.net.URI;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A FeatureType that adds the information about the XMLSchema used to create the delegate.   
 * @author Jesse
 * @since 1.1.0
 */
class WFSFeatureType implements SimpleFeatureType {
    SimpleFeatureType delegate;
    
    private URI schemaURI;
    
    /**
     * If lenient then it will not throw exceptions on create() if the attributes aren't legal.
     */
    private boolean lenient;

    public WFSFeatureType( SimpleFeatureType delegate, URI schemaURI ) {
        this( delegate, schemaURI, false);
    }

    public WFSFeatureType( SimpleFeatureType delegate, URI schemaURI, boolean lenient2 ) {
        this.delegate = delegate;
        this.schemaURI=schemaURI;
        lenient=lenient2;
    }

    public SimpleFeature create( Object[] attributes, String featureID ) throws IllegalAttributeException {
        if( lenient && delegate instanceof DefaultFeatureType ){
            WFSFeatureType schema = new WFSFeatureType(delegate,schemaURI );
            return new LenientFeature(schema, attributes, featureID);
        }else{
            return delegate.create(attributes, featureID);
        }
    }
    public void setLenient( boolean lenient) {
        this.lenient=lenient;
    }

    boolean isLenient() {
        return lenient;
    }

    public Feature create( Object[] attributes ) throws IllegalAttributeException {
        return create(attributes, null);
    }

    public Feature duplicate( Feature original ) throws IllegalAttributeException {
        if( original == null ) return null;
        FeatureType featureType = original.getFeatureType();
        if (!delegate.equals(this)) { 
        throw new IllegalAttributeException("Feature type " + featureType
                        + " does not match " + this);
        }
        String id = original.getID();
        int numAtts = delegate.getAttributeCount();
        Object attributes[] = new Object[numAtts];
        for (int i = 0; i < numAtts; i++) {
        AttributeType curAttType = getAttributeType(i);
            attributes[i] = curAttType.duplicate(original.getAttribute(i));
        }
        return delegate.create(attributes, id );
    }

    public boolean equals( Object arg0 ) {
        if( !(arg0 instanceof WFSFeatureType) )
            return false;
        WFSFeatureType ft=(WFSFeatureType) arg0;
        return delegate.equals(ft.delegate);
    }

    public int find( AttributeType type ) {
        return delegate.find(type);
    }

    public int find( String attName ) {
        return delegate.find(attName);
    }

    public FeatureType[] getAncestors() {
        return delegate.getAncestors();
    }

    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    public AttributeType getAttributeType( int position ) {
        return delegate.getAttributeType(position);
    }

    public AttributeType getAttributeType( String xPath ) {
        return delegate.getAttributeType(xPath);
    }

    public AttributeType[] getAttributeTypes() {
        return delegate.getAttributeTypes();
    }

    public GeometryAttributeType getDefaultGeometry() {
    	return delegate.getDefaultGeometry();
    }

    public URI getNamespace() {
        return delegate.getNamespace();
    }

    public String getTypeName() {
        return delegate.getTypeName();
    }

    public boolean hasAttributeType( String xPath ) {
        return delegate.hasAttributeType(xPath);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isAbstract() {
        return delegate.isAbstract();
    }

    public boolean isDescendedFrom( FeatureType type ) {
        return delegate.isDescendedFrom(type);
    }

    public boolean isDescendedFrom( URI nsURI, String typeName ) {
        return delegate.isDescendedFrom(nsURI, typeName);
    }

    public URI getSchemaURI() {
        return schemaURI;
    }
    
    public String toString() {
        return delegate.toString();
    }
    
    public AttributeDescriptor getAttribute(String name) {
		return delegate.getAttribute(name);
	}

	public AttributeDescriptor getAttribute(int index) {
		return delegate.getAttribute(index);
	}
	
	public AttributeDescriptor getAttribute(Name name) {
	    return delegate.getAttribute(name);
	}
	
	public PropertyDescriptor getProperty(Name name) {
	    return delegate.getProperty(name);
	}
	
	public PropertyDescriptor getProperty(String name) {
	    return delegate.getProperty(name);
	}

	public org.opengis.feature.type.AttributeType getType(String name) {
		return delegate.getType( name );
	}

	public org.opengis.feature.type.AttributeType getType(Name name) {
        return delegate.getType( name );
    }
	
	public org.opengis.feature.type.AttributeType getType(int index) {
		return delegate.getType( index );
	}

	public List getTypes() {
		return delegate.getTypes();
	}

	public CoordinateReferenceSystem getCRS() {
		return delegate.getCRS();
	}

	public Class getBinding() {
		return delegate.getBinding();
	}

	public Collection getProperties() {
		return delegate.getProperties();
	}

	public boolean isInline() {
		return delegate.isInline();
	}

	public List getRestrictions() {
		return delegate.getRestrictions();
	}

	public org.opengis.feature.type.AttributeType getSuper() {
		return delegate.getSuper();
	}

	public boolean isIdentified() {
		return delegate.isIdentified();
	}

	public InternationalString getDescription() {
		return delegate.getDescription();
	}

	public Name getName() {
		return delegate.getName();
	}

	public Map<Object, Object> getUserData() {
	    return delegate.getUserData();
	}
	
	public int indexOf(String name) {
		return delegate.indexOf(name);
	}
	
	public int indexOf(Name name) {
	    return delegate.indexOf(name);
	}
	
    public List getAttributes() {
        return delegate.getAttributes();
    }
}
