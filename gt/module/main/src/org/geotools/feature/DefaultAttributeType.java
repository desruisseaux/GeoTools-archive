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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.filter.Filter;
import org.geotools.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Simple, immutable class to store attributes.  This class should be
 * sufficient for all simple (ie. non-schema) attribute implementations of
 * this interface.
 *
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @version $Id: DefaultAttributeType.java,v 1.27 2004/04/03 13:47:18 aaime Exp $
 */
public class DefaultAttributeType implements AttributeType {
    /** Name of this attribute. */
    protected final String name;

    /** Class type of this attribute. */
    protected final Class type;

    /** Indicates if nulls are allowed for this attribute */
    protected final boolean nillable;
    protected final int min,max;
    protected Object defaultValue;

	/**
	 * Defaults are flat, always return 1.
	 * @task REVISIT: return 0?  That's our current assumption in some 
	 * code, but
	 */
	public int getMinOccurs() {
		return 1;
	}

	/**
	 * Defaults are flat, always return 1.
	 */
	public int getMaxOccurs() {
		return max;
	}

    /**
     * Constructor with name and type.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param nillable If nulls are allowed for the attribute of this type.
     * @param fieldLength DOCUMENT ME!
     * @param defaultValue default value when none is suppled
     *
     * @task REVISIT: make this protected?  I think it's only used by facotries
     *       at this time.
     */
    protected DefaultAttributeType(String name, Class type, boolean nillable, int min, int max,
            Object defaultValue, Filter f) {
            this.name = (name == null) ? "" : name;
            this.type = (type == null) ? Object.class : type;
            this.nillable = nillable;
            this.min = min;this.max = max;
            this.defaultValue = defaultValue;
            this.filter = f;
        }
    protected DefaultAttributeType(String name, Class type, boolean nillable, int min, int max,
            Object defaultValue) {
            this.name = (name == null) ? "" : name;
            this.type = (type == null) ? Object.class : type;
            this.nillable = nillable;
            this.min = min;this.max = max;
            this.defaultValue = defaultValue;
        }

    protected DefaultAttributeType(String name, Class type, boolean nillable,
                                   Object defaultValue) {
	this(name, type, nillable, 1, 1,  defaultValue);
    }

    protected DefaultAttributeType(AttributeType copy) {
        this.name = copy.getName();
        this.type = copy.getType();
        this.nillable = copy.isNillable();
        this.min = copy.getMinOccurs();
        this.max = copy.getMaxOccurs();
        this.defaultValue = copy.createDefaultValue();
    }

    /**
     * Gets the name of this attribute.
     *
     * @return The name of this attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this attribute.  All attributes that are assigned to
     * this AttributeType must be an instance of this class.  Subclasses are
     * allowed as well.
     *
     * @return The class that the attribute must match to be valid for this
     *         AttributeType.
     */
    public Class getType() {
        return type;
    }

    /**
     * Returns whether nulls are allowed for this attribute.
     *
     * @return true if nulls are permitted, false otherwise.
     */
    public boolean isNillable() {
        return nillable;
    }

    /** Return a safe Object copy.
     * Obtain a duplicate Object if the object is mutable, or the same Object
     * reference if it is immutable.
     * @return A duplicated Object if the type is mutable or the same Object
     *         if it is immutable or null if the passed Object is null.
     * @throws IllegalAttributeException if the Object cannot be duplicated.
     */
    public Object duplicate(Object src) throws IllegalAttributeException {
        if (src == null) {
            return null;
        }

        //
        // The following are things I expect
        // Features will contain.
        // 
        if (src instanceof String || src instanceof Integer
                || src instanceof Double || src instanceof Float
                || src instanceof Byte || src instanceof Boolean
                || src instanceof Short || src instanceof Long
                || src instanceof Character || src instanceof Number) {
            return src;
        }
        
        if (src instanceof Date) {
            return new Date( ((Date)src).getTime() );
        }

        if (src instanceof Object[]) {
            Object[] array = (Object[]) src;
            Object[] copy = new Object[array.length];

            for (int i = 0; i < array.length; i++) {
                copy[i] = duplicate(array[i]);
            }

            return copy;
        }

        if (src instanceof Geometry) {
            Geometry geometry = (Geometry) src;

            return geometry.clone();
        }

        if (src instanceof org.geotools.feature.Feature) {
            org.geotools.feature.Feature feature = (org.geotools.feature.Feature) src;

            return feature.getFeatureType().duplicate(feature);
        }

        // 
        // We are now into diminishing returns
        // I don't expect Features to contain these often
        // (eveything is still nice and recursive)
        //
        Class type = src.getClass();

        if (type.isArray() && type.getComponentType().isPrimitive()) {
            int length = Array.getLength(src);
            Object copy = Array.newInstance(type.getComponentType(), length);
            System.arraycopy(src, 0, copy, 0, length);

            return copy;
        }

        if (type.isArray()) {
            int length = Array.getLength(src);
            Object copy = Array.newInstance(type.getComponentType(), length);

            for (int i = 0; i < length; i++) {
                Array.set(copy, i, duplicate(Array.get(src, i)));
            }

            return copy;
        }

        if (src instanceof List) {
            List list = (List) src;
            List copy = new ArrayList(list.size());

            for (Iterator i = list.iterator(); i.hasNext();) {
                copy.add(duplicate(i.next()));
            }

            return Collections.unmodifiableList(copy);
        }

        if (src instanceof Map) {
            Map map = (Map) src;
            Map copy = new HashMap(map.size());

            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                copy.put(entry.getKey(), duplicate(entry.getValue()));
            }

            return Collections.unmodifiableMap(copy);
        }
        
        

