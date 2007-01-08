package org.geotools.maven.xmlcodegen;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.xsd.XSDSchema;


/**
 * Generates an instance of {@link org.opengis.feature.type.Schema } from an xml schema.
 * 
 * @goal generateSchema
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class SchemaGeneratorMojo extends AbstractGeneratorMojo {

	/**
	 * Flag controlling wether complex types from the schema should be included.
	 * @parameter expression="true"
	 */
	boolean includeComplexTypes;
	/**
	 * Flag controlling wether simple types from the schema should be included.
	 * @parameter expression="true"
	 */
	boolean includeSimpleTypes;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
    	XSDSchema schema = schema();
    	if ( schema == null ) 
    		return;
    	
    	SchemaGenerator generator = new SchemaGenerator(schema);
        
    	generator.setComplexTypes( includeComplexTypes );
        generator.setSimpleTypes( includeSimpleTypes );
    	generator.setOverwriting( overwriteExistingFiles );
		generator.setLocation( outputDirectory.getAbsolutePath() );
        
        try {
			generator.generate( );
		} 
        catch (Exception e) {
        	getLog().error( e );
		}
    }
    
}
