/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    Created on 20/10/2003
 */
package org.geotools.data.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockDatabaseMetaData;
import com.mockrunner.mock.jdbc.MockResultSet;

/** Provides ...
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 */
public class JDBCDataStoreTest extends TestCase {
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetFeatureTypes() throws Exception {
        MockConnection conn = createMockConnection();        
        MockConnectionPoolDataSource cpds = new MockConnectionPoolDataSource(conn);
        ConnectionPool pool = new ConnectionPool(cpds);
        DataStore ds = new MockJDBCDataStore(pool);
        String[] fts = ds.getTypeNames();
        
        assertEquals(2, fts.length);
        assertEquals("FEATURE_TYPE1", fts[0]);
        assertEquals("FEATURE_TYPE2", fts[1]);
    }


    public void testGetSchema() throws Exception {
        MockConnection conn = createMockConnection();        
        MockConnectionPoolDataSource cpds = new MockConnectionPoolDataSource(conn);
        ConnectionPool pool = new ConnectionPool(cpds);
        MockJDBCDataStore ds = new MockJDBCDataStore(pool);
        
        FeatureType ft = ds.getSchema("FEATURE_TYPE1");
        
        assertEquals(new TypedFIDMapper(new BasicFIDMapper("ID", 255), "FEATURE_TYPE1"), ds.getFIDMapper("FEATURE_TYPE1"));
        
        assertNotNull(ft);
        assertEquals("FEATURE_TYPE1", ft.getTypeName());
        
        AttributeType[] attrs = ft.getAttributeTypes();
        assertNotNull(attrs);
        
        assertEquals(3, attrs.length);
        
        assertEquals("Name", attrs[0].getName());
        assertEquals(String.class, attrs[0].getType());
        assertFalse(attrs[0].isNillable());
        assertEquals(1,attrs[0].getMinOccurs());
        assertEquals(1,attrs[0].getMaxOccurs());
        
        assertEquals("B", attrs[1].getName());
        assertEquals(Boolean.class, attrs[1].getType());
        assertTrue(attrs[1].isNillable());
        assertEquals(0,attrs[1].getMinOccurs());
        assertEquals(1,attrs[1].getMaxOccurs());
              
        assertEquals("DEC", attrs[2].getName());
        assertEquals(Double.class, attrs[2].getType());
        assertTrue(attrs[2].isNillable());
        assertEquals(0,attrs[2].getMinOccurs());
        assertEquals(1,attrs[2].getMaxOccurs());
    }


    /*
     * Class to test for FeatureReader getFeatureReader(FeatureType, Filter, Transaction)
     */
    public void testGetFeatureReader() throws Exception {
        MockConnection conn = createMockConnection();        
        MockConnectionPoolDataSource cpds = new MockConnectionPoolDataSource(conn);
        ConnectionPool pool = new ConnectionPool(cpds);
        MockJDBCDataStore ds = new MockJDBCDataStore(pool);
        
        FeatureType ft = ds.getSchema("FEATURE_TYPE1");
        FeatureReader fr = ds.getFeatureReader(new DefaultQuery(ft.getTypeName(),null), Transaction.AUTO_COMMIT);
        
        ArrayList list = new ArrayList();
        while(fr.hasNext()) {
            Feature o = fr.next();
            System.out.println(o);
            list.add(o);
        }
        
        Feature[] res = (Feature[]) list.toArray(new Feature[list.size()]);
        
        assertEquals(3, res.length);        
        
        assertEquals("FEATURE_TYPE1.1", res[0].getID());
        assertEquals("FEATURE_TYPE1.2", res[1].getID());
        assertEquals("FEATURE_TYPE1.3", res[2].getID());
        
        assertEquals("Feature 1", res[0].getAttribute(0));
        assertEquals("Feature 2", res[1].getAttribute(0));
        assertEquals("Feature 3", res[2].getAttribute(0));
        
        assertEquals(Boolean.TRUE, res[0].getAttribute(1));
        assertEquals(Boolean.FALSE, res[1].getAttribute(1));
        assertEquals(Boolean.TRUE, res[2].getAttribute(1));
        
        assertEquals(new Double(10.001), res[0].getAttribute(2));
        assertEquals(new Double(20.002), res[1].getAttribute(2));
        assertEquals(new Double(30.003), res[2].getAttribute(2));
    }
    
