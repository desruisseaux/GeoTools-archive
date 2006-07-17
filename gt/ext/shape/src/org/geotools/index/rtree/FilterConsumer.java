/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.index.rtree;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.DefaultExpression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.NullFilter;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.expression.MathExpression;

import java.util.Iterator;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 * @source $URL$
 */
public class FilterConsumer implements FilterVisitor {
    private static Logger LOGGER = Logger.getLogger("org.geotools.index.rtree");
    private Envelope bounds = null;

    public Envelope getBounds() {
        return this.bounds;
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
     */
    public void visit(Filter filter) {
        if (filter.getFilterType() == Filter.NONE.getFilterType()) {
            this.bounds = new Envelope(Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY);
        } else if (filter.getFilterType() == Filter.ALL.getFilterType()) {
            this.bounds = null;
        } else {
            LOGGER.warning("Unknown filter type:" + filter.toString());
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
     */
    public void visit(BetweenFilter filter) {
        LOGGER.finest("BetweenFilter ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
     */
    public void visit(CompareFilter filter) {
        LOGGER.finest("CompareFilter ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
     */
    public void visit(GeometryFilter filter) {
        if (filter.getFilterType() == Filter.GEOMETRY_BBOX || 
		filter.getFilterType() == Filter.GEOMETRY_EQUALS || 
		filter.getFilterType() == Filter.GEOMETRY_INTERSECTS || 
		filter.getFilterType() == Filter.GEOMETRY_TOUCHES || 
		filter.getFilterType() == Filter.GEOMETRY_CROSSES || 
		filter.getFilterType() == Filter.GEOMETRY_WITHIN || 
		filter.getFilterType() == Filter.GEOMETRY_CONTAINS || 
		filter.getFilterType() == Filter.GEOMETRY_OVERLAPS || 
		filter.getFilterType() == Filter.GEOMETRY_DWITHIN ) {
            DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
            DefaultExpression right = (DefaultExpression) filter
                .getRightGeometry();

            if (left != null) {
                left.accept(this);
            }

            if (right != null) {
                right.accept(this);
            }
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
     */
    public void visit(LikeFilter filter) {
        LOGGER.finest("LikeFilter ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
     */
    public void visit(LogicFilter filter) {
        switch (filter.getFilterType()) {
        case AbstractFilter.LOGIC_NOT:
            LOGGER.finest("[NOT] LogicFilter ignored!");

            break;

        case AbstractFilter.LOGIC_OR:
            LOGGER.finest("[OR] LogicFilter ignored!");

            break;

        case AbstractFilter.LOGIC_AND:

            Iterator list = filter.getFilterIterator();

            while (list.hasNext()) {
                ((AbstractFilter) list.next()).accept(this);
            }

            break;

        default:
            LOGGER.finest("LogicFilter ignored!");
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
     */
    public void visit(NullFilter filter) {
        LOGGER.finest("NullFilter ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
     */
    public void visit(FidFilter filter) {
        LOGGER.finest("FidFilter ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.AttributeExpression)
     */
    public void visit(AttributeExpression expression) {
        LOGGER.finest("AttributeExpression ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.Expression)
     */
    public void visit(Expression expression) {
        LOGGER.finest("Expression ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.LiteralExpression)
     */
    public void visit(LiteralExpression expression) {
        if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY) {
            Geometry bbox = (Geometry) expression.getLiteral();

            if (this.bounds == null) {
                this.bounds = bbox.getEnvelopeInternal();
            } else {
                this.bounds.expandToInclude(bbox.getEnvelopeInternal());
            }
        } else {
            LOGGER.warning("LiteralExpression ignored!");
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.MathExpression)
     */
    public void visit(MathExpression expression) {
        LOGGER.finest("MathExpression ignored!");
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.FunctionExpression)
     */
    public void visit(FunctionExpression expression) {
        LOGGER.finest("FunctionExpression ignored!");
    }
}
