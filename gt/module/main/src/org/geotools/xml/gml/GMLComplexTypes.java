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
package org.geotools.xml.gml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.xml.gml.GMLSchema.AttributeList;
import org.geotools.xml.gml.GMLSchema.GMLAttribute;
import org.geotools.xml.gml.GMLSchema.GMLComplexType;
import org.geotools.xml.gml.GMLSchema.GMLElement;
import org.geotools.xml.gml.GMLSchema.GMLNullType;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.Type;
import org.geotools.xml.xLink.XLinkSchema;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * This class is intended to act as a collection of package visible GML
 * complexType definition to be used by the GMLSchema
 *
 * @author $author$
 * @version $Revision: 1.9 $
 *
 * @see GMLSchema
 * @see CompleType
 */
public class GMLComplexTypes {
    // used for debugging
    private static Logger logger = Logger.getLogger(
            "net.refractions.gml.static");
    
    public static final String STREAM_HINT = "org.geotools.xml.gml.STREAM_HINT";
    private static final String STREAM_FEATURE_NAME_HINT = "org.geotools.xml.gml.STREAM_FEATURE_NAME_HINT";

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
     * This class represents an AbstractGeometryType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an AbstractGeometryType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class AbstractGeometryType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new AbstractGeometryType();

        // static list of attributes
        private static Attribute[] attributes = {
                new GMLSchema.GMLAttribute("gid",
                    XSISimpleTypes.ID.getInstance()),
                new GMLSchema.GMLAttribute("srsName",
                    XSISimpleTypes.AnyURI.getInstance())
            };

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return true;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return new Attribute[0];
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return new DefaultSequence(new Element[0]);
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "AbstractGeometryType";
        }

        /**
         * @see org.geotools.xml.xsi.Type#getValue(org.geotools.xml.xsi.Element,
         *      org.geotools.xml.xsi.ElementValue[], org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            try {
                Geometry g = (Geometry) value[0].getValue();

                //TODO have someone that knows more help.
                String srsName = attrs.getValue("srsName");

                if ((srsName != null) && !"".equals(srsName)) {
                    //TODO support real coord systems here
                    //                    if(srsName.matches(".*epsg#\\d*")){
                    //                        String[] t = srsName.split(".*epsg#");
                    //                        if(t!=null && t.length==1){
                    //                            String epsg = t[0];
                    //                            CoordinateSystem cs = (new CoordinateSystemEPSGFactory()).createCoordinateSystem(epsg);
                    //                            g.setUserData(cs);
                    //                        }
                    //                    }
                    g.setUserData(srsName);
                }

                return g;
            } catch (ClassCastException e) {
                // there was an error, this is an abstract type
                throw new SAXException(
                    "Expected a Geometry to be passed to this abstract type");

                //            } catch (FactoryException e) {
                //                throw new SAXException(e);
            }
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Geometry.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }
    }

    /**
     * <p>
     * This class represents an AbstractGeometryCollectionBaseType within the
     * GML Schema.  This includes both the data and parsing functionality
     * associated  with an AbstractGeometryCollectionBaseType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class AbstractGeometryCollectionBaseType extends GMLComplexType {
        private static final Attribute[] attributes = {
                new GMLSchema.GMLAttribute("gid",
                    XSISimpleTypes.ID.getInstance()),
                new GMLSchema.GMLAttribute("srsName",
                    XSISimpleTypes.AnyURI.getInstance(), Attribute.REQUIRED)
            };

        // singleton instance
        private static final GMLComplexType instance = new AbstractGeometryCollectionBaseType();

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return true;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return new Attribute[0];
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return new DefaultSequence(new Element[0]);
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "AbstractGeometryCollectionBaseType";
        }

        /**
         * @see org.geotools.xml.xsi.Type#getValue(org.geotools.xml.xsi.Element,
         *      org.geotools.xml.xsi.ElementValue[], org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            try {
                Geometry g = (Geometry) value[0].getValue();

                //TODO have someone that knows more help.
                String srsName = attrs.getValue("srsName");

                if ((srsName != null) && !"".equals(srsName)) {
                    //TODO support real coord systems here
                    //                    if(srsName.matches(".*epsg#\\d*")){
                    //                        String[] t = srsName.split(".*epsg#");
                    //                        if(t!=null && t.length==1){
                    //                            String epsg = t[0];
                    //                            CoordinateSystem cs = (new CoordinateSystemEPSGFactory()).createCoordinateSystem(epsg);
                    //                            g.setUserData(cs);
                    //                        }
                    //                    }
                    g.setUserData(srsName);
                }

                return g;
            } catch (ClassCastException e) {
                // there was an error, this is an abstract type
                throw new SAXException(
                    "Expected a Geometry to be passed to this abstract type");

                //            } catch (FactoryException e) {
                //                throw new SAXException(e);
            }
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Geometry.class;
        }

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }
    }

    /**
     * <p>
     * This class represents an GeometryAssociationType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an GeometryAssociationType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class GeometryAssociationType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new GeometryAssociationType();

        // the static attribute list
        private static final Attribute[] attributes = loadAttributes();

        // the static element list
        private static final Element[] elems = {
                new GMLElement("_Geometry",
                    GMLComplexTypes.AbstractGeometryType.getInstance(), 0, 1,
                    true, null),
            };

        // static child sequence
        private static final DefaultSequence elements = new DefaultSequence(elems);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes which GeometryAssociations include
         */
        private static Attribute[] loadAttributes() {
            Attribute[] gp = XLinkSchema.SimpleLink.getInstance().getAttributes();
            Attribute[] r = new Attribute[gp.length + 1];

            for (int i = 1; i < gp.length; i++)
                r[i] = gp[i];

            r[gp.length] = AttributeList.attributes[0];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return elements;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "GeometryAssociationType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length == 0) || (value[0] == null)) {
                return null; // do nothing ... this is allowed
            }

            if (value.length > 1) {
                throw new SAXException("Cannot have more than one geom per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            return (Geometry) value[0].getValue();
        }

        public Class getInstanceType() {
            return Geometry.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            if (name == null) {
                return null;
            }

            for (int i = 0; i < elems.length; i++)
                if (name.equals(elems[i].getName())) {
                    return elems[i];
                }

            return null;
        }
    }

    /**
     * <p>
     * This class represents an PointMemberType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * PointMemberType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PointMemberType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PointMemberType();

        // static list of attributes
        private static final Attribute[] attributes = loadAttributes();

        // static list of elements
        private static final Element[] elements = {
                new GMLElement("Point",
                    GMLComplexTypes.PointType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence 
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes required for a PointMember
         */
        private static Attribute[] loadAttributes() {
            Attribute[] parent = GeometryAssociationType.attributes;
            Attribute[] gp = GMLSchema.GMLAssociationAttributeGroup.attributes;
            Attribute[] r = new Attribute[parent.length + gp.length];

            for (int i = 0; i < parent.length; i++)
                r[i] = parent[i];

            for (int i = 0; i < gp.length; i++)
                r[i + parent.length] = gp[i];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PointMemberType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length == 0) || (value[0] == null)) {
                return null; // do nothing ... this is allowed
            }

            if (value.length > 1) {
                throw new SAXException("Cannot have more than one geom per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            return (Point) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Point.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an LineStringMemberType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an LineStringMemberType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class LineStringMemberType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new LineStringMemberType();

        // static list of attributes 
        private static final Attribute[] attributes = loadAttributes();

        // static list of elements
        private static final Element[] elements = {
                new GMLElement("LineString",
                    GMLComplexTypes.LineStringType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes required for a LineStringMember
         */
        private static Attribute[] loadAttributes() {
            Attribute[] parent = GeometryAssociationType.attributes;
            Attribute[] gp = GMLSchema.GMLAssociationAttributeGroup.attributes;
            Attribute[] r = new Attribute[parent.length + gp.length];

            for (int i = 0; i < parent.length; i++)
                r[i] = parent[i];

            for (int i = 0; i < gp.length; i++)
                r[i + parent.length] = gp[i];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "LineStringMemberType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length == 0) || (value[0] == null)) {
                return null; // do nothing ... this is allowed
            }

            if (value.length > 1) {
                throw new SAXException("Cannot have more than one geom per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            return (LineString) value[0].getValue();
        }

        public Class getInstanceType() {
            return LineString.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an PolygonMemberType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * PolygonMemberType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PolygonMemberType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PolygonMemberType();

        // static attribute list
        private static final Attribute[] attributes = loadAttributes();

        // static list of elements
        private static final Element[] elements = {
                new GMLElement("Polygon",
                    GMLComplexTypes.PolygonType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes required for a PolygonMember
         */
        private static Attribute[] loadAttributes() {
            Attribute[] parent = GeometryAssociationType.attributes;
            Attribute[] gp = GMLSchema.GMLAssociationAttributeGroup.attributes;
            Attribute[] r = new Attribute[parent.length + gp.length];

            for (int i = 0; i < parent.length; i++)
                r[i] = parent[i];

            for (int i = 0; i < gp.length; i++)
                r[i + parent.length] = gp[i];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PolygonMemberType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length == 0) || (value[0] == null)) {
                return null; // do nothing ... this is allowed
            }

            if (value.length > 1) {
                throw new SAXException("Cannot have more than one geom per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            return (Polygon) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Polygon.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an LinearRingMemberType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an LinearRingMemberType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class LinearRingMemberType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new LinearRingMemberType();

        // static attribute list
        private static final Attribute[] attributes = loadAttributes();

        // static element list
        private static final Element[] elements = {
                new GMLElement("LinearRing",
                    GMLComplexTypes.LinearRingType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes required for a LinearRingMember
         */
        private static Attribute[] loadAttributes() {
            Attribute[] parent = GeometryAssociationType.attributes;
            Attribute[] gp = GMLSchema.GMLAssociationAttributeGroup.attributes;
            Attribute[] r = new Attribute[parent.length + gp.length];

            for (int i = 0; i < parent.length; i++)
                r[i] = parent[i];

            for (int i = 0; i < gp.length; i++)
                r[i + parent.length] = gp[i];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "LinearRingMemberType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length == 0) || (value[0] == null)) {
                return null; // do nothing ... this is allowed
            }

            if (value.length > 1) {
                throw new SAXException("Cannot have more than one geom per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            return (LinearRing) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LinearRing.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an PointType within the GML Schema.  This includes
     * both the data and parsing functionality associated  with an PointType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PointType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PointType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("coord",
                    GMLComplexTypes.CoordType.getInstance(), 1, 1, false, null),
                new GMLElement("coordinates",
                    GMLComplexTypes.CoordinatesType.getInstance(), 1, 1, false,
                    null)
            };

        // static choice
        private static final DefaultChoice seq = new DefaultChoice(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PointType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if (value.length > 1) {
                throw new SAXException("Cannot have more than one coord per "
                    + getName());
            }

            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Object t = value[0].getValue();
            Point p = null;

            if (t == null) {
                throw new SAXException("Invalid coordinate specified");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            if (t instanceof Coordinate) {
                Coordinate c = (Coordinate) t;
                p = gf.createPoint(c);
            } else {
                CoordinateSequence c = (CoordinateSequence) t;
                p = gf.createPoint(c);
            }

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p);

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs,hints);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Point.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an LineStringType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * LineStringType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class LineStringType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new LineStringType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("coord",
                    GMLComplexTypes.CoordType.getInstance(), 2,
                    Integer.MAX_VALUE, false, null),
                new GMLElement("coordinates",
                    GMLComplexTypes.CoordinatesType.getInstance(), 1, 1, false,
                    null)
            };

        // static choice
        private static final DefaultChoice seq = new DefaultChoice(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "LineStringType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Object t = value[0].getValue();
            LineString p = null;

            if (t == null) {
                throw new SAXException("Invalid coordinate specified");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            if (t instanceof Coordinate) {
                if (value.length < 2) {
                    throw new SAXException(
                        "Cannot have more than one coord per " + getName());
                }

                // there should be more
                Coordinate[] c = new Coordinate[value.length];

                for (int i = 0; i < c.length; i++)
                    c[i] = (Coordinate) value[i].getValue();

                p = gf.createLineString(c);
            } else {
                if (value.length > 1) {
                    throw new SAXException(
                        "Cannot have more than one coordinate sequence per "
                        + getName());
                }

                CoordinateSequence c = (CoordinateSequence) t;
                p = gf.createLineString(c);
            }

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p);

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs,hints);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LineString.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an LinearRingType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * LinearRingType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class LinearRingType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new LinearRingType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("coord",
                    GMLComplexTypes.CoordType.getInstance(), 2, 4, false, null),
                new GMLElement("coordinates",
                    GMLComplexTypes.CoordinatesType.getInstance(), 1, 1, false,
                    null)
            };

        // static sequence
        private static final DefaultChoice seq = new DefaultChoice(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "LinearRingType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Object t = value[0].getValue();
            LinearRing p = null;

            if (t == null) {
                throw new SAXException("Invalid coordinate specified");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            if (t instanceof Coordinate) {
                if (value.length < 4) {
                    throw new SAXException(
                        "Cannot have more than one coord per " + getName());
                }

                // there should be more
                Coordinate[] c = new Coordinate[value.length];

                for (int i = 0; i < c.length; i++)
                    c[i] = (Coordinate) value[i].getValue();

                p = gf.createLinearRing(c);
            } else {
                if (value.length > 1) {
                    throw new SAXException(
                        "Cannot have more than one coordinate sequence per "
                        + getName());
                }

                CoordinateSequence c = (CoordinateSequence) t;
                p = gf.createLinearRing(c);
            }

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p);

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs,hints);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LinearRing.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an BoxType within the GML Schema.  This includes
     * both the data and parsing functionality associated  with an BoxType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class BoxType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new BoxType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("coord",
                    GMLComplexTypes.CoordType.getInstance(), 2, 2, false, null),
                new GMLElement("coordinates",
                    GMLComplexTypes.CoordinatesType.getInstance(), 1, 1, false,
                    null)
            };

        // static sequence
        private static final DefaultChoice seq = new DefaultChoice(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "BoxType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Object t = value[0].getValue();
            LineString p = null;

            if (t == null) {
                throw new SAXException("Invalid coordinate specified");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            try {
                if (t instanceof Coordinate) {
                    if (value.length != 2) {
                        throw new SAXException(
                            "Cannot have more than one coord per " + getName());
                    }

                    // there should be more
                    Coordinate[] c = new Coordinate[value.length];

                    for (int i = 0; i < c.length; i++)
                        c[i] = (Coordinate) value[i].getValue();

                    p = gf.createLineString(c);
                } else {
                    if (value.length > 1) {
                        throw new SAXException(
                            "Cannot have more than one coordinate sequence per "
                            + getName());
                    }

                    CoordinateSequence c = (CoordinateSequence) t;
                    p = gf.createLineString(c);
                }
            } catch (ClassCastException cce) {
                logger.warning(cce.toString());
                logger.warning(t + ((t == null) ? "" : t.getClass().getName()));
                throw cce;
            }

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p.getEnvelope());

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs,hints);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Geometry.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an PolygonType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * PolygonType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PolygonType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PolygonType();

        // static list of elements
        private static final Element[] elements = {
                new GMLElement("outerBoundaryIs",
                    GMLComplexTypes.LinearRingMemberType.getInstance(), 1, 1,
                    false, null),
                new GMLElement("innerBoundaryIs",
                    GMLComplexTypes.LinearRingMemberType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null)
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PolygonType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            LinearRing outerLR = null;
            LinearRing[] innerLR = new LinearRing[(value.length > 1)
                ? (value.length - 1) : 0];
            int innerIndex = 0;

            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equalsIgnoreCase(value[i].getElement()
                                                                       .getName())) {
                    outerLR = (LinearRing) value[i].getValue();
                } else {
                    innerLR[innerIndex++] = (LinearRing) value[i].getValue();
                }
            }

            Polygon p = gf.createPolygon(outerLR, innerLR);

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p);

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs,hints);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Polygon.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an GeometryCollectionType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an GeometryCollectionType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class GeometryCollectionType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new GeometryCollectionType();

        // static lsit of elements
        private static final Element[] elements = {
                new GMLElement("geometryMember",
                    GMLComplexTypes.GeometryAssociationType.getInstance(), 1,
                    Integer.MAX_VALUE, false, null),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "GeometryCollectionType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            Geometry[] geoms = new Geometry[value.length];

            for (int i = 0; i < value.length; i++) {
                geoms[i] = (Geometry) value[i].getValue();
            }

            return gf.createGeometryCollection(geoms);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryCollection.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiPointType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * MultiPointType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiPointType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiPointType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("pointMember",
                    GMLComplexTypes.PointMemberType.getInstance(), 1,
                    Integer.MAX_VALUE, false,
                    new GMLElement("geometryMember",
                        GMLComplexTypes.GeometryAssociationType.getInstance(),
                        1, 1, false, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiPointType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            Point[] geoms = new Point[value.length];

            for (int i = 0; i < value.length; i++) {
                geoms[i] = (Point) value[i].getValue();
            }

            return gf.createMultiPoint(geoms);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiPoint.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiLineStringType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an MultiLineStringType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiLineStringType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiLineStringType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("lineStringMember",
                    GMLComplexTypes.LineStringMemberType.getInstance(), 1,
                    Integer.MAX_VALUE, false,
                    new GMLElement("geometryMember",
                        GMLComplexTypes.GeometryAssociationType.getInstance(),
                        1, 1, false, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiLineStringType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            LineString[] geoms = new LineString[value.length];

            for (int i = 0; i < value.length; i++) {
                geoms[i] = (LineString) value[i].getValue();
            }

            return gf.createMultiLineString(geoms);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiLineString.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiPolygonType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * MultiPolygonType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiPolygonType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiPolygonType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("polygonMember",
                    GMLComplexTypes.PolygonMemberType.getInstance(), 1,
                    Integer.MAX_VALUE, false,
                    new GMLElement("geometryMember",
                        GMLComplexTypes.GeometryAssociationType.getInstance(),
                        1, 1, false, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiPolygonType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            GeometryFactory gf = new GeometryFactory(DefaultCoordinateSequenceFactory
                    .instance());

            Polygon[] geoms = new Polygon[value.length];

            for (int i = 0; i < value.length; i++) {
                geoms[i] = (Polygon) value[i].getValue();
            }

            return gf.createMultiPolygon(geoms);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiPolygon.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an CoordType within the GML Schema.  This includes
     * both the data and parsing functionality associated  with an CoordType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class CoordType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new CoordType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("X", XSISimpleTypes.Decimal.getInstance(), 1, 1,
                    false, null),
                new GMLElement("Y", XSISimpleTypes.Decimal.getInstance(), 0, 1,
                    false, null),
                new GMLElement("Z", XSISimpleTypes.Decimal.getInstance(), 0, 1,
                    false, null)
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "CoordType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Element e = value[0].getElement();

            if (e == null) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            Double x;
            Double y;
            Double z;
            x = y = z = null;

            for (int i = 0; i < value.length; i++) {
                if (elements[0].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    x = (Double) value[i].getValue();
                }

                if (elements[1].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    y = (Double) value[i].getValue();
                }

                if (elements[2].getName().equals(value[i].getElement().getType()
                                                             .getName())) {
                    z = (Double) value[i].getValue();
                }
            }

            if (x == null) {
                throw new SAXException("An X coord is required");
            }

            if (y == null) {
                return new Coordinate(x.doubleValue(), 0);
            }

            if (z == null) {
                return new Coordinate(x.doubleValue(), y.doubleValue());
            }

            return new Coordinate(x.doubleValue(), y.doubleValue(),
                z.doubleValue());
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Coordinate.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an CoordinatesType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * CoordinatesType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class CoordinatesType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new CoordinatesType();

        // static attribute list
        private static final Attribute[] attributes = {
                new GMLAttribute("decimal",
                    XSISimpleTypes.String.getInstance(), Attribute.OPTIONAL, "."),
                new GMLAttribute("cs", XSISimpleTypes.String.getInstance(),
                    Attribute.OPTIONAL, ","),
                new GMLAttribute("ts", XSISimpleTypes.String.getInstance(),
                    Attribute.OPTIONAL, "\t")
            };

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractGeometryCollectionBaseType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return new DefaultSequence(new Element[0]);
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "CoordinatesType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if (value.length != 1) {
                throw new SAXException(
                    "Internal error, ElementValues require an associated Element.");
            }

            String dec;
            String cs;
            String ts;
            dec = attrs.getValue("", "decimal");

            if (dec == null) {
                dec = attrs.getValue(GMLSchema.NAMESPACE, "decimal");
            }

            dec = ((dec == null) || (dec == "")) ? "." : dec;

            cs = attrs.getValue("", "cs");

            if (cs == null) {
                cs = attrs.getValue(GMLSchema.NAMESPACE, "cs");
            }

            cs = ((cs == null) || (cs == "")) ? ",\\s*" : cs+"\\s*";
            ts = attrs.getValue("", "ts");

            if (ts == null) {
                ts = attrs.getValue(GMLSchema.NAMESPACE, "ts");
            }

            ts = ((ts == null) || (ts == "") || ts.matches("\\s")) ? "\\s+"
                                                                   : (ts
                + "\\s*"); // handle whitespace

            String val = (String) value[0].getValue();
//System.out.println("**"+val+"**");
//System.out.println("TOUPLE SPLITER = ^^^"+ts+"^^^");
            String[] touples = val.split(ts);
            Coordinate[] coordinates = new Coordinate[touples.length];

            for (int i = 0; i < touples.length; i++) {
                String[] points = touples[i].split(cs);
                double[] pts = new double[points.length];

                for (int j = 0; j < points.length; j++) {
                    String t = "";

                    try {
                        if (!dec.equals(".")) {
                            dec = dec.replaceAll("\\", "\\");
                            t = points[j].replaceAll(dec, ".");
                        } else {
                            t = points[j];
                        }

                        pts[j] = Double.parseDouble(t);
                    } catch (NumberFormatException e) {
                        logger.warning(e.toString());
                        logger.warning("Double = '" + t + "' " + j
                            + "/" + points.length+"  Touples = "+i+"/"+touples.length);
                        throw e;
                    }
                }

                if (pts.length == 1) {
                    coordinates[i] = new Coordinate(pts[0], 0);
                } else {
                    if (pts.length == 2) {
                        coordinates[i] = new Coordinate(pts[0], pts[1]);
                    } else {
                        // should be three or there was an error
                        coordinates[i] = new Coordinate(pts[0], pts[1], pts[2]);
                    }
                }
            }

            return DefaultCoordinateSequenceFactory.instance().create(coordinates);
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return CoordinateSequence.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }

        /**
         * @see schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            return true;
        }
    }

    /**
     * <p>
     * This class represents an AbstractFeatureType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an AbstractFeatureType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class AbstractFeatureType extends GMLComplexType {
        // static attribute list
        private static final Attribute[] attributes = {
                new GMLAttribute("fid", XSISimpleTypes.ID.getInstance(),
                    Attribute.OPTIONAL),
            };

        // static element list
        private static final Element[] elements = {
                new GMLElement("description",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null),
                new GMLElement("name", XSISimpleTypes.String.getInstance(), 0,
                    1, false, null),
                new GMLElement("boundedBy",
                    GMLComplexTypes.BoundingShapeType.getInstance(), 0, 1,
                    false, null),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        // singleton instance
        private static final GMLComplexType instance = new AbstractFeatureType();

        // used for mapping ftName + namespace to actual FTs
        private static final HashMap featureTypeMappings = new HashMap();

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return true;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "AbstractFeatureType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            Feature f = getFeature(element,value,attrs,hints);
            if(hints==null || hints.get(STREAM_HINT)==null)
                return f;
			if(hints.get(STREAM_FEATURE_NAME_HINT)==null)
			    hints.put(STREAM_FEATURE_NAME_HINT,element.getName());	
			String nm = (String)hints.get(STREAM_FEATURE_NAME_HINT);
            if(nm!=null && nm.equals(element.getName())){
                stream(f,(FCBuffer)hints.get(STREAM_HINT));
                return null;
            }else{
                return f;
            }
        	}
        
       public Feature getFeature(Element element, ElementValue[] value,
                    Attributes attrs, Map hints) throws SAXException {
            FeatureType ft = (FeatureType) featureTypeMappings.get(element.getType()
                                                                          .getNamespace()
                    + "#" + element.getName());

            if (ft == null) {
                ft = loadFeatureType(element, value, attrs);
            }

            Object[] values = new Object[value.length];

            for (int i = 0; i < value.length; i++)
                values[i] = value[i].getValue();

            String fid = attrs.getValue("", "fid");

            if ((fid == null) || "".equals(fid)) {
                fid = attrs.getValue(GMLSchema.NAMESPACE, "fid");
            }

            if ((fid != null) || !"".equals(fid)) {
                try {
                    return ft.create(values, fid);
                } catch (IllegalAttributeException e) {
                    logger.warning(e.toString());
                    throw new SAXException(e);
                }
            }

            try {
                return ft.create(values);
            } catch (IllegalAttributeException e1) {
                logger.warning(e1.toString());
                throw new SAXException(e1);
            }
        }

        
        private void stream(Feature feature, FCBuffer 
                featureCollectionBuffer) throws SAXNotSupportedException, SAXException{
            if(!featureCollectionBuffer.addFeature(feature))
                throw new SAXException("Buffer overflow");
            if(featureCollectionBuffer.state<=0){
                switch(featureCollectionBuffer.state){
                	case FCBuffer.STOP:
                	    throw new StopException();
                	case FCBuffer.FINISH:
                	    return;
                	default:
                	    featureCollectionBuffer.state = 
                	        (featureCollectionBuffer.getCapacity() - 
                	        featureCollectionBuffer.getSize())/3;
            		logger.finest("New State "+featureCollectionBuffer.state+" "+featureCollectionBuffer.getSize());
                		while(featureCollectionBuffer.getSize()>featureCollectionBuffer.getCapacity()-1){
                		    logger.finest("waiting for reader");
                		    Thread.yield();
                		}
                }
            }else{
                featureCollectionBuffer.state--;
                logger.finest("New State "+featureCollectionBuffer.state+" "+featureCollectionBuffer.getSize());
            }
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Feature.class;
        }

        /*
         * Creates a FT from the information provided
         */
        private FeatureType loadFeatureType(Element element,
            ElementValue[] value, Attributes attrs) throws SAXException {
            String ftName = element.getName();
            String ftNS = element.getType().getNamespace();
            logger.finest("Creating feature type for " + ftName + ":" + ftNS);

            FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(ftName);
            typeFactory.setNamespace(ftNS);
            typeFactory.setName(ftName);

            GeometryAttributeType geometryAttribute = null;

            for (int i = 0; i < value.length; i++) {
                String name = value[i].getElement().getName();
                Class type = value[i].getElement().getType().getInstanceType(); //value[i].getValue().getClass();
                boolean nillable = value[i].getElement().isNillable();

                AttributeType attributeType = AttributeTypeFactory
                    .newAttributeType(name, type, nillable);
                typeFactory.addType(attributeType);

                if ((geometryAttribute == null)
                        && attributeType instanceof GeometryAttributeType) {
                    if (!value[i].getElement().getType().getName()
                                     .equalsIgnoreCase(BoxType.getInstance()
                                                                  .getName())) {
                        geometryAttribute = (GeometryAttributeType) attributeType;
                    }
                }
            }

            if (geometryAttribute != null) {
                typeFactory.setDefaultGeometry(geometryAttribute);
            }

            try {
                FeatureType ft = typeFactory.getFeatureType();
                featureTypeMappings.put(element.getType().getNamespace() + "#"
                    + element.getName(), ft);

                return ft;
            } catch (SchemaException e) {
                logger.warning(e.toString());
                throw new SAXException(e);
            }
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an AbstractFeatureCollectionsBaseType within the
     * GML Schema.  This includes both the data and parsing functionality
     * associated  with an AbstractFeatureCollectionsBaseType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class AbstractFeatureCollectionsBaseType extends AbstractFeatureType {
        // static element list
        private static final Element[] elements = {
                new GMLElement("description",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null),
                new GMLElement("name", XSISimpleTypes.String.getInstance(), 0,
                    1, false, null),
                new GMLElement("boundedBy",
                    GMLComplexTypes.BoundingShapeType.getInstance(), 1, 1,
                    false, null),
            };

        //static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        // singleton instance
        private static final GMLComplexType instance = new AbstractFeatureCollectionsBaseType();

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return true;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return AbstractFeatureType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "AbstractFeatureCollectionBaseType";
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an AbstractFeatureCollectionType within the GML
     * Schema.  This includes both the data and parsing functionality
     * associated  with an AbstractFeatureCollectionType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class AbstractFeatureCollectionType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new AbstractFeatureCollectionType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("description",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null),
                new GMLElement("name", XSISimpleTypes.String.getInstance(), 0,
                    1, false, null),
                new GMLElement("boundedBy",
                    GMLComplexTypes.BoundingShapeType.getInstance(), 1, 1,
                    false, null),
                new GMLElement("featureMember",
                    GMLComplexTypes.FeatureAssociationType.getInstance(), 0,
                    Integer.MAX_VALUE, false, null),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return true;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if(hints==null || hints.get(STREAM_HINT)==null)
                return getCollection(value);
            FCBuffer fcb = (FCBuffer)hints.get(STREAM_HINT);
            fcb.state = FCBuffer.FINISH;
            return null;
        }
        
        /**
         * 
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints){
            if(hints==null || hints.get(STREAM_HINT)==null)
                return true;
            Type e = element.getType();
            while(e!=null){
                if(e.getName()!=null && e.getName().equals(BoxType.getInstance().getName()))
                    return true;
                e = e.getParent();
            }
            return false;
        }
        
        private FeatureCollection getCollection(ElementValue[] value){
            FeatureCollection fc = FeatureCollections.newCollection();
            for(int i=1;i<value.length;i++) // bbox is slot 0
                fc.add(value[i].getValue());
            return fc;
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return FeatureCollection.class;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "AbstractFeatureCollectionType";
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an GeometryPropertyType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an GeometryPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class GeometryPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new GeometryPropertyType();

        // static attribute list
        private static final Attribute[] attributes = loadAttributes();

        // static element list
        private static final Element[] elements = {
                new GMLElement("_Geometry",
                    GMLComplexTypes.AbstractGeometryType.getInstance(), 0, 1,
                    true, null),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes for a Geom Prop Type
         */
        private static Attribute[] loadAttributes() {
            Attribute[] gp = XLinkSchema.SimpleLink.getInstance().getAttributes();
            Attribute[] r = new Attribute[gp.length + 1];

            for (int i = 1; i < gp.length; i++)
                r[i] = gp[i];

            r[gp.length] = GMLSchema.AttributeList.attributes[0];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "GeometryPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (Geometry) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Geometry.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an FeatureAssociationType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an FeatureAssociationType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class FeatureAssociationType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new FeatureAssociationType();

        // static attribute list
        private static final Attribute[] attributes = loadAttributes();

        // static element list
        private static final Element[] elements = {
                new GMLElement("_Feature",
                    GMLComplexTypes.AbstractFeatureType.getInstance(), 0, 1,
                    true, null),
            };

        // static sequence
        static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /*
         * statically loads the attributes for a Feature Association Type
         */
        private static Attribute[] loadAttributes() {
            Attribute[] gp = XLinkSchema.SimpleLink.getInstance().getAttributes();
            Attribute[] r = new Attribute[gp.length + 1];

            for (int i = 1; i < gp.length; i++)
                r[i] = gp[i];

            r[gp.length] = GMLSchema.AttributeList.attributes[0];

            return r;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "FeatureAssociationType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one feature " + value.length);
            }

            logger.finest((value[0].getValue() == null) ? "null"
                                                        : value[0].getValue()
                                                                  .getClass()
                                                                  .getName());

            return (Feature) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Feature.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an BoundingShapeType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * BoundingShapeType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class BoundingShapeType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new BoundingShapeType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("Box", GMLComplexTypes.BoxType.getInstance(), 1,
                    1, false, null),
                new GMLElement("null", new GMLNullType(), 1, 1, false, null),
            };

        // static choice
        private static final DefaultChoice seq = new DefaultChoice(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
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
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "BoundingShapeType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (Geometry) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Geometry.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an PointPropertyType within the GML Schema.  This
     * includes both the data and parsing functionality associated  with an
     * PointPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PointPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PointPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("Point",
                    GMLComplexTypes.PointType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PointPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (Point) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Point.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an PolygonPropertyType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an PolygonPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class PolygonPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new PolygonPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("Polygon",
                    GMLComplexTypes.PolygonType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "PolygonPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (Polygon) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Polygon.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an LineStringPropertyType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an LineStringPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class LineStringPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new LineStringPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("LineString",
                    GMLComplexTypes.LineStringType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "LineStringPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (LineString) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LineString.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiPointPropertyType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an MultiPointPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiPointPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiPointPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("MultiPoint",
                    GMLComplexTypes.MultiPointType.getInstance(), 0, 1, false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiPointPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (MultiPoint) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiPoint.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiLineStringPropertyType within the GML
     * Schema.  This includes both the data and parsing functionality
     * associated  with an MultiLineStringPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiLineStringPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiLineStringPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("MultiLineString",
                    GMLComplexTypes.MultiLineStringType.getInstance(), 0, 1,
                    false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiLineStringPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (MultiLineString) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiLineString.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiPolygonPropertyType within the GML Schema.
     * This includes both the data and parsing functionality associated  with
     * an MultiPolygonPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiPolygonPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiPolygonPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("MultiPolygon",
                    GMLComplexTypes.MultiPolygonType.getInstance(), 0, 1,
                    false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiPolygonPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (MultiPolygon) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return MultiPolygon.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }

    /**
     * <p>
     * This class represents an MultiGeometryPropertyType within the GML
     * Schema.  This includes both the data and parsing functionality
     * associated  with an MultiGeometryPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    static class MultiGeometryPropertyType extends GMLComplexType {
        // singleton instance
        private static final GMLComplexType instance = new MultiGeometryPropertyType();

        // static element list
        private static final Element[] elements = {
                new GMLElement("MultiGeometry",
                    GMLComplexTypes.GeometryCollectionType.getInstance(), 0, 1,
                    false,
                    new GMLElement("_Geometry",
                        GMLComplexTypes.AbstractGeometryType.getInstance(), 1,
                        1, true, null)),
            };

        // static sequence
        private static final DefaultSequence seq = new DefaultSequence(elements);

        /*
         * part of the singleton pattern
         *
         * @see GMLComplexType#getInstance()
         */
        static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }

        /**
         * @see schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return FeatureAssociationType.attributes;
        }

        /**
         * @see schema.ComplexType#getChildren()
         */
        public ElementGrouping getChild() {
            return seq;
        }

        /**
         * @see schema.ComplexType#getName()
         */
        public String getName() {
            return "MultiGeometryPropertyType";
        }

        /**
         * @see schema.Type#getValue(java.util.List)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException("must be one geometry");
            }

            return (GeometryCollection) value[0].getValue();
        }

        /**
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryCollection.class;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
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
    }
}
