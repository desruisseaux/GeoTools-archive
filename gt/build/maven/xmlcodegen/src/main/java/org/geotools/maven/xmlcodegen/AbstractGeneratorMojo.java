package org.geotools.maven.xmlcodegen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.optional.sitraka.bytecode.Utils;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
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
	 * The .xsd file defining the schema to generate bindings for.
	 * 
	 * @parameter 
	 * @required
	 */
	protected File schemaLocation;
	
	/**
	 * Directory containing xml schemas, default is ${basedir}/src/main/xsd.
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
	 * The destination package of the generated source files in the standard dot-seperated 
	 * naming format.
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
    
    /**
     * The local maven repository
     * 
     * @parameter expression="${localRepository}" 
     */
    ArtifactRepository localRepository;
    
    /**
     * Remote maven repositories
     *  
     * @parameter expression="${project.remoteArtifactRepositories}" 
     */
    List remoteRepositories;
    
    /** 
     * @component 
     */
    ArtifactFactory artifactFactory;
    
    /**
     * @component
     */
    ArtifactResolver artifactResolver;
    
    protected XSDSchema schema() {
    
    	getLog().info( artifactFactory.toString() );
    	
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
		
		//build an "extended" classloader for "well-known
		List artifacts = new ArrayList();
		artifacts.add( 
			artifactFactory.createArtifact( 
				"org.geotools", "gt2-xml-gml2", project.getVersion(), null, "jar"
			) 
		);
		artifacts.add( 
			artifactFactory.createArtifact( 
				"org.geotools", "gt2-xml-gml3", project.getVersion(), null, "jar"
			) 
		);
		artifacts.add( 
			artifactFactory.createArtifact( 
				"org.geotools", "gt2-xml-filter", project.getVersion(), null, "jar"
			) 
		);
		artifacts.add( 
			artifactFactory.createArtifact( 
				"org.geotools", "gt2-xml-sld", project.getVersion(), null, "jar"
			) 
		);
	
		List urls = new ArrayList();
		for ( Iterator a = artifacts.iterator(); a.hasNext(); ) {
			Artifact artifact = (Artifact) a.next();
			try {
				artifactResolver.resolve( artifact, remoteRepositories, localRepository );
				urls.add( artifact.getFile().toURL() );
			} 
			catch( Exception e ) {
				getLog().warn( "Unable to resolve " + artifact.getId() );
			}
		}
		
		ClassLoader ext = 
			new URLClassLoader( (URL[]) urls.toArray( new URL[ urls.size() ] ), getClass().getClassLoader() );
	
		//use extended classloader to load up configuration classes to load schema files
		// with
		final List resolvers = new ArrayList();
		resolvers.add( "org.geotools.xlink.bindings.XLINKSchemaLocationResolver" );
		resolvers.add( "org.geotools.gml2.bindings.GMLSchemaLocationResolver" );
		resolvers.add( "org.geotools.gml3.bindings.GMLSchemaLocationResolver" );
		resolvers.add( "org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver" );
		resolvers.add( "org.geotools.filter.v1_0.OGCSchemaLocationResolver" );
		resolvers.add( "org.geotools.filter.v1_1.OGCSchemaLocationResolver" );
		
		for ( int i = 0; i < resolvers.size(); i++ ) {
			String className = (String) resolvers.get( i );
			try {
				Class clazz = ext.loadClass( className );
				resolvers.set( i, clazz );
			} 
			catch (ClassNotFoundException e) {
				getLog().debug( "Unable to load " + className );
				resolvers.set( i , null );
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
				
				//check for well known 
				for ( Iterator i = resolvers.iterator(); i.hasNext(); ) {
					Class configClass = (Class) i.next();
					if ( configClass == null ) {
						continue;
					}
					
					if ( configClass.getResource( fileName ) != null ) {
						//copy stream to a temp file
						try {
							file = File.createTempFile( "xmlcodegen", "xsd" );
							file.deleteOnExit();
							
							getLog().debug( "Copying " + configClass.getResource( fileName ) + " " + file );
							
							BufferedOutputStream output = 
								new BufferedOutputStream( new FileOutputStream ( file ) );
							InputStream input = configClass.getResourceAsStream( fileName );
							
							int b = -1;
							while ( ( b = input.read() ) != -1 ) {
								output.write( b );
							}
							
							input.close();
							output.close();
							
							getLog().debug( "Resolving " + schemaLocation + " to " + file.getAbsolutePath() );
							return file.getAbsolutePath();
							
						} 
						catch (IOException e) {
							getLog().debug( e );
							continue;
						}
						
					}
				}
				
				getLog().warn( "Could not resolve location for: " + fileName );
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
