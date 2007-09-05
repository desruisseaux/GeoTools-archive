package org.geotools.maven.xmlcodegen;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocator;

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
     * Flag controlling wether a parser configuration ( {@link org.geotools.xml.Configuration} ) 
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
    boolean generateAttributeBindings;
    
    /**
     * Flag controlling wether bindings for eleements should be generated, default is
     * false.
     * 
     * @parameter expression="false"
     */
    boolean generateElementBindings;
    
    /**
     * Flag controlling wether bindings for types should be generated, default is
     * true.
     * 
     * @parameter expression="true"
     */
    boolean generateTypeBindings;
	
    /**
     * List of constructor arguments that should be supplied to generated bindings
     * 
     * @parameter
     */
    BindingConstructorArgument[] bindingConstructorArguments;
    
    /**
     * The base class for complex bindings. If unspecified {@link org.geotools.xml.AbstractComplexBinding}
     * is used.
     * 
     * @parameter default="org.geotools.xml.AbstractComplexBinding"
     * 
     */
    Class complexBindingBaseClass;
    
    /**
     * The base class for simple bindings. If unspecified {@link org.geotools.xml.AbstractSimpleBinding}
     * is used.
     * 
     * @parameter default="org.geotools.xml.AbstractSimpleBinding"
     */
    Class simpleBindingBaseClass;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
		
    	XSDSchema xsdSchema = schema();
    	if ( xsdSchema == null ) {
    		return;
    	}
		
		BindingGenerator generator = new BindingGenerator();
		generator.setGenerateAttributes( generateAttributeBindings );
		generator.setGenerateElements( generateElementBindings );
		generator.setGenerateTypes( generateTypeBindings );
		generator.setGenerateConfiguration( generateConfiguration );
		generator.setOverwriting( overwriteExistingFiles );
		generator.setLocation( outputDirectory.getAbsolutePath() );
		generator.setSchemaSourceDirectory(schemaSourceDirectory);
		
		if ( schemaLookupDirectories != null ) {
		    generator.setSchemaLookupDirectories(schemaLookupDirectories);
		}
	
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
		if ( bindingConstructorArguments != null ) {
			HashMap map = new HashMap();
			
			for ( int i = 0; i < bindingConstructorArguments.length; i++) {
				String name = bindingConstructorArguments[i].getName();
				String type = bindingConstructorArguments[i].getType();
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
	}

}
