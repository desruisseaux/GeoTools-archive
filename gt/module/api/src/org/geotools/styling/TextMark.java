/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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

import org.geotools.filter.Expression;


/**
 * Note: this isn't in the SLD spec
 */
public interface TextMark extends Mark {
    public Expression getSymbol();

    public void setSymbol(String symbol);

    public Font[] getFonts();

    public void setWellKnownName(Expression wellKnownName);

    public Expression getWellKnownName();

    public void addFont(Font font);

    public void setSymbol(Expression symbol);
}
