/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.validation.impl;

import java.awt.Color;

import javax.swing.text.JTextComponent;

import org.geowidgets.framework.validation.*;

/** Signals (in)acceptable values by setting the background color of the
 * component to the specified colors. You may set one of the colors to null,
 * to use the UI element's standard color in this case. (F.e. if you want to
 * signal correct values only, set <code>badColor = null</code>.
 * Inportant: Set the "normal" background color of the component <b>before</b>
 * this objet is initialized, since later the value will be overwritten by
 * this object. */
public class BackgroundColorNotifier implements IValidationNotifier {
    JTextComponent textComp;
    Color badColor;
    Color goodColor;

    /** Constructs a new notifier that changes the object's background depending
     * on the validation result. 
     * @param textComp the text component whose color shall be changed.
     * @param badColor the color to show when the result is negative or <code>null</code>
     * if to use the object's current background color
     * @param goodColor the color to show when the result is positive or <code>null</code>
     * if to use the object's current background color
     */
    public BackgroundColorNotifier(JTextComponent textComp, Color badColor,
            Color goodColor) {
        this.textComp = textComp;
        this.badColor = (badColor != null) ? badColor : textComp
                .getBackground();
        this.goodColor = (goodColor != null) ? goodColor : textComp
                .getBackground();
    }

    public void validationPerformed(ValidationEvent ev) {
        //What is better: Simply set the color each time or test if the color really changes
        Color oldColor = this.textComp.getBackground();
        Color newColor = ev.validationPassed ? goodColor : badColor;
        if (!newColor.equals(oldColor)) this.textComp.setBackground(newColor);

    }
}
