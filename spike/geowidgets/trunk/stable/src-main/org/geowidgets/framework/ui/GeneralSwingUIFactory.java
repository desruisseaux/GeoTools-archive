/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.Util;

/** This class is responsible for delivering general visible elements and their
 * attributes, such as layout constants and colors. This ensures that all widgets
 * get a consistent look and feel but allows to customize this look and feel.
 * 
 * @author Matthias Basler
 */
public class GeneralSwingUIFactory {
    /** The default <code>GeneralSwingUIFactory</code>. */
    protected static GeneralSwingUIFactory me = new GeneralSwingUIFactory();

    /** Creates the instance of this singleton class. */
    protected GeneralSwingUIFactory() {
    }

    /** @return the default look and feel for the CRS widgets. */
    public static GeneralSwingUIFactory getDefault() {
        return me;
    }
//******************************************************************************    

//Layout and grid bag constants
    /** Stores the default layout. */
    protected GridBagLayout defaultLayout = new GridBagLayout();

    /** @return the default LayoutManager, which is GridBagLayout. It is highly
     * recommended not to overwrite this, since there are lots of dependencies. */
    public LayoutManager2 getDefaultLayout() {
        return defaultLayout;
    }

    /** Stores the default insets. */
    protected Insets defaultInsets = new Insets(2, 2, 2, 2);

    /** @return the default insets for Swing UI elements in GridBagLayout.*/
    public Insets getInsets() {
        return defaultInsets;
    }

    /** @return the default inset width for Swing UI elements in GridBagLayout.*/
    public int getInsetWidth() {
        return 2;
    }

    protected ComfortGBC GBC_DEFAULT = new ComfortGBC(0, 0).fillX().anchorNW()
            .setInsets(getInsets());
    protected ComfortGBC GBC_LABEL = new ComfortGBC(0, 0).fillX().anchorW()
            .setInsets(getInsets());
    protected ComfortGBC GBC_COMBO = new ComfortGBC(0, 0).fillX().anchorW()
            .setInsets(getInsets()).setWeight(1, 0);
    protected ComfortGBC GBC_PANEL = new ComfortGBC(0, 0).fillX().anchorNW()
            .setInsets(getInsets()).setWeight(1, 1).setSize(2, 1);

    /** @return grid bag constraints for no particular use.
     * Simply configure them as preferred. */
    public ComfortGBC getDefaultGBC() {
        return (ComfortGBC) GBC_DEFAULT.clone();
    }
    
    /** @return grid bag constraints preconfigured for labels such as in typical
     * label-textfield or label-dropdown combinations. */
    public ComfortGBC getLabelGBC() {
        return (ComfortGBC) GBC_LABEL.clone();
    }

    /** @return grid bag constraints preconfigured for text fields and dropdows
     * such as in typical label-textfield or label-dropdown combinations. */
    public ComfortGBC getComboGBC() {
        return (ComfortGBC) GBC_COMBO.clone();
    }

    /** @return grid bag constraints preconfigured for panels. The user will
     * probably need to adjust width, height - and the position, of course. */
    public ComfortGBC getPanelGBC() {
        return (ComfortGBC) GBC_PANEL.clone();
    }

//Colors
    protected final Color backColor = new JPanel().getBackground();
    protected final Color textBackColor = new JTextField().getBackground();
    protected final Color foreColor = new JPanel().getForeground();
    protected final Color labelColor = new Color(0, 0, 0);//Black
    protected final Color textColor = new Color(0, 0, 0);//Black
    protected final Color descColor = new Color(0, 0, 128);//Blue

    
    /** @return the default background color, e.g. for panels. */
    public final Color getBackgroundColor() {
        return backColor;
    }
    /** @return the background color for text components, trees and such. */
    public final Color getTextBackgroundColor() {
        return textBackColor;
    }
    /** @return the color foreground color, e.g. for panels. In case of
     * labels <code>getLabelColor()</code> will be used and in case of
     * text components <code>getTextColor()</code> will be used instead. */
    public final Color getForegroundColor() {
        return foreColor;
    }
    
    /** @return the color to use for labels. */
    public final Color getLabelColor() {
        return descColor;
    }

    /** @return the color to use for text fields, dropdowns and such. */
    public final Color getTextColor() {
        return textColor;
    }

