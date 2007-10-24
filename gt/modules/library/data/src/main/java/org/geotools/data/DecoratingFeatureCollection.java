package org.geotools.data;

import java.io.IOException;

import org.geotools.factory.Hints;
import org.geotools.feature.FeatureIterator;

/**
 * Extension of {@link DecoratingFeatureCollection} which also implements
 * {@link ContentFeatureCollection}.
 * 
 * TODO: merge with org.geotools.feature.collection.DecoratingFeatureCollection
 * once fc proposal is sorted out.
 *
 */
public class DecoratingFeatureCollection extends org.geotools.feature.collection.DecoratingFeatureCollection 
    implements ContentFeatureCollection {

    ContentFeatureCollection delegate;
    
    protected DecoratingFeatureCollection( ContentFeatureCollection delegate ) {
        super( delegate );
        this.delegate = delegate;
    }

    public void setHints(Hints hints) {
        delegate.setHints( hints );
    }
    
    public FeatureIterator inserter() throws IOException {
        return delegate.inserter();
    }
    
    public FeatureIterator writer() throws IOException {
        return delegate.writer();
    }
}
