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
package org.geotools.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Used to resolve an XPath expression used on a metadata element
 *
 * Either the Metadata.Element object is returned (if getElement is called)
 * or the dereference value of the element is returned( when getValue is called)
 *
 * The following XPath options are permitted:
 * <ul>
 * <li>Hard reference: FileData/Name <br>
 * the <code>Element</code> Name in the <code>Entity</code>
 * FileData is returned
 * <li>Regular expressions: FileData/\w+ <br>
 * all the <code>Elements</code> in the <code>Entity</code> FileData
 * </ul>
 * @author jeichar
 */
public class XPath {
    String[] terms;

    /**
     * Creates a new XPath object.
     *
     * @param xpath An xpath string that conforms to the specification @linkplain org.geotools.metadata.XPath
     */
    public XPath(String xpath) {
        terms = xpath.split("/");
    }

    /**
     * Access the XPath terms
     *
     * @return the XPath terms
     */
    public String[] getTerms() {
        String[] copy = new String[terms.length];
        System.arraycopy(terms, 0, copy, 0, terms.length);

        return copy;
    }

    /**
     * Set one of the XPath terms.
     * Allows for modifying XPath objects
     *
     * @param index the index of the term to set
     * @param newTerm The new term
     */
    public void setTerm(int index, String newTerm) {
        terms[index] = newTerm;
    }

    private List match(Metadata metadata, Metadata.Entity entity, int index) {
        List elements = entity.getElements();
        List result = new ArrayList();

        String term = terms[index];
        List matches = find(term, elements);

        /*
         * if element == null then the term is not one of the elements therefore
         * the xpath is not valid for the Entity
         */
        if (matches.isEmpty()) {
            return result;
        }

        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            Metadata.Element element = (Metadata.Element) iter.next();

            /*
             * if there are more terms and element is an entity then recurse 1
             * level deeper. if there are more terms but element is not an
             * entity then fail and return null if there are no more terms then
             * we have a match and return element
             */
            if (index < (terms.length - 1)) {
                if (element.isMetadataEntity()) {
                    if (metadata != null) {
                        metadata = (Metadata) metadata.getElement(element);
                    }

                    result.addAll(match(metadata, element.getEntity(), index
                            + 1));
                } else {
                    continue;
                }
            } else {
                if (metadata == null) {
                    result.add(element);
                } else {
                    result.add(metadata.getElement(element));
                }
            }
        }

        return result;
    }

    private List find(String term, List elements) {
        List result = new ArrayList();

        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Metadata.Element element = (Metadata.Element) iter.next();
            String n = element.getName();

            if (n.matches(term)) {
                result.add(element);
            }
        }

        return result;
    }

    /**
     * Returns a List of the Metadata.Elements that satisfy the XPath expression
     * represented by this object
     *
     * @param entity the Metadata.Entity that used as the root of the XPath evaluation
     * @return List of the Metadata.Elements that satisfy the XPath expression
     * represented by this object
     */
    public List getElement(Metadata.Entity entity) {
        return match(null, entity, 0);
    }

    /**
    * Returns a List of Objects which are the values of the Metadata.Element indicated by
    * the XPath expression which this object represents
    *
    * @param metadata The Metadata class that is the root of the evaluation.
    * @return List of Objects which are the values of the Metadata.Element indicated by
    * the XPath expression which this object represents
    * 			Minimal ordering guarantees.  Values from a object are grouped but not the
    * 			order of the elements in a group or the order of the groups
    */
    public List getValue(Metadata metadata) {
        return match(metadata, metadata.getEntity(), 0);
    }

    /**
     * A utility method that creates an XPath, traverses an entity and returns the
     * Element <bold>values</bold> matched.
     *
     * @param xpath XPath Expression
     * @param metadata The metadata hat will be used as the root
     *
     * @return A List of all the Values that the xpath matched
     * 			Minimal ordering guarantees.  Values from a object are grouped but not the
     * 			order of the elements in a group or the order of the groups
     */
    public static List getValue(String xpath, Metadata metadata) {
        XPath path = new XPath(xpath);

        return path.getValue(metadata);
    }

    /**
     * A utility method that creates an XPath, traverses an entity and returns the
     * Elements matched.
     *
     * @param xpath XPath Expression
     * @param entity The entity that will be used as the root
     *
     * @return List of matched Elements
     * 			Minimal ordering guarantees.  Values from a object are grouped but not the
     * 			order of the elements in a group or the order of the groups
     */
    public static List getElement(String xpath, Metadata.Entity entity) {
        XPath path = new XPath(xpath);

        return path.getElement(entity);
    }

    /**
     * A utility method that creates an XPath, traverses an entity and returns the
     * Elements matched.
     *
     * @param xpath XPath Expression
     * @param metadata The metadata hat will be used as the root
     *
     * @return List of matched Elements
     * 			Minimal ordering guarantees.  Values from a object are grouped but not the
     * 			order of the elements in a group or the order of the groups
     */
    public static List getElement(String xpath, Metadata metadata) {
        XPath path = new XPath(xpath);

        return path.getElement(metadata.getEntity());
    }
}
