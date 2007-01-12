package org.geotools.data.postgis.table;

import java.sql.ResultSetMetaData;

import org.geotools.feature.Feature;

/**
 * Represents a "normal" with primary key and a geometry column.
 * <p>
 * What is normal?
 * <ul>
 * <li>Single Primary Key as the first column (for Feature ID )
 * <li>Sinjgle Geomety Column with entry in the GEOMETRY_COLUMNS describing CRS and bounds
 * </ul>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class NormalTable extends Table {
      public NormalTable( ResultSetMetaData metadata ){
          super( metadata );
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
