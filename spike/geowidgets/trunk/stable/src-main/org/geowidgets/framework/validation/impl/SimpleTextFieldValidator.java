/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation.impl;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.geowidgets.framework.Util;
import org.geowidgets.framework.validation.ValidationEvent;

/** Validates the input of a single text component (not only text field). It
 * can be used to check for <ul>
 * <li> non-null input</li>
 * <li> any number input (just set min and max to Double.MIN_VALUE and Double.MAX_VALUE)</li>
 * <li> a number range</li>
 * <li> a String from a list of predefined Strings. </li>
 * 
 *
 * @author Matthias Basler
 */
public class SimpleTextFieldValidator extends _Validator implements DocumentListener {
    protected static final int TYPE_NONNULL = 0;
    protected static final int TYPE_VALUE_RANGE = 1;
    protected static final int TYPE_VALUE_TEXTVALUES = 2;
    protected JTextComponent textComp;
    protected int type = 0;
    protected double minValue = Double.MIN_VALUE, maxValue = Double.MAX_VALUE;
    protected Set<String> values = new HashSet<String>();
    protected String msg = null;

    /** Creates a validator that checks that the text is not empty. 
     * @param errMsg the error message to use when the validation result is negative
     * @param textComp the text component to validate */
    public SimpleTextFieldValidator(String errMsg, JTextComponent textComp) {
        this.type = TYPE_NONNULL;
        this.msg = errMsg;
        this.textComp = textComp;
        this.textComp.getDocument().addDocumentListener(this);
    }

    /** Creates a validator that checks if the (numerical) value is in a certain range. <p>
     * Hint: If no min value or no max value is needed, set them to
     * Double.MIN_VALUE or Double.MAX_VALUE respectively.
     * @param errMsg the error message to use when the validation result is negative
     * @param textComp the text component to validate 
     * @param minValue the value must be >= this value in order to return true
     * @param maxValue the value must be <= this value in order to return true */
    public SimpleTextFieldValidator(String errMsg, JTextComponent textComp,
            double minValue, double maxValue) {
        this(errMsg, textComp);
        this.type = TYPE_VALUE_RANGE;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /** Creates a validator that checks if the text value is one of the given set.
     * @param errMsg the error message to use when the validation result is negative
     * @param textComp the text component to validate
     * @param values the input must be one of the Strings in this list in order to return true;
     */
    public SimpleTextFieldValidator(String errMsg, JTextComponent textComp,
            Set<String> values) {
        this(errMsg, textComp);
        this.type = TYPE_VALUE_RANGE;
        this.values = values;
    }

    public ValidationEvent validateInternal() {
        String text = textComp.getText();
        ValidationEvent ev = new ValidationEvent();
        if (this.type == TYPE_NONNULL) {
            ev.validationPassed = (!Util.isEmpty(text));
            if (!ev.validationPassed) ev.problemMessage = this.msg;
        } else if (this.type == TYPE_VALUE_RANGE) {
            try {
                double value = Double.parseDouble(text);
                ev.validationPassed = (value >= this.minValue && value <= this.maxValue);
            } catch (NumberFormatException e) {
                ev.validationPassed = false;
            }
            if (!ev.validationPassed) ev.problemMessage = this.msg;

        } else if (this.type == TYPE_VALUE_TEXTVALUES) {
            ev.validationPassed = false;
            for (String value : values) {
                if (value.equals(text)) {
                    ev.validationPassed = true;
                    break;
                }
            }
            if (!ev.validationPassed) ev.problemMessage = this.msg;
        }
        return ev;
    }

//DocumentListener
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        validate();
    }

}
