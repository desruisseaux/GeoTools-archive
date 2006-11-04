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
package org.geotools.data.oracle.io;

import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: aaime $
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/jdbc/src/main/java/org/geotools/data/jdbc/MutableFIDFeature.java $
 * @version $Id: MutableFIDFeature.java 22347 2006-10-24 01:57:23Z jdeolive $
 * Last Modified: $Date: 2004/04/09 15:30:52 $ 
 */
public class MutableFIDFeature extends DefaultFeature {

  public MutableFIDFeature(DefaultFeatureType ft, Object[] attributes, String fid)
    throws IllegalAttributeException {
    super(ft, attributes, fid);

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
}
