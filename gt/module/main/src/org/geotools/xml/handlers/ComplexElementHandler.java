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
package org.geotools.xml.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.xml.XMLElementHandler;
import org.geotools.xml.schema.All;
import org.geotools.xml.schema.Any;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * <p>
 * This class is intended to handle parsing an xml element from an instance 
 * document for elements who's type is both known and complex. This handler 
 * is used within the XMLSAXHandler to handle sax events generated by the 
 * SAX parser.
 * </p>
 * @see ComplexType
 *
 * @author dzwiers www.refractions.net
 */
public class ComplexElementHandler extends XMLElementHandler {
    private ComplexType type; // saves casting all over
    private Element elem;
    private String text;
    private Attributes attr;
    private List elements;
    private Object value = null;
    private ElementHandlerFactory ehf;

    /**
     * Creates a new ComplexElementHandler object for Element 
     * elem using ElementHandlerFactory ehf.
     *
     * @param ehf ElementHandlerFactory
     * @param elem Element
     *
     * @throws SAXException
     */
    public ComplexElementHandler(ElementHandlerFactory ehf, Element elem)
        throws SAXException {
        this.ehf = ehf;

        if (elem == null) {
            logger.warning("ComplexType provided was null");
            throw new SAXException(new NullPointerException(
                    "ComplexType provided was null"));
        }

        this.elem = elem;

        try {
            type = (ComplexType) elem.getType();
        } catch (ClassCastException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        if ((type.getChild() == null) && (type.getParent() == null)) {
            logger.warning("ComplexType's Child provided was null");
            throw new SAXException(new NullPointerException(
                    "ComplexType's Child provided was null"));
        }
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getElement()
     */
    public Element getElement() {
        return elem;
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#characters(java.lang.String)
     */
    public void characters(String text) throws SAXException {
        if (type.isMixed()) {
            if (this.text != null) {
                this.text = this.text.concat(text);
            } else {
                this.text = text;
            }
        } else {
            if(!"".equals(text.trim())){
            if (type.getName() == null) {
                throw new SAXException("This type may not have mixed content");
            } else {
            	throw new SAXException("The " + type.getName()
                    + " type may not have mixed content");
        	}
            }
        }
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#endElement(java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, Map hints)
        throws SAXException {
        text = text==null?null:text.trim();
        if (elements == null) {
            if (type != null) {
                ElementValue[] vals = new ElementValue[1];
                vals[0] = new DefaultElementValue(null, text); // null is ok as
                                                               // this
                                                               // represents the
                                                               // mixed content

                value = type.getValue(elem, vals, attr,hints);
            } else {
                value = text;
            }

            return;
        }

        // validate the complex element ... throws an exception when it's been bad
        validateElementOrder();

        ElementValue[] vals = new ElementValue[elements.size()
            + (type.isMixed() ? 1 : 0)];

        for (int i = 0; i < vals.length; i++) {
            XMLElementHandler xeh = (XMLElementHandler) elements.get(i);
            vals[i] = new DefaultElementValue(xeh.getElement(), xeh.getValue());
        }

        if (type.isMixed()) {
            vals[vals.length - 1] = new DefaultElementValue(null, text); 
            // null is ok as this represents the mixed content
        }

        value = type.getValue(elem, vals, attr,hints);
        
        // clean memory
        attr = null;
        elements = null;
        text = null;
    }

    /*
     * This starts off the fun or checking element order for complex types 
     * (note mixtures of All, Any, Choice, Element, Group, Sequence).
     * 
     */
    private void validateElementOrder() throws SAXException {
        if ((elements == null) || (elements.size() == 0)) {
            return;
        }

        int i = 0;
        boolean changed = true;

        for (;
                (i < elements.size()) && changed
                && (i < type.getChild().getMaxOccurs());) {
            int t = i;
            i = valid(type.getChild(), i);
            changed = t != i;
        }

        if (i < type.getChild().getMinOccurs()) {
            throw new SAXException("Too few elements declared for "
                + type.getName());
        }

        if (i != elements.size()) {
            throw new SAXException("Invalid element order according: "
                + type.getName() + "[" + i + "]");
        }
    }

    /*
     * Generic validation method which simulates recursion, and avoids casting :)
     * The index is the starting index in the list of elements, for the particular 
     * ElementGrouping. The last index matched is returned.
     */
    private int valid(ElementGrouping eg, int index) throws SAXException {
        if (eg == null) {
            return index;
        }

        switch (eg.getGrouping()) {
        case ElementGrouping.SEQUENCE:
            return valid((Sequence) eg, index);

        case ElementGrouping.ALL:
            return valid((All) eg, index);

        case ElementGrouping.ANY:
            return valid((Any) eg, index);

        case ElementGrouping.CHOICE:
            return valid((Choice) eg, index);

        case ElementGrouping.GROUP:
            return valid((Group) eg, index);

        case ElementGrouping.ELEMENT:
            return valid((Element) eg, index);
        }

        return index;
    }

    /*
     * Validates an All tag
     * @see valid(ElementGrouping)
     */
    private int valid(All all, int index) throws SAXException {
        Element[] elems = all.getElements();
        int[] r = new int[elems.length];

        for (int i = 0; i < r.length; i++)
            r[i] = 0;

        boolean c = true;

        while (c) {
            c = false;

            for (int i = 0; i < elems.length; i++) {
                if (elems[i].getType().getName().equalsIgnoreCase(((XMLElementHandler) elements
                            .get(index)).getName())) {
                    r[i]++;
                    index++;
                    i = elems.length;
                    c = true;
                }
            }
        }

        for (int i = 0; i < r.length; i++) {
            if ((r[i] < elems[i].getMinOccurs())
                    || (r[i] > elems[i].getMaxOccurs())) {
                throw new SAXException("Too many or too few "
                    + elems[i].getName());
            }
        }

        return index;
    }

    /*
     * Validates an Any tag
     * @see valid(ElementGrouping)
     */
    private int valid(Any any, int index) {
        if (any.getNamespace().equalsIgnoreCase(((XMLElementHandler) elements
                                                     .get(index)).getElement()
                                                     .getType().getNamespace())) {
            return index + 1;
        }

        return index;
    }

    /*
     * Validates an Choice tag
     * @see valid(ElementGrouping)
     */
    private int valid(Choice choice, int index) throws SAXException {
        ElementGrouping[] eg = choice.getChildren();

        if (eg == null) {
            return index;
        }

        int r = index;

        for (int i = 0; i < eg.length; i++) {
            int t = valid(eg[i], index);

            if ((t > index) && (t > r)) {
                r = t;
            }
        }

        return r;
    }

    /*
     * Validates an Group tag
     * @see valid(ElementGrouping)
     */
    private int valid(Group group, int index) throws SAXException {
        if (group.getChild() == null) {
            return index;
        }

        return valid(group.getChild(), index);
    }

    /*
     * Validates an Element tag
     * @see valid(ElementGrouping)
     */
    private int valid(Element element, int index) {
        XMLElementHandler indexHandler = ((XMLElementHandler) elements.get(index));

        if ((indexHandler.getName() != null)
                && indexHandler.getName().equalsIgnoreCase(element.getName())) {
            return index + 1;
        }

        if ((indexHandler.getElement().getType() != null)
                && (indexHandler.getElement().getType().getName() != null)
                && indexHandler.getElement().getType().getName()
                                   .equalsIgnoreCase(element.getType().getName())) {
            return index + 1;
        }

        if (indexHandler.getElement().getType() instanceof ComplexType) {
            ComplexType ct = (ComplexType) indexHandler.getElement().getType();
            Type parent = ct.getParent();

            while (parent != null) {
                if ((parent.getName() != null)
                        && parent.getName().equalsIgnoreCase(element.getType()
                                                                        .getName())) {
                    return index + 1;
                }

                parent = parent.getParent();
            }
        }

        return index;
    }

    /*
     * Validates a Sequence tag
     * @see valid(ElementGrouping)
     */
    private int valid(Sequence seq, int index) throws SAXException {
        ElementGrouping[] eg = seq.getChildren();

        if (eg == null) {
            return index;
        }

        int ind = index;

        for (int i = 0; i < eg.length; i++) {
            int r = ind;
            int r2 = ind; // will hold new index
            int j = 0;

            for (;
                    (j < eg[i].getMaxOccurs()) && (r >= 0)
                    && (r < elements.size()); j++) {
                r2 = r;
                r = valid(eg[i], r);
            }

            if (r > 0) {
                r2 = r;
            }

            if (j < eg[i].getMinOccurs()) {
                throw new SAXException("Too few " + eg[i]);
            }

            ind = r2;
        }

        return ind;
    }

    /*
     * (non-Javadoc)
     *
     * @see schema.XMLElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes attr) throws SAXException {
        this.attr = new AttributesImpl(attr);
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XMLElementHandler getHandler(String namespaceURI, String localName, Map hints)
        throws SAXException {
        if (elements == null) {
            elements = new LinkedList();
        }

        logger.finest("Starting search for element handler " + localName
            + " :: " + namespaceURI);

        Element e = type.findChildElement(localName);

        if (e != null) {
            XMLElementHandler r = ehf.createElementHandler(e);
            if(type.cache(r.getElement(),hints))
                elements.add(r);
            return r;
        }

        logger.finest("Checking the document schemas");

        XMLElementHandler r = ehf.createElementHandler(namespaceURI, localName);

        if (r != null) {
            if(type.cache(r.getElement(),hints))
                elements.add(r);
            return r;
        }

        throw new SAXException("Could not find element handler for "
            + namespaceURI + " : " + localName);
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getValue()
     */
    public Object getValue() throws SAXException {
        // endElement sets the value
        return value;
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getName()
     */
    public String getName() {
        return elem.getName();
    }

    /**
     * <p> 
     * Default Implementation used to pass values to type instances
     * </p>
     * @author dzwiers
     *
     * @see ElementValue
     */
    private static class DefaultElementValue implements ElementValue {
        Element t;
        Object value;

        /**
         * 
         * Stores the two values for use within the specified type
         * @see ComplexElementHandler#endElement(String, String)
         * 
         * @param value String
         * @param t Element
         */
        public DefaultElementValue(Element t, Object o) {
            this.t = t;
            value = o;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.ElementValue#getElement()
         */
        public Element getElement() {
            return t;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.ElementValue#getValue()
         */
        public Object getValue() {
            return value;
        }
    }
}
