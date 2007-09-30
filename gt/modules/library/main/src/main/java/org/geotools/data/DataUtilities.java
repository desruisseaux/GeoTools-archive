/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.geotools.data.collection.CollectionDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.Name;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.util.Converters;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility functions for use when implementing working with data classes.
 * <p>
 * TODO: Move FeatureType manipulation to feature package
 * </p>
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public class DataUtilities {
    static Map typeMap = new HashMap();
    
    static FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    static {
        typeMap.put("String", String.class);
        typeMap.put("string", String.class);
        typeMap.put("\"\"", String.class);
        typeMap.put("Integer", Integer.class);
        typeMap.put("int", Integer.class);
        typeMap.put("0", Integer.class);
        typeMap.put("Double", Double.class);
        typeMap.put("double", Double.class);
        typeMap.put("0.0", Double.class);
        typeMap.put("Float", Float.class);
        typeMap.put("float", Float.class);
        typeMap.put("0.0f", Float.class);
        typeMap.put("Boolean", Boolean.class);
        typeMap.put("true",Boolean.class);
        typeMap.put("false",Boolean.class);
        typeMap.put("Geometry", Geometry.class);
        typeMap.put("Point", Point.class);
        typeMap.put("LineString", LineString.class);
        typeMap.put("Polygon", Polygon.class);
        typeMap.put("MultiPoint", MultiPoint.class);
        typeMap.put("MultiLineString", MultiLineString.class);
        typeMap.put("MultiPolygon", MultiPolygon.class);
        typeMap.put("GeometryCollection", GeometryCollection.class);
        typeMap.put("Date",Date.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String[] attributeNames(SimpleFeatureType featureType) {
        String[] names = new String[featureType.getAttributeCount()];
        final int count = featureType.getAttributeCount();
        for (int i = 0; i < count; i++) {
        	names[i] = featureType.getAttribute(i).getLocalName();
        }
        
        return names;
    }
    
    /**
     * Takes a URL and converts it to a File. The attempts to deal with 
     * Windows UNC format specific problems, specifically files located
     * on network shares and different drives.
     * 
     * If the URL.getAuthority() returns null or is empty, then only the
     * url's path property is used to construct the file. Otherwise, the
     * authority is prefixed before the path.
     * 
     * It is assumed that url.getProtocol returns "file".
     * 
     * Authority is the drive or network share the file is located on.
     * Such as "C:", "E:", "\\fooServer"
     * 
     * @param url a URL object that uses protocol "file"
     * @return a File that corresponds to the URL's location
     */
    public static File urlToFile (URL url) {
    	String auth = url.getAuthority();
    	String path = url.getPath();
		File f = null;
		if (auth != null && !auth.equals("")) {
			f = new File("//"+auth+path);
		} else {
			f = new File(path);
		}
		return f;
    }


    /**
     * Traverses the filter and returns any encoutered property names.
     * <p>
     * The feautre type is supplied as contexts used to lookup expressions in cases where the 
     * attributeName does not match the actual name of the type.
     * </p>
     */
    public static String[] attributeNames( Filter filter, final SimpleFeatureType featureType ) {
    	 if (filter == null) {
             return new String[0];
         }
         FilterAttributeExtractor attExtractor = new FilterAttributeExtractor(featureType);
         filter.accept(attExtractor, null);
         String[] attributeNames = attExtractor.getAttributeNames();
         return attributeNames;
    }
    
    /**
     * Traverses the filter and returns any encoutered property names.
     * @deprecated use {@link #attributeNames(Filter, FeatureType)}/
     */
    public static String[] attributeNames(Filter filter) {
       return attributeNames( filter, null );
    }

    /**
     * Traverses the expression and returns any encoutered property names.
     * <p>
     * The feautre type is supplied as contexts used to lookup expressions in cases where the 
     * attributeName does not match the actual name of the type.
     * </p>
     */
    public static String[] attributeNames(Expression expression, final SimpleFeatureType featureType ) {
    	 if (expression == null) {
             return new String[0];
         }
         FilterAttributeExtractor attExtractor = new FilterAttributeExtractor(featureType);
         expression.accept(attExtractor, null);
         String[] attributeNames = attExtractor.getAttributeNames();
         return attributeNames;
    }
    
    /**
     * Traverses the expression and returns any encoutered property names.
     * @deprecated use {@link #attributeNames(Expression, FeatureType)}/
     */
    public static String[] attributeNames(Expression expression) {
       return attributeNames( expression, null );
    }
    
    /**
     *
     * @param filter DOCUMENT ME!
     * @param visitor DOCUMENT ME!
     */
    public static void traverse(Filter filter, FilterVisitor visitor) {
        traverse(traverseDepth(filter), visitor);
    }
    /**
     *
     * @param expression DOCUMENT ME!
     * @param visitor DOCUMENT ME!
     */
    public static void traverse(Expression expression, FilterVisitor visitor) {
        traverse(traverseDepth(expression), visitor);
    }
    /**
     * Performs a depth first traversal on Filter.
     * 
     * <p>
     * Filters can contain Expressions and other Filters, this method will call
     * visitor.visit( Filter ) and visitor.visit( Expression )
     * </p>
     *
     * @param set Set of Filter and Expression information
     * @param visitor Vistor to traverse across set
     */
    public static void traverse(Set set, final FilterVisitor visitor) {
        for (Iterator i = set.iterator(); i.hasNext();) {
            Object here = i.next();

            if (here instanceof Filter) {
                ((Filter) here).accept(visitor, null);
            } else if (here instanceof Expression) {
                if (!(visitor instanceof ExpressionVisitor)) {
                    throw new IllegalArgumentException("visitor is not an ExpressionVisitor");
                }
                ((Expression) here).accept((ExpressionVisitor) visitor, null);
            } else {
                throw new IllegalArgumentException("Not a Filter or an Expression: " + here);
            }
//            if (here instanceof BetweenFilter) {
//                visitor.visit((BetweenFilter) here);
//            } else if (here instanceof CompareFilter) {
//                visitor.visit((CompareFilter) here);
//            } else if (here instanceof GeometryFilter) {
//                visitor.visit((GeometryFilter) here);
//            } else if (here instanceof LikeFilter) {
//                visitor.visit((LikeFilter) here);
//            } else if (here instanceof LogicFilter) {
//                visitor.visit((LogicFilter) here);
//            } else if (here instanceof NullFilter) {
//                visitor.visit((NullFilter) here);
//            } else if (here instanceof FidFilter) {
//                visitor.visit((FidFilter) here);
//            } else if (here instanceof Filter) {
//                visitor.visit((org.geotools.filter.Filter) here);
//            } else if (here instanceof AttributeExpression) {
//                visitor.visit((AttributeExpression) here);
//            } else if (here instanceof LiteralExpression) {
//                visitor.visit((LiteralExpression) here);
//            } else if (here instanceof MathExpression) {
//                visitor.visit((MathExpression) here);
//            } else if (here instanceof FunctionExpression) {
//                visitor.visit((FunctionExpression) here);
//            } else if (here instanceof Expression) {
//                visitor.visit((org.geotools.filter.Filter) here);
//            }
        }
    }

    /**
     * Performs a depth first traversal of Filter.
     *
     * @param filter
     *
     * @return Set of Filters in traversing filter
     */
    public static Set traverseDepth(Filter filter) {
        final Set set = new HashSet();
        FilterVisitor traverse = new Traversal() {
                void traverse(Filter f) {
                    set.add(f);
                }

                void traverse(Expression expression) {
                    set.add(expression);
                }
            };

        filter.accept(traverse, null );
        
        return set;
    }
    /**
     * Performs a depth first traversal of Filter.
     *
     * @param expression
     *
     * @return Set of Filters in traversing filter
     */
    public static Set traverseDepth(Expression expression) {
        final Set set = new HashSet();
        ExpressionVisitor traverse = new Traversal() {
                void traverse(Filter f) {
                    set.add(f);
                }

                void traverse(Expression expr) {
                    set.add(expr);
                }
            };

        expression.accept(traverse, null);

        return set;
    }

    /**
     * Compare operation for FeatureType.
     * 
     * <p>
     * Results in:
     * </p>
     * 
     * <ul>
     * <li>
     * 1: if typeA is a sub type/reorder/renamespace of typeB
     * </li>
     * <li>
     * 0: if typeA and typeB are the same type
     * </li>
     * <li>
     * -1: if typeA is not subtype of typeB
     * </li>
     * </ul>
     * 
     * <p>
     * Comparison is based on AttributeTypes, an IOException is thrown if the
     * AttributeTypes are not compatiable.
     * </p>
     * 
     * <p>
     * Namespace is not considered in this opperations. You may still need to
     * reType to get the correct namesapce, or reorder.
     * </p>
     *
     * @param typeA FeatureType beind compared
     * @param typeB FeatureType being compared against
     *
     */
    public static int compare(SimpleFeatureType typeA, SimpleFeatureType typeB) {
        if (typeA == typeB) {
            return 0;
        }

        if (typeA == null) {
            return -1;  
        }

        if (typeB == null) {
            return -1;
        }

        int countA = typeA.getAttributeCount();
        int countB = typeB.getAttributeCount();

        if (countA > countB) {
            return -1;
        }

        // may still be the same featureType
        // (Perhaps they differ on namespace?)
        AttributeDescriptor a;

        // may still be the same featureType
        // (Perhaps they differ on namespace?)
        int match = 0;

        for (int i = 0; i < countA; i++) {
            a = typeA.getAttribute(i);

            if (isMatch(a, typeB.getAttribute(i))) {
                match++;
            } else if (isMatch(a, typeB.getAttribute(a.getLocalName()))) {
                // match was found in a different position
            } else {
                // cannot find any match for Attribute in typeA
                return -1;
            }
        }

        if ((countA == countB) && (match == countA)) {
            // all attributes in typeA agreed with typeB
            // (same order and type)
            //            if (typeA.getNamespace() == null) {
            //            	if(typeB.getNamespace() == null) {
            //            		return 0;
            //            	} else {
            //            		return 1;
            //            	}
            //            } else if(typeA.getNamespace().equals(typeB.getNamespace())) {
            //                return 0;
            //            } else {
            //                return 1;
            //            }
            return 0;
        }

        return 1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param a DOCUMENT ME!
     * @param b DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static boolean isMatch(AttributeDescriptor a, AttributeDescriptor b) {
        if (a == b) {
            return true;
        }

        if (b == null) {
            return false;
        }

        if (a == null) {
            return false;
        }

        if (a.equals(b)) {
            return true;
        }

        if (a.getLocalName().equals(b.getLocalName())
                && a.getClass().equals(b.getClass())) {
            return true;
        }

        return false;
    }

    /**
     * Creates duplicate of feature adjusted to the provided featureType.
     *
     * @param featureType FeatureType requested
     * @param feature Origional Feature from DataStore
     *
     * @return An instance of featureType based on feature
     *
     * @throws IllegalAttributeException If opperation could not be performed
     */
    public static SimpleFeature reType(SimpleFeatureType featureType, SimpleFeature feature)
        throws IllegalAttributeException {
        SimpleFeatureType origional = feature.getFeatureType();

        if (featureType.equals(origional)) {
            return SimpleFeatureBuilder.copy(feature);
        }

        String id = feature.getID();
        int numAtts = featureType.getAttributeCount();
        Object[] attributes = new Object[numAtts];
        String xpath;

        for (int i = 0; i < numAtts; i++) {
            AttributeDescriptor curAttType = featureType.getAttribute(i);
            xpath = curAttType.getLocalName();
            attributes[i] = duplicate(feature.getAttribute(xpath));
        }

        return SimpleFeatureBuilder.build(featureType, attributes, id);
    }
    
    public static Object duplicate( Object src ) {
//JD: this method really needs to be replaced with somethign better
        
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
        
        if (src instanceof URL || src instanceof URI ) {
            return src; //immutable
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

        if (src instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) src;
            return SimpleFeatureBuilder.copy(feature);
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
        
        if( src instanceof GridCoverage ){
            return src; // inmutable
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
     * Constructs an empty feature to use as a Template for new content.
     * 
     * <p>
     * We may move this functionality to FeatureType.create( null )?
     * </p>
     *
     * @param featureType Type of feature we wish to create
     *
     * @return A new Feature of type featureType
     *
     * @throws IllegalAttributeException if we could not create featureType
     *         instance with acceptable default values
     */
    public static SimpleFeature template(SimpleFeatureType featureType)
        throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType), null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param featureID DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public static SimpleFeature template(SimpleFeatureType featureType, String featureID)
        throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType), featureID);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public static Object[] defaultValues(SimpleFeatureType featureType)
        throws IllegalAttributeException {
        return defaultValues(featureType, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param atts DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public static SimpleFeature template(SimpleFeatureType featureType, Object[] atts)
        throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType,defaultValues(featureType, atts),null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param featureID DOCUMENT ME!
     * @param atts DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public static SimpleFeature template(SimpleFeatureType featureType, String featureID,
        Object[] atts) throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType, atts), featureID);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param values DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     */
    public static Object[] defaultValues(SimpleFeatureType featureType,
        Object[] values) throws IllegalAttributeException {
        if (values == null) {
            values = new Object[featureType.getAttributeCount()];
        } else if (values.length != featureType.getAttributeCount()) {
            throw new ArrayIndexOutOfBoundsException("values");
        }

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            values[i] = defaultValue(featureType.getAttribute(i));
        }

        return values;
    }

    /**
     * Provides a defautlValue for attributeType.
     * 
     * <p>
     * Will return null if attributeType isNillable(), or attempt to use
     * Reflection, or attributeType.parse( null )
     * </p>
     *
     * @param attributeType
     *
     * @return null for nillable attributeType, attempt at reflection
     *
     * @throws IllegalAttributeException If value cannot be constructed for
     *         attribtueType
     */
    public static Object defaultValue(AttributeDescriptor attributeType)
        throws IllegalAttributeException {
            Object value = attributeType.getDefaultValue();
        
        if (value == null && !attributeType.isNillable()) {
            throw new IllegalAttributeException(
                "Got null default value for non-null type.");
        }
        return value;
    }

    /**
     * Returns a non-null default value for the class that is passed in.  This is a helper class an can't create a 
     * default class for any type but it does support:
     * <ul>
     * <li>String</li>
     * <li>Object - will return empty string</li>
     * <li>Number</li>
     * <li>Character</li>
     * <li>JTS Geometries</li>
     * </ul>
     * 
     *
     * @param type
     * @return
     */
    public static Object defaultValue(Class type){
        if( type==String.class || type==Object.class){
            return "";
        }
        if( type==Integer.class ){
            return new Integer(0);
        }
        if( type==Double.class ){
            return new Double(0);
        }
        if( type==Long.class ){
            return new Long(0);
        }
        if( type==Short.class ){
            return new Short((short)0);
        }
        if( type==Float.class ){
            return new Float(0.0f);
        }
        if( type==Character.class ){
            return new Character(' ');
        }
        
        GeometryFactory fac=new GeometryFactory();
        Coordinate coordinate = new Coordinate(0, 0);
        Point point = fac.createPoint(coordinate);

        if( type==Point.class ){
            return point;
        }
        if( type==MultiPoint.class ){
            return fac.createMultiPoint(new Point[]{point});
        }
        if( type==LineString.class ){
            return fac.createLineString(new Coordinate[]{coordinate,coordinate,coordinate,coordinate});
        }
        LinearRing linearRing = fac.createLinearRing(new Coordinate[]{coordinate,coordinate,coordinate,coordinate});
        if( type==LinearRing.class ){
            return linearRing;
        }
        if( type==MultiLineString.class ){
            return fac.createMultiLineString(new LineString[]{linearRing});
        }
        Polygon polygon = fac.createPolygon(linearRing, new LinearRing[0]);
        if( type==Polygon.class ){
            return polygon;
        }
        if( type==MultiPolygon.class ){
            return fac.createMultiPolygon(new Polygon[]{polygon});
        }
        
        throw new IllegalArgumentException(type+" is not supported by this method");
    }
    /**
     * Creates a FeatureReader for testing.
     *
     * @param features Array of features
     *
     * @return FeatureReader spaning provided feature array
     *
     * @throws IOException If provided features Are null or empty
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public static FeatureReader reader(final SimpleFeature[] features)
        throws IOException {
        if ((features == null) || (features.length == 0)) {
            throw new IOException("Provided features where empty");
        }
    
        return new FeatureReader() {
                SimpleFeature[] array = features;
                int offset = -1;

                public SimpleFeatureType getFeatureType() {
                    return features[0].getFeatureType();
                }

                public SimpleFeature next(){
                    if (!hasNext()) {
                        throw new NoSuchElementException("No more features");
                    }

                    return array[++offset];
                }

                public boolean hasNext(){
                    return (array != null) && (offset < (array.length - 1));
                }

                public void close(){
                    array = null;
                    offset = -1;
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureArray DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public static FeatureSource source(final SimpleFeature[] featureArray) {
        final SimpleFeatureType featureType;

        if ((featureArray == null) || (featureArray.length == 0)) {
            featureType = FeatureTypes.EMPTY;
        } else {
            featureType = featureArray[0].getFeatureType();
        }

        DataStore arrayStore = new AbstractDataStore() {
                public String[] getTypeNames() {
                    return new String[] { featureType.getTypeName() };
                }

                public SimpleFeatureType getSchema(String typeName)
                    throws IOException {
                    if ((typeName != null)
                            && typeName.equals(featureType.getTypeName())) {
                        return featureType;
                    }

                    throw new IOException(typeName + " not available");
                }

                protected FeatureReader getFeatureReader(String typeName)
                    throws IOException {
                    return reader(featureArray);
                }
            };

        try {
            return arrayStore.getFeatureSource(arrayStore.getTypeNames()[0]);
        } catch (IOException e) {
            throw new RuntimeException(
                "Something is wrong with the geotools code, "
                + "this exception should not happen", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param collection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NullPointerException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public static FeatureSource source(final FeatureCollection collection) {
        if (collection == null) {
            throw new NullPointerException();
        }

        DataStore store = new CollectionDataStore(collection);

        try {
            return store.getFeatureSource(store.getTypeNames()[0]);
        } catch (IOException e) {
            throw new RuntimeException(
                "Something is wrong with the geotools code, "
                + "this exception should not happen", e);
        }
    }
    
    public static FeatureCollection results(SimpleFeature[] featureArray){
        return results(collection(featureArray));
    }

    /**
     * Returns collection if non empty.
     *
     * @param collection
     *
     * @return provided collection
     *
     * @throws IOException Raised if collection was empty
     */
    public static FeatureCollection results(final FeatureCollection collection){
        if (collection.size() == 0) {
            //throw new IOException("Provided collection was empty");
        }
        return collection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param collection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static FeatureReader reader(Collection collection)
        throws IOException {
        return reader((SimpleFeature[]) collection.toArray(
                new SimpleFeature[collection.size()]));
    }

    /**
     * Copies the provided features into a FeatureCollection.
     * <p>
     * Often used when gathering features for FeatureStore:<pre><code>
     * featureStore.addFeatures( DataUtilities.collection(array));
     * </code></pre>
     * 
     * @param features Array of features
     * @return FeatureCollection
     */
    public static FeatureCollection collection(SimpleFeature[] features) {
        FeatureCollection collection = FeatureCollections.newCollection();
		final int length = features.length;
		for (int i = 0; i < length; i++) {
            collection.add(features[i]);
        }
        return collection;
    }
    /**
     * Copies the provided features into a FeatureCollection.
     * <p>
     * Often used when gathering a FeatureCollection into memory.
     * 
     * @param featureCollection the features to add to a new feature collection.
     * @return FeatureCollection
     */
    public static FeatureCollection collection( FeatureCollection featureCollection ){
        return new DefaultFeatureCollection( featureCollection );
    }

    /**
     * Copies the provided features into a FeatureCollection.
     * <p>
     * Often used when gathering a FeatureCollection into memory.
     *
     * @param list features to add to a new FeatureCollection
     * @return FeatureCollection
     */
    public static FeatureCollection collection( List<SimpleFeature> list ) {
        FeatureCollection collection = FeatureCollections.newCollection();
        for ( SimpleFeature feature : list ){
            collection.add( list );
        }
        return collection;
    }

    /**
     * Copies the provided features into a FeatureCollection.
     * <p>
     * Often used when gathering features for FeatureStore:<pre><code>
     * featureStore.addFeatures( DataUtilities.collection(feature));
     * </code></pre>
     * 
     * @param feature a feature to add to a new collection
     * @return FeatureCollection
     */
    public static FeatureCollection collection( SimpleFeature feature ){
        FeatureCollection collection = FeatureCollections.newCollection();
        collection.add(feature);
        return collection;
    }

    /**
     * Copies the provided reader into a FeatureCollection, reader will be closed.
     * <p>
     * Often used when gathering features for FeatureStore:<pre><code>
     * featureStore.addFeatures( DataUtilities.collection(reader));
     * </code></pre>
     * 
     * @return FeatureCollection
     */
    public static FeatureCollection collection(FeatureReader reader) throws IOException {
        FeatureCollection collection = FeatureCollections.newCollection();        
        try {
            while( reader.hasNext() ) {
                try {
                    collection.add( reader.next() );
                } catch (NoSuchElementException e) {
                    throw (IOException) new IOException("EOF").initCause( e );
                } catch (IllegalAttributeException e) {
                    throw (IOException) new IOException().initCause( e );
                }                
            }
        }
        finally {
            reader.close();
        }
        return collection;
    }
    /**
     * Copies the provided reader into a FeatureCollection, reader will be closed.
     * <p>
     * Often used when gathering features for FeatureStore:<pre><code>
     * featureStore.addFeatures( DataUtilities.collection(reader));
     * </code></pre>
     * 
     * @return FeatureCollection
     */
    public static FeatureCollection collection(FeatureIterator reader) throws IOException {
        FeatureCollection collection = FeatureCollections.newCollection();        
        try {
            while( reader.hasNext() ) {
                try {
                    collection.add( reader.next() );
                } catch (NoSuchElementException e) {
                    throw (IOException) new IOException("EOF").initCause( e );
                }               
            }
        }
        finally {
            reader.close();
        }
        return collection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param att DOCUMENT ME!
     * @param otherAtt DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static boolean attributesEqual(Object att, Object otherAtt) {
        if (att == null) {
            if (otherAtt != null) {
                return false;
            }
        } else {
            if (!att.equals(otherAtt)) {
                if (att instanceof Geometry && otherAtt instanceof Geometry) {
                    // we need to special case Geometry
                    // as JTS is broken
                    // Geometry.equals( Object ) and Geometry.equals( Geometry )
                    // are different 
                    // (We should fold this knowledge into AttributeType...)
                    // 
                    if (!((Geometry) att).equals((Geometry) otherAtt)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Create a derived FeatureType
     * 
     * <p></p>
     *
     * @param featureType
     * @param properties - if null, every property of the feature type in input will be used
     * @param override
     *
     *
     * @throws SchemaException
     */
    public static SimpleFeatureType createSubType(SimpleFeatureType featureType,
            String[] properties, CoordinateReferenceSystem override)
            throws SchemaException {
        URI namespaceURI = null;
        if ( featureType.getName().getNamespaceURI() != null ) {
            try {
                namespaceURI = new URI( featureType.getName().getNamespaceURI() );
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        
        return createSubType( featureType, properties, override, featureType.getTypeName(), namespaceURI );

    }

    public static SimpleFeatureType createSubType(SimpleFeatureType featureType,
        String[] properties, CoordinateReferenceSystem override, String typeName, URI namespace )
        throws SchemaException {
        
        if ((properties == null) && (override == null)) {
            return featureType;
        }
        
        if(properties == null) {
          properties = new String[featureType.getAttributeCount()];
          for (int i = 0; i < properties.length; i++) {
            properties[i] = featureType.getAttribute(i).getLocalName();
          }
        }

        String namespaceURI = namespace != null ? namespace.toString() : null;
        boolean same = featureType.getAttributeCount() == properties.length &&
            featureType.getTypeName().equals( typeName ) &&
            Utilities.equals(featureType.getName().getNamespaceURI(), namespaceURI );
            

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            AttributeDescriptor type = featureType.getAttribute(i);
            same = type.getLocalName().equals(properties[i])
                && (((override != null)
                && type instanceof GeometryDescriptor)
                ? assertEquals(override, ((GeometryDescriptor) type).getCRS())
                : true);
        }

        if (same) {
            return featureType;
        }

        AttributeDescriptor[] types = new AttributeDescriptor[properties.length];

        for (int i = 0; i < properties.length; i++) {
            types[i] = featureType.getAttribute(properties[i]);

            if ((override != null) && types[i] instanceof GeometryDescriptor) {
                AttributeTypeBuilder ab = new AttributeTypeBuilder();
                ab.init( types[i] );
                ab.setCRS(override);
                types[i] = ab.buildDescriptor(types[i].getLocalName());
            }
        }

        if( typeName == null ) typeName = featureType.getTypeName();
        if( namespace == null && featureType.getName().getNamespaceURI() != null )
            try {
                namespace = new URI(featureType.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }    
            
            
        
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName( typeName );
        tb.setNamespaceURI( namespace );
        tb.addAll(types);
        
        return tb.buildFeatureType();
    }
    
    private static boolean assertEquals(Object o1, Object o2){
    	return o1 == null && o2 == null? true :
    		(o1 != null? o1.equals(o2) : false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param properties DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SchemaException DOCUMENT ME!
     */
    public static SimpleFeatureType createSubType(SimpleFeatureType featureType,
        String[] properties) throws SchemaException {
        if (properties == null) {
            return featureType;
        }

        boolean same = featureType.getAttributeCount() == properties.length;

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            same = featureType.getAttribute(i).getLocalName().equals(properties[i]);
        }

        if (same) {
            return featureType;
        }

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName( featureType.getName() );
        
        for (int i = 0; i < properties.length; i++) {
            tb.add( featureType.getAttribute(properties[i]) );
        }
        return tb.buildFeatureType();
    }

    /**
     * Utility method for FeatureType construction.
     * <p>
     * Will parse a String of the form: <i>"name:Type,name2:Type2,..."</i>
     * </p>
     * 
     * <p>
     * Where <i>Type</i> is defined by createAttribute.
     * </p>
     * 
     * <p>
     * You may indicate the default Geometry with an astrix: "*geom:Geometry".
     * </p>
     * 
     * <p>
     * Example:<code>name:"",age:0,geom:Geometry,centroid:Point,url:java.io.URL"</code>
     * </p>
     *
     * @param identification identification of FeatureType:
     *        (<i>namesapce</i>).<i>typeName</i>
     * @param typeSpec Specification for FeatureType
     *
     *
     * @throws SchemaException
     */
    public static SimpleFeatureType createType(String identification, String typeSpec)
        throws SchemaException {
        int split = identification.lastIndexOf('.');
        String namespace = (split == -1) ? null
                                         : identification.substring(0, split);
        String typeName = (split == -1) ? identification
                                        : identification.substring(split + 1);

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName( typeName );
        tb.setNamespaceURI(namespace);
        
        String[] types = typeSpec.split(",");
        int geometryIndex = -1; // records * specified goemetry 
        AttributeDescriptor attributeType;
        GeometryDescriptor geometryAttribute = null; // records guess 

        for (int i = 0; i < types.length; i++) {
            if (types[i].startsWith("*")) {
                types[i] = types[i].substring(1);
                geometryIndex = i;
            }

            attributeType = createAttribute(types[i]);
            tb.add(attributeType);

            if ((geometryAttribute == null)
                    && attributeType instanceof GeometryDescriptor) {
                if (geometryIndex == -1) {
                    geometryAttribute = (GeometryDescriptor) attributeType;
                } else if (geometryIndex == i) {
                    geometryAttribute = (GeometryDescriptor) attributeType;
                }
            }
        }

        if (geometryAttribute != null) {
            tb.setDefaultGeometry(geometryAttribute.getLocalName());
        }

        return tb.buildFeatureType();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param fid DOCUMENT ME!
     * @param text DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public static SimpleFeature parse(SimpleFeatureType type, String fid, String[] text)
        throws IllegalAttributeException {
        Object[] attributes = new Object[text.length];

        for (int i = 0; i < text.length; i++) {
            AttributeType attType = type.getAttribute(i).getType();
            attributes[i] = Converters.convert(text[i], attType.getBinding());
        }

        return SimpleFeatureBuilder.build(type, attributes, fid);
    }

    /**
     * A "quick" String representation of a FeatureType.
     * <p>
     * This string representation may be used with createType( name, spec ).
     * </p>
     * @param featureType FeatureType to represent
     *
     * @return The string "specification" for the featureType
     */
    public static String spec(SimpleFeatureType featureType) {
        List types = featureType.getAttributes();

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < types.size(); i++) {
            AttributeDescriptor type = (AttributeDescriptor) types.get( i ); 
            buf.append(type.getLocalName());
            buf.append(":");
            buf.append(typeMap(type.getType().getBinding()));

            if (i < (types.size() - 1)) {
                buf.append(",");
            }
        }

        return buf.toString();
    }

    static Class type(String typeName) throws ClassNotFoundException {
        if (typeMap.containsKey(typeName)) {
            return (Class) typeMap.get(typeName);
        }

        return Class.forName(typeName);
    }

    static String typeMap(Class type) {
        for (Iterator i = typeMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Entry) i.next();

            if (entry.getValue().equals(type)) {
                return (String) entry.getKey();
            }
        }

        return type.getName();
    }

    /**
     * Takes two {@link Query}objects and produce a new one by mixing the
     * restrictions of both of them.
     * 
     * <p>
     * The policy to mix the queries components is the following:
     * 
     * <ul>
     * <li>
     * typeName: type names MUST match (not checked if some or both queries
     * equals to <code>Query.ALL</code>)
     * </li>
     * <li>
     * handle: you must provide one since no sensible choice can be done
     * between the handles of both queries
     * </li>
     * <li>
     * maxFeatures: the lower of the two maxFeatures values will be used (most
     * restrictive)
     * </li>
     * <li>
     * attributeNames: the attributes of both queries will be joined in a
     * single set of attributes. IMPORTANT: only <b><i>explicitly</i></b>
     * requested attributes will be joint, so, if the method
     * <code>retrieveAllProperties()</code> of some of the queries returns
     * <code>true</code> it does not means that all the properties will be
     * joined. You must create the query with the names of the properties you
     * want to load.
     * </li>
     * <li>
     * filter: the filtets of both queries are or'ed
     * </li>
     * </ul>
     * </p>
     *
     * @param firstQuery Query against this DataStore
     * @param secondQuery DOCUMENT ME!
     * @param handle DOCUMENT ME!
     *
     * @return Query restricted to the limits of definitionQuery
     *
     * @throws NullPointerException if some of the queries is null
     * @throws IllegalArgumentException if the type names of both queries do
     *         not match
     */
    public static Query mixQueries(Query firstQuery, Query secondQuery,
        String handle) {
        if ((firstQuery == null) || (secondQuery == null)) {
            throw new NullPointerException("got a null query argument");
        }

        if (firstQuery.equals(Query.ALL)) {
            return secondQuery;
        } else if (secondQuery.equals(Query.ALL)) {
            return firstQuery;
        }

        if ((firstQuery.getTypeName() != null)
                && (secondQuery.getTypeName() != null)) {
            if (!firstQuery.getTypeName().equals(secondQuery.getTypeName())) {
                String msg = "Type names do not match: "
                    + firstQuery.getTypeName() + " != "
                    + secondQuery.getTypeName();
                throw new IllegalArgumentException(msg);
            }
        }
        
        // mix versions, if possible
        String version;
        if(firstQuery.getVersion() != null) {
            if(secondQuery.getVersion() != null && !secondQuery.getVersion().equals(firstQuery.getVersion()))
                throw new IllegalArgumentException("First and second query refer different versions");
            version = firstQuery.getVersion();
        } else {
            version = secondQuery.getVersion();
        }
            

        //none of the queries equals Query.ALL, mix them
        //use the more restrictive max features field
        int maxFeatures = Math.min(firstQuery.getMaxFeatures(),
                secondQuery.getMaxFeatures());

        //join attributes names
        String[] propNames = joinAttributes(firstQuery.getPropertyNames(),
                secondQuery.getPropertyNames());

        //join filters
        Filter filter = firstQuery.getFilter();
        Filter filter2 = secondQuery.getFilter();

        if ((filter == null) || filter.equals(Filter.INCLUDE)) {
            filter = filter2;
        } else if ((filter2 != null) && !filter2.equals(Filter.INCLUDE)) {
            filter = ff.and( filter, filter2);
        }

        //build the mixed query
        String typeName = firstQuery.getTypeName() != null? 
        		firstQuery.getTypeName() : secondQuery.getTypeName();

        DefaultQuery mixed = new DefaultQuery(typeName, filter, maxFeatures, propNames, handle);
        mixed.setVersion(version);
        return mixed;
    }

    /**
     * Creates a set of attribute names from the two input lists of names,
     * maintaining the order of the first list and appending the non repeated
     * names of the second.
     * <p>
     * In the case where both lists are <code>null</code>, <code>null</code> 
     * is returned.
     * </p>
     *
     * @param atts1 the first list of attribute names, who's order will be
     *        maintained
     * @param atts2 the second list of attribute names, from wich the non
     *        repeated names will be appended to the resulting list
     *
     * @return Set of attribute names from <code>atts1</code> and
     *         <code>atts2</code>
     */
    private static String[] joinAttributes(String[] atts1, String[] atts2) {
        String[] propNames = null;

        if ( atts1 == null && atts2 == null ) {
        	return null;
        }
        
        List atts = new LinkedList();

        if (atts1 != null) {
            atts.addAll(Arrays.asList(atts1));
        }

        if (atts2 != null) {
            for (int i = 0; i < atts2.length; i++) {
                if (!atts.contains(atts2[i])) {
                    atts.add(atts2[i]);
                }
            }
        }

        propNames = new String[atts.size()];
        atts.toArray(propNames);

        return propNames;
    }

    /**
     * Returns AttributeType based on String specification (based on UML).
     * 
     * <p>
     * Will parse a String of the form: <i>"name:Type:hint"</i>
     * </p>
     * 
     * <p>
     * Where <i>Type</i> is:
     * </p>
     * 
     * <ul>
     * <li>
     * 0,Interger,int: represents Interger
     * </li>
     * <li>
     * 0.0, Double, double: represents Double
     * </li>
     * <li>
     * "",String,string: represents String
     * </li>
     * <li>
     * Geometry: represents Geometry
     * </li>
     * <li>
     * <i>full.class.path</i>: represents java type
     * </li>
     * </ul>
     * 
     * <p>
     * Where <i>hint</i> is "hint1;hint2;...;hintN", in which "hintN" is one 
     * of:
     * <ul>
     *  <li><code>nillable</code></li>
     *  <li><code>srid=<#></code></li>
     * </ul>
     * </p>
     *
     * @param typeSpec
     *
     *
     * @throws SchemaException If typeSpect could not be interpreted
     */
    static AttributeDescriptor createAttribute(String typeSpec)
        throws SchemaException {
        int split = typeSpec.indexOf(":");

        String name;
        String type;
        String hint = null;

        if (split == -1) {
            name = typeSpec;
            type = "String";
        } else {
            name = typeSpec.substring(0, split);

            int split2 = typeSpec.indexOf(":", split + 1);

            if (split2 == -1) {
                type = typeSpec.substring(split + 1);
            } else {
                type = typeSpec.substring(split + 1, split2);
                hint = typeSpec.substring(split2 + 1);
            }
        }

        try {
            boolean nillable = true;
            CoordinateReferenceSystem crs = null;
            
            if ( hint != null ) {
                StringTokenizer st = new StringTokenizer( hint, ";" );
                while ( st.hasMoreTokens() ) {
                    String h = st.nextToken();
                    h = h.trim();
                    
                    //nillable?
                    //JD: i am pretty sure this hint is useless since the 
                    // default is to make attributes nillable
                    if ( h.equals( "nillable" )) {
                        nillable = true;
                    }
                    //spatial reference identieger?
                    if ( h.startsWith("srid=" )) {
                        String srid = h.split("=")[1];
                        Integer.parseInt( srid );
                        try {
                            crs = CRS.decode( "EPSG:" + srid );
                        } 
                        catch( Exception e ) {
                            String msg = "Error decoding srs: " + srid;
                            throw new SchemaException( msg, e );
                        }
                    }
                }
            }
            
            Class clazz = type(type);
            if(Geometry.class.isAssignableFrom(clazz)) {
            	GeometryType at = new GeometryTypeImpl(new Name( name ), clazz , crs, false, false, Collections.EMPTY_LIST, null, null );
	            return new GeometryDescriptorImpl( at, new Name(name), 0,1, nillable, null );
            } else {
	            AttributeType at = new AttributeTypeImpl( new Name( name ), clazz , false, false, Collections.EMPTY_LIST, null, null );
	            return new AttributeDescriptorImpl( at, new Name(name), 0,1, nillable, null );
            }
        } catch (ClassNotFoundException e) {
            throw new SchemaException("Could not type " + name + " as:" + type);
        }
    }

//    /**
//     * A quick and dirty FilterVisitor.
//     * 
//     * <p>
//     * This is useful when creating FilterVisitors for use with traverseDepth(
//     * Filter, FilterVisitor ) method.
//     * </p>
//     * 
//     * <p>
//     * visit( Filter ) and visit( Expression ) will pass their arguments off to
//     * more specialized functions.
//     * </p>
//     * @deprecated TODO: Traversal 
//     */
//    abstract static class AbstractFilterVisitor implements FilterVisitor {
//        /**
//         * DOCUMENT ME!
//         *
//         * @param filter DOCUMENT ME!
//         */
//        public void visit(Filter filter) {
//            if (filter instanceof BetweenFilter) {
//                visit((BetweenFilter) filter);
//            } else if (filter instanceof CompareFilter) {
//                visit((CompareFilter) filter);
//            } else if (filter instanceof GeometryFilter) {
//                visit((GeometryFilter) filter);
//            } else if (filter instanceof LikeFilter) {
//                visit((LikeFilter) filter);
//            } else if (filter instanceof LogicFilter) {
//                visit((LogicFilter) filter);
//            } else if (filter instanceof NullFilter) {
//                visit((NullFilter) filter);
//            } else if (filter instanceof FidFilter) {
//                visit((FidFilter) filter);
//            }
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param betweenFilter DOCUMENT ME!
//         */
//        public void visit(BetweenFilter betweenFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param comparefilter DOCUMENT ME!
//         */
//        public void visit(CompareFilter comparefilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param geometryFilter DOCUMENT ME!
//         */
//        public void visit(GeometryFilter geometryFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param likeFilter DOCUMENT ME!
//         */
//        public void visit(LikeFilter likeFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param logicFilter DOCUMENT ME!
//         */
//        public void visit(LogicFilter logicFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param nullFilter DOCUMENT ME!
//         */
//        public void visit(NullFilter nullFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param fidFilter DOCUMENT ME!
//         */
//        public void visit(FidFilter fidFilter) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param attributeExpression DOCUMENT ME!
//         */
//        public void visit(AttributeExpression attributeExpression) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param expression DOCUMENT ME!
//         */
//        public void visit(Expression expression) {
//            if (expression instanceof AttributeExpression) {
//                visit((AttributeExpression) expression);
//            } else if (expression instanceof LiteralExpression) {
//                visit((LiteralExpression) expression);
//            } else if (expression instanceof MathExpression) {
//                visit((MathExpression) expression);
//            } else if (expression instanceof FunctionExpression) {
//                visit((FunctionExpression) expression);
//            }
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param literalExpression DOCUMENT ME!
//         */
//        public void visit(LiteralExpression literalExpression) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param mathExpression DOCUMENT ME!
//         */
//        public void visit(MathExpression mathExpression) {
//            // DOCUMENT ME!
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param functionExpression DOCUMENT ME!
//         */
//        public void visit(FunctionExpression functionExpression) {
//            // DOCUMENT ME!
//        }
//    }
//
//    /**
//     * Will traverse the entire data structure
//     * 
//     * @deprecated Please use org.geotools.filter.visitor.AbstractFilterVisitor
//     */
//    abstract static class Traversal extends AbstractFilterVisitor {
//        abstract void traverse(Filter filter);
//
//        abstract void traverse(Expression expression);
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param betweenFilter DOCUMENT ME!
//         */
//        public void visit(BetweenFilter betweenFilter) {
//            traverse(betweenFilter.getLeftValue());
//            visit(betweenFilter.getLeftValue());
//
//            traverse(betweenFilter.getMiddleValue());
//            visit(betweenFilter.getMiddleValue());
//
//            traverse(betweenFilter.getRightValue());
//            visit(betweenFilter.getRightValue());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param compareFilter DOCUMENT ME!
//         */
//        public void visit(CompareFilter compareFilter) {
//            traverse(compareFilter.getLeftValue());
//            visit(compareFilter.getLeftValue());
//
//            traverse(compareFilter.getRightValue());
//            visit(compareFilter.getRightValue());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param geometryFilter DOCUMENT ME!
//         */
//        public void visit(GeometryFilter geometryFilter) {
//            traverse(geometryFilter.getLeftGeometry());
//            visit(geometryFilter.getLeftGeometry());
//
//            traverse(geometryFilter.getRightGeometry());
//            visit(geometryFilter.getRightGeometry());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param likeFilter DOCUMENT ME!
//         */
//        public void visit(LikeFilter likeFilter) {
//            traverse(likeFilter.getValue());
//            visit(likeFilter.getValue());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param logicFilter DOCUMENT ME!
//         */
//        public void visit(LogicFilter logicFilter) {
//            for (Iterator i = logicFilter.getFilterIterator(); i.hasNext();) {
//                // GR: LogicFilters are the only ones whose members are Filters
//                // instead of Expressions, so it was causing ClassCastExceptions
//                Filter f = (Filter) i.next();
//                traverse(f);
//                visit((org.geotools.filter.Filter) f);
//            }
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param nullFilter DOCUMENT ME!
//         */
//        public void visit(NullFilter nullFilter) {
//            traverse(nullFilter.getNullCheckValue());
//            visit(nullFilter.getNullCheckValue());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param mathExpression DOCUMENT ME!
//         */
//        public void visit(MathExpression mathExpression) {
//            traverse(mathExpression.getLeftValue());
//            visit(mathExpression.getLeftValue());
//
//            traverse(mathExpression.getRightValue());
//            visit(mathExpression.getRightValue());
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param functionExpression DOCUMENT ME!
//         */
//        public void visit(FunctionExpression functionExpression) {
//            Expression[] args = functionExpression.getArgs();
//
//            for (int i = 0; i < args.length; i++) {
//                traverse(args[i]);
//                visit(args[i]);
//            }
//        }
//    }
//	
	
    /**
     * Will traverse the entire data structure
     */
    abstract static class Traversal extends DefaultFilterVisitor {
        abstract void traverse(Filter filter);

        abstract void traverse(Expression expression);

        public Object visit(ExcludeFilter filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(IncludeFilter filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(And filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Id filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Not filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Or filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsBetween filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsEqualTo filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsNotEqualTo filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsGreaterThan filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsLessThan filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsLessThanOrEqualTo filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsLike filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(PropertyIsNull filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(BBOX filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Beyond filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Contains filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Crosses filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Disjoint filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(DWithin filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Equals filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Intersects filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Overlaps filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Touches filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visit(Within filter, Object data) {
            traverse(filter);
            return super.visit(filter, data);
        }

        public Object visitNullFilter(Object data) {
            return super.visitNullFilter(data);
        }

        public Object visit(NilExpression expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Add expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Divide expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Function expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Literal expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Multiply expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(PropertyName expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }

        public Object visit(Subtract expr, Object data) {
            traverse(expr);
            return super.visit(expr, data);
        }
    }
    
    /**
	 * Manually calculates the bounds of a feature collection.
	 * @param collection
	 * @return
	 */
	public static Envelope bounds( FeatureCollection collection ) {
		Iterator i = collection.iterator();
		try {
			ReferencedEnvelope bounds = new ReferencedEnvelope(collection.getSchema().getCRS());
			if ( !i.hasNext() ) {
				bounds.setToNull();
				return bounds;
			}
			
			bounds.init( ( (SimpleFeature) i.next() ).getBounds() );
			return bounds;
		}
		finally {
			collection.close( i );
		}
	}
}
