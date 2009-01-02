package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKBWriter;


public class PostGISPSDialect extends PreparedStatementSQLDialect {
    
    private PostGISDialect delegate;

    public PostGISPSDialect(JDBCDataStore store, PostGISDialect delegate) {
        super(store);
        this.delegate = delegate;
    }
    
        
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        return delegate.decodeGeometryEnvelope(rs, column, cx);
    }


    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {
        return delegate
                .decodeGeometryValue(descriptor, rs, column, factory, cx);
    }


    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid,
            StringBuffer sql) {
        delegate.encodeGeometryColumn(gatt, srid, sql);
    }


    public void encodeGeometryEnvelope(String tableName, String geometryColumn,
            StringBuffer sql) {
        delegate.encodeGeometryEnvelope(tableName, geometryColumn, sql);
    }


    public void encodePrimaryKey(String column, StringBuffer sql) {
        delegate.encodePrimaryKey(column, sql);
    }


    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        return delegate.getGeometrySRID(schemaName, tableName, columnName, cx);
    }


    public String getGeometryTypeName(Integer type) {
        return delegate.getGeometryTypeName(type);
    }


    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
        return delegate.getMapping(columnMetaData, cx);
    }


    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        return delegate.getNextAutoGeneratedValue(schemaName, tableName,
                columnName, cx);
    }


    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
        return delegate.getNextSequenceValue(schemaName, sequenceName, cx);
    }


    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        return delegate.getSequenceForColumn(schemaName, tableName, columnName,
                cx);
    }


    public boolean isLooseBBOXEnabled() {
        return delegate.isLooseBBOXEnabled();
    }


    public void postCreateTable(String schemaName,
            SimpleFeatureType featureType, Connection cx) throws SQLException {
        delegate.postCreateTable(schemaName, featureType, cx);
    }


    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        delegate.registerClassToSqlMappings(mappings);
    }


    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
        delegate.registerSqlTypeNameToClassMappings(mappings);
    }


    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        delegate.setLooseBBOXEnabled(looseBBOXEnabled);
    }


    @Override
    public void prepareGeometryValue(Geometry g, int srid, Class binding,
            StringBuffer sql) {
        if (g != null) {
            sql.append("GeomFromWKB(?, " + srid + ")");
        } else {
            sql.append("?");
        }
    }

    @Override
    public void setGeometryValue(Geometry g, int srid, Class binding,
            PreparedStatement ps, int column) throws SQLException {
        if (g != null) {
            byte[] bytes = new WKBWriter().write(g);
            ps.setBytes(column, bytes);
        } else {
            ps.setNull(column, Types.OTHER, "Geometry");
        }
    }

    @Override
    public PreparedFilterToSQL createPreparedFilterToSQL() {
        PostgisPSFilterToSql fts = new PostgisPSFilterToSql(this);
        fts.setLooseBBOXEnabled(delegate.isLooseBBOXEnabled());
        return fts;
    }

}
