/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.renderer.lite;

import java.util.Iterator;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author jones
 * 
 * @source $URL$
 * @deprecated
 */
public class BoundsExtractor implements FilterVisitor {
	Envelope clippedbbox;

	private short logicType = Filter.LOGIC_AND;

	public BoundsExtractor(Envelope bbox) {
		this.clippedbbox = bbox;
	}

	public BoundsExtractor(int minx, int maxx, int miny, int maxy) {
		this.clippedbbox = new Envelope(minx, maxx, miny, maxy);
	}

	public void visit(Filter filter) {
		if (Filter.NONE == filter) {
			return;
		}
		switch (filter.getFilterType()) {
		case FilterType.BETWEEN:
			visit((BetweenFilter) filter);

			break;

		case FilterType.COMPARE_EQUALS:
		case FilterType.COMPARE_GREATER_THAN:
		case FilterType.COMPARE_GREATER_THAN_EQUAL:
		case FilterType.COMPARE_LESS_THAN:
		case FilterType.COMPARE_LESS_THAN_EQUAL:
		case FilterType.COMPARE_NOT_EQUALS:
			visit((BetweenFilter) filter);

			break;

		case FilterType.FID:
			visit((BetweenFilter) filter);

			break;

		case FilterType.GEOMETRY_BBOX:
		case FilterType.GEOMETRY_BEYOND:
		case FilterType.GEOMETRY_CONTAINS:
		case FilterType.GEOMETRY_CROSSES:
		case FilterType.GEOMETRY_DISJOINT:
		case FilterType.GEOMETRY_DWITHIN:
		case FilterType.GEOMETRY_EQUALS:
		case FilterType.GEOMETRY_INTERSECTS:
		case FilterType.GEOMETRY_OVERLAPS:
		case FilterType.GEOMETRY_TOUCHES:
		case FilterType.GEOMETRY_WITHIN:
			visit((GeometryFilter) filter);

			break;

		case FilterType.LIKE:
			visit((LikeFilter) filter);

			break;

		case FilterType.LOGIC_AND:
		case FilterType.LOGIC_NOT:
		case FilterType.LOGIC_OR:
			visit((LogicFilter) filter);

			break;

		case FilterType.NULL:
			visit((NullFilter) filter);

			break;

		default:
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
	 */
	public void visit(BetweenFilter filter) {
		if (filter != null) {
			if (filter.getLeftValue() != null)
				filter.getLeftValue().accept(this);
			if (filter.getRightValue() != null)
				filter.getRightValue().accept(this);
			if (filter.getMiddleValue() != null)
				filter.getMiddleValue().accept(this);
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
	 */
	public void visit(CompareFilter filter) {
		if (filter != null) {
			if (filter.getLeftValue() != null)
				filter.getLeftValue().accept(this);
			if (filter.getRightValue() != null)
				filter.getRightValue().accept(this);
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
	 */
	public void visit(GeometryFilter filter) {
		if (filter != null) {

			LiteralExpression le = null;
			Envelope bbox = null;
			if (filter.getLeftGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
				le = (LiteralExpression) filter.getLeftGeometry();
				if (le != null && le.getLiteral() != null
						&& le.getLiteral() instanceof Geometry) {
					bbox = ((Geometry) le.getLiteral()).getEnvelopeInternal();
				}
			} else {
				if (filter.getRightGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
					le = (LiteralExpression) filter.getRightGeometry();
					if (le != null && le.getLiteral() != null
							&& le.getLiteral() instanceof Geometry) {
						Geometry g = (Geometry) le.getLiteral();
						bbox = g.getEnvelopeInternal();
					}
				}
			}

			if (bbox != null) {
				switch (logicType) {
				case Filter.LOGIC_AND:
					and(bbox, filter.getFilterType());
					break;

				case Filter.LOGIC_OR:
					or(bbox, filter.getFilterType());
					break;

				default:
					break;
				}
			}

		}
	}

	private void or(Envelope bbox, short s) {
		switch (s) {

		case FilterType.GEOMETRY_BBOX:
		case FilterType.GEOMETRY_CONTAINS:
		case FilterType.GEOMETRY_CROSSES:
		case FilterType.GEOMETRY_DWITHIN:
		case FilterType.GEOMETRY_EQUALS:
		case FilterType.GEOMETRY_INTERSECTS:
		case FilterType.GEOMETRY_OVERLAPS:
		case FilterType.GEOMETRY_TOUCHES:
		case FilterType.GEOMETRY_WITHIN:
			if (!bbox.intersects(clippedbbox)) {
				if (clippedbbox == null || clippedbbox.isNull())
					clippedbbox = bbox;
				else
					clippedbbox.expandToInclude(bbox);
			} else {
				boolean changed = false;
				double minx, miny, maxx, maxy;
				minx = clippedbbox.getMinX();
				miny = clippedbbox.getMinY();
				maxx = clippedbbox.getMaxX();
				maxy = clippedbbox.getMaxY();
				if (minx > bbox.getMinX()) {
					minx = bbox.getMinX();
					changed = true;
				}
				if (maxx < bbox.getMaxX()) {
					maxx = bbox.getMaxX();
					changed = true;
				}
				if (miny > bbox.getMinY()) {
					miny = bbox.getMinY();
					changed = true;
				}
				if (maxy < bbox.getMaxY()) {
					maxy = bbox.getMaxY();
					changed = true;
				}
				if (changed) {
					clippedbbox = new Envelope(minx, maxx, miny, maxy);
				}
			}
			return;
		case FilterType.GEOMETRY_BEYOND:
		case FilterType.GEOMETRY_DISJOINT:
			return;
		}
	}

	private void and(Envelope bbox, short s) {
		switch (s) {

		case FilterType.GEOMETRY_BBOX:
		case FilterType.GEOMETRY_CONTAINS:
		case FilterType.GEOMETRY_CROSSES:
		case FilterType.GEOMETRY_DWITHIN:
		case FilterType.GEOMETRY_EQUALS:
		case FilterType.GEOMETRY_INTERSECTS:
		case FilterType.GEOMETRY_OVERLAPS:
		case FilterType.GEOMETRY_TOUCHES:
		case FilterType.GEOMETRY_WITHIN:
			if (!bbox.intersects(clippedbbox)) {
				clippedbbox = new Envelope(clippedbbox.getMinX(), clippedbbox
						.getMinX(), clippedbbox.getMinY(), clippedbbox
						.getMinY());
			} else {
				boolean changed = false;
				double minx, miny, maxx, maxy;
				minx = clippedbbox.getMinX();
				miny = clippedbbox.getMinY();
				maxx = clippedbbox.getMaxX();
				maxy = clippedbbox.getMaxY();
				if (minx < bbox.getMinX()) {
					minx = bbox.getMinX();
					changed = true;
				}
				if (maxx > bbox.getMaxX()) {
					maxx = bbox.getMaxX();
					changed = true;
				}
				if (miny < bbox.getMinY()) {
					miny = bbox.getMinY();
					changed = true;
				}
				if (maxy > bbox.getMaxY()) {
					maxy = bbox.getMaxY();
					changed = true;
				}
				if (changed) {
					clippedbbox = new Envelope(minx, maxx, miny, maxy);
				}
			}
			return;
		case FilterType.GEOMETRY_BEYOND:
		case FilterType.GEOMETRY_DISJOINT:
			return;
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
	 */
	public void visit(LikeFilter filter) {
		if (filter != null) {
			if (filter.getValue() != null)
				filter.getValue().accept(this);
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
	 */
	public void visit(LogicFilter filter) {

		short oldType = logicType;
		if (filter != null) {
			switch (filter.getFilterType()) {
			case Filter.LOGIC_OR: {
				Envelope original = clippedbbox;
				clippedbbox = new Envelope();
				logicType = logicType == Filter.LOGIC_NOT ? logicType
						: Filter.LOGIC_OR;
				Iterator i = filter.getFilterIterator();
				while (i.hasNext()) {
					Filter tmp = (Filter) i.next();
					tmp.accept(this);
				}
				if (logicType != Filter.LOGIC_NOT) {
					logicType = Filter.LOGIC_AND;

					Envelope newBbox = clippedbbox;
					clippedbbox = original;

					and(newBbox, Filter.GEOMETRY_INTERSECTS);
				}
				break;
			}
			case Filter.LOGIC_AND: {
				Iterator i = filter.getFilterIterator();
				logicType = logicType == Filter.LOGIC_NOT ? logicType
						: Filter.LOGIC_AND;
				while (i.hasNext()) {
					Filter tmp = (Filter) i.next();
					tmp.accept(this);
				}

				break;
			}
			case Filter.LOGIC_NOT:
				Iterator i = filter.getFilterIterator();
				logicType = logicType == Filter.LOGIC_NOT ? Filter.LOGIC_AND
						: Filter.LOGIC_NOT;
				while (i.hasNext()) {
					Filter tmp = (Filter) i.next();
					tmp.accept(this);
				}
			default:
				break;
			}
			logicType = oldType;

		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
	 */
	public void visit(NullFilter filter) {
		if (filter != null) {
			if (filter.getNullCheckValue() != null)
				filter.getNullCheckValue().accept(this);
		}
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
	 */
	public void visit(FidFilter filter) {
		// do nothing
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
	 */
	public void visit(AttributeExpression expression) {
		// do nothing
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
	 */
	public void visit(Expression expression) {
		// do nothing
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
	 */
	public void visit(LiteralExpression expression) {
		// do nothing
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
	 */
	public void visit(MathExpression expression) {
		// do nothing
	}

	/*
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
	 */
	public void visit(FunctionExpression expression) {
		// do nothing
	}

	public Envelope getBBox() {
		return clippedbbox;
	}
}
