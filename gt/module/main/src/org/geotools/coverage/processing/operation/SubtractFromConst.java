/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.coverage.processing.operation;

// JAI dependencies (for javadoc)
import javax.media.jai.operator.SubtractFromConstDescriptor;

// Geotools dependencies
import org.geotools.coverage.processing.OperationJAI;


/**
 * Subtracts every sample values of the source coverage from constants (one for each band).
 * If the number of constants supplied is less than the number of bands of the destination,
 * then the constant from entry 0 is applied to all the bands. Otherwise, a constant from a
 * different entry is applied to each band.
 *
 * <P><STRONG>Name:</STRONG>&nbsp;<CODE>"SubtractFromConst"</CODE><BR>
 *    <STRONG>JAI operator:</STRONG>&nbsp;<CODE>"{@linkplain SubtractFromConstDescriptor SubtractFromConst}"</CODE><BR>
 *    <STRONG>Parameters:</STRONG></P>
 * <table border='3' cellpadding='6' bgcolor='F4F8FF'>
 *   <tr bgcolor='#B9DCFF'>
 *     <th>Name</th>
 *     <th>Class</th>
 *     <th>Default value</th>
 *     <th>Minimum value</th>
 *     <th>Maximum value</th>
 *   </tr>
 *   <tr>
 *     <td>{@code "Source"}</td>
 *     <td>{@link org.geotools.coverage.grid.GridCoverage2D}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "constants"}</td>
 *     <td>{@code double[]}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 * </table>
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.coverage.processing.Operations#subtractFrom
 * @see SubtractFromConstDescriptor
 *
 * @todo Should operates on {@code sampleToGeophysics} transform when possible.
 *       See <A HREF="http://jira.codehaus.org/browse/GEOT-610">GEOT-610</A>.
 */
public class SubtractFromConst extends OperationJAI {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6941277637814235474L;

    /**
     * Constructs a default {@code "SubtractFromConst"} operation.
     */
    public SubtractFromConst() {
        super("SubtractFromConst");
    }
}
