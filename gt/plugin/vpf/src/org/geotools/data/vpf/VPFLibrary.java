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

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.*;

/*
 * VPFLibrary.java
 *
 * Created on 19. april 2004, 14:53
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFLibrary implements FileConstants {
    private double xmin;
    private double ymin;
    private double xmax;
    private double ymax;
    private File directory = null;
    private String libCover = null;
    private VPFCoverage[] coverages = null;
    private VPFDataBase base = null;

    /** Creates a new instance of VPFLibrary */
    public VPFLibrary(TableRow tr, File dir, VPFDataBase base)
               throws IOException {
        this.base = base;
        xmin = tr.get(VPFLibraryIfc.FIELD_XMIN).doubleValue();
        ymin = tr.get(VPFLibraryIfc.FIELD_YMIN).doubleValue();
        xmax = tr.get(VPFLibraryIfc.FIELD_XMAX).doubleValue();
        ymax = tr.get(VPFLibraryIfc.FIELD_YMAX).doubleValue();
        libCover = tr.get(VPFLibraryIfc.FIELD_LIB_NAME).toString();
        this.directory = new File(dir, libCover);
        setCoverages();
    }

    private void setCoverages() throws IOException {
        if (!directory.getName().equals("rference")) {
            String vpfTableName = new File(directory, COVERAGE_ATTRIBUTE_TABLE).toString();
            TableInputStream vpfTable = new TableInputStream(vpfTableName);
            List list = vpfTable.readAllRows();
            vpfTable.close();

            TableRow[] coverages_tmp = (TableRow[]) list.toArray(
                                                 new TableRow[list.size()]);
            coverages = new VPFCoverage[coverages_tmp.length];

            for (int i = 0; i < coverages_tmp.length; i++) {
                coverages[i] = new VPFCoverage(coverages_tmp[i], directory, 
                                               base);
            }
        }
    }

    public VPFFeatureClass[] getFeatureClasses() {
        if (!directory.getName().equals("rference")) {
            ArrayList arr = new ArrayList();
            VPFFeatureClass[] tmp = null;

            for (int i = 0; i < coverages.length; i++) {
                if (coverages[i] != null) {
                    tmp = coverages[i].getFeatureClasses();

                    if (tmp != null) {
                        for (int j = 0; j < tmp.length; j++) {
                            arr.add(tmp[j]);
                        }
                    }
                }
            }

            return (VPFFeatureClass[]) arr.toArray(tmp);
        }

        return null;
    }

    public VPFCoverage[] getCoverages() {
        return coverages;
    }

    /** Getter for property xmax.
     * @return Value of property xmax.
     *
     */
    public double getXmax() {
        return xmax;
    }

    /** Getter for property xmin.
     * @return Value of property xmin.
     *
     */
    public double getXmin() {
        return xmin;
    }

    /** Getter for property ymax.
     * @return Value of property ymax.
     *
     */
    public double getYmax() {
        return ymax;
    }

    /** Getter for property ymin.
     * @return Value of property ymin.
     *
     */
    public double getYmin() {
        return ymin;
    }

    public String toString() {
        return "Dette er library : " + libCover + " with extensions:\n" + 
               getXmin() + " " + getYmin() + " - " + getXmax() + " " + 
               getYmax() + "\n";
    }

    public VPFFeatureClass getFeatureClass(String typename) {
        VPFFeatureClass tmp = null;

        if (coverages != null) {
            for (int i = 0; i < coverages.length; i++) {
                if (coverages[i] != null) {
                    tmp = coverages[i].getFeatureClass(typename);

                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
        }

        return null;
    }
}