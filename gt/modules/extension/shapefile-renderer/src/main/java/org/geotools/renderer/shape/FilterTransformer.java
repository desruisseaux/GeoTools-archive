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
package org.geotools.renderer.shape;

import java.util.Iterator;

import org.geotools.filter.Filter;
import org.geotools.filter.Filters;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Transforms all GeometryExpressions with the provided transform.
 * @author Jesse
 */
public class FilterTransformer extends AbstractProcessFilterGeometiresVisitor {

    final MathTransform mt;
    
    public FilterTransformer(final MathTransform mt) {
        super();
        this.mt = mt;
    }


    public void visit(LogicFilter filter) {
        for (Iterator iter = filter.getFilterIterator(); iter.hasNext();) {
            Object element = iter.next();
            if( element==Filter.INCLUDE ){
                Filters.accept(Filter.NONE, this);
            }else
            if( element==Filter.EXCLUDE ){
                Filters.accept(Filter.ALL, this);
            }else{
                ((Filter) element).accept(this);
            }
        }
    }

    public void visit(LiteralExpression expression) {
        Object value = expression.getLiteral();
        try {
            if( value instanceof com.vividsolutions.jts.geom.Geometry ){
                expression.setLiteral(JTS.transform((com.vividsolutions.jts.geom.Geometry)value, mt));
        }
            if( value instanceof Envelope ){
                expression.setLiteral(JTS.transform((Envelope)value, mt));
        }
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (IllegalFilterException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

}
