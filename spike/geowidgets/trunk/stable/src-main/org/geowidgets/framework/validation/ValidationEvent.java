/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation;

/** An event created if a validation occurs, usually if the user changes the
 * input or selection of some UI elements. The event contains information
 * about whether the validation was successful (that is: the user typed/chose
 * acceptable values) or not and a String that describes the problem in a
 * user-understandable way and can be used to signal to problem to the user.
 * A throwable can also be attached. */
public class ValidationEvent {
    /** <code>True</code> if the validation result is positive. */
    public boolean validationPassed;
    /** <code>True</code> if the last validation result had been positive. */
    public boolean validationPassedBefore;
    /** A message to signal the user what problems exist. */
    public String problemMessage;
    /** A throwable that might have arisen during validation or that caused
     * the trouble in the first place. Might be null. */
    public Throwable cause;

}
