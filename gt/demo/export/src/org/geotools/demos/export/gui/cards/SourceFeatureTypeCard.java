/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.demos.export.gui.cards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class SourceFeatureTypeCard extends WizzardCard {
    /** DOCUMENT ME! */
    private static final String HELP_MSG = "Select a feature type to export";

    /** DOCUMENT ME! */
    private DataStore dataStore;

    /** DOCUMENT ME! */
    private JList typeNamesList = new JList(new DefaultListModel());

    /** DOCUMENT ME!  */
    private JTable propsTable = new JTable(new FeatureTypeTableModel());

    /**
     * Creates a new SourceFeatureTypeCard object.
     *
     * @param listener DOCUMENT ME!
     * @param prev DOCUMENT ME!
     */
    public SourceFeatureTypeCard(CardListener listener, WizzardCard prev) {
        super(listener, prev);

        JPanel typesPanel = new JPanel(new BorderLayout(2, 2));
        JScrollPane scroll = new JScrollPane(typeNamesList);
        scroll.setPreferredSize(new Dimension(240, 100));
        typesPanel.add(scroll, BorderLayout.WEST);
        typesPanel.add(new JLabel("Select a feature type: "), BorderLayout.NORTH);

        JPanel propsPanel = new JPanel(new BorderLayout(2, 2));
        propsPanel.add(new JLabel("FeatureType attributes:"), BorderLayout.NORTH);
        scroll = new JScrollPane(propsTable);
        scroll.setPreferredSize(new Dimension(240, 100));
        propsPanel.add(scroll, BorderLayout.CENTER);

        getGui().add(typesPanel, BorderLayout.WEST);
        getGui().add(propsPanel, BorderLayout.CENTER);

        typeNamesList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    String typeName = (String) typeNamesList.getSelectedValue();
                    FeatureType schema = null;

                    getListener().setNextEnabled(typeName != null);

                    if (typeName != null) {
                        try {
                            schema = dataStore.getSchema(typeName);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            showError("ERROR obtaining schema", e1.getMessage());
                        }
                    }

                    FeatureTypeTableModel schemaTableModel = (FeatureTypeTableModel) propsTable
                        .getModel();
                    schemaTableModel.setFeatureType(schema);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getHelpMessage() {
        return HELP_MSG;
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
        if (!(paramObject instanceof DataStore)) {
            throw new IllegalArgumentException("Expected DataStore: "
                + paramObject);
        }

        this.dataStore = (DataStore) paramObject;
    }

    /**
     * DOCUMENT ME!
     */
    public void show() {
        super.show();
        getListener().setPreviousEnabled(true);
        getListener().setNextEnabled(getSchema() != null);
        fillTypeNames();
    }

    /**
     * DOCUMENT ME!
     */
    private void fillTypeNames() {
        String[] typeNames;

        try {
            typeNames = dataStore.getTypeNames();
        } catch (IOException e) {
            showError("Error getting typenames", e.getMessage());

            return;
        }

        DefaultListModel list = (DefaultListModel) typeNamesList.getModel();
        list.clear();
        Arrays.sort(typeNames);

        for (int i = 0; i < typeNames.length; i++) {
            list.addElement(typeNames[i]);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public boolean nextPressed() {
        FeatureType schema = getSchema();
        FeatureSource fsource = null;

        try {
            fsource = dataStore.getFeatureSource(schema.getTypeName());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error obtaining feature source for "
                + schema.getTypeName(), e.getMessage());
            return false;
        }

        WizzardCard next = getNextCard();
        next.setParameter(fsource);
        return true;
    }

    /**
	 * @return
	 */
	private FeatureType getSchema() {
		FeatureTypeTableModel model = (FeatureTypeTableModel) propsTable
            .getModel();
        FeatureType schema = model.getFeatureType();
		return schema;
	}

	/**
     * DOCUMENT ME!
     */
    public boolean previousPressed() {
    	return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getCardName() {
        return "FSOURCE_CARD";
    }

    /**
     * Table model to show a the attributes of a FeatureType
     */
    private class FeatureTypeTableModel extends DefaultTableModel {
        /** DOCUMENT ME! */
        private FeatureType schema;

        /** DOCUMENT ME! */
        private final String[] colNames = { "Name", "Type", "Length" };

        /**
         * Creates a new FeatureTypesTableModel object.
         */
        public FeatureTypeTableModel() {
        }

        /**
         * DOCUMENT ME!
         *
         * @param schema DOCUMENT ME!
         */
        public void setFeatureType(FeatureType schema) {
            this.schema = schema;
            fireTableDataChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public FeatureType getFeatureType() {
            return schema;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getColumnCount() {
            return colNames.length;
        }

        /**
         * DOCUMENT ME!
         *
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getColumnName(int column) {
            return colNames[column];
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getRowCount() {
            return (schema == null) ? 0 : schema.getAttributeCount();
        }

        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getValueAt(int row, int column) {
            AttributeType attr = schema.getAttributeType(row);
            Object val = null;

            switch (column) {
            case 0:
                val = attr.getName();

                break;

            case 1:
                val = attr.getType().getName();

                break;

            case 2:
                val = (attr.getFieldLength() == 0) ? null
                                                   : new Integer(attr
                        .getFieldLength());

                break;
            }

            return val;
        }

        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
