/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import org.opengis.filter.Filter;

import java.io.IOException;

public class IndexView
    implements FeatureSource
{
    private final FeatureIndex index;
    private final Query view;

    public IndexView( FeatureIndex index, Query q )
    {
        this.index = index;
        this.view = q;
    }

    public IndexView( FeatureIndex index )
    {
        this( index, Query.ALL );
    }

    public void addFeatureListener( FeatureListener arg0 )
    {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds(  )
                       throws IOException
    {
        // TODO Auto-generated method stub
        return index.getBounds( view );
    }

    public Envelope getBounds( Query q )
                       throws IOException
    {
        // TODO Auto-generated method stub
        return index.getBounds( restrict( q ) );
    }

    public int getCount( Query q )
                 throws IOException
    {
        // TODO Auto-generated method stub
        return index.getCount( restrict( q ) );
    }

    public DataStore getDataStore(  )
    {
        // TODO Auto-generated method stub
        return index.getDataStore(  );
    }

    public FeatureCollection getFeatures(  )
                                  throws IOException
    {
        // TODO Auto-generated method stub
        return index.getFeatures( view );
    }

    public FeatureCollection getFeatures( Query q )
                                  throws IOException
    {
        return index.getFeatures( restrict( q ) );
    }

    public FeatureCollection getFeatures( Filter f )
                                  throws IOException
    {
        // TODO Auto-generated method stub
        return index.getFeatures( new DefaultQuery( "", f ) );
    }

    public FeatureType getSchema(  )
    {
        return index.getSchema(  );
    }

    public void removeFeatureListener( FeatureListener arg0 )
    {
        // TODO Auto-generated method stub
    }

    private Query restrict( Query q )
    {
        // TODO combine view query and new query
        return q;
    }
}
