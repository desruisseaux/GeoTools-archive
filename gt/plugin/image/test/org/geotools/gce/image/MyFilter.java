package org.geotools.gce.image; 
import java.io.File;
import java.io.FilenameFilter;

class MyFileFilter implements FilenameFilter {
    public boolean accept(File file, String name) {

        if (name.endsWith(".gif") ||
            name.endsWith(".jpg") ||
            name.endsWith(".jpeg") ||
            name.endsWith(".tif") ||
            name.endsWith(".tiff") ||
            name.endsWith(".png")) {

            return true;
        }
        return false;
    }
}