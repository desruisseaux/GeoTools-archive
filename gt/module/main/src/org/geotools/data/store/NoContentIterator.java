package org.geotools.data.store;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This iterator is used to indicate that contents could not be aquired.
 * <p>
 * The normal Collection.iterator() method does not let us return an error
 * (we always have to return an iterator). However Iterator.next() can
 * be used to return an NoSuchElementException.
 * </p>
 * <p>
 * So we are basically going to lie, we are going to pretend their is content
 * *once*, and when they ask for it we are going to hit them with
 * a NoSuchElementExcetion. This is a mean trick, but it does convey the idea
 * of asking for content that is supposed to be there and failing to aquire it.
 * </p>
 * @author jgarnett
 * @since 2.1.RC0
 * @source $URL$
 */
public class NoContentIterator implements Iterator {
    Throwable origionalProblem;
    public NoContentIterator( Throwable t ){
        origionalProblem = t;
    }
    public boolean hasNext() {
        return origionalProblem != null;
    }
    public Object next() {
        if( origionalProblem == null ){
            // you only get the real error on the first offense
            // (after that you are just silly)
            //
            throw new NoSuchElementException();            
        }
        NoSuchElementException cantFind = new NoSuchElementException( "Could not aquire feature:" + origionalProblem );
        cantFind.initCause( origionalProblem );
        origionalProblem = null;
        throw cantFind;
    }

    public void remove() {
        if( origionalProblem == null ){
            // user did not call next first
            throw new UnsupportedOperationException();
        }
        // User did not call next first
        throw new IllegalStateException();        
    }
}
