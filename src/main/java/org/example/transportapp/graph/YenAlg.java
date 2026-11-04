package org.example.transportapp.graph;

import org.example.transportapp.model.City;
import org.example.transportapp.model.Departure;

import java.time.LocalTime;
import java.util.*;

/**
 * Klasa koja predstavlja implementaciju Yen-ovog algoritma za pronalazak K najkraćih ruta između gradova.
 * Yen-ov algoritam koristi rezultate Dijkstra algoritma za prvi najkraći put, a zatim pronalazi naredne najkraće rute
 * uklanjanjem grana i ponovnim izvršavanjem Dijksta algoritma.
 * @see DijkstraAlg
 */
public class YenAlg {

    /**
     * Pronalazi k najkraćih ruta po zadanom kriterijumu između dva grada.
     * @param graph Transportni graf
     * @param start Početni grad
     * @param target Odredišni grad
     * @param criteria Kriterijum optimizacije
     * @param k Broj ruta
     * @return Lista od k ruta
     */
    public static List<DijkstraAlg.PathResult> findKShortestPaths(TransportGraph graph, City start, City target, DijkstraAlg.Criteria criteria, int k) {
        List<DijkstraAlg.PathResult> results = new ArrayList<>();

        //pronalazak najkrace rute
        DijkstraAlg.PathResult first = DijkstraAlg.findShortestPath(graph, start, criteria, null, 0);
//        DijkstraAlg.PathResult first = DijkstraAlg.findBestRouteOverAllFirstDepartures(DijkstraAlg.findAllRoutesDifferentFirstDeparture(graph, start, target, criteria), target);
        if(first.reconstructPath(start, target).isEmpty()) {
            return results;
        }
        results.add(first);

        //kandidati
        PriorityQueue<DijkstraAlg.PathResult> candidates = new PriorityQueue<>(Comparator.comparingInt(r -> r.getDistances().get(target)));

        for(int i = 1; i < k; i++) {
            if(results.size() < i) {
                break;
            }

            DijkstraAlg.PathResult prevBest = results.get(i -1);
            List<City> prevPath = prevBest.reconstructPath(start, target);

            for(int j = 0; j < prevPath.size() - 1; j++) {
                City spurNode = prevPath.get(j);

                //dio rute koji ostaje
                DijkstraAlg.PathResult rootResult = extractSubPathResult(prevBest, start, spurNode);
                List<City> rootPath = new ArrayList<>(prevPath.subList(0, j + 1));

                //priprema podataka koji se prosljedjuju u dijkstra
                City prevCity = prevBest.getPrevious().get(spurNode);
                LocalTime spurStartTime = null;
                if(prevCity != null) {
                    Departure depToSpur = prevBest.getChosenDeparture().get(spurNode);
                    if(depToSpur != null) {
                        spurStartTime = LocalTime.parse(depToSpur.getDepartureTime()).plusMinutes(depToSpur.getDurationMinutes());
                    }
                }

                //klon grafa
                TransportGraph clone = graph.cloneGraph();

                //uklanjanje grana koje se ponavljaju
                for(DijkstraAlg.PathResult found : results) {
                    List<City> p = found.reconstructPath(start, target);

                    if(p.size() > j && rootPath.equals(p.subList(0, j + 1))) {
                        City fromCity = p.get(j);
                        City toCity = p.get(j + 1);
                        Departure departure = found.getChosenDeparture().get(toCity);
                        clone.removeEdge(fromCity, toCity, departure);
                    }
                }

                //novi nastavak rute
                DijkstraAlg.PathResult spurResult = DijkstraAlg.findShortestPath(clone, spurNode, criteria, spurStartTime, prevBest.getDistances().get(spurNode));
                List<City> spurPath = spurResult.reconstructPath(spurNode, target);
                if(spurPath.isEmpty()) {
                    continue;
                }

                //root + novi nastavak
                DijkstraAlg.PathResult finalRoute = mergeResults(rootResult, spurResult, spurNode, target);
                candidates.add(finalRoute);
            }

            while(!candidates.isEmpty()) {
                DijkstraAlg.PathResult candidate = candidates.poll();
                boolean alreadyExists = results.stream().anyMatch(r -> {
                    List<City> p1 = r.reconstructPath(start, target);
                    List<City> p2 = candidate.reconstructPath(start, target);

                    if(!p1.equals(p2)) {
                        return false;
                    }

                    for(City city : p1) {
                        Departure d1 = r.getChosenDeparture().get(city);
                        Departure d2 = candidate.getChosenDeparture().get(city);
                        if(!Objects.equals(d1, d2)) {
                            return false;
                        }
                    }
                    return true;
                });

                if(!alreadyExists) {
                    results.add(candidate);
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Kreira novi {@link DijkstraAlg.PathResult} koji sadrži samo dio originalne rute od startno grada do datog čvora.
     * Koristi se za kreiranje root dijela puta.
     * @param full Originalna ruta
     * @param start Početni grad
     * @param spurNode Čvor do kojeg se izdvaja putanja
     * @return Nova ruta (podruta originalne)
     */
    private static DijkstraAlg.PathResult extractSubPathResult(DijkstraAlg.PathResult full, City start, City spurNode) {
        List<City> subCities = new ArrayList<>();
        City current = spurNode;
        while(current != null) {
            subCities.add(0, current);
            if(current.equals(start)) {
                break;
            }
            current = full.getPrevious().get(current);
        }

        Map<City, Integer> newDistances = new HashMap<>();
        Map<City, City> newPrevious = new HashMap<>();
        Map<City, Departure> newChosenDepartures = new HashMap<>();
        Map<City, LocalTime> newArrivalTime = new HashMap<>();

        for(City city : subCities) {
            newDistances.put(city, full.getDistances().get(city));
            LocalTime t = full.getArrivalTime().get(city);
            if(t != null) {
                newArrivalTime.put(city, t);
            }
            City prev = full.getPrevious().get(city);
            if(prev != null && subCities.contains(prev)) {
                newPrevious.put(city, prev);
                Departure dep = full.getChosenDeparture().get(city);
                if(dep != null) {
                    newChosenDepartures.put(city, dep);
                }
            }
        }
        return new DijkstraAlg.PathResult(newDistances, newPrevious, newChosenDepartures, newArrivalTime);
    }

    /**
     * Spaja dva rezultata u jednu rutu. Kombinuje postojeći root dio putanje sa novim nastavkom do odredišta.
     * @param rootResult Root dio putanje
     * @param spurResult Nastavak putanje
     * @param spurNode Čvor gdje se vrši spajanje
     * @param target Odredišni grad
     * @return Nova kompletna ruta
     */
    private static DijkstraAlg.PathResult mergeResults(DijkstraAlg.PathResult rootResult, DijkstraAlg.PathResult spurResult, City spurNode, City target) {
        Map<City, Integer> mergedDistances = new HashMap<>(rootResult.getDistances());
        Map<City, City> mergedPrevious = new HashMap<>(rootResult.getPrevious());
        Map<City, Departure> mergedChosenDeparture = new HashMap<>(rootResult.getChosenDeparture());
        Map<City, LocalTime> mergedArrivalTime = new HashMap<>(rootResult.getArrivalTime());

        List<City> spurPath = spurResult.reconstructPath(spurNode, target);

        for(City city : spurPath) {
            if(city.equals(spurNode)) {
                continue;
            }
            Integer distance = spurResult.getDistances().get(city);
            if(distance != null) {
                mergedDistances.put(city, distance);
            }

            City prev = spurResult.getPrevious().get(city);
            if(prev == null) {
                mergedPrevious.put(city, spurNode);
            } else {
                mergedPrevious.put(city, prev);
            }

            Departure departure = spurResult.getChosenDeparture().get(city);
            if(departure != null) {
                mergedChosenDeparture.put(city, departure);
            }

            LocalTime arrival = spurResult.getArrivalTime().get(city);
            if(arrival != null) {
                mergedArrivalTime.put(city, arrival);
            }
        }
        return new DijkstraAlg.PathResult(mergedDistances, mergedPrevious, mergedChosenDeparture, mergedArrivalTime);
    }
}