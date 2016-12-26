package rocks.appconcept.javatools.parser.peg;

/**
 * @author yanimetaxas
 */
public class AstQuotedString extends AstBase {

    private final char quote;

    public AstQuotedString(char quote) {
        this.quote = quote;
    }

    @Override
    public Output parse(Input input) {
        Input.Point start = input.getPoint();
        if (isFailureCached(start))
            return FAILED;

        String data = input.input;
        int length = data.length();
        int end = input.position;
        if (end + 2 > length || data.charAt(end) != quote)
            return cacheFailure(start);
        end++;

        StringBuilder result = new StringBuilder();
        while (end < length && data.charAt(end) != quote) {
            char c = data.charAt(end);
            end++;
            if (c == '\\' && end < length) {
                c = data.charAt(end);
                end++;
            }
            result.append(c);
        }

        if (end < length && data.charAt(end) == quote) {
            end++;
            input.position = end;
            return new Output(result.toString());
        }
        return cacheFailure(start);
    }
}
