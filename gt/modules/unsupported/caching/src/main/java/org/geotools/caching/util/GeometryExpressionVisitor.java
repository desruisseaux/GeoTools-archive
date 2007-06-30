/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.util;

import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;


public class GeometryExpressionVisitor implements ExpressionVisitor {
    public Object visit(NilExpression arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(Add arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(Divide arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(Function arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(Literal arg0, Object arg1) {
        return arg0.getValue();
    }

    public Object visit(Multiply arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(PropertyName arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(Subtract arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }
}
