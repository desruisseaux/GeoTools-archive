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

package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.naming.OperationNotSupportedException;

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.wfs.WFSSchema;
import org.xml.sax.SAXException;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 * @source $URL$
 */
public class WFSTransactionState implements State {
    private WFSDataStore ds = null;
    private String[] fids;
    private LinkedList actions = new LinkedList();

    private WFSTransactionState() {
        // should not be used
    }

    /**
     * 
     * @param ds
     */
    public WFSTransactionState(WFSDataStore ds) {
        this.ds = ds;
    }

    /**
     * 
     * @see org.geotools.data.Transaction.State#setTransaction(org.geotools.data.Transaction)
     */
    public void setTransaction(Transaction transaction) {
        if (transaction != null) {
            //			authId = null;
            fids = null;
        }

        actions = new LinkedList();
    }

    /**
     * 
     * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
     */
    public void addAuthorization(String AuthID){
        //		authId = AuthID;
    }

    /**
     * Not implemented
     * 
     * @return String
     */
    public String getLockId() {
        return null; // add this later
    }

    /**
     * 
     * @see org.geotools.data.Transaction.State#commit()
     */
    public void commit() throws IOException {
        // TODO deal with authID and locking ... WFS only allows one authID / transaction ...
        TransactionResult tr = null;

        if (((ds.protocol & WFSDataStore.POST_PROTOCOL) == WFSDataStore.POST_PROTOCOL)
                && (tr == null)) {
            try {
                tr = commitPost();
            } catch (OperationNotSupportedException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
                tr = null;
            } catch (SAXException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
                tr = null;
            }
        }

//        if (((ds.protocol & WFSDataStore.GET_PROTOCOL) == WFSDataStore.GET_PROTOCOL)
//                && (tr == null)) {
//            try {
//                tr = commitPost();
//            } catch (OperationNotSupportedException e) {
//                WFSDataStoreFactory.logger.warning(e.toString());
//                tr = null;
//            } catch (SAXException e) {
//                WFSDataStoreFactory.logger.warning(e.toString());
//                tr = null;
//            }
//        }

        if (tr == null) {
            throw new IOException("An error occured");
        }

        if (tr.getStatus() == TransactionResult.FAILED) {
            throw new IOException(tr.getError().toString());
        }

        fids = tr.getInsertResult().getFids();
        actions = new LinkedList();
    }

    private TransactionResult commitPost()
        throws OperationNotSupportedException, IOException, SAXException {
        URL postUrl = ds.capabilities.getTransaction().getPost();
        
//System.out.println("POST Commit URL = "+postUrl);

        if (postUrl == null) {
            return null;
        }

        HttpURLConnection hc = WFSDataStore.getConnection(postUrl,ds.auth,true);
//System.out.println("connection to commit");
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,
			WFSSchema.getInstance().getElements()[24]); // Transaction
        Set fts = new HashSet();
        Iterator i = actions.iterator();
        while(i.hasNext()){
        	Action a = (Action)i.next();
        	fts.add(a.getTypeName());
        }
        Set ns = new HashSet();
        ns.add(WFSSchema.NAMESPACE.toString());
        i = fts.iterator();
        while(i.hasNext()){
        	ns.add(ds.getSchema((String)i.next()).getNamespace().toString());
        }
        hints.put(DocumentWriter.SCHEMA_ORDER,
    			ns.toArray(new String[ns.size()])); // Transaction
        
//System.out.println("Ready to print Debug");
//        // DEBUG
//StringWriter debugw = new StringWriter();
//DocumentWriter.writeDocument(this, WFSSchema.getInstance(), debugw, hints);
//System.out.println("TRANSACTION   \n\n");
//System.out.println(debugw.getBuffer());
//        // END DEBUG
        
        OutputStream os = hc.getOutputStream();

        // write request
        Writer w = new OutputStreamWriter(os);

        DocumentWriter.writeDocument(this, WFSSchema.getInstance(), w, hints);
        os.flush();
        os.close();

        InputStream is = this.ds.getInputStream(hc);

        hints = new HashMap();

        TransactionResult ft = (TransactionResult) DocumentFactory.getInstance(is,
                hints, Level.WARNING);
        return ft;
    }

    /**
     * 
     * @see org.geotools.data.Transaction.State#rollback()
     */
    public void rollback(){
        fids = null;
        actions = new LinkedList();
    }

    /**
     * 
     * @return Fid Set
     */
    public String[] getFids() {
        return fids;
    }

    /**
     * 
     * @param a
     */
    public void addAction(Action a) {
        actions.add(a);
    }

    /**
     * 
     * @return List of Actions
     */
    public List getActions() {
        return new LinkedList(actions);
    }
}
