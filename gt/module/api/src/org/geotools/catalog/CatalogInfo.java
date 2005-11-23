package org.geotools.catalog;

import java.net.URI;

/**
 * Represents a bean style metadata accessor for metadata about a catalog. This may be the result of
 * a request to a metadata service. All methods within an implementation of this interface should
 * NOT block. Much of this is based on Dublin Core and the RDF application profile.
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 */
public interface CatalogInfo {

	/**
	 * returns the catalog title May Not Block.
	 * 
	 * @return
	 */
	String getTitle();

	/**
	 * returns the keywords assocaited with this catalog May Not Block. Maps to Dublin Core's
	 * Subject element
	 * 
	 * @return
	 */
	String[] getKeywords();

	/**
	 * returns the catalog description.
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Returns the catalog source. May Not Block. Maps to the Dublin Core Server Element
	 * 
	 * @return
	 */
	URI getSource();

}