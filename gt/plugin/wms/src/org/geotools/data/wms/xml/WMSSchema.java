/*
 * Created on 26-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.xml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.PrintHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.DefaultAttribute;
import org.geotools.xml.schema.DefaultFacet;
import org.geotools.xml.schema.DefaultSimpleType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;
import org.geotools.xml.schema.impl.AttributeGT;
import org.geotools.xml.wfs.WFSSchema;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.geotools.data.wms.xml.WMSComplexTypes.OperationType;
import org.geotools.data.wms.xml.WMSComplexTypes._WMS_CapabilitiesType;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSSchema implements Schema {

    private static Schema instance = new WMSSchema();
    public static URI NAMESPACE = makeURI("http://www.opengis.net/wms");
    
    static final Element[] elements = new Element[] {
        new WMSElement("WMS_Capabilities", _WMS_CapabilitiesType.getInstance()),
        
        new WMSElement("Name", XSISimpleTypes.String.getInstance()),
        new WMSElement("Title", XSISimpleTypes.String.getInstance()),
        new WMSElement("Abstract", XSISimpleTypes.String.getInstance()),
        new WMSElement("KeywordList", _KeywordListType.getInstance()), 
        new WMSElement("Keyword", _KeywordType.getInstance()),
        new WMSElement("OnlineResource", _OnlineResourceType.getInstance()),
        new WMSElement("Format", XSISimpleTypes.String.getInstance()),
        
        new WMSElement("Service", _ServiceType.getInstance()),
        new WMSElement("ContactInformation", _ContactInformationType.getInstance()),
        new WMSElement("ContactPersonPrimary", _ContactPersonPrimaryType.getInstance()),
        new WMSElement("ContactPerson", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactOrganization", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactPosition", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactAddress", _ContactAddressType.getInstance()),
        new WMSElement("AddressType", XSISimpleTypes.String.getInstance()),
        new WMSElement("Address", XSISimpleTypes.String.getInstance()),
        new WMSElement("City", XSISimpleTypes.String.getInstance()),
        new WMSElement("StateOrProvince", XSISimpleTypes.String.getInstance()),
        new WMSElement("PostCode", XSISimpleTypes.String.getInstance()),
        new WMSElement("Country", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactVoiceTelephone", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactFascimileTelephone", XSISimpleTypes.String.getInstance()),
        new WMSElement("ContactElectronicMailAddress", XSISimpleTypes.String.getInstance()),
        
        new WMSElement("Fees", XSISimpleTypes.String.getInstance()),
        new WMSElement("AccessConstraints", XSISimpleTypes.String.getInstance()),
        new WMSElement("LayerLimit", XSISimpleTypes.PositiveInteger.getInstance()),
        new WMSElement("MaxWidth", XSISimpleTypes.PositiveInteger.getInstance()),
        new WMSElement("MaxHeight", XSISimpleTypes.PositiveInteger.getInstance()),
        
        new WMSElement("Capability", _CapabilityType.getInstance()),
        new WMSElement("Request", _RequestType.getInstance()),
        new WMSElement("GetCapabilities", OperationType.getInstance()),
        new WMSElement("GetMap", OperationType.getInstance()),
        new WMSElement("GetFeatureInfo", OperationType.getInstance()),
        new WMSElement("_ExtendedOperation", OperationType.getInstance()), //is abstract
        
        new WMSElement("DCPType", _DCPTypeType.getInstance()),
        new WMSElement("HTTP", _HTTPType.getInstance()),
        new WMSElement("Get", _GetType.getInstance()),
        new WMSElement("Post", _PostType.getInstance()),
                
        new WMSElement("Exception", _ExceptionType.getInstance()),
        new WMSElement("_ExtendedCapabilities", _ExtendedCapabilitiesType.getInstance()),
        
        new WMSElement("Layer", _LayerType.getInstance()),
        new WMSElement("CRS", XSISimpleTypes.String.getInstance()),
        new WMSElement("EX_GeographicBoundingBox", _EX_GeographicBoundingBoxType.getInstance()),
        new WMSElement("BoundingBox", _BoundingBoxType.getInstance()),
        new WMSElement("Dimension", _DimensionType.getInstance()),
        new WMSElement("Attribution", _AttributionType.getInstance()),
        new WMSElement("LogoURL", _LogoURLType.getInstance()),
        new WMSElement("MetadataURL", _MetadataURLType.getInstance()),
        new WMSElement("AuthorityURL", _AuthorityURLType.getInstance()),
        new WMSElement("Identifier", _IdentifierType.getInstance()),
        new WMSElement("DataURL", _DataURLType.getInstance()),
        new WMSElement("FeatureListURL", _FeatureListURLType.getInstance()),
        new WMSElement("Style", _StyleType.getInstance()),
        new WMSElement("LegendURL", _LegendURLType.getInstance()),
        new WMSElement("StyleSheetURL", _StyleSHeetURLType.getInstance()),
        new WMSElement("StyleURL", _StyleURLType.getInstance()),
        new WMSElement("MinScaleDenominator", XSISimpleTypes.Double.getInstance()),
        new WMSElement("MaxScaleDenominator", XSISimpleTypes.Double.getInstance())
    };
    
    static final ComplexType[] complexTypes = new ComplexType[] {
        OperationType.getInstance()
    };

    static final SimpleType[] simpleTypes = new SimpleType[] {
        new DefaultSimpleType(null, "longitudeType", NAMESPACE, DefaultSimpleType.RESTRICTION,
                new SimpleType[] { XSISimpleTypes.String.getInstance() },
                	new Facet[] { 
                		new DefaultFacet(Facet.MININCLUSIVE, "-180"),
                		new DefaultFacet(Facet.MAXINCLUSIVE, "180") },
                	SimpleType.NONE
                ),
                
        new DefaultSimpleType(null, "latitudeType", NAMESPACE, DefaultSimpleType.RESTRICTION,
                new SimpleType[] { XSISimpleTypes.String.getInstance() },
                	new Facet[] { 
                		new DefaultFacet(Facet.MININCLUSIVE, "-90"),
                		new DefaultFacet(Facet.MAXINCLUSIVE, "90") },
                SimpleType.NONE
        ),

    };

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getAttributeGroups()
     */
    public AttributeGroup[] getAttributeGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getAttributes()
     */
    public Attribute[] getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getBlockDefault()
     */
    public int getBlockDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getComplexTypes()
     */
    public ComplexType[] getComplexTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getElements()
     */
    public Element[] getElements() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getFinalDefault()
     */
    public int getFinalDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getGroups()
     */
    public Group[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getId()
     */
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getImports()
     */
    public Schema[] getImports() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getPrefix()
     */
    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getSimpleTypes()
     */
    public SimpleType[] getSimpleTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getTargetNamespace()
     */
    public URI getTargetNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getURI()
     */
    public URI getURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getVersion()
     */
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#includesURI(java.net.URI)
     */
    public boolean includesURI( URI uri ) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#isAttributeFormDefault()
     */
    public boolean isAttributeFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#isElementFormDefault()
     */
    public boolean isElementFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Schema instance() {
        return instance;
    }

    // convinience method to deal with the URISyntaxException
    private static URI makeURI(String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            // do nothing
            return null;
        }
    }
    
    static class WMSElement implements Element {
        
        private int max;
        private int min;
        private String name;
        private Type type;

        /**
         * @param name
         * @param type
         */
        public WMSElement( String name, Type type ) {
            super();
            this.name = name;
            this.type = type;
            this.min = 1;
            this.max = 1;
        }
        /**
         * @param max
         * @param min
         * @param name
         * @param type
         */
        public WMSElement( String name, Type type, int min, int max ) {
            super();
            this.max = max;
            this.min = min;
            this.name = name;
            this.type = type;
        }
        private WMSElement() {
            
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getDefault()
         */
        public String getDefault() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return NONE;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getFixed()
         */
        public String getFixed() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#isForm()
         */
        public boolean isForm() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getMaxOccurs()
         */
        public int getMaxOccurs() {
            // TODO Auto-generated method stub
            return max;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getMinOccurs()
         */
        public int getMinOccurs() {
            // TODO Auto-generated method stub
            return min;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return name;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getNamespace()
         */
        public URI getNamespace() {
            // TODO Auto-generated method stub
            return NAMESPACE;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#isNillable()
         */
        public boolean isNillable() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getSubstitutionGroup()
         */
        public Element getSubstitutionGroup() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Element#getType()
         */
        public Type getType() {
            // TODO Auto-generated method stub
            return type;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            // TODO Auto-generated method stub
            return ELEMENT;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement( String name ) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    static abstract class WMSComplexType implements ComplexType {

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache( Element element, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs, Map hints ) throws SAXException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public URI getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints ) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement( String name ) {
            // TODO Auto-generated method stub
            return null;
        }

    }
    
    static class WMSAttribute extends AttributeGT {

        public WMSAttribute( String id, String name, URI namespace, SimpleType type, int use, String _default, String fixed, boolean form ) {
            super(id, name, namespace, type, use, _default, fixed, form);
        }
        
        public WMSAttribute(String name, SimpleType simpleType) {
            super(null, name, WMSSchema.NAMESPACE, simpleType, OPTIONAL, null,
                null, false);
        }
    }
}
