package org.geotools.caching;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.FeatureStore;

import org.geotools.feature.FeatureCollection;


public interface FeatureCache extends FeatureStore {
    public void clear();

    public void put(FeatureCollection fc, Envelope e);

    public FeatureCollection get(Envelope e);

    public FeatureCollection peek(Envelope e);

    public void remove(Envelope e);
}
