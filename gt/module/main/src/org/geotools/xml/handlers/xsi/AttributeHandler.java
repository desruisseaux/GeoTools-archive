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
import org.geotools.xml.schema.SimpleType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;


/**
 * AttributeHandler purpose.
 * 
 * <p>
 * Represents an 'attribute' element
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * 
 *  AttributeHandler x = new AttributeHandler(...);
 *  
 * </code></pre>
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class AttributeHandler extends XSIElementHandler {
    /** 'attribute'  */
    public final static String LOCALNAME = "attribute";

    /** OPTIONAL  */
    public static final int OPTIONAL = 0;

    /** PROHIBITED  */
    public static final int PROHIBITED = 1;

    /** REQUIRED  */
    public static final int REQUIRED = 2;
    
    private static int offset = 0;
    private String id;
    private String name;
    private String type;
    private String ref;
    private String def;
    private String fixed;
    private int use;
    private SimpleTypeHandler simpleType;
    private int hashCodeOffset = getOffset();
    private Attribute cache = null;

    /*
     * hashCode() helper
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((type == null)
        ? 1 : type.hashCode()) * ((name == null) ? 1 : name.hashCode()))
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
            // simpleType
            if (SimpleTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                SimpleTypeHandler sth = new SimpleTypeHandler();

                if (simpleType == null) {
                    simpleType = sth;
                } else {
                    throw new SAXNotRecognizedException(
                        "Extension may only have one 'simpleType' or 'complexType' declaration.");
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

        String name = atts.getValue("", "name");

        if (name == null) {
            name = atts.getValue(namespaceURI, "name");
        }

        String ref = atts.getValue("", "ref");

        if (ref == null) {
            ref = atts.getValue(namespaceURI, "ref");
        }

        String type = atts.getValue("", "type");

        if (type == null) {
            type = atts.getValue(namespaceURI, "type");
        }

        this.name = name;
        this.type = type;
        this.ref = ref;

        def = atts.getValue("", "default");

        if (def == null) {
            def = atts.getValue(namespaceURI, "default");
        }

        fixed = atts.getValue("", "fixed");

        if (fixed == null) {
            fixed = atts.getValue(namespaceURI, "fixed");
        }

        // form -- Ignore
        String use = atts.getValue("", "use");

        if (use == null) {
            use = atts.getValue(namespaceURI, "use");
        }

        this.use = findUse(use);
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
     * Convert the 'use' attribute to an int mask
     * </p>
     *
     * @param use
     * @return
     */
    public static int findUse(String use) {
        if ("optional".equalsIgnoreCase(use)) {
            return OPTIONAL;
        }

        if ("prohibited".equalsIgnoreCase(use)) {
            return PROHIBITED;
        }

        if ("required".equalsIgnoreCase(use)) {
            return REQUIRED;
        }

        return -1;
    }

    /**
     * 
     * <p>
     * converts an int mask representing use to the string representation
     * </p>
     *
     * @param use
     * @return
     */
    public static String writeUse(int use) {
        switch (use) {
        case OPTIONAL:
            return "optional";

        case PROHIBITED:
            return "prohibited";

        case REQUIRED:
            return "required";

        default:
            return null;
        }
    }

    /**
     * 
     * <p>
     * Returns the attribute name
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
     * creates a smaller simpler version
     * </p>
     *
     * @param parent
     * @return
     * @throws SAXException
     */
    protected Attribute compress(SchemaHandler parent)
        throws SAXException {
        if (cache != null) {
            return cache;
        }

        AttributeDefault a = new AttributeDefault();
        a.defaulT = def;
        a.fixed = fixed;
        a.id = id;
        a.name = name;
        a.use = use;

        // a.form = form; TODO add form support?
        if (simpleType != null) {
            a.simpleType = simpleType.compress(parent);
        } else {
            if ((ref != null) && !"".equalsIgnoreCase(ref)) {
                Attribute refA = parent.lookUpAttribute(ref);

                if (refA == null) {
                    throw new SAXException("Attribute '" + ref
                        + "' was refered and not found");
                }

                a.simpleType = refA.getSimpleType();
                a.name = refA.getName();
                a.use = use | refA.getUse();

                if ((def == null) || "".equalsIgnoreCase(def)) {
                    a.defaulT = refA.getDefault();
                }

                if ((fixed == null) || "".equalsIgnoreCase(fixed)) {
                    a.fixed = refA.getFixed();
                }
            } else if ((type != null) && (!"".equalsIgnoreCase(type))) {
                // 	look it up --- find it
                a.simpleType = parent.lookUpSimpleType(type);
            }
        }

        cache = a;

        id = type = ref;
        
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
     * Represents an Attribute
     * </p>
     * @see Attribute
     * @author dzwiers
     *
     */
    private class AttributeDefault implements Attribute {
        // file visible to avoid set* methods
        
        String defaulT;
        String fixed;
        String id;
        String name;
        int use;
        boolean form;
        SimpleType simpleType;

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getDefault()
         */
        public String getDefault() {
            return defaulT;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getFixed()
         */
        public String getFixed() {
            return fixed;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#isForm()
         */
        public boolean isForm() {
            return form;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getSimpleType()
         */
        public SimpleType getSimpleType() {
            return simpleType;
        }

        /**
         * 
         * @see org.geotools.xml.xsi.Attribute#getUse()
         */
        public int getUse() {
            return use;
        }
    }
}
