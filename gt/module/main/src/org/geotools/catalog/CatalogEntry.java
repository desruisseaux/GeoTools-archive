package org.geotools.catalog;

// J2SE direct dependencies
import java.util.Iterator;
import java.util.Map;

// Annotations
///import org.opengis.annotation.UML;
///import static org.opengis.annotation.Obligation.*;


/**
 * The Catalog Entry contains all the {@linkplain MetadataEntity metadata entities} used to
 * describe one resource. More specifically, an object of this class contains a collection of
 * {@linkplain MetadataEntity metadata entities} which together describe the associated resource
 * for the purpose of discovery. This collection of {@linkplain MetadataEntity metadata entities}
 * is usually only a subset of all the <code>MetaDataEntity</code> that exists for the associated
 * resource.
 * 
 * <p>
 * One or more of the {@linkplain MetadataEntity metadata entities} usually directly reference the
 * associated resource, and provide the basic information needed to locate and subsequently access
 * that resource, either directly or through an access service. When applicable, one or more of the
 * included {@linkplain MetadataEntity metadata entities} may specify the spatial location of the
 * resource. The spatial location could be defined by a (minimum bounding) rectangle, and/or by a
 * polygon bounding the ground area covered. Perhaps the spatial location could be defined by any
 * OpenGIS geometry. For example, the spatial location of a feature describing a single point could
 * be defined by that point.
 * </p>
 * 
 * <p>
 * If the resource is a dataset, several of the included {@linkplain MetadataEntity metadata entities}
 * may define the types of data included in the dataset, plus the quality of that data.
 * </p>
 * <p>
 * NOTE:  The specification <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A>
 *        does not specify the methods of the CatalogEntity interface.  The methods in this interface
 *        were inferred from reading the abstract catalog specification: 
 *        <A HREF="http://www.opengis.org/docs/99-113.pdf">Catalog Services</A> section 3.1.2
 * 
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A> 
 * @version <A HREF="http://www.opengis.org/docs/02-087r3.pdf">Catalog Services 1.1.1</A> 
 */
///@UML (identifier="CG_CatalogEntity")
public interface CatalogEntry {
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
    Object resource();

    /**
     * Used to obtain the name of Associated Data.    
     * 
     * @return The name of the associated data.
     *
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification
     */
    String getDataName();

    /**
     * Map of MetadataEntity by name
     * <p>
     * Changes from GeoAPI Catalog discouver methods
     * <ul>
     * <li>getNumMetadata() implemented as metadata().size()
     * <li>getMetadataNames() implemented as metadata().keys().toArray( new String[ map.size() ] )
     * <li>getMetadata(String name) impelented as metadata().get( name )
     * <li>iterator() implemented as metadata().values().iterator()
     * <li>MetadataEntity getMetadata(int index) not needed 
     * </ul>
     * </p>
     * @UML inferred from Section 3.1.2.1 <i>Functions </i> in the
     *      <A HREF="http://www.opengis.org/docs/99-113.pdf">OGC Abstract
     *      Catalog Services </A> Specification    
     */
    Map metadata();
    
    /**
     * Names of available metadata.
     * <p>
     * Implemented as metadata().keys().toArray( new String[ metadata().size() ] )
     * </p>
     * @return names of available metadata
     */
    String[] getMetadataNames();
}
