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

    /** Creates a new instance of VPFFeatureReader */
    public VPFFeatureReader(VPFFeatureType type) {
        this.featureType = type;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        reset();
    }

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
        while(readNext());
        return hasNext;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next() throws IOException, IllegalAttributeException, 
                                 NoSuchElementException {
        return currentFeature;
    }
    /**
     * Read a row and determine if it matches the feature type
     * Three possibilities here:
     * row is null -- hasNext = false, do not try again
     * row matches -- hasNext = true, do not try again
     * row does not match -- hasNext is undefined because we must try again
     * @return Whether we need to read again
     */
    private boolean readNext() throws IOException {
        boolean result = true;
    	VPFFile file = (VPFFile) featureType.getFeatureClass().getFileList().get(0);
    	hasNext = false;
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
            result = false;
		}
		// Exclude objects with a different FACC Code
		else if (featureType.getFaccCode() != null){
		    try {
                String faccCode = row.getAttribute("f_code").toString().trim(); 
                if(featureType.getFaccCode().equals(faccCode)){
                    retrieveObject(file, row);
                    hasNext = true;
                    result = false;
                }
            } catch (RuntimeException exc) {
                // Ignore this case because it typically means the f_code is invalid
            }
		} 
        return result;
    }
    /** 
     * Get the values from all of the columns
     * based on their presence (or absense) in the rows
     *
     * Potential cases:
     * simple column
     * join column
     * non-matching join
     * null value
     * geometry
     * 
     * @param file the file
     * @param row the row
     *
     */ 
    private void retrieveObject(VPFFile file, Feature row) throws IOException{
        VPFFile secondFile = null;
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
                        // If there is a problem, forget about mapping and continue
                        } catch (IOException exc) {
                        } catch (RuntimeException exc) {
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