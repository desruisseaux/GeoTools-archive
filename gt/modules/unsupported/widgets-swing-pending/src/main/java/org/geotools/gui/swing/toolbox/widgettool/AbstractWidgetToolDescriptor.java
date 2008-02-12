/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.toolbox.widgettool;

import org.geotools.gui.swing.toolbox.Parameter;

/**
 *
 * @author johann sorel
 */
public abstract class AbstractWidgetToolDescriptor implements WidgetToolDescriptor{

    public String getDescription() {
        return "";
    }

    public String[] getPath() {
        return EMPTY_STRING_ARRAY;
    }

    public String[] getKeyWords() {
        return EMPTY_STRING_ARRAY;
    }

    public String[] getCategories() {
        return EMPTY_STRING_ARRAY;
    }

    public Parameter[] getParametersInfo() {
        return EMPTY_PARAMETER_ARRAY;
    }

}
