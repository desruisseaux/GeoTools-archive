package org.geotools.graph.util;

import java.util.HashMap;

public class IndexedStack extends java.util.Stack {
  private HashMap m_index; //object to index in stack 
  
  public IndexedStack() {
    super();
    m_index = new HashMap();  
  }
  
  public Object push(Object item) {
    m_index.put(item, new Integer(size()));
    return super.push(item);
  }

  public Object pop() {
    Object value = super.pop();
    m_index.remove(value);
    return(value);
  }

  public boolean contains(Object elem) {
    return(m_index.get(elem) != null);
  }

}