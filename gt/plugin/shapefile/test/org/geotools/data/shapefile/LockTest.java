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
package org.geotools.data.shapefile;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * 
 * @author jones
 */
public class LockTest extends TestCase {
    class LockingThread extends Thread {
        private Lock lock;

        boolean unlocked = true;

        private boolean readlock;

        public LockingThread(Lock lockArg, boolean readlockArg) {
            this.lock = lockArg;
            this.readlock = readlockArg;
        }

        public synchronized boolean unlock() throws InterruptedException {
            if (unlocked)
                return false;

            notifyAll();
            int count = 0;
            while (!unlocked && count < 2) {
                wait(500);
                count++;
            }

            if (unlocked)
                return true;

            return false;
        }
        int DELAY=1;
        public synchronized boolean wakeup() throws InterruptedException {

            int count = 0;
            while ( unlocked && count < DELAY) {
                wait(500);
                count++;
            }

            if (unlocked)
                return false;

            return false;
        }

        public synchronized boolean doLock() throws InterruptedException {
            if (!unlocked)
                return false;

            start();
            int count = 0;
            while (unlocked && count < DELAY) {
                wait(500);
                count++;
            }

            if (!unlocked)
                return true;

            return false;
        }

        public void run() {
            if (readlock)
                readlock();
            else
                writelock();
        }

        private void readlock() {
            synchronized (lock) {

                try {
                    lock.lockRead();
                    unlocked = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            synchronized (lock) {
                lock.unlockRead();
                unlocked = true;
            }
        }

        private void writelock() {

            synchronized (lock) {
                try {
                    lock.lockWrite();
                    unlocked = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            synchronized (lock) {
                lock.unlockWrite();
                unlocked = true;
            }
        }
    }

    /*
     * Test method for 'org.geotools.data.shapefile.Lock.lockRead()'
     */
    public void testLockRead() throws Exception {
        Lock lock = new Lock();
        lock.lockRead();
        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());

        lock.lockRead();

        assertEquals(2, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());

        LockingThread thread = new LockingThread(lock, true);

        assertTrue(thread.doLock());

        assertTrue(lock.canRead());
        assertFalse(lock.canWrite());

        lock.unlockRead();

        assertEquals(1, lock.getReadLocks(thread));
        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertFalse(lock.canWrite());

        assertTrue(thread.unlock());

        assertEquals(-1, lock.getReadLocks(thread));
        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());

        lock.unlockRead();

        assertEquals(-1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());

        try {
            lock.unlockRead();
            fail("all locks should have been released");
        } catch (Throwable e) {
            // good
        }

    }

    /*
     * Test method for 'org.geotools.data.shapefile.Lock.lockWrite()'
     */
    public void testLockWrite() throws Exception {
        Lock lock = new Lock();

        lock.lockWrite();

        assertEquals(-1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());
        assertTrue(lock.ownWriteLock(Thread.currentThread()));

        lock.lockRead();

        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.canRead());
        assertTrue(lock.canWrite());

        lock.unlockRead();

        assertTrue(lock.ownWriteLock(Thread.currentThread()));

        LockingThread thread = new LockingThread(lock, false);

        assertFalse(thread.doLock());

        assertEquals(-1, lock.getReadLocks(thread));
        assertTrue(thread.unlocked);
        assertFalse(lock.ownWriteLock(thread));

        lock.unlockWrite();
        assertFalse(lock.ownWriteLock(Thread.currentThread()));

        assertFalse(thread.wakeup());

        assertEquals(-1, lock.getReadLocks(thread));
        assertTrue(lock.ownWriteLock(thread));

        assertTrue(thread.unlock());

        try {
            lock.unlockWrite();
            fail("already unlocked");
        } catch (Throwable e) {
            // good
        }
        
lock.lockRead();
        
        thread = new LockingThread(lock, false);
        
        assertFalse(thread.doLock());

        assertEquals(-1, lock.getReadLocks(thread));
        assertTrue(thread.unlocked);
        assertFalse(lock.ownWriteLock(thread));
        
        lock.unlockRead();

        assertFalse(thread.wakeup());
        
        assertTrue(lock.ownWriteLock(thread));
        assertEquals(-1, lock.getReadLocks(Thread.currentThread()));
        
    }
    
    public void testReadLockThenWriteLock() throws Exception {
        Lock lock = new Lock();
        
        lock.lockRead();
        
        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertFalse(lock.ownWriteLock(Thread.currentThread()));

        lock.lockWrite();
        assertEquals(1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.ownWriteLock(Thread.currentThread()));

        lock.unlockRead();
        assertEquals(-1, lock.getReadLocks(Thread.currentThread()));
        assertTrue(lock.ownWriteLock(Thread.currentThread()));
        
        lock.unlockWrite();
        assertEquals(-1, lock.getReadLocks(Thread.currentThread()));
        assertFalse(lock.ownWriteLock(Thread.currentThread()));
        
    }
}
