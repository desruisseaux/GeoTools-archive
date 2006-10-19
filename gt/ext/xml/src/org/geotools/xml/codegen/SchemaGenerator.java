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
package org.geotools.xml.codegen;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.build.basic.BasicDirectedGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.GraphWalker;
import org.geotools.graph.traverse.basic.BasicGraphTraversal;
import org.geotools.graph.traverse.standard.DirectedDepthFirstTopologicalIterator;
import org.geotools.graph.util.graph.CycleDetector;
import org.geotools.graph.util.graph.DirectedCycleDetector;
import org.geotools.xml.Schemas;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;


/**
 * Parses an XML schema to procuce an instance of
 * {@link org.opengis.feature.type.Schema}.
 *
 *         <p>
 *
 *  </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class SchemaGenerator extends AbstractGenerator {
    /**
     * The xsd schema from which gt types will be
     * generated.
     */
    XSDSchema schema;

    /**
     * Mapping from XSD types to Geotools types.
     */
    HashMap /*<XSDTypeDefinition,AttributeType>*/ types;

    /**
     * Factory used to build geotools types.
     */

    //TypeFactory factory;
    /**
     * Flag indicating wether simple types should be processed.
     */
    boolean simpleTypes;

    /**
     * Flag indiciating wether complex types should be processed.
     */
    boolean complexTypes;

    /**
     * Flag indicating wether to follow type references within
     * complex type definitions.
     */
    boolean followComplexTypes;

    /**
     *  Mapping of schemas imported by the schema being processed, indexed by
     *  namespace.
     */
    HashMap /*<String,Schema>*/ imports;

    /**
     * Logger
     */
    Logger logger = Logger.getLogger("org.geotools.xml");

    public SchemaGenerator(XSDSchema schema /*, TypeFactory factory*/) {
        this.schema = schema;
        //this.factory = factory;
        types = new HashMap();
        simpleTypes = true;
        complexTypes = true;
        followComplexTypes = true;
        imports = new HashMap();
    }

    /**
     * @return The parsed xml schema.
     */
    public XSDSchema getSchema() {
        return schema;
    }

    /**
     * @param complexTypes Flag indicating wether or not to process complex
     * types in the supplied schema.
     */
    public void setComplexTypes(boolean complexTypes) {
        this.complexTypes = complexTypes;
    }

    /**
     * @param simpleTypes Flag indicating wether or not to process complex
     * types in the supplied schema.
     */
    public void setSimpleTypes(boolean simpleTypes) {
        this.simpleTypes = simpleTypes;
    }

    /**
     * Indicates to generator wether to follow the type definitons of
     * complex types.
     * <p>
     * Warning, setting this flag to <code>true</code> will result in all
     * generated complex types being empty.
     * </p>
     */
    public void setFollowComplexTypes(boolean followComplexTypes) {
        this.followComplexTypes = followComplexTypes;
    }

    /**
     * Provide an explicit mapping from an XSD type
     * @param namespace
     * @param name
     */
    public void addTypeMapping(String namespace, String name,
        AttributeType gtType) {
        if (namespace == null) {
            namespace = schema.getTargetNamespace();
        }
        assert name != null;

        //find the type in the xsd schema
        List typeDefs = schema.getTypeDefinitions();

        for (Iterator itr = typeDefs.iterator(); itr.hasNext();) {
            XSDTypeDefinition xsdType = (XSDTypeDefinition) itr.next();
            String tns = xsdType.getTargetNamespace();
            String tn = xsdType.getName();

            if (namespace.equals(tns) && name.equals(tn)) {
                types.put(xsdType, gtType);

                return;
            }
        }

        throw new IllegalArgumentException("Type: [" + namespace + "," + name
            + "] not found");
    }

    /**
     * Adds an imported schema to be used for type lookups.
     */
    public void addImport(Schema imported) {
        imports.put(imported.namespace().getURI(), imported);
    }

    /**
     * Returns an imported schema for a particular namespace.
     *
     * @return The imported schema, or null if non exists.
     */
    public Schema getImport(String namespace) {
        return (Schema) imports.get(namespace);
    }

    /**
     * @return The collection of schemas imported by the schema being generated.
     */
    public Collection getImports() {
        return imports.values();
    }

    /**
     * @param type Geotools attribute type.
     *
     * @return the XSD type associated with <code>type</code>.
     */
    public XSDTypeDefinition getXSDType(AttributeType type) {
        for (Iterator itr = types.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Entry) itr.next();
            XSDTypeDefinition xsdType = (XSDTypeDefinition) entry.getKey();
            AttributeType gtType = (AttributeType) entry.getValue();

            if (gtType.equals(type)) {
                return xsdType;
            }
        }

        return null;
    }

    /**
     * Generates the Geotools schema from the XML schema.
     */
    public void generate() throws Exception {
        List typeDefs = schema.getTypeDefinitions();

        //process simple types
        if (simpleTypes) {
            for (Iterator itr = typeDefs.iterator(); itr.hasNext();) {
                XSDTypeDefinition xsdType = (XSDTypeDefinition) itr.next();

                if (xsdType.getName() == null) {
                    continue;
                }

                if (!xsdType.getTargetNamespace()
                                .equals(schema.getTargetNamespace())) {
                    continue;
                }

                if (xsdType instanceof XSDSimpleTypeDefinition) {
                    logger.info(xsdType.getName());
                    createType((XSDSimpleTypeDefinition) xsdType);
                }
            }
        }

        //process complex types
        if (complexTypes) {
            for (Iterator itr = typeDefs.iterator(); itr.hasNext();) {
                XSDTypeDefinition xsdType = (XSDTypeDefinition) itr.next();

                if (xsdType.getName() == null) {
                    continue;
                }

                if (!xsdType.getTargetNamespace()
                                .equals(schema.getTargetNamespace())) {
                    continue;
                }

                if (xsdType instanceof XSDComplexTypeDefinition) {
                    logger.info(xsdType.getName());
                    createType((XSDComplexTypeDefinition) xsdType);
                }
            }
        }

        Schema gtSchema = new SchemaImpl(schema.getTargetNamespace());

        for (Iterator itr = types.values().iterator(); itr.hasNext();) {
            AttributeType gtType = (AttributeType) itr.next();
            gtSchema.put(new org.geotools.feature.Name(
                    schema.getTargetNamespace(), gtType.getName()), gtType);
        }

        Object[] input = new Object[] {
                gtSchema, Schemas.getTargetPrefix(schema), this
            };
        String result = execute("SchemaClassTemplate", input);
        String className = Schemas.getTargetPrefix(schema).toUpperCase()
            + "Schema";

        write(result, className);
    }

    /**
     * Returns a list of the types in the generated schema sorted
     * as follows:
     * <p>
     *         <ul>
     *         <li>If A is a super type of B, then A appears in list before B.
     *         <li>If B is complex and A is referenced from the type definition
     * of B, then A appears in the list before B.
     *         </ul>
     *  </p>
     */

    //	public List sort() {
    //		//build a directed graph representing dependencies among types
    //		GraphGenerator gg = new BasicDirectedGraphGenerator();
    //		
    //		for (Iterator itr = types.values().iterator(); itr.hasNext();) {
    //			AttributeType type = (AttributeType) itr.next();
    //			AttributeType superType =  type.getSuper();
    //			
    //			if (superType != null) {
    //				//add edge type -> parent
    //				gg.add(new Object[]{type,superType});
    //			}
    //			
    //			if (type instanceof ComplexType) {
    //				ComplexType cType = (ComplexType) type;
    //				
    //				//add an edge for each descriptor
    //				Collection atts = cType.attributes();
    //				for (Iterator aitr = atts.iterator(); aitr.hasNext();) {
    //					AttributeDescriptor ad = (AttributeDescriptor) aitr.next();
    //					gg.add(new Object[]{type,ad.getType()});
    //				}
    //			}
    //		}
    //		
    //		Graph graph = gg.getGraph();
    //		
    //		//test the graph for cycles
    //		CycleDetector cycleDetector = new DirectedCycleDetector(graph);
    //		if (cycleDetector.containsCycle()) {
    //			logger.info("Cycle found");
    //			return null;
    //		}
    //			 
    //		
    //		//no cycles, perform a topological sorting of the graph
    //		DirectedDepthFirstTopologicalIterator iterator = 
    //			new DirectedDepthFirstTopologicalIterator();
    //		
    //		final ArrayList sorted = new ArrayList();
    //		GraphWalker walker = new GraphWalker() {
    //			
    //			public int visit(Graphable element, GraphTraversal traversal) {
    //				AttributeType type = (AttributeType) element.getObject();
    //				
    //				//only add if in this schema
    //				if (type.getName().getNamespaceURI().equals(schema.getTargetNamespace())) {
    //					sorted.add(element.getObject());	
    //				}
    //				
    //				return GraphTraversal.CONTINUE;
    //			}
    //			
    //			public void finish() { }
    //		};
    //		
    //		GraphTraversal traversal = 
    //			new BasicGraphTraversal(graph,walker,iterator);
    //		traversal.init();
    //		traversal.traverse();
    //		
    //		assert sorted.size() == types.size();
    //		Collections.reverse(sorted);
    //		
    //		return sorted;
    //	}

    //	private AttributeType createType(XSDTypeDefinition xsdType) {
    //		if (xsdType instanceof XSDSimpleTypeDefinition) {
    //			return createType((XSDSimpleTypeDefinition)xsdType);
    //		}
    //		else {
    //			return createType((XSDComplexTypeDefinition)xsdType);
    //		}
    //	}
    private AttributeType createType( /*XSDSimpleTypeDefinition*/
        XSDTypeDefinition xsdType) {
        if (types.containsKey(xsdType)) {
            return (AttributeType) types.get(xsdType);
        }

        //import?
        if (!xsdType.getTargetNamespace().equals(schema.getTargetNamespace())) {
            return (AttributeType) findType(xsdType);
        }

        //first build super type
        AttributeType superType = null;
        XSDTypeDefinition baseType = xsdType.getBaseType();

        if ((baseType != null) && !baseType.equals(xsdType)) {
            if (baseType.getName() != null) {
                //ignore unamed types
                //superType = createType((XSDSimpleTypeDefinition)baseType);
                superType = createType(baseType);
                assert superType != null;
            }
        }

        //TODO: actually derive valus from type
        //		AttributeType gtType = factory.createAttributeType(
        //			name(xsdType), Object.class, false, false, Collections.EMPTY_SET, 
        //			superType, null
        //		);
        AttributeType gtType = AttributeTypeFactory.newAttributeType(xsdType
                .getName(), Object.class);
        types.put(xsdType, gtType);

        return gtType;
    }

    //	private AttributeType createType(XSDComplexTypeDefinition xsdType) {
    //		//already processed?
    //		if (types.containsKey(xsdType)) {
    //			return (AttributeType) types.get(xsdType);
    //		}
    //		
    //		//import?
    //		if (!xsdType.getTargetNamespace().equals(schema.getTargetNamespace())) {
    //			return findType(xsdType);
    //		}
    //		
    //		//first build super type
    //		AttributeType/*ComplexType*/ superType = null;
    //		XSDTypeDefinition baseType = xsdType.getBaseType();
    //		if (baseType != null && !baseType.equals(xsdType)) {
    //			if (baseType.getName() != null) {
    //				//ignore unamed types
    //				superType = createType(/*(XSDComplexTypeDefinition)*/baseType);
    //				assert superType != null;
    //			}
    //		}
    //		
    //		// now build child types
    //		ArrayList properties = new ArrayList();
    //		if (followComplexTypes) {
    //			List children = Schemas.getChildElementDeclarations(xsdType);
    //			for (Iterator itr = children.iterator(); itr.hasNext();) {
    //				XSDElementDeclaration element = 
    //					(XSDElementDeclaration) itr.next();
    //				XSDTypeDefinition childType = element.getTypeDefinition();
    //				
    //				AttributeType gtType = createType(childType);
    //				assert gtType != null;
    //				
    //				String uri = element.getTargetNamespace();
    //				String name = element.getName();
    //				
    //				//TODO: minOccurs
    //				//TODO: maxOccurs
    //				//TODO: nillable
    //				AttributeDescriptor ad = factory.createAttributeDescriptor(
    //					gtType, new org.geotools.util.Name(uri,name),1,1,false
    //				);
    //				properties.add(ad);
    //			}
    //		}
    //		
    //		
    //		//TODO: isIdentifiable
    //		//TODO: restrictions
    //		//TODO: description
    //		ComplexType gtType = factory.createComplexType(
    //			name(xsdType), properties, false, xsdType.isAbstract(), 
    //			Collections.EMPTY_SET, superType, null
    //		);
    //		types.put(xsdType,gtType);
    //		return gtType;
    //	}
    private AttributeType findType(XSDTypeDefinition xsdType) {
        Name name = name(xsdType);

        if (imports != null) {
            for (Iterator itr = imports.values().iterator(); itr.hasNext();) {
                Schema imported = (Schema) itr.next();

                if (imported.containsKey(name)) {
                    return (AttributeType) imported.get(name);
                }
            }
        }

        throw new IllegalStateException("Could not find imported type: " + name);
    }

    /**
     * Convenience method for gettign the name of a type.
     */
    private Name name(XSDTypeDefinition type) {
        return new org.geotools.feature.Name(type.getTargetNamespace(),
            type.getName());
    }

    public static void main(String[] args) throws Exception {
        XSDSchema schema = XSDUtil.getSchemaForSchema(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);

        SchemaGenerator generator = new SchemaGenerator(schema);
        generator.setComplexTypes(false);
        generator.setFollowComplexTypes(false);
        generator.setSimpleTypes(true);

        generator.generate();
    }
}
