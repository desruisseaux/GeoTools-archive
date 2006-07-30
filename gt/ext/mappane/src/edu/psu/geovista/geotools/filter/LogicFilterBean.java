package edu.psu.geovista.geotools.filter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import java.io.Serializable;
import org.geotools.filter.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Point;
import javax.swing.event.EventListenerList;
import java.util.EventObject;


public class LogicFilterBean implements Serializable{

  private LogicFilter filter;
  private FilterFactory factory;
  private EventListenerList ell = new EventListenerList();

  public LogicFilterBean() throws Exception{
    factory = FilterFactoryFinder.createFilterFactory();
    filter = factory.createLogicFilter((short) 42); //OBVIOUSLY, (short) 42 IS A PLACEHOLDER FOR WHEN I FIGURE OUT HOW TO WORK LOGIC FILTERS!

    fireFilter();
  }

  public Filter getFilter(){
      return filter;
  }
  
  //WHAT ELSE IS NEEDED HERE?

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