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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;


/**
 * Default color map entry implementation
 *
 * @author aaime
 * @source $URL$
 */
public class ColorMapEntryImpl extends AbstractGTComponent
    implements ColorMapEntry {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
        .getLogger("org.geotools.core");
    private static final FilterFactory filterFactory = FilterFactoryFinder
        .createFilterFactory();
    private Expression quantity;
    private Expression opacity;
    private Expression color;
    private String label;

    /**
     * @see org.geotools.styling.ColorMapEntry#getLabel()
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        this.label = label;
        fireChanged();
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setColor(org.geotools.filter.Expression)
     */
    public void setColor(Expression color) {
        this.color = color;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getColor()
     */
    public Expression getColor() {
        return this.color;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setOpacity(org.geotools.filter.Expression)
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getOpacity()
     */
    public Expression getOpacity() {
        return this.opacity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setQuantity(org.geotools.filter.Expression)
     */
    public void setQuantity(Expression quantity) {
        this.quantity = quantity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getQuantity()
     */
    public Expression getQuantity() {
        return quantity;
    }
}
