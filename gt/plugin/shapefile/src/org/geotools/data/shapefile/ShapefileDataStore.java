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
package org.geotools.data.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.AbstractFeatureLocking;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.AbstractFeatureStore;
import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultFIDReader;
import org.geotools.data.DefaultTypeEntry;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.TypeEntry;
import org.geotools.data.shapefile.dbf.DbaseFileException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.prj.PrjFileReader;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.data.shapefile.shp.xml.Metadata;
import org.geotools.data.shapefile.shp.xml.ShpXmlFileReader;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.LengthFunction;
import org.geotools.filter.LiteralExpression;
import org.geotools.geometry.JTS.ReferencedEnvelope;
import org.geotools.referencing.crs.CoordinateReferenceSystem;
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
 *
 * @todo fix file creation bug
 */
public class ShapefileDataStore extends AbstractFileDataStore {
    protected final URL shpURL;
    protected final URL dbfURL;
    protected final URL shxURL;
    protected final URL prjURL;
    protected final URL xmlURL;
    
    protected URI namespace =null; //namespace provided by the constructor's map
    protected FeatureType schema; // read only
    
    /**
     * Creates a new instance of ShapefileDataStore.
     *
     * @param url The URL of the shp file to use for this DataSource.
     *
     * @throws java.net.MalformedURLException If computation of related URLs
     *         (dbf,shx) fails.
     */
    public ShapefileDataStore(URL url)
    throws java.net.MalformedURLException {
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
        String prjext = ".prj";
        String xmlext = ".shp.xml";//yes, its a double extention.
        
        if (filename.endsWith(shpext) || filename.endsWith(dbfext)
        || filename.endsWith(shxext)) {
            filename = filename.substring(0, filename.length() - 4);
        } else if (filename.endsWith(".SHP") || filename.endsWith(".DBF")
        || filename.endsWith(".SHX")) {
            filename = filename.substring(0, filename.length() - 4);
            shpext = ".SHP";
            dbfext = ".DBF";
            shxext = ".SHX";
            prjext = ".PRJ";
            xmlext = ".SHP.XML";
        }
        
        shpURL = new URL(filename + shpext);
        dbfURL = new URL(filename + dbfext);
        shxURL = new URL(filename + shxext);
        prjURL = new URL(filename + prjext);
        xmlURL = new URL(filename + xmlext);
        
    }
    
    /**
     *   this sets the datastore's namespace during construction
     *   (so the schema - FeatureType - will have the correct value)
     *   You can call this with namespace = null, but I suggest you give it an actual namespace.
     * @param url
     * @param namespace
     * @throws java.net.MalformedURLException
     */
    public ShapefileDataStore(URL url,URI namespace) throws java.net.MalformedURLException
    {
       	this(url);
    	this.namespace = namespace;
    }
    
    /**
     * Create our own TypeEntry that will calculate BBox based on
     * available metadata.
     */
    protected TypeEntry createTypeEntry( final String typeName ) {
        URI namespace;
        try {
            namespace = getSchema( typeName ).getNamespace();
        } catch (IOException e) {
            namespace = null;
        }
        return new DefaultTypeEntry( this, namespace, typeName ) {
            /** Use ShapefileDataStore createMetadata method */
            protected Map createMetadata() {
                return ShapefileDataStore.this.createMetadata( typeName );
            }
            /**
             * Grab bounds from metadata, if possible.
             *
             * @return geographic bounding box
             */
            protected Envelope createBounds() {
                Envelope bbox = null;
                Metadata meta = (Metadata) metadata().get( "shp.xml" );
                if( meta != null ) {
                    bbox = meta.getIdinfo().getLbounding();
                    if( bbox != null ) {
                        return bbox;
                    }
                    bbox = meta.getIdinfo().getBounding();
                    // we would need to reproject this :-P
                    // so lets not bother right now ...
                }
                return super.createBounds();
            }
        };
    }
    /**
     * Latch onto xmlURL if it is there, we may be able to get out of
     * calculating the bounding box!
     * <p>
     * This method is called by the createTypeEntry
     * anonymous inner class DefaultTypeEntry.
     * </p>
     * @return Map with xmlURL parsed, or an EMPTY_MAP.
     */
    protected Map createMetadata( String typeName ) {
        if( xmlURL == null ) {
            return Collections.EMPTY_MAP;
        }
        try {
            System.out.println("found metadata = " + xmlURL );
            ShpXmlFileReader reader = new ShpXmlFileReader( xmlURL );
            
            Map map = new HashMap();
            map.put( "shp.xml", reader.parse() );
            System.out.println("parsed ..." + xmlURL );
            return map;
        }
        catch (Throwable t ) {
            LOGGER.warning("Could not parse "+xmlURL+":"+t.getLocalizedMessage() );
            return Collections.EMPTY_MAP;
        }
    }
    
