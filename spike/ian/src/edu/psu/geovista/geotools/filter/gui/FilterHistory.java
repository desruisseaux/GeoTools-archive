/*
 * FilterHistory.java
 *
 * Created on May 25, 2005, 3:40 PM
 */

package edu.psu.geovista.geotools.filter.gui;

import edu.psu.geovista.geotools.filter.FilterEvent;
import edu.psu.geovista.geotools.filter.FilterListener;
import edu.psu.geovista.geotools.filter.model.FilterModel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geotools.filter.Filter;

/**
 *
 * @author jamesm
 */
public class FilterHistory extends JPanel{
    FilterModel model;
    final DefaultListModel listModel = new DefaultListModel();
    JList list = new JList();
    
    
    /** Creates a new instance of FilterHistory */
    public FilterHistory() {
        setLayout(new BorderLayout());
        add(list,BorderLayout.CENTER);
        list.setModel(listModel);
        list.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                Filter f = (Filter)list.getSelectedValue();
                model.setFilter(f);
            }
        });
    }
    
    public void setFilterModel(FilterModel m){
        this.model = m;
        model.addFilterListener(new FilterListener(){
            public void filterChange(FilterEvent fe){
                if(!model.isChanging()){
                    if(!listModel.contains(fe.getFilter())){
                        list.clearSelection();
                        listModel.add(0,fe.getFilter());
                    }
                }
            }
        });
    }
    
    
    
}
