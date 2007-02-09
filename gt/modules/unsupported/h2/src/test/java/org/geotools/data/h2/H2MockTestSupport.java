package org.geotools.data.h2;

import java.sql.Types;

import org.geotools.feature.simple.SimpleTypeFactoryImpl;

import junit.framework.TestCase;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockDatabaseMetaData;
import com.mockrunner.mock.jdbc.MockResultSet;

/**
 * Sets up mock jdbc data for unit testing.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 */
public class H2MockTestSupport extends TestCase {

	H2DataStore dataStore;

	protected MockResultSet createTableMetaDataResultSet() {
		MockResultSet tables = new MockResultSet("tables");

		tables.addColumn("TABLE_CAT");
		tables.addColumn("TABLE_SCHEM");
		tables.addColumn("TABLE_NAME");
		tables.addColumn("TABLE_TYPE");

		tables.addRow(new Object[] { null, "schema", "ft1", "TABLE" });

		return tables;
	}

	protected MockResultSet createColumnMetaDataResultSet() {
		MockResultSet columns = new MockResultSet("columns");

		columns.addColumn("TABLE_CAT");
		columns.addColumn("TABLE_SCHEM");
		columns.addColumn("TABLE_NAME");
		columns.addColumn("COLUMN_NAME");
		columns.addColumn("DATA_TYPE");

		columns.addRow(new Object[] { null, "schema", "ft1", "id1",
				new Integer(Types.VARCHAR) });
		columns.addRow(new Object[] { null, "schema", "ft1", "id2",
				new Integer(Types.INTEGER) });
		columns.addRow(new Object[] { null, "schema", "ft1", "bool",
				new Integer(Types.BOOLEAN) });
		columns.addRow(new Object[] { null, "schema", "ft1", "double",
				new Integer(Types.DOUBLE) });
		columns.addRow(new Object[] { null, "schema", "ft1", "int",
				new Integer(Types.INTEGER) });
		columns.addRow(new Object[] { null, "schema", "ft1", "string",
				new Integer(Types.VARCHAR) });

		return columns;
	}

	private MockResultSet createPrimaryKeyMetaDataResultSet() {

		MockResultSet rs = new MockResultSet("types");
		rs.addColumn("TABLE_CAT");
		rs.addColumn("TABLE_SCHEM");
		rs.addColumn("TABLE_NAME");
		rs.addColumn("COLUMN_NAME");

		rs.addRow(new Object[] { null, "schema", "ft1", "id1" });
		rs.addRow(new Object[] { null, "schema", "ft1", "id2" });

		return rs;
	}

	private MockResultSet createTypeInfoMetaDataResultSet() {
		
		MockResultSet rs = new MockResultSet("primaryKeys");
		rs.addColumn("DATA_TYPE");
		rs.addColumn("TYPE_NAME");
		
		rs.addRow(new Object[] { new Integer( Types.INTEGER ), "int" } );
		rs.addRow(new Object[] { new Integer( Types.BOOLEAN ), "bool" } );
		rs.addRow(new Object[] { new Integer( Types.DOUBLE ), "double" } );
		rs.addRow(new Object[] { new Integer( Types.VARCHAR ), "varchar" } );
		

		return rs;
	}

	protected void setUp() throws Exception {
		// create the database metadata
		MockDatabaseMetaData metaData = new MockDatabaseMetaData();
		metaData.setTables(createTableMetaDataResultSet());
		metaData.setColumns(createColumnMetaDataResultSet());
		metaData.setPrimaryKeys(createPrimaryKeyMetaDataResultSet());
		metaData.setTypeInfo(createTypeInfoMetaDataResultSet());

		// create the mock connection
		MockConnection connection = new MockConnection();
		connection.setMetaData(metaData);

		MockConnectionPoolDataSource connectionPoolDataSource = new MockConnectionPoolDataSource(
				connection);

		H2Content content = new H2Content(connectionPoolDataSource);
		content.setDatabaseSchema( "schema" );
		
		dataStore = new H2DataStore(content);
		dataStore.setTypeFactory(new SimpleTypeFactoryImpl());
		dataStore.setNamespaceURI("http://www.geotools.org/h2");
	}

}
