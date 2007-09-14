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

package org.geotools.gui.swing.datachooser.model;

import java.util.ArrayList;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.geotools.data.DataStore;
import org.jdesktop.swingx.JXTable;

/**
 * @author johann sorel
 */
public class VectorTableModel implements TableModel{
    
    
    private ArrayList<String> columns = new ArrayList<String>();
    private ArrayList<Class> classes = new ArrayList<Class>();
    private ArrayList<DataStore> stores = new ArrayList<DataStore>();
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<String> adresses = new ArrayList<String>();
    private JXTable tab;
    
    /** Creates a new instance of BasicTableModel 
     * @param tab 
     */
    public VectorTableModel(JXTable tab) {
        super();
        this.tab = tab;
        init();
    }
    
    private void init(){
        columns.add("Nom");
        columns.add("Adresse");
        classes.add(String.class);
        classes.add(String.class); 
        tab.revalidate();
    }
    
    
    public void removeSelected(){
        for(int i=tab.getSelectedRows().length-1; i>=0; i--){
            stores.remove(tab.getSelectedRows()[i]);
            names.remove(tab.getSelectedRows()[i]);
            adresses.remove(tab.getSelectedRows()[i]);
        }
        tab.revalidate();
        tab.repaint();
    }
    
    public void addSource(DataStore store,String adresse,String name){
        stores.add(store);
        adresses.add(adresse);
        names.add(name);
        tab.revalidate();
        tab.repaint();
    }
    
    public int getColumnCount(){
        return columns.size();
    }
    
    public Class getColumnClass(int i){
        return classes.get(i);
    }
    
    public String getColumnName(int column) {
        return columns.get(column);
    }
    
    public int getRowCount() {
        return adresses.size();
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex){
            case 0 : return names.get(rowIndex);
            case 1 : return adresses.get(rowIndex);
            default : return "n/a";
        }
    }
    
    public DataStore getDataStore(int rowIndex){
        return stores.get(rowIndex);
    }
    
    public String getName(int rowIndex){
        return names.get(rowIndex);
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    public void addTableModelListener(TableModelListener l) {}
    public void removeTableModelListener(TableModelListener l) {}
    
}
