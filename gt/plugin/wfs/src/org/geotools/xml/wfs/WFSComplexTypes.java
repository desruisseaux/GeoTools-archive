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
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.wfs.WFSSchema.WFSAttribute;
import org.geotools.xml.wfs.WFSSchema.WFSComplexType;
import org.geotools.xml.wfs.WFSSchema.WFSElement;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import java.io.IOException;
import java.util.Map;

//import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

//import java.util.HashMap;
import javax.naming.OperationNotSupportedException;


//import org.apache.commons.beanutils.*;

/**
 * <p>
 * This class is intended to act as a collection of package visible WFS
 * complexType definition to be used by the WFSSchema
 * </p>
 *
 * @author Norman Barker www.comsine.com
 *
 * @see WFSSchema
 * @see ComplexType
 */
public class WFSComplexTypes {
    // used for debugging
    private static Logger logger = Logger.getLogger(
            "net.refractions.wfs.static");

    /**
     * <p>
     * Default implementation, used to pass data to parents in the  inheritance
     * tree.
     * </p>
     *
     * @author dzwiers
     *
     * @see ElementValue
     */
    private static class DefaultElementValue implements ElementValue {
        // local data variables 
        private Element elem;
        private Object value;

        /**
         * The input method for the data to store.
         *
         * @param elem
         * @param value
         */
        public DefaultElementValue(Element elem, Object value) {
            this.elem = elem;
            this.value = value;
        }

        /**
         * @see schema.ElementValue#getElement()
         */
        public Element getElement() {
            return elem;
        }

        /**
         * @see schema.ElementValue#getValue()
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * <p>
     * Many complexTypes have Choices as part of their definition. Instances of
     * this class are used to represent these choices.
     * </p>
     *
     * @author dzwiers
     *
     * @see Choice
     */
    private static class DefaultChoice implements Choice {
        // the element set to pick one of
        private Element[] elements = null;

        /*
         * Should not be called
         */
        private DefaultChoice() {
        }

        /**
         * Initializes this instance with a set of elements to choose from.
         *
         * @param elems
         */
        public DefaultChoice(Element[] elems) {
            elements = elems;
        }

        /**
         * @see schema.Choice#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see schema.Choice#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return 1;
        }

        /**
         * @see schema.Choice#getMinOccurs()
         */
        public int getMinOccurs() {
            return 1;
        }

        /**
         * @see schema.Choice#getChildren()
         */
        public ElementGrouping[] getChildren() {
            return elements;
        }

        /**
         * @see schema.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return CHOICE;
        }

        /**
         * @see schema.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            if ((elements == null) || (elements.length == 0) || (name == null)) {
                return null;
            }

            for (int i = 0; i < elements.length; i++)
                if (name.equals(elements[i].getName())) {
                    return elements[i];
                }

            return null;
        }
    }

    /**
     * <p>
     * Many complexTypes have Sequences as part of their definition. Instances
     * of this class are used to represent these sequences.
     * </p>
     *
     * @author dzwiers
     */
    private static class DefaultSequence implements Sequence {
        // the list of elements in the sequence (order matters here)
        private Element[] elements = null;

        /*
         * Should not be called
         */
        private DefaultSequence() {
        }

        /**
         * Initializes the Sequence with a list of elements within the Sequence
         *
         * @param elems
         */
        public DefaultSequence(Element[] elems) {
            elements = elems;
        }

        /**
         * @see schema.Sequence#getChildren()
         */
        public ElementGrouping[] getChildren() {
            return elements;
        }

        /**
         * @see schema.Sequence#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see schema.Sequence#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return 1;
        }

        /**
         * @see schema.Sequence#getMinOccurs()
         */
        public int getMinOccurs() {
            return 1;
        }

        /**
         * @see schema.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return SEQUENCE;
        }

        /**
         * @see schema.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            if ((elements == null) || (elements.length == 0) || (name == null)) {
                return null;
            }

            for (int i = 0; i < elements.length; i++)
                if (name.equals(elements[i].getName())) {
                    return elements[i];
                }

            return null;
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
                    WFSComplexTypes.ServiceType.getInstance(), 1, 1, false, null),
                new WFSElement("Capability",
                    WFSComplexTypes.GetCapabilitiesType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("FeatureTypeList",
                    WFSComplexTypes.FeatureTypeListType.getInstance(), 1, 1,
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
            return attributes;
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

        static WFSComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#findChildElement(java.lang.String)
         */
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
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
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

            //            Map map = new HashMap();
            WFSService service = new WFSService();

            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    name = (String) value[i].getValue();

                    //                	map.put("name", name);
                    service.setName(name);
                }

                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    title = (String) value[i].getValue();

                    //                	map.put("title", title);
                    service.setTitle(title);
                }

                if (elements[2].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    keywords = (String) value[i].getValue();

                    //                	map.put("keywords", keywords);
                    service.setKeywords(keywords);
                }

                if (elements[3].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    onlineResource = (String) value[i].getValue();

                    //                	map.put("onlineResource", onlineResource);
                    service.setOnlineResource(onlineResource);
                }

                if (elements[4].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    fees = (String) value[i].getValue();

                    //                	map.put("fees", fees);
                    service.setFees(fees);
                }

