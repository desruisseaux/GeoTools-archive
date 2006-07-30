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
package edu.psu.geovista.geotools.filter;


// Geotools dependencies
import org.geotools.feature.Feature;
import org.geotools.filter.*;

// J2SE dependencies
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import java.util.EventObject;
import javax.swing.event.EventListenerList;

/**
 * Defines a feature ID filter, which holds a list of feature IDs. This filter
 * stores a series of feature IDs, which are used to distinguish features
 * uniquely.
 *
 * @author Rob Hranac, TOPP
 * @version $Id$
 */
public class DynamicFilter extends AbstractFilterImpl implements FidFilter {
    /** Logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private int min, max;
    private EventListenerList ell = new EventListenerList();
 
    /** List of the feature IDs. */
    private Set fids = new HashSet();

    /**
     * Empty constructor.
     */
    public DynamicFilter() {
      super(FilterFactoryFinder.createFilterFactory());
      filterType = AbstractFilter.FID;
    }

    /**
     * Constructor with first fid set
     *
     * @param initialFid The type of comparison.
     */
    protected DynamicFilter(String initialFid) {
    	super(FilterFactoryFinder.createFilterFactory());
        filterType = AbstractFilter.FID;
        addFid(initialFid);
    }

    /**
     * Adds a feature ID to the filter.
     *
     * @param fid A single feature ID.
     */
    public final void addFid(String fid) {
        LOGGER.finest("got fid: " + fid);
        fids.add(fid);
    }

    public final void removeFid(String fid){
      fids.remove(fid);
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     */
    public boolean evaluate(Feature feature) {
        if (feature == null) {
            return false;
        }

        return fids.contains(feature.getID());
    }

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the compare filter.
     */
    public String toString() {
        StringBuffer fidFilter = new StringBuffer();

        Iterator fidIterator = fids.iterator();

        while (fidIterator.hasNext()) {
            fidFilter.append(fidIterator.next().toString());

            if (fidIterator.hasNext()) {
                fidFilter.append(", ");
            }
        }

        return "[ " + fidFilter.toString() + " ]";
    }

    /**
     * Returns a flag indicating object equality.
     *
     * @param filter the filter to test equality on.
     *
     * @return String representation of the compare filter.
     */
    public boolean equals(Object filter) {
        LOGGER.finest("condition: " + filter);

        if ((filter != null) && (filter.getClass() == this.getClass())) {
            LOGGER.finest("condition: " + ((DynamicFilter) filter).filterType);

            if (((DynamicFilter) filter).filterType == AbstractFilter.FID) {
                return fids.equals(((DynamicFilter) filter).getFidsSet());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return a hash code value for this fid filter object.
     */
    public int hashCode() {
        return fids.hashCode();
    }

    /**
     * Returns all the fids in this filter.
     *
     * @return An array of all the fids in this filter.
     */
    public String[] getFids() {
        return (String[]) fids.toArray(new String[0]);
    }

    /**
     * Accessor method for fid set.
     *
     * @return the internally stored fids.
     */
    Set getFidsSet() {
        return fids;
    }

    
    
    /*** 
     * Adds a collection of feature IDs to the filter. 
     * 
     * @param fids A collection of feature IDs. 
     */ 
    public void addAllFids(java.util.Collection collection) {
        fids.addAll(collection); 
    }
    
    /*** 
     * Removes a collection of feature IDs from the filter. 
     * 
     * @param fids A collection of feature IDs. 
     */ 
    public void removeAllFids(java.util.Collection collection) {
        fids.remove(collection); 
    }

	public Set getIDs() {
		return fids;
	}

	public void setIDs(Set fids) {
		this.fids = fids;
	}
    
}
