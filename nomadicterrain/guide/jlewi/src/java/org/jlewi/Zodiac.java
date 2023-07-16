package org.jlewi;

import swisseph.*;
//import com.stevesoft.pat.Regex;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Zodiac  {
       
  Swetest alg = new Swetest();

  /**
   * Calculates the zodiac (taurus, aries), the <code>sign</code> for a date
   * and a planet
   *
   * @param planetNumber an <code>int</code> value
   * @param date a <code>String</code> value
   * @return an <code>int</code> value
   */
  public int sign(int planetNumber, String date) {

    String i1 = "-p" + planetNumber;
    String i2 = "-b" + date;
    String i3 = "-fZ";
    String i4 = "-head";
    String i5 = "-roundmin";

    String[] args = new String[]{i1, i2, i3, i4, i5};

    alg.main_start(args);

    String result = alg.globalResult;

    Pattern p = Pattern.compile("([a-z][a-z])");
    Matcher m = p.matcher(result);
    String sign = "";
    if (m.find()) {
      sign = m.group(1);
    }

    int signNo = 0;
    for (String z:Swetest.zod_nam) {
      if (sign.equals(z)) break;
      signNo++;
    }
                       
    return signNo;
  }

  /**
   * Calculates a decan number within a sign, the result
   * will be between 1,3.
   *
   * @param date a <code>Date</code> value
   * @return an <code>int</code> value
   */
  public int decanWithinSign(int planetNumber, String date) {
        
    String i1 = "-p" + planetNumber;
    String i2 = "-b" + date;
    String i3 = "-fZ";
    String i4 = "-head";
    String i5 = "-roundmin";

    String[] args = new String[]{i1, i2, i3, i4, i5};

    alg.main_start(args);

    String result = alg.globalResult;

    String res[] = result.split(" ");

    String token = "";
    if (res[0].equals("")) {
      token = res[1];
    } else {
      token = res[0];
    }

    int decan = (int)((new Double(token))/10.0);
        
    return decan+1;
  }

  /**
   * Describe <code>calc</code> method here.
   *
   * @param planetNumber an <code>int</code> value
   * @param date a <code>String</code> value
   * @return an <code>int</code> value
   */
  public int decan(int planetNumber, String date) {
    int sign = sign(planetNumber, date);
    int decan = decanWithinSign(planetNumber, date);
    return (sign * 3) + decan;
  }

  /**
   * Return decan information for all planets
   *
   * @param date a <code>String</code> value
   * @return an <code>int[]</code> value
   */
  public int[] decans(String date) {
    int d[] = new int[10];
    for (int i=0;i<10;i++) {
      d[i] = decan(i, date);
    }
    return d;        
  }
    
}
