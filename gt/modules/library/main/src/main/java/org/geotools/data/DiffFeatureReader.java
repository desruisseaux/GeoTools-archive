/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filters;
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A FeatureReader that considers differences.
 * <p>
 * Used to implement In-Process Transaction support. This implementation will need to peek ahead in
 * order to check for deletetions.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/module/main/src/org/geotools/data/DiffFeatureReader.java $
 */
public class DiffFeatureReader implements FeatureReader {
    FeatureReader reader;
    Diff diff;

    /** Next value as peeked by hasNext() */
    Feature next = null;
    private Filter filter;
    private Set encounteredFids;

	private Iterator addedIterator;
	private Iterator modifiedIterator;
	private int fidIndex=0;
	private Iterator spatialIndexIterator;
	
	private boolean indexedGeometryFilter = false;
	private boolean fidFilter = false;
	
    /**
     * This constructor grabs a "copy" of the current diff.
     * <p>
     * This reader is not "live" to changes over the course of the Transaction. (Iterators are not
     * always stable of the course of modifications)
     * </p>
     * 
     * @param reader
     * @param diff2 Differences of Feature by FID
     */
    public DiffFeatureReader( FeatureReader reader, Diff diff2 ) {
        this(reader, diff2, Filter.INCLUDE);
    }

    /**
     * This constructor grabs a "copy" of the current diff.
     * <p>
     * This reader is not "live" to changes over the course of the Transaction. (Iterators are not
     * always stable of the course of modifications)
     * </p>
     * 
     * @param reader
     * @param diff2 Differences of Feature by FID
     */
    public DiffFeatureReader( FeatureReader reader, Diff diff2, Filter filter ) {
        this.reader = reader;
        this.diff = diff2;
        this.filter = filter;
        encounteredFids=new HashSet();

        if( filter instanceof FidFilter ){
        	fidFilter=true;
        }else if( isSubsetOfBboxFilter(filter) ){
        	indexedGeometryFilter=true;
        }
        
        synchronized (diff) {
        	if( indexedGeometryFilter ){
        		spatialIndexIterator=getIndexedFeatures().iterator();
        	}
        	addedIterator=diff.added.values().iterator();
        	modifiedIterator=diff.modified2.values().iterator();
        }
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        if (hasNext()) {
        	Feature live = next;
        	next = null;

            return live;
        }

        throw new NoSuchElementException("No more Feature exists");
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            // We found it already
            return true;
        }
        Feature peek;

        if( filter==Filter.EXCLUDE)
            return false;
        
        while( (reader != null) && reader.hasNext() ) {

            try {
                peek = reader.next();
            } catch (NoSuchElementException e) {
                throw new DataSourceException("Could not aquire the next Feature", e);
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Could not aquire the next Feature", e);
            }

            String fid = peek.getID();
            encounteredFids.add(fid);

            if (diff.modified2.containsKey(fid)) {
                Feature changed = (Feature) diff.modified2.get(fid);
                if (changed == TransactionStateDiff.NULL || !filter.evaluate(changed) ) {
                    continue;
                } else {
                    next = changed;
                    return true;
                }
            } else {

                next = peek; // found feature
                return true;
            }
        }

        queryDiff();
        return next != null;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }

        if (diff != null) {
            diff = null;
            addedIterator=null;
        }
    }
    
    protected void queryDiff() {
        if( fidFilter ){
            queryFidFilter();
        } else if( indexedGeometryFilter ){
        	querySpatialIndex();
        } else {
        	queryAdded();
        	queryModified();
         }
    }

	protected void querySpatialIndex() {
		while( spatialIndexIterator.hasNext() && next == null ){
			Feature f = (Feature) spatialIndexIterator.next();
			if( encounteredFids.contains(f.getID()) || !filter.evaluate(f)){
				continue;
			}
			next = f;
		}
	}
    
	protected void queryAdded() {
		while( addedIterator.hasNext() && next == null ){
			next = (Feature) addedIterator.next();
			if( encounteredFids.contains(next.getID()) || !filter.evaluate(next)){
				next = null;
			}
		}
	}
	
	protected void queryModified() {
		while( modifiedIterator.hasNext() && next == null ){
			next = (Feature) modifiedIterator.next();
			if( next==TransactionStateDiff.NULL || encounteredFids.contains(next.getID()) || !filter.evaluate(next) ){
				next = null;
			}
		}
	}
	
	protected void queryFidFilter() {
		FidFilter fidFilter = (FidFilter) filter;
		if (fidIndex == -1) {
		    fidIndex = 0;
		}
		while( fidIndex < fidFilter.getFids().length && next == null ) {
		    String fid = fidFilter.getFids()[fidIndex];
		    if( encounteredFids.contains(fid) ){
		    	fidIndex++;
		    	continue;
		    }
			next = (Feature) diff.modified2.get(fid);
		    if( next==null ){
		    	next = (Feature) diff.added.get(fid);
		    }
		    fidIndex++;
		}
	}
    
    protected List getIndexedFeatures() {
        // TODO: check geom is default geom.
        Envelope env = extractBboxForSpatialIndexQuery((GeometryFilter) filter);
        return diff.queryIndex(env);
    }

    protected Envelope extractBboxForSpatialIndexQuery(GeometryFilter f) {
        GeometryFilter geomFilter = (GeometryFilter) f;
        Expression leftGeometry = geomFilter.getLeftGeometry();
        Expression rightGeometry = geomFilter.getRightGeometry();
        
        Geometry g;
        if( leftGeometry instanceof LiteralExpression ){
        	g=(Geometry) ((LiteralExpression) leftGeometry).getLiteral();
        }else{
        	g=(Geometry) ((LiteralExpression) rightGeometry).getLiteral();
        }
        return g.getEnvelopeInternal();
    }
    
    protected boolean isDefaultGeometry(AttributeExpression ae) {
    	return reader.getFeatureType().getDefaultGeometry().getName().equals(ae.getAttributePath());
    }
    
    protected boolean isSubsetOfBboxFilter(Filter f) {
       return filter instanceof Contains ||
            filter instanceof Crosses ||
            filter instanceof Overlaps ||
            filter instanceof Touches ||
            filter instanceof Within ||
            filter instanceof BBOX;
    }
}
