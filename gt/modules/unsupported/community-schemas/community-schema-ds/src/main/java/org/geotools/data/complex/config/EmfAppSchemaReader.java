package org.geotools.data.complex.config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDAttributeUseCategory;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.data.feature.adapter.ISOAttributeTypeAdapter;
import org.geotools.data.feature.adapter.ISOFeatureTypeAdapter;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.gml3.GMLSchema;
import org.geotools.gml3.bindings.GML;
import org.geotools.gml3.smil.SMIL20LANGSchema;
import org.geotools.gml3.smil.SMIL20Schema;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.geotools.xs.XSSchema;
import org.geotools.xs.bindings.XS;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

public class EmfAppSchemaReader {
    private static final Logger LOGGER = Logger.getLogger(EmfAppSchemaReader.class.getPackage()
            .getName());

    /**
     * Keyword used to store the EMF model of AttributeDescriptors and
     * AttributeTypes as userData properties on each instance
     */
    public static final String EMF_USERDATA_KEY = "EMF_MODEL";

    /**
     * Caches the GML 3.1.1 types and its dependencies
     */
    private static Map FOUNDATION_TYPES = new HashMap();

    /**
     * Contains all the AttributeDescriptors and AttributeTypes defined in the
     * application schema and its imports
     */
    private Map registry;

    private TypeFactory typeFactory;

