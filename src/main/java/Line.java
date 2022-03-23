public class Line implements Comparable<Line> {

    private final String lineName;
    private final String lineNumber;

    public Line(String lineName, String lineNumber) {
        this.lineName = lineName;
        this.lineNumber = lineNumber;
    }

    public String getLineName() {
        return lineName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    @Override
    public int compareTo(Line o) {
        return o.getLineNumber().compareTo(lineNumber);
    }
}
