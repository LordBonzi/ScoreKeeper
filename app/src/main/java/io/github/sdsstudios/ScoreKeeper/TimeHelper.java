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

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat weekFormat = new SimpleDateFormat("d MMM");
    private SimpleDateFormat yearFormat = new SimpleDateFormat("d MMM yyyy");
    private Calendar currentDate = Calendar.getInstance();
    private Date theDate;

    public String gameDate(String dateArray) {

        try {
            theDate = simpleDateFormat.parse(dateArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar myCal = new GregorianCalendar();
        myCal.setTime(theDate);

        boolean day = currentDate.get(Calendar.DAY_OF_MONTH) - myCal.get(Calendar.DAY_OF_MONTH) == 0
                && currentDate.get(Calendar.MONTH) == myCal.get(Calendar.MONTH)
                && currentDate.get(Calendar.YEAR) == myCal.get(Calendar.YEAR);

        boolean year = currentDate.get(Calendar.YEAR) - myCal.get(Calendar.YEAR) != 0;

        String dateStr = null;
        if (day){
            dateStr = timeFormat.format(myCal.getTime());

        }else if(year){
            dateStr = yearFormat.format(myCal.getTime());

        }else{
            dateStr = weekFormat.format(myCal.getTime());
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
