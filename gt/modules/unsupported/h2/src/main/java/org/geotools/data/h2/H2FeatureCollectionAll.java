package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * H2 FeatureCollection which represents access to an entire table.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2FeatureCollectionAll implements FeatureCollection {

	/**
	 * state about table / feature type
	 */
	H2ContentState state;
	
	public H2FeatureCollectionAll( H2ContentState state ) {
		this.state = state;
	}
	
	public void accepts(FeatureVisitor visitor, ProgressListener progress)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void addListener(CollectionListener listener)
			throws NullPointerException {
		// TODO Auto-generated method stub

	}
	
	public void removeListener(CollectionListener listener)
		throws NullPointerException {
		// TODO Auto-generated method stub
	
	}

	public FeatureIterator features() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close(FeatureIterator close) {
		// TODO Auto-generated method stub

	}

	
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void close(Iterator close) {
		// TODO Auto-generated method stub

	}

	
	public FeatureType getFeatureType() {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureType getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	

	public FeatureList sort(SortBy order) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureCollection subCollection(Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public void purge() {
		// TODO Auto-generated method stub

	}

	public boolean add(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray(Object[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getAttributes(Object[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Envelope getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public Geometry getDefaultGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAttribute(int position, Object val)
			throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub

	}

	public void setAttribute(String xPath, Object attribute)
			throws IllegalAttributeException {
		// TODO Auto-generated method stub

	}

	public void setDefaultGeometry(Geometry geometry)
			throws IllegalAttributeException {
		// TODO Auto-generated method stub

	}

	class IteratorAll implements java.util.Iterator {

		Connection connection;
		
		IteratorAll() throws SQLException {
			//grab a connection
			connection = state.getDataStore().getConnectionPoolDataSource()
				.getPooledConnection().getConnection();
		}
		
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		public Object next() {
			// TODO Auto-generated method stub
			return null;
		}

		public void remove() {
			// TODO Auto-generated method stub
		}
		
		public void close() throws SQLException {
			connection.close();
		}
		
	}
}
