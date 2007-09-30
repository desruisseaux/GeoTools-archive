/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 * 
 *    Created on 13/01/2004
 */
package org.geotools.data.jdbc;

import java.util.List;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: aaime $
 * @source $URL$
 * @version $Id$
 * Last Modified: $Date: 2004/04/09 15:30:52 $ 
 */
public class MutableFIDFeature extends SimpleFeatureImpl {

	String featureId;
	
  public MutableFIDFeature(List properties, SimpleFeatureType ft, String fid)
    throws IllegalAttributeException {
    super(properties, ft, fid);

  }

  /**
   * Sets the FID, used by datastores only.
   * 
   * I would love to protect this for safety reason, i.e. so client classes can't
   *  use it by casting to it.
   * 
   * @param id The fid to set.
   */
  public void setID(String id) {
	  this.featureId = id;
  }
  
  public String getID() {
	  return featureId;
  }
}
