package org.geotools.jdbc;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

public class JDBCDataStoreFactory implements DataStoreFactorySpi {

    public boolean canProcess(Map params) {
        return false;
    }

    public DataStore createDataStore(Map params) throws IOException {
        return null;
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public Param[] getParametersInfo() {
        return null;
    }

    public boolean isAvailable() {
        return false;
    }

    public Map getImplementationHints() {
        return null;
    }

}
