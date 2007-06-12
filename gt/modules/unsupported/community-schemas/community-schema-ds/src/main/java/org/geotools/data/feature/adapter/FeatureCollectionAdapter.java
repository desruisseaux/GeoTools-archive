/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.feature.adapter;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeName;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FeatureCollectionAdapter extends AbstractCollection {

	private FeatureCollection gtFeatures;

	private SimpleFeatureType isoType;
    
    private SimpleFeatureFactory attributeFactory;
	
    private int maxFeatures = Integer.MAX_VALUE;
    
    private AttributeDescriptor featureDescriptor;
    
    public FeatureCollectionAdapter(SimpleFeatureType isoType, FeatureCollection features, SimpleFeatureFactory attributeFactory) {
		this.isoType = isoType;
		this.gtFeatures = features;
		
        TypeName typeName = isoType.getName();
        Name name = Types.attributeName(typeName.getNamespaceURI(), typeName.getLocalPart());
        featureDescriptor = new AttributeDescriptorImpl(isoType, name, 0, Integer.MAX_VALUE, true);
    }

	public Iterator iterator() {
		final Iterator gtFeatureIterator = gtFeatures.iterator();

		Iterator isoFeatures = new Iterator() {
            int featureCount = 0;
			public boolean hasNext() {
				return featureCount <= maxFeatures && gtFeatureIterator.hasNext();
			}

			public Object next() {
                featureCount++;
				Feature gtFeature = (Feature) gtFeatureIterator.next();
				org.opengis.feature.Feature isoFeature;
				isoFeature = new ISOFeatureAdapter(gtFeature, isoType, attributeFactory, featureDescriptor);
				return isoFeature;
			}

			public void remove() {
				gtFeatureIterator.remove();
			}

		};
		return isoFeatures;
	}

	public int size() {
		return gtFeatures.size();
	}

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }
}
