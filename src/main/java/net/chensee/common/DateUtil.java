package net.chensee.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date getNYRSFMDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(date);
        Date date1 = null;
        try {
            date1 = sdf.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static String getNYRSFMDateStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String getNYRDateStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static Date getNYRDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(date);
        Date date1 = null;
        try {
            date1 = sdf.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static Date getNYRDateByStr(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        try {
            date1 = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static Date getNYRSFMDateByStr(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = null;
        try {
            date1 = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static Date setDate(Date date,int value){
        return new Date(date.getTime() + value * 1000);
    }

    public static Date setDateDay(Date date,int value){
        Calendar calc =Calendar.getInstance();
        calc.setTime(date);
        calc.add(Calendar.DATE, value);
        return calc.getTime();
    }

    public static Date setDateSecond(Date date,int value){
        Calendar calc =Calendar.getInstance();
        calc.setTime(date);
        calc.add(Calendar.SECOND, value);
        return calc.getTime();
    }

    public static Date setDateHour(Date date,int value){
        Calendar calc =Calendar.getInstance();
        calc.setTime(date);
        calc.add(Calendar.HOUR, value);
        return calc.getTime();
    }

    public static String getSFMDateStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }

}
