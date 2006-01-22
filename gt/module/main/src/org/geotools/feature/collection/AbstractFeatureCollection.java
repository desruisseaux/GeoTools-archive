package org.geotools.feature.collection;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractFeatureCollection extends AbstractResourceCollection implements FeatureCollection {
    FeatureState state;

    /**
     * Feature methods will be delegated to provided state.
     * <p>
     * You can use this implemenation with a choice of stratagy objects:
     * <ul>
     * <li>BaseFeatureState - when this collection is independent
     * <li>SubFeatureState - when this collection delegates content
     * </ul> 
     */
    protected AbstractFeatureCollection( FeatureState state ){
        this.state = state;
    }

    /**
     * Creates an AbstractFeatureCollection delegating the FeatureState
     * implementaion content to iterator() and close( iterator ).
     * 
     * @param schema
     */
    public AbstractFeatureCollection( FeatureType schema ) {
        state = new BaseFeatureState( this, schema );
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
    public FeatureType getSchema() {
        return state.getChildFeatureType();
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
        FeatureIterator iter = new DelegateFeatureIterator( this, openIterator() );
        open.add( iter );
        return iter; 
    }
    public void close( FeatureIterator close ) {     
        closeIterator( close );
        open.remove( close );
    }
    public void closeIterator( FeatureIterator close ) {
        DelegateFeatureIterator iter = (DelegateFeatureIterator) close;
        closeIterator( iter.delegate );
        iter.close(); 
    }
    public void purge() {
        for( Iterator i = open.iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof FeatureIterator ){
                FeatureIterator resourceIterator = (FeatureIterator) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
        }        
        super.purge();
    }
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
        
    //
    // Feature Collections API
    //
    public FeatureList subList( Filter filter ) {
        return new SubFeatureList(this, filter );
    }
    
    public FeatureCollection subCollection( Filter filter ) {
        return new SubFeatureCollection( this, filter );
    }

    public FeatureList sort( SortBy order ) {
        return new SubFeatureList(this, order );
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