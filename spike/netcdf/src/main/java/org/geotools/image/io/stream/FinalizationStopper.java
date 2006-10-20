/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le D�veloppement
 *    (C) 2006, Geomatys
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
package org.geotools.image.io.stream;

// J2SE dependencies
import java.util.Timer;
import java.util.TimerTask;


/**
 * Stop l'ex�cution de {@link System#runFinalization} lorsqu'un d�lai a �t� d�pass�. Cette m�thode
 * est utilis�e pour �viter les bloquages que l'on observe parfois.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FinalizationStopper extends TimerTask {
    /**
     * Le timer pour arr�ter les ex�cution de {@link System#runFinalization}.
     */
    private static final Timer TIMER = new Timer("Finalization stopper", true);

    /**
     * Le processus � arr�ter.
     */
    private final Thread toStop;

    /**
     * Construit une nouvelle t�che qui arr�tera le processus courant.
     */
    FinalizationStopper() {
        this.toStop = Thread.currentThread();
        TIMER.schedule(this, 4000);
    }

    /**
     * Interrompt l'ex�cution.
     */
    public void run() {
        toStop.interrupt();
    }
}
