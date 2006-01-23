/*
 * Created on 20/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.util.HashMap;

import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.AttributeType;

/** Provides a Mock JDBC DataStore for testing the abstract DataStore implementation.
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 */
public class MockJDBCDataStore extends JDBCDataStore {
	
    /**
     * @param connectionPool
     * @throws DataSourceException
     */
    public MockJDBCDataStore(ConnectionPool connectionPool) throws IOException {
        super( connectionPool, new JDBCDataStoreConfig("www.refractions.net","test",10000) );                
        typeHandler.setFIDMapper("FEATURE_TYPE1", new TypedFIDMapper(new BasicFIDMapper("ID", 255), "FEATURE_TYPE1"));
		typeHandler.setFIDMapper("FEATURE_TYPE2", new TypedFIDMapper(new BasicFIDMapper("ID", 255), "FEATURE_TYPE2"));
    }

    

    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.JDBCDataStore#createGeometryReader(org.geotools.feature.AttributeType, org.geotools.data.jdbc.JDBCDataStore.QueryData, int)
     */
    protected AttributeReader createGeometryReader(AttributeType attrType, QueryData queryData, int index)
        throws DataSourceException {
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.JDBCDataStore#createGeometryWriter(org.geotools.feature.AttributeType, org.geotools.data.jdbc.JDBCDataStore.QueryData, int)
     */
    protected AttributeWriter createGeometryWriter(AttributeType attrType, QueryData queryData, int index)
        throws DataSourceException {     
        return null;
    }



    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) {
        return null;
    }
}
