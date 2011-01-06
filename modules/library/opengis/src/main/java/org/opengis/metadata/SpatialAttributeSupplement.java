/*$************************************************************************************************
 **
 ** $Id: SpatialAttributeSupplement.java 1439 2009-07-03 01:42:05Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/metadata/SpatialAttributeSupplement.java $
 **
 ** Copyright (C) 2004-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.metadata;

import java.util.Collection;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Spatial attributes in the application schema for the feature types.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 2.0
 *
 * @deprecated This type has been removed from ISO 19115:2003
 */
@Deprecated
@UML(identifier="MD_SpatialAttributeSupplement", specification=ISO_19115)
public interface SpatialAttributeSupplement {
    /**
     * Provides information about the list of feature types with the same spatial representation.
     *
     * @return The list of feature types with the same spatial representation.
     */
    @UML(identifier="theFeatureTypeList", obligation=MANDATORY, specification=ISO_19115)
    Collection<FeatureTypeList> getFeatureTypeList();
}
