import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class DetailKnihaDialog extends Stage {

    private KnihaDatabase db;
    private TableView<Kniha> tableView;
    private Kniha kniha;
    private ObservableList<Kniha> finishedBooks;
    private ObservableList<Kniha> readBooks;
    private ObservableList<Kniha> wantToReadBooks;

    public DetailKnihaDialog(Kniha kniha, KnihaDatabase db, TableView<Kniha> tableView,
                             ObservableList<Kniha> finishedBooks, ObservableList<Kniha> readBooks, ObservableList<Kniha> wantToReadBooks) {
        this.db = db;
        this.tableView = tableView;
        this.kniha = kniha;
        this.finishedBooks = finishedBooks;
        this.readBooks = readBooks;
        this.wantToReadBooks = wantToReadBooks;

        setTitle("Detail knihy");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label();
        titleLabel.textProperty().bind(kniha.nazevProperty());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setWrapText(true);

        vbox.getChildren().add(titleLabel);

        vbox.getChildren().addAll(
                createLabeledField("Autor: ", kniha.autorProperty()),
                createLabeledField("Status: ", kniha.statusProperty())
        );

        if (kniha.getStatus().equals("rozečtená")) {
            vbox.getChildren().add(createLabeledField("Strana: ", kniha.stranaProperty().asString()));
        }

        vbox.getChildren().addAll(
                createLabeledFieldWithMaxHeight("Popis: ", kniha.popisProperty(), 150),
                createLabeledFieldWithMaxHeight("Poznámky: ", kniha.poznamkyProperty(), 250)
        );

        // Tlačítko Upravit
        Button editButton = new Button("Upravit");
        editButton.setOnAction(event -> {
            EditKnihaDialog editKnihaDialog = new EditKnihaDialog(kniha, db, tableView, finishedBooks, readBooks, wantToReadBooks);
            close();
            editKnihaDialog.showAndWait();
            try {
                refreshListsAndTables();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // Tlačítko Smazat
        Button deleteButton = new Button("Smazat");
        deleteButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Potvrzení smazání");
            alert.setHeaderText(null);
            alert.setContentText("Opravdu chcete smazat tuto knihu?");

            ButtonType buttonTypeYes = new ButtonType("Ano", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("Ne", ButtonBar.ButtonData.NO);

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonTypeYes) {
                    try {
                        db.deleteKniha(kniha);
                        removeKnihaFromList(kniha);
                        tableView.getItems().remove(kniha);
                        close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("An error occurred while deleting the book: " + e.getMessage());
                    }
                } else {
                    alert.close();
                }
            });
        });

        HBox buttonBox = new HBox(10, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox, 750, 500);
        setScene(scene);
    }

    private void removeKnihaFromList(Kniha kniha) {
        if (kniha.getStatus().equals("přečtená")) {
            finishedBooks.remove(kniha);
        } else if (kniha.getStatus().equals("rozečtená")) {
            readBooks.remove(kniha);
        } else if (kniha.getStatus().equals("chci číst")) {
            wantToReadBooks.remove(kniha);
        }
    }

    private void refreshListsAndTables() throws SQLException {
        finishedBooks.setAll(db.getKnihyByStatusAndUser("přečtená", kniha.getUzivatel()));
        readBooks.setAll(db.getKnihyByStatusAndUser("rozečtená", kniha.getUzivatel()));
        wantToReadBooks.setAll(db.getKnihyByStatusAndUser("chci číst", kniha.getUzivatel()));
        tableView.refresh();
    }

    private HBox createLabeledField(String labelText, Object fieldValue) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Label valueLabel = new Label();

        if (fieldValue instanceof StringBinding) {
            valueLabel.textProperty().bind((StringBinding) fieldValue);
        } else if (fieldValue instanceof ReadOnlyProperty) {
            valueLabel.textProperty().bind(((ReadOnlyProperty<String>) fieldValue));
        }

        valueLabel.setWrapText(true);
        HBox hbox = new HBox(5, label, valueLabel);
        hbox.setAlignment(Pos.TOP_LEFT);
        return hbox;
    }

    private VBox createLabeledFieldWithMaxHeight(String labelText, ReadOnlyStringProperty fieldValue, int maxHeight) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextArea valueTextArea = new TextArea();
        valueTextArea.textProperty().bind(fieldValue);
        valueTextArea.setWrapText(true);
        valueTextArea.setEditable(false);
        valueTextArea.setPrefHeight(maxHeight);
        valueTextArea.setMaxHeight(maxHeight);
        VBox vbox = new VBox(5, label, valueTextArea);
        vbox.setAlignment(Pos.TOP_LEFT);
        return vbox;
    }
}