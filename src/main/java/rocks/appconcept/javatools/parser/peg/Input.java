package rocks.appconcept.javatools.parser.peg;

import java.util.Objects;

/**
 * @author yanimetaxas
 */
public class Input {

    public final String input;
    public int position;
    public int maxRegisteredPosition;
    private final int inputHashcode;

    public Input(String input, int position) {
        this.input = input;
        this.position = position;
        this.inputHashcode = input.hashCode();
    }

    public Point getPoint() {
        maxRegisteredPosition = Math.max(maxRegisteredPosition, position);
        return new Point(input, position);
    }

    public boolean has(int i) {
        return input.length() - position >= i;
    }

    public class Point {
        private final String text;
        private final int position;
        private final int hash;

        public Point(String text, int position) {
            this.text = text;
            this.position = position;
            this.hash = inputHashcode * 31 + position;
        }

        public int getPosition() {
            return position;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return Objects.equals(position, point.position) &&
                    Objects.equals(text, point.text);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
