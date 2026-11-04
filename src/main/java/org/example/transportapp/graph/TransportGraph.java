package org.example.transportapp.graph;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.example.transportapp.model.City;
import org.example.transportapp.model.Departure;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;


import java.util.*;

/**
 * Klasa koja predstavlja transportni graf.
 * Čvorovi su gradovi (City), a grane su polasci (Departure) koji povezuju gradove.
 * Graf je usmjeren.
 */
public class TransportGraph {
    //grad -> lista polazaka
    private final Map<City, List<Departure>> graph;
    //naziv grada -> city
    private final Map<String, City> cityNameMap;
    //stanica -> grad
    private final Map<String, City> stationToCity;

    private Graph gsGraph;
    private FxViewer viewer;

    /**
     * Konstruktor koji kreira graf na osnovu liste gradova i polazaka.
     * @param cities Lista gradova
     * @param departures Lista polazaka
     */
    public TransportGraph(List<City> cities, List<Departure> departures) {
        this.graph = new HashMap<>();
        this.cityNameMap = new HashMap<>();
        this.stationToCity = new HashMap<>();
        buildGraph(cities, departures);
    }

    private String css = """
            node {
                size: 17px; 
                fill-color: #3498db;
                text-alignment: above;
                text-size: 14;
                text-style: bold;
                text-color: #2c3e50;
                shape: circle; 
            }
            edge {
                arrow-size: 8px, 3px;
                text-mode: hidden;
            }
            edge.autobus {
                fill-color: #FFD700;
                size: 2px;
            }
            edge.voz {
                fill-color: #32CD32;  
                size: 3px;
            }
            edge.marked {
                fill-color: #e74c3c;
                size: 3px;
            }
            """;

    /**
     * Generiše i prikazuje vizuelni prikaz grafa pomoću GraphStream/JavaFX.
     * Svaki grad se prikazuje kao čvor, a svaka veza (polazak) kao usmjerena grana
     * sa bojom zavisno od tipa transporta (autobus/voz).
     * @return
     */
    public Parent showGraph() {
        System.setProperty("org.graphstream.ui", "javafx");
        gsGraph = new MultiGraph("TransportGraph");
        gsGraph.setAttribute("ui.stylesheet", css);
        gsGraph.setStrict(false);
        gsGraph.setAutoCreate(true);

        for(City city : getAllCities()) {
            Node node = gsGraph.addNode(city.getName());
            node.setAttribute("ui.label", city.getName());
        }

        for(City from : getAllCities()) {
            for(Departure dep : getDeparturesFromCity(from)) {
                City to = getCityByName(dep.getToCity());
                if(to != null) {
                    String edgeId = dep.getFromStation() + "-" + to.getName() + "-" + dep.getDepartureTime() + "-" + dep.getDurationMinutes() + "-" + dep.getPrice();
                    Edge edge = gsGraph.addEdge(edgeId, from.getName(), to.getName(), true);
                    if("autobus".equalsIgnoreCase(dep.getType())) {
                        edge.setAttribute("ui.class", "autobus");
                        edge.setAttribute("transportType", dep.getType());
                    } else if("voz".equalsIgnoreCase(dep.getType())) {
                        edge.setAttribute("ui.class", "voz");
                        edge.setAttribute("transportType", dep.getType());
                    }
                }
            }
        }

        viewer = new FxViewer(gsGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);
        return new BorderPane(viewPanel);
    }

    /**
     * Dodaje grad u graf ako već ne postoji.
     * @param city Grad koji se dodaje
     */
    private void addCity(City city) {
        if(city == null)
            return;
        graph.putIfAbsent(city, new ArrayList<>());
        cityNameMap.put(city.getName(), city);

        if(city.getBusStation() != null) {
            stationToCity.put(city.getBusStation().getId(), city);
        }

        if(city.getTrainStation() != null) {
            stationToCity.put(city.getTrainStation().getId(), city);
        }
    }

    /**
     * Dodaje polazak u graf ako polazak ne postoji.
     * @param departure Polazak koji se dodaje
     */
    private void addDeparture(Departure departure) {
        if(departure == null)
            return;

        City fromCity = stationToCity.get(departure.getFromStation());
        if(fromCity != null) {
            graph.get(fromCity).add(departure);
        }
    }