    /** @return the color to use for descriptions. */
    public final Color getDescriptionColor() {
        return descColor;
    }
    
//Borders
    protected final Border panelBorder = new EtchedBorder();
    protected final Border textBorder = new JTextField().getBorder();
    /*
    static{
        textBorder = new BevelBorder(BevelBorder.LOWERED,);
    }*/
    
    /** @return the default border to use when grouping UI elements on panels
     * or in similar contexts. */
    public final Border getDefaultPanelBorder(){
        return panelBorder;
    }
    /** @return the default border to use for user input elements, such as
     * trees or button bars. It is supposed to mimic the border of text fields or
     * other text components. (F.e. in Windows it is a bevel border, so the
     * component's interior is a bit set back.) */
    public final Border getDefaultTextComponentBorder(){
        return textBorder;
    }
    

//Sizes
    protected int textFieldHeight = (new JTextField()).getPreferredSize().height;

    /** @return the minimum recommended size for text fields, dropdowns and the like. */
    public Dimension getMinTextFieldSize() {
        return new Dimension(60, 18);
    }
    //public Dimension getPrefTextFieldSize(){return new Dimension(160, 180);}

//Messages
    /** No symbol. */
    public static final int MESSAGE_PLAIN = JOptionPane.PLAIN_MESSAGE;
    /** Question mark symbol. */
    public static final int MESSAGE_QUESTION = JOptionPane.QUESTION_MESSAGE;
    /** Used for information messages. */
    public static final int MESSAGE_INFO = JOptionPane.INFORMATION_MESSAGE;
    /** Used for warning messages. */
    public static final int MESSAGE_WARNING = JOptionPane.WARNING_MESSAGE;
    /** Used for exceptions and errors. */
    public static final int MESSAGE_ERROR = JOptionPane.ERROR_MESSAGE;

    /** Dialog will have "Yes", "No" and "Cancel" buttons. */
    public static final int OPTION_YES_NO_CANCEL = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Dialog will have "Yes" and "Cancel" buttons. */
    public static final int OPTION_YES_NO = JOptionPane.YES_NO_OPTION;
    /** Dialog will have "OK" and "Cancel" buttons. */
    public static final int OPTION_OK_CANCEL = JOptionPane.OK_CANCEL_OPTION;

    /** User selected "Cancel" or closed the dialog. */
    public static final int RESULT_CANCEL = JOptionPane.CANCEL_OPTION;
    /** User selected "No". */
    public static final int RESULT_NO = JOptionPane.NO_OPTION;
    /** User selected "Yes". */
    public static final int RESULT_YES = JOptionPane.YES_OPTION;
    /** User selected "OK". */
    public static final int RESULT_OK = JOptionPane.OK_OPTION;

    protected final String CAUSE = Res.get(Res.WIDGETS, "err.Cause"); //$NON-NLS-1$

    /** Shows a dialog with an OK button to the user.
     * @param parent the parent component
     * @param msg the message to show to the user
     * @param title the dialog title
     * @param msgTypeConst one of the MESSAGE_xxx constants, which determine the symbol.
     */
    public void showMessage(Component parent, String msg, String title,
            int msgTypeConst) {
        JOptionPane.showMessageDialog(parent, msg, title, msgTypeConst);
    }

    /** Shows an information dialog with an OK button to the user.
     * @param parent the parent component
     * @param msg the message to show to the user
     * @param title the dialog title
     */
    public void showInfo(Component parent, String msg, String title) {
        JOptionPane.showMessageDialog(parent, msg, title, MESSAGE_INFO);
    }

    /** Shows an warning dialog with an OK button to the user.
     * @param parent the parent component
     * @param msg the message to show to the user
     * @param title the dialog title
     */
    public void showWarning(Component parent, String msg, String title) {
        JOptionPane.showMessageDialog(parent, msg, title, MESSAGE_WARNING);
    }

