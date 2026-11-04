package org.example.transportapp.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.transportapp.data.TransportDataGenerator;

/**
 * JavaFX aplikacija koja omogućava unos dimenzija (redova i kolona) za generisanje slučajnih transportnih podataka i mape.
 * Korisnik unosi broj redova i kolona, a zatim se pokreće {@link TransportDataGenerator} čiji
 * se rezultat snima u JSON fajl i otvara se glavna mapa {@link MapView}.
 */
public class InsertDataWindow extends Application {
    private TransportDataGenerator transportDataGenerator;

    /**
     * Ulazna tačka apliakcije. Pokreće prozor za unos dimenzija mape.
     * @param primaryStage Glavni prozor
     */
    @Override
    public void start(Stage primaryStage) {
        showInputWindow(primaryStage);
    }

    /**
     * Prikazuje prozor u koji korisnik unosi podatke za generisanje.
     * @param primaryStage Prosljeđeni Stage iz start metode
     */
    private void showInputWindow(Stage primaryStage) {
        Stage inputStage = new Stage();

        Label titleLabel = new Label("Enter the dimensions for your country map");
        titleLabel.getStyleClass().add("subtitle-label");

        TextField rowsField = new TextField();
        rowsField.setPromptText("Number of rows (n)");
        rowsField.getStyleClass().add("input-field");

        TextField colsField = new TextField();
        colsField.setPromptText("Number of columns (m)");
        colsField.getStyleClass().add("input-field");

        rowsField.setTextFormatter(new TextFormatter<>(txt ->
                txt.getControlNewText().matches("\\d*") ? txt : null));
        colsField.setTextFormatter(new TextFormatter<>(txt ->
                txt.getControlNewText().matches("\\d*") ? txt : null));

        Button generateButton = new Button("Generate");
        generateButton.getStyleClass().add("action-button");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("input-grid");
        grid.add(new Label("Rows (n):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Columns (m):"), 0, 1);
        grid.add(colsField, 1, 1);

        VBox root = new VBox(20);
        root.getStyleClass().add("main-container");
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(titleLabel, grid, generateButton);

        VBox.setMargin(titleLabel, new Insets(20, 0, 20, 0));
        VBox.setMargin(grid, new Insets(0, 0, 20, 0));

        generateButton.setOnAction(e -> {
            try {
                int n = Integer.parseInt(rowsField.getText());
                int m = Integer.parseInt(colsField.getText());
                if(n <= 0 || m <= 0) {
                    throw new NumberFormatException("Numbers must be positive.");
                }

                transportDataGenerator = new TransportDataGenerator(n, m);
                TransportDataGenerator.TransportData data = transportDataGenerator.generateData();
                transportDataGenerator.saveToJson(data, "transport_data.json");

                Stage mainStage = new Stage();
                new MapView().start(mainStage);

                inputStage.close();
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid input");
                alert.setHeaderText("Invalid map dimensions");
                alert.setContentText("Please enter positive whole numbers for rows and columns.");
                alert.showAndWait();
            }
        });

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        inputStage.setScene(scene);
        inputStage.setResizable(false);
        inputStage.show();
    }
}
