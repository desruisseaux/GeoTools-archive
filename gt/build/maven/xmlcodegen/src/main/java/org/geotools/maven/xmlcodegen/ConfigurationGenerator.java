package org.geotools.maven.xmlcodegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDSchema;
import org.geotools.xml.Schemas;

public class ConfigurationGenerator extends AbstractGenerator {
	public void generate(XSDSchema schema)  {
     
        try {
			String result = execute("ConfigurationTemplate", schema );
			        
			String prefix = Schemas.getTargetPrefix(schema).toUpperCase();
			write(result, prefix + "Configuration");

		}
        catch( Exception e ) {
        	logger.log( Level.SEVERE, "Error generating resolver", e );
        }
        
        //copy over all included schemas
        ArrayList includes = new ArrayList();
       
        File file = null;
		try {
			file = findSchemaFile( schema.getSchemaLocation() );
		} 
		catch (IOException e) {
			logger.log(Level.SEVERE, "", e );
		}
		
        if ( file != null ) {
        	includes.add( file );
        }
        else {
        	logger.log( Level.SEVERE, "Could not find: " + schema.getSchemaLocation() + " to copy." );        	
        }
        
        for (Iterator i = Schemas.getIncludes(schema).iterator(); i.hasNext();) {
            XSDInclude include = (XSDInclude) i.next();
            
            file = null;
    		try {
    			file = findSchemaFile( include.getSchemaLocation() );
    		} 
    		catch (IOException e) {
    			logger.log(Level.SEVERE, "", e );
    		}
    		
            if ( file != null ) {
            	includes.add( file );
            }
            else {
            	logger.log( Level.SEVERE, "Could not find: " + include.getSchemaLocation() + " to copy." );        	
            }
		}

		for (Iterator i = includes.iterator(); i.hasNext();) {
		    File include = (File) i.next();
		    try {
				copy(include);
			} 
		    catch (IOException e) {
		    	logger.log( Level.WARNING, "Could not copy file " + include , e );
			}
		}
    }

}
