package org.geotools.data.h2;

import org.geotools.jdbc.JDBCForeignKeyTest;
import org.geotools.jdbc.JDBCTestSetup;

public class H2ForeignKeyTest extends JDBCForeignKeyTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup() {
          
            protected void setUpData() throws Exception {
                super.setUpData();
                
                try {
                    run ( "DROP TABLE \"geotools\".\"feature_relationships\"; COMMIT;" );
                }
                catch( Exception e ) {};
                
                String sql = "CREATE TABLE \"geotools\".\"feature_relationships\" (" + 
                    "\"table\" varchar, \"col\" varchar, \"rtable\" varchar, \"rcol\" varchar" + 
                ")";
                run( sql );
                
                try {
                    run ( "DROP TABLE \"geotools\".\"feature_associations\"; COMMIT;" );
                }
                catch( Exception e ) {}
                
                sql = "CREATE TABLE \"geotools\".\"feature_associations\" (" +
                    "\"fid\" varchar, \"rtable\" varchar, \"rcol\" varchar, \"rfid\" varchar" + 
                ");";
                run( sql );
                
                try {
                    run ( "DROP TABLE \"geotools\".\"fk\"; COMMIT;" );
                }
                catch( Exception e ) {}
                
                sql = "CREATE TABLE \"geotools\".\"fk\" (" + 
                  "\"id\" int AUTO_INCREMENT(1) PRIMARY KEY, " + 
                  "\"geometry\" BLOB, \"intProperty\" int, " + 
                  "\"ft1\" int REFERENCES \"ft1\"" +   
                ")";        
                run( sql ); 
              
                sql = "INSERT INTO \"geotools\".\"fk\" VALUES (" + 
                  "0,GeomFromText('POINT(0 0)',4326), 0, 0);";
                run( sql );
                
                sql = "INSERT INTO \"geotools\".\"feature_relationships\" VALUES (" + 
                    "'fk', 'ft1', 'ft1', 'id'" + 
                ");";
                run( sql );
                
                sql = "INSERT INTO \"geotools\".\"feature_associations\" VALUES (" + 
                    "'fk.0', 'ft1', 'id', 'ft1.0' " +  
                ");";
                run ( sql );
            }
            
            public void tearDown() throws Exception {
                super.tearDown();
                
                try {
                    run ( "DROP TABLE \"geotools\".\"fk\"; COMMIT;" );
                }
                catch( Exception e ) {}
            }
        };
    }

}
