import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KnihaDatabase {
    private Connection conn;

    public KnihaDatabase(String url, String user, String password) throws SQLException {
        try {
            System.out.println("Attempting to connect to the database...");
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection established.");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database: " + e.getMessage());
            throw e;
        }
    }

    private void createTables() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS knihy ("
                + "id SERIAL PRIMARY KEY, "
                + "nazev VARCHAR(255) NOT NULL, "
                + "autor VARCHAR(255) NOT NULL, "
                + "popis TEXT, "
                + "poznamky TEXT, "
                + "status VARCHAR(50) NOT NULL, "
                + "strana INTEGER, "
                + "uzivatel VARCHAR(255) NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            System.out.println("Creating tables if they do not exist...");
            stmt.execute(createTableSQL);
            System.out.println("Tables created or already exist.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to create tables: " + e.getMessage());
            throw e;
        }
    }

    public void addKniha(Kniha kniha, String uzivatel) throws SQLException {
        String insertSQL = "INSERT INTO knihy (nazev, autor, popis, poznamky, status, strana, uzivatel) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, kniha.getNazev());
            pstmt.setString(2, kniha.getAutor());
            pstmt.setString(3, kniha.getPopis());
            pstmt.setString(4, kniha.getPoznamky());
            pstmt.setString(5, kniha.getStatus());
            pstmt.setInt(6, kniha.getStrana());
            pstmt.setString(7, uzivatel);
            pstmt.executeUpdate();
            System.out.println("Book added successfully: " + kniha.getNazev());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to add book: " + e.getMessage());
            throw e;
        }
    }

    public List<Kniha> getKnihyByStatusAndUser(String status, String uzivatel) throws SQLException {
        List<Kniha> knihy = new ArrayList<>();
        String selectSQL = "SELECT * FROM knihy WHERE status = ? AND uzivatel = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, status);
            pstmt.setString(2, uzivatel);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Kniha kniha = new Kniha(
                            rs.getInt("id"),
                            rs.getString("nazev"),
                            rs.getString("autor"),
                            rs.getString("status"),
                            rs.getInt("strana"),
                            rs.getString("popis"),
                            rs.getString("poznamky"),
                            rs.getString("uzivatel")
                    );
                    knihy.add(kniha);
                }
                System.out.println("Books fetched successfully for status: " + status + " and user: " + uzivatel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to fetch books for status " + status + " and user " + uzivatel + ": " + e.getMessage());
            throw e;
        }
        return knihy;
    }


    public void updateKniha(Kniha kniha) throws SQLException {
        String updateSQL = "UPDATE knihy SET nazev = ?, autor = ?, popis = ?, poznamky = ?, status = ?, strana = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, kniha.getNazev());
            pstmt.setString(2, kniha.getAutor());
            pstmt.setString(3, kniha.getPopis());
            pstmt.setString(4, kniha.getPoznamky());
            pstmt.setString(5, kniha.getStatus());
            pstmt.setInt(6, kniha.getStrana());
            pstmt.setInt(7, kniha.getId());
            pstmt.executeUpdate();
            System.out.println("Book updated successfully: " + kniha.getNazev());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to update book: " + e.getMessage());
            throw e;
        }
    }

    public void deleteKniha(Kniha kniha) throws SQLException {
        String deleteSQL = "DELETE FROM knihy WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, kniha.getId());
            pstmt.executeUpdate();
            System.out.println("Book deleted successfully. ID: " + kniha.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to delete book: " + e.getMessage());
            throw e;
        }
    }
}