    /**
     * Determine if the location of this shapefile is local or remote.
     *
     * @return true if local, false if remote
     */
    public boolean isLocal() {
        return shpURL.getProtocol().equals("file");
    }
    
    /**
     * Delete existing files.
     */
    private void clear() {
        if (isLocal()) {
            delete(shpURL);
            delete(dbfURL);
            delete(shxURL);
            delete(prjURL);
            delete(xmlURL);
        }
    }
    
    /**
     * Delete a URL (file)
     *
     * @param u DOCUMENT ME!
     */
    private void delete(URL u) {
        File f = new File(u.getFile());
        f.delete();
    }
    
    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will
     * be obtained from the urls input stream.
     *
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private ReadableByteChannel getReadChannel(URL url)
    throws IOException {
        ReadableByteChannel channel = null;

        if (url.getProtocol().equals("file")){// && useMemoryMappedBuffer) {
            File file = null;
            if(url.getHost()!=null && !url.getHost().equals("")) {
                //win
                file = new File(url.getHost()+":"+url.getFile());
            }else {
                //linux
                file = new File(url.getFile());
            }

			if (!file.exists() || !file.canRead()) {
			    throw new IOException(
			            "File either doesn't exist or is unreadable : " + file);
			}                        
			FileInputStream in = new FileInputStream(file);            
			channel = in.getChannel();
			
        } else {
            InputStream in = url.openConnection().getInputStream();
            channel = Channels.newChannel(in);
        }
        
        return channel;
    }
    
    /**
     * Obtain a WritableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Currently, this method will
     * return a generic channel for remote urls, however both shape and dbf
     * writing can only occur with a local FileChannel channel.
     *
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected WritableByteChannel getWriteChannel(URL url)
    throws IOException {
        WritableByteChannel channel;

        if (url.getProtocol().equals("file")){// && useMemoryMappedBuffer) {
            File file = null;
            if(url.getHost()!=null && !url.getHost().equals("")) {
                //win
                file = new File(url.getHost()+":"+url.getFile());
            }else {
                //linux
                file = new File(url.getFile());
            }
            
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
        } else {
            OutputStream out = url.openConnection().getOutputStream();
            channel = Channels.newChannel(out);
        }
        
        return channel;
    }
    
    /**
     * Create a FeatureReader for the provided type name.
     *
     * @param typeName The name of the FeatureType to create a reader for.
     *
     * @return A new FeatureReader.
     *
     * @throws IOException If an error occurs during creation
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName)
    throws IOException {
        typeCheck(typeName);
        
        return getFeatureReader();
    }
    protected FeatureReader getFeatureReader()
    throws IOException {
        
        try {
            return createFeatureReader(getSchema().getTypeName(), getAttributesReader(true),
            schema);
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema", se);
        }
    }
    
    /**
     * Just like the basic version, but adds a small optimization: if no attributes
     * are going to be read, don't uselessly open and read the dbf file.
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String, org.geotools.data.Query)
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
    throws IOException {
        String[] propertyNames = query.getPropertyNames();
        String defaultGeomName = schema.getDefaultGeometry().getName();
        
        if ((propertyNames != null) && (propertyNames.length == 1)
        && propertyNames[0].equals(defaultGeomName)) {
            try {
                FeatureType newSchema = DataUtilities.createSubType(schema,
                propertyNames);
                
                return createFeatureReader(typeName,
                getAttributesReader(false), newSchema);
            } catch (SchemaException se) {
                throw new DataSourceException("Error creating schema", se);
            }
        }
        return super.getFeatureReader(typeName, query);
    }
    
    protected FeatureReader createFeatureReader(String typeName, Reader r,
    FeatureType readerSchema) throws SchemaException{
        return new org.geotools.data.FIDFeatureReader(r,
        new DefaultFIDReader(typeName), readerSchema);
    }
    
    /**
     * Returns the attribute reader, allowing for a pure shapefile reader, or
     * a combined dbf/shp reader.
     * @param readDbf - if true, the dbf fill will be opened and read
     * @return
     * @throws IOException
     */
    protected Reader getAttributesReader(boolean readDbf)
    throws IOException {
        AttributeType[] atts = (schema == null) ? readAttributes()
        : schema.getAttributeTypes();
        
        if (!readDbf) {
            LOGGER.fine("The DBF file won't be opened since no attributes will be read from it");
            atts = new AttributeType[] { schema.getDefaultGeometry() };
            
            return new Reader(atts, openShapeReader(), null);
        }
        return new Reader(atts, openShapeReader(), openDbfReader());
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
            return new ShapefileReader(rbc, true);
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
        
        return new DbaseFileReader(rbc);
    }
    
