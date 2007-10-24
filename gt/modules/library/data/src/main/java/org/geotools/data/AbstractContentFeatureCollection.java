package org.geotools.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.data.store.FeatureIteratorIterator;
import org.geotools.factory.Hints;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.util.ProgressListener;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.sort.SortBy;

public abstract class AbstractContentFeatureCollection implements ContentFeatureCollection {

    /**
     * data store the collection originated from
     */
    protected ContentDataStore dataStore;
    /**
     * feature store the collection originated from.
     */
    protected ContentFeatureSource featureSource;
    /**
     * state of the feature source 
     */
    protected ContentState state;
    
    /** Internal listener storage list */
    protected List listeners = new ArrayList(2);

    /** Set of open resource iterators */
    protected final Set open = new HashSet();
    
    /** hints */
    protected Hints hints = new Hints(Collections.EMPTY_MAP);
    
    /**
     * feature listener which listens to the feautre source and 
     * forwards events to its listeners.
     */
    FeatureListener listener = new FeatureListener(){
        public void changed(FeatureEvent featureEvent) {
            if( listeners.isEmpty() ) return;

            FeatureCollection collection = (FeatureCollection) AbstractContentFeatureCollection.this;
            CollectionEvent event = new CollectionEvent( collection, featureEvent );

            CollectionListener[] notify = (CollectionListener[]) listeners.toArray( new CollectionListener[ listeners.size() ]);
            for( int i=0; i<notify.length; i++ ){
                CollectionListener listener = notify[i];
                try {
                    listener.collectionChanged( event );
                }
                catch (Throwable t ){
                    //TODO: log this
                    //ContentDataStore.LOGGER.log( Level.WARNING, "Problem encountered during notification of "+event, t );
                }
            }
        }           
    };
    
    protected AbstractContentFeatureCollection( ContentFeatureStore featureSource, ContentState state ) {
        this.featureSource = featureSource;
        this.dataStore = featureSource.getDataStore();
        this.state = state;
        
        //add the feautre source listener
        featureSource.addFeatureListener(listener);
    }
    
    public SimpleFeatureType getSchema() {
        return featureSource.getSchema();
    }
    
    public void setHints(Hints hints) {
        this.hints = hints;
    }
    
    public Hints getHints() {
        return hints;
    }
    
    //Visitors
    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext();){
                if (size > 0) progress.progress( position++/size );
                try {
                    SimpleFeature feature = (SimpleFeature) iterator.next();
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

    public void accepts(org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) throws IOException {
    }
    
    
    //Listeners
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
    
    // Iterators
    //public FeatureIterator features(){
    public FeatureIterator features(){
        try {
            return open( createFeatureIterator() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    protected abstract FeatureIterator createFeatureIterator() throws Exception;
    
    public FeatureIterator writer() {
        try {
            return open( createFeatureWriter() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    protected abstract FeatureIterator createFeatureWriter() throws Exception;
    
    public FeatureIterator inserter() {
        try {
            return open( createFeatureInserter() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    protected abstract FeatureIterator createFeatureInserter() throws Exception;
    
    protected FeatureIterator open( FeatureIterator iterator ) {
        // keep track of the iterator
        open.add( iterator );
        return iterator;
    }
    public void close( FeatureIterator iterator ) {
        iterator.close();
        open.remove( iterator );
    }
    
    public Iterator iterator() {
        FeatureIterator iterator = features();
        return new FeatureIteratorIterator( iterator );
    }
    
    public void close(Iterator close) {
        if ( close instanceof FeatureIteratorIterator ) {
            FeatureIteratorIterator iterator = (FeatureIteratorIterator) close;
            close( iterator.getDelegate() );
        }
    }
    
    //Unsupported
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection arg0) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection arg0) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection arg0) {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray(Object[] arg0) {
        throw new UnsupportedOperationException();
    }

    public FeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(Name name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(int indedx) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public int getAttributeCount() {
        throw new UnsupportedOperationException();
    }

    public List<Object> getAttributes() {
        throw new UnsupportedOperationException();
    }

    public Object getDefaultGeometry() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getFeatureType() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getType() {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(Name name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(int index, Object value)
            throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public void setAttributes(List<Object> attributes) {
        throw new UnsupportedOperationException();
    }

    public void setAttributes(Object[] attributes) {
        throw new UnsupportedOperationException();
    }

    public void setDefaultGeometry(Object defaultGeometry) {
        throw new UnsupportedOperationException();
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        throw new UnsupportedOperationException();
    }

    public String getID() {
        throw new UnsupportedOperationException();
    }

    public void setDefaultGeometryProperty(GeometryAttribute defaultGeometryProperty) {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties() {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties(Name name) {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties(String name) {
        throw new UnsupportedOperationException();
    }

    public Property getProperty(Name name) {
        throw new UnsupportedOperationException();
    }

    public Property getProperty(String name) {
        throw new UnsupportedOperationException();
    }

    public Collection<? extends Property> getValue() {
        throw new UnsupportedOperationException();
    }

    public void setValue(Collection<Property> value) {
        throw new UnsupportedOperationException();
    }

    public AttributeDescriptor getDescriptor() {
        throw new UnsupportedOperationException();
    }

    public Name getName() {
        throw new UnsupportedOperationException();
    }

    public Map<Object, Object> getUserData() {
        throw new UnsupportedOperationException();
    }

    public boolean isNillable() {
        throw new UnsupportedOperationException();
    }

    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }
}
