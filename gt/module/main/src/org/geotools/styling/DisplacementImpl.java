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
import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;


/**
 * DOCUMENT ME!
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class DisplacementImpl extends AbstractGTComponent
    implements Displacement, Cloneable {
    /** The logger for the default core module. */
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
        .getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private Expression displacementX = null;
    private Expression displacementY = null;

    public DisplacementImpl() {
        this(FilterFactoryFinder.createFilterFactory());
    }

    public DisplacementImpl(FilterFactory factory) {
        filterFactory = factory;

        try {
            displacementX = filterFactory.createLiteralExpression(new Integer(0));
            displacementY = filterFactory.createLiteralExpression(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            LOGGER.severe("Failed to build defaultDisplacement: " + ife);
        }
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;

        try {
            displacementX = filterFactory.createLiteralExpression(new Integer(0));
            displacementY = filterFactory.createLiteralExpression(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            LOGGER.severe("Failed to build defaultDisplacement: " + ife);
        }

        fireChanged();
    }

    /**
     * Setter for property displacementX.
     *
     * @param displacementX New value of property displacementX.
     */
    public void setDisplacementX(Expression displacementX) {
        this.displacementX = displacementX;
        fireChanged();
    }

    /**
     * Setter for property displacementY.
     *
     * @param displacementY New value of property displacementY.
     */
    public void setDisplacementY(Expression displacementY) {
        this.displacementY = displacementY;
        fireChanged();
    }

    /**
     * Getter for property displacementX.
     *
     * @return Value of property displacementX.
     */
    public Expression getDisplacementX() {
        return displacementX;
    }

    /**
     * Getter for property displacementY.
     *
     * @return Value of property displacementY.
     */
    public Expression getDisplacementY() {
        return displacementY;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.opengis.util.Cloneable#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Will not happen");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof DisplacementImpl) {
            DisplacementImpl other = (DisplacementImpl) obj;

            return Utilities.equals(displacementX, other.displacementX)
            && Utilities.equals(displacementY, other.displacementY);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 37;
        int result = 17;

        if (displacementX != null) {
            result = (result * PRIME) + displacementX.hashCode();
        }

        if (displacementY != null) {
            result = (result * PRIME) + displacementY.hashCode();
        }

        return result;
    }
}
