import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.sql.SQLException;

public class KnihaApp extends Application {
    private KnihaDatabase db;
    private UserDatabase userDb;
    public ObservableList<Kniha> readBooks;
    public ObservableList<Kniha> finishedBooks;
    public ObservableList<Kniha> wantToReadBooks;
    private static final String DB_NAME = "knihydb";
    private static final String HOST = "localhost:5432";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Password";
    private String currentUsername;

    @Override
    public void start(Stage primaryStage) {
        try {
            userDb = new UserDatabase("jdbc:postgresql://" + HOST + "/" + DB_NAME, USER, PASSWORD);

            BorderPane loginRoot = new BorderPane();
            Scene loginScene = new Scene(loginRoot, 300, 300);

            VBox loginForm = new VBox(10);
            loginForm.setPadding(new Insets(20));
            loginForm.setAlignment(Pos.CENTER);

            TextField usernameField = new TextField();
            PasswordField passwordField = new PasswordField();
            Button loginButton = new Button("Přihlásit");
            Button registerButton = new Button("Registrace");

            loginForm.getChildren().addAll(
                    new Label("Uživatelské jméno:"), usernameField,
                    new Label("Heslo:"), passwordField,
                    loginButton,
                    registerButton
            );

            loginRoot.setCenter(loginForm);
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Přihlášení");

            loginButton.setOnAction(event -> {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (isValidLogin(username, password)) {
                    currentUsername = username;
                    try {
                        db = new KnihaDatabase("jdbc:postgresql://" + HOST + "/" + DB_NAME, USER, PASSWORD);

                        finishedBooks = FXCollections.observableArrayList(db.getKnihyByStatusAndUser("přečtená", currentUsername));
                        readBooks = FXCollections.observableArrayList(db.getKnihyByStatusAndUser("rozečtená", currentUsername));
                        wantToReadBooks = FXCollections.observableArrayList(db.getKnihyByStatusAndUser("chci číst", currentUsername));

                        BorderPane root = new BorderPane();

                        TabPane tabPane = new TabPane();

                        Tab tabFinished = new Tab("Přečtené knihy", createTableView(finishedBooks, false));
                        tabFinished.setClosable(false);
                        Tab tabRead = new Tab("Rozečtené knihy", createTableView(readBooks, true));
                        tabRead.setClosable(false);
                        Tab tabWantToRead = new Tab("Chci číst", createTableView(wantToReadBooks, false));
                        tabWantToRead.setClosable(false);

                        // Prázdný Tab pro mezery
                        Tab emptyTab = new Tab("");
                        emptyTab.setDisable(true);
                        emptyTab.getStyleClass().add("empty-tab");

                        Tab tabAddBook = new Tab(" + ");
                        tabAddBook.setClosable(false);
                        tabAddBook.getStyleClass().add("tab-add-book");

                        Tab exportXmlTab = new Tab("Převést do XML");
                        exportXmlTab.setClosable(false);

                        tabPane.getTabs().addAll(tabFinished, tabRead, tabWantToRead, emptyTab, tabAddBook, exportXmlTab);

                        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                            if (newTab == tabAddBook) {
                                tabPane.getSelectionModel().select(oldTab); // Zpět na starý tab
                                AddKnihaDialog addKnihaDialog = new AddKnihaDialog(db, finishedBooks, readBooks, wantToReadBooks, username);
                                addKnihaDialog.show();
                            }
                            if (newTab == exportXmlTab) {
                                exportBooksToXML();
                                tabPane.getSelectionModel().select(oldTab); // Zpět na starý tab
                            }
                        });

                        HBox hbox = new HBox(tabPane);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(tabPane, Priority.ALWAYS);

                        root.setCenter(hbox);

                        Scene scene = new Scene(root);

                        // Center the login window on the screen
                        primaryStage.centerOnScreen();

                        // Set the size of the login window to 75% of the screen
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        primaryStage.setWidth(screenBounds.getWidth() * 0.75);
                        primaryStage.setHeight(screenBounds.getHeight() * 0.75);

                        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                        primaryStage.setScene(scene);
                        primaryStage.setTitle("Sledování knih");

                        primaryStage.centerOnScreen();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("An error occurred during application startup: " + e.getMessage());
                    }
                } else {
                    showAlert("Chyba přihlášení", "Nesprávné uživatelské jméno nebo heslo.");
                }
            });

            registerButton.setOnAction(event -> showRegisterDialog());

