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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.ifc.VPFLibraryIfc;
import org.geotools.data.vpf.io.TableInputStream;
import org.geotools.data.vpf.io.TableRow;


/**
 * Class <code>VPFDataBase</code> is responsible for 
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version $Id: VPFDataBase.java,v 1.1 2004/05/03 11:48:20 knutejoh Exp $
 */
public class VPFDataBase implements FileConstants {
    private VPFLibrary[] libraries = null;
    private TableRow[][] coverages = null;
    private HashMap tilingSchema = null;

    public VPFDataBase(File directory) throws IOException, Exception {
        // read data base header info
        //this.directory = directory;
        String vpfTableName = new File(directory, DATABASE_HEADER_TABLE).toString();
        TableInputStream vpfTable = new TableInputStream(vpfTableName);
        TableRow dataBaseInfo = (TableRow) vpfTable.readRow();
        vpfTable.close();


        // read libraries info
        vpfTableName = new File(directory, LIBRARY_ATTTIBUTE_TABLE).toString();
        vpfTable = new TableInputStream(vpfTableName);

        List list = vpfTable.readAllRows();
        vpfTable.close();

        TableRow[] libraries_tmp = (TableRow[]) list.toArray(new TableRow[list.size()]);
        libraries = new VPFLibrary[libraries_tmp.length];

        for (int i = 0; i < libraries_tmp.length; i++) {
            libraries[i] = new VPFLibrary(libraries_tmp[i], directory, this);
        }
    }

    public VPFCoverage[] getCoverages() {
        ArrayList arr = new ArrayList();
        VPFCoverage[] tmp = null;

        for (int i = 0; i < libraries.length; i++) {
            tmp = libraries[i].getCoverages();

            for (int j = 0; j < tmp.length; j++) {
                arr.add(tmp[j]);
            }
        }

        return (VPFCoverage[]) arr.toArray(tmp);
    }

    public void setTilingSchema(HashMap schema) {
        tilingSchema = schema;
    }

    public HashMap getTilingSchema() {
        return tilingSchema;
    }

    public VPFFeatureClass[] getFeatureClasses() {
        ArrayList arr = new ArrayList();
        VPFFeatureClass[] tmp = null;

        for (int i = 0; i < libraries.length; i++) {
            tmp = libraries[i].getFeatureClasses();

            if (tmp != null) {
                for (int j = 0; j < tmp.length; j++) {
                    arr.add(tmp[j]);
                }
            }
        }

        return (VPFFeatureClass[]) arr.toArray(tmp);
    }

    public VPFLibrary[] getLibraries() {
        return libraries;
    }

    public double getMinX() {
        if (libraries.length > 0) {
            double xmin = libraries[0].getXmin();

            for (int i = 1; i < libraries.length; i++) {
                xmin = Math.min(xmin, libraries[i].getXmin());
            }

            return xmin;
        }

        return 0d;
    }

    public double getMinY() {
        if (libraries.length > 0) {
            double ymin = libraries[0].getYmin();

            for (int i = 1; i < libraries.length; i++) {
                ymin = Math.min(ymin, libraries[i].getYmin());
            }

            return ymin;
        }

        return 0d;
    }

    public double getMaxX() {
        if (libraries.length > 0) {
            double xmax = libraries[0].getXmax();

            for (int i = 1; i < libraries.length; i++) {
                xmax = Math.min(xmax, libraries[i].getXmax());
            }

            return xmax;
        }

        return 0d;
    }

    public double getMaxY() {
        if (libraries.length > 0) {
            double ymax = libraries[0].getYmax();

            for (int i = 1; i < libraries.length; i++) {
                ymax = Math.min(ymax, libraries[i].getYmax());
            }

            return ymax;
        }

        return 0d;
    }

    public String toString() {
        return "This database has these extensions: \n" + getMinX() + " " + 
               getMinY() + " - " + getMaxX() + " " + getMinY() + "\n";
    }

    public VPFFeatureClass getFeatureClass(String typename) {
        VPFFeatureClass tmp = null;

        for (int i = 0; i < libraries.length; i++) {
            tmp = libraries[i].getFeatureClass(typename);

            if (tmp != null) {
                return tmp;
            }
        }

        return null;
    }
}