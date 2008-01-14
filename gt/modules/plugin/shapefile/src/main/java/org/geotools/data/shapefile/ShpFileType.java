/**
 * 
 */
package org.geotools.data.shapefile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Enumerates the known types of files associated with a shapefile.
 * 
 * @author jesse
 */
public enum ShpFileType {

    SHP("shp"), DBF("dbf"), SHX("shx"), PRJ("prj"), QIX("qix"), FIX("fix"), SHP_XML(
            "shp.xml"), GRX("grx");

    public final String extension;
    public final String extensionWithPeriod;

    private ShpFileType(String extension) {
        this.extension = extension.toLowerCase();
        this.extensionWithPeriod = "." + this.extension;
    }

    /**
     * Returns the base of the file or null if the file passed in is not of the
     * correct type (has the correct extension.)
     * <p>
     * For example if the file is c:\shapefiles\file1.dbf. The DBF type will
     * return c:\shapefiles\file1 but all other will return null.
     */
    public String toBase(File file) {
        String path = file.getPath();
        return toBase(path);
    }

    /**
     * Returns the base of the file or null if the file passed in is not of the
     * correct type (has the correct extension.)
     * <p>
     * For example if the file is c:\shapefiles\file1.dbf. The DBF type will
     * return c:\shapefiles\file1 but all other will return null.
     */
    public String toBase(String path) {
        if (!path.toLowerCase().endsWith(extensionWithPeriod)
                || path.equalsIgnoreCase(extensionWithPeriod)) {
            return null;
        }

        int indexOfExtension = path.toLowerCase().lastIndexOf(
                extensionWithPeriod);
        return path.substring(0, indexOfExtension);
    }

    /**
     * Returns the base of the file or null if the file passed in is not of the
     * correct type (has the correct extension.)
     * <p>
     * For example if the file is c:\shapefiles\file1.dbf. The DBF type will
     * return c:\shapefiles\file1 but all other will return null.
     */
    public String toBase(URL url) {
        try {
            return toBase(java.net.URLDecoder.decode(url.toExternalForm(),
                    "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            return toBase(url.toExternalForm());
        }
    }
}
