package org.geotools.data.complex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * {@link DataAccessFactory} for the ComplexDataStore implementation.
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class ComplexDataAccessFactory extends ComplexDataStoreFactory implements DataAccessFactory {

    public boolean canAccess(Object bean) {
        if (!(bean instanceof Map)) {
            return false;
        }
        return super.canProcess((Map) bean);
    }

    public boolean canCreateContent(Object arg0) {
        return false;
    }

    public DataAccess createAccess(Object params) throws IOException {
        FeatureAccess store = (FeatureAccess) super.createDataStore((Map) params);
        return store;
    }

    public Object createAccessBean() {
        return new HashMap();
    }

    public DataAccess createContent(Object params) {
        throw new UnsupportedOperationException();
    }

    public Object createContentBean() {
        return null;
    }

    public InternationalString getName() {
        return new SimpleInternationalString(super.getDisplayName());
    }

}
