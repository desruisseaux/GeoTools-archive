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
 */
package org.geotools.xml.handlers;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.DocumentFactory;
import org.geotools.xml.XMLElementHandler;
import org.geotools.xml.schema.All;
import org.geotools.xml.schema.Any;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Sequence;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * <p>
 * This class is intended to handle parsing an xml element from an instance
 * document for elements who's type is both known and complex. This handler is
 * used within the XMLSAXHandler to handle sax events generated by the  SAX
 * parser.
 * </p>
 *
 * @author dzwiers www.refractions.net
 *
 * @see ComplexType
 */
public class ComplexElementHandler extends XMLElementHandler {
    
    /** <code>serialVersionUID</code> field */
    private static final long serialVersionUID = ComplexElementHandler.class.hashCode();
    
    private ComplexType type; // saves casting all over
    private Element elem;
    private String text;
    private Attributes attr;
    private List elements;
    private Object value = null;
    private ElementHandlerFactory ehf;

    /**
     * Creates a new ComplexElementHandler object for Element  elem using
     * ElementHandlerFactory ehf.
     *
     * @param ehf ElementHandlerFactory
     * @param elem Element
     *
     * @throws SAXException
     */
    public ComplexElementHandler(ElementHandlerFactory ehf, Element elem)
        throws SAXException {
        this.ehf = ehf;

        if (elem == null) {
            logger.warning("ComplexType provided was null");
            throw new SAXException(new NullPointerException(
                    "ComplexType provided was null"));
        }

        this.elem = elem;

        try {
            type = (ComplexType) elem.getType();
        } catch (ClassCastException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getElement()
     */
    public Element getElement() {
        return elem;
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#characters(java.lang.String)
     */
    public void characters(String text) throws SAXException {
        if (type.isMixed()) {
            if (this.text != null) {
                this.text = this.text.concat(text);
            } else {
                this.text = text;
            }
        } else {
            if (!"".equals(text.trim())) {
                if (type.getName() == null) {
                    throw new SAXException(
                        "This type may not have mixed content");
                }

                throw new SAXException("The " + type.getName()
                    + " type may not have mixed content");
            }
        }
    }

    /**
     * @param namespaceURI
     * @param localName
     * @param hints
     * @throws SAXException
     * @throws OperationNotSupportedException
     */
    public void endElement(URI namespaceURI, String localName, Map hints)
        throws OperationNotSupportedException, SAXException {
        text = (text == null) ? null : text.trim();
        
        if(hints == null){
            hints = new HashMap();
            hints.put(ElementHandlerFactory.KEY,ehf);
        }else{
            if(!hints.containsKey(ElementHandlerFactory.KEY))
                hints.put(ElementHandlerFactory.KEY,ehf);
        }

        if (elements == null) {
            if (type != null) {
                ElementValue[] vals;
				if(type.isMixed()){
					vals = new ElementValue[1];
                	vals[0] = new DefaultElementValue(null, text); // null is ok as
                			// this represents the mixed content
				}else{
					vals = new ElementValue[0];
				}
                value = type.getValue(elem, vals, attr, hints);
            } else {
                value = text;
            }

            return;
        }

        // validate the complex element ... throws an exception when it's been bad
        boolean validate = hints == null || !hints.containsKey(DocumentFactory.VALIDATION_HINT) ||
			hints.get(DocumentFactory.VALIDATION_HINT)==null || !(hints.get(DocumentFactory.VALIDATION_HINT) instanceof Boolean) ||
			((Boolean)hints.get(DocumentFactory.VALIDATION_HINT)).booleanValue();
        if(validate)
        	validateElementOrder();
        
        ElementValue[] vals = new ElementValue[elements.size()
            + (type.isMixed() ? 1 : 0)];

        for (int i = 0; i < elements.size(); i++) {
            XMLElementHandler xeh = (XMLElementHandler) elements.get(i);
            vals[i] = new DefaultElementValue(xeh.getElement(), xeh.getValue());
        }

        if (type.isMixed()) {
            vals[vals.length - 1] = new DefaultElementValue(null, text);

            // null is ok as this represents the mixed content
        }

        value = type.getValue(elem, vals, attr, hints);

        // clean memory
        attr = null;
        elements = null;
        text = null;
    }

    /*
     * This starts off the fun or checking element order for complex types
     * (note mixtures of All, Any, Choice, Element, Group, Sequence).
     *
     */
    private void validateElementOrder() throws SAXException {
        if ((elements == null) || (elements.size() == 0)) {
            // TODO ensure we have enough elements
            return;
        }

        int i = 0;
        int count =0;
        int[] i2 = new int[2];
        int cache = 0; // old pos.
        i2[1]=1;
        while(i<elements.size() && i2[1] == 1){
        	i2[0] = i;
        	i2[1] = 0;
            cache = i2[0];
            i2 = valid(type.getChild(), i);
            if( i2[1] == 0 && i == i2[0] ){
            	// done running
            	if (count < type.getChild().getMinOccurs()) {
            		throw new SAXException("Too few elements declared for "
                        + type.getName() + "("+elem.getName()+")");
                }
            }else{
                if(cache == i2[0]){
                    // we have not progressed .. progress us
                    i = i2[0]+1;
                }else{
                    i = i2[0];
                }
            	count++;
            }
        }
        if(count > type.getChild().getMaxOccurs()){
    		throw new SAXException("Too many elements declared for "
                    + type.getName());
        }

        if (i != elements.size()) {
            throw new SAXException("Invalid element order according: "
                + type.getName() + "[" + i + "]");
        }
    }

    /*
     * Generic validation method which simulates recursion, and avoids casting :)
     * The index is the starting index in the list of elements, for the particular
     * ElementGrouping. The last index matched is returned.
     */
    private int[] valid(ElementGrouping eg, int index) throws SAXException {
        if (eg == null) {
            return new int[]{index,1};
        }

        switch (eg.getGrouping()) {
        case ElementGrouping.SEQUENCE:
            int[] tmp = valid((Sequence) eg, index);
                        return tmp;

        case ElementGrouping.ALL:
            return valid((All) eg, index);

        case ElementGrouping.ANY:
            return valid((Any) eg, index);

        case ElementGrouping.CHOICE:
            return valid((Choice) eg, index);

        case ElementGrouping.GROUP:
            return valid((Group) eg, index);

        case ElementGrouping.ELEMENT:
            tmp = valid((Element) eg, index);
            return tmp;
        }

        return new int[]{index,1};
    }

    /*
     * Validates an All tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(All all, int index) {
        Element[] elems = all.getElements();
        int[] r = new int[elems.length];

        for (int i = 0; i < r.length; i++)
            r[i] = 0;

        boolean c = true;
        int head = index;
        while (c) {
            c = false;

            for (int i = 0; i < elems.length; i++) {
                if (elems[i].getType().getName().equalsIgnoreCase(((XMLElementHandler) elements
                            .get(head)).getName())) {
                    r[i]++;
                    head++;
                    i = elems.length;
                    c = true;
                }
            }
        }

        for (int i = 0; i < r.length; i++) {
            if ((r[i] < elems[i].getMinOccurs())
                    || (r[i] > elems[i].getMaxOccurs())) {
                return new int[]{index,0};
            }
        }

        return new int[]{head,1};
    }

    /*
     * Validates an Any tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(Any any, int index) {
        if (any.getNamespace().equals(((XMLElementHandler) elements.get(index)).getElement()
                                           .getType().getNamespace())) {
            return new int[]{index+1,1};
        }

        return new int[]{index,1};
    }

    /*
     * Validates an Choice tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(Choice choice, int index) throws SAXException {
        ElementGrouping[] eg = choice.getChildren();

        if (eg == null) {
            return new int[]{index,1};
        }
        
        int i = 0; // choice child index;

        int end = index;
        int t = index;
        int count = 0;
        int t2[] = null;
        while(i<eg.length && end<elements.size()){
        	t2 = valid(eg[i], t);
        	if(t2[1] == 0 && t2[0] == t){// nothing, next
    			// move along
    			if(t2[0]>end && count>=eg[i].getMinOccurs() && count<=eg[i].getMaxOccurs())
    				end = t2[0];
    			count = 0;
    			i++;
    			t = index;
        	}else{
        		if(count==eg[i].getMaxOccurs()){
        			// move along
        			if(t2[0]>end && count>=eg[i].getMinOccurs())
        				end = t2[0];
        			count = 0;
        			i++;
        			t = index;
        		}else{
        			t = t2[0];
        			if(t == elements.size()){
        				end = t;
        			}
        			count++;
        		}
    		}
        }

        return new int[]{end,end==index?0:1};
    }

    /*
     * Validates an Group tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(Group group, int index) throws SAXException {
        if (group.getChild() == null) {
            return new int[]{index,1};
        }

        return valid(group.getChild(), index);
    }

    /*
     * Validates an Element tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(Element element, int index) {

    	// does this element equate to the index in the doc?

        int[] r = null;
        
        XMLElementHandler indexHandler = null;
        if(index<elements.size()){
            indexHandler = ((XMLElementHandler) elements.get(index));
        }else{
            // not found :)
            return new int[]{index,0};
        }
        
        if(r ==null && (indexHandler == null || indexHandler.getElement() == null))
        	return new int[]{index,0};
        
        if(r == null && indexHandler.getElement() == element)
        	r =  new int[]{index+1,1};
        
        if(r == null && element.getName()==null)
        	return new int[]{index,0};
        
        if(r == null && (element.getName()!=null && element.getName().equalsIgnoreCase(indexHandler.getName())))
        	r =  new int[]{index+1,1};
        if(r == null && element.getName()!=null){
        Element e = indexHandler.getElement();
        while(r == null && e != null){
        	if(element.getName().equalsIgnoreCase(e.getName())){
        		r =  new int[]{index+1,1};
        	}
        	e = e.getSubstitutionGroup();
        }
        }
        
        if(r == null){
            r = new int[]{index,0};
        }
        return r;
    }

    /*
     * Validates a Sequence tag
     * @see valid(ElementGrouping)
     */
    private int[] valid(Sequence seq, int index) throws SAXException {
        ElementGrouping[] eg = seq.getChildren();

        if (eg == null) {
            return new int[]{index,1};
        }

        int tIndex = index; // top of element matching list
        int t = 0; // top of child list
        
        int count = 0; // used for n-ary at a single spot
        int i2[] = new int[2];
        while(t<eg.length && tIndex<elements.size()){
        	i2 = valid(eg[t],tIndex); // new top element
        	if(i2[1]==1){ // they matched
        	    if(tIndex==i2[0]){
        	        // didn't more ahead ...
        	        t++; // force next spot
        	        count = 0; // reset
        	    }else{
        	        count ++;
        	        if(count<=eg[t].getMaxOccurs()){
        	            tIndex = i2[0]; // store index
        	        }else{
        	            // error, so redo
        	            if(eg[t].getMinOccurs()>count){
        	                // not good
        	                return new int[]{index,0}; // not whole sequence
        	            }
        	            t++;
        	            count=0; // next defined type
        	        }
        	    }
        	}else{
                // didn't match
                
    			// move along and retest that spot
    			if(eg[t].getMinOccurs()>count){
    				// not good
					return new int[]{index,0}; // not whole sequence
    			}
    			t++;
    			count=0; // next defined type
        	}
        }
        return new int[]{tIndex,1};
    }

    /**
     * 
     * TODO summary sentence for startElement ...
     * 
     * @see org.geotools.xml.XMLElementHandler#startElement(java.net.URI, java.lang.String, org.xml.sax.Attributes)
     * @param namespaceURI
     * @param localName
     * @param attr
     */
    public void startElement(URI namespaceURI, String localName, Attributes attr) {
        this.attr = new AttributesImpl(attr);
    }

    /**
     * 
     * TODO summary sentence for getHandler ...
     * 
     * @see org.geotools.xml.XMLElementHandler#getHandler(java.net.URI, java.lang.String, java.util.Map)
     * @param namespaceURI
     * @param localName
     * @param hints
     * @return XMLElementHandler
     * @throws SAXException
     */
    public XMLElementHandler getHandler(URI namespaceURI, String localName,
        Map hints) throws SAXException {
        if (elements == null) {
            elements = new LinkedList();
        }

        logger.finest("Starting search for element handler " + localName
            + " :: " + namespaceURI);

        Element e = type.findChildElement(localName);
        if (e != null && namespaceURI.equals(e.getNamespace())){
            XMLElementHandler r = ehf.createElementHandler(e);

            if (type.cache(r.getElement(), hints)) {
                elements.add(r);
            }

            return r;
        }

        logger.finest("Checking the document schemas");

        XMLElementHandler r = ehf.createElementHandler(namespaceURI, localName);

        if (r != null) {
            if (type.cache(r.getElement(), hints)) {
                elements.add(r);
            }

            return r;
        }

        // validation?
        if(hints != null && hints.containsKey(DocumentFactory.VALIDATION_HINT)){
            Boolean valid = (Boolean)hints.get(DocumentFactory.VALIDATION_HINT);
            if(valid != null && !valid.booleanValue()){
                return new IgnoreHandler();
            }
        }
        
        throw new SAXException("Could not find element handler for "
            + namespaceURI + " : " + localName + " as a child of "
            + type.getName() + ".");
    }

    /**
     * 
     * TODO summary sentence for getValue ...
     * 
     * @see org.geotools.xml.XMLElementHandler#getValue()
     * @return Object
     */
    public Object getValue() {
        // endElement sets the value
        return value;
    }

    /**
     * @see org.geotools.xml.XMLElementHandler#getName()
     */
    public String getName() {
        return elem.getName();
    }

    /**
     * <p>
     * Default Implementation used to pass values to type instances
     * </p>
     *
     * @author dzwiers
     *
     * @see ElementValue
     */
    private static class DefaultElementValue implements ElementValue {
        Element t;
        Object value;

        /**
         * Stores the two values for use within the specified type
         *
         * @param t Element
         * @param o String
         */
        public DefaultElementValue(Element t, Object o) {
            this.t = t;
            value = o;
        }

        /**
         * 
         * TODO summary sentence for getElement ...
         * 
         * @see org.geotools.xml.schema.ElementValue#getElement()
         * @return Element
         */
        public Element getElement() {
            return t;
        }

        /**
         * 
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.ElementValue#getValue()
         * @return Object
         */
        public Object getValue() {
            return value;
        }
    }
}
