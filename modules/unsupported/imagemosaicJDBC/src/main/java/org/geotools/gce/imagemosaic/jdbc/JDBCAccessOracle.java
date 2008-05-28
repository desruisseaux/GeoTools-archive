package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.geometry.GeneralEnvelope;

import org.geotools.referencing.CRS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.MessageFormat;

import java.util.logging.Level;


class JDBCAccessOracle extends JDBCAccessBase {
    private String SRSSelect = "select srid from ALL_SDO_GEOM_METADATA where owner = ? and table_name=? and column_name = ?";
    private String SRSSelectCurrentSchema = "select srid from USER_SDO_GEOM_METADATA where table_name=? and column_name = ?";
    private String CRSSelect = "select wktext from mdsys.cs_srs where srid=?";
    private String extentSelect = null;
    private String allSelect = null;
    private String allSelectJoined = null;
    private String gridSelect = null;
    private String gridSelectJoined = null;

    JDBCAccessOracle(Config config) throws IOException {
        super(config);
        initStatementStrings(config);
    }

    private void initStatementStrings(Config config) {
        String geomAttr = config.getGeomAttributeNameInSpatialTable();

        extentSelect = "select min(sdo_geom.sdo_min_mbr_ordinate(" + geomAttr +
            ",1))," + "min(sdo_geom.sdo_min_mbr_ordinate(" + geomAttr +
            ",2))," + "max(sdo_geom.sdo_max_mbr_ordinate(" + geomAttr +
            ",1))," + "max(sdo_geom.sdo_max_mbr_ordinate(" + geomAttr +
            ",2)) from {0}";

        //		extentSelect="SELECT SDO_UTIL.TO_WKBGEOMETRY(SDO_AGGR_MBR("+geomAttr+")) FROM {0} ";

        //		spatialSelect =  "select "+ config.getKeyAttributeNameInSpatialTable()+","+
        //			"SDO_GEOM.SDO_MBR("+geomAttr+") FROM {0}";
        String spatialSelectClause = "select s." +
            config.getKeyAttributeNameInSpatialTable() + "," +
            "sdo_geom.sdo_min_mbr_ordinate(s." + geomAttr + ",1)," +
            "sdo_geom.sdo_min_mbr_ordinate(s." + geomAttr + ",2)," +
            "sdo_geom.sdo_max_mbr_ordinate(s." + geomAttr + ",1)," +
            "sdo_geom.sdo_max_mbr_ordinate(s." + geomAttr + ",2)";

        allSelect = spatialSelectClause + ",s." +
            config.getBlobAttributeNameInTileTable() + " from {0} s";
        allSelectJoined = spatialSelectClause + ",t." +
            config.getBlobAttributeNameInTileTable() +
            " from {0} s, {1} t  WHERE ";
        allSelectJoined += (" s." + config.getKeyAttributeNameInSpatialTable() +
        " = t." + config.getKeyAttributeNameInTileTable());

        String whereClause = " SDO_FILTER(s." + geomAttr + "," +
            "SDO_GEOMETRY(2003, ?, NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY(?,?, ?,?))" +
            ") = 'TRUE'";

        gridSelect = allSelect + " WHERE " + whereClause;
        gridSelectJoined = allSelectJoined + " AND " + whereClause;
    }

    @Override
    protected String getRandomTileStatement(ImageLevelInfo li) {
        if (li.isImplementedAsTableSplit()) {
            return MessageFormat.format(allSelectJoined,
                new Object[] { li.getSpatialTableName(), li.getTileTableName() });
        } else {
            return MessageFormat.format(allSelect,
                new Object[] { li.getSpatialTableName() });
        }
    }

    @Override
    protected String getGridSelectStatement(ImageLevelInfo li) {
        String stmt = null;

        if (li.isImplementedAsTableSplit()) {
            stmt = MessageFormat.format(gridSelectJoined,
                    new Object[] { li.getSpatialTableName(), li.getTileTableName() });
        } else {
            stmt = MessageFormat.format(gridSelect,
                    new Object[] { li.getSpatialTableName() });
        }

        if (stmt.indexOf("'") == -1) {
            stmt = stmt.replace("TRUE", "'TRUE'"); // BUG, format remvoes single Quotes
        }

        return stmt;
    }

    @Override
    protected String getExtentSelectStatment(ImageLevelInfo li) {
        return MessageFormat.format(extentSelect,
            new Object[] { li.getSpatialTableName() });
    }

    @Override
    protected Integer getSRSID(ImageLevelInfo li, Connection con)
        throws IOException {
        Number result = null;
        String schema = null;

        try {
            schema = getSchemaFromSpatialTable(li.getSpatialTableName());

            PreparedStatement s = null;

            if (schema == null) {
                s = con.prepareStatement(SRSSelectCurrentSchema);
                s.setString(1, li.getSpatialTableName());
                s.setString(2, config.getGeomAttributeNameInSpatialTable());
            } else {
                s = con.prepareStatement(SRSSelect);
                s.setString(1, schema);
                s.setString(2, li.getSpatialTableName());
                s.setString(3, config.getGeomAttributeNameInSpatialTable());
            }

            ResultSet r = s.executeQuery();

            if (r.next()) {
                result = (Number) r.getObject(1);
            }

            r.close();
            s.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

        if (result == null) {
            String msg = null;

            if (schema != null) {
                msg = MessageFormat.format("No entry in ALL_SDO_GEOM_METADATA where for {0},{1},{2}",
                        new Object[] {
                            schema, li.getSpatialTableName(),
                            config.getGeomAttributeNameInSpatialTable()
                        });
            } else {
                msg = MessageFormat.format("No entry in USER_SDO_GEOM_METADATA where for {0},{1}",
                        new Object[] {
                            li.getSpatialTableName(),
                            config.getGeomAttributeNameInSpatialTable()
                        });
            }

            LOGGER.log(Level.SEVERE, msg);
            throw new IOException(msg);
        }

        return result.intValue();
    }

    @Override
    protected CoordinateReferenceSystem getCRS(ImageLevelInfo li, Connection con)
        throws IOException {
        CoordinateReferenceSystem result = null;

        try {
            PreparedStatement s = con.prepareStatement(CRSSelect);
            s.setInt(1, li.getSrsId());

            ResultSet r = s.executeQuery();

            if (r.next()) {
                String definition = r.getString(1);
                result = CRS.parseWKT(definition);
            }

            r.close();
            s.close();
        } catch (Exception e) {
            LOGGER.warning("Cannot parse WKT defintion from db, srsid: " +
                li.getSrsId() + " : " + e.getMessage());
        }

        return result;
    }

    @Override
    protected void setGridSelectParams(PreparedStatement s,
        GeneralEnvelope envelope, ImageLevelInfo li) throws SQLException {
        s.setDouble(1, li.getSrsId());
        s.setDouble(2, envelope.getMinimum(0));
        s.setDouble(3, envelope.getMinimum(1));
        s.setDouble(4, envelope.getMaximum(0));
        s.setDouble(5, envelope.getMaximum(1));
    }
}
