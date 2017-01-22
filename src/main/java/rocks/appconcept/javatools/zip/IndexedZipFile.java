package rocks.appconcept.javatools.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Indexed zip file reader. <p>Returns input streams for archived files in constant(-ish) time.</p>
 * <p><b>Note:</b> Does not do any CRC32 verification.</p> <p>Tries to handle filename encoding
 * according to the "spec": http://www.pkware.com/documents/casestudies/APPNOTE.TXT</p>
 *
 * @author yanimetaxas
 */
public class IndexedZipFile {

  /* The binary file contents */
  private final byte[] file;
  /* Filename -> Entry mapping for fast access */
  private Map<String, IndexEntry> index;

  /**
   * Create an indexed zip file from the specified byte array
   *
   * @param file The entire zip file
   */
  public IndexedZipFile(byte[] file) {
    this.file = file;
    indexFile();
  }

  /**
   * Returns a collection of the file names contained in the zip file.
   *
   * @return The names of the zip entries in the zip file
   */
  public Collection<String> getFileNames() {
    return index.keySet();
  }

  /**
   * Updates the specified digest with vital information on the contents of the file, while
   * excluding some files.
   *
   * @param signature The signature to update.
   * @param excluded The filenames to not include in the signature update. Can be null to include
   * all content.
   */
  public void updateMessageDigest(MessageDigest signature, Collection<String> excluded) {
    for (Map.Entry<String, IndexEntry> entry : index.entrySet()) {
      if (excluded == null || !excluded.contains(entry.getKey())) {
        IndexEntry value = entry.getValue();
        signature.update(file, value.offset, value.streamLength);
      }
    }
  }

  /**
   * Returns an input stream for the specified entry.
   *
   * @param name The name of the entry.
   * @return The input stream for the specified entry, or null if no such entry could be found.
   */
  public InputStream getInputStream(String name) {
    InputStream res = null;

    if (index.containsKey(name)) {
      IndexEntry entry = index.get(name);
      if (entry.streamLength == 0) {
        return EmptyInputStream.instance;
      }
      if (entry.compressionMethod == 0) {
        res = new ByteArrayInputStream(file, entry.offset, entry.streamLength);
      } else if (entry.compressionMethod == 8) {
        // NOTE: The Inflater(true) requires that the inputstream be padded with a single zero-byte
        res = new InflaterInputStream(
            new ZeroPaddedByteArrayInputStream(file, entry.offset, entry.streamLength),
            new Inflater(true));
      } else {
        throw new IllegalArgumentException("Unknown compression method.");
      }
    }

    return res;
  }

  /**
   * Returns the uncompressed size of the specified entry.
   *
   * @param name The name of the entry.
   * @return The length in bytes of the specified entry, or -1 if no such entry could be found.
   */
  public int getSize(String name) {
    IndexEntry entry = index.get(name);
    if (entry == null) {
      return -1;
    }
    return entry.uncompressed;
  }

  /* Entry, describing where in the binary file to find the compressed stream */
  private static class IndexEntry {

    int offset;
    int compressionMethod;
    int streamLength;
    int uncompressed;
  }

  /* ByteArrayInputStream wrapper that returns an extra zero byte (necessary for the Inflater) */
  private static class ZeroPaddedByteArrayInputStream extends InputStream {

    private byte[] buf;
    private int pos, count;

    public ZeroPaddedByteArrayInputStream(byte[] buf, int offset, int length) {
      this.buf = buf;
      this.pos = offset;
      this.count = Math.min(offset + length, buf.length);
    }

    @Override
    public int read() throws IOException {
      return pos < count ? (buf[pos++] & 0xff) : (pos == count ? 0 : -1);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (pos > count) {
        return -1;
      }
      if (pos == count) {
        b[off] = 0;
        pos++;
        return 1;
      }
      if (pos + len > count) {
        len = count - pos;
      }
      System.arraycopy(buf, pos, b, off, len);
      pos += len;
      return len;
    }

    @Override
    public long skip(long n) throws IOException {
      if (pos + n > count) {
        n = count - pos;
      }
      if (n < 0) {
        return 0;
      }
      pos += n;
      return n;
    }
  }

  /* Fast inputstream without any input */
  private static class EmptyInputStream extends InputStream {

    public static final InputStream instance = new EmptyInputStream();

