package org.geotools.catalog.defaults;

import java.net.URI;

import javax.swing.Icon;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.geometry.JTS.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of GeoResourceInfo.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DefaultGeoResourceInfo implements GeoResourceInfo {

    protected String title, description, name;
    protected String[] keywords;
    protected URI schema;
    protected Icon icon;
    protected ReferencedEnvelope bounds;

    protected DefaultGeoResourceInfo() {
        // for over-riding
    }

    public DefaultGeoResourceInfo( String title, String name, String description, URI schema,
            Envelope bounds, CoordinateReferenceSystem crs, String[] keywords, Icon icon ) {
        this.title = title;
        this.description = description;
        this.name = name;
        this.keywords = keywords;
        this.schema = schema;
        this.icon = icon;
        this.bounds = new ReferencedEnvelope(bounds, crs);
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getTitle()
	 */
    public String getTitle() {
        return title;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getKeywords()
	 */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getDescription()
	 */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getSchema()
	 */
    public URI getSchema() { // aka namespace
        return schema;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getName()
	 */
    public String getName() { // aka layer/type name
        return name;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getIcon()
	 */
    public Icon getIcon() {
        return icon;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getBounds()
	 */
    public Envelope getBounds() { // part of Coverage
        return bounds;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.GeoResourceInfo#getCRS()
	 */
    public CoordinateReferenceSystem getCRS() { // part of Coverage
        return bounds.getCRS();
    }
}
