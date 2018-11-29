package main;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class provides common methods that will be used by the other classes
 * to perform calculations/conversions
 */
public class Util {

    /**
     * Convert Long milliseconds to date
     *
     * @param longTimestamp Milliseconds time as a Long
     * @return A date in string format
     */
    public static String convertLongToDate(Long longTimestamp) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        return df.format(longTimestamp);
    }


    /**
     * Convert Date to Long milliseconds
     *
     * @param date A date in a string format
     * @return Data value in milliseconds in long format
     */
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
