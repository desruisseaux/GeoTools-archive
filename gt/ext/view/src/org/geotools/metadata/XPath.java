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

    private List match(Metadata metadata, Metadata.Entity entity, int index) {

        List elements = entity.getElements();
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
                    if( metadata != null )
                        metadata = (Metadata) metadata.getElement(element);
                    result.addAll(match(metadata, element.getEntity(), index + 1));
                }else
                    continue;
            }else{
                if( metadata==null )
                    result.add(element);
                else
                    result.add(metadata.getElement(element));
            }
            
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
     */
    public List getValue(Metadata metadata) {
        return match(metadata, metadata.getEntity(), 0);
    }
    
    /**
     * 
     * @param expr
     * @param metadata
     * @return
     */
    public static List getValue(String expr, Metadata metadata) {
        XPath xpath = new XPath(expr);
        return xpath.getValue(metadata);
    }

    public static List getElement(String expr, Metadata.Entity entity) {
        XPath xpath = new XPath(expr);
        return xpath.getElement(entity);
    }

    public static List getElement(String expr, Metadata metadata) {
        XPath xpath = new XPath(expr);
        return xpath.getElement(metadata.getEntity());
    }

    private class Term {
        String term;

        Term(String term) {
            this.term = term;
        }

    }
}