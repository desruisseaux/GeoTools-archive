package org.geotools.feature.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.geotools.data.FeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class SubFeatureList extends SubFeatureCollection implements FeatureList {
	
	/**
	 * Create a simple SubFeatureList with the provided
	 * filter.
	 * 
	 * @param filter
	 */
	public SubFeatureList(FeatureList list, Filter filter) {
		super( list, filter );
	}

	/**
	 * Sublist of this sublist!
	 * <p>
	 * Implementation will ensure this does not get out of hand, order
	 * is maintained and only indexed once.
	 * </p>
	 */
	public FeatureList subList(Filter filter) {
		if( collection instanceof SubFeatureList){
			SubFeatureList sublist = (SubFeatureList) collection;
			return new SubFeatureList( sublist, this.filter.and( filter ) );
			
		}
		return new SubFeatureList( this, filter );
	}

	public boolean addAll(int arg0, Collection arg1) {
		return false;
	}

	public Object get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object set(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void add(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	public Object remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ListIterator listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ListIterator listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public List subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}
}
