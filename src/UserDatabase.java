import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class UserDatabase {
    private Connection conn;

    public UserDatabase(String url, String user, String password) throws SQLException {
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
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id SERIAL PRIMARY KEY, "
                + "username VARCHAR(255) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "salt VARCHAR(255) NOT NULL)";
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

    public void registerUser(String username, String password) throws SQLException {
        // Generate salt
        String salt = generateSalt();

        // Hash password with salt
        String passwordHash = hashPassword(password, salt);

        // Insert user into database
        String insertSQL = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            System.out.println("User registered successfully: " + username);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to register user: " + e.getMessage());
            throw e;
        }
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        String selectSQL = "SELECT password_hash, salt FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    String inputPasswordHash = hashPassword(password, salt);
                    return passwordHash.equals(inputPasswordHash);
                } else {
                    System.out.println("User not found: " + username);
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to authenticate user: " + e.getMessage());
            throw e;
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to hash password: " + e.getMessage());
        }
    }
}