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
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.j2d;


// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.Adapters;
import org.geotools.ct.TransformException;
import org.geotools.data.FeatureSource;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;

// Geotools dependencies
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListener;
import org.geotools.resources.XMath;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.geotools.styling.Style;
import java.awt.Component;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;

// J2SE dependencies
import java.util.Map;
import org.geotools.feature.IllegalAttributeException;


/**
 * A renderer for rendering {@linkPlain Style styled}{@linkPlain Feature features}. This renderer
 * listen for {@linkPlain CollectionEvent feature collection changes} and invokes {@link
 * RenderedLayer#repaint} automatically on changes.
 *
 * @author Martin Desruisseaux
 * @version $Id: StyledMapRenderer.java,v 1.1 2003/12/04 23:21:02 aaime Exp $
 */
public class StyledMapRenderer extends Renderer {
    /** The factory for rendered layers. */
    private final RenderedLayerFactory factory;

    /**
     * The layer to be rendered. Keys are {@link Layer} objects and values are {@link
     * StyledRenderer.LayerEntry}.
     */
    private final Map renderedLayers = new HashMap();

    /**
     * Construct a new renderer for the specified component.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public StyledMapRenderer(final Component owner) {
        this(owner, new RenderedLayerFactory());
    }

    /**
     * Construct a new renderer using the specified factory.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     * @param factory DOCUMENT ME!
     */
    public StyledMapRenderer(final Component owner, final RenderedLayerFactory factory) {
        super(owner);
        this.factory = factory;
    }

    /**
     * Set a new context as the current one. This method performs the following steps:
     * 
     * <ul>
     * <li>
     * Remove all previous layers.
     * </li>
     * <li>
     * Set the coordinate system to the context CS.
     * </li>
     * <li>
     * Add all layers found in the context.
     * </li>
     * <li>
     * Register listeners for feature changes.
     * </li>
     * </ul>
     * 
     *
     * @param context The new context, or <code>null</code> for removing any previous context.
     */
    public synchronized void setMapContext(final MapContext mapContext) throws TransformException, IOException, IllegalAttributeException {
        removeAllLayers();
        if (mapContext != null) {
            final Envelope box = mapContext.getAreaOfInterest();

            // TODO: use CoordinateReferenceSystem instead?
            final CoordinateSystem cs = (CoordinateSystem) mapContext.getCoordinateReferenceSystem();
            factory.setCoordinateSystem(cs);
            setCoordinateSystem(cs);

            MapLayer[] layers = mapContext.getLayers();
            for (int i = 0; i < layers.length; i++) {
                addLayer(layers[i]);
            }
        }
    }

    /**
     * Add a layer to this renderer. A single {@link Layer} may be converted into an arbitrary
     * amount of {@link RenderedLayer}s. Those rendered layers will have {@linkPlain
     * RenderedLayer#getZOrder z-order} values as 4.0, 4.1, 4.2, etc. where 4 is the layer number,
     * and .0, .1, .2... is the rendered layer number for this particular layer.
     *
     * @param layer The layer to add.
     *
     * @throws TransformException if some feature in the layer use an incompatible coordinate
     *         system.
     * @throws AssertionError DOCUMENT ME!
     */
    public synchronized void addLayer(final MapLayer layer)
        throws TransformException, IOException, IllegalAttributeException {
        removeLayer(layer);

        final Style style = layer.getStyle();
        final FeatureSource source = layer.getFeatureSource();
        final RenderedLayer[] rend = factory.create(source, style);
        final boolean visible = layer.isVisible();
        final int baseZOrder = renderedLayers.size();
        final double zOrderScale = XMath.pow10((int) Math.ceil(XMath.log10(rend.length)));
        for (int j = 0; j < rend.length; j++) {
            final RenderedLayer rendered = rend[j];
            rendered.setVisible(visible);
            rendered.setZOrder((float) (baseZOrder + (j / zOrderScale)));
            addLayer(rendered);
        }

        final LayerEntry entry = new LayerEntry(layer, rend);
        if (renderedLayers.put(layer, entry) != null) {
            throw new AssertionError(); // Should never happen.
        }

        layer.addMapLayerListener(entry);
    }

    /**
     * Remove a layer from this renderer. Nothing is done if the specified layer is
     * <code>null</code> or not found in this renderer.
     *
     * @param layer The layer to remove.
     */
    public synchronized void removeLayer(final MapLayer layer) {
        final LayerEntry entry = (LayerEntry) renderedLayers.remove(layer);
        if (entry != null) {
            layer.removeMapLayerListener(entry);

            final RenderedLayer[] rendered = entry.rendered;
            for (int i = 0; i < rendered.length; i++) {
                removeLayer(rendered[i]);
            }
        }
    }

    /**
     * Remove all layers from this renderer.
     */
    public synchronized void removeAllLayers() {
        if (renderedLayers.size() == 0) {
            return;
        }

        for (Iterator it = renderedLayers.values().iterator(); it.hasNext();) {
            final LayerEntry entry = (LayerEntry) it.next();
            entry.layer.removeMapLayerListener(entry);
        }

        renderedLayers.clear();

        super.removeAllLayers();
    }

    /**
     * Map a {@link Layer} to a set of {@link RenderedLayer} and to the listeners needed for
     * catching changes in collection and visibility.
     *
     * @author Martin Desruisseaux
     * @version $Id: StyledMapRenderer.java,v 1.1 2003/12/04 23:21:02 aaime Exp $
     */
    private final class LayerEntry implements MapLayerListener {
        /** The layer. */
        final MapLayer layer;

        /** The rendered layers. */
        final RenderedLayer[] rendered;

        /**
         * Construct a new entry.
         *
         * @param layer DOCUMENT ME!
         * @param rendered The rendered layers.
         */
        public LayerEntry(final MapLayer layer, final RenderedLayer[] rendered) {
            this.layer = layer;
            this.rendered = rendered;
        }

        /**
         * Tells that all rendered layers need to be repainted. This method can be invoked from any
         * thread; it doesn't need to be the <cite>Swing</cite> thread.
         */
        public void repaint() {
            for (int i = 0; i < rendered.length; i++) {
                final RenderedLayer layer = rendered[i];
                layer.repaint();
            }
        }

        /**
         * Invoked when some property of this layer has changed. May be data,  style, title,
         * visibility.
         *
         * @param event encapsulating the event information
         */
        public void layerChanged(MapLayerEvent event) {
            try {
                if ((event.getReason() == MapLayerEvent.DATA_CHANGED)
                        || (event.getReason() == MapLayerEvent.STYLE_CHANGED)) {
                    addLayer(layer);
                }
            } catch (Exception e) {
                handleException("StyledMapRenderer", "layerChanged", e);
            }
        }

        public void layerShown(MapLayerEvent event) {
        }

        public void layerHidden(MapLayerEvent event) {
        }
    }
}
