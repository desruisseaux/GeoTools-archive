package org.geotools.data.sample;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

public class SampleDataStoreFactory implements DataStoreFactorySpi {

    public DataStore createDataStore(Map params) throws IOException {
        return new SampleDataStore();
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        return new SampleDataStore();
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
