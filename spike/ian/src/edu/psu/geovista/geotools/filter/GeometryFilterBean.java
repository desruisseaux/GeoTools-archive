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


public class GeometryFilterBean implements Serializable{

  private GeometryFilter filter;
  private FilterFactory factory;
  private EventListenerList ell = new EventListenerList();

  public GeometryFilterBean() throws Exception{
    factory = FilterFactoryFinder.createFilterFactory();
    filter = factory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
    fireFilter();
  }

  public void setType(short s) throws Exception{
    filter = factory.createGeometryFilter(s);
    fireFilter();
  }

  public void setType(FilterTypeEvent fte){
    try{
      setType(fte.getType());
    }
    catch (Exception e){
      System.out.println("There was an exception in trying to set the filter type for a geometry filter. /n" + e.getMessage());
    }
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