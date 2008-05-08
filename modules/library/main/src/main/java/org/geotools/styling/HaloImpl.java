/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
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
package org.geotools.styling;


// OpenGIS dependencies
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.resources.Utilities;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;


/**
 * Direct implementation of Halo.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class HaloImpl implements Halo, Cloneable {
    /** The logger for the default core module. */
    private static final java.util.logging.Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private Fill fill = new FillImpl();
    private Expression radius = null;

    public HaloImpl() {
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()));
    }

    public HaloImpl(FilterFactory factory) {
        filterFactory = factory;
        init();
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
        init();
    }

    private void init() {
        try {
            radius = filterFactory.literal(1);
        } catch (org.geotools.filter.IllegalFilterException ife) {
            LOGGER.severe("Failed to build defaultHalo: " + ife);
        }

        fill.setColor(filterFactory.literal("#FFFFFF")); // default halo is white
    }

    /**
     * Getter for property fill.
     *
     * @return Value of property fill.
     */
    public org.geotools.styling.Fill getFill() {
        return fill;
    }

    /**
     * Setter for property fill.
     *
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.Fill fill) {
        this.fill = fill;
    }

    /**
     * Getter for property radius.
     *
     * @return Value of property radius.
     */
    public Expression getRadius() {
        return radius;
    }

    /**
     * Setter for property radius.
     *
     * @param radius New value of property radius.
     */
    public void setRadius(Expression radius) {
        this.radius = radius;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone of the Halo.
     *
     * @return The clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        try {
            HaloImpl clone = (HaloImpl) super.clone();
            clone.fill = (Fill) ((Cloneable) fill).clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This will never happen");
        }
    }

    /**
     * Compares this HaloImpl with another for equality.
     *
     * @param obj THe other HaloImpl.
     *
     * @return True if they are equal.  They are equal if their fill and radius
     *         is equal.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof HaloImpl) {
            HaloImpl other = (HaloImpl) obj;

            return Utilities.equals(radius, other.radius)
            && Utilities.equals(fill, other.fill);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 37;
        int result = 17;

        if (radius != null) {
            result = (result * PRIME) + radius.hashCode();
        }

        if (fill != null) {
            result = (result * PRIME) + fill.hashCode();
        }

        return result;
    }
}
