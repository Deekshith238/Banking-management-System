import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBConnection Wrapper Class
 * Located at root of src/ for project structure compatibility.
 * Delegates to dao.DBConnection.
 */
public class DBConnection {

    public static Connection getConnection() throws SQLException {
        return dao.DBConnection.getConnection();
    }
}
