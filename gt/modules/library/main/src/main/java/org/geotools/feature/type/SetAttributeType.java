/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature.type;

import java.util.HashSet;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.filter.Filter;

/**
 * Represents an un-ordered Set of AttributeTypes. For SFS this should not be used. 
 * For GML this is the same as an element whose complexType contains a 
 * All of Attributes.
 * 
 * NOTE: Some of the methods in this class has nasty order-dependant assumptions, please fix.
 * 
 * @author dzwiers
 * @source $URL$
 */
public class SetAttributeType implements AttributeType {

	private final boolean nill;
	private final int min,max;
	private final String name;
	private final AttributeType[] children;
	private Filter restriction;
	
	
	/**
	 * @param copy
	 */
	public SetAttributeType(SetAttributeType copy) {
		nill = copy.isNillable();
		min = copy.getMinOccurs();
		max = copy.getMaxOccurs();
		name = copy.getName();
		children = copy.getAttributeTypes();
		restriction = copy.getRestriction();
	}

	// The field for 'Class type' should be added when GT has moved to java 1.5
    public SetAttributeType(String name, boolean nillable, int min, int max,
    		AttributeType[] children, Filter restriction) {
    	nill = nillable;
    	this.min = min;
    	this.max = max;
    	this.name = name;
    	this.children = children;
    	this.restriction = restriction;
    }

    public SetAttributeType(String name, boolean nillable,
    		AttributeType[] children) {
    	this(name, nillable, 1, 1, children,Filter.EXCLUDE);
    }
    public Filter getRestriction(){return restriction;}
	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#getType()
	 */
	public Class getType() {
//		The field for 'Class type' should be added when GT has moved to java 1.5
		return Set.class;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#isNillable()
	 */
	public boolean isNillable() {
		return nill;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#getMinOccurs()
	 */
	public int getMinOccurs() {
		return min;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#getMaxOccurs()
	 */
	public int getMaxOccurs() {
		return max;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#isGeometry()
	 */
	public boolean isGeometry() {
		return false;
	}

	/**
	 * This method is unstable ... and does not yet check validity well.
	 * TODO make this method robust
	 * 
	 * This method assumes the Objects are in the order of the attributes. 
	 * In the future, this should be implemented with a bubble sort type 
	 * algorithm for testing each object vs each child. Bubble sort is 
	 * recommended as the sample size is typically less than 25 elements, 
	 * and the operation takes O(n*n) time.
	 */
	public Object parse(Object value) throws IllegalArgumentException {
		if(value instanceof Set){
			Object[] in;
			in = ((Set)value).toArray();
			Set out = new HashSet(in.length);
			if(in.length == children.length){
				for(int i=0;i<children.length;i++){
					out.add(children[i].parse(in[i]));
				}
				return out;
			}
			throw new IllegalArgumentException("Expected "+children.length+" Objects, got "+in.length+" Objects");
		}
		throw new IllegalArgumentException("Not an Object []");
	}

	/**
	 * This method is unstable ... and does not yet check validity well.
	 * TODO make this method robust
	 * 
	 * This method assumes the Objects are in the order of the attributes. 
	 * In the future, this should be implemented with a bubble sort type 
	 * algorithm for testing each object vs each child. Bubble sort is 
	 * recommended as the sample size is typically less than 25 elements, 
	 * and the operation takes O(n*n) time.
	 * 
	 * Note that on the Attribute side of the fence ... this is acutally 
	 * an unordered List (a Set of elements, where each element has multiplicity ...)
	 */
	public void validate(Object obj) throws IllegalArgumentException {
		if(obj instanceof Set){
			Object[] in;
			in = ((Set)obj).toArray();
			if(in.length == children.length){
				for(int i=0;i<children.length;i++){
					children[i].validate(in[i]);
				}
				return;
			}
			throw new IllegalArgumentException("Expected "+children.length+" Objects, got "+in.length+" Objects");
		}
		throw new IllegalArgumentException("Not an Object []");
		
	}

	/**
	 * This method is unstable ... and does not yet check validity well.
	 * TODO make this method robust
	 * 
	 * This method assumes the Objects are in the order of the attributes. 
	 * In the future, this should be implemented with a bubble sort type 
	 * algorithm for testing each object vs each child. Bubble sort is 
	 * recommended as the sample size is typically less than 25 elements, 
	 * and the operation takes O(n*n) time.
	 */
	public Object duplicate(Object src) throws IllegalAttributeException {
		if(src instanceof Set){
			Object[] in;
			in = ((Set)src).toArray();
			Set out = new HashSet(in.length);
			if(in.length == children.length){
				for(int i=0;i<children.length;i++){
					out.add( children[i].duplicate(in[i]));
				}
				return out;
			}
			throw new IllegalArgumentException("Expected "+children.length+" Objects, got "+in.length+" Objects");
		}
		throw new IllegalArgumentException("Not an Object []");
	}


	/**
	 * This method is unstable ... and does not yet check validity well.
	 * TODO make this method robust
	 * 
	 * This method assumes the Objects are in the order of the attributes. 
	 * In the future, this should be implemented with a bubble sort type 
	 * algorithm for testing each object vs each child. Bubble sort is 
	 * recommended as the sample size is typically less than 25 elements, 
	 * and the operation takes O(n*n) time.
	 */
	public Object createDefaultValue() {
		Set out = new HashSet(children.length);
		for(int i=0;i<children.length;i++){
			out.add( children[i].createDefaultValue());
		}
		return out;
	}


    /**
     * This is only used twice in the whole geotools code base, and  one of
     * those is for a test, so we're removing it from the interface. If
     * getAttributeType does not have the AttributeType it will just return
     * null.  Gets the number of occurrences of this attribute.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return Number of occurrences.
     */
    public boolean hasAttributeType(String xPath) {
        return getAttributeType(xPath) != null;
    }

    /**
     * Returns the number of attributes at the first 'level' of the schema.
     *
     * @return equivalent value to getAttributeTypes().length
     */
    public int getAttributeCount(){
    	return children.length;
    }
    

    /**
     * Gets the attributeType at this xPath, if the specified attributeType
     * does not exist then null is returned.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return True if attribute exists.
     */
    public AttributeType getAttributeType(String xPath) {        
        AttributeType attType = null;
        int idx = find(xPath);
        if (idx >= 0)
            attType = children[idx];
        return attType;
    }

    /**
     * Find the position of a given AttributeType.
     *
     * @param type The type to search for.
     *
     * @return -1 if not found, a zero-based index if found.
     */
    public int find(AttributeType type) {
        if (type == null) return -1;
        int idx = find(type.getName());
        if (idx < 0 || !children[idx].equals(type))
            idx = -1;
        return idx;
    }
    
    /**
     * Find the position of an AttributeType which matches the given String.
     * @param attName the name to look for
     * @return -1 if not found, zero-based index otherwise
     */
    public int find(String attName) {
    	int i=0;
    	while(i<children.length && !attName.equals(children[i].getName()))i++;
        return i == children.length?-1:i;
    }

    /**
     * Gets the attributeType at the specified index.
     *
     * @param position the position of the attribute to check.
     *
     * @return The attribute type at the specified position.
     */
    public AttributeType getAttributeType(int position) {
        return children[position];
    }

    public AttributeType[] getAttributeTypes() {
        return (AttributeType[]) children.clone();
    }
}
