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
package org.geotools.catalog;

// J2SE direct dependencies

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
public interface Catalog extends Discovery {
    /**
     * Adds a {@linkplain CatalogEntry catalog entry} to the catalog.
     * Not all catalogs can provide this functionality so an
     * exception may be throws.
     *
     * For example A {@link org.opengis.coverage.grid.GridCoverageExchange} implementation
     * maybe implement catalog as well, however it may not be possible for for
     * <code>GridCoverageExchange</code> to remove or add to its catalog because
     * it maybe dependent on another resource such as a database.
     * 
     * @param entry Catalog entry to add.
     * @throws UnsupportedOperationException if the catalog does not provide this functionality.
     *
     * @UML inferred from section 3.1.1.1.2 <i>Other Functions on Catalog </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    void add(CatalogEntry entry) throws UnsupportedOperationException;

    /**
     * Removes a {@linkplain CatalogEntry catalog entry} from the catalog.
     * Not all catalogs can provide this functionality so an
     * exception may be throws.
     *
     * For example A {@link org.opengis.coverage.grid.GridCoverageExchange} implementation
     * maybe implement catalog as well, however it may not be possible for for
     * <code>GridCoverageExchange</code> to remove or add to its catalog because
     * it maybe dependent on another resource such as a database.
     * 
     * @param entry Catalog entry to remove
     * @throws UnsupportedOperationException if the catalog does not provide this functionality.
     *
     * @UML inferred from section 3.1.1.1.2 <i>Other Functions on Catalog </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    void remove(CatalogEntry entry) throws UnsupportedOperationException;
}
