package org.geotools.data.grid;

import java.io.IOException;

import org.geotools.data.ParamValues;
import org.geotools.factory.Factory;
import org.opengis.catalog.CatalogEntry;

/**
 * @author dzwiers
 */
public interface GridFormatFactorySpi extends Factory, CatalogEntry {

    public static final String DESCRIPTION_KEY = "org.geotools.feature.data.raster.GridFormatFactorySpi.DESCRIPTION_KEY";
    public static final String NAME_KEY = "org.geotools.feature.data.raster.GridFormatFactorySpi.NAME_KEY";

    GridEntry createRasterFormat(ParamValues params) throws IOException;
   
    GridEntry createNewRasterFormat(ParamValues params) throws IOException;

    boolean canProcess(ParamValues params);
    
    // same as (String)getMetaData(DataStoreFactorySpi.NAME_KEY)
    String getName();
    
    // same as (String)getMetaData(DataStoreFactorySpi.DESCRIPTION_KEY)
    String getDescription();
}
