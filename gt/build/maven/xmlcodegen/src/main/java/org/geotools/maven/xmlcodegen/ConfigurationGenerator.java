package org.geotools.maven.xmlcodegen;

import java.util.logging.Level;

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
    }

}
