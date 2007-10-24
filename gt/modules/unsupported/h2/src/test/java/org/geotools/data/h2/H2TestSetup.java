package org.geotools.data.h2;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.jdbc.JDBCTestSetup;
import org.geotools.data.jdbc.SQLDialect;

/**
 * Test harness for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2TestSetup extends JDBCTestSetup {

    protected void initializeDatabase() throws Exception {
        //spatially enable the database
        try {
            run( getClass().getResourceAsStream( "h2.sql" ) );
        }
        catch( Exception e ) {
            
        }
    }
    
    protected void setUpData() throws Exception {
        
        //drop old data
        try {
            run ( "DROP TABLE \"geotools\".\"ft1\"; COMMIT;" );
        }
        catch( Exception e ) {}
        try {
            run ( "DROP TABLE \"geotools\".\"ft2\"; COMMIT;" );
        }
        catch( Exception e ) {}
        try {
            run ( "DROP SCHEMA \"geotools\"; COMMIT;" );    
        }
        catch( Exception e ) {}
        
        //create some data
        StringBuffer sb = new StringBuffer().append( "CREATE SCHEMA \"geotools\";" ); 
        
//        sb.append( "CREATE TABLE \"geotools\".\"ft1\" " )
//            .append( "(\"id\" int AUTO_INCREMENT(1) PRIMARY KEY , " )
//            .append( "\"geometry\" OTHER, \"intProperty\" int, " ) 
//            .append( "\"doubleProperty\" double, \"stringProperty\" varchar);" );
        
        sb.append( "CREATE TABLE \"geotools\".\"ft1\" " )
        .append( "(\"id\" int AUTO_INCREMENT(1) PRIMARY KEY , " )
        .append( "\"geometry\" BLOB, \"intProperty\" int, " ) 
        .append( "\"doubleProperty\" double, \"stringProperty\" varchar);" );
//        sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
//            .append( "0,setSRID(GeometryFromText('POINT(0 0)'),4326), 0, 0.0,'zero');");
//    
//        sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
//            .append( "1,setSRID(GeometryFromText('POINT(1 1)'),4326), 1, 1.1,'one');");
//
//        sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
//            .append( "2,setSRID(GeometryFromText('POINT(2 2)'),4326), 2, 2.2,'two');");

        sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
        .append( "0,GeomFromText('POINT(0 0)',4326), 0, 0.0,'zero');");

    sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
        .append( "1,GeomFromText('POINT(1 1)',4326), 1, 1.1,'one');");

    sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
        .append( "2,GeomFromText('POINT(2 2)',4326), 2, 2.2,'two');");
    
        run( sb.toString() );
    }

    protected DataSource createDataSource() {
        //set up the data source
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:h2:geotools");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setPoolPreparedStatements(false);
        
        return dataSource;
    }
    
    protected SQLDialect createSQLDialect() {
        return new H2Dialect();
    }
}
