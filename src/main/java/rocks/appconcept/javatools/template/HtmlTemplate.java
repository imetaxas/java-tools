package rocks.appconcept.javatools.template;

import rocks.appconcept.javatools.parser.Patterns;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Template engine for rendering a subset of velocity templates.
 * <p/>
 * Printing variables:
 * $simple or ${complicated}
 * Dot notation is not supported.
 * Only [a-zA-Z] are allowed in variable names.
 * <p/>
 * Include other files:
 * #include("other.vm")
 * Included files must be pre-compiled.
 * <p/>
 * Comments:
 * #* This is a (multi line) comment *#
 * <p/>
 * Conditionals:
 * #if(variable) This is conditional #end
 * Only existence tests supported (null and empty string is false-y).
 * Nested conditionals are allowed.
 *
 * @author yanimetaxas
 */
public class HtmlTemplate {

    public static final Pattern TAG_PATTERN = Pattern.compile("\\$([a-zA-Z]+)|\\$\\{([a-zA-Z]+)\\}|#if\\(\\$([a-zA-Z]+)\\)|(#end)|#include\\(\"([^\"]+)\"\\)|(?:#\\*.*?\\*#)", Pattern.DOTALL);
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private List<Writer> writers = new ArrayList<>();
    private Map<String, HtmlTemplate> templates = Collections.emptyMap();

    public HtmlTemplate(String html, Map<String, String> presets) {
        int lastEnd = 0;
        for (Iterator<MatchResult> iterator = Patterns.allMatches(TAG_PATTERN, html).iterator(); iterator.hasNext(); ) {
            MatchResult matchResult = iterator.next();
            writers.add(new ConstantWriter(html.substring(lastEnd, matchResult.start())));

            String variable = matchResult.group(1);
            if (variable == null) variable = matchResult.group(2);
            String condition = matchResult.group(3);
            String include = matchResult.group(5);
            if (variable != null) {
                if (presets != null && presets.containsKey(variable)) {
                    writers.add(new ConstantWriter(presets.get(variable)));
                } else {
                    writers.add(new VariableWriter(variable));
                }
            } else if (condition != null) {
                int conditionStart = matchResult.end();
                int level = 0;
                while (true) {
                    try {
                        matchResult = iterator.next();
                        if (matchResult.group(3) != null) {
                            level += 1;
                        } else if (matchResult.group(4) != null) {
                            if (level == 0) {
                                break;
                            } else {
                                level -= 1;
                            }
                        }
                    } catch (NoSuchElementException nsee) {
                        throw new RuntimeException("Unbalanced if/end in template (pos=" + conditionStart + ")");
                    }
                }
                int conditionEnd = matchResult.start();
                if (presets != null && presets.containsKey(condition)) {
                    if (HtmlTemplate.isTruthy(presets.get(condition))) {
                        writers.addAll(new HtmlTemplate(html.substring(conditionStart, conditionEnd), presets).writers);
                    }
                } else {
                    writers.add(new ConditionalWriter(condition, new HtmlTemplate(html.substring(conditionStart, conditionEnd), presets).writers));
                }
            } else if (include != null) {
                writers.add(new IncludeWriter(include));
            }
            lastEnd = matchResult.end();
        }
        writers.add(new ConstantWriter(html.substring(lastEnd)));
    }

    private static boolean isTruthy(String s) {
        return s != null && !s.isEmpty();
    }

    public void renderTemplate(Map<String, String> values, OutputStream stream) throws IOException {
        for (Writer writer : writers) {
            writer.write(values, stream);
        }
    }

    public void setIncludableTemplates(Map<String, HtmlTemplate> templates) {
        this.templates = templates;
    }


    private interface Writer {
        void write(Map<String, String> values, OutputStream os) throws IOException;
    }

    private static class ConstantWriter implements Writer {
        private byte[] data;

        public ConstantWriter(String str) {
            this.data = str.getBytes(UTF8);
        }

        public void write(Map<String, String> values, OutputStream os) throws IOException {
            os.write(data);
        }
    }

    private static class VariableWriter implements Writer {
        private String variableName;

        private VariableWriter(String variableName) {
            this.variableName = variableName;
        }

        public void write(Map<String, String> values, OutputStream os) throws IOException {
            os.write(values.get(variableName).getBytes(UTF8));
        }
    }

    private class IncludeWriter implements Writer {
        private String include;

        public IncludeWriter(String include) {
            this.include = include;
        }

        public void write(Map<String, String> values, OutputStream os) throws IOException {
            templates.get(include).renderTemplate(values, os);
        }
    }

    private static class ConditionalWriter implements Writer {
        private String variable;
        private List<Writer> subWriters;

        private ConditionalWriter(String variable, List<Writer> subWriters) {
            this.variable = variable;
            this.subWriters = subWriters;
        }

        public void write(Map<String, String> values, OutputStream os) throws IOException {
            if (values != null && HtmlTemplate.isTruthy(values.get(variable))) {
                for (Writer subWriter : subWriters) {
                    subWriter.write(values, os);
                }
            }
        }
    }
}
