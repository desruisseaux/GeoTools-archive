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
package org.geotools.catalog;

import org.geotools.xml.XPath;
import org.opengis.catalog.MetadataEntity;
import java.util.Iterator;
import java.util.List;

/**
 * An XPath implementation that searches
 * {@link org.opengis.catalog.MetadataEntity}objects and returns a
 * {@link org.opengis.catalog.MetadataEntity.Element}Object when find is called
 * and a {@link org.opengis.catalog.MetadataEntity}when value is called.
 * 
 * When find or nodePath operations are used
 * {@link org.opengis.catalog.MetadataEntity},
 * {@link org.opengis.catalog.MetadataEntity.Element}and
 * {@link org.opengis.catalog.MetadataEntity.EntityType}objects are all legal.
 * However, only {@link org.opengis.catalog.MetadataEntity}objects are legal
 * when calling the value method.
 * 
 * 
 * 
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public class MetadataXPath extends XPath {
    /**
     * Creates a new MetadataXPath object.
     * 
     * @param xpath
     *            A xpath in string representation. {@see XPath}for a
     *            description of how to write XPath expressions
     */
    public MetadataXPath(String xpath) {
        super(xpath);
    }

    /**
     * Returns the name of the Node.
     * 
     * @param o
     *            {@link org.opengis.catalog.MetadataEntity.Element}to find a
     *            name for
     * 
     * @return the name of the
     *         {@link org.opengis.catalog.MetadataEntity.Element}
     */
    protected String getNodeName(Object o) {
        return ((MetadataEntity.Element) o).getName();
    }

    /**
     * The first element in the list must be a
     * {@link org.opengis.catalog.MetadataEntity}and each following element
     * must be a {@link org.opengis.catalog.MetadataEntity.Element}
     * 
     * The real value in the Metadata element indicated by the path is returned
     * 
     * @param path
     *            The first element in the list must be a
     *            {@link org.opengis.catalog.MetadataEntity}and each following
     *            element must be a
     *            {@link org.opengis.catalog.MetadataEntity.Element}
     * 
     * @return The real value in the Metadata element indicated by the path
     */
    protected Object solve(List path) {
        Iterator i = path.iterator();
        MetadataEntity current = (MetadataEntity) i.next();

        while (i.hasNext()) {
            MetadataEntity.Element elem = (MetadataEntity.Element) i.next();

            if (!i.hasNext()) {
                return current.getElement(elem);
            }

            current = (MetadataEntity) current.getElement(elem);
        }

        return null;
    }

    /**
     * Returns a iterator that returns all the
     * {@link org.opengis.catalog.MetadataEntity.Element}children of object o
     * 
     * @param o
     *            A {@link org.opengis.catalog.MetadataEntity}or
     *            {@link org.opengis.catalog.MetadataEntity.Element}or a
     *            {@link org.opengis.catalog.MetadataEntity.EntityType}
     * 
     * @return a iterator that returns all the
     *         {@link org.opengis.catalog.MetadataEntity.Element}children of
     *         object o
     */
    protected Iterator getChildren(Object o) {
        MetadataEntity.EntityType entity = null;

        if (o instanceof MetadataEntity) {
            entity = ((MetadataEntity) o).getEntityType();
        }

        if (o instanceof MetadataEntity.EntityType) {
            entity = (MetadataEntity.EntityType) o;
        }

        if (o instanceof MetadataEntity.Element) {
            entity = ((MetadataEntity.Element) o).getEntityType();
        }

        if (entity == null) {
            return new NullIterator();
        }

        return entity.getElements().iterator();
    }

    /**
     * @see org.geotools.xml.XPath#isLegalNode(java.lang.Object)
     */
    protected boolean isLegalNode(Object o, int operation, boolean isList) {
        if (operation == OP_VALUE)
            return (o instanceof MetadataEntity)?true:false;
        if (isList)
            return o instanceof MetadataEntity.Element ? true : false;

        if (o instanceof MetadataEntity
                || o instanceof MetadataEntity.EntityType
                || o instanceof MetadataEntity.Element) {
            return true;
        }

        return false;
    }
}