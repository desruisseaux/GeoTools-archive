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
package org.geotools.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO type description
 * 
 * @author jeichar
 *  
 */
public class XPath {

    String[] terms;

    public XPath(String xpath) {
        terms = xpath.split("/");
    }

    public String[] getTerms() {
        String[] copy = new String[terms.length];
        System.arraycopy(terms, 0, copy, 0, terms.length);
        return copy;
    }

    public void setTerm(int index, String newTerm) {
        terms[index] = newTerm;
    }

    private List match(Metadata metadata, int index) {

        List elements = metadata.getEntity().getElements();
        List result = new ArrayList();

        String term = terms[index];
        List matches = find(term, elements);

        /*
         * if element == null then the term is not one of the elements therefore
         * the xpath is not valid for the Entity
         */
        if (matches.isEmpty())
            return result;

        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            Metadata.Element element = (Metadata.Element) iter.next();

            /*
             * if there are more terms and element is an entity then recurse 1
             * level deeper. if there are more terms but element is not an
             * entity then fail and return null if there are no more terms then
             * we have a match and return element
             */
            if (index < terms.length - 1){
                if (element.isMetadataEntity()) {
                    metadata = (Metadata) metadata.getElement(element);
                    result.addAll(match(metadata, index + 1));
                }else
                    return result;
            }else
                result.add(element);
            
        }
        return result;
    }

    private List find(String term, List elements) {
        List result = new ArrayList();

        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Metadata.Element element = (Metadata.Element) iter.next();
            String n=element.getName();
            if (n.matches(term))
                result.add(element);
        }

        return result;
    }

    public List match(Metadata metadata) {
        return match(metadata, 0);
    }

    public static List match(String expr, Metadata metadata) {
        XPath xpath = new XPath(expr);
        return xpath.match(metadata);
    }

    private class Term {
        String term;

        Term(String term) {
            this.term = term;
        }

    }
}