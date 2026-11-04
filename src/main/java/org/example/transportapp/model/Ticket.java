package org.example.transportapp.model;

import javafx.scene.control.Alert;
import org.example.transportapp.graph.DijkstraAlg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Klasa koja omogućava generisanje, čuvanje i analizu transportnih karata.
 */
public class Ticket {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Generiše i čuva kartu u direktorijumu za određenu rutu.
     * @param start Početni grad
     * @param target Odredišni grad
     * @param route Ruta za koju se generiše karta
     * @param totalTime Ukupno vrijeme putovanja
     * @param totalPrice Ukupna cijena putovanja
     * @throws IOException Izuzetak koji se baca ukoliko dođe do greške
     */
    public void generateTicket(City start, City target, DijkstraAlg.PathResult route, String totalTime, String totalPrice) throws IOException {
        List<City> path = route.reconstructPath(start, target);
        List<Departure> departures = new ArrayList<>();
        for(int i = 0; i < path.size(); i++) {
            Departure departure = route.getChosenDeparture().get(path.get(i));
            if (departure != null) {
                departures.add(departure);
            }
        }

        Path ticketDir = Paths.get("tickets");
        if(!Files.exists(ticketDir)) {
            Files.createDirectories(ticketDir);
        }

        String totalPriceClean = totalPrice.replaceAll("[^0-9]", "");
        String ticketId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String content = buildContent(start, target, departures, totalTime, totalPriceClean, ticketId);

        String fileName = String.format("Ticket_%s_to_%s_%s_%s.txt",
                start.getName(),
                target.getName(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                ticketId);

        Path filePath = ticketDir.resolve(fileName);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));

        showSuccessAlert(filePath);
    }

    /**
     * Sastavlja kompletan tekst koji će se nalaziti u tekstualnom fajlu.
     * Sadrži informacije o izabranoj ruti, o cijeni, datumu i ukupnom vremenu putoavanja.
     * Čuva i jedinstveni identifikator.
     * @param start Početni grad
     * @param target Odredišni grad
     * @param departures Lista polazaka u ruti
     * @param totalTime Ukupno trajanje putovanja
     * @param totalPrice Ukupna cijena putovanja
     * @param ticketId Identifikator karte
     * @return Kompletno formatiran tekst karte spreman za zapis
     */
    private String buildContent(City start, City target, List<Departure> departures, String totalTime, String totalPrice, String ticketId) {
        final int WIDTH = 62;
        StringBuilder sb = new StringBuilder();

        sb.append("  ╔").append("═".repeat(WIDTH)).append("╗\n");
        sb.append(String.format("  ║ %-" + (WIDTH - 2) + "s ║\n", "TRANSPORTATION BILL"));
        sb.append("  ╚").append("═".repeat(WIDTH)).append("╝\n\n");

        sb.append("  ╔").append("═".repeat(WIDTH)).append("╗\n");
        sb.append(String.format("  ║ %-25s %34s ║\n", "FROM:", start.getName()));
        sb.append(String.format("  ║ %-25s %34s ║\n", "TO:", target.getName()));
        sb.append(String.format("  ║ %-25s %34s ║\n", "TRAVEL TIME:", totalTime));
        sb.append(String.format("  ║ %-25s %34s ║\n", "TOTAL PRICE:", totalPrice + " KM"));
        sb.append("  ╚").append("═".repeat(WIDTH)).append("╝\n\n");

        sb.append("  ╔══════════════════════╦══════════════════════╦════════╦═══════╗\n");
        sb.append("  ║      DEPARTURE       ║       ARRIVAL        ║  TYPE  ║ PRICE ║\n");
        sb.append("  ╠══════════════════════╬══════════════════════╬════════╬═══════╣\n");

        for(Departure departure : departures) {
            LocalTime departureTime = LocalTime.parse(departure.getDepartureTime(), TIME_FORMATTER);
            LocalTime arrivalTime = departureTime.plusMinutes(departure.getDurationMinutes());
            String type = departure.getType().equalsIgnoreCase("autobus") ? "BUS" : "TRAIN";

            sb.append(String.format("  ║ %-20s ║ %-20s ║ %-6s ║ %-5d ║\n",
                    departure.getFromStation() + " (" + departure.getDepartureTime() + ")",
                    departure.getToCity() + " (" + arrivalTime + ")",
                    type,
                    departure.getPrice()));
        }
        sb.append("  ╚══════════════════════╩══════════════════════╩════════╩═══════╝\n\n");

        sb.append("  ╔").append("═".repeat(WIDTH)).append("╗\n");
        sb.append(String.format("  ║ %-25s %34s ║\n",
                "DATE:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        sb.append(String.format("  ║ %-25s %34s ║\n",
                "TIME:", LocalTime.now().format(TIME_FORMATTER)));
        sb.append(String.format("  ║ %-25s %34s ║\n", "TICKET ID:", ticketId));
        sb.append("  ╚").append("═".repeat(WIDTH)).append("╝\n");

        return sb.toString();
    }

    /**
     * Prikazuje prozor sa porukom da je karta uspješno generisana.
     * @param filePath Putanja do sačuvane karte
     */
    private void showSuccessAlert(Path filePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ticket generated");
        alert.setHeaderText("Ticket has been successfully saved");
        alert.setContentText("Location: " + filePath.toAbsolutePath());
        alert.showAndWait();
    }

    /**
     * Vraća ukupan broj generisanih karata.
     * @return Broj karata
     * @throws IOException ako dođe do greške pri čitanju direktorijuma
     */
    public int getSoldTicketsCount() throws IOException {
        Path ticketDir = Paths.get("tickets");
        if(Files.exists(ticketDir)) {
            try (var files = Files.list(ticketDir)) {
                return (int) files.filter(p -> p.toString().endsWith(".txt")).count();
            }
        }
        return 0;
    }

    /**
     * Računa ukupan prihod na osnovu svih sačuvanih karata.
     * @return Ukupan prihod od prodaje karata
     * @throws IOException ako dođe do greške prilikom čitanja fajlova
     */
    public int calculateTotalIncome() throws IOException {
        Path ticketDir = Paths.get("tickets");
        if(!Files.exists(ticketDir)) return 0;
        try (var files = Files.list(ticketDir)) {
            return files.filter(p -> p.toString().endsWith(".txt"))
                    .mapToInt(this::extractFromTicket)
                    .sum();
        }
    }

    /**
     * Pomoćna metoda koja iz teksta pojedinačnih karata izdvaja ukupnu cijenu.
     * @param ticketPath Putanja do fajla karte
     * @return Ukupna cijena
     */
    private int extractFromTicket(Path ticketPath) {
        try {
            String ticket = Files.readString(ticketPath, StandardCharsets.UTF_8);
            for(String line : ticket.split("\n")) {
                if(line.contains("TOTAL PRICE:")) {
                    String price = line.replaceAll(".*TOTAL PRICE:\\s*", "").replaceAll("[^0-9]", "").trim();
                    return Integer.parseInt(price);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading ticket: " + ticketPath);
        }
        return 0;
    }
}
