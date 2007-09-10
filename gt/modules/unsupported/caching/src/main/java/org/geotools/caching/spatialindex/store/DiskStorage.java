/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.spatialindex.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.SpatialIndex;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.grid.GridNode;


public class DiskStorage implements Storage {
    public final static String DATA_FILE_PROPERTY = "DiskStorage.DataFile";
    public final static String INDEX_FILE_PROPERTY = "DiskStorage.IndexFile";
    public final static String PAGE_SIZE_PROPERTY = "DiskStorage.PageSize";
    protected static Logger logger = Logger.getLogger("org.geotools.caching.spatialindex.store");
    int stats_bytes = 0;
    int stats_n = 0;
    private RandomAccessFile data_file;
    private FileChannel data_channel;
    private File dataFile;
    private File indexFile;
    private int page_size;
    private int nextPage = 0;
    private TreeSet<Integer> emptyPages;
    private HashMap<NodeIdentifier, Entry> pageIndex;

    //    private byte[] buffer;
    //    private ByteBuffer buffer;
    protected SpatialIndex parent;

    private DiskStorage(File f, int page_size) throws IOException {
        this(f, page_size, new File(f.getCanonicalPath() + ".idx"));
    }

    private DiskStorage(File f, int page_size, File index_file)
        throws IOException {
        dataFile = f;
        data_file = new RandomAccessFile(f, "rw");
        data_channel = data_file.getChannel();
        this.indexFile = index_file;
        this.page_size = page_size;
        emptyPages = new TreeSet<Integer>();
        pageIndex = new HashMap<NodeIdentifier, Entry>();

        //        buffer = new byte[page_size];
        //        buffer = ByteBuffer.allocate(page_size);
    }

    private DiskStorage(File f, File index_file) throws IOException {
        dataFile = f;
        data_file = new RandomAccessFile(f, "rw");
        data_channel = data_file.getChannel();
        this.indexFile = index_file;

        if (index_file.exists()) {
            initializeFromIndex();

            //            buffer = new byte[page_size];
            //            buffer = ByteBuffer.allocate(page_size);
        } else {
            throw new IOException(index_file + " does not exist !");
        }
    }

    /** Factory method : create a new Storage of type DiskStorage.
     * Valid properties are :
     * <ul>
     *   <li>DiskStorage.DATA_FILE_PROPERTY : filename (mandatory) ; overrides given file if index is not provided.
     *   <li>DiskStorage.INDEX_FILE_PROPERTY : filename ;
     *                                         if exists, must be a valid index file
     *                                         and data file must be the valid data file associated with this index.
     *   <li>DiskStorage.PAGE_SIZE_PROPERTY : int, required if INDEX_FILE does not exist, or is not provided.
     * </ul>
     * @param property set
     * @return new instance of DiskStorage
     */
    public static Storage createInstance(Properties pset) {
        try {
            File f = new File(pset.getProperty(DATA_FILE_PROPERTY));

            if (pset.containsKey(INDEX_FILE_PROPERTY)) {
                File index = new File(pset.getProperty(INDEX_FILE_PROPERTY));

                if (index.exists()) {
                    return new DiskStorage(f, index);
                } else {
                    int page_size = Integer.parseInt(pset.getProperty(PAGE_SIZE_PROPERTY));

                    return new DiskStorage(f, page_size, index);
                }
            } else {
                int page_size = Integer.parseInt(pset.getProperty(PAGE_SIZE_PROPERTY));

                return new DiskStorage(f, page_size);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING,
                "DiskStorage : error occured when creating new instance : " + e);

            return null;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("DiskStorage : invalid property set.");
        }
    }

    /** Default factory method : create a new Storage of type DiskStorage,
     * with page size set to default 1000 bytes, and data file is a new temporary file.
     *
     * @return new instance of DiskStorage with default parameters.
     */
    public static Storage createInstance() {
        try {
            return new DiskStorage(File.createTempFile("storage", ".tmp"), 1000);
        } catch (IOException e) {
            logger.log(Level.WARNING,
                "DiskStorage : error occured when creating new instance : " + e);

            return null;
        }
    }

    public void setParent(SpatialIndex parent) {
        this.parent = parent;
    }

    public void clear() {
        for (Iterator<java.util.Map.Entry<NodeIdentifier, Entry>> it = pageIndex.entrySet()
                                                                                .iterator();
                it.hasNext();) {
            java.util.Map.Entry<NodeIdentifier, Entry> next = it.next();
            Entry e = next.getValue();

            synchronized (next.getKey()) {
                int n = 0;

                while (n < e.pages.size()) {
                    emptyPages.add(e.pages.get(n));
                    n++;
                }

                it.remove();
            }
        }
    }

