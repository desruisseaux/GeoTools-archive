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

import org.geotools.demos.export.ExportParameters;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.LiteralExpression;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.util.SimpleInternationalString;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class EditPropertiesCard extends WizzardCard {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(EditPropertiesCard.class.getPackage()
                                                                                  .getName());

    /** DOCUMENT ME! */
    private static final String HELP_MSG =
        "Edit the properties of the output feature type.\n"
        + "Here you can specify a new Coordinate Reference System, as well as to modify \n"
        + "the schema and type name of the resulting feature type";

    /** DOCUMENT ME! */
    private ExportParameters params;

    /** DOCUMENT ME! */
    private JTextField typeNameField = new JTextField(30);

    /** DOCUMENT ME! */
    private JLabel currentCRSLabel = new JLabel();

    /** DOCUMENT ME! */
    private JTable editAttributesTable = new JTable(new EditAttsTableModel());

    /** DOCUMENT ME! */
    JComboBox comboReprojectCRS = new JComboBox();

    /** DOCUMENT ME! */
    JComboBox comboOverrideCRS = new JComboBox();

    /**
     * DOCUMENT ME!
     *
     * @param listener
     * @param prev
     */
    public EditPropertiesCard(CardListener listener, WizzardCard prev) {
        super(listener, prev);
        buildGui();
    }

    /**
     * DOCUMENT ME!
     */
    public void show() {
        super.show();
        getListener().setPreviousEnabled(true);
        getListener().setNextEnabled((params != null) && params.hasEnoughInfo());
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
     * @return DOCUMENT ME!
     */
    public boolean nextPressed() {
        WizzardCard next = getNextCard();

        FeatureType sourceSchema = params.getFeatureSource().getSchema();
        String name = this.typeNameField.getText().trim();
        LOGGER.info("name: " + name);

        if (name.length() == 0) {
            JOptionPane.showMessageDialog(getGui(),
                "Please provide a type name", "Bad inputs",
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        if (name.equals(sourceSchema.getTypeName())) {
            params.setNewTypeName(null);
        } else {
            LOGGER.info("Setting new type name: " + name);
            params.setNewTypeName(name);
        }

        CoordinateReferenceSystem overrideCRS = null;
        CoordinateReferenceSystem reprojectCRS = null;

        try {
            overrideCRS = getCRS(comboOverrideCRS);
            reprojectCRS = getCRS(comboReprojectCRS);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error parsing CRS",
                "An error has ocurred while parsing one a Coordinate Reference System: "
                + e.getMessage());

            return false;
        }

        params.setOverrideCRS(overrideCRS);
        params.setReprojectCRS(reprojectCRS);

        next.setParameter(this.params);

        return true;
    }

    /**
     * Takes the selected CRS code from the passed JComboBox, and parses it
     * using the corresponding CRSAuthorityFactory to return the  actual
     * <code>CoordinateReferenceSystem</code> instance.
     *
     * @param combo the JComboBox from with the selected CRS code is taken.
     *
     * @return the <code>CoordinateReferenceSystem</code> instance that
     *         corresponds to the CRS code selected in the combo box.
     *
     * @throws FactoryException if the CRS code can't be parsed to an actual
     *         <code>CoordinateReferenceSystem</code>.
     */
    private CoordinateReferenceSystem getCRS(JComboBox combo)
        throws FactoryException {
        CoordinateReferenceSystem crs = null;

        if (combo.isEnabled()) {
            CRSCodeDescPair selectedCRS = (CRSCodeDescPair) combo
                .getSelectedItem();
            LOGGER.info("Combo is enabled, parsing CRS '" + selectedCRS.code
                + "'");
            crs = CRS.decode(selectedCRS.code);

            LOGGER.info("CRS correctly parsed: " + crs);
        }

        return crs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
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
        return "EDIT_PROPS";
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
        if (!(paramObject instanceof ExportParameters)) {
            throw new IllegalArgumentException("Expected ExportParameters: "
                + paramObject);
        }

        this.params = (ExportParameters) paramObject;
        updateInfo();
    }

    /**
     * Updates the gui widgets with the values from the ExportParameters object
     */
    private void updateInfo() {
        if (params == null) {
            return;
        }

        FeatureType sourceSchema = params.getFeatureSource().getSchema();
        typeNameField.setText(sourceSchema.getTypeName());

        CoordinateReferenceSystem sourceCRS = sourceSchema.getDefaultGeometry()
                                                          .getCoordinateSystem();
        currentCRSLabel.setText(String.valueOf(sourceCRS));

        EditAttsTableModel model = (EditAttsTableModel) this.editAttributesTable
            .getModel();
        model.setSchema(sourceSchema);
    }

    /**
     * Builds the gui for modifying the type name, specifying the output CRS
     * and selecting the properties to export, their  type and length
     */
    private void buildGui() {
        JPanel guiPanel = getGui();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        guiPanel.add(panel, BorderLayout.NORTH);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(panel2);
        panel2.add(new JLabel("Feature type name: "));
        panel2.add(typeNameField);

        panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(panel2);
        panel2.add(new JLabel("Current Coordinate Reference System: "));
        panel2.add(currentCRSLabel);

        panel2 = new JPanel(new BorderLayout());
        panel.add(panel2);

        final JCheckBox chkOverride = new JCheckBox(
                "Force Coordinate Reference System to: ");
        panel2.add(chkOverride, BorderLayout.WEST);
        chkOverride.setToolTipText(
            "Enable this checkbox to specify a CRS wich overrides the current one.");

        comboOverrideCRS.setEnabled(false);
        panel2.add(comboOverrideCRS, BorderLayout.CENTER);
        fillCRSs(comboOverrideCRS);

        chkOverride.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    comboOverrideCRS.setEnabled(chkOverride.isSelected());
                }
            });

        panel2 = new JPanel(new BorderLayout());
        panel.add(panel2);

        final JCheckBox chkReproject = new JCheckBox(
                "New Coordinate Reference System: ");
        panel2.add(chkReproject, BorderLayout.WEST);
        chkReproject.setToolTipText(
            "Enable this checkbox to specify a CRS to reproject the source features into");

        comboReprojectCRS.setEnabled(false);
        panel2.add(comboReprojectCRS, BorderLayout.CENTER);
        fillCRSs(comboReprojectCRS);

        chkReproject.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    comboReprojectCRS.setEnabled(chkReproject.isSelected());
                }
            });

        panel = new JPanel(new BorderLayout(2, 2));
        guiPanel.add(panel);
        panel.add(new JLabel("Edit the feature type attributes:"),
            BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(this.editAttributesTable);
        scroll.setPreferredSize(new Dimension(240, 120));
        panel.add(scroll, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param combo DOCUMENT ME!
     */
    private void fillCRSs(JComboBox combo) {
        Set authorities = FactoryFinder.getCRSAuthorityFactories();
        CRSAuthorityFactory af;
        LOGGER.info("Looking for CRS authority factories...");

        List availableCrs = new LinkedList();

        for (Iterator it = authorities.iterator(); it.hasNext();) {
            af = (CRSAuthorityFactory) it.next();
            LOGGER.info("Found authority factory: " + af);

            Set codes;

            try {
                codes = af.getAuthorityCodes(CoordinateReferenceSystem.class);
            } catch (FactoryException e) {
                showError("Error getting authority codes", e.getMessage());

                continue;
            }

            String code = null;
            InternationalString desc = null;

            for (Iterator codesIt = codes.iterator(); codesIt.hasNext();
                    code = (String) codesIt.next()) {
                LOGGER.fine("adding " + code);

                if (code == null) {
                    LOGGER.warning("Skipping null code");

                    continue;
                }

                try {
                    desc = af.getDescriptionText(code);
                } catch (Exception e) {
                    LOGGER.warning("Getting description for CRS code " + code
                        + ": " + e.getMessage());

                    desc = new SimpleInternationalString("");
                }

                combo.addItem(new CRSCodeDescPair(code, desc));
            }
        }
    }

    /**
     * Simple holder of CRS code/description pairs
     */
    private static class CRSCodeDescPair {
        /** DOCUMENT ME! */
        String code;

        /** DOCUMENT ME! */
        InternationalString description;

        /**
         * Creates a new CRSCodeDescPair object.
         *
         * @param code DOCUMENT ME!
         * @param desc DOCUMENT ME!
         */
        public CRSCodeDescPair(String code, InternationalString desc) {
            this.code = code;
            this.description = desc;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String toString() {
            return code + "| " + description;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private static class EditAttsTableModel extends DefaultTableModel {
        /** DOCUMENT ME! */
        private static String[] colNames = { "Include", "Name", "Type", "Length" };

        /** DOCUMENT ME! */
        private FeatureType schema;

        /**
         * Composed from the schema attributes, holds the values of each
         * attribute property in the form [colNameProperty][propertyValue].
         * [Boolean][String][Class][Integer]
         */
        private Object[][] newValues;

        /**
         * DOCUMENT ME!
         *
         * @param schema DOCUMENT ME!
         */
        public void setSchema(FeatureType schema) {
            this.schema = schema;

            if (schema == null) {
                this.newValues = null;

                return;
            }

            this.newValues = new Object[schema.getAttributeCount()][colNames.length];

            Object val = null;
            AttributeType type;

            for (int row = 0; row < schema.getAttributeCount(); row++) {
                type = schema.getAttributeType(row);
                newValues[row][0] = Boolean.TRUE;
                newValues[row][1] = type.getName();
                newValues[row][2] = type.getType();
                int fieldLen=0;
                Filter f = type.getRestriction();
            	if(f !=null && f!=Filter.ALL && f != Filter.NONE && (f.getFilterType() == f.COMPARE_LESS_THAN || f.getFilterType() == f.COMPARE_LESS_THAN_EQUAL)){
            		try{
            		CompareFilter cf = (CompareFilter)f;
            		if(cf.getLeftValue() instanceof org.geotools.filter.LengthFunction){
            			fieldLen = Integer.parseInt(((LiteralExpression)cf.getRightValue()).getLiteral().toString());
            		}else{
            			if(cf.getRightValue() instanceof org.geotools.filter.LengthFunction){
            				fieldLen = Integer.parseInt(((LiteralExpression)cf.getLeftValue()).getLiteral().toString());
                		}
            		}
            		}catch(NumberFormatException e){
            			fieldLen = 256;
            		}
            	}else{
            		fieldLen = 256;
            	}
                
                if (fieldLen <= 0) {
                    fieldLen = 255;
                }
                newValues[row][3] = new Integer(fieldLen);
            }

            fireTableDataChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws UnsupportedOperationException DOCUMENT ME!
         */
        public FeatureType getNewFeatureType() {
            throw new UnsupportedOperationException("not implemented yet");
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
            return (newValues == null) ? 0 : newValues.length;
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
            return newValues[row][column];
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
            return column != 2; //do not allow to edit the attribute type
        }

        /**
         * Sets the object value for the cell at <code>column</code> and
         * <code>row</code>.  <code>aValue</code> is the new value.  This
         * method will generate a <code>tableChanged</code> notification.
         *
         * @param aValue the new value; this can be null
         * @param row the row whose value is to be changed
         * @param column the column whose value is to be changed
         */
        public void setValueAt(Object aValue, int row, int column) {
            LOGGER.info("Setting value for row " + row + " col " + column
                + ": " + aValue);
            newValues[row][column] = aValue;
            fireTableCellUpdated(row, column);
        }
    }
}
