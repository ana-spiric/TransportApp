package org.example.transportapp.model;

import java.util.Objects;

/**
 * Klasa koja predstavlja polazak sa neke stranice ka odredišnom gradu.
 * Sadrži informacije o vremenu polaska, trajanju putovanja, cijeni i minimalnom vremenu koje je potrebno za transfer.
 * Sadrži i informaciju o tipu prevoza koji je u pitanju (autobus ili voz).
 */
public class Departure {
    private String type;
    private String fromStation;
    private String toCity;
    private String departureTime;
    private int durationMinutes;
    private int price;
    private int minTransferTime;

    /**
     * Konstruktor koji kreira novi polazak sa zadatim parametrima.
     * @param type Tip prevoza (autobus ili voz)
     * @param fromStation Stanica polaska
     * @param toCity Odredišni grad
     * @param departureTime Vrijeme polaska
     * @param durationMinutes Trajanje putovanja u minutama
     * @param price Cijena karte
     * @param minTransferTime Minimalno vrijeme potrebno za transfer u minutama
     */
    public Departure(String type, String fromStation, String toCity, String departureTime, int durationMinutes, int price, int minTransferTime) {
        this.type = type;
        this.fromStation = fromStation;
        this.toCity = toCity;
        this.departureTime = departureTime;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    /**
     * Vraća tip prevoza (autobus ili voz).
     * @return String koji predstavlja tip prevoza
     */
    public String getType() {
        return type;
    }

    /**
     * Postavlja tip prevoza.
     * @param type Tip prevoza koji se dodjeljuje
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Vraća stanicu polaska.
     * @return String koji predstavlja naziv stanice
     */
    public String getFromStation() {
        return fromStation;
    }

    /**
     * Postavlja stanicu polaska.
     * @param fromStation Nova stanica polaska
     */
    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    /**
     * Vraća odredišni grad.
     * @return String koji reprezentuje naziv grada
     */
    public String getToCity() {
        return toCity;
    }

    /**
     * Postavlja odredišni grad.
     * @param toCity Novi odredišni grad
     */
    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    /**
     * Vraća vrijeme polaska.
     * @return Vrijeme polaska
     */
    public String getDepartureTime() {
        return departureTime;
    }

    /**
     * Postavlja vrijeme polaska
     * @param departureTime Novo vrijeme polaska
     */
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Vraća trajanje putovanja u minutama.
     * @return Trajanje u minutama
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Postavlja trajanje putovanja.
     * @param durationMinutes Novo trajanje u minutama
     */
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * Vraća cijenu karte.
     * @return Cijena
     */
    public int getPrice() {
        return price;
    }

    /**
     * Postavlja cijenu karte.
     * @param price Nova cijena
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * Vraća minimalno vrijeme potrebno za transfer.
     * @return Vrijeme u minutama potrebno za transfer
     */
    public int getMinTransferTime() {
        return minTransferTime;
    }

    /**
     * Postavlja minimalno vrijeme transfera.
     * @param minTransferTime Novo vrijeme potrebno za transfer u minutama
     */
    public void setMinTransferTime(int minTransferTime) {
        this.minTransferTime = minTransferTime;
    }

    /**
     * Vraća opis polaska sa svim informacijama koje ga opisuju.
     * @return String koji predstavlja opis polaska
     */
    @Override
    public String toString() {
        return String.format("\n(%s, From - To: %s -> %s, Departure time: %s, Duration: %dmin, Price: %d, Min transfer time: %dmin)",
                getType(),
                getFromStation(),
                getToCity(),
                getDepartureTime(),
                getDurationMinutes(),
                getPrice(),
                getMinTransferTime());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Departure)) return false;
        Departure dep = (Departure) o;

        return durationMinutes == dep.durationMinutes && price == dep.price && minTransferTime == dep.minTransferTime &&
                Objects.equals(type, dep.type) && Objects.equals(fromStation, dep.fromStation) &&
                Objects.equals(toCity, dep.toCity) && Objects.equals(departureTime, dep.departureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fromStation, toCity, departureTime, durationMinutes, price, minTransferTime);
    }
}
