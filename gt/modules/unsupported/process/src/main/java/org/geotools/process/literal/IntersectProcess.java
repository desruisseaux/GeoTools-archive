package org.geotools.process.literal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author Jody
 */
class IntersectProcess implements Process {

    private IntersectsFactory factory;
    private Map<String, Object> params;

    public IntersectProcess( IntersectsFactory intersectsFactory, Map<String, Object> parameters ) {
        this.factory = intersectsFactory;
        this.params = parameters;
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public Map<String, Object> process( ProgressListener monitor ) {
        if( monitor == null ) monitor = new NullProgressListener();
        try {
            monitor.started();
            monitor.setTask( Text.text("Grab arguments") );
            monitor.progress( 10.0f );
            Geometry geom1 = (Geometry) params.get( IntersectsFactory.GEOM1.key );
            Geometry geom2 = (Geometry) params.get( IntersectsFactory.GEOM2.key );
            
            monitor.setTask( Text.text("Grab arguments") );
            monitor.progress( 10.0f );
            
            if( monitor.isCanceled() ){
                return null; // user has canceled this operation
            }
            Geometry intersect = geom1.intersection( geom2 );
            
            monitor.setTask( Text.text("Encode result" ));
            monitor.progress( 90.0f );
            
            Map<String, Object> result = new HashMap<String, Object>(1);
            result.put( IntersectsFactory.RESULT.key, intersect );
            monitor.complete(); // same as 100.0f
            
            return result;
        }
        catch (Exception eek){
            monitor.exceptionOccurred(eek);
        }
        finally {
            monitor.dispose();
        }
        return null;
    }

}
