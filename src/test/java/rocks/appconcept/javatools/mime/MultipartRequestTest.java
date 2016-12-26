package rocks.appconcept.javatools.mime;

import rocks.appconcept.javatools.file.FileUtils;
import junit.framework.TestCase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * @author yanimetaxas
 */
public class MultipartRequestTest extends TestCase {

    public void testBetaSticker1Png() throws Exception {
        String contentType = "multipart/form-data; boundary=\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/";
        String body = "--\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\r\n" +
                "Content-Disposition: form-data; name=\"sticker\"; filename=\"path\\beta-sticker-1.png\"\r\n" +
                "Content-Type: image/png\r\n" +
                "Content-Transfer-Encoding: base64\r\n" +
                "\r\n" +
                "iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAYAAADhAJiYAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABh5JREFUeNrMmHtIHEcYwGfv" +
                "5SNwaovxEanEiJKqlYCCTRo1f0SvDeof1legEcE/YttQaNOiaQjYFFtpKaJILZU8SCRUWqlJGpoWepGLTXqUEnzFxCrnK9DEelbvvPOe/WacuY7r7HmGFjrw" +
                "sbNzt7u//V7zfYvQ/2xI/9K1/NyvMP9PgCTuGmmL6/0ckD9UOGmbIExUsqMkAPHJjv5QwKRtgKioqDlh5+w/7IFeCuLlxCeA2zQ0IcCwh2qoaLH09fUdTElJ" +
                "2e/1elU+n0/y+9fvPz4+fvfYsWN3YOoBcXPiocLghD4mBYHhQTCErqWlZU9FRcXJqKiowyqVSk/uSEH4o8fjWVlYWDB2d3e3d3R0WGB5jYqLg/NyGgsKxMNg" +
                "kDB4451NTU3vxcXF1SlBKB0tFsuVxsbGjlu3bj2GJQeIk8K5RVBqBTMxrYRfuHAh9/jx4+ejo6MPS9I6f6hHPOC6rOLi4vyVlZXf7t27Z5c5/iZfkgMxxyUw" +
                "Fy9ezC0tLe3V6XRJ/MOCAYjWwsLCni0oKCh98uSJaWhoyMZFn0/uT2qBqYi/1NbWxjc0NJwPFUYExc/B53R5eXk5ZrN5YH5+3slFn5+D2uBDzG90IJETExOt" +
                "zGdC9RelNf78wYMH3xQWFn4Ep0sgyyCr1NmJP6kEIa5tbW3dEx8fXxeKRoJpT76OR3p6enllZWUKTCOwNalFAglWDkTCvLq6+uR2YYKZSw4GQVKNfZQCafjk" +
                "qhKYTBsTE3NY/uYi2Q4MP5KTkw9QGB3VEMv6G/YioqFLly5lazQavfytxobnUW+PWTGisIyNPEL3QYLB4PPIyMi4EydO7JUBbTIZ0RDYOFPkE8t/OdHczCK6" +
                "Y/qdzP8BfUTW8Tj/uQndvT1F5vOzVvTLz1PwX4cQbt++fekURsNpSNLIw16v1z/HLsRRgecsSnovm8nxs5bvUe+NN1Bz47fkfBaAXj2aA2BWEsM/3hhFX1/5" +
                "Fe3NTEAfvn8NXTO+tSH68IiNjU2Qw/AmCzg2XCQp+YyhJAu9c+pl9GJ+KmhiEt38bhjpoyJQRtYudA60k3dwD6o4mouKjmSiolcy0ArRqnXz3rT+knwFEShh" +
                "NKLNlmmFP7Kf8XxuehHpj0QQmLdPGch/ioYyCSAe57pMaHnJgcprctDdwUkRjKi8CUTWhipvbm7uvlJo3zFNoHJDOznPeGEXqn+9EBUf+AQZXvqU+BEG/KCp" +
                "Hz2flYh+ALO9++ZX5L/Mj3gfevjw4ZRoP+PzD/b4HadPn844c+aMkb0F1DqIz9byzBvquXytvr6+7vr16+Ow9CfN2njjdfFAWpo9o2FnNmm12kQMw24gcvSn" +
                "hbHb7Y+huHsNlhapLNHSxK3idlq287qhhrkKlSByOBzIZrPhGyCn04ncbjfRGAMV5ZlQxvDw8E+yYi1Q3qpleYjUQlNTU5aysrJqgNBhIAwGVSDCkFj48BVF" +
                "ULA1eCl7XV3dx1CKYK3YqKnY7u9Ti2royclJ76FDh1YhxefgsoFpCIOtra0RuGBQwYbRaLzc1dVlpjA2ZiqmKbWsDAmEYU9Pz8Tg4OCNoqKixNTU1BQostDq" +
                "6iqBcrlcRBiYfEff1KBR+OnpabPBYOikWlnhtOOWm0zUffpnZ2ednZ2dJtCYMTs7+xkA2x0eHk6gsMYwFPYr/EC1Wo2LMEWzWa1WC1QRZ8FUVgpj42ohD3um" +
                "WqHjRFxf5RkZGVkCNQ9CcTWQn5+flpSUtBOiMKAt7Fek/FSAmpmZMVdVVZ0dGxv7g4PhteMVlbBIofv0sh4Lbmhtb2+/Cbv1eFpaWmJCQsJODMO0hGGgUghA" +
                "Aay9v7//i5KSki9lmmG+4+Jg/MHaIH6f0dCkqaNFFc5VkViam5v319TUNEDdvRubEGsNYHGqsAwMDFxta2u7DdpdpA+3c+LgWiHfVkCiFnpDw0iLqwgqO6BV" +
                "KoPo00K6WIDsOzE6OrpE395FzeLgxMn5jVe0dYTa26s5jfFg4VR0nAuwNtrFda1rgmToD6VzVWq3eTPyYAxOwwH5gvT2PiWY7X4fUgJTywp1fivyyL6E+Lb6" +
                "XvQ0X9AkBeeXZED+p/k+9LcAAwAXm3hBLzoZPAAAAABJRU5ErkJggg==\r\n" +
                "--\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/--";

        MultipartRequest req = MultipartRequest.parse(contentType, body.getBytes());
        assertEquals("6abbcffd12b4ada5a6a084fe9e4584f846331bc4", sha1(req.getPart("sticker").getBinaryValue()));
        assertEquals("beta-sticker-1.png", req.getPart("sticker").getSubmittedFileName());
        assertEquals("image/png", req.getPart("sticker").getContentType());
    }

