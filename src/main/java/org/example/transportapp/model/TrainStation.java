package org.example.transportapp.model;

import java.util.List;

/**
 * Klasa koja predstavlja željezničku stanicu.
 * Nasljeđuje osnovne funkcionalnosti od Station klase i definiše tip stanice.
 * @see Station
 */
public class TrainStation extends Station {

    /**
     * Konstruktor za kreiranje željezničke stanice.
     * @param id Jedinstveni identifikator stanice
     * @param departureList Lista polazaka vezana za stanicu
     */
    public TrainStation(String id, List<Departure> departureList) {
        super(id, departureList);
    }

    /**
     * Vraća tip stanice - željeznička.
     * @return String "TRAIN" koji identifikuje tip stanice
     */
    @Override
    public String getStationType() {
        return "TRAIN";
    }
}