/*
 * PickleDataSource.java
 *
 * Created on June 4, 2003, 8:33 AM
 */

package org.geotools.data.pickle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

/**
 *
 * @author  Ian Schneider
 */
class PickleDataSource  {
  
  final String objectFile;
  final String classFile;
  
  /** Creates a new instance of PickleDataSource */
  public PickleDataSource(File parent,String name) {
    this.objectFile = new File(parent, name + ".obj").getAbsolutePath();
    this.classFile  = new File(parent, name + ".clz").getAbsolutePath();
  } 
  
  public void setFeatures(FeatureCollection collection) throws DataSourceException {
    FileOutputStream obj = null;
    FileOutputStream clz = null;
    PickledFeatureProtocol protocol = null;
    try {
      obj = new FileOutputStream(objectFile);
      clz = new FileOutputStream(classFile);
      protocol = PickledFeatureProtocol.getWriter(obj,clz);
      protocol.write(collection);
    } catch (IOException ioe) {
      throw new DataSourceException("IOError",ioe);
    } finally {
      try {
        if (obj != null)
          obj.close();
        if (clz != null)
          clz.close();
        if (protocol != null)
          protocol.close();
      } catch (Exception e) {}
    }
  }
  
  
  public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
    PickledFeatureProtocol protocol = null;
    try {
      FileInputStream obj = new FileInputStream(objectFile);
      FileInputStream clz = new FileInputStream(classFile);
      protocol = PickledFeatureProtocol.getReader(obj,clz);
      protocol.read(collection);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        protocol.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  public Feature getFeature(int idx) throws DataSourceException {
    FileInputStream obj = null;
    FileInputStream clz = null;
    PickledFeatureProtocol protocol = null;
    try {
      obj = new FileInputStream(objectFile);
      clz = new FileInputStream(classFile);
      protocol = PickledFeatureProtocol.getReader(obj,clz);
      return protocol.read(idx);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (obj != null)
          obj.close();
        if (clz != null)
          clz.close();
        if (protocol != null)
          protocol.close();
      } catch (Exception e) {}
    }
    return null;
  }
  
  public FeatureType getSchema() throws DataSourceException {
    return null;
  }    
  
}
