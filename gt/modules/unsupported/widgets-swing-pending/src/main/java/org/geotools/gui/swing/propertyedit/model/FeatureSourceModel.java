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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.geotools.data.FeatureSource;
import org.jdesktop.swingx.JXTable;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.FeatureType;

/**
 *
 * @author johann sorel
 */

public class FeatureSourceModel implements TableModel{
    
    
    private ArrayList<String> columns = new ArrayList<String>();
    private ArrayList<Class> classes = new ArrayList<Class>();
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private FeatureSource source;
    private FeatureCollection collection;
    private JXTable tab;
    
    /** Creates a new instance of BasicTableModel 
     * @param tab 
     * @param collection 
     * @param source 
     */
    public FeatureSourceModel(JXTable tab, FeatureCollection collection, FeatureSource source) {
        super();
        this.tab = tab;
        this.collection = collection;
        this.source = source;
        
        init();
    }
    
    private void init(){
        classes.clear();
        columns.clear();
        features.clear();
        
        Collection<Attribute> col = collection.attributes();
                
        Iterator<Attribute> ite = col.iterator();            
        while (ite.hasNext()){
            Attribute att = ite.next();
            System.out.println( att.getValue() );
        }
        
        
        Collection<FeatureType> typ = collection.memberTypes();
        Iterator<FeatureType> it = typ.iterator();            
        while (it.hasNext()){
            FeatureType att = it.next();
            System.out.println( att.getDefaultGeometry() );
        }
        
        /* TEST
        FeatureCollectionType type = collection.getType();
        Collection<AttributeDescriptor> col = type.attributes();
        
        Iterator<AttributeDescriptor> ite = col.iterator();
        
        while (ite.hasNext()){
            AttributeDescriptor att = ite.next();
            System.out.println( att.getName() );
        }*/
        
        /*FeatureType ft = collection.getSchema();
        
        for(int i=0; i<ft.getAttributeCount(); i++ ){
            columns.add(ft.getAttributeType(i).getName());
            classes.add(ft.getAttributeType(i).getType());
        }
        
        
        FeatureIterator fi = collection.features();
        
        while(fi.hasNext()){
            features.add(fi.next());
        }
        
        tab.revalidate();*/
        
    }
    
    
    public int getColumnCount(){
        return columns.size();
    }
    
    
    public void reset(FeatureCollection fc){
        collection = fc;
        init();
        tab.revalidate();
    }
    
    
    
    public Class getColumnClass(int i){
        return classes.get(i);
    }
    
    public String getColumnName(int column) {
        return columns.get(column);
    }
    
    public int getRowCount() {
        return features.size();
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return features.get(rowIndex).getValue().toArray()[columnIndex];
        //return features.get(rowIndex).getAttribute(columnIndex);
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        /*
        FeatureStore store;
        
        if( source instanceof FeatureStore ){
            
            store = (FeatureStore) source;
            DefaultTransaction transaction = new DefaultTransaction("trans_maj");
            
            store.setTransaction( transaction );
            
            FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
            Filter filter = ff.id( Collections.singleton( ff.featureId( features.get(rowIndex).getID() )));
            
            FeatureType featureType = store.getSchema();
            AttributeType attributeType = featureType.getAttributeType( collection.getSchema().getAttributeType(columnIndex).getName() );
            
            try {
                store.modifyFeatures( attributeType, aValue, filter );
                transaction.commit();
                
            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    transaction.rollback();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
        try {
            features.get(rowIndex).setAttribute(columnIndex,aValue);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        } catch (IllegalAttributeException ex) {
            ex.printStackTrace();
        }
        
        */
        
    }
    
    
    
    
    public void addTableModelListener(TableModelListener l) {
    }
    
    public void removeTableModelListener(TableModelListener l) {
    }
    
    
    
}
