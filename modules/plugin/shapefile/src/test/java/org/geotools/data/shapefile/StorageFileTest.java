package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShpFileType.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Map;

import junit.framework.TestCase;

public class StorageFileTest extends TestCase implements FileReader {

    private ShpFiles shpFiles1;
    private ShpFiles shpFiles2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<ShpFileType, File> files1 = ShpFilesTest.createFiles("Files1",
                ShpFileType.values(), false);
        Map<ShpFileType, File> files2 = ShpFilesTest.createFiles("Files2",
                ShpFileType.values(), false);

        shpFiles1 = new ShpFiles(files1.get(SHP));
        shpFiles2 = new ShpFiles(files2.get(SHP));
    }

    public void testReplaceOriginal() throws Exception {
        ShpFiles files1 = shpFiles1;
        ShpFileType type = PRJ;
        StorageFile storagePRJ1 = files1.getStorageFile(type);
        String writtenToStorageFile = "Copy";

        writeData(storagePRJ1, writtenToStorageFile);

        storagePRJ1.replaceOriginal();
        assertEquals(0, files1.numberOfLocks());

        assertCorrectData(files1, type, writtenToStorageFile);
    }

    private void writeData(StorageFile storage, String writtenToStorageFile)
            throws IOException {
        File file = storage.getFile();
        file.deleteOnExit();

        FileWriter writer = new FileWriter(file);

        writer.write(writtenToStorageFile);

        writer.close();
    }

    private void assertCorrectData(ShpFiles files1, ShpFileType type,
            String writtenToStorageFile) throws IOException {
        ReadableByteChannel channel = files1.getReadChannel(type, this);
        try {
            ByteBuffer buffer = ByteBuffer.allocate(20);
            channel.read(buffer);
            buffer.flip();
            String data = new String(buffer.array()).trim();
            assertEquals(writtenToStorageFile, data);
        } finally {
            channel.close();
        }
    }

    public void testReplaceOriginals() throws Exception {

        StorageFile storagePRJ1 = shpFiles1.getStorageFile(PRJ);
        StorageFile storageSHP1 = shpFiles1.getStorageFile(SHP);
        StorageFile storagePRJ2 = shpFiles2.getStorageFile(PRJ);
        StorageFile storageSHP2 = shpFiles2.getStorageFile(SHP);

        String sPRJ1 = "storagePRJ1";
        String sSHP1 = "storageSHP1";
        String sPRJ2 = "storagePRJ2";
        String sSHP2 = "storageSHP2";

        writeData(storagePRJ1, sPRJ1);
        writeData(storageSHP1, sSHP1);
        writeData(storagePRJ2, sPRJ2);
        writeData(storageSHP2, sSHP2);

        StorageFile.replaceOriginals(storagePRJ1, storagePRJ2, storageSHP1,
                storageSHP2, storageSHP2);

        this.assertCorrectData(shpFiles1, PRJ, sPRJ1);
        this.assertCorrectData(shpFiles1, SHP, sSHP1);
        this.assertCorrectData(shpFiles2, PRJ, sPRJ2);
        this.assertCorrectData(shpFiles2, SHP, sSHP2);

        assertEquals(0, shpFiles1.numberOfLocks());
        assertEquals(0, shpFiles2.numberOfLocks());

    }

    public void testReplaceOriginalsEmptyArgs() throws Exception {

        StorageFile.replaceOriginals(new StorageFile[0]);

    }

    public void testCompareTo() throws IOException {
        StorageFile storagePRJ1 = shpFiles1.getStorageFile(PRJ);
        StorageFile storageSHP1 = shpFiles1.getStorageFile(SHP);
        StorageFile storagePRJ2 = shpFiles2.getStorageFile(PRJ);
        StorageFile storageSHP2 = shpFiles2.getStorageFile(SHP);

        assertEquals(-1, storagePRJ1.compareTo(storageSHP1));
        assertEquals(1, storagePRJ1.compareTo(storagePRJ2));

        StorageFile[] array = new StorageFile[] { storagePRJ1, storagePRJ2,
                storageSHP1, storageSHP2 };

        Arrays.sort(array);

        assertEquals(-1, array[0].compareTo(array[1]));
        assertEquals(-1, array[2].compareTo(array[3]));
        assertEquals(1, array[1].compareTo(array[2]));
    }

    public String id() {
        return getClass().getName();
    }

}