        //
        // I have lost hope and am returning the orgional reference
        // Please extend this to support additional classes.
        //
        // And good luck getting Cloneable to work
        throw new IllegalAttributeException("Do not know how to deep copy "
            + type.getName());
    }

    /**
     * Override of hashCode.
     *
     * @return hashCode for this object.
     */
    public int hashCode() {
        return name.hashCode() ^ type.hashCode();
    }

    /**
     * Override of equals.
     *
     * @param other the object to be tested for equality.
     *
     * @return whether other is equal to this attribute Type.
     */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        AttributeType att = (AttributeType) other;

        if (name == null) {
            if (att.getName() != null) {
                return false;
            }
        }

        if (!name.equals(att.getName())) {
            return false;
        }

        if (!type.equals(att.getType())) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether the attribute is a geometry.
     *
     * @return true if the attribute's type is a geometry.
     */
    public boolean isGeometry() {
        return Geometry.class.isAssignableFrom(this.type);
    }

    /**
     * Gets a representation of this object as a string.
     *
     * @return A representation of this object as a string
     */
    public String toString() {
        String details = "name=" + name;
        details += (" , type=" + type);
        details += (" , nillable=" + nillable);

        return "DefaultAttributeType [" + details + "]";
    }

    /**
     * Allows this AttributeType to convert an argument to its prefered storage
     * type. If no parsing is possible, returns the original value. If a parse
     * is attempted, yet fails (i.e. a poor decimal format) throw the
     * Exception. This is mostly for use internally in Features, but
     * implementors should simply follow the rules to be safe.
     *
     * @param value the object to attempt parsing of.
     *
     * @return <code>value</code> converted to the preferred storage of this
     *         <code>AttributeType</code>.  If no parsing was possible then
     *         the same object is returned.
     *
     * @throws IllegalArgumentException if parsing is attempted and is
     *         unsuccessful.
     */
    public Object parse(Object value) throws IllegalArgumentException {
        return value;
    }

    /**
     * Whether the tested object passes the validity constraints of  this
     * AttributeType.  At a minimum it should be of the correct class
     * specified by {@link #getType()}, non-null if isNillable is
     * <tt>false</tt>, and a geometry if isGeometry is <tt>true</tt>.  If The
     * object does not validate then an IllegalArgumentException reporting the
     * error in validation should be thrown.
     *
     * @param attribute The object to be tested for validity.
     *
     * @throws IllegalArgumentException if the object does not validate.
     */
    public void validate(Object attribute) throws IllegalArgumentException {
        if (attribute == null) {
            if (!isNillable()) {
                throw new IllegalArgumentException(getName()
                    + " is not nillable");
            }

            return;
        } else if (!type.isAssignableFrom(attribute.getClass())) {
            throw new IllegalArgumentException(attribute.getClass().getName()
                + " is not an acceptable class for " + getName()
                + " as it is not assignable from " + type);
        }
    }

    public Object createDefaultValue() {
        return defaultValue;
    }

    /* This should no longer be needed, as FeatureType implements AttributeType
     */
    static class Feature extends DefaultAttributeType {
        /** The featureType to use for validation. */
        private final FeatureType featureType;

        /**
         * Constructor with name, type and nillable.
         *
         * @param name Name of this attribute.
         * @param type The FeatureType to use for validation.
         * @param nillable If nulls are allowed for the attribute of this type.
         * @param defaultValue default value when none is suppled
         */
        public Feature(String name, FeatureType type, boolean nillable, int min, int max,
            Object defaultValue) {
            super(name, org.geotools.feature.Feature.class, nillable,min,max, 
                defaultValue);
            this.featureType = type;
        }
        
        public Object duplicate(Object o) throws IllegalAttributeException {
            if (o instanceof org.geotools.feature.Feature) {
                org.geotools.feature.Feature f = (org.geotools.feature.Feature) o;
                return f.getFeatureType().duplicate(f);
            }
            if (o == null)
                return o;
            throw new IllegalAttributeException("Could not duplicate " + o.getClass().getName());
        }

        /**
         * Whether the tested object is a Feature and its attributes validate
         * against the featureType.   An IllegalArgumentException reporting
         * the error in validation is thrown if validation fails..
         *
         * @param attribute The object to be tested for validity.
         *
         * @throws IllegalArgumentException if the object does not validate.
         */
        public void validate(Object attribute) throws IllegalArgumentException {
            super.validate(attribute);

            org.geotools.feature.Feature att = (org.geotools.feature.Feature) attribute;

            if (att == null) {
                return;
            }

            if (!(att.getFeatureType().isDescendedFrom(featureType)
                    || att.getFeatureType().equals(featureType))) {
                throw new IllegalArgumentException(
                    "Not correct FeatureType, expected " + featureType
                    + " got " + att.getFeatureType());
            }
        }
    }

    /* TODO: revisit this - we want to move stuff out of the hierarchy, but this already
     * has an interface outside.  We should figure out how we want our defaults vs. our
     * interfaces for types to live.
     */
    public static class Geometric extends DefaultAttributeType
        implements GeometryAttributeType {
        /** CoordianteSystem used by this GeometryAttributeType */
        protected CoordinateReferenceSystem coordinateSystem;
        protected GeometryFactory geometryFactory;
        private Filter filter;

        public Geometric(String name, Class type, boolean nillable, int min, int max,
            Object defaultValue, CoordinateReferenceSystem cs, Filter filter) {
            super(name, type, nillable,min,max, defaultValue);
            this.filter = filter;
            coordinateSystem = cs;
            geometryFactory = cs == null ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(cs);
            
            /*
            coordinateSystem = (cs != null) ? cs : LocalCoordinateSystem.CARTESIAN;
            geometryFactory = (cs == LocalCoordinateSystem.CARTESIAN)
                ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(cs);
             */
        }

       public Geometric(String name, Class type, boolean nillable,
            Object defaultValue, CoordinateReferenceSystem cs,Filter filter) {
            this(name, type, nillable,1,1, defaultValue, cs,filter);
       }

        public Geometric(GeometryAttributeType copy, CoordinateReferenceSystem override) {
            super(copy);
            coordinateSystem = copy.getCoordinateSystem();

            if (override != null) {
                coordinateSystem = override;
            }

            if (coordinateSystem == null) {
                coordinateSystem = GeocentricCRS.CARTESIAN;
            }
            geometryFactory = (coordinateSystem == GeocentricCRS.CARTESIAN)
                ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(coordinateSystem);            
        }

        public CoordinateReferenceSystem getCoordinateSystem() {
            return coordinateSystem;
        }

        public GeometryFactory getGeometryFactory() {
            return geometryFactory;
        }

        public Object parse(Object value) throws IllegalArgumentException {
            if (value == null) {
                return value;
            }

            if (value instanceof Geometry) {
                return value;
            }

            // consider wkt/wkb/gml support?
            throw new RuntimeException(
                "AttributeGT.Geometric cannot parse " + value);
        }
        
        public Object duplicate(Object o) throws IllegalAttributeException {
            if (o == null)
                return o;
            if (o instanceof Geometry) {
                return ((Geometry)o).clone();
            }
            throw new IllegalAttributeException("Cannot duplicate " + o.getClass().getName());
        }

		/* (non-Javadoc)
		 * @see org.geotools.feature.PrimativeAttributeType#getRestriction()
		 */
		public Filter getRestriction() {
			return filter;
		}
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.AttributeType#getRestriction()
	 */
	public Filter getRestriction() {
		return filter;
	}
	private Filter filter;
}


