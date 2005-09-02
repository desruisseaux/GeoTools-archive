/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geowidgets.framework.Res;

/** A cell editor that works directly on objects of any type. <p/>
 * (Some code taken from {@linkplain org.eclipse.jface.viewers.ComboBoxCellEditor},
 * which unfortunately wasn't suitable with its index-based architecture.
 * The Eclipse copyright is printed above the functions in question.)
 * @param <T> The cell editor will work on a list of objects of this type.
 * It will revieve objects of type T and will return T objects ... or Strings
 * in case the user entered a String not to be found in the items list.
 * */
public class GeneralComboCellEditor<T extends Object> extends CellEditor {
    private static final int STYLE_DEFAULT = SWT.NONE;
    /** The combo control that is used internally. */
    protected CCombo comboBox;

    //ComboBoxCellEditor already has an attribute "items", so use "objects"
    /** The list of objects stored whose labels are use in the dropdown list. */
    protected List<T> objects;
    /** This object determines how to read out the label String from the objects. */
    protected ILabelProvider labelProvider;

    /** Creates a new cell editor that shows the specified items in its list.
     * The default LabelProvider (which uses the <code>toString()</code> method)
     * is used to find the label of each item.
     * @param parent the parent control
     * @param items the objects to select from
     * @param editable if <code>true</code>, the user can type any value into
     * the combo. In case of such a custom value a <code>String</code> is returned.
     */
    public GeneralComboCellEditor(Composite parent, final List<T> items, boolean editable) {
        this(parent, items, editable, new GeneralComboLabelProvider());
    }

    /** Creates a new cell editor that shows the specified items in its list.
     * A custom LabelProvider can be set. 
     * @param parent the parent control
     * @param items the objects to select from
     * @param editable if <code>true</code>, the user can type any value into
     * the combo. In case of such a custom value a <code>String</code> is returned.
     * @param labelProvider determines how to read out the label String from the objects.
     */
    public GeneralComboCellEditor(Composite parent, final List<T> items,
            boolean editable, ILabelProvider labelProvider) {
        this(parent, items, labelProvider, (editable) ? STYLE_DEFAULT : STYLE_DEFAULT
                | SWT.READ_ONLY);
    }

    /** Creates a new cell editor that shows the specified items in its list.
     * A custom LabelProvider and style can be set. 
     * @param parent the parent control
     * @param items the objects to select from
     * @param labelProvider determines how to read out the label String from the objects.
     * @param style the style bits, such as <code>SWT.READ_ONLY</code>
     */
    public GeneralComboCellEditor(Composite parent, final List<T> items,
            ILabelProvider labelProvider, int style) {
        super(parent, style);
        this.objects = items;
        this.labelProvider = labelProvider;

        if (this.comboBox != null) {
            comboBox.setItems(getStrings(items, labelProvider));
        }
    }

    /*public void setItems(T[] items) {
     super.setItems(getStrings(items, this.labelProvider));
     }*/

    /** We need an array of strings. Currently we assume the items to be sorted. 
     * @param items the items whose labels are to be determined
     * @param labelProvider the label provider that knows how to get the label
     * of this sort of objects
     * @return the items's labels in an array of Strings */
    protected String[] getStrings(List<T> items, ILabelProvider labelProvider) {
        if (items == null)
            throw new NullPointerException(Res.get("err.NullPointerItems")); //$NON-NLS-1$ //"Items may not be null"
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < items.size(); ++i) {
            String label = labelProvider.getText(items.get(i));
            list.add(i, label);
        }
        //Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    /** Returns the object at the selected index. */
    protected Object doGetValue() {
        if (comboBox == null) return null;
        int i = comboBox.getSelectionIndex();
        if (i > -1) return objects.get(i);
        else return comboBox.getText();
    }

    protected void doSetValue(Object value) {
        if (value == null || comboBox == null) return;
        String valueLabel = labelProvider.getText(value);

        for (int i = 0; i < this.objects.size(); ++i) {
            if (labelProvider.getText(objects.get(i)).equals(valueLabel)) {
                comboBox.select(i);
            }
        }
        comboBox.setText(valueLabel);
    }

//  Focus and such--------------------------------------------------------------
    protected void doSetFocus() {
        comboBox.setFocus();
    }

    protected void focusLost() {
        if (isActivated()) {
            //fireApplyEditorValue();
            deactivate();
        }
    }

    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            //fireApplyEditorValue();
            deactivate();
        }
    }

//  Create control--------------------------------------------------------------
/* The functions below are partly modified versions of code taken from
 * org.eclipse.jface.viewers ComboBoxCellEditor. It's copyright is printed below:
 *******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
    
    protected Control createControl(Composite parent) {
        comboBox = new CCombo(parent, getStyle());
        comboBox.setVisibleItemCount(10);
        comboBox.setFont(parent.getFont());
        
        comboBox.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201  
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
        });

        comboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                fireApplyEditorValue();
                deactivate();
                //selection = comboBox.getSelectionIndex();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                //If the user entered a value, check if it is one of the values
                //in the list. If so, select this value.
                if (comboBox.getSelectionIndex() == -1) {
                    String[] items = comboBox.getItems();
                    for (int i = 0; i < items.length; ++i) {
                        if (items[i].equals(comboBox.getText())) {
                            comboBox.select(i);
                            break;
                        }
                    }
                }
                fireApplyEditorValue();
                deactivate();
            }

        });

        comboBox.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                GeneralComboCellEditor.this.focusLost();
            }
        });
        return comboBox;
    }

    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        if ((comboBox == null) || comboBox.isDisposed()) layoutData.minimumWidth = 60;
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(comboBox);
            layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }
}
