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
package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.feature.collection.FeatureState;
import org.geotools.feature.collection.SubFeatureCollection;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.feature.visitor.FeatureVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.geotools.filter.SortBy2;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;

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
 * @source $URL$
 */
public abstract class DataFeatureCollection implements FeatureCollection {
    
	/** logger */
	static Logger LOGGER = Logger.getLogger( "org.geotools.data" );
	
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
    
    public FeatureReader reader() throws IOException {
    	return new DelegateFeatureReader( getSchema(), features() );
    }
    
    //
    // Feature Results methods
    // 
    // To be implemented by subclass
    //    
    public abstract FeatureType getSchema();

    public abstract Envelope getBounds();

    public abstract int getCount() throws IOException;;

    //public abstract FeatureCollection collection() throws IOException;

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
    
    //
    // Content Access
    //
    /** Set of open resource iterators & featureIterators */
    private final Set open = new HashSet();

    /**
     * FeatureIterator is entirely based on iterator().
     * <p>
     * So when we implement FeatureCollection.iterator() this will work
     * out of the box.
     */
    public FeatureIterator features() {
    	FeatureIterator iterator = new DelegateFeatureIterator( this, iterator() );
        open.add( iterator );
        return iterator;
    }
   
    /**
     * Iterator may (or may) not support modification.
     */
    final public Iterator iterator() {
    	Iterator iterator;
		try {
			iterator = openIterator();
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
		
    	open.add( iterator );
    	return iterator;    	    	
    }
    
    /**
     * Returns a FeatureWriterIterator, or FeatureReaderIterator over content.
     * <p>
     * If you have a way to tell that you are readonly please subclass with
     * a less hardcore check - this implementations catches a
     * UnsupportedOpperationsException from wrtier()!
     * 
     * @return Iterator, should be closed closeIterator 
     */
    protected Iterator openIterator() throws IOException
    {    	
    	try {
            return new FeatureWriterIterator( writer() );
        }
        catch (IOException badWriter) {
            return new NoContentIterator( badWriter );
        }
        catch( UnsupportedOperationException readOnly ){
        }
        try {
            return new FeatureReaderIterator( reader() );
        } catch (IOException e) {
            return new NoContentIterator( e );
        }        
    }

    final public void close( Iterator close ) {
    	try {
			closeIterator( close );
		} 
    	catch (IOException e) {
			LOGGER.log( Level.WARNING, "Error closing iterator", e );
		}
    	open.remove( close );
    }   
    
    protected void closeIterator( Iterator close ) throws IOException
    {
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
    
    public void close( FeatureIterator iterator) {
    	iterator.close();
        open.remove( iterator );        
    }

   
    
    /** Default implementation based on getCount() - this may be expensive */
    public int size() {
        try {
            return getCount();
        } catch (IOException e) {
            return 0;
        }
    }
    public void purge(){    	
    	for( Iterator i = open.iterator(); i.hasNext(); ){
    		Object iterator =  i.next();
    		try {
    			if( iterator instanceof Iterator ){
    				closeIterator( (Iterator) iterator );
    			}
    			if( iterator instanceof FeatureIterator){
    				( (FeatureIterator) iterator ).close();
    			}
    		}
    		catch( Throwable e){
    			// TODO: Log e = ln
    		}
    		finally {
    			i.remove();
    		}
    	}
    }
    //
    // Off into implementation land!
    //
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
        return toArray( new Feature[ size() ]);
    }

    public Object[] toArray( Object[] array ) {
        List list = new ArrayList();
        Iterator i = iterator();
        try {
            while( i.hasNext() ){
                list.add( i.next() );
            }
        }
        finally {
            close( i );
        }
        return list.toArray( array );
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

    /**
     * Optimized implementation of addAll that recognizes the
     * use of collections obtained with subCollection( filter ).
     * <p>
     * This method is constructed by either:
     * <ul>
     * <li>Filter OR
     * <li>Removing an extact match of Filter AND
     * </ul>
     * 
     */
    public boolean addAll(Collection arg0) {
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
            featureType = new DefaultFeatureType("AbstractFeatureCollectionType",FeatureTypes.DEFAULT_NAMESPACE,ats,new LinkedList(),null);        
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
            if( !FeatureState.isFeatures( list )) return;
            
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

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator iterator = null;
        if (progress == null) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
        	for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext(); progress.progress( position++/size )){
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
	        }            
        }
        finally {
            progress.complete();            
        	close( iterator );
        }
	}
    
    /**
     * Will return an optimized subCollection based on access
     * to the origional FeatureSource.
     * <p>
     * The subCollection is constructed by using an AND Filter.
     * For the converse of this opperation please see
     * collection.addAll( Collection ), it has been optimized
     * to be aware of these filter based SubCollections.
     * </p>
     * <p>
     * This method is intended in a manner similar to subList,
     * example use:
     * <code>
     * collection.subCollection( myFilter ).clear()
     * </code>
     * </p>    
     * @param filter Filter used to determine sub collection.
     * @since GeoTools 2.2, Filter 1.1
     */
    public FeatureCollection subCollection(Filter filter) {
    	return new SubFeatureCollection( this, filter );
    }

    /**
     * Construct a sorted view of this content.
     * <p>
     * Sorts may be combined togther in a stable fashion, in congruence
     * with the Filter 1.1 specification.
     * </p>
     * This method should also be able to handle GeoTools specific
     * sorting through detecting order as a SortBy2 instance.
     * 
     * @param order
     * 
     * @since GeoTools 2.2, Filter 1.1
     * @return FeatureList sorted according to provided order

     */
    public FeatureList sort(SortBy order) {
    	if( order instanceof SortBy2){
    		SortBy2 advanced = (SortBy2) order;
    		return sort( advanced );
    	}
    	return null; // new OrderedFeatureList( this, order );
    }
    /**
     * Allows for "Advanced" sort capabilities specific to the
     * GeoTools platform!
     * <p>
     * Advanced in this case really means making use of a generic
     * Expression, rather then being limited to PropertyName.
     * </p>
     * @param order GeoTools SortBy
     * @return FeatureList sorted according to provided order
     */
    public FeatureList sort(SortBy2 order ){
    	return null;
    }    
}
