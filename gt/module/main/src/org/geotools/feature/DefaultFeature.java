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
package org.geotools.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.util.Cloneable;

// J2SE dependencies
import java.rmi.server.UID;
import java.util.List;


/**
 * Provides a more efficient feature representation for the flat and complex
 * features. This implementation actually not only enforces feature type
 * synchronization, it also enforces the use of its accessor methods to change
 * the state of internal object representations.  In this case, the
 * implementation is  trivial, since all allowed attribute objects (from the
 * feature type) are immutable.
 *
 * @author Chris Holmes, TOPP <br>
 * @author Rob Hranac, TOPP
 * @author Ian Schneider ARS-USDA
 *
 * @task TODO: look at synchronization (or locks as IanS thinks)
 * @task REVISIT: Right now we always validate, which means whenever a  Feature
 *       is created or a new value set then an operation must be performed.
 *       One thing we should consider is to allow a Feature to turn off its
 *       its validation - which would likely improve performance with large
 *       datasets.  If you are reading from a database, with a FeatureType you
 *       got from the database, it is probably a reasonable assumption that
 *       the Features  contained in it will properly validate.  I am not sure
 *       if this should with a switch in DefaultFeature, or perhaps an
 *       interface that says if it is validating or not, or maybe even an
 *       option in Feature.  But it would be a nice option to have - if
 *       datastore implementors could at least create their features without
 *       validating (though probably should return Features that will check
 *       for validity if someone else tries to change them).
 */
public class DefaultFeature implements SimpleFeature, Cloneable {
    /** The unique id of this feature */
    protected String featureId;

    /** Flat feature type schema for this feature. */
    private final DefaultFeatureType schema;

    /** Attributes for the feature. */
    private Object[] attributes;

    /** The bounds of this feature. */
    private Envelope bounds;

    /** The collection that this Feature is a member of */
    private FeatureCollection parent;

    /**
     * Creates a new instance of flat feature, which must take a flat feature
     * type schema and all attributes as arguments.
     *
     * @param schema Feature type schema for this flat feature.
     * @param attributes Initial attributes for this feature.
     * @param featureID The unique ID for this feature.
     *
     * @throws IllegalAttributeException Attribtues do not conform to feature
     *         type schema.
     * @throws NullPointerException if schema is null.
     */
    protected DefaultFeature(DefaultFeatureType schema, Object[] attributes,
        String featureID)
        throws IllegalAttributeException, NullPointerException {
        if (schema == null) {
            throw new NullPointerException("schema");
        }

        this.schema = schema;
        this.featureId = (featureID == null) ? defaultID() : featureID;
        this.attributes = new Object[schema.getAttributeCount()];

        setAttributes(attributes);
    }

    /**
     * Creates a new instance of flat feature, which must take a flat feature
     * type schema and all attributes as arguments.
     *
     * @param schema Feature type schema for this flat feature.
     * @param attributes Initial attributes for this feature.
     *
     * @throws IllegalAttributeException Attribtues do not conform to feature
     *         type schema.
     *
     * @task REVISIT: should we allow this?  Force users to explicitly set
     *       featureID to null?
     */
    protected DefaultFeature(DefaultFeatureType schema, Object[] attributes)
        throws IllegalAttributeException {
        this(schema, attributes, null);
    }

    /**
     * Creates an ID from a hashcode.
     *
     * @return an id for the feature.
     */
    String defaultID() {
        return "fid-" + (new UID()).toString();
    }

    /**
     * Gets a reference to the feature type schema for this feature.
     *
     * @return A copy of this feature's metadata in the form of a feature type
     *         schema.
     */
    public FeatureType getFeatureType() {
        return schema;
    }

    /**
     * Gets the unique indentification string of this Feature.
     *
     * @return The unique id.
     */
    public String getID() {
        return featureId;
    }

    /**
     * Copy all the attributes of this Feature into the given array. If the
     * argument array is null, a new one will be created. Gets all attributes
     * from this feature, returned as a complex object array.  This array
     * comes with no metadata, so to interpret this  collection the caller
     * class should ask for the schema as well.
     *
     * @param array The array to copy the attributes into.
     *
     * @return The array passed in, or a new one if null.
     */
    public Object[] getAttributes(Object[] array) {
        Object[] retArray;

        if (array == null) {
            retArray = new Object[attributes.length];
        } else {
            retArray = array;
        }

        System.arraycopy(attributes, 0, retArray, 0, attributes.length);

        return retArray;
    }