    /**
     * Kreira mapu gradova i polazaka koja reprezentuje graf.
     * @param cities Lista gradova
     * @param departures Lista polazaka
     */
    private void buildGraph(List<City> cities, List<Departure> departures) {
        for(City city : cities) {
            addCity(city);
        }

        for(Departure departure : departures) {
            addDeparture(departure);
        }
    }

    /**
     * Vraća sve polaske iz datog grada.
     * @param city Grad za koji se traže polasci
     * @return Lista polazaka
     */
    public List<Departure> getDeparturesFromCity(City city) {
        return graph.getOrDefault(city, new ArrayList<>());
    }

    /**
     * Vraća grad po nazivu.
     * @param name Naziv grada
     * @return Grad koje odgovara naziv
     */
    public City getCityByName(String name) {
        return cityNameMap.get(name);
    }

    /**
     * Vraća sve gradove iz grafa.
     * @return Set gradova
     */
    public Set<City> getAllCities() {
        return graph.keySet();
    }

    /**
     * Vraća susjedne gradove (gradove do kojih postoji direktan polazak).
     * @param city Grad čiji se susjedi traže
     * @return Lista susjednih gradova
     */
    public List<City> getNeighborCities(City city) {
        Set<City> neighbors = new HashSet<>();
        for(Departure departure : getDeparturesFromCity(city)) {
            City neighbor = getCityByName(departure.getToCity());
            if(neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return new ArrayList<>(neighbors);
    }

//    public TransportGraph cloneGraph() {
//        // Pravimo nove liste za deep copy
//        List<City> citiesCopy = new ArrayList<>(cityNameMap.values());
//        List<Departure> departuresCopy = new ArrayList<>();
//
//        for(List<Departure> deps : graph.values()) {
//            for(Departure dep : deps) {
//                // Pretpostavljam da Departure ima copy constructor ili ga možemo ručno kopirati
//                Departure copy = new Departure(
//                        dep.getType(),
//                        dep.getFromStation(),
//                        dep.getToCity(),
//                        dep.getDepartureTime(),
//                        dep.getDurationMinutes(),
//                        dep.getPrice(),
//                        dep.getMinTransferTime()
//                );
//                departuresCopy.add(copy);
//            }
//        }
//        return new TransportGraph(citiesCopy, departuresCopy);
//    }

    /**
     * Pravi kopiju trenutnog grafa.
     * @return Novi graf sa istim podacima
     */
    public TransportGraph cloneGraph() {
        List<City> citiesCopy = new ArrayList<>(cityNameMap.values());
        List<Departure> departuresCopy = new ArrayList<>();
        for(Map.Entry<City, List<Departure>> e : graph.entrySet()) {
            for(Departure departure : e.getValue()) {
                departuresCopy.add(new Departure(departure.getType(), departure.getFromStation(),
                        departure.getToCity(), departure.getDepartureTime(), departure.getDurationMinutes(),
                        departure.getPrice(), departure.getMinTransferTime()));
            }
        }

        return new TransportGraph(citiesCopy, departuresCopy);
    }

    /**
     * Uklanja određenu granu (polazak) iz grafa.
     * @param from Početni grad
     * @param target Odredišni grad
     * @param depToRemove Konkretan polazak koji se uklanja
     */
    public void removeEdge(City from, City target, Departure depToRemove) {
        List<Departure> departures = graph.get(from);
        if(departures != null) {
            departures.removeIf(dep -> dep.equals(depToRemove));
        }
    }

    /**
     * Vraća GraphStream objekat koji predstavlja graf koji se koristi za vizuelizaciju.
     * @return
     */
    public Graph getGsGraph() {
        return gsGraph;
    }

    /**
     * Generiše tekstualni opis grafa.
     * @return String koji sadrži sve podatke o grafu
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<City, List<Departure>> entry : graph.entrySet()) {
            sb.append(entry.getKey().getName()).append(" -> ");
            for (Departure dep : entry.getValue()) {
                sb.append(dep.getToCity()).append(" (").append(dep.getType()).append("), ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
