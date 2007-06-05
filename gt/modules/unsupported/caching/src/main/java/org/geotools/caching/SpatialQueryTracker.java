package org.geotools.caching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.* ;
import org.geotools.index.rtree.memory.MemoryPageStore;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class SpatialQueryTracker implements QueryTracker {

	private static DataDefinition df = createDataDefinition() ;
	private RTree tree = createTree() ;
	private final HashMap map = new HashMap() ;
	private final FilterFactory filterFactory = new FilterFactoryImpl() ;
	
	public void clear() {
		try {
			map.clear() ;
			tree.close() ;
			tree = createTree() ;
		} catch (TreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Query match(Query q) {
		if (!accepts(q)) return q ;
		BBOXImpl bb = (BBOXImpl) q.getFilter() ;
		try {
			Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY()) ;
			Geometry searchArea = getRectangle(env) ;
			List results = tree.search(env) ;
			if (results.size() == 0) return q ;
			for (Iterator i = results.iterator(); i.hasNext(); ) {
				Data d = (Data) i.next() ;
				Envelope e = (Envelope) map.get(d.getValue(0)) ;
				Polygon rect = getRectangle(e) ;
				if (rect.contains(searchArea)) {
					return new DefaultQuery(q.getTypeName(), Filter.EXCLUDE) ;
				}
				searchArea = searchArea.difference(rect) ;
			}
			Envelope se = searchArea.getEnvelopeInternal() ;
			Filter newbb = filterFactory.bbox(bb.getPropertyName(), se.getMinX(), se.getMinY(), se.getMaxX(), se.getMaxY(), bb.getSRS()) ;
			return new DefaultQuery(q.getTypeName(),newbb) ;
		} catch (TreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return q ;
	}

	public void register(Query q) {
		if (accepts(q)) {
			BBOXImpl bb = (BBOXImpl) q.getFilter() ;
			try {
				Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY()) ;
				Data d = new Data(df) ;
				Integer key = new Integer(env.hashCode()) ;
				d.addValue(key) ;
				map.put(key, env) ;
				tree.insert(env, d) ;
			} catch (TreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LockTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void unregister(Query q) {
		if (accepts(q)) {
			BBOXImpl bb = (BBOXImpl) q.getFilter() ;
			Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY()) ;
			try {
				List results = tree.search(env) ;
				for (Iterator i = results.iterator(); i.hasNext(); ) {
					Data d = (Data) i.next() ;
					Envelope e = (Envelope) map.get(d.getValue(0)) ;
					tree.delete(e) ;
					map.remove(d.getValue(0)) ;
				}
			} catch (TreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LockTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean accepts(Query q) {
		if (q.getFilter() instanceof BBOXImpl) {
			return true ;
		} else {
			return false ;
		}
	}
	
	private static RTree createTree() {
		try {
			PageStore ps = new MemoryPageStore(df, 8, 4, PageStore.SPLIT_QUADRATIC) ;
			RTree tree = new RTree(ps) ;
			return tree ;
		} catch (TreeException e) {
			throw (RuntimeException) new RuntimeException().initCause(e) ;
		}
	}
	
	private static DataDefinition createDataDefinition() {
		DataDefinition df = new DataDefinition("US-ASCII") ;
		df.addField(Integer.class) ;
		return df ;
	}
	
	private static Polygon getRectangle(Envelope e) {
		Coordinate[] coords = new Coordinate[] {
				new Coordinate(e.getMinX(), e.getMinY()),
				new Coordinate(e.getMaxX(), e.getMinY()),
				new Coordinate(e.getMaxX(), e.getMaxY()),
				new Coordinate(e.getMinX(), e.getMaxY()),
				new Coordinate(e.getMinX(), e.getMinY()) } ;
		CoordinateArraySequence seq = new CoordinateArraySequence(coords) ;
		LinearRing ls = new LinearRing(seq, new GeometryFactory()) ;
		Polygon ret = new Polygon(ls, null, new GeometryFactory()) ;
		return ret ;
	}
	
}
