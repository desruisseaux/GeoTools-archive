/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le D�veloppement
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
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.array;

// Dependencies
import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * It�rateur balayant les donn�es d'un tableau {@link JTSArray}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class JTSIterator extends PointIterator {
    /**
     * Tableau de donn�es � balayer.
     */
    private final Coordinate[] coords;

    /**
     * Index suivant celui de la derni�re donn�e � balayer.
     */
    private final int upper;

    /**
     * Index de la prochaine donn�e � retourner.
     */
    private int index;

    /**
     * Construit un it�rateur qui balaiera la plage sp�cifi�e d'un tableau de donn�es.
     */
    public JTSIterator(Coordinate[] coords, int lower, int upper) {
        this.coords = coords;
        this.index  = lower;
        this.upper  = upper;
    }

    /**
     * Indique si les m�thodes {@link #next} peuvent retourner d'autres donn�es.
     */
    public boolean hasNext() {
        return index < upper;
    }

    /**
     * Retourne la valeur de la longitude courante. Avant d'appeller
     * une seconde fois cette m�thode, il faudra <g>obligatoirement</g>
     * avoir appel� {@link #nextY}.
     */
    public float nextX() {
        return (float)coords[index].x;
    }

    /**
     * Retourne la valeur de la latitude courante, puis avance au point
     * suivant. Chaque appel de cette m�thode doit <g>obligatoirement</g>
     * avoir �t� pr�c�d�e d'un appel � la m�thode {@link #nextX}.
     */
    public float nextY() {
        return (float)coords[index++].y;
    }

    /**
     * Retourne la valeur du point courant dans un objet {@link Point2D},
     * puis avance au point suivant. Cette m�thode combine un appel de
     * {@link #nextX} suivit de {@link #nextY}.
     */
    public Object next() {
        final Coordinate coord = coords[index++];
        return new Point2D.Double(coord.x, coord.y);
    }
}
