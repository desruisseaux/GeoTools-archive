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
package org.geotools.renderer.geom;

// J2SE dependencies
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.geotools.renderer.array.ArrayData;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.geotools.util.WeakHashSet;


/**
 * Soft reference for <code>float[]</code> array of (<var>x</var>,<var>y</var>) coordinates.
 * There is at most one instance of this class for each instance of {@link Polyline}. This
 * class is strictly for internal use by {@link PolygonPathIterator}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class PolylineCache {
    /**
     * Cache vers les transformation affine d�j� cr��es. Cette cache utilise des r�f�rences
     * faibles pour ne retenir les transformations que si ells sont d�j� utilis�es ailleurs
     * dans la machine virtuelle. <strong>Tous les objets plac�s dans cette cache devraient
     * �tre laiss�s constants (immutables).</strong>
     */
    private static final WeakHashSet pool = new WeakHashSet();

    /**
     * Transformation affine identit�. Cette transformation affine
     * sera partag�e par plusieurs objets {@link PolylineCache} et
     * ne doit pas �tre modifi�e.
     */
    private static final AffineTransform IDENTITY = new AffineTransform();

    /**
     * Transformation affine qui avait �t� utilis�e pour transformer les donn�es cach�e.
     */
    private AffineTransform transform = IDENTITY;

    /**
     * Nombre d'objets {@link PathIterator} qui utilisent le tableau de points {@link #array}.
     * Ce nombre sera incr�ment� � chaque appel de {@link #getRenderingArray} et d�cr�ment�
     * par {@link #releaseRenderingArray}.
     */
    private short lockCount;

    /**
     * The array of (<var>x</var>,<var>y</var>) coordinates.   It may be either a strong
     * reference to <code>float[]</code>, a soft {@link Reference}, or <code>null</code>.
     */
    private Object array;

    /**
     * Number of valid elements in {@link #array}. This is twice the number of valid points.
     */
    private int length;

    /**
     * <code>true</code> if the last call of {@link #getRenderingArray} has recomputed
     * the cache array. This information is for statistics purpose only.
     */
    private boolean recomputed;

    /**
     * <code>true</code> if the flag <code>recomputed</code> should be reset during the next
     * invocation of {@link #getRenderingArray}.  This flag is set to <code>true</code> when
     * the user invokes {@link #getPointCount}, which is an indication that the rendering is
     * probably finished and the next call to {@link #getRenderingArray} will probably be for
     * a new rendering.
     */
    private boolean reset;

    /**
     * Codes computed by {@link ArrayData#curves}. We don't have to touch it here.
     */
    private int[] curves;

    /**
     * Construct a new empty cache. This constructor is used by {@link Polyline#getCache} only.
     */
    PolylineCache() {
    }

    /**
     * Returns an array of decimated and transformed (<var>x</var>,<var>y</var>) coordinates.
     * The method {@link #releaseRenderingArray} <strong>must</strong> be invoked once the
     * rendering is finished.
     *
     * @param  polyline The source polyline.
     * @param  destination The destination iterator. The {@link ArrayData#array} field will be
     *         set to a direct reference to cache's internal data. Consequently, data should not
     *         be modified outside this <code>getRenderingArray</code> method.
     * @param  newTransform Transformation affine � appliquer sur les donn�es. La valeur
     *         <code>null</code> sera interpr�t�e comme �tant la transformation identit�e.
     *
     * @see #releaseRenderingArray
     * @see #getPointCount
     */
    public void getRenderingArray(final Polyline polyline,
                                  final ArrayData destination,
                                  AffineTransform newTransform)
    {
        assert polyline.isFrozen() || Thread.holdsLock(polyline);
        if (reset) {
            reset      = false;
            recomputed = false;
        }
        if (newTransform != null) {
            newTransform = (AffineTransform) pool.canonicalize(new AffineTransform(newTransform));
            // TODO: This line may fill 'pool' with a lot of entries
            //       (>100) when the user change zoom often (e.g. is
            //       scrolling). Should we look for an other way?
        } else {
            newTransform = IDENTITY;
        }
        /*
         * Gets the cached array (which may be a strong or a soft reference) and cast it
         * to type 'float[]'. The 'array' local variable hide the 'array' class variable,
         * but both of them always refer to the same object.  The local variable is just
         * casted to 'float[]'.
         */
        if (array instanceof Reference) {
            array = ((Reference) array).get();
        }
        float[] array = (float[]) this.array;
        /*
         * Si la transformation affine n'a pas chang� depuis la derni�re fois, alors on pourra
         * retourner le tableau directement.  Sinon, on tentera de modifier les coordonn�es en
         * prenant en compte seulement le **changement** de la transformation affine depuis la
         * derni�re fois.   Mais cette �tape ne sera faite qu'� la condition que le tableau ne
         * soit pas en cours d'utilisation par un autre it�rateur (lockCount==0).
         */
        if (array != null) {
            // If we are using this array for the second time, it may be worth to trim it...
            this.array = array = XArray.resize(array, length);
            if (newTransform.equals(transform)) {
                lockCount++;
                destination.setData(array, length, curves);
                // No change to the 'recomputed' flag,  because this case occurs often
                // during a single drawing process (for example Graphics2D.fill(shape)
                // followed immediately by Graphics2D.draw(shape)).
                return;
            }
            if (lockCount == 0) try {
                final AffineTransform change = transform.createInverse();
                change.preConcatenate(newTransform);
                change.transform(array, 0, array, 0, length/2);
                transform  = newTransform;
                lockCount  = 1;
                recomputed = false;
                destination.setData(array, length, curves);
                return;
            } catch (NoninvertibleTransformException exception) {
                Utilities.unexpectedException("org.geotools.renderer.geom", "Polyline",
                                              "getPathIterator", exception);
                // Continue... On va simplement reconstruire le tableau � partir de la base.
            } else {
                // Should be uncommon. Doesn't hurt, but may be a memory issue for big polyline.
                Polyline.LOGGER.info(Resources.format(ResourceKeys.WARNING_EXCESSIVE_MEMORY_USAGE));
                this.array = array = new float[32];
            }
        } else {
            this.array = array = new float[32];
        }
        /*
         * Reconstruit le tableau de points � partir des donn�es de bas niveau.
         * La projection cartographique sera appliqu�e par {@link Polyline#toArray}.
         */
        destination.setData(array, 0, null);
        polyline.toArray(destination, polyline.getRenderingResolution());
        this.array = array = destination.array();
        this.length = destination.length();
        this.curves = destination.curves();
        assert (length & 1) == 0;
        if (array.length >= 2*length) {
            // If the array is much bigger then needed, trim to size.
            this.array = array = XArray.resize(array, length);
            destination.setData(array, length, curves);
        }
        lockCount  = 1;
        transform  = newTransform;
        transform.transform(array, 0, array, 0, length/2);
        recomputed = true;
    }

    /**
     * Signal that an array is no longer in use. This method <strong>must</strong>
     * be invoked after {@link #getRenderingArray}.
     *
     * @param array The array to release (got from {@link #getRenderingArray}).
     *
     * @task TODO: in some future version, we should wait a little bit longer
     *             before to change a strong reference into a soft one.
     */
    final void releaseRenderingArray(final float[] array) {
        if (array == null) {
            return;
        }
        final Object intern = this.array;
        if (intern == array) {
            // TODO: in some future version, we should wait a little bit longer
            //       before to change a strong reference into a soft one.
            this.array = new SoftReference(array);
        }
        else if (!(intern instanceof Reference) || ((Reference)intern).get()!=array) {
            // This cache doesn't own the array. Nothing to do.
            return;
        }
        lockCount--;
        assert lockCount >= 0;
    }

    /**
     * Returns the number of points in the cache. This method is invoked only for statistics
     * purpose after a rendering. The number of points is the absolute value of the returned
     * value. A positive value means that the cache has been reused. A negative value means
     * that the cache has been flushed and recomputed.
     */
    public final int getPointCount() {
        reset = true;
        final int n = length/2;
        return recomputed ? -n : n;
    }
}
