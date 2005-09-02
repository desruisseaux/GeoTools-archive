/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation;

/** A UI element implementing this interface can reviece notification from an
 * IValidation that it's user input or user selection are correct. It must register
 * at a IValidator to recieve such events.
 * 
 * @see IValidator
 * @author Matthias Basler
 */
public interface IValidationListener {
    /** Called after a validation has been performed to send the result
     * to the listener.
     * @param ev contains the result and, if applies, the error message */
    public void validationPerformed(ValidationEvent ev);
}
