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
package org.geotools.sld.bindings;

import org.picocontainer.MutablePicoContainer;
import org.geotools.xml.BindingConfiguration;


/**
 * Binding configuration for the http://www.opengis.net/sld schema.
 *
 * @generated
 */
public final class SLDBindingConfiguration implements BindingConfiguration {
    /**
     * @generated modifiable
     */
    public void configure(MutablePicoContainer container) {
        //Types
        container.registerComponentImplementation(SLD.PARAMETERVALUETYPE,
            SLDParameterValueTypeBinding.class);
        container.registerComponentImplementation(SLD.SELECTEDCHANNELTYPE,
            SLDSelectedChannelTypeBinding.class);
        container.registerComponentImplementation(SLD.SYMBOLIZERTYPE, SLDSymbolizerTypeBinding.class);

        //Elements
        container.registerComponentImplementation(SLD.ANCHORPOINT, SLDAnchorPointBinding.class);
        container.registerComponentImplementation(SLD.CHANNELSELECTION,
            SLDChannelSelectionBinding.class);
        container.registerComponentImplementation(SLD.COLORMAP, SLDColorMapBinding.class);
        container.registerComponentImplementation(SLD.COLORMAPENTRY, SLDColorMapEntryBinding.class);
        container.registerComponentImplementation(SLD.CONTRASTENHANCEMENT,
            SLDContrastEnhancementBinding.class);
        container.registerComponentImplementation(SLD.CSSPARAMETER, SLDCssParameterBinding.class);
        container.registerComponentImplementation(SLD.DISPLACEMENT, SLDDisplacementBinding.class);

        container.registerComponentImplementation(SLD.EXTENT, SLDExtentBinding.class);
        container.registerComponentImplementation(SLD.EXTERNALGRAPHIC,
            SLDExternalGraphicBinding.class);
        container.registerComponentImplementation(SLD.FEATURETYPECONSTRAINT,
            SLDFeatureTypeConstraintBinding.class);

        container.registerComponentImplementation(SLD.FEATURETYPESTYLE,
            SLDFeatureTypeStyleBinding.class);
        container.registerComponentImplementation(SLD.FILL, SLDFillBinding.class);
        container.registerComponentImplementation(SLD.FONT, SLDFontBinding.class);

        container.registerComponentImplementation(SLD.GEOMETRY, SLDGeometryBinding.class);
        container.registerComponentImplementation(SLD.GRAPHIC, SLDGraphicBinding.class);
        container.registerComponentImplementation(SLD.GRAPHICFILL, SLDGraphicFillBinding.class);
        container.registerComponentImplementation(SLD.GRAPHICSTROKE, SLDGraphicStrokeBinding.class);

        container.registerComponentImplementation(SLD.HALO, SLDHaloBinding.class);

        container.registerComponentImplementation(SLD.IMAGEOUTLINE, SLDImageOutlineBinding.class);

        container.registerComponentImplementation(SLD.LABELPLACEMENT, SLDLabelPlacementBinding.class);

        container.registerComponentImplementation(SLD.LAYERFEATURECONSTRAINTS,
            SLDLayerFeatureConstraintsBinding.class);
        container.registerComponentImplementation(SLD.LEGENDGRAPHIC, SLDLegendGraphicBinding.class);
        container.registerComponentImplementation(SLD.LINEPLACEMENT, SLDLinePlacementBinding.class);
        container.registerComponentImplementation(SLD.LINESYMBOLIZER, SLDLineSymbolizerBinding.class);
        container.registerComponentImplementation(SLD.MARK, SLDMarkBinding.class);

        container.registerComponentImplementation(SLD.NAMEDLAYER, SLDNamedLayerBinding.class);
        container.registerComponentImplementation(SLD.NAMEDSTYLE, SLDNamedStyleBinding.class);

        container.registerComponentImplementation(SLD.ONLINERESOURCE, SLDOnlineResourceBinding.class);

        container.registerComponentImplementation(SLD.OVERLAPBEHAVIOR,
            SLDOverlapBehaviorBinding.class);
        container.registerComponentImplementation(SLD.PERPENDICULAROFFSET,
            SLDPerpendicularOffsetBinding.class);
        container.registerComponentImplementation(SLD.POINTPLACEMENT, SLDPointPlacementBinding.class);
        container.registerComponentImplementation(SLD.POINTSYMBOLIZER,
            SLDPointSymbolizerBinding.class);
        container.registerComponentImplementation(SLD.POLYGONSYMBOLIZER,
            SLDPolygonSymbolizerBinding.class);

        container.registerComponentImplementation(SLD.RASTERSYMBOLIZER,
            SLDRasterSymbolizerBinding.class);

        container.registerComponentImplementation(SLD.REMOTEOWS, SLDRemoteOWSBinding.class);

        container.registerComponentImplementation(SLD.RULE, SLDRuleBinding.class);

        container.registerComponentImplementation(SLD.SHADEDRELIEF, SLDShadedReliefBinding.class);

        container.registerComponentImplementation(SLD.STROKE, SLDStrokeBinding.class);
        container.registerComponentImplementation(SLD.STYLEDLAYERDESCRIPTOR,
            SLDStyledLayerDescriptorBinding.class);
        container.registerComponentImplementation(SLD.SYMBOLIZER, SLDSymbolizerBinding.class);
        container.registerComponentImplementation(SLD.TEXTSYMBOLIZER, SLDTextSymbolizerBinding.class);

        container.registerComponentImplementation(SLD.USERLAYER, SLDUserLayerBinding.class);
        container.registerComponentImplementation(SLD.USERSTYLE, SLDUserStyleBinding.class);
    }
}
