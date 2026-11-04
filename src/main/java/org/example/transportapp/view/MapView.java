package org.example.transportapp.view;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.example.transportapp.data.TransportDataParser;
import org.example.transportapp.graph.DijkstraAlg;
import org.example.transportapp.graph.TransportGraph;
import org.example.transportapp.graph.YenAlg;
import org.example.transportapp.model.*;
import org.graphstream.graph.Edge;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Glavni JavaFX prikaz aplikacije transportne mreže.
 * Učitava podatke o gradovima i polascima iz JSON fajla i prikazuje interaktivnu mapu transportne mreže sa:
 * odabirom polaznog i odredišnog grada, opcijama optimizacije rute, prikaz mape, prikaz rezultata izvršavanjaalgoritma.
 */
public class MapView extends Application {
    private final String DATA_PATH = "transport_data.json";
    private final TransportDataParser transportDataParser = new TransportDataParser();
    private List<City> cities;
    private List<Departure> departures;
    private BorderPane root;
    private Label soldTicketsValue, totalIncomeValue;

    {
        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Učitava gradove i polaske iz JSON fajla.
     * @throws IOException ako dođe do greške pri čitanju fajla
     */
    private void loadData() throws IOException {
        transportDataParser.loadData(DATA_PATH);
        this.cities = transportDataParser.getCities();
        this.departures = transportDataParser.getDepartures();
        System.out.println("All data loaded from path: " + DATA_PATH);
        System.out.println("Cities - " + cities.size());
        System.out.println("Departures - " + departures.size());
    }

    /**
     * Kreira osnovni korisnički interfejs, postavlja scenu i prikazuje prozor.
     * @param primaryStage Glavni prozor aplikacije
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        root = new BorderPane();
        root.setPadding(new Insets(10));

        showMap(primaryStage);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Transport routes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Kreira i prikazuje interaktivnu mapu transportne mreže zajedno sa lijevim panelom.
     * Lijevi panel sadrži odabir polaznog i odredišnog grada, izbor kriterijuma optimizacije, legendu
     * i prikaz statistike o prodanim kartama i prihodima.
     * @param stage Trenutni prozor aplikacije
     */
    private void showMap(Stage stage) {
        //graf
        TransportGraph graph = new TransportGraph(cities, departures);
        Parent graphPane = graph.showGraph();
        root.setCenter(graphPane);

        //lijevi panel
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.getStyleClass().add("left-panel");

        Label from = new Label("From:");
        ComboBox<String> comboFrom = new ComboBox<>();
        comboFrom.setPrefWidth(200);

        Label to = new Label("To:");
        ComboBox<String> comboTo = new ComboBox<>();
        comboTo.setPrefWidth(200);

        try {
            loadCities(comboFrom, comboTo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Label criteria = new Label("Optimization criteria:");

        VBox.setMargin(comboTo, new Insets(0, 0, 15, 0));

        ToggleGroup criteriaGroup = new ToggleGroup();
        RadioButton buttonTime = new RadioButton("Fastest route");
        buttonTime.setToggleGroup(criteriaGroup);
        buttonTime.setSelected(true);
        RadioButton buttonPrice = new RadioButton("Cheapest route");
        buttonPrice.setToggleGroup(criteriaGroup);
        RadioButton buttonTransfer = new RadioButton("Fewest transfers");
        buttonTransfer.setToggleGroup(criteriaGroup);

        Button findRoute = new Button("Find route");
        findRoute.getStyleClass().add("action-button");

        HBox busLegend = createLegend("#FFD700", "BUS ROUTE");
        HBox trainLegend = createLegend("#32CD32", "TRAIN ROUTE");
        HBox optimalLegend = createLegend("#e74c3c", "OPTIMAL ROUTE");
        VBox legendBox = new VBox(5, busLegend, trainLegend, optimalLegend);
        legendBox.setPadding(new Insets(15, 0, 0, 0));

        //ovde treba dio za sold tickets
        Ticket ticket = new Ticket();
        Label soldTicketsLabel = new Label("Sold tickets:");
        Label totalIncomeLabel = new Label("Total income:");

        try {
            soldTicketsValue = new Label(String.valueOf(ticket.getSoldTicketsCount()));
            totalIncomeValue = new Label(String.valueOf(ticket.calculateTotalIncome()) + " KM");
        } catch (IOException e) {
            soldTicketsValue = new Label("0");
            totalIncomeValue = new Label("0 KM");
            System.err.println("Error loading statistics: " + e.getMessage());
        }

        soldTicketsLabel.getStyleClass().add("left-panel.label");
        soldTicketsValue.getStyleClass().add("left-panel.label");
        HBox soldTicketsBox = new HBox(5, soldTicketsLabel, soldTicketsValue);
        soldTicketsBox.setAlignment(Pos.CENTER_LEFT);
        totalIncomeLabel.getStyleClass().add("left-panel.label");
        totalIncomeValue.getStyleClass().add("left-panel.label");
        HBox totalIncomeBox = new HBox(5, totalIncomeLabel, totalIncomeValue);
        totalIncomeBox.setAlignment(Pos.CENTER_LEFT);

        findRoute.setOnAction(e -> {
            String fromCity = comboFrom.getSelectionModel().getSelectedItem();
            String toCity = comboTo.getSelectionModel().getSelectedItem();

            DijkstraAlg.Criteria criteriaSelected = DijkstraAlg.Criteria.TIME;
            if(buttonPrice.isSelected()) {
                criteriaSelected = DijkstraAlg.Criteria.PRICE;
            } else if(buttonTransfer.isSelected()) {
                criteriaSelected = DijkstraAlg.Criteria.TRANSFER;
            }

            City start = graph.getCityByName(fromCity);
            City target = graph.getCityByName(toCity);

            List<DijkstraAlg.PathResult> firstRoutes = DijkstraAlg.findAllRoutesDifferentFirstDeparture(graph, start, target, criteriaSelected);
            List<DijkstraAlg.PathResult> secondRoutes = YenAlg.findKShortestPaths(graph, start, target, criteriaSelected, 5);
            DijkstraAlg.PathResult shortestPath = DijkstraAlg.findBestRouteOverAllFirstDepartures(firstRoutes, target);
            List<DijkstraAlg.PathResult> top5routes = DijkstraAlg.topFiveUniqueRoutes(firstRoutes, secondRoutes, start, target);

            if(shortestPath == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No route found");
                alert.setHeaderText(null);
                alert.setContentText("No route could be found between " + start.getName() + " and " + target.getName());
                alert.showAndWait();
                return;
            }

            if(top5routes != null && !top5routes.isEmpty()) { //ispis provjera
                for (int i = 0; i < top5routes.size(); i++) {
                    DijkstraAlg.printRouteWithDepartures(top5routes.get(i), start, target);
                }
                System.out.println("\nThe best route:");
                DijkstraAlg.printRouteWithDepartures(shortestPath, start, target);

                //restartovanje grafa
                graph.getGsGraph().edges().forEach(edge -> {
                    String transportType = (String) edge.getAttribute("transportType");
                    if (transportType != null) {
                        edge.setAttribute("ui.class", transportType.toLowerCase());
                    }
                });
                //oznacavanje najkrace rute
                List<City> cityPath = shortestPath.reconstructPath(start, target);
                for (int i = 0; i < cityPath.size() - 1; i++) {
                    City cityFrom = cityPath.get(i);
                    City cityTo = cityPath.get(i + 1);

                    Departure departure = shortestPath.getChosenDeparture().get(cityTo);
                    if (departure == null) continue;
                    String edgeId = departure.getFromStation() + "-" + cityTo.getName() + "-" + departure.getDepartureTime() + "-" + departure.getDurationMinutes() + "-" + departure.getPrice();
                    Edge edge = graph.getGsGraph().getEdge(edgeId);
                    if (edge != null) {
                        edge.setAttribute("ui.class", "marked");
                    } else {
                        System.out.println("Edge not found for " + edgeId);
                    }
                }

                VBox rightPanel = addRightPanel(shortestPath, top5routes, start, target);
                root.setRight(rightPanel);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No route found");
                alert.setHeaderText(null);
                alert.setContentText("No route could be found between " + start.getName() + " and " + target.getName());
                alert.showAndWait();
            }
        });

        leftPanel.getChildren().addAll(from, comboFrom, to, comboTo, criteria, buttonTime, buttonPrice, buttonTransfer, findRoute,
                new Separator(), legendBox, new Separator(),
                soldTicketsBox, totalIncomeBox);
        root.setLeft(leftPanel);
    }

    /**
     * Kreira tabelu polazaka za zadatu rutu sa kolonama za vrijeme polska i dolaska, tip prevoza i cijenu.
     * @param route Ruta koja se prikazuje u tabeli
     * @param start Početni grad
     * @param target Odredišni grad
     * @return TableView sa polascima na ruti
     */
    public TableView<Departure> createRouteTable(DijkstraAlg.PathResult route, City start, City target) {
        TableView<Departure> routeTable = new TableView<>();
        routeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Departure, String> fromCol = new TableColumn<>("Departure");
        fromCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFromStation()
                                + " (" + data.getValue().getDepartureTime() + ")"
                )
        );

        TableColumn<Departure, String> toCol = new TableColumn<>("Arrival");
        toCol.setCellValueFactory(data -> {
            Departure d = data.getValue();
            try {
                LocalTime depTime = LocalTime.parse(d.getDepartureTime());
                LocalTime arrTime = depTime.plusMinutes(d.getDurationMinutes());
                return new SimpleStringProperty(d.getToCity() + " (" + arrTime + ")");
            } catch (Exception e) {
                return new SimpleStringProperty(d.getToCity() + " (time error)");
            }
        });

        TableColumn<Departure, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        dType(data.getValue().getType())
                )
        );

