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

package org.geotools.gui.swing.contexttree.renderer;

import javax.swing.JLabel;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;

/**
 *
 * @author johann sorel
 */
public class StyleCellProvider extends ComponentProvider<JLabel>{
    
    /** Creates a new instance of SymbolRendererProvider */
    public StyleCellProvider() {
    }

    @Override
    protected void format(CellContext cellContext) {   
        ((StyleViewComponent)rendererComponent).format(cellContext.getValue());       
    }

    @Override
    protected void configureState(CellContext cellContext) {}

    
    @Override
    protected StyleViewComponent createRendererComponent() {
        return new StyleViewComponent();
    }
    
}
