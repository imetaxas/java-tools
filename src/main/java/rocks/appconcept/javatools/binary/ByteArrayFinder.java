package rocks.appconcept.javatools.binary;

import java.util.ArrayList;
import java.util.List;

/**
 * Somewhat efficiently search for the occurrences of a sequence of bytes within a byte array.
 * @author yanimetaxas
 */
public class ByteArrayFinder {

    private ByteArrayFinder() {}

    /**
     * Return all indexes where the specified pattern occurs in the data.
     * It will not find overlapping indexes, i.e finding "aa" in "aaaa" will return [0, 2] and not [0,1,2].
     * @param pattern The pattern to search for
     * @param data The data to look for the pattern in
     * @return List of integer indexes where the pattern occurs or an empty list if no occurrences were found.
     */
    public static List<Integer> findAllIndexes(byte[] pattern, byte[] data) {
        List<Integer> result = new ArrayList<>();
        int[] failure = computeFailure(pattern);
        int cursor = 0;
        while (cursor < data.length) {
            int found = indexOf(data, pattern, failure, cursor);
            if (found == -1) break;
            result.add(found);
            cursor = found + pattern.length;
        }

        return result;
    }

    private static int indexOf(byte[] data, byte[] pattern, int[] failure, int start) {
        int j = 0;

        for (int i = start; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}
