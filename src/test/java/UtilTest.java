import main.Util;
import org.junit.Assert;
import org.junit.Test;

public class UtilTest {


    @Test
    public void convertDateToLong_Successful() {
        long testTime = Util.convertDateToLong("11/28/2018-02:32:02");
        Assert.assertEquals(testTime, 1543390322000L);
    }

    @Test
    public void convertLongToDate_Successful() {
        String testTime = Util.convertLongToDate(1543390322000L);
        Assert.assertEquals(testTime, "11/28/2018-02:32:02");
    }
}

