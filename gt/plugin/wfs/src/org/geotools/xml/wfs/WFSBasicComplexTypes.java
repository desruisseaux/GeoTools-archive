/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.xml.wfs;

import org.geotools.xml.PrintHandler;
import org.geotools.xml.gml.GMLComplexTypes;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.DefaultAttribute;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Type;
import org.geotools.xml.wfs.WFSSchema.WFSComplexType;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import java.io.IOException;
import java.util.Map;
import javax.naming.OperationNotSupportedException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSBasicComplexTypes {
    
    public final static String LOCK_KEY = "WFSBasicComplexTypes.LOCKID.KEY";
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

        public static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
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
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "GetFeatureType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
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
     * This class represents an DescribeFeatureType within the WFS Schema. This
     * includes both the data and parsing functionality associated with a
     * DescribeFeatureType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class DescribeFeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DescribeFeatureTypeType();

        public static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
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
     * This class represents an GetCapabilitiesType within the WFS Schema. This
     * includes both the data and parsing functionality associated with a
     * GetCapabilitiesType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetCapabilitiesType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetCapabilitiesType();

        public static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
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

    static class QueryType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new QueryType();

        public static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
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
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "QueryType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
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

    static class FeatureCollectionType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();

        public static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return new Attribute[] {new DefaultAttribute(null,"lockId",WFSSchema.NAMESPACE,XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL,null,null,false),};
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return ((ComplexType)getParent()).getChild();
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            return GMLComplexTypes.AbstractFeatureCollectionType.getInstance();
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            
            String lock = null;
            lock = attrs.getValue("","lockID");
            if(lock == null || "".equals(lock))
                lock = attrs.getValue(WFSSchema.NAMESPACE,"lockID");
            
            if(hints!=null && lock!=null && (!"".equals(lock)))
                hints.put(LOCK_KEY,lock);
            
            return getParent().getValue(element,value,attrs,hints);
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeatureCollectionType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return getParent().getInstanceType();
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return getParent().canEncode(element,value,hints);
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO add the lockId attribute
            getParent().encode(element,value,output,hints);
        }
    }
}
