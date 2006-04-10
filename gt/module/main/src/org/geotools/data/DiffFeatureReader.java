/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;

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

	private Iterator diffIterator;
	private int fidIndex=0;
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
        this(reader, diff2, Filter.NONE);
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
        if( filter instanceof FidFilter ){
        	encounteredFids=new HashSet();
        }
        diffIterator=diff.added.values().iterator();
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

        if( filter==Filter.ALL)
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

            if (diff.modified2.containsKey(fid)) {
                Feature changed = (Feature) diff.modified2.get(fid);
                if (changed == TransactionStateDiff.NULL) {
                    continue;
                } else {
                    if (filter.contains(changed)) {
                        next = changed;
                        if( encounteredFids!=null )
                        	encounteredFids.add(next.getID());
                        return true; // found modified feature
                    }

                }
            } else {

                if (filter.contains(peek)) {
                    next = peek; // found feature
                    if( encounteredFids!=null )
                    	encounteredFids.add(next.getID());
                    return true;
                } 
            }
        }

        queryDiff();
        return next != null;
    }

    private void queryDiff() {
        if (filter instanceof FidFilter) {
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

        } else {
        	if( diffIterator.hasNext() )
        		next=(Feature) diffIterator.next();
        }
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
            diffIterator=null;
        }
    }
}
