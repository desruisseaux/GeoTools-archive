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

package org.geotools.map;

import org.geotools.data.DataSource;
import org.geotools.styling.Style;


/**
 * Layer is an aggregation of both a FeatureCollection and Style.
 *
 * @author Cameron Shorter
 * @version $Id: LayerImpl.java,v 1.5 2003/05/16 21:10:19 jmacgill Exp $
 *
 * @task REVISIT: This class maybe should contain CoordinateSystem, which could
 *       either be set externally, or derived from one of its features.
 */
public class LayerImpl implements Layer {
    /** Specify the DataSource which provides the features for this layer. */
    private DataSource dataSource;
    /** Specify the style for this layer. */
    private Style style;
    /**
     * Specify whether this layer is visable or not.  Defaults to TRUE on
     * initialisation.
     */
    private boolean visability = true;
    /** The title of this layer for use in Legend and similar. */
    private String title;

    /**
     * Creates a Layer.
     *
     * @param dataSource The dataSource to query in order to get features for
     *        this layer.
     * @param style The style to use when rendering features associated with
     *        this layer.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected LayerImpl(
        DataSource dataSource,
        Style style
    ) throws IllegalArgumentException {
        if ((style == null) || (dataSource == null)) {
            throw new IllegalArgumentException();
        } else {
            this.dataSource = dataSource;
            this.style = style;
            visability = true;
        }
    }

    /**
     * Get the dataSource for this layer.
     *
     * @return the DataSource used to create this layer.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get the style for this layer.
     *
     * @return The style (SLD).
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     *
     * @param visability Set the layer visable if TRUE.
     */
    public void setVisability(boolean visability) {
        this.visability = visability;
    }

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     *
     * @return TRUE if visable.
     */
    public boolean getVisability() {
        return visability;
    }

    /**
     * Get the title of this layer.  If title has not been defined then an
     * empty string is returned.
     *
     * @return The title of this layer.
     */
    public String getTitle() {
        if (title == null) {
            return new String("");
        } else {
            return title;
        }
    }

    /**
     * Set the title of this layer.
     *
     * @param title The title of this layer.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the title of this layer.  If no title has been defined, then the
     * class name is returned.
     *
     * @return the title of this layer.
     */
    public String toString() {
        if (title == null) {
            return super.toString();
        } else {
            return title;
        }
    }
}
