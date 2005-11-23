package org.geotools.catalog;

import java.io.IOException;

/**
 * Allows processing of resolve deltas.
 * <p>
 * Usage:
 * 
 * <pre>
 *  class Visitor implements IResolveDeltaVisitor {
 *      public boolean visit(IResolveDelta delta) {
 *          switch (delta.getKind()) {
 *          case IDelta.ADDED :
 *              // handle added handled
 *              break;
 *          case IDelta.REMOVED :
 *              // handle removed handled
 *              break;
 *          case IDelta.CHANGED :
 *              // handle changed handled
 *              break;
 *          case IDelta.REPLACED :
 *              // handle replaced handled
 *              break;    
 *          }
 *          return true;
 *      }
 *  }
 *  ICatalogDelta rootDelta = ...;
 *  rootDelta.accept(new Visitor());
 * </pre>
 * 
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 * @since 0.9.0
 */
public interface ResolveDeltaVisitor {

    /**
     * Visits the given resolve delta.
     * 
     * @return <code>true</code> if the resource delta's children should be visited;
     *         <code>false</code> if they should be skipped.
     * @exception CoreException if the visit fails for some reason.
     */
    boolean visit( ResolveDelta delta ) throws IOException;
}