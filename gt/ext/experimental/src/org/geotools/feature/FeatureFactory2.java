package org.geotools.feature;

/**
 * Experiment land, these changes will be made to FeatureFactory interface unless any can
 * think of a reason against.
 * <p>
 * <ul>Q & A:
 * <li>Q: How not to break code?
 * <br>A: Move the FeatureFactory methods of create( Object[]) and create(Object[], String id) to FeatureType.
 *        The only code scene in the wild makes use of FeatureType.create( ... ) directly. We will have to
 *        stop FeatureType of extending FeatureFactory though (they *are* different after all).
 * <li>
 * </ul>
 * </p>
 * <p>
 * This method is expected to pass on any hints provided via a Map passed to the Constructor, in the event
 * a only a no argument constructor is found FeatureFactory will log a warning, in the Geotools 2.2 timeframe
 * this will be upgraded to an error.
 * </p>
 * @author Jody Garnett
 */
public interface FeatureFactory2 {

}
