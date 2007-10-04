/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.gui.swing.extended;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import org.geotools.filter.Filters;
import org.geotools.gui.swing.extended.event.NumberEventListener;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;



/** cette classe s'apparente a un JSpinner.
 * Elle est parfaitement adaptée pour gérer un interval de nombre entier.
 * @author Johann Sorel
 *
 */
public class JNumberPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private EventListenerList listeners = new EventListenerList();
    private ImageIcon ico_up = IconBundle.getResource().getIcon("16_smallarrow_up");
    private ImageIcon ico_down = IconBundle.getResource().getIcon("16_smallarrow_down");
    private double min = 0;
    private double max = 65000;
    private double val = min;
    private JTextField jtf_valeur = new JTextField(5);
    private JButton jbu_up = new JButton(ico_up);
    private JButton jbu_down = new JButton(ico_down);
    private boolean floatable = false;

    public JNumberPanel() {
        this(0, 65000);
    }

    /**
     * @param min : valeur minimum possible
     * @param max : valeur maximum possible
     */
    public JNumberPanel(int min, int max) {
        super(new FlowLayout(1, 1, 1));
        this.max = max;
        this.min = min;
        val = min;

        jtf_valeur.setHorizontalAlignment(0);
        jtf_valeur.setEditable(true);
        jtf_valeur.setText(String.valueOf(val));
        jtf_valeur.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    Float f = Float.parseFloat(jtf_valeur.getText());
                    if (floatable) {
                        setValue(f);
                    } else {
                        setValue(f.intValue());
                    }
                } catch (Exception ex) {
                    setValue(val);
                }
            }
        });

        jtf_valeur.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                try {
                    Float f = Float.parseFloat(jtf_valeur.getText());
                    if (floatable) {
                        setValue(f);
                    } else {
                        setValue(f.intValue());
                    }
                } catch (Exception ex) {
                    setValue(val);
                }
            }
        });

        JPanel pan = new JPanel(new GridLayout(2, 1, 0, 0));
        pan.add(jbu_up);
        pan.add(jbu_down);

        jbu_up.setPreferredSize(new Dimension(ico_up.getIconWidth() + 2, jtf_valeur.getPreferredSize().height / 2));
        jbu_down.setPreferredSize(new Dimension(ico_down.getIconWidth() + 2, jtf_valeur.getPreferredSize().height / 2));
        jbu_up.setBorderPainted(false);
        jbu_down.setBorderPainted(false);
        jbu_up.setBackground(jtf_valeur.getBackground());
        jbu_down.setBackground(jtf_valeur.getBackground());
        jbu_down.addActionListener(this);
        jbu_up.addActionListener(this);


        add(jtf_valeur);
        add(pan);
    }

    public void setMargins(float min, float max) {
        this.max = max;
        this.min = min;
        setValue(val);
    }

    public void setFloatable(boolean b) {
        floatable = b;
    }

    /**
     * @param v : nouvelle valeur
     */
    public void setValue(double v) {
        if (v > max) {
            val = max;
        } else if (v < min) {
            val = min;
        } else {
            val = v;
        }

        if (floatable) {
            jtf_valeur.setText(String.valueOf(val));
        } else {
            jtf_valeur.setText(String.valueOf(new Float(val).intValue()));
        }

        fireNumberChanged();
    }
    
    /**
     * @param v : nouvelle valeur
     */
    public void setValue(Expression exp) {
        if( exp!= null ){
            double d = Filters.asDouble(exp);
            
            if(d != Double.NaN)
                setValue(d);
            else
                setValue(1);
        }
        else{
            setValue(1);
        }        
        fireNumberChanged();
    }

    /**
     * @return retourne un entier.
     */
    public int getIntValue() {
        return new Float(val).intValue();
    }

    public float getFloatValue() {
        return new Double(val).floatValue();
    }
    
    public Expression getExpressionValue() {
        StyleBuilder sb = new StyleBuilder();        
        return sb.literalExpression(val);
    }

    @Override
    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);

        jtf_valeur.setPreferredSize(new Dimension(jtf_valeur.getPreferredSize().width, getPreferredSize().height - 2));

        jbu_up.setPreferredSize(new Dimension(ico_up.getIconWidth() + 2, jtf_valeur.getPreferredSize().height / 2));
        jbu_down.setPreferredSize(new Dimension(ico_down.getIconWidth() + 2, jtf_valeur.getPreferredSize().height / 2));
    }
    ////////////////////////////////////////////////

    /** S ajouter a l ecoute .
     * @param tinyev
     */
    public void addNumberEventListener(NumberEventListener tinyev) {
        listeners.add(NumberEventListener.class, tinyev);
    }

    /** retirer un element TinyEventListener.
     * @param tinyev
     */
    public void removeNNumberEventListener(NumberEventListener tinyev) {
        listeners.remove(NumberEventListener.class, tinyev);
    }

    /** retourne toutes les TinyEventListener.
     * @return TinyEventListener[]
     */
    public NumberEventListener[] getNumberEventListener() {
        return listeners.getListeners(NumberEventListener.class);
    }

    private void fireNumberChanged() {

        for (NumberEventListener listener : getNumberEventListener()) {
            listener.NumberChanged(val);
        }
    }
    /////////////////////////////////////////

    public void actionPerformed(ActionEvent a) {

        if (a.getSource().equals(jbu_up)) {
            val++;
        } else {
            val--;
        }
        setValue(val);
    }
}