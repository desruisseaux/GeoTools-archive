/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature.collection;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.opengis.filter.Filter;
import org.geotools.filter.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Implementation of FeatureList to get you started.
 * You will need to provide a FeatureState for collection attributes, and
 * implement the following:
 * <ul>
 * <li>features()
 * <li>close(FeatureIterator)
 * <li>getSchema()
 * <li>accepts(FeatureVisitor)
 * <li>subList(Filter)
 * <li>subCollection(Filter)
 * <li>sort(SortBy)
 * </ul>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class AbstractFeatureList extends AbstractResourceList implements FeatureList {
    FeatureState state;

    /** Feature methods will be delegated to provided state */
    protected AbstractFeatureList( FeatureState state ){
        this.state = state;
    }
    /** Feature methods will be delegated to provided state */
    protected AbstractFeatureList( FeatureCollection collection ){
        this.state = new SubFeatureState( collection, this );
    }

    //
    // FeatureCollection - Feature methods
    //
    public FeatureCollection getParent() {
        return state.getParent();
    }
    public void setParent( FeatureCollection collection ) {
        state.setParent( collection );
    }
    public FeatureType getFeatureType() {
        return state.getFeatureType();
    }
    public String getID() {
        return state.getId();
    }
    public Object[] getAttributes( Object[] attributes ) {
        return state.getAttributes( attributes );
    }
    public Object getAttribute( String xPath ) {
        return state.getAttribute( xPath );
    }
    public Object getAttribute( int index ) {
        return state.getAttribute( index );
    }
    public void setAttribute( int position, Object val ) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        state.setAttribute( position, val );
    }
    public int getNumberOfAttributes() {
        return state.getNumberOfAttributes();
    }
    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {
        state.setAttribute( xPath, attribute );
    }
    public Geometry getDefaultGeometry() {
        return state.getDefaultGeometry();
    }
    public void setDefaultGeometry( Geometry geometry ) throws IllegalAttributeException {
        state.setDefaultGeometry( geometry );
    }
    public Envelope getBounds() {
        return state.getBounds();
    }
    //
    // FeatureCollection - Events
    //
    public void addListener( CollectionListener listener ) {
        state.addListener( listener );
    }
    public void removeListener( CollectionListener listener ) throws NullPointerException {
        state.removeListener( listener );
    }
    
    //
    // FeatureCollection - Feature Access
    // 
    public FeatureIterator features() {
        // TODO Auto-generated method stub
        return null;
    }
    public void close( FeatureIterator close ) {
        // TODO Auto-generated method stub        
    }
    public FeatureType getSchema() {
        // TODO Auto-generated method stub
        return null;
    }
    public void accepts( FeatureVisitor visitor ) throws IOException {
        // TODO Auto-generated method stub        
    }
        
    //
    // FeatureCollection - Collection API
    //
    public FeatureList subList( Filter filter ) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public FeatureCollection subCollection( Filter filter ) {
        // TODO Auto-generated method stub
        return null;
    }
    public FeatureList sort( SortBy order ) {
        // TODO Auto-generated method stub
        return null;
    }
    //
    // FeatureCollection - Legacy
    //
    public FeatureReader reader() throws IOException {
        return new DelegateFeatureReader( getSchema(), features() );
    }
    public int getCount() throws IOException {
        return size();
    }
    public FeatureCollection collection() throws IOException {
        return this;
    }
}