/*$************************************************************************************************
 **
 ** $Id: CatalogEntry.java,v 1.4 2004/05/01 02:37:04 desruisseaux Exp $
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/catalog/CatalogEntry.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/

package org.geotools.catalog;

// J2SE direct dependencies
import java.util.Iterator;

import org.geotools.metadata.Metadata;


/**
 * The Catalog Entry contains all the MetaDataEntity Entities used to describe one resource. More
 * specifically, an object of this class contains a collection of MetaDataEntity Entities which together
 * describe the associated resource for the purpose of discovery. This collection of MetaDataEntity Entities
 * is usually only a subset of all the MetaDataEntity that exists for the associated resource.
 * 
 * <p>
 * One or more of the MetaDataEntity Entities usually directly reference the associated resource, and
 * provide the basic information needed to locate and subsequently access that resource, either directly
 * or through an access service. When applicable, one or more of the included MetaDataEntity Entities may
 * specify the spatial location of the resource. The spatial location could be defined by a (minimum
 * bounding) rectangle, and/or by a polygon bounding the ground area covered. Perhaps the spatial
 * location could be defined by any OpenGIS geometry. For example, the spatial location of a feature
 * describing a single point could be defined by that point.
 * </p>
 * 
 * <p>
 * If the resource is a dataset, several of the included MetaDataEntity Entities may define the types of data
 * included in the dataset, plus the quality of that data.
 * </p>
* <p>
 * NOTE:  The specification <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A>
 * does not specify the methods of the CatalogEntity interface.  The methods in this interface
 * were inferred from reading the abstract catalog specification: 
 * <A HREF="http://www.opengis.org/docs/99-113.pdf">Catalog Services</A> section 3.1.2
 * 
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A> 
 * @version <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A> 
 * @UML abstract CG_CatalogEntity
 */
public interface CatalogEntry{
    /**
     * Each <code>CatalogEntry</code> describes a resource, this method provides access to that
     * resource.
     *  
     * @return The resource described by current <code>CatalogEntry</code>.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public Object getResource();

    /**
     * Used to obtain the name of Associated Data.    
     * 
     * @return The name of the associated data
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public String getDataName();

    /**
     * Returns the number of MetaDataEntity associated with the <code>CatalogEntry</code>.
     *
     * @return The number of MetaDataEntity
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public int getNumMetadata();

    /**
     * Returns an array of the names of the MetaDataEntity associated with the
     * <code>CatalogEntry</code>.
     *
     * @return An array with all the MetaDataEntity names.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public String[] getMetadataNames();

    /**
     * Obtain the MetaDataEntity referenced by the index.
     *
     * @param index the index of the MetaDataEntity required.
     * @return the indexed MetaDataEntity.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public Metadata getMetadata(int index);

    /**
     * Obtain the Metadata refered to by the name.
     *
     * @param name the name of the Metadata required.
     * @return the requested Metadata.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public Metadata getMetadata(String name);

    /**
     * Returns an iterator that can be used to iterate through the associated Metadata.
     *
     * @return An iterator for iterating through the Metadata.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification 
     */
    public Iterator iterator();
}
