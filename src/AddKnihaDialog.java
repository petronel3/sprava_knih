import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AddKnihaDialog extends Stage {
    public AddKnihaDialog(KnihaDatabase db, ObservableList<Kniha> přečtenéKnihy, ObservableList<Kniha> rozečtenéKnihy, ObservableList<Kniha> chciČístKnihy, String username) {
        setTitle("Přidat novou knihu");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label nazevLabel = new Label("Název:");
        TextField nazevField = new TextField();
        Label autorLabel = new Label("Autor:");
        TextField autorField = new TextField();
        Label statusLabel = new Label("Status:");
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("rozečtená", "přečtená", "chci číst"));
        Label stranaLabel = new Label("Strana:");
        TextField stranaField = new TextField();
        stranaLabel.setVisible(false);
        stranaField.setVisible(false);
        Label popisLabel = new Label("Popis:");
        TextArea popisField = new TextArea();
        Label poznamkyLabel = new Label("Poznámky:");
        TextArea poznamkyField = new TextArea();

        statusChoiceBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.equals("rozečtená")) {
                stranaLabel.setVisible(true);
                stranaField.setVisible(true);
            } else {
                stranaLabel.setVisible(false);
                stranaField.setVisible(false);
            }
        });

        Button addButton = new Button("Přidat");
        addButton.setPrefWidth(300);
        addButton.setPrefHeight(50);
        addButton.setStyle("-fx-background-color: #90EE90;");
        addButton.setOnAction(e -> {
            try {
                Kniha kniha = new Kniha(0, nazevField.getText(), autorField.getText(), statusChoiceBox.getValue(),
                        statusChoiceBox.getValue().equals("rozečtená") ? Integer.parseInt(stranaField.getText()) : 0,
                        popisField.getText(), poznamkyField.getText(), username);

                db.addKniha(kniha, username);
                switch (kniha.getStatus()) {
                    case "rozečtená":
                        rozečtenéKnihy.add(kniha);
                        break;
                    case "přečtená":
                        přečtenéKnihy.add(kniha);
                        break;
                    case "chci číst":
                        chciČístKnihy.add(kniha);
                        break;
                }
                close();
            } catch (SQLException e1) {
                e1.printStackTrace();
                showErrorDialog("Error", "Failed to add book: " + e1.getMessage());
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
                showErrorDialog("Invalid Input", "Please enter a valid page number.");
            }
        });

        grid.add(nazevLabel, 0, 0);
        grid.add(nazevField, 1, 0);
        grid.add(autorLabel, 0, 1);
        grid.add(autorField, 1, 1);
        grid.add(statusLabel, 0, 2);
        grid.add(statusChoiceBox, 1, 2);
        grid.add(stranaLabel, 0, 3);
        grid.add(stranaField, 1, 3);
        grid.add(popisLabel, 0, 4);
        grid.add(popisField, 1, 4);
        grid.add(poznamkyLabel, 0, 5);
        grid.add(poznamkyField, 1, 5);

        vbox.getChildren().add(grid);
        vbox.getChildren().add(addButton);

        Scene scene = new Scene(vbox, 600, 500);
        setScene(scene);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}