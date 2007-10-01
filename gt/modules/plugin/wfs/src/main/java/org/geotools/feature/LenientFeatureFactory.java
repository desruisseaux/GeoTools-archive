package org.geotools.feature;

import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class LenientFeatureFactory extends FeatureFactoryImpl {

    public SimpleFeature createSimpleFeature(List<Attribute> properties, SimpleFeatureType type, String id) {
        return new LenientFeature( properties, type, id );
    }
}
