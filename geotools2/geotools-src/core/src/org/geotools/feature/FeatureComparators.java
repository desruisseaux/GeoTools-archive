/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.feature;

/** A utility class for creating simple Comparators for Features.
 * @author Ian Schneider
 */
public class FeatureComparators {
  
  /** A utility comparator for comparison by id.
   */
  public static final java.util.Comparator BY_ID = new java.util.Comparator() {
    public int compare(Object o1, Object o2) {
      Feature f1 = (Feature) o1;
      Feature f2 = (Feature) o2;
      return f1.getID().compareTo(f2.getID());
    }
  };
  
  /** Create a Comparator which compares Features by the attribute at the given index.
   * The attribute at the index MUST be Comparable. This will probably not work for
   * heterogenous collections, UNLESS the classes at the given index are the same.
   * @param idx The index to look up attributes at.
   * @return A new Comparator.
   */  
  public static java.util.Comparator byAttributeIndex(final int idx) {
    return new Index(idx);
  }
  
  /** Create a Comparator which compares Features by the attribute found at the given
   * path. The attribute found MUST be Comparable. This will probably not work for
   * heterogenous collections, UNLESS the attributes found are the same class.
   * @param name The xpath to use while comparing.
   * @return A new Comparator.
   */  
  public static java.util.Comparator byAttributeName(final String name) {
    return new Name(name);
  }
  
  public static class Index implements java.util.Comparator {
    
    final int idx;
    
    public Index(int i) {
      idx = i;
    }
    
    public int compare(Object o1, Object o2) {
      Feature f1 = (Feature) o1;
      Feature f2 = (Feature) o2;
      return compareAtts(f1.getAttribute(idx),f2.getAttribute(idx));
    }
    
    protected int compareAtts(Object att1,Object att2) {
      return ((Comparable) att1).compareTo((Comparable) att2); 
    }
    
  }
  
  public static class Name implements java.util.Comparator {
    
    final String name;
    
    public Name(String name) {
      this.name = name;
    }
    
    public int compare(Object o1, Object o2) {
      Feature f1 = (Feature) o1;
      Feature f2 = (Feature) o2;
      return compareAtts(f1.getAttribute(name),f2.getAttribute(name));
    }
    
    protected int compareAtts(Object att1,Object att2) {
      if (att1 == null && att2 == null) return 0;
      return ((Comparable) att1).compareTo((Comparable) att2); 
    }
    
  }
  
}
