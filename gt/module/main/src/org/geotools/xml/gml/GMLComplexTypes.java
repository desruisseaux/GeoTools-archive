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
import com.vividsolutions.jts.geom.Envelope;
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
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.xml.PrintHandler;
import org.geotools.xml.gml.FCBuffer.StopException;
import org.geotools.xml.gml.GMLSchema.AttributeList;
import org.geotools.xml.gml.GMLSchema.GMLAttribute;
import org.geotools.xml.gml.GMLSchema.GMLComplexType;
import org.geotools.xml.gml.GMLSchema.GMLElement;
import org.geotools.xml.gml.GMLSchema.GMLNullType;
import org.geotools.xml.schema.All;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.Type;
import org.geotools.xml.xLink.XLinkSchema;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;


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
    protected static Logger logger = getLogger();
    private static final Logger getLogger(){
    	Logger l = Logger.getLogger("net.refractions.gml.static");
    	l.setLevel(Level.WARNING);
    	return l;
    }
    	

    /** DOCUMENT ME!  */
    public static final String STREAM_HINT = "org.geotools.xml.gml.STREAM_HINT";
    private static final String STREAM_FEATURE_NAME_HINT = "org.geotools.xml.gml.STREAM_FEATURE_NAME_HINT";

    static void encode(Element e, Geometry g, PrintHandler output)
        throws OperationNotSupportedException, IOException {
        if (g instanceof Point) {
            encode(e, (Point) g, output);

            return;
        }

        if (g instanceof Polygon) {
            encode(e, (Polygon) g, output);

            return;
        }

        if (g instanceof LinearRing) {
            encode(e, (LinearRing) g, output);

            return;
        }

        if (g instanceof LineString) {
            encode(e, (LineString) g, output);

            return;
        }

        if (g instanceof MultiLineString) {
            encode(e, (MultiLineString) g, output);

            return;
        }

        if (g instanceof MultiPolygon) {
            encode(e, (MultiPolygon) g, output);

            return;
        }

        if (g instanceof MultiPoint) {
            encode(e, (MultiPoint) g, output);

            return;
        }

        if (g instanceof GeometryCollection) {
            encode(e, (GeometryCollection) g, output);

            return;
        }
    }

    static void encode(Element e, Point g, PrintHandler output)
        throws IOException {
        if ((g == null) || (g.getCoordinate() == null)) {
        	throw new IOException("Bad Point Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "Point", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        encodeCoords(null, g.getCoordinates(), output);

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "Point");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, LineString g, PrintHandler output)
        throws IOException {
        if ((g == null) || (g.getNumPoints() == 0)) {
        	throw new IOException("Bad LineString Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "LineString", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        encodeCoords(null, g.getCoordinates(), output);

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "LineString");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, LinearRing g, PrintHandler output)
        throws IOException {
        if ((g == null) || (g.getNumPoints() == 0)) {
        	throw new IOException("Bad LinearRing Data");
        }

        if (e == null) {
            encode((new GMLSchema()).getElements()[39], (LineString) g, output);
        } else {
            encode(e, (LineString) g, output);
        }
    }

    static void encode(Element e, Polygon g, PrintHandler output)
        throws OperationNotSupportedException, IOException {
        if ((g == null) || (g.getNumPoints() == 0)) {
        	throw new IOException("Bad Polygon Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "Polygon", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        ((new GMLSchema()).getElements()[35]).getType().encode((new GMLSchema()).getElements()[35],g.getExteriorRing(),output,null);

        if (g.getNumInteriorRing() > 0) {
            for (int i = 0; i < g.getNumInteriorRing(); i++)
            	((new GMLSchema()).getElements()[36]).getType().encode((new GMLSchema()).getElements()[36],g.getInteriorRingN(i),output,null);
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "Polygon");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, MultiPoint g, PrintHandler output)
        throws IOException {
        if ((g == null) || (g.getNumGeometries() <= 0)) {
        	throw new IOException("Bad MultiPoint Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // 	no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "MultiPoint", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        for (int i = 0; i < g.getNumGeometries(); i++) {
            output.startElement(GMLSchema.NAMESPACE, "pointMember", null);
            encode(null, (Point) g.getGeometryN(i), output);
            output.endElement(GMLSchema.NAMESPACE, "pointMember");
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "MultiPoint");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, MultiLineString g, PrintHandler output)
        throws IOException {

        if ((g == null) || g.getNumGeometries() <= 0) {
        	throw new IOException("Bad MultiLineString Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // 	no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "MultiLineString", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        for (int i = 0; i < g.getNumGeometries(); i++) {
            output.startElement(GMLSchema.NAMESPACE, "lineStringMember", null);
            encode(null, (LineString) g.getGeometryN(i), output);
            output.endElement(GMLSchema.NAMESPACE, "lineStringMember");
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "MultiLineString");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, MultiPolygon g, PrintHandler output)
        throws OperationNotSupportedException, IOException {
        if ((g == null) || (g.getNumGeometries() <= 0)) {
        	throw new IOException("Bad MultiPolygon Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // 	no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "MultiPolygon", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        for (int i = 0; i < g.getNumGeometries(); i++) {
            output.startElement(GMLSchema.NAMESPACE, "polygonMember", null);
            encode(null, (Polygon) g.getGeometryN(i), output);
            output.endElement(GMLSchema.NAMESPACE, "polygonMember");
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "MultiPolygon");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encode(Element e, GeometryCollection g, PrintHandler output)
        throws OperationNotSupportedException, IOException {
        if ((g == null) || (g.getNumGeometries() <= 0)) {
        	throw new IOException("Bad GeometryCollection Data");
        }

        AttributesImpl ai = new AttributesImpl();

        // 	no GID
        if (g.getUserData() != null) {
            // TODO Fix this when parsing is better ... should be a coord reference system
            ai.addAttribute("", "srsName", "", "anyURI",
                g.getUserData().toString());
        } else {
            if (g.getSRID() != 0) {
                // deprecated version
                ai.addAttribute("", "srsName", "", "anyURI", "" + g.getSRID());
            } else {
                ai = null;
            }
        }

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "MultiGeometry", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        for (int i = 0; i < g.getNumGeometries(); i++) {
            output.startElement(GMLSchema.NAMESPACE, "geometryMember", null);
            encode(null, (Polygon) g.getGeometryN(i), output);
            output.endElement(GMLSchema.NAMESPACE, "geometryMember");
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "MultiGeometry");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encodeCoord(Element e, Coordinate coord, PrintHandler output)
        throws IOException {
        if (coord == null) {
            return;
        }

        AttributesImpl ai = new AttributesImpl();
        ai.addAttribute("", "X", "", "decimal", "" + coord.x);
        ai.addAttribute("", "Y", "", "decimal", "" + coord.y);

        if (coord.z != Double.NaN) {
            ai.addAttribute("", "Z", "", "decimal", "" + coord.z);
        }

        if (e == null) {
            output.element(GMLSchema.NAMESPACE, "coord", ai);
        } else {
            output.element(e.getNamespace(), e.getName(), ai);
        }
    }

    static void encodeCoords(Element e, CoordinateSequence coords,
        PrintHandler output) throws IOException {
        if ((coords == null) || (coords.size() == 0)) {
            return;
        }

        encodeCoords(e, coords.toCoordinateArray(), output);
    }

    static void encodeCoords(Element e, Coordinate[] coords, PrintHandler output)
        throws IOException {
        if ((coords == null) || (coords.length == 0)) {
            return;
        }

        AttributesImpl ai = new AttributesImpl();
        String dec;
        String cs;
        String ts;
        dec = ".";
        cs = ",";
        ts = " ";
        ai.addAttribute("", "decimal", "", "string", dec);
        ai.addAttribute("", "cs", "", "string", cs);
        ai.addAttribute("", "ts", "", "string", ts);

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "coordinates", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        Coordinate c = coords[0];

        if (Double.isNaN(c.z)) {
            output.characters(c.x + cs + c.y);
        } else {
            output.characters(c.x + cs + c.y + cs + c.z);
        }

        for (int i = 1; i < coords.length; i++) {
            c = coords[i];

            if (Double.isNaN(c.z)) {
                output.characters(ts + c.x + cs + c.y);
            } else {
                output.characters(ts + c.x + cs + c.y + cs + c.z);
            }
        }

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "coordinates");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

    static void encodeCoords(Element e, Envelope env, PrintHandler output)
        throws IOException {
        if ((env == null)) {
            return;
        }

        AttributesImpl ai = new AttributesImpl();
        String dec;
        String cs;
        String ts;
        dec = ".";
        cs = ",";
        ts = " ";
        ai.addAttribute("", "decimal", "", "string", dec);
        ai.addAttribute("", "cs", "", "string", cs);
        ai.addAttribute("", "ts", "", "string", ts);

        if (e == null) {
            output.startElement(GMLSchema.NAMESPACE, "coordinates", ai);
        } else {
            output.startElement(e.getNamespace(), e.getName(), ai);
        }

        output.characters(env.getMinX() + cs + env.getMinY() + ts + env.getMaxX() + cs + env.getMaxY());

        if (e == null) {
            output.endElement(GMLSchema.NAMESPACE, "coordinates");
        } else {
            output.endElement(e.getNamespace(), e.getName());
        }
    }

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
    public static class AbstractGeometryType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof Geometry;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
                throw new OperationNotSupportedException();
            Geometry g = (Geometry)value;
            GMLComplexTypes.encode(null, g, output);
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
    public static class AbstractGeometryCollectionBaseType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        
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
        public static GMLComplexType getInstance() {
            return instance;
        }

        /**
         * @see schema.ComplexType#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            throw new OperationNotSupportedException();
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
    public static class GeometryAssociationType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Geometry));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Geometry g = (Geometry) value;

            output.startElement(element.getNamespace(), element.getName(), null);
            GMLComplexTypes.encode(element, g, output);
            output.endElement(element.getNamespace(), element.getName());
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
    public static class PointMemberType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Geometry));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Point g = (Point) value;

            output.startElement(element.getNamespace(), element.getName(), null);
            GMLComplexTypes.encode(null, g, output);
            output.endElement(element.getNamespace(), element.getName());
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
    public static class LineStringMemberType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof LineString));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            LineString g = (LineString) value;

            output.startElement(element.getNamespace(), element.getName(), null);
            GMLComplexTypes.encode(null, g, output);
            output.endElement(element.getNamespace(), element.getName());
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
    public static class PolygonMemberType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Polygon));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Polygon g = (Polygon) value;

            output.startElement(element.getNamespace(), element.getName(), null);
            GMLComplexTypes.encode(null, g, output);
            output.endElement(element.getNamespace(), element.getName());
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
    public static class LinearRingMemberType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof LinearRing));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            LinearRing g = (LinearRing) value;

            output.startElement(element.getNamespace(), element.getName(), null);
            GMLComplexTypes.encode(null, g, output);
            output.endElement(element.getNamespace(), element.getName());
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
    public static class PointType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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
                attrs, hints);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Point));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Point g = (Point) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class LineStringType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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
                // TODO -- be forgiving
                if(c.size() == 1){
                    c = DefaultCoordinateSequenceFactory.instance().create(new Coordinate[]{c.getCoordinate(0),c.getCoordinate(0)});
                }
                p = gf.createLineString(c);
            }

            ElementValue[] ev = new ElementValue[1];
            ev[0] = new DefaultElementValue(element, p);

            return AbstractGeometryType.getInstance().getValue(element, ev,
                attrs, hints);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof LineString));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            LineString g = (LineString) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class LinearRingType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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
                attrs, hints);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof LinearRing));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            LinearRing g = (LinearRing) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class BoxType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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
                attrs, hints);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Geometry));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Geometry g = (Geometry) value;

            AttributesImpl ai = new AttributesImpl();

            // no GID
            if (g.getUserData() != null) {
                // TODO Fix this when parsing is better ... should be a coord reference system
                ai.addAttribute("", "srsName", "", "anyURI",
                    g.getUserData().toString());
            } else {
                if (g.getSRID() != 0) {
                    // deprecated version
                    ai.addAttribute("", "srsName", "", "anyURI",
                        "" + g.getSRID());
                } else {
                    ai = null;
                }
            }

            if ((g == null) || (g.getNumPoints() == 0)
                    || (g.getCoordinates().length == 0)) {
                return;
            }

            output.startElement(GMLSchema.NAMESPACE, element.getName(), ai);

//            Coordinate[] coords = g.getCoordinates();
            Envelope e = g.getEnvelopeInternal();
            encodeCoords(element, e, output);
            output.endElement(GMLSchema.NAMESPACE, element.getName());
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
    public static class PolygonType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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
                attrs, hints);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Polygon));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Polygon g = (Polygon) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class GeometryCollectionType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof GeometryCollection));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            GeometryCollection g = (GeometryCollection) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class MultiPointType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof MultiPoint));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            MultiPoint g = (MultiPoint) value;

            GMLComplexTypes.encode(element, g, output);
        }
    }

    /**
     * <p>
     * This class represents an MultiLineStringType within the GML Schema. This
     * includes both the data and parsing functionality associated  with an
     * MultiLineStringType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    public static class MultiLineStringType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof MultiLineString));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            MultiLineString g = (MultiLineString) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class MultiPolygonType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof MultiPolygon));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            MultiPolygon g = (MultiPolygon) value;

            GMLComplexTypes.encode(element, g, output);
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
    public static class CoordType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            	if(!element.isNillable())
            		throw new SAXException(
                    	"Internal error, ElementValues require an associated Element.");
            	return null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Coordinate));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            Coordinate g = (Coordinate) value;

            GMLComplexTypes.encodeCoord(element, g, output);
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
    public static class CoordinatesType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        
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
        public static GMLComplexType getInstance() {
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
//            return AbstractGeometryCollectionBaseType.attributes;
        	return attributes;
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
                dec = attrs.getValue(GMLSchema.NAMESPACE.toString(), "decimal");
            }

            dec = ((dec == null) || (dec == "")) ? "." : dec;

            cs = attrs.getValue("", "cs");

            if (cs == null) {
                cs = attrs.getValue(GMLSchema.NAMESPACE.toString(), "cs");
            }

            cs = ((cs == null) || (cs == "")) ? ",\\s*" : (cs + "\\s*");
            ts = attrs.getValue("", "ts");

            if (ts == null) {
                ts = attrs.getValue(GMLSchema.NAMESPACE.toString(), "ts");
            }

            ts = ((ts == null) || (ts == "") || ts.matches("\\s")) ? "\\s+"
                                                                   : (ts
                + "\\s*"); // handle whitespace

            String val = (String) value[0].getValue();

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
                        logger.warning("Double = '" + t + "' " + j + "/"
                            + points.length + "  Touples = " + i + "/"
                            + touples.length);
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof CoordinateSequence));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!canEncode(element, value, hints)) {
                throw new OperationNotSupportedException("Cannot encode");
            }

            CoordinateSequence g = (CoordinateSequence) value;

            GMLComplexTypes.encodeCoords(element, g, output);
        }
    }

    /**
     * <p>
     * This class represents an AbstractFeatureType within the GML Schema. This
     * includes both the data and parsing functionality associated  with an
     * AbstractFeatureType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    public static class AbstractFeatureType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
        // static attribute list
        private static final Attribute[] attributes = {
                new GMLAttribute("fid", XSISimpleTypes.ID.getInstance(),
                    Attribute.OPTIONAL),
            };

        // static element list
        private static final Element[] elements = {
                new GMLElement("description",
                    XSISimpleTypes.String.getInstance(), 0, 1, false, null){
                    public boolean isNillable() {
                        return true;
                    }
                },
                new GMLElement("name", XSISimpleTypes.String.getInstance(), 0,1,false,null){
                    public boolean isNillable() {
                        return true;
                    }
                },
                new GMLElement("boundedBy",
                    GMLComplexTypes.BoundingShapeType.getInstance(), 0, 1,
                    false, null){
                        public boolean isNillable() {
                            return true;
                        }
                    },
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
        public static GMLComplexType getInstance() {
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
            Feature f = getFeature(element, value, attrs, hints);

            if ((hints == null) || (hints.get(STREAM_HINT) == null)) {
                return f;
            }

            if (hints.get(STREAM_FEATURE_NAME_HINT) == null) {
                hints.put(STREAM_FEATURE_NAME_HINT, element.getName());
            }

            String nm = (String) hints.get(STREAM_FEATURE_NAME_HINT);

            if ((nm != null) && nm.equals(element.getName())) {
                stream(f, (FCBuffer) hints.get(STREAM_HINT));

                return null;
            }
            return f;
        }

        public Feature getFeature(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            FeatureType ft = (FeatureType) featureTypeMappings.get(element.getType()
                                                                          .getNamespace()
                    + "#" + element.getName());

            if (ft == null) {
                ft = loadFeatureType(element, value, attrs);
            }

            Object[] values = new Object[ft.getAttributeCount()];
            for(int i=0;i<values.length;i++)
                values[i] = null;

            for (int i = 0; i < value.length; i++){
                //find index for value
                int j = -1;
                for (int k=0;k<ft.getAttributeCount() && j==-1;k++){
                    // TODO use equals
//System.out.print(k+"");
                    if((ft.getAttributeType(k).getName()==null && value[i].getElement().getName()==null) ||
                            ft.getAttributeType(k).getName().equals(value[i].getElement().getName()))
                        j = k;
                }
//System.out.print(j+" "+(j!=-1?ft.getAttributeType(j).getName()+"  ":"")+value[i].getElement().getName()+" ** "+value[i].getValue());
//System.out.println(" ft?"+(j!=-1?ft.getAttributeType(j).isNillable()+"  ":"null")+" elem?"+value[i].getElement().isNillable());
                if(j!=-1)
                    values[j] = value[i].getValue();
            }
//for(int i=0;i<values.length;i++){
//	System.out.print(i+" ** ");
//	System.out.print(values[i]);
//	System.out.print(" ? ");
//	System.out.print(ft.getAttributeType(i).isNillable());
//	System.out.println();
//}

            String fid = attrs.getValue("", "fid");

            if ((fid == null) || "".equals(fid)) {
                fid = attrs.getValue(GMLSchema.NAMESPACE.toString(), "fid");
            }
//System.out.println("\n"+values.length);
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

        private void stream(Feature feature, FCBuffer featureCollectionBuffer)
            throws SAXNotSupportedException, SAXException {
            if (!featureCollectionBuffer.addFeature(feature)) {
                throw new SAXException("Buffer overflow");
            }

            if (featureCollectionBuffer.state <= 0) {
                switch (featureCollectionBuffer.state) {
                case FCBuffer.STOP:
                    throw new StopException(); // alternative to stop()

                case FCBuffer.FINISH:
                    return;

                default:
                    featureCollectionBuffer.state = (featureCollectionBuffer
                        .getCapacity() - featureCollectionBuffer.getSize()) / 3;
                    logger.finest("New State " + featureCollectionBuffer.state
                        + " " + featureCollectionBuffer.getSize());

                    while (featureCollectionBuffer.getSize() > (featureCollectionBuffer
                            .getCapacity() - 1)) {
                        logger.finest("waiting for reader");
                        Thread.yield();
                    }
                }
            } else {
                featureCollectionBuffer.state--;
                logger.finest("New State " + featureCollectionBuffer.state
                    + " " + featureCollectionBuffer.getSize());
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
            
            FeatureType ft = createFeatureType(element);
                featureTypeMappings.put(element.getType().getNamespace() + "#"
                    + element.getName(), ft);
//System.out.println(ft);
                return ft;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || (element == null)
                    || !(value instanceof Feature)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;
            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (canEncode(element, value, hints)) {
                Feature f = (Feature) value;
                if (element == null) {
                    print(f, output, hints);
                } else {
                    print(element, f, output, hints);
                }
            }
        }

        private void print(Element e, Feature f, PrintHandler ph, Map hints)
            throws OperationNotSupportedException, IOException {
            AttributesImpl ai = new AttributesImpl();

            if ((f.getID() != null) && !f.getID().equals("")) {
                ai.addAttribute("", "fid", "", "ID", f.getID());
            } else {
                ai = null;
            }

            ph.startElement(e.getNamespace(), e.getName(), ai);

            FeatureType ft = f.getFeatureType();
            AttributeType[] ats = ft.getAttributeTypes();

            if (ats != null) {
                for (int i = 0; i < ats.length; i++) {
                    Element e2 = e.findChildElement(ats[i].getName());
                    e2.getType().encode(e2, f.getAttribute(i), ph, hints);
                }
            }

            ph.endElement(e.getNamespace(), e.getName());
        }

        private void print(Feature f, PrintHandler ph, Map hints)
            throws OperationNotSupportedException, IOException {
            AttributesImpl ai = new AttributesImpl();

            if ((f.getID() != null) && !f.getID().equals("")) {
                ai.addAttribute("", "fid", "", "ID", f.getID());
            } else {
                ai = null;
            }

            ph.startElement(GMLSchema.NAMESPACE, "_Feature", ai);

            FeatureType ft = f.getFeatureType();
            AttributeType[] ats = ft.getAttributeTypes();

            if (ats != null) {
                for (int i = 0; i < ats.length; i++) {
                    Type t = XSISimpleTypes.find(ats[i].getType());
                    t.encode(null, f.getAttribute(i), ph, hints);
                }
            }

            ph.endElement(GMLSchema.NAMESPACE, "_Feature");
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
    public static class AbstractFeatureCollectionsBaseType extends AbstractFeatureType {
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
        public static GMLComplexType getInstance() {
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
    public static class AbstractFeatureCollectionType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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
            if ((hints == null) || (hints.get(STREAM_HINT) == null)) {
                return getCollection(value);
            }

            FCBuffer fcb = (FCBuffer) hints.get(STREAM_HINT);
            fcb.state = FCBuffer.FINISH;

            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element,
         *      java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            if ((hints == null) || (hints.get(STREAM_HINT) == null)) {
                return true;
            }

            ComplexType e = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while (e != null) {
                if ((e.getName() != null)
                        && e.getName().equals(BoxType.getInstance().getName())) {
                    return true;
                }

                e = (e.getParent() instanceof ComplexType)
                    ? (ComplexType) e.getParent() : null;
            }

            return false;
        }

        private FeatureCollection getCollection(ElementValue[] value) {
            FeatureCollection fc = FeatureCollections.newCollection();

            for (int i = 1; i < value.length; i++) // bbox is slot 0

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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof FeatureCollection));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || (!(value instanceof FeatureCollection))) {
                return;
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "_featureCollection",
                    null);
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
            }

            FeatureCollection fc = (FeatureCollection) value;

            if (fc.getBounds() != null) {
                BoundingShapeType.getInstance().encode(null, fc.getBounds(),
                    output, hints);
            }

            FeatureIterator i = fc.features();
            Element e = null;

            while (i.hasNext()) {
                Feature f = i.next();
                output.startElement(GMLSchema.NAMESPACE, "featureMember", null);

                if (e == null) { // first time
                    e = output.findElement(f.getFeatureType().getTypeName());
                    // should go to an abstract FT eventually
                    ComplexType t = e.getType() instanceof ComplexType? (ComplexType)e.getType():null;
                    while(t!=null && t!=AbstractFeatureType.getInstance())
                        t = t.getParent() instanceof ComplexType? (ComplexType)t.getParent():null;
                    if(t!=AbstractFeatureType.getInstance()){
                        // not the right element ... try by type
                        e = output.findElement(value);
                        // should go to an abstract FT eventually
                        t = e.getType() instanceof ComplexType? (ComplexType)e.getType():null;
                        while(t!=null && t!=AbstractFeatureType.getInstance())
                            t = t.getParent() instanceof ComplexType? (ComplexType)t.getParent():null;
                        if(t!=AbstractFeatureType.getInstance()){
                            throw new OperationNotSupportedException("Could not find a correct Element for FeatureType "+f.getFeatureType().getTypeName());
                        }
                    }
                }

                if (e == null) {
                    throw new NullPointerException(
                        "Feature Definition not found in Schema "
                        + element.getNamespace());
                }

                AbstractFeatureType.getInstance().encode(e, f, output, hints);
                output.endElement(GMLSchema.NAMESPACE, "featureMember");
            }

            if (element == null) {
                output.endElement(GMLSchema.NAMESPACE, "_featureCollection");
            } else {
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class GeometryPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return ((t != null) && (value instanceof Geometry));
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!(value instanceof Geometry)) {
                return;
            }

            Geometry g = (Geometry) value;

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "geometryProperty",
                    null);
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
            }

            GMLComplexTypes.encode(null, g, output);

            if (element == null) {
                output.endElement(GMLSchema.NAMESPACE, "geometryProperty");
            } else {
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class FeatureAssociationType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if (!(value instanceof Feature)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t == this;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!(value instanceof Feature)) {
                return;
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "featureMember", null);
                AbstractFeatureType.getInstance().encode(null, value, output,
                    hints);
                output.endElement(GMLSchema.NAMESPACE, "featureMember");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                AbstractFeatureType.getInstance().encode(element
                    .findChildElement(((Feature) value).getFeatureType()
                                       .getTypeName()), value, output, hints);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class BoundingShapeType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

            return value[0].getValue() instanceof Geometry?(Geometry) value[0].getValue():null;
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || (element == null)
                    || !(value instanceof Geometry)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if (!(value instanceof Geometry)) {
                return;
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "boundedBy", null);
                BoxType.getInstance().encode(null, value, output, hints);
                output.endElement(GMLSchema.NAMESPACE, "boundedBy");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);

                if (element.findChildElement("Box") != null) {
                    if (element.findChildElement("Box").getType().canEncode(element
                                .findChildElement("Box"), value, hints)) {
                        element.findChildElement("Box").getType().encode(element
                            .findChildElement("Box"), value, output, hints);
                    }
                }else{
                    if(element.getType() instanceof ComplexType){
                        ComplexType ct = (ComplexType)element.getType();
                        Element[] elems = ct.getChildElements();
                        if(elems!=null)
                        for(int i=0;i<elems.length;i++)
                            if(elems[i].getType().canEncode(elems[i],value,hints)){
                                elems[i].getType().encode(elems[i],value,output,hints);
                                i = elems.length;
                            }
                    }
                    // otherwise don't encode
                }

                BoxType.getInstance().encode(null, value, output, hints);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class PointPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof Point)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof Point)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "pointProperty", null);
                GMLComplexTypes.encode(null, (Point) value, output);
                output.endElement(GMLSchema.NAMESPACE, "pointProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (Point) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
        }
    }

    /**
     * <p>
     * This class represents an PolygonPropertyType within the GML Schema. This
     * includes both the data and parsing functionality associated  with an
     * PolygonPropertyType.
     * </p>
     *
     * @author dzwiers
     *
     * @see GMLComplexType
     * @see ComplexType
     */
    public static class PolygonPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof Polygon)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof Polygon)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "polygonProperty", null);
                GMLComplexTypes.encode(null, (Polygon) value, output);
                output.endElement(GMLSchema.NAMESPACE, "polygonProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (Polygon) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class LineStringPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof LineString)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof Point)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "lineStringProperty",
                    null);
                GMLComplexTypes.encode(null, (LineString) value, output);
                output.endElement(GMLSchema.NAMESPACE, "lineStringProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (LineString) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class MultiPointPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof MultiPoint)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof MultiPoint)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE, "multiPointProperty",
                    null);
                GMLComplexTypes.encode(null, (MultiPoint) value, output);
                output.endElement(GMLSchema.NAMESPACE, "multiPointProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (MultiPoint) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class MultiLineStringPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof MultiLineString)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof MultiLineString)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE,
                    "multiLineStringProperty", null);
                GMLComplexTypes.encode(null, (MultiLineString) value, output);
                output.endElement(GMLSchema.NAMESPACE, "multiLineStringProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (MultiLineString) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class MultiPolygonPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof MultiPolygon)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof MultiPolygon)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE,
                    "multiPolygonProperty", null);
                GMLComplexTypes.encode(null, (MultiPolygon) value, output);
                output.endElement(GMLSchema.NAMESPACE, "multiPolygonProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (MultiPolygon) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
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
    public static class MultiGeometryPropertyType extends GMLComplexType {

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elements;
        }
        
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
        public static GMLComplexType getInstance() {
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

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if ((value == null) || !(value instanceof GeometryCollection)) {
                return false;
            }

            ComplexType t = (element.getType() instanceof ComplexType)
                ? (ComplexType) element.getType() : null;

            while ((t != null) && (t != this))
                t = (t.getParent() instanceof ComplexType)
                    ? (ComplexType) t.getParent() : null;

            return t != null;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if ((value == null) || !(value instanceof GeometryCollection)) {
            	throw new OperationNotSupportedException("Value is "+value == null?"null":value.getClass().getName());
            }

            if (element == null) {
                output.startElement(GMLSchema.NAMESPACE,
                    "multiGeometryProperty", null);
                GMLComplexTypes.encode(null, (GeometryCollection) value, output);
                output.endElement(GMLSchema.NAMESPACE, "multiGeometryProperty");
            } else {
                output.startElement(element.getNamespace(), element.getName(),
                    null);
                GMLComplexTypes.encode(null, (GeometryCollection) value, output);
                output.endElement(element.getNamespace(), element.getName());
            }
        }
    }


    /*
     * Creates a FT from the information provided
     */
    public static FeatureType createFeatureType(Element element) throws SAXException {
        String ftName = element.getName();
        URI ftNS = element.getType().getNamespace();
        logger.finest("Creating feature type for " + ftName + ":" + ftNS);

        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(ftName);
        typeFactory.setNamespace(ftNS);
        typeFactory.setName(ftName);

        GeometryAttributeType geometryAttribute = null;

        ElementGrouping child = ((ComplexType)element.getType()).getChild();
//        FeatureType parent = null;
//        if(((ComplexType)element.getType()).getParent()instanceof ComplexType)
//            parent = createFeatureType((ComplexType)((ComplexType)element.getType()).getParent());
        
//        if(parent != null && parent.getAttributeTypes()!=null){
//            typeFactory.addTypes(parent.getAttributeTypes());
//            if(parent.getDefaultGeometry()!=null){
//                geometryAttribute = parent.getDefaultGeometry();
//            }
//        }
        
        AttributeType[] attrs = (AttributeType[])getAttributes(child).toArray(new AttributeType[]{,});
        for(int i=0;i<attrs.length;i++){
        	if(attrs[i]!=null){
        		typeFactory.addType(attrs[i]);

        if ((geometryAttribute == null)
                && attrs[i].isGeometry()) {
            if (!attrs[i].getName()
//                    .equalsIgnoreCase(BoxType.getInstance().getName())) {
                .equalsIgnoreCase(AbstractFeatureType.getInstance().getChildElements()[2].getName())){
                geometryAttribute = (GeometryAttributeType) attrs[i];
            }
        }}
        }

        if (geometryAttribute != null) {
            typeFactory.setDefaultGeometry(geometryAttribute);
        }

        try {
            FeatureType ft = typeFactory.getFeatureType();
            return ft;
        } catch (SchemaException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
    }
    public static FeatureType createFeatureType(ComplexType element) throws SAXException {
        String ftName = element.getName();
        URI ftNS = element.getNamespace();
        logger.finest("Creating feature type for " + ftName + ":" + ftNS);

        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(ftName);
        typeFactory.setNamespace(ftNS);
        typeFactory.setName(ftName);

        GeometryAttributeType geometryAttribute = null;

        ElementGrouping child = (element).getChild();
//        FeatureType parent = null;
//        if(element.getParent()instanceof ComplexType)
//            parent = createFeatureType((ComplexType)element.getParent());
        
//        if(parent != null && parent.getAttributeTypes()!=null){
//            typeFactory.addTypes(parent.getAttributeTypes());
//            if(parent.getDefaultGeometry()!=null){
//                geometryAttribute = parent.getDefaultGeometry();
//            }
//        }
        
        AttributeType[] attrs = (AttributeType[])getAttributes(child).toArray(new AttributeType[]{,});
        for(int i=0;i<attrs.length;i++){
        	if(attrs[i]!=null){
            typeFactory.addType(attrs[i]);

            if ((geometryAttribute == null)
                && attrs[i].isGeometry()) {
                if (!attrs[i].getName()
//                  .equalsIgnoreCase(BoxType.getInstance().getName())) {
                    .equalsIgnoreCase(AbstractFeatureType.getInstance().getChildElements()[2].getName())){
                    geometryAttribute = (GeometryAttributeType) attrs[i];
                }
            }}
        }

        if (geometryAttribute != null) {
            typeFactory.setDefaultGeometry(geometryAttribute);
        }

        try {
            FeatureType ft = typeFactory.getFeatureType();
            return ft;
        } catch (SchemaException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
    }
    
    private static List getAttributes(ElementGrouping eg){
        ElementGrouping[] elems = null;
    	List l = new LinkedList();
        
        switch(eg.getGrouping()){
        
        case ElementGrouping.CHOICE:
            // assume for most cases the first is chosen
            // TODO make a better solution
            return getAttributes(((Choice)eg).getChildren()[0]);
        case ElementGrouping.GROUP:
            return getAttributes(((Group)eg).getChild());
        case ElementGrouping.ELEMENT:
            // AttributeType
            l.add(getAttribute((Element)eg));
        	return l;
            
        case ElementGrouping.ALL:
            elems = ((All)eg).getElements();
        	break;
        case ElementGrouping.SEQUENCE:
            elems = ((Sequence)eg).getChildren();
    	break;
        }
    	if(elems!=null)
        	for(int i=0;i<elems.length;i++)
        	    l.addAll(getAttributes(elems[i]));
        return l;
    }
    
    private static AttributeType getAttribute(Element eg){
    	if(eg.getNamespace() == GMLSchema.NAMESPACE && (AbstractFeatureType.getInstance().getChildElements()[0] == eg || AbstractFeatureType.getInstance().getChildElements()[1] == eg || AbstractFeatureType.getInstance().getChildElements()[2] == eg))
    		return null;
    	
    	AttributeType at = null;
		if(eg.getType() instanceof ComplexType && eg.getType().getInstanceType().equals(Object[].class)){
			ComplexType ct = (ComplexType)eg.getType();
//System.out.println(ct.getChild());
//System.out.println(ct.getParent());

			switch(ct.getChild().getGrouping()){
			case ElementGrouping.CHOICE:
			case ElementGrouping.ALL:
			case ElementGrouping.SEQUENCE:
			case ElementGrouping.GROUP:
				List l =  getAttributes(ct.getChild());
				if(l!=null)
				return (AttributeType)l.get(0); // HACK
 
		}}
	    	at = AttributeTypeFactory.newAttributeType(eg.getName(),eg.getType().getInstanceType(),eg.isNillable());
//	    	System.out.println("Creating "+eg.getName()+" FT nil?"+at.isNillable()+" Elem nil?"+eg.isNillable()+" "+eg.getType().getInstanceType()+" "+eg.getType().getNamespace()+":"+eg.getType().getName());
		
    	return at;
    }
}
