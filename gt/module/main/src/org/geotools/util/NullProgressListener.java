/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
package org.geotools.util;


/**
 * A default progress listener implementation suitable for
 * subclassing.
 * <p>
 * This implementation supports cancelation and getting/setting the description.
 * The default implementations of the other methods do nothing.
 * </p>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 */
public class NullProgressListener implements ProgressListener {
    /**
     * Description of the undergoing action.
     */
    private String description;

    /**
     * {@code true} if the action is canceled.
     */
    private boolean canceled = false;

    /**
     * Creates a null progress listener with no description.
     */
    public NullProgressListener() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void started() {
        //do nothing
    }

    public void progress(float percent) {
        //do nothing
    }

    public void complete() {
        //do nothing
    }

    public void dispose() {
        //do nothing
    }

    public void setCanceled(boolean cancel) {
        this.canceled = cancel;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void warningOccurred(String source, String margin, String warning) {
        //do nothing
    }

    public void exceptionOccurred(Throwable exception) {
        //do nothing
    }
}
