package org.example.transportapp.graph;

import org.example.transportapp.model.City;
import org.example.transportapp.model.Departure;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Klasa koja predstavlja implementaciju Dijkstra algoritma koji omogućava račuananje najkraćih ruta po različitim kriterijumima.
 * Kriterijumi: ukupno vrijeme putovanja, ukupna cijena putovanja i broj presjedanja.
 * Klasa koristi podatke o gradovima i polascima kako bi pronašla optimalne rute.
 */
public class DijkstraAlg {

    /**
     * Kriterijumi po kojima je moguće izvršiti optimizaciju.
     * TIME - ukupno trajanje putoavanja
     * PRICE - ukupna cijena putovanja
     * TRANSFER - ukupan broj presijedanja
     */
    public enum Criteria {
        TIME, PRICE, TRANSFER
    }

    /**
     * Pronalazak najkraće rute između gradova u zavisnosti od izabranog kriterijuma.
     * @param graph Graf koji predstavlja transportnu mrežu gradova
     * @param start Početni grad
     * @param criteria Kriterijum optimizacije
     * @param startTime Vrijeme polaska
     * @param distance Početna udaljenost
     * @return Objekat {@link PathResult} sa svim potrebnim informacijama
     */
    public static PathResult findShortestPath(TransportGraph graph, City start, Criteria criteria, LocalTime startTime, int distance) {
        Map<City, Integer> distances = new HashMap<>();
        Map<City, City> previous = new HashMap<>();
        Map<City, Departure> chosenDeparture = new HashMap<>();
        Map<City, LocalTime> arrivalTime = new HashMap<>();

        for(City city : graph.getAllCities()) {
            distances.put(city, Integer.MAX_VALUE);
            previous.put(city, null);
            arrivalTime.put(city, null);
        }

        LocalTime realStart = startTime != null ? startTime : LocalTime.MIN;

        distances.put(start, distance);
        arrivalTime.put(start, realStart);

        PriorityQueue<City> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        queue.add(start);
        boolean isStart = true;
        if(distance != 0) isStart = false;

        while(!queue.isEmpty()) {
            City currentCity = queue.poll();

            for(City neighbor : graph.getNeighborCities(currentCity)) {
                List<Departure> departures = graph.getDeparturesFromCity(currentCity);

                int minWeight = Integer.MAX_VALUE;
                int minSecondary = Integer.MAX_VALUE;
                Departure bestDeparture = null;

                for(Departure dep : departures) {
                    if(!dep.getToCity().equalsIgnoreCase(neighbor.getName())) {
                        continue;
                    }

                    LocalTime lastArrival = arrivalTime.get(currentCity);
                    LocalTime departureTime = LocalTime.parse(dep.getDepartureTime());

                    int waitMinutes = 0;
                    if(!isStart && lastArrival != null) {
                        waitMinutes = departureTime.toSecondOfDay() / 60 - lastArrival.toSecondOfDay() / 60;
                        if(waitMinutes < dep.getMinTransferTime()) {
                            waitMinutes += 24 * 60;
                        }
                    }

                   // int totalTime = (isStart ? dep.getDurationMinutes() : waitMinutes + dep.getDurationMinutes());
                    int totalTime = totalTime(dep, lastArrival, isStart);
                    int mainWeight = 0;
                    int secondaryWeight = 0;

                    switch (criteria) {
                        case TIME:
                            mainWeight = totalTime;
                            break;
                        case PRICE:
                            mainWeight = dep.getPrice() + distances.get(currentCity);
                            break;
                        case TRANSFER:
                            mainWeight = distances.get(currentCity) + 1;
                            secondaryWeight = distances.get(currentCity) + totalTime;
                            break;
                    }

                    boolean better = false;
                    if(criteria != Criteria.TRANSFER && mainWeight < minWeight) {
                        better = true;
                    } else if(criteria == Criteria.TRANSFER) {
                        if(mainWeight < minWeight || (mainWeight == minWeight && secondaryWeight < minSecondary)) {
                            better = true;
                        }
                    }

                    if(better) {
                        minWeight = mainWeight;
                        minSecondary = secondaryWeight;
                        bestDeparture = dep;
                    }

//                    System.out.printf("Checking departure %s -> %s | depTime: %s | duration: %d | price: %d | wait: %d | totalTime: %d\n",
//                            currentCity.getName(),
//                            neighbor.getName(),
//                            dep.getDepartureTime(),
//                            dep.getDurationMinutes(),
//                            dep.getPrice(),
//                            waitMinutes,
//                            totalTime
//                    );

                }

                if(bestDeparture != null) {
                    LocalTime newArrivalTime = LocalTime.parse(bestDeparture.getDepartureTime()).plusMinutes(bestDeparture.getDurationMinutes());
                    boolean shouldUpdate = false;
                    if(criteria != Criteria.TRANSFER) {
                        int newDistance = (criteria == Criteria.TIME ? distances.get(currentCity) + totalTime(bestDeparture, arrivalTime.get(currentCity), isStart)
                                : distances.get(currentCity) + bestDeparture.getPrice());
                        if(newDistance < distances.get(neighbor)) {
                            shouldUpdate = true;
                            distances.put(neighbor, newDistance);
                        }
                    } else {
                        int newMainDistance = distances.get(currentCity) + 1;
                        int newSecondary = distances.get(currentCity) + totalTime(bestDeparture, arrivalTime.get(currentCity), isStart);
                        int currentMain = distances.get(neighbor);
                        int currentSecondary = (arrivalTime.get(neighbor) != null ? arrivalTime.get(neighbor).toSecondOfDay() / 60 : Integer.MAX_VALUE);
                        if(newMainDistance < currentMain || (newMainDistance == currentMain && newSecondary < currentSecondary)) {
                            shouldUpdate = true;
                            distances.put(neighbor, newMainDistance);
                        }
                    }

                    if(shouldUpdate) {
                        previous.put(neighbor, currentCity);
                        chosenDeparture.put(neighbor, bestDeparture);
                        arrivalTime.put(neighbor, newArrivalTime);
                        queue.add(neighbor);

//                        System.out.printf("Updated %s: distance=%d, arrivalTime=%s\n",
//                                neighbor.getName(),
//                                distances.get(neighbor),
//                                arrivalTime.get(neighbor)
//                        );
                    }
                }
            }
            isStart = false;
        }
        return new PathResult(distances, previous, chosenDeparture, arrivalTime);
    }

