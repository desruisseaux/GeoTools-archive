package org.geotools.data.jdbc.ds;

import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.factory.GeoTools;
import org.geotools.factory.JNDI;

/**
 * A datasource factory SPI doing JDNI lookups
 * @author Administrator
 *
 */
public class JNDIDataSourceFactory extends AbstractDataSourceFactorySpi {

    public static final Param JNDI_REFNAME = new Param("jdniReferenceName", String.class,
            "The path where the connection pool must be located", true);

    private static final Param[] PARAMS = new Param[] { JNDI_REFNAME };

    public DataSource createDataSource(Map params) throws IOException {
        return createNewDataSource(params);
    }

    public DataSource createNewDataSource(Map params) throws IOException {
        String refName = (String) JNDI_REFNAME.lookUp(params);
        try {
            return (DataSource) JNDI.getInitialContext(GeoTools.getDefaultHints()).lookup(refName);
        } catch (Exception e) {
            throw new DataSourceException("Could not find the specified data source in JNDI", e);
        }
    }

    public String getDescription() {
        return "A JNDI based DataSource locator. Provide the JDNI location of a DataSource object in order to make it work";
    }

    public Param[] getParametersInfo() {
        return PARAMS;
    }

    /**
     * Make sure a JNDI context is available
     */
    public boolean isAvailable() {
        try {
            JNDI.getInitialContext(GeoTools.getDefaultHints());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