    /**
     * Gets an attribute for this feature at the location specified by xPath.
     *
     * @param xPath XPath representation of attribute location.
     *
     * @return Attribute.
     */
    public Object getAttribute(String xPath) {
        int idx = schema.find(xPath);

        if (idx == -1) {
            return null;
        }

        return attributes[idx];
    }

    /**
     * Gets an attribute by the given zero-based index.
     *
     * @param index the position of the attribute to retrieve.
     *
     * @return The attribute at the given index.
     */
    public Object getAttribute(int index) {
        return attributes[index];
    }

    /**
     * Sets the attribute at position to val.
     *
     * @param position the index of the attribute to set.
     * @param val the new value to give the attribute at position.
     *
     * @throws IllegalAttributeException if the passed in val does not validate
     *         against the AttributeType at that position.
     */
    public void setAttribute(int position, Object val)
        throws IllegalAttributeException {
        AttributeType type = schema.getAttributeType(position);

        try {
            Object parsed = type.parse(val);
            type.validate(parsed);
            setAttributeValue(position, parsed);
        } catch (IllegalArgumentException iae) {
            throw new IllegalAttributeException(type, val, iae);
        }
    }

    /**
     * Sets the attribute value at a given position, performing no parsing or
     * validation. This is so subclasses can have access to setting the array,
     * without opening it up completely.
     *
     * @param position the index of the attribute to set.
     * @param val the new value to give the attribute at position.
     */
    protected void setAttributeValue(int position, Object val) {
        attributes[position] = val;
    }

    /**
     * Sets all attributes for this feature, passed as an array.  All
     * attributes are checked for validity before adding.
     *
     * @param attributes All feature attributes.
     *
     * @throws IllegalAttributeException Passed attributes do not match feature
     *         type.
     */
    public void setAttributes(Object[] attributes)
        throws IllegalAttributeException {
        // the passed in attributes were null, lets make that a null array
        Object[] newAtts = attributes;

        if (attributes == null) {
            newAtts = new Object[this.attributes.length];
        }

        if (newAtts.length != this.attributes.length) {
            throw new IllegalAttributeException(
                "Wrong number of attributes expected "
                + schema.getAttributeCount() + " got " + newAtts.length);
        }

        for (int i = 0, ii = newAtts.length; i < ii; i++) {
            setAttribute(i, newAtts[i]);
        }
    }

    /**
     * Sets a single attribute for this feature, passed as a complex object. If
     * the attribute does not exist or the object does not conform to the
     * internal feature type, an exception is thrown.
     *
     * @param xPath XPath representation of attribute location.
     * @param attribute Feature attribute to set.
     *
     * @throws IllegalAttributeException Passed attribute does not match
     *         feature type
     */
    public void setAttribute(String xPath, Object attribute)
        throws IllegalAttributeException {
        int idx = schema.find(xPath);

        if (idx < 0) {
            throw new IllegalAttributeException("No attribute named " + xPath);
        }

        setAttribute(idx, attribute);
    }

    /**
     * Gets the geometry for this feature.
     *
     * @return Geometry for this feature.
     */
    public Geometry getDefaultGeometry() {
        int idx = schema.defaultGeomIdx;

        if (idx == -1) {
            return null;
        }

        return (Geometry) attributes[idx];
    }

    /**
     * Modifies the geometry.
     *
     * @param geometry All feature attributes.
     *
     * @throws IllegalAttributeException if the feature does not have a
     *         geometry.
     */
    public void setDefaultGeometry(Geometry geometry)
        throws IllegalAttributeException {
        int idx = schema.defaultGeomIdx;

        if (idx < 0) {
            throw new IllegalAttributeException(
                "Feature does not have geometry");
        }

        attributes[idx] = geometry;
        bounds = null;
    }

    /**
     * Get the number of attributes this feature has. This is simply a
     * convenience method for calling
     * getFeatureType().getNumberOfAttributes();
     *
     * @return The total number of attributes this Feature contains.
     */
    public int getNumberOfAttributes() {
        return attributes.length;
    }

