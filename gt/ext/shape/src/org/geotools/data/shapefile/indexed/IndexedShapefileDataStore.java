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
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.AbstractFeatureLocking;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.AbstractFeatureStore;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.dbf.IndexedDbaseFileReader;
import org.geotools.data.shapefile.prj.PrjFileReader;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.filter.Filter;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.UnsupportedFilterException;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.geotools.index.rtree.FilterConsumer;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.fs.FileSystemPageStore;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.xml.gml.GMLSchema;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * A DataStore implementation which allows reading and writing from Shapefiles.
 *
 * @author Ian Schneider
 * @author Tommaso Nolli
 *
 * @todo fix file creation bug
 */
public class IndexedShapefileDataStore extends ShapefileDataStore {
    public static final byte TREE_NONE = 0;
    public static final byte TREE_GRX = 1;
    public static final byte TREE_QIX = 2;
    final URL treeURL;
    byte treeType;
    final boolean createIndex;
    final boolean useIndex;

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     *
     * @throws java.net.MalformedURLException If computation of related URLs
     *         (dbf,shx) fails.
     */
    public IndexedShapefileDataStore(URL url)
        throws java.net.MalformedURLException {
        this(url, null, true, true, TREE_GRX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     * @param namespace DOCUMENT ME!
     *
     * @throws java.net.MalformedURLException If computation of related URLs
     *         (dbf,shx) fails.
     */
    public IndexedShapefileDataStore(URL url, URI namespace)
        throws java.net.MalformedURLException {
        this(url, namespace, true, true, TREE_GRX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     * @param namespace DOCUMENT ME!
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     *
     * @throws java.net.MalformedURLException
     */
    public IndexedShapefileDataStore(URL url, URI namespace,
        boolean useMemoryMappedBuffer) throws java.net.MalformedURLException {
        this(url, namespace, useMemoryMappedBuffer, true, TREE_GRX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     *
     * @throws java.net.MalformedURLException
     */
    public IndexedShapefileDataStore(URL url, boolean useMemoryMappedBuffer)
        throws java.net.MalformedURLException {
        this(url, (URI) null, useMemoryMappedBuffer, true, TREE_GRX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     * @param createIndex enable/disable automatic index creation if needed
     *
     * @throws java.net.MalformedURLException
     */
    public IndexedShapefileDataStore(URL url, boolean useMemoryMappedBuffer,
        boolean createIndex) throws java.net.MalformedURLException {
        this(url, null, useMemoryMappedBuffer, createIndex, TREE_GRX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     * @param createIndex enable/disable automatic index creation if needed
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     * @param treeType DOCUMENT ME!
     *
     * @throws java.net.MalformedURLException
     * @throws NullPointerException DOCUMENT ME!
     */
    public IndexedShapefileDataStore(URL url, URI namespace,
        boolean useMemoryMappedBuffer, boolean createIndex, byte treeType)
        throws java.net.MalformedURLException {
        super(url, namespace);

        String filename = null;

        if (url == null) {
            throw new NullPointerException("Null URL for ShapefileDataSource");
        }

        try {
            filename = java.net.URLDecoder.decode(url.toString(), "US-ASCII");
        } catch (java.io.UnsupportedEncodingException use) {
            throw new java.net.MalformedURLException("Unable to decode " + url
                + " cause " + use.getMessage());
        }

        String shpext = ".shp";
        String dbfext = ".dbf";
        String shxext = ".shx";
        String grxext = ".grx";
        String qixext = ".qix";

        if (filename.endsWith(shpext) || filename.endsWith(dbfext)
                || filename.endsWith(shxext)) {
            filename = filename.substring(0, filename.length() - 4);
        } else if (filename.endsWith(".SHP") || filename.endsWith(".DBF")
                || filename.endsWith(".SHX")) {
            filename = filename.substring(0, filename.length() - 4);
            shpext = ".SHP";
            dbfext = ".DBF";
            shxext = ".SHX";
            grxext = ".GRX";
            qixext = ".QIX";
        }

        this.treeType = treeType;
        this.useMemoryMappedBuffer = useMemoryMappedBuffer;
        this.useIndex = treeType != TREE_NONE;
        this.createIndex = createIndex && useIndex;

        if (this.isLocal()) {
            if (treeType == TREE_QIX) {
                treeURL = new URL(filename + qixext);
                this.treeType = TREE_QIX;
                LOGGER.fine("Using qix tree");
            } else if (treeType == TREE_GRX) {
                treeURL = new URL(filename + grxext);
                LOGGER.fine("Using grx tree");
            } else {
                treeURL = new URL(filename + grxext);
                this.treeType = TREE_NONE;
            }
        } else {
            treeURL = new URL(filename + grxext);
            this.treeType = TREE_NONE;
        }
    }

    /**
     * Determine if the location of this shape is local or remote.
     *
     * @return true if local, false if remote
     */
    public boolean isLocal() {
        return shpURL.getProtocol().equals("file");
    }

    /**
     * Use the spatial index if available and adds a small optimization: if no
     * attributes are going to be read, don't uselessly open and read the dbf
     * file.
     *
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
        throws IOException {
        String[] propertyNames = query.getPropertyNames();
        String defaultGeomName = schema.getDefaultGeometry().getName();

        FeatureType newSchema = schema;
        boolean readDbf = true;

        try {
            if ((propertyNames != null) && (propertyNames.length == 1)
                    && propertyNames[0].equals(defaultGeomName)) {
                readDbf = false;
                newSchema = DataUtilities.createSubType(schema, propertyNames);
            }

            return createFeatureReader(typeName,
                getAttributesReader(readDbf, query.getFilter()), newSchema);
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema", se);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     * @param r
     * @param readerSchema
     *
     * @return
     *
     * @throws SchemaException
     * @throws IOException
     */
    protected FeatureReader createFeatureReader(String typeName, Reader r,
        FeatureType readerSchema) throws SchemaException, IOException {
        return new org.geotools.data.FIDFeatureReader(r,
            new ShapeFIDReader(typeName, r), readerSchema);
    }

    /**
     * Returns the attribute reader, allowing for a pure shape reader, or a
     * combined dbf/shp reader.
     *
     * @param readDbf - if true, the dbf fill will be opened and read
     * @param filter - a Filter to use
     *
     * @return
     *
     * @throws IOException
     */
    protected Reader getAttributesReader(boolean readDbf, Filter filter)
        throws IOException {
        Envelope bbox = null;

        if (filter != null) {
            FilterConsumer fc = new FilterConsumer();
            filter.accept(fc);
            bbox = fc.getBounds();
        }

        AttributeType[] atts = (schema == null) ? readAttributes()
                                                : schema.getAttributeTypes();

        List goodRecs = null;

        if ((bbox != null) && this.useIndex) {
            try {
                goodRecs = this.queryTree(bbox);
            } catch (TreeException e) {
                throw new IOException("Error querying index: " + e.getMessage());
            }
        }

        IndexedDbaseFileReader dbfR = null;

        if (!readDbf) {
            LOGGER.fine("The DBF file won't be opened since no attributes "
                + "will be read from it");
            atts = new AttributeType[] { schema.getDefaultGeometry() };
        } else {
            dbfR = (IndexedDbaseFileReader) openDbfReader();
        }

        return new Reader(atts, openShapeReader(), dbfR, goodRecs);
    }

    /**
     * Queries the spatial index
     *
     * @param bbox
     *
     * @return a List of <code>Data</code> objects
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws TreeException DOCUMENT ME!
     */
    private List queryTree(Envelope bbox)
        throws DataSourceException, IOException, TreeException {
        if (this.treeType == TREE_GRX) {
            return this.queryRTree(bbox);
        } else if (this.treeType == TREE_QIX) {
            return this.queryQuadTree(bbox);
        } else {
            // Should not happen
            return null;
        }
    }

    /**
     * RTree query
     *
     * @param bbox
     *
     * @return
     *
     * @throws DataSourceException
     * @throws IOException
     */
    private List queryRTree(Envelope bbox)
        throws DataSourceException, IOException {
        List goodRecs = null;
        RTree rtree = this.openRTree();

        try {
            if ((rtree != null) && (rtree.getBounds() != null)
                    && !bbox.contains(rtree.getBounds())) {
                goodRecs = rtree.search(bbox);
            }
        } catch (LockTimeoutException le) {
            throw new DataSourceException("Error querying RTree", le);
        } catch (TreeException re) {
            throw new DataSourceException("Error querying RTree", re);
        } finally {
            try {
                rtree.close();
            } catch (Exception ee) {
            }
        }

        return goodRecs;
    }

    /**
     * QuadTree Query
     *
     * @param bbox
     *
     * @return
     *
     * @throws DataSourceException
     * @throws IOException
     * @throws TreeException DOCUMENT ME!
     */
    private List queryQuadTree(Envelope bbox)
        throws DataSourceException, IOException, TreeException {
        List tmp = null;
        List goodRecs = null;
        IndexFile shx = null;
        QuadTree tree = null;

        try {
            tree = this.openQuadTree();

            if ((tree != null) && !bbox.contains(tree.getRoot().getBounds())) {
                tmp = tree.search(bbox);

                if (tmp.size() > 0) {
                    // WARNING: QuadTree records number begins from 0
                    shx = this.openIndexFile();

                    Collections.sort(tmp);

                    DataDefinition def = new DataDefinition("US-ASCII");
                    def.addField(Integer.class);
                    def.addField(Long.class);

                    Data data = null;
                    Integer recno = null;

                    for (int i = 0; i < tmp.size(); i++) {
                        recno = (Integer) tmp.get(i);
                        data = new Data(def);
                        data.addValue(new Integer(recno.intValue() + 1));
                        data.addValue(new Long(shx.getOffsetInBytes(
                                    recno.intValue())));
                    }
                }
            }
        } catch (StoreException le) {
            throw new DataSourceException("Error querying QuadTree", le);
        } finally {
            try {
                tree.close();
            } catch (Exception ee) {
            }

            try {
                shx.close();
            } catch (Exception ee) {
            }
        }

        return goodRecs;
    }

    /**
     * Convenience method for opening a ShapefileReader.
     *
     * @return An IndexFile
     *
     * @throws IOException
     */
    protected IndexFile openIndexFile() throws IOException {
        ReadableByteChannel rbc = getReadChannel(shxURL);

        if (rbc == null) {
            return null;
        }

        // return new IndexFile(rbc, this.useMemoryMappedBuffer);
        return new IndexFile(rbc, false);
    }

    /**
     * Convenience method for opening a ShapefileReader.
     *
     * @return A new ShapefileReader.
     *
     * @throws IOException If an error occurs during creation.
     * @throws DataSourceException DOCUMENT ME!
     */
    protected ShapefileReader openShapeReader() throws IOException {
        ReadableByteChannel rbc = getReadChannel(shpURL);

        if (rbc == null) {
            return null;
        }

        try {
            return new ShapefileReader(rbc, true, useMemoryMappedBuffer,
                readWriteLock);
        } catch (ShapefileException se) {
            throw new DataSourceException("Error creating ShapefileReader", se);
        }
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     *
     * @return A new DbaseFileReader
     *
     * @throws IOException If an error occurs during creation.
     */
    protected DbaseFileReader openDbfReader() throws IOException {
        ReadableByteChannel rbc = getReadChannel(dbfURL);

        if (rbc == null) {
            return null;
        }

        return new IndexedDbaseFileReader(rbc, this.useMemoryMappedBuffer);
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     *
     * @return A new DbaseFileReader
     *
     * @throws IOException If an error occurs during creation.
     * @throws FactoryException DOCUMENT ME!
     */
    protected PrjFileReader openPrjReader()
        throws IOException, FactoryException {
        ReadableByteChannel rbc = null;

        try {
            rbc = getReadChannel(prjURL);
        } catch (IOException e) {
            LOGGER.warning("projection (.prj) for shapefile not available");
        }

        if (rbc == null) {
            return null;
        }

        return new PrjFileReader(rbc);
    }

    /**
     * Convenience method for opening an RTree index.
     *
     * @return A new RTree.
     *
     * @throws IOException If an error occurs during creation.
     * @throws DataSourceException DOCUMENT ME!
     */
    protected RTree openRTree() throws IOException {
        if (!this.isLocal()) {
            return null;
        }

        File file = new File(treeURL.getPath());

        if (!file.exists() || (file.length() == 0)) {
            if (this.createIndex) {
                try {
                    this.buildRTree();
                } catch (TreeException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        RTree ret = null;

        try {
            FileSystemPageStore fps = new FileSystemPageStore(file);
            ret = new RTree(fps);
        } catch (TreeException re) {
            throw new DataSourceException("Error opening RTree", re);
        }

        return ret;
    }

    /**
     * Convenience method for opening a QuadTree index.
     *
     * @return A new QuadTree
     *
     * @throws StoreException
     */
    protected QuadTree openQuadTree() throws StoreException {
        File file = new File(treeURL.getPath());

        if (!file.exists() || (file.length() == 0)) {
            if (this.createIndex) {
                try {
                    this.buildQuadTree();
                } catch (TreeException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        FileSystemIndexStore store = new FileSystemIndexStore(file);

        return store.load();
    }

    /**
     * Get an array of type names this DataStore holds.<BR/>ShapefileDataStore
     * will always return a single name.
     *
     * @return An array of length one containing the single type held.
     */
    public String[] getTypeNames() {
        return new String[] { getCurrentTypeName(), };
    }

    /**
     * Create the type name of the single FeatureType this DataStore
     * represents.<BR/> For example, if the urls path is
     * file:///home/billy/mytheme.shp, the type name will be mytheme.
     *
     * @return A name based upon the last path component of the url minus the
     *         extension.
     */
    protected String createFeatureTypeName() {
        String path = shpURL.getPath();
        int slash = Math.max(0, path.lastIndexOf('/') + 1);
        int dot = path.indexOf('.', slash);

        if (dot < 0) {
            dot = path.length();
        }

        return path.substring(slash, dot);
    }

    protected String getCurrentTypeName() {
        return (schema == null) ? createFeatureTypeName() : schema.getTypeName();
    }

    /**
     * A convenience method to check if a type name is correct.
     *
     * @param requested The type name requested.
     *
     * @throws IOException If the type name is not available
     */
    protected void typeCheck(String requested) throws IOException {
        if (!getCurrentTypeName().equals(requested)) {
            throw new IOException("No such type : " + requested);
        }
    }

    /**
     * Create a FeatureWriter for the given type name.
     *
     * @param typeName The typeName of the FeatureType to write
     * @param transaction DOCUMENT ME!
     *
     * @return A new FeatureWriter.
     *
     * @throws IOException If the typeName is not available or some other error
     *         occurs.
     */
    protected FeatureWriter createFeatureWriter(String typeName,
        Transaction transaction) throws IOException {
        typeCheck(typeName);

        return new Writer(typeName);
    }

    /**
     * Obtain the FeatureType of the given name. ShapefileDataStore contains
     * only one FeatureType.
     *
     * @param typeName The name of the FeatureType.
     *
     * @return The FeatureType that this DataStore contains.
     *
     * @throws IOException If a type by the requested name is not present.
     */
    public FeatureType getSchema(String typeName) throws IOException {
        typeCheck(typeName);

        return getSchema();
    }

    public FeatureType getSchema() throws IOException {
        if (schema == null) {
            try {
                AttributeType[] types = readAttributes();
                FeatureType parent = null;
                Class geomType = types[0].getType();

                if ((geomType == Point.class) || (geomType == MultiPoint.class)) {
                    parent = BasicFeatureTypes.POINT;
                } else if ((geomType == Polygon.class)
                        || (geomType == MultiPolygon.class)) {
                    parent = BasicFeatureTypes.POLYGON;
                } else if ((geomType == LineString.class)
                        || (geomType == MultiLineString.class)) {
                    parent = BasicFeatureTypes.LINE;
                }

                if (parent != null) {
                    schema = FeatureTypes.newFeatureType(readAttributes(),
                            createFeatureTypeName(), namespace, false,
                            new FeatureType[] { parent });
                } else {
                    if (namespace != null) {
                        schema = FeatureTypes.newFeatureType(readAttributes(),
                                createFeatureTypeName(), namespace, false);
                    } else {
                        schema = FeatureTypes.newFeatureType(readAttributes(),
                                createFeatureTypeName(), GMLSchema.NAMESPACE,
                                false);
                    }
                }
            } catch (SchemaException se) {
                throw new DataSourceException("Error creating FeatureType", se);
            }
        }

        return schema;
    }

    /**
     * Create the AttributeTypes contained within this DataStore.
     *
     * @return An array of new AttributeTypes
     *
     * @throws IOException If AttributeType reading fails
     */
    protected AttributeType[] readAttributes() throws IOException {
        ShapefileReader shp = openShapeReader();
        IndexedDbaseFileReader dbf = (IndexedDbaseFileReader) openDbfReader();
        AbstractCRS cs = null;

        try {
            PrjFileReader prj = openPrjReader();

            if (prj != null) {
                cs = (AbstractCRS) prj.getCoodinateSystem();
            }
        } catch (FactoryException fe) {
            cs = null;
        }

        try {
            GeometryAttributeType geometryAttribute = (GeometryAttributeType) AttributeTypeFactory
                .newAttributeType("the_geom",
                    JTSUtilities.findBestGeometryClass(
                        shp.getHeader().getShapeType()), true, 0, null, cs);

            AttributeType[] atts;

            // take care of the case where no dbf and query wants all =>
            // geometry only
            if (dbf != null) {
                DbaseFileHeader header = dbf.getHeader();
                atts = new AttributeType[header.getNumFields() + 1];
                atts[0] = geometryAttribute;

                for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
                    Class clazz = header.getFieldClass(i);
                    atts[i + 1] = AttributeTypeFactory.newAttributeType(header
                            .getFieldName(i), clazz, true,
                            header.getFieldLength(i));
                }
            } else {
                atts = new AttributeType[] { geometryAttribute };
            }

            return atts;
        } finally {
            try {
                shp.close();
            } catch (IOException ioe) {
                // do nothing
            }

            try {
                dbf.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }


    /**
     * Gets the bounding box of the file represented by this data store as a
     * whole (that is, off all of the features in the shape)
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private Envelope getBounds() throws DataSourceException {
        // This is way quick!!!
        ReadableByteChannel in = null;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);
            in = getReadChannel(shpURL);
            in.read(buffer);
            buffer.flip();

            ShapefileHeader header = new ShapefileHeader();
            header.read(buffer, true);

            return new Envelope(header.minX(), header.maxX(), header.minY(),
                header.maxY());
        } catch (IOException ioe) {
            // What now? This seems arbitrarily appropriate !
            throw new DataSourceException("Problem getting Bbox", ioe);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getBounds(org.geotools.data.Query)
     */
    protected Envelope getBounds(Query query) throws IOException {
        Envelope ret = null;

        if (query.getFilter() == Filter.NONE) {
            ret = getBounds();
        } else if (this.useIndex) {
            RTree rtree = this.openRTree();

            if (rtree != null) {
                try {
                    ret = rtree.getBounds(query.getFilter());
                } catch (TreeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (UnsupportedFilterException e) {
                    // Ignoring...
                } finally {
                    try {
                        rtree.close();
                    } catch (Exception ee) {
                    }
                }
            }
        }

        return ret;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(final String typeName)
        throws IOException {
        final FeatureType featureType = getSchema(typeName);

        if (isWriteable) {
            if (getLockingManager() != null) {
                return new AbstractFeatureLocking() {
                        public DataStore getDataStore() {
                            return IndexedShapefileDataStore.this;
                        }

                        public void addFeatureListener(FeatureListener listener) {
                            listenerManager.addFeatureListener(this, listener);
                        }

                        public void removeFeatureListener(
                            FeatureListener listener) {
                            listenerManager.removeFeatureListener(this, listener);
                        }

                        public FeatureType getSchema() {
                            return featureType;
                        }

                        public Envelope getBounds(Query query)
                            throws IOException {
                            return IndexedShapefileDataStore.this.getBounds(query);
                        }
                    };
            } else {
                return new AbstractFeatureStore() {
                        public DataStore getDataStore() {
                            return IndexedShapefileDataStore.this;
                        }

                        public void addFeatureListener(FeatureListener listener) {
                            listenerManager.addFeatureListener(this, listener);
                        }

                        public void removeFeatureListener(
                            FeatureListener listener) {
                            listenerManager.removeFeatureListener(this, listener);
                        }

                        public FeatureType getSchema() {
                            return featureType;
                        }

                        public Envelope getBounds(Query query)
                            throws IOException {
                            return IndexedShapefileDataStore.this.getBounds(query);
                        }
                    };
            }
        } else {
            return new AbstractFeatureSource() {
                    public DataStore getDataStore() {
                        return IndexedShapefileDataStore.this;
                    }

                    public void addFeatureListener(FeatureListener listener) {
                        listenerManager.addFeatureListener(this, listener);
                    }

                    public void removeFeatureListener(FeatureListener listener) {
                        listenerManager.removeFeatureListener(this, listener);
                    }

                    public FeatureType getSchema() {
                        return featureType;
                    }

                    public Envelope getBounds(Query query)
                        throws IOException {
                        return IndexedShapefileDataStore.this.getBounds(query);
                    }
                };
        }
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getCount(org.geotools.data.Query)
     */
    protected int getCount(Query query) throws IOException {
        if (query.getFilter() == Filter.NONE) {
            ShapefileReader reader = new ShapefileReader(getReadChannel(shpURL),
                    readWriteLock);
            int count = -1;

            try {
                count = reader.getCount(count);
            } catch (IOException e) {
                throw e;
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ioe) {
                    // do nothing
                }
            }

            return count;
        }

        return super.getCount(query);
    }

    /**
     * Builds the RTree index
     *
     * @throws TreeException DOCUMENT ME!
     */
    private void buildRTree() throws TreeException {
        if (isLocal()) {
            LOGGER.info("Creating spatial index for " + shpURL.getPath());

            ShapeFileIndexer indexer = new ShapeFileIndexer();
            indexer.setIdxType(ShapeFileIndexer.RTREE);
            indexer.setShapeFileName(shpURL.getPath());

            try {
                indexer.index(false, readWriteLock);
            } catch (MalformedURLException e) {
                throw new TreeException(e);
            } catch (LockTimeoutException e) {
                throw new TreeException(e);
            } catch (Exception e) {
                File f = new File(treeURL.getPath());

                if (f.exists()) {
                    f.delete();
                }

                if (e instanceof TreeException) {
                    throw (TreeException) e;
                } else {
                    throw new TreeException(e);
                }
            }
        }
    }

    /**
     * Builds the QuadTree index
     *
     * @throws TreeException DOCUMENT ME!
     */
    private void buildQuadTree() throws TreeException {
        if (isLocal()) {
            LOGGER.info("Creating spatial index for " + shpURL.getPath());

            ShapeFileIndexer indexer = new ShapeFileIndexer();
            indexer.setIdxType(ShapeFileIndexer.QUADTREE);
            indexer.setShapeFileName(shpURL.getPath());

            try {
                indexer.index(false, readWriteLock);
            } catch (MalformedURLException e) {
                throw new TreeException(e);
            } catch (LockTimeoutException e) {
                throw new TreeException(e);
            } catch (Exception e) {
                File f = new File(treeURL.getPath());

                if (f.exists()) {
                    f.delete();
                }

                if (e instanceof TreeException) {
                    throw (TreeException) e;
                } else {
                    throw new TreeException(e);
                }
            }
        }
    }

    public boolean isMemoryMapped() {
        return useMemoryMappedBuffer;
    }

    /**
     * An AttributeReader implementation for shape. Pretty straightforward.
     * <BR/>The default geometry is at position 0, and all dbf columns follow.
     * <BR/>The dbf file may not be necessary, if not, just pass null as the
     * DbaseFileReader
     */
    protected static class Reader extends AbstractAttributeIO
        implements AttributeReader {
        private static final DataComparator dataComparator = new DataComparator();
        protected ShapefileReader shp;
        protected IndexedDbaseFileReader dbf;
        protected IndexedDbaseFileReader.Row row;
        protected ShapefileReader.Record record;
        protected List goodRecs;
        private int cnt;
        private int recno;

        /**
         * Create the shape reader
         *
         * @param atts - the attributes that we are going to read.
         * @param shp - the shape reader, required
         * @param dbf - the dbf file reader. May be null, in this case no
         *        attributes will be read from the dbf file
         * @param goodRecs DOCUMENT ME!
         */
        public Reader(AttributeType[] atts, ShapefileReader shp,
            IndexedDbaseFileReader dbf, List goodRecs) {
            super(atts);
            this.shp = shp;
            this.dbf = dbf;
            this.goodRecs = goodRecs;

            // Sort the list for forward only file reads
            if (this.goodRecs != null) {
                Collections.sort(this.goodRecs, dataComparator);
            }

            this.cnt = 0;
            this.recno = 0;
        }

        public void close() throws IOException {
            try {
                shp.close();

                if (dbf != null) {
                    dbf.close();
                }
            } finally {
                row = null;
                record = null;
                shp = null;
                dbf = null;
                goodRecs = null;
            }
        }

        public boolean hasNext() throws IOException {
            if (this.goodRecs != null) {
                return this.cnt < this.goodRecs.size();
            }

            int n = shp.hasNext() ? 1 : 0;

            if (dbf != null) {
                n += (dbf.hasNext() ? 2 : 0);
            }

            if ((n == 3) || ((n == 1) && (dbf == null))) {
                return true;
            }

            if (n == 0) {
                return false;
            }

            throw new IOException(((n == 1) ? "Shp" : "Dbf")
                + " has extra record");
        }

        public void next() throws IOException {
            if (this.goodRecs != null) {
                Data data = (Data) this.goodRecs.get(this.cnt);
                this.recno = ((Integer) data.getValue(0)).intValue();

                if (dbf != null) {
                    dbf.goTo(this.recno);
                }

                Long l = (Long) data.getValue(1);
                shp.goTo((int) l.longValue());

                this.cnt++;
            } else {
                this.recno++;
            }

            record = shp.nextRecord();

            if (dbf != null) {
                row = dbf.readRow();
            }
        }

        public int getRecordNumber() {
            return this.recno;
        }

        public Object read(int param)
            throws IOException, java.lang.ArrayIndexOutOfBoundsException {
            switch (param) {
            case 0:
                return record.shape();

            default:

                if (row != null) {
                    return row.read(param - 1);
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * A FeatureWriter for ShapefileDataStore. Uses a write and annotate
     * technique to avoid buffering attributes and geometries. Because the
     * shape and dbf require header information which can only be obtained by
     * reading the entire series of Features, the headers are updated after
     * the initial write completes.
     */
    protected class Writer implements FeatureWriter {
        // store current time here as flag for temporary write
        private long temp;

        // the FeatureReader to obtain the current Feature from
        protected FeatureReader featureReader;

        // the AttributeReader
        protected Reader attReader;

        // the current Feature
        private Feature currentFeature;

        // the FeatureType we are representing
        private FeatureType featureType;

        // an array for reuse in Feature creation
        private Object[] emptyAtts;

        // an array for reuse in writing to dbf.
        private Object[] transferCache;
        private ShapeType shapeType;
        private ShapeHandler handler;

        // keep track of shape length during write, starts at 100 bytes for
        // required header
        private int shapefileLength = 100;

        // keep track of the number of records written
        private int records = 0;

        // hold 1 if dbf should write the attribute at the index, 0 if not
        private byte[] writeFlags;
        private ShapefileWriter shpWriter;
        private DbaseFileWriter dbfWriter;
        private DbaseFileHeader dbfHeader;
        private FileChannel dbfChannel;

        // keep track of bounds during write
        private Envelope bounds = new Envelope();

        public Writer(String typeName) throws IOException {
            // set up reader
            try {
                attReader = getAttributesReader(true, null);
                featureReader = createFeatureReader(typeName, attReader, schema);
                temp = System.currentTimeMillis();
            } catch (Exception e) {
                FeatureType schema = getSchema(typeName);

                if (schema == null) {
                    throw new IOException(
                        "To create a shape, you must first call createSchema()");
                }

                featureReader = new EmptyFeatureReader(schema);
                temp = 0;
            }

            this.featureType = featureReader.getFeatureType();

            // set up buffers and write flags
            emptyAtts = new Object[featureType.getAttributeCount()];
            writeFlags = new byte[featureType.getAttributeCount()];

            int cnt = 0;

            for (int i = 0, ii = featureType.getAttributeCount(); i < ii;
                    i++) {
                // if its a geometry, we don't want to write it to the dbf...
                if (!(featureType.getAttributeType(i) instanceof GeometryAttributeType)) {
                    cnt++;
                    writeFlags[i] = (byte) 1;
                }
            }

            // dbf transfer buffer
            transferCache = new Object[cnt];

            // open underlying writers
            shpWriter = new ShapefileWriter((FileChannel) getWriteChannel(
                        getStorageURL(shpURL, temp)),
                    (FileChannel) getWriteChannel(getStorageURL(shxURL, temp)),
                    readWriteLock);

            dbfChannel = (FileChannel) getWriteChannel(getStorageURL(dbfURL, temp));
            dbfHeader = createDbaseHeader();
            dbfWriter = new DbaseFileWriter(dbfHeader, dbfChannel);

        }

        /**
         * Go back and update the headers with the required info.
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void flush() throws IOException {
            if ((records <= 0) && (shapeType == null)) {
                GeometryAttributeType geometryAttributeType = featureType
                    .getDefaultGeometry();

                Class gat = geometryAttributeType.getType();
                shapeType = JTSUtilities.getShapeType(gat);
            }

            shpWriter.writeHeaders(bounds, shapeType, records, shapefileLength);

            dbfHeader.setNumRecords(records);
            dbfChannel.position(0);
            dbfHeader.writeHeader(dbfChannel);
        }

        /**
         * Attempt to create a DbaseFileHeader for the FeatureType. Note, we
         * cannot set the number of records until the write has completed.
         *
         * @return DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         * @throws DbaseFileException DOCUMENT ME!
         */
        protected DbaseFileHeader createDbaseHeader()
            throws IOException, DbaseFileException {
            DbaseFileHeader header = new DbaseFileHeader();

            for (int i = 0, ii = featureType.getAttributeCount(); i < ii;
                    i++) {
                AttributeType type = featureType.getAttributeType(i);

                Class colType = type.getType();
                String colName = type.getName();
                int fieldLen = FeatureTypes.getFieldLength(type);

                if (fieldLen <= 0) {
                    fieldLen = 255;
                }

                // @todo respect field length
                if ((colType == Integer.class) || (colType == Short.class)
                        || (colType == Byte.class)) {
                    header.addColumn(colName, 'N', Math.min(fieldLen, 10), 0);
                } else if (colType == Long.class) {
                    header.addColumn(colName, 'N', Math.min(fieldLen, 19), 0);
                } else if ((colType == Double.class)
                        || (colType == Float.class)
                        || (colType == Number.class)) {
                    int l = Math.min(fieldLen, 33);
                    int d = Math.max(l - 2, 0);
                    header.addColumn(colName, 'N', l, d);
                } else if (java.util.Date.class.isAssignableFrom(colType)) {
                    header.addColumn(colName, 'D', fieldLen, 0);
                } else if (colType == Boolean.class) {
                    header.addColumn(colName, 'L', 1, 0);
                } else if (CharSequence.class.isAssignableFrom(colType)) {
                    // Possible fix for GEOT-42 : ArcExplorer doesn't like 0
                    // length
                    // ensure that maxLength is at least 1
                    header.addColumn(colName, 'C', Math.min(254, fieldLen), 0);
                } else if (Geometry.class.isAssignableFrom(colType)) {
                    continue;
                } else {
                    throw new IOException("Unable to write : "
                        + colType.getName());
                }
            }

            return header;
        }

        /**
         * In case someone doesn't close me.
         *
         * @throws Throwable DOCUMENT ME!
         */
        protected void finalize() throws Throwable {
            if (featureReader != null) {
                try {
                    close();
                } catch (Exception e) {
                    // oh well, we tried
                }
            }
        }

        /**
         * Clean up our temporary write if there was one
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void clean() throws IOException {
            if (temp == 0) {
                return;
            }

            copyAndDelete(shpURL, temp);
            copyAndDelete(shxURL, temp);
            copyAndDelete(dbfURL, temp);
        }


        /**
         * Release resources and flush the header information.
         *
         * @throws IOException DOCUMENT ME!
         */
        public void close() throws IOException {
            if (featureReader == null) {
                throw new IOException("Writer closed");
            }
            

            // make sure to write the last feature...
            if (currentFeature != null) {
                write();
            }

            // if the attribute reader is here, that means we may have some
            // additional tail-end file flushing to do if the Writer was closed
            // before the end of the file
            if (attReader != null) {
                shapeType = attReader.shp.getHeader().getShapeType();
                handler = shapeType.getShapeHandler();

                // handle the case where zero records have been written, but the
                // stream is closed and the headers
                if (records == 0) {
                    shpWriter.writeHeaders(bounds, shapeType, 0, 0);
                }

                // copy array for bounds
                double[] env = new double[4];

                while (attReader.hasNext()) {
                    // transfer bytes from shape
                    shapefileLength += attReader.shp.transferTo(shpWriter,
                        ++records, env);

                    // bounds update
                    bounds.expandToInclude(env[0], env[1]);
                    bounds.expandToInclude(env[2], env[3]);

                    // transfer dbf bytes
                    attReader.dbf.transferTo(dbfWriter);
                }
            }

            // close reader, flush headers, and copy temp files, if any
            try {
                featureReader.close();
            } finally {
                try {
                    flush();
                } finally {
                    shpWriter.close();
                    dbfWriter.close();
                    dbfChannel.close();
                }

                featureReader = null;
                shpWriter = null;
                dbfWriter = null;
                dbfChannel = null;
                clean();

                /*
                 * TODO This is added here for simplicity... index geometry
                 * during shp record writes
                 */
                try {
                    String filename = shpURL.getFile().substring(0,
                            shpURL.getFile().length() - 4);
                    File file = new File(filename + ".qix");

                    if (file.exists()) {
                        file.delete();
                    }

                    file = new File(filename + ".grx");

                    if (file.exists()) {
                        file.delete();
                    }

                    if (createIndex) {
                        if (treeType == TREE_GRX) {
                            buildRTree();
                            filename = shpURL.getFile().substring(0,
                                    shpURL.getFile().length() - 4);
                            File toDelete= new File(filename + ".qix");

                            if (toDelete.exists()) {
                            	toDelete.delete();
                            }
                        } else if (treeType == TREE_QIX) {
                            buildQuadTree();
                            filename = shpURL.getFile().substring(0,
                                    shpURL.getFile().length() - 4);
                            File otherIndex= new File(filename + ".grx");

                            if (otherIndex.exists()) {
                            	otherIndex.delete();
                            }
                        }
                    }
                } catch (TreeException e) {
                    LOGGER.log(Level.WARNING, "Error creating RTree", e);
                }
            }
        }

        public org.geotools.feature.FeatureType getFeatureType() {
            return featureType;
        }

        public boolean hasNext() throws IOException {
            if (featureReader == null) {
                throw new IOException("Writer closed");
            }

            return featureReader.hasNext();
        }

        public org.geotools.feature.Feature next() throws IOException {
            // closed already, error!
            if (featureReader == null) {
                throw new IOException("Writer closed");
            }

            // we have to write the current feature back into the stream
            if (currentFeature != null) {
                write();
            }

            // is there another? If so, return it
            if (featureReader.hasNext()) {
                try {
                    return currentFeature = featureReader.next();
                } catch (IllegalAttributeException iae) {
                    throw new DataSourceException("Error in reading", iae);
                }
            }

            // reader has no more (no were are adding to the file)
            // so return an empty feature
            try {
                return currentFeature = DataUtilities.template(getFeatureType(),
                        emptyAtts);
            } catch (IllegalAttributeException iae) {
                throw new DataSourceException("Error creating empty Feature",
                    iae);
            }
        }

        public void remove() throws IOException {
            if (featureReader == null) {
                throw new IOException("Writer closed");
            }

            if (currentFeature == null) {
                throw new IOException("Current feature is null");
            }

            // mark the current feature as null, this will result in it not
            // being rewritten to the stream
            currentFeature = null;
        }

        public void write() throws IOException {
            if (currentFeature == null) {
                throw new IOException("Current feature is null");
            }

            if (featureReader == null) {
                throw new IOException("Writer closed");
            }

            // writing of Geometry
            Geometry g = currentFeature.getDefaultGeometry();

            // if this is the first Geometry, find the shapeType and handler
            if (shapeType == null) {
                int dims = JTSUtilities.guessCoorinateDims(g.getCoordinates());

                try {
                    shapeType = JTSUtilities.getShapeType(g, dims);

                    // we must go back and annotate this after writing
                    shpWriter.writeHeaders(new Envelope(), shapeType, 0, 0);
                    handler = shapeType.getShapeHandler();
                } catch (ShapefileException se) {
                    throw new RuntimeException("Unexpected Error", se);
                }
            }

            // convert geometry
            g = JTSUtilities.convertToCollection(g, shapeType);

            // bounds calculations
            Envelope b = g.getEnvelopeInternal();

            if (!b.isNull()) {
                bounds.expandToInclude(b);
            }

            // file length update
            shapefileLength += (handler.getLength(g) + 8);

            // write it
            shpWriter.writeGeometry(g);

            // writing of attributes
            int idx = 0;

            for (int i = 0, ii = featureType.getAttributeCount(); i < ii;
                    i++) {
                // skip geometries
                if (writeFlags[i] > 0) {
                    transferCache[idx++] = currentFeature.getAttribute(i);
                }
            }

            dbfWriter.write(transferCache);

            // one more down...
            records++;

            // clear the currentFeature
            currentFeature = null;
        }
    }
}
