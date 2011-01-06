/*$************************************************************************************************
 **
 ** $Id: package-info.java 1264 2008-07-09 17:46:15Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/metadata/extent/package-info.java $
 **
 ** Copyright (C) 2004-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/

/**
 * {@linkplain org.opengis.metadata.extent.Extent} information. The following is adapted from
 * <A HREF="http://www.opengis.org/docs/01-111.pdf">OpenGIS&reg; Metadata (Topic 11)</A> specification.
 *
 * <P ALIGN="justify">The datatype in this package is an aggregate of the metadata
 * elements that describe the spatial and temporal extent of the referring entity.
 * The {@linkplain org.opengis.metadata.extent.Extent extent} entity contains information about the
 * {@linkplain org.opengis.metadata.extent.GeographicExtent geographic},
 * {@linkplain org.opengis.metadata.extent.TemporalExtent temporal} and the
 * {@linkplain org.opengis.metadata.extent.VerticalExtent vertical} extent of the referring
 * entity.
 *
 * The {@linkplain org.opengis.metadata.extent.GeographicExtent geographic extent} can be subclassed as
 * {@linkplain org.opengis.metadata.extent.BoundingPolygon bounding polygon},
 * {@linkplain org.opengis.metadata.extent.GeographicBoundingBox geographic bounding box} and
 * {@linkplain org.opengis.metadata.extent.GeographicDescription geographic description}.</P>
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @since   GeoAPI 1.0
 */
package org.opengis.metadata.extent;
