/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.vpf;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.vpf.file.VPFFile;
import org.geotools.data.vpf.file.VPFFileFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/*
 * VPFFeatureReader.java
 *
 * Created on 13. april 2004, 14:35
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFFeatureReader implements FeatureReader {
    private boolean hasNext = true;
    private Feature currentFeature = null;
    private final VPFFeatureType featureType;
//    private VPFReader reader;
//    /**
//     * The current values for this iterator 
//     */
//    private final AbstractList values;
//    private FeatureType type = null;
//    private int attributeCount = 0;

//    /** Creates a new instance of VPFFeatureReader */
//    public VPFFeatureReader(String typeName, VPFDataBase dataBase, 
//                            FeatureType type) {
//        this.type = type;
//        checkFeatureType(dataBase, typeName);
//    }

    /** Creates a new instance of VPFFeatureReader */
    public VPFFeatureReader(VPFFeatureType type) {
        this.featureType = type;
    }

//    /**
//     * @param dataBase
//     * @param typename
//     */
//    private void checkFeatureType(VPFDataBase dataBase, String typename) {
//        try {
////            System.out.println("Typename er: " + typename);
//
//            VPFFeatureClass vpfclass = dataBase.getFeatureClass(typename);
//
//            if (vpfclass != null) {
//                reader = vpfclass.getReader();
//                attributeCount = reader.getAttributeCount();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        reset();
//        reader.close();
    }

//    /**
//     * Destroys the ressources associated to the iterator. Can be called several
//     * times. I don't know what to do here since the factory technically manages
//     * these streams
//     * 
//     * @throws SQLException
//     *             if something wrong occurs.
//     */
//    public void destroy() throws SQLException {
//    }

    /**
     * Put together a map of VPF files and their corresponding
     * TableRows
     * 
     * @param file
     * @param row
     * @return
     */
    private Map generateFileRowMap(VPFFile file, Feature row)
    		throws IOException{
    	String tileFileName = null;
    	Map rows = new HashMap();
    	rows.put(file, row);
    	Iterator joinIter = featureType.getFeatureClass().getJoinList().iterator();
    	while (joinIter.hasNext()) {
    		ColumnPair columnPair = (ColumnPair) joinIter.next();
    		VPFFile primaryFile = getVPFFile(columnPair.column1);
    		VPFFile joinFile = null;
    		joinFile = getVPFFile(columnPair.column2);
    
    		if (!rows.containsKey(joinFile) && rows.containsKey(primaryFile)) {
    			Feature joinRow = (Feature) rows.get(primaryFile);
    
    			try {
    				int joinID = Integer.parseInt(joinRow.getAttribute(columnPair.column1.getName()).toString());
    				rows.put(joinFile, getVPFFile(columnPair.column2).getRowFromId(columnPair.column2.getName(), joinID));
    			} catch (NullPointerException exc) {
    				// Non-matching joins - just put in a NULL
    				rows.put(joinFile, null);
    			} catch (IllegalAttributeException exc) {
                    // I really don't expect to see this one
                    exc.printStackTrace();
                    rows.put(joinFile, null);
                }
    		}
    	}
    	return rows;
    
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return featureType;
    }

//    /**
//     * Retrieves the tile name for a particular row
//     * @deprecated
//     * @param row
//     *            a row that hopefully contains a tile_id
//     * @return the tile name
//     */
//    private Integer getTileNumber(Feature row) throws SQLException {
//    	Integer result = null;
//        result = row.getAttribute("tile_id")
//    	AttributeType[] columns = featureType.getFeatureClass().getAttributeTypes();
//    	Iterator tileColumnIter = columns.iterator();
//    	int index = -1;
//    	while (tileColumnIter.hasNext()) {
//    		VPFColumn column = (VPFColumn) tileColumnIter.next();
//    		if (column.getName().toLowerCase().equals("tile_id")) {
//    			index = columns.indexOf(column);
//    			break;
//    		}
//    	}
//    	if (index >= 0) {
//    		result = new Integer(((RowField) row.get(index)).intValue());
//    	} else {
//    		throw new SQLException("Table row does not contain a tile_id");
//    	}
//    	return result;
//    }
//
//    /**
//     * Returns the column value for a given column index.
//     * 
//     * @param columnIndex
//     *            index of the column.
//     * @return the column value.
//     * @throws SQLException
//     *             if something wrong occurs.
//     */
//    public Object getValue(int columnIndex) throws SQLException {
//    	try {
//    		return values.get(columnIndex);
//    	} catch (RuntimeException exp) {
//    		return new String();
//    	}
//    }

//    /**
//     * Returns the column value for a given column name. Note: if there are
//     * multiple columns with the same name (usually indicitive of a join) the
//     * first will be returned
//     * 
//     * @param columnName
//     *            name of the column.
//     * @return the value at that column.
//     * @throws SQLException
//     *             if the column is not there.
//     */
//    public Object getValue(String columnName) throws SQLException {
//    	Object result = null;
//    	VPFColumn column;
//    	Iterator iter = featureType.getFeatureClass().getColumns().iterator();
//    	int count = -1;
//    	while (iter.hasNext()) {
//    		count++;
//    		column = (VPFColumn) iter.next();
//    		if (column.getName().equals(columnName)) {
//    			break;
//    		}
//    	}
//    	if ((count >= 0) && (count < values.size())) {
//    		result = values.get(count);
//    	} else {
//    		throw new SQLException("Row " + columnName
//    				+ " does not exist in this table.");
//    	}
//    	return result;
//    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        Object[] values = new Object[featureType.getAttributeCount()];
        try {
            currentFeature = featureType.create(values);
        } catch (IllegalAttributeException exc) {
            // This really shouldn't happen since everything should be nillable
            exc.printStackTrace();
        }
        readNext(false);
        return hasNext;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next() throws IOException, IllegalAttributeException, 
                                 NoSuchElementException {
        return currentFeature;
//        reader.next();
//
//        String fid = reader.getFeatureID();
//        Object[] values = new Object[attributeCount];
//
//        for (int i = 0; i < attributeCount; i++) {
//            values[i] = reader.read(i);
//        }
//
//        Feature f = null;
//
//        try {
//            f = type.create(values, fid);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return f;
    }