            primaryStage.show();


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred during application startup: " + e.getMessage());
        }
    }

    private void showRegisterDialog() {
        Stage registerStage = new Stage();
        registerStage.setTitle("Registrace");

        VBox registerForm = new VBox(10);
        registerForm.setPadding(new Insets(20));
        registerForm.setAlignment(Pos.CENTER);

        TextField newUsernameField = new TextField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button registerButton = new Button("Registrovat");

        registerForm.getChildren().addAll(
                new Label("Nové uživatelské jméno:"), newUsernameField,
                new Label("Nové heslo:"), newPasswordField,
                new Label("Potvrdit heslo:"), confirmPasswordField,
                errorLabel,
                registerButton
        );

        registerButton.setOnAction(event -> {
            String username = newUsernameField.getText();
            String password = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Hesla se neshodují.");
                return;
            }

            try {
                userDb.registerUser(username, password);
                registerStage.close();
                showSuccessAlert("Registrace úspěšná", "Uživatel byl úspěšně zaregistrován.");
            } catch (SQLException e) {
                errorLabel.setText("Registrace selhala: " + e.getMessage());
            }
        });

        Scene registerScene = new Scene(registerForm, 350, 300);
        registerStage.setScene(registerScene);
        registerStage.show();
    }

    private TableView<Kniha> createTableView(ObservableList<Kniha> knihyList, boolean includePageColumn) {
        TableView<Kniha> tableView = new TableView<>(knihyList);
        tableView.getStyleClass().add("table-view");

        TableColumn<Kniha, String> nameCol = new TableColumn<>("Název knihy");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNazev()));
        nameCol.getStyleClass().add("col-name");

        TableColumn<Kniha, String> authorCol = new TableColumn<>("Autor");
        authorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAutor()));
        authorCol.getStyleClass().add("col-author");

        TableColumn<Kniha, Number> pageCol;
        if (includePageColumn) {
            pageCol = new TableColumn<>("Strana");
            pageCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStrana()));
            pageCol.getStyleClass().add("col-page");
        } else {
            pageCol = null;
        }

        TableColumn<Kniha, String> descCol = new TableColumn<>("Popis");
        descCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPopis()));
        descCol.getStyleClass().add("col-desc");

        if (includePageColumn) {
            tableView.getColumns().addAll(nameCol, authorCol, pageCol, descCol);
        } else {
            tableView.getColumns().addAll(nameCol, authorCol, descCol);
        }

        // Nastavení dynamické šířky pro descCol
        tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
            double remainingWidth = newVal.doubleValue()
                    - nameCol.getWidth()
                    - authorCol.getWidth()
                    - (includePageColumn ? pageCol.getWidth() : 0);
            descCol.setPrefWidth(remainingWidth);
        });

        tableView.setRowFactory(tv -> {
            TableRow<Kniha> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Kniha rowData = row.getItem();
                    DetailKnihaDialog detailKnihaDialog = new DetailKnihaDialog(rowData, db, tableView, finishedBooks, readBooks, wantToReadBooks);
                    detailKnihaDialog.show();
                }
            });
            return row;
        });

        return tableView;
    }

    private boolean isValidLogin(String username, String password) {
        try {
            return userDb.authenticateUser(username, password);
        } catch (SQLException e) {
            showAlert("Chyba", "Nastala chyba při autentizaci: " + e.getMessage());
            return false;
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        //dialogPane.getStylesheets().add(getClass().getResource("success-alert.css").toExternalForm());
        dialogPane.getStyleClass().add("success-alert");

        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportBooksToXML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Uložit seznam knih jako XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                // Vytvoření kořenového elementu <books>
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("books");
                doc.appendChild(rootElement);

                // Přidání knih jako elementy <book>
                addBooksToXML(doc, rootElement, finishedBooks, "finished");
                addBooksToXML(doc, rootElement, readBooks, "reading");
                addBooksToXML(doc, rootElement, wantToReadBooks, "want_to_read");

                // Uložení do XML souboru
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);

                transformer.transform(source, result);

                showAlert("Export do XML", "Seznam knih byl úspěšně uložen do XML souboru.");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Chyba při exportu do XML", "Nastala chyba při ukládání seznamu knih do XML souboru: " + e.getMessage());
            }
        }
    }

    private void addBooksToXML(Document doc, Element rootElement, ObservableList<Kniha> knihyList, String category) {
        if (knihyList == null) return;

        for (Kniha kniha : knihyList) {
            Element bookElement = doc.createElement("book");
            rootElement.appendChild(bookElement);

            // Přidání atributu kategorie
            bookElement.setAttribute("category", category);

            // Přidání elementů pro informace o knize
            addChildElement(doc, bookElement, "nazev", kniha.getNazev());
            addChildElement(doc, bookElement, "autor", kniha.getAutor());
            addChildElement(doc, bookElement, "strana", String.valueOf(kniha.getStrana()));
            addChildElement(doc, bookElement, "popis", kniha.getPopis());
        }
    }

    private void addChildElement(Document doc, Element parentElement, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parentElement.appendChild(element);
    }

    public static void main(String[] args) {
        launch(args);
    }
}