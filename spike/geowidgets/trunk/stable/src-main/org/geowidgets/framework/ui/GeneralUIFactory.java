/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.ui;

/** This class is responsible for delivering information about ui issues
 * that are independent of UI implementation (e.g. Swing, SWT, JFace, ...).
 * 
 * @author Matthias Basler
 */
public class GeneralUIFactory {
    protected static GeneralUIFactory me = new GeneralUIFactory();

    protected GeneralUIFactory() {
    }

    /** @return the default look and feel for the CRS widgets. */
    public static GeneralUIFactory getDefault() {
        return me;
    }

//******************************************************************************      
//InformationLabel text formatting
    /** @return the String before the first content in a multiline enumeration.
     * Such a multiline description is f.e. <pre>
     * - Code: 1234
     * - Name: MyObject </pre>
     * In the above case the prefix would be "- ", the NewLine string would be
     * "\n- " and the suffix (after the last content) would be empty. */
    public String getMultiLinePrefix() {
        return "- ";}//$NON-NLS-1$

    /** @return the String to insert between to lines in a multiline enumeration.
     * @see #getMultiLinePrefix() */
    public String getMultiLineNewLine() {
        return "\n- ";}//$NON-NLS-1$

    /** @return the String to append after the last line in a multiline enumeration.
     * @see #getMultiLinePrefix() */
    public String getMultiLineSuffix() {
        return "";}//$NON-NLS-1$
    
//HTML formatting
    /** @return the String to insert at the start of a HTML label text.
     * This implementation opens the html and page tags. */
    public String getHTMLPrefix() {
        return "<html><page>";}//$NON-NLS-1$

    /** @return the line break for HTML labels. */
    public String getHTMLNewLine() {
        return " </p>";}//$NON-NLS-1$

    /** @return the String to append at the end of a HTML label text. 
     * This implementation closes the page and html tags. */
    public String getHTMLSuffix() {
        return "</page></html>";}//$NON-NLS-1$    
}