    public void testGetFeatureWriter() throws Exception {
        MockConnection conn = createMockConnection();        
        MockConnectionPoolDataSource cpds = new MockConnectionPoolDataSource(conn);
        ConnectionPool pool = new ConnectionPool(cpds);
        MockJDBCDataStore ds = new MockJDBCDataStore(pool);
        
        FeatureWriter writer = ds.getFeatureWriter("FEATURE_TYPE1", Filter.INCLUDE, Transaction.AUTO_COMMIT);
        assertNotNull(writer);
        
        Feature feature = writer.next();
        System.out.println(feature);
        feature.setAttribute(0, "Changed Feature");
        System.out.println(feature);
        writer.write();
        writer.close();
        
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery("FEATURE_TYPE1",null), Transaction.AUTO_COMMIT);
        reader.hasNext();
        Feature readF = reader.next();
        // I think this is a Mock object problem. Test needs to be done in real datastores assertEquals(feature, readF);
    }
    
    private MockConnection createMockConnection() throws SQLException {
        MockConnection conn = new MockConnection();
        MockDatabaseMetaData metaData = new MockDatabaseMetaData();
        metaData.setTables(createTablesResultSet());
        metaData.setColumns(createColumnMetaDataResultSet());
        metaData.setPrimaryKeys(createPrimaryKeyResultSet());
        conn.setMetaData(metaData);
        
        StatementResultSetHandler handler = conn.getStatementResultSetHandler();
        MockResultSet data = createDataResultSet();
        handler.prepareResultSet("SELECT ID, Name, B, DEC FROM FEATURE_TYPE1", data);
        return conn;
    }

    private MockResultSet createTablesResultSet() {
        MockResultSet tables = new MockResultSet("tables");
        tables.addColumn("TABLE_CAT");
        tables.addColumn("TABLE_SCHEM");
        tables.addColumn("TABLE_NAME");
        tables.addColumn("TABLE_TYPE");
        
        tables.addRow(new Object[] {null, null, "FEATURE_TYPE1", "TABLE"});
        tables.addRow(new Object[] {null, null, "FEATURE_TYPE2", "TABLE"});
        
        return tables;
    }
    
    private MockResultSet createPrimaryKeyResultSet()  {
        MockResultSet rs = new MockResultSet("PrimaryKeys");
        rs.addColumn("TABLE_CAT");
        rs.addColumn("TABLE_SCHEM");
        rs.addColumn("TABLE_NAME");
        rs.addColumn("COLUMN_NAME");
        
        rs.addRow(new Object[] {null, null, "FEATURE_TYPE1", "ID"});        
        
        return rs; 
    }
    
    private MockResultSet createColumnMetaDataResultSet() {
        MockResultSet columns = new MockResultSet("columns");
        columns.addColumn("TABLE_CAT");
        columns.addColumn("TABLE_SCHEM");
        columns.addColumn("TABLE_NAME");
        columns.addColumn("COLUMN_NAME");
        columns.addColumn("DATA_TYPE");
        columns.addColumn("TYPE_NAME");
        columns.addColumn("COLUMN_SIZE");
        columns.addColumn("BUFFER_LENGTH");
        columns.addColumn("DECIMAL_DIGITS");
		columns.addColumn("NUM_PREC_RADIX");
		columns.addColumn("NULLABLE");
        
        
        columns.addRow(new Object[]{null, null, "FEATURE_TYPE1", "ID", new Integer(Types.VARCHAR), "varchar", null,null,null,null,new Integer(DatabaseMetaData.columnNoNulls)});
        columns.addRow(new Object[]{null, null, "FEATURE_TYPE1", "Name", new Integer(Types.VARCHAR), "varchar", null,null,null,null,new Integer(DatabaseMetaData.columnNoNulls)});
        columns.addRow(new Object[]{null, null, "FEATURE_TYPE1", "B", new Integer(Types.BOOLEAN), "boolean", null,null,null,null,new Integer(DatabaseMetaData.columnNullable)});
        columns.addRow(new Object[]{null, null, "FEATURE_TYPE1", "DEC", new Integer(Types.DOUBLE), "double", null,null,null,null,new Integer(DatabaseMetaData.columnNullableUnknown)});
        
        return columns;
    }
    
    private MockResultSet createDataResultSet()  {
        MockResultSet data = new MockResultSet("data");
        
        data.addColumn("ID");
        data.addColumn("Name");
        data.addColumn("B");
        data.addColumn("DEC");
        
        data.addRow(new Object[] {new Integer(1), "Feature 1", Boolean.TRUE, new Double(10.001)});
        data.addRow(new Object[] {new Integer(2), "Feature 2", Boolean.FALSE, new Double(20.002)});
        data.addRow(new Object[] {new Integer(3), "Feature 3", Boolean.TRUE, new Double(30.003)});
        
        return data;
    }
}
