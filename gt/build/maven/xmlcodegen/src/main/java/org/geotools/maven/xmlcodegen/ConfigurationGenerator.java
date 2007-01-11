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
        try {
			includes.add(new File(new URI(schema.getSchemaLocation())));
		} 
        catch (URISyntaxException e) {
        	logger.log( Level.SEVERE, "Error generating resolver", e );
		}
        for (Iterator i = Schemas.getIncludes(schema).iterator(); i.hasNext();) {
            XSDInclude include = (XSDInclude) i.next();
            
            try {
				includes.add(new File(new URI(include.getSchemaLocation())));
			} 
            catch (URISyntaxException e) {
            	logger.log( Level.SEVERE, "Error generating resolver", e );
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
