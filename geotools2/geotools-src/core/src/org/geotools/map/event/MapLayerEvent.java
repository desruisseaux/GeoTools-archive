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
package org.geotools.map.event;


// J2SE dependencies
import java.util.EventObject;


/**
 * Event fired when some {@linkPlain Layer layer} property changes.
 *
 * @author Andrea Aime
 * @author Ian Turton
 * @author Martin Desruisseaux
 * @version $Id: MapLayerEvent.java,v 1.2 2004/03/14 18:44:22 aaime Exp $
 *
 * @see Layer
 * @see LayerListener
 */
public class MapLayerEvent extends EventObject {
    /**
     * Flag set when the layer visibility changed.
     *
     * @see #getReason
     */
    public static final int VISIBILITY_CHANGED = 1;

    /**
     * Flag set when the some metadata (like the title) changes
     *
     * @see #getReason
     */
    public static final int METADATA_CHANGED = 2;

    /**
     * Flag set when the data attached to this layer changed.
     *
     * @see #getReason
     */
    public static final int DATA_CHANGED = 3;

    /**
     * Flag set when the style attached to this layer changed.
     *
     * @see #getReason
     */
    public static final int STYLE_CHANGED = 4;

    /** The reason for the change. */
    private final int reason;

    /**
     * Creates a new instance of <code>LayerEvent</code> with the specified reason.
     *
     * @param source The source of the event change.
     * @param reason Why the event was fired.
     *
     * @throws IllegalArgumentException If the <code>reason</code> is not a valid enum.
     */
    public MapLayerEvent(final Object source, final int reason)
        throws IllegalArgumentException {
        super(source);
        this.reason = reason;

        if ((reason <= 0) || (reason > STYLE_CHANGED)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the reason why this event is fired. It is one of {@link #VISIBILITY_CHANGED} or
     * {@link #TITLE_CHANGED} constants.
     *
     * @return DOCUMENT ME!
     */
    public int getReason() {
        return reason;
    }
}
