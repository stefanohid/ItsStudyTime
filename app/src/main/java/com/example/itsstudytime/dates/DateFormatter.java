package com.example.itsstudytime.dates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    public static String formatDate(String date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int year = Integer.parseInt(date.substring(0,4));
            int month = Integer.parseInt(date.substring(5,7));
            int day = Integer.parseInt(date.substring(8,10));
            LocalDate ld = LocalDate.of(year, month, day);

            DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH);
            DateTimeFormatter formatIt = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ITALIAN);
            if (Locale.getDefault().getLanguage().contentEquals("en")) {
                return format.format(ld);
            } else {
                return formatIt.format(ld);
            }
        }
       return null;
    }

    public static String formatDate(LocalDate date) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH);
            DateTimeFormatter formatIt = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ITALIAN);
            if (Locale.getDefault().getLanguage().contentEquals("en")) {
                return format.format(date);
            } else {
                return formatIt.format(date);
            }
        }
        return null;
    }

    public static Date fromDateToString(String date) {
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7));
        int day = Integer.parseInt(date.substring(8,10));
        Date d = new Date(year, month, day);
        return d;
    }
}
