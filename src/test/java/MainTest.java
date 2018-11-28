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

    @Test
    public void succeededToParseDate() {
        final Util util = Mockito.spy(new Util());
        long testTime = util.convertDateToLong("11/28/2018-02:32:02");
        Assert.assertEquals(testTime, 1543390322000L);
    }


}