package org.geotools.maven.xmlcodegen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Schemas;

/**
 * Utility class used for generating.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GeneratorUtils {

	/**
	 * Returns all the types defined in <param>schema</param>, including anonymous complex
	 * types.
	 * 
	 * @param schema the schema.
	 * 
	 * @return A list of types, including anonymous complex types.
	 */
	public static List allTypes( XSDSchema schema ) {
		//get the named types
		List types = new ArrayList( schema.getTypeDefinitions() );
    	
		//get anonymous types
		List anonymous = new ArrayList();
		
		//add any anonymous types of global elements
		for ( Iterator e = schema.getElementDeclarations().iterator(); e.hasNext(); ) {
			XSDElementDeclaration element = (XSDElementDeclaration ) e.next();
			if ( element.getAnonymousTypeDefinition() != null ) {
				element.getAnonymousTypeDefinition().setName( "_" + element.getName() );
				anonymous.add( element.getAnonymousTypeDefinition() );
			}
		}
		
    	//add any anonymous types foudn with type definitions
    	for ( Iterator t = types.iterator(); t.hasNext(); ) {
    		XSDTypeDefinition type = (XSDTypeDefinition) t.next();
    		List particles = Schemas.getChildElementParticles( type, false );
    		for ( Iterator p = particles.iterator(); p.hasNext(); ) {
    			XSDParticle particle = (XSDParticle) p.next();
    			XSDElementDeclaration element = (XSDElementDeclaration) particle.getContent();
    			
    			//ignore element references, caught in teh above loop
    			if ( element.isElementDeclarationReference() )
    				continue;
    			
    			if ( element.getAnonymousTypeDefinition() != null ) {
    				element.getAnonymousTypeDefinition().setName( type.getName() + "_" + element.getName() );
    				anonymous.add( element.getAnonymousTypeDefinition() );
    			}
    		}
    	}
    	
    	types.addAll( anonymous );
    	return types;
	}
}
