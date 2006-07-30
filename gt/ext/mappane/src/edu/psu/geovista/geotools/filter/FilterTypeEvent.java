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

public class FilterTypeEvent extends EventObject {

short type;

  public FilterTypeEvent(Object source, short s) {
    super(source);
    type = s;
  }

  public short getType(){
    return type;
  }
}