                if (elements[5].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    accessConstraints = (String) value[i].getValue();

                    //                	map.put("accessConstraints", accessConstraints);
                    service.setAccessConstraints(accessConstraints);
                }
            }

            // check the required elements 
            if ((name == null) || (title == null) || ((onlineResource) == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }

            //            ServiceType service = new ServiceType();
            //			try {
            //				BeanUtils.populate(service, map);
            //			} catch (IllegalAccessException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			} catch (InvocationTargetException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			}
            return service;
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
                new WFSElement("HTTP", WFSComplexTypes.HTTPType.getInstance(),
                    1, 1, false, null)
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /**
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
            return "DCPTypeType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs) throws SAXException, SAXNotSupportedException {
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

            //            Map map = new HashMap();
            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    request = value[i].getValue();

                    //                	map.put("name", request);
                    capab.setRequest(request);
                }

                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    vendorSpecificCapabs = (String) value[i].getValue();

                    //                	map.put("vendorSpecificCapabilities", vendorSpecificCapabs);
                    capab.setVendorSpecificCapabilities(vendorSpecificCapabs);
                }
            }

            // check the required elements 
            if ((request == null) || (vendorSpecificCapabs == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }

            //			try {
            //				BeanUtils.populate(capab, map);
            //			} catch (IllegalAccessException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			} catch (InvocationTargetException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			}
            return capab;
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
                new WFSElement("Get", WFSComplexTypes.GetType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("Post", WFSComplexTypes.GetType.getInstance(),
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

        /**
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
            return attributes;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
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

        /**
         * part of the singleton pattern
         *
         * @see WFSComplexType#getInstance()
         */
        static WFSComplexType getInstance() {
            return instance;
        }

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
            return attributes;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
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
                    WFSComplexTypes.OperationsType.getInstance(), 0, 1, false,
                    null),
                new WFSElement("FeatureType",
                    WFSComplexTypes.FeatureTypeType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null)
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

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

        // static element list
        private static final Element[] elements = {  };

        /**
         * @see org.geotools.xml.schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }

        /* *
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
            return null;
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

        // static element list
        private static final Element[] elements = {
                new WFSElement("Insert",
                    WFSComplexTypes.EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Update",
                    WFSComplexTypes.EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Delete",
                    WFSComplexTypes.EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Query",
                    WFSComplexTypes.EmptyType.getInstance(), 1, 1, false, null),
                new WFSElement("Lock", WFSComplexTypes.EmptyType.getInstance(),
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
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null),
                new WFSElement("LatLongBoundingBox",
                    WFSComplexTypes.LatLonBoundingBoxType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("MetadataURL",
                    WFSComplexTypes.MetadataURLType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null)
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

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

        public Element findChildElement(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

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
        private static Attribute[] attributes = {
                new WFSAttribute("type", XSISimpleTypes.NMTOKEN.getInstance(),
                    WFSAttribute.REQUIRED),
                new WFSAttribute("format",
                    XSISimpleTypes.NMTOKEN.getInstance(), WFSAttribute.REQUIRED)
            };

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
            return attributes;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
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
                    WFSComplexTypes.RequestType.getInstance(), 1, 1, false, null),
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
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs) throws SAXException, SAXNotSupportedException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Object request;
            String vendorSpecificCapabs;
            request = vendorSpecificCapabs = null;

            WFSCapability capab = new WFSCapability();

            //            Map map = new HashMap();
            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    request = value[i].getValue();

                    //                	map.put("name", request);
                    capab.setRequest(request);
                }

                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    vendorSpecificCapabs = (String) value[i].getValue();

                    //                	map.put("vendorSpecificCapabilities", vendorSpecificCapabs);
                    capab.setVendorSpecificCapabilities(vendorSpecificCapabs);
                }
            }

            // check the required elements 
            if ((request == null) || (vendorSpecificCapabs == null)) {
                throw new SAXException(
                    "Required Service Elements are missing, check"
                    + " for the existence of Name, Title , or OnlineResource elements.");
            }

            //			try {
            //				BeanUtils.populate(capab, map);
            //			} catch (IllegalAccessException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			} catch (InvocationTargetException e1) {
            //				logger.warning("Unable to set properties on ServiceType object");
            //			}
            return capab;
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
                    WFSComplexTypes.GetCapabilitiesType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("DescribeFeatureType",
                    WFSComplexTypes.DescribeFeatureType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("Transaction",
                    WFSComplexTypes.TransactionType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null),
                new WFSElement("GetFeature",
                    WFSComplexTypes.GetFeatureTypeType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("GetFeatureWithLock",
                    WFSComplexTypes.GetFeatureTypeType.getInstance(), 1, 1,
                    false, null),
                new WFSElement("LockFeature",
                    WFSComplexTypes.LockFeatureTypeType.getInstance(), 1, 1,
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
    static class DescribeFeatureType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DescribeFeatureType();

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

        // static element list
        private static final Element[] elements = {
                new WFSElement("XMLSCHEMA",
                    WFSComplexTypes.EmptyType.getInstance(), 1,
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

        // static element list
        private static final Element[] elements = {
                new WFSElement("GML2", WFSComplexTypes.EmptyType.getInstance(),
                    1, 1, false, null),
                new WFSElement("GML2-GZIP",
                    WFSComplexTypes.EmptyType.getInstance(), 0, 1, false, null)
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
}
