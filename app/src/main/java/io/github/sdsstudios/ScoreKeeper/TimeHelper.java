package io.github.sdsstudios.ScoreKeeper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Seth Schroeder on 28/05/2016.
 */

public class TimeHelper {

    private static SimpleDateFormat mGameDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat mWeekFormat = new SimpleDateFormat("d MMM");
    private static SimpleDateFormat mYearFormat = new SimpleDateFormat("d MMM yyyy");
    private static Calendar mCurrentDate = Calendar.getInstance();
    private static Date mDate;

    public static String gameDate(String dateArray) {

        try {
            mDate = mGameDateFormat.parse(dateArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar myCal = new GregorianCalendar();
        myCal.setTime(mDate);

        boolean day = mCurrentDate.get(Calendar.DAY_OF_MONTH) - myCal.get(Calendar.DAY_OF_MONTH) == 0
                && mCurrentDate.get(Calendar.MONTH) == myCal.get(Calendar.MONTH)
                && mCurrentDate.get(Calendar.YEAR) == myCal.get(Calendar.YEAR);

        boolean year = mCurrentDate.get(Calendar.YEAR) - myCal.get(Calendar.YEAR) != 0;

        String dateStr = null;
        if (day){
            dateStr = mTimeFormat.format(myCal.getTime());

        }else if(year){
            dateStr = mYearFormat.format(myCal.getTime());

        }else{
            dateStr = mWeekFormat.format(myCal.getTime());
        }
        return dateStr;
    }

    public Long convertToLong(String time) throws ParseException {
        Long timeLong = 0L;

        DateFormat f = new SimpleDateFormat("hh:mm:ss:S");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date d = f.parse(time);
        timeLong = d.getTime();

        if (!TimeZone.getTimeZone("GMT").inDaylightTime(d)){
            timeLong = timeLong - 3600000;
        }

        return timeLong;
    }


}
