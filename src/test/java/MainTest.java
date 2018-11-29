import main.Main;
import org.junit.Test;

public class MainTest {

    @Test(expected = Exception.class)
    public void mainTest() throws Exception {
        Main.databaseUrl = null;
        Main.main(null);
    }

}