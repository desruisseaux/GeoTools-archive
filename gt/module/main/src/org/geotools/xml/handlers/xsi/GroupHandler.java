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

import java.net.URI;

import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.schema.DefaultGroup;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.Group;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;


/**
 * GroupHandler purpose.
 * 
 * <p>
 * Representa a 'group' element
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class GroupHandler extends ElementGroupingHandler {
    /** 'group' */
    public final static String LOCALNAME = "group";
    private static int offset = 0;
    private String id;
    private String name;
    private String ref = null;
    private int maxOccurs = 1;
    private int minOccurs = 1;
    private ElementGroupingHandler child; // one of 'all', 'choice', or 'sequence'
    private int hashCodeOffset = getOffset();
    private Group cache = null;

    /*
     * helper for hashCode()
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((name == null) ? 1 : name.hashCode()))
        + hashCodeOffset;
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
            // all
            if (AllHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                AllHandler sth = new AllHandler();

                if (child == null) {
                    child = sth;
                } else {
                    throw new SAXNotRecognizedException(LOCALNAME
                        + " may only have one child.");
                }

                return sth;
            }

            // choice
            if (ChoiceHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                ChoiceHandler sth = new ChoiceHandler();

                if (child == null) {
                    child = sth;
                } else {
                    throw new SAXNotRecognizedException(LOCALNAME
                        + " may only have one child.");
                }

                return sth;
            }

            // sequence
            if (SequenceHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                SequenceHandler sth = new SequenceHandler();

                if (child == null) {
                    child = sth;
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

        String max = atts.getValue("", "maxOccurs");

        if (max == null) {
            max = atts.getValue(namespaceURI, "maxOccurs");
        }

        String min = atts.getValue("", "minOccurs");

        if (min == null) {
            min = atts.getValue(namespaceURI, "minOccurs");
        }

        name = atts.getValue("", "name");

        if (name == null || "".equals(name)) {
            name = atts.getValue(namespaceURI, "name");
        }

        ref = atts.getValue("", "ref");

        if (ref == null || "".equals(ref)) {
            ref = atts.getValue(namespaceURI, "ref"); // mutally exclusive with
        }
System.out.println("REF ^^^ ="+ref);
        // name ...
        if ((min != null) && !"".equalsIgnoreCase(min)) {
            minOccurs = Integer.parseInt(min);
        }

        if ((max != null) && !"".equalsIgnoreCase(max)) {
            if ("unbounded".equalsIgnoreCase(max)) {
                maxOccurs = ElementTypeHandler.UNBOUNDED;
            } else {
                maxOccurs = Integer.parseInt(max);
            }
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
     * returns the group's name
     * </p>
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.geotools.xml.XSIHandlers.ElementGroupingHandler#compress(org.geotools.xml.XSIHandlers.SchemaHandler)
     */
    protected ElementGrouping compress(SchemaHandler parent)
        throws SAXException {
        if (cache != null) {
            return cache;
        }

        String id = this.id;
        String name = this.name;
        URI uri = parent.getTargetNamespace();
        int minOccurs = this.minOccurs;
        int maxOccurs = this.maxOccurs;
        ElementGrouping child = (this.child == null) ? null
              : this.child.compress(parent); // deal with all/choice/sequnce
System.out.println("***** "+name+":::"+child);
        if (ref != null) {
System.out.println("Group Ref = "+ref);
            Group g = parent.lookUpGroup(ref);
System.out.println("GroupHandler.compress()" + (g==null?"null":g.getClass().getName()));
            if (g != null) {
                if ((id == null) || "".equalsIgnoreCase(id)) {
                    id = g.getId();
                }

                minOccurs = g.getMinOccurs();
                maxOccurs = g.getMaxOccurs();
                name = g.getName();
                uri = g.getNamespace();
                
                child = (g.getChild() == null) ? null : g.getChild();
            }
        }
System.out.println("$$$$$");

        cache = new DefaultGroup(id, name, uri, child,
                minOccurs, maxOccurs);

        child = null;

        return cache;
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
