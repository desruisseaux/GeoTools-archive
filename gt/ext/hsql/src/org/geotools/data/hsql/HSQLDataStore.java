/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.hsql;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.geotools.data.geometryless.JDBCDataStore;
import org.geotools.data.jdbc.ConnectionPool;
/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * HSQLDataStore x = new HSQLDataStore( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author jgarnett
 * @since 0.6.0
 */
public class HSQLDataStore extends JDBCDataStore {

     Object server; // hsql server goes here
     
     /** work with temp database? */
     public HSQLDataStore() throws IOException{
         this( File.createTempFile( "temp", "hsql" ));
     }     
     public HSQLDataStore( File file ) throws IOException{
         this( server( file ) );         
     }
     public HSQLDataStore(Object server) throws IOException {
         super( pool( server ));
     }

     static ConnectionPool pool( Object server ){
         return null;
     }
    /**
      * Will connect to file if a database exists, or
      * create a database using the provided filename.
      * 
      * @param file
      * @return HSQL Server connected to the file
      */
     static final Object server( File file ){
         return null;
     }
}
