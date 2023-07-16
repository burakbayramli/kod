package swisseph;

/*
  This is a port of the Swiss Ephemeris Free Edition, Version 1.64.01
  of Astrodienst AG, Switzerland from the original C Code to Java. For
  copyright see the original copyright notices below and additional
  copyright notes in the file named LICENSE, or - if this file is not
  available - the copyright notes at http://www.astro.ch/swisseph/ and
  following.
   
  For any questions or comments regarding this port to Java, you should
  ONLY contact me and not Astrodienst, as the Astrodienst AG is not involved
  in this port in any way.

  Thomas Mack, mack@ifis.cs.tu-bs.de, 23rd of April 2001

*/
/* Copyright (C) 1997 - 2000 Astrodienst AG, Switzerland.
   All rights reserved.

   This file is part of Swiss Ephemeris Free Edition.

   Swiss Ephemeris is distributed with NO WARRANTY OF ANY KIND.  No author
   or distributor accepts any responsibility for the consequences of using it,
   or for whether it serves any particular purpose or works at all, unless he
   or she says so in writing.  Refer to the Swiss Ephemeris Public License
   ("SEPL" or the "License") for full details.

   Every copy of Swiss Ephemeris must include a copy of the License,
   normally in a plain ASCII text file named LICENSE.  The License grants you
   the right to copy, modify and redistribute Swiss Ephemeris, but only
   under certain conditions described in the License.  Among other things, the
   License requires that the copyright notices and this notice be preserved on
   all copies.

   For uses of the Swiss Ephemeris which do not fall under the definitions
   laid down in the Public License, the Swiss Ephemeris Professional Edition
   must be purchased by the developer before he/she distributes any of his
   software or makes available any product or service built upon the use of
   the Swiss Ephemeris.

   Authors of the Swiss Ephemeris: Dieter Koch and Alois Treindl

   The authors of Swiss Ephemeris have no control or influence over any of
   the derived works, i.e. over software or services created by other
   programmers which use Swiss Ephemeris functions.

   The names of the authors or of the copyright holder (Astrodienst) must not
   be used for promoting any software, product or service which uses or contains
   the Swiss Ephemeris. This copyright notice is the ONLY place where the
   names of the authors can legally appear, except in cases where they have
   given special permission in writing.

   The trademarks 'Swiss Ephemeris' and 'Swiss Ephemeris inside' may be used
   for promoting such software, products or services.
*/
import swisseph.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * A class to test the astrological house calculations.<p>
 */
public class Swehtest {

    static final String zod_nam[] = new String[]{
        "ar","ta","ge","cn","le","vi",
        "li","sc","sa","cp","aq","pi"
    };

    static RandomAccessFile fp=null;

    SwissData swed=new SwissData();
    SwissLib sl=new SwissLib();
    SwissEph sw=new SwissEph();
    CFmt f=new CFmt();

    public static void main(String[] argv) {
        Swehtest swt=new Swehtest();
        swt.main_start(argv);
    }
  
