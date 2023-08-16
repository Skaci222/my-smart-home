package com.myproject.room;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    static DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    @TypeConverter
    public static Date toDate(Long value) throws ParseException {
       return value == null ? null: new Date(value);
        /*if (value != null){
            try {
                dateFormat.parse(value);
            }catch(ParseException e){
                e.printStackTrace();
            }
        }
        return null;*/
    }

    @TypeConverter
    public static Long DateToTimeStamp(Date date){
       return date == null ? null :date.getTime();
       //return date == null ? null : dateFormat.format(date);
    }
}
