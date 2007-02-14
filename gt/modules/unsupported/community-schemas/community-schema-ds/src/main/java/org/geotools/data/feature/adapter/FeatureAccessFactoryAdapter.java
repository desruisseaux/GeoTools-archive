package org.geotools.data.feature.adapter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.util.InternationalString;

/**
 * A {@link DataAccessFactory} that adapts any available geotools
 * {@link DataStore} to the {@link FeatureAccess} interface.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * 
 */
public class FeatureAccessFactoryAdapter implements DataAccessFactory {

    /**
     * @param expected
     *            {@link Map} instance suitable for a
     *            {@link DataStoreFactorySpi#canProcess(Map)}
     */
    public boolean canAccess(Object params) {
        if (!(params instanceof Map)) {
            return false;
        }
        try {
            DataStore dataStore = DataStoreFinder.getDataStore((Map) params);
            return dataStore != null;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean canCreateContent(Object arg0) {
        return false;
    }

    public DataAccess createAccess(Object params) throws IOException {
        DataStore dataStore = DataStoreFinder.getDataStore((Map) params);
        SimpleFeatureFactory attributeFactory = new SimpleFeatureFactoryImpl();
        FeatureAccessAdapter adapter = new FeatureAccessAdapter(dataStore, attributeFactory);
        return adapter;
    }

    public Object createAccessBean() {
        return new HashMap();
    }

    public DataAccess createContent(Object bean) {
        throw new UnsupportedOperationException();
    }

    public Object createContentBean() {
        throw new UnsupportedOperationException();
    }

    public InternationalString getName() {
        return new SimpleInternationalString("FeatureAccess adapter for DataStores");
    }

    public boolean isAvailable() {
        return true;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

}
