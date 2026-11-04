package org.example.transportapp.model;

import java.util.List;

/**
 * Klasa koja predstavlja autobusku stanicu.
 * Nasljeđuje osnovne funkcionalnosti od Station klase i definiše tip stanice.
 * @see Station
 */
public class BusStation extends Station {

    /**
     * Konstruktor za kreiranje autobuske stanice.
     * @param id Jedinstveni identifikator stanice
     * @param departureList Lista polazaka vezana za stanicu
     */
    public BusStation(String id, List<Departure> departureList) {
        super(id, departureList);
    }

    /**
     * Vraća tip stanice - autobuska.
     * @return String "BUS" koji identifikuje tip stanice
     */
    @Override
    public String getStationType() {
        return "BUS";
    }
}