    private EmfAppSchemaReader() {
        registry = new HashMap();
        typeFactory = new TypeFactoryImpl();
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public Map getTypeRegistry() {
        return new HashMap(this.registry);
    }

    public void parse(URL location) throws IOException {
        if (FOUNDATION_TYPES.isEmpty()) {
            createFoundationTypes();
        }
        registry.putAll(FOUNDATION_TYPES);

        String appSchemaUri = location.toExternalForm();
        LOGGER.info("Parsing application schema to emf:" + appSchemaUri);
        XSDSchema xsdSchema = Schemas.parse(appSchemaUri);

        LOGGER.info("Importing application schema " + appSchemaUri);
        importSchema(xsdSchema);
    }

    private void createFoundationTypes() {
        synchronized (FOUNDATION_TYPES) {
            if (!FOUNDATION_TYPES.isEmpty()) {
                return;
            }
            Schema schema;
            schema = new XSSchema();
            importSchema(schema);

            schema = new SMIL20Schema();
            importSchema(schema);

            schema = new SMIL20LANGSchema();
            importSchema(schema);

            schema = new GMLSchema();
            importSchema(schema);

            LOGGER.info("Creating GMLConfiguration to get the prebuilt gml schemas from");
            GMLConfiguration configuration = new GMLConfiguration();
            LOGGER.info("Aquiring prebuilt gml schema an its dependencies");
            SchemaIndex index = Schemas.findSchemas(configuration);
            XSDSchema[] schemas = index.getSchemas();

            LOGGER.info("Importing GML schema and dependencies");
            for (int i = 0; i < schemas.length; i++) {
                XSDSchema xsdSchema = schemas[i];
                importSchema(xsdSchema);
            }

            FOUNDATION_TYPES.putAll(registry);
            registry.clear();
        }
    }

    private void importSchema(XSDSchema xsdSchema) {
        String targetNamespace = xsdSchema.getTargetNamespace();
        LOGGER.fine("Importing schema " + targetNamespace);

        List typeDefinitions = xsdSchema.getTypeDefinitions();
        LOGGER.finer("Importing " + targetNamespace + " type definitions");
        importXsdTypeDefinitions(typeDefinitions);

        List elementDeclarations = xsdSchema.getElementDeclarations();
        LOGGER.finer("Importing " + targetNamespace + " element definitions");
        importElementDeclarations(elementDeclarations);
    }

    private void importElementDeclarations(List elementDeclarations) {
        XSDElementDeclaration elemDecl;
        for (Iterator it = elementDeclarations.iterator(); it.hasNext();) {
            elemDecl = (XSDElementDeclaration) it.next();
            LOGGER.finest("Creating attribute descriptor for " + elemDecl.getQName());
            AttributeDescriptor descriptor = createAttributeDescriptor(null, elemDecl);
            LOGGER.finest("Registering attribute descriptor " + descriptor.getName());
            register(descriptor);
        }
    }

    private void register(AttributeDescriptor descriptor) {
        Name name = descriptor.getName();
        registry.put(name, descriptor);
    }

    private void register(AttributeType type) {
        TypeName name = type.getName();
        Object old = registry.put(name, type);
        if (old != null) {
            System.err.println(type.getName() + " replaced by new value.");
        }
    }

    private AttributeDescriptor createAttributeDescriptor(XSDComplexTypeDefinition container,
            XSDElementDeclaration elemDecl) {
        String targetNamespace = elemDecl.getTargetNamespace();
        String name = elemDecl.getName();
        Name elemName = new org.geotools.feature.Name(targetNamespace, name);

        AttributeType type = getTypeOf(elemDecl);

        int minOccurs = container == null ? 0 : Schemas.getMinOccurs(container, elemDecl);
        int maxOccurs = container == null ? Integer.MAX_VALUE : Schemas.getMaxOccurs(container,
                elemDecl);
        boolean nillable = elemDecl.isNillable();

        if (maxOccurs == -1) {
            // this happens when maxOccurs is set to "unbounded"
            maxOccurs = Integer.MAX_VALUE;
        }
        AttributeDescriptor descriptor = typeFactory.createAttributeDescriptor(type, elemName,
                minOccurs, maxOccurs, nillable);

        descriptor.putUserData(EMF_USERDATA_KEY, elemDecl);

        return descriptor;
    }

    /**
     * If the type of elemDecl is annonymous creates a new type with the same
     * name than the atrribute and returns it. If it is not anonymous, looks it
     * up on the registry and in case the type does not exists in the registry
     * creates it and adds it to the registry.
     * 
     * @param elemDecl
     * @return
     */
    private AttributeType getTypeOf(XSDElementDeclaration elemDecl) {
        boolean registerIfNotExists = false;
        XSDTypeDefinition typeDefinition;

        // TODO REVISIT, I'm not sure this is the way to find out if the
        // element's type is defined in line (an thus no need to register it
        // as a global type)
        typeDefinition = elemDecl.getAnonymousTypeDefinition();
        if (typeDefinition == null) {
            registerIfNotExists = true;
            typeDefinition = elemDecl.getTypeDefinition();
        }

        AttributeType type;
        if (registerIfNotExists) {
            String targetNamespace = typeDefinition.getTargetNamespace();
            String name = typeDefinition.getName();
            type = getType(targetNamespace, name);
            if (type == null) {
                type = createType(typeDefinition);
                register(type);
            }
        } else {
            String name = elemDecl.getName();
            String targetNamespace = elemDecl.getTargetNamespace();
            TypeName overrideName = Types.typeName(targetNamespace, name);
            type = createType(overrideName, typeDefinition);
        }
        return type;
    }

    private AttributeType createType(XSDTypeDefinition typeDefinition) {
        String targetNamespace = typeDefinition.getTargetNamespace();
        String name = typeDefinition.getName();
        TypeName typeName = Types.typeName(targetNamespace, name);
        return createType(typeName, typeDefinition);
    }

    private AttributeType createType(final TypeName assignedName,
            final XSDTypeDefinition typeDefinition) {

        AttributeType attType;

        final XSDTypeDefinition baseType = typeDefinition.getBaseType();
        AttributeType superType = null;
        if (baseType != null) {
            String targetNamespace = baseType.getTargetNamespace();
            String name = baseType.getName();
            superType = getType(targetNamespace, name);
        }

        if (typeDefinition instanceof XSDComplexTypeDefinition) {
            XSDComplexTypeDefinition complexTypeDef;
            complexTypeDef = (XSDComplexTypeDefinition) typeDefinition;
            boolean includeParents = false;
            List children;
            children = Schemas.getChildElementDeclarations(typeDefinition, includeParents);

            final Collection schema = new ArrayList(children.size());

            XSDElementDeclaration childDecl;
            AttributeDescriptor descriptor;
            for (Iterator it = children.iterator(); it.hasNext();) {
                childDecl = (XSDElementDeclaration) it.next();
                descriptor = createAttributeDescriptor(complexTypeDef, childDecl);
                if (descriptor != null) {
                    schema.add(descriptor);
                }
            }

            attType = createType(assignedName, schema, typeDefinition, superType);

        } else {
            XSDSimpleTypeDefinition simpleType = typeDefinition.getSimpleType();
            XSDAnnotation derivationAnnotation = simpleType.getDerivationAnnotation();

            Class binding = String.class;
            boolean isIdentifiable = false;
            boolean isAbstract = false;
            Set restrictions = Collections.EMPTY_SET;
            InternationalString description = null;
            attType = typeFactory.createAttributeType(assignedName, binding, isIdentifiable,
                    isAbstract, restrictions, superType, description);
        }

        attType.putUserData(EMF_USERDATA_KEY, typeDefinition);
        return attType;
    }

    private AttributeType createType(TypeName assignedName, Collection schema,
            XSDTypeDefinition typeDefinition, AttributeType superType) {

        AttributeType abstractFType = getType(GML.NAMESPACE, GML.AbstractFeatureType.getLocalPart());
        assert abstractFType != null;

        boolean isFeatureType = isDerivedFrom(typeDefinition, abstractFType.getName());
        boolean isSimpleContent = isSimpleContent(schema);

        boolean isAbstract = false;// TODO
        Set restrictions = Collections.EMPTY_SET;
        InternationalString description = null; // TODO

        AttributeType type;
        if (isFeatureType) {
            if (isSimpleContent) {
                SimpleTypeFactory fac = new SimpleTypeFactoryImpl();
                List types = new ArrayList(schema);
                // let the factory decide
                CoordinateReferenceSystem crs = null;
                // let the factory decide
                AttributeDescriptor defaultGeometry = null;
                type = fac.createFeatureType(assignedName, schema, defaultGeometry, crs,
                        isAbstract, restrictions, superType, description);
            } else {
                type = typeFactory.createFeatureType(assignedName, schema, null, null, isAbstract,
                        restrictions, superType, description);

            }
        } else {
            boolean isIdentifiable = isIdentifiable((XSDComplexTypeDefinition) typeDefinition);
            type = typeFactory.createComplexType(assignedName, schema, isIdentifiable, isAbstract,
                    restrictions, superType, description);
        }
        return type;
    }

    /**
     * Determines if elements of the given complex type definition are required
     * to have an identifier by looking for a child element of
     * <code>typeDefinition</code> of the form
     * <code>&lt;xs:attribute ref=&quot;gml:id&quot; use=&quot;required&quot; /&gt;</code>
     * 
     * @param typeDefinition
     * @return
     */
    private boolean isIdentifiable(XSDComplexTypeDefinition typeDefinition) {
        List attributeUses = typeDefinition.getAttributeUses();

        final String idAttName = GML.id.getLocalPart();

        for (Iterator it = attributeUses.iterator(); it.hasNext();) {
            XSDAttributeUse use = (XSDAttributeUse) it.next();
            XSDAttributeUseCategory useCategory = use.getUse();

            XSDAttributeDeclaration idAtt = use.getAttributeDeclaration();

            String targetNamespace = idAtt.getTargetNamespace();
            String name = idAtt.getName();
            if (GML.NAMESPACE.equals(targetNamespace) && idAttName.equals(name)) {
                if (XSDAttributeUseCategory.REQUIRED_LITERAL.equals(useCategory)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if all the AttributeDescriptors contained in
     * <code>schema</code> are of a simple type and no one has maxOccurs > 1.
     * 
     * @param schema
     * @return
     */
    private boolean isSimpleContent(Collection schema) {
        AttributeDescriptor descriptor;
        for (Iterator it = schema.iterator(); it.hasNext();) {
            descriptor = (AttributeDescriptor) it.next();
            if (descriptor.getMaxOccurs() > 1) {
                return false;
            }
            if (descriptor.getType() instanceof ComplexType) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if <code>typeDefinition</code> is derived
     * from a type named <code>superTypeName</code>
     * 
     * @param typeDefinition
     * @param superTypeName
     * @return
     */
    private boolean isDerivedFrom(final XSDTypeDefinition typeDefinition,
            final TypeName superTypeName) {

        XSDTypeDefinition baseType;
        final String superNS = superTypeName.getNamespaceURI();
        final String superName = superTypeName.getLocalPart();

        String targetNamespace;
        String name;
        while ((baseType = typeDefinition.getBaseType()) != null) {
            targetNamespace = baseType.getTargetNamespace();
            name = baseType.getName();
            if (XS.NAMESPACE.equals(targetNamespace) && XS.ANYTYPE.getLocalPart().equals(name)) {
                return false;
            }
            if (superNS.equals(targetNamespace) && superName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private AttributeType getType(String namespace, String name) {
        TypeName typeName = Types.typeName(namespace, name);
        return getType(typeName);
    }

    private AttributeType getType(TypeName typeName) {
        AttributeType type = (AttributeType) registry.get(typeName);
        return type;
    }

    private void importXsdTypeDefinitions(List typeDefinitions) {
        XSDTypeDefinition typeDef;
        AttributeType attType;
        for (Iterator it = typeDefinitions.iterator(); it.hasNext();) {
            typeDef = (XSDTypeDefinition) it.next();
            String targetNamespace = typeDef.getTargetNamespace();
            String name = typeDef.getName();
            attType = getType(targetNamespace, name);
            if (attType == null) {
                LOGGER.finest("Creating attribute type " + typeDef.getQName());
                attType = createType(typeDef);
                LOGGER.finest("Registering attribute type " + attType.getName());
                register(attType);
            } else {
                LOGGER.finer("Ignoring type " + typeDef.getQName()
                        + " as it already exists in the registry");
            }
        }
        LOGGER.finer("--- type definitions imported successfully ---");
    }

    private void importSchema(Schema schema) {
        for (Iterator it = schema.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            Name key = (Name) entry.getKey();
            Object value = entry.getValue();
            if (registry.containsKey(key)) {
                LOGGER.finer("Ignoring " + key + " as it already exists. type "
                        + value.getClass().getName());
            } else {
                LOGGER.finer("Importing " + key + " of type " + value.getClass().getName());
                if (value instanceof AttributeType) {
                    AttributeType type = (AttributeType) value;
                    register(type);
                } else if (value instanceof AttributeDescriptor) {
                    AttributeDescriptor descriptor = (AttributeDescriptor) value;
                    register(descriptor);
                } else if (value instanceof org.geotools.feature.AttributeType) {
                    org.geotools.feature.AttributeType gtType;
                    gtType = (org.geotools.feature.AttributeType) value;
                    String nsUri = schema.namespace().getURI();
                    AttributeType isoType = ISOAttributeTypeAdapter.adapter(nsUri, gtType);
                    register(isoType);
                } else if (value instanceof org.geotools.feature.FeatureType) {
                    org.geotools.feature.FeatureType gtType;
                    gtType = (org.geotools.feature.FeatureType) value;
                    FeatureType isoType = new ISOFeatureTypeAdapter(gtType);
                    register(isoType);
                }
            }
        }
        LOGGER.fine("Schema " + schema.namespace().getURI() + " imported successfully");
    }

    public static EmfAppSchemaReader newInstance() {
        return new EmfAppSchemaReader();
    }

}
