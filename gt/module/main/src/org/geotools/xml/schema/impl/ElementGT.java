/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.xml.schema.impl;

import java.net.URI;

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.Type;

/**
 * Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example Use:<pre><code>
 * ElementGT x = new ElementGT( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author dzwiers
 * @since 0.3
 */
public class ElementGT implements Element {

    private Type type = null;
    private boolean _abstract,nillable;
    private Element sub = null;
    private URI ns = null;
    private String name = null,id = null;
    private int min=1,max=1;
    
    /**
     * Construct <code>ElementGT</code>.
     *
     * @param id
     * @param name
     * @param namespace
     * @param type
     * @param min
     * @param max
     * @param nillable
     * @param substitutionGroup
     * @param _abstract
     */
    public ElementGT(String id,String name, URI namespace, Type type, int min, int max, boolean nillable, Element substitutionGroup, boolean _abstract){
        this.type = type;
        this._abstract = _abstract;
        this.nillable = nillable;
        this.sub = substitutionGroup;
        this.ns = namespace;
        this.name = name;
        this.id = id;
        this.min = min;
        this.max = max;
    }
    
    /**
     * TODO summary sentence for isAbstract ...
     * 
     * @see org.geotools.xml.schema.Element#isAbstract()
     * @return
     */
    public boolean isAbstract() {
        return _abstract;
    }

    /**
     * TODO summary sentence for getBlock ...
     * 
     * @see org.geotools.xml.schema.Element#getBlock()
     * @return
     */
    public int getBlock() {
        return Schema.NONE;
    }

    /**
     * TODO summary sentence for getDefault ...
     * 
     * @see org.geotools.xml.schema.Element#getDefault()
     * @return
     */
    public String getDefault() {
        return null;
    }

    /**
     * TODO summary sentence for getFinal ...
     * 
     * @see org.geotools.xml.schema.Element#getFinal()
     * @return
     */
    public int getFinal() {
        return Schema.NONE;
    }

    /**
     * TODO summary sentence for getFixed ...
     * 
     * @see org.geotools.xml.schema.Element#getFixed()
     * @return
     */
    public String getFixed() {
        return null;
    }

    /**
     * TODO summary sentence for isForm ...
     * 
     * @see org.geotools.xml.schema.Element#isForm()
     * @return
     */
    public boolean isForm() {
        return false;
    }

    /**
     * TODO summary sentence for getId ...
     * 
     * @see org.geotools.xml.schema.Element#getId()
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * TODO summary sentence for getMaxOccurs ...
     * 
     * @see org.geotools.xml.schema.ElementGrouping#getMaxOccurs()
     * @return
     */
    public int getMaxOccurs() {
        return max;
    }

    /**
     * TODO summary sentence for getMinOccurs ...
     * 
     * @see org.geotools.xml.schema.ElementGrouping#getMinOccurs()
     * @return
     */
    public int getMinOccurs() {
        return min;
    }

    /**
     * TODO summary sentence for getName ...
     * 
     * @see org.geotools.xml.schema.Element#getName()
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * TODO summary sentence for getNamespace ...
     * 
     * @see org.geotools.xml.schema.Element#getNamespace()
     * @return
     */
    public URI getNamespace() {
        return ns;
    }

    /**
     * TODO summary sentence for isNillable ...
     * 
     * @see org.geotools.xml.schema.Element#isNillable()
     * @return
     */
    public boolean isNillable() {
        return nillable;
    }

    /**
     * TODO summary sentence for getSubstitutionGroup ...
     * 
     * @see org.geotools.xml.schema.Element#getSubstitutionGroup()
     * @return
     */
    public Element getSubstitutionGroup() {
        return sub;
    }

    /**
     * TODO summary sentence for getType ...
     * 
     * @see org.geotools.xml.schema.Element#getType()
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * TODO summary sentence for getGrouping ...
     * 
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     * @return
     */
    public int getGrouping() {
        return Element.ELEMENT;
    }

    /**
     * TODO summary sentence for findChildElement ...
     * 
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     * @param name
     * @return
     */
    public Element findChildElement( String name ) {
        return (getName()!=null && getName().equals(name))?this:null;
    }
}
