/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2003, Geotools
 * Project Managment Committee (PMC)
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *  
 */
package script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

public class Header {

    public static void directory( File directory ){
        File files[] = directory.listFiles( new FilenameFilter(){
            public boolean accept( File file, String name ) {
                return name.endsWith(".java");
            }            
        });
        for( int i=0; i < files.length; i++){
            try {
                java( files[i] );
            }
            catch( Exception erp){
                System.err.println("Could not process "+files[i]+":"+erp);
            }
        }
    }
    public static void java( File file ) throws Exception {
        File tmp = new File( file.getAbsolutePath()+".tmp" );
        FileReader filerd = new FileReader( file );
        FileWriter filewr = new FileWriter( tmp );
        
        BufferedReader reader = new BufferedReader( filerd );
        BufferedWriter writer = new BufferedWriter( filewr );
        
        String line = reader.readLine();
        HEADER: for( ; line != null; line = reader.readLine() ){            
            if( line.startsWith("package") ) break HEADER;            
        }
        if( line == null ){
            return; // this file does not even have pacakge?
        }
        header( writer );
        COPY: do {
            writer.write( line ); writer.write( "\n" );            
            line = reader.readLine();
        } while ( line != null );
        
        file.delete();
        tmp.renameTo( file );
    }
    
    /**
     * TODO summary sentence for header ...
     * 
     * @param writer
     * @throws IOException 
     */
    private static void header( BufferedWriter w ) throws IOException {
        w.write("/*\n");
        w.write(" * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2005, Geotools\n" ); 
        w.write(" * Project Managment Committee (PMC)\n"  );
        w.write(" *\n" );
        w.write(" * This library is free software; you can redistribute it and/or modify it under\n" );
        w.write(" * the terms of the GNU Lesser General Public License as published by the Free\n" );
        w.write(" * Software Foundation; version 2.1 of the License.\n" );
        w.write(" *\n" );
        w.write(" * This library is distributed in the hope that it will be useful, but WITHOUT\n" );
        w.write(" * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\n" );
        w.write(" * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more\n" );
        w.write(" * details.\n");
    }
    public static void main( String[] args ){
        String path = null;
        
        if( args.length == 0 ){
            path = ".";
        }
        if( args.length == 1 ){
            path = args[0];
        }
        else {
            System.out.println("usage: java script.Header path");
            System.exit(1);
        }
        File file = new File( path );
        if (file.isDirectory()){
            directory( file );
        }
        else if ( file.getName().endsWith(".java")){
            try {
                java( file );
            } catch (Exception erp) {               
                System.err.println("Could not process "+file+":"+erp);                
            }
        }
    }
}