/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on August 12, 2003, 7:29 PM
 */
package org.geotools.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author jamesm
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/main/src/test/java/org/geotools/feature/MockFeatureCollection.java $
 */
public class MockFeatureCollection implements org.geotools.feature.FeatureCollection {

    /** Creates a new instance of MockFeatureCollection */
    public MockFeatureCollection() {
    }

    public void addListener( CollectionListener listener ) {
    }

    public FeatureIterator features() {
        return null;
    }

    public ReferencedEnvelope getBounds() {
        return null;
    }

    public void removeListener( CollectionListener listener ) {

    }

    public boolean add( Object o ) {
        return false;
    }

    public boolean addAll( java.util.Collection c ) {
        return false;
    }

    public void clear() {
    }

    public boolean contains( Object o ) {
        return false;
    }

    public boolean containsAll( java.util.Collection c ) {
        return false;
    }

    public boolean equals( Object o ) {
        return false;
    }

    public int hashCode() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public java.util.Iterator iterator() {
        return null;
    }

    public boolean remove( Object o ) {
        return false;
    }

    public boolean removeAll( java.util.Collection c ) {
        return false;
    }

    public boolean retainAll( java.util.Collection c ) {
        return false;
    }

    public int size() {
        return 0;
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray( Object[] a ) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#getFeatureType()
     */
    public FeatureType getFeatureType() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getParent()
     */
    public FeatureCollection getParent() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
     */
    public void setParent( FeatureCollection collection ) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getID()
     */
    public String getID() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
     */
    public Object[] getAttributes( Object[] attributes ) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
     */
    public Object getAttribute( String xPath ) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getAttribute(int)
     */
    public Object getAttribute( int index ) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
     */
    public void setAttribute( int position, Object val ) throws IllegalAttributeException,
            ArrayIndexOutOfBoundsException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#getNumberOfAttributes()
     */
    public int getNumberOfAttributes() {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {

    }

    public Geometry getPrimaryGeometry() {

        return null;
    }

    public void setPrimaryGeometry( Geometry geometry ) throws IllegalAttributeException {

    }

    public FeatureType getSchema() {
        return null;
    }

    public FeatureReader reader() throws IOException {
        return null;
    }

    public int getCount() throws IOException {
        return 0;
    }

    public FeatureCollection collection() throws IOException {
        return null;
    }

    public void close( Iterator iterator ) {
    }

    public void close( FeatureIterator iterator ) {
    }

    public void accepts( FeatureVisitor visitor, ProgressListener progress ) throws IOException {
    }

    public FeatureCollection subCollection( Filter filter ) {

        return null;
    }

    public FeatureCollection sort( SortBy order ) {

        return null;
    }

    public void purge() {

    }

    public Object getDefaultGeometryValue() {

        return null;
    }

    public SimpleFeatureCollectionType getType() {
        return null;
    }

    public List getTypes() {

        return null;
    }

    public Object getValue( String name ) {

        return null;
    }

    public Object getValue( int index ) {

        return null;
    }

    public List getValues() {

        return null;
    }

    public Object operation( String name, Object parameters ) {

        return null;
    }

    public void setDefaultGeometryValue( Object geometry ) {

    }

    public void setValue( String name, Object value ) {

    }

    public void setValue( int index, Object value ) {

    }

    public void setValues( List values ) {

    }

    public void setValues( Object[] values ) {

    }

    public CoordinateReferenceSystem getCRS() {

        return null;
    }

    public GeometryAttribute getDefaultGeometry() {

        return null;
    }

    public Object getUserData( Object key ) {

        return null;
    }

    public void putUserData( Object key, Object value ) {

    }

    public void setCRS( CoordinateReferenceSystem crs ) {

    }

    public void setDefaultGeometry( GeometryAttribute geometryAttribute ) {

    }

    public Collection associations() {

        return null;
    }

    public Collection attributes() {

        return null;
    }

    public Object getValue() {

        return null;
    }

    public List get( Name name ) {

        return null;
    }

    public AttributeDescriptor getDescriptor() {

        return null;
    }

    public void setValue( Object newValue ) throws IllegalArgumentException {

    }
    public void setValue( List values ) {

    }
    public boolean nillable() {

        return false;
    }

    public Object operation( Name name, List parameters ) {

        return null;
    }

    public PropertyDescriptor descriptor() {

        return null;
    }

    public Name name() {

        return null;
    }

    public SimpleFeatureCollectionType getFeatureCollectionType() {
        return null;
    }

    public List getAttributes() {
        return null;
    }
    public SimpleFeatureType getMemberType() {

        return null;
    }

    public Collection memberTypes() {
        return Collections.EMPTY_SET;
    }

    public void accepts( org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress ) {

    }
}
