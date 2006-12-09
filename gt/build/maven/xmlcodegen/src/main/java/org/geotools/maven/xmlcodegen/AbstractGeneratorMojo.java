package org.geotools.maven.xmlcodegen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.xml.Schemas;

/**
 * Generates the bindings and utility classes used to parse xml documents 
 * for a particular schema. 
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public abstract class AbstractGeneratorMojo extends AbstractMojo {

	/**
	 * The schema to generate bindings for.
	 * 
	 * @parameter 
	 * @required
	 */
	protected File schemaLocation;
	
	/**
	 * The destination package of the generated source files 
	 * in the standard dot-seperated naming format.
	 * 
	 * @parameter
	 */
	protected String destinationPackage;
	
	/**
	 * Directory to output generated files to. Default is ${project.build.sourceDirectory}
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 */
	protected File outputDirectory;
	
	/**
	 * Directory containing xml schemas, defualt is ${basedir}/src/main/xsd.
	 *
	 * @parameter expression="${basedir}/src/main/xsd"
	 */
	protected File schemaSourceDirectory;
	
	/**
	 * Additional directories used to locate included and imported schemas.
	 * 
	 * @parameter
	 */
	protected File[] schemaLookupDirectories;
	
	/**
	 * Flag controlling wether files should overide files that already 
	 * exist with the same name. False by default.
	 * 
	 * @param expression="false"
	 */
	protected boolean overwriteExistingFiles;
	
	/**
     * The currently executing project
     * 
     * @parameter expression="${project}"
     */
    MavenProject project;
    
    protected XSDSchema schema() {
    	//check schema source
		if ( !schemaSourceDirectory.exists() ) {
			getLog().error( schemaSourceDirectory.getAbsolutePath() + " does not exist" );
			return null;
		}
		
		//check schema
		if ( !schemaLocation.exists() ) {
			//check relative to schemaSourceDirectory
			schemaLocation = new File( schemaSourceDirectory, schemaLocation.getName() );
			if ( !schemaLocation.exists() ) {
				getLog().error( "Could not locate schema: " + schemaLocation.getName() );
				return null;
			}
		}
		
		//add a location resolver which checks the schema source directory
		XSDSchemaLocationResolver locationResolver = new XSDSchemaLocationResolver() {

			public String resolveSchemaLocation(
				XSDSchema schema, String namespaceURI, String schemaLocation 
			) {
			
				//check location directlry
				File file = new File( schemaLocation );  
				if ( file.exists() ) {
					getLog().debug( "Resolving " + schemaLocation + " to " + schemaLocation );
					return schemaLocation;
				}
				
				String fileName = new File( schemaLocation ).getName();
				
				//check under teh schema source directory
				file = new File( schemaSourceDirectory, fileName ); 
				if ( file.exists() ) {
					getLog().debug( "Resolving " + schemaLocation + " to " + file.getAbsolutePath() );
					return file.getAbsolutePath();
				}
				
				//check hte lookup directories
				if ( schemaLookupDirectories != null ) {
					for ( int i = 0; i < schemaLookupDirectories.length; i++ ) {
						File schemaLookupDirectory = schemaLookupDirectories[ i ];
						file = new File( schemaLookupDirectory, fileName );
						if ( file.exists() ) {
							getLog().debug( "Resolving " + schemaLocation + " to " + file.getAbsolutePath() );
							return file.getAbsolutePath();
						}
							
					}
				}
				
				getLog().warn( "Could not resolve location for: " + schemaLocation );
				return null;
			}
			
		};
		
		//parse the schema
		XSDSchema xsdSchema = null;
		try {
			getLog().info( "Parsing schema: " + schemaLocation );
			xsdSchema = 
				Schemas.parse( 
					schemaLocation.getAbsolutePath(),
					(XSDSchemaLocator[]) null, new XSDSchemaLocationResolver[]{ locationResolver }
				);
			
			if ( xsdSchema == null ) {
				throw new NullPointerException();
			}
		} 
		catch (Exception e) {
			getLog().error( "Failed to parse schema");
			getLog().error( e );
			return null;
		}	
		
		return xsdSchema;
    }
    
}
