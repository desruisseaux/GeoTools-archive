/*$************************************************************************************************
 **
 ** $Id: Catalog.java,v 1.7 2004/09/23 00:39:13 desruisseaux Exp $
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/catalog/Catalog.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.catalog;

// J2SE direct dependencies
import java.util.List;

// Annotations
///import org.opengis.annotation.UML;
///import static org.opengis.annotation.Obligation.*;


/**
 * A collection of {@linkplain CatalogEntry catalog entries} that is organized to assist in the discovery,
 * access, and retrieval of geospatial resources or services that are of interest to the user, especially when the
 * existence or whereabouts of the resource are not know to the user.
 * 
 * <p>
 * Being a collection of {@linkplain CatalogEntry catalog entries}, a catalog should be able to enumerate
 * each of its entries, and should allow entries to be added or removed. It should also support queries
 * that will enable the user to obtain entries of interest based on specified criteria.
 * </p>
 * 
 * <p>
 * NOTE:  The specification <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A>
 *        does not specify the methods of the Catalog interface.  The methods in this interface
 *        were inferred from reading the abstract catalog specification: 
 *        <A HREF="http://www.opengis.org/docs/99-113.pdf">Catalog Services</A> section 3.1.1
 * 
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A> 
 * @version <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A> 
 */
///@UML (identifier="CG_Catalog")
public interface Discovery {
    
    /**
     * Searches through the catalog and finds the entries that that match the query.
     * 
     * @param  query A {@linkplain QueryRequest query definition} used to select
     *         {@linkplain CatalogEntry catalog entries}.
     * @return {@linkplain QueryResult Query result} containing all matching
     *         {@linkplain CatalogEntry catalog entries}.
     *
     * @UML inferred from section 3.1.1.1.1 <i>Query Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    List search(QueryRequest query); // really a List<CatalogEntry> 

    /**
     * Entire contents.
     * <p>
     * Shortcut for query( QueryDefinition.ALL )
     * <M/p>
     *
     * @return Traverse the entire catalog.
     *
     * @UML inferred from section 3.1.1.1.2 <i>Other Functions on Catalog </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    List entries();
}
