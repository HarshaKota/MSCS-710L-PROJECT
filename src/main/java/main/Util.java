package main;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Util {

    // Convert Long milliseconds to date
    public static String convertLongToDate(Long longTimestamp) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        return df.format(longTimestamp);
    }

    // Convert Date to Long milliseconds
    public static Long convertDateToLong(String date) {
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
