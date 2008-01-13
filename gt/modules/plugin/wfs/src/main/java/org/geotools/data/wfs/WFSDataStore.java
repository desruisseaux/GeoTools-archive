package org.geotools.data.wfs;

import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * {@link DataStore} extension interface to provide WFS specific extra
 * information.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public interface WFSDataStore extends DataStore {

    /**
     * @return the Title for the service identification in the capabilities
     *         document for the WFS Service this DataStore refers to.
     */
    public String getTitle();

    /**
     * @return the Abstract for the service identification in the capabilities
     *         document for the WFS Service this DataStore refers to.
     */
    public String getAbstract();

    /**
     * @return the Keywords for the service identification in the capabilities
     *         document for the WFS Service this DataStore refers to.
     */
    public List<String> getKeywords();

    /**
     * 
     * @param typeName
     *            the type name to return the Title from.
     * @return
     * @throws NoSuchElementException
     *             if typeName does not correspond to a FeatureType declared in
     *             the WFS capabilities document.
     */
    public String getTitle(String typeName) throws NoSuchElementException;

    /**
     * 
     * @param typeName
     *            the type name to return the Abstract from.
     * @return
     * @throws NoSuchElementException
     *             if typeName does not correspond to a FeatureType declared in
     *             the WFS capabilities document.
     */
    public String getAbstract(String typeName) throws NoSuchElementException;

    /**
     * The bounds of {@code typeName} in {@code EPSG:4326} as stated in the WFS
     * capabilities document.
     * 
     * @param typeName
     *            the type name to return the WGS84 bounds from.
     * @return
     * @throws NoSuchElementException
     *             if typeName does not correspond to a FeatureType declared in
     *             the WFS capabilities document.
     */
    public ReferencedEnvelope getLatLonBoundingBox(String typeName) throws NoSuchElementException;

}
