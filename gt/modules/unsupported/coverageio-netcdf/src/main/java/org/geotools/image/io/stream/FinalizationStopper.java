/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
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
 * Stop l'exécution de {@link System#runFinalization} lorsqu'un délai a été dépassé. Cette méthode
 * est utilisée pour éviter les bloquages que l'on observe parfois.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FinalizationStopper extends TimerTask {
    /**
     * Le timer pour arrêter les exécution de {@link System#runFinalization}.
     */
    private static final Timer TIMER = new Timer("Finalization stopper", true);

    /**
     * Le processus à arrêter.
     */
    private final Thread toStop;

    /**
     * Construit une nouvelle tâche qui arrêtera le processus courant.
     */
    FinalizationStopper() {
        this.toStop = Thread.currentThread();
        TIMER.schedule(this, 4000);
    }

    /**
     * Interrompt l'exécution.
     */
    public void run() {
        toStop.interrupt();
    }
}
