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
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.Sequence;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.LinkedList;
import java.util.List;


/**
 * SequenceHandler purpose.
 * 
 * <p>
 * represents a sequence element
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class SequenceHandler extends ElementGroupingHandler {
    /** 'sequence' */
    public final static String LOCALNAME = "sequence";
    private String id;
    private int maxOccurs;
    private int minOccurs;
    private List children; // element, group, choice, sequence or any
    private Sequence cache = null;

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()))
        + ((children == null) ? 2 : children.hashCode());
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        logger.finest("Getting Handler for " + localName + " :: "
            + namespaceURI);

        if (namespaceURI.equalsIgnoreCase(namespaceURI)) {
            // child types
            //
            // any
            if (AnyHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (children == null) {
                    children = new LinkedList();
                }

                AnyHandler ah = new AnyHandler();
                children.add(ah);

                return ah;
            }

            // choice
            if (ChoiceHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (children == null) {
                    children = new LinkedList();
                }

                ChoiceHandler ah = new ChoiceHandler();
                children.add(ah);

                return ah;
            }

            // element
            if (ElementTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (children == null) {
                    children = new LinkedList();
                }

                ElementTypeHandler ah = new ElementTypeHandler();
                children.add(ah);

                return ah;
            }

            // group
            if (GroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (children == null) {
                    children = new LinkedList();
                }

                GroupHandler ah = new GroupHandler();
                children.add(ah);

                return ah;
            }

            // sequence
            if (LOCALNAME.equalsIgnoreCase(localName)) {
                if (children == null) {
                    children = new LinkedList();
                }

                SequenceHandler ah = new SequenceHandler();
                children.add(ah);

                return ah;
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
        // id
        id = atts.getValue("", "id");

        if (id == null) {
            id = atts.getValue(namespaceURI, "id");
        }

        // maxOccurs
        String maxOccurs = atts.getValue("", "maxOccurs");

        if (maxOccurs == null) {
            maxOccurs = atts.getValue(namespaceURI, "maxOccurs");
        }

        // minOccurs
        String minOccurs = atts.getValue("", "minOccurs");

        if (minOccurs == null) {
            minOccurs = atts.getValue(namespaceURI, "minOccurs");
        }

        if ((minOccurs != null) && !"".equalsIgnoreCase(minOccurs)) {
            this.minOccurs = Integer.parseInt(minOccurs);
        } else {
            this.minOccurs = 1;
        }

        if ((maxOccurs != null) && !"".equalsIgnoreCase(maxOccurs)) {
            if ("unbounded".equalsIgnoreCase(maxOccurs)) {
                this.maxOccurs = ElementTypeHandler.UNBOUNDED;
            } else {
                this.maxOccurs = Integer.parseInt(maxOccurs);
            }
        } else {
            this.maxOccurs = 1;
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * @see org.geotools.xml.XSIHandlers.ElementGroupingHandler#compress(org.geotools.xml.XSIHandlers.SchemaHandler)
     */
    protected ElementGrouping compress(SchemaHandler parent)
        throws SAXException {
        if (cache != null) {
            return cache;
        }

        DefaultSequence ds = new DefaultSequence();
        ds.id = id;
        ds.minOccurs = minOccurs;
        ds.maxOccurs = maxOccurs;

        logger.finest(id + " :: This Sequence has "
            + ((children == null) ? 0 : children.size()) + " children");

        if (children != null) {
            ds.children = new ElementGrouping[children.size()];

            // TODO compress sequences here
            // sequqnces can be inlined here.
            for (int i = 0; i < ds.children.length; i++)
                ds.children[i] = ((ElementGroupingHandler) children.get(i))
                    .compress(parent);
        }

        cache = ds;
        children = null;
        id = null;

        return ds;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return SEQUENCE;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }

    /**
     * <p>
     * Default implementation of a sequence for a parsed xml sequence
     * </p>
     *
     * @author dzwiers
     *
     * @see Sequence
     */
    private static class DefaultSequence implements Sequence {
        // file visible avoids set* methods
        ElementGrouping[] children;
        String id;
        int minOccurs;
        int maxOccurs;

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            if (children == null) {
                return null;
            }

            for (int i = 0; i < children.length; i++) {
                Element t = children[i].findChildElement(name);

                if (t != null) { // found it

                    return t;
                }
            }

            return null;
        }

        /**
         * @see org.geotools.xml.xsi.Sequence#getChildren()
         */
        public ElementGrouping[] getChildren() {
            return children;
        }

        /**
         * @see org.geotools.xml.xsi.Sequence#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return maxOccurs;
        }

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#getMinOccurs()
         */
        public int getMinOccurs() {
            return minOccurs;
        }

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return SEQUENCE;
        }
    }
}
