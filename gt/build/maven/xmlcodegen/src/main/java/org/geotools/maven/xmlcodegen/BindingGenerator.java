/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.maven.xmlcodegen;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Schemas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generates bindings for types, elements, and attributes declared in an xml
 * schema.
 * <p>
 * Usage Example:
 * <pre>
 *         <code>
 *  XSDSchem schema = ...
 *  BindingGenerator g = new BindingGenerator();
 *  g.setPackageBase( "org.geotools.xml.xs" );
 *  g.setLocation( "/home/user" );
 *  g.generate( schema );
 *         </code>
 * </pre>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class BindingGenerator extends AbstractGenerator {
    static Logger logger = Logger.getLogger("org.geotools.xml");
    boolean generatingBindingInterface = true;
    boolean generatingBindingConfiguration = true;
    boolean generateAttributes = true;
    boolean generateElements = true;
    boolean generateTypes = true;
    boolean generateConfiguration = true;
    boolean generateSchemaLocationResolver = true;
    
    Set included = null;
    
    /**
     * Map of string, class which define the name and type of binding constructor
     * arguments.
     */
    Map/*<String,Class>*/ bindingConstructorArguments;
    
    public void setBindingConstructorArguments(
        Map bindingConstructorArguments) {
        this.bindingConstructorArguments = bindingConstructorArguments;
    }

    
    public void setGeneratingBindingConfiguration(
        boolean generatingBindingConfiguration) {
        this.generatingBindingConfiguration = generatingBindingConfiguration;
    }

    public void setGeneratingBindingInterface(
        boolean generatingBindingInterface) {
        this.generatingBindingInterface = generatingBindingInterface;
    }

    public void setGenerateAttributes(boolean generateAttributes) {
        this.generateAttributes = generateAttributes;
    }

    public void setGenerateElements(boolean generateElements) {
        this.generateElements = generateElements;
    }

    public void setGenerateTypes(boolean generateTypes) {
        this.generateTypes = generateTypes;
    }

    public void setIncluded(Set included) {
		this.included = included;
	}
    
    public void generate(XSDSchema schema) {
        List components = new ArrayList();

        if (generateElements) {
            List elements = schema.getElementDeclarations();

            for (Iterator e = elements.iterator(); e.hasNext();) {
                XSDElementDeclaration element = (XSDElementDeclaration) e.next();
                generate(element, schema);

                if (target(element, schema)) {
                	components.add(element);
                }
            }
        }

        if (generateTypes) {
        	List types = GeneratorUtils.allTypes( schema );

            for (Iterator t = types.iterator(); t.hasNext();) {
                XSDTypeDefinition type = (XSDTypeDefinition) t.next();
                generate( type, schema );
                
                if (target(type, schema)) {
                    components.add(type);
                }
            }
        }

        if (generateAttributes) {
            List attributes = schema.getAttributeDeclarations();

            for (Iterator a = attributes.iterator(); a.hasNext();) {
                XSDAttributeDeclaration attribute = (XSDAttributeDeclaration) a
                    .next();
                generate(attribute, schema);

                if (target(attribute, schema)) {
                    components.add(attribute);
                }
            }
        }

        if (generatingBindingConfiguration) {
            try {
                String result = execute("BindingConfigurationTemplate",
                        new Object[] { schema, components });
                write(result,
                    prefix(schema).toUpperCase() + "BindingConfiguration");
            } catch (Exception e) {
                String msg = "Error generating binding configuration";
                logger.log(Level.WARNING, msg, e);
            }
        }

        if (generatingBindingInterface) {
            try {
                String result = execute("BindingInterfaceTemplate", schema);
                write(result, prefix(schema).toUpperCase());
            } catch (Exception e) {
                String msg = "Error generating binding interface";
                logger.log(Level.WARNING, msg, e);
            }
        }
    }

    boolean included( XSDNamedComponent c ) {
    	return included != null ? included.contains( c.getName() ) : true;
    }
    
    boolean target(XSDNamedComponent c, XSDSchema schema) {
    	return c.getTargetNamespace().equals(schema.getTargetNamespace());
    }

    void generate(XSDNamedComponent c, XSDSchema schema) {
        if (!target(c, schema)) {
            return;
        }

        if ( !included( c ) ) {
        	return;
        }
        
        logger.info( "Generating binding for " + c.getName() );
        try {
            String result = execute("CLASS",
                    new Object[] { c, bindingConstructorArguments });
            write(result, name(c));
        } catch (Exception ioe) {
            String msg = "Unable to generate binding for " + c;
            logger.log(Level.WARNING, msg, ioe);
        }
    }

    String name(XSDNamedComponent c) {
        return c.getName().substring(0, 1).toUpperCase()
        + c.getName().substring(1) + "Binding";
    }

    public static void main(String[] args) throws Exception {
        XSDSchema schema = Schemas.parse("/home/jdeolive/devel/geotools/trunk/demo/xml-po/src/main/xsd/po.xsd");
        System.out.println( schema.getQNamePrefixToNamespaceMap() );
//        ArrayList cargList = new ArrayList();
//        HashSet includedTypes = new HashSet();
//        BindingGenerator g = new BindingGenerator();
//
//        if (args.length == 0) {
//            usage();
//            System.exit(0);
//        }
//
//        for (int i = 0; i < args.length; i++) {
//            String arg = args[i];
//
//            if ("--help".equals(arg)) {
//                usage();
//                System.exit(0);
//            }
//
//            if ("--schema".equals(arg)) {
//                schema = Schemas.parse(args[++i]);
//            } else if ("--output".equals(arg)) {
//                g.setLocation(args[++i]);
//            } else if ("--package".equals(arg)) {
//                g.setPackageBase(args[++i]);
//            } else if ("--include-type".equals(arg)) {
//            	includedTypes.add(args[++i]);
//            } else if ("--carg".equals(arg)) {
//                try {
//                    cargList.add(Class.forName(args[++i]));
//                } catch (ClassNotFoundException e) {
//                    String msg = "Could not load class: " + args[i];
//                    throw (IllegalArgumentException) new IllegalArgumentException(msg)
//                    .initCause(e);
//                }
//            } else if ("--noelements".equals(arg)) {
//                g.setGenerateElements(false);
//            } else if ("--noattributes".equals(arg)) {
//                g.setGenerateAttributes(false);
//            } else if ("--notypes".equals(arg)) {
//                g.setGenerateTypes(false);
//            } else if ("--no-binding-interface".equals(arg)) {
//            	g.setGeneratingBindingInterface( false );
//            } else if ("--no-binding-configuration".equals(arg)) {
//            	g.setGeneratingBindingConfiguration( false );
//            }
//            
//        }
//
//        Class[] cargs = null;
//
//        if (!cargList.isEmpty()) {
//            cargs = (Class[]) cargList.toArray(new Class[cargList.size()]);
//        }
//
//        if (schema == null) {
//            String msg = "ERROR: schema not specified";
//            usage();
//
//            throw new IllegalArgumentException(msg);
//        }
//
//        if (g.getLocation() == null) {
//            g.setLocation(System.getProperty("user.dir"));
//        }
//
//        g.setIncludedTypes(includedTypes);
//        //g.setBindingConstructorArguments(cargs);
//        g.generate(schema);
    }

    public static void usage() {
        System.out.println("Options");
        System.out.println("\t\t--help: Print this message");
        System.out.println("\t\t--schema <path>: Path to schema file");
        System.out.println("\t\t--output <path>: Path to output directory");
        System.out.println("\t\t--package <package>: Package out writen files");
        System.out.println("\t\t--include-type <type>: Include a single type" );
        System.out.println(
            "\t\t--carg <class>: Qualified class name of binding constructor argument");
        System.out.println(
            "\t\t--noelements: Turn off element binding generation");
        System.out.println(
            "\t\t--noattributes: Turn off attribute binding generation");
        System.out.println("\t\t--notypes: Turn off type binding generation");
        System.out.println("\t\t--no-binding-interface: Turn off binding interface generation");
        System.out.println("\t\t--no-binding-configuration: Turn off binding configuration generation");
    }
}
