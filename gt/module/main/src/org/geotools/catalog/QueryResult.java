/*$************************************************************************************************
 **
 ** $Id: QueryResult.java,v 1.7 2004/09/23 00:39:13 desruisseaux Exp $
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/catalog/QueryResult.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.catalog;

// J2SE direct dependencies
import java.util.Iterator;


/**
 * A collection of {@linkplain CatalogEntry catalog entries}.
 *
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A> 
 * @version <A HREF="http://www.opengis.org/docs/99-113.pdf">Catalog Services</A> 
 *
 * @UML inferred from Section 3.1.1.1 <i>Query Functions</i> in the
 *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
 *      Catalog Services </A> Specification
 */
public interface QueryResult {
    /**
     * Gets the number of {@linkplain CatalogEntry catalog entries} found.
     *
     * @return the number of CatalogEntries in this result object.
     *
     * @UML inferred from Section 3.1.1.1 <i>Query Functions</i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    int getNumEntries();

    /**
     * Returns the indexed {@linkplain CatalogEntry catalog entry}.
     *
     * @param index the index of the {@linkplain CatalogEntry catalog entry} requested.
     * @return the requested {@linkplain CatalogEntry catalog entry}.
     *
     * @UML inferred from Section 3.1.1.1 <i>Query Functions</i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    CatalogEntry getEntry(int index);

    /**
     * Returns an iterator that can be used to iterate through
     * {@linkplain CatalogEntry catalog entries}.
     *
     * @return An iterator for iterating through the {@linkplain CatalogEntry catalog entries}.
     *
     * @UML inferred from Section 3.1.1.1 <i>Query Functions</i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    Iterator/*<CatalogEntry>*/ iterator();
}