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
package org.geotools.data.wms.getCapabilities;

import java.util.List;

/**
 * @author rgould
 *
 * Nested list of zero or more map Layers offered by this server.
 */
public class Layer {
    /** A machine-readable (typically one word) identifier */
    private String name;
    
    /** The title is for informative display to a human. */
    private String title;
    
    /** The abstract is a longer narrative description */
    private String _abstract;
    
    /** List of keywords or keyword phrases (Strings) to help catalog searching */
    private List keywordList;
    
    /** A list of Strings representing SRSs */
    private List srs;
    
    /** 
     * The LatLonBoundingBox attributes indicate the edges of the 
     * enclosing rectangle in latitude/longitude decimal degrees 
     * (as in SRS EPSG:4326 [WGS1984 lat/lon]
     */
    private LatLonBoundingBox latLonBoundingBox;
    
    /**
     * The BoundingBox attributes indicate the edges of the bounting box in
     * units of the specified spatial reference system
     * 
     * A list of type BoundingBox
     */
    private List boundingBoxes;
    
    /** 
     * The Dimension element declares the existance of a dimension
     * 
     * A list of type Dimension 
     */
    private List dimensions;
    
    /**
     * The Extent element indicates what values along a dimension are valid.
     * 
     * A List of type Extent 
     */
    private List extents;
    
    /**
     * Attribution indicates the provider of a Layer or collection of Layers.
     * The provider's URL, descriptive title string, and/or logo image URL may
     * be supplied.  Client applications may choose to display one or more of these
     * items.  A format element indicates the MIME type of the logo image located
     * at LogoURL.  The logo image's width and height assist client application
     * in laying out space to display the logo.
     */
    private Attribution attribution;
    
    /**
     * A Map Server may use zero or more Identifier elements to list ID numbers
     * or labels defined by a particular Authority.  For example, the Global Change
     * Master Directory (gcmd.gsfc.nasa.gov) defines a DIF_ID label for every
     * dataset.  The authority name and explanatory URL are defined in a spearate
     * AuthorityURL element, which may be defined once and inherited by subsidiary
     * layers.  Identifiers themselves are not inherited.
     * 
     * A list of the type AuthorityURL
     */
    private List authorityURLs;
    
    /**
     * A list of type Identifier
     */
    private List identifiers;
    
    /**
     * A Map Server may use zero or more MetadataURL elements to offer detailed, 
     * standardized metadata about the data underneath a particular layer. The type
     * attribute indicates the standard to which the metadata complies. Two types
     * are defined at present: 'TC211' = ISO TC211 19115; 'FGDC' = FGDC CSDGM.  The
     * format element indicates how the metadata is structured.
     * 
     * A list of type MetadataURL
     */
    private List metadataURLs;
    
    /**
     * A Map Server may use DataURL to offer more information about the data 
     * underneath a particular layer. While the semantics are not well-defined, as
     * long as the results of an HTTP GET request against the DataURL are properly 
     * MIME-typed, Viewer Clients and Cascading Map Servers can make use of this.
     * 
     * A list of type DataURL
     */
    private List dataURL;
    
    /**
     * A Map Server may use FeatureListURL to point to a list of the features
     * represented in a Layer.
     * 
     * A list of type FeatureListURL
     */
    private List featureListURL;
    
    /**
     * A Style element lists the name by which a style is requested and a
     * human-readable title for pick lists, optionally (and ideally) provides a
     * human-readable description, and optionally gives a style URL.
     * 
     * A list of type Style
     */
    private List styles;
    
    /**
     * Minimum and maximum scale hints for which it is appropriate to display this layer.
     */
    private ScaleHint scaleHint;
    
    /**
     * Layers can be nested.
     */
    private List subLayers;
    
    /**
     * Optional attributes
     */
    private boolean queryable = false;
    private String cascaded;
    private boolean opaque = false;
    private boolean noSubsets = false;
    private String fixedWidth;
    private String fixedHeight;
    
    /**
     * @param title
     */
    public Layer(String title) {
        this.title = title;
    }
    
    public String get_abstract() {
        return _abstract;
    }
    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }
    public Attribution getAttribution() {
        return attribution;
    }
    public void setAttribution(Attribution attribution) {
        this.attribution = attribution;
    }
    public List getAuthorityURLs() {
        return authorityURLs;
    }
    public void setAuthorityURLs(List authorityURLs) {
        this.authorityURLs = authorityURLs;
    }
    public List getBoundingBoxes() {
        return boundingBoxes;
    }
    public void setBoundingBoxes(List boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }
    public List getSubLayers() {
        return subLayers;
    }
    public void setChildLayer(List subLayer) {
        this.subLayers = subLayer;
    }
    public List getDataURL() {
        return dataURL;
    }
    public void setDataURL(List dataURL) {
        this.dataURL = dataURL;
    }
    public List getDimensions() {
        return dimensions;
    }
    public void setDimensions(List dimensions) {
        this.dimensions = dimensions;
    }
    public List getExtents() {
        return extents;
    }
    public void setExtents(List extents) {
        this.extents = extents;
    }
    public List getFeatureListURL() {
        return featureListURL;
    }
    public void setFeatureListURL(List featureListURL) {
        this.featureListURL = featureListURL;
    }
    public List getIdentifiers() {
        return identifiers;
    }
    public void setIdentifiers(List identifiers) {
        this.identifiers = identifiers;
    }
    public List getKeywordList() {
        return keywordList;
    }
    public void setKeywordList(List keywordList) {
        this.keywordList = keywordList;
    }
    public LatLonBoundingBox getLatLonBoundingBox() {
        return latLonBoundingBox;
    }
    public void setLatLonBoundingBox(LatLonBoundingBox latLonBoundingBox) {
        this.latLonBoundingBox = latLonBoundingBox;
    }
    public List getMetadataURLs() {
        return metadataURLs;
    }
    public void setMetadataURLs(List metadataURLs) {
        this.metadataURLs = metadataURLs;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ScaleHint getScaleHint() {
        return scaleHint;
    }
    public void setScaleHint(ScaleHint scaleHint) {
        this.scaleHint = scaleHint;
    }
    public List getSrs() {
        return srs;
    }
    public void setSrs(List srs) {
        this.srs = srs;
    }
    public List getStyles() {
        return styles;
    }
    public void setStyles(List styles) {
        this.styles = styles;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getCascaded() {
        return cascaded;
    }
    public void setCascaded(String cascaded) {
        this.cascaded = cascaded;
    }
    public String getFixedHeight() {
        return fixedHeight;
    }
    public void setFixedHeight(String fixedHeight) {
        this.fixedHeight = fixedHeight;
    }
    public String getFixedWidth() {
        return fixedWidth;
    }
    public void setFixedWidth(String fixedWidth) {
        this.fixedWidth = fixedWidth;
    }
    public boolean isNoSubsets() {
        return noSubsets;
    }
    public void setNoSubsets(boolean noSubsets) {
        this.noSubsets = noSubsets;
    }
    public boolean isOpaque() {
        return opaque;
    }
    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }
    public boolean isQueryable() {
        return queryable;
    }
    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }
    public void setSubLayers(List subLayers) {
        this.subLayers = subLayers;
    }
}
