import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class UtilTest {


    @Test
    public void succeededToParseDate() {
        final Util util = Mockito.spy(new Util());
        long testTime = util.convertDateToLong("11/28/2018-02:32:02");
        Assert.assertEquals(testTime, 1543390322000L);
    }

    @Test
    public void succeededToConvertLongToDate() {
        final Util util = Mockito.spy(new Util());
        String testTime = util.convertLongToDate(1543390322000L);
        Assert.assertEquals(testTime, "11/28/2018-02:32:02");
    }
}

