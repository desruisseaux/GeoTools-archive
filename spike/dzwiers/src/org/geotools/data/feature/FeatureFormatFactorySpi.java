package org.geotools.data.feature;

import java.io.IOException;

import org.geotools.data.ParamValues;
import org.geotools.factory.Factory;
import org.opengis.catalog.CatalogEntry;

/**
 * @author dzwiers
 */
public interface FeatureFormatFactorySpi extends Factory, CatalogEntry {

    public static final String DESCRIPTION_KEY = "org.geotools.data.feature.DataFormatFactorySpi.DESCRIPTION_KEY";
    public static final String NAME_KEY = "org.geotools.data.feature.DataFormatFactorySpi.NAME_KEY";

    FeatureTypeEntry createFeatureFormat(ParamValues params) throws IOException;
   
    FeatureTypeEntry createNewFeatureFormat(ParamValues params) throws IOException;

    boolean canProcess(ParamValues params);
    
    // same as (String)getMetaData(DataStoreFactorySpi.NAME_KEY)
    String getName();
    
    // same as (String)getMetaData(DataStoreFactorySpi.DESCRIPTION_KEY)
    String getDescription();
}