    @Override
    public int read() throws IOException {
      return -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
      return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return -1;
    }

    @Override
    public long skip(long n) throws IOException {
      return 0;
    }

    @Override
    public int available() throws IOException {
      return 0;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public void reset() throws IOException {
    }

    @Override
    public boolean markSupported() {
      return false;
    }
  }

  /*
   * Most of this code has been ported from the tunpack.dll library from TCserver.
   */
  private void indexFile() {

    index = new LinkedHashMap<String, IndexEntry>();
    int p = 0;

    for (p = file.length - 22; p > 0; p--) {
      if (getDword(file, p) == 0x06054b50) {
        int startofCDR = (int) getDword(file, p + 16);
        if (getDword(file, startofCDR) == 0x02014b50) {
          indexFileInternal(startofCDR);
          return;
        }
      }
    }
    throw new IllegalArgumentException("Bad ZIP file. No Central Directory.");
  }

  private void indexFileInternal(int startofCDR) {
    int p = startofCDR;
    int flags, method, fnamelen, extralen, commentlen;
    int localptr;
    long compressed, uncompressed; // crc32;
    int left = file.length - p;
    for (; ; ) {
      if (left < 4) {
        break;
      }
      long sig = getDword(file, p);
      p += 4;
      left -= 4;
      if (sig != 0x02014b50) {
        break;
      }

      if (left < 42) {
        throw new IllegalArgumentException("Bad ZIP file. Premature end.");
      }
      p += 2; /* skip version made by */
      p += 2; /* skip version to extract */
      flags = getWord(file, p);
      p += 2;
      method = getWord(file, p);
      p += 2;
      p += 4; /* skip filetimes */
      p += 4; /* skip crc32 */
      compressed = getDword(file, p);
      p += 4;
      uncompressed = getDword(file, p);
      p += 4;
      fnamelen = getWord(file, p);
      p += 2;
      extralen = getWord(file, p);
      p += 2;
      commentlen = getWord(file, p);
      p += 2;
      p += 8; /* skip disk numbers + attributes */
      localptr = (int) getDword(file, p);
      p += 4;
      left -= 42;

      if (left < fnamelen) {
        throw new IllegalArgumentException("Bad ZIP file. Premature end.");
      }

      String name;
      // BIT 11 -> UTF-8 else IBM Codepage 437  (see http://www.pkware.com/documents/casestudies/APPNOTE.TXT )
      if ((flags & (1 << 11)) == 0) {
        char[] namec = new char[fnamelen];
        for (int i = 0; i < fnamelen; i++) {
          namec[i] = cp437chars[file[p + i] & 0xff];
        }
        name = new String(namec);
      } else {
        name = getUTF8String(file, p, fnamelen);
      }

      p += fnamelen;
      left -= fnamelen;
      if (left < extralen) {
        throw new IllegalArgumentException("Bad ZIP file. Premature end.");
      }
      p += extralen;
      left -= extralen;
      if (left < commentlen) {
        throw new IllegalArgumentException("Bad ZIP file. Premature end.");
      }
      p += commentlen;
      left -= commentlen;

      int localskip = 30 + getWord(file, localptr + 26) + getWord(file, localptr + 28);
      IndexEntry entry = new IndexEntry();
      entry.offset = localptr + localskip;
      entry.compressionMethod = method;
      entry.streamLength = (int) compressed;
      entry.uncompressed = (int) uncompressed;
      index.put(name, entry);
    }
  }

  /* Helper function to read a little endian 16-bit word from a byte array */
  private static int getWord(byte[] file, int p) {
    return (file[p] & 0xff) | ((file[p + 1] & 0xff) << 8);
  }

  /* Helper function to read a little endian 32-bit dword from a byte array */
  private static long getDword(byte[] file, int p) {
    return ((file[p] & 0xff) | ((file[p + 1] & 0xff) << 8) | ((file[p + 2] & 0xff) << 16) | (
        (file[p + 3] & 0xff) << 24));
  }

