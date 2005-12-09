/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.event.AbstractGTRoot;
import org.geotools.event.GTDelta;
import org.geotools.event.GTDeltaImpl;
import org.geotools.event.GTEvent;
import org.geotools.event.GTEventImpl;


/**
 * Holds styling information (from a StyleLayerDescriptor document).
 * 
 * <p>
 * This class is based on version 1.0 of the SLD specification.
 * </p>
 * 
 * <p>
 * For many of us in geotools this is the reason we came along for the ride - a
 * pretty picture. For documentation on the use of this class please consult
 * the SLD 1.0 specification.
 * </p>
 * 
 * <p>
 * We may experiment with our own (or SLD 1.1) ideas but will mark such
 * experiments for you. This is only an issue of you are considering writing
 * out these objects for interoptability with other systems.
 * </p>
 * 
 * <p>
 * General strategy for supporting multiple SLD versions (and experiments):
 * 
 * <ul>
 * <li>
 * These classes will be <b>BIGGER</b> and more capabile then any one
 * specification
 * </li>
 * <li>
 * We can define (and support) explicit interfaces tracking each version
 * (preferably GeoAPI would hold these)
 * </li>
 * <li>
 * We can use Factories (aka SLD1Factory and SLD1_1Factory and SEFactory) to
 * support the creation of conformant datastructures. Code (such as user
 * interfaces) can be parameratized with these factories when they need to
 * confirm to an exact version supported by an individual service. We hope
 * that specifications are always adaptive, and will be forced to throw
 * unsupported exceptions when functionality is removed from a specification.
 * </li>
 * </ul>
 * </p>
 */
public class StyledLayerDescriptorImpl extends AbstractGTRoot
    implements StyledLayerDescriptor {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.styling");

    /** Holds value of property name. */
    private String name;

    /** Holds value of property title. */
    private String title;

    /** Holds value of property abstract. */
    private String abstractStr;
    private List layers = new ArrayList();
    
    /**
     * Convenience method for grabbing the default style from the
     * StyledLayerDescriptor.
     *
     * @return first Style (in SLD-->UserLayers-->UserStyles) that claims to be
     *         the default
     */
    public Style getDefaultStyle() {
        //descend into the layers
        for (int i = 0; i < layers.size(); i++) {
            StyledLayer layer = (StyledLayer) layers.get(i);

            if (layer instanceof UserLayer) {
                UserLayer userLayer = (UserLayer) layer;

                //descend into the styles
                Style[] styles = userLayer.getUserStyles();

                for (int j = 0; j < styles.length; j++) {
                    //return the first style that claims to be the default
                    if (styles[j].isDefault()) {
                        return styles[j];
                    }
                }
            }
        }

        return null;
    }

    public StyledLayer[] getStyledLayers() {
        return (StyledLayerImpl[]) layers.toArray(new StyledLayerImpl[layers
                .size()]);
        }

        public void setStyledLayers(StyledLayer[] layers) {
            this.layers.clear();

            for (int i = 0; i < layers.length; i++) {
                addStyledLayer(layers[i]);
            }

            LOGGER.fine("StyleLayerDescriptorImpl added " + this.layers.size()
                + " styled layers");
            fireChanged(); // TODO Handle StyledLayer List
        }

        public void addStyledLayer(StyledLayer layer) {
            layer.setParent(this);
            layers.add(layer);
        }

        /**
         * Getter for property name.
         *
         * @return Value of property name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Setter for property name.
         *
         * @param name New value of property name.
         */
        public void setName(String name) {
            this.name = name;
            fireChanged();
        }

        /**
         * Getter for property title.
         *
         * @return Value of property title.
         */
        public String getTitle() {
            return this.title;
        }

        /**
         * Setter for property title.
         *
         * @param title New value of property title.
         */
        public void setTitle(String title) {
            this.title = title;
            fireChanged();
        }

        /**
         * Getter for property abstractStr.
         *
         * @return Value of property abstractStr.
         */
        public java.lang.String getAbstract() {
            return abstractStr;
        }

        /**
         * Setter for property abstractStr.
         *
         * @param abstractStr New value of property abstractStr.
         */
        public void setAbstract(java.lang.String abstractStr) {
            this.abstractStr = abstractStr;
            fireChanged();
        }


        /**
         * Issue a change event w/ PRE_DELETE
         *
         * @param childDelta Delta describing change
         */
        public void removed(GTDelta childDelta) {
        	if( !hasListeners() ) return;
            
            GTDelta delta = new GTDeltaImpl("", GTDelta.NO_INDEX,
                    GTDelta.Kind.NO_CHANGE, this, childDelta);
            GTEventImpl event = new GTEventImpl(this, GTEvent.Type.PRE_DELETE,
                    delta);
            fire(event);
        }

		/**
         * Used to pass on "We changed" notification from children.
         *
         * @param delta Describes change
         */
        public void changed(GTDelta delta) {
        	if( !hasListeners() ) return;

            fire(new GTDeltaImpl("", GTDelta.NO_INDEX, GTDelta.Kind.NO_CHANGE,
                    this, delta));
        }
        
        public void accept(StyleVisitor visitor) {
            visitor.visit(this);
        }
    }
