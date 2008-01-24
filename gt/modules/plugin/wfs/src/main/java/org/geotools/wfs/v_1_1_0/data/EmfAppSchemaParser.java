package org.geotools.wfs.v_1_1_0.data;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GML;
import org.geotools.util.logging.Logging;
import org.geotools.xml.Binding;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.picocontainer.MutablePicoContainer;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class to parse FeatureType given by an XML schema location and the
 * name of the Feature <b>Element</b> whose type is the one needed.
 * <p>
 * Currently only <b>simple</b> FeatureTypes are supported. In the feature,
 * complex schemas may be supported by porting the <a
 * href="http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/config/EmfAppSchemaReader.java">EmfAppSchemaParser</a>
 * class in the community schema datastore module, depending on the availability
 * of complex {@link Feature} support on the mainstream GeoTools distribution.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
class EmfAppSchemaParser {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * Holds the mapping of xsd types to java types as needed by AttributeType
     * 
     * @see #findBinding(XSDElementDeclaration, Map)
     */
    private static final Map<QName, Class> xsdTypeToJavaBindings = new HashMap<QName, Class>();

    public static SimpleFeatureType parse(final QName featureTypeName, final URL schemaLocation,
            final CoordinateReferenceSystem crs) {
        ApplicationSchemaConfiguration configuration;
        {
            String namespaceURI = featureTypeName.getNamespaceURI();
            String uri = schemaLocation.toExternalForm();
            configuration = new ApplicationSchemaConfiguration(namespaceURI, uri);
        }
        SchemaIndex schemaIndex = Schemas.findSchemas(configuration);

        XSDComplexTypeDefinition typeDefinition = null;
        {
            XSDElementDeclaration elementDeclaration = null;
            elementDeclaration = schemaIndex.getElementDeclaration(featureTypeName);

            XSDTypeDefinition typeDef = elementDeclaration.getTypeDefinition();
            typeDefinition = (XSDComplexTypeDefinition) typeDef;
        }

        SimpleFeatureType featureType = createFeatureType(typeDefinition, configuration, crs);
        return featureType;
    }

    @SuppressWarnings("unchecked")
    private static SimpleFeatureType createFeatureType(
            final XSDComplexTypeDefinition typeDefinition, final Configuration configuration,
            final CoordinateReferenceSystem crs) {
        final List<XSDElementDeclaration> childElementDeclarations;
        {
            final boolean includeParents = true;
            childElementDeclarations = Schemas.getChildElementDeclarations(typeDefinition,
                    includeParents);
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        {
            final String typeNameNsUri = typeDefinition.getTargetNamespace();
            final String typeName = typeDefinition.getName();
            //set global state
            builder.setNamespaceURI(typeNameNsUri);
            builder.setName(typeName);
            builder.setCRS(crs);
        }

        final Map<QName, Class<? extends Binding>> bindings = configuration.setupBindings();

        String defaultGeometryName = null;
        for (XSDElementDeclaration elemDecl : childElementDeclarations) {
            String name = elemDecl.getName();
            String uri = elemDecl.getTargetNamespace();
            int maxOccurs = Schemas.getMaxOccurs(typeDefinition, elemDecl);
            int minOccurs = Schemas.getMinOccurs(typeDefinition, elemDecl);
            Class binding = findBinding(elemDecl, bindings);
            if (Geometry.class.isAssignableFrom(binding)) {
                if (!(GML.NAMESPACE.equals(uri)) && !(GML.location.getLocalPart().equals(name))) {
                    defaultGeometryName = name;
                }
            }
            builder.add(name, binding);
        }
        if (defaultGeometryName != null) {
            builder.setDefaultGeometry(defaultGeometryName);
        }

        SimpleFeatureType type = builder.buildFeatureType();
        return type;
    }

    private static Class findBinding(final XSDElementDeclaration elemDecl,
            Map<QName, Class<? extends Binding>> bindings) {
        final XSDTypeDefinition typeDefinition = elemDecl.getTypeDefinition();
        final QName elementName;
        final QName typeName;
        elementName = new QName(elemDecl.getTargetNamespace(), elemDecl.getName());
        typeName = new QName(typeDefinition.getTargetNamespace(), typeDefinition.getName());

        if (xsdTypeToJavaBindings.containsKey(elementName)) {
            return xsdTypeToJavaBindings.get(elementName);
        }
        if (xsdTypeToJavaBindings.containsKey(typeName)) {
            return xsdTypeToJavaBindings.get(typeName);
        }

        Class<? extends Binding> typeBinding = bindings.get(elementName);
        if (typeBinding == null) {
            typeBinding = bindings.get(typeName);
        }

        final Class binding;
        if (typeBinding == null) {
            LOGGER.info("No binding found for " + elementName + " of type " + typeName
                    + ", binding to String.class");
            binding = String.class;
        } else {
            Binding bindingInstance = null;
            try {
                bindingInstance = typeBinding.newInstance();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Error instantiating binding " + typeBinding.getName(), e);
            }
            binding = bindingInstance == null ? String.class : bindingInstance.getType();
        }

        synchronized (xsdTypeToJavaBindings) {
            xsdTypeToJavaBindings.put(typeName, binding);
        }
        return binding;
    }

}
