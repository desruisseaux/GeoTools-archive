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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FilterVisitorFilterWrapper;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

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
import com.vividsolutions.jts.geom.PrecisionModel;


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
    public static String[] attributeNames(FeatureType featureType) {
        String[] names = new String[featureType.getAttributeCount()];

		final int count = featureType.getAttributeCount();
		for (int i = 0; i < count; i++) {
			names[i] = featureType.getAttributeType(i).getName();
		}

        return names;
    }

    /**
     * Traverses the filter and returns any encoutered property names.
     * <p>
     * The feautre type is supplied as contexts used to lookup expressions in cases where the 
     * attributeName does not match the actual name of the type.
     * </p>
     */
    public static String[] attributeNames( Filter filter, final FeatureType featureType ) {
    	 if (filter == null) {
             return new String[0];
         }

         final Set set = new HashSet();
         traverse(filter,
             new DataUtilities.AbstractFilterVisitor() {
                 public void visit(AttributeExpression attributeExpression) {
                	//evaluate to get actual attribute type
                	 if ( featureType != null ) {
                		 AttributeType type = 
                			 (AttributeType) attributeExpression.evaluate( featureType );
                		 if ( type != null ) {
                			 set.add( type.getName() );
                			 return;
                		 }
                	 }
                	
                	 set.add(attributeExpression.getAttributePath());
                 }
             });

         if (set.size() == 0) {
             return new String[0];
         }

         String[] names = new String[set.size()];
         int index = 0;

         for (Iterator i = set.iterator(); i.hasNext(); index++) {
             names[index] = (String) i.next();
         }
         return names;
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
    public static String[] attributeNames(Expression expression, final FeatureType featureType ) {
    	 if (expression == null) {
             return new String[0];
         }

         final Set set = new HashSet();
         traverse(expression,
             new DataUtilities.AbstractFilterVisitor() {
                 public void visit(AttributeExpression attributeExpression) {
                	//evaluate to get actual attribute type
                	 if ( featureType != null ) {
                		 AttributeType type = 
                			 (AttributeType) attributeExpression.evaluate( featureType );
                		 if ( type != null ) {
                			 set.add( type.getName() );
                			 return;
                		 }
                	 }
                	
                	 set.add(attributeExpression.getAttributePath());
                 }
             });

         if (set.size() == 0) {
             return new String[0];
         }

         String[] names = new String[set.size()];
         int index = 0;

         for (Iterator i = set.iterator(); i.hasNext(); index++) {
             names[index] = (String) i.next();
         }
         return names;
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

            if (here instanceof BetweenFilter) {
                visitor.visit((BetweenFilter) here);
            } else if (here instanceof CompareFilter) {
                visitor.visit((CompareFilter) here);
            } else if (here instanceof GeometryFilter) {
                visitor.visit((GeometryFilter) here);
            } else if (here instanceof LikeFilter) {
                visitor.visit((LikeFilter) here);
            } else if (here instanceof LogicFilter) {
                visitor.visit((LogicFilter) here);
            } else if (here instanceof NullFilter) {
                visitor.visit((NullFilter) here);
            } else if (here instanceof FidFilter) {
                visitor.visit((FidFilter) here);
            } else if (here instanceof Filter) {
                visitor.visit((org.geotools.filter.Filter) here);
            } else if (here instanceof AttributeExpression) {
                visitor.visit((AttributeExpression) here);
            } else if (here instanceof LiteralExpression) {
                visitor.visit((LiteralExpression) here);
            } else if (here instanceof MathExpression) {
                visitor.visit((MathExpression) here);
            } else if (here instanceof FunctionExpression) {
                visitor.visit((FunctionExpression) here);
            } else if (here instanceof Expression) {
                visitor.visit((org.geotools.filter.Filter) here);
            }
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

        filter.accept( new FilterVisitorFilterWrapper( traverse ), null );
        
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
        FilterVisitor traverse = new Traversal() {
                void traverse(Filter f) {
                    set.add(f);
                }

                void traverse(Expression expr) {
                    set.add(expr);
                }
            };

        expression.accept(traverse);

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
    public static int compare(FeatureType typeA, FeatureType typeB) {
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
        AttributeType a;

        // may still be the same featureType
        // (Perhaps they differ on namespace?)
        int match = 0;

        for (int i = 0; i < countA; i++) {
            a = typeA.getAttributeType(i);

            if (isMatch(a, typeB.getAttributeType(i))) {
                match++;
            } else if (isMatch(a, typeB.getAttributeType(a.getName()))) {
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
    public static boolean isMatch(AttributeType a, AttributeType b) {
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

        if (a.getName().equals(b.getName())
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
    public static Feature reType(FeatureType featureType, Feature feature)
        throws IllegalAttributeException {
        FeatureType origional = feature.getFeatureType();

        if (featureType.equals(origional)) {
            return featureType.duplicate(feature);
        }

        String id = feature.getID();
        int numAtts = featureType.getAttributeCount();
        Object[] attributes = new Object[numAtts];
        String xpath;

        for (int i = 0; i < numAtts; i++) {
            AttributeType curAttType = featureType.getAttributeType(i);
            xpath = curAttType.getName();
            attributes[i] = curAttType.duplicate(feature.getAttribute(xpath));
        }

        return featureType.create(attributes, id);
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
    public static Feature template(FeatureType featureType)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType));
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
    public static Feature template(FeatureType featureType, String featureID)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType), featureID);
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
    public static Object[] defaultValues(FeatureType featureType)
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
    public static Feature template(FeatureType featureType, Object[] atts)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType, atts));
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
    public static Feature template(FeatureType featureType, String featureID,
        Object[] atts) throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType, atts), featureID);
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
    public static Object[] defaultValues(FeatureType featureType,
        Object[] values) throws IllegalAttributeException {
        if (values == null) {
            values = new Object[featureType.getAttributeCount()];
        } else if (values.length != featureType.getAttributeCount()) {
            throw new ArrayIndexOutOfBoundsException("values");
        }

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            values[i] = defaultValue(featureType.getAttributeType(i));
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
    public static Object defaultValue(AttributeType attributeType)
        throws IllegalAttributeException {
            Object value = attributeType.createDefaultValue();
        
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
    public static FeatureReader reader(final Feature[] features)
        throws IOException {
        if ((features == null) || (features.length == 0)) {
            throw new IOException("Provided features where empty");
        }
    
        return new FeatureReader() {
                Feature[] array = features;
                int offset = -1;

                public FeatureType getFeatureType() {
                    return features[0].getFeatureType();
                }

                public Feature next(){
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
    public static FeatureSource source(final Feature[] featureArray) {
        final FeatureType featureType;

        if ((featureArray == null) || (featureArray.length == 0)) {
            featureType = DefaultFeatureType.EMPTY;
        } else {
            featureType = featureArray[0].getFeatureType();
        }

        DataStore arrayStore = new AbstractDataStore() {
                public String[] getTypeNames() {
                    return new String[] { featureType.getTypeName() };
                }

                public FeatureType getSchema(String typeName)
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
    
    public static FeatureCollection results(Feature[] featureArray){
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
        return reader((Feature[]) collection.toArray(
                new Feature[collection.size()]));
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
    public static FeatureCollection collection(Feature[] features) {
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
     * Often used when gathering features for FeatureStore:<pre><code>
     * featureStore.addFeatures( DataUtilities.collection(feature));
     * </code></pre>
     * 
     * @param features Array of features
     * @return FeatureCollection
     */
    public static FeatureCollection collection( Feature feature ){
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
     * @param features Array of features
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
     * @param features Array of features
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
    public static FeatureType createSubType(FeatureType featureType,
            String[] properties, CoordinateReferenceSystem override)
            throws SchemaException {
        return createSubType( featureType, properties, override, featureType.getTypeName(), featureType.getNamespace() );
    }

    public static FeatureType createSubType(FeatureType featureType,
        String[] properties, CoordinateReferenceSystem override, String typeName, URI namespace )
        throws SchemaException {
        
        if ((properties == null) && (override == null)) {
            return featureType;
        }
        
        if(properties == null) {
          properties = new String[featureType.getAttributeCount()];
          for (int i = 0; i < properties.length; i++) {
            properties[i] = featureType.getAttributeType(i).getName();
          }
        }

        boolean same = featureType.getAttributeCount() == properties.length &&
            featureType.getTypeName().equals( typeName ) &&
            featureType.getNamespace().equals( namespace );

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            AttributeType type = featureType.getAttributeType(i);
            same = type.getName().equals(properties[i])
                && (((override != null)
                && type instanceof GeometryAttributeType)
                ? assertEquals(override, ((GeometryAttributeType) type).getCoordinateSystem())
                : true);
        }

        if (same) {
            return featureType;
        }

        AttributeType[] types = new AttributeType[properties.length];

        for (int i = 0; i < properties.length; i++) {
            types[i] = featureType.getAttributeType(properties[i]);

            if ((override != null) && types[i] instanceof GeometryAttributeType) {
                types[i] = new GeometricAttributeType((GeometricAttributeType) types[i],
                        override);
            }
        }

        if( typeName == null ) typeName = featureType.getTypeName();
        if( namespace == null ) namespace = featureType.getNamespace();
        
        return FeatureTypeFactory.newFeatureType(types, typeName, namespace);
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
    public static FeatureType createSubType(FeatureType featureType,
        String[] properties) throws SchemaException {
        if (properties == null) {
            return featureType;
        }

        boolean same = featureType.getAttributeCount() == properties.length;

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            same = featureType.getAttributeType(i).getName().equals(properties[i]);
        }

        if (same) {
            return featureType;
        }

        AttributeType[] types = new AttributeType[properties.length];

        for (int i = 0; i < properties.length; i++) {
            types[i] = featureType.getAttributeType(properties[i]);
        }
        return FeatureTypeFactory.newFeatureType(types,
            featureType.getTypeName(), featureType.getNamespace());
    }

    /**
     * Utility method for FeatureType construction.
     * 
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
    public static FeatureType createType(String identification, String typeSpec)
        throws SchemaException {
        int split = identification.lastIndexOf('.');
        String namespace = (split == -1) ? null
                                         : identification.substring(0, split);
        String typeName = (split == -1) ? identification
                                        : identification.substring(split + 1);

        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(typeName);
        try {
            if( namespace != null ){
                typeFactory.setNamespace( new URI(namespace));
            }
        } catch (URISyntaxException badNamespace ) {
            throw new SchemaException( badNamespace );            
        }
        typeFactory.setName(typeName);

        String[] types = typeSpec.split(",");
        int geometryIndex = -1; // records * specified goemetry 
        AttributeType attributeType;
        GeometryAttributeType geometryAttribute = null; // records guess 

        for (int i = 0; i < types.length; i++) {
            if (types[i].startsWith("*")) {
                types[i] = types[i].substring(1);
                geometryIndex = i;
            }

            attributeType = createAttribute(types[i]);
            typeFactory.addType(attributeType);

            if ((geometryAttribute == null)
                    && attributeType instanceof GeometryAttributeType) {
                if (geometryIndex == -1) {
                    geometryAttribute = (GeometryAttributeType) attributeType;
                } else if (geometryIndex == i) {
                    geometryAttribute = (GeometryAttributeType) attributeType;
                }
            }
        }

        if (geometryAttribute != null) {
            typeFactory.setDefaultGeometry(geometryAttribute);
        }

        return typeFactory.getFeatureType();
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
    public static Feature parse(FeatureType type, String fid, String[] text)
        throws IllegalAttributeException {
        Object[] attributes = new Object[text.length];

        for (int i = 0; i < text.length; i++) {
            attributes[i] = type.getAttributeType(i).parse(text[i]);
        }

        return type.create(attributes, fid);
    }

    /**
     * Record typeSpec for the provided featureType
     *
     * @param featureType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String spec(FeatureType featureType) {
        AttributeType[] types = featureType.getAttributeTypes();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < types.length; i++) {
            buf.append(types[i].getName());
            buf.append(":");
            buf.append(typeMap(types[i].getType()));

            if (i < (types.length - 1)) {
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

        return new DefaultQuery(typeName, filter, maxFeatures, propNames, handle);
    }

    /**
     * Creates a set of attribute names from the two input lists of names,
     * maintaining the order of the first list and appending the non repeated
     * names of the second.
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
     * Where <i>hint</i> is "nilable".
     * </p>
     *
     * @param typeSpec
     *
     *
     * @throws SchemaException If typeSpect could not be interpreted
     */
    static AttributeType createAttribute(String typeSpec)
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
            if ((hint != null) && (hint.indexOf("nillable") != -1)) {
                return AttributeTypeFactory.newAttributeType(name, type(type),
                    true);
            }

            return AttributeTypeFactory.newAttributeType(name, type(type));
        } catch (ClassNotFoundException e) {
            throw new SchemaException("Could not type " + name + " as:" + type);
        }
    }

    /**
     * A quick and dirty FilterVisitor.
     * 
     * <p>
     * This is useful when creating FilterVisitors for use with traverseDepth(
     * Filter, FilterVisitor ) method.
     * </p>
     * 
     * <p>
     * visit( Filter ) and visit( Expression ) will pass their arguments off to
     * more specialized functions.
     * </p>
     */
    public abstract static class AbstractFilterVisitor implements FilterVisitor {
        /**
         * DOCUMENT ME!
         *
         * @param filter DOCUMENT ME!
         */
        public void visit(org.geotools.filter.Filter filter) {
            if (filter instanceof BetweenFilter) {
                visit((BetweenFilter) filter);
            } else if (filter instanceof CompareFilter) {
                visit((CompareFilter) filter);
            } else if (filter instanceof GeometryFilter) {
                visit((GeometryFilter) filter);
            } else if (filter instanceof LikeFilter) {
                visit((LikeFilter) filter);
            } else if (filter instanceof LogicFilter) {
                visit((LogicFilter) filter);
            } else if (filter instanceof NullFilter) {
                visit((NullFilter) filter);
            } else if (filter instanceof FidFilter) {
                visit((FidFilter) filter);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param betweenFilter DOCUMENT ME!
         */
        public void visit(BetweenFilter betweenFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param comparefilter DOCUMENT ME!
         */
        public void visit(CompareFilter comparefilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param geometryFilter DOCUMENT ME!
         */
        public void visit(GeometryFilter geometryFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param likeFilter DOCUMENT ME!
         */
        public void visit(LikeFilter likeFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param logicFilter DOCUMENT ME!
         */
        public void visit(LogicFilter logicFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param nullFilter DOCUMENT ME!
         */
        public void visit(NullFilter nullFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param fidFilter DOCUMENT ME!
         */
        public void visit(FidFilter fidFilter) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param attributeExpression DOCUMENT ME!
         */
        public void visit(AttributeExpression attributeExpression) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param expression DOCUMENT ME!
         */
        public void visit(Expression expression) {
            if (expression instanceof AttributeExpression) {
                visit((AttributeExpression) expression);
            } else if (expression instanceof LiteralExpression) {
                visit((LiteralExpression) expression);
            } else if (expression instanceof MathExpression) {
                visit((MathExpression) expression);
            } else if (expression instanceof FunctionExpression) {
                visit((FunctionExpression) expression);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param literalExpression DOCUMENT ME!
         */
        public void visit(LiteralExpression literalExpression) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param mathExpression DOCUMENT ME!
         */
        public void visit(MathExpression mathExpression) {
            // DOCUMENT ME!
        }

        /**
         * DOCUMENT ME!
         *
         * @param functionExpression DOCUMENT ME!
         */
        public void visit(FunctionExpression functionExpression) {
            // DOCUMENT ME!
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.23 $
     */
    public abstract static class Traversal extends AbstractFilterVisitor {
        abstract void traverse(Filter filter);

        abstract void traverse(Expression expression);

        /**
         * DOCUMENT ME!
         *
         * @param betweenFilter DOCUMENT ME!
         */
        public void visit(BetweenFilter betweenFilter) {
            traverse(betweenFilter.getLeftValue());
            visit(betweenFilter.getLeftValue());

            traverse(betweenFilter.getMiddleValue());
            visit(betweenFilter.getMiddleValue());

            traverse(betweenFilter.getRightValue());
            visit(betweenFilter.getRightValue());
        }

        /**
         * DOCUMENT ME!
         *
         * @param compareFilter DOCUMENT ME!
         */
        public void visit(CompareFilter compareFilter) {
            traverse(compareFilter.getLeftValue());
            visit(compareFilter.getLeftValue());

            traverse(compareFilter.getRightValue());
            visit(compareFilter.getRightValue());
        }

        /**
         * DOCUMENT ME!
         *
         * @param geometryFilter DOCUMENT ME!
         */
        public void visit(GeometryFilter geometryFilter) {
            traverse(geometryFilter.getLeftGeometry());
            visit(geometryFilter.getLeftGeometry());

            traverse(geometryFilter.getRightGeometry());
            visit(geometryFilter.getRightGeometry());
        }

        /**
         * DOCUMENT ME!
         *
         * @param likeFilter DOCUMENT ME!
         */
        public void visit(LikeFilter likeFilter) {
            traverse(likeFilter.getValue());
            visit(likeFilter.getValue());
        }

        /**
         * DOCUMENT ME!
         *
         * @param logicFilter DOCUMENT ME!
         */
        public void visit(LogicFilter logicFilter) {
            for (Iterator i = logicFilter.getFilterIterator(); i.hasNext();) {
		//GR: LogicFilters are the only ones whose members are Filters
		//instead of Expressions, so it was causing ClassCastExceptions
                Filter f = (Filter) i.next();
		traverse(f);
		visit((org.geotools.filter.Filter) f);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param nullFilter DOCUMENT ME!
         */
        public void visit(NullFilter nullFilter) {
            traverse(nullFilter.getNullCheckValue());
            visit(nullFilter.getNullCheckValue());
        }

        /**
         * DOCUMENT ME!
         *
         * @param mathExpression DOCUMENT ME!
         */
        public void visit(MathExpression mathExpression) {
            traverse(mathExpression.getLeftValue());
            visit(mathExpression.getLeftValue());

            traverse(mathExpression.getRightValue());
            visit(mathExpression.getRightValue());
        }

        /**
         * DOCUMENT ME!
         *
         * @param functionExpression DOCUMENT ME!
         */
        public void visit(FunctionExpression functionExpression) {
            Expression[] args = functionExpression.getArgs();

            for (int i = 0; i < args.length; i++) {
                traverse(args[i]);
                visit(args[i]);
            }
        }
    }
	/**
	 * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
	 * (temporary).
	 * 
	 * @param gridCoverage
	 *            the grid coverage
	 * 
	 * @return a feature with the grid coverage envelope as the geometry and the
	 *         grid coverage itself in the "grid" attribute
	 * @throws TransformException
	 * @throws SchemaException
	 * @throws FactoryConfigurationError
	 * @throws IllegalAttributeException
	 */
	public final static FeatureCollection wrapGc(GridCoverage gridCoverage)
			throws TransformException, FactoryConfigurationError,
			SchemaException, IllegalAttributeException {

		// create surrounding polygon
		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Rectangle2D rect = ((GridCoverage2D) gridCoverage)
				.getEnvelope2D();
		final CoordinateReferenceSystem

		sourceCrs = CRSUtilities.getCRS2D(((GridCoverage2D) gridCoverage)
				.getCoordinateReferenceSystem());

		// final boolean lonFirst = !GridGeometry2D.swapXY(sourceCrs
		// .getCoordinateSystem());
		final Coordinate[] coord = new Coordinate[5];
		// if (lonFirst) {
		coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
		coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
		coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
		coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
		coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
		// } else {
		// coord[0] = new Coordinate(rect.getMinY(), rect.getMinX());
		// coord[1] = new Coordinate(rect.getMaxY(), rect.getMinX());
		// coord[2] = new Coordinate(rect.getMaxY(), rect.getMaxX());
		// coord[3] = new Coordinate(rect.getMinY(), rect.getMaxX());
		// coord[4] = new Coordinate(rect.getMinY(), rect.getMinX());
		// }
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon bounds = new Polygon(ring, null, gf);

		// create the feature type
		final GeometricAttributeType geom = new GeometricAttributeType("geom",
				Polygon.class, true, 1, 1, null, sourceCrs, null);
		final AttributeType grid = AttributeTypeFactory.newAttributeType(
				"grid", GridCoverage.class);

		final AttributeType[] attTypes = { geom, grid };
		// Fix the schema name
		final String typeName = "GridCoverage";
		final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
				.newFeatureType(attTypes, typeName);

		// create the feature
		Feature feature = schema.create(new Object[] { bounds, gridCoverage });

		final FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(feature);

		return collection;
	}

	/**
	 * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
	 * (temporary).
	 * 
	 * @param gridCoverageReader
	 *            the grid coverage 
	 * 
	 * @return a feature with the grid coverage envelope as the geometry and the
	 *         grid coverage itself in the "grid" attribute
	 * @throws TransformException
	 * @throws SchemaException
	 * @throws FactoryConfigurationError
	 * @throws IllegalAttributeException
	 */
	public final static FeatureCollection wrapGcReader(
			AbstractGridCoverage2DReader gridCoverageReader)
			throws TransformException, FactoryConfigurationError,
			SchemaException, IllegalAttributeException {

		// create surrounding polygon
		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Rectangle2D rect = gridCoverageReader.getOriginalEnvelope()
				.toRectangle2D();
		final CoordinateReferenceSystem sourceCrs = CRSUtilities
				.getCRS2D(gridCoverageReader.getCrs());

		// TODO hack to be removed
		// final boolean lonFirst = !GridGeometry2D.swapXY(sourceCrs
		// .getCoordinateSystem());
		final Coordinate[] coord = new Coordinate[5];
		// if (lonFirst) {
		coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
		coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
		coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
		coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
		coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
		// } else {
		// coord[0] = new Coordinate(rect.getMinY(), rect.getMinX());
		// coord[1] = new Coordinate(rect.getMaxY(), rect.getMinX());
		// coord[2] = new Coordinate(rect.getMaxY(), rect.getMaxX());
		// coord[3] = new Coordinate(rect.getMinY(), rect.getMaxX());
		// coord[4] = new Coordinate(rect.getMinY(), rect.getMinX());
		// }
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon bounds = new Polygon(ring, null, gf);

		// create the feature type
		final GeometricAttributeType geom = new GeometricAttributeType("geom",
				Polygon.class, true, 1, 1, null, sourceCrs, null);
		final AttributeType grid = AttributeTypeFactory.newAttributeType(
				"grid", AbstractGridCoverage2DReader.class);

		final AttributeType[] attTypes = { geom, grid };
		// Fix the schema name
		final String typeName = "GridCoverageReader";
		final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
				.newFeatureType(attTypes, typeName);

		// create the feature
		Feature feature = schema.create(new Object[] { bounds,
				gridCoverageReader });

		final FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(feature);

		return collection;
	}
	
	/**
	 * Manually calculates the bounds of a feature collection.
	 * @param collection
	 * @return
	 */
	public static Envelope bounds( FeatureCollection collection ) {
		Iterator i = collection.iterator();
		try {
			Envelope bounds = new Envelope();
			if ( !i.hasNext() ) {
				bounds.setToNull();
				return bounds;
			}
			
			bounds.init( ( (Feature) i.next() ).getBounds() );
			return bounds;
		}
		finally {
			collection.close( i );
		}
	}
}
