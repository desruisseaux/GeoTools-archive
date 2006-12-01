package org.geotools.data.grid;

import java.util.Collection;

import org.geotools.data.Store;
import org.opengis.filter.Filter;

public interface GridStore extends GridSource, Store {

    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent()
     */
    public Collection content();
    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent( Filter )
     */
    public Collection content( Filter filter );
    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent( String, String)
     */
    public Collection content( String query, String queryLanguage );
    
    /** Read/Write access to GridCoverage */    
    public Collection modifiableContent();
    
    /** Read/Write access to GridCoverage */    
    public Collection modifiableContent( Filter filter );
    
    /** Read/Write access to GridCoverage */
    public Collection modifiableContent( String filter, String queryLanguage );
}
