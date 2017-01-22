package rocks.appconcept.javatools.mime;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rocks.appconcept.javatools.binary.ByteArrayFinder;

/**
 * Simple helper for parsing a multi-part post request from a browser.
 *
 * @author yanimetaxas
 */
public class MultipartRequest {

  private static Charset US_ASCII = Charset.forName("US-ASCII");
  private static final Pattern BOUNDARY_LOCATOR = Pattern.compile(".*boundary=([^;]+);?");

  private final List<Part> parts;

  private MultipartRequest(List<Part> parts) {
    this.parts = parts;
  }

  public List<Part> getParts() {
    return parts;
  }

  public Part getPart(String name) {
    for (Part part : parts) {
      if (name.equals(part.getName())) {
        return part;
      }
    }
    return null;
  }

  public static MultipartRequest parse(String contentType, byte[] requestBody) {
    List<Part> result = new ArrayList<>();
    Matcher matcher = BOUNDARY_LOCATOR.matcher(contentType);
    if (matcher.matches()) {
      byte[] boundary = ("--" + matcher.group(1)).getBytes(Charset.forName("UTF-8"));
      List<Integer> indexes = ByteArrayFinder.findAllIndexes(boundary, requestBody);
      if (!indexes.isEmpty()) {
        int start = indexes.remove(0);
        for (int end : indexes) {
          int offset = start + boundary.length + 2;
          result.add(new Part(requestBody, offset, end - 2));
          start = end;
        }
      }
    }

    return new MultipartRequest(Collections.unmodifiableList(result));
  }

  public static class Part {

    private final Map<String, List<String>> headers = new HashMap<>();
    private final byte[] data;
    private final int offset;
    private final int binaryLength;
    private String name;
    private String fileName;

    public String getName() {
      return name;
    }

    public String getSubmittedFileName() {
      return fileName;
    }

    public long getBinarySize() {
      return binaryLength;
    }

    public String getContentType() {
      return getHeader("Content-Type");
    }

    public String getStringValue() {
      return new String(data, offset, binaryLength, Charset.forName("UTF-8"));
    }

    public byte[] getBinaryValue() {
      if ("base64".equalsIgnoreCase(getHeader("Content-Transfer-Encoding"))) {
        return Base64.getMimeDecoder().decode(ByteBuffer.wrap(data, offset, binaryLength)).array();
      }

      byte[] bytes = new byte[binaryLength];
      System.arraycopy(data, offset, bytes, 0, binaryLength);
      return bytes;
    }

    public String getHeader(String name) {
      name = name.toLowerCase();
      return headers.containsKey(name) ? headers.get(name).get(0) : null;
    }

    public Collection<String> getHeaders(String name) {
      name = name.toLowerCase();
      return headers.get(name);
    }

    public Collection<String> getHeaderNames() {
      return headers.keySet(); // transform to correct casing?
    }

    private Part(byte[] data, int offset, int end) {
      int cursor = offset;

      while (data[cursor] != 0x0d || data[cursor + 1] != 0x0a) {

        int headerNameStart = cursor;
        while (data[cursor] != ':') {
          cursor += 1;
        }
        int headerNameEnd = cursor;
        String headerName = new String(data, headerNameStart, (headerNameEnd - headerNameStart),
            US_ASCII);

        cursor += 1; // skip ":"
        if (data[cursor] == ' ') {
          cursor += 1; // optionally skip " "
        }

        int headerValueStart = cursor;
        while (data[cursor] != 0x0d) {
          cursor += 1;
        }
        int headerValueEnd = cursor;
        String headerValue = new String(data, headerValueStart, (headerValueEnd - headerValueStart),
            US_ASCII);

        cursor += 2; // skip nl

        headers.compute(headerName.toLowerCase(), (k, v) -> {
          List<String> list = v == null ? new ArrayList<>() : v;
          list.add(headerValue);
          return list;
        });

        if (headerName.equalsIgnoreCase("content-disposition")) {
          for (String p : headerValue.split(";\\s*")) {
            if (p.startsWith("name=")) {
              this.name = p.substring("name=".length()).trim().replace("\"", "");
            }
            if (p.startsWith("filename=")) {
              this.fileName = p.substring("filename=".length()).trim().replace("\"", "");
              int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
              if (slash >= 0) {
                fileName = fileName.substring(slash + 1);
              }
            }
          }
        }
      }
      cursor += 2; // skip "\n"

      this.data = data;
      this.offset = cursor;
      this.binaryLength = end - cursor;
    }
  }
}
