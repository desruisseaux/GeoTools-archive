/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature;

import java.util.Collections;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Factory;

/**
 * A utility class for working with FeatureCollections.
 * Provides a mechanism for obtaining a FeatureCollection instance.
 * @author  Ian Schneider
 * @source $URL$
 */
public abstract class FeatureCollections implements Factory {

  /**
   * Holds a reference to a FeatureCollections implementation once
   * one has been requested for the first time using instance().
   */
  private static FeatureCollections instance = null;
  
  private static FeatureCollections instance() {
    if (instance == null) {
      instance = CommonFactoryFinder.getFeatureCollections( null );
    }
    return instance;
  }
  
  /**
   * create a new FeatureCollection using the current default factory.
   * @return A FeatureCollection instance.
   */
  public static FeatureCollection newCollection() {
    return instance().createCollection(); 
  }
  
  /**
   * Creates a new FeatureCollection with a particular id using the current 
   * default factory.
   * 
   * @param id The id of the feature collection.
   * 
   * @return A new FeatureCollection intsance with the specified id.
   * 
   * @since 2.4
   */
  public static FeatureCollection newCollection( String id ) {
	  return instance().createCollection( id );
  }
  
  /**
   * Subclasses must implement this to return a new FeatureCollection object.
   * @return A new FeatureCollection
   */
  protected abstract FeatureCollection createCollection();
  
  /**
   * Subclasses must implement this to return a new FeatureCollection object 
   * with a particular id.
   * 
   * @param id The identification of the feature collection.
   * 
   * @return A new FeatureCollection with the specified id. 
   */
  protected abstract FeatureCollection createCollection( String id );
  
  /**
   * Returns the implementation hints. The default implementation returns en empty map.
   */
  public Map getImplementationHints() {
    return Collections.EMPTY_MAP;
  }  
}
