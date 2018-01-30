package rocks.appconcept.javatools.profiler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import rocks.appconcept.javatools.io.StreamUtils;

/**
 * Basic profiler utility.
 *
 * @author yanimetaxas
 */
public final class Profiler {

  private Profiler() {
  }

  public static String sampling(final long repetitions, final ProfileBlock runnable) {
    List<String> traces = new ArrayList<>();
    try {
      Thread thread = new Thread(() -> {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new StreamUtils.VoidOutputStream()));
        try {
          for (long i = 0; i < repetitions; i++) {
            runnable.run();
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          System.setOut(out);
        }
      });

      long startTime = System.currentTimeMillis();
      thread.start();
      while (thread.isAlive()) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        if (stackTrace.length > 0) {
          traces.add(stackTrace[0].toString());
        }
        Thread.sleep(0, 10);
      }
      long endTime = System.currentTimeMillis();

      Map<String, Integer> top = new HashMap<>();
      for (String method : traces) {
        if (top.containsKey(method)) {
          top.put(method, top.get(method) + 1);
        } else {
          top.put(method, 1);
        }
      }
      ArrayList<Map.Entry<String, Integer>> samples = new ArrayList<>(top.entrySet());
      Collections.sort(samples, (o1, o2) -> o2.getValue() - o1.getValue());

      int totalSamples = traces.size();
      long totalTime = (endTime - startTime);
      StringBuilder out = new StringBuilder();
      out.append("--------------------------------------------------\n");
      out.append("Profile based on ").append(totalSamples).append(" samples, ").append(totalTime)
          .append("ms.\n");
      for (Iterator<Map.Entry<String, Integer>> iterator = samples.iterator();
          iterator.hasNext(); ) {
        Map.Entry<String, Integer> sample = iterator.next();
        long percentage = 100 * sample.getValue() / totalSamples;
        if (sample.getValue() == 1 || percentage == 0) {
          long totalSkippedSamples = 0;
          while (iterator.hasNext()) {
            totalSkippedSamples += sample.getValue();
            sample = iterator.next();
          }
          long totalSkippedPercentage = 100 * totalSkippedSamples / totalSamples;
          out.append("Rest of samples ~ ").append(totalSkippedPercentage).append("% (")
              .append(totalSkippedSamples).append(" samples in total)\n");
          break;
        }
        out.append(sample.getKey()).append(": ").append(percentage).append("% (")
            .append(sample.getValue()).append(" samples)\n");
      }
      out.append("--------------------------------------------------\n");
      return out.toString();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Representation of a method under profiling.
   * (Should be @FunctionalInterface in Java 1.8)
   */
  public interface ProfileBlock {
    void run() throws Exception;
  }
}
