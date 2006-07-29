/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.net.URI;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;

/**
 * A FeatureType that adds the information about the XMLSchema used to create the FeatureType.   
 * @author Jesse
 * @since 1.1.0
 */
class WFSFeatureType implements FeatureType {
    FeatureType delegate;
    private URI schemaURI;

    public WFSFeatureType( FeatureType delegate, URI schemaURI ) {
        this.delegate = delegate;
        this.schemaURI=schemaURI;
    }

    public Feature create( Object[] attributes, String featureID ) throws IllegalAttributeException {
        return delegate.create(attributes, featureID);
    }

    public Feature create( Object[] attributes ) throws IllegalAttributeException {
        return delegate.create(attributes);
    }

    public Feature duplicate( Feature feature ) throws IllegalAttributeException {
        return delegate.duplicate(feature);
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
}
