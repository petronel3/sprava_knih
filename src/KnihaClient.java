import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.List;

public class KnihaClient {
    private KnihaServerInterface server;
    private String user;

    public KnihaClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            server = (KnihaServerInterface) registry.lookup("KnihaServer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean authenticateUser(String username, String password) {
        try {
            return server.authenticateUser(username, password);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void registerUser(String username, String password) {
        try {
            server.registerUser(username, password);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Kniha> getKnihyByStatusAndUser(String status, String username) {
        try {
            return server.getKnihyByStatusAndUser(status, username);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addKniha(Kniha kniha, String username) {
        try {
            server.addKniha(kniha, username);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateKniha(Kniha kniha) {
        try {
            server.updateKniha(kniha);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteKniha(Kniha kniha) {
        try {
            server.deleteKniha(kniha);
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        try {
            return server.getId();
        } catch (RemoteException | SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
