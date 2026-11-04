package org.example.transportapp.model;

import java.util.List;

/**
 * Apstraktna klasa koja predstavlja stanicu u transportnom sistemu.
 * Služi kao osnova za raličite tipove stanica: autobuska i željeznička.
 * Zajednički atributi su identifikator stanice i lista polazaka.
 */
public abstract class Station {
    private String id;
    private List<Departure> departureList;

    /**
     * Konstruktor za kreiranje nove stanice.
     * @param id Jedinstveni identifikator stanice
     * @param departureList Lista polazaka vezana za stanicu
     */
    public Station(String id, List<Departure> departureList) {
        this.id = id;
        this.departureList = departureList;
    }

    /**
     * Vraća jedinstveni identifikator stanice.
     * @return String koji reprezentuje id stanice
     */
    public String getId() {
        return id;
    }

    /**
     * Postavlja jedinstveni identifikator stanice.
     * @param id Vrijednost jedinstvenog identifikatora koja se dodjeljuje
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Vraća listu polazaka iz stanice.
     * @return Lista objekata tipa Departure
     */
    public List<Departure> getDepartureList() {
        return departureList;
    }

    /**
     * Postavlja listu polazaka za stanicu.
     * @param departureList Lista polazaka koja se dodjeljuje stanici
     */
    public void setDepartureList(List<Departure> departureList) {
        this.departureList = departureList;
    }

    /**
     * Vraća tip stanice.
     * Izvedene klase moraju implementirati ovu metodu da bi vratile svoj specifični tip stanice.
     * @return String koji predstavlja tip stanice
     */
    public abstract String getStationType();

    /**
     * Vraća opis stanice, koji uključuje identifikator, tip i informacije o polascima.
     * @return String sa opisom stanice.
     */
    @Override
    public String toString() {
        return "\nStation: " + id + " (Type: " + getStationType() + ", Departures: " + departureList + ")";
    }
}

