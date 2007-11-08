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
package org.geotools.gui.swing.contexttree.column;


import org.geotools.gui.swing.contexttree.renderer.DefaultCellEditor;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;


/**
 * @author johann sorel
 */
public class OpacityTreeTableColumn extends TreeTableColumn {


    /**
     * Creates a new instance of JXVisibleColumn
     */
    public OpacityTreeTableColumn() {
        super();
        
        setHeaderRenderer(new DefaultHeaderRenderer(IconBundle.getResource().getIcon("16_opacity"),null,TextBundle.getResource().getString("col_opacity")));
        setCellRenderer(new DefaultCellRenderer( new OpacityComponent()));
        setCellEditor(new DefaultCellEditor( new OpacityComponent()));

        setEditable(true);
        setResizable(false);
        setMaxWidth(60);
        setMinWidth(60);
        setPreferredWidth(60);
        setWidth(25);
    }

    public void setValue(Object target, Object value) {

    }

    public Object getValue(Object target) {

        if (target instanceof MapLayer) {
            return target;
        } else {
            return "n/a";
        }
    }

    public String getName() {
        return TextBundle.getResource().getString("col_symbol");
    }

    public boolean isCellEditable(Object target) {

        if (target instanceof MapLayer) {
            return isEditable();
        } else {
            return false;
        }
    }

    public Class getColumnClass() {
        return Boolean.class;
    }

    @Override
    public boolean isEditableOnMouseOver() {
        return true;
    }


}

