package org.geotools.xml.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.xml.Schemas;

/**
 * Generates an instance of {@link org.eclipse.xsd.util.XSDSchemaLocationResolver} for 
 * a particular schema.
 * <p>
 * The schema supplied, and any included schemas ( not imported ), are added to 
 * the set of schemas that the resulting class can resolve. 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class SchemaLocationResolverGenerator extends AbstractGenerator {

	public void generate( XSDSchema schema ) throws Exception {
		
		ArrayList includes = new ArrayList();
		ArrayList namespaces = new ArrayList();
		
		includes.add( new File( schema.getSchemaLocation() ) );
		namespaces.add( schema.getTargetNamespace() );
		
		List included = Schemas.getIncludes( schema );
		for ( Iterator i = included.iterator(); i.hasNext(); ) {
			XSDInclude include = (XSDInclude) i.next();
			
			includes.add( new File( include.getSchemaLocation() ) );
			namespaces.add( include.getSchema().getTargetNamespace() );
		}
	
		String result = 
			execute( "SchemaLocationResolverTemplate", new Object[]{ schema, includes, namespaces } );
		String prefix = Schemas.getTargetPrefix( schema ).toUpperCase();
		write( result, prefix + "SchemaLocationResolver" );
		
		//copy over all the schemas
		for ( Iterator i = includes.iterator(); i.hasNext(); ) {
			File include = (File) i.next();
			copy( include );
		}
	}
	
	public static void main( String[] args ) throws Exception {
		
		SchemaLocationResolverGenerator g = new SchemaLocationResolverGenerator();
		XSDSchema schema = null;
	
		for ( int i = 0; i < args.length; i++) {
			String arg = args[i];
		
			if ( "--schema".equals( arg ) ) {
				schema = Schemas.parse( args[++i] );
			}
			else if ( "--output".equals( arg ) ) {
				g.setLocation( args[++i] );
			}
			else if ( "--package".equals( arg ) ) {
				g.setPackageBase( args[++i] );
			}
		}
		
		g.generate( schema );
	}
}
