package edu.psu.geovista.geotools.filter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import java.util.EventObject;
import org.geotools.filter.Filter;

public class FilterEvent extends EventObject{

  private Filter filter;
  private boolean inProgress;

  public FilterEvent(Object source, Filter f) {
    this(source,f,false);
  }
  
  public FilterEvent(Object source, Filter f, boolean inProgress){
    super(source);
    filter = f;
    this.inProgress = inProgress;
  }

  public Filter getFilter(){
    return filter;
  }
  
  public boolean isInProgress(){
      return inProgress;
  }

}