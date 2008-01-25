package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.bindings.GML3ParsingUtils;
import org.geotools.util.logging.Logging;
import org.geotools.xml.BindingFactory;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.geotools.xml.impl.BindingFactoryImpl;
import org.geotools.xml.impl.BindingLoader;
import org.geotools.xml.impl.BindingWalkerFactoryImpl;
import org.geotools.xml.impl.NamespaceSupportWrapper;
import org.geotools.xml.impl.ParserHandler;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.xml.sax.helpers.NamespaceSupport;

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
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0/data/EmfAppSchemaParser.java $
 */
class EmfAppSchemaParser {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * Note: this code is borrowed and adapted from
     * {@link ParserHandler#startDocument()}
     * 
     * @param wfsConfiguration
     * @param featureTypeName
     * @param schemaLocation
     * @param crs
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static SimpleFeatureType parse(final Configuration wfsConfiguration,
            final QName featureTypeName, final URL schemaLocation,
            final CoordinateReferenceSystem crs) throws IOException {

        XSDElementDeclaration elementDecl = parseFeatureType(featureTypeName, schemaLocation);

        Map bindings = wfsConfiguration.setupBindings();
        BindingLoader bindingLoader = new BindingLoader(bindings);

        // create the document handler + root context
        // DocumentHandler docHandler =
        // handlerFactory.createDocumentHandler(this);

        MutablePicoContainer context = wfsConfiguration.setupContext(new DefaultPicoContainer());
        NamespaceSupport namespaces = new NamespaceSupport();
        // setup the namespace support
        context.registerComponentInstance(namespaces);
        context.registerComponentInstance(new NamespaceSupportWrapper(namespaces));

        // binding factory support
        BindingFactory bindingFactory = new BindingFactoryImpl(bindingLoader);
        context.registerComponentInstance(bindingFactory);

        // binding walker support
        BindingWalkerFactoryImpl bwFactory = new BindingWalkerFactoryImpl(bindingLoader, context);
        context.registerComponentInstance(bwFactory);

        try {
            SimpleFeatureType featureType = GML3ParsingUtils.featureType(elementDecl, bwFactory);
            if (crs != null) {
                SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.setName(featureType.getName());
                builder.setAbstract(featureType.isAbstract());
                builder.setDescription(featureType.getDescription());
                if (featureType.getSuper() instanceof SimpleFeatureType) {
                    builder.setSuperType((SimpleFeatureType) featureType.getSuper());
                }
                List<AttributeDescriptor> attributes = featureType.getAttributes();
                final GeometryDescriptor defaultGeometry = featureType.getDefaultGeometry();
                for (AttributeDescriptor descriptor : attributes) {
                    if (descriptor instanceof GeometryDescriptor) {
                        String name = descriptor.getLocalName();
                        Class binding = descriptor.getType().getBinding();
                        builder.add(name, binding, crs);
                    } else {
                        builder.add(descriptor);
                    }
                }
                if (defaultGeometry != null) {
                    builder.setDefaultGeometry(defaultGeometry.getLocalName());
                }
                featureType = builder.buildFeatureType();
            }
            return featureType;
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw (IOException) new IOException().initCause(e);
        }
    }

    /**
     * TODO: add connectionfactory parameter to handle authentication, gzip, etc
     * 
     * @param featureTypeName
     * @param schemaLocation
     * @return
     */
    private static XSDElementDeclaration parseFeatureType(final QName featureTypeName,
            final URL schemaLocation) {
        ApplicationSchemaConfiguration configuration;
        {
            String namespaceURI = featureTypeName.getNamespaceURI();
            String uri = schemaLocation.toExternalForm();
            configuration = new ApplicationSchemaConfiguration(namespaceURI, uri);
        }
        SchemaIndex schemaIndex = Schemas.findSchemas(configuration);

        XSDElementDeclaration elementDeclaration;
        elementDeclaration = schemaIndex.getElementDeclaration(featureTypeName);
        return elementDeclaration;
    }
}
