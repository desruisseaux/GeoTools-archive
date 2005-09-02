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

/** Combines several single validations into a combined validation where either
 * all or one of them (AND or OR) must be true for this validation to return true. */
public class CombinedValidator extends _Validator implements IValidationListener {
    /** All input validators must return true in order for this validator
     * to return a positive result. */
    public static int COMBINE_AND = 0;
    /** At least one of the input validators must return true in order for this
     * validator to return a positive result. */
    public static int COMBINE_OR = 1;

    protected String errMsg = null;
    protected int bool;
    Set<IValidator> validators;

    /** Constructs a new validator that determines its validation result from
     * the combination of other validation results. 
     * @param errMsg the error message to use when the validation result is negative
     * @param bool one of the COMBINE_xxx constants that describe how to combine
     * the results of the validators.
     * @param validators the input validators, from which this validator's result depends
     */
    public CombinedValidator(String errMsg, int bool, Set<IValidator> validators) {
        this.errMsg = errMsg;
        this.bool = bool;
        this.validators = validators;
        for (IValidator val : validators) {
            val.validate(); //to initialize the "lastResult"
            val.addValidationListener(this);
        }
    }

    /** Constructs a new validator that determines its validation result from
     * the combination of other validation results. 
     * @param errMsg the error message to use when the validation result is negative
     * @param bool one of the COMBINE_xxx constants that describe how to combine
     * the results of the validators.
     * @param val1 an input validators, from which this validator's result depends
     * @param val2 another input validators, from which this validator's result depends
     */
    public CombinedValidator(String errMsg, int bool, IValidator val1,
            IValidator val2) {
        this(errMsg, bool, createSet(val1, val2));
    }

    private static Set<IValidator> createSet(IValidator val1, IValidator val2) {
        Set<IValidator> set = new HashSet<IValidator>();
        set.add(val1);
        set.add(val2);
        return set;
    }

    public ValidationEvent validateInternal() {
        ValidationEvent ev = new ValidationEvent();
        if (bool == COMBINE_AND) {//All must be true
            ev.validationPassed = true;
            for (IValidator val : validators) {
                if (!val.getPreviousResult()) {
                    ev.validationPassed = false;
                    ev.problemMessage = this.errMsg;
                    break;
                }
            }
        } else if (bool == COMBINE_OR) {//Only one must be true
            ev.validationPassed = false;
            ev.problemMessage = this.errMsg;
            for (IValidator val : validators) {
                if (val.getPreviousResult()) {
                    ev.validationPassed = true;
                    ev.problemMessage = null; //No problem ...
                    break;
                }
            }
        } else return null;
        return ev;
    }

    public void validationPerformed(ValidationEvent ev) {
        //Some validator's result changed. Update our's as the consequence.
        validate();
    }

}