    public void testSampleUpload() throws Exception {
        String contentType = "multipart/form-data; boundary=----WebKitFormBoundary7DvlAl01j1VCThd7";
        byte[] body = FileUtils.readFully(getClass().getClassLoader().getResourceAsStream("sample_upload.bin"));
        MultipartRequest req = MultipartRequest.parse(contentType, body);

        assertEquals(5, req.getParts().size());
        assertEquals("c0394cd18def09bf24031b8bae414260dc6c9772", sha1(req.getPart("file").getBinaryValue()));
        assertEquals("lns-wltsioer - Copy.tcx", req.getPart("file").getSubmittedFileName());
        assertEquals("1261536a-e091-4ca9-a07e-6dc571304305", req.getPart("objectId").getStringValue());
        assertEquals("81e35cbe-90ab-4a87-81d6-e4d8c1287823", req.getPart("objectRev").getStringValue());
        assertEquals("model", req.getPart("fieldName").getStringValue());
        assertEquals("\u06dekonstiga\r\n\ttecken\u06de", req.getPart("exempel").getStringValue());

        assertEquals(1, req.getPart("file").getHeaders("CoNtEnT-tYpE").size());
        assertEquals(2, req.getPart("file").getHeaderNames().size());
        assertNull(req.getPart("missing"));
        assertNull(req.getPart("file").getHeader("missing"));
        assertEquals(224855, req.getPart("file").getBinarySize());
    }

    private static String sha1(byte[] convertme) {
        try {
            Formatter formatter = new Formatter();
            for (byte b : MessageDigest.getInstance("SHA-1").digest(convertme)) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}