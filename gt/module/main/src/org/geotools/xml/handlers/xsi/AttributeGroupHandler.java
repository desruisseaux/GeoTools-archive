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
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * AttributeGroupHandler purpose.
 * 
 * <p>
 * Represents an 'attributeGroup' element.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class AttributeGroupHandler extends XSIElementHandler {
    /** 'attributeGroup'  */
    public final static String LOCALNAME = "attributeGroup";
    
    private static int offset = 0;
    private String id;
    private String name;
    private String ref;
    private AnyAttributeHandler anyAttribute;
    private List attrDecs;
    private int hashCodeOffset = getOffset();
    private AttributeGroup cache = null;

    /*
     * Helper method for hashCode
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((ref == null)
        ? 1 : ref.hashCode()) * ((name == null) ? 1 : name.hashCode()))
        + hashCodeOffset;
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
            // attribute
            if (AttributeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (attrDecs == null) {
                    attrDecs = new LinkedList();
                }

                AttributeHandler ah = new AttributeHandler();
                attrDecs.add(ah);

                return ah;
            }

            // attributeGroup
            if (AttributeGroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (attrDecs == null) {
                    attrDecs = new LinkedList();
                }

                AttributeGroupHandler ah = new AttributeGroupHandler();
                attrDecs.add(ah);

                return ah;
            }

            // anyAttribute
            if (AnyAttributeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                AnyAttributeHandler sth = new AnyAttributeHandler();

                if (anyAttribute == null) {
                    anyAttribute = sth;
                } else {
                    throw new SAXNotRecognizedException(LOCALNAME
                        + " may only have one child declaration.");
                }

                return sth;
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

        name = atts.getValue("", "name");

        if (name == null) {
            name = atts.getValue(namespaceURI, "name");
        }

        ref = atts.getValue("", "ref");

        if (ref == null) {
            ref = atts.getValue(namespaceURI, "ref");
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
     * returns the 'name' attribute
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * <p>
     * Reduces the memory imprint returning a smaller object
     * </p>
     *
     * @param parent
     * @return
     * @throws SAXException
     */
    protected AttributeGroup compress(SchemaHandler parent)
        throws SAXException {
        if (cache != null) {
            return cache;
        }

        AttributeGroupDefault agd = new AttributeGroupDefault();

        agd.id = id;
        agd.name = name;
        agd.anyAttributeNamespace = (anyAttribute == null) ? null
                                                           : anyAttribute
            .getNamespace();

        if (attrDecs != null) {
            Iterator i = attrDecs.iterator();
            HashSet h = new HashSet();

            while (i.hasNext()) {
                Object o = i.next();

                if (o instanceof AttributeHandler) {
                    h.add(((AttributeHandler) o).compress(parent));
                } else {
                    AttributeGroupHandler agh = (AttributeGroupHandler) o;
                    AttributeGroup ag = agh.compress(parent);

                    if ((ag != null) && (ag.getAttributes() != null)) {
                        Attribute[] aa = ag.getAttributes();

                        for (int j = 0; j < aa.length; j++)
                            h.add(aa[j]);
                    }
                }
            }

            agd.attributes = (Attribute[]) h.toArray(new Attribute[h.size()]);
        }

        if ((ref != null) && !"".equalsIgnoreCase(ref)) {
            AttributeGroup ag = parent.lookUpAttributeGroup(ref);

            if (ag == null) {
                throw new SAXException("AttributeGroup '" + ref
                    + "' was refered and not found");
            }

            agd.name = ag.getName();

            if ((anyAttribute == null)
                    || "".equalsIgnoreCase(anyAttribute.getNamespace())) {
                agd.anyAttributeNamespace = ag.getAnyAttributeNameSpace();
            }

            if (agd.attributes != null) {
                throw new SAXException(
                    "Cannot have a ref and children for an AttributeGroup");
            }

            agd.attributes = ag.getAttributes();
        }

        cache = agd;

        return cache;
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
     * An implementation of AttributeGroup
     * </p>
     * @see AttributeGroup
     * @author dzwiers
     *
     */
    private class AttributeGroupDefault implements AttributeGroup {
        String anyAttributeNamespace;
        Attribute[] attributes;
        String id;
        String name;

        /**
         * 
         * @see org.geotools.xml.xsi.AttributeGroup#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return anyAttributeNamespace;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.AttributeGroup#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.AttributeGroup#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.AttributeGroup#getName()
         */
        public String getName() {
            return name;
        }
    }
}