    /**
     * Get the total bounds of this feature which is calculated by doing a
     * union of the bounds of each geometry this feature is associated with.
     *
     * @return An Envelope containing the total bounds of this Feature.
     *
     * @task REVISIT: what to return if there are no geometries in the feature?
     *       For now we'll return a null envelope, make this part of
     *       interface? (IanS - by OGC standards, all Feature must have geom)
     */
    public Envelope getBounds() {
        if (bounds == null) {
            bounds = new Envelope();

            for (int i = 0, n = schema.getAttributeCount(); i < n; i++) {
                if (schema.getAttributeType(i).isGeometry()) {
                    Geometry g = (Geometry) attributes[i];

                    // IanS - check for null geometry!
                    if (g == null) {
                        continue;
                    }

                    Envelope e = g.getEnvelopeInternal();

                    // IanS
                    // as of JTS 1.3, expandToInclude does not check to see if
                    // Envelope is "null", and simply adds the flagged values.
                    // This ensures that this behavior does not occur.
                    if (!e.isNull()) {
                        bounds.expandToInclude(e);
                    }
                }
            }
        }

        // lets be defensive
        return new Envelope(bounds);
    }

    /**
     * Creates an exact copy of this feature.
     *
     * @return A default feature.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        try {
            DefaultFeature clone = (DefaultFeature) super.clone();

            for (int i = 0; i < attributes.length; i++) {
                try {
                    clone.setAttribute(i, attributes[i]);
                } catch (IllegalAttributeException e1) {
                    throw new RuntimeException("The impossible has happened", e1);
                }
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The impossible has happened", e);
        }
    }

    /**
     * Returns a string representation of this feature.
     *
     * @return A representation of this feature as a string.
     */
    public String toString() {
        String retString = "Feature[ id=" + getID() + " , ";
        FeatureType featType = getFeatureType();

        for (int i = 0, n = attributes.length; i < n; i++) {
            retString += (featType.getAttributeType(i).getName() + "=");
            retString += attributes[i];

            if ((i + 1) < n) {
                retString += " , ";
            }
        }

        return retString += " ]";
    }

    /**
     * returns a unique code for this feature
     *
     * @return A unique int
     */
    public int hashCode() {
        return featureId.hashCode() * schema.hashCode();
    }

    /**
     * override of equals.  Returns if the passed in object is equal to this.
     *
     * @param obj the Object to test for equality.
     *
     * @return <code>true</code> if the object is equal, <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Feature)) {
            return false;
        }

        Feature feat = (Feature) obj;

        if (!feat.getFeatureType().equals(schema)) {
            return false;
        }

        // this check shouldn't exist, by contract, 
        //all features should have an ID.
        if (featureId == null) {
            if (feat.getID() != null) {
                return false;
            }
        }

        if (!featureId.equals(feat.getID())) {
            return false;
        }

        for (int i = 0, ii = attributes.length; i < ii; i++) {
            Object otherAtt = feat.getAttribute(i);

            if (attributes[i] == null) {
                if (otherAtt != null) {
                    return false;
                }
            } else {
                if (!attributes[i].equals(otherAtt)) {
                    if (attributes[i] instanceof Geometry
                            && otherAtt instanceof Geometry) {
                        // we need to special case Geometry
                        // as JTS is broken
                        // Geometry.equals( Object ) and Geometry.equals( Geometry )
                        // are different 
                        // (We should fold this knowledge into AttributeType...)
                        // 
                        if (!((Geometry) attributes[i]).equals(
                                    (Geometry) otherAtt)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Gets the feature collection this feature is stored in.
     *
     * @return the collection that is the parent of this feature.
     */
    public FeatureCollection getParent() {
        return parent;
    }

    /**
     * Sets the parent collection this feature is stored in, if it is not
     * already set.  If it is set then this method does nothing.
     *
     * @param collection the collection to be set as parent.
     */
    public void setParent(FeatureCollection collection) {
        if (parent == null) {
            parent = collection;
        }
    }

    public Feature toComplex() {
        try {
            return new ComplexWrapper(this);
        } catch (IllegalAttributeException iae) {
            throw new RuntimeException("the impossible has happened: ", iae);
        }
    }

