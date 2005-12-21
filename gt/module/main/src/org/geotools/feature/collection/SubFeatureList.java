package org.geotools.feature.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureList;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.SortBy;
import org.geotools.filter.SortOrder;

public class SubFeatureList extends SubFeatureCollection implements FeatureList, RandomFeatureAccess {
    List sort; 
    List index;
    
    public SubFeatureList(FeatureCollection list, Filter filter){
        this( list, filter, null );
    }
    public SubFeatureList(FeatureCollection list, SortBy sort ){
        this( list, null, sort );
    }
    /**
	 * Create a simple SubFeatureList with the provided
	 * filter.
	 * 
	 * @param filter
	 */
	public SubFeatureList(FeatureCollection list, Filter filter, SortBy subSort) {
		super( list,  filter );
        state = new SubFeatureState( list, this );
        if( subSort == null ){
            sort = Collections.EMPTY_LIST;
        } else {
            sort = new ArrayList();                
            if (collection instanceof SubFeatureList) {
                SubFeatureList sorted = (SubFeatureList) collection;                    
                sort.addAll( sorted.sort );
            }
            sort.add( subSort );
        }
        index = null;                
	}
    
    public SubFeatureList(FeatureCollection list, List order) {
        super( list );        
        state = new SubFeatureState( list, this );        
        index = order;
        filter = null;
    }
    
    /** Lazy create a filter based on index */
    protected Filter createFilter() {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter fids = ff.createFidFilter();
        fids.addAllFids( index );
            
        return fids;
    }
    
    protected List index(){
        if( index == null ){
            index = createIndex();
        }
        return index;
    }

    /** Put this SubFeatureList in touch with its inner index */
    protected List createIndex(){
        List fids = new ArrayList();        
        Iterator it = collection.iterator();
        try {            
            while( it.hasNext() ){
                Feature feature = (Feature) it.next();
                if( filter.contains( feature ) ){
                    fids.add( feature.getID() );
                }
            }
            if( sort != null && !sort.isEmpty()){
                final SortBy initialOrder = (SortBy) sort.get( sort.size() -1 );                
                Collections.sort( fids, new Comparator(){
                    public int compare( Object key1, Object key2 ) {
                        Feature feature1 = getFeatureMember( (String) key1 );
                        Feature feature2 = getFeatureMember( (String) key2 );
                        
                        int compare = compare( feature1, feature2, initialOrder );
                        if( compare == 0 && sort.size() > 1 ){
                            for( int i=sort.size()-1; compare == 0 && i>=0; i--){
                                compare = compare( feature1, feature2, (SortBy) sort.get( i ));
                            }                            
                        }                        
                        return compare;
                    }
                    protected int compare( Feature feature1, Feature feature2, SortBy order){
                        AttributeExpression name = order.getPropertyName();
                        Comparable value1 = (Comparable) name.getValue( feature1 );
                        Comparable value2 = (Comparable) name.getValue( feature2 );
                        
                        if( order.getSortOrderType() == SortOrder.ASCENDING ){
                            return value1.compareTo( value2 );
                        }
                        else return value2.compareTo( value1 );                        
                    }
                });
            }
        }
        finally {
            collection.close( it );
        }
        return fids;
    }

    public boolean addAll(int index, Collection c) {
        boolean modified = false;
        Iterator e = c.iterator();
        try {
            while (e.hasNext()) {
                add(index++, e.next());
                modified = true;
            }
            return modified;
        }
        finally {
            if( c instanceof ResourceCollection ){
                ((ResourceCollection)c).close( e );
            }
        }
    }

    public Object get( int index ) {
        if( collection instanceof RandomFeatureAccess){
            RandomFeatureAccess random = (RandomFeatureAccess) collection;
            String id = (String) index().get( index );            
            random.getFeatureMember( id );
        }
        Iterator it = iterator();
        try {
            for( int i=0; it.hasNext(); i++){
                Feature feature = (Feature) it.next();
                if( i == index ){
                    return feature;
                }
            }
            throw new IndexOutOfBoundsException();
        }
        finally {
            close( it );
        }
    }
    
    public Object set( int index, Object feature ) {
        throw new UnsupportedOperationException();
    }
    
    public void add( int index, Object feature ) {
        throw new UnsupportedOperationException();
    }
    
    public Object remove( int index ) {
        String fid = (String) index().get( index );
        Feature feature = getFeatureMember( fid );
        
        if( collection.remove( feature ) )
            return feature;
        
        return null;
    }
    public int indexOf( Object o ) {
        Feature feature = (Feature) o;
        return index().indexOf( feature.getID() );
    }
    public int lastIndexOf( Object o ) {
        Feature feature = (Feature) o;
        return index().lastIndexOf( feature.getID() );
    }
    public ListIterator listIterator() {
        return listIterator( 0 );
    }
    public ListIterator listIterator(final int index) {
        if (index<0 || index>size())
            throw new IndexOutOfBoundsException("Index: "+index);
        ListIterator iterator = openIterator( index );
        open.add( iterator );
        return iterator;                        
    }
    public ListIterator openIterator( final int index ){           
       return new SubListItr(index);
    }
    class SubListItr implements ListIterator {
        ListIterator it;
        String fid;
        public SubListItr( int fromIndex ){
            it = index().subList( fromIndex,index.size() ).listIterator();
        }
        public boolean hasNext() {
            return it.hasNext();
        }
        public Object next() {
            fid = (String) it.next();
            return getFeatureMember( fid );
        }
        public void remove() {            
            it.remove();
            if( fid == null )
                throw new IllegalStateException();
            removeFeatureMember( fid );
            index.remove( fid );            
        }
        public boolean hasPrevious() {
            return it.hasPrevious();
        }
        public Object previous() {
            fid = (String) it.previous();
            return getFeatureMember( fid );
        }
        public int nextIndex() {
            return it.nextIndex();
        }
        public int previousIndex() {
            return it.previousIndex();
        }
        public void set( Object arg0 ) {
            throw new UnsupportedOperationException();
        }
        public void add( Object feature ) {
            SubFeatureList.this.add( it.nextIndex()-1,feature );
        }
    }
    
    //
    // Fature Collection methods
    //
    /**
     * Sublist of this sublist!
     * <p>
     * Implementation will ensure this does not get out of hand, order
     * is maintained and only indexed once.
     * </p>
     */
    public FeatureList subList(Filter subfilter) {
        return new SubFeatureList( this, subfilter );
    }
    //
    // RandomFeatureAccess
    //
    
    public Feature getFeatureMember( String id ) throws NoSuchElementException {
        int position = index.indexOf( id );
        if( position == -1){
            throw new NoSuchElementException(id);
        }        
        if( collection instanceof RandomFeatureAccess ){
            RandomFeatureAccess random = (RandomFeatureAccess) collection;
            random.getFeatureMember( id ); 
        }
        return (Feature) get( position );
    }
    public Feature removeFeatureMember( String id ) {
        int position = index.indexOf( id );
        if( position == -1){
            throw new NoSuchElementException(id);
        }        
        if( collection instanceof RandomFeatureAccess ){
            RandomFeatureAccess random = (RandomFeatureAccess) collection;
            if( index != null ) index.remove( id );            
            return random.removeFeatureMember( id );            
        }
        return (Feature) remove( position );
    }
    //
    // FeatureList
    //
    public List subList( int fromIndex, int toIndex ) {
        return new SubFeatureList( this, index().subList( fromIndex, toIndex ));
    }
    public FeatureList sort( SortBy order ) {
        return super.sort(order);
    }
   
}
