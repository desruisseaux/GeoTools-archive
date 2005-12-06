/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.catalog.defaults;

import java.net.URI;

import javax.swing.Icon;

import org.geotools.catalog.ServiceInfo;

/**
 * Implementation of ServiceInfo.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DefaultServiceInfo implements ServiceInfo {

    protected String title, description, _abstract;
    protected URI schema;
    protected URI source, publisher;
    protected String[] keywords;
    protected Icon icon;

    protected DefaultServiceInfo() {
        // to be used in an over-ride
    }

    public DefaultServiceInfo( String title, String description, String _abstract, URI source,
            URI publisher, URI schema, String[] keywords, Icon icon ) {
        this.title = title;
        this.description = description;
        this._abstract = _abstract;
        this.schema = schema;
        this.source = source;
        this.publisher = publisher;
        this.keywords = keywords;
        this.icon = icon;
    }

    /**
     * Returns the service title, may be empty or null if unsupported.
     * <p>
     * Note this is always metadata, and is in user terms.
     * </p>
     * 
     * @return title, may be empty, null if unsupported.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the service keywords. Maps to the Dublin Core Subject element.
     * 
     * @return
     */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

    /**
     * Returns the service description
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return the service abstract
     * 
     * @return
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Return the service publisher
     * 
     * @return
     */
    public URI getPublisher() {
        return publisher;
    }

    /**
     * Returns the xml schema namespace for this service type. Maps to the Dublin Code Format
     * element
     * 
     * @return
     */
    public URI getSchema() { // aka format
        return schema;
    }

    /**
     * Returns the service source. Maps to the Dublin Core Server Element
     * 
     * @return
     */
    public URI getSource() { // aka server
        return source;
    }

    /**
     * Base symbology (with out decorators) representing this IService.
     * <p>
     * The Icon returned should conform the the Eclipse User Interface Guidelines (16x16
     * image with a 16x15 glyph centered).
     * </p>
     * <p>
     * This plug-in provides default images based on service type:
     * 
     * <pre><code>
     *  &lt;b&gt;return&lt;/b&gt; ISharedImages.getImagesDescriptor( IService );
     * </code></pre>
     * 
     * <ul>
     * <p>
     * Any LabelProvider should use the default image, a label decorator should be used to pick up
     * these images in a separate thread. This allows services like WFS make blocking request to
     * pick up the image from their GetCapabilities.
     * </p>
     * 
     * @return Icon symbolizing this IService.
     */
    public Icon getIcon() {
        return icon;
    }
}
