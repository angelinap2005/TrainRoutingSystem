
package util.Graph;

import dto.RailLine;
import dto.RailStation;
import dto.Route;
import dto.Station;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
/* code from https://graphstream-project.org/doc/ */
@Getter
@Setter
public class GraphObjectGenerator {
    private List<Route> routes;
    private List<Station> stations;
    private Map<String, List<RailStation>> lineToStationsMap;
    private Map<String, Set<String>> stationConnections;

    public GraphObjectGenerator() {
        routes = new ArrayList<>();
        stations = new ArrayList<>();
        lineToStationsMap = new HashMap<>();
        stationConnections = new HashMap<>();
    }

    public void controller(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        try {
            mapStationsToLines(railLines, railStations);
            createStationNetwork(railStations);
        } catch (Exception e) {
            System.err.println("Error in controller: " + e.getMessage());
        }
    }

    private void mapStationsToLines(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        lineToStationsMap.clear();
        stationConnections.clear();

        // Map each line to its stations
        for (RailLine line : railLines) {
            if (line != null && line.getName() != null) {
                List<RailStation> stationsOnLine = findStationsOnLine(line, railStations);
                lineToStationsMap.put(line.getName(), stationsOnLine);
            }
        }
    }

    private List<RailStation> findStationsOnLine(RailLine line, ArrayList<RailStation> allStations) {
        List<RailStation> stationsOnLine = new ArrayList<>();

        for (RailStation station : allStations) {
            if (station != null && station.getRailLines() != null) {
                boolean isOnLine = station.getRailLines().stream()
                        .anyMatch(rl -> rl != null && rl.getName() != null &&
                                rl.getName().equals(line.getName()));
                if (isOnLine) {
                    stationsOnLine.add(station);
                }
            }
        }

        return stationsOnLine;
    }

    private void createStationNetwork(ArrayList<RailStation> railStations) {
        stations.clear();
        routes.clear();

        for (RailStation currentStation : railStations) {
            if (currentStation == null || currentStation.getRailLines() == null) {
                continue;
            }

            ArrayList<Route> stationRoutes = new ArrayList<>();
            Set<String> processedConnections = new HashSet<>();

            for (RailLine line : currentStation.getRailLines()) {
                if (line == null || line.getName() == null) {
                    continue;
                }

                List<RailStation> connectedStations = findConnectedStations(currentStation, line);
                for (RailStation destinationStation : connectedStations) {
                    String connectionKey = currentStation.getName() + "-" + destinationStation.getName();
                    String reverseKey = destinationStation.getName() + "-" + currentStation.getName();

                    if (!processedConnections.contains(connectionKey) &&
                            !processedConnections.contains(reverseKey)) {
                        Route route = new Route(line);
                        route.setDestination(destinationStation);
                        stationRoutes.add(route);
                        processedConnections.add(connectionKey);
                    }
                }
            }

            stations.add(new Station(currentStation, stationRoutes));
        }
    }

    private List<RailStation> findConnectedStations(RailStation station, RailLine line) {
        List<RailStation> connectedStations = new ArrayList<>();
        List<RailStation> stationsOnLine = lineToStationsMap.get(line.getName());

        if (stationsOnLine == null || stationsOnLine.isEmpty()) {
            return connectedStations;
        }

        int stationIndex = stationsOnLine.indexOf(station);
        if (stationIndex == -1) {
            return connectedStations;
        }

        // Add adjacent stations
        if (stationIndex > 0) {
            connectedStations.add(stationsOnLine.get(stationIndex - 1));
        }
        if (stationIndex < stationsOnLine.size() - 1) {
            connectedStations.add(stationsOnLine.get(stationIndex + 1));
        }

        return connectedStations;
    }
}