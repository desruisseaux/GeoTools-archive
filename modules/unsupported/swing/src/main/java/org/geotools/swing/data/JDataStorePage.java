/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.data;

import java.awt.Font;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.swing.wizard.JPage;
import org.geotools.swing.wizard.ParamField;

/**
 * A wizard page that will prompt the user for a file of the supplied format ask for any additional
 * information.
 */
public class JDataStorePage extends JPage {

    protected DataStoreFactorySpi format;

    private Map<Param, ParamField> fields = new HashMap<Param, ParamField>();

    protected Map<String, Serializable> connectionParameters;

    private boolean required = true;

    public JDataStorePage(DataStoreFactorySpi format) {
        this(format, null);
    }

    public JDataStorePage(DataStoreFactorySpi format, Map<String, Serializable> params) {
        this.format = format;
        if (params == null) {
            params = new HashMap<String, Serializable>();
            for (Param param : format.getParametersInfo()) {
                params.put(param.key, (Serializable) param.sample);
            }
        }
        this.connectionParameters = params;
    }

    public void setRequried(boolean showRequired) {
        this.required = showRequired;
    }

    @Override
    public JPanel createPanel() {
        final JPanel page = new JPanel(new MigLayout());
        JLabel title = new JLabel(format.getDisplayName());
        Font titleFont = new Font("Arial", Font.BOLD, 14);
        title.setFont(titleFont);
        page.add(title, "span");
        JLabel description = new JLabel(format.getDescription());
        page.add(description, "grow, span");

        for (Param param : format.getParametersInfo()) {
            if (param.required != required)
                continue;
            JLabel label = new JLabel(param.title.toString());
            page.add(label);

            ParamField field = ParamField.create(param);
            JComponent component = field.doLayout();
            page.add(component, "span, wrap");

            fields.put(param, field);

            if (param.description != null) {
                JLabel info = new JLabel("<html>" + param.description.toString());
                page.add(info, "skip, span, wrap");
            }
        }
        return page;
    }

    @Override
    public void preDisplayPanel() {
        // populate panel from params map
        for (Entry<Param, ParamField> entry : fields.entrySet()) {
            Param param = entry.getKey();
            ParamField field = entry.getValue();
            Object value;
            try {
                value = param.lookUp(connectionParameters);
            } catch (IOException e) {
                value = param.sample;
            }
            field.setValue(value);
        }
        for (Entry<Param, ParamField> entry : fields.entrySet()) {
            ParamField field = entry.getValue();
            field.addListener(getJWizard().getController());
        }
    }

    @Override
    public void preClosePanel() {
        for (Entry<Param, ParamField> entry : fields.entrySet()) {
            Param param = entry.getKey();
            ParamField field = entry.getValue();

            Object value = field.getValue();
            connectionParameters.put(param.key, (Serializable) value);
            field.setValue(value);
        }
        for (Entry<Param, ParamField> entry : fields.entrySet()) {
            ParamField field = entry.getValue();
            field.removeListener(getJWizard().getController());
        }
    }

    @Override
    public boolean isValid() {
        // populate panel
        for (Entry<Param, ParamField> entry : fields.entrySet()) {
            if (!entry.getValue().validate()) {
                return false; // not validate
            }
            if (entry.getKey().required && entry.getValue().getValue() == null) {

            }
        }
        return true;
    }
}