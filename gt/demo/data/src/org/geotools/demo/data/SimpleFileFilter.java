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
package org.geotools.demo.data;

import java.io.File;

import javax.swing.filechooser.FileFilter;

class SimpleFileFilter extends FileFilter {

    private String fileExtWithDot = "";
    private String desc = "";

    public SimpleFileFilter(String fileExt, String desc) {
        if (fileExt.startsWith("."))
            this.fileExtWithDot = fileExt;
        else
            this.fileExtWithDot = "." + fileExt;
	    this.desc = desc;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return(f.getName().endsWith(this.fileExtWithDot));
    }

    public String getDescription() {
        return(this.desc);
    }

}