    static final class ComplexWrapper extends DefaultFeature
        implements ComplexFeature {
        /**
         * Private constructor to wrap the attributes in list.  Could consider
         * making this public, but for now it seems better to keep it private
         * since we do no check to make sure tha attribute array isn't already
         * complex - and thus if it was we would wrap it in Lists again.
         *
         * @param fType DOCUMENT ME!
         * @param atts DOCUMENT ME!
         * @param fid DOCUMENT ME!
         *
         * @throws IllegalAttributeException DOCUMENT ME!
         */
        private ComplexWrapper(DefaultFeatureType fType, Object[] atts,
            String fid) throws IllegalAttributeException {
            super(fType, wrapInList(atts, fType.getAttributeCount()), fid);
        }

        public ComplexWrapper(DefaultFeatureType fType, Object[] atts)
            throws IllegalAttributeException {
            this(fType, atts, null);
        }

        //This could be problematic, not sure if all SimpleFeatures will have
        //DefaultFeatureTypes.
        public ComplexWrapper(SimpleFeature feature)
            throws IllegalAttributeException {
            this((DefaultFeatureType) feature.getFeatureType(),
                feature.getAttributes(null), feature.getID());
        }

        /*public Object getAttribute(String name) {
           return wrapInList(super.getAttribute(name));
           }
           public Object getAttribute(int index) {
               return wrapInList(super.getAttribute(index));
               }*/
        public void setAttribute(int index, Object value)
            throws IllegalAttributeException {
            checkList(value);

            List valList = (List) value;
            int listSize = valList.size();

            if (listSize == 0) {
                super.setAttribute(index, wrapInList(null));
            } else {
                AttributeType type = super.getFeatureType().getAttributeType(index);
                Object val = valList.get(0);

                try {
                    Object parsed = type.parse(val);
                    type.validate(parsed);
                    setAttributeValue(index, wrapInList(parsed));
                } catch (IllegalArgumentException iae) {
                    throw new IllegalAttributeException(type, val, iae);
                }
            }
        }

        /*public Object[] getAttributes(Object[] array) {
           Object[] retArray;
           if (array == null) {
               retArray = new Object[super.getNumberOfAttributes()];
           } else {
               retArray = array;
           }
           for (int i = 0; i < array.length; i++) {
               retArray[i] = wrapInList(array[i]);
           }
        
           return retArray;
           }*/
        public void checkList(Object value) throws IllegalAttributeException {
            if (value instanceof List) {
                List valList = (List) value;
                int listSize = valList.size();

                if (listSize > 1) {
                    String errMsg = "The attribute: " + valList + " has more "
                        + "attributes (" + listSize
                        + ") than is allowed by an "
                        + " attributeType in a Simple Feature (1)";
                    throw new IllegalAttributeException(errMsg);
                }
            } else {
                String errMsg = "All objects set in a ComplexFeature must be "
                    + "Lists, to account for multiplicity";
                throw new IllegalAttributeException(errMsg);
            }
        }

        /**
         * Sets the attribute at the given xPath.  Note that right now this
         * just does the name, and will fail on anything other than the name.
         *
         * @param xPath The name of the attribute to Set.
         * @param attribute The value to set - must be a List, for this Complex
         *        Feature.
         *
         * @throws IllegalAttributeException DOCUMENT ME!
         *
         * @task TODO: Revisit xPath stuff - get it working or do external
         *       implementation.
         */
        public void setAttribute(String xPath, Object attribute)
            throws IllegalAttributeException {
            int idx = super.getFeatureType().find(xPath);

            if (idx < 0) {
                throw new IllegalAttributeException("No attribute named "
                    + xPath);
            }

            setAttribute(idx, attribute);
        }

        protected static List wrapInList(Object attribute) {
            return java.util.Collections.singletonList(attribute);
        }

        protected static Object[] wrapInList(Object[] attributes,
            int defaultSize) {
            Object[] retArray = attributes;

            if (attributes == null) {
                retArray = new Object[defaultSize];
            } else {
                retArray = attributes;
            }

            for (int i = 0; i < attributes.length; i++) {
                retArray[i] = wrapInList(attributes[i]);
            }

            return retArray;
        }
    }
}
