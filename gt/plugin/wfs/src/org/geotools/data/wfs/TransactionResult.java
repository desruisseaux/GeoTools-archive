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
 * Created on 21-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.filter.FidFilter;
import org.xml.sax.SAXException;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class TransactionResult {
    public static final int NO_STATUS = 0;
    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int PARTIAL = 4;
    private FidFilter insertResult;
    private int status;

    //	private String locator;
    //	private String message;
    private SAXException error;

    private TransactionResult() {
    }

    public TransactionResult(int status, FidFilter insertResult,
        SAXException error) {
        this.status = status;
        this.insertResult = insertResult;
        this.error = error;
    }

    // locator nullable
    public TransactionResult(int status, FidFilter insertResult,
        String locator, String message) {
        this.status = status;
        this.insertResult = insertResult;
        error = new SAXException(message + ":"
                + ((locator == null) ? "" : locator));
    }

    public static int parseStatus(String s) {
        if ("SUCCESS".equalsIgnoreCase(s)) {
            return SUCCESS;
        }

        if ("FAILED".equalsIgnoreCase(s)) {
            return FAILED;
        }

        if ("PARTIAL".equalsIgnoreCase(s)) {
            return PARTIAL;
        }

        return NO_STATUS;
    }

    public static String printStatus(int i) {
        switch (i) {
        case SUCCESS:
            return "SUCCESS";

        case FAILED:
            return "FAILED";

        case PARTIAL:
            return "PARTIAL";

        default:
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the error.
     */
    public SAXException getError() {
        return error;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the insertResult.
     */
    public FidFilter getInsertResult() {
        return insertResult;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }
}
