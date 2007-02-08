package org.geotools.data.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opengis.filter.Filter;

public class FilteringCollection implements Collection {

	private Collection collection;

	private Filter filter;

	public FilteringCollection(Collection collection, Filter filter) {
		this.collection = collection;
		this.filter = filter;
	}

	public boolean add(Object obj) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection col) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean contains(Object o) {
		if (!filter.evaluate(o)) {
			return false;
		}
		boolean internalContains = collection.contains(o);
		if (!internalContains) {
			return false;
		}
		for (Iterator it = iterator(); it.hasNext();) {
			Object object = it.next();
			if (object.equals(o)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAll(Collection col) {
		
		return false;
	}

	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	public Iterator iterator() {
		return new FilteringIterator(collection.iterator(), filter);
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

	private int size = -1;
	
	public int size() {
		if(size == -1){
			int count = 0;
			for(Iterator it = iterator(); it.hasNext();){
				it.next();
				count++;
			}
			size = count;
		}
		return size;
	}

	public Object[] toArray() {
		List list = new ArrayList();
		for(Iterator it = iterator(); it.hasNext();){
			list.add(it.next());
		}
		return list.toArray();
	}

	public Object[] toArray(Object[] destination) {
		List list = new ArrayList();
		for(Iterator it = iterator(); it.hasNext();){
			list.add(it.next());
		}
		return list.toArray(destination);
	}

}
