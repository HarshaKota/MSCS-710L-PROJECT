import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    static String convertLongToDate(Long longTimestamp) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        return df.format(longTimestamp);
    }

    static Long convertDateToLong(String date) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        Long time = null;
        try {
            time = df.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
