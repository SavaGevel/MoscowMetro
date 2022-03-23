import java.util.ArrayList;
import java.util.List;

public class Station {

    private final String stationName;
    private final String lineNumber;
    private final List<Station> connections = new ArrayList<>();
    private final List<String> connectionsInfo;

    public Station(String stationName, String lineNumber, List<String> connectionsInfo) {
        this.stationName = stationName;
        this.lineNumber = lineNumber;
        this.connectionsInfo = connectionsInfo;
    }

    public String getStationName() {
        return stationName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public List<String> getConnectionsInfo() {
        return connectionsInfo;
    }

    public List<Station> getConnections() {
        return connections;
    }

    public void addConnection(Station station) {
        connections.add(station);
    }

}
