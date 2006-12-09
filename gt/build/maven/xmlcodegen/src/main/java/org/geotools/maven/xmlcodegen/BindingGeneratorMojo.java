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
public class BindingGeneratorMojo extends AbstractGeneratorMojo {

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
    
    public void execute() throws MojoExecutionException, MojoFailureException {
		
    	XSDSchema xsdSchema = schema();
    	if ( xsdSchema == null ) {
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
		} catch (Exception e) {
			getLog().error( e );
			return;
		}
		
		ClassLoader cl = new URLClassLoader( (URL[]) urls.toArray( new URL[ urls.size() ] ) );
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
			generator.setIncluded( included );
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
