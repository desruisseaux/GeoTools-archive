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
package org.geotools.xml.handlers.xsi;

import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * SimpleTypeHandler purpose.
 * 
 * <p>
 * represents a simpleType element
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class SimpleTypeHandler extends XSIElementHandler {
    /** NONE  */
    public static final int NONE = 0;

    /** ALL  */
    public static final int ALL = 7;

    /** 'simpleType'  */
    public final static String LOCALNAME = "simpleType";
    
    private static int offset = 0;
    private String id;
    private String name;
    private int finaL;
    private XSIElementHandler child; // one of List, Restriction or Union
    private int hashCodeOffset = getOffset();
    
    private SimpleType cache;

    /*
     * helper for hashCode()
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((finaL == 0)
        ? 1 : finaL) * ((name == null) ? 1 : name.hashCode())) + hashCodeOffset;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        if (SchemaHandler.namespaceURI.equalsIgnoreCase(namespaceURI)) {
            // child types
            //
            // list
            if (ListHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                ListHandler lh = new ListHandler();

                if (child == null) {
                    child = lh;
                } else {
                    throw new SAXNotRecognizedException(getLocalName()
                        + " may only have one '" + ListHandler.LOCALNAME
                        + "' declaration.");
                }

                return lh;
            }

            // restriction
            if (RestrictionHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                RestrictionHandler lh = new RestrictionHandler();

                if (child == null) {
                    child = lh;
                } else {
                    throw new SAXNotRecognizedException(getLocalName()
                        + " may only have one '" + RestrictionHandler.LOCALNAME
                        + "' declaration.");
                }

                return lh;
            }

            // union
            if (UnionHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                UnionHandler lh = new UnionHandler();

                if (child == null) {
                    child = lh;
                } else {
                    throw new SAXNotRecognizedException(getLocalName()
                        + " may only have one '" + UnionHandler.LOCALNAME
                        + "' declaration.");
                }

                return lh;
            }
        }

        return null;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts) throws SAXException {
        id = atts.getValue("", "id");

        if (id == null) {
            id = atts.getValue(namespaceURI, "id");
        }

        String finaL = atts.getValue("", "final");

        if (finaL == null) {
            finaL = atts.getValue(namespaceURI, "final");
        }

        this.finaL = findFinal(finaL);

        name = atts.getValue("", "name");

        if (name == null) {
            name = atts.getValue(namespaceURI, "name");
        }
    }

    /**
     * 
     * <p>
     * translates the final attribute to an integer mask
     * </p>
     *
     * @param finaL
     * @return
     */
    public static int findFinal(String finaL) {
        if ((finaL == null) || "".equalsIgnoreCase(finaL)) {
            return NONE;
        }

        String[] tokens = finaL.split("\\s");
        int r = 0;

        for (int i = 0; i < tokens.length; i++) {
            if ("#all".equalsIgnoreCase(tokens[i])) {
                r = ALL;
                i = tokens.length;
            } else {
                if ("union".equalsIgnoreCase(tokens[i])) {
                    r += UNION;
                } else {
                    if ("list".equalsIgnoreCase(tokens[i])) {
                        r += LIST;
                    } else {
                        if ("restriction".equalsIgnoreCase(tokens[i])) {
                            r += RESTRICTION;
                        }
                    }
                }
            }
        }

        return r;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * 
     * <p>
     * returns the simpletype's name
     * </p>
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * <p>
     * compacts the data resolving references.
     * </p>
     *
     * @param parent
     * @return
     */
    protected SimpleType compress(SchemaHandler parent) {
        logger.info("Start compressing SimpleType " + getName());

        if (cache != null) {
            return cache;
        }

        DefaultSimpleType dst = new DefaultSimpleType();
        dst.finaL = finaL;
        dst.id = id;
        dst.name = name;
        dst.namespace = parent.getTargetNamespace();
        dst.setup(child, parent);
        
        cache = dst;

        logger.info("End compressing SimpleType " + getName());
        id = null;child = null;

        return dst;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return SIMPLETYPE;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }

    /**
     * 
     * <p> 
     * Default implementation of a Simpletype to represent parsed instances
     * </p>
     * @see SimpleType
     * @author dzwiers
     *
     */
    private static class DefaultSimpleType implements SimpleType {
        // file visible to avoid set* methods
        int finaL;
        String id;
        String name;
        String namespace;
        private SimpleType[] children = null;
        private boolean isUnion = false;
        private boolean isList = false;
        private FacetHandler[] constraints;

        /**
         * 
         * @see org.geotools.xml.xsi.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // if it's a union ... deal with it i guess
            return children[0].getInstanceType();
        }

        /**
         * 
         * @see org.geotools.xml.xsi.SimpleType#getFinal()
         */
        public int getFinal() {
            return finaL;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.SimpleType#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Type#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Type#getParent()
         */
        public Type getParent() {
            if (!isUnion && (children != null) && (children.length == 1)) {
                return children[0];
            }

            return null;
        }

        /**
         * 
         * <p>
         * used inplace of a constructor to allow for fancier recursive calls.
         * </p>
         *
         * @param child
         * @param parent
         */
        void setup(XSIElementHandler child, SchemaHandler parent) {
            switch (child.getHandlerType()) {
            case RESTRICTION:
                setup((RestrictionHandler) child, parent);

                break;

            case LIST:
                setup((ListHandler) child, parent);

                break;

            case UNION:
                setup((UnionHandler) child, parent);

                break;

            default:
                throw new RuntimeException(
                    "Should not be here ... child is one of the other three types.");
            }
        }

        /**
         * 
         * <p>
         * helper method for setup(XSIElementHandler,SchemaHandler)
         * </p>
         *
         * @param rest
         * @param parent
         */
        void setup(RestrictionHandler rest, SchemaHandler parent) {
            if (rest.getChild() != null) {
                children = new SimpleType[1];
                children[0] = ((SimpleTypeHandler) rest.getChild()).compress(parent);
            } else {
                // find this rest.getBase()
                children = new SimpleType[1];
                children[0] = parent.lookUpSimpleType(rest.getBase());
            }

            if (rest.getConstraints() != null) {
                constraints = (FacetHandler[]) rest.getConstraints().toArray(new FacetHandler[0]);
            }
        }

        /**
         * 
         * <p>
         * helper method for setup(XSIElementHandler,SchemaHandler)
         * </p>
         *
         * @param rest
         * @param parent
         */
        void setup(ListHandler lst, SchemaHandler parent) {
            isList = true;

            if (lst.getSimpleType() != null) {
                children = new SimpleType[1];
                children[0] = (lst.getSimpleType()).compress(parent);
            } else {
                // find this rest.getBase()
                children = new SimpleType[1];
                children[0] = parent.lookUpSimpleType(lst.getItemType());
            }
        }

        /**
         * 
         * <p>
         * helper method for setup(XSIElementHandler,SchemaHandler)
         * </p>
         *
         * @param rest
         * @param parent
         */
        void setup(UnionHandler union, SchemaHandler parent) {
            isUnion = true;

            List l = new LinkedList();

            if (union.getMemberTypes() != null) {
                String[] qNames = union.getMemberTypes().split("\\s");

                for (int i = 0; i < qNames.length; i++)
                    l.add(parent.lookUpSimpleType(qNames[i]));
            }

            if (union.getSimpleTypes() != null) {
                Iterator i = union.getSimpleTypes().iterator();

                while (i.hasNext()) {
                    l.add(((SimpleTypeHandler) i.next()).compress(parent));
                }
            }

            children = (SimpleType[]) l.toArray(children);
        }

        /**
         * <p>
         * This method ignores the attributes from the xml node
         * </p>
         *
         * @see schema.Type#getValue(java.lang.Object, org.xml.sax.Attributes)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException {
            if ((value == null) || (value.length != 1)) {
                throw new SAXException(
                    "can only have one text value ... and one is required");
            }

            if (isUnion) {
                return getUnionValue(element, value[0], attrs, hints);
            }

            if (isList) {
                return getListValue(element, value[0], attrs, hints);
            }

            return getRestValue(element, value[0], attrs, hints);
        }

        /*
         * Helper for getValue(Element,ElementValue[])
         */
        private Object getUnionValue(Element element, ElementValue value,
            Attributes attrs, Map hints) throws SAXException {
            if (children == null) {
                return null;
            }
            ElementValue[] valss = new ElementValue[1];
            valss[0] = value;
            for (int i = 0; i < children.length; i++) {
                Object o = children[0].getValue(element, valss, attrs, hints);
                if (o != null) {
                    return o;
                }
            }
            return null;
        }

        /*
         * Helper for getValue(Element,ElementValue[])
         */
        private Object getListValue(Element element, ElementValue value,
            Attributes attrs, Map hints) throws SAXException {
            if ((children == null) || (children[0] == null)) {
                return null;
            }

            String[] vals = ((String) value.getValue()).split("\\s");
            List l = new LinkedList();
            DefaultElementValue[] valss = new DefaultElementValue[1];
            valss[0] = new DefaultElementValue();
            valss[0].e = value.getElement();

            for (int i = 0; i < vals.length; i++) {
                valss[0].val = vals[i];
                l.add(children[0].getValue(element, valss, attrs, hints));
            }

            valss[0].val = l;

            return valss[0];
        }

        /*
         * Helper for getValue(Element,ElementValue[])
         */
        private Object getRestValue(Element element, ElementValue value,
            Attributes attrs, Map hints) throws SAXException {
            if ((children == null) || (children[0] == null)) {
                return null;
            }

            if (constraints == null) {
                return null;
            }

            if (constraints.length == 0) {
                ElementValue[] t = new ElementValue[1];
                t[0] = value;

                return children[0].getValue(element, t, attrs, hints);
            }

            String val = (String) value.getValue();

            if (constraints[0].getType() == FacetHandler.ENUMERATION) {
                for (int i = 0; i < constraints.length; i++) {
                    if (val.equalsIgnoreCase(constraints[i].getValue())) {
                        ElementValue[] t = new ElementValue[1];
                        t[0] = value;

                        return children[0].getValue(element, t, attrs, hints);
                    }
                }

                return null;
            } else {
                Number nval = null;
                Date dval = null;
                String sval = val;

                ElementValue[] t = new ElementValue[1];
                t[0] = value;

                Object o = children[0].getValue(element, t, attrs, hints);

                if (o instanceof Number) {
                    nval = (Number) o;
                }

                if (o instanceof Date) {
                    dval = (Date) o;
                }

                // check each constraint
                for (int i = 0; i < constraints.length; i++) {
                    switch (constraints[i].getType()) {
                    case FacetHandler.ENUMERATION:
                        throw new SAXException(
                            "cannot have enumerations mixed with other facets.");

                    case FacetHandler.FRACTIONDIGITS:

                        int decimals = val.length() - val.indexOf(".");
                        int maxDec = Integer.parseInt(constraints[i].getValue());

                        if (decimals > maxDec) {
                            throw new SAXException("Too many decimal places");
                        }

                        break;

                    case FacetHandler.LENGTH:

                        int maxLength = Integer.parseInt(constraints[i]
                                .getValue());

                        if (val.length() != maxLength) {
                            throw new SAXException("Too long places");
                        }

                        break;

                    case FacetHandler.MAXEXCLUSIVE:

                        if (nval != null) {
                            Double max = Double.valueOf(constraints[i].getValue());

                            if (nval.doubleValue() > max.doubleValue()) {
                                throw new SAXException("Too large a value");
                            }
                        }

                        if (dval != null) {
                            Date max;

                            try {
                                max = DateFormat.getDateTimeInstance().parse(constraints[i]
                                        .getValue());
                            } catch (ParseException e) {
                                throw new SAXException(e);
                            }

                            if (dval.after(max)) {
                                throw new SAXException("Too large a value");
                            }
                        }

                        break;

                    case FacetHandler.MAXINCLUSIVE:

                        if (nval != null) {
                            Double max = Double.valueOf(constraints[i].getValue());

                            if (nval.doubleValue() >= max.doubleValue()) {
                                throw new SAXException("Too large a value");
                            }
                        }

                        if (dval != null) {
                            Date max;

                            try {
                                max = DateFormat.getDateTimeInstance().parse(constraints[i]
                                        .getValue());
                            } catch (ParseException e) {
                                throw new SAXException(e);
                            }

                            if (dval.compareTo(max) > 0) {
                                throw new SAXException("Too large a value");
                            }
                        }

                    case FacetHandler.MAXLENGTH:
                        maxLength = Integer.parseInt(constraints[i].getValue());

                        if (val.length() > maxLength) {
                            throw new SAXException("Too long places");
                        }

                        break;

                    case FacetHandler.MINEXCLUSIVE:

                        if (nval != null) {
                            Double max = Double.valueOf(constraints[i].getValue());

                            if (nval.doubleValue() < max.doubleValue()) {
                                throw new SAXException("Too large a value");
                            }
                        }

                        if (dval != null) {
                            Date max;

                            try {
                                max = DateFormat.getDateTimeInstance().parse(constraints[i]
                                        .getValue());
                            } catch (ParseException e) {
                                throw new SAXException(e);
                            }

                            if (dval.before(max)) {
                                throw new SAXException("Too large a value");
                            }
                        }

                    case FacetHandler.MININCLUSIVE:

                        if (nval != null) {
                            Double max = Double.valueOf(constraints[i].getValue());

                            if (nval.doubleValue() <= max.doubleValue()) {
                                throw new SAXException("Too large a value");
                            }
                        }

                        if (dval != null) {
                            Date max;

                            try {
                                max = DateFormat.getDateTimeInstance().parse(constraints[i]
                                        .getValue());
                            } catch (ParseException e) {
                                throw new SAXException(e);
                            }

                            if (dval.compareTo(max) < 0) {
                                throw new SAXException("Too large a value");
                            }
                        }

                    case FacetHandler.MINLENGTH:
                        maxLength = Integer.parseInt(constraints[i].getValue());

                        if (val.length() < maxLength) {
                            throw new SAXException("Too short places");
                        }

                        break;

                    case FacetHandler.PATTERN:

                        if (val.split(constraints[i].getValue()).length != 1) {
                            throw new SAXException("Does not match pattern");
                        }

                        break;

                    case FacetHandler.TOTALDIGITS:
                        maxLength = Integer.parseInt(constraints[i].getValue())
                            + 1;

                        if (val.length() > maxLength) {
                            throw new SAXException("Too many digits");
                        }

                        break;
                    }
                }

                return o;
            }
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Type#getNamespace()
         */
        public String getNamespace() {
            return namespace;
        }

        private static class DefaultElementValue implements ElementValue {
            private Element e;
            private Object val;

            /* (non-Javadoc)
             * @see schema.ElementValue#getElement()
             */
            public Element getElement() {
                return e;
            }

            /* (non-Javadoc)
             * @see schema.ElementValue#getValue()
             */
            public Object getValue() {
                return val;
            }
        }
    }
}
