/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation.impl;

import java.util.HashSet;
import java.util.Set;

import org.geowidgets.framework.validation.*;

/** Base class for validators that manages the listeners so the implementation
 * need not care about that. Subclasses need only call <code>notifyListeners(event)</code>
 * after the validation. Also subclasses must care about updating <code>lastResult</code>
 * after every validation.
 */
public abstract class _Validator implements IValidator {
    protected Set<IValidationListener> listeners = new HashSet<IValidationListener>();
    protected boolean lastResult = true;

    public void addValidationListener(IValidationListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeValidationListener(IValidationListener listener) {
        if (listener != null) listeners.remove(listener);
    }

    /** Performs only the actual validation. No notifications need to
     * be sent. This is all taken care of by the base object. 
     * @return the ValidationEvent, which describes the validation result. */
    public abstract ValidationEvent validateInternal();

    /** Calls <code>validateInternal()</code> and then cares about
     * updating <code>lastResult</code> and sending the notifications. */
    public boolean validate() {
        ValidationEvent ev = validateInternal();
        if (ev == null) return false; //Exit here, do no notify listeners
        ev.validationPassedBefore = this.lastResult;
        this.lastResult = ev.validationPassed;//Must be updated BEFORE notifying listeners
        notifyListeners(ev);
        return ev.validationPassed;
    }

    public boolean getPreviousResult() {
        return lastResult;
    }

    /** Performs the task of notifying all registered listeners.
     * Convenience method. (Subclasses can do that on their own if they like). 
     * @param ev the validation result*/
    protected void notifyListeners(ValidationEvent ev) {
        for (IValidationListener listener : listeners)
            listener.validationPerformed(ev);
    }

    public Set<IValidationListener> getValidationListeners() {
        return listeners;
    }
}
