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
package org.geotools.xml.wfs;

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;
import java.net.URI;


/**
 * <p>
 * This class represents a hard coded, java interpreted version  of the WFS
 * WFS-basic schema. Instances of this class should be prefered for use over a
 * parsed instance as this class will create real instances  for elements
 * who's types correspond to types defined in this schema.
 * </p>
 *
 * @author Norman Barker www.comsine.com
 */
public class WFSSchema implements Schema {
    /** DOCUMENT ME!  */
    public static String NAMESPACE = "http://www.opengis.net/wfs";

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getAttributeGroups()
     */
    public AttributeGroup[] getAttributeGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getAttributes()
     */
    public Attribute[] getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getBlockDefault()
     */
    public int getBlockDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getComplexTypes()
     */
    public ComplexType[] getComplexTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getElements()
     */
    public Element[] getElements() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getFinalDefault()
     */
    public int getFinalDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getGroups()
     */
    public Group[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getId()
     */
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getImports()
     */
    public Schema[] getImports() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getSimpleTypes()
     */
    public SimpleType[] getSimpleTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getTargetNamespace()
     */
    public String getTargetNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getURIs()
     */
    public URI getURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#getVersion()
     */
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#includesURI(java.net.URI)
     */
    public boolean includesURI(URI uri) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#isAttributeFormDefault()
     */
    public boolean isAttributeFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.Schema#isElementFormDefault()
     */
    public boolean isElementFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getPrefix()
     */
    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>
     * This abstract class represents some default and constant values
     * associated with a GML complexType.
     * </p>
     *
     * @see ComplexType
     */
    static abstract class WFSComplexType implements ComplexType {
        /**
         * @see schema.ComplexType#getBlock()
         */
        public int getBlock() {
            return Schema.NONE;
        }

        /**
         * @see schema.ComplexType#getFinal()
         */
        public int getFinal() {
            return Schema.NONE;
        }

        /**
         * @see schema.ComplexType#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            return false;
        }

        /**
         * @see schema.ComplexType#getNamespace()
         */
        public String getNamespace() {
            return WFSSchema.NAMESPACE;
        }

        /*
         * included here to deal generically with a GML complexType ...
         * part of the singleton pattern.
         */
        static WFSComplexType getInstance() {
            return null;
        }

        /**
         * @see schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            return false;
        }

        /**
         * @see schema.ComplexType#getParent()
         */
        public Type getParent() {
            return null;
        }
    }

    /**
     * <p>
     * Adds some common information and functionality to a base element to  be
     * used by the WFSSchema. The remaining data will be configured upon
     * creation.
     * </p>
     *
     * @see Element
     */
    static class WFSElement implements Element {
        // default visibily to remove the set* methods ... this class is 
        // only package visible
        boolean abstracT = false;
        int max;
        int min;
        String name;
        Type type;
        Element substitutionGroup;

        /*
         * Should never be called
         */
        private WFSElement() {
        }

        /**
         * Configures the Element for this particular WFS instance.  The
         * following params match schema definition attributes found in an
         * element declaration. Those missing have been hard coded for the gml
         * Schema.
         *
         * @param name
         * @param type
         * @param min
         * @param max
         * @param abstracT
         * @param substitutionGroup
         */
        public WFSElement(String name, Type type, int min, int max,
            boolean abstracT, Element substitutionGroup) {
            this.abstracT = abstracT;
            this.max = max;
            this.min = min;
            this.name = name;
            this.type = type;
            this.substitutionGroup = substitutionGroup;
        }

        /**
         * Creates a clone using the new min/max occurences.
         *
         * @param element
         * @param min
         * @param max
         */
        public WFSElement(WFSElement element, int min, int max) {
            this.abstracT = element.isAbstract();
            this.max = max;
            this.min = min;
            this.name = element.getName();
            this.type = element.getType();
            this.substitutionGroup = element.getSubstitutionGroup();
        }

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            if (this.name != null) {
                if (this.name.equals(name)) {
                    return this;
                }
            }

            return null;
        }

        /**
         * @see org.geotools.xml.xsi.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return ELEMENT;
        }

        /**
         * @see schema.Element#isAbstract()
         */
        public boolean isAbstract() {
            return abstracT;
        }

        /**
         * @see schema.Element#getBlock()
         */
        public int getBlock() {
            return Schema.NONE;
        }

        /**
         * @see schema.Element#getDefault()
         */
        public String getDefault() {
            return null;
        }

        /**
         * @see schema.Element#getFinal()
         */
        public int getFinal() {
            return Schema.NONE;
        }

        /**
         * @see schema.Element#getFixed()
         */
        public String getFixed() {
            return null;
        }

        /**
         * @see schema.Element#isForm()
         */
        public boolean isForm() {
            return false;
        }

        /**
         * @see schema.Element#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see schema.Element#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return max;
        }

        /**
         * @see schema.Element#getMinOccurs()
         */
        public int getMinOccurs() {
            return min;
        }

        /**
         * @see schema.Element#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see schema.Element#isNillable()
         */
        public boolean isNillable() {
            return false;
        }

        /**
         * @see schema.Element#getSubstitutionGroup()
         */
        public Element getSubstitutionGroup() {
            return substitutionGroup;
        }

        /**
         * @see schema.Element#getType()
         */
        public Type getType() {
            return type;
        }

        /**
         * @see schema.Element#getNamespace()
         */
        public String getNamespace() {
            return WFSSchema.NAMESPACE;
        }
    }

    /**
     * <p>
     * An instance of this class represents a WFS attribute. This
     * implementation contains some constant data pertinent to the WFS Schema,
     * and some configurable data depending on the WFS attribute being
     * represented.
     * </p>
     *
     * @author Norman Barker
     *
     * @see Attribute
     */
    static class WFSAttribute implements Attribute {
        // package visible class variable, used to avoid set* methods
        String name;
        String def = null;
        SimpleType simpleType;
        int use = Attribute.OPTIONAL;

        /*
         * Should never be called
         */
        private WFSAttribute() {
        }

        /**
         * Creates a GML attribute based on the name and type provided.
         *
         * @param name
         * @param simpleType
         */
        public WFSAttribute(String name, SimpleType simpleType) {
            this.name = name;
            this.simpleType = simpleType;
        }

        /**
         * Creates a GML attribute based on the name, use and type provided.
         *
         * @param name
         * @param simpleType
         * @param use
         */
        public WFSAttribute(String name, SimpleType simpleType, int use) {
            this.name = name;
            this.simpleType = simpleType;
            this.use = use;
        }

        /**
         * Creates a GML attribute based on the name, use, default  and type
         * provided.
         *
         * @param name
         * @param simpleType
         * @param use
         * @param def
         */
        public WFSAttribute(String name, SimpleType simpleType, int use,
            String def) {
            this.name = name;
            this.simpleType = simpleType;
            this.use = use;
            this.def = def;
        }

        /**
         * @see schema.Attribute#getNameSpace()
         */
        public String getNameSpace() {
            return WFSSchema.NAMESPACE;
        }

        /**
         * @see schema.Attribute#getDefault()
         */
        public String getDefault() {
            return def;
        }

        /**
         * @see schema.Attribute#getFixed()
         */
        public String getFixed() {
            return null;
        }

        /**
         * @see schema.Attribute#isForm()
         */
        public boolean isForm() {
            return false;
        }

        /**
         * @see schema.Attribute#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see schema.Attribute#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see schema.Attribute#getUse()
         */
        public int getUse() {
            return use;
        }

        /**
         * @see schema.Attribute#getSimpleType()
         */
        public SimpleType getSimpleType() {
            return simpleType;
        }
    }
}
