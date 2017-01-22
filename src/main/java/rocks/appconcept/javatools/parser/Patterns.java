package rocks.appconcept.javatools.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expression utilities.
 *
 * @author yanimetaxas
 */
public final class Patterns {

  private Patterns() {
  }

  /**
   * Returns an iterator with all the matches of the pattern for the specified input.
   *
   * @param pattern The pattern
   * @param input The input string
   * @return All matches
   */
  public static Iterable<MatchResult> allMatches(final Pattern pattern, final CharSequence input) {
    return () -> new Iterator<MatchResult>() {
      final Matcher matcher = pattern.matcher(input);
      MatchResult pending;

      public boolean hasNext() {
        if (pending == null && matcher.find()) {
          pending = matcher.toMatchResult();
        }
        return pending != null;
      }

      public MatchResult next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        MatchResult next = pending;
        pending = null;
        return next;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Interface for use with the {@link Patterns#replaceAll(Pattern, String, Replacer)} method.
   */
  public interface Replacer {

    /**
     * Return a replacement for the current match.
     *
     * @param result The match result.
     * @return The String to replace with.
     */
    String replace(MatchResult result);
  }

  /**
   * Replaces all matches of the specified pattern from the input with the result of calling the
   * specified replacer method.
   *
   * @param pattern The pattern to match.
   * @param input The input to replace values in.
   * @param replacer The replacer to use.
   */
  public static String replaceAll(final Pattern pattern, final String input, Replacer replacer) {
    StringBuilder out = new StringBuilder();
    replaceAll(pattern, input, out, replacer);
    return out.toString();
  }

  /**
   * Replaces all matches of the specified pattern from the input with the result of calling the
   * specified replacer method.
   *
   * @param pattern The pattern to match.
   * @param input The input to replace values in.
   * @param out The StringBuilder to replace append the result to.
   * @param replacer The replacer to use.
   */
  public static void replaceAll(final Pattern pattern, final String input, StringBuilder out,
      Replacer replacer) {
    int lastEnd = 0;
    for (MatchResult matchResult : Patterns.allMatches(pattern, input)) {
      out.append(input.substring(lastEnd, matchResult.start()));
      String replacement = replacer.replace(matchResult);
      if (replacement != null) {
        out.append(replacement);
      }
      lastEnd = matchResult.end();
    }
    out.append(input.substring(lastEnd));
  }
}
