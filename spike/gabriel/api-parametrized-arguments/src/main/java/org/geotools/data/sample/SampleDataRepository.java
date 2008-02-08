package org.geotools.data.sample;

import java.io.IOException;
import java.util.List;

import org.geotools.data.FeatureData;
import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public class SampleDataRepository implements FeatureData<FeatureType, Feature> {

    public void createSchema(FeatureType featureType) throws IOException {
    }

    public Source<FeatureType, Feature> getFeatureSource(Name name) throws IOException {
        return null;
    }

    public List<Name> getNames() throws IOException {
        return null;
    }

    public FeatureType getSchema(Name name) throws IOException {
        return null;
    }

    public Source<FeatureType, Feature> getView(Query query) throws IOException, SchemaException {
        return null;
    }

    public void updateSchema(String typeName, FeatureType featureType) throws IOException {
    }

}
