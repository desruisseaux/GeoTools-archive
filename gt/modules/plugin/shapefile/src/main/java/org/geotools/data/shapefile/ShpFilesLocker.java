/**
 * 
 */
package org.geotools.data.shapefile;

import java.net.URL;

class ShpFilesLocker {
    final URL url;
    final FileReader reader;
    final FileWriter writer;

    public ShpFilesLocker(URL url, FileReader reader) {
        this.url = url;
        this.reader = reader;
        this.writer = null;
        ShapefileDataStoreFactory.LOGGER.fine("Read lock: " + url + " by "
                + reader.id());
//        new Exception("Locking "+url+" for read by "+reader+" in thread "+threadName).printStackTrace();
    }

    public ShpFilesLocker(URL url, FileWriter writer) {
        this.url = url;
        this.reader = null;
        this.writer = writer;
        ShapefileDataStoreFactory.LOGGER.fine("Write lock: " + url + " by "
                + writer.id());
//        new Exception("Locking "+url+" for write by "+writer+" in thread "+threadName).printStackTrace();
    }

    /**
     * Verifies that the url and requestor are the same as the url and the
     * reader or writer of this class. assertions are used so this will do
     * nothing if assertions are not enabled.
     */
    public void compare(URL url2, Object requestor) {
        URL url = this.url;
        assert (url2 == url) : "Expected: " + url + " but got: " + url2;
        assert (reader == null || requestor == reader) : "Expected the requestor and the reader to be the same object: "
                + reader.id();
        assert (writer == null || requestor == writer) : "Expected the requestor and the writer to be the same object: "
                + writer.id();
    }

    @Override
    public String toString() {
        if (reader != null) {
            return "read on " + url + " by " + reader.id();
        } else {
            return "write on " + url + " by " + writer.id();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reader == null) ? 0 : reader.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((writer == null) ? 0 : writer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ShpFilesLocker other = (ShpFilesLocker) obj;
        if (reader == null) {
            if (other.reader != null)
                return false;
        } else if (!reader.equals(other.reader))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        if (writer == null) {
            if (other.writer != null)
                return false;
        } else if (!writer.equals(other.writer))
            return false;
        return true;
    }

}