/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.process.literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 *  Process to union 2 or more geometries together
 * @author gdavis
 */
class UnionProcess implements Process {

    private UnionFactory factory;
    private Map<String, Object> resultMap;
    private Map<String, Object> inputMap;
    private boolean started = false;        

    public UnionProcess( UnionFactory unionFactory ) {
        this.factory = unionFactory;
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public void process(ProgressListener monitor) {
		if (started) return;
		
		started = true;
		if( monitor == null ) monitor = new NullProgressListener();
		if (resultMap == null) resultMap = new HashMap<String, Object>(1);
        
        try {
            monitor.started();
            monitor.setTask( Text.text("Grabbing arguments") );
            monitor.progress( 10.0f );
            List<Geometry> list = (List<Geometry>) inputMap.get( UnionFactory.GEOM1.key );          
            
            int div = list.size();
            if (div < 1) div = 1;
            
            int chunk = 80 / div;
            int count = 1;
            Geometry result = null;
            for( Geometry geom : list ){
                if( monitor.isCanceled() ) return; // user has canceled this operation                
                if( result == null ) {
                    result = geom;
                }
                else {
                    monitor.setTask( Text.text("Processing Union " + count + " of " + list.size()) );
                    count++;
                    monitor.progress( 10.0f + chunk );                  	
                    result = result.union( geom );
                }                
            }
            
            monitor.setTask( Text.text("Encoding result" ));
            monitor.progress( 90.0f );
            
            resultMap.put( UnionFactory.RESULT.key, result );
            monitor.complete(); // same as 100.0f
        }
        catch (Exception eek){
            monitor.exceptionOccurred(eek);
        }
        finally {
            monitor.dispose();
        }
    }
    
	public void setInput(Map<String, Object> input) {
		// do any validation or pre-processing work here
		this.inputMap = input;
	}

	public Map<String, Object> getResult() {
		return resultMap;
	}   

}