  /*
   * Fetches a UTF8-encoded String from the specified byte array.
   * Stolen from java.util.ZipInputStream without permission.
   */
  private static String getUTF8String(byte[] b, int off, int len) {
    // First, count the number of characters in the sequence
    int count = 0;
    int max = off + len;
    int i = off;
    while (i < max) {
      int c = b[i++] & 0xff;
      switch (c >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          // 0xxxxxxx
          count++;
          break;
        case 12:
        case 13:
          // 110xxxxx 10xxxxxx
          if ((b[i++] & 0xc0) != 0x80) {
            throw new IllegalArgumentException();
          }
          count++;
          break;
        case 14:
          // 1110xxxx 10xxxxxx 10xxxxxx
          if (((b[i++] & 0xc0) != 0x80) ||
              ((b[i++] & 0xc0) != 0x80)) {
            throw new IllegalArgumentException();
          }
          count++;
          break;
        default:
          // 10xxxxxx, 1111xxxx
          throw new IllegalArgumentException();
      }
    }
    if (i != max) {
      throw new IllegalArgumentException();
    }
    // Now decode the characters...
    char[] cs = new char[count];
    i = 0;
    while (off < max) {
      int c = b[off++] & 0xff;
      switch (c >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          // 0xxxxxxx
          cs[i++] = (char) c;
          break;
        case 12:
        case 13:
          // 110xxxxx 10xxxxxx
          cs[i++] = (char) (((c & 0x1f) << 6) | (b[off++] & 0x3f));
          break;
        case 14:
          // 1110xxxx 10xxxxxx 10xxxxxx
          int t = (b[off++] & 0x3f) << 6;
          cs[i++] = (char) (((c & 0x0f) << 12) | t | (b[off++] & 0x3f));
          break;
        default:
          // 10xxxxxx, 1111xxxx
          throw new IllegalArgumentException();
      }
    }
    return new String(cs, 0, count);
  }

  /* Code page 437 mapping. Stolen from http://unicode.org/Public/MAPPINGS/VENDORS/MICSFT/PC/CP437.TXT */
  private static final char[] cp437chars = new char[]{
      0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009, 0x000a,
      0x000b, 0x000c, 0x000d, 0x000e, 0x000f,
      0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017, 0x0018, 0x0019, 0x001a,
      0x001b, 0x001c, 0x001d, 0x001e, 0x001f,
      0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a,
      0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
      0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a,
      0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
      0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a,
      0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
      0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a,
      0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
      0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a,
      0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
      0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a,
      0x007b, 0x007c, 0x007d, 0x007e, 0x007f,
      0x00c7, 0x00fc, 0x00e9, 0x00e2, 0x00e4, 0x00e0, 0x00e5, 0x00e7, 0x00ea, 0x00eb, 0x00e8,
      0x00ef, 0x00ee, 0x00ec, 0x00c4, 0x00c5,
      0x00c9, 0x00e6, 0x00c6, 0x00f4, 0x00f6, 0x00f2, 0x00fb, 0x00f9, 0x00ff, 0x00d6, 0x00dc,
      0x00a2, 0x00a3, 0x00a5, 0x20a7, 0x0192,
      0x00e1, 0x00ed, 0x00f3, 0x00fa, 0x00f1, 0x00d1, 0x00aa, 0x00ba, 0x00bf, 0x2310, 0x00ac,
      0x00bd, 0x00bc, 0x00a1, 0x00ab, 0x00bb,
      0x2591, 0x2592, 0x2593, 0x2502, 0x2524, 0x2561, 0x2562, 0x2556, 0x2555, 0x2563, 0x2551,
      0x2557, 0x255d, 0x255c, 0x255b, 0x2510,
      0x2514, 0x2534, 0x252c, 0x251c, 0x2500, 0x253c, 0x255e, 0x255f, 0x255a, 0x2554, 0x2569,
      0x2566, 0x2560, 0x2550, 0x256c, 0x2567,
      0x2568, 0x2564, 0x2565, 0x2559, 0x2558, 0x2552, 0x2553, 0x256b, 0x256a, 0x2518, 0x250c,
      0x2588, 0x2584, 0x258c, 0x2590, 0x2580,
      0x03b1, 0x00df, 0x0393, 0x03c0, 0x03a3, 0x03c3, 0x00b5, 0x03c4, 0x03a6, 0x0398, 0x03a9,
      0x03b4, 0x221e, 0x03c6, 0x03b5, 0x2229,
      0x2261, 0x00b1, 0x2265, 0x2264, 0x2320, 0x2321, 0x00f7, 0x2248, 0x00b0, 0x2219, 0x00b7,
      0x221a, 0x207f, 0x00b2, 0x25a0, 0x00a0};
}
