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

package org.geotools.gui.swing.toolbox.widgettool.shapecreation;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import org.geotools.gui.swing.toolbox.widgettool.shapecreation.Data.TYPE;

/**
 *
 * @author johann sorel
 */
public class TypeEditor implements TableCellEditor{

    private final EventListenerList LISTENERS = new EventListenerList();
    private JComboBox box = new JComboBox();
    
    TypeEditor(){        
        box.addItem(TYPE.INTEGER);
        box.addItem(TYPE.LONG);
        box.addItem(TYPE.DOUBLE);
        box.addItem(TYPE.STRING);
        box.addItem(TYPE.DATE);
    }
    
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {        
        box.setSelectedItem(value);
        return box;
    }

    public Object getCellEditorValue() {
        return box.getSelectedItem();
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        CellEditorListener[] lst = LISTENERS.getListeners(CellEditorListener.class);
        
        for(CellEditorListener l : lst){
            l.editingStopped(new ChangeEvent(this));
        }
        
        return true;
    }

    public void cancelCellEditing() {
        CellEditorListener[] lst = LISTENERS.getListeners(CellEditorListener.class);
        
        for(CellEditorListener l : lst){
            l.editingCanceled(new ChangeEvent(this));
        }
    }

    public void addCellEditorListener(CellEditorListener l) {
        LISTENERS.add(CellEditorListener.class, l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        LISTENERS.remove(CellEditorListener.class, l);
    }

}
