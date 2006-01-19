/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, 2ie Technologie
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
 */
package org.geotools.image.jai;

// JAI dependencies
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;


/**
 * The descriptor for the {@link Hysteresis} operation.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Lionel Flahaut
 */
public class HysteresisDescriptor extends OperationDescriptorImpl {
    /**
     * The operation name, which is {@value}.
     */
    public static final String OPERATION_NAME = "org.geotools.Hysteresis";

    /**
     * Constructs the descriptor.
     */
    public HysteresisDescriptor() {
        super(new String[][]{{"GlobalName",  OPERATION_NAME},
                             {"LocalName",   OPERATION_NAME},
                             {"Vendor",      "org.geotools"},
                             {"Description", "Thresholding by hysteresis"},
                             {"DocURL",      "http://www.geotools.org/"}, // TODO: provides more accurate URL
                             {"Version",     "1.0"},
                             {"arg0Desc",    "The low threshold value, inclusive."},
                             {"arg1Desc",    "The high threshold value, inclusive."},
                             {"arg2Desc",    "The value to give to filtered pixel."}},
              new String[]   {RenderedRegistryMode.MODE_NAME}, 1,
              new String[]   {"low", "high", "padValue"}, // Argument names
              new Class []   {Double.class, Double.class, Double.class},    // Argument classes
              new Object[]   {NO_PARAMETER_DEFAULT, NO_PARAMETER_DEFAULT, new Double(0)},
              null // No restriction on valid parameter values.
       );
    }
}
