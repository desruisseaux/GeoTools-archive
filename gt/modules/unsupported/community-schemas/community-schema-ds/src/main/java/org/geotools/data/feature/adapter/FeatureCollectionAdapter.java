package org.geotools.data.feature.adapter;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;

public class FeatureCollectionAdapter extends AbstractCollection {

	private FeatureCollection gtFeatures;

	private SimpleFeatureType isoType;
    
    private SimpleFeatureFactory attributeFactory;
	
    private int maxFeatures = Integer.MAX_VALUE;
    
    public FeatureCollectionAdapter(SimpleFeatureType isoType, FeatureCollection features, SimpleFeatureFactory attributeFactory) {
		this.isoType = isoType;
		this.gtFeatures = features;
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
				isoFeature = new ISOFeatureAdapter(gtFeature, isoType, attributeFactory);
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
