/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * 
 * @source $URL$
 */
class TestReader implements FeatureReader{

    /**
	 * 
	 */
	private FeatureType type;
	private Feature feature;

    public TestReader(FeatureType type, Feature f) {
        this.type = type;
		this.feature=f;
    }
    
    public FeatureType getFeatureType() {
        return type;
    }

    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        next=false;
        return feature;
    }

    boolean next=true;
    public boolean hasNext() throws IOException {
        return next;
    }

    public void close() throws IOException {
    }
    
}
