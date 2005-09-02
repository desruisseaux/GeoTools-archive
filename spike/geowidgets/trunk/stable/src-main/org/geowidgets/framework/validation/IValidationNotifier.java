/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation;

/** Cares about signalling the result of a validation to the user.
 * Feedback could be given by colors and/or by displaying the error message
 * in case the validation failed.
 * Caution: Displaying a popup message dialog everytime a validation fails
 * is not a good idea, since validation is done <b>everytime</b> the user
 * types/changes anything. Better do something "in the background". <p>
 * Note: The IValidationNotifier is just a "IValidationListener" with a special
 * purpose, no additional methods and functions are defined.
 */
public interface IValidationNotifier extends IValidationListener {

}
