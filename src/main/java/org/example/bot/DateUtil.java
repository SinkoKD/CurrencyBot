package org.example.bot;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static Date addMinutes(Date date, int days) {
        System.out.printf("In the DateUtil");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, days);
        System.out.printf("Finish in the DateUtil");
        return cal.getTime();
    }
}