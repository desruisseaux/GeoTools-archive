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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import java.util.LinkedList;
import java.util.List;


/**
 * KeyrefHandler purpose.
 * 
 * <p>
 * represents a 'keyref' element. This class is not currently used asside  from
 * as a placeholder.
 * </p>
 * TODO use this class semantically
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class KeyrefHandler extends XSIElementHandler {
    /** 'keyref' */
    public final static String LOCALNAME = "keyref";
    private String id;
    private String name;
    private String refer; //TODO check for referential support when using this type
    private SelectorHandler selector;
    private List fields;

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((refer == null)
        ? 1 : refer.hashCode()) * ((name == null) ? 1 : name.hashCode());
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        if (SchemaHandler.namespaceURI.equalsIgnoreCase(namespaceURI)) {
            // child types
            //
            // field
            if (FieldHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (fields == null) {
                    fields = new LinkedList();
                }

                FieldHandler fh = new FieldHandler();
                fields.add(fh);

                return fh;
            }

            // selector
            if (SelectorHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                SelectorHandler sth = new SelectorHandler();

                if (selector == null) {
                    selector = sth;
                } else {
                    throw new SAXNotRecognizedException(LOCALNAME
                        + " may only have one child.");
                }

                return sth;
            }
        }

        return null;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts) throws SAXException {
        id = atts.getValue("", "id");

        if (id == null) {
            id = atts.getValue(namespaceURI, "id");
        }

        name = atts.getValue("", "name");

        if (name == null) {
            name = atts.getValue(namespaceURI, "name");
        }

        refer = atts.getValue("", "refer");

        if (refer == null) {
            refer = atts.getValue(namespaceURI, "refer");
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * <p>
     * returns a list of child field elements
     * </p>
     *
     * @return
     */
    public List getFields() {
        return fields;
    }

    /**
     * <p>
     * returns the id attribute
     * </p>
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * <p>
     * returns the name attribute
     * </p>
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * returns the refer attribute
     * </p>
     *
     * @return
     */
    public String getRefer() {
        return refer;
    }

    /**
     * <p>
     * returns the child selector element
     * </p>
     *
     * @return
     */
    public SelectorHandler getSelector() {
        return selector;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }
}
