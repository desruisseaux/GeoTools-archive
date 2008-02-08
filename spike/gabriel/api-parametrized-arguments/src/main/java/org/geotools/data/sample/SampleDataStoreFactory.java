package org.geotools.data.sample;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Map;

import javax.swing.Icon;

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

    public boolean isAvailable() {
        return false;
    }

    public Map<Key, ?> getImplementationHints() {
        return null;
    }

    public boolean canCreateNew(Map params) {
        return false;
    }

    public Icon getIcon() {
        return null;
    }

    public org.geotools.data.FeatureDataFactory.Param[] getParametersInfo() {
        return null;
    }

}
