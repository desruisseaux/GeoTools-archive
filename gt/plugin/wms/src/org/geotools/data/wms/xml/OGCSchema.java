package org.geotools.data.wms.xml;

import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;

public class ogcSchema implements Schema {

    public static final URI NAMESPACE = loadNS();
    private static URI loadNS() {
        try {
            return new URI("http://www.opengis.net/ows");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public int getBlockDefault() {
        return 0;
    }

    public int getFinalDefault() {
        return 0;
    }

    public String getId() {
        return "null";
    }
    private static Schema[] imports = null;

    public Schema[] getImports() {
        if (imports == null) {
            imports = new Schema[]{
            // TODO add instance of org.geotools.xml.gml.GMLSchema@df8f5e,
            // TODO add instance of
            // org.geotools.xml.handlers.xsi.SchemaHandler$DefaultSchema@16b13c7
            };
        }
        return imports;
    }

    public String getPrefix() {
        return "ogc";
    }

    public URI getTargetNamespace() {
        return NAMESPACE;
    }

    public URI getURI() {
        return NAMESPACE;
    }

    public String getVersion() {
        return "null";
    }

    public boolean includesURI( URI uri ) {
        // // TODO fill me in!
        return false; // // safer
    }

    public boolean isAttributeFormDefault() {
        return false;
    }

    public boolean isElementFormDefault() {
        return false;
    }

    public AttributeGroup[] getAttributeGroups() {
        return null;
    }
    public Attribute[] getAttributes() {
        return null;
    }
    /**
     * TODO comment here
     */
    private static ComplexType[] complexTypes = null;
    public ComplexType[] getComplexTypes() {
        if (complexTypes == null) {
            complexTypes = new ComplexType[]{ogcComplexTypes.VendorType.getInstance()};
        }
        return complexTypes;
    }
    /**
     * TODO comment here
     */
    private static Element[] elements = null;
    public Element[] getElements() {
        if (elements == null) {
            elements = new Element[]{
                    new ogcElement("GetCapabilities",ogcComplexTypes._GetCapabilities.getInstance(),null,1,1),
                    new ogcElement("GetMap", ogcComplexTypes._GetMap.getInstance(), null, 1, 1),
                    new ogcElement("ogc:GetFeatureInfo", ogcComplexTypes._GetFeatureInfo.getInstance(), null,
                            1, 1)};
        }
        return elements;
    }
    public Group[] getGroups() {
        return null;
    }
    /**
     * TODO comment here
     */
    private static SimpleType[] simpleTypes = null;
    public SimpleType[] getSimpleTypes() {
        if (simpleTypes == null) {
            simpleTypes = new SimpleType[]{
                    ogcSimpleTypes.CapabilitiesSectionType.getInstance(),
                    ogcSimpleTypes.FormatType.getInstance(),
                    ogcSimpleTypes.OWSType.getInstance(),
                    ogcSimpleTypes.ExceptionsType.getInstance()};
        }
        return simpleTypes;
    }
}
