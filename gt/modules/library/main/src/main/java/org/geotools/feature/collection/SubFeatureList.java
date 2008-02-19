/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.collection.ResourceList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

public class SubFeatureList extends SubFeatureCollection implements RandomFeatureAccess {
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
         
        index = order;
        filter = null;
    }
    
    AbstractResourceCollection createResourceCollection() {
    	return new DefaultResourceList(
    		new DefaultResourceList.Strategy() {

    			 public Object get( int index ) {
    			        if( collection instanceof RandomFeatureAccess){
    			            RandomFeatureAccess random = (RandomFeatureAccess) collection;
    			            String id = (String) index().get( index );            
    			            random.getFeatureMember( id );
    			        }
    			        Iterator it = iterator();
    			        try {
    			            for( int i=0; it.hasNext(); i++){
    			                SimpleFeature feature = (SimpleFeature) it.next();
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
    			 
				public int size() {
					// TODO Auto-generated method stub
					return 0;
				}
    			
    		}
    	);
    }
    
    /** Lazy create a filter based on index */
    protected Filter createFilter() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Set featureIds = new HashSet();
        for(Iterator it = index.iterator(); it.hasNext();){
           featureIds.add(ff.featureId((String) it.next())); 
        }
        Id fids = ff.id(featureIds);
            
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
                SimpleFeature feature = (SimpleFeature) it.next();
                if( filter.evaluate(feature ) ){
                    fids.add( feature.getID() );
                }
            }
            if( sort != null && !sort.isEmpty()){
                final SortBy initialOrder = (SortBy) sort.get( sort.size() -1 );                
                Collections.sort( fids, new Comparator(){
                    public int compare( Object key1, Object key2 ) {
                        SimpleFeature feature1 = getFeatureMember( (String) key1 );
                        SimpleFeature feature2 = getFeatureMember( (String) key2 );
                        
                        int compare = compare( feature1, feature2, initialOrder );
                        if( compare == 0 && sort.size() > 1 ){
                            for( int i=sort.size()-1; compare == 0 && i>=0; i--){
                                compare = compare( feature1, feature2, (SortBy) sort.get( i ));
                            }                            
                        }                        
                        return compare;
                    }
                    protected int compare( SimpleFeature feature1, SimpleFeature feature2, SortBy order){
                        PropertyName name = order.getPropertyName();
                        Comparable value1 = (Comparable) name.evaluate( feature1 );
                        Comparable value2 = (Comparable) name.evaluate( feature2 );
                        
                        if( order.getSortOrder() == SortOrder.ASCENDING ){
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

    
    public void add(int index, Object element) {
		((ResourceList)rc).add(index,element);
	}
	public boolean addAll(int index, Collection c) {
		return ((ResourceList)rc).addAll(index,c);
	}
	public Object get(int index) {
		return ((ResourceList)rc).get(index);
	}
	public int indexOf(Object o) {
		return ((ResourceList)rc).indexOf(o);
	}
	public int lastIndexOf(Object o) {
		return ((ResourceList)rc).lastIndexOf(o);
	}
	public ListIterator listIterator() {
		return ((ResourceList)rc).listIterator();
	}
	public ListIterator listIterator(int index) {
		return ((ResourceList)rc).listIterator(index);
	}
	public Object remove(int index) {
		return ((ResourceList)rc).remove(index);
	}
	public Object set(int index, Object element) {
		return ((ResourceList)rc).set(index, element);
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
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subList(Filter subfilter) {
        return new SubFeatureCollection( this, subfilter );
    }
    //
    // RandomFeatureAccess
    //
    
    public SimpleFeature getFeatureMember( String id ) throws NoSuchElementException {
        int position = index.indexOf( id );
        if( position == -1){
            throw new NoSuchElementException(id);
        }        
        if( collection instanceof RandomFeatureAccess ){
            RandomFeatureAccess random = (RandomFeatureAccess) collection;
            random.getFeatureMember( id ); 
        }
        return (SimpleFeature) get( position );
    }
    public SimpleFeature removeFeatureMember( String id ) {
        int position = index.indexOf( id );
        if( position == -1){
            throw new NoSuchElementException(id);
        }        
        if( collection instanceof RandomFeatureAccess ){
            RandomFeatureAccess random = (RandomFeatureAccess) collection;
            if( index != null ) index.remove( id );            
            return random.removeFeatureMember( id );            
        }
        return (SimpleFeature) remove( position );
    }   
}
