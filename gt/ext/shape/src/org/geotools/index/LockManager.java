/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.index;

/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 * @source $URL$
 */
public class LockManager {
    private static final int EXCLUSIVE_LOCK_TIMEOUT = 20;
    private static final int SHARED_LOCK_TIMEOUT = 10;
    public static final short READ = 1;
    public static final short WRITE = 2;
    private Lock exclusiveLock;
    private int leases;

    public LockManager() {
    }

    public synchronized void release(Lock lock) {
        LockImpl li = (LockImpl) lock;

        if (li.getType() == Lock.EXCLUSIVE) {
            this.exclusiveLock = null;
        } else {
            this.leases--;
        }

        this.notify();
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws LockTimeoutException DOCUMENT ME!
     */
    public synchronized Lock aquireExclusive() throws LockTimeoutException {
        int cnt = 0;

        while (((this.exclusiveLock != null) || (this.leases > 0))
                && (cnt < EXCLUSIVE_LOCK_TIMEOUT)) {
            cnt++;

            try {
                this.wait(500);
            } catch (InterruptedException e) {
                throw new LockTimeoutException(e);
            }
        }

        if ((this.exclusiveLock != null) || (this.leases > 0)) {
            throw new LockTimeoutException("Timeout aquiring exclusive lock");
        }

        this.exclusiveLock = new LockImpl(Lock.EXCLUSIVE);

        return this.exclusiveLock;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws LockTimeoutException DOCUMENT ME!
     */
    public synchronized Lock aquireShared() throws LockTimeoutException {
        int cnt = 0;

        while ((this.exclusiveLock != null) && (cnt < SHARED_LOCK_TIMEOUT)) {
            cnt++;

            try {
                this.wait(500);
            } catch (InterruptedException e) {
                throw new LockTimeoutException(e);
            }
        }

        if (this.exclusiveLock != null) {
            throw new LockTimeoutException("Timeout aquiring shared lock");
        }

        this.leases++;

        return new LockImpl(Lock.SHARED);
    }

    /**
     * DOCUMENT ME!
     *
     * @author Tommaso Nolli
     */
    private class LockImpl implements Lock {
        private short type;

        /**
         * DOCUMENT ME!
         *
         * @param type
         */
        public LockImpl(short type) {
            this.type = type;
        }

        /**
         * @see org.geotools.index.Lock#getType()
         */
        public short getType() {
            return this.type;
        }
    }
}
