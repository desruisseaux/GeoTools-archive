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
package org.geotools.event;

/**
 * Allows for processing of StyleDeltas.
 * 
 * <p>
 * Example Usage:
 * <pre>
 *  class Visitor implements StyleDeltaVisitor {
 *      public boolean visit(StyleDelta delta) {
 *          switch (delta.getKind()) {
 *          case StyleDelta.Kind.ADDED :
 *              // handle added handled
 *              break;
 *          case StyleDelta.Kind.REMOVED :
 *              // handle removed handled
 *              break;
 *          case StyleDelta.Kind.CHANGED :
 *              // handle changed handled
 *              break;
 *          }
 *          return true; // visit children
 *      }
 *  }
 *  StyleDelta delta = styleEvent.getDelta();
 *  delta.accept(new Visitor());
 * </pre>
 * </p>
 *
 * @author Jody Garnett
 */
public interface GTDeltaVisitor {
    /**
     * Visits the given delta.
     *
     * @param delta DOCUMENT ME!
     *
     * @return <code>true</code> if the delta's children should be visited;
     *         <code>false</code> if they should be skipped.
     */
    public boolean visit(GTDelta delta);
}