    public Node get(NodeIdentifier id) {
        Node node = null;
        byte[] data;
        Entry e;
        ByteBuffer buffer = ByteBuffer.allocate(page_size);

        synchronized (findUniqueInstance(id)) {
            //        	try {
            //				logGet();
            //			} catch (IOException e1) {
            //				// TODO Auto-generated catch block
            //				e1.printStackTrace();
            //			}
            e = pageIndex.get(id);

            if (e == null) {
                return null;
            }

            data = new byte[e.length];

            int page = 0;
            int rem = data.length;
            int len = 0;
            int next = 0;
            int index = 0;

            while (next < e.pages.size()) {
                page = e.pages.get(next);
                len = (rem > page_size) ? page_size : rem;

                try {
                    //        			data_file.seek(page * page_size);
                    data_channel.position(page * page_size);

                    //        			int bytes_read = data_file.read(buffer);
                    buffer.position(0);

                    int bytes_read = data_channel.read(buffer);

                    if (bytes_read != page_size) {
                        System.out.println(bytes_read);
                        throw new IllegalStateException("Data file might be corrupted.");
                    }

                    //        			System.arraycopy(buffer.array(), 0, data, index, len);
                    buffer.position(0);
                    buffer.get(data, index, len);
                    rem -= bytes_read;
                    index += bytes_read;
                    next++;
                } catch (IOException io) {
                    throw new IllegalStateException(io);
                }
            }
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            node = (Node) ois.readObject();
            node.init(parent);
            ois.close();
            bais.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NullPointerException e1) {
            System.out.println(Thread.currentThread().getName() + " : fatal exception at "
                + System.currentTimeMillis());

            Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();

            for (Iterator<java.util.Map.Entry<Thread, StackTraceElement[]>> it = m.entrySet()
                                                                                  .iterator();
                    it.hasNext();) {
                java.util.Map.Entry<Thread, StackTraceElement[]> entry = it.next();
                System.out.println(entry.getKey().getName());

                for (int i = 0; i < entry.getValue().length; i++) {
                    System.out.println("   " + entry.getValue()[i]);
                }
            }

            System.out.println(e);
            System.out.println(pageIndex.get(id));
            //Runtime.getRuntime().exit(1);
            e1.printStackTrace();
            // find pages that may been used by other
            System.err.println("Involved NodeIdentifier : " + id);

            if (e.pages.isEmpty()) {
                System.err.println("No pages in current entry");
            }

            for (Iterator<Integer> it = e.pages.iterator(); it.hasNext();) {
                Integer p = it.next();

                for (Iterator<Entry> itpi = pageIndex.values().iterator(); itpi.hasNext();) {
                    Entry n = itpi.next();

                    if (n != e) {
                        for (Iterator<Integer> itn = n.pages.iterator(); itn.hasNext();) {
                            if (p.equals(itn.next())) {
                                System.err.println("Page " + p + " used by other entry : " + n);
                            }
                        }
                    }
                }

                for (Iterator<Integer> itep = emptyPages.iterator(); itep.hasNext();) {
                    if (p.equals(itep.next())) {
                        System.err.println("Page " + p + " in recycle bin");
                    }
                }
            }

            Runtime.getRuntime().exit(1);
        }

        return node;
    }

    public void put(Node n) {
        byte[] data = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(n);
            data = baos.toByteArray();
            stats_bytes += data.length;
            stats_n++;
            oos.close();
            baos.close();
        } catch (IOException e1) {
            logger.log(Level.SEVERE, "Cannot put data in DiskStorage : " + e1);

            return;
        }

        Entry e = new Entry(n.getIdentifier());
        Entry old = null;

        if (pageIndex.containsKey(e.id)) {
            old = pageIndex.get(e.id);

            if (old == null) {
                // problem
                throw new IllegalStateException("old entry null");
            }
        } else {
            synchronized (pageIndex) {
                if (!pageIndex.containsKey(e.id)) {
                    pageIndex.put(e.id, null); // advertise we created a new entry
                } else {
                    old = pageIndex.get(e.id);
                }
            }
        }

