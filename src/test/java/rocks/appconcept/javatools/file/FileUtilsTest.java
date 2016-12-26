package rocks.appconcept.javatools.file;

import rocks.appconcept.javatools.CoverageTool;
import junit.framework.TestCase;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author yanimetaxas (2013-02-20)
 */
public class FileUtilsTest extends TestCase {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int LARGE_SIZE = 1024 * 1024 * 10 + 1;

    public void testStaticClass() throws Exception {
        CoverageTool.callPrivateConstructor(FileUtils.class);
    }

    public void testCopyStream() throws Exception {
        testCopyStream("Testing".getBytes(UTF8));
        testCopyStream(new byte[LARGE_SIZE]);
    }

    private void testCopyStream(byte[] src) throws IOException {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        assertEquals(src.length, FileUtils.copyStream(new ByteArrayInputStream(src), dest));
        assertTrue(Arrays.equals(src, dest.toByteArray()));
    }

    public void testReadFullyBytes() throws Exception {
        ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(zipBytes);
        zipOut.putNextEntry(new ZipEntry("Testing"));
        zipOut.write("Testing".getBytes(UTF8));
        zipOut.closeEntry();
        zipOut.putNextEntry(new ZipEntry("Testing2"));
        zipOut.write("Testing2".getBytes(UTF8));
        zipOut.closeEntry();
        zipOut.close();

        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipBytes.toByteArray()));
        for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
            assertEquals(entry.getName(), new String(FileUtils.readFully(zipIn), UTF8));
        }

        byte[] bytes = new byte[LARGE_SIZE];
        assertTrue(Arrays.equals(bytes, FileUtils.readFully(new ByteArrayInputStream(bytes))));
    }

    public void testReadWriteFileFully() throws Exception {
        testReadWriteFileFully("Testing".getBytes(UTF8));
        testReadWriteFileFully(new byte[LARGE_SIZE]);
    }

    private void testReadWriteFileFully(byte[] bytes) throws IOException {
        File tempFile = File.createTempFile("test", "");
        FileUtils.writeFully(tempFile, bytes);
        byte[] readBytes = FileUtils.readFully(tempFile);
        assertTrue(tempFile.delete());
        assertTrue(Arrays.equals(bytes, readBytes));
    }

    public void testReadFullyReader() throws Exception {
        assertEquals("Testing", FileUtils.readFully(new StringReader("Testing")));
        assertEquals("Testing\nTwo lines", FileUtils.readFully(new StringReader("Testing\r\nTwo lines")));
        String big = new String(new char[LARGE_SIZE]);
        assertEquals(big, FileUtils.readFully(new StringReader(big)));
    }


    public void testReaderFromStream() throws Exception {
        char [] chars = new char[3];
        String expected = "\u06de\u06de\u06de";
        FileUtils.readerFromStream(new ByteArrayInputStream(expected.getBytes(UTF8))).read(chars);
        assertEquals(expected, new String(chars));
    }
}
