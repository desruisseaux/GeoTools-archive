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

public class GeometrySupportComboBox extends JPanel implements Serializable, ActionListener {

  JComboBox jcb;
  EventListenerList filterTypeList = new EventListenerList();

  public GeometrySupportComboBox() {
    String[] options = {"Bounding box", "Equals", "Disjoint", "Intersects", "Touches", "Crosses", "Within", "Contains", "Overlaps", "Beyond", "DWithin"}; //What is DWithin anyways?
    jcb = new JComboBox(options);
    add(jcb);

    jcb.addActionListener(this);
  }

  public short getSelectedType(){
    int i = jcb.getSelectedIndex();
    short toBeReturned = 0;
    switch(i){
      case 0:
        toBeReturned = AbstractFilter.GEOMETRY_BBOX;
        break;
      case 1:
        toBeReturned = AbstractFilter.GEOMETRY_EQUALS;
        break;
      case 2:
        toBeReturned = AbstractFilter.GEOMETRY_DISJOINT;
        break;
      case 3:
        toBeReturned = AbstractFilter.GEOMETRY_INTERSECTS;
        break;
      case 4:
        toBeReturned = AbstractFilter.GEOMETRY_TOUCHES;
        break;
      case 5:
        toBeReturned = AbstractFilter.GEOMETRY_CROSSES;
        break;
      case 6:
        toBeReturned = AbstractFilter.GEOMETRY_WITHIN;
        break;
      case 7:
        toBeReturned = AbstractFilter.GEOMETRY_CONTAINS;
        break;
      case 8:
        toBeReturned = AbstractFilter.GEOMETRY_OVERLAPS;
        break;
      case 9:
        toBeReturned = AbstractFilter.GEOMETRY_BEYOND;
        break;
      case 10:
        toBeReturned = AbstractFilter.GEOMETRY_DWITHIN;
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