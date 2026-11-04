package org.example.transportapp.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.transportapp.model.*;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Klasa za parsiranje transportnih podataka iz JSON fajla.
 * Učitava i konvertuje podatke u odgovarajuće modele (City, Station, Departure).
 */
public class TransportDataParser {
    private final ObjectMapper mapper = new ObjectMapper();

    private List<List<String>> countryMap;
    private List<City> cities;
    private List<Departure> departures;

    /**
     * Vraća listu svih polazaka iz svih stanica.
     * @return Lista Departure objekata
     */
    public List<Departure> getDepartures() {
        return departures;
    }

    /**
     * Vraća mapu gradova u obliku 2D liste.
     * @return Matrica gradova
     */
    public List<List<String>> getCountryMap() {
        return countryMap;
    }

    /**
     * Vraća listu gradova sa pripadajućim stanicama.
     * @return Lista City objekata
     */
    public List<City> getCities() {
        return cities;
    }

    /**
     * Učitava JSON fajl sa putanje.
     * @param path Putanja do JSON fajla
     * @return JsonNode sa učitanim podacima
     * @throws IOException Izuzetak koji se baca ako dodđe do greške pri učitavanju fajla
     */
    public JsonNode loadFile(String path) throws IOException {
        File file = new File(path);
        return mapper.readTree(file);
    }

    /**
     * Učitava i parsira sve podatke iz JSON fajla.
     * @param path Putanja do JSON fajla
     * @throws IOException Izuzetak koji se baca ako dodđe do greške pri učitavanju fajla
     */
    public void loadData(String path) throws IOException {
        try {
            JsonNode root = loadFile(path);
            parseCountryMap(root.get("countryMap"));
            parseCities(root.get("stations"));
            parseDepartures(root.get("departures"));
            linkStationsAndDepartures();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsira mapu gradova iz JSON čvora.
     * @param countryMapNode JSON čvor sa mapom grada
     */
    private void parseCountryMap(JsonNode countryMapNode) {
        countryMap = new ArrayList<>();
        if(countryMapNode != null && countryMapNode.isArray()) {
            for(JsonNode node : countryMapNode) {
                if(node.isArray()) {
                    List<String> row = new ArrayList<>();
                    for(JsonNode cell : node) {
                        row.add(cell.asText());
                    }
                    countryMap.add(row);
                }
            }
        }
    }

    /**
     * Parsira gradove i stanice iz JSON čvora.
     * @param stationsNode JSON čvor sa stanicama
     */
    private void parseCities(JsonNode stationsNode) {
        cities = new ArrayList<>();
        if(stationsNode != null && stationsNode.isArray()) {
            for(JsonNode stationNode : stationsNode) {
                String name = stationNode.get("city").asText();
                BusStation busStation = new BusStation(stationNode.get("busStation").asText(), new ArrayList<>());
                TrainStation trainStation = new TrainStation(stationNode.get("trainStation").asText(), new ArrayList<>());
                City city = new City(name, busStation, trainStation);
                cities.add(city);
            }
        }
    }

    /**
     * Parsira polaske iz JSON čvora.
     * @param departuresNode JSON čvor sa polascima
     */
    private void parseDepartures(JsonNode departuresNode) {
        departures = new ArrayList<>();
        if(departuresNode != null && departuresNode.isArray()) {
            for(JsonNode node : departuresNode) {
                Departure departure = new Departure(node.get("type").asText(),
                        node.get("from").asText(), node.get("to").asText(),
                        node.get("departureTime").asText(),
                        node.get("duration").asInt(), node.get("price").asInt(),
                        node.get("minTransferTime").asInt());
                departures.add(departure);
            }
        }
    }

    /**
     * Povezuje stanice sa odgovarajućim polascima.
     */
    private void linkStationsAndDepartures() {
        Map<String, List<Departure>> departuresByStation = new HashMap<>();

        for(Departure departure : departures) {
            departuresByStation.computeIfAbsent(departure.getFromStation(), k -> new ArrayList<>()).add(departure);
        }

        for(City city : cities) {
            if(city.getBusStation() != null) {
                city.getBusStation().setDepartureList(departuresByStation.getOrDefault(city.getBusStation().getId(), Collections.emptyList()));
            }

            if(city.getTrainStation() != null) {
                city.getTrainStation().setDepartureList(departuresByStation.getOrDefault(city.getTrainStation().getId(), Collections.emptyList()));
            }
        }
    }

//        public static void main(String[] args) {
//        TransportDataParser parser = new TransportDataParser();
//        String path = "transport_data.json";
//        try {
//            parser.loadData(path);
//
//            System.out.println("=== COUNTRY MAP ===");
//            parser.getCountryMap().forEach(row -> System.out.println(String.join(", ", row)));
//            System.out.println("\n=== CITIES AND STATIONS ===");
//            System.out.println("--------------------------------------------------------");
//            parser.getCities().stream().forEach(city -> System.out.println(city.toString()));
//            System.out.println("\n=== DEPARTURES ===");
//            System.out.println("--------------------------------------------------------");
//            parser.getDepartures().stream().limit(10).forEach(dep -> System.out.println(dep.toString()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
