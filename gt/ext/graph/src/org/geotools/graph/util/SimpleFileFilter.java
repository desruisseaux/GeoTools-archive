package org.geotools.graph.util;

import java.io.File;
import java.io.Serializable;

import javax.swing.filechooser.FileFilter;

public class SimpleFileFilter extends FileFilter implements Serializable {

  private String m_ext = null;
  private String m_desc = null;
  
  public SimpleFileFilter() {}
  
  public SimpleFileFilter(String ext, String desc) {
    this.m_ext = ext;
    this.m_desc = desc;  
  }
  
  public boolean accept(File f) {
    if (f.isDirectory()) return(true);
    String path = f.getAbsolutePath();
    if (path.length() < m_ext.length() + 1) return(false);
    return(path.substring(path.length()-4)).equals("." + m_ext);
  }

  public String getExtension() {
    return(m_ext);  
  }
  
  public String getDescription() {
    return(m_desc);
  }
  
  public boolean equals(Object o) {
    if (o == null) return(false);
    if (o instanceof SimpleFileFilter) {
      SimpleFileFilter other = (SimpleFileFilter)o;
      return(m_ext.equals(other.m_ext));  
    }
    return(false);    
  }
}