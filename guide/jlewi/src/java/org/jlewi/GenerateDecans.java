package org.jlewi;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.apache.commons.lang.time.FastDateFormat;

public class GenerateDecans
{
    public static long addDays(long time, int amount) {
	Calendar calendar = Calendar.getInstance();
	synchronized(calendar) {
	    calendar.setTimeInMillis(time);
	    calendar.add(Calendar.DAY_OF_MONTH, amount);
	    return calendar.getTimeInMillis();
	}
    }
    
    public static Date nextDay(Date date) {
	return new Date(addDays(date.getTime(), 1));
    }
  
    public static String twoDigitize(Integer i) {
	String s = i.toString();    
	if (s.length() == 1) return "0" + s;
	return s;
    }
    
    public static Date stringToDateddMM(String str) {
    
	SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	Date date = null;
	try {
	    fmt.setLenient(false);  
	    date = fmt.parse(str); 
	} catch (java.text.ParseException e) { 
	    return null;
	}
  
	return date;
    }  
    
    public static final void main(final String[] args) throws Exception {
	
	Date date = stringToDateddMM("01/01/1800");
	Date end = stringToDateddMM("01/01/2100");

	BufferedWriter wd = new BufferedWriter(new FileWriter("/tmp/decans.dat"));

	Zodiac zodiac = new Zodiac();
    
	while (!date.after(end)) {
	    try {
        
		String str = "";
        
		int month = date.getMonth() ;
		int year = date.getYear()+1900;
		int day = date.getDate();                
		wd.write("" + year + twoDigitize(month+1) + twoDigitize(day) + " ");  
		String sDate = twoDigitize(day)+"."+twoDigitize(month+1)+"."+year;		
		int ds[] = zodiac.decans(sDate);
		for (int j = 0; j < 10; j++) {
		    wd.write(""+ds[j]);
		    wd.write(":");
		}
		wd.write("\n");        
		wd.flush();		
        
		date = GenerateDecans.nextDay(date);
        
		
	    } catch (Exception e) {
		
	    }		
	}
    }
}