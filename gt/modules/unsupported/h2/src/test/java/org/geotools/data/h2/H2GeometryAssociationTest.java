package org.geotools.data.h2;

import org.geotools.jdbc.JDBCGeometryAssociationTestSupport;
import org.geotools.jdbc.JDBCTestSetup;

public class H2GeometryAssociationTest extends
        JDBCGeometryAssociationTestSupport {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup() {
            protected void setUpData() throws Exception {
                super.setUpData();
                
                try {
                    run ( "DROP TABLE \"geometry\"; COMMIT;" );
                }
                catch( Exception e ) {}
                String sql = "CREATE TABLE \"geometry\" (" + 
                    "\"id\" VARCHAR, \"name\" VARCHAR, \"description\" VARCHAR,  " +
                    "\"type\" VARCHAR, \"geometry\" BLOB" + 
                ")";
                run( sql );
                
                sql = "INSERT INTO \"geometry\" VALUES (" + 
                    "'0','name-0','description-0', 'POINT', GeomFromText('POINT(0 0)',4326) " + 
                ");";
                run( sql );
              
                sql = "INSERT INTO \"geometry\" VALUES (" + 
                    "'1','name-1','description-1', 'POINT', GeomFromText('POINT(1 1)',4326) " + 
                ");";
                run( sql );
                
                try {
                    run ( "DROP TABLE \"multi_geometry\"; COMMIT;" );
                }
                catch( Exception e ) {}
                sql = "CREATE TABLE \"multi_geometry\" (" + 
                    "\"id\" VARCHAR, \"mgid\" VARCHAR" + 
                    
                ")";
                run( sql );
                
                sql = "INSERT INTO \"geometry\" VALUES (" + 
                    "'2','name-2','description-2','MULTIPOINT', NULL" + 
                ");";
                run( sql );
                
                sql = "INSERT INTO \"multi_geometry\" VALUES ('2', '0');";
                run ( sql );
                
                sql = "INSERT INTO \"multi_geometry\" VALUES ('2', '1');";
                run ( sql );
                
                try {
                    run ( "DROP TABLE \"geometry_associations\"; COMMIT;" );
                }
                catch( Exception e ) {}
                sql = "CREATE TABLE \"geometry_associations\" (" + 
                    "\"fid\" VARCHAR, \"gid\" VARCHAR, \"gname\" VARCHAR, " + 
                    "\"ref\" BOOLEAN" + 
                ")";
                run( sql );

                sql = "INSERT INTO \"geometry_associations\" VALUES (" + 
                    "'ga.0', '0', 'geometry', false" + 
                ");";
                run( sql );
                
                sql = "INSERT INTO \"geometry_associations\" VALUES (" + 
                    "'ga.1', '1', 'geometry', true" + 
                ");";
                run( sql );
            
                sql = "INSERT INTO \"geometry_associations\" VALUES (" + 
                    "'ga.2', '2', 'geometry', false" + 
                ");";
                run( sql );
            
                try {
                    run ( "DROP TABLE \"geotools\".\"ga\"; COMMIT;" );
                }
                catch( Exception e ) {}
                
                sql = "CREATE TABLE \"geotools\".\"ga\" (" + 
                  "\"id\" int AUTO_INCREMENT(1) PRIMARY KEY, " + 
                  "\"geometry\" BLOB " +   
                ")";        
                run( sql ); 
              
                sql = "INSERT INTO \"geotools\".\"ga\" VALUES (0, NULL);";
                run( sql );
                
                sql = "INSERT INTO \"geotools\".\"ga\" VALUES (1, NULL);";
                run( sql );
                
                sql = "INSERT INTO \"geotools\".\"ga\" VALUES (2, NULL);";
                run( sql );

                
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
