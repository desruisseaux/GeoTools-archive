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
import org.geotools.xml.schema.Any;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * AnyHandler purpose.
 * 
 * <p>
 * Represents an 'any' element.
 * </p>
 * 
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class AnyHandler extends ElementGroupingHandler {
    /** 'any'  */
    public final static String LOCALNAME = "any";

    /** strict  */
    public static final int STRICT = 0;

    /** lax  */
    public static final int LAX = 1;

    /** skip  */
    public static final int SKIP = 2;
    
    private String id;
    private String namespace;
    private int minOccurs;
    private int maxOccurs;
//    private int processContents;
    private Any cache = null;

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()))
        + (minOccurs * maxOccurs);
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {

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

        String min = atts.getValue("", "minOccurs");

        if (min == null) {
            min = atts.getValue(namespaceURI, "minOccurs");
        }

        String max = atts.getValue("", "maxOccurs");

        if (max == null) {
            max = atts.getValue(namespaceURI, "maxOccurs");
        }

        namespace = atts.getValue("", "namespace");

        if (namespace == null) {
            namespace = atts.getValue(namespaceURI, "namespace");
        }

//        String processContents = atts.getValue("", "processContents");
//
//        if (processContents == null) {
//            processContents = atts.getValue(namespaceURI, "processContents");
//        }
//
//        this.processContents = findProcess(processContents);

        if ((null == min) || "".equalsIgnoreCase(min)) {
            minOccurs = 1;
        } else {
            minOccurs = Integer.parseInt(min);
        }

        if ((null == max) || "".equalsIgnoreCase(max)) {
            maxOccurs = 1;
        } else {
            if ("unbounded".equalsIgnoreCase(max)) {
                maxOccurs = ElementTypeHandler.UNBOUNDED;
            } else {
                maxOccurs = Integer.parseInt(max);
            }
        }
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
     * maps strings -> int constants for the 'process' attribute 
     * </p>
     *
     * @param process
     * @return
     * @throws SAXException
     */
    public static int findProcess(String process) throws SAXException {
        if ((process == null) || "".equalsIgnoreCase(process)) {
            return STRICT;
        }

        if ("lax".equalsIgnoreCase(process)) {
            return LAX;
        } else {
            if ("skip".equalsIgnoreCase(process)) {
                return SKIP;
            } else {
                if ("strict".equalsIgnoreCase(process)) {
                    return STRICT;
                } else {
                    throw new SAXException("Unknown Process Type: '" + process
                        + "'");
                }
            }
        }
    }

    /**
     * 
     * <p>
     * reverses the findProcess method, converting from integers to String for 
     * the process attribute.
     * </p>
     *
     * @param process
     * @return
     */
    public static String writeProcess(int process) {
        switch (process) {
        case LAX:
            return "lax";

        case SKIP:
            return "skip";

        case STRICT:default:
            return "strict";
        }
    }

    /**
     * 
     * @see org.geotools.xml.XSIHandlers.ElementGroupingHandler#compress(org.geotools.xml.XSIHandlers.SchemaHandler)
     */
    protected ElementGrouping compress(SchemaHandler parent)
        throws SAXException {
        if (cache != null) {
            return cache;
        }

        DefaultAny da = new DefaultAny();
        da.id = id;
        da.namespace = namespace;
        da.minOccurs = minOccurs;
        da.maxOccurs = maxOccurs;

        cache = da;
        id = namespace = null;

        return da;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
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
     * Any instance implementation
     * </p>
     * @see Any
     * @author dzwiers
     *
     */
    private static class DefaultAny implements Any {
        String id;
        String namespace;
        int maxOccurs;
        int minOccurs;

        public Element findChildElement(String name) {
            //TODO look up namespace Schema and do this correctly
            return null;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Any#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.ElementGrouping#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return maxOccurs;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.ElementGrouping#getMinOccurs()
         */
        public int getMinOccurs() {
            return minOccurs;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Any#getNamespace()
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return ANY;
        }
    }
}
