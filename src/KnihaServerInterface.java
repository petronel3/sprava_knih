import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

public interface KnihaServerInterface extends Remote {
    boolean authenticateUser(String username, String password) throws RemoteException, SQLException;
    void registerUser(String username, String password) throws RemoteException, SQLException;
    List<Kniha> getKnihyByStatusAndUser(String status, String username) throws RemoteException, SQLException;
    void addKniha(Kniha kniha, String uzivatel) throws RemoteException, SQLException;
    void updateKniha(Kniha kniha) throws RemoteException, SQLException;
    void deleteKniha(Kniha kniha) throws RemoteException, SQLException;
    int getId() throws RemoteException, SQLException;
}
