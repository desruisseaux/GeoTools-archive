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
/**
 */
package org.geotools.data.vpf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.geotools.data.vpf.file.VPFFile;
import org.geotools.data.vpf.file.VPFFileFactory;
import org.geotools.data.vpf.ifc.DataTypesDefinition;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.readers.AreaGeometryFactory;
import org.geotools.data.vpf.readers.ConnectedNodeGeometryFactory;
import org.geotools.data.vpf.readers.EntityNodeGeometryFactory;
import org.geotools.data.vpf.readers.LineGeometryFactory;
import org.geotools.data.vpf.readers.TextGeometryFactory;
import org.geotools.data.vpf.readers.VPFGeometryFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.AnnotationFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


/**
 * A VPF feature class. Note that feature classes may contain one
 * or more feature types. However, all of the feature types of a 
 * feature class share the same schema. A feature type will therefore
 * delegate its schema related operations to its feature class.
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 */
public class VPFFeatureClass implements DataTypesDefinition, FileConstants,
    FeatureType {
    /**
     * The contained feature type
     */
    private DefaultFeatureType featureType;

    /**
     * The columns that are part of this feature class
     */
    private final List columns = new Vector();

    /** The coverage this feature class is part of */
    private final VPFCoverage coverage;

    /** The path of the directory containing this feature class */
    private final String directoryName;

    /** A list of files which are read to retrieve data for this feature class */
    private final AbstractList fileList = new Vector();

    /** A list of ColumnPair objects which identify the file joins */
    private final AbstractList joinList = new Vector();

    /** The name of the feature class */
    private final String typeName;

    /** The uri of the namespace in which features should be created */
    private final URI namespace;

    /**
     * The geometry factory for this feature class
     */
    private VPFGeometryFactory geometryFactory;

    /**
     * Indicator that the feature type is a text feature.
     */
    private boolean textTypeFeature = false;

    /**
     * Constructor
     *
     * @param cCoverage the owning coverage
     * @param cName the name of the class
     * @param cDirectoryName the directory containing the class
     * @throws SchemaException For problems making one of the feature classes as a FeatureType.
     */
    public VPFFeatureClass(VPFCoverage cCoverage, String cName,
        String cDirectoryName) throws SchemaException{
        this(cCoverage, cName, cDirectoryName, null);
    }

    /**
     * Constructor
     *
     * @param cCoverage the owning coverage
     * @param cName the name of the class
     * @param cDirectoryName the directory containing the class
     * @param cNamespace the namespace to create features with.  If null then
     *        a default from VPFLibrary.DEFAULTNAMESPACE is assigned.
     * @throws SchemaException For problems making one of the feature classes as a FeatureType.
     */
    public VPFFeatureClass(VPFCoverage cCoverage, String cName,
        String cDirectoryName, URI cNamespace) throws SchemaException{
        coverage = cCoverage;
        directoryName = cDirectoryName;
        typeName = cName;
        if (cNamespace == null) {
            namespace = VPFLibrary.DEFAULT_NAMESPACE;
        } else {
            namespace = cNamespace;
        }
	    

        String fcsFileName = directoryName + File.separator + TABLE_FCS;

        try {
            VPFFile fcsFile = (VPFFile) VPFFileFactory.getInstance().getFile(fcsFileName);
            Iterator iter = fcsFile.readAllRows().iterator();

            while (iter.hasNext()) {
                Feature feature = (Feature) iter.next();
                String featureClassName = feature.getAttribute("feature_class")
                                                 .toString().trim();

                if (typeName.equals(featureClassName)) {
                    addFCS(feature);
                }
            }

            // Deal with the geometry column
            iter = columns.iterator();

            GeometryAttributeType gat = null;
            AttributeType geometryColumn = null;

            while (iter.hasNext()) {
                geometryColumn = (AttributeType) iter.next();

                if (geometryColumn.isGeometry()) {
                    if(geometryColumn instanceof GeometryAttributeType){
                        gat = (GeometryAttributeType)geometryColumn;
                    }else if (geometryColumn instanceof VPFColumn){
                        gat = ((VPFColumn)geometryColumn).getGeometryAttributeType();
                    }

                    break;
                }
            }

            Vector superTypes = new Vector();
            // if it's a text geometry feature type add annotation as a super type
            if (textTypeFeature) {
                superTypes.add( AnnotationFeatureType.ANNOTATION );
            }
            
            featureType = new DefaultFeatureType(cName, namespace, columns,
                    superTypes, gat);
        } catch (IOException exp) {
            //We've already searched the FCS file once successfully
            //So this should never happen
            exp.printStackTrace();
//        } catch (SchemaException exc) {
//            // TODO Auto-generated catch block
//            exc.printStackTrace();
        }
    }

    /**
     * Add the information from a new FCS row.
     *
     * @param row The FCS table row
     */
    private void addFCS(Feature row) //throws IOException
     {
        String table1 = row.getAttribute("table1").toString();
        String table1Key = row.getAttribute("table1_key").toString();
        String table2 = row.getAttribute("table2").toString();
        String table2Key = row.getAttribute("table2_key").toString();

        try {
            VPFFile vpfFile1 = VPFFileFactory.getInstance().getFile(directoryName.concat(
                        File.separator).concat(table1));
            addFileToTable(vpfFile1);

            VPFFile vpfFile2 = null;
            AttributeType joinColumn1 = (VPFColumn) vpfFile1.getAttributeType(table1Key);
            AttributeType joinColumn2;

            try {
                vpfFile2 = VPFFileFactory.getInstance().getFile(directoryName.concat(
                            File.separator).concat(table2));
                addFileToTable(vpfFile2);
                joinColumn2 = (VPFColumn) vpfFile2.getAttributeType(table2Key);
            } catch (IOException exc) {
                fileList.add(null);

                // We need to add a geometry column 
                joinColumn2 = buildGeometryColumn(table2);
            }

            // FCS's that are the inverse of existing ones are not needed
            // But we should never get this far
            if (!joinList.contains(new ColumnPair(joinColumn2, joinColumn1))) {
                joinList.add(new ColumnPair(joinColumn1, joinColumn2));
            }
        } catch (IOException exc) {
            // File was not present 
            // which means it is for a geometry table
            // we can safely ignore it for now 
            //          exc.printStackTrace();
        }
    }
    /**
     * Create a geometry column (usually for feature classes that
     * make use of tiles so simple joins can not be used)
     * @param table The name of the table containing the geometric primitives
     * @return An <code>AttributeType</code> for the geometry column which is actually a <code>GeometryAttributeType</code>
     */
    private AttributeType buildGeometryColumn(String table) {
        AttributeType result = null;

        table = table.trim().toLowerCase();

        // Why would the fileList already contain a null?
        //      if(!fileList.contains(null)){
        CoordinateReferenceSystem crs = getCoverage().getLibrary().getCoordinateReferenceSystem();
        if(crs != null){
            result = AttributeTypeFactory.newAttributeType("GEOMETRY",
                               Geometry.class, true, -1, null, crs );
        }else{
            result = AttributeTypeFactory.newAttributeType("GEOMETRY",
                               Geometry.class, true );
        }
        columns.add(result);

        setGeometryFactory(table);

        //      }
        return result;
    }
    /**
     * Identifies the type of geometry factory to use based on the
     * name of the table containing the geometry, then constructs the
     * appropriate geometry factory object.
     * @param table The name of the geometry table
     */
    private void setGeometryFactory(String table) {
        if (table.equals(EDGE_PRIMITIVE)) {
            geometryFactory = new LineGeometryFactory();
        } else if (table.equals(FACE_PRIMITIVE)) {
            geometryFactory = new AreaGeometryFactory();
        } else if (table.equals(CONNECTED_NODE_PRIMITIVE)) {
            geometryFactory = new ConnectedNodeGeometryFactory();
        } else if (table.equals(ENTITY_NODE_PRIMITIVE)) {
            geometryFactory = new EntityNodeGeometryFactory();
        } else if (table.equals(TEXT_PRIMITIVE)) {
            geometryFactory = new TextGeometryFactory();
            textTypeFeature = true;
        }

        // if an invalid string is returned, there will be no geometry
    }
    /**
     * Adds all of the columns from a VPF file into the table. Note:
     * This does not handle columns with the same name particularly well. 
     * Perhaps the xpath mechanism can be used to help here.
     * @param vpfFile the <code>VPFFile</code> object to use
     */
    private void addFileToTable(VPFFile vpfFile) {
        //      Class columnClass;
        boolean addPrimaryKey = fileList.isEmpty();

        // Check to see if we have already grabbed this file
        if (!fileList.contains(vpfFile)) {
            fileList.add(vpfFile);

            // Pull the columns off of the file and add them to our schema
            // Except for the first file, ignore the first column since it is a join column
            for (int inx = addPrimaryKey ? 0 : 1;
                    inx < vpfFile.getAttributeCount(); inx++) {
                columns.add(vpfFile.getAttributeType(inx));
            }
        }
    }
    
    /**
     * The coverage that owns this feature class
     * @return a <code>VPFCoverage</code> object
     */
    public VPFCoverage getCoverage() {
        return coverage;
    }

    /**
     * The path to the directory that contains this feature class
     *
     * @return a <code>String</code> value representing the path to the directory.
     */
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * Returns a list of file objects
     *
     * @return a <code>List</code> containing <code>VPFFile</code> objects
     */
    public List getFileList() {
        return fileList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public List getJoinList() {
        return joinList;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getNamespace()
     */
    public URI getNamespace() {
        return featureType.getNamespace();
    }

    // not needed
//    /* (non-Javadoc)
//     * @see org.geotools.feature.FeatureType#getNamespace()
//     */
//    public URI getNamespaceURI() {
//        return featureType.getNamespaceURI();
//    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getTypeName()
     */
    public String getTypeName() {
        return featureType.getTypeName();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeTypes()
     */
    public AttributeType[] getAttributeTypes() {
        return featureType.getAttributeTypes();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#hasAttributeType(java.lang.String)
     */
    public boolean hasAttributeType(String xPath) {
        return featureType.hasAttributeType(xPath);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeType(java.lang.String)
     */
    public AttributeType getAttributeType(String xPath) {
        return featureType.getAttributeType(xPath);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#find(org.geotools.feature.AttributeType)
     */
    public int find(AttributeType type) {
        return featureType.find(type);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getDefaultGeometry()
     */
    public GeometryAttributeType getDefaultGeometry() {
        return featureType.getDefaultGeometry();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeCount()
     */
    public int getAttributeCount() {
        return featureType.getAttributeCount();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeType(int)
     */
    public AttributeType getAttributeType(int position) {
        return featureType.getAttributeType(position);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isDescendedFrom(java.lang.String, java.lang.String)
     */
    public boolean isDescendedFrom(URI nsURI, String typeName) {
        return featureType.isDescendedFrom(nsURI, typeName);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isDescendedFrom(org.geotools.feature.FeatureType)
     */
    public boolean isDescendedFrom(FeatureType type) {
        return featureType.isDescendedFrom(type);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isAbstract()
     */
    public boolean isAbstract() {
        return featureType.isAbstract();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAncestors()
     */
    public FeatureType[] getAncestors() {
        return featureType.getAncestors();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#duplicate(org.geotools.feature.Feature)
     */
    public Feature duplicate(Feature feature) throws IllegalAttributeException {
        return featureType.duplicate(feature);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureFactory#create(java.lang.Object[])
     */
    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return featureType.create(attributes);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureFactory#create(java.lang.Object[], java.lang.String)
     */
    public Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException {
        return featureType.create(attributes, featureID);
    }
    /**
     * @return Returns the geometryFactory.
     */
    public VPFGeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#find(java.lang.String)
     */
    public int find(String attName) {
        return featureType.find(attName);
    }
    
    public boolean equals(Object obj) {
        return featureType.equals(obj);
    }

    public int hashCode() {
        return featureType.hashCode();
    }
}
