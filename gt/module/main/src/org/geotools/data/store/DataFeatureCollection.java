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
package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.AbstractFeatureCollection;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.xml.gml.GMLSchema;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A starting point for implementing FeatureCollection's backed
 * by real data.
 * <p>
 * The API you are required to implement is *identical* the the barebones
 * FeatureResults interface:
 * <ul>
 * <li>getSchema()
 * <li>reader()
 * <li>getBounds()
 * <li>getCount()
 * <li>collection()
 * </p>
 * <p>
 * This class will implement the 'extra' methods required by FeatureCollection
 * for you (in simple terms based on the FeatureResults API). Anything that is
 * <i>often</i> customized is available to you as a constructor parameters.
 * <p>
 * Enjoy.
 * </p>
 * @author jgarnett
 * @since 2.1.RC0
 */
public abstract class DataFeatureCollection extends AbstractFeatureCollection {
    
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    static private int unique = 0;
    
    /**
     * Collection based on a generic collection
     */
    protected DataFeatureCollection(){
        this( "features"+(unique++) );
    }
    /**
     * Collection based on a generic collection
     */
    protected DataFeatureCollection( String id ){
        ID = id;
        featureType = null;
    }
    
    /** Subclass must think about what consitructors it needs. */
    protected DataFeatureCollection( String id, FeatureType featureType ){
        ID = id;
        this.featureType = featureType;
    }
    
    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(Feature[] features, int type) {        
        CollectionEvent cEvent = new CollectionEvent(this, features, type);
        
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener) listeners.get(i)).collectionChanged(cEvent);
        }
    }
    protected void fireChange(Feature feature, int type) {
        fireChange(new Feature[] {feature}, type);
    }    
    protected void fireChange(Collection coll, int type) {
        Feature[] features = new Feature[coll.size()];
        features = (Feature[]) coll.toArray(features);
        fireChange(features, type);
    }
    
    //
    // Feature Results methods
    // 
    // To be implemented by subclass
    //    
    public abstract FeatureType getSchema();

    public abstract FeatureReader reader() throws IOException;;

    public abstract Envelope getBounds();

    public abstract int getCount() throws IOException;;

    public abstract FeatureCollection collection() throws IOException;

    //
    // Additional Subclass "hooks"
    //
    /**
     * Subclass may provide an implementation of this method to indicate
     * that read/write support is provided.
     * <p>
     * All operations that attempt to modify the "data" will
     * use this method, allowing them to throw an "UnsupportedOperationException"
     * in the same manner as Collections.unmodifiableCollection(Collection c)
     * </p>
     * @throws UnsupportedOperationException To indicate that write support is not avaiable
     */
    protected FeatureWriter writer() throws IOException {
        throw new UnsupportedOperationException( "Modification of this collection is not supported" );
    }
    //
    // FeatureCollection methods
    // 
    // implemented in terms of feature results
    //
    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * FeatureIterator is entirely based on iterator().
     * <p>
     * So when we implement FeatureCollection.iterator() this will work
     * out of the box.
     */
    public FeatureIterator features() {
        return new FeatureIterator( this );
    }
   
    /**
     * Iterator may (or may) not support modification.
     */
    public Iterator iterator() {        
        FeatureWriter writer = null;
        try {
            writer = writer();
            return new FeatureWriterIterator( writer );
        }
        catch (IOException badWriter) {
            return new NoContentIterator( badWriter );
        }
        catch( UnsupportedOperationException readOnly ){
            // okay then lets try a reader based iterator
        }
        try {
            return new FeatureReaderIterator( reader() );
        } catch (IOException e) {
            return new NoContentIterator( e );
        }        
    }

    public void close( FeatureIterator iterator) {
        if( iterator != null ) iterator.close();
    }

    public void close( Iterator close ) {
        if( close == null ){
            // iterator probably failed during consturction !
        }
        else if( close instanceof FeatureReaderIterator ){
            FeatureReaderIterator iterator = (FeatureReaderIterator) close;
            iterator.close(); // only needs package visability
        }
        else if( close instanceof FeatureWriterIterator ){
            FeatureWriterIterator iterator = (FeatureWriterIterator) close;
            iterator.close(); // only needs package visability
        }
    }   

    /** Default implementation based on getCount() - this may be expensive */
    public int size() {
        try {
            return getCount();
        } catch (IOException e) {
            return 0;
        }
    }
    /**
     * Default implementation based on creating an reader, testing hasNext, and closing.
     * <p>
     * For once the Collections API does not give us an escape route, we *have* to check the data.
     * </p>
     */
    public boolean isEmpty() {
        FeatureReader reader = null;
        try {
            reader = reader();
            try {
                return !reader.hasNext();
            } catch (IOException e) {
                return true; // error seems like no features are available 
            }
        } catch (IOException e) {
            return true;
        }
        finally {
            if( reader != null ){
                try {
                    reader.close();
                } catch (IOException e) {
                    // return value already set
                }
            }
        }
    }

    public boolean contains( Object o ) {
        if( !(o instanceof Feature) ) return false;
        Feature value = (Feature) o;
        String ID = value.getID();
        
        FeatureReader reader = null;
        try {
            reader = reader();
            try {
                while( reader.hasNext() ){
                    Feature feature = reader.next();
                    if( !ID.equals( feature.getID() )){
                        continue; // skip with out full equal check
                    }
                    if( value.equals( feature )) return true;
                }
                return false; // not found
            } catch (IOException e) {
                return false; // error seems like no features are available 
            } catch (NoSuchElementException e) {
                return false; // error seems like no features are available
            } catch (IllegalAttributeException e) {
                return false; // error seems like no features are available
            }
        } catch (IOException e) {
            return false;
        }
        finally {
            if( reader != null ){
                try {
                    reader.close();
                } catch (IOException e) {
                    // return value already set
                }
            }
        }
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray( Object[] arg0 ) {
        return null;
    }

    public boolean add( Object arg0 ) {
        return false;
    }

    public boolean remove( Object arg0 ) {
        return false;
    }

    public boolean containsAll( Collection arg0 ) {
        return false;
    }

    public boolean addAll( Collection arg0 ) {
        return false;
    }

    public boolean removeAll( Collection arg0 ) {        
        return false;
    }

    public boolean retainAll( Collection arg0 ) {
        return false;
    }

    public void clear() {
        
    }

    //
    // Feature methods
    //
    // Remember the FT model is baed on the idea of a single AttributeType
    // of FeatureAttributeType with the value of getSchema
    //
    private FeatureCollection parent;
    private final String ID;
    /** The featureType of this actual colletion */
    FeatureType featureType;
    
    /**
     * FeatureType of this FeatureCollection.
     * <p>
     * Unless a FeatureType was provided during consturction (or this method is
     * overriden) a FeatureType will be generated based on getSchmea according
     * to the following assumptions:
     * <ul>
     * <li>FeatureType is gml:AbstractFeatureCollectionType
     * <li>first attribute is getSchema.typeName
     * <li>the attribute FeatureType the same as returned by getSchema()
     * </ul>
     * </p> 
     */
    public synchronized FeatureType getFeatureType() {
        if( featureType == null ){
            List ats = new LinkedList();
            ats.add(new FeatureAttributeType( getSchema().getTypeName(), getSchema(),false));
            featureType = new DefaultFeatureType("AbstractFeatureColletionType",GMLSchema.NAMESPACE,ats,new LinkedList(),null);        
        }
        return featureType;
    }
    public FeatureCollection getParent() {
        return parent; // TODO deal with listeners?        
    }
    public void setParent(FeatureCollection collection) {
        parent = collection;
    }    
    public String getID() {
        return ID;
    }
    public Object[] getAttributes( Object[] attributes ) {
        List list = (List) getAttribute( 0 );
        return list.toArray( attributes );        
    }
    /**
     * Not really interested yet .. 
     */
    public Object getAttribute( String xPath ) {
        if(xPath.indexOf(featureType.getTypeName())>-1)
            if(xPath.endsWith("]")){
                // TODO get index and grab it
                return getAttribute(0);
            }else{
                return getAttribute(0);
            }
        return null;
    }
    
    public Object getAttribute( int index ) {
        if(index == 0){
            FeatureReader reader = null;
            try {
                reader = reader();
                FeatureType schema = getSchema();
                
                List list = new ArrayList();
                while( reader.hasNext() ){
                    Feature feature = reader.next();
                    Feature copy = schema.duplicate( feature );
                    copy.setParent( this );
                    list.add( copy );
                }
                return list;
            } catch (IOException e) {
                return null; // could not find contents
            } catch (NoSuchElementException e) {
                return null; // could not find contents
            } catch (IllegalAttributeException e) {
                return null; // could not find contents
            }
            finally {
                if( reader != null ){
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }            
        }
        return null;        
    }
    
    public void setAttribute( int position, Object val ) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        if(position == 0 && val instanceof Collection){
            Collection list = (Collection)val;
            if( !isFeatures( list )) return;
            
            FeatureWriter writer = null;
            try {
                writer = writer();  // will error out if readOnly
                while( writer.hasNext() ){
                    writer.next();
                    writer.remove();
                }
                // add in list contents
                Feature feature = null;                
                for( Iterator i = list.iterator(); i.hasNext(); ){
                    feature = (Feature) i.next();    
                    
                    Feature newFeature = writer.next(); // grab a "new" Feature
                    newFeature.setParent( this );
                    Object values[] = feature.getAttributes( null );
                    for( int a=0; a<values.length; a++){
                        newFeature.setAttribute( a, values[a] );                        
                    }
                    writer.write();
                }                
            } catch (IOException io) {
                throw (ArrayIndexOutOfBoundsException)new ArrayIndexOutOfBoundsException().initCause( io );            
            }       
            finally {
                if( writer != null ){
                    try {
                        writer.close();
                    } catch (IOException io) {
                        throw (IllegalAttributeException) new IllegalAttributeException("Unsuccessful:"+io).initCause( io );                    
                    }
                }
            }
        }
    }

    public int getNumberOfAttributes() {
        return size();
    }

    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {
        if(xPath.indexOf(featureType.getTypeName())>-1){
            if(xPath.endsWith("]")){
                // TODO get index and grab it
            }else{
                setAttribute(0,attribute);
            }
        }
        /*
        FeatureWriter writer = null;
        try {
            writer = writer();  // will error out if readOnly
            for( int index=0; writer.hasNext(); index++ ){
                Feature feature = writer.next();
                if( index != position ) break;
                
            }
        } catch (IOException io) {
            throw (ArrayIndexOutOfBoundsException)new ArrayIndexOutOfBoundsException().initCause( io );            
        }       
        finally {
            if( writer != null ){
                try {
                    writer.close();
                } catch (IOException io) {
                    throw (IllegalAttributeException) new IllegalAttributeException("Unsuccessful:"+io).initCause( io );                    
                }
            }
        }*/
    }

    public Geometry getDefaultGeometry() {
        return null;
    }

    public void setDefaultGeometry( Geometry geometry ) throws IllegalAttributeException {
        throw new IllegalAttributeException( "DefaultGeometry not supported" );
    }
    
}