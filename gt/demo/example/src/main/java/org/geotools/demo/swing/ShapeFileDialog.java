package org.geotools.demo.swing;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class ShapeFileDialog extends JFileChooser {

    public ShapeFileDialog(){
        setFileFilter( new FileFilter(){
            public boolean accept( File f ) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }
            public String getDescription() {
                return "Shapefiles";
            }               
        });
        int returnVal = showOpenDialog( null );
    }
    public static File showOpenShapefile( Component parent ) throws HeadlessException {
        ShapeFileDialog dialog = new ShapeFileDialog();
        int returnVal = dialog.showOpenDialog(parent);
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = dialog.getSelectedFile();
        System.out.println("You chose to open this file: " + file.getName());
        return file;
    }
}