        TableColumn<Departure, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPrice()))
        );

        routeTable.getColumns().addAll(fromCol, toCol, typeCol, priceCol);

        List<Departure> ordered = new ArrayList<>();
        List<City> path = route.reconstructPath(start, target);
        for (int i = 0; i < path.size() - 1; i++) {
            City next = path.get(i + 1);
            Departure dep = route.getChosenDeparture().get(next);
            if (dep != null) ordered.add(dep);
        }

//        ordered.forEach(d ->
//                System.out.println("TABLE -> " + d.getFromStation() + " -> " + d.getToCity())
//        );

        routeTable.setItems(FXCollections.observableArrayList(ordered));
        return routeTable;
    }

    // Pomoćna metoda da normalizuje tip
    private String dType(String raw) {
        if (raw == null) return "";
        return raw.equalsIgnoreCase("autobus") ? "bus" : "train";
    }

    /**
     * Kreira i vraća desni panel koji prikazuje detalje o najkraćoj ruti i omogućava kupovinu karte ili pregled alternativnih ruta.
     * Panel sadrži tabelu sa podacima, dva dugmeta (kupovina karte i pregled dodatnih ruta).
     * @param shortestPath Najkraća ruta koja se prikazuje u tabeli
     * @param top5routes Top pet najbolji ruta
     * @param start Početni grad
     * @param target Odredišni grad
     * @return VBox koji predstavlja desni panel
     */
    private VBox addRightPanel(DijkstraAlg.PathResult shortestPath, List<DijkstraAlg.PathResult> top5routes, City start, City target) {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(350);
        rightPanel.getStyleClass().add("left-panel");

        Label routeLabel = new Label("ROUTE DETAILS");
        routeLabel.getStyleClass().add("left-panel.label");

        TableView<Departure> routeTableView = createRouteTable(shortestPath, start, target);
        routeTableView.setMinWidth(350);

        HBox totalTimeBox = new HBox(5);
        Label totalTimeLabel = new Label("Total travel time:");
        totalTimeLabel.getStyleClass().add("left-panel.label");
        Label totalTimeValue = new Label();
        totalTimeBox.getChildren().addAll(totalTimeLabel, totalTimeValue);

        HBox totalPriceBox = new HBox(5);
        Label totalPriceLabel = new Label("Total price:");
        totalPriceLabel.getStyleClass().add("left-panel.label");
        Label totalPriceValue = new Label();
        totalPriceBox.getChildren().addAll(totalPriceLabel, totalPriceValue);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button buyTicket = new Button("Buy ticket");
        Button moreRoutes = new Button("More routes");
        buyTicket.getStyleClass().add("action-button1");
        moreRoutes.getStyleClass().add("action-button1");
        buttonBox.getChildren().addAll(buyTicket, moreRoutes);

        addTimePriceData(shortestPath, start, target, totalTimeValue, totalPriceValue);

        buyTicket.setOnAction(e -> {
            try {
                Ticket ticket = new Ticket();
                ticket.generateTicket(start, target, shortestPath, totalTimeValue.getText(), totalPriceValue.getText());
                soldTicketsValue.setText(String.valueOf(ticket.getSoldTicketsCount()));
                totalIncomeValue.setText(String.valueOf(ticket.calculateTotalIncome()) + " KM");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        moreRoutes.setOnAction(e -> {
            if(top5routes != null && !top5routes.isEmpty()) {
                new TopRoutesView(this, top5routes, start, target).show();
            } else return;
        });

        rightPanel.getChildren().addAll(routeLabel, routeTableView, new Separator(),
                totalTimeBox, totalPriceBox, new Separator(), buttonBox);
        return rightPanel;
    }

    /**
     * Dodaje informacije o ukupnom vremenu i cijeni putovanja u zadate labele.
     * @param route Ruta nad kojom se vrši računanje
     * @param start Početni grad
     * @param target Odredišni grad
     * @param totalTimeValue Labela za ukupno trjanje putovanja
     * @param totalPriceValue Label za ukupnu cijenu
     */
    public void addTimePriceData(DijkstraAlg.PathResult route, City start, City target, Label totalTimeValue, Label totalPriceValue) {

        int totalMinutes = route.computeTotalMinutes(start, target);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        totalTimeValue.setText(hours + " h " + minutes + " min");

        int totalPrice = 0;
        List<City> path = route.reconstructPath(start, target);
        for(int i = 0; i < path.size() - 1; i++) {
            City next = path.get(i + 1);
            Departure departure = route.getChosenDeparture().get(next);
            if(departure != null) {
                totalPrice += departure.getPrice();
            }
        }
        totalPriceValue.setText(totalPrice + " KM");
    }

    /**
     * Učitava gradove i smješta ih u combo box-ove.
     * @param comboFrom ComboBox za početne gradove
     * @param comboTo ComboBox za odredišne gradove
     * @throws IOException ako dođe do greške prilikom učitavanja
     */
    private void loadCities(ComboBox<String> comboFrom, ComboBox<String> comboTo) throws IOException {
        for(City city : cities) {
            comboFrom.getItems().add(city.getName());
            comboTo.getItems().add(city.getName());
        }
        comboFrom.getSelectionModel().selectFirst();
        comboTo.getSelectionModel().selectLast();
    }

    /**
     * Kreira legendu sa informacija o određenim tipovima prevoza i optimalnoj ruti.
     * @param color Boja
     * @param text Tekst
     * @return HBox koji predstavlja legendu
     */
    private HBox createLegend(String color, String text) {
        Rectangle rectangle = new Rectangle(20,10);
        rectangle.setStyle("-fx-fill: " + color + "; -fx-stroke: #2c3e50");
        Label label = new Label(text);
        HBox hBox = new HBox(5, rectangle, label);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }

    /**
     * Postavlja novu vrijednost za labelu broja prodatih karata.
     * @param soldTicketsValue Vrijednost koja predstavlja broj prodatih karata
     */
    public void setSoldTicketsValue(String soldTicketsValue) {
        this.soldTicketsValue.setText(soldTicketsValue);
    }

    /**
     * Postavlja novu vrijednost za labelu ukupnog prihoda.
     * @param totalIncomeValue Vrijednost koja predstavlja ukupan prihod
     */
    public void setTotalIncomeValue(String totalIncomeValue) {
        this.totalIncomeValue.setText(totalIncomeValue);
    }

}
