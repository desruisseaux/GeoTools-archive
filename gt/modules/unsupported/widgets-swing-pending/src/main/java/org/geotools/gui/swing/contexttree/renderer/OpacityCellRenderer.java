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


import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.RolloverRenderer;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.TableCellContext;

public class OpacityCellRenderer implements TableCellRenderer, RolloverRenderer, Serializable {
    
    protected ComponentProvider componentController;
    private CellContext<JTable> cellContext;
    
    
    
    /**
     * Instantiates a default table renderer with the given componentController.
     * If the controller is null, creates and uses a default. The default
     * controller is of type <code>LabelProvider</code>.
     *
     * @param componentController the provider of the configured component to
     *        use for cell rendering
     */
    public OpacityCellRenderer(ComponentProvider componentController) {
        if (componentController == null) {
            componentController = new LabelProvider();
        }
        this.componentController = componentController;
        this.cellContext = new TableCellContext();
    }
    
    /**
     * Instantiates a default table renderer with a default component
     * controller using the given converter.
     *
     * @param converter the converter to use for mapping the
     *   content value to a String representation.
     *
     */
    public OpacityCellRenderer(StringValue converter) {
        this(new LabelProvider(converter));
    }
    
    // -------------- implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns a configured component, appropriate to render the given
     * list cell.
     *
     * @param table the <code>JTable</code>
     * @param value the value to assign to the cell at <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     */
    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
        cellContext.installContext(table, value, row, column, isSelected, hasFocus,true, true);
        return componentController.getRendererComponent(cellContext);
                
    }
    /**
     * @param background
     */
    public void setBackground(Color background) {
        componentController.getRendererComponent(cellContext).setBackground(background);        
    }
    /**
     * @param foreground
     */
    public void setForeground(Color foreground) {
        componentController.getRendererComponent(cellContext).setForeground(foreground);        
    }
    
    //----------------- RolloverRenderer
    
    /**
     * {@inheritDoc}
     */
    public void doClick() {
        if (isEnabled()) {
            ((RolloverRenderer) componentController).doClick();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return (componentController instanceof RolloverRenderer) && ((RolloverRenderer) componentController).isEnabled();
        
    }
    
    
}
