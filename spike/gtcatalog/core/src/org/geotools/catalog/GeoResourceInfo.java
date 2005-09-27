/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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

import java.net.URI;

import javax.swing.Icon;


import org.geotools.geometry.JTS.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Represents a bean style metadata accessor for metadata about a geoResource.
 * <p>
 * The methods within this class must be non-blocking. This class, and sub-classes represent cached
 * versions of the metadata about a particular service.
 * </p>
 * <p>
 * Much of this interface is based on Dublin Core and the RDF application profile.
 * </p>
 * <p>
 * Any changes to this content will be communicate by an event by the assocaited GeoResource.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 */
public class GeoResourceInfo {

    protected String title, description, name;
    protected String[] keywords;
    protected URI schema;
    protected Icon icon;
    protected ReferencedEnvelope bounds;

    protected GeoResourceInfo() {
        // for over-riding
    }

    public GeoResourceInfo( String title, String name, String description, URI schema,
            Envelope bounds, CoordinateReferenceSystem crs, String[] keywords, Icon icon ) {
        this.title = title;
        this.description = description;
        this.name = name;
        this.keywords = keywords;
        this.schema = schema;
        this.icon = icon;
        this.bounds = new ReferencedEnvelope(bounds, crs);
    }

    /**
     * Returns the resource's title
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the keywords assocaited with this resource
     * <p>
     * Known Mappings:
     * <ul>
     * <li> Maps to Dublin Core's Subject element
     * </ul>
     * </p>
     * 
     * @return Keywords for use with search, or <code>null</code> unavailable.
     */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

    /**
     * Returns the resource's description.
     * <p>
     * Known Mappings:
     * <ul>
     * <li>WFS GetCapabilities description
     * <li>WMS GetCapabilities description
     * </ul>
     * </p>
     * 
     * @return description of resource, or <code>null</code> if unavailable
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the xml schema namespace for this resource type.
     * <p>
     * Known Mappings:
     * <ul>
     * <li>Dublin Code Format element
     * </ul>
     * </p>
     * 
     * @return namespace, used with getName() to identify resource
     */
    public URI getSchema() { // aka namespace
        return schema;
    }

    /**
     * Returns the name of the data ... such as the typeName or LayerName.
     * <p>
     * Known Mappings:
     * <ul>
     * <li>WFS typeName
     * <li>Database table name
     * <li>WMS layer name
     * </ul>
     * </p>
     * 
     * @return name of the data, used with getSchema() to identify resource
     */
    public String getName() { // aka layer/type name
        return name;
    }

    /**
     * Base symbology (with out decorators) representing this resource.
     * <p>
     * The Icon returned should conform the the Eclipse User Interface Guidelines (16x16
     * image with a 16x15 glyph centered).
     * </p>
     * <p>
     * This plug-in provides default based on resource type:
     * 
     * <pre><code>
     *  &lt;b&gt;return&lt;/b&gt; ISharedImages.getImagesDescriptor( IGeoResoruce );
     * </code></pre>
     * 
     * <ul>
     * <p>
     * Any LabelProvider should use the default image, a label decorator should be used to pick up
     * these images in a separate thread. This allows resources like WMS to make blocking request of
     * an external service.
     * </p>
     * 
     * @return Icon symbolizing this resource
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Returns the BBox of the resource if one exists, The null envelope otherwise.
     * <p>
     * The bounds are returned in (ie should be reprojected to) Lat Long:
     * <ul>
     * <li>DefaultGeographicCRS.WGS84
     * <li>EPSG:4369 (LatLong NAD83)
     * <li>ESPG 4326 (another LatLong)
     * </ul>
     * </p>
     * <p>
     * Known Mappings:
     * <ul>
     * <li>1st part of the Dublin Core Coverage
     * </ul>
     * </p>
     * <p>
     * </p>
     * 
     * @return Lat Long bounding box of the resource
     */
    public ReferencedEnvelope getBounds() { // part of Coverage
        return bounds;
    }

    /**
     * Returns the CRS of the resource if one exists, null otherwise.
     * <p>
     * Known Mappings:
     * <ul>
     * <li>2nd part of the Dublin Core Coverage
     * </ul>
     * </p>
     * 
     * @return CRS of the resource, or <code>null</code> if unknown.
     */
    public CoordinateReferenceSystem getCRS() { // part of Coverage
        return bounds.getCRS();
    }
}
