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
package org.geotools.catalog;

import java.net.URI;

import javax.swing.Icon;

/**
 * Provides metadata information about a service.
 * <p>
 * A bean - style metadata which must already be loaded. Much of the names and motivation have been
 * taken from Dublin Code and it's application profile for RDF.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 */
public interface ServiceInfo {
	 /**
     * Returns the service title, may be empty or null if unsupported.
     * <p>
     * Note this is always metadata, and is in user terms.
     * </p>
     * 
     * @return title, may be empty, null if unsupported.
     */
    String getTitle();

    /**
     * Returns the service keywords. Maps to the Dublin Core Subject element.
     * 
     * @return
     */
    String[] getKeywords();

    /**
     * Returns the service description
     * 
     * @return
     */
    String getDescription();

    /**
     * Return the service abstract
     * 
     * @return
     */
    String getAbstract();

    /**
     * Return the service publisher
     * 
     * @return
     */
    URI getPublisher();

    /**
     * Returns the xml schema namespace for this service type. Maps to the Dublin Code Format
     * element
     * 
     * @return
     */
    URI getSchema();

    /**
     * Returns the service source. Maps to the Dublin Core Server Element
     * 
     * @return
     */
    URI getSource();

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
    Icon getIcon();
}
