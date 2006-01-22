package org.geotools.data.geometryless;

import java.util.logging.Logger;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.filter.SQLEncoder;
/**
 * A Geometryless-specific instance of DefaultSQLBuilder, which supports geometries created form standard data types
 * @author Rob Atkinson rob@socialchange.net.au
 * @source $URL$
 */
public class GeometrylessSQLBuilder extends DefaultSQLBuilder {
    
       /** The logger for the mysql module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.geometryless");

   private String XCoordColumnName = null;
   private String YCoordColumnName = null;
   
    public GeometrylessSQLBuilder(SQLEncoder encoder) {
        super(encoder);


    }
    

    public GeometrylessSQLBuilder(SQLEncoder encoder, String x, String y) {
        super(encoder);
     this.XCoordColumnName = x;
       this.YCoordColumnName = y;
   }

    /**
     * Produces the select information required.
     * 
     * <p>
     * The featureType, if known, is always requested.
     * </p>
     * 
     * <p>
     * sql: <code>featureID (,attributeColumn)</code>
     * </p>
     * 
     * <p>
     * We may need to provide AttributeReaders with a hook so they can request
     * a wrapper function.
     * </p>
     *
     * @param sql
     * @param fidColumnName
     * @param attributes
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper, AttributeType[] attributes) {
   
    	
    	
        for (int i = 0; i < mapper.getColumnCount(); i++) {
        	LOGGER.finest(mapper.getColumnName(i));
            sql.append(mapper.getColumnName(i));
            if (attributes.length > 0 || i < (mapper.getColumnCount() - 1)) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

     	LOGGER.finest(attributes[i].getName() + " isGeom: " +attributes[i].isGeometry() );
            if (attributes[i].isGeometry()) {
                sql.append("AsText(" + attributes[i].getName() + ") AS " + attributes[i].getName());
            } else {
                sql.append(colName);
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

}
