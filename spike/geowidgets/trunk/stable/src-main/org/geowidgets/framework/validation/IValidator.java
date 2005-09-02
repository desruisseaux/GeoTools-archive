/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation;

import java.util.Set;

/** A class that validates user inputs and or selections and decides
 * if they are correct or, for instance incomplete ore ambigous.
 * A validator implementation should get constructed with one or several
 * UI elements as agruments (depending on its task). The validator is responsible
 * for registering as a listener to them and performing its check as the values
 * change. It is the responsibility of the UI element(s) to register at the
 * IValidator implementation to recieve the result of the validation.
 * 
 * @see IValidationListener
 * @author Matthias Basler
 */
public interface IValidator {
    /** Initialize validation and notify the listeners about the result.
     * @return true if the validation was passed, false if it failed. */
    public boolean validate();

    /** @return the result of the last validation <b>without</b> validating again. */
    public boolean getPreviousResult();

    /** Register a new listener to get notified for validation results.
     * @param listener a listener to add */
    public void addValidationListener(IValidationListener listener);

    /** Remove a listener from the list of those that listen for valudation results.
     * @param listener the listener to remove */
    public void removeValidationListener(IValidationListener listener);

    /** @return all validation listener, such as validation notifiers. */
    public Set<IValidationListener> getValidationListeners();
}