    /**
     * Pomoćna metoda koja računa ukupno vrijeme (čekanje + trajanje putovanja) za određeni polazak.
     * @param departure Polazak
     * @param lastArrival Vrijeme dolaska u prethnodni grad
     * @param isStart Provjera da li je u pitanju početni grad
     * @return Ukupno vrijeme u minutama
     */
    private static int totalTime(Departure departure, LocalTime lastArrival, boolean isStart) {
        if(isStart || lastArrival == null) {
            return departure.getDurationMinutes();
        }
        int diff = LocalTime.parse(departure.getDepartureTime()).toSecondOfDay() / 60 - lastArrival.toSecondOfDay() / 60;
        if(diff < departure.getMinTransferTime()) {
            diff += 24 * 60;
        }
        return diff + departure.getDurationMinutes();
    }

    /**
     * Funkcija koja traži različite rute za sve moguće polaske iz početnog grada.
     * @param graph Transportni graf
     * @param start Početni grad
     * @param target Odredišni grad
     * @param criteria Kriterijum optimizacije
     * @return Lista svih pronađenih ruta sa različitim prvim polaskom
     */
    public static List<PathResult> findAllRoutesDifferentFirstDeparture(TransportGraph graph, City start, City target, DijkstraAlg.Criteria criteria) {
        TransportGraph clone = graph.cloneGraph();
        List<PathResult> results = new ArrayList<>();

        while(!clone.getDeparturesFromCity(start).isEmpty()) {
            PathResult best = findShortestPath(clone, start, criteria, null, 0);
            List<City> path = best.reconstructPath(start, target);
            if(path.isEmpty() || path.size() < 2) {
                break;
            }
            results.add(best);
            City nextCity = path.get(1);
            Departure firstDeparture = best.getChosenDeparture().get(nextCity);
            clone.removeEdge(start, nextCity, firstDeparture);
        }
        return results;
    }

