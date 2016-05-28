package io.github.sdsstudios.ScoreKeeper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Seth Schroeder on 28/05/2016.
 */

public class DateHelper {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
    SimpleDateFormat weekFormat = new SimpleDateFormat("d MMM");//dd/MM/yyyy
    SimpleDateFormat yearFormat = new SimpleDateFormat("d MMM yyyy");//dd/MM/yyyy
    Calendar currentDate = Calendar.getInstance();
    private String dateStr = null;
    private Date theDate;

    public String gameDate(String dateArray){

        try {
            theDate = simpleDateFormat.parse(dateArray);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar myCal = new GregorianCalendar();
        myCal.setTime(theDate);

        boolean day = currentDate.get(Calendar.DAY_OF_MONTH) - myCal.get(Calendar.DAY_OF_MONTH) == 0
                && currentDate.get(Calendar.MONTH) == myCal.get(Calendar.MONTH)
                && currentDate.get(Calendar.YEAR) == myCal.get(Calendar.YEAR);

        boolean year = currentDate.get(Calendar.YEAR) - myCal.get(Calendar.YEAR) != 0;

        if (day){
            dateStr = timeFormat.format(myCal.getTime());
        }else if(year){
            dateStr = yearFormat.format(myCal.getTime());

        }else{
            dateStr = weekFormat.format(myCal.getTime());
        }
        return dateStr;
    }


}
