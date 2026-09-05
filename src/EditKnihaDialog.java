import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditKnihaDialog extends Stage {

    private KnihaDatabase db;
    private Kniha kniha;
    private ObservableList<Kniha> finishedBooks;
    private ObservableList<Kniha> readBooks;
    private ObservableList<Kniha> wantToReadBooks;
    private TableView<Kniha> tableView;

    public EditKnihaDialog(Kniha kniha, KnihaDatabase db, TableView<Kniha> tableView,
                           ObservableList<Kniha> finishedBooks, ObservableList<Kniha> readBooks, ObservableList<Kniha> wantToReadBooks) {
        this.db = db;
        this.kniha = kniha;
        this.tableView = tableView;
        this.finishedBooks = finishedBooks;
        this.readBooks = readBooks;
        this.wantToReadBooks = wantToReadBooks;

        setTitle("Upravit knihu");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        TextField nazevField = new TextField();
        nazevField.setText(kniha.getNazev());
        TextField autorField = new TextField();
        autorField.setText(kniha.getAutor());
        TextArea popisArea = new TextArea();
        popisArea.setText(kniha.getPopis());
        TextArea poznamkyArea = new TextArea();
        poznamkyArea.setText(kniha.getPoznamky());

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("přečtená", "rozečtená", "chci číst");
        statusComboBox.setValue(kniha.getStatus());

        TextField stranaField = new TextField();
        Label stranaLabel = new Label("Strana:");
        gridPane.add(stranaLabel, 0, 3);
        gridPane.add(stranaField, 1, 3);

        // Listener na změny v ComboBoxu pro status
        statusComboBox.setOnAction(event -> {
            String selectedStatus = statusComboBox.getValue();
            if (selectedStatus.equals("rozečtená")) {
                stranaLabel.setVisible(true);
                stranaField.setVisible(true);
                stranaField.setText(String.valueOf(kniha.getStrana()));
            } else {
                stranaLabel.setVisible(false);
                stranaField.setVisible(false);
                stranaField.setText(""); // Reset hodnoty pole stran
            }
        });

        // Nastavení viditelnosti v závislosti na aktuálním statusu
        if (kniha.getStatus().equals("rozečtená")) {
            stranaLabel.setVisible(true);
            stranaField.setVisible(true);
            stranaField.setText(String.valueOf(kniha.getStrana()));
        } else {
            stranaLabel.setVisible(false);
            stranaField.setVisible(false);
        }

        gridPane.add(new Label("Název:"), 0, 0);
        gridPane.add(nazevField, 1, 0);
        gridPane.add(new Label("Autor:"), 0, 1);
        gridPane.add(autorField, 1, 1);
        gridPane.add(new Label("Status:"), 0, 2);
        gridPane.add(statusComboBox, 1, 2);
        gridPane.add(new Label("Popis:"), 0, 4);
        gridPane.add(popisArea, 1, 4);
        gridPane.add(new Label("Poznámky:"), 0, 5);
        gridPane.add(poznamkyArea, 1, 5);

        Button saveButton = new Button("Uložit");
        saveButton.setOnAction(event -> {
            kniha.setNazev(nazevField.getText());
            kniha.setAutor(autorField.getText());
            kniha.setPopis(popisArea.getText());
            kniha.setPoznamky(poznamkyArea.getText());

            String newStatus = statusComboBox.getValue();
            if (newStatus.equals("rozečtená")) {
                kniha.setStrana(Integer.parseInt(stranaField.getText()));
            } else {
                kniha.setStrana(0);
            }
            kniha.setStatus(newStatus);

            try {
                db.updateKniha(kniha);
                refreshListsAndTables();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            close();
        });

        gridPane.add(saveButton, 1, 6);
        Scene scene = new Scene(gridPane, 600, 400);
        setScene(scene);
    }

    private void refreshListsAndTables() throws SQLException {
        finishedBooks.setAll(db.getKnihyByStatusAndUser("přečtená", kniha.getAutor()));
        readBooks.setAll(db.getKnihyByStatusAndUser("rozečtená", kniha.getAutor()));
        wantToReadBooks.setAll(db.getKnihyByStatusAndUser("chci číst", kniha.getAutor()));
        tableView.refresh();
    }
}