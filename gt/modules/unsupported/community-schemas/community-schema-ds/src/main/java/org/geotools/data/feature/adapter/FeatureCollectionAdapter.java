package org.geotools.data.feature.adapter;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeName;

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
        Name name = new org.geotools.feature.Name(typeName.getNamespaceURI(), typeName.getLocalPart());
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
