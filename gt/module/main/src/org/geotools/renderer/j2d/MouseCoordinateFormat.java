/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1998, P�ches et Oc�ans Canada
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.util.Locale;

import org.geotools.pt.CoordinateFormat;
import org.geotools.pt.CoordinatePoint;
import org.opengis.referencing.operation.TransformException;


/**
 * Formateurs des coordonn�es point�es par le curseur de la souris. Les instances de cette classe
 * pourront �crire les coordonn�es point�es ainsi qu'une �ventuelle valeurs sous cette coordonn�es
 * (par exemple la temp�rature sur une image satellitaire de temp�rature).
 *
 * The {@linkplain #getCoordinateSystem output coordinate system} may have an arbitrary
 * number of dimensions (as long as a transform exists from the two-dimensional
 * {@linkplain Renderer#getCoordinateSystem renderer's coordinate system}), but
 * is usually two-dimensional.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MouseCoordinateFormat extends CoordinateFormat {
    /**
     * The coordinate point to format.
     */
    private CoordinatePoint point;

    /**
     * Buffer pour l'�criture des coordonn�es.
     */
    private final StringBuffer buffer = new StringBuffer();

    /**
     * Indique si la m�thode {@link #format} doit �crire la valeur apr�s la coordonn�e. Les
     * valeurs sont obtenues en appelant la m�thode <code>RenderedLayer.formatValue(...)</code>.
     * Par d�faut, les valeurs (si elles sont disponibles) sont �crites.
     */
    private boolean valueVisible = true;

    /**
     * Construct a coordinate format for the default locale.
     */
    public MouseCoordinateFormat() {
        super();
    }
    

    /**
     * Construit un objet qui �crira les coordonn�es point�es par le
     * curseur de la souris. Les coordonn�es seront �crites selon le
     * syst�me de coordonn�es par d�faut "WGS 1984".
     *
     * @param locale The locale for formatting coordinates and numbers.
     */
    public MouseCoordinateFormat(final Locale locale) {
        super(locale);
    }

    /**
     * Indique si la m�thode {@link #format} doit �crire la valeur apr�s la coordonn�e.
     * Par d�faut, les valeurs (si elles sont disponibles) sont �crites.
     */
    public boolean isValueVisible() {
        return valueVisible;
    }

    /**
     * Sp�cifie si la m�thode {@link #format} doit aussi �crire la valeur apr�s la
     * coordonn�e.
     */
    public void setValueVisible(final boolean valueVisible) {
        this.valueVisible = valueVisible;
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant les coordonn�es point�es par le curseur
     * de la souris.  Les coordonn�es seront �crites selon le syst�me de coordonn�es sp�cifi�
     * lors du dernier appel de {@link #setCoordinateSystem}. Si une des couches peut ajouter
     * une valeur � la coordonn�e (par exemple une couche qui repr�sente une image satellitaire
     * de temp�rature) et que l'�criture des valeurs est autoris�e (voir {@link #isValueVisible}),
     * alors la valeur sera �crite apr�s les coordonn�es.
     *
     * @param  event Ev�nements contenant les coordonn�es de la souris.
     * @return Cha�ne de caract�res repr�sentant les coordonn�es point�es
     *         par le curseur de la souris.
     */
    public String format(final GeoMouseEvent event) {
        final Renderer renderer = event.context.renderer;
        try {
            point = event.getCoordinate(getCoordinateSystem(), point);
        } catch (TransformException exception) {
            return "ERROR";
        }
        buffer.setLength(0);
        format(point, buffer, null);
        if (valueVisible) {
            final int length = buffer.length();
            buffer.append("  (");
            if (renderer.formatValue(event, buffer)) {
                buffer.append(')');
            } else {
                buffer.setLength(length);
            }
        }
        return buffer.toString();
    }
}