    private int main_start(String[] argv) {
        String s;
        String hsy="", slat="", ssidt="";
        double cusp[]=new double[1+12+3];  /* empty + 12 houses */
        double ascmc[]=new double[10];     /* asc, mc, armc, vertex ... */
        double armc, lat, sidt;
        double eps = 23.45;
        int i;
        while (true) {
            do_printf("lat., sid.time, house system (dd.fraction hh.fraction character):");
            //      gets(s);
            try {
                InputStreamReader in=new InputStreamReader(System.in);
                BufferedReader bin=new BufferedReader(in);
                s=bin.readLine();
                
            } catch (IOException ie) {
                DevNull.println(ie.getMessage());
                break;
            }
            char ch=s.charAt(0);
            if (ch == '.' || ch == '\0' || ch == 'q' || ch == 'e')
                return SweConst.OK;
            else {
                //        if (sscanf(s, "%s %s %s",slat,ssidt,hsy) == EOF)
                StringTokenizer tk=new StringTokenizer(s);
                if (tk.countTokens()<3) {
                    System.exit(0);/**/
                } else {
                    slat=tk.nextToken().trim();
                    ssidt=tk.nextToken().trim();
                    hsy=tk.nextToken().trim();
                }
            }
            //      lat = atof(slat);
            try {
                lat=Double.valueOf(slat).doubleValue();
            } catch (NumberFormatException nf) {
                lat=0.;
            }
            //      sidt = atof(ssidt);
            try {
                sidt=Double.valueOf(ssidt).doubleValue();
            } catch (NumberFormatException nf) {
                sidt=0.;
            }
            armc = sidt * 15;
            s="latitude= "+f.fmt("%f",lat)+", sid. time= "+
                f.fmt("%f",sidt)+", hsy="+hsy.charAt(0)+" (eps = 23"+swed.ODEGREE_CHAR+"27')\n";
            do_printf(s);
            sw.swe_houses_armc(armc, lat, eps, (int) hsy.charAt(0), cusp, ascmc);
            /* to compute houses directly from a date and geogr. position,
             * you can call
             * swe_houses(tjd_ut, lat, lon, (int) hsy[0], cusp, ascmc);
             */
            for (i = 1; i <= 12; i++)  {
                s="house "+f.fmt("%2d",i)+"  "+dms(cusp[i], 0)+"\n";
                do_printf(s);
            }
            s="AC        "+dms(ascmc[0], 0)+"\n";
            do_printf(s);
            s="MC        "+dms(ascmc[1], 0)+"\n";
            do_printf(s);
            s="Vert.     "+dms(ascmc[3], 0)+"\n";
            do_printf(s);
        }
        //    end_program:
        return SweConst.OK;
    }

    void do_printf(String info) {
      //DevNull.print(info);
    }

    static final int BIT_ROUND_SEC  =1;
    static final int BIT_ROUND_MIN  =2;
    static final int BIT_ZODIAC     =4;
    String dms(double x, long iflag) { 
        int izod;
        long k, kdeg, kmin, ksec;
        String c = swed.ODEGREE_CHAR;
        //    char *sp, s1[50];
        String s1;
        //    static char s[50];    
        String s;
        int sgn;
        s = "";
        if ((iflag & SweConst.SEFLG_EQUATORIAL)!=0)
            c = "h";
        if (x < 0) {
            x = -x; 
            sgn = -1;
        } else
            sgn = 1;
        if ((iflag & BIT_ROUND_MIN)!=0)
            x += sl.swe_degnorm(x + 0.5/60);
        if ((iflag & BIT_ROUND_SEC)!=0)
            x += 0.5/3600;
        if ((iflag & BIT_ZODIAC)!=0) {
            izod = (int) (x / 30); 
            x = x%30.;
            kdeg = (long) x;
            s=f.fmt("%2ld",kdeg)+" "+zod_nam[izod]+" ";
        } else {
            kdeg = (long) x; 
            s=" "+f.fmt("%3ld", kdeg)+c;
        } 
        x -= kdeg;
        x *= 60;
        kmin = (long) x;
        if ((iflag & BIT_ZODIAC)!=0 && (iflag & BIT_ROUND_MIN)!=0)
            s1=f.fmt("%2ld", kmin);
        else
            s1=f.fmt("%2ld", kmin)+"'";
        s+=s1;
        if ((iflag & BIT_ROUND_MIN)!=0)
            //      goto return_dms;
            return return_dms(sgn,s);
        x -= kmin;
        x *= 60;
        ksec = (long) x;
        if ((iflag & BIT_ROUND_SEC)!=0)
            s1=f.fmt("%2ld", ksec)+"\"";
        else
            s1=f.fmt("%2ld", ksec);
        s+=s1;
        if ((iflag & BIT_ROUND_SEC)!=0)
            //      goto return_dms;
            return return_dms(sgn,s);
        x -= ksec;
        k = (long) (x * 10000);
        s1="."+f.fmt("%04ld", k);
        s+=s1;
        return return_dms(sgn,s);
        //    return_dms:;
        //    if (sgn < 0) {
        //      sp = strpbrk(s, "0123456789");
        //      *(sp-1) = '-';
        //    }
        //    return(s);
    }

    private String return_dms(int sgn, String s) {
        if (sgn < 0) {
            for (int i=0; i<s.length();i++) {
                if (Character.isDigit(s.charAt(i))) {
                    s=s.substring(0,i-1)+"-"+s.substring(i);
                    break;
                }
            }
        } 
        return(s);
    } 

}
