package edu.psu.geovista.geotools.filter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import javax.swing.event.EventListenerList;
import org.geotools.filter.Filter;

public class DynamicFilterBean {

  private DynamicFilter filter;
  private int min, max;
  private EventListenerList ell = new EventListenerList();

  public DynamicFilterBean() {
    filter = new DynamicFilter();
    min = 0;
    max = 100;
    for (int x = min; x <= max; x++){
      filter.addFid("" + x);
    }
    fireFilter();
  }

  public void setMinimum(int i) {
    int oldMin = min;
    min = i;
    if (min > oldMin){
      for (int x = oldMin; x < min; x++){
        filter.removeFid("" + x);
      }
    } else {
      for (int x = min; x <= oldMin; x++){
        if (x <= max)
          filter.addFid("" + x);
      }
    }
    fireFilter();
  }

  public void setMaximum(int i) {
    int oldMax = max;
    max = i;
    if (max > oldMax){
      for (int x = oldMax; x <= max; x++){
        if (x >= min)
          filter.addFid("" + x);
      }
    } else {
      for (int x = max + 1; x <= oldMax; x++){
        filter.removeFid("" + x);
      }
    }
    fireFilter();
  }

  public Filter getFilter(){
      return filter;
  }
  
  public void addFilterListener(FilterListener fl){
    ell.add(FilterListener.class, fl);
  }

  public void removeFilterListener(FilterListener fl){
    ell.remove(FilterListener.class, fl);
  }

  public void fireFilter(){
    Object[] listeners = ell.getListenerList();
    int numListeners = listeners.length;
    FilterEvent fe = new FilterEvent(this, filter);
    for (int i = 0; i < numListeners; i++){
      if (listeners[i]==FilterListener.class) {
        // pass the event to the listeners event dispatch method
        ((FilterListener)listeners[i+1]).filterChange(fe);
      }
    }
  }


}