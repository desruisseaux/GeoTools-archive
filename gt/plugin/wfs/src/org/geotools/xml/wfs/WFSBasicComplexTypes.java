
package org.geotools.xml.wfs;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author Norman Barker www.comsine.com
 * @author dzwiers
 *
 */
public class WFSBasicComplexTypes {

    /**
     * <p>
     * This class represents an GetFeatureTypeType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * GetFeatureTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetFeatureType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetFeatureType();
    
        // static element list
        private static final Element[] elements = {
                new WFSElement("ResultFormat",
                    WFSComplexTypes.ResultFormatType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("DCPType",
                    WFSComplexTypes.DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        public Element findChildElement(String name) {
            return null;
        }
    
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
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs) throws SAXException, SAXNotSupportedException {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element,
         *      java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
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
                    WFSComplexTypes.SchemaDescriptionLanguageType.getInstance(),
                    1, 1, false, null),
                new WFSElement("DCPType",
                    WFSComplexTypes.DCPTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };
    
        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);
    
        public Element findChildElement(String name) {
            return null;
        }
    
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
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs) throws SAXException, SAXNotSupportedException {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element,
         *      java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
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
                    WFSComplexTypes.ServiceType.getInstance(), 1,
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
         * @see org.geotools.xml.schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }
    
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
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs) throws SAXException, SAXNotSupportedException {
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element,
         *      java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
    
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
        }
    }

    static class QueryType  extends WFSComplexType{
        
    }

    static class FeatureCollectionType  extends WFSComplexType{
        
    }
}
