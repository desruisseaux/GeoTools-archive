package edu.psu.geovista.geotools.filter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import java.util.EventListener;
import org.geotools.filter.Filter;
import java.util.EventObject;

public interface FilterListener extends EventListener{

  public void filterChange(FilterEvent fe);

}