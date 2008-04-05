/**
 * Define a Process API used to wrap up processes for reuse.
 * <p>
 * This API is made available at three levels:
 * <ul>
 * <li>Process an interface similar in spirit to Runnable used to make spatial data manipulation, transformation, etc.. available to client code
 * <li>ProcessFinder used for discovery and creation of Process implementations
 * <li>ProcessFactory used to advertise additional implementations to the framework (via the FactorySPI plug-in system)
 * </ul>
 */
package org.geotools.process;