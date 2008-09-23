/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.BuildResultStack;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Touches;

/**
 * Bulds a {@link Touches} filter
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class SpatialTouchesBuilder extends SpatialOperationBuilder {

    public SpatialTouchesBuilder(BuildResultStack resultStack,
            FilterFactory filterFactory) {
        super(resultStack, filterFactory);
        
    }

    /* (non-Javadoc)
     * @see org.geotools.filter.text.txt.SpatialOperationBuilder#buildFilter(org.opengis.filter.expression.Expression, org.opengis.filter.expression.Expression)
     */
    @Override
    protected BinarySpatialOperator buildFilter(final Expression expr1,
            final Expression expr2) {

        return getFilterFactory().touches(expr1, expr2);
    }

}