        synchronized (e.id) {
            write(data, e, old);
            //        	writeReadable(n, e.pages.get(0));
            pageIndex.put(e.id, e);
        }
    }

    void write(byte[] data, Entry e, Entry old) {
        ByteBuffer buffer = ByteBuffer.allocate(page_size);
        e.length = data.length;

        int rem = data.length;
        int page;
        int len;
        int index = 0;
        int next = 0;

        FileLock fl;

        try {
            fl = data_channel.lock();

            while (rem > 0) {
                if ((old != null) && (next < old.pages.size())) {
                    page = old.pages.get(next);
                    next++;
                } else if (!emptyPages.isEmpty()) {
                    synchronized (emptyPages) {
                        Integer i = emptyPages.first();
                        page = i.intValue();

                        if (!emptyPages.remove(i)) {
                            throw new RuntimeException("buggy here !!!!");
                        }
                    }
                } else {
                    page = nextPage++;
                }

                len = (rem > page_size) ? page_size : rem;
                buffer.position(0);
                buffer.put(data, index, len);

                //      		System.arraycopy(data, index, buffer, 0, len);
                try {
                    //        			logPageAccess(page, len);
                    //      			data_file.seek(page * page_size);
                    data_channel.position(page * page_size);
                    //      			data_file.write(buffer);
                    buffer.position(0);
                    data_channel.write(buffer);
                } catch (IOException io) {
                    // TODO
                    throw new IllegalStateException(io);
                }

                rem -= len;
                index += len;
                e.pages.add(new Integer(page));
            }

            fl.release();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (old != null) { // don't forget to recycle pages

            while (next < old.pages.size()) {
                emptyPages.add(new Integer(old.pages.get(next)));
                next++;
            }
        }
    }

    public void remove(NodeIdentifier id) {
        Entry e = pageIndex.get(id);

        if (e == null) {
            // problem
            throw new IllegalArgumentException("Invalid identifier " + id.toString());
        }

        synchronized (findUniqueInstance(id)) {
            int next = 0;

            while (next < e.pages.size()) {
                emptyPages.add(new Integer(e.pages.get(next)));
                next++;
            }

            pageIndex.remove(id);
        }
    }

    public void flush() {
        try {
            FileOutputStream os = new FileOutputStream(indexFile);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeInt(this.page_size);
            oos.writeInt(this.nextPage);
            oos.writeObject(this.emptyPages);
            oos.writeObject(this.pageIndex);
            oos.close();
            os.close();
            data_channel.close();
            data_file.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot close DiskStorage normally : " + e);
        }
    }

    protected void initializeFromIndex() throws IOException {
        FileInputStream is = new FileInputStream(indexFile);
        ObjectInputStream ois = new ObjectInputStream(is);
        this.page_size = ois.readInt();
        this.nextPage = ois.readInt();

        try {
            this.emptyPages = (TreeSet<Integer>) ois.readObject();
            this.pageIndex = (HashMap<NodeIdentifier, Entry>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException().initCause(e);
        } finally {
            ois.close();
            is.close();
        }
    }

    public Properties getPropertySet() {
        Properties pset = new Properties();

        try {
            pset.setProperty(STORAGE_TYPE_PROPERTY, DiskStorage.class.getCanonicalName());
            pset.setProperty(DATA_FILE_PROPERTY, dataFile.getCanonicalPath());
            pset.setProperty(INDEX_FILE_PROPERTY, indexFile.getCanonicalPath());
            pset.setProperty(PAGE_SIZE_PROPERTY, new Integer(page_size).toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while creating DiskStorage property set : " + e);
        }

        return pset;
    }

    public NodeIdentifier findUniqueInstance(NodeIdentifier id) {
        if (pageIndex.containsKey(id)) {
            return pageIndex.get(id).id;
        } else {
            return id;
        }
    }

    void logPageAccess(int page, int length) throws IOException {
        File log = new File("log/" + page + ".log");
        FileWriter fw = new FileWriter(log, true);
        fw.write(System.currentTimeMillis() + " : " + Thread.currentThread().getName()
            + " writing " + length + " bytes.\n");
        fw.close();
    }

    void logGet() throws IOException {
        FileWriter getlog = new FileWriter("log/get.log", true);
        getlog.write(Thread.currentThread().getName() + " : " + System.currentTimeMillis() + "\n");
        getlog.close();
    }

    void writeReadable(Node n, int page) {
        try {
            FileWriter fw = new FileWriter("log/" + page + ".node");
            fw.write(((GridNode) n).toReadableText());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class Entry implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -9013786524696213884L;
    int length = 0;
    NodeIdentifier id;
    ArrayList<Integer> pages = new ArrayList<Integer>();

    Entry(NodeIdentifier id) {
        this.id = id;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Id : " + id);
        sb.append(", Length : " + length);

        for (Iterator<Integer> it = pages.iterator(); it.hasNext();) {
            sb.append("\n    page = " + it.next());
        }

        return sb.toString();
    }
}
