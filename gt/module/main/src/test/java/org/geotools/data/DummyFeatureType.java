/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 *
 *    Created on 20 novembre 2003, 21.31
 */
package org.geotools.data;

import java.net.URI;

import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;

/**
 * An empty FeatureType implementation used in the AbstractDataSourceTest
 * @author  wolf
 * @source $URL$
 */
public class DummyFeatureType implements FeatureType {
    private String typeName;
    
    /** Creates a new instance of DummyFeatureType */
    public DummyFeatureType(String typeName) {
    }
    
    public org.geotools.feature.Feature create(Object[] attributes) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public org.geotools.feature.Feature create(Object[] attributes, String featureID) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public org.geotools.feature.Feature duplicate(org.geotools.feature.Feature feature) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public int find(org.geotools.feature.AttributeType type) {
        return 0;
    }
    
    public FeatureType[] getAncestors() {
        return new FeatureType[] {};
    }
    
    public int getAttributeCount() {
        return 0;
    }
    
    public org.geotools.feature.AttributeType getAttributeType(String xPath) {
        return null;
    }
    
    public org.geotools.feature.AttributeType getAttributeType(int position) {
        return null;
    }
    
    public org.geotools.feature.AttributeType[] getAttributeTypes() {
        return new org.geotools.feature.AttributeType[] {};
    }
    
    public GeometryAttributeType getDefaultGeometry() {
        return null;
    }
    
    public URI getNamespace() {
        return null;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public boolean hasAttributeType(String xPath) {
        return false;
    }
    
    public boolean isAbstract() {
        return true;
    }
    
    public boolean isDescendedFrom(FeatureType type) {
        return false;
    }
    
    public boolean isDescendedFrom(URI nsURI, String typeName) {
        return false;
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureType#find(java.lang.String)
	 */
	public int find(String attName) {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
