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

import org.geotools.xml.XMLElementHandler;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.SimpleType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;
import java.util.Map;


/**
 * <p>
 * This class is intended to handle parsing an xml element from an instance
 * document for elements who's type is both known and simple. This handler  is
 * used within the XMLSAXHandler to handle sax events generated by the  SAX
 * parser.
 * </p>
 *
 * @author dzwiers www.refractions.net
 *
 * @see SimpleType
 * @see XMLElementHandler
 */
public class SimpleElementHandler extends XMLElementHandler {
    private SimpleType type; // save casting all over
    private Element elem;
    private String text = "";
    private Object value = null;
    private Attributes attr = null;

    /**
     * Creates a new SimpleElementHandler object.
     *
     * @param st Element the simple element which we will parse
     */
    public SimpleElementHandler(Element st) {
        elem = st;
        type = (SimpleType) st.getType();
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getElement()
     */
    public Element getElement() {
        return elem;
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XMLElementHandler getHandler(URI namespaceURI, String localName,
        Map hints) throws SAXException {
        throw new SAXException(
            "Should not have any children - this is a simpleType");
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getValue()
     */
    public Object getValue() throws SAXException {
        return value;
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getName()
     */
    public String getName() {
        return type.getName();
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#characters(java.lang.String)
     */
    public void characters(String text) throws SAXException {
        if (this.text != null) {
            this.text = this.text.concat(text);
        } else {
            this.text = text;
        }
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(URI namespaceURI, String localName, Map hints)
        throws SAXException {
        text = (text == null) ? null : text.trim();

        ElementValue[] vals = new ElementValue[1];
        vals[0] = new DefaultElementValue(text, elem);
        value = type.getValue(elem, vals, attr, hints);
        attr = null;
        text = null;
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(URI namespaceURI, String localName,
        Attributes attr) throws SAXException {
        this.attr = new AttributesImpl(attr);
    }

    /**
     * <p>
     * Default Implementation used to pass values to type instances
     * </p>
     *
     * @author dzwiers
     *
     * @see ElementValue
     */
    private static class DefaultElementValue implements ElementValue {
        private String value = "";
        private Element t;

        /**
         * Stores the two values for use within the specified type
         *
         * @param value String
         * @param t Element
         *
         * @see SimpleElementHandler#endElement(String, String)
         */
        public DefaultElementValue(String value, Element t) {
            this.value = value;
            this.t = t;
        }

        /**
         * @see org.geotools.xml.xsi.ElementValue#getElement()
         */
        public Element getElement() {
            return t;
        }

        /**
         * @see org.geotools.xml.xsi.ElementValue#getValue()
         */
        public Object getValue() {
            return value;
        }
    }
}
