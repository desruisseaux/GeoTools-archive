package org.geotools.styling;

/**
 * Allows for processing of StyleDeltas.
 * <p>
 * Example Usage:
 * 
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
 * @author Jody Garnett
 *
 */
public interface StyleDeltaVisitor {

	/**
     * Visits the given delta.
     * 
     * @return <code>true</code> if the delta's children should be visited;
     *         <code>false</code> if they should be skipped.
     */
    public boolean visit( StyleDelta delta );
}
