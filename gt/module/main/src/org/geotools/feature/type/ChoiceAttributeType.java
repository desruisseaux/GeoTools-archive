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
package org.geotools.feature.type;

import org.geotools.feature.AttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.util.Set;



/**
 * This represents a Choice of AttributeTypes. That means, an Attribute of this
 * type may be one of any of this AttributeType's children. This attribute  is
 * not valid for Simple Features, and maps to the Choice construct in GML.
 * 
 * <p>
 * Another way to think about the ChoiceAttributeType is as a Union
 * construction from C - it can store a number of different types of value,
 * but it only stores the one value.  The parse and validate methods try out
 * each of the choices to  see if one of them might work, since all are valid.
 * The order that the child attributeTypes (the choices you can use) are
 * specified is important, because  some objects can parse and validate
 * against several types.  The first choice that returns true is the one that
 * will
 * </p>
 *
 * @author dzwiers
 * @author Chris Holmes, TOPP
 */
public class ChoiceAttributeType implements AttributeType {
    private final boolean nill;
    private final int min;
    private final int max;
    private final String name;
    private final AttributeType[] children;
	private Filter restriction;
	
	
	/**
	 * @param copy
	 */
	protected ChoiceAttributeType(ChoiceAttributeType copy) {
		nill = copy.isNillable();
		min = copy.getMinOccurs();
		max = copy.getMaxOccurs();
		name = copy.getName();
		children = copy.getAttributeTypes();
		restriction = copy.getRestriction();
	}
    // The field for 'Class type' should be added when GT has moved to java 1.5
    protected ChoiceAttributeType(String name, boolean nillable, int min,
        int max, AttributeType[] children, Filter restriction) {
        nill = nillable;
        this.min = min;
        this.max = max;
        this.name = name;
        this.children = children;
    	this.restriction = restriction;
    }

    protected ChoiceAttributeType(String name, boolean nillable,
    		AttributeType[] children) {
    	this(name, nillable, 1, 1, children,Filter.ALL);
    }
    
    public Filter getRestriction(){return restriction;}

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the class of the object.  For a choice this is fairly useless, as
     * it  just returns Object, since we can not tell more than that.
     *
     * @return currently always returns Object.class, since we can't tell more.
     *
     * @task REVISIT: Perhaps we should add a getTypes() method that returns an
     *       array of classes, that would represent the classes that you can
     *       choose from.
     *
     * @see org.geotools.feature.AttributeType#getType()
     */
    public Class getType() {
        //		The field for 'Class type' should be added when GT has moved to java 1.5
        return Object.class;
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
     * Goes through the children, and searches for a parser that works. This
     * method  searches in the order in which the children are specified ...
     * please keep  this in mind when creating these objects if you care about
     * precedence.
     *
     * @param value The object to parse.
     *
     * @return The object parsed into the appropriate form for the Attribute.
     *
     * @throws IllegalArgumentException If the object could not be parsed by
     *         any of the child attribute Types.
     */
    public Object parse(Object value) throws IllegalArgumentException {
        for (int i = 0; i < children.length; i++) {
            try {
                return children[i].parse(value);
            } catch (IllegalArgumentException e) {
                // ignore ... try the next
            }
        }

        throw new IllegalArgumentException("Could not be parsed :(");
    }

    /**
     * Goes through the children, and searches for a validator that works. This
     * method  searches in the order in which the children are specified ...
     * please keep  this in mind when creating these objects if you care about
     * precedence.
     *
     * @param obj The object to validate.
     *
     * @throws IllegalArgumentException If none of the children can validate.
     */
    public void validate(Object obj) throws IllegalArgumentException {
        for (int i = 0; i < children.length; i++) {
            try {
                children[i].validate(obj);

                return; // validates
            } catch (IllegalArgumentException e) {
                // ignore ... try the next
            }
        }

        throw new IllegalArgumentException("Could not be validated :(");
    }

    /**
     * Goes through the children, and searches for a duplicator that works.
     * This method  searches in the order in which the children are specified
     * ... please keep  this in mind when creating these objects if you care
     * about precedence.
     *
     * @param src The object to be duplicated.
     *
     * @return A deep copy of the original object.
     *
     * @throws IllegalAttributeException For any attribute errors.
     * @throws IllegalArgumentException If the object could not be duplicated.
     */
    public Object duplicate(Object src) throws IllegalAttributeException {
        for (int i = 0; i < children.length; i++) {
            try {
                return children[i].duplicate(src);
            } catch (IllegalArgumentException e) {
                // ignore ... try the next
            }
        }

        throw new IllegalArgumentException("Could not be duplicated :(");
    }

    /**
     * Returns the default value for the first child which does not  throw an
     * exception, null otherwise.
     *
     * @return The default value of the first choice that does not throw an
     *         exception.
     */
    public Object createDefaultValue() {
        for (int i = 0; i < children.length; i++) {
            try {
                return children[i].createDefaultValue();
            } catch (IllegalArgumentException e) {
                // ignore ... try the next
            }
        }

        return null;
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
    public int getAttributeCount() {
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

        if (idx >= 0) {
            attType = children[idx];
        }

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
        if (type == null) {
            return -1;
        }

        int idx = find(type.getName());

        if ((idx < 0) || !children[idx].equals(type)) {
            idx = -1;
        }

        return idx;
    }

    /**
     * Find the position of an AttributeType which matches the given String.
     *
     * @param attName the name to look for
     *
     * @return -1 if not found, zero-based index otherwise
     */
    public int find(String attName) {
        int i = 0;

        while ((i < children.length) && !attName.equals(children[i].getName()))
            i++;

        return (i == children.length) ? (-1) : i;
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
