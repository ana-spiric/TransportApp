package org.example.transportapp.demo;

import javafx.application.Application;
import org.example.transportapp.view.InsertDataWindow;

/**
 * Klasa koja pokreće JavaFX aplikaciju za generisanje i vizuelizaciju transportne mreže.
 * Ova klasa služi kao ulazna tačka u aplikaciju i pokreće glavni JavaFX prozor {@link InsertDataWindow}.
 */
public class Main {

    /**
     * Glavna metoda koja se poziva pri pokretanju aplikacije.
     * Pokreće JavaFX aplikaciju pozivom klase MainApp koja sadrži glavni korisnički interfejs.
     * @param args Argumenti komandne linije
     */
    public static void main(String[] args) {
        Application.launch(InsertDataWindow.class, args);
    }
}
