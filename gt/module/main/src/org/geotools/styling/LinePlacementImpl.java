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

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.expression.Expression;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;

// J2SE dependencies
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class LinePlacementImpl extends AbstractGTComponent
    implements LinePlacement, Cloneable {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private Expression perpendicularOffset = null;

    public LinePlacementImpl() {
        this(FilterFactoryFinder.createFilterFactory());
    }

    public LinePlacementImpl(FilterFactory factory) {
        filterFactory = factory;
        init();
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
        init();
    }

    /**
     * Creates a new instance of DefaultLinePlacement
     */
    private void init() {
        try {
            perpendicularOffset = filterFactory.createLiteralExpression(new Integer(
                        0));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            LOGGER.severe("Failed to build defaultLinePlacement: " + ife);
        }
    }

    /**
     * Getter for property perpendicularOffset.
     *
     * @return Value of property perpendicularOffset.
     */
    public Expression getPerpendicularOffset() {
        return perpendicularOffset;
    }

    /**
     * Setter for property perpendicularOffset.
     *
     * @param perpendicularOffset New value of property perpendicularOffset.
     */
    public void setPerpendicularOffset(Expression perpendicularOffset) {
        this.perpendicularOffset = perpendicularOffset;
        fireChanged();
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see Cloneable#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This can not happen");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof LinePlacementImpl) {
            LinePlacementImpl other = (LinePlacementImpl) obj;

            return Utilities.equals(perpendicularOffset,
                other.perpendicularOffset);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 37;
        int result = 17;

        if (perpendicularOffset != null) {
            result = (result * PRIME) + perpendicularOffset.hashCode();
        }

        return result;
    }
}
