import main.Database;
import org.junit.Test;

public class A_DatabaseTest {

    @Test(expected = Exception.class)
    public void closeDatabaseConnection_NoConnection() throws Exception {
        Database dbObj = new Database();
        dbObj.closeDatabaseConnection();
    }
}
