
package org.geotools.xml.wfs;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.PrintHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.DefaultChoice;
import org.geotools.xml.schema.DefaultFacet;
import org.geotools.xml.schema.DefaultSequence;
import org.geotools.xml.schema.DefaultSimpleType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.wfs.WFSSchema.WFSAttribute;
import org.geotools.xml.wfs.WFSSchema.WFSComplexType;
import org.geotools.xml.wfs.WFSSchema.WFSElement;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author Norman Barker www.comsine.com
 * @author dzwiers
 *
 */
public class WFSCapabilitiesComplexTypes {

    /**
     * <p>
     * This class represents an SchemaDescriptionLanguageType within the WFS
     * Schema.  This includes both the data and parsing functionality
     * associated with a SchemaDescriptionLanguageType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class SchemaDescriptionLanguageType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new SchemaDescriptionLanguageType();
        
        public static WFSComplexType getInstance(){return instance;}
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("XMLSCHEMA",
                    EmptyType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements) {
                /**
                 * @see schema.Choice#getMaxOccurs()
                 */
                public int getMaxOccurs() {
                    return Integer.MAX_VALUE;
                }
            };
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "SchemaDescriptionLanguageType";
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException("Method not completed yet.");
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException("Method not completed yet.");
        }
    }

    /**
     * <p>
     * This class represents an EmptyType within the WFS Schema.  This includes
     * both the data and parsing functionality associated with a EmptyType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class EmptyType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new EmptyType();
        
        public static WFSComplexType getInstance(){return instance;}

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException("Method not completed yet.");
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException("Method not completed yet.");
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "EmptyType";
        }
    }

    /**
     * <p>
     * This class represents an ResultFormatType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * ResultFormatType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class ResultFormatType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new ResultFormatType();
        
        public static WFSComplexType getInstance(){return instance;}

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException("Method not completed yet.");
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException("Method not completed yet.");
        }
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("GML2", EmptyType.getInstance(),
                    1, 1, false, null),
                new WFSElement("GML2-GZIP",
                    EmptyType.getInstance(), 0, 1, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements) {
                /**
                 * @see schema.Sequence#getMaxOccurs()
                 */
                public int getMaxOccurs() {
                    return Integer.MAX_VALUE;
                }
            };
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "ResultFormatType";
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
    }

    /**
     * <p>
     * This class represents an OperationsType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * OperationsType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class OperationsType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new OperationsType();
        
        public static WFSComplexType getInstance(){return instance;}

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException("Method not completed yet.");
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException("Method not completed yet.");
        }
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Insert",
                    EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Update",
                    EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Delete",
                    EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Query",
                    EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Lock", EmptyType.getInstance(),
                    1, 1, false, null),
            };
    
        // static sequence
        private static final DefaultChoice seq = new DefaultChoice(elements) {
                /**
                 * @see schema.Choice#getMaxOccurs()
                 */
                public int getMaxOccurs() {
                    return Integer.MAX_VALUE;
                }
            };
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "OperationsType";
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
    }

    /**
     * <p>
     * This class represents an MetadataURLType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * MetadataURLType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class MetadataURLType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new MetadataURLType();
        
        public static WFSComplexType getInstance(){return instance;}
        
        private static Attribute[] attributes = {
                new WFSAttribute("type", 
                        new DefaultSimpleType(null,null,WFSSchema.NAMESPACE,SimpleType.RESTRICTION, 
                        new SimpleType[] {XSISimpleTypes.NMTOKEN.getInstance()},
                        new Facet[] {new DefaultFacet(Facet.ENUMERATION,"TC211"), 
                        new DefaultFacet(Facet.ENUMERATION,"FGDC")}, SimpleType.NONE), 
                        WFSAttribute.REQUIRED),
                new WFSAttribute("format", 
                        new DefaultSimpleType(null,null,WFSSchema.NAMESPACE,SimpleType.RESTRICTION, 
                        new SimpleType[] {XSISimpleTypes.NMTOKEN.getInstance()},
                        new Facet[] {new DefaultFacet(Facet.ENUMERATION,"XML"), 
                        new DefaultFacet(Facet.ENUMERATION,"SGML"),new DefaultFacet(Facet.ENUMERATION,"TXT")}, 
                        SimpleType.NONE), WFSAttribute.REQUIRED)
            };
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // SimpleContent
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "MetadataURLType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return String.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an LatLonBoundingBoxType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * LatLonBoundingBoxType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class LatLonBoundingBoxType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new LatLonBoundingBoxType();
    
        // static element list
        private static Attribute[] attributes = {
                new WFSAttribute("minx", XSISimpleTypes.String.getInstance(),
                    WFSAttribute.REQUIRED),
                new WFSAttribute("miny", XSISimpleTypes.String.getInstance(),
                    WFSAttribute.REQUIRED),
                new WFSAttribute("maxx", XSISimpleTypes.String.getInstance(),
                    WFSAttribute.REQUIRED),
                new WFSAttribute("maxy", XSISimpleTypes.String.getInstance(),
                    WFSAttribute.REQUIRED)
            };
        public static WFSComplexType getInstance(){return instance;}
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LatLonBoundingBoxType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an PostType within the WFS Schema.  This includes
     * both the data and parsing functionality associated with a PostType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class PostType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new PostType();
    
        /**
         * @see org.geotools.xml.schema.ComplexType#findChildElement(java.lang.String)
         */
    
        // static list of attributes
        private static Attribute[] attributes = {
                new WFSAttribute("onlineResource",
                    XSISimpleTypes.String.getInstance(), WFSAttribute.REQUIRED)
            };
        public static WFSComplexType getInstance(){return instance;}
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "PostType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an HTTPType within the WFS Schema.  This includes
     * both the data and parsing functionality associated with a HTTPType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class HTTPType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new HTTPType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Get", GetType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("Post", GetType.getInstance(),
                    0, Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultChoice seq = new DefaultChoice(elements) {
                /**
                 * @see schema.Choice#getMaxOccurs()
                 */
                public int getMaxOccurs() {
                    return Integer.MAX_VALUE;
                }
            };
            /**
             * @see org.geotools.xml.schema.Type#getName()
             */
            public String getName() {
                return "HTTPType";
            }
            public static WFSComplexType getInstance(){return instance;}
            
            /**
             * @see org.geotools.xml.schema.ComplexType#getAttributes()
             */
            public Attribute[] getAttributes() {
                return null;
            }
            /**
             * @see org.geotools.xml.schema.ComplexType#getChild()
             */
            public ElementGrouping getChild() {
                return seq;
            }
            /**
             * @see org.geotools.xml.schema.ComplexType#getChildElements()
             */
            public Element[] getChildElements() {
                return elements;
            }
            /**
             * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
             */
            public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
                // TODO Auto-generated method stub
                throw new SAXNotSupportedException();
            }
            /**
             * @see org.geotools.xml.schema.Type#getInstanceType()
             */
            public Class getInstanceType() {
                // TODO Auto-generated method stub
                return null;
            }
            /**
             * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
             */
            public boolean canEncode(Element element, Object value, Map hints) {
                // TODO Auto-generated method stub
                return false;
            }
            /**
             * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
             */
            public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
                // TODO Auto-generated method stub
                throw new OperationNotSupportedException();
            }
    }

    /**
     * <p>
     * This class represents an GetType within the WFS Schema.  This includes
     * both the data and parsing functionality associated with a GetType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetType();
    
        // static list of attributes
        private static Attribute[] attributes = {
                new WFSAttribute("onlineResource",
                    XSISimpleTypes.String.getInstance(), WFSAttribute.REQUIRED)
            };
        public static WFSComplexType getInstance(){return instance;}
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "GetType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an FeatureTypeType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * FeatureTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class FeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeatureTypeType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Name", XSISimpleTypes.QName.getInstance(), 1,
                    1, false, null),
                new WFSElement("Title", XSISimpleTypes.String.getInstance(), 0,
                    1, false, null),
                new WFSElement("Abstract", XSISimpleTypes.String.getInstance(),
                    0, 1, false, null),
                new WFSElement("Keywords", XSISimpleTypes.String.getInstance(),
                    0, 1, false, null),
                new WFSElement("SRS", XSISimpleTypes.String.getInstance(), 1,
                    1, false, null),
                new WFSElement("Operations",
                    OperationsType.getInstance(), 0, 1, false, null),
                new WFSElement("LatLongBoundingBox",
                    LatLonBoundingBoxType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("MetadataURL",
                    MetadataURLType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeatureTypeType";
        }
        public static WFSComplexType getInstance(){return instance;}
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an DCPTypeType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * DCPTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class DCPTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DCPTypeType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("HTTP", HTTPType.getInstance(),
                    1, 1, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
        
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "DCPTypeType";
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            Element e = value[0].getElement();
    
            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }
    
            WFSCapability capab = new WFSCapability();
            capab.setRequest("DCPType");
    
            Object request;
            String vendorSpecificCapabs;
            request = vendorSpecificCapabs = null;
    
            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    request = value[i].getValue();
    
                    capab.setRequest(request);
                }
    
                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    vendorSpecificCapabs = (String) value[i].getValue();
    
                    capab.setVendorSpecificCapabilities(vendorSpecificCapabs);
                }
            }
    
            // check the required elements 
            if ((request == null) || (vendorSpecificCapabs == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }
            return capab;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return WFSCapability.class;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an LockFeatureTypeType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * LockFeatureTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class LockFeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new LockFeatureTypeType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("DCPType",
                    DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LockFeatureTypeType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an RequestType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * RequestType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class RequestType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new RequestType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("GetCapabilities",
                    GetCapabilitiesType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("DescribeFeatureType",
                    DescribeFeatureTypeType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("Transaction",
                    TransactionType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("GetFeature",
                    GetFeatureTypeType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("GetFeatureWithLock",
                    GetFeatureTypeType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("LockFeature",
                    LockFeatureTypeType.getInstance(), 1, 1,
                    false, null)
            };
    
        // static sequence
        private static final DefaultChoice seq = new DefaultChoice(elements) {
                /**
                 * @see schema.Choice#getMaxOccurs()
                 */
                public int getMaxOccurs() {
                    return Integer.MAX_VALUE;
                }
            };
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "RequestType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an ServiceType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * ServiceType .
     * </p>
     *
     * @see WFSComplexType
     */
    static class ServiceType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new ServiceType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Name", XSISimpleTypes.String.getInstance(), 1,
                    1, false, null),
                new WFSElement("Title", XSISimpleTypes.String.getInstance(), 1,
                    1, false, null),
                new WFSElement("Keywords", XSISimpleTypes.String.getInstance(),
                    0, 1, false, null),
                new WFSElement("Abstract", XSISimpleTypes.String.getInstance(),
                    0, 1, false, null),
                new WFSElement("OnlineResource",
                    XSISimpleTypes.AnyURI.getInstance(), 1, 1, false, null),
                new WFSElement("Fees", XSISimpleTypes.String.getInstance(), 0,
                    1, false, null),
                new WFSElement("AccessConstraints",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null)
            };
    
        // static choice
        private static final DefaultChoice seq = new DefaultChoice(elements);
    
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return WFSService.class;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "ServiceType";
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            Element e = value[0].getElement();
    
            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }
    
            String name;
            String title;
            String keywords;
            String onlineResource;
            String fees;
            String accessConstraints;
            name = title = keywords = onlineResource = fees = accessConstraints = null;
    
            WFSService service = new WFSService();
    
            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    name = (String) value[i].getValue();
    
                    service.setName(name);
                }
    
                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    title = (String) value[i].getValue();
    
                    service.setTitle(title);
                }
    
                if (elements[2].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    keywords = (String) value[i].getValue();
    
                    service.setKeywords(keywords);
                }
    
                if (elements[3].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    onlineResource = (String) value[i].getValue();
    
                    service.setOnlineResource(onlineResource);
                }
    
                if (elements[4].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    fees = (String) value[i].getValue();
    
                    service.setFees(fees);
                }
    
                if (elements[5].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    accessConstraints = (String) value[i].getValue();
    
                    service.setAccessConstraints(accessConstraints);
                }
            }
    
            // check the required elements 
            if ((name == null) || (title == null) || ((onlineResource) == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }
            return service;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an FeatureTypeListType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * FeatureTypeListType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class FeatureTypeListType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeatureTypeListType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Operations",
                    OperationsType.getInstance(), 0, 1, false,
                    null),
                new WFSElement("FeatureType",
                    FeatureTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeatureTypeListType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an CapabilityType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * CapabilityType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class CapabilityType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new CapabilityType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Request",
                    RequestType.getInstance(), 1, 1, false, null),
                new WFSElement("VendorSpecificCapabilities",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        public Element findChildElement(String name) {
            if (name == null) {
                return null;
            }
    
            for (int i = 0; i < elements.length; i++)
                if (name.equals(elements[i].getName())) {
                    return elements[i];
                }
    
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return WFSCapability.class;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "CapabilityType";
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */

        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            Element e = value[0].getElement();
    
            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }
    
            Object request;
            String vendorSpecificCapabs;
            request = vendorSpecificCapabs = null;
    
            WFSCapability capab = new WFSCapability();
    
            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    request = value[i].getValue();
    
                    capab.setRequest(request);
                }
    
                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    vendorSpecificCapabs = (String) value[i].getValue();
    
                    capab.setVendorSpecificCapabilities(vendorSpecificCapabs);
                }
            }
    
            // check the required elements 
            if ((request == null) || (vendorSpecificCapabs == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }
    
            return capab;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an WFS_CapabilitiesType within the WFS Schema.
     * This includes both the data and parsing functionality associated  with
     * an WFS_CapabilitiesType .
     * </p>
     *
     * @see WFSComplexType
     */
    static class WFS_CapabilitiesType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new WFS_CapabilitiesType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("Service",
                    ServiceType.getInstance(), 1, 1, false, null),
                new WFSElement("Capability",
                    GetCapabilitiesType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("FeatureTypeList",
                    FeatureTypeListType.getInstance(), 1, 1,
                    false, null),
                
                // TODO Add Filter_Capababilities from filterCapabilities.xsd
                new WFSElement("Filter_Capabilities",
                    XSISimpleTypes.String.getInstance(), 1, 1, false, null)
            };
        private static final Attribute[] attributes = {
                new WFSAttribute("version",
                    XSISimpleTypes.String.getInstance(), WFSAttribute.REQUIRED,
                    "1.0.0"),
                new WFSAttribute("updateSequence",
                    XSISimpleTypes.NonNegativeInteger.getInstance(),
                    WFSAttribute.REQUIRED, "0")
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "WFS_CapabilitiesType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an GetCapabilitiesType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * GetCapabilitiesType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetCapabilitiesType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetCapabilitiesType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("DCPType",
                    ServiceType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        /*
         * part of the singleton pattern
         *
         * @see WFSComplexType#getInstance()
         */
        static WFSComplexType getInstance() {
            return instance;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "GetCapabilitiesType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an DescribeFeatureType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * DescribeFeatureType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class DescribeFeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DescribeFeatureTypeType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("SchemaDescriptionLanguage",
                    SchemaDescriptionLanguageType.getInstance(),
                    1, 1, false, null),
                new WFSElement("DCPType",
                    DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "DescribeFeatureTypeType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an TransactionType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * TransactionType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class TransactionType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new TransactionType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("DCPType",
                    DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "TransactionType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    /**
     * <p>
     * This class represents an GetFeatureTypeType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * GetFeatureTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetFeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetFeatureTypeType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("ResultFormat",
                    ResultFormatType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("DCPType",
                    DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "GetFeatureTypeType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }
}
