package org.geotools.data.coverage.grid.file;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.opengis.coverage.grid.Format;

/**
 * @author jeichar
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FormatManager {

    private static FormatManager manager;
    
    private HashMap formatMap=new HashMap();

    private FormatManager() {
        Iterator iter = GridFormatFinder.getAvailableFormats();
        while (iter.hasNext()) {
            GridFormatFactorySpi factory = (GridFormatFactorySpi) iter.next();
            formatMap.put(factory.createFormat(), factory);
        }
    }
    
    public static FormatManager getFormatManager(){
        if( manager==null)
            manager=new FormatManager();
        return manager;
    }
    
    public Format[] getFormats(){
        Format[] format=new Format[formatMap.size()];
        return (Format[])formatMap.keySet().toArray(format);
    }
    
    public Format getFormat(File f){
        Set entries=formatMap.entrySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Map.Entry element = (Map.Entry) iter.next();
            GridFormatFactorySpi factory=(GridFormatFactorySpi)element.getValue();
            try{
            if( factory.accepts(f.toURL()))
                return (Format)element.getKey();
            }catch(MalformedURLException e){}
        }
        
        return null;
    }
    
    public GridFormatFactorySpi getFactory(Format f){
        return (GridFormatFactorySpi)formatMap.get(f);
    }

    public java.io.FileFilter getFileFilter() {
        return new FormatFileFilter();
    }

    private class FormatFileFilter implements java.io.FileFilter {
        /*
         * (non-Javadoc)
         * 
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File pathname) {
            Collection factories = formatMap.values();

            for (Iterator iter = factories.iterator(); iter.hasNext();) {
                GridFormatFactorySpi factory = (GridFormatFactorySpi) iter
                        .next();
                try {
                    if (factory.accepts(pathname.toURL()))
                        return true;
                } catch (MalformedURLException e) {
                }
            }
            return false;
        }
    }

}