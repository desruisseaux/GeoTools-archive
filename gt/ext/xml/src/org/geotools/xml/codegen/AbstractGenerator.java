package org.geotools.xml.codegen;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.xsd.XSDSchema;

/**
 * Abstract base class for code generators.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractGenerator {

	static Logger logger = Logger.getLogger( "org.geotools.xml" );
	
	/**
	 * Package base
	 */
	String packageBase;
	/**
	 * location to write out files
	 */
	String location;
	/**
	 * Flag determining if generator will overwrite existing files.
	 */
	boolean overwriting = false;
	
	/**
	 * Sets the classes to be used as constructor arguments for the bindings.
	 */
	Class[] bindingConstructorArguments;
	
	/**
	 * Sets the base package for generated classes.
	 * 
	 * @param packageBase Dot seperate package name, or <code>null</code> for 
	 * no package.
	 */
	public void setPackageBase(String packageBase) {
		this.packageBase = packageBase;
	}
	
	public String getPackageBase() {
		return packageBase;
	}
	
	/**
	 * Sets the location to write out generated java classes.
	 * 
	 * @param location A file path.
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	/**
	 * Flag controlling the behaviour of the generator when a generated file
	 * already exists.
	 * <p>
	 * If set to <code>true</code>, the generator will overwrite existing files.
	 * if set to <code>false</code>, the generator will not overwrite the file
	 * and issue a warning.
	 * </p>
	 * 
	 * @param overwriting overwrite flag.
	 */
	public void setOverwriting( boolean overwriting ) {
		this.overwriting = overwriting;
	}
	
	public void setBindingConstructorArguments( 
		Class[] bindingConstructorArguments
	) {
		this.bindingConstructorArguments = bindingConstructorArguments;
	}
	
	
	/**
	 * Writes out a string to a file. 
	 * <p>
	 * THe file written out is located under {@link #location}, with the path 
	 * generated from {@link #packageBase} appended.
	 * </p>
	 * 
	 * @param result Result to write to the files.
	 * @param className The name of the file to write out.
	 */
	protected void write( String result, String className ) throws IOException {
		//convert package to a path
		File location = outputLocation();
		
		location.mkdirs();
		location = new File( location, className + ".java" );
		
		//check for existing file
		if ( location.exists() && !overwriting) {
			logger.warning( "Generated file: " + location + " already exists." );
			return;
		}
		
		BufferedOutputStream out = 
			new BufferedOutputStream( new FileOutputStream( location ) );
		
		if ( packageBase != null ) {
			out.write( ("package " + packageBase + ";\n\n").getBytes() );
		}
		
		out.write( result.getBytes() );
		
		out.flush();
		out.close();
	}
	
	/**
	 * Copues a file to the output location. 
	 * <p>
	 * THe file written out is located under {@link #location}, with the path 
	 * generated from {@link #packageBase} appended.
	 * </p>
	 * 
	 * @param file The file to copy.
	 */
	protected void copy( File file ) throws IOException {
		File dest = new File( outputLocation(), file.getName() );
		
		//check for existing file
		if ( dest.exists() && !overwriting) {
			logger.warning( "Generated file: " + dest + " already exists." );
			return;
		}
		
		InputStream in = new BufferedInputStream( new FileInputStream( file ) );
		OutputStream out = new BufferedOutputStream( new FileOutputStream( dest ) );
		
		int b = -1;
		while( ( b = in.read() ) != -1 ) out.write( b );
		
		out.flush();
		out.close();
		in.close();
	}
	
	/**
	 * Convenience method for generating the output location of generated files based on 
	 * {@link #getLocation()} 
	 * @return
	 */
	protected File outputLocation() {
		File location = new File( this.location );
		if ( packageBase != null ) {
			String path = packageBase.replace( '.', File.separatorChar );
			location = new File( location, path );
		}
		
		return location;
	}
	
	/**
	 * Executes a code generation template.
	 * <p>
	 * The class of the template is formed by prepending 
	 * <code>org.geotools.xml.codegen.</code> to <code>name</code>. 
	 * <p>
	 * 
	 * @param The non-qualified class name of the template.
	 * @param The input to the template.
	 * 
	 * @return The result of the code generator
	 * 
	 * @throws ClassNotFoundException If the template class could not be 
	 * found.
	 * 
	 * @throws RuntimeException If any exceptions ( ex, relection) occur.
	 * while attempting to execute the template.
	 *
	 */
	protected String execute( String templateName, Object input ) 
		throws ClassNotFoundException, RuntimeException {
		
		Class c = Class.forName( "org.geotools.xml.codegen.templates." + templateName );
		
		try {
			Object template = c.newInstance();
			
			Method generate = c.getMethod( "generate", new Class[] { Object.class } );
			return (String) generate.invoke( template, new Object[]{ input } );
		} 
		catch (Exception e) { 
			throw new RuntimeException( e );
		} 
	}
	
	String prefix( XSDSchema schema ) {
		String uri = schema.getTargetNamespace();
		Iterator i = schema.getQNamePrefixToNamespaceMap().entrySet().iterator();
		for ( ; i.hasNext(); ) {
			Map.Entry entry = (Map.Entry) i.next();
			if ( entry.getValue().equals( uri ) ) {
				return (String)entry.getKey();
			}
		}
		
		return null;
	}
	
	
} 