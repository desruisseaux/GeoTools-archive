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

package org.geotools.gui.swing.propertyedit.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.jdesktop.swingx.JXTable;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureStore;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

/**
 *
 * @author johann sorel
 */
public class FeatureSourceModel implements TableModel {

    private ArrayList<PropertyDescriptor> columns = new ArrayList<PropertyDescriptor>();
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private FeatureSource source;
    private FeatureCollection collection;
    private JXTable tab;

    /** Creates a new instance of BasicTableModel
     * @param tab
     * @param collection
     * @param source
     */
    public FeatureSourceModel(JXTable tab, FeatureSource source) {
        super();
        this.tab = tab;
        this.source = source;

        init();
    }

    private void init() {

        columns.clear();
        features.clear();

        FeatureType ft = source.getSchema();

        Collection<PropertyDescriptor> cols = ft.getProperties();
        Iterator<PropertyDescriptor> ite = cols.iterator();

        PropertyDescriptor desc;
        while (ite.hasNext()) {
            columns.add(ite.next());
        }

        try {
            FeatureIterator fi = source.getFeatures().features();
            while (fi.hasNext()) {
                features.add(fi.next());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Class getColumnClass(int column) {
        return columns.get(column).getType().getBinding();
    }

    public String getColumnName(int column) {
        return columns.get(column).getName().toString();
    }

    public int getRowCount() {
        return features.size();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return features.get(rowIndex).getProperty(columns.get(columnIndex).getName()).getValue();
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        FeatureStore store;
        if (source instanceof FeatureStore) {

            System.out.println("la");
            /*try {
            Feature feature = features[rowIndex];
            feature.getProperty(columns.get(columnIndex).getName()).setValue(aValue);
            //features.get(rowIndex).setAttribute(columnIndex, aValue);
            } catch (Exception ex) {
            ex.printStackTrace();
            }*/

            store = (FeatureStore) source;
            DefaultTransaction transaction = new DefaultTransaction("trans_maj");

            //transaction.
            //store.
            /*store.setTransaction(transaction);
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
            Filter filter = ff.id(Collections.singleton(ff.featureId(features.get(rowIndex).getID())));
            FeatureType featureType = store.getSchema();
            AttributeType attributeType = featureType.getAttributeType(collection.getSchema().getAttributeType(columnIndex).getName());
            try {
            store.modifyFeatures(attributeType, aValue, filter);
            transaction.commit();
            } catch (IOException ex) {
            ex.printStackTrace();
            try {
            transaction.rollback();
            } catch (IOException e) {
            e.printStackTrace();
            }
            }*/
        }
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}