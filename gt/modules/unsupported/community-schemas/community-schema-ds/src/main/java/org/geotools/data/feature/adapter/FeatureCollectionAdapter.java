package org.geotools.data.feature.adapter;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;

public class FeatureCollectionAdapter extends AbstractCollection {

	private FeatureCollection gtFeatures;

	private SimpleFeatureType isoType;

	public FeatureCollectionAdapter(SimpleFeatureType isoType, FeatureCollection features) {
		this.isoType = isoType;
		this.gtFeatures = features;
	}

	public Iterator iterator() {
		final Iterator gtFeatureIterator = gtFeatures.iterator();

		Iterator isoFeatures = new Iterator() {
			public boolean hasNext() {
				return gtFeatureIterator.hasNext();
			}

			public Object next() {
				Feature gtFeature = (Feature) gtFeatureIterator.next();
				org.opengis.feature.Feature isoFeature;
				isoFeature = new ISOFeatureAdapter(gtFeature, isoType);
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
}
