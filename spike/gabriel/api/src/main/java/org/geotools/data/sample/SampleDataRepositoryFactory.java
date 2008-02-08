package org.geotools.data.sample;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataRepository;
import org.geotools.data.DataRepositoryFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class SampleDataRepositoryFactory implements DataRepositoryFactory<FeatureType, Feature> {

    public DataRepository<FeatureType, Feature> createDataStore(Map params) throws IOException {
        return new SampleDataRepository();
    }

    public DataRepository<FeatureType, Feature> createNewDataStore(Map params) throws IOException {
        return new SampleDataRepository();
    }

    public boolean canProcess(Map params) {
        return false;
    }

    public String getDescription() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public org.geotools.data.DataRepositoryFactory.Param[] getParametersInfo() {
        return null;
    }

    public boolean isAvailable() {
        return false;
    }

    public Map<Key, ?> getImplementationHints() {
        return null;
    }
}
