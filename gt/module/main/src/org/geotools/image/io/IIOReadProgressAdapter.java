/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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

// Image I/O
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;


/**
 * An abstract adapter class for receiving image progress events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class IIOReadProgressAdapter implements IIOReadProgressListener {
    /**
     * Reports that a sequence of read operations is beginning.
     */
    public void sequenceStarted(ImageReader source, int minIndex) {
    }
    
    /**
     * Reports that a sequence of read operationshas completed.
     */
    public void sequenceComplete(ImageReader source) {
    }
    
    /**
     * Reports that an image read operation is beginning.
     */
    public void imageStarted(ImageReader source, int imageIndex) {
    }
    
    /**
     * Reports the approximate degree of completion of the current
     * <code>read</code> call of the associated <code>ImageReader</code>.
     */
    public void imageProgress(ImageReader source, float percentageDone) {
    }
    
    /**
     * Reports that the current image read operation has completed.
     */
    public void imageComplete(ImageReader source) {
    }
    
    /**
     * Reports that a thumbnail read operation is beginning.
     */
    public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
    }
    
    /**
     * Reports the approximate degree of completion of the current <code>getThumbnail</code>
     * call within the associated <code>ImageReader</code>.
     */
    public void thumbnailProgress(ImageReader source, float percentageDone) {
    }
    
    /**
     * Reports that a thumbnail read operation has completed.
     */
    public void thumbnailComplete(ImageReader source) {
    }
    
    /**
     * Reports that a read has been aborted via the reader's <code>abort</code> method.
     */
    public void readAborted(ImageReader source) {
    }
}