/**
 * Helper class used to force CS information on JTS Geometry
 */
class CSGeometryFactory extends GeometryFactory {
    
    static public GeometryFactory DEFAULT = new GeometryFactory();    
    static public PrecisionModel DEFAULT_PRECISON_MODEL = new PrecisionModel();

    public CSGeometryFactory(CoordinateReferenceSystem cs) {
        super(toPrecisionModel(cs), toSRID(cs));
    }

    public GeometryCollection createGeometryCollection(Geometry[] geometries) {
        GeometryCollection gc = super.createGeometryCollection(geometries);

        // JTS14
        //gc.setUserData( cs );
        return gc;
    }

    public LinearRing createLinearRing(Coordinate[] coordinates) {
        LinearRing lr = super.createLinearRing(coordinates);

        // JTS14
        //gc.setUserData( cs );
        return lr;
    }

    //
    // And so on
    // Utility Functions
    private static int toSRID(CoordinateReferenceSystem cs) {
        if ((cs == null) || (cs == GeocentricCRS.CARTESIAN)) {
            return 0;
        }

        // not sure how to tell SRID from CoordinateSystem?
        return 0;
    }

    private static PrecisionModel toPrecisionModel(CoordinateReferenceSystem cs) {
        if ((cs == null) || (cs == GeocentricCRS.CARTESIAN)) {
            return DEFAULT_PRECISON_MODEL;
        }

        return DEFAULT_PRECISON_MODEL;
    }
}
