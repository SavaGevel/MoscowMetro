import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private static final String JSON_FILE_PATH = "/Users/savelijgevel/desktop/map.json";

    static Map<Line, List<Station>> metro = new TreeMap<>();

    public static void main(String[] args) {

        try {

            Path metroHtml = Path.of("/Users/savelijgevel/downloads/Карта_метро_Москвы_со_станциями_МЦК_и_МЦД_2020.html");

            String html = Files.readString(metroHtml);
            Document doc = Jsoup.parse(html);

            getLines(doc);
            createJsonFile(metro);
            getStationsNumberOnLines();
            getNumberOfConnections();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getLines(Document doc) {

        doc.body().select("span.js-metro-line.t-metrostation-list-header.t-icon-metroln")
                .forEach(element -> metro.put(new Line(element.text(), element.attr("data-line")),
                        getStations(doc, element.attr("data-line"))));

        getConnectionsFromConnectionsInfo();
    }

    public static List<Station> getStations(Document doc, String line) {
        return doc.body().select("div.js-metro-stations.t-metrostation-list-table")
                .select("[data-line="+line+"]")
                .select("a.js-metrostation")
                .stream()
                .map(element -> new Station(element.select("span.name").text(), line,
                        element.select("span.t-icon-metroln")
                                .stream()
                                .map(connection -> connection.attr("class") + ";" + connection.attr("title"))
                                .toList()))
                .toList();
    }

    private static void getConnectionsFromConnectionsInfo() {

        metro.keySet()
                .forEach(line -> metro.get(line)
                        .forEach(station -> station.getConnectionsInfo()
                                .stream()
                                .filter(info -> !info.isBlank())
                                .forEach(connection -> {
                                    String[] info = connection.split(";");
                                    String connectedLine = info[0].substring(info[0].lastIndexOf("-") + 1);
                                    String connectedStation = info[1].substring(info[1].indexOf("«") + 1, info[1].lastIndexOf("»"));

                                    metro.keySet()
                                            .stream()
                                            .filter(metroLine -> metroLine.getLineNumber().equals(connectedLine))
                                            .forEach(metroLine -> metro.get(metroLine)
                                                    .stream()
                                                    .filter(metroStation -> metroStation.getStationName().equals(connectedStation))
                                                    .forEach(station::addConnection));

                                })));
    }


    public static void createJsonFile(Map<Line, List<Station>> metro) {

        JSONObject metroScheme = new JSONObject();

        JSONObject stationsObject = new JSONObject();

        metro.keySet().forEach(line -> stationsObject.put(line.getLineNumber(),
                metro.get(line).stream().map(Station::getStationName).toList()));

        List<List<JSONObject>> connections = getListOfConnectedStations();


        metroScheme.put("stations",stationsObject);

        metroScheme.put("lines", metro.keySet().stream()
                .map(line -> new JSONObject(Map.of("name", line.getLineName(), "number", line.getLineNumber())))
                .toList());

        metroScheme.put("connections", connections);



        try {
            if(Files.notExists(Path.of(JSON_FILE_PATH))) {
                Files.createFile(Path.of(JSON_FILE_PATH));
            }
            Files.writeString(Path.of(JSON_FILE_PATH), metroScheme.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<List<JSONObject>> getListOfConnectedStations() {
        List<List<Station>> connections = new LinkedList<>();
        metro.keySet()
                .forEach(line -> metro.get(line)
                        .stream()
                        .filter(stations -> !stations.getConnections().isEmpty())
                        .filter(station -> {
                            for(List<Station> connected : connections) {
                                if(connected.contains(station)) return false;
                            }
                            return true;
                        })
                        .forEach(station -> {
                            List<Station> connectedStations = station.getConnections();
                            connectedStations.add(station);
                            connections.add(connectedStations);
                        }));

        return connections.stream()
                .map(connectedStations -> connectedStations.stream()
                        .map(station ->
                                new JSONObject(Map.of("line",station.getLineNumber(),
                                                    "station", station.getStationName()))).toList())
                .toList();
    }

    public static void getStationsNumberOnLines() {

        try {

            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(JSON_FILE_PATH);
            JSONObject metro = (JSONObject) parser.parse(reader);

            JSONObject stationsObject = (JSONObject) metro.get("stations");

            stationsObject.keySet().forEach(line -> {
                System.out.print(line.toString() + ": ");
                JSONArray array = (JSONArray) stationsObject.get(line.toString());
                System.out.println(array.size());
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void getNumberOfConnections() {

        try {

            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(JSON_FILE_PATH);
            JSONObject metro = (JSONObject) parser.parse(reader);

            JSONArray connections = (JSONArray) metro.get("connections");
            System.out.println("Количество переходов метро: " + connections.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