    /** Prints an error dialog with following lines <ul>
     * <li> - Localized error/exception message.</li>
     * <li> - Cause, if non-null</li>
     * <li> - additional message</li>
     * </ul> 
     * @param parent the parent component
     * @param msg a message to show to the user
     * @param title the dialog title
     * @param e the throwable that caused all the trouble. */
    public void showError(Component parent, String msg, String title,
            Throwable e) {
        String message = e.getLocalizedMessage() + "\n"; //$NON-NLS-1$
        if (e.getCause() != null)
            message += CAUSE + e.getCause().getLocalizedMessage() + "\n"; //$NON-NLS-1$
        message += msg;
        JOptionPane.showMessageDialog(parent, msg, title, MESSAGE_WARNING);
    }

    /** Shows nearly any sort of dialog. Different symbols and buttons are possible.
     * @param parent the parent component
     * @param msg a message to show to the user
     * @param title the dialog title
     * @param msgTypeConst one of the MESSAGE_xxx constants, which determine the symbol.
     * @param optionTypeConst determines which buttons appear,
     * use one of the OPTION_xxx constants.
     * @return the user's choice as one od the RESULT_xxx constants.
     */
    public int showConfirmDialog(Component parent, String msg, String title,
            int msgTypeConst, int optionTypeConst) {
        int result = JOptionPane.showConfirmDialog(parent, msg, title,
                optionTypeConst, msgTypeConst);
        if (result == JOptionPane.CLOSED_OPTION
                || result == JOptionPane.NO_OPTION)
            result = JOptionPane.CANCEL_OPTION;
        return result;
    }

//Standard UI elements
    /** Applies a consistent look and feel to the label. Override to change
     * certain aspects (such as standard font or color) of all labels.
     * @return the customized JLabel
     * @param label the label to get customized. Its text must be preserved.
     * @deprecated Use <code>customize(C)</code> instead. */
    public JLabel customizeJLabel(JLabel label) {
        label.setForeground(this.labelColor);
        label.setOpaque(false); //Maybe not necessary
        label.setMinimumSize(getMinTextFieldSize());
        return label;

    }

    /** @return a new JLabel to which the consistent look and feel has been applied.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param textResource the key for the localized String.*/
    public JLabel createJLabel(ResourceBundle rb, String textResource) {
        return customize(new JLabel(Res.get(rb, textResource)));
    }

    /** Applies a consistent look and feel to the text field. Override to change
     * certain aspects (such as standard font or color) of all text fields . 
     * @return the customized JTextField
     * @param textField the text field to get customized. Its text must be preserved.
     * @deprecated Use <code>customize(C)</code> instead. */
    public JTextField customizeJTextField(JTextField textField) {
        textField.setForeground(this.textColor);
        textField.setMinimumSize(getMinTextFieldSize());
        return textField;
    }

    /** @return a new JTextField to which the consistent look and feel has been applied.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param textResource the key for the localized String.*/
    public JTextField createJTextField(ResourceBundle rb, String textResource) {
        return customize(new JTextField(Res.get(rb, textResource)));
    }

