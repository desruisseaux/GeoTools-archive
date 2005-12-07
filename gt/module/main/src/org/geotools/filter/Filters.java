package org.geotools.filter;

import org.geotools.filter.visitor.DuplicatorFilterVisitor;

/**
 * Utility class for working with Filters & Expression.
 * <p>
 * Note additional utility methods are available if you create
 * an instanceof this Object w/ a FilterFactory.
 * </p>
 * <p>
 * Example use:
 * <pre><code>
 * Filters filters = new Filters( factory );
 * filters.duplicate( origional );
 * </code></pre>
 * The above example creates a copy of the provided Filter,
 * the factory provided will be used when creating the duplicated
 * content.
 * </p>
 * @author Jody Garnett, Refractions Research
 *
 */
public class Filters {
	FilterFactory ff;
	
	public Filters(){
		this( FilterFactoryFinder.createFilterFactory() );
	}	
	public Filters( FilterFactory factory ){
		ff = factory;
	}
	public void setFilterFactory( FilterFactory factory ){
		ff = factory;
	}
    /**
     * Deep copy the filter.
     * <p>
     * Filter objects are mutable, when copying a rich
     * data structure (like SLD) you will need to duplicate
     * the Filters referenced therein.
     * </p>
     */
    public Filter duplicate( Filter filter ){
    	DuplicatorFilterVisitor xerox = new DuplicatorFilterVisitor( ff );
    	filter.accept( xerox );
    	return (Filter) xerox.getCopy();
    	
    }
}

