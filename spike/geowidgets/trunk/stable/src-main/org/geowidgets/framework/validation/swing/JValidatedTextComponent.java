/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation.swing;

import javax.swing.text.JTextComponent;

import org.geowidgets.framework.validation.*;

/** A wrapper around a text component that adds validation. Validation means
 * that the user's input or selection is monitored and a feedback can be given
 * if it is acceptable or invalid.
 * @param <T> some Swing text component
 */
public class JValidatedTextComponent<T extends JTextComponent> {
    protected T component;

    /** Constructs a text component wrapper that is prepared for validation.
     * Use the <code>setValidator()</code> and <code>setNotifier()</code>
     * methods to register the approporiate objects to care about validation. 
     * @param textComp the text component that gets validation
     */
    public JValidatedTextComponent(T textComp) {
        this.component = textComp;
    }

    /** Constructs a text component wrapper that is prepared for validation.
     * Use the <code>setValidator()</code> and <code>setNotifier()</code>
     * methods to register the approporiate objects to care about validation. 
     * @param textComp the text component that gets validation 
     * @param validator listens to the user's input and validates it
     * @param notifier listens to the validation and signals the result to the user
     */
    public JValidatedTextComponent(T textComp, IValidator validator,
            IValidationNotifier notifier) {
        this.component = textComp;
        this.setValidator(validator, false);
        this.setNotifier(notifier);
        validator.validate();
    }

    /** @return the text component to which validation is added. */
    public T getTextComponent() {
        return this.component;
    }

//Validation
    IValidator validator = null;

    /** @return the object that validate's the user's input. */
    public IValidator getValidator() {
        return this.validator;
    }

    /** Sets/changes the validator to determine what inputs are valid/invalid.
     * @param validator the object that validate's the user's input. 
     * @param keepNotifiers if true, all notifiers from the old validator
     * are registered at the new notifier. Note: Only notifiers, not other
     * listeners, are carried forward. */
    public void setValidator(IValidator validator, boolean keepNotifiers) {
        if (keepNotifiers) {
            for (IValidationListener l : this.validator
                    .getValidationListeners()) {
                if (l instanceof IValidationNotifier) {//Copy only notifiers, not all listeners
                    validator.addValidationListener(l);
                }
            }
        }
        this.validator = validator;
    }

    IValidationNotifier notifier = null;

    /** @return the object that signals the validation result to the user */
    public IValidationNotifier getNotifier() {
        return this.notifier;
    }

    /** Sets/changes the notifier to determine the way that validation results
     * are made known to the user.
     * @param notifier a new notifier, which signals the validation result to the user */
    public void setNotifier(IValidationNotifier notifier) {
        if (validator != null)
            validator.removeValidationListener(this.notifier);
        this.notifier = notifier;
        if (validator != null) validator.addValidationListener(this.notifier);
    }

}