    /** Applies a consistent look and fell to the dropdown. Override to change
     * certain aspects (such as standard font or color) of all dropdown lists.
     * Be sure to call this <b>before</b> further changing attributes or
     * your changes might get overwritten. <p/>
     * This implementation sets the number of visible rows to 10 and cares
     * about automatic tooltip updating.
     * @return the customized dropdown list.
     * @param combo the dropdown to get customized. Its content must be preserved.*/
    protected JComboBox customizeJCombo(JComboBox combo) {
        combo.setForeground(this.textColor);
        combo.setMinimumSize(getMinTextFieldSize());
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width,
                textFieldHeight));
        combo.setBorder(getDefaultTextComponentBorder());
        combo.setMaximumRowCount(10);
        if (combo.getSelectedItem() != null)
            combo.setToolTipText(combo.getSelectedItem().toString());
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox source = (JComboBox) e.getSource();
                if (source.getSelectedItem() != null) {
                    source.setToolTipText(source.getSelectedItem().toString());
                }
            }
        });
        return combo;
    }
    
    /** Applies a consistent look and fell to the list. Override to change
     * certain aspects (such as standard font or color) of all lists.
     * Be sure to call this <b>before</b> further changing attributes or
     * your changes might get overwritten. <p/>
     * This implementation cares about automatic tooltip updating.
     * @return the customized dropdown list.
     * @param list the list to get customized. Its content must be preserved.*/
    protected JList customizeJList(JList list) {
        list.setForeground(this.textColor);
        list.setMinimumSize(getMinTextFieldSize());
        list.setPreferredSize(new Dimension(list.getPreferredSize().width,
                textFieldHeight));
        list.setBorder(getDefaultTextComponentBorder());
        list.setLayoutOrientation(JList.VERTICAL);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if (list.getSelectedValue() != null)
            list.setToolTipText(list.getSelectedValue().toString());
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                JList source = (JList) e.getSource();
                if (source.getSelectedValue() != null) {
                    source.setToolTipText(source.getSelectedValue().toString());
                }
            }
        });
        return list;
    }    

    /** @return a JTextArea that is formatted to show multi-line descriptions.
     * The font is set according to the font used for normal labels. The text
     * area is set non-editable. */
    public JTextArea createInformationJTextArea() {
        JTextArea textArea = new JTextArea();
        customize(textArea);
        /* Should be done by customize
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        */
        textArea.setEditable(false);
        textArea.setForeground(this.descColor);
        textArea.setFont((new JLabel()).getFont());
        textArea.setBorder(new EmptyBorder(getInsets()));
        return textArea;
    }

    /** Applies a consistent look and feel to the panel. Override to change
     * certain aspects (such as standard font or color) of all panel. 
     * @return the customized JPanel
     * @param panel the panel to get customized.
     * @deprecated Use <code>customize(C)</code> instead. */
    public JPanel customizeJPanel(JPanel panel) {
        panel.setLayout(this.defaultLayout);
        return panel;
    }
    
    /** @return a JPanel with the default layout manager (GridBagLayout).*/
    public JPanel createJPanel() {
        return customize(new JPanel());
    }

    /** @return a titled JPanel with the default layout manager (GridBagLayout).
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param titleResource the key for the localized String.*/
    public JPanel createJPanel(ResourceBundle rb, String titleResource) {
        JPanel panel = createJPanel();
        if (Util.ensureNotEmpty(titleResource))
            panel.setBorder(new TitledBorder(new EtchedBorder(), Res.get(rb,
                    titleResource)));
        return customize(panel);
    }
    
    
    /** Applies a consistent look to all components. This allows to write
     * generic widgets, which still look slightly different in every application.
     * @param <SomeJComponent> any JComponent. Output class = input class.
     * @param c the component to be customized
     * @return the input component, customized to the application's default
     * colors, sizes and "look" in general.
     */
    public <SomeJComponent extends JComponent> SomeJComponent customize(SomeJComponent c){
        Class cl = c.getClass();
        //Fore and back color get overwritten later, where appropriate.
        c.setBackground(this.getBackgroundColor());
        c.setForeground(this.getForegroundColor());
        
        if (JTextComponent.class.isAssignableFrom(cl)) {
            JTextComponent c2 = (JTextComponent)c;
            c2.setBackground(getTextBackgroundColor());
            c2.setForeground(this.getTextColor());
            if (JTextField.class.isAssignableFrom(cl)) {
                JTextField textField = (JTextField)c;
                textField.setMinimumSize(getMinTextFieldSize());
            } else if (JTextArea.class.isAssignableFrom(cl)) {
                JTextArea textArea = (JTextArea)c;
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
            }
        } else if (JTree.class.isAssignableFrom(cl) ||
                JComboBox.class.isAssignableFrom(cl) ||
                JList.class.isAssignableFrom(cl)){
            c.setBackground(getTextBackgroundColor());
            c.setBorder(getDefaultTextComponentBorder());
            if (JComboBox.class.isAssignableFrom(cl))
                this.customizeJCombo((JComboBox)c);
            else if (JList.class.isAssignableFrom(cl))
                this.customizeJList((JList)c);
            
        } else if (JLabel.class.isAssignableFrom(cl)) {
            JLabel c2 = (JLabel)c;
            c2.setForeground(this.getTextColor());
            c2.setOpaque(false);
        } else if (JPanel.class.isAssignableFrom(cl)) {
            JPanel c2 = (JPanel)c;
            c2.setLayout(getDefaultLayout());
        } else if (JTabbedPane.class.isAssignableFrom(cl)){
            JTabbedPane c2 = (JTabbedPane)c;
            c2.setTabPlacement(JTabbedPane.TOP);
        }
        return c;
    }
    
//Deprecated
    
}
