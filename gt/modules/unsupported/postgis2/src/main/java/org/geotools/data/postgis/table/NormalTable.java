package org.geotools.data.postgis.table;

import java.sql.ResultSetMetaData;

import org.geotools.data.postgis.PostgisDataStore;
import org.geotools.data.store.ContentDataStore;
import org.geotools.feature.Feature;
import org.opengis.feature.type.TypeName;

/**
 * Represents a "normal" with primary key and a geometry column.
 * <p>
 * What is normal?
 * <ul>
 * <li>Single Primary Key as the first column (for Feature ID )
 * <li>Single Geometry Column with entry in the GEOMETRY_COLUMNS describing CRS and bounds
 * </ul>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class NormalTable extends Table {

      public NormalTable(PostgisDataStore dataStore, TypeName typeName) {
          super( dataStore, typeName );)
      }
    /**
       * Maps feaure id to first attribute.
       * <p>
       * FeatureID is formed with TABLE_NAME.ATTRIBUTE_0
       */
      String generateFid(Feature feature) {
          return feature.getFeatureType().getTypeName()+"."+feature.getAttribute(0);
      }
      
}
