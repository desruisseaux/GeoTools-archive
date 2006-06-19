package edu.psu.geovista.geotools.filter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import javax.swing.*;
import org.geotools.filter.AbstractFilter;
import java.awt.event.*;
import javax.swing.event.EventListenerList;
import java.io.Serializable;

public class CompareSupportComboBox extends JPanel implements Serializable, ActionListener {

  JComboBox jcb;
  EventListenerList filterTypeList = new EventListenerList();

  public CompareSupportComboBox() {
    String[] options = {"Less than", "Less than or equal to", "Equal to", "Not equal to", "Greater than or equal to", "Greater than"};
    jcb = new JComboBox(options);
    add(jcb);

    jcb.addActionListener(this);
  }

  public short getSelectedType(){
    int i = jcb.getSelectedIndex();
    short toBeReturned = 0;
    switch(i){
      case 0:
        toBeReturned = AbstractFilter.COMPARE_LESS_THAN;
        break;
      case 1:
        toBeReturned = AbstractFilter.COMPARE_LESS_THAN_EQUAL;
        break;
      case 2:
        toBeReturned = AbstractFilter.COMPARE_EQUALS;
        break;
      case 3:
        toBeReturned = AbstractFilter.COMPARE_NOT_EQUALS;
        break;
      case 4:
        toBeReturned = AbstractFilter.COMPARE_GREATER_THAN_EQUAL;
        break;
      case 5:
        toBeReturned = AbstractFilter.COMPARE_GREATER_THAN;
        break;
    }
    return toBeReturned;
  }

  public void actionPerformed(ActionEvent e){
    short type = getSelectedType();
    fireFilterType(type);
  }

  public void addFilterTypeListener(FilterTypeListener lrl){
    filterTypeList.add(FilterTypeListener.class, lrl);
  }

  public void removeFilterTypeListener(FilterTypeListener lrl){
    filterTypeList.remove(FilterTypeListener.class, lrl);
  }

  public void fireFilterType(short type){
    Object[] listeners = filterTypeList.getListenerList();
    int numListeners = listeners.length;
    FilterTypeEvent ndse = new FilterTypeEvent(this, type);
    for (int i = 0; i < numListeners; i++){
      if (listeners[i]==FilterTypeListener.class) {
        // pass the event to the listeners event dispatch method
        ((FilterTypeListener)listeners[i+1]).setType(ndse);
      }
    }
  }

}