package util.graph;

import dto.RailLine;
import dto.RailStation;
import dto.Route;
import dto.Station;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

/*For distance/weight calculations code was take from:
* https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
* https://www.geodatasource.com/resources/tutorials/how-to-calculate-the-distance-between-2-locations-using-java
* https://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
*/

@Getter
@Setter
public class GraphObjectGenerator {
    private List<Route> routes;
    private List<Station> stations;
    private Map<String, List<RailStation>> lineToStationsMap;
    private Map<String, Map<String, Double>> stationDistances;

    public GraphObjectGenerator() {
        routes = new ArrayList<>();
        stations = new ArrayList<>();
        lineToStationsMap = new HashMap<>();
        stationDistances = new HashMap<>();
    }

    public void controller(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        try {
            buildStationMaps(railLines, railStations);
            generateStationNetwork(railStations);
            calculateStationDistances();
        } catch (Exception e) {
            System.err.println("Error in controller: " + e.getMessage());
        }
    }

    private void buildStationMaps(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        lineToStationsMap.clear();
        stationDistances.clear();

        for (RailLine line : railLines) {
            if (line != null && line.getName() != null) {
                List<RailStation> stationsOnLine = getStationsForLine(line, railStations);
                lineToStationsMap.put(line.getName(), stationsOnLine);
            }
        }
    }

    private List<RailStation> getStationsForLine(RailLine line, ArrayList<RailStation> allStations) {
        return allStations.stream().filter(station -> station != null && station.getRailLines() != null && station.getRailLines().stream().anyMatch(rl -> rl != null && rl.getName() != null && rl.getName().equals(line.getName()))).toList();
    }

    private void generateStationNetwork(ArrayList<RailStation> railStations) {
        stations.clear();
        routes.clear();

        for (RailStation currentStation : railStations) {
            if (currentStation == null || currentStation.getRailLines() == null) {
                continue;
            }

            Set<String> processedConnections = new HashSet<>();
            ArrayList<Route> stationRoutes = new ArrayList<>();

            for (RailLine line : currentStation.getRailLines()) {
                if (line == null || line.getName() == null) {
                    continue;
                }

                List<RailStation> connectedStations = findAdjacentStations(currentStation, line);
                for (RailStation destinationStation : connectedStations) {
                    String connectionKey = getConnectionKey(currentStation.getName(), destinationStation.getName());
                    String reverseKey = getConnectionKey(destinationStation.getName(), currentStation.getName());

                    if (!processedConnections.contains(connectionKey) && !processedConnections.contains(reverseKey)) {

                        double distance = calculateDistance(currentStation, destinationStation);
                        Route route = new Route(line);
                        route.setDestination(destinationStation);
                        route.setWeight(distance);

                        stationRoutes.add(route);
                        processedConnections.add(connectionKey);

                        // Store the distance in both directions
                        storeDistance(currentStation.getName(), destinationStation.getName(), distance);
                    }
                }
            }

            stations.add(new Station(currentStation, stationRoutes));
        }
    }

    private void calculateStationDistances() {
        for (Station station : stations) {
            Map<String, Double> distances = new HashMap<>();
            stationDistances.put(station.getRailStation().getName(), distances);

            for (Route route : station.getRoutes()) {
                String destName = route.getDestination().getName();
                distances.put(destName, route.getWeight());
            }
        }
    }

    private List<RailStation> findAdjacentStations(RailStation station, RailLine line) {
        List<RailStation> adjacentStations = new ArrayList<>();
        List<RailStation> stationsOnLine = lineToStationsMap.get(line.getName());

        if (stationsOnLine == null || stationsOnLine.isEmpty()) {
            return adjacentStations;
        }

        int currentIndex = stationsOnLine.indexOf(station);
        if (currentIndex == -1) {
            return adjacentStations;
        }

        if (currentIndex > 0) {
            adjacentStations.add(stationsOnLine.get(currentIndex - 1));
        }
        if (currentIndex < stationsOnLine.size() - 1) {
            adjacentStations.add(stationsOnLine.get(currentIndex + 1));
        }

        return adjacentStations;
    }

    private double calculateDistance(RailStation station1, RailStation station2) {
        try {
            String[] coords1 = station1.getCoordinates();
            String[] coords2 = station2.getCoordinates();

            if (coords1 != null && coords2 != null && coords1.length >= 2 && coords2.length >= 2) {
                //convert strings to doubles
                double lon1 = Double.parseDouble(coords1[0]);
                double lat1 = Double.parseDouble(coords1[1]);
                double lon2 = Double.parseDouble(coords2[0]);
                double lat2 = Double.parseDouble(coords2[1]);

                return haversineDistance(lat1, lon1, lat2, lon2);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error calculating distance between " + station1.getName() + " and " + station2.getName() + ": " + e.getMessage());
        }
        //default distance if calculation fails
        return 1.0;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        //radius of Earth in kilometers
        final int EARTH_RADIUS = 6371;

        //get the difference between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        //apply Haversine formula
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return Math.round(EARTH_RADIUS * c * 100.0) / 100.0; //round to 2 decimal places
    }

    private void storeDistance(String station1, String station2, double distance) {
        stationDistances.computeIfAbsent(station1, k -> new HashMap<>()).put(station2, distance);
        stationDistances.computeIfAbsent(station2, k -> new HashMap<>()).put(station1, distance);
    }

    private String getConnectionKey(String station1, String station2) {
        return station1 + "-" + station2;
    }
}