    /**
     * Pronalazi najbolju rutu iz liste ruta koja se prosljeđuje.
     * @param allRoutes Lista svih ruta
     * @param target Odredišni grad
     * @return Najbolja ruta {@link PathResult}
     */
    public static PathResult findBestRouteOverAllFirstDepartures(List<PathResult> allRoutes, City target) {
        if(allRoutes == null || allRoutes.isEmpty()) {
            return null;
        }

        return allRoutes.stream().filter(r -> r.getDistances().get(target) != null)
                .min(Comparator.comparingInt(r -> r.getDistances().get(target))).orElse(null);
    }

    /**
     * Kombinuje dvije liste ruta i vraća najviše pet jedinstvenih.
     * Bez duplikata istih gradova i polazaka, sortirani po ukupnoj udaljenosti.
     * @param list1 Prva lista ruta
     * @param list2 Druga lista ruta
     * @param start Početni grad
     * @param target Odredišni grad
     * @return Lista pet najboljih ruta
     */
    public static List<DijkstraAlg.PathResult> topFiveUniqueRoutes(List<DijkstraAlg.PathResult> list1, List<DijkstraAlg.PathResult> list2, City start, City target) {
        List<DijkstraAlg.PathResult> merged = new ArrayList<>();
        merged.addAll(list1);
        merged.addAll(list2);

        List<DijkstraAlg.PathResult> results = new ArrayList<>();

        for (DijkstraAlg.PathResult candidate : merged) {
            Integer dist = candidate.getDistances().get(target);
            if (dist == null) continue;

            boolean alreadyExists = results.stream().anyMatch(r -> {
                List<City> p1 = r.reconstructPath(start, target);
                List<City> p2 = candidate.reconstructPath(start, target);

                if (!p1.equals(p2)) return false;

                for (City c : p1) {
                    Departure d1 = r.getChosenDeparture().get(c);
                    Departure d2 = candidate.getChosenDeparture().get(c);
                    if (!Objects.equals(d1, d2)) return false;
                }
                return true;
            });

            if (!alreadyExists) {
                results.add(candidate);
            }
        }

//        results.sort(Comparator.comparingInt(r -> r.getDistances().get(target)));
        results.sort(
                Comparator
                        .comparingInt((DijkstraAlg.PathResult r) -> r.getDistances().get(target))
                        .thenComparingInt(r -> r.computeTotalMinutes(start, target))
                        .thenComparingInt(r -> r.totalPrice(r, start, target))
        );

        return results.size() > 5 ? results.subList(0, 5) : results;
    }

    /**
     * Ispis rute u konzolu sa svim odabranim polascima, uključujući sve bitne informacije.
     * @param result Ruta za koju se ispisuju podaci
     * @param start Početni grad
     * @param target Odredišni grad
     */
    public static void printRouteWithDepartures(PathResult result, City start, City target) {
        if(result == null) {
            System.out.println("No available routes from " + start.getName() + " to " + target.getName());
            return;
        }
        List<City> path = result.reconstructPath(start, target);
        if(path.isEmpty()) {
            System.out.println("No available routes from " + start.getName() + " to " + target.getName());
            return;
        }

        System.out.println("Route from " + start.getName() + " to " + target.getName() + " - distance: " + result.getDistances().get(target));
        System.out.println("-------------------------------------------------------------------");

        for(int i = 0; i < path.size() - 1; i++) {
            City current = path.get(i);
            City next = path.get(i + 1);
            Departure departure = result.getChosenDeparture().get(next);
            if(departure != null) {
                int hours = departure.getDurationMinutes() / 60;
                int minutes = departure.getDurationMinutes() % 60;

                System.out.printf("%s -> %s | Type: %s | Departure: %s | Duration: %dh %dmin | Price: %d\n",
                        current.getName(),
                        next.getName(),
                        departure.getType(),
                        departure.getDepartureTime(),
                        hours,
                        minutes,
                        departure.getPrice()
                );
            }
        }
        System.out.println("-------------------------------------------------------------------");
    }