//
//    /**
//     * Retrieves data for the next element
//     * 
//     * @return true if it succeeds. False if no next value is available
//     * @throws SQLException
//     *             if something wrong occurs.
//     */
//    public boolean next() throws SQLException {
//    	values.clear();
//    	return next(false);
//    }

    //TODO: simplify this convoluted mess Venture with caution!
    /**
     * 
     */
    private void readNext(boolean reentrant) throws IOException {
    	VPFFile file = (VPFFile) featureType.getFeatureClass().getFileList().get(0);
    	hasNext = false;
        VPFFile secondFile = null;
    
		Feature row = null;
        try {
            if(file.hasNext()){
                row = file.readFeature();
            }
        } catch (IOException exc1) {
            // TODO Auto-generated catch block
            exc1.printStackTrace();
        } catch (IllegalAttributeException exc1) {
            // TODO Auto-generated catch block
            exc1.printStackTrace();
        }
        if ((row == null)) {
            hasNext = false;
		}
		// Exclude objects with a different FACC Code
		else if ((featureType.getFaccCode() == null)
				|| featureType.getFaccCode().equals(
						row.getAttribute("f_code").toString().trim())) {

			// Get the values from all of the columns
			// based on their presence (or absense) in the rows

			// Potential cases:
			// simple column
			// join column
			// non-matching join
		    // null value
			// geometry 
			Map rows = generateFileRowMap(file, row);
            AttributeType[] attributes = featureType.getFeatureClass()
					.getAttributeTypes();
            Object[] values = new Object[attributes.length];
            for(int inx = 0; inx < attributes.length; inx++){
				VPFColumn column = null;
                try {
                    column = (VPFColumn) attributes[inx];
                    Object value = null;
                    secondFile = getVPFFile(column); 
                    Feature tempRow = (Feature) rows.get(secondFile);
                    if(tempRow != null){
                        value = tempRow.getAttribute(column.getName());
                        if (column.isAttemptLookup()){
                            try {
                                // Attempt to perform a lookup and conversion
                                String featureClassName = getVPFFile(column).getFileName();
                                String intVdtFileName = featureType.getFeatureClass().getDirectoryName().concat(File.separator).concat("int.vdt");
                                VPFFile intVdtFile = VPFFileFactory.getInstance().getFile(intVdtFileName);
                                Iterator intVdtIter = intVdtFile.readAllRows().iterator();
                                while(intVdtIter.hasNext()){
                                    Feature intVdtRow = (Feature)intVdtIter.next();
                                    if(intVdtRow.getAttribute("table").toString().trim().equals(featureClassName) && 
                                            (Short.parseShort(intVdtRow.getAttribute("value").toString()) == Short.parseShort(value.toString()) &&
                                            (intVdtRow.getAttribute("attribute").toString().trim().equals(column.getName())))){
                                        value = intVdtRow.getAttribute("description").toString().trim();
                                        break;
                                    }
                                }
                            } catch (RuntimeException exc) {
                                // If there is a problem, forget about mapping and continue
                            }
                        }
                    }
                    try {
                        currentFeature.setAttribute(inx, value);
                    } catch (ArrayIndexOutOfBoundsException exc) {
                        // TODO Auto-generated catch block
                        exc.printStackTrace();
                    } catch (IllegalAttributeException exc) {
                        // TODO Auto-generated catch block
                        exc.printStackTrace();
                    }
                } catch (ClassCastException exc2) {
                    try {
                        // This is the area geometry case
                        featureType.getFeatureClass().getGeometryFactory().createGeometry(featureType, currentFeature);
                    } catch (IllegalAttributeException exc) {
                        // TODO Auto-generated catch block
                        exc.printStackTrace();
                    } catch (SQLException exc) {
                        // TODO Auto-generated catch block
                        exc.printStackTrace();
                    }
                }
			}
            hasNext = true;
		} else {
			try {
				readNext(true);
			} catch (StackOverflowError exc) {
                // We might read many many features before finding one that matches FACC Codes
				// Allow the stack to collapse before trying again
				if (reentrant) {
					throw exc;
				} else {
					readNext(false);
				}
			}
		}
    }
    /**
     * Returns the VPFFile for a particular column.
     * It will only find the first match, but that should be okay
     * because duplicate columns will cause even bigger problems elsewhere.
     * @param column the column to search for 
     * @return the VPFFile that owns this column
     */
    private VPFFile getVPFFile(AttributeType column){
        VPFFile result = null;
        VPFFile temp;
        Iterator iter = featureType.getFeatureClass().getFileList().iterator();
        while(iter.hasNext()){
            temp = (VPFFile)iter.next();
            if((temp != null) && (temp.find(column) >= 0)){
                result = temp;
                break;
            }
        }
        return result;
    }
    /**
     * Need to reset the stream for the next time Resets the iterator by
     * resetting the stream.
     * 
     */
    public void reset(){
    		VPFFile file = (VPFFile) featureType.getFeatureClass()
    				.getFileList().get(0);
    		file.reset();
    }
}