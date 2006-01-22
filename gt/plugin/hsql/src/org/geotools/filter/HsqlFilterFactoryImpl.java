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
/*
 * HsqlFilterFactoryImpl.java
 *
 * Created on 18 July 2005
 */
package org.geotools.filter;

/**
 * Extention to the FilterFactoryImpl, supplies the HSQL specific filters 
 * needed for query generation.
 *
 * @author Amr Alam, Refractions Research
 */
public class HsqlFilterFactoryImpl extends FilterFactoryImpl {
    /**
     * Creates a new instance of HsqlFilterFactoryImpl
     */
    public HsqlFilterFactoryImpl() {
    }

    /**
     * Creates a Geometry Expression with an initial schema.
     *
     * @param schema the schema to create with.
     *
     * @return The new Attribute Expression.
     */
    public GeometryExpressionImpl createGeometryExpression(String colName) {
        return new GeometryExpressionImpl(colName);
    }
}