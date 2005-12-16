package org.geotools.feature.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.geotools.data.FeatureReader;
import org.geotools.feature.AbstractFeatureCollection;
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

/**
 * Order is maintained in an in memory index, you have been warned!
 * <p>
 * In an ideal world (ie when you have time) please consider
 * running a bbox based associated spatial index.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 */
public class OrderedFeatureList /*extends AbstractFeatureCollection implements FeatureList */{
	
	/** Delegate FeatureCollection being ordered */
	FeatureCollection collection;
	
	/** Order by which collection is sorted */
	SortBy order;
	
	/**
	 * 
	 * @param colletion
	 * @param order
	 */
	public OrderedFeatureList( FeatureCollection collection, SortBy order ){
		this.collection = collection;
		this.order = order;
	}
	
	public FeatureList subList(Filter filter) {
		return null; //new SubFeatureList( this, filter );
	}

}
