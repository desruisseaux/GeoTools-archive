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
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.wfs.WFSSchema;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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


/**
 * DOCUMENT ME!
 *
 * @author dzwiers TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSTransactionState implements State {
    private WFSDataStore ds = null;
    private String[] fids;
    private LinkedList actions = new LinkedList();

    private WFSTransactionState() {
    }

    public WFSTransactionState(WFSDataStore ds) {
        this.ds = ds;
    }

    //	private String authId;

    /* (non-Javadoc)
     * @see org.geotools.data.Transaction.State#setTransaction(org.geotools.data.Transaction)
     */
    public void setTransaction(Transaction transaction) {
        if (transaction != null) {
            //			authId = null;
            fids = null;
        }

        actions = new LinkedList();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
     */
    public void addAuthorization(String AuthID) throws IOException {
        //		authId = AuthID;
    }

    public String getLockId() {
        return null; // add this later
    }

    /* (non-Javadoc)
     * @see org.geotools.data.Transaction.State#commit()
     */
    public void commit() throws IOException {
        // TODO deal with authID and locking ... WFS only allows one authID / transaction ...
        TransactionResult tr = null;

        if (((ds.protos & WFSDataStore.POST_FIRST) == WFSDataStore.POST_FIRST)
                && (tr == null)) {
            try {
                tr = commitPost();
            } catch (OperationNotSupportedException e) {
                WFSDataStore.logger.warning(e.toString());
                tr = null;
            } catch (SAXException e) {
                WFSDataStore.logger.warning(e.toString());
                tr = null;
            }
        }

        //	get not supported using kvp in spec except delete ... we don't allow it
        //	    if((ds.protos & WFSDataStore.GET_FIRST) == WFSDataStore.GET_FIRST && tr == null)
        //	        tr = commitGet();
        if (((ds.protos & WFSDataStore.POST_OK) == WFSDataStore.POST_OK)
                && (tr == null)) {
            try {
                tr = commitPost();
            } catch (OperationNotSupportedException e) {
                WFSDataStore.logger.warning(e.toString());
                tr = null;
            } catch (SAXException e) {
                WFSDataStore.logger.warning(e.toString());
                tr = null;
            }
        }

        //	    if((ds.protos & WFSDataStore.GET_OK) == WFSDataStore.GET_OK && tr == null)
        //	        tr = commitGet();
        if (tr == null) {
            throw new IOException("An error occured");
        }

        if (tr.getStatus() == TransactionResult.FAILED) {
            throw new IOException(tr.getError().toString());
        }

        fids = tr.getInsertResult().getFids();
    }

    private TransactionResult commitPost()
        throws OperationNotSupportedException, IOException, SAXException {
        URL postUrl = ds.capabilities.getTransaction().getPost();

        if (postUrl == null) {
            return null;
        }

        HttpURLConnection hc = (HttpURLConnection) postUrl.openConnection();
        hc.setRequestMethod("POST");

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

//try{
//StringWriter sw = new StringWriter();
//
//DocumentWriter.writeDocument(this, WFSSchema.getInstance(), sw, hints);
//System.out.println(sw.toString());
//sw.flush();
//sw.close();
//}catch(Exception e){
//	e.printStackTrace();
//}
        
        OutputStream os = WFSDataStore.getOutputStream(hc, ds.auth);

        // write request
        Writer w = new OutputStreamWriter(os);

        DocumentWriter.writeDocument(this, WFSSchema.getInstance(), w, hints);
        os.flush();
        os.close();

        InputStream is = WFSDataStore.getInputStream(hc, ds.auth);

        hints = new HashMap();

        TransactionResult ft = (TransactionResult) DocumentFactory.getInstance(is,
                hints, Level.WARNING);
System.out.println("RESULT IS NULL? "+(ft == null));
        return ft;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.Transaction.State#rollback()
     */
    public void rollback() throws IOException {
        fids = null;
        actions = new LinkedList();
    }

    public String[] getFids() {
        return fids;
    }

    public void addAction(Action a) {
        actions.add(a);
    }

    public List getActions() {
        return new LinkedList(actions);
    }
}
