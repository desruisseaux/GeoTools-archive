package org.geotools.graph.util;

public interface Queue {
 
  public void enq(Object object);
  
  public Object deq();
  
  public boolean isEmpty();
  
  public void clear(); 
}