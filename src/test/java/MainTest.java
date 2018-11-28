import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;

import static org.mockito.BDDMockito.given;

public class MainTest {

    @Test(expected = Exception.class)
    public void main() throws Exception {
        Main.databaseUrl = null;
        Main.main(null);
    }

}