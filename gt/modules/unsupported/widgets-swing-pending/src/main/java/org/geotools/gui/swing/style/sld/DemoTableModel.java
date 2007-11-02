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

package org.geotools.gui.swing.style.sld;

import java.io.InputStream;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

/**
 *
 * @author johann sorel
 */
public class DemoTableModel extends AbstractTableModel implements TableModel {

    Rule[] rules = null;

    /**
     * 
     * @param demofile sld file containing style exemples
     */
    public DemoTableModel(String demofile) {

        Configuration configuration = new SLDConfiguration();
        Parser parser = new Parser(configuration);

        InputStream input = DemoTableModel.class.getResourceAsStream(demofile);

        try {
            StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse( input );
            rules = SLD.styles(sld)[0].getFeatureTypeStyles()[0].getRules();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRowCount() {
        return rules.length;
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rules[rowIndex].getSymbolizers()[0];
        } else if (columnIndex == 1) {
            return TextBundle.getResource().getString( rules[rowIndex].getName());
        }
        return "n/a";
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
}