    /**
     * Convenience method for opening a DbaseFileReader.
     *
     * @return A new DbaseFileReader
     *
     * @throws IOException If an error occurs during creation.
     */
    protected PrjFileReader openPrjReader() throws IOException, FactoryException {
        ReadableByteChannel rbc = null;
        try{
            rbc = getReadChannel(prjURL);
        }
        catch(IOException e){
            LOGGER.warning("projection (.prj) for shapefile not available");
        }
        if (rbc == null) {
            return null;
        }
        
        return new PrjFileReader(rbc);
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
     *
     * @return A new FeatureWriter.
     *
     * @throws IOException If the typeName is not available or some other error
     *         occurs.
     */
    protected FeatureWriter getFeatureWriter(String typeName)
    throws IOException {
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
     * @throws DataSourceException DOCUMENT ME!
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
                if(geomType == Point.class || geomType == MultiPoint.class){
                    parent = BasicFeatureTypes.POINT;
                }
                else if(geomType == Polygon.class || geomType == MultiPolygon.class){
                    parent = BasicFeatureTypes.POLYGON;
                }
                else if(geomType == LineString.class || geomType == MultiLineString.class){
                    parent = BasicFeatureTypes.LINE;
                }
                if(parent != null){
                    schema = FeatureTypeFactory.newFeatureType(readAttributes(),
                    createFeatureTypeName(), namespace, false, new FeatureType[] {parent});
                }
                else {
                	if (namespace != null)
                	{
                		schema = FeatureTypeFactory.newFeatureType(readAttributes(),
                				createFeatureTypeName(),namespace,false);
                	}
                	else
                	{
                		schema = FeatureTypeFactory.newFeatureType(readAttributes(), createFeatureTypeName(), GMLSchema.NAMESPACE, false);
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
        DbaseFileReader dbf = openDbfReader();
        CoordinateReferenceSystem cs = null;
        try{
            PrjFileReader prj = openPrjReader();
            if(prj!=null){
                cs = (CoordinateReferenceSystem) prj.getCoodinateSystem();
            }
        }
        catch(FactoryException fe){
            cs = null;
        }
        try {
            
            GeometryAttributeType geometryAttribute = (GeometryAttributeType)AttributeTypeFactory
            .newAttributeType("the_geom",
            JTSUtilities.findBestGeometryClass(
            shp.getHeader().getShapeType()),true, 0, null, cs);
            
            AttributeType[] atts;
            
            // take care of the case where no dbf and query wants all => geometry only
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
     * Set the FeatureType of this DataStore. This method will delete any
     * existing local resources or throw an IOException if the DataStore is
     * remote.
     *
     * @param featureType The desired FeatureType.
     *
     * @throws IOException If the DataStore is remote.
     */
    public void createSchema(FeatureType featureType) throws IOException {
        if (!isLocal()) {
            throw new IOException(
            "Cannot create FeatureType on remote shapefile");
        }
        
        clear();
        schema = featureType;
    }
    
    /**
     * Gets the bounding box of the file represented by this data store as a
     * whole (that is, off all of the features in the shapefile)
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
            
            Envelope env = new Envelope(header.minX(), header.maxX(), header.minY(),
                    header.maxY());
            if(schema!=null)
                return new ReferencedEnvelope(env,schema.getDefaultGeometry().getCoordinateSystem());
            return new ReferencedEnvelope(env,null);
        } catch (IOException ioe) {
            // What now? This seems arbitrarily appropriate !
            throw new DataSourceException("Problem getting Bbox", ioe);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }
    
    protected Envelope getBounds(Query query) throws IOException {
        if (query.getFilter().equals(Filter.NONE) ) {
            return getBounds();
        }
        return null; // too expensive
        // TODO should we just return the layer? matches the javadocs
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
                        return ShapefileDataStore.this;
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
                        return ShapefileDataStore.this.getBounds(query);
                    }
                };
            }
                return new AbstractFeatureStore() {
                    public DataStore getDataStore() {
                        return ShapefileDataStore.this;
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
                        return ShapefileDataStore.this.getBounds(query);
                    }
                };
        }
            return new AbstractFeatureSource() {
                public DataStore getDataStore() {
                    return ShapefileDataStore.this;
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
                    return ShapefileDataStore.this.getBounds(query);
                }
            };
    }
    
    /**
     * An AttributeReader implementation for Shapefile. Pretty straightforward.
     * <BR/>The default geometry is at position 0, and all dbf columns follow.
     * <BR/>The dbf file may not be necessary, if not, just pass null as the DbaseFileReader
     */
    protected static class Reader extends AbstractAttributeIO
    implements AttributeReader {
        protected ShapefileReader shp;
        protected DbaseFileReader dbf;
        protected DbaseFileReader.Row row;
        protected ShapefileReader.Record record;
        int cnt;
        
        /**
         * Create the shapefile reader
         * @param atts - the attributes that we are going to read.
         * @param shp - the shapefile reader, required
         * @param dbf - the dbf file reader. May be null, in this case no attributes
         *              will be read from the dbf file
         */
        public Reader(AttributeType[] atts, ShapefileReader shp,
        DbaseFileReader dbf) {
            super(atts);
            this.shp = shp;
            this.dbf = dbf;
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
            }
        }
        
        public boolean hasNext() throws IOException {
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
            record = shp.nextRecord();
            
            if (dbf != null) {
                row = dbf.readRow();
            }
        }
        
        public Object read(int param)
        throws IOException, java.lang.ArrayIndexOutOfBoundsException {
            switch (param) {
                case 0:
                    return record.shape();
                    
                default:
                    
                    if (row != null) {
                        return row.read(param - 1);
                    }
                    return null;
            }
        }
    }
    
    /**
     * A FeatureWriter for ShapefileDataStore. Uses a write and annotate
     * technique to avoid buffering attributes and geometries. Because the
     * shapefile and dbf require header information which can only be obtained
     * by reading the entire series of Features, the headers are updated after
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
        
        // keep track of shapefile length during write, starts at 100 bytes for
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
                attReader = getAttributesReader(true);
                featureReader = createFeatureReader(typeName, attReader, schema);
                temp = System.currentTimeMillis();
            } catch (Exception e) {
                getSchema(); // load it
                
                if (schema == null) {
                    throw new IOException(
                    "To create a shapefile, you must first call createSchema()");
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
                if (!featureType.getAttributeType(i).isGeometry()) {
                    cnt++;
                    writeFlags[i] = (byte) 1;
                }
            }
            
            // dbf transfer buffer
            transferCache = new Object[cnt];
            
            // open underlying writers
            shpWriter = new ShapefileWriter((FileChannel) getWriteChannel(
            getStorageURL(shpURL)),
            (FileChannel) getWriteChannel(getStorageURL(shxURL)));
            
            dbfChannel = (FileChannel) getWriteChannel(getStorageURL(dbfURL));
            dbfHeader = createDbaseHeader();
            dbfWriter = new DbaseFileWriter(dbfHeader, dbfChannel);
        }
        
        /**
         * Get a temporary URL for storage based on the one passed in
         *
         * @param url DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws java.net.MalformedURLException DOCUMENT ME!
         */
        protected URL getStorageURL(URL url)
        throws java.net.MalformedURLException {
            return (temp == 0) ? url : getStorageFile(url).toURL();
        }
        
        /**
         * Get a temproray File based on the URL passed in
         *
         * @param url DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        protected File getStorageFile(URL url) {
            String f = url.getFile();
            f = temp + f.substring(f.lastIndexOf("/") + 1);
            
            File tf = new File(System.getProperty("java.io.tmpdir"), f);
            
            return tf;
        }
        
        /**
         * Go back and update the headers with the required info.
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void flush() throws IOException {
            //not sure the check for records <=0 is necessary,
            //but if records > 0 and shapeType is null there's probably
            //another problem.
            if(records <= 0 && shapeType == null)
            {
                GeometryAttributeType geometryAttributeType = featureType.getDefaultGeometry();
                                                                                
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

            	int fieldLen = -1;
            	Filter f = type.getRestriction();
            	if(f !=null && f!=Filter.ALL && f != Filter.NONE && (f.getFilterType() == f.COMPARE_LESS_THAN || f.getFilterType() == f.COMPARE_LESS_THAN_EQUAL)){
            		try{
            		CompareFilter cf = (CompareFilter)f;
            		if(cf.getLeftValue() instanceof LengthFunction){
            			fieldLen = Integer.parseInt(((LiteralExpression)cf.getRightValue()).getLiteral().toString());
            		}else{
            			if(cf.getRightValue() instanceof LengthFunction){
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
                    // Possible fix for GEOT-42 : ArcExplorer doesn't like 0 length
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
            
            copyAndDelete(shpURL);
            copyAndDelete(shxURL);
            copyAndDelete(dbfURL);
        }
        
        /**
         * Copy the file at the given URL to the original
         *
         * @param src DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void copyAndDelete(URL src) throws IOException {
            File storage = getStorageFile(src);
            File dest = new File(src.getFile());
            FileChannel in = null;
            FileChannel out = null;
            try {
                in = new FileInputStream(storage).getChannel();
                out = new FileOutputStream(dest).getChannel();
                long len = in.size();
                long copied = out.transferFrom(in, 0, in.size());
                
                if (len != copied) {
                    throw new IOException("unable to complete write");
                }
                
                storage.delete();
            } finally {
                if( in != null ) in.close();
                if( out != null ) out.close();
            }
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
                    // transfer bytes from shapefile
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