    /**
     * Rezultat izvršavanja Dijkstra algoritma koji sadrži:
     * mapu udaljenosti od startnog grada do svakog drugog, putanju prethodnih gradova,
     * izabrane polaske za svako kretanje i vrijeme dolaska u gradove.
     */
    public  static class PathResult {
        private final Map<City, Integer> distances;
        private final Map<City, City> previous;
        private final Map<City, Departure> chosenDeparture;
        private final Map<City, LocalTime> arrivalTime;

        public PathResult(Map<City, Integer> distances, Map<City, City> previous, Map<City, Departure> chosenDeparture, Map<City, LocalTime> arrivalTime) {
            this.distances = distances;
            this.previous = previous;
            this.chosenDeparture = chosenDeparture;
            this.arrivalTime = arrivalTime;
        }

        /**
         * Vraća mapu gradova i odgovarajućih distanci.
         * @return Mapa minimalnih udaljenosti
         */
        public Map<City, Integer> getDistances() {
            return distances;
        }

        /**
         * Vraća mapu gradova i njihovih susjeda.
         * @return Mapa gradova potrebna ze rekonstrukciju putanje
         */
        public Map<City, City> getPrevious() {
            return previous;
        }

        /**
         * Vraća mapu polazaka za svaki korak na putanji.
         * @return Mapa gradova i polazaka koji vode do njih
         */
        public Map<City, Departure> getChosenDeparture() {
            return chosenDeparture;
        }

        /**
         * Vraća mapu vremena dolaska u određene gradove.
         * @return Mapa gradova i dolaska u gradove
         */
        public Map<City, LocalTime> getArrivalTime() {
            return arrivalTime;
        }

        /**
         * Rekonstruiše listu gradova koji čine optimalnu rutu.
         * @param start Početni grad
         * @param target Odredišni grad
         * @return Lista gradova redom od startnog do odredišnog
         */
        public List<City> reconstructPath(City start, City target) {
            List<City> path = new ArrayList<>();
            City current = target;
            while(current != null) {
                path.add(0, current);
                current = previous.get(current);
            }
            if(!path.isEmpty() && path.get(0).equals(start)) {
                return path;
            } else {
                return Collections.emptyList();
            }
        }

        /**
         * Izračunava ukupno trajanje putovanja u minutama za određenu rutu.
         * Uključuje čekanje na presjedanjima.
         * @param start Početni grad
         * @param target Odredišni grad
         * @return Ukupno vrijeme u minutama
         */
        public int computeTotalMinutes(City start, City target) {
            List<City> path = reconstructPath(start, target);
            if (path.size() < 2) return 0;

            int totalMinutes = 0;
            LocalTime prevArrival = null;

            for (int i = 0; i < path.size() - 1; i++) {
                City next = path.get(i + 1);
                Departure dep = chosenDeparture.get(next);
                if (dep == null) continue;

                LocalTime depTime = LocalTime.parse(dep.getDepartureTime());

                if (prevArrival != null) {
                    int wait = depTime.toSecondOfDay()/60 - prevArrival.toSecondOfDay()/60;
                    if (wait < dep.getMinTransferTime()) {
                        wait += 24 * 60;
                    }
                    totalMinutes += wait;
                }

                totalMinutes += dep.getDurationMinutes();
                prevArrival = LocalTime.parse(dep.getDepartureTime()).plusMinutes(dep.getDurationMinutes());
            }
            return totalMinutes;
        }

        private static int totalPrice(DijkstraAlg.PathResult r, City start, City target) {
            int total = 0;
            List<City> path = r.reconstructPath(start, target);
            for (int i = 0; i < path.size() - 1; i++) {
                Departure d = r.getChosenDeparture().get(path.get(i + 1));
                if (d != null) total += d.getPrice();
            }
            return total;
        }


    }

}