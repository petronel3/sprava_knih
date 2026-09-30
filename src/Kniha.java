import javafx.beans.property.*;

public class Kniha {

    private IntegerProperty id;
    private StringProperty nazev;
    private StringProperty autor;
    private StringProperty status;
    private IntegerProperty strana;
    private StringProperty popis;
    private StringProperty poznamky;
    private StringProperty uzivatel;


    public Kniha(int id, String nazev, String autor, String status, int strana, String popis, String poznamky, String uzivatel) {
        this.id = new SimpleIntegerProperty(id);
        this.nazev = new SimpleStringProperty(nazev);
        this.autor = new SimpleStringProperty(autor);
        this.status = new SimpleStringProperty(status);
        this.strana = new SimpleIntegerProperty(strana);
        this.popis = new SimpleStringProperty(popis);
        this.poznamky = new SimpleStringProperty(poznamky);
        this.uzivatel = new SimpleStringProperty(uzivatel);
    }

    // Gettery a settery

    public String getUzivatel() {
        return uzivatel.get();
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNazev() {
        return nazev.get();
    }

    public StringProperty nazevProperty() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev.set(nazev);
    }

    public String getAutor() {
        return autor.get();
    }

    public StringProperty autorProperty() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor.set(autor);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public int getStrana() {
        return strana.get();
    }

    public IntegerProperty stranaProperty() {
        return strana;
    }

    public void setStrana(int strana) {
        this.strana.set(strana);
    }

    public String getPopis() {
        return popis.get();
    }

    public StringProperty popisProperty() {
        return popis;
    }

    public void setPopis(String popis) {
        this.popis.set(popis);
    }

    public String getPoznamky() {
        return poznamky.get();
    }

    public StringProperty poznamkyProperty() {
        return poznamky;
    }

    public void setPoznamky(String poznamky) {
        this.poznamky.set(poznamky);
    }
}