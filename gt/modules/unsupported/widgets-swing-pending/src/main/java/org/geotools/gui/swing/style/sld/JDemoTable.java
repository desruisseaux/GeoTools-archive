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

package org.geotools.gui.swing.style.sld;

import java.awt.Color;

import javax.swing.ListSelectionModel;

import org.geotools.gui.swing.contexttree.renderer.StyleCellProvider;
import org.geotools.gui.swing.contexttree.renderer.StyleCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.ComponentProvider;

/**
 *
 * @author johann sorel
 */
public class JDemoTable extends JXTable{

    /**
     * Table for style exemple
     */
    public JDemoTable(){
        super();
    }
        
    /**
     * 
     * @param sldsource path to sld file
     */
    public void setSLDSource(String sldsource){
        setModel(new DemoTableModel(sldsource ));
        setHorizontalScrollEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ComponentProvider myProvider = new StyleCellProvider();
        getColumnExt(0).setCellRenderer(new StyleCellRenderer(myProvider));
        getColumnExt(0).setMaxWidth(25);
        getColumnExt(0).setMinWidth(25);
        getColumnExt(0).setPreferredWidth(25);
        getColumnExt(0).setWidth(25);
        setTableHeader(null);
        setGridColor(Color.LIGHT_GRAY);
        setShowVerticalLines(false);
        setColumnMargin(0);
        setRowMargin(0);
    }
    
    
}
