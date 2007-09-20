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

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Map;
import javax.swing.JTextField;
import org.geotools.data.DataStoreFactorySpi.Param;

/**
 *
 * @author jody garnett, johann sorel
 */
public class JParamField extends JTextField {

    private Param param;
    private Object value;

    public JParamField(){
        this(null, Collections.EMPTY_MAP);
    }
    
    public JParamField(Param param) {
        this(param, Collections.EMPTY_MAP);
    }

    public JParamField(Param param, Map map) {
        super(14);
        this.param = param;
        setValue(map.get(param.key));
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        });
        setToolTipText(param.description);
    }

    public void refresh() {
        try {
            JParamField.this.value = param.parse(getText());
            setToolTipText(param.description);
            setForeground(Color.BLACK);
        } catch (Throwable e) {
            setToolTipText(e.getLocalizedMessage());
            setForeground(Color.RED);
            JParamField.this.value = null;
        }
    }

    public void setValue(Object value) {
        if (value == null) {
            value = param.sample;
        }
        this.value = value;
        if (value == null) {
            setText("");
        } else {
            setText(param.text(value));
        }
    }

    public Object getValue() {
        return value;
    }

    public void setParam(Param param) {
        this.param = param;
    }
}