/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org
 * (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 * This librarrrrry is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.JFileChooser;

public class Header {
    private static int linecount=0;
    public static void directory( File directory ){
        File files[] = directory.listFiles( new FilenameFilter(){
            public boolean accept( File file, String name ) {
                return name.endsWith(".java");
            }
        });
        if( files == null ){
            System.err.println( "No java files "+directory );
        } else {
            for( int i=0; i < files.length; i++){
                try {
                    java( files[i] );
                } catch( Exception erp){
                    System.err.println("Could not process "+files[i]+":"+erp);
                }
            }
        }
        File subdirs[] = directory.listFiles( new FilenameFilter(){
            public boolean accept( File dir, String name ) {
                File temp = new File(dir,name);
                return temp.isDirectory() && !name.startsWith(".");
            }
        });
        if( subdirs != null ){
            for( int i=0; i < subdirs.length; i++){
                directory( subdirs[i] );
            }
        }
    }
    public static void java( File file ) throws Exception {
        FileReader filerd = new FileReader( file );
        BufferedReader reader = new BufferedReader( filerd );
        
        String line = reader.readLine();
        HEADER: for( ; line != null; line = reader.readLine() ){
            if( line.trim().endsWith("*/") ){
                line = reader.readLine();
                break HEADER;
            }
            if( line.trim().startsWith("package") ){ //no header at all?
                break HEADER;
            }
        }
        if( line == null ){
            System.err.println("Warning: "+file+"does not have 'package' ... skipping" );
            return; // this file does not even have pacakge?
        }
        File tmp = new File( file.getAbsolutePath()+".tmp" );
        tmp.deleteOnExit();
        FileWriter filewr = new FileWriter( tmp );
        BufferedWriter writer = new BufferedWriter( filewr );
        header( writer );
        COPY: do {
            writer.write( line ); writer.write( "\n" );
            line = reader.readLine();
            linecount++;
        } while ( line != null );
        reader.close();
        writer.close();
        
        System.out.println( "Processed "+file );
        file.delete();
        tmp.renameTo( file );
    }
    
    private static void header( BufferedWriter w ) throws IOException {
        w.write("/*\n");
        w.write(" * Geotools2 - OpenSource GIS mapping toolkit http://geotools.org\n");
        w.write(" * (C) 2002-2005, Geotools Project Managment Committee (PMC)\n"  );
        w.write(" *\n" );
        w.write(" * This library is free software; you can redistribute it and/or modify it under\n" );
        w.write(" * the terms of the GNU Lesser General Public License as published by the Free\n" );
        w.write(" * Software Foundation; version 2.1 of the License.\n" );
        w.write(" *\n" );
        w.write(" * This library is distributed in the hope that it will be useful, but WITHOUT\n" );
        w.write(" * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\n" );
        w.write(" * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more\n" );
        w.write(" * details.\n");
        w.write(" *\n");
        w.write(" * File may contain documentation and or interfaces derived from Open Geospatial \n");
        w.write(" * Consortium, Inc. (OGC) specifications. The work of the OGC is acknowledged.\n");
        w.write(" */\n");
    }
    public static void main( String[] args ) throws IOException {
        String path = null;
        
        if( args.length == 0 ){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File dir = chooser.getSelectedFile();
                path = dir.getPath();
                System.out.println("path is "+path);
            } else{
                return;
            }
            //path = ".";
        }
        if( args.length == 1 ){
            path = args[0];
        }
        File file = new File( path );
        if (file.isDirectory()){
            directory( file );
        } else if ( file.getName().endsWith(".java")){
            try {
                java( file );
            } catch (Exception erp) {
                System.err.println("Could not process "+file+":"+erp);
            }
        }
         System.out.println("final line count is "+linecount);
    }
}
