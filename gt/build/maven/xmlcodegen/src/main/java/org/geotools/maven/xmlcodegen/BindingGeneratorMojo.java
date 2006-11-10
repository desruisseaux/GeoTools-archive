package org.geotools.maven.xmlcodegen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
 * @goal generate 
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class BindingGeneratorMojo extends AbstractMojo {

	/**
	 * The schema to generate bindings for.
	 * 
	 * @parameter 
	 * @required
	 */
	private File schemaLocation;
	
	/**
	 * The destination package of the generated source files 
	 * in the standard dot-seperated naming format.
	 * 
	 * @parameter
	 */
	private String destinationPackage;
	
	/**
	 * Directory to output generated files to. Default is ${project.build.sourceDirectory}
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 */
	private File outputDirectory;
	
	/**
	 * Directory containing xml schemas, defualt is ${basedir}/src/main/xsd.
	 *
	 * @parameter expression="${basedir}/src/main/xsd"
	 */
	private File schemaSourceDirectory;
	
	/**
	 * Additional directories used to locate included and imported schemas.
	 * 
	 * @parameter
	 */
	private File[] schemaLookupDirectories;
	
	/**
	 * Flag controlling wether files should overide files that already 
	 * exist with the same name. False by default.
	 * 
	 * @param expression="false"
	 */
	private boolean overwriteExistingFiles;
	
	/**
	 * Flag controlling wether the interface containg all the binding names should
	 * be generated, default is true.
	 * 
	 * @parameter expression="true"
	 */
    boolean generateBindingInterface;
    
    /**
	 * Flag controlling wether the binding configuration implementation should 
	 * be generated, default is true.
	 * 
	 * @parameter expression="true"
	 */
    boolean generateBindingConfiguration;
    
    /**
     * Flag controlling wether a schmea location resolver should be generated, 
     * the default is true.
     * 
     * @parameter expression="true"
     */
    boolean generateSchemaLocationResolver;
    
    /**
     * Flag controlling wether a parser configuration should be generated, 
     * the default is true.
     * 
     * @parameter expression="true"
     */
    boolean generateConfiguration;
    
    /**
     * Flag controlling wether bindings for attributes should be generated, default is
     * false.
     * 
     * @parameter expression="false"
     */
    boolean generateAttributes;
    
    /**
     * Flag controlling wether bindings for eleements should be generated, default is
     * false.
     * 
     * @parameter expression="false"
     */
    boolean generateElements;
    
    /**
     * Flag controlling wether bindings for types should be generated, default is
     * true.
     * 
     * @parameter expression="true"
     */
    boolean generateTypes = true;
	
    /**
     * List of names of attributes, elements, types to include, if unset all will
     * be generated.
     * 
     * @parameter
     */
    String[] includes;
    
    /**
     * List of constructor arguments that should be supplied to generated bindings
     * 
     * @parameter
     */
    ConstructorArgument[] constructorArguments;
    
    /**
     * The currently executing project
     * 
     * @parameter expression="${project}"
     */
    MavenProject project;
    
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		//list of urls to use as class loading locations
		List urls = new ArrayList();
		try {
			List l = project.getCompileClasspathElements();
			for ( Iterator i = l.iterator(); i.hasNext(); ) {
				String element = (String) i.next();
				File d = new File( element );
			
				if ( d.exists() && d.isDirectory() ) {
					urls.add( d.toURL() );
				}
			}
		} catch (DependencyResolutionRequiredException e) {
			getLog().error( e );
			return;
		} catch (MalformedURLException e) {
			getLog().error( e );
			return;
		}
		
		ClassLoader cl = new URLClassLoader( (URL[]) urls.toArray( new URL[ urls.size() ] ) );
	
		//check schema source
		if ( !schemaSourceDirectory.exists() ) {
			getLog().error( schemaSourceDirectory.getAbsolutePath() + " does not exist" );
			return;
		}
		
		//check schema
		if ( !schemaLocation.exists() ) {
			//check relative to schemaSourceDirectory
			schemaLocation = new File( schemaSourceDirectory, schemaLocation.getName() );
			if ( !schemaLocation.exists() ) {
				getLog().error( "Could not locate schema: " + schemaLocation.getName() );
				return;
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
			return;
		}
		
		BindingGenerator generator = new BindingGenerator();
		generator.setGeneratingBindingConfiguration( generateBindingConfiguration );
		generator.setGeneratingBindingInterface( generateBindingInterface );
		generator.setGenerateAttributes( generateAttributes );
		generator.setGenerateElements( generateElements );
		generator.setGenerateTypes( generateTypes );
		generator.setOverwriting( overwriteExistingFiles );
		generator.setLocation( outputDirectory.getAbsolutePath() );
	
		if ( destinationPackage != null ) {
			generator.setPackageBase( destinationPackage );
		}
		
		if ( constructorArguments != null ) {
			HashMap map = new HashMap();
			
			for ( int i = 0; i < constructorArguments.length; i++) {
				String name = constructorArguments[i].getName();
				String type = constructorArguments[i].getType();
				Class clazz = null;
				
				try {
					clazz = cl.loadClass( type );
				} catch (ClassNotFoundException e) {
					getLog().error( "Could not locate class:" + type );
					return;
				}
				
				map.put( name, clazz );
			}
			
			generator.setBindingConstructorArguments( map );
		}
		
		if ( includes != null && includes.length > 0 ) {
			HashSet included = new HashSet( Arrays.asList( includes ) );
			getLog().info( "Including: " + included ); 
			generator.setIncludedTypes( included );
		}
		
		getLog().info( "Generating bindings...");
		generator.generate( xsdSchema );
		
		//schema location resolver
		if ( generateSchemaLocationResolver ) {
			SchemaLocationResolverGenerator slrg = new SchemaLocationResolverGenerator();
			getLog().info( "Generating schema location resolver to " + outputDirectory.getAbsolutePath() );
			slrg.setLocation( outputDirectory.getAbsolutePath() );
			slrg.setOverwriting( overwriteExistingFiles );
			slrg.setPackageBase( destinationPackage );
			
			slrg.generate( xsdSchema );
		}
		
		//parser configuration
		if ( generateConfiguration ) {
			getLog().info( "Generating parser configuration...");
			ConfigurationGenerator cg = new ConfigurationGenerator();
			cg.setLocation( outputDirectory.getAbsolutePath() );
			cg.setOverwriting( overwriteExistingFiles );
			cg.setPackageBase( destinationPackage );
			cg.generate( xsdSchema );
		}
	}

}
