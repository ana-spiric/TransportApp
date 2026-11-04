package org.example.transportapp.model;

/**
 * Klasa koja predstavlja grad u transportnom sistemu.
 * Svaki grad karakteriše naziv, autobuska i željeznička stanica.
 */
public class City {
    private String name;
    private BusStation busStation;
    private TrainStation trainStation;

    /**
     * Konstruktor koji kreira novi grad sa zadatim stanicama.
     * @param name Naziv grada
     * @param busStation Autobuska stanica grada
     * @param trainStation Željeznička stanica grada
     */
    public City(String name, BusStation busStation, TrainStation trainStation) {
        this.name = name;
        this.busStation = busStation;
        this.trainStation = trainStation;
    }

    /**
     * Vraća naziv grada.
     * @return String koji predstavlja naziv grada
     */
    public String getName() {
        return name;
    }

    /**
     * Postavlja naziv grada.
     * @param name Naziv grada koji se dodjeljuje
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Vraća autobusku stanicu.
     * @return BusStation objekat koji predstavlja autobusku stanicu grada
     */
    public BusStation getBusStation() {
        return busStation;
    }

    /**
     * Postavlja autobusku stanicu grada.
     * @param busStation Autobuska stanica koja se dodjeljuje
     */
    public void setBusStation(BusStation busStation) {
        this.busStation = busStation;
    }

    /**
     * Vraća željezničku stanicu.
     * @return TrainStation objekat koji predstavlja željezničku stanicu grada
     */
    public TrainStation getTrainStation() {
        return trainStation;
    }

    /**
     * Postavlja željezničku stanicu grada.
     * @param trainStation Željeznička stanica koja se dodjeljuje
     */
    public void setTrainStation(TrainStation trainStation) {
        this.trainStation = trainStation;
    }

    /**
     * Vraća opis grada koji uključuje naziv, autobusku i željezničku stanicu.
     * @return String sa opisom grada
     */
    @Override
    public String toString() {
        return "\nCity: " + name + "\nBus station: " + busStation + ", \nTrain station: " + trainStation;
    }
}
