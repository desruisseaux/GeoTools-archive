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
package org.geotools.image.io;

// J2SE dependencies
import javax.swing.event.EventListenerList;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOReadProgressListener;


/**
 * A container of image I/O listeners. This class provides a set of {@code addFooListener(...)}
 * and {@code removeFooListener(...)} methods for adding and removing various listeners, and a
 * {@code addListenersTo(...)} method for copying listeners to the an image reader. This class is
 * convenient when {@code ImageReader.addFooListener(...)} can't be invoked directly because the
 * {@link ImageReader} instance is not yet know or available.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Add other listener types.
 */
public class IIOListeners {    
    /**
     * List of listeners.
     */
    private final EventListenerList listeners = new EventListenerList();
    
    /**
     * Creates a new instance of {@code IIOListeners}.
     */
    public IIOListeners() {
    }
        
    /**
     * Adds an {@code IIOReadWarningListener} to the list of registered warning listeners.
     */
    public void addIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.add(IIOReadWarningListener.class, listener);
    }
    
    /**
     * Removes an {@code IIOReadWarningListener} from the list of registered warning listeners.
     */
    public void removeIIOReadWarningListener(final IIOReadWarningListener listener) {
        listeners.remove(IIOReadWarningListener.class, listener);
    }
    
    /**
     * Adds an {@code IIOReadProgressListener} to the list of registered progress listeners.
     */
    public void addIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.add(IIOReadProgressListener.class, listener);
    }
    
    /**
     * Removes an {@code IIOReadProgressListener} from the list of registered progress listeners.
     */
    public void removeIIOReadProgressListener(final IIOReadProgressListener listener) {
        listeners.remove(IIOReadProgressListener.class, listener);
    }

    /**
     * Add all listeners registered in this object to the specified image reader.
     */
    public void addListenersTo(final ImageReader reader) {
        final Object[] listeners = this.listeners.getListenerList();
        for (int i=0; i<listeners.length;) {
            final Object classe   = listeners[i++];
            final Object listener = listeners[i++];
            if (IIOReadProgressListener.class.equals(classe)) {
                final IIOReadProgressListener l = (IIOReadProgressListener) listener;
                reader.removeIIOReadProgressListener(l); // Ensure singleton
                reader.   addIIOReadProgressListener(l);
                continue;
            }
            if (IIOReadWarningListener.class.equals(classe)) {
                final IIOReadWarningListener l = (IIOReadWarningListener) listener;
                reader.removeIIOReadWarningListener(l); // Ensure singleton
                reader.   addIIOReadWarningListener(l);
                continue;
            }
        }
    }
}
