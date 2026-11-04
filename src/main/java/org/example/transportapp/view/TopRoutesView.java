package org.example.transportapp.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.transportapp.graph.DijkstraAlg;
import org.example.transportapp.model.City;
import org.example.transportapp.model.Departure;
import org.example.transportapp.model.Ticket;

import java.io.IOException;
import java.util.List;

/**
 * JavaFX prozor koji prikazuje listu alternativnih ruta između dva grada i omogućava kupovinu karata za svaku od njih.
 * Za svaku rutu prikazuje tabelu sa polascima, ukupno trajanje putovanja i cijenu.
 * Nakon kupovine karte ažurira se broj pordatih karata i ukupni prihod u {@link MapView}.
 */
public class TopRoutesView extends Stage {

    private MapView mapView;

    /**
     * Kreira i prikazuje prozor sa alternativnim rutama.
     * @param mapView Glavni prikaz mape
     * @param routes Lista ruta koje se prikazuju
     * @param start Početni grad
     * @param target Odredišni grad
     */
    public TopRoutesView(MapView mapView, List<DijkstraAlg.PathResult> routes, City start, City target) {
        this.mapView = mapView;
        showTopRoutes(routes, start, target);
    }

    /**
     * Kreira korisnički interfejs za prikaz svih alternativnih ruta.
     * @param routes Lista ruta koje se prikazuju
     * @param start Početni grad
     * @param target Odredišni grad
     */
    private void showTopRoutes(List<DijkstraAlg.PathResult> routes, City start, City target) {
        setTitle("More routes");

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(15));

        Label title = new Label("ALTERNATIVE ROUTES FROM " + start.getName() + " TO " + target.getName());
        title.getStyleClass().add("route-label");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        mainContainer.getChildren().add(title);

        for(int i = 0; i < routes.size(); i++) {
            DijkstraAlg.PathResult route = routes.get(i);

            Label routeLabel = new Label("ROUTE " + (i + 1));
            routeLabel.getStyleClass().add("left-panel.label");

            TableView<Departure> routeTable = mapView.createRouteTable(route, start, target);
            routeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            routeTable.setMinWidth(630);
            routeTable.setPrefHeight(200);

            Label totalTimeValue = new Label();
            Label totalPriceValue = new Label();
            mapView.addTimePriceData(route, start, target, totalTimeValue, totalPriceValue);

            HBox timeBox = new HBox(5);
            Label totalTimeLabel = new Label("Total travel time:");
            totalTimeLabel.getStyleClass().add("left-panel.label");
            timeBox.getChildren().addAll(totalTimeLabel, totalTimeValue);

            HBox priceBox = new HBox(5);
            Label totalPriceLabel = new Label("Total price:");
            totalPriceLabel.getStyleClass().add("left-panel.label");
            priceBox.getChildren().addAll(totalPriceLabel, totalPriceValue);

            Button buyButton = new Button("Buy ticket");
            buyButton.getStyleClass().add("action-button1");

            buyButton.setOnAction(e -> {
                try {
                    Ticket ticket = new Ticket();
                    ticket.generateTicket(start, target, route, totalTimeValue.getText(), totalPriceValue.getText());
                    mapView.setSoldTicketsValue(String.valueOf(ticket.getSoldTicketsCount()));
                    mapView.setTotalIncomeValue(String.valueOf(ticket.calculateTotalIncome()) + " KM");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            VBox infoBox = new VBox(10, timeBox, priceBox, new Separator(), buyButton);
            infoBox.setPadding(new Insets(0, 0, 0, 20));
            infoBox.setAlignment(Pos.TOP_LEFT);
            infoBox.setMinWidth(200);

            HBox contentRow = new HBox(20, routeTable, infoBox);
            contentRow.setAlignment(Pos.CENTER_LEFT);

            VBox routeBox = new VBox(10, routeLabel, contentRow);
            routeBox.setPadding(new Insets(10));
            routeBox.getStyleClass().add("left-panel");

            mainContainer.getChildren().add(routeBox);

            if(i < routes.size() - 1) {
                Separator routeSeparator = new Separator();
                routeSeparator.setPadding(new Insets(10, 0, 10, 0));
                mainContainer.getChildren().add(routeSeparator);
            }
        }
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1000, 660);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        this.setScene(scene);
        setResizable(false);

    }
}
