import org.junit.Test;

public class MainTest {

    @Test(expected = Exception.class)
    public void main() throws Exception {
        Main.databaseUrl = null;
        Main.main(null);
    }

}