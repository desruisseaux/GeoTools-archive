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


public class CompareFilterBean implements Serializable, FilterTypeListener{

  private CompareFilter filter;
  private FilterFactory factory;
  private EventListenerList ell = new EventListenerList();

  public CompareFilterBean() throws Exception{
    factory = FilterFactoryFinder.createFilterFactory();
    filter = factory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
    setAttribute("target");
    setComparisonValue(50);
    fireFilter();
  }

  public void setType(short s) throws Exception{
    filter = factory.createCompareFilter(s);
    fireFilter();
  }

  public void setType(FilterTypeEvent fte){
    try{
      setType(fte.getType());
    }
    catch (Exception e){
      System.out.println("There was an exception in trying to set the filter type for a comparison filter. /n" + e.getMessage());
    }
  }

  /**
   * @task REVISIT: hard-coding the feature types for the attribute expression.
   * Better to get these from the FeatureMaker, and then allow the user to select
   * one from a JComboBox
   */
  public void setAttribute(String s) throws Exception{
    AttributeType[] pointAttribute = new AttributeType[3];
    pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
    pointAttribute[1] = AttributeTypeFactory.newAttributeType("population", Double.class);
    pointAttribute[2] = AttributeTypeFactory.newAttributeType("target", Double.class);
    FeatureType pointType = FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");

    filter.addLeftValue(factory.createAttributeExpression(pointType, s));
    fireFilter();
  }

  public void setComparisonValue(int i) throws IllegalFilterException{
    filter.addRightValue(factory.createLiteralExpression(i));
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