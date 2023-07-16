/*
  This is a port of the Swiss Ephemeris Free Edition, Version 1.70.02
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
package swisseph;

import java.util.*;
import java.net.*;
import java.io.*;

/**
 * This class is the basic class for planetary calculations.<p>
 * One important note: in all this package, negative longitudes are considered
 * to be <b>west</b> of Greenwich, positive longitudes are seen as <b>east</b>
 * of Greenwich. America seems to often use a different notation!<p>
 * <I><B>You will find the complete documentation for the original
 * SwissEphemeris package at <A HREF="http://www.astro.ch/swisseph/sweph_g.htm">
 * http://www.astro.ch/swisseph/sweph_g.htm</A>. By far most of the information 
 * there is directly valid for this port to Java as well.</B></I>
 */
public class SwissEph {

    SwissData swed;
    SwephMosh smosh;
    SwephJPL sj;
    SwissLib sl;
    Swecl sc=null;
    Swemmoon sm;
    SweHouse sh=null;
    Extensions ext=null;

    double lastLat=0.;
    double lastLong=0.;
    int lastHSys=-1;

    
    //////////////////////////////////////////////////////////////////////////////
    // Constructors: /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    /**
     * Constructs a new SwissEph object with the default search path for the
     * Swiss Ephemeris data files.
     * @see SweConst#SE_EPHE_PATH
     */
    public SwissEph() {
        this(null);
    }

    public String getTmp() {
        String tmp = "";
        if(System.getProperty("os.name").toLowerCase().indexOf("windows") > -1){
            tmp = "c:/temp";
        } else {
            tmp = "/tmp";
        }
        return tmp;
    }    

    /**
     * Constructs a new SwissEph object with the specified search path for
     * the Swiss Ephemeris data files. If you want to use this class in
     * applets, you would have to specify the path as a valid http URL on
     * the same www server from where your applet gets served.
     * @param path The search path for the Swiss Ephemeris
     * and JPL
     * data files. If null or empty, a default path will be used.
     * You will have to quote ':', ';' and '\' characters, so a
     * path like <code>&quot;C:\swiss\ephe&quot;</code> has to be written as
     * <code>&quot;C\\:\\\\swiss\\\\ephe&quot;</code>, as any '\' will be
     * evaluated twice: the first time by the Java compiler, and the second
     * time by the program itself. You can specify multiple path elements
     * separated by the (unquoted) ':' or ';' character. See swe_set_ephe_path()
     * for more information.
     * @see SweConst#SE_EPHE_PATH
     * @see SwissEph#swe_set_ephe_path(java.lang.String)
     */
    public SwissEph(String path) {
               
        swed     = new SwissData();
        sl       = new SwissLib(this.swed);
        sm       = new Swemmoon(this.swed, this.sl);
        smosh    = new SwephMosh(this.sl, this, this.swed);
        sj       = new SwephJPL(this, this.swed, this.sl);

        swed.ephe_path_is_set=false;
        swed.jpl_file_is_open=false;
        swed.fixfp=null;
        swed.ephepath=SweConst.SE_EPHE_PATH;
        swed.jplfnam=SweConst.SE_FNAME_DFT;
        swed.geopos_is_set=false;
        swed.ayana_is_set=false;
        swe_set_ephe_path(path);

    }
    //////////////////////////////////////////////////////////////////////////////
    // End of Constructors ///////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    // Public Methods: ///////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private int httpBufSize=300;

    /**
     * This sets the buffer size for access to Swiss Ephemeris or JPL data
     * files, if you specify an http-URL in swe_set_ephe_path() or via the
     * SwissEph constructor. The buffer size determines, how many bytes will
     * get read on one single HTTP request. Increased buffer size will
     * result in a reduced number of HTTP-requests, but it will increase
     * the amount of data to be transferred. As the access to the data is
     * <I>somehow</I> random, it does not make so much sense to increase the
     * size arbitrarily.<P>
     * Some test numbers for the calculation of the sun:<BR>
     * <table border="1"><tr><th>buffer<br>size</th><th>HTTP Requests<br>for the sun</th><th>HTTP Requests<br>for 9 planets</th></tr>
     * <tr><td align="right">100</td><td align="right">57</td><td align="right">69</td></tr>
     * <tr><td align="right">200</td><td align="right">30</td><td align="right">40</td></tr>
     * <tr><td align="right">300</td><td align="right">23</td><td align="right">33</td></tr>
     * <tr><td align="right">400</td><td align="right">19</td><td align="right">29</td></tr>
     * <tr><td align="right">800</td><td align="right">14</td><td align="right">24</td></tr></table>
     * @param size The size of the buffer. It defaults to 300 bytes. Values less
     * than 100 bytes will be increased to 100 bytes, as you will only increase
     * the number of requests dramatically, but the amount of bytes transferred
     * will just be minimal less.
     */
    public void setHttpBufSize(int size) {
        httpBufSize=size;
        if (size<100) {
            httpBufSize=100;
        }
        swe_close();
    }

    /* The routine called by the user.
     * It checks whether a position for the same planet, the same t, and the
     * same flag bits has already been computed.
     * If yes, this position is returned. Otherwise it is computed.
     * -> If the SEFLG_SPEED flag has been specified, the speed will be returned
     * at offset 3 of position array x[]. Its precision is probably better
     * than 0.002"/day.
     * -> If the SEFLG_SPEED3 flag has been specified, the speed will be computed
     * from three positions. This speed is less accurate than SEFLG_SPEED,
     * i.e. better than 0.1"/day. And it is much slower. It is used for
     * program tests only.
     * -> If no speed flag has been specified, no speed will be returned.
     */
    private int swe_calc_epheflag_sv = 0;

    /**
     * This is the main calculation routine for all planets, asteroids, lunar
     * nodes and apogees.
     * See <A HREF="SwissEph.html#swe_calc(double, int, int, double[], java.lang.StringBuffer)">swe_calc(...)</A> for more information.
     * @param tjd_ut The Julian Day number in UT (Universal Time).
     * @param ipl The body to be calculated. See
     * <A HREF="SweConst.html">SweConst</A> for a list of bodies
     * @param iflag A flag that contains detailed specification on how the body
     * is to be computed. See <A HREF="SweConst.html">SweConst</A>
     * for a list of valid flags (SEFLG_*).
     * @param xx A double[6] in which the result is returned. See above for more
     * details.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return iflag or SweConst.ERR (-1); iflag MAY have changed from input
     * parameter!
     */
    public int swe_calc_ut(double tjd_ut, int ipl, int iflag, double xx[],
                           StringBuffer serr) {
        return swe_calc(tjd_ut + SweDate.getDeltaT(tjd_ut), ipl, iflag, xx, serr);
    }
    /**
     * This is the main calculation routine for all planets, asteroids, lunar
     * nodes and apogees. It is equal to swe_calc_ut() with the exception that
     * the time has to be given in ET (Ephemeris Time or Dynamical Time). You
     * would get ET by adding deltaT to the UT, e.g.,
     * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
     * <P>The parameter xx is used as an output parameter containing the
     * following info:
     * <P><CODE><BLOCKQUOTE>xx[0]:&nbsp;&nbsp;&nbsp;longitude<BR>
     * xx[1]:&nbsp;&nbsp;&nbsp;latitude<BR>
     * xx[2]:&nbsp;&nbsp;&nbsp;distance in AU<BR>
     * xx[3]:&nbsp;&nbsp;&nbsp;speed in longitude (degree / day)<BR>
     * xx[4]:&nbsp;&nbsp;&nbsp;speed in latitude (degree / day)<BR>
     * xx[5]:&nbsp;&nbsp;&nbsp;speed in distance (AU / day)<BR>
     * </BLOCKQUOTE></CODE><P>
     * The speed infos will be calculated only, if the appropriate SEFLG_*
     * switch is set.
     * @param tjd The Julian Day number in ET (UT + deltaT).
     * @param ipl The body to be calculated. See
     * <A HREF="SweConst.html">SweConst</A> for a list of bodies
     * @param iflag A flag that contains detailed specification on how the body
     * is to be computed. See <A HREF="SweConst.html">SweConst</A>
     * for a list of valid flags (SEFLG_*).
     * @param xx A double[6] in which the result is returned. See above for more
     * details.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return iflag or SweConst.ERR (-1); iflag MAY have changed from input
     * parameter!
     * @see #swe_calc_ut(double, int, int, double[], java.lang.StringBuffer)
     * @see #swe_fixstar_ut(java.lang.StringBuffer, double, int, double[], java.lang.StringBuffer)
     * @see #swe_fixstar(java.lang.StringBuffer, double, int, double[], java.lang.StringBuffer)
     */
    public int swe_calc(double tjd, int ipl, int iflag, double xx[],
                        StringBuffer serr) {
        // It has been rewritten to be wrapper to the old interface without
        // exception handling like it was in C. The old routine can now be
        // found in the method _calc().
        int ret = 0;
        try {
            ret = _calc(tjd, ipl, iflag, xx, serr);
        } catch (SwissephException se) {
            ret = se.getRC();
            serr.setLength(0);
            serr.append(se.getMessage());
        }
        return ret;
    }

    // This is the new recommended interface for planetary calculations.
    // It should be rewritten to be used for fixstars as well.
    public int calc(double jdET, int ipl, int iflag, double xx[])
        throws SwissephException {
        return _calc(jdET, ipl, iflag, xx, new StringBuffer());
    }

    private int _calc(double tjd, int ipl, int iflag, double xx[],
                      StringBuffer serr) throws SwissephException {
        int i, j;
        int iflgcoor;
        int iflgsave = iflag;
        int epheflag;
        SavePositions sd;
        double x[]=new double[6], xs[];
        double x0[]=new double[24],
            x2[]=new double[24];
        double dt;

        /* if ephemeris flag != ephemeris flag of last call,
         * we clear the save area, to prevent swecalc() using
         * previously computed data for current calculation.
         * except with ipl = SE_ECL_NUT which is not dependent
         * on ephemeris, and except if change is from
         * ephemeris = 0 to ephemeris = SEFLG_DEFAULTEPH
         * or vice-versa.
         */
        epheflag = iflag & SweConst.SEFLG_EPHMASK;
        if ((epheflag & SweConst.SEFLG_DEFAULTEPH)!=0) {
            epheflag = 0;
        }
        if (swe_calc_epheflag_sv != epheflag && ipl != SweConst.SE_ECL_NUT) {
            swe_close();
            swe_calc_epheflag_sv = epheflag;
        }
        /* high precision speed prevails fast speed */
        if ((iflag & SweConst.SEFLG_SPEED3)!=0 && (iflag & SweConst.SEFLG_SPEED)!=0) {
            iflag = iflag & ~SweConst.SEFLG_SPEED3;
        }
        /* cartesian flag excludes radians flag */
        if (((iflag & SweConst.SEFLG_XYZ)!=0) &&
            ((iflag & SweConst.SEFLG_RADIANS)!=0)) {
            iflag = iflag & ~SweConst.SEFLG_RADIANS;
        }
        /*    if (iflag & SweConst.SEFLG_ICRS)
              iflag |= SweConst.SEFLG_J2000;*/
        /* pointer to save area */
        if (ipl < SweConst.SE_NPLANETS && ipl >= SweConst.SE_SUN) {
            sd = swed.savedat[ipl];
        } else {
            /* other bodies, e.g. asteroids called with ipl = SE_AST_OFFSET + MPC# */
            sd = swed.savedat[SweConst.SE_NPLANETS];
        }
        /*
         * if position is available in save area, it is returned.
         * this is the case, if tjd = tsave and iflag = iflgsave.
         * coordinate flags can be neglected, because save area
         * provides all coordinate types.
         * if ipl > SE_AST(EROID)_OFFSET, ipl must be checked,
         * because all asteroids called by MPC number share the same
         * save area.
         */
        iflgcoor = SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_XYZ |
            SweConst.SEFLG_RADIANS;

        try { // SwissephExceptions from swecalc
            if (sd.tsave != tjd || tjd == 0 || ipl != sd.ipl ||
                ((sd.iflgsave & ~iflgcoor) != (iflag & ~iflgcoor))) {
                /*
                 * otherwise, new position must be computed
                 */
                if ((iflag & SweConst.SEFLG_SPEED3) == 0) {
                    /*
                     * with high precision speed from one call of swecalc()
                     * (FAST speed)
                     */
                    sd.tsave = tjd;
                    sd.ipl = ipl;
                    if ((sd.iflgsave = swecalc(tjd, ipl, iflag, sd.xsaves, serr)) ==
                        SweConst.ERR) {
                        return swe_calc_error(xx);
                    }
                } else {
                    /*
                     * with speed from three calls of swecalc(), slower and less accurate.
                     * (SLOW speed, for test only)
                     */
                    sd.tsave = tjd;
                    sd.ipl = ipl;
                    switch(ipl) {
                        case SweConst.SE_MOON:
                            dt = SwephData.MOON_SPEED_INTV;
                            break;
                        case SweConst.SE_OSCU_APOG:
                        case SweConst.SE_TRUE_NODE:
                            /* this is the optimum dt with Moshier ephemeris, but not with
                             * JPL ephemeris or SWISSEPH. To avoid completely false speed
                             * in case that JPL is wanted but the program returns Moshier,
                             * we use Moshier optimum.
                             * For precise speed, use JPL and FAST speed computation,
                             */
                            dt = SwephData.NODE_CALC_INTV_MOSH;
                            break;
                        default:
                            dt = SwephData.PLAN_SPEED_INTV;
                            break;
                    }
                    sd.iflgsave = swecalc(tjd-dt, ipl, iflag, x0, serr);
                    if (sd.iflgsave == SweConst.ERR) {
                        return swe_calc_error(xx);
                    }
                    sd.iflgsave = swecalc(tjd+dt, ipl, iflag, x2, serr);
                    if (sd.iflgsave == SweConst.ERR) {
                        return swe_calc_error(xx);
                    }
                    sd.iflgsave = swecalc(tjd, ipl, iflag, sd.xsaves, serr);
                    if (sd.iflgsave == SweConst.ERR) {
                        return swe_calc_error(xx);
                    }
                    denormalize_positions(x0, sd.xsaves, x2);
                    calc_speed(x0, sd.xsaves, x2, dt);
                }
            }
        } catch (SwissephException se) {
            sd.iflgsave = SweConst.ERR;
            swe_calc_error(xx);
            throw se;
        }
        // end_swe_calc:
        int xsOffset=0;
        xs=sd.xsaves;
        if ((iflag & SweConst.SEFLG_EQUATORIAL) != 0) {
            xsOffset=12;        /* equatorial coordinates */
            //    } else {
            //      xsOffset=0;         /* ecliptic coordinates */
        }
        if ((iflag & SweConst.SEFLG_XYZ)!=0) {
            xsOffset+=6;         /* cartesian coordinates */
        }
        if (ipl == SweConst.SE_ECL_NUT) {
            i = 4;
        } else {
            i = 3;
        }
        for (j = 0; j < i; j++) { x[j] = xs[j+xsOffset]; }
        for (j = i; j < 6; j++) { x[j] = 0; }
        if ((iflag & (SweConst.SEFLG_SPEED3 | SweConst.SEFLG_SPEED))!=0) {
            for (j = 3; j < 6; j++) { x[j] = xs[j+xsOffset]; }
        }
        if ((iflag & SweConst.SEFLG_RADIANS)!=0) {
            if (ipl == SweConst.SE_ECL_NUT) {
                for (j = 0; j < 4; j++)
                    x[j] *= SwissData.DEGTORAD;
            } else {
                for (j = 0; j < 2; j++)
                    x[j] *= SwissData.DEGTORAD;
                if ((iflag & (SweConst.SEFLG_SPEED3 | SweConst.SEFLG_SPEED))!=0) {
                    for (j = 3; j < 5; j++)
                        x[j] *= SwissData.DEGTORAD;
                }
            }  
        } 
        for (i = 0; i <= 5; i++) {
            xx[i] = x[i];
        }
        iflag = sd.iflgsave;
        /* if no ephemeris has been specified, do not return chosen ephemeris */
        if ((iflgsave & SweConst.SEFLG_EPHMASK) == 0) {
            iflag = iflag & ~SweConst.SEFLG_DEFAULTEPH;
        }
        return iflag;
    }

    /* closes all open files, frees space of planetary data,
     * deletes memory of all computed positions
     */
    /**
     * This method closes all open data files and sets planetary data objects
     * to null for a hint to the garbage collector that those objects can be
     * freed.
     */
    public void swe_close() {
        int i;
        try {
            /* close SWISSEPH files */
            for(i=0;i<SwephData.SEI_NEPHFILES;i++) {
                swed.fidat[i].clearData();
            }
            /* free planets data space */
            for(i=0;i<SwephData.SEI_NPLANETS;i++) {
                swed.pldat[i].clearData();
            }
            for (i=0; i <= SweConst.SE_NPLANETS; i++) {/* "<=" is correct! see decl.*/
                swed.savedat[i].clearData();
            }
            /* clear node data space */
            for(i=0;i<SwephData.SEI_NNODE_ETC;i++) {
                swed.nddat[i].clearData();
            }
            swed.oec.clearData();
            swed.oec2000.clearData();
            swed.nut.clearData();
            swed.nut2000.clearData();
            swed.nutv.clearData();
            /* close JPL file */
            sj.swi_close_jpl_file();
            swed.jpl_file_is_open=false;
            /* close fixed stars */
            if (swed.fixfp!=null) {
                swed.fixfp.close();
                swed.fixfp=null;
            }
        } catch (java.io.IOException e) {
            // NBT
        }
    }

    /* sets ephemeris file path.
     * also calls swe_close(). this makes sure that swe_calc()
     * won't return planet positions previously computed from other
     * ephemerides
     */
    /**
     * This sets the search path for the ephemeris data files. Asteroid files
     * are searched in the subdirectories ast0 to ast9 first. Multiple path
     * elements are separated by a semikolon (;) or colon (:). Ephemeris
     * path elements can be normal file system paths or http-URLs. If your
     * elements contain colons or semikolons or spaces or backslashes, you
     * have to escape them with a backslash (\), e.g.
     * <CODE>&quot;./ephe:C\\:\\\\ephe:http\\://th-mack.de/datafiles&quot;</CODE>
     * for a search path of: a) local subdirectory ephe, or, if something is
     * not found here, b) in C:\ephe, or as a last resort c)
     * http://th-mack.de/datafiles.<P><B>Note: Opposed to the behaviour of
     * the C version, the Java version does not evaluate environment variables.
     * This is also true for the environment variable SE_EPHE_PATH!</B><BR>
     * @param path The search path for the Swiss Ephemeris. If null or empty,
     * a default path will be used.
     * and JPL
     * data files. You will have to quote ':', ';' and '\' characters, so a
     * path like <code>&quot;C:\swiss\ephe&quot;</code> has to be written as
     * <code>&quot;C\\:\\\\swiss\\\\ephe&quot;</code>, as any '\' will be
     * evaluated twice: the first time by the Java compiler, and the second
     * time by the program itself.
     */
    public void swe_set_ephe_path(String path) {
        String s="";
        swed.ephe_path_is_set=true;
        /* close all open files and delete all planetary data */
        swe_close();
        //  /* environment variable SE_EPHE_PATH has priority */
        //  if ((sp = getenv("SE_EPHE_PATH")) != NULL
        //    && strlen(sp) != 0
        //    && strlen(sp) <= AS_MAXCH-1-13) {
        //    strcpy(s, sp);
        //  } else
        if (path == null || path.length() == 0) {
            s=SweConst.SE_EPHE_PATH;
        } else if (path.length() <= SwissData.AS_MAXCH-1-13) {
            s=path;
        } else {
            s=SweConst.SE_EPHE_PATH;
        }
        // JAVA: Skipping this code in the Java version - it does not do anything
        // meaningful anyway...
        //    if (! s.endsWith(swed.DIR_GLUE)) {
        //      s+=swed.DIR_GLUE;
        //    }
        swed.ephepath=s;
    }

    /* sets jpl file name.
     * also calls swe_close(). this makes sure that swe_calc()
     * won't return planet positions previously computed from other
     * ephemerides
     */
    /**
     * This sets the name of the file that contains the ephemeris data
     * for the use with the JPL ephemeris. It defaults to the string
     * "de406.eph" defined in SweConst.SE_FNAME_DFT. If a path is given
     * in fname, the path will be cut off, as the path is given by
     * swe_set_ephe_path(...).
     * @param fname Name of the JPL data file
     * @see SweConst#SE_FNAME_DFT
     * @see SwissEph#swe_set_ephe_path(java.lang.String)
     */
    public void swe_set_jpl_file(String fname) {
        /* close all open files and delete all planetary data */
        swe_close();
        /* if path is contained in fnam, it is filled into the path variable */
        if (fname.indexOf(swed.DIR_GLUE)>=0) {
            fname=fname.substring(fname.lastIndexOf(swed.DIR_GLUE));
        }
        if (fname.length() >= SwissData.AS_MAXCH) {
            fname=fname.substring(0,SwissData.AS_MAXCH);
        }
        swed.jplfnam=fname;
    }

    /**
     * This sets the ayanamsha mode for sidereal planet calculations. If you
     * don't set the ayanamsha mode, it will default to Fagan/Bradley
     * (SE_SIDM_FAGAN_BRADLEY).
     * The predefined ayanamsha modes are as follows:<P><CODE>
     * <blockquote>
     * SE_SIDM_FAGAN_BRADLEY<BR>
     * SE_SIDM_LAHIRI<BR>
     * SE_SIDM_DELUCE<BR>
     * SE_SIDM_RAMAN<BR>
     * SE_SIDM_USHASHASHI<BR>
     * SE_SIDM_KRISHNAMURTI<BR>
     * SE_SIDM_DJWHAL_KHUL<BR>
     * SE_SIDM_YUKTESHWAR<BR>
     * SE_SIDM_JN_BHASIN<BR>
     * SE_SIDM_BABYL_KUGLER1<BR>
     * SE_SIDM_BABYL_KUGLER2<BR>
     * SE_SIDM_BABYL_KUGLER3<BR>
     * SE_SIDM_BABYL_HUBER<BR>
     * SE_SIDM_BABYL_ETPSC<BR>
     * SE_SIDM_ALDEBARAN_15TAU<BR>
     * SE_SIDM_HIPPARCHOS<BR>
     * SE_SIDM_SASSANIAN<BR>
     * SE_SIDM_GALCENT_0SAG<BR>
     * SE_SIDM_J2000<BR>
     * SE_SIDM_J1900<BR>
     * SE_SIDM_B1950<BR>
     * SE_SIDM_USER
     * </blockquote>
     * </CODE><P>
     * You do not consider the parameters ayan_t0 and t0 if you use a
     * predefined ayanamsha as above. If you specify SE_SIDM_USER, you
     * have to give the value of a reference date t0, and the value of
     * of the ayanamsha at that date in ayan_t0.
     * @param sid_mode One of the above ayanamsha modes plus (optionally)
     * one of the non-standard sidereal calculation modes of
     * <CODE>SE_SIDBIT_ECL_T0</CODE> or <CODE>SE_SIDBIT_SSY_PLANE</CODE>.
     * @param t0 Reference date (Julian day), if sid_mode is SE_SIDM_USER
     * @param ayan_t0 Initial ayanamsha at t0, if sid_mode is SE_SIDM_USER. This
     * is (tropical position - sidereal position) at date t0.
     * @see SweConst#SE_SIDM_FAGAN_BRADLEY
     * @see SweConst#SE_SIDM_LAHIRI
     * @see SweConst#SE_SIDM_DELUCE
     * @see SweConst#SE_SIDM_RAMAN
     * @see SweConst#SE_SIDM_USHASHASHI
     * @see SweConst#SE_SIDM_KRISHNAMURTI
     * @see SweConst#SE_SIDM_DJWHAL_KHUL
     * @see SweConst#SE_SIDM_YUKTESHWAR
     * @see SweConst#SE_SIDM_JN_BHASIN
     * @see SweConst#SE_SIDM_BABYL_KUGLER1
     * @see SweConst#SE_SIDM_BABYL_KUGLER2
     * @see SweConst#SE_SIDM_BABYL_KUGLER3
     * @see SweConst#SE_SIDM_BABYL_HUBER
     * @see SweConst#SE_SIDM_BABYL_ETPSC
     * @see SweConst#SE_SIDM_ALDEBARAN_15TAU
     * @see SweConst#SE_SIDM_HIPPARCHOS
     * @see SweConst#SE_SIDM_SASSANIAN
     * @see SweConst#SE_SIDM_GALCENT_0SAG
     * @see SweConst#SE_SIDM_J2000
     * @see SweConst#SE_SIDM_J1900
     * @see SweConst#SE_SIDM_B1950
     * @see SweConst#SE_SIDM_USER
     * @see SweConst#SE_SIDBIT_ECL_T0
     * @see SweConst#SE_SIDBIT_SSY_PLANE
     */
    public void swe_set_sid_mode(int sid_mode, double t0, double ayan_t0) {
        SidData sip = swed.sidd;
        sip.sid_mode = sid_mode;
        if (sid_mode >= SweConst.SE_SIDBITS) {
            sid_mode %= SweConst.SE_SIDBITS;
        }
        /* standard equinoxes: positions always referred to ecliptic of t0 */
        if (sid_mode == SweConst.SE_SIDM_J2000
            || sid_mode == SweConst.SE_SIDM_J1900
            || sid_mode == SweConst.SE_SIDM_B1950) {
            sip.sid_mode |= SweConst.SE_SIDBIT_ECL_T0;
        }
        if (sid_mode >= SwissData.SE_NSIDM_PREDEF && sid_mode != SweConst.SE_SIDM_USER) {
            sip.sid_mode = sid_mode = SweConst.SE_SIDM_FAGAN_BRADLEY;
        }
        swed.ayana_is_set = true;
        if (sid_mode == SweConst.SE_SIDM_USER) {
            sip.t0 = t0;
            sip.ayan_t0 = ayan_t0;
        } else {
            sip.t0 = SwephData.ayanamsa[sid_mode].t0;
            sip.ayan_t0 = SwephData.ayanamsa[sid_mode].ayan_t0;
        }
        swi_force_app_pos_etc();
    }

    /* the ayanamsa (precession in longitude)
     * according to Newcomb's definition: 360 -
     * longitude of the vernal point of t referred to the
     * ecliptic of t0.
     */
    /**
     * This calculates the ayanamsha for a given date. You should call
     * swe_set_sid_mode(...) before, where you will set the mode of ayanamsha,
     * as many different ayanamshas are used in the world today.
     * @param tjd_et The date as Julian Day in ET (Ephemeris Time or Dynamic Time)
     * @return The value of the ayanamsha
     * @see #swe_set_sid_mode(int, double, double)
     * @see #swe_get_ayanamsa_ut(double)
     */
    public double swe_get_ayanamsa(double tjd_et) {
        double x[]=new double[6], eps;
        SidData sip = swed.sidd;
        if (!swed.ayana_is_set) {
            swe_set_sid_mode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
        }
        /* vernal point (tjd), cartesian */
        x[0] = 1;
        x[1] = x[2] = 0;
        /* to J2000 */
        if (tjd_et != SwephData.J2000) {
            sl.swi_precess(x, tjd_et, SwephData.J_TO_J2000);
        }
        /* to t0 */
        sl.swi_precess(x, sip.t0, SwephData.J2000_TO_J);
        /* to ecliptic */
        eps = sl.swi_epsiln(sip.t0);
        sl.swi_coortrf(x, x, eps);
        /* to polar */
        sl.swi_cartpol(x, x);
        /* subtract initial value of ayanamsa */
        x[0] = x[0] * SwissData.RADTODEG - sip.ayan_t0;
        /* get ayanamsa */
        return sl.swe_degnorm(-x[0]);
    }

    /**
     * This calculates the ayanamsha for a given date. You should call
     * swe_set_sid_mode(...) before, where you will set the mode of ayanamsha,
     * as many different ayanamshas are used in the world today.
     * @param tjd_ut The date as Julian Day in UT (Universal Time)
     * @return The value of the ayanamsha
     * @see #swe_set_sid_mode(int, double, double)
     * @see #swe_get_ayanamsa(double)
     */
    public double swe_get_ayanamsa_ut(double tjd_ut) {
        return swe_get_ayanamsa(tjd_ut + SweDate.getDeltaT(tjd_ut));
    }

    /**********************************************************
     * get fixstar positions
     * parameters:
     * star         name of star or line number in star file
     *              (start from 1, don't count comment).
     *              If no error occurs, the name of the star is returned
     *              in the format trad_name, nomeclat_name
     *
     * tjd          absolute julian day
     * iflag        s. swecalc(); speed bit does not function
     * x            pointer for returning the ecliptic coordinates
     * serr         error return string
     **********************************************************/
    /**
     * Computes fixed stars. This method is identical to swe_fixstar_ut() with
     * the one exception that the time has to be given in ET (Ephemeris Time or
     * Dynamical Time instead of Universal Time UT). You would get ET by adding
     * deltaT to the UT, e.g.,
     * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
     * See <A HREF="SwissEph.html#swe_fixstar_ut(java.lang.StringBuffer, double, int, double[], java.lang.StringBuffer)">
     * swe_fixstar_ut(...)</A> for missing information.
     */
    public int swe_fixstar(StringBuffer star, double tjd, int iflag, double xx[],
                           StringBuffer serr) {
        int i;
        int star_nr = 0;
        boolean isnomclat = false;
        int cmplen;
        // Missing parameters are in "int swe_fixstar_found(...)"!
        String sstar=null;
        String fstar=null;
        String s, sp;
        int line = 0;
        int fline = 0;
        int epheflag, iflgsave;
        iflag |= SweConst.SEFLG_SPEED; /* we need this in order to work correctly */
        iflgsave = iflag;

        if (serr != null) {
            serr.setLength(0);
        }
        iflag = plaus_iflag(iflag);
        if (((iflag & SweConst.SEFLG_SIDEREAL)!=0) && !swed.ayana_is_set) {
            swe_set_sid_mode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
        }
        epheflag = iflag & SweConst.SEFLG_EPHMASK;
        /******************************************
         * obliquity of ecliptic 2000 and of date *
         ******************************************/
        swi_check_ecliptic(tjd);
        /******************************************
         * nutation                               *
         ******************************************/
        swi_check_nutation(tjd, iflag);
        if (swed.fixfp == null) {
            try {
                swed.fixfp = swi_fopen(SwephData.SEI_FILE_FIXSTAR, SweConst.SE_STARFILE,
                                       swed.ephepath, serr);
            } catch (SwissephException se) {
                return swe_fixstar_error(xx,SweConst.ERR);
            }
        }
        /******************************************************
         * Star file
         * close to the beginning, a few stars selected by Astrodienst.
         * These can be accessed by giving their number instead of a name.
         * All other stars can be accessed by name.
         * Comment lines start with # and are ignored.
         ******************************************************/
        swed.fixfp.seek(0);
        sstar=star.toString().substring(0,
                                        Math.min(star.length(),SweConst.SE_MAX_STNAME));
        if (sstar.length()>0) {
            if (sstar.charAt(0) == ',') {
                isnomclat = true;
            } else if (Character.isDigit(sstar.charAt(0))) {
                // Use SwissLib.atoi(...)!!!
                star_nr = Integer.parseInt(sstar);
            } else {
                /* traditional name of star to lower case */
                sstar=sstar.toLowerCase();
                if (sstar.indexOf(',')>=0) {
                    sstar=sstar.substring(0,sstar.indexOf(','));
                }
            }
            sstar=sstar.trim();
        }
        cmplen = sstar.length();
        if (cmplen == 0) {
            if (serr != null) {
                serr.setLength(0);
                serr.append("star name empty");
            }
            return swe_fixstar_error(xx,SweConst.ERR);
        }
        try {
            while ((s=swed.fixfp.readLine())!=null) {
                fline++;
                if (s.startsWith("#")) { continue; }
                line++;
                if (star_nr == line) {
                    return swe_fixstar_found(serr,s,star,tjd,iflag,iflgsave,epheflag,xx);
                }
                else if (star_nr > 0) {
                    continue;
                }
                if(s.indexOf(',')<0) {
                    if (serr != null) {
                        serr.setLength(0);
                        serr.append("star file "+SweConst.SE_STARFILE+" damaged at line "+
                                    fline);
                    }
                    return swe_fixstar_error(xx,SweConst.ERR);
                }
                sp=s.substring(s.indexOf(','));
                if (isnomclat) {
                    if (sp.substring(0,Math.min(sp.length(),cmplen)).equals(
                                                                            sstar.substring(0,Math.min(sstar.length(),cmplen)))) {
                        return swe_fixstar_found(serr,s,star,tjd,iflag,iflgsave,epheflag,xx);
                    } else {
                        continue;
                    }
                }
                fstar=s.substring(0,
                                  Math.min(SweConst.SE_MAX_STNAME,s.indexOf(','))).trim();
                i = fstar.length();
                if (i < cmplen) {
                    continue;
                }
                fstar=fstar.toLowerCase();
                if (fstar.substring(0,Math.min(fstar.length(),cmplen)).equals(
                                                                              sstar.substring(0,Math.min(sstar.length(),cmplen)))) {
                    return swe_fixstar_found(serr,s,star,tjd,iflag,iflgsave,epheflag,xx);
                }
            }
        } catch (java.io.IOException ioe) {
        }
        if (serr != null && star.length() < SwissData.AS_MAXCH - 20) {
            serr.setLength(0);
            serr.append("star "+star+" not found");
        }
        return swe_fixstar_error(xx,SweConst.ERR);
    }

    /**
     * Computes fixed stars. This method is identical to swe_fixstar() with the
     * one exception that the time has to be given in UT (Universal Time instead
     * of Ephemeris Time or Dynamical Time ET).<P>
     * The fixed stars are defined in the file fixstars.cat and the star
     * parameter must refer to any entry in that file. The entries in that file
     * start with <I>traditional_name&nbsp;,nomenclature_name,...</I>, e.g.,
     * "<CODE>Alpheratz&nbsp;&nbsp;&nbsp;&nbsp;,alAnd,</CODE>"[...].
     * @param star Actually, it is an input and an output parameter at the same
     * time. So it is not possible to define it as a String, but rather as a
     * StringBuffer. On input it defines the star to be calculated and can be
     * in three forms:<BR>
     * - as a positive integer number meaning the star in the file fixstars.cat
     * that is given on the line number of the given number, without counting
     * any comment lines beginning with #.<BR>
     * - as a traditional name case insensitively compared to the first name
     * on every line in fixstars.cat.<BR>
     * - as a nomenclature prefixed by a comma. This name is compared in a case
     * preserving manner to the nomenclature name on every line in
     * fixstars.cat.<BR>
     * On Output it returns the complete name (traditional plus nomenclature
     * name), e.g. "<CODE>Alpheratz,alAnd</CODE>".
     * @param tjd_ut The Julian Day in UT
     * @param iflag Any of the SweConst.SEFLG_* flags
     * @param xx A double[6] used as output parameter only. This returns
     * longitude, latitude and the distance (in AU) of the fixed stars, but
     * it does <B>not</B> return any speed values in xx[3] to xx[5] as it does
     * in swe_calc() / swe_calc_ut(), even if you specify SweConst.SEFLG_SPEED
     * in the flags parameter!
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return iflag or SweConst.ERR (-1); iflag MAY have changed from input
     * parameter!
     */
    public int swe_fixstar_ut(StringBuffer star, double tjd_ut, int iflag,
                              double[] xx, StringBuffer serr) {
        return swe_fixstar(star, tjd_ut + SweDate.getDeltaT(tjd_ut),
                           iflag, xx, serr);
    }


    /**
     * This will return the planet name for the given planet number. If you are
     * looking for names of asteroids, it may be possible that no name is
     * available so far. The names should be found in the asteroids data file,
     * but if nothing is found there, the name will be looked up in the file
     * seasnam.txt that should be more up to date and can be updated by the user.
     * You can get a list of names from
     * <A HREF="http://cfa-www.harvard.edu/iau/lists/MPNames.html">http://cfa-www.harvard.edu/iau/lists/MPNames.html</A>,
     * which you would like to rename to seasnam.txt and move to your ephemeris
     * directory.
     * @param ipl The planet number
     * @return The name of the planet
     */
    public String swe_get_planet_name(int ipl) {
        String s="";
        int i;
        int retc;
        double xp[]=new double[6];
        if (ipl != 0 && ipl == swed.i_saved_planet_name) {
            s=swed.saved_planet_name;
            return s;
        }
        switch(ipl) {
            case SweConst.SE_SUN:
                s = SwephData.SE_NAME_SUN;
                break;
            case SweConst.SE_MOON:
                s = SwephData.SE_NAME_MOON;
                break;
            case SweConst.SE_MERCURY:
                s = SwephData.SE_NAME_MERCURY;
                break;
            case SweConst.SE_VENUS:
                s = SwephData.SE_NAME_VENUS;
                break;
            case SweConst.SE_MARS:
                s = SwephData.SE_NAME_MARS;
                break;
            case SweConst.SE_JUPITER:
                s = SwephData.SE_NAME_JUPITER;
                break;
            case SweConst.SE_SATURN:
                s = SwephData.SE_NAME_SATURN;
                break;
            case SweConst.SE_URANUS:
                s = SwephData.SE_NAME_URANUS;
                break;
            case SweConst.SE_NEPTUNE:
                s = SwephData.SE_NAME_NEPTUNE;
                break;
            case SweConst.SE_PLUTO:
                s = SwephData.SE_NAME_PLUTO;
                break;
            case SweConst.SE_MEAN_NODE:
                s = SwephData.SE_NAME_MEAN_NODE;
                break;
            case SweConst.SE_TRUE_NODE:
                s = SwephData.SE_NAME_TRUE_NODE;
                break;
            case SweConst.SE_MEAN_APOG:
                s = SwephData.SE_NAME_MEAN_APOG;
                break;
            case SweConst.SE_OSCU_APOG:
                s = SwephData.SE_NAME_OSCU_APOG;
                break;
            case SweConst.SE_INTP_APOG: 
                s = SwephData.SE_NAME_INTP_APOG;
                break;  
            case SweConst.SE_INTP_PERG: 
                s = SwephData.SE_NAME_INTP_PERG;
                break;  
            case SweConst.SE_EARTH:
                s = SwephData.SE_NAME_EARTH;
                break;
            case SweConst.SE_CHIRON:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_CHIRON:
                s = SwephData.SE_NAME_CHIRON;
                break;
            case SweConst.SE_PHOLUS:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_PHOLUS:
                s = SwephData.SE_NAME_PHOLUS;
                break;
            case SweConst.SE_CERES:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_CERES:
                s = SwephData.SE_NAME_CERES;
                break;
            case SweConst.SE_PALLAS:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_PALLAS:
                s = SwephData.SE_NAME_PALLAS;
                break;
            case SweConst.SE_JUNO:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_JUNO:
                s = SwephData.SE_NAME_JUNO;
                break;
            case SweConst.SE_VESTA:
            case SweConst.SE_AST_OFFSET + SwephData.MPC_VESTA:
                s = SwephData.SE_NAME_VESTA;
                break;
            default:
                /* fictitious planets */
                if (ipl >= SweConst.SE_FICT_OFFSET && ipl <= SweConst.SE_FICT_MAX) {
                    return smosh.swi_get_fict_name(ipl - SweConst.SE_FICT_OFFSET, s);
                }
                /* asteroids */
                if (ipl > SweConst.SE_AST_OFFSET) {
                    /* if name is already available */
                    if (ipl == swed.fidat[SwephData.SEI_FILE_ANY_AST].ipl[0]) {
                        s=swed.fidat[SwephData.SEI_FILE_ANY_AST].astnam;
                        /* else try to get it from ephemeris file */
                    } else {
                        retc = sweph(SwephData.J2000, ipl, SwephData.SEI_FILE_ANY_AST, 0,
                                     null, SwephData.NO_SAVE, xp, null);
                        if (retc != SweConst.ERR && retc != SwephData.NOT_AVAILABLE) {
                            s=swed.fidat[SwephData.SEI_FILE_ANY_AST].astnam;
                        } else {
                            s=(ipl - SweConst.SE_AST_OFFSET)+": not found";
                        }
                    }
                    /* If there is a provisional designation only in ephemeris file,
                     * we look for a name in seasnam.txt, which can be updated by
                     * the user.
                     * Some old ephemeris files return a '?' in the first position.
                     * There are still a couple of unnamed bodies that got their
                     * provisional designation before 1925, when the current method
                     * of provisional designations was introduced. They have an 'A'
                     * as the first character, e.g. A924 RC.
                     * The file seasnam.txt may contain comments starting with '#'.
                     * There must be at least two columns:
                     * 1. asteroid catalog number
                     * 2. asteroid name
                     * The asteroid number may or may not be in brackets
                     */
                    // Hopefully, I did understand the whole thing correctly...
                    if (s.charAt(0) == '?' || Character.isDigit(s.charAt(1))) {
                        int ipli = (int) (ipl - SweConst.SE_AST_OFFSET), iplf = 0;
                        FilePtr fp = null;
                        String si;
                        try {
                            fp = swi_fopen(-1, SweConst.SE_ASTNAMFILE, swed.ephepath, null);
                        } catch (SwissephException se) {
                        }
                        if (fp != null) {
                            while(ipli != iplf) {
                                try {
                                    si=fp.readLine();
                                    if (si==null) { break; }
                                    StringTokenizer tk=new StringTokenizer(si," \t([{"); // }
                                    String sk=tk.nextToken();
                                    if (sk.startsWith("#") ||
                                        Character.isWhitespace(sk.charAt(0))) {
                                        continue;
                                    }
                                    /* catalog number of body of current line */
                                    iplf = Double.valueOf(sk).intValue();
                                    if (ipli != iplf) {
                                        continue;
                                    }
                                    s=tk.nextToken("#\r\n").trim();
                                    fp.close();
                                } catch (java.io.IOException ioe) {
                                    // NBT
                                } catch (NoSuchElementException nse) {
                                    continue; /* there is no name */
                                }
                            }
                        }
                    }
                } else  {
                    i = ipl;
                    s=""+i;
                }
                break;
                // End of default
        } // End of switch()
        if (s.length() < 80) {
            swed.i_saved_planet_name = ipl;
            swed.saved_planet_name = s;
        }
        return s;
    }

    public String swe_get_ayanamsa_name(int isidmode) {
        if (isidmode < SwissData.SE_NSIDM_PREDEF)
            return SwissData.ayanamsa_name[isidmode];
        return null;
    }

    /* set geographic position and altitude of observer */
    /**
     * If you want to do calculations relative to the observer on some place
     * on the earth rather than relative to the center of the earth, you will
     * want to set the geographic location with this method.
     * @param geolon The Longitude in degrees
     * @param geolat The Latitude in degrees
     * @param geoalt The height above sea level in meters
     */
    public void swe_set_topo(double geolon, double geolat, double geoalt) {
        swed.topd.geolon = geolon;
        swed.topd.geolat = geolat;
        swed.topd.geoalt = geoalt;
        swed.geopos_is_set = true;
        /* to force new calculation of observer position vector */
        swed.topd.teval = 0;
        /* to force new calculation of light-time etc.
         */
        swi_force_app_pos_etc();
    }


    ////////////////////////////////////////////////////////////////////////////
    // Methods from SwephJPL.java: /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public double[] getJPLRange(String fname) {
        if (sj==null) {
            sj=new SwephJPL(this, swed, sl);
        }
        return sj.getJPLRange(fname);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods from Swecl.java: ////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Computes the azimut and height from either ecliptic or equatorial
     * coordinates.
     * <P>xaz is an output parameter as follows:
     * <P><CODE>
     * xaz[0]:&nbsp;&nbsp;&nbsp;azimuth, i.e. position degree, measured from
     * the south point to west.<BR>
     * xaz[1]:&nbsp;&nbsp;&nbsp;true altitude above horizon in degrees.<BR>
     * xaz[2]:&nbsp;&nbsp;&nbsp;apparent (refracted) altitude above horizon
     * in degrees.
     * </CODE><P>
     * @param tjd_ut time and date in UT
     * @param calc_flag SweConst.SE_ECL2HOR (xin[0] contains ecliptic
     * longitude, xin[1] the ecliptic latitude) or SweConst.SE_EQU2HOR (xin[0] =
     * rectascension, xin[1] = declination)
     * @param geopos A double[3] containing the longitude, latitude and
     * height of the geographic position. Eastern longitude and northern
     * latitude is given by positive values, western longitude and southern
     * latitude by negative values.
     * @param atpress atmospheric pressure in mBar (hPa). If it is 0, the pressure
     * will be estimated from geopos[2] and attemp.
     * @param attemp atmospheric temperature in degrees Celsius.
     * @param xin double[3] with a content depending on parameter calc_flag.
     * See there. xin[3] does not need to be defined.
     * @param xaz Output parameter: a double[3] returning values as specified
     * above.
     */
    public void swe_azalt(double tjd_ut, int calc_flag, double[] geopos,
                          double atpress, double attemp, double[] xin,
                          double[] xaz) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        sc.swe_azalt(tjd_ut, calc_flag, geopos, atpress, attemp, xin, xaz);
    }

    /**
     * Computes either ecliptic or equatorial coordinates from azimuth and true
     * altitude. The true altitude might be gained from an apparent altitude by
     * calling swe_refrac.<P>xout is an output parameter containing the ecliptic
     * or equatorial coordinates, depending on the value of the parameter
     * calc_flag.
     * @param tjd_ut time and date in UT
     * @param calc_flag SweConst.SE_HOR2ECL or SweConst.SE_HOR2EQU
     * @param geopos A double[3] containing the longitude, latitude and
     * height of the geographic position. Eastern longitude and northern
     * latitude is given by positive values, western longitude and southern
     * latitude by negative values.
     * @param xin double[2] with azimuth and true altitude of planet
     * @param xout Output parameter: a double[2] returning either ecliptic or
     * equatorial coordinates
     */
    public void swe_azalt_rev(double tjd_ut, int calc_flag, double[] geopos,
                              double[] xin, double[] xout) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        sc.swe_azalt_rev(tjd_ut, calc_flag, geopos, xin, xout);
    }

    /**
     * Computes the attributes of a lunar eclipse for a given Julian Day,
     * geographic longitude, latitude, and height.
     * <BLOCKQUOTE><P><CODE>
     * attr[0]:&nbsp;&nbsp;&nbsp;umbral magnitude at tjd<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;penumbral magnitude<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of moon at tjd. <I>Not yet
     * implemented.</I><BR>
     * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of moon above horizon at tjd.
     * <I>Not yet implemented.</I><BR>
     * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of moon above horizon at tjd.
     * <I>Not yet implemented.</I><BR>
     * attr[7]:&nbsp;&nbsp;&nbsp;distance of moon from opposition in degrees
     * </CODE></BLOCKQUOTE><P><B>Attention: attr must be a double[20]!</B>
     * @param tjd_ut The Julian Day number in UT
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param geopos A double[3] containing geographic longitude, latitude and
     * height in meters above sea level in this order. Eastern longitude and
     * northern latitude is given by positive values, western longitude and
     * southern latitude by negative values.
     * @param attr A double[20], on return containing the attributes of the
     * eclipse as above
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * 0, if there is no lunar eclipse at that time and location<BR>
     * otherwise:<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_PENUMBRAL<BR>
     * SweConst.SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_PENUMBRAL
     * @see SweConst#SE_ECL_PARTIAL
     */
    public int swe_lun_eclipse_how(double tjd_ut, int ifl, double[] geopos,
                                   double[] attr, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_lun_eclipse_how(tjd_ut, ifl, geopos, attr, serr);
    }

    /**
     * Computes the next lunar eclipse anywhere on earth.
     * <P>tret is an output parameter with the following meaning:
     * <P><CODE>
     * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
     * tret[1]:&nbsp;&nbsp;&nbsp;<BR>
     * tret[2]:&nbsp;&nbsp;&nbsp;time of the begin of partial phase.<BR>
     * tret[3]:&nbsp;&nbsp;&nbsp;time of the end of partial phaseend.<BR>
     * tret[4]:&nbsp;&nbsp;&nbsp;time of the begin of totality.<BR>
     * tret[5]:&nbsp;&nbsp;&nbsp;time of the end of totality.<BR>
     * tret[6]:&nbsp;&nbsp;&nbsp;time of the begin of center line.<BR>
     * tret[7]:&nbsp;&nbsp;&nbsp;time of the end of center line<BR>
     * </CODE><P><B>Attention: tret must be a double[10]!</B>
     * @param tjd_start The Julian Day number in UT, from when to start searching
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param ifltype SweConst.SE_ECL_TOTAL for total eclipse or 0 for any eclipse
     * @param tret A double[10], on return containing the times of different
     * occasions of the eclipse as above
     * @param backward !=0, if search should be done backwards
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * otherwise:<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL<BR>
     * SweConst.SE_ECL_ANNULAR_TOTAL<BR>in combination with:<BR>
     * SweConst.SE_ECL_CENTRAL<BR>
     * SweConst.SE_ECL_NONCENTRAL
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_ANNULAR_TOTAL
     * @see SweConst#SE_ECL_CENTRAL
     * @see SweConst#SE_ECL_NONCENTRAL
     */
    public int swe_lun_eclipse_when(double tjd_start, int ifl, int ifltype,
                                    double[] tret, int backward,
                                    StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_lun_eclipse_when(tjd_start,ifl,ifltype,tret,backward,serr);
    }

    /**
     * Computes planetary nodes and apsides (perihelia, aphelia, second focal
     * points of the orbital ellipses). This method is identical to
     * swe_nod_aps_ut() with the one exception that the time has to be given
     * in ET (Ephemeris Time or Dynamical Time). You would get ET by adding
     * deltaT to the UT, e.g.,
     * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
     * See <A HREF="SwissEph.html#swe_nod_aps_ut(double, int, int, int, double[], double[], double[], double[], java.lang.StringBuffer)">swe_nod_aps_ut(...)</A> for missing information.
     */
    public int swe_nod_aps(double tjd_et, int ipl, int iflag, int  method,
                           double[] xnasc, double[] xndsc,
                           double[] xperi, double[] xaphe,
                           StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_nod_aps(tjd_et, ipl, iflag, method, xnasc, xndsc,
                              xperi, xaphe, serr);
    }

    /**
     * Computes planetary nodes and apsides (perihelia, aphelia, second focal
     * points of the orbital ellipses). This method is identical to
     * swe_nod_aps_ut() with the one exception that the time has to be given
     * in UT (Universal Time) and not in ET (Ephemeris Time or Dynamical Time).
     * @param tjd_ut The time in UT
     * @param ipl Planet number
     * @param iflag Any of the SEFLG_* flags
     * @param method Defines, what kind of calculation is wanted (SE_NODBIT_MEAN,
     * SE_NODBIT_OSCU, SE_NODBIT_OSCU_BAR, SE_NODBIT_FOPOINT)
     * @param xnasc Output parameter of double[6]. On return it contains six
     * doubles for the ascending node
     * @param xndsc Output parameter of double[6]. On return it contains six
     * doubles for the descending node
     * @param xperi Output parameter of double[6]. On return it contains six
     * doubles for the perihelion
     * @param xaphe Output parameter of double[6]. On return it contains six
     * doubles for the aphelion
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return SweConst.OK (0) or SweConst.ERR (-1)
     * @see SwissEph#swe_nod_aps(double, int, int, int, double[], double[], double[], double[], java.lang.StringBuffer)
     * @see SweConst#OK
     * @see SweConst#ERR
     * @see SweConst#SE_NODBIT_MEAN
     * @see SweConst#SE_NODBIT_OSCU
     * @see SweConst#SE_NODBIT_OSCU_BAR
     * @see SweConst#SE_NODBIT_FOPOINT
     */
    public int swe_nod_aps_ut(double tjd_ut, int ipl, int iflag, int  method,
                              double[] xnasc, double[] xndsc,
                              double[] xperi, double[] xaphe,
                              StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_nod_aps_ut(tjd_ut, ipl, iflag, method, xnasc, xndsc,
                                 xperi, xaphe, serr);
    }

    /**
     * Computes phase, phase angel, elongation, apparent diameter and apparent
     * magnitude for sun, moon, all planets and asteroids. This method is
     * identical to swe_pheno_ut() with the one exception that the time
     * has to be given in ET (Ephemeris Time or Dynamical Time). You
     * would get ET by adding deltaT to the UT, e.g.,
     * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
     * See <A HREF="SwissEph.html#swe_pheno_ut(double, int, int, double[], java.lang.StringBuffer)">swe_pheno_ut(...)</A> for missing information.
     */
    public int swe_pheno(double tjd, int ipl, int iflag, double[] attr,
                         StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_pheno(tjd, ipl, iflag, attr, serr);
    }

    /**
     * Computes phase, phase angel, elongation, apparent diameter and apparent
     * magnitude for sun, moon, all planets and asteroids.
     * <P>attr is an output parameter with the following meaning:
     * <P><CODE><BLOCKQUOTE>
     * attr[0]:&nbsp;&nbsp;&nbsp;phase angle (earth-planet-sun).<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;phase (illumined fraction of disc).<BR>
     * attr[2]:&nbsp;&nbsp;&nbsp;elongation of planet.<BR>
     * attr[3]:&nbsp;&nbsp;&nbsp;apparent diameter of disc.<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;apparent magnitude.<BR>
     * </BLOCKQUOTE></CODE><P><B>Attention: attr must be a double[20]!</B>
     * @param tjd_ut The Julian Day number in UT (Universal Time).
     * @param ipl The body number to be calculated. See class
     * <A HREF="SweConst.html">SweConst</A> for a list of bodies (SE_*)
     * @param iflag Which ephemeris is to be used (SEFLG_JPLEPH, SEFLG_SWIEPH,
     * SEFLG_MOSEPH). Also allowable flags: SEFLG_TRUEPOS, SEFLG_HELCTR.
     * @param attr A double[20] in which the result is returned. See above for more
     * details.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return SweConst.OK (0) or SweConst.ERR (-1)
     * @see SweConst#OK
     * @see SweConst#ERR
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     * @see SweConst#SEFLG_TRUEPOS
     * @see SweConst#SEFLG_HELCTR
     */
    public int swe_pheno_ut(double tjd_ut, int ipl, int iflag, double[] attr,
                            StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_pheno_ut(tjd_ut, ipl, iflag, attr, serr);
    }

    /**
     * Calculates the true altitude from the apparent altitude or vice versa.
     * @param inalt The true or apparent altitude to be converted
     * @param atpress Atmospheric pressure in mBar (hPa). If it is 0, the pressure
     * will be estimated from attemp on sea level.
     * @param attemp Atmospheric temperature in degrees Celsius.
     * @param calc_flag SweConst.SE_TRUE_TO_APP or SweConst.SE_APP_TO_TRUE
     * @return The converted altitude
     */
    public double swe_refrac(double inalt, double atpress, double attemp,
                             int calc_flag) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_refrac(inalt, atpress, attemp, calc_flag);
    }

    /**
     * Calculates the times of rising, setting and meridian transits for all
     * planets, asteroids, the moon, and the fixed stars.
     * @param tjd_ut The Julian Day number in UT, from when to start searching
     * @param ipl Planet number, if times for planet or moon are to be calculated.
     * @param starname The name of the star, if times for a star should be
     * calculated. It has to be null or the empty string otherwise!
     * @param epheflag To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param rsmi Specification, what type of calculation is wanted
     * (SE_CALC_RISE, SE_CALC_SET, SE_CALC_MTRANSIT, SE_CALC_ITRANSIT,
     * SE_BIT_DISC_CENTER, SE_BIT_NO_REFRACTION). If it is 0, SE_CALC_RISE is
     * calculated
     * @param geopos A double[3] containing the longitude, latitude and
     * height of the observer. Eastern longitude and northern
     * latitude is given by positive values, western longitude and southern
     * latitude by negative values.
     * @param atpress atmospheric pressure in mBar (hPa). If it is 0, the pressure
     * will be estimated from geopos[2] and attemp (1013.25 mbar for sea level).
     * When calculating MTRANSIT or ITRANSIT, this parameter is not used.
     * @param attemp atmospheric temperature in degrees Celsius. When
     * calculating MTRANSIT or ITRANSIT, this parameter is not used.
     * @param tret Return value containing the time of rise or whatever was
     * requested. This is UT.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails
     * @return SweConst.OK (0) or SweConst.ERR (-1)
     * @see SweConst#OK
     * @see SweConst#ERR
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     * @see SweConst#SE_CALC_RISE
     * @see SweConst#SE_CALC_SET
     * @see SweConst#SE_CALC_MTRANSIT
     * @see SweConst#SE_CALC_ITRANSIT
     * @see SweConst#SE_BIT_DISC_CENTER
     * @see SweConst#SE_BIT_NO_REFRACTION
     * @see DblObj
     */
    public int swe_rise_trans(double tjd_ut, int ipl, StringBuffer starname,
                              int epheflag, int rsmi, double[] geopos,
                              double atpress, double attemp, DblObj tret,
                              StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_rise_trans(tjd_ut, ipl, starname, epheflag, rsmi, geopos,
                                 atpress, attemp, tret, serr);
    }

    /**
     * Computes the attributes of a solar eclipse for a given Julian Day,
     * geographic longitude, latitude, and height.
     * <P><CODE><BLOCKQUOTE>
     * attr[0]:&nbsp;&nbsp;&nbsp;fraction of solar diameter covered by moon
     * (magnitude)<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;ratio of lunar diameter to solar one<BR>
     * attr[2]:&nbsp;&nbsp;&nbsp;fraction of solar disc covered by moon
     * (obscuration)<BR>
     * attr[3]:&nbsp;&nbsp;&nbsp;diameter of core shadow in km<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of sun at tjd<BR>
     * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of sun above horizon at tjd<BR>
     * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of sun above horizon at tjd<BR>
     * attr[7]:&nbsp;&nbsp;&nbsp;angular distance of moon from sun in degrees
     * </BLOCKQUOTE></CODE><P><B>Attention: attr must be a double[20]!</B>
     * @param tjd_ut The Julian Day number in UT
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param geopos A double[3] containing geographic longitude, latitude and
     * height in meters above sea level in this order. Eastern longitude and
     * northern latitude is given by positive values, western longitude and
     * southern latitude by negative values.
     * @param attr A double[20], on return containing the attributes of the
     * eclipse as above
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * 0, if there is no solar eclipse at that time and location<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_sol_eclipse_how(double tjd_ut, int ifl, double[] geopos,
                                   double[] attr, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_sol_eclipse_how(tjd_ut, ifl, geopos, attr, serr);
    }

    /**
     * Computes the next solar eclipse anywhere on earth.
     * <P>tret is an output parameter with the following meaning:
     * <P><CODE>
     * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
     * tret[1]:&nbsp;&nbsp;&nbsp;time, when the eclipse takes place at local
     * apparent noon.<BR><BLOCKQUOTE>
     * tret[2]:&nbsp;&nbsp;&nbsp;time of the begin of the eclipse.<BR>
     * tret[3]:&nbsp;&nbsp;&nbsp;time of the end of the eclipse.<BR>
     * tret[4]:&nbsp;&nbsp;&nbsp;time of the begin of totality.<BR>
     * tret[5]:&nbsp;&nbsp;&nbsp;time of the end of totality.<BR>
     * tret[6]:&nbsp;&nbsp;&nbsp;time of the begin of center line.<BR>
     * tret[7]:&nbsp;&nbsp;&nbsp;time of the end of center line<BR>
     * tret[8]:&nbsp;&nbsp;&nbsp;time, when annular-total eclipse becomes total --
     * <I>Not yet implemented.</I><BR>
     * tret[9]:&nbsp;&nbsp;&nbsp;time, when annular-total eclipse becomes annular
     * again -- <I>Not yet implemented.</I>
     * </BLOCKQUOTE></CODE><P><B>Attention: tret must be a double[10]!</B>
     * @param tjd_start The Julian Day number in UT, from when to start searching
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param ifltype SweConst.SE_ECL_TOTAL for total eclipse or 0 for any eclipse
     * @param tret A double[10], on return containing the times of different
     * occasions of the eclipse as above
     * @param backward !=0, if search should be done backwards
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL<BR>
     * SweConst.SE_ECL_ANNULAR_TOTAL<BR>in combination with:<BR>
     * SweConst.SE_ECL_CENTRAL<BR>
     * SweConst.SE_ECL_NONCENTRAL
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_ANNULAR_TOTAL
     * @see SweConst#SE_ECL_CENTRAL
     * @see SweConst#SE_ECL_NONCENTRAL
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_sol_eclipse_when_glob(double tjd_start, int ifl, int ifltype,
                                         double tret[], int backward,
                                         StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_sol_eclipse_when_glob(tjd_start, ifl, ifltype, tret,
                                            backward, serr);
    }

    /**
     * Computes the next solar eclipse at a given geographical position. Note the
     * uncertainty of Delta T for the remote past and the future.<P>
     * tret is an output parameter with the following meaning:
     * <P><CODE>
     * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
     * tret[1]:&nbsp;&nbsp;&nbsp;time of first contact.<BR>
     * tret[2]:&nbsp;&nbsp;&nbsp;time of second contact.<BR>
     * tret[3]:&nbsp;&nbsp;&nbsp;time of third contact.<BR>
     * tret[4]:&nbsp;&nbsp;&nbsp;time of forth contact.<BR>
     * tret[5]:&nbsp;&nbsp;&nbsp;time of sun rise between first and forth contact
     * -- <I>Not yet implemented.</I><BR>
     * tret[6]:&nbsp;&nbsp;&nbsp;time of sun set between first and forth contact
     * -- <I>Not yet implemented.</I><BR>
     * </CODE><P>
     * attr is an output parameter with the following meaning:
     * <P><CODE>
     * attr[0]:&nbsp;&nbsp;&nbsp;fraction of solar diameter covered by moon
     * (magnitude).<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;ratio of lunar diameter to solar one.<BR>
     * attr[2]:&nbsp;&nbsp;&nbsp;fraction of solar disc covered by moon
     * (obscuration).<BR>
     * attr[3]:&nbsp;&nbsp;&nbsp;diameter of core shadow in km.<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of sun at tjd.<BR>
     * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of sun above horizon at tjd.<BR>
     * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of sun above horizon at tjd.<BR>
     * attr[7]:&nbsp;&nbsp;&nbsp;elongation of moon in degrees.<BR>
     * </CODE><P><B>Attention: attr must be a double[20]!</B>
     * @param tjd_start The Julian Day number in UT, from when to start searching
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH)
     * @param geopos A double[3] containing the longitude, latitude and
     * height of the geographic position. Eastern longitude and northern
     * latitude is given by positive values, western longitude and southern
     * latitude by negative values.
     * @param tret A double[7], on return containing the times of different
     * occasions of the eclipse as specified above
     * @param attr A double[20], on return containing different attributes of
     * the eclipse. See above.
     * @param backward true, if search should be done backwards
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL<BR>in combination with:<BR>
     * SweConst.SE_ECL_VISIBLE<BR>
     * SweConst.SE_ECL_MAX_VISIBLE<BR>
     * SweConst.SE_ECL_1ST_VISIBLE<BR>
     * SweConst.SE_ECL_2ND_VISIBLE<BR>
     * SweConst.SE_ECL_3RD_VISIBLE<BR>
     * SweConst.SE_ECL_4TH_VISIBLE
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_VISIBLE
     * @see SweConst#SE_ECL_MAX_VISIBLE
     * @see SweConst#SE_ECL_1ST_VISIBLE
     * @see SweConst#SE_ECL_2ND_VISIBLE
     * @see SweConst#SE_ECL_3RD_VISIBLE
     * @see SweConst#SE_ECL_4TH_VISIBLE
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_sol_eclipse_when_loc(double tjd_start, int ifl,
                                        double[] geopos, double[] tret,
                                        double[] attr, int backward,
                                        StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_sol_eclipse_when_loc(tjd_start, ifl, geopos, tret, attr,
                                           backward, serr);
    }

    /**
     * Computes the geographic location for a given time, where a solar
     * eclipse is central (or maximum for a non-central eclipse).
     * <P>Output parameters:<P><CODE><BLOCKQUOTE>
     * geopos[0]:&nbsp;&nbsp;&nbsp;geographic longitude of central line, positive
     * values mean east of Greenwich, negative values west of Greenwich<BR>
     * geopos[1]:&nbsp;&nbsp;&nbsp;geographic latitude of central line,
     * positive values mean north of equator, negative values south<BR>
     * </CODE><P><CODE>
     * attr[0]:&nbsp;&nbsp;&nbsp;fraction of solar diameter covered by moon
     * (magnitude)<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;ratio of lunar diameter to solar one<BR>
     * attr[2]:&nbsp;&nbsp;&nbsp;fraction of solar disc covered by moon
     * (obscuration)<BR>
     * attr[3]:&nbsp;&nbsp;&nbsp;diameter of core shadow in km<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of sun at tjd<BR>
     * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of sun above horizon at tjd<BR>
     * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of sun above horizon at tjd<BR>
     * attr[7]:&nbsp;&nbsp;&nbsp;angular distance of moon from sun in degrees
     * </BLOCKQUOTE></CODE><P><B>Attention: geopos must be a double[10], attr
     * a double[20]!</B>
     * @param tjd_ut The Julian Day number in UT
     * @param ifl To indicate, which ephemeris to be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH, SEFLG_MOSEPH)
     * @param geopos A double[10], on return containing the geographic positions.
     * @param attr A double[20], on return containing the attributes of the
     * eclipse as above.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * 0, if there is no solar eclipse at that time<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_CENTRAL<BR>
     * SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_NONCENTRAL<BR>
     * SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_CENTRAL<BR>
     * SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_NONCENTRAL<BR>
     * SweConst.SE_ECL_PARTIAL<BR>
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_CENTRAL
     * @see SweConst#SE_ECL_NONCENTRAL
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_sol_eclipse_where(double tjd_ut, int ifl, double[] geopos,
                                     double[] attr, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_sol_eclipse_where(tjd_ut, ifl, geopos, attr, serr);
    }


    /* Same declaration as swe_sol_eclipse_when_loc().
     * In addition:
     * int32 ipl          planet number of occulted body
     * char* starname     name of occulted star. Must be NULL or "", if a planetary
     *                    occultation is to be calculated. For the use of this
     *                    field, also see swe_fixstar().
     * int32 ifl        ephemeris flag. If you want to have only one conjunction
     *                    of the moon with the body tested, add the following flag:
     *                    ifl |= SE_ECL_ONE_TRY. If this flag is not set,
     *                    the function will search for an occultation until it
     *                    finds one. For bodies with ecliptical latitudes > 5,
     *                    the function may search successlessly until it reaches
     *                    the end of the ephemeris.
     */
    /**
     * Computes the next eclipse of any planet or fixstar at a given geographical
     * position. Note the uncertainty of Delta T for the remote past and the
     * future.<P>
     * tret is an output parameter with the following meaning:
     * <P><CODE>
     * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
     * tret[1]:&nbsp;&nbsp;&nbsp;time of first contact.<BR>
     * tret[2]:&nbsp;&nbsp;&nbsp;time of second contact.<BR>
     * tret[3]:&nbsp;&nbsp;&nbsp;time of third contact.<BR>
     * tret[4]:&nbsp;&nbsp;&nbsp;time of forth contact.<BR>
     * tret[5]:&nbsp;&nbsp;&nbsp;time of sun rise between first and forth contact
     * -- <I>Not yet implemented.</I><BR>
     * tret[6]:&nbsp;&nbsp;&nbsp;time of sun set between first and forth contact
     * -- <I>Not yet implemented.</I><BR>
     * </CODE><P>
     * attr is an output parameter with the following meaning:
     * <P><CODE>
     * attr[0]:&nbsp;&nbsp;&nbsp;fraction of solar diameter covered by moon
     * (magnitude).<BR>
     * attr[1]:&nbsp;&nbsp;&nbsp;ratio of lunar diameter to solar one.<BR>
     * attr[2]:&nbsp;&nbsp;&nbsp;fraction of solar disc covered by moon
     * (obscuration).<BR>
     * attr[3]:&nbsp;&nbsp;&nbsp;diameter of core shadow in km.<BR>
     * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of sun at tjd.<BR>
     * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of sun above horizon at tjd.<BR>
     * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of sun above horizon at tjd.<BR>
     * attr[7]:&nbsp;&nbsp;&nbsp;elongation of moon in degrees.<BR>
     * </CODE><P><B>Attention: attr must be a double[20]!</B>
     * @param tjd_start The Julian Day number in UT, from when to start searching
     * @param ipl Planet number of the occulted planet. See SE_SUN etc. for the
     * planet numbers.
     * @param starname The name of the fixstar, if looking for an occulted
     * fixstar. This has to be null or an empty StringBuffer, if you are looking
     * for a planet specified in parameter ipl. See routine swe_fixstar() for this
     * parameter.
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH). Additionally, you can specify SE_ECL_ONE_TRY,
     * to only search for one conjunction of the moon with the planetary body.
     * If this flag is not set, the function will search for an occultation until
     * it finds one. For bodies with ecliptical latitudes > 5, the function may
     * search successlessly until it reaches the end of the ephemeris.
     * @param geopos A double[3] containing the longitude, latitude and
     * height of the geographic position. Eastern longitude and northern
     * latitude is given by positive values, western longitude and southern
     * latitude by negative values.
     * @param tret A double[7], on return containing the times of different
     * occasions of the eclipse as specified above
     * @param attr A double[20], on return containing different attributes of
     * the eclipse. See above.
     * @param backward any value != 0 means, search should be done backwards
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL<BR>in combination with:<BR>
     * SweConst.SE_ECL_VISIBLE<BR>
     * SweConst.SE_ECL_MAX_VISIBLE<BR>
     * SweConst.SE_ECL_1ST_VISIBLE<BR>
     * SweConst.SE_ECL_2ND_VISIBLE<BR>
     * SweConst.SE_ECL_3RD_VISIBLE<BR>
     * SweConst.SE_ECL_4TH_VISIBLE
     * @see #swe_fixstar_ut(StringBuffer, double, int, double[], StringBuffer)
     * @see SweConst#SE_ECL_ONE_TRY
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_VISIBLE
     * @see SweConst#SE_ECL_MAX_VISIBLE
     * @see SweConst#SE_ECL_1ST_VISIBLE
     * @see SweConst#SE_ECL_2ND_VISIBLE
     * @see SweConst#SE_ECL_3RD_VISIBLE
     * @see SweConst#SE_ECL_4TH_VISIBLE
     * @see SweConst#SE_ECL_ONE_TRY
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_lun_occult_when_loc(double tjd_start, int ipl, StringBuffer starname, int ifl,
                                       double[] geopos, double[] tret, double[] attr, int backward, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_lun_occult_when_loc(tjd_start, ipl, starname, ifl, geopos, tret, attr, backward, serr);
    }

    /**
     * Computes the geographic location for a given time, where a planet
     * occultation by the moon is central or maximum for a non-central
     * occultation.
     * @param tjd_ut The Julian Day number in UT
     * @param ipl The planet, whose occultation by the moon should be searched.
     * @param starname The fixstar, whose occultation by the moon should be
     * searched. See swe_fixstar() for details. It has to be null or the empty
     * string, if a planet (see parameter ipl) is to be searched.
     * @param ifl To indicate, which ephemeris to be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH, SEFLG_MOSEPH)
     * @param geopos A double[10], on return containing the geographic positions.
     * @param attr A double[20], on return containing the attributes of the
     * eclipse as above.
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * 0, if there is no solar eclipse at that time<BR>
     * @see #swe_sol_eclipse_where(double, int, double[], double[], java.lang.StringBuffer)
     * @see #swe_fixstar_ut(StringBuffer, double, int, double[], StringBuffer)
     */
    public int swe_lun_occult_where(double tjd_ut,
                                    int ipl,
                                    StringBuffer starname,
                                    int ifl,
                                    double[] geopos,
                                    double[] attr,
                                    StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_lun_occult_where(tjd_ut, ipl, starname, ifl, geopos, attr, serr);
    }


    /* When is the next lunar occultation anywhere on earth?
     * This function also finds solar eclipses, but is less efficient
     * than swe_sol_eclipse_when_glob().
     *
     * input parameters:
     *
     * tjd_start          start time for search (UT)
     * ipl                planet number of occulted body
     * starname           name of occulted star. Must be NULL or "", if a planetary
     *                    occultation is to be calculated. For the use of this
     *                    field, also see swe_fixstar().
     * ifl                      ephemeris to be used (SEFLG_SWIEPH, etc.)
     *                  ephemeris flag. If you want to have only one conjunction
     *                    of the moon with the body tested, add the following flag:
     *                    ifl |= SE_ECL_ONE_TRY. If this flag is not set,
     *                    the function will search for an occultation until it
     *                    finds one. For bodies with ecliptical latitudes > 5,
     *                    the function may search successlessly until it reaches
     *                    the end of the ephemeris.
     *
     * ifltype          eclipse type to be searched (SE_ECL_TOTAL, etc.)
     *                    0, if any type of eclipse is wanted
     *                    this functionality also works with occultations
     *
     * return values:
     *
     * retflag    SE_ECL_TOTAL or SE_ECL_ANNULAR or SE_ECL_PARTIAL
     *              or SE_ECL_ANNULAR_TOTAL
     *              SE_ECL_CENTRAL
     *              SE_ECL_NONCENTRAL
     *
     * tret[0]    time of maximum eclipse
     * tret[1]    time, when eclipse takes place at local apparent noon
     * tret[2]    time of eclipse begin
     * tret[3]    time of eclipse end
     * tret[4]    time of totality begin
     * tret[5]    time of totality end
     * tret[6]    time of center line begin
     * tret[7]    time of center line end
     * tret[8]    time when annular-total eclipse becomes total
     *               not implemented so far
     * tret[9]    time when annular-total eclipse becomes annular again
     *               not implemented so far
     *         declare as tret[10] at least!
     *
     */
    /**
     * Computes the next lunar occultation anywhere on earth.
     * This method also finds solar eclipses, but is less efficient
     * than swe_sol_eclipse_when_glob().
     * <P>tret is an output parameter with the following meaning:
     * <P><CODE>
     * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
     * tret[1]:&nbsp;&nbsp;&nbsp;time, when the eclipse takes place at local
     * apparent noon.<BR><BLOCKQUOTE>
     * tret[2]:&nbsp;&nbsp;&nbsp;time of the begin of the eclipse.<BR>
     * tret[3]:&nbsp;&nbsp;&nbsp;time of the end of the eclipse.<BR>
     * tret[4]:&nbsp;&nbsp;&nbsp;time of the begin of totality.<BR>
     * tret[5]:&nbsp;&nbsp;&nbsp;time of the end of totality.<BR>
     * tret[6]:&nbsp;&nbsp;&nbsp;time of the begin of center line.<BR>
     * tret[7]:&nbsp;&nbsp;&nbsp;time of the end of center line<BR>
     * tret[8]:&nbsp;&nbsp;&nbsp;time, when annular-total eclipse becomes total --
     * <I>Not yet implemented.</I><BR>
     * tret[9]:&nbsp;&nbsp;&nbsp;time, when annular-total eclipse becomes annular
     * again -- <I>Not yet implemented.</I>
     * </BLOCKQUOTE></CODE><P><B>Attention: tret must be a double[10]!</B>
     * @param tjd_start The Julian Day number in UT, from when to start searching
     * @param ipl planet number of occulted body
     * @param starname name of occulted star. Must be null or &quot;&quot;, if
     * a planetary occultation is to be calculated. For the use of this
     * field, also see swe_fixstar().
     * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
     * SEFLG_SWIEPH or SEFLG_MOSEPH). If you want to have only one conjunction
     * of the moon with the body tested, add flag SE_ECL_ONE_TRY. If this flag
     * is not set, the function will search for an occultation until it
     * finds one. For bodies with ecliptical latitudes > 5, the function may
     * search successlessly until it reaches the end of the ephemeris.
     * @param ifltype eclipse type to be searched (SE_ECL_TOTAL, etc.).
     * 0, if any type of eclipse is wanted. This functionality also works
     * with occultations.
     * @param tret A double[10], on return containing the times of different
     * occasions of the eclipse as above
     * @param backward if != 0, search is done backwards
     * @param serr A StringBuffer containing a warning or error message, if
     * something fails.
     * @return -1 (SweConst.ERR), if the calculation failed<BR>
     * SweConst.SE_ECL_TOTAL<BR>
     * SweConst.SE_ECL_ANNULAR<BR>
     * SweConst.SE_ECL_PARTIAL<BR>
     * SweConst.SE_ECL_ANNULAR_TOTAL<BR>in combination with:<BR>
     * SweConst.SE_ECL_CENTRAL<BR>
     * SweConst.SE_ECL_NONCENTRAL
     * @see #swe_sol_eclipse_when_glob(double, int, int, double[], int, java.lang.StringBuffer)
     * @see #swe_fixstar_ut(StringBuffer, double, int, double[], StringBuffer)
     * @see SweConst#SE_ECL_ONE_TRY
     * @see SweConst#SE_ECL_TOTAL
     * @see SweConst#SE_ECL_ANNULAR
     * @see SweConst#SE_ECL_PARTIAL
     * @see SweConst#SE_ECL_ANNULAR_TOTAL
     * @see SweConst#SE_ECL_CENTRAL
     * @see SweConst#SE_ECL_NONCENTRAL
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_lun_occult_when_glob(
                                        double tjd_start, int ipl, StringBuffer starname, int ifl, int ifltype,
                                        double[] tret, int backward, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_lun_occult_when_glob(tjd_start, ipl, starname, ifl, ifltype, tret, backward, serr);
    }

    /* function finds the gauquelin sector position of a planet or fixed star
     * 
     * if starname != NULL then a star is computed.
     * iflag: use the flags SE_SWIEPH, SE_JPLEPH, SE_MOSEPH, SEFLG_TOPOCTR.
     *
     * imeth defines method:
     *           imeth = 0                  sector from longitude and latitude
     *           imeth = 1                  sector from longitude, with lat = 0
     *           imeth = 2                  sector from rise and set
     *           imeth = 3                  sector from rise and set with refraction
     * rise and set are defined as appearance and disappearance of disc center.
     *
     * geopos is an array of 3 doubles for geo. longitude, geo. latitude, elevation.
     * atpress and attemp are only needed for imeth = 3. If imeth = 3,
     * If imeth=3 and atpress not given (= 0), the programm assumes 1013.25 mbar;
     * if a non-zero height above sea is given in geopos, atpress is estimated.
     * dgsect is return area (pointer to a double)
     * serr is pointer to error string, may be NULL
     */
    /**
     * Finds the gauquelin sector position of a planet or fixed star.
     * @param t_ut Time in UT.
     * @param ipl Planet number.
     * @param starname If starname != null and not an empty string, then a
     * fixstar is computed and not a planet specified in ipl. See swe_fixstar()
     * method on this.
     * @param iflag Use the flags SE_SWIEPH, SE_JPLEPH, SE_MOSEPH, SEFLG_TOPOCTR.
     * @param imeth defines the method.<br>
     * <blockquote>
     * imeth = 0: sector from longitude and latitude<br>
     * imeth = 1: sector from longitude, with lat = 0<br>
     * imeth = 2: sector from rise and set<br>
     * imeth = 3: sector from rise and set with refraction<br>
     * </blockquote>
     * Rise and set are defined as appearance and disappearance of disc center.
     * @param geopos An array of 3 doubles for geo. longitude, geo. latitude, elevation in meter.
     * @param atpress Only needed for imeth = 3.
     * If imeth=3 and atpress not given (= 0), the programm assumes 1013.25 mbar;
     * if a non-zero height above sea is given in geopos, atpress is estimated.
     * @param attemp Temperature. Only needed for imeth = 3.
     * @param dgsect Return value.
     * @param serr Pointer to error string, may be null.
     * @return SweConst.OK (0) or SweConst.ERR (-1) on error.
     * @see #swe_fixstar_ut(StringBuffer, double, int, double[], StringBuffer)
     * @see SweConst#SEFLG_TOPOCTR
     * @see SweConst#SEFLG_JPLEPH
     * @see SweConst#SEFLG_SWIEPH
     * @see SweConst#SEFLG_MOSEPH
     */
    public int swe_gauquelin_sector(double t_ut, int ipl, StringBuffer starname, int iflag, int imeth, double[] geopos, double atpress, double attemp, DblObj dgsect, StringBuffer serr) {
        if (sc==null) {
            sc=new Swecl(this, sl, sm, swed);
        }
        return sc.swe_gauquelin_sector(t_ut, ipl, starname, iflag, imeth, geopos, atpress, attemp, dgsect, serr);
    }
    ////////////////////////////////////////////////////////////////////////////
    // Methods from SweHouse.java: /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The function returns a value between 1.0 and 12.999999, indicating in
     * which house a planet is and how far from its cusp it is. With Koch houses,
     * the function sometimes returns 0, if the computation was not possible.
     * @param armc The ARMC (= sidereal time)
     * @param geolat The latitude
     * @param eps The ecliptic obliquity (e.g. xx[0] of swe_calc(...))
     * @param hsys The house system. See swe_houses(...) for a list of all
     * houses.
     * @param xpin A double[2] containing the ecliptic longitude and latitude of
     * the planet in degrees. It is an input parameter, describing tropical positions.
     * @param serr StringBuffer to contain any error messages or warnings
     * @return A value between 1.0 and 12.999999, indicating in which house a
     * planet is and how far from its cusp it is. Koch may return 0, if the
     * calculation was not possible.
     * @see #swe_houses(double, int, double, double, int, double[], double[])
     */
    public double swe_house_pos(double armc, double geolat, double eps,
                                int hsys, double xpin[], StringBuffer serr) {
        if (sh==null) {
            sh=new SweHouse(sl, this, swed);
        }
        return sh.swe_house_pos(armc, geolat, eps, hsys, xpin, serr);
    }


    /**
     * Calculates the house positions and other vital points. You would use
     * this method instead of swe_houses, if you do not have a date available,
     * but just the ARMC (sidereal time).
     * @param armc The ARMC (= sidereal time)
     * @param geolat The latitude on earth, for which the calculation has to be
     * done.
     * @param eps The ecliptic obliquity (e.g. xx[0] of swe_calc(...))
     * @param hsys The house system as a character given as an integer. See
     * swe_houses(...) for a list of all houses.
     * @param cusp The house cusps are returned here in cusp[1...12] for
     * the house 1 to 12.
     * @param ascmc The special points like ascendent etc. are returned here.
     * See swe_houses(...) for further info on this parameter.
     * @see SwissEph#swe_houses(double, int, double, double, int, double[], double[])
     * @see SwissEph#swe_calc
     * @return SweConst.OK (==0) or SweConst.ERR (==-1), if calculation was not
     * possible due to nearness to the polar circle in Koch or Placidus house system
     * or when requesting Gauquelin sectors. Calculation automatically switched to
     * Porphyry house calculation method in this case, so that valid houses will be
     * returned anyway, just in a different house system than requested.
     */
    public int swe_houses_armc(double armc, double geolat, double eps,
                               int hsys, double[] cusp, double[] ascmc) {
        if (sh==null) {
            sh=new SweHouse(sl, this, swed);
        }
        return sh.swe_houses_armc(armc, geolat, eps, hsys, cusp, ascmc, 0);
    }


    /**
     * Calculates the house positions and other vital points. The possible
     * house systems are:<P><CODE><BLOCKQUOTE>
     * (int)'P'&nbsp;&nbsp;Placidus<BR>
     * (int)'K'&nbsp;&nbsp;Koch<BR>
     * (int)'O'&nbsp;&nbsp;Porphyrius<BR>
     * (int)'R'&nbsp;&nbsp;Regiomontanus<BR>
     * (int)'C'&nbsp;&nbsp;Campanus<BR>
     * (int)'A'&nbsp;&nbsp;equal (cusp 1 is ascendent)<BR>
     * (int)'E'&nbsp;&nbsp;equal (cusp 1 is ascendent)<BR>
     * (int)'V'&nbsp;&nbsp;Vehlow equal (asc. in middle of house 1)<BR>
     * (int)'X'&nbsp;&nbsp;axial rotation system/ Meridian houses<BR>
     * (int)'H'&nbsp;&nbsp;azimuthal or horizontal system<BR>
     * (int)'T'&nbsp;&nbsp;Polich/Page ('topocentric' system)<BR>
     * (int)'B'&nbsp;&nbsp;Alcabitius
     * </BLOCKQUOTE></CODE><P>
     *
     * As Koch and Placidus don't work in the polar circle, the
     * calculation is done in that case by swapping MC/IC so that MC is
     * always before AC in the zodiac. Then the quadrants are divided into
     * 3 equal parts.<P>
     * The parameter ascmc is defined as double[10] and will return the
     * following points:<P><CODE><BLOCKQUOTE>
     * ascmc[0] = ascendant<BR>
     * ascmc[1] = mc<BR>
     * ascmc[2] = armc (= sidereal time)<BR>
     * ascmc[3] = vertex<BR>
     * ascmc[4] = equatorial ascendant<BR>
     * ascmc[5] = co-ascendant (Walter Koch)<BR>
     * ascmc[6] = co-ascendant (Michael Munkasey)<BR>
     * ascmc[7] = polar ascendant (Michael Munkasey)<BR>
     * ascmc[8] = reserved for future use<BR>
     * ascmc[9] = reserved for future use
     *  </BLOCKQUOTE></CODE>
     * You can use the SE_ constants below from SweConst.java to access
     * these values in ascmc[].<p>
     * @param tjd_ut The Julian Day number in UT
     * @param iflag An additional flag for calculation. It must be 0 or
     * SEFLG_SIDEREAL and / or SEFLG_RADIANS.
     * @param geolat The latitude on earth, for which the calculation has to be
     * done.
     * @param geolon The longitude on earth, for which the calculation has to be
     * done. Eastern longitude and northern latitude is given by positive values,
     * western longitude and southern latitude by negative values.
     * @param hsys The house system as a character given as an integer.
     * @param cusp (double[13]) The house cusps are returned here in
     * cusp[1...12] for the houses 1 to 12.
     * @param ascmc (double[10]) The special points like ascendent etc. are
     * returned here. See the list above.
     * @return SweConst.OK (==0) or SweConst.ERR (==-1), if calculation was not
     * possible due to nearness to the polar circle in Koch or Placidus house system
     * or when requesting Gauquelin sectors. Calculation automatically switched to
     * Porphyry house calculation method in this case, so that valid houses will be
     * returned anyway, just in a different house system than requested.
     * @see SwissEph#swe_set_sid_mode(int, double, double)
     * @see SweConst#SEFLG_RADIANS
     * @see SweConst#SEFLG_SIDEREAL
     */  
    public int swe_houses(double tjd_ut, int iflag, double geolat,
                          double geolon, int hsys, double[] cusp,
                          double[] ascmc) {
        return swe_houses(tjd_ut, iflag, geolat, geolon, hsys, cusp, ascmc, 0);
    }
    public int swe_houses(double tjd_ut, int iflag, double geolat,
                          double geolon, int hsys, double[] cusp,
                          double[] ascmc, int aOffs) {
        if (sh==null) {
            sh=new SweHouse(sl, this, swed);
        }
        return sh.swe_houses(tjd_ut, iflag, geolat, geolon, hsys, cusp, ascmc, aOffs);
    }


    /**
     * Searches for the next or previous transit of a planet over a specified
     * longitude, latitude, distance or speed value with geocentric or topocentric
     * positions in a tropical or sidereal zodiac. Dates are interpreted as ET
     * (=UT&nbsp;+&nbsp;deltaT).<p>
     *
     * @param tc The TransitCalculator that should be used here.
     * @param jdET The date (and time) in ET, from where to start searching.
     * @param backwards If backward search should be performed.
     * @return return A double containing the julian day number for the next /
     * previous transit as ET.
     * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
     * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
     */
    public double getTransitET(TransitCalculator tc, double jdET, boolean backwards)
        throws IllegalArgumentException, SwissephException {
        return getTransitET(tc,
                            jdET,
                            backwards,
                            (backwards?-Double.MAX_VALUE:Double.MAX_VALUE));
    }
    /**
     * Searches for the next or previous transit of a planet over a specified
     * longitude, latitude, distance or speed value with geocentric or topocentric
     * positions in a tropical or sidereal zodiac. Dates are interpreted as ET
     * (=UT&nbsp;+&nbsp;deltaT).<p>
     *
     * @param tc The TransitCalculator that should be used here.
     * @param jdET The date (and time) in ET, from where to start searching.
     * @param backwards If backward search should be performed.
     * @param jdLimit This is the date, when the search for transits should be
     * stopped, even if no transit point had been found up to then.
     * @return return A double containing the julian day number for the next /
     * previous transit as ET.
     * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
     * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
     */
    public double getTransitET(TransitCalculator tc, double jdET, boolean backwards, double jdLimit)
        throws IllegalArgumentException, SwissephException {
        if (ext==null) { ext=new Extensions(this); }
        return ext.getTransit(tc, jdET, backwards, jdLimit);
    }
    /**
     * Searches for the next or previous transit of a planet over a specified
     * longitude, latitude, distance or speed value with geocentric or topocentric
     * positions in a tropical or sidereal zodiac. Dates are interpreted as UT
     * (=ET&nbsp;-&nbsp;deltaT).<p>
     *
     * @param tc The TransitCalculator that should be used here.
     * @param jdUT The date (and time) in UT, from where to start searching.
     * @param backwards If backward search should be performed.
     * @return return A double containing the julian day number for the next /
     * previous transit as UT.
     * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
     * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
     */
    public double getTransitUT(
                               TransitCalculator tc,
                               double jdUT,
                               boolean backwards)
        throws IllegalArgumentException, SwissephException {
        if (ext==null) { ext=new Extensions(this); }
        double jdET = ext.getTransit(
                                     tc,
                                     jdUT + SweDate.getDeltaT(jdUT),
                                     backwards,
                                     (backwards?-Double.MAX_VALUE:Double.MAX_VALUE));
        return jdET - SweDate.getDeltaT(jdET);
    }
    /**
     * Searches for the next or previous transit of a planet over a specified
     * longitude, latitude, distance or speed value with geocentric or topocentric
     * positions in a tropical or sidereal zodiac. Dates are interpreted as UT
     * (=ET&nbsp;-&nbsp;deltaT).<p>
     *
     * @param tc The TransitCalculator that should be used here.
     * @param jdUT The date (and time) in UT, from where to start searching.
     * @param backwards If backward search should be performed.
     * @param jdLimit This is the date, when the search for transits should be
     * stopped, even if no transit point had been found up to then. It is
     * interpreted as UT time as well.
     * @return return A double containing the julian day number for the next /
     * previous transit as UT.
     * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
     * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
     */
    public double getTransitUT(
                               TransitCalculator tc,
                               double jdUT,
                               boolean backwards,
                               double jdLimit)
        throws IllegalArgumentException, SwissephException {
        if (ext==null) { ext=new Extensions(this); }
        double jdET = ext.getTransit(
                                     tc,
                                     jdUT + SweDate.getDeltaT(jdUT),
                                     backwards,
                                     jdLimit + SweDate.getDeltaT(jdLimit));
        return jdET - SweDate.getDeltaT(jdET);
    }
    //////////////////////////////////////////////////////////////////////////////
    // End of public methods /////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    private int swe_calc_error(double[] xx) {
        for (int i = 0; i < xx.length; i++) {
            xx[i] = 0;
        }
        return SweConst.ERR;
    }


    int swecalc(double tjd, int ipl, int iflag, double[] x, StringBuffer serr) 
        throws SwissephException {
        int i;
        int ipli, ipli_ast, ifno;
        int retc;
        int epheflag = SweConst.SEFLG_DEFAULTEPH;
        PlanData pdp;
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        PlanData ndp;
        double xp[], xp2[];
        double ss[]=new double[3];
        String serr2="";

        if (serr!=null) { serr.setLength(0); }
        /******************************************
         * iflag plausible?                       *
         ******************************************/
        iflag = plaus_iflag(iflag);
        /******************************************
         * which ephemeris is wanted, which is used?
         * Three ephemerides are possible: MOSEPH, SWIEPH, JPLEPH.
         * JPLEPH is best, SWIEPH is nearly as good, MOSEPH is least precise.
         * The availability of the various ephemerides depends on the installed
         * ephemeris files in the users ephemeris directory. This can change at
         * any time.
         * Swisseph should try to fulfil the wish of the user for a specific
         * ephemeris, but use a less precise one if the desired ephemeris is not
         * available for the given date and body.
         * If internal ephemeris errors are detected (data error, file length error)
         * an error is returned.
         * If the time range is bad but another ephemeris can deliver this range,
         * the other ephemeris is used.
         * If no ephemeris is specified, DEFAULTEPH is assumed as desired.
         * DEFAULTEPH is defined at compile time, usually as JPLEPH.
         * The caller learns from the return flag which ephemeris was used.
         * ephe_flag is extracted from iflag, but can change later if the
         * desired ephe is not available.
         ******************************************/
        if ((iflag & SweConst.SEFLG_MOSEPH)!=0) {
            epheflag = SweConst.SEFLG_MOSEPH;
        }
        if ((iflag & SweConst.SEFLG_SWIEPH)!=0) {
            epheflag = SweConst.SEFLG_SWIEPH;
        }
        if ((iflag & SweConst.SEFLG_JPLEPH)!=0) {
            epheflag = SweConst.SEFLG_JPLEPH;
        }
        /* no barycentric calculations with Moshier ephemeris */
        if (((iflag & SweConst.SEFLG_BARYCTR)!=0) &&
            ((iflag & SweConst.SEFLG_MOSEPH)!=0)) {
            if (serr != null) {
                serr.append("barycentric Moshier positions are not supported.");
            }
            throw new SwissephException(tjd, SwissephException.INVALID_PARAMETER_COMBINATION,
                                        SweConst.ERR, serr);
        }
        if (epheflag != SweConst.SEFLG_MOSEPH && !swed.ephe_path_is_set) {
            swe_set_ephe_path(null);
        }
        if ((iflag & SweConst.SEFLG_SIDEREAL)!=0 && !swed.ayana_is_set) {
            swe_set_sid_mode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
        }
        /******************************************
         * obliquity of ecliptic 2000 and of date *
         ******************************************/
        swi_check_ecliptic(tjd);
        /******************************************
         * nutation                               *
         ******************************************/
        swi_check_nutation(tjd, iflag);
        /******************************************
         * select planet and ephemeris            *
         *                                        *
         * ecliptic and nutation                  *
         ******************************************/
        if (ipl == SweConst.SE_ECL_NUT) {
            x[0] = swed.oec.eps + swed.nut.nutlo[1];	/* true ecliptic */
            x[1] = swed.oec.eps;			/* mean ecliptic */
            x[2] = swed.nut.nutlo[0];		/* nutation in longitude */
            x[3] = swed.nut.nutlo[1];		/* nutation in obliquity */
            /*if ((iflag & SweConst.SEFLG_RADIANS) == 0)*/
            for (i = 0; i <= 3; i++)
                x[i] *= SwissData.RADTODEG;
            return(iflag);
            /******************************************
             * moon                                   *
             ******************************************/
        } else if (ipl == SweConst.SE_MOON) {
            /* internal planet number */
            ipli = SwephData.SEI_MOON;
            pdp = swed.pldat[ipli];
            xp = pdp.xreturn;
            switch(epheflag) {
                case SweConst.SEFLG_JPLEPH:
                    retc = jplplan(tjd, ipli, iflag, SwephData.DO_SAVE, null,null,null, serr);
                    /* read error or corrupt file */
                    if (retc == SweConst.ERR) {
                        swecalc_error(x);
                        throw new SwissephException(tjd, SwissephException.DATA_FILE_ERROR,
                                                    SweConst.ERR, serr);
                    }
                    /* jpl ephemeris not on disk or date beyond ephemeris range
                     *     or file corrupt */
                    if (retc == SwephData.NOT_AVAILABLE) {
                        iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append(" \ntrying Swiss Eph; ");
                        }
                        retc =  sweph_moon(tjd, ipli, iflag, serr);
                        if (retc == SweConst.ERR) {
                            return swecalc_error(x);
                        }
                    } else if (retc == SwephData.BEYOND_EPH_LIMITS) {
                        if (tjd > SwephData.MOSHLUEPH_START &&
                            tjd < SwephData.MOSHLUEPH_END) {
                            iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
                            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                                serr.append(" \nusing Moshier Eph; ");
                            }
                            //              goto moshier_moon;
                            retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
                            if (retc == SweConst.ERR) {
                                return swecalc_error(x);
                            }
                        } else
                            return swecalc_error(x);
                    }
                    break;
                case SweConst.SEFLG_SWIEPH:
                    retc =  sweph_moon(tjd, ipli,iflag, serr);
                    if (retc == SweConst.ERR) {
                        return swecalc_error(x);
                    }
                    break;
                case SweConst.SEFLG_MOSEPH:
                    //          moshier_moon:
                    retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
                    if (retc == SweConst.ERR) {
                        return swecalc_error(x);
                    }
                    break;
                default:
                    break;
            }
            /* heliocentric, lighttime etc. */
            if ((retc = app_pos_etc_moon(iflag, serr))!=SweConst.OK) {
                return swecalc_error(x); // retc may be wrong with sidereal calculation
            }
            /**********************************************
             * barycentric sun                            *
             * (only JPL and SWISSEPH ephemerises)        *
             **********************************************/
        } else if (ipl == SweConst.SE_SUN &&
                   ((iflag & SweConst.SEFLG_BARYCTR)!=0)) {
            /* barycentric sun must be handled separately because of
             * the following reasons:
             * ordinary planetary computations use the function
             * main_planet() and its subfunction jplplan(),
             * see further below.
             * now, these functions need the swisseph internal
             * planetary indices, where SEI_EARTH = SEI_SUN = 0.
             * therefore they don't know the difference between
             * a barycentric sun and a barycentric earth and
             * always return barycentric earth.
             * to avoid this problem, many functions would have to
             * be changed. as an alternative, we choose a more
             * separate handling. */
            ipli = SwephData.SEI_SUN;	/* = SEI_EARTH ! */
            xp = pedp.xreturn;

            switch (epheflag) {
                case SweConst.SEFLG_JPLEPH:
                    /* open ephemeris, if still closed */
                    if (!swed.jpl_file_is_open) {
                        retc = sj.swi_open_jpl_file(ss, swed.jplfnam, swed.ephepath, serr);
                        swed.jpldenum = sj.swi_get_jpl_denum();
                        if (retc != SweConst.OK) {
                            retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
                        }
                        if (retc == SweConst.ERR) {
                            return swecalc_error(x);
                        }
                        swed.jpl_file_is_open = true;
                    }
                    retc = sj.swi_pleph(tjd, SwephJPL.J_SUN, SwephJPL.J_SBARY, psdp.x, serr);
                    if (retc == SweConst.ERR || retc == SwephData.BEYOND_EPH_LIMITS) {
                        sj.swi_close_jpl_file();
                        swed.jpl_file_is_open = false;
                        return swecalc_error(x);
                    }
                    /* jpl ephemeris not on disk or date beyond ephemeris range
                     *     or file corrupt */
                    if (retc == SwephData.NOT_AVAILABLE) {
                        iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append(" \ntrying Swiss Eph; ");
                        }
                        retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
                        if (retc == SweConst.ERR) {
                            return swecalc_error(x);
                        }
                    }
                    psdp.teval = tjd;
                    break;
                case SweConst.SEFLG_SWIEPH:
                    retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
                    if (retc == SweConst.ERR) {
                        return swecalc_error(x);
                    }
                    break;
                default:
                    return SweConst.ERR;
            }
            /* flags */
            if ((retc = app_pos_etc_sbar(iflag, serr)) != SweConst.OK) {
                return swecalc_error(x);
            }
            /* iflag has possibly changed */
            iflag = pedp.xflgs;
            /* barycentric sun is now in save area of barycentric earth.
             * (pedp->xreturn = swed.pldat[SEI_EARTH].xreturn).
             * in case a barycentric earth computation follows for the same
             * date, the planetary functions will return the barycentric
             * SUN unless we force a new computation of pedp->xreturn.
             * this can be done by initializing the save of iflag.
             */
            pedp.xflgs = -1;
            /******************************************
             * mercury - pluto                        *
             ******************************************/
        } else if (ipl == SweConst.SE_SUN 	/* main planet */
                   || ipl == SweConst.SE_MERCURY
                   || ipl == SweConst.SE_VENUS
                   || ipl == SweConst.SE_MARS
                   || ipl == SweConst.SE_JUPITER
                   || ipl == SweConst.SE_SATURN
                   || ipl == SweConst.SE_URANUS
                   || ipl == SweConst.SE_NEPTUNE
                   || ipl == SweConst.SE_PLUTO
                   || ipl == SweConst.SE_EARTH) {
            if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
                if (ipl == SweConst.SE_SUN) {
                    /* heliocentric position of Sun does not exist */
                    for (i = 0; i < 24; i++) {
                        x[i] = 0;
                    }
                    return iflag;
                }
            } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
                // NOOP
            } else {		/* geocentric */
                if (ipl == SweConst.SE_EARTH) {
                    /* geocentric position of Earth does not exist */
                    for (i = 0; i < 24; i++) {
                        x[i] = 0;
                    }
                    return iflag;
                }
            }
            /* internal planet number */
            ipli = SwissData.pnoext2int[ipl];
            pdp = swed.pldat[ipli];
            xp = pdp.xreturn;
            retc = main_planet(tjd, ipli, epheflag, iflag, serr);
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /* iflag has possibly changed in main_planet() */
            iflag = pdp.xflgs;
            /**********************************************
             * mean lunar node                            *
             * for comment s. moshmoon.c, swi_mean_node() *
             **********************************************/
        } else if (ipl == SweConst.SE_MEAN_NODE) {
            if (((iflag & SweConst.SEFLG_HELCTR)!=0) ||
                ((iflag & SweConst.SEFLG_BARYCTR)!=0)) {
                /* heliocentric/barycentric lunar node not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_MEAN_NODE];
            xp = ndp.xreturn;
            xp2 = ndp.x;
            retc = sm.swi_mean_node(tjd, xp2, serr);
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /* speed (is almost constant; variation < 0.001 arcsec) */
            retc = sm.swi_mean_node(tjd - SwephData.MEAN_NODE_SPEED_INTV, xp2, 3, serr);
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            xp2[3] = sl.swe_difrad2n(xp2[0], xp2[3]) / SwephData.MEAN_NODE_SPEED_INTV;
            xp2[4] = xp2[5] = 0;
            ndp.teval = tjd;
            ndp.xflgs = -1;
            /* lighttime etc. */
            retc = app_pos_etc_mean(SwephData.SEI_MEAN_NODE, iflag, serr);
            if (retc != SweConst.OK) {
                return swecalc_error(x);
            }
            /* to avoid infinitesimal deviations from latitude = 0
             * that result from conversions */
            if ((iflag & SweConst.SEFLG_SIDEREAL)==0 &&
                (iflag & SweConst.SEFLG_J2000)==0) {
                ndp.xreturn[1] = 0.0;	/* ecl. latitude       */
                ndp.xreturn[4] = 0.0;	/*               speed */
                ndp.xreturn[5] = 0.0;	/*      radial   speed */
                ndp.xreturn[8] = 0.0;	/* z coordinate        */
                ndp.xreturn[11] = 0.0;	/*               speed */
            }
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /**********************************************
             * mean lunar apogee ('dark moon', 'lilith')  *
             * for comment s. moshmoon.c, swi_mean_apog() *
             **********************************************/
        } else if (ipl == SweConst.SE_MEAN_APOG) {
            if (((iflag & SweConst.SEFLG_HELCTR)!=0) ||
                ((iflag & SweConst.SEFLG_BARYCTR)!=0)) {
                /* heliocentric/barycentric lunar apogee not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_MEAN_APOG];
            xp = ndp.xreturn;
            xp2 = ndp.x;
            retc = sm.swi_mean_apog(tjd, xp2, serr);
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /* speed (is not constant! variation ~= several arcsec) */
            retc = sm.swi_mean_apog(tjd - SwephData.MEAN_NODE_SPEED_INTV, xp2, 3, serr);
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            for(i = 0; i <= 1; i++) {
                xp2[3+i] = sl.swe_difrad2n(xp2[i], xp2[3+i]) / SwephData.MEAN_NODE_SPEED_INTV;
            }
            xp2[5] = 0;
            ndp.teval = tjd;
            ndp.xflgs = -1;
            /* lighttime etc. */
            if ((retc = app_pos_etc_mean(SwephData.SEI_MEAN_APOG, iflag, serr)) !=
                SweConst.OK) {
                return swecalc_error(x);
            }
            /* to avoid infinitesimal deviations from r-speed = 0
             * that result from conversions */
            ndp.xreturn[5] = 0.0;	/*               speed */
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /***********************************************
             * osculating lunar node ('true node')         *
             ***********************************************/
        } else if (ipl == SweConst.SE_TRUE_NODE) {
            if (((iflag & SweConst.SEFLG_HELCTR)!=0) ||
                ((iflag & SweConst.SEFLG_BARYCTR)!=0)) {
                /* heliocentric/barycentric lunar node not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_TRUE_NODE];
            xp = ndp.xreturn;
            retc = lunar_osc_elem(tjd, SwephData.SEI_TRUE_NODE, iflag, serr);
            iflag = ndp.xflgs;
            /* to avoid infinitesimal deviations from latitude = 0
             * that result from conversions */
            if ((iflag & SweConst.SEFLG_SIDEREAL)==0 &&
                (iflag & SweConst.SEFLG_J2000)==0) {
                ndp.xreturn[1] = 0.0;	/* ecl. latitude       */
                ndp.xreturn[4] = 0.0;	/*               speed */
                ndp.xreturn[8] = 0.0;	/* z coordinate        */
                ndp.xreturn[11] = 0.0;	/*               speed */
            }
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /***********************************************
             * osculating lunar apogee                     *
             ***********************************************/
        } else if (ipl == SweConst.SE_OSCU_APOG) {
            if (((iflag & SweConst.SEFLG_HELCTR)!=0) ||
                ((iflag & SweConst.SEFLG_BARYCTR)!=0)) {
                /* heliocentric/barycentric lunar apogee not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_OSCU_APOG];
            xp = ndp.xreturn;
            retc = lunar_osc_elem(tjd, SwephData.SEI_OSCU_APOG, iflag, serr);
            iflag = ndp.xflgs;
            if (retc == SweConst.ERR) {
                return swecalc_error(x);
            }
            /***********************************************
             * interpolated lunar apogee                   *    
             ***********************************************/
        } else if (ipl == SweConst.SE_INTP_APOG) {
            if ((iflag & SweConst.SEFLG_HELCTR)!=0 ||
                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                /* heliocentric/barycentric lunar apogee not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_INTP_APOG];
            xp = ndp.xreturn;
            retc = intp_apsides(tjd, SwephData.SEI_INTP_APOG, iflag, serr); 
            iflag = ndp.xflgs;
            if (retc == SweConst.ERR)
                return swecalc_error(x);
            /*********************************************** 
             * interpolated lunar perigee                  *    
             ***********************************************/
        } else if (ipl == SweConst.SE_INTP_PERG) {
            if ((iflag & SweConst.SEFLG_HELCTR)!=0 ||
                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                /* heliocentric/barycentric lunar apogee not allowed */
                for (i = 0; i < 24; i++) {
                    x[i] = 0;
                }
                return iflag;
            }
            ndp = swed.nddat[SwephData.SEI_INTP_PERG];
            xp = ndp.xreturn;
            retc = intp_apsides(tjd, SwephData.SEI_INTP_PERG, iflag, serr); 
            iflag = ndp.xflgs;
            if (retc == SweConst.ERR)
                return swecalc_error(x);
            /*********************************************** 
             * minor planets                               *
             ***********************************************/
        } else if (ipl == SweConst.SE_CHIRON
                   || ipl == SweConst.SE_PHOLUS
                   || ipl == SweConst.SE_CERES		/* Ceres - Vesta */
                   || ipl == SweConst.SE_PALLAS
                   || ipl == SweConst.SE_JUNO
                   || ipl == SweConst.SE_VESTA
                   || ipl > SweConst.SE_AST_OFFSET) {
            /* internal planet number */
            if (ipl < SweConst.SE_NPLANETS) {
                ipli = SwissData.pnoext2int[ipl];
            } else if (ipl <= SweConst.SE_AST_OFFSET + SwephData.MPC_VESTA) {
                ipli = SwephData.SEI_CERES + ipl - SweConst.SE_AST_OFFSET - 1;
                ipl = SweConst.SE_CERES + ipl - SweConst.SE_AST_OFFSET - 1;
            } else {		/* any asteroid except*/
                ipli = SwephData.SEI_ANYBODY;
            }
            if (ipli == SwephData.SEI_ANYBODY) {
                ipli_ast = ipl;
            } else {
                ipli_ast = ipli;
            }
            pdp = swed.pldat[ipli];
            xp = pdp.xreturn;
            if (ipli_ast > SweConst.SE_AST_OFFSET) {
                ifno = SwephData.SEI_FILE_ANY_AST;
            } else {
                ifno = SwephData.SEI_FILE_MAIN_AST;
            }
            if (ipli == SwephData.SEI_CHIRON &&
                (tjd < SwephData.CHIRON_START || tjd > SwephData.CHIRON_END)) {
                if (serr != null) {
                    serr.setLength(0);
                    serr.append("Chiron's ephemeris is restricted to JD "+
                                SwephData.CHIRON_START+" - JD "+
                                SwephData.CHIRON_END);
                }
                return SweConst.ERR;
            }
            if (ipli == SwephData.SEI_PHOLUS && tjd < SwephData.PHOLUS_START) {
                if (serr != null) {
                    serr.setLength(0);
                    serr.append("Pholus's ephemeris is restricted to the time after JD "+
                                SwephData.PHOLUS_START);
                }
                return SweConst.ERR;
            }
            //  do_asteroid:
            while (true) {
                /* earth and sun are also needed */
                retc = main_planet(tjd, SwephData.SEI_EARTH, epheflag, iflag, serr);
                if (retc == SweConst.ERR) {
                    return swecalc_error(x);
                }
                /* iflag (ephemeris bit) has possibly changed in main_planet() */
                iflag = swed.pldat[SwephData.SEI_EARTH].xflgs;
                /* asteroid */
                if (serr != null) {
                    serr2=serr.toString();
                    serr.setLength(0);
                }
                /* asteroid */
                retc = sweph(tjd, ipli_ast, ifno, iflag, psdp.x, SwephData.DO_SAVE,
                             null, serr);
                if (retc == SweConst.ERR || retc == SwephData.NOT_AVAILABLE) {
                    return swecalc_error(x);
                }
                retc = app_pos_etc_plan(ipli_ast, iflag, serr);
                if (retc == SweConst.ERR) {
                    return swecalc_error(x);
                }
                /* app_pos_etc_plan() might have failed, if t(light-time)
                 * is beyond ephemeris range. in this case redo with Moshier
                 */
                if (retc == SwephData.NOT_AVAILABLE ||
                    retc == SwephData.BEYOND_EPH_LIMITS) {
                    if (epheflag != SweConst.SEFLG_MOSEPH) {
                        iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | SweConst.SEFLG_MOSEPH;
                        epheflag = SweConst.SEFLG_MOSEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append("\nusing Moshier eph.; ");
                        }
                        //          goto do_asteroid;
                        continue;
                    } else
                        return swecalc_error(x);
                }
                break;
            }
            /* add warnings from earth/sun computation */
            if (serr != null && serr.length()==0 && serr2.length()!=0) {
                serr.setLength(0);
                serr2=serr2.substring(0,Math.min(serr2.length(),SwissData.AS_MAXCH-5));
                serr.append("sun: "+serr2);
            }
            /***********************************************
             * fictitious planets                          *
             * (Isis-Transpluto and Uranian planets)       *
             ***********************************************/
            // JAVA: Geht nur mit Moshier Routinen???
        } else if (ipl >= SweConst.SE_FICT_OFFSET && ipl <= SweConst.SE_FICT_MAX) {
            /* internal planet number */
            ipli = SwephData.SEI_ANYBODY;
            pdp = swed.pldat[ipli];
            xp = pdp.xreturn;
            //  do_fict_plan:
            while (true) {
                /* the earth for geocentric position */
                retc = main_planet(tjd, SwephData.SEI_EARTH, epheflag, iflag, serr);
                /* iflag (ephemeris bit) has possibly changed in main_planet() */
                iflag = swed.pldat[SwephData.SEI_EARTH].xflgs;
                /* planet from osculating elements */
                if (smosh.swi_osc_el_plan(tjd, pdp.x, ipl-SweConst.SE_FICT_OFFSET,
                                          ipli, pedp.x, psdp.x, serr) != SweConst.OK) {
                    return swecalc_error(x);
                }
                if (retc == SweConst.ERR) {
                    return swecalc_error(x);
                }
                retc = app_pos_etc_plan_osc(ipl, ipli, iflag, serr);
                if (retc == SweConst.ERR) {
                    return swecalc_error(x);
                }
                /* app_pos_etc_plan_osc() might have failed, if t(light-time)
                 * is beyond ephemeris range. in this case redo with Moshier
                 */
                if (retc == SwephData.NOT_AVAILABLE ||
                    retc == SwephData.BEYOND_EPH_LIMITS) {
                    if (epheflag != SweConst.SEFLG_MOSEPH) {
                        iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | SweConst.SEFLG_MOSEPH;
                        epheflag = SweConst.SEFLG_MOSEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append("\nusing Moshier eph.; ");
                        }
                        //        goto do_fict_plan;
                        continue;
                    } else
                        return swecalc_error(x);
                }
                break;
            }
            /***********************************************
             * invalid body number                         *
             ***********************************************/
        } else {
            if (serr != null) {
                serr.setLength(0);
                serr.append("illegal planet number "+ipl+".");
            }
            return swecalc_error(x);
        }
        for (i = 0; i < 24; i++) {
            x[i] = xp[i];
        }
        return(iflag);
    }

    int moshier_moon(double tjd, boolean do_save, double[] xpmret,
                     StringBuffer serr) {
        int retc = sm.swi_moshmoon(tjd, do_save, null, serr);/**/
        if (retc == SweConst.ERR) {
            return SweConst.ERR;
        }
        /* for hel. position, we need earth as well */
        retc = smosh.swi_moshplan(tjd, SwephData.SEI_EARTH, do_save, null, null, serr);/**/
        if (retc == SweConst.ERR) {
            return SweConst.ERR;
        }
        return SweConst.OK;
    }

    int swecalc_error(double x[]) {
        /***********************************************
         * return error                                *
         ***********************************************/
        //  return_error:;
        for (int i = 0; i < 24; i++) {
            x[i] = 0.;
        }
        return SweConst.ERR;
    }

    int sweph_sbar(double tjd, int iflag, PlanData psdp, PlanData pedp,
                   StringBuffer serr) {
        int retc;
        /* sweplan() provides barycentric sun as a by-product in save area;
         * it is saved in swed.pldat[SEI_SUNBARY].x */
        retc = sweplan(tjd, SwephData.SEI_EARTH, SwephData.SEI_FILE_PLANET, iflag,
                       SwephData.DO_SAVE, null, null, null, null, serr);
        if (retc == SweConst.ERR || retc == SwephData.NOT_AVAILABLE) {
            return SweConst.ERR;
        }
        psdp.teval = tjd;
        /* pedp.teval = tjd; */
        return SweConst.OK;
    }

    int sweph_moon(double tjd, int ipli, int iflag, StringBuffer serr) {
        int retc;
        retc = sweplan(tjd, ipli, SwephData.SEI_FILE_MOON, iflag, SwephData.DO_SAVE,
                       null, null, null, null, serr);
        if (retc == SweConst.ERR) {
            return SweConst.ERR;
        }
        /* if sweph file not found, switch to moshier */
        if (retc == SwephData.NOT_AVAILABLE) {
            if (tjd > SwephData.MOSHLUEPH_START && tjd < SwephData.MOSHLUEPH_END) {
                iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
                if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                    serr.append(" \nusing Moshier eph.; ");
                }
                //        goto moshier_moon;
                retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
                if (retc == SweConst.ERR) {
                    return SweConst.ERR;
                }
            } else
                return SweConst.ERR;
        }
        return SweConst.OK;
    }

    /* calculates obliquity of ecliptic and stores it together
     * with its date, sine, and cosine
     */
    void calc_epsilon(double tjd, Epsilon e) {
        e.teps = tjd;
        e.eps = sl.swi_epsiln(tjd);
        e.seps = Math.sin(e.eps);
        e.ceps = Math.cos(e.eps);
    }

    /* computes a main planet from any ephemeris, if it
     * has not yet been computed for this date.
     * since a geocentric position requires the earth, the
     * earth's position will be computed as well. With SWISSEPH
     * files the barycentric sun will be done as well.
     * With Moshier, the moon will be done as well.
     *
     * tjd          = julian day
     * ipli         = body number
     * epheflag     = which ephemeris? JPL, SWISSEPH, Moshier?
     * iflag        = other flags
     *
     * the geocentric apparent position of ipli (or whatever has
     * been specified in iflag) will be saved in
     * &swed.pldat[ipli].xreturn[];
     *
     * the barycentric (heliocentric with Moshier) position J2000
     * will be kept in
     * &swed.pldat[ipli].x[];
     */
    int main_planet(double tjd, int ipli, int epheflag, int iflag,
                    StringBuffer serr) {
        int retc;
        boolean calc_swieph=false;
        boolean calc_moshier=false;
        if (epheflag == SweConst.SEFLG_JPLEPH) {
            retc = jplplan(tjd, ipli, iflag, SwephData.DO_SAVE,
                           null, null, null,serr);
            /* read error or corrupt file */
            if (retc == SweConst.ERR) {
                return SweConst.ERR;
            }
            /* jpl ephemeris not on disk or date beyond ephemeris range */
            if (retc == SwephData.NOT_AVAILABLE) {
                iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                    serr.append(" \ntrying Swiss Eph; ");
                }
                calc_swieph=true;
                //        goto sweph_planet;
            } else if (retc == SwephData.BEYOND_EPH_LIMITS) {
                if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END) {
                    iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
                    if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                        serr.append(" \nusing Moshier Eph; ");
                    }
                    calc_moshier=true;
                    //          goto moshier_planet;
                } else {
                    return SweConst.ERR;
                }
            }
            if (!calc_swieph && !calc_moshier) {
                /* geocentric, lighttime etc. */
                if (ipli == SwephData.SEI_SUN) {
                    retc = app_pos_etc_sun(iflag, serr)/**/;
                } else {
                    retc = app_pos_etc_plan(ipli, iflag, serr);
                }
                if (retc == SweConst.ERR) {
                    return SweConst.ERR;
                }
                /* t for light-time beyond ephemeris range */
                if (retc == SwephData.NOT_AVAILABLE) {
                    iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                    if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                        serr.append(" \ntrying Swiss Eph; ");
                    }
                    calc_swieph=true;
                    //          goto sweph_planet;
                } else if (retc == SwephData.BEYOND_EPH_LIMITS) {
                    if (tjd > SwephData.MOSHPLEPH_START &&
                        tjd < SwephData.MOSHPLEPH_END) {
                        iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append(" \nusing Moshier Eph; ");
                        }
                        calc_moshier=true;
                        //            goto moshier_planet;
                    } else {
                        return SweConst.ERR;
                    }
                }
            }
        } // SweConst.SEFLG_JPLEPH
        if (epheflag == SweConst.SEFLG_SWIEPH || calc_swieph) {
            //      sweph_planet:
            /* compute barycentric planet (+ earth, sun, moon) */
            retc = sweplan(tjd, ipli, SwephData.SEI_FILE_PLANET, iflag, SwephData.DO_SAVE,
                           null, null, null, null, serr);
            if (retc == SweConst.ERR) {
                return SweConst.ERR;
            }
            /* if sweph file not found, switch to moshier */
            if (retc == SwephData.NOT_AVAILABLE) {
                if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END) {
                    iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
                    if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                        serr.append(" \nusing Moshier eph.; ");
                    }
                    calc_moshier=true;
                    //          goto moshier_planet;
                } else {
                    return SweConst.ERR;
                }
            }
            if (!calc_moshier) {
                /* geocentric, lighttime etc. */
                if (ipli == SwephData.SEI_SUN) {
                    retc = app_pos_etc_sun(iflag, serr)/**/;
                } else {
                    retc = app_pos_etc_plan(ipli, iflag, serr);
                }
                if (retc == SweConst.ERR) {
                    return SweConst.ERR;
                }
                /* if sweph file for t(lighttime) not found, switch to moshier */
                if (retc == SwephData.NOT_AVAILABLE) {
                    if (tjd > SwephData.MOSHPLEPH_START &&
                        tjd < SwephData.MOSHPLEPH_END) {
                        iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
                        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                            serr.append(" \nusing Moshier eph.; ");
                        }
                        calc_moshier=true;
                        //          goto moshier_planet;
                    } else {
                        return SweConst.ERR;
                    }
                }
            } // SweConst.SEFLG_SWIEPH
        } // !calc_moshier
        if (epheflag == SweConst.SEFLG_MOSEPH || calc_moshier) {
            //      moshier_planet:
            retc = smosh.swi_moshplan(tjd, ipli, SwephData.DO_SAVE, null, null, serr);/**/
            if (retc == SweConst.ERR) {
                return SweConst.ERR;
            }
            /* geocentric, lighttime etc. */
            if (ipli == SwephData.SEI_SUN) {
                retc = app_pos_etc_sun(iflag, serr)/**/;
            } else {
                retc = app_pos_etc_plan(ipli, iflag, serr);
            }
            if (retc == SweConst.ERR) {
                return SweConst.ERR;
            }
        }
        return SweConst.OK;
    }

    /* Computes a main planet from any ephemeris or returns
     * it again, if it has been computed before.
     * In barycentric equatorial position of the J2000 equinox.
     * The earth's position is computed as well. With SWISSEPH
     * and JPL ephemeris the barycentric sun is computed, too.
     * With Moshier, the moon is returned, as well.
     *
     * tjd          = julian day
     * ipli         = body number
     * epheflag     = which ephemeris? JPL, SWISSEPH, Moshier?
     * iflag        = other flags
     * xp, xe, xs, and xm are the pointers, where the program
     * either finds or stores (if not found) the barycentric
     * (heliocentric with Moshier) positions of the following
     * bodies:
     * xp           planet
     * xe           earth
     * xs           sun
     * xm           moon
     *
     * xm is used with Moshier only
     */
    int main_planet_bary(double tjd, int ipli, int epheflag, int iflag,
                         boolean do_save,
                         double xp[], double xe[], double xs[], double xm[],
                         StringBuffer serr) {
        int i;
        int retc;
        boolean calc_moshier=false;
        boolean calc_swieph=false;
        if (epheflag == SweConst.SEFLG_JPLEPH) {
            retc = jplplan(tjd, ipli, iflag, do_save, xp, xe, xs, serr);
            /* read error or corrupt file */
            if (retc == SweConst.ERR || retc == SwephData.BEYOND_EPH_LIMITS) {
                return retc;
            }
            /* jpl ephemeris not on disk or date beyond ephemeris range */
            if (retc == SwephData.NOT_AVAILABLE) {
                iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                    serr.append(" \ntrying Swiss Eph; ");
                }
                calc_swieph=true;
                //        goto sweph_planet;
            }
        }
        if (epheflag == SweConst.SEFLG_SWIEPH || calc_swieph) {
            //      sweph_planet:
            /* compute barycentric planet (+ earth, sun, moon) */
            retc = sweplan(tjd, ipli, SwephData.SEI_FILE_PLANET, iflag, do_save,
                           xp, xe, xs, xm, serr);
            if (retc == SweConst.ERR || retc == SwephData.NOT_AVAILABLE) {
                return retc;
            }
        }
        if (epheflag == SweConst.SEFLG_MOSEPH || calc_moshier) {
            retc = smosh.swi_moshplan(tjd, ipli, do_save, xp, xe, serr);/**/
            if (retc == SweConst.ERR) {
                return SweConst.ERR;
            }
            for (i = 0; i <= 5; i++) {
                xs[i] = 0;
            }
        }
        return SweConst.OK;
    }

    /* SWISSEPH
     * this routine computes heliocentric cartesian equatorial coordinates
     * of equinox 2000 of
     * geocentric moon
     *
     * tjd          julian date
     * iflag        flag
     * do_save      save J2000 position in save area pdp->x ?
     * xp           array of 6 doubles for lunar position and speed
     * serr         error string
     */
    int swemoon(double tjd, int iflag, boolean do_save, double xpret[],
                StringBuffer serr) {
        int i, retc;
        PlanData pdp = swed.pldat[SwephData.SEI_MOON];
        int speedf1, speedf2;
        double xx[]=new double[6], xp[];
        if (do_save) {
            xp = pdp.x;
        }
        else
            xp = xx;
        /* if planet has already been computed for this date, return
         * if speed flag has been turned on, recompute planet */
        speedf1 = pdp.xflgs & SweConst.SEFLG_SPEED;
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        if (tjd == pdp.teval
            && pdp.iephe == SweConst.SEFLG_SWIEPH
            && ((speedf2==0) || (speedf1!=0))) {
            xp = pdp.x;
        } else {
            /* call sweph for moon */
            retc = sweph(tjd, SwephData.SEI_MOON, SwephData.SEI_FILE_MOON, iflag,
                         null, do_save, xp, serr);
            if (retc != SweConst.OK) {
                return(retc);
            }
            if (do_save) {
                pdp.teval = tjd;
                pdp.xflgs = -1;
                pdp.iephe = SweConst.SEFLG_SWIEPH;
            }
        }
        if (xpret != null) {
            for (i = 0; i <= 5; i++) {
                xpret[i] = xp[i];
            }
        }
        return SweConst.OK;
    }

    /* SWISSEPH
     * this function computes
     * 1. a barycentric planet
     * plus, under certain conditions,
     * 2. the barycentric sun,
     * 3. the barycentric earth, and
     * 4. the geocentric moon,
     * in barycentric cartesian equatorial coordinates J2000.
     *
     * these are the data needed for calculation of light-time etc.
     *
     * tjd          julian date
     * ipli         SEI_ planet number
     * ifno         ephemeris file number
     * do_save      write new positions in save area
     * xp           array of 6 doubles for planet's position and velocity
     * xpe                                 earth's
     * xps                                 sun's
     * xpm                                 moon's
     * serr         error string
     *
     * xp - xpm can be NULL. if do_save is TRUE, all of them can be NULL.
     * the positions will be written into the save area (swed.pldat[ipli].x)
     */
    int sweplan(double tjd, int ipli, int ifno, int iflag, boolean do_save,
                double xpret[], double xperet[], double xpsret[],
                double xpmret[], StringBuffer serr) {
        int i, retc;
        boolean do_earth = false, do_moon = false, do_sunbary = false;
        PlanData pdp = swed.pldat[ipli];
        PlanData pebdp = swed.pldat[SwephData.SEI_EMB];
        PlanData psbdp = swed.pldat[SwephData.SEI_SUNBARY];
        PlanData pmdp = swed.pldat[SwephData.SEI_MOON];
        double xxp[]=new double[6], xxm[]=new double[6],
            xxs[]=new double[6], xxe[]=new double[6];
        double xp[], xpe[], xpm[], xps[];
        int speedf1, speedf2;
        /* xps (barycentric sun) may be necessary because some planets on sweph
         * file are heliocentric, other ones are barycentric. without xps,
         * the heliocentric ones cannot be returned barycentrically.
         */
        if (do_save || ipli == SwephData.SEI_SUNBARY
            || (pdp.iflg & SwephData.SEI_FLG_HELIO)!=0
            || xpsret != null || (iflag & SweConst.SEFLG_HELCTR)!=0) {
            do_sunbary = true;
        }
        if (do_save || ipli == SwephData.SEI_EARTH || xperet != null) {
            do_earth = true;
        }
        if (ipli == SwephData.SEI_MOON) {
            do_earth = true;
            do_sunbary = true;
        }
        if (do_save || ipli == SwephData.SEI_MOON || ipli == SwephData.SEI_EARTH ||
            xperet != null || xpmret != null) {
            do_moon = true;
        }
        if (do_save) {
            xp = pdp.x;
            xpe = pebdp.x;
            xps = psbdp.x;
            xpm = pmdp.x;
        } else {
            xp = xxp;
            xpe = xxe;
            xps = xxs;
            xpm = xxm;
        }
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        /* barycentric sun */
        if (do_sunbary) {
            speedf1 = psbdp.xflgs & SweConst.SEFLG_SPEED;
            /* if planet has already been computed for this date, return
             * if speed flag has been turned on, recompute planet */
            if (tjd == psbdp.teval
                && psbdp.iephe == SweConst.SEFLG_SWIEPH
                && ((speedf2==0) || (speedf1!=0))) {
                for (i = 0; i <= 5; i++) {
                    xps[i] = psbdp.x[i];
                }
            } else {
                retc = sweph(tjd, SwephData.SEI_SUNBARY, SwephData.SEI_FILE_PLANET, iflag,
                             null, do_save, xps, serr);/**/
                if (retc != SweConst.OK) {
                    return(retc);
                }
            }
            if (xpsret != null) {
                for (i = 0; i <= 5; i++) {
                    xpsret[i] = xps[i];
                }
            }
        }
        /* moon */
        if (do_moon) {
            speedf1 = pmdp.xflgs & SweConst.SEFLG_SPEED;
            if (tjd == pmdp.teval
                && pmdp.iephe == SweConst.SEFLG_SWIEPH
                && ((speedf2==0) || (speedf1!=0))) {
                for (i = 0; i <= 5; i++) {
                    xpm[i] = pmdp.x[i];
                }
            } else {
                retc = sweph(tjd, SwephData.SEI_MOON, SwephData.SEI_FILE_MOON, iflag, null,
                             do_save, xpm, serr);
                if (retc == SweConst.ERR) {
                    return(retc);
                }
                /* if moon file doesn't exist, take moshier moon */
                if (swed.fidat[SwephData.SEI_FILE_MOON].fptr == null) {
                    if (serr != null && serr.length() + 35 < SwissData.AS_MAXCH) {
                        serr.append(" \nusing Moshier eph. for moon; ");
                    }
                    retc = sm.swi_moshmoon(tjd, do_save, xpm, serr);
                    if (retc != SweConst.OK) {
                        return(retc);
                    }
                }
            }
            if (xpmret != null) {
                for (i = 0; i <= 5; i++) {
                    xpmret[i] = xpm[i];
                }
            }
        }
        /* barycentric earth */
        if (do_earth) {
            speedf1 = pebdp.xflgs & SweConst.SEFLG_SPEED;
            if (tjd == pebdp.teval
                && pebdp.iephe == SweConst.SEFLG_SWIEPH
                && ((speedf2==0) || (speedf1!=0))) {
                for (i = 0; i <= 5; i++) {
                    xpe[i] = pebdp.x[i];
                }
            } else {
                retc = sweph(tjd, SwephData.SEI_EMB, SwephData.SEI_FILE_PLANET, iflag, null,
                             do_save, xpe, serr);
                if (retc != SweConst.OK) {
                    return(retc);
                }
                /* earth from emb and moon */
                embofs(xpe, 0, xpm, 0);
                /* speed is needed, if
                 * 1. true position is being computed before applying light-time etc.
                 *    this is the position saved in pdp->x.
                 *    in this case, speed is needed for light-time correction.
                 * 2. the speed flag has been specified.
                 */
                if (xpe == pebdp.x || ((iflag & SweConst.SEFLG_SPEED)!=0)) {
                    embofs(xpe, 3, xpm, 3);
                }
            }
            if (xperet != null) {
                for (i = 0; i <= 5; i++) {
                    xperet[i] = xpe[i];
                }
            }
        }
        if (ipli == SwephData.SEI_MOON) {
            for (i = 0; i <= 5; i++) {
                xp[i] = xpm[i];
            }
        } else if (ipli == SwephData.SEI_EARTH) {
            for (i = 0; i <= 5; i++) {
                xp[i] = xpe[i];
            }
        } else if (ipli == SwephData.SEI_SUN) {
            for (i = 0; i <= 5; i++) {
                xp[i] = xps[i];
            }
        } else {
            /* planet */
            speedf1 = pdp.xflgs & SweConst.SEFLG_SPEED;
            if (tjd == pdp.teval
                && pdp.iephe == SweConst.SEFLG_SWIEPH
                && ((speedf2==0) || (speedf1!=0))) {
                for (i = 0; i <= 5; i++) {
                    xp[i] = pdp.x[i];
                }
                return(SweConst.OK);
            } else {
                retc = sweph(tjd, ipli, ifno, iflag, null, do_save, xp, serr);
                if (retc != SweConst.OK) {
                    return(retc);
                }
                /* if planet is heliocentric, it must be transformed to barycentric */
                if ((pdp.iflg & SwephData.SEI_FLG_HELIO)!=0) {
                    /* now barycentric planet */
                    for (i = 0; i <= 2; i++) {
                        xp[i] += xps[i];
                    }
                    if (do_save || ((iflag & SweConst.SEFLG_SPEED)!=0)) {
                        for (i = 3; i <= 5; i++) {
                            xp[i] += xps[i];
                        }
                    }
                }
            }
        }
        if (xpret != null) {
            for (i = 0; i <= 5; i++) {
                xpret[i] = xp[i];
            }
        }
        return SweConst.OK;
    }

    /* jpl ephemeris.
     * this function computes
     * 1. a barycentric planet position
     * plus, under certain conditions,
     * 2. the barycentric sun,
     * 3. the barycentric earth,
     * in barycentric cartesian equatorial coordinates J2000.
  
     * tjd          julian day
     * ipli         sweph internal planet number
     * do_save      write new positions in save area
     * xp           array of 6 doubles for planet's position and speed vectors
     * xpe                                 earth's
     * xps                                 sun's
     * serr         pointer to error string
     *
     * xp - xps can be NULL. if do_save is TRUE, all of them can be NULL.
     * the positions will be written into the save area (swed.pldat[ipli].x)
     */
    int jplplan(double tjd, int ipli, int iflag, boolean do_save,
                double xpret[], double xperet[], double xpsret[],
                StringBuffer serr) throws SwissephException {
        int i, retc;
        boolean do_earth = false, do_sunbary = false;
        double ss[]=new double[3];
        double xxp[]=new double[6], xxe[]=new double[6], xxs[]=new double[6];
        double xp[], xpe[], xps[];
        int ictr = SwephJPL.J_SBARY;
        PlanData pdp = swed.pldat[ipli];
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        /* we assume Teph ~= TDB ~= TT. The maximum error is < 0.002 sec, 
         * corresponding to an ephemeris error < 0.001 arcsec for the moon */
        /* double tjd_tdb, T;
           T = (tjd - 2451545.0)/36525.0;
           tjd_tdb = tjd + (0.001657 * sin(628.3076 * T + 6.2401)
           + 0.000022 * sin(575.3385 * T + 4.2970)
           + 0.000014 * sin(1256.6152 * T + 6.1969)) / 8640.0;*/
        if (do_save) {
            xp = pdp.x;
            xpe = pedp.x;
            xps = psdp.x;
        } else {
            xp = xxp;
            xpe = xxe;
            xps = xxs;
        }
        if (do_save || ipli == SwephData.SEI_EARTH || xperet != null
            || (ipli == SwephData.SEI_MOON)) {
            /* && (iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR |
               SweConst.SEFLG_NOABERR))!=0)) */
            do_earth = true;
        }
        if (do_save || ipli == SwephData.SEI_SUNBARY || xpsret != null
            || (ipli == SwephData.SEI_MOON)) {
            /* && (iflag & (SEFLG_HELCTR | SEFLG_NOABERR)))) */
            do_sunbary = true;
        }
        if (ipli == SwephData.SEI_MOON) {
            ictr = SwephJPL.J_EARTH;
        }
        /* open ephemeris, if still closed */
        if (!swed.jpl_file_is_open) {
            retc = sj.swi_open_jpl_file(ss, swed.jplfnam, swed.ephepath, serr);
            swed.jpldenum = sj.swi_get_jpl_denum();
            if (retc != SweConst.OK) {
                throw new SwissephException(tjd, SwissephException.FILE_OPEN_FAILED,
                                            retc, serr);
            }
            swed.jpl_file_is_open = true;
        }
        if (do_earth) {
            /* barycentric earth */
            if (tjd != pedp.teval || tjd == 0) {
                retc = sj.swi_pleph(tjd, SwephJPL.J_EARTH, SwephJPL.J_SBARY, xpe, serr);
                if (retc != SweConst.OK) {
                    sj.swi_close_jpl_file();
                    swed.jpl_file_is_open = false;
                    return retc;
                }
                if (do_save) {
                    pedp.teval = tjd;
                    pedp.xflgs = -1;       /* new light-time etc. required */
                    pedp.iephe = SweConst.SEFLG_JPLEPH;
                }
            } else {
                xpe = pedp.x;
            }
            if (xperet != null) {
                for (i = 0; i <= 5; i++) {
                    xperet[i] = xpe[i];
                }
            }
  
        }
        if (do_sunbary) {
            /* barycentric sun */
            if (tjd != psdp.teval || tjd == 0) {
                retc = sj.swi_pleph(tjd, SwephJPL.J_SUN, SwephJPL.J_SBARY, xps, serr);
                if (retc != SweConst.OK) {
                    sj.swi_close_jpl_file();
                    swed.jpl_file_is_open = false;
                    return retc;
                }
                if (do_save) {
                    psdp.teval = tjd;
                    psdp.xflgs = -1;
                    psdp.iephe = SweConst.SEFLG_JPLEPH;
                }
            } else {
                xps = psdp.x;
            }
            if (xpsret != null) {
                for (i = 0; i <= 5; i++) {
                    xpsret[i] = xps[i];
                }
            }
        }
        /* earth is wanted */
        if (ipli == SwephData.SEI_EARTH) {
            for (i = 0; i <= 5; i++) {
                xp[i] = xpe[i];
            }
            /* sunbary is wanted */
        } if (ipli == SwephData.SEI_SUNBARY) {
            for (i = 0; i <= 5; i++) {
                xp[i] = xps[i];
            }
            /* other planet */
        } else {
            /* if planet already computed */
            if (tjd == pdp.teval && pdp.iephe == SweConst.SEFLG_JPLEPH) {
                xp = pdp.x;
            } else {
                retc = sj.swi_pleph(tjd, SwephData.pnoint2jpl[ipli], ictr, xp, serr);
                if (retc != SweConst.OK) {
                    sj.swi_close_jpl_file();
                    swed.jpl_file_is_open = false;
                    return retc;
                }
                if (do_save) {
                    pdp.teval = tjd;
                    pdp.xflgs = -1;
                    pdp.iephe = SweConst.SEFLG_JPLEPH;
                }
            }
        }
        if (xpret != null) {
            for (i = 0; i <= 5; i++) {
                xpret[i] = xp[i];
            }
        }
        return (SweConst.OK);
    }

    /*
     * this function looks for an ephemeris file,
     * opens it, if not yet open,
     * reads constants, if not yet read,
     * computes a planet, if not yet computed
     * attention: asteroids are heliocentric
     *            other planets barycentric
     *
     * tjd          julian date
     * ipli         SEI_ planet number
     * ifno         ephemeris file number
     * xsunb        INPUT (!) array of 6 doubles containing barycentric sun
     *              (must be given with asteroids)
     * do_save      boolean: save result in save area
     * xp           return array of 6 doubles for planet's position
     * serr         error string
     */
    int sweph(double tjd, int ipli, int ifno, int iflag, double xsunb[],
              boolean do_save, double xpret[], StringBuffer serr) {
        int i, ipl, retc, subdirlen;
        String s="", subdirnam, fname;
        double t, tsv;
        double xemb[]=new double[6], xx[]=new double[6], xp[];
        PlanData pdp;
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        FileData fdp = swed.fidat[ifno];
        int speedf1, speedf2;
        boolean need_speed;
        ipl = ipli;
        if (ipli > SweConst.SE_AST_OFFSET) {
            ipl = SwephData.SEI_ANYBODY;
        }
        pdp = swed.pldat[ipl];
        if (do_save) {
            xp = pdp.x;
        } else {
            xp = xx;
        }
        /* if planet has already been computed for this date, return.
         * if speed flag has been turned on, recompute planet */
        speedf1 = pdp.xflgs & SweConst.SEFLG_SPEED;
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        if (tjd == pdp.teval
            && pdp.iephe == SweConst.SEFLG_SWIEPH
            && ((speedf2==0) || (speedf1!=0))
            && ipl < SwephData.SEI_ANYBODY) {
            if (xpret != null) {
                for (i = 0; i <= 5; i++) {
                    xpret[i] = pdp.x[i];
                }
            }
            return SweConst.OK;
        }
        /******************************
         * get correct ephemeris file *
         ******************************/
        if (fdp.fptr != null) {
            /* if tjd is beyond file range, close old file.
             * if new asteroid, close old file. */
            if (tjd < fdp.tfstart || tjd > fdp.tfend
                || (ipl == SwephData.SEI_ANYBODY && ipli != pdp.ibdy)) {
                try {
                    fdp.fptr.close();
                } catch (java.io.IOException e) {
                    // NBT
                }
                fdp.fptr = null;
                if (pdp.refep != null) {
                    pdp.refep = null;
                }
                if (pdp.segp != null) {
                    pdp.segp = null;
                }
            }
        }
        /* if sweph file not open, find and open it */
        if (fdp.fptr == null) {
            fname=sl.swi_gen_filename(new SweDate(tjd), ipli);
            subdirnam=fname;
            if (subdirnam.lastIndexOf(swed.DIR_GLUE)>0) {
                subdirnam=subdirnam.substring(0,subdirnam.indexOf(swed.DIR_GLUE));
                subdirlen=subdirnam.length();
            } else {
                subdirlen=0;
            }
            s=fname;

            while (fdp.fptr==null) {
                try {
                    fdp.fptr=swi_fopen(ifno,s,swed.ephepath, serr);
                } catch (SwissephException se) {
                }
                if (fdp.fptr == null ||
                    (fdp.fptr!=null && fdp.fptr.fp==null && fdp.fptr.sk==null)) {
                    /*
                     * if it is a numbered asteroid file, try also for short files (..s.se1)
                     * On the second try, the inserted 's' will be seen and not tried again.
                     */
                    if (ipli > SweConst.SE_AST_OFFSET) {
                        if (s.indexOf("s.")<=0) {
                            s=s.substring(0,s.indexOf("."))+"s."+SwephData.SE_FILE_SUFFIX;
                            continue;
                        } else {
                            /*
                             * if we still have 'ast0' etc. in front of the filename,
                             * we remove it now, remove the 's' also,
                             * and try in the main ephemeris directory instead of the
                             * asteroid subdirectory.
                             */
                            s=s.substring(0,s.indexOf("s."))+s.substring(s.indexOf("s.")+1);
                            if (subdirlen>0 &&
                                s.startsWith(subdirnam.substring(
                                                                 0,Math.min(subdirnam.length(),subdirlen)))) {
                                s=s.substring(subdirlen+1);
                                continue;
                            }
                        }
                    }
                    return(SwephData.NOT_AVAILABLE);
                }
                if (fdp.fptr!=null) { break; }
            }

            /* during the search error messages may have been built, delete them */
            if (serr != null) {
                serr.setLength(0);
            }
            retc = swed.fidat[ifno].read_const(ifno, serr, swed);
            if (retc != SweConst.OK) {
                return(retc);
            }
        }
        /* if first ephemeris file (J-3000), it might start a mars period
         * after -3000. if last ephemeris file (J3000), it might end a
         * 4000-day-period before 3000. */
        if (tjd < fdp.tfstart || tjd > fdp.tfend) {
            if (serr != null) {
                if (tjd < fdp.tfstart) {
                    s="jd "+tjd+" < Swiss Eph. lower limit "+fdp.tfstart+";";
                } else {
                    s="jd "+tjd+" > Swiss Eph. upper limit "+fdp.tfend+";";
                }
                if (serr.length()+s.length() < SwissData.AS_MAXCH) {
                    serr.append(s);
                }
            }
            return(SwephData.NOT_AVAILABLE);
        }
        /******************************
         * get planet's position
         ******************************/
        /* get new segment, if necessary */
        if (pdp.segp == null || tjd < pdp.tseg0 || tjd > pdp.tseg1) {
            retc = swed.fidat[ifno].get_new_segment(swed, tjd, ipl, ifno, serr);
            if (retc != SweConst.OK) {
                return(retc);
            }
            /* rotate cheby coeffs back to equatorial system.
             * if necessary, add reference orbit. */
            if ((pdp.iflg & SwephData.SEI_FLG_ROTATE)!=0) {
                rot_back(ipl); /**/
            } else {
                pdp.neval = pdp.ncoe;
            }
        }
        /* evaluate chebyshew polynomial for tjd */
        t = (tjd - pdp.tseg0) / pdp.dseg;
        t = t * 2 - 1;
        /* speed is needed, if
         * 1. true position is being computed before applying light-time etc.
         *    this is the position saved in pdp->x.
         *    in this case, speed is needed for light-time correction.
         * 2. the speed flag has been specified.
         */
        need_speed = (do_save || ((iflag & SweConst.SEFLG_SPEED)!=0));
        for (i = 0; i <= 2; i++) {
            xp[i]  = sl.swi_echeb (t, pdp.segp, i*pdp.ncoe, pdp.neval);
            if (need_speed) {
                xp[i+3] = sl.swi_edcheb(t, pdp.segp, i*pdp.ncoe, pdp.neval) / pdp.dseg * 2;
            } else
                xp[i+3] = 0;      /* von Alois als billiger fix, evtl. illegal */
        }
        /* if planet wanted is barycentric sun and must be computed
         * from heliocentric earth and barycentric earth: the
         * computation above gives heliocentric earth, therefore we
         * have to compute barycentric earth and subtract heliocentric
         * earth from it. this may be necessary with calls from
         * sweplan() and from app_pos_etc_sun() (light-time). */
        if (ipl == SwephData.SEI_SUNBARY &&
            (pdp.iflg & SwephData.SEI_FLG_EMBHEL)!=0) {
            /* sweph() calls sweph() !!! for EMB.
             * Attention: a new calculation must be forced in any case.
             * Otherwise EARTH (instead of EMB) will possibly taken from
             * save area.
             * to force new computation, set pedp->teval = 0 and restore it
             * after call of sweph(EMB).
             */
            tsv = pedp.teval;
            pedp.teval = 0;
            retc = sweph(tjd, SwephData.SEI_EMB, ifno, iflag | SweConst.SEFLG_SPEED,
                         null, SwephData.NO_SAVE, xemb, serr);
            if (retc != SweConst.OK) {
                return(retc);
            }
            pedp.teval = tsv;
            for (i = 0; i <= 2; i++) {
                xp[i] = xemb[i] - xp[i];
            }
            if (need_speed) {
                for (i = 3; i <= 5; i++) {
                    xp[i] = xemb[i] - xp[i];
                }
            }
        }
        /* asteroids are heliocentric.
         * if JPL or SWISSEPH, convert to barycentric */
        if ((iflag & SweConst.SEFLG_JPLEPH)!=0 ||
            (iflag & SweConst.SEFLG_SWIEPH)!=0) {
            if (ipl >= SwephData.SEI_ANYBODY) {
                for (i = 0; i <= 2; i++) {
                    xp[i] += xsunb[i];
                }
                if (need_speed) {
                    for (i = 3; i <= 5; i++) {
                        xp[i] += xsunb[i];
                    }
                }
            }
        }
        if (do_save) {
            pdp.teval = tjd;
            pdp.xflgs = -1;    /* do new computation of light-time etc. */
            if (ifno == SwephData.SEI_FILE_PLANET ||
                ifno == SwephData.SEI_FILE_MOON) {
                pdp.iephe = SweConst.SEFLG_SWIEPH;/**/
            } else {
                pdp.iephe = psdp.iephe;
            }
        }
        if (xpret != null) {
            for (i = 0; i <= 5; i++) {
                xpret[i] = xp[i];
            }
        }
        return SweConst.OK;
    }

    /*
     * Alois 2.12.98: inserted error message generation for file not found
     */
    FilePtr swi_fopen(int ifno, String fname, String ephepath,
                      StringBuffer serr) throws SwissephException {
        int np, i;
        java.io.RandomAccessFile fp = null;
        String fnamp;
        String[] cpos=new String[20];
        String path, s, s1;
        //s1=ephepath;
        s1=getTmp();
        
        np = sl.swi_cutstr(s1, SwissData.PATH_SEPARATOR, cpos, 20);
        for (i = 0; i < np; i++) {
            path=cpos[i];
            if ("".equals(path)) { path="."; }
            fnamp=path+swed.DIR_GLUE;

            if (fnamp.length() + fname.length() < SwissData.AS_MAXCH) {
                fnamp+=fname;
            } else {
                if (serr != null) {
                    serr.setLength(0);
                    serr.append("error: file path and name must be shorter than "+
                                SwissData.AS_MAXCH+".");
                }
                throw new SwissephException(1./0., SwissephException.INVALID_FILE_NAME,
                                            SweConst.ERR, serr);
            }
            try {          
                fp = new java.io.RandomAccessFile(fnamp, SwissData.BFILE_R_ACCESS);
                if (ifno >= 0) {
                    swed.fidat[ifno].fnam=fnamp;
                }
                FilePtr sfp = new FilePtr(fp,null,null,null,fnamp,-1,httpBufSize);
                return sfp;
            } catch (java.io.IOException ex) {
                // Maybe it is an URL...
                FilePtr f=tryFileAsURL(path+"/"+fname, ifno);
                if (f!=null) {
                    return f;
                }
            } catch (SecurityException ex) {
                // Probably an applet, we try fnamp as an URL:
                FilePtr f=tryFileAsURL(path+"/"+fname, ifno);
                if (f!=null) {
                    return f;
                }
            }
        }
//         //    s="SwissEph file '"+fname+"' not found in PATH '"+ephepath+"'";
//         s="SwissEph file '"+fname+"' not found in the paths of: ";
//         for (int n=0;n<cpos.length;n++) {
//             if (cpos[n]!=null && !"".equals(cpos[n])) { s+="'"+cpos[n]+"', "; }
//         }
//         /* s may be longer then AS_MAXCH */
//         // Who cares...
//         //    s=s.substring(0,Math.min(s.length(),SwissData.AS_MAXCH));
//         if (serr != null) {
//             serr.setLength(0);
//             serr.append(s);
//         }
//         ////#ifdef TRACE0
//         //    Trace.level--;
//         ////#endif /* TRACE0 */
//         throw new SwissephException(1./0., SwissephException.FILE_NOT_FOUND,
//                                     SwephData.NOT_AVAILABLE, serr);
        return null;
    }

    private FilePtr tryFileAsURL(String fnamp, int ifno) {
        ////#ifdef TRACE0
        //    Trace.level++;
        //    Trace.trace(Trace.level, "SwissEph.tryFileAsURL(String, int)");
        ////#ifdef TRACE1
        //    DevNull.println("    fnamp: " + fnamp + "\n    ifno: " + ifno);
        ////#endif /* TRACE1 */
        ////#endif /* TRACE0 */
        Socket sk=null;
        try {
            URL u=new URL(fnamp);
            sk=new Socket(u.getHost(),(u.getPort()<0?80:u.getPort()));
            String sht="HEAD "+fnamp+" HTTP/1.1\r\n"+
                "User-Agent: "+FilePtr.useragent+"\r\n"+
                "Host: "+u.getHost()+":"+(u.getPort()<0?80:u.getPort())+
                "\r\n\r\n";
            sk.setSoTimeout(5000);
            InputStream is=sk.getInputStream();
            BufferedOutputStream os=new BufferedOutputStream(sk.getOutputStream());
            for(int n=0; n<sht.length(); n++) {
                os.write((byte)sht.charAt(n));
            }
            os.flush();
            String sret=""+(char)is.read();
            while (is.available()>0) {
                sret+=(char)is.read();
            }
            int idx=sret.indexOf("Content-Length:");
            if (idx < 0) {
                sk.close();
                ////#ifdef TRACE0
                //        Trace.level--;
                ////#endif /* TRACE0 */
                return null;
            }
            // We need to query ranges, otherwise it will not make much sense...
            if (sret.indexOf("Accept-Ranges: none")>=0) {
                System.err.println("Server does not accept HTTP range requests. "+
                                   "Aborting!");
                sk.close();
                ////#ifdef TRACE0
                //        Trace.level--;
                ////#endif /* TRACE0 */
                return null;
            }
            sret=sret.substring(idx+"Content-Length:".length());
            sret=sret.substring(0,sret.indexOf("\n")).trim();
            // We might want to check for a minimum length?
            long len=Long.parseLong(sret);
            if (ifno >= 0) {
                swed.fidat[ifno].fnam=fnamp;
            }
            ////#ifdef TRACE0
            //      Trace.level--;
            ////#endif /* TRACE0 */
            return new FilePtr(null,sk,is,os,fnamp,len,httpBufSize);
        } catch (MalformedURLException m) {
        } catch (IOException ie) {
        } catch (NumberFormatException nf) {
            // Why this? Should not be able to happen...
        } catch (SecurityException se) {
        }
        try { sk.close(); }
        catch (IOException e) { }
        catch (NullPointerException np) { }
        ////#ifdef TRACE0
        //    Trace.level--;
        ////#endif /* TRACE0 */
        return null;
    }

    /* converts planets from barycentric to geocentric,
     * apparent positions
     * precession and nutation
     * according to flags
     * ipli         planet number
     * iflag        flags
     * serr         error string
     */
    int app_pos_etc_plan(int ipli, int iflag, StringBuffer serr) {
        int i, j, niter, retc = SweConst.OK;
        int ipl;
        int ifno, ibody;
        int flg1, flg2;
        double xx[]=new double[6], dx[]=new double[3], dt, t, dtsave_for_defl;
        double xobs[]=new double[6], xobs2[]=new double[6];
        double xearth[]=new double[6], xsun[]=new double[6];
        double xxsp[]=new double[6], xxsv[]=new double[6];
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData pdp;
        Epsilon oe = swed.oec2000;
        int epheflag = iflag & SweConst.SEFLG_EPHMASK;
        t = dtsave_for_defl = 0;      /* dummy assignment to silence gcc */
        /* ephemeris file */
        if (ipli > SweConst.SE_AST_OFFSET) {
            ifno = SwephData.SEI_FILE_ANY_AST;
            ibody = SwephData.IS_ANY_BODY;
            pdp = swed.pldat[SwephData.SEI_ANYBODY];
        } else if (ipli == SwephData.SEI_CHIRON
                   || ipli == SwephData.SEI_PHOLUS
                   || ipli == SwephData.SEI_CERES
                   || ipli == SwephData.SEI_PALLAS
                   || ipli == SwephData.SEI_JUNO
                   || ipli == SwephData.SEI_VESTA) {
            ifno = SwephData.SEI_FILE_MAIN_AST;
            ibody = SwephData.IS_MAIN_ASTEROID;
            pdp = swed.pldat[ipli];
        } else {
            ifno = SwephData.SEI_FILE_PLANET;
            ibody = SwephData.IS_PLANET;
            pdp = swed.pldat[ipli];
        }
        /* if the same conversions have already been done for the same
         * date, then return */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = pdp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        if (flg1 == flg2) {
            pdp.xflgs = iflag;
            pdp.iephe = iflag & SweConst.SEFLG_EPHMASK;
            return SweConst.OK;
        }
        /* the conversions will be done with xx[]. */
        for (i = 0; i <= 5; i++) {
            xx[i] = pdp.x[i];
        }
        /* if heliocentric position is wanted */
        if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
            if (pdp.iephe == SweConst.SEFLG_JPLEPH ||
                pdp.iephe == SweConst.SEFLG_SWIEPH) {
                for (i = 0; i <= 5; i++) {
                    xx[i] -= swed.pldat[SwephData.SEI_SUNBARY].x[i];
                }
            }
        }
        /************************************
         * observer: geocenter or topocenter
         ************************************/
        /* if topocentric position is wanted  */
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            if (swed.topd.teval != pedp.teval
                || pedp.teval == 0) {
                if (swi_get_observer(pedp.teval, iflag, SwephData.DO_SAVE, xobs, serr)
                    != SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = swed.topd.xobs[i];
                }
            }
            /* barycentric position of observer */
            for (i = 0; i <= 5; i++) {
                xobs[i] = xobs[i] + pedp.x[i];
            }
        } else {
            /* barycentric position of geocenter */
            for (i = 0; i <= 5; i++) {
                xobs[i] = pedp.x[i];
            }
        }
        /*******************************
         * light-time geocentric       *
         *******************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0) {
            /* number of iterations - 1 */
            if (pdp.iephe == SweConst.SEFLG_JPLEPH ||
                pdp.iephe == SweConst.SEFLG_SWIEPH) {
                niter = 1;
            } else {      /* SEFLG_MOSEPH or planet from osculating elements */
                niter = 0;
            }
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                /*
                 * Apparent speed is influenced by the fact that dt changes with
                 * motion. This makes a difference of several hundredths of an
                 * arc second. To take this into account, we compute
                 * 1. true position - apparent position at time t - 1.
                 * 2. true position - apparent position at time t.
                 * 3. the difference between the two is the part of the daily motion
                 * that results from the change of dt.
                 */
                for (i = 0; i <= 2; i++) {
                    xxsv[i] = xxsp[i] = xx[i] - xx[i+3];
                }
                for (j = 0; j <= niter; j++) {
                    for (i = 0; i <= 2; i++) {
                        dx[i] = xxsp[i];
                        if (((iflag & SweConst.SEFLG_HELCTR)==0) &&
                            (iflag & SweConst.SEFLG_BARYCTR)==0) {
                            dx[i] -= (xobs[i] - xobs[i+3]);
                        }
                    }
                    /* new dt */
                    dt = Math.sqrt(sl.square_sum(dx)) * SweConst.AUNIT / SwephData.CLIGHT /
                        86400.0;
                    for (i = 0; i <= 2; i++) {      /* rough apparent position at t-1 */
                        xxsp[i] = xxsv[i] - dt * pdp.x[i+3];
                    }
                }
                /* true position - apparent position at time t-1 */
                for (i = 0; i <= 2; i++) {
                    xxsp[i] = xxsv[i] - xxsp[i];
                }
            }
            /* dt and t(apparent) */
            for (j = 0; j <= niter; j++) {
                for (i = 0; i <= 2; i++) {
                    dx[i] = xx[i];
                    if ((iflag & SweConst.SEFLG_HELCTR)==0 &&
                        (iflag & SweConst.SEFLG_BARYCTR)==0) {
                        dx[i] -= xobs[i];
                    }
                }
                dt = Math.sqrt(sl.square_sum(dx)) *SweConst.AUNIT / SwephData.CLIGHT / 86400.0;
                /* new t */
                t = pdp.teval - dt;
                dtsave_for_defl = dt;
                for (i = 0; i <= 2; i++) {        /* rough apparent position at t*/
                    xx[i] = pdp.x[i] - dt * pdp.x[i+3];
                }
            }
            /* part of daily motion resulting from change of dt */
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                for (i = 0; i <= 2; i++) {
                    xxsp[i] = pdp.x[i] - xx[i] - xxsp[i];
                }
            }
            /* new position, accounting for light-time (accurate) */
            switch(epheflag) {
                case SweConst.SEFLG_JPLEPH:
                    if (ibody == SwephData.IS_PLANET) {
                        ipl = SwephData.pnoint2jpl[ipli];
                        retc = sj.swi_pleph(t, ipl, SwephJPL.J_SBARY, xx, serr);
                        if (retc != SweConst.OK) {
                            sj.swi_close_jpl_file();
                            swed.jpl_file_is_open = false;
                        }
                    } else {        /* asteroid */
                        /* first sun */
                        retc = sj.swi_pleph(t, SwephJPL.J_SUN, SwephJPL.J_SBARY, xsun, serr);
                        if (retc != SweConst.OK) {
                            sj.swi_close_jpl_file();
                            swed.jpl_file_is_open = false;
                        }
                        /* asteroid */
                        retc = sweph(t, ipli, ifno, iflag, xsun, SwephData.NO_SAVE, xx, serr);
                    }
                    if (retc != SweConst.OK) {
                        return(retc);
                    }
                    /* for accuracy in speed, we need earth as well */
                    if ((iflag & SweConst.SEFLG_SPEED)!=0
                        && (iflag & SweConst.SEFLG_HELCTR)==0
                        && (iflag & SweConst.SEFLG_BARYCTR)==0) {
                        retc = sj.swi_pleph(t, SwephJPL.J_EARTH, SwephJPL.J_SBARY, xearth, serr);
                        if (retc != SweConst.OK) {
                            sj.swi_close_jpl_file();
                            swed.jpl_file_is_open = false;
                            return(retc);
                        }
                    }
                    break;
                case SweConst.SEFLG_SWIEPH:
                    if (ibody == SwephData.IS_PLANET) {
                        retc = sweplan(t, ipli, ifno, iflag, SwephData.NO_SAVE, xx, xearth,
                                       xsun, null, serr);
                    } else {          /*asteroid*/
                        retc = sweplan(t, SwephData.SEI_EARTH, SwephData.SEI_FILE_PLANET,
                                       iflag, SwephData.NO_SAVE, xearth, null, xsun, null,
                                       serr);
                        if (retc == SweConst.OK) {
                            retc = sweph(t, ipli, ifno, iflag, xsun, SwephData.NO_SAVE, xx,
                                         serr);
                        }
                    }
                    if (retc != SweConst.OK) {
                        return(retc);
                    }
                    break;
                case SweConst.SEFLG_MOSEPH:
                default:
                    /*
                     * with moshier or other ephemerides, subtraction of dt * speed
                     * is sufficient (has been done in light-time iteration above)
                     */
                    /* if speed flag is true, we call swi_moshplan() for new t.
                     * this does not increase position precision,
                     * but speed precision, which becomes better than 0.01"/day.
                     * for precise speed, we need earth as well.
                     */
                    if ((iflag & SweConst.SEFLG_SPEED)!=0
                        && (iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR))==0) {
                        if (ibody == SwephData.IS_PLANET) {
                            retc = smosh.swi_moshplan(t, ipli, SwephData.NO_SAVE, xxsv,
                                                      xearth, serr);
                        } else {                /* if asteroid */
                            retc = sweph(t, ipli, ifno, iflag, null, SwephData.NO_SAVE, xxsv,
                                         serr);
                            if (retc == SweConst.OK) {
                                retc = smosh.swi_moshplan(t, SwephData.SEI_EARTH,
                                                          SwephData.NO_SAVE, xearth, xearth,
                                                          serr);
                            }
                        }
                        if (retc != SweConst.OK) {
                            return(retc);
                        }
                        /* only speed is taken from this computation, otherwise position
                         * calculations with and without speed would not agree. The difference
                         * would be about 0.01", which is far below the intrinsic error of the
                         * moshier ephemeris.
                         */
                        for (i = 3; i <= 5; i++) {
                            xx[i] = xxsv[i];
                        }
                    }
                    break;
            }
            if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
                if (pdp.iephe == SweConst.SEFLG_JPLEPH ||
                    pdp.iephe == SweConst.SEFLG_SWIEPH) {
                    for (i = 0; i <= 5; i++) {
                        xx[i] -= swed.pldat[SwephData.SEI_SUNBARY].x[i];
                    }
                }
            }
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                /* observer position for t(light-time) */
                if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
                    if (swi_get_observer(t, iflag, SwephData.NO_SAVE, xobs2, serr) !=
                        SweConst.OK) {
                        return SweConst.ERR;
                    }
                    for (i = 0; i <= 5; i++) {
                        xobs2[i] += xearth[i];
                    }
                } else {
                    for (i = 0; i <= 5; i++) {
                        xobs2[i] = xearth[i];
                    }
                }
            }
        }
        /*******************************
         * conversion to geocenter     *
         *******************************/
        if ((iflag & SweConst.SEFLG_HELCTR)==0 &&
            (iflag & SweConst.SEFLG_BARYCTR)==0) {
            /* subtract earth */
            for (i = 0; i <= 5; i++) {
                xx[i] -= xobs[i];
            }
            if ((iflag & SweConst.SEFLG_TRUEPOS) == 0 ) {
                /*
                 * Apparent speed is also influenced by
                 * the change of dt during motion.
                 * Neglect of this would result in an error of several 0.01"
                 */
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    for (i = 3; i <= 5; i++) {
                        xx[i] -= xxsp[i-3];
                    }
                }
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /************************************
         * relativistic deflection of light *
         ************************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOGDEFL)==0) {
            /* SEFLG_NOGDEFL is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_deflect_light(xx, 0, dtsave_for_defl, iflag);
        }
        /**********************************
         * 'annual' aberration of light   *
         **********************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOABERR)==0) {
            /* SEFLG_NOABERR is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_aberr_light(xx, xobs, iflag);
            /*
             * Apparent speed is also influenced by
             * the difference of speed of the earth between t and t-dt.
             * Neglecting this would involve an error of several 0.1"
             */
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                for (i = 3; i <= 5; i++) {
                    xx[i] += xobs[i] - xobs2[i];
                }
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED) == 0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS) == 0 && swed.jpldenum >= 403) {
            sl.swi_bias(xx, iflag, false);
        }/**/
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = xx[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        if ((iflag & SweConst.SEFLG_J2000)==0) {
            sl.swi_precess(xx, pdp.teval, SwephData.J2000_TO_J);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, pdp.teval, SwephData.J2000_TO_J);
            }
            oe = swed.oec;
        } else {
            oe = swed.oec2000;
        }
        return app_pos_rest(pdp, iflag, xx, xxsv, oe, serr);
    }

    int app_pos_rest(PlanData pdp, int iflag,
                     double[] xx, double[] x2000,
                     Epsilon oe, StringBuffer serr) {
        int i;
        /************************************************
         * nutation                                     *
         ************************************************/
        if ((iflag & SweConst.SEFLG_NONUT)==0) {
            swi_nutate(xx, 0, iflag, false);
        }
        /* now we have equatorial cartesian coordinates; save them */
        for (i = 0; i <= 5; i++) {
            pdp.xreturn[18+i] = xx[i];
        }
        /************************************************
         * transformation to ecliptic.                  *
         * with sidereal calc. this will be overwritten *
         * afterwards.                                  *
         ************************************************/
        sl.swi_coortrf2(xx, xx, oe.seps, oe.ceps);
        if ((iflag & SweConst.SEFLG_SPEED) !=0) {
            sl.swi_coortrf2(xx, 3, xx, 3, oe.seps, oe.ceps);
        }
        if ((iflag & SweConst.SEFLG_NONUT)==0) {
            sl.swi_coortrf2(xx, xx, swed.nut.snut, swed.nut.cnut);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                sl.swi_coortrf2(xx, 3, xx, 3, swed.nut.snut, swed.nut.cnut);
            }
        }
        /* now we have ecliptic cartesian coordinates */
        for (i = 0; i <= 5; i++) {
            pdp.xreturn[6+i] = xx[i];
        }
        /************************************
         * sidereal positions               *
         ************************************/
        if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
            /* project onto ecliptic t0 */
            if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0) {
                if (swi_trop_ra2sid_lon(x2000, pdp.xreturn, 6, pdp.xreturn, 18, iflag,
                                        serr) != SweConst.OK) {
                    return SweConst.ERR;
                }
                /* project onto solar system equator */
            } else if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
                if (swi_trop_ra2sid_lon_sosy(x2000, pdp.xreturn, 6, pdp.xreturn, 18,
                                             iflag, serr) != SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                /* traditional algorithm */
                sl.swi_cartpol_sp(pdp.xreturn, 6, pdp.xreturn, 0);
                pdp.xreturn[0] -= swe_get_ayanamsa(pdp.teval) * SwissData.DEGTORAD;
                sl.swi_polcart_sp(pdp.xreturn, 0, pdp.xreturn, 6);
            }
        }
        /************************************************
         * transformation to polar coordinates          *
         ************************************************/
        sl.swi_cartpol_sp(pdp.xreturn, 18, pdp.xreturn, 12);
        sl.swi_cartpol_sp(pdp.xreturn, 6, pdp.xreturn, 0);
        /**********************
         * radians to degrees *
         **********************/
        /*if ((iflag & SEFLG_RADIANS) == 0) {*/
        for (i = 0; i < 2; i++) {
            pdp.xreturn[i] *= SwissData.RADTODEG;                /* ecliptic */
            pdp.xreturn[i+3] *= SwissData.RADTODEG;
            pdp.xreturn[i+12] *= SwissData.RADTODEG;     /* equator */
            pdp.xreturn[i+15] *= SwissData.RADTODEG;
        }
        /*}*/
        /* save, what has been done */
        pdp.xflgs = iflag;
        pdp.iephe = iflag & SweConst.SEFLG_EPHMASK;
        return SweConst.OK;
    }

    /*
     * input coordinates are J2000, cartesian.
     * xout         ecliptical sidereal position
     * xoutr        equatorial sidereal position
     */
    int swi_trop_ra2sid_lon(double[] xin, double[] xout, double[] xoutr,
                            int iflag, StringBuffer serr) {
        return swi_trop_ra2sid_lon(xin, xout, 0, xoutr, 0, iflag, serr);
    }
    int swi_trop_ra2sid_lon(double[] xin, double[] xout, int xoOffs,
                            double[] xoutr, int xrOffs, int iflag,
                            StringBuffer serr) {
        double x[]=new double[6];
        int i;
        SidData sip = swed.sidd;
        Epsilon oectmp=new Epsilon();
        for (i = 0; i <= 5; i++) {
            x[i] = xin[i];
        }
        if (sip.t0 != SwephData.J2000) {
            sl.swi_precess(x, sip.t0, SwephData.J2000_TO_J);
            sl.swi_precess(x, 3, sip.t0, SwephData.J2000_TO_J);      /* speed */
        }
        for (i = 0; i <= 5; i++) {
            xoutr[i+xrOffs] = x[i];
        }
        calc_epsilon(swed.sidd.t0, oectmp);
        sl.swi_coortrf2(x, x, oectmp.seps, oectmp.ceps);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            sl.swi_coortrf2(x, 3, x, 3, oectmp.seps, oectmp.ceps);
        }
        /* to polar coordinates */
        sl.swi_cartpol_sp(x, x);
        /* subtract ayan_t0 */
        x[0] -= sip.ayan_t0 * SwissData.DEGTORAD;
        /* back to cartesian */
        sl.swi_polcart_sp(x, 0, xout, xoOffs);
        return SweConst.OK;
    }

    /*
     * input coordinates are J2000, cartesian.
     * xout         ecliptical sidereal position
     * xoutr        equatorial sidereal position
     */
    int swi_trop_ra2sid_lon_sosy(double[] xin, double[] xout, double[] xoutr,
                                 int iflag, StringBuffer serr) {
        return swi_trop_ra2sid_lon_sosy(xin, xout, 0, xoutr, 0, iflag, serr);
    }
    int swi_trop_ra2sid_lon_sosy(double[] xin, double[] xout, int xoOffs,
                                 double[] xoutr, int xrOffs, int iflag,
                                 StringBuffer serr) {
        double x[]=new double[6], x0[]=new double[6];
        int i;
        SidData sip = swed.sidd;
        Epsilon oe = swed.oec2000;
        double plane_node = SwephData.SSY_PLANE_NODE_E2000;
        double plane_incl = SwephData.SSY_PLANE_INCL;
        for (i = 0; i <= 5; i++) {
            x[i] = xin[i];
        }
        /* planet to ecliptic 2000 */
        sl.swi_coortrf2(x, x, oe.seps, oe.ceps);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            sl.swi_coortrf2(x, 3, x, 3, oe.seps, oe.ceps);
        }
        /* to polar coordinates */
        sl.swi_cartpol_sp(x, x);
        /* to solar system equator */
        x[0] -= plane_node;
        sl.swi_polcart_sp(x, x);
        sl.swi_coortrf(x, x, plane_incl);
        sl.swi_coortrf(x, 3, x, 3, plane_incl);
        sl.swi_cartpol_sp(x, x);
        /* zero point of t0 in J2000 system */
        x0[0] = 1;
        x0[1] = x0[2] = 0;
        if (sip.t0 != SwephData.J2000) {
            sl.swi_precess(x0, sip.t0, SwephData.J_TO_J2000);
        }
        /* zero point to ecliptic 2000 */
        sl.swi_coortrf2(x0, x0, oe.seps, oe.ceps);
        /* to polar coordinates */
        sl.swi_cartpol(x0, x0);
        /* to solar system equator */
        x0[0] -= plane_node;
        sl.swi_polcart(x0, x0);
        sl.swi_coortrf(x0, x0, plane_incl);
        sl.swi_cartpol(x0, x0);
        /* measure planet from zero point */
        x[0] -= x0[0];
        x[0] *= SwissData.RADTODEG;
        /* subtract ayan_t0 */
        x[0] -= sip.ayan_t0;
        x[0] = sl.swe_degnorm(x[0]) * SwissData.DEGTORAD;
        /* back to cartesian */
        sl.swi_polcart_sp(x, 0, xout, xoOffs);
        return SweConst.OK;
    }

    /* converts planets from barycentric to geocentric,
     * apparent positions
     * precession and nutation
     * according to flags
     * ipli         planet number
     * iflag        flags
     */
    int app_pos_etc_plan_osc(int ipl, int ipli, int iflag, StringBuffer serr) {
        int i, j, niter, retc;
        double xx[]=new double[6], dx[]=new double[3], dt, dtsave_for_defl;
        double xearth[]=new double[6], xsun[]=new double[6], xmoon[]=new double[6];
        double xxsv[]=new double[6], xxsp[]=new double[6],
            xobs[]=new double[6], xobs2[]=new double[6];
        double t;
        PlanData pdp = swed.pldat[ipli];
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        Epsilon oe = swed.oec2000;
        int epheflag = SweConst.SEFLG_DEFAULTEPH;
        dt = dtsave_for_defl = 0;     /* dummy assign to silence gcc */
        if ((iflag & SweConst.SEFLG_MOSEPH)!=0) {
            epheflag = SweConst.SEFLG_MOSEPH;
        } else if ((iflag & SweConst.SEFLG_SWIEPH)!=0) {
            epheflag = SweConst.SEFLG_SWIEPH;
        } else if ((iflag & SweConst.SEFLG_JPLEPH)!=0) {
            epheflag = SweConst.SEFLG_JPLEPH;
        }
        /* the conversions will be done with xx[]. */
        for (i = 0; i <= 5; i++) {
            xx[i] = pdp.x[i];
        }
        /************************************
         * barycentric position is required *
         ************************************/
        /* = heliocentric position with Moshier ephemeris */
        /************************************
         * observer: geocenter or topocenter
         ************************************/
        /* if topocentric position is wanted  */
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            if (swed.topd.teval != pedp.teval
                || swed.topd.teval != 0) {
                if (swi_get_observer(pedp.teval, iflag, SwephData.DO_SAVE, xobs, serr)
                    != SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = swed.topd.xobs[i];
                }
            }
            /* barycentric position of observer */
            for (i = 0; i <= 5; i++) {
                xobs[i] = xobs[i] + pedp.x[i];
            }
        } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
            for (i = 0; i <= 5; i++) {
                xobs[i] = 0;
            }
        } else if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
            if ((iflag & SweConst.SEFLG_MOSEPH)!=0) {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = 0;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = psdp.x[i];
                }
            }
        } else {
            for (i = 0; i <= 5; i++) {
                xobs[i] = pedp.x[i];
            }
        }
        /*******************************
         * light-time                  *
         *******************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0) {
            niter = 1;
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                /*
                 * Apparent speed is influenced by the fact that dt changes with
                 * motion. This makes a difference of several hundredths of an
                 * arc second. To take this into account, we compute
                 * 1. true position - apparent position at time t - 1.
                 * 2. true position - apparent position at time t.
                 * 3. the difference between the two is the daily motion resulting from
                 * the change of dt.
                 */
                for (i = 0; i <= 2; i++) {
                    xxsv[i] = xxsp[i] = xx[i] - xx[i+3];
                }
                for (j = 0; j <= niter; j++) {
                    for (i = 0; i <= 2; i++) {
                        dx[i] = xxsp[i];
                        if ((iflag & SweConst.SEFLG_HELCTR)==0 &&
                            (iflag & SweConst.SEFLG_BARYCTR)==0) {
                            dx[i] -= (xobs[i] - xobs[i+3]);
                        }
                    }
                    /* new dt */
                    dt = Math.sqrt(sl.square_sum(dx)) * SweConst.AUNIT / SwephData.CLIGHT /
                        86400.0;
                    for (i = 0; i <= 2; i++) {
                        xxsp[i] = xxsv[i] - dt * pdp.x[i+3];/* rough apparent position */
                    }
                }
                /* true position - apparent position at time t-1 */
                for (i = 0; i <= 2; i++) {
                    xxsp[i] = xxsv[i] - xxsp[i];
                }
            }
            /* dt and t(apparent) */
            for (j = 0; j <= niter; j++) {
                for (i = 0; i <= 2; i++) {
                    dx[i] = xx[i];
                    if ((iflag & SweConst.SEFLG_HELCTR)==0 &&
                        (iflag & SweConst.SEFLG_BARYCTR)==0) {
                        dx[i] -= xobs[i];
                    }
                }
                /* new dt */
                dt = Math.sqrt(sl.square_sum(dx)) *SweConst.AUNIT / SwephData.CLIGHT / 86400.0;
                dtsave_for_defl = dt;
                /* new position: subtract t * speed
                 */
                for (i = 0; i <= 2; i++) {
                    xx[i] = pdp.x[i] - dt * pdp.x[i+3];/**/
                    xx[i+3] = pdp.x[i+3];
                }
            }
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                /* part of daily motion resulting from change of dt */
                for (i = 0; i <= 2; i++) {
                    xxsp[i] = pdp.x[i] - xx[i] - xxsp[i];
                }
                t = pdp.teval - dt;
                /* for accuracy in speed, we will need earth as well */
                retc = main_planet_bary(t, SwephData.SEI_EARTH, epheflag, iflag,
                                        SwephData.NO_SAVE, xearth, xearth, xsun,
                                        xmoon, serr);
                if (smosh.swi_osc_el_plan(t, xx, ipl-SweConst.SE_FICT_OFFSET, ipli,
                                          xearth, xsun, serr) != SweConst.OK) {
                    return(SweConst.ERR);
                }
                if (retc != SweConst.OK) {
                    return(retc);
                }
                if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
                    if (swi_get_observer(t, iflag, SwephData.NO_SAVE, xobs2, serr) !=
                        SweConst.OK) {
                        return SweConst.ERR;
                    }
                    for (i = 0; i <= 5; i++) {
                        xobs2[i] += xearth[i];
                    }
                } else {
                    for (i = 0; i <= 5; i++) {
                        xobs2[i] = xearth[i];
                    }
                }
            }
        }
        /*******************************
         * conversion to geocenter     *
         *******************************/
        for (i = 0; i <= 5; i++) {
            xx[i] -= xobs[i];
        }
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0) {
            /*
             * Apparent speed is also influenced by
             * the change of dt during motion.
             * Neglect of this would result in an error of several 0.01"
             */
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                for (i = 3; i <= 5; i++) {
                    xx[i] -= xxsp[i-3];
                }
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /************************************
         * relativistic deflection of light *
         ************************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOGDEFL)==0) {
            /* SEFLG_NOGDEFL is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_deflect_light(xx, 0, dtsave_for_defl, iflag);
        }
        /**********************************
         * 'annual' aberration of light   *
         **********************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOABERR)==0) {
            /* SEFLG_NOABERR is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_aberr_light(xx, xobs, iflag);
            /*
             * Apparent speed is also influenced by
             * the difference of speed of the earth between t and t-dt.
             * Neglecting this would involve an error of several 0.1"
             */
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                for (i = 3; i <= 5; i++) {
                    xx[i] += xobs[i] - xobs2[i];
                }
            }
        }
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = xx[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        if ((iflag & SweConst.SEFLG_J2000)==0) {
            sl.swi_precess(xx, pdp.teval, SwephData.J2000_TO_J);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, pdp.teval, SwephData.J2000_TO_J);
            }
            oe = swed.oec;
        } else
            oe = swed.oec2000;
        return app_pos_rest(pdp, iflag, xx, xxsv, oe, serr);
    }

    /* influence of precession on speed
     * xx           position and speed of planet in equatorial cartesian
     *              coordinates */
    void swi_precess_speed(double xx[], double t, int direction) {
        swi_precess_speed(xx, 0, t, direction);
    }
    void swi_precess_speed(double xx[], int xOffs, double t, int direction) {
        Epsilon oe;
        double fac;
        double tprec = (t - SwephData.J2000) / 36525.0;
        if (direction == SwephData.J2000_TO_J) {
            fac = 1;
            oe = swed.oec;
        } else {
            fac = -1;
            oe = swed.oec2000;
        }
        /* first correct rotation.
         * this costs some sines and cosines, but neglect might
         * involve an error > 1"/day */
        sl.swi_precess(xx, 3+xOffs, t, direction);
        /* then add 0.137"/day */
        sl.swi_coortrf2(xx, xOffs, xx, xOffs, oe.seps, oe.ceps);
        sl.swi_coortrf2(xx, 3+xOffs, xx, 3+xOffs, oe.seps, oe.ceps);
        sl.swi_cartpol_sp(xx, xOffs, xx, xOffs);
        xx[3+xOffs] += (50.290966 + 0.0222226 * tprec) /
            3600 / 365.25 * SwissData.DEGTORAD * fac;
        /* formula from Montenbruck, German 1994, p. 18 */
        sl.swi_polcart_sp(xx, xOffs, xx, xOffs);
        sl.swi_coortrf2(xx, xOffs, xx, xOffs, -oe.seps, oe.ceps);
        sl.swi_coortrf2(xx, 3+xOffs, xx, 3+xOffs, -oe.seps, oe.ceps);
    }

    /* multiplies cartesian equatorial coordinates with previously
     * calculated nutation matrix. also corrects speed.
     */
    void swi_nutate(double xx[], int offs, int iflag, boolean backward) {
        int i;
        double x[]=new double[6], xv[]=new double[6];
        for (i = 0; i <= 2; i++) {
            if (backward) {
                x[i] = xx[0+offs] * swed.nut.matrix[i][0] +
                    xx[1+offs] * swed.nut.matrix[i][1] +
                    xx[2+offs] * swed.nut.matrix[i][2];
            } else {
                x[i] = xx[0+offs] * swed.nut.matrix[0][i] +
                    xx[1+offs] * swed.nut.matrix[1][i] +
                    xx[2+offs] * swed.nut.matrix[2][i];
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            /* correct speed:
             * first correct rotation */
            for (i = 0; i <= 2; i++) {
                if (backward) {
                    x[i+3] = xx[3+offs] * swed.nut.matrix[i][0] +
                        xx[4+offs] * swed.nut.matrix[i][1] +
                        xx[5+offs] * swed.nut.matrix[i][2];
                } else {
                    x[i+3] = xx[3+offs] * swed.nut.matrix[0][i] +
                        xx[4+offs] * swed.nut.matrix[1][i] +
                        xx[5+offs] * swed.nut.matrix[2][i];
                }
            }
            /* then apparent motion due to change of nutation during day.
             * this makes a difference of 0.01" */
            for (i = 0; i <= 2; i++) {
                if (backward) {
                    xv[i] = xx[0+offs] * swed.nutv.matrix[i][0] +
                        xx[1+offs] * swed.nutv.matrix[i][1] +
                        xx[2+offs] * swed.nutv.matrix[i][2];
                } else {
                    xv[i] = xx[0+offs] * swed.nutv.matrix[0][i] +
                        xx[1+offs] * swed.nutv.matrix[1][i] +
                        xx[2+offs] * swed.nutv.matrix[2][i];
                }
                /* new speed */
                xx[3+i+offs] = x[3+i] + (x[i] - xv[i]) / SwephData.NUT_SPEED_INTV;
            }
        }
        /* new position */
        for (i = 0; i <= 2; i++) {
            xx[i+offs] = x[i];
        }
    }

    /* computes 'annual' aberration
     * xx           planet's position accounted for light-time
     *              and gravitational light deflection
     * xe           earth's position and speed
     */
    void swi_aberr_light(double xx[], double xe[], int iflag) {
        swi_aberr_light(xx, 0, xe, iflag);
    }
    void swi_aberr_light(double xx[], int xxOffs, double xe[], int iflag) {
        int i;
        double xxs[]=new double[6], v[]=new double[6], u[]=new double[6], ru;
        double xx2[]=new double[6], dx1, dx2;
        double b_1, f1, f2;
        double v2;
        double intv = SwephData.PLAN_SPEED_INTV;
        for (i = 0; i <= 5; i++) {
            u[i] = xxs[i] = xx[i+xxOffs];
        }
        ru = Math.sqrt(sl.square_sum(u));
        for (i = 0; i <= 2; i++) {
            v[i] = xe[i+3] / 24.0 / 3600.0 / SwephData.CLIGHT * SweConst.AUNIT;
        }
        v2 = sl.square_sum(v);
        b_1 = Math.sqrt(1 - v2);
        f1 = dot_prod(u, v) / ru;
        f2 = 1.0 + f1 / (1.0 + b_1);
        for (i = 0; i <= 2; i++) {
            xx[i+xxOffs] = (b_1*xx[i+xxOffs] + f2*ru*v[i]) / (1.0 + f1);
        }
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            /* correction of speed
             * the influence of aberration on apparent velocity can
             * reach 0.4"/day
             */
            for (i = 0; i <= 2; i++) {
                u[i] = xxs[i] - intv * xxs[i+3];
            }
            ru = Math.sqrt(sl.square_sum(u));
            f1 = dot_prod(u, v) / ru;
            f2 = 1.0 + f1 / (1.0 + b_1);
            for (i = 0; i <= 2; i++) {
                xx2[i] = (b_1*u[i] + f2*ru*v[i]) / (1.0 + f1);
            }
            for (i = 0; i <= 2; i++) {
                dx1 = xx[i+xxOffs] - xxs[i];
                dx2 = xx2[i] - u[i];
                dx1 -= dx2;
                xx[i+3+xxOffs] += dx1 / intv;
            }
        }
    }

    /* computes relativistic light deflection by the sun
     * ipli         sweph internal planet number
     * xx           planet's position accounted for light-time
     * dt           dt of light-time
     */
    void swi_deflect_light(double xx[], int offs, double dt, int iflag) {
        int i;
        double xx2[]=new double[6];
        double u[]=new double[6], e[]=new double[6], q[]=new double[6];
        double ru, re, rq, uq, ue, qe, g1, g2;
        double xx3[]=new double[6], dx1, dx2, dtsp;
        double xsun[]=new double[6], xearth[]=new double[6];
        double sina, sin_sunr, meff_fact;
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        int iephe = pedp.iephe;
        for (i = 0; i <= 5; i++) {
            xearth[i] = pedp.x[i];
        }
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            for (i = 0; i <= 5; i++) {
                xearth[i] += swed.topd.xobs[i];
            }
        }
        /* U = planetbary(t-tau) - earthbary(t) = planetgeo */
        for (i = 0; i <= 2; i++) {
            u[i] = xx[i+offs];
        }
        /* Eh = earthbary(t) - sunbary(t) = earthhel */
        if (iephe == SweConst.SEFLG_JPLEPH || iephe == SweConst.SEFLG_SWIEPH) {
            for (i = 0; i <= 2; i++) {
                e[i] = xearth[i] - psdp.x[i];
            }
        } else {
            for (i = 0; i <= 2; i++) {
                e[i] = xearth[i];
            }
        }
        /* Q = planetbary(t-tau) - sunbary(t-tau) = 'planethel' */
        /* first compute sunbary(t-tau) for */
        if (iephe == SweConst.SEFLG_JPLEPH || iephe == SweConst.SEFLG_SWIEPH) {
            for (i = 0; i <= 2; i++) {
                /* this is sufficient precision */
                xsun[i] = psdp.x[i] - dt * psdp.x[i+3];
            }
            for (i = 3; i <= 5; i++) {
                xsun[i] = psdp.x[i];
            }
        } else {
            for (i = 0; i <= 5; i++) {
                xsun[i] = psdp.x[i];
            }
        }
        for (i = 0; i <= 2; i++) {
            q[i] = xx[i+offs] + xearth[i] - xsun[i];
        }
        ru = Math.sqrt(sl.square_sum(u));
        rq = Math.sqrt(sl.square_sum(q));
        re = Math.sqrt(sl.square_sum(e));
        for (i = 0; i <= 2; i++) {
            u[i] /= ru;
            q[i] /= rq;
            e[i] /= re;
        }
        uq = dot_prod(u,q);
        ue = dot_prod(u,e);
        qe = dot_prod(q,e);
        /* When a planet approaches the center of the sun in superior
         * conjunction, the formula for the deflection angle as given
         * in Expl. Suppl. p. 136 cannot be used. The deflection seems
         * to increase rapidly towards infinity. The reason is that the
         * formula considers the sun as a point mass. AA recommends to
         * set deflection = 0 in such a case.
         * However, to get a continous motion, we modify the formula
         * for a non-point-mass, taking into account the mass distribution
         * within the sun. For more info, s. meff().
         */
        sina = Math.sqrt(1 - ue * ue);      /* sin(angle) between sun and planet */
        sin_sunr = SwephData.SUN_RADIUS / re;   /* sine of sun radius (= sun radius) */
        if (sina < sin_sunr) {
            meff_fact = meff(sina / sin_sunr);
        } else {
            meff_fact = 1;
        }
        g1 = 2.0 * SwephData.HELGRAVCONST * meff_fact / SwephData.CLIGHT / SwephData.CLIGHT / SweConst.AUNIT / re;
        g2 = 1.0 + qe;
        /* compute deflected position */
        for (i = 0; i <= 2; i++) {
            xx2[i] = ru * (u[i] + g1/g2 * (uq * e[i] - ue * q[i]));
        }
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            /* correction of speed
             * influence of light deflection on a planet's apparent speed:
             * for an outer planet at the solar limb with
             * |v(planet) - v(sun)| = 1 degree, this makes a difference of 7"/day.
             * if the planet is within the solar disc, the difference may increase
             * to 30" or more.
             * e.g. mercury at j2434871.45:
             *  distance from sun               45"
             *  1. speed without deflection     2\xb310'10".4034
             *    2. speed with deflection        2\xb310'42".8460 (-speed flag)
             *    3. speed with deflection        2\xb310'43".4824 (< 3 positions/
             *                                                     -speed3 flag)
             * 3. is not very precise. Smaller dt would give result closer to 2.,
             * but will probably never be as good as 2, unless long doubles are
             * used. (try also j2434871.46!!)
             * however, in such a case speed changes rapidly. before being
             * passed by the sun, the planet accelerates, and after the sun
             * has passed it slows down. some time later it regains 'normal'
             * speed.
             * to compute speed, we do the same calculation as above with
             * slightly different u, e, q, and find out the difference in
             * deflection.
             */
            dtsp = -SwephData.DEFL_SPEED_INTV;
            /* U = planetbary(t-tau) - earthbary(t) = planetgeo */
            for (i = 0; i <= 2; i++) {
                u[i] = xx[i+offs] - dtsp * xx[i+3+offs];
            }
            /* Eh = earthbary(t) - sunbary(t) = earthhel */
            if (iephe == SweConst.SEFLG_JPLEPH || iephe == SweConst.SEFLG_SWIEPH) {
                for (i = 0; i <= 2; i++) {
                    e[i] = xearth[i] - psdp.x[i] - dtsp * (xearth[i+3] - psdp.x[i+3]);
                }
            } else {
                for (i = 0; i <= 2; i++) {
                    e[i] = xearth[i] - dtsp * xearth[i+3];
                }
            }
            /* Q = planetbary(t-tau) - sunbary(t-tau) = 'planethel' */
            for (i = 0; i <= 2; i++) {
                q[i] = u[i] + xearth[i] - xsun[i] - dtsp * (xearth[i+3] - xsun[i+3]);
            }
            ru = Math.sqrt(sl.square_sum(u));
            rq = Math.sqrt(sl.square_sum(q));
            re = Math.sqrt(sl.square_sum(e));
            for (i = 0; i <= 2; i++) {
                u[i] /= ru;
                q[i] /= rq;
                e[i] /= re;
            }
            uq = dot_prod(u,q);
            ue = dot_prod(u,e);
            qe = dot_prod(q,e);
            sina = Math.sqrt(1 - ue * ue);    /* sin(angle) between sun and planet */
            sin_sunr = SwephData.SUN_RADIUS / re; /* sine of sun radius (= sun radius) */
            if (sina < sin_sunr) {
                meff_fact = meff(sina / sin_sunr);
            } else {
                meff_fact = 1;
            }
            g1 = 2.0 * SwephData.HELGRAVCONST * meff_fact / SwephData.CLIGHT /
                SwephData.CLIGHT / SweConst.AUNIT / re;
            g2 = 1.0 + qe;
            for (i = 0; i <= 2; i++) {
                xx3[i] = ru * (u[i] + g1/g2 * (uq * e[i] - ue * q[i]));
            }
            for (i = 0; i <= 2; i++) {
                dx1 = xx2[i] - xx[i+offs];
                dx2 = xx3[i] - u[i] * ru;
                dx1 -= dx2;
                xx[i+3+offs] += dx1 / dtsp;
            }
        } /* endif speed */
        /* deflected position */
        for (i = 0; i <= 2; i++) {
            xx[i+offs] = xx2[i];
        }
    }

    /* converts the sun from barycentric to geocentric,
     *          the earth from barycentric to heliocentric
     * computes
     * apparent position,
     * precession, and nutation
     * according to flags
     * iflag        flags
     * serr         error string
     */
    int app_pos_etc_sun(int iflag, StringBuffer serr) {
        int i, j, niter, retc = SweConst.OK;
        int flg1, flg2;
        double xx[]=new double[6], xxsv[]=new double[6], dx[]=new double[3], dt, t;
        double xearth[]=new double[6], xsun[]=new double[6], xobs[]=new double[6];
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        Epsilon oe = swed.oec2000;
        /* if the same conversions have already been done for the same
         * date, then return */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = pedp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        if (flg1 == flg2) {
            pedp.xflgs = iflag;
            pedp.iephe = iflag & SweConst.SEFLG_EPHMASK;
            return SweConst.OK;
        }
        /************************************
         * observer: geocenter or topocenter
         ************************************/
        /* if topocentric position is wanted  */
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            if (swed.topd.teval != pedp.teval
                || swed.topd.teval == 0) {
                if (swi_get_observer(pedp.teval, iflag, SwephData.DO_SAVE, xobs, serr)
                    != SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = swed.topd.xobs[i];
                }
            }
            /* barycentric position of observer */
            for (i = 0; i <= 5; i++) {
                xobs[i] = xobs[i] + pedp.x[i];
            }
        } else {
            /* barycentric position of geocenter */
            for (i = 0; i <= 5; i++) {
                xobs[i] = pedp.x[i];
            }
        }
        /***************************************
         * true heliocentric position of earth *
         ***************************************/
        if (pedp.iephe == SweConst.SEFLG_MOSEPH ||
            (iflag & SweConst.SEFLG_BARYCTR)!=0) {
            for (i = 0; i <= 5; i++) {
                xx[i] = xobs[i];
            }
        } else {
            for (i = 0; i <= 5; i++) {
                xx[i] = xobs[i] - psdp.x[i];
            }
        }
        /*******************************
         * light-time                  *
         *******************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0) {
            /* number of iterations - 1
             * the following if() does the following:
             * with jpl and swiss ephemeris:
             *   with geocentric computation of sun:
             *     light-time correction of barycentric sun position.
             *   with heliocentric or barycentric computation of earth:
             *     light-time correction of barycentric earth position.
             * with moshier ephemeris (heliocentric!!!):
             *   with geocentric computation of sun:
             *     nothing! (aberration will be done later)
             *   with heliocentric or barycentric computation of earth:
             *     light-time correction of heliocentric earth position.
             */
            if (pedp.iephe == SweConst.SEFLG_JPLEPH ||
                pedp.iephe == SweConst.SEFLG_SWIEPH ||
                (iflag & SweConst.SEFLG_HELCTR)!=0 ||
                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                for (i = 0; i <= 5; i++) {
                    xearth[i] = xobs[i];
                    if (pedp.iephe == SweConst.SEFLG_MOSEPH) {
                        xsun[i] = 0;
                    } else {
                        xsun[i] = psdp.x[i];
                    }
                }
                niter = 1;        /* # of iterations */
                for (j = 0; j <= niter; j++) {
                    /* distance earth-sun */
                    for (i = 0; i <= 2; i++) {
                        dx[i] = xearth[i];
                        if ((iflag & SweConst.SEFLG_BARYCTR)==0) {
                            dx[i] -= xsun[i];
                        }
                    }
                    /* new t */
                    dt = Math.sqrt(sl.square_sum(dx)) * SweConst.AUNIT / SwephData.CLIGHT /
                        86400.0;
                    t = pedp.teval - dt;
                    /* new position */
                    switch(pedp.iephe) {
                        /* if geocentric sun, new sun at t'
                         * if heliocentric or barycentric earth, new earth at t' */
                        case SweConst.SEFLG_JPLEPH:
                            if ((iflag & SweConst.SEFLG_HELCTR)!=0 ||
                                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                                retc = sj.swi_pleph(t, SwephJPL.J_EARTH, SwephJPL.J_SBARY, xearth, serr);
                            } else {
                                retc = sj.swi_pleph(t, SwephJPL.J_SUN, SwephJPL.J_SBARY, xsun, serr);
                            }
                            if (retc != SweConst.OK) {
                                sj.swi_close_jpl_file();
                                swed.jpl_file_is_open = false;
                                return(retc);
                            }
                            break;
                        case SweConst.SEFLG_SWIEPH:
                            /*
                              retc = sweph(t, SEI_SUN, SEI_FILE_PLANET, iflag, NULL, NO_SAVE, xearth, serr);
                            */
                            if ((iflag & SweConst.SEFLG_HELCTR)!=0 ||
                                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                                retc = sweplan(t, SwephData.SEI_EARTH,
                                               SwephData.SEI_FILE_PLANET, iflag,
                                               SwephData.NO_SAVE, xearth, null, xsun, null,
                                               serr);
                            } else {
                                retc = sweph(t, SwephData.SEI_SUNBARY,
                                             SwephData.SEI_FILE_PLANET, iflag, null,
                                             SwephData.NO_SAVE, xsun, serr);
                            }
                            break;
                        case SweConst.SEFLG_MOSEPH:
                            if ((iflag & SweConst.SEFLG_HELCTR)!=0 ||
                                (iflag & SweConst.SEFLG_BARYCTR)!=0) {
                                retc = smosh.swi_moshplan(t, SwephData.SEI_EARTH,
                                                          SwephData.NO_SAVE, xearth, xearth,
                                                          serr);
                            }
                            /* with moshier there is no barycentric sun */
                            break;
                        default:
                            retc = SweConst.ERR;
                            break;
                    }
                    if (retc != SweConst.OK) {
                        return(retc);
                    }
                }
                /* apparent heliocentric earth */
                for (i = 0; i <= 5; i++) {
                    xx[i] = xearth[i];
                    if ((iflag & SweConst.SEFLG_BARYCTR)==0) {
                        xx[i] -= xsun[i];
                    }
                }
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /*******************************
         * conversion to geocenter     *
         *******************************/
        if ((iflag & SweConst.SEFLG_HELCTR)==0 &&
            (iflag & SweConst.SEFLG_BARYCTR)==0) {
            for (i = 0; i <= 5; i++) {
                xx[i] = -xx[i];
            }
        }
        /**********************************
         * 'annual' aberration of light   *
         **********************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOABERR)==0) {
            /* SEFLG_NOABERR is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_aberr_light(xx, xobs, iflag);
        }
        if ((iflag & SweConst.SEFLG_SPEED) == 0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS) == 0 && swed.jpldenum >= 403) {
            sl.swi_bias(xx, iflag, false);
        }/**/
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = xx[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        if ((iflag & SweConst.SEFLG_J2000)==0) {
            sl.swi_precess(xx, pedp.teval, SwephData.J2000_TO_J);/**/
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, pedp.teval, SwephData.J2000_TO_J);/**/
            }
            oe = swed.oec;
        } else
            oe = swed.oec2000;
        return app_pos_rest(pedp, iflag, xx, xxsv, oe, serr);
    }

    /* transforms the position of the moon:
     * heliocentric position
     * barycentric position
     * astrometric position
     * apparent position
     * precession and nutation
     *
     * note:
     * for apparent positions, we consider the earth-moon
     * system as independant.
     * for astrometric positions (SEFLG_NOABERR), we
     * consider the motions of the earth and the moon
     * related to the solar system barycenter.
     */
    int app_pos_etc_moon(int iflag, StringBuffer serr) {
        int i;
        int flg1, flg2;
        double xx[]=new double[6], xxsv[]=new double[6], xobs[]=new double[6],
            xxm[]=new double[6], xs[]=new double[6], xe[]=new double[6],
            xobs2[]=new double[6], dt;
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        PlanData pdp = swed.pldat[SwephData.SEI_MOON];
        Epsilon oe = swed.oec;
        double t;
        int retc;
        /* if the same conversions have already been done for the same
         * date, then return */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = pdp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        if (flg1 == flg2) {
            pdp.xflgs = iflag;
            pdp.iephe = (iflag & SweConst.SEFLG_EPHMASK);
            return SweConst.OK;
        }
        /* the conversions will be done with xx[]. */
        for (i = 0; i <= 5; i++) {
            xx[i] = pdp.x[i];
            xxm[i] = xx[i];
        }
        /***********************************
         * to solar system barycentric
         ***********************************/
        for (i = 0; i <= 5; i++) {
            xx[i] += pedp.x[i];
        }
        /*******************************
         * observer
         *******************************/
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            if (swed.topd.teval != pdp.teval
                || swed.topd.teval == 0) {
                if (swi_get_observer(pdp.teval, iflag, SwephData.DO_SAVE, xobs, null) !=
                    SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = swed.topd.xobs[i];
                }
            }
            for (i = 0; i <= 5; i++) {
                xxm[i] -= xobs[i];
            }
            for (i = 0; i <= 5; i++) {
                xobs[i] += pedp.x[i];
            }
        } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
            for (i = 0; i <= 5; i++) {
                xobs[i] = 0;
            }
            for (i = 0; i <= 5; i++) {
                xxm[i] += pedp.x[i];
            }
        } else if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
            for (i = 0; i <= 5; i++) {
                xobs[i] = psdp.x[i];
            }
            for (i = 0; i <= 5; i++) {
                xxm[i] += pedp.x[i] - psdp.x[i];
            }
        } else {
            for (i = 0; i <= 5; i++) {
                xobs[i] = pedp.x[i];
            }
        }
        /*******************************
         * light-time                  *
         *******************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0) {
            dt = Math.sqrt(sl.square_sum(xxm)) * SweConst.AUNIT /
                SwephData.CLIGHT / 86400.0;
            t = pdp.teval - dt;
            switch(pdp.iephe) {
                case SweConst.SEFLG_JPLEPH:
                    retc = sj.swi_pleph(t, SwephJPL.J_MOON, SwephJPL.J_EARTH, xx, serr);
                    if (retc == SweConst.OK) {
                        retc = sj.swi_pleph(t, SwephJPL.J_EARTH, SwephJPL.J_SBARY, xe, serr);
                    }
                    if (retc == SweConst.OK && (iflag & SweConst.SEFLG_HELCTR)!=0) {
                        retc = sj.swi_pleph(t, SwephJPL.J_SUN, SwephJPL.J_SBARY, xs, serr);
                    }
                    if (retc != SweConst.OK) {
                        sj.swi_close_jpl_file();
                        swed.jpl_file_is_open = false;
                    }
                    for (i = 0; i <= 5; i++) {
                        xx[i] += xe[i];
                    }
                    break;
                case SweConst.SEFLG_SWIEPH:
                    retc = sweplan(t, SwephData.SEI_MOON, SwephData.SEI_FILE_MOON, iflag, SwephData.NO_SAVE, xx, xe, xs, null, serr);
                    if (retc != SweConst.OK) {
                        return(retc);
                    }
                    for (i = 0; i <= 5; i++) {
                        xx[i] += xe[i];
                    }
                    break;
                case SweConst.SEFLG_MOSEPH:
                    /* this method results in an error of a milliarcsec in speed */
                    for (i = 0; i <= 2; i++) {
                        xx[i] -= dt * xx[i+3];
                        xe[i] = pedp.x[i] - dt * pedp.x[i+3];
                        xe[i+3] = pedp.x[i+3];
                        xs[i] = 0;
                        xs[i+3] = 0;
                    }
                    break;
            }
            if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
                if (swi_get_observer(t, iflag, SwephData.NO_SAVE, xobs2, null) !=
                    SweConst.OK) {
                    return SweConst.ERR;
                }
                for (i = 0; i <= 5; i++) {
                    xobs2[i] += xe[i];
                }
            } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
                for (i = 0; i <= 5; i++) {
                    xobs2[i] = 0;
                }
            } else if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
                for (i = 0; i <= 5; i++) {
                    xobs2[i] = xs[i];
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs2[i] = xe[i];
                }
            }
        }
        /*************************
         * to correct center
         *************************/
        for (i = 0; i <= 5; i++) {
            xx[i] -= xobs[i];
        }
        /**********************************
         * 'annual' aberration of light   *
         **********************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0 &&
            (iflag & SweConst.SEFLG_NOABERR)==0) {
            /* SEFLG_NOABERR is on, if SEFLG_HELCTR or SEFLG_BARYCTR */
            swi_aberr_light(xx, xobs, iflag);
            /*
             * Apparent speed is also influenced by
             * the difference of speed of the earth between t and t-dt.
             * Neglecting this would lead to an error of several 0.1"
             */
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                for (i = 3; i <= 5; i++) {
                    xx[i] += xobs[i] - xobs2[i];
                }
            }
        }
        /* if !speedflag, speed = 0 */
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS) == 0 && swed.jpldenum >= 403) {
            sl.swi_bias(xx, iflag, false);
        }/**/
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = xx[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        if ((iflag & SweConst.SEFLG_J2000) == 0) {
            sl.swi_precess(xx, pdp.teval, SwephData.J2000_TO_J);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, pdp.teval, SwephData.J2000_TO_J);
            }
            oe = swed.oec;
        } else {
            oe = swed.oec2000;
        }
        return app_pos_rest(pdp, iflag, xx, xxsv, oe, serr);
    }

    /* transforms the position of the barycentric sun:
     * precession and nutation
     * according to flags
     * iflag        flags
     * serr         error string
     */
    int app_pos_etc_sbar(int iflag, StringBuffer serr) {
        int i;
        double xx[]=new double[6], xxsv[]=new double[6], dt;
        PlanData psdp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psbdp = swed.pldat[SwephData.SEI_SUNBARY];
        Epsilon oe = swed.oec;
        /* the conversions will be done with xx[]. */
        for (i = 0; i <= 5; i++) {
            xx[i] = psbdp.x[i];
        }
        /**************
         * light-time *
         **************/
        if ((iflag & SweConst.SEFLG_TRUEPOS)==0) {
            dt = Math.sqrt(sl.square_sum(xx)) * SweConst.AUNIT / SwephData.CLIGHT / 86400.0;
            for (i = 0; i <= 2; i++) {
                xx[i] -= dt * xx[i+3];    /* apparent position */
            }
        }
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS) == 0 && swed.jpldenum >= 403) {
            sl.swi_bias(xx, iflag, false);
        }/**/
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = xx[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        if ((iflag & SweConst.SEFLG_J2000)==0) {
            sl.swi_precess(xx, psbdp.teval, SwephData.J2000_TO_J);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, psbdp.teval, SwephData.J2000_TO_J);
            }
            oe = swed.oec;
        } else {
            oe = swed.oec2000;
        }
        return app_pos_rest(psdp, iflag, xx, xxsv, oe, serr);
    }

    /* transforms position of mean lunar node or apogee:
     * input is polar coordinates in mean ecliptic of date.
     * output is, according to iflag:
     * position accounted for light-time
     * position referred to J2000 (i.e. precession subtracted)
     * position with nutation
     * equatorial coordinates
     * cartesian coordinates
     * heliocentric position is not allowed ??????????????
     *         DAS WAERE ZIEMLICH AUFWENDIG. SONNE UND ERDE MUESSTEN
     *         SCHON VORHANDEN SEIN!
     * ipl          bodynumber (SE_MEAN_NODE or SE_MEAN_APOG)
     * iflag        flags
     * serr         error string
     */
    int app_pos_etc_mean(int ipl, int iflag, StringBuffer serr) {
        int i;
        int flg1, flg2;
        double xx[]=new double[6], xxsv[]=new double[6];
        PlanData pdp = swed.nddat[ipl];
        Epsilon oe;
        /* if the same conversions have already been done for the same
         * date, then return */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = pdp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        if (flg1 == flg2) {
            pdp.xflgs = iflag;
            pdp.iephe = iflag & SweConst.SEFLG_EPHMASK;
            return SweConst.OK;
        }
        for (i = 0; i <= 5; i++) {
            xx[i] = pdp.x[i];
        }
        /* cartesian equatorial coordinates */
        sl.swi_polcart_sp(xx, xx);
        sl.swi_coortrf2(xx, xx, -swed.oec.seps, swed.oec.ceps);
        sl.swi_coortrf2(xx, 3, xx, 3, -swed.oec.seps, swed.oec.ceps);
        if ((iflag & SweConst.SEFLG_SPEED)==0) {
            for (i = 3; i <= 5; i++) {
                xx[i] = 0;
            }
        }
        /* J2000 coordinates; required for sidereal positions */
        if (((iflag & SweConst.SEFLG_SIDEREAL)!=0
             && (swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0)
            || (swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
            for (i = 0; i <= 5; i++) {
                xxsv[i] = xx[i];
            }
            /* xxsv is not J2000 yet! */
            if (pdp.teval != SwephData.J2000) {
                sl.swi_precess(xxsv, pdp.teval, SwephData.J_TO_J2000);
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    swi_precess_speed(xxsv, pdp.teval, SwephData.J_TO_J2000);
                }
            }
        }
        /*****************************************************
         * if no precession, equator of date -> equator 2000 *
         *****************************************************/
        if ((iflag & SweConst.SEFLG_J2000)!=0) {
            sl.swi_precess(xx, pdp.teval, SwephData.J_TO_J2000);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(xx, pdp.teval, SwephData.J_TO_J2000);
            }
            oe = swed.oec2000;
        } else {
            oe = swed.oec;
        }
        return app_pos_rest(pdp, iflag, xx, xxsv, oe, serr);
    }

    /* SWISSEPH
     * adds reference orbit to chebyshew series (if SEI_FLG_ELLIPSE),
     * rotates series to mean equinox of J2000
     *
     * ipli         planet number
     */
    void rot_back(int ipli) {
        int i;
        double t, tdiff;
        double qav, pav, dn;
        double omtild, com, som, cosih2;
        double x[][]=new double[SwephData.MAXORD+1][3];
        double uix[]=new double[3], uiy[]=new double[3], uiz[]=new double[3];
        double xrot, yrot, zrot;
        double chcfx[];
        double refepx[];
        double seps2000 = swed.oec2000.seps;
        double ceps2000 = swed.oec2000.ceps;
        PlanData pdp = swed.pldat[ipli];
        int nco = pdp.ncoe;
        int chcfyOffs;
        int chcfzOffs;
        int refepyOffs;
        t = pdp.tseg0 + pdp.dseg / 2;
        chcfx = pdp.segp;
        chcfyOffs = nco;
        chcfzOffs = 2 * nco;
        refepx = pdp.refep;
        refepyOffs = nco;
        tdiff= (t - pdp.telem) / 365250.0;
        if (ipli == SwephData.SEI_MOON) {
            dn = pdp.prot + tdiff * pdp.dprot;
            i = (int) (dn / SwephData.TWOPI);
            dn -= i * SwephData.TWOPI;
            qav = (pdp.qrot + tdiff * pdp.dqrot) * Math.cos(dn);
            pav = (pdp.qrot + tdiff * pdp.dqrot) * Math.sin(dn);
        } else {
            qav = pdp.qrot + tdiff * pdp.dqrot;
            pav = pdp.prot + tdiff * pdp.dprot;
        }
        /*calculate cosine and sine of average perihelion longitude. */
        for (i = 0; i < nco; i++) {
            x[i][0] = chcfx[i];
            x[i][1] = chcfx[i+chcfyOffs];
            x[i][2] = chcfx[i+chcfzOffs];
        }
        if ((pdp.iflg & SwephData.SEI_FLG_ELLIPSE)!=0) {
            omtild = pdp.peri + tdiff * pdp.dperi;
            i = (int) (omtild / SwephData.TWOPI);
            omtild -= i * SwephData.TWOPI;
            com = Math.cos(omtild);
            som = Math.sin(omtild);
            /*add reference orbit.  */
            for (i = 0; i < nco; i++) {
                x[i][0] = chcfx[i] + com * refepx[i] - som * refepx[i+refepyOffs];
                x[i][1] = chcfx[i+chcfyOffs] + com * refepx[i+refepyOffs] + som * refepx[i];
            }
        }
        /* construct right handed orthonormal system with first axis along
           origin of longitudes and third axis along angular momentum
           this uses the standard formulas for equinoctal variables
           (see papers by broucke and by cefola).      */
        cosih2 = 1.0 / (1.0 + qav * qav + pav * pav);
        /*     calculate orbit pole. */
        uiz[0] = 2.0 * pav * cosih2;
        uiz[1] = -2.0 * qav * cosih2;
        uiz[2] = (1.0 - qav * qav - pav * pav) * cosih2;
        /*     calculate origin of longitudes vector. */
        uix[0] = (1.0 + qav * qav - pav * pav) * cosih2;
        uix[1] = 2.0 * qav * pav * cosih2;
        uix[2] = -2.0 * pav * cosih2;
        /*     calculate vector in orbital plane orthogonal to origin of
               longitudes.                                               */
        uiy[0] =2.0 * qav * pav * cosih2;
        uiy[1] =(1.0 - qav * qav + pav * pav) * cosih2;
        uiy[2] =2.0 * qav * cosih2;
        /*     rotate to actual orientation in space.         */
        for (i = 0; i < nco; i++) {
            xrot = x[i][0] * uix[0] + x[i][1] * uiy[0] + x[i][2] * uiz[0];
            yrot = x[i][0] * uix[1] + x[i][1] * uiy[1] + x[i][2] * uiz[1];
            zrot = x[i][0] * uix[2] + x[i][1] * uiy[2] + x[i][2] * uiz[2];
            if (Math.abs(xrot) + Math.abs(yrot) + Math.abs(zrot) >= 1e-14) {
                pdp.neval = i;
            }
            x[i][0] = xrot;
            x[i][1] = yrot;
            x[i][2] = zrot;
            if (ipli == SwephData.SEI_MOON) {
                /* rotate to j2000 equator */
                x[i][1] = ceps2000 * yrot - seps2000 * zrot;
                x[i][2] = seps2000 * yrot + ceps2000 * zrot;
            }
        }
        for (i = 0; i < nco; i++) {
            chcfx[i] = x[i][0];
            chcfx[i+chcfyOffs] = x[i][1];
            chcfx[i+chcfzOffs] = x[i][2];
        }
    }

    /* Adjust position from Earth-Moon barycenter to Earth
     *
     * xemb = hel./bar. position or velocity vectors of emb (input)
     *                                                  earth (output)
     * xmoon= geocentric position or velocity vector of moon
     */
    void embofs(double xemb[], int eOffs, double xmoon[], int mOffs) {
        int i;
        for (i = 0; i <= 2; i++) {
            xemb[i+eOffs] -= xmoon[i+mOffs] / (SwephData.EARTH_MOON_MRAT + 1.0);
        }
    }

    /* calculates the nutation matrix
     * nu           pointer to nutation data structure
     * oe           pointer to epsilon data structure
     */
    void nut_matrix(Nut nu, Epsilon oe) {
        double psi, eps;
        double sinpsi, cospsi, sineps, coseps, sineps0, coseps0;
        psi = nu.nutlo[0];
        eps = oe.eps + nu.nutlo[1];
        sinpsi = Math.sin(psi);
        cospsi = Math.cos(psi);
        sineps0 = oe.seps;
        coseps0 = oe.ceps;
        sineps = Math.sin(eps);
        coseps = Math.cos(eps);
        nu.matrix[0][0] = cospsi;
        nu.matrix[0][1] = sinpsi * coseps;
        nu.matrix[0][2] = sinpsi * sineps;
        nu.matrix[1][0] = -sinpsi * coseps0;
        nu.matrix[1][1] = cospsi * coseps * coseps0 + sineps * sineps0;
        nu.matrix[1][2] = cospsi * sineps * coseps0 - coseps * sineps0;
        nu.matrix[2][0] = -sinpsi * sineps0;
        nu.matrix[2][1] = cospsi * coseps * sineps0 - sineps * coseps0;
        nu.matrix[2][2] = cospsi * sineps * sineps0 + coseps * coseps0;
    }

    /* lunar osculating elements, i.e.
     * osculating node ('true' node) and
     * osculating apogee ('black moon', 'lilith').
     * tjd          julian day
     * ipl          body number, i.e. SEI_TRUE_NODE or SEI_OSCU_APOG
     * iflag        flags (which ephemeris, nutation, etc.)
     * serr         error string
     *
     * definitions and remarks:
     * the osculating node and the osculating apogee are defined
     * as the orbital elements of the momentary lunar orbit.
     * their advantage is that when the moon crosses the ecliptic,
     * it is really at the osculating node, and when it passes
     * its greatest distance from earth it is really at the
     * osculating apogee. with the mean elements this is not
     * the case. (some define the apogee as the second focus of
     * the lunar ellipse. but, as seen from the geocenter, both
     * points are in the same direction.)
     * problems:
     * the osculating apogee is given in the 'New International
     * Ephemerides' (Editions St. Michel) as the 'True Lilith'.
     * however, this name is misleading. this point is based on
     * the idea that the lunar orbit can be approximated by an
     * ellipse.
     * arguments against this:
     * 1. this procedure considers celestial motions as two body
     *    problems. this is quite good for planets, but not for
     *    the moon. the strong gravitational attraction of the sun
     *    destroys the idea of an ellipse.
     * 2. the NIE 'True Lilith' has strong oscillations around the
     *    mean one with an amplitude of about 30 degrees. however,
     *    when the moon is in apogee, its distance from the mean
     *    apogee never exceeds 5 degrees.
     * besides, the computation of NIE is INACCURATE. the mistake
     * reaches 20 arc minutes.
     * According to Santoni, the point was calculated using 'les 58
     * premiers termes correctifs au Perigee moyen' published by
     * Chapront and Chapront-Touze. And he adds: "Nous constatons
     * que meme en utilisant ces 58 termes CORRECTIFS, l'erreur peut
     * atteindre 0,5d!" (p. 13) We avoid this error, computing the
     * orbital elements directly from the position and the speed vector.
     *
     * how about the node? it is less problematic, because we
     * we needn't derive it from an orbital ellipse. we can say:
     * the axis of the osculating nodes is the intersection line of
     * the actual orbital plane of the moon and the plane of the
     * ecliptic. or: the osculating nodes are the intersections of
     * the two great circles representing the momentary apparent
     * orbit of the moon and the ecliptic. in this way they make
     * some sense. then, the nodes are really an axis, and they
     * have no geocentric distance. however, in this routine
     * we give a distance derived from the osculating ellipse.
     * the node could also be defined as the intersection axis
     * of the lunar orbital plane and the solar orbital plane,
     * which is not precisely identical to the ecliptic. this
     * would make a difference of several arcseconds.
     *
     * is it possible to keep the idea of a continuously moving
     * apogee that is exact at the moment when the moon passes
     * its greatest distance from earth?
     * to achieve this, we would probably have to interpolate between
     * the actual apogees.
     * the nodes could also be computed by interpolation. the resulting
     * nodes would deviate from the so-called 'true node' by less than
     * 30 arc minutes.
     *
     * sidereal and j2000 true node are first computed for the ecliptic
     * of epoch and then precessed to ecliptic of t0(ayanamsa) or J2000.
     * there is another procedure that computes the node for the ecliptic
     * of t0(ayanamsa) or J2000. it is excluded by
     * #ifdef SID_TNODE_FROM_ECL_T0
     */
    private int lunar_osc_elem(double tjd, int ipl, int iflag, StringBuffer serr) {
        int i, j, istart;
        int ipli = SwephData.SEI_MOON;
        int epheflag = SweConst.SEFLG_DEFAULTEPH;
        int retc = SweConst.ERR;
        int flg1, flg2;
        PlanData ndp, ndnp, ndap;
        Epsilon oe;
        double speed_intv = SwephData.NODE_CALC_INTV;   /* to silence gcc warning */
        double a, b;
        double xpos[][]=new double[3][6], xx[][]=new double[3][6],
            xxa[][]=new double[3][6];
        double xp[];
        double xnorm[]=new double[6], r[]=new double[6];
        double rxy, rxyz, t, dt, fac, sgn;
        double sinnode, cosnode, sinincl, cosincl, sinu, cosu, sinE, cosE;
        double uu, ny, sema, ecce, Gmsm, c2, v2, pp;
        int speedf1, speedf2;
        oe = swed.oec;
        ndp = swed.nddat[ipl];
        /* if elements have already been computed for this date, return
         * if speed flag has been turned on, recompute */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = ndp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        speedf1 = ndp.xflgs & SweConst.SEFLG_SPEED;
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        if (tjd == ndp.teval
            && tjd != 0
            && flg1 == flg2
            && ((speedf2==0) || (speedf1!=0))) {
            ndp.xflgs = iflag;
            ndp.iephe = iflag & SweConst.SEFLG_EPHMASK;
            return SweConst.OK;
        }
        /* the geocentric position vector and the speed vector of the
         * moon make up the lunar orbital plane. the position vector
         * of the node is along the intersection line of the orbital
         * plane and the plane of the ecliptic.
         * to calculate the osculating node, we need one lunar position
         * with speed.
         * to calculate the speed of the osculating node, we need
         * three lunar positions and the speed of each of them.
         * this is relatively cheap, if the jpl-moon or the swisseph
         * moon is used. with the moshier moon this is much more
         * expensive, because then we need 9 lunar positions for
         * three speeds. but one position and speed can normally
         * be taken from swed.pldat[moon], which corresponds to
         * three moshier moon calculations.
         * the same is also true for the osculating apogee: we need
         * three lunar positions and speeds.
         */
        /*********************************************
         * now three lunar positions with speeds     *
         *********************************************/
        if ((iflag & SweConst.SEFLG_MOSEPH)!=0) {
            epheflag = SweConst.SEFLG_MOSEPH;
        } else if ((iflag & SweConst.SEFLG_SWIEPH)!=0) {
            epheflag = SweConst.SEFLG_SWIEPH;
        } else if ((iflag & SweConst.SEFLG_JPLEPH)!=0) {
            epheflag = SweConst.SEFLG_JPLEPH;
        }
        /* there may be a moon of wrong ephemeris in save area
         * force new computation: */
        swed.pldat[SwephData.SEI_MOON].teval = 0;
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            istart = 0;
        } else {
            istart = 2;
        }
        if (serr != null) {
            serr.setLength(0);
        }
        //  three_positions:
        do {
            switch(epheflag) {
                case SweConst.SEFLG_JPLEPH:
                    speed_intv = SwephData.NODE_CALC_INTV;
                    for (i = istart; i <= 2; i++) {
                        if (i == 0) {
                            t = tjd - speed_intv;
                        } else if (i == 1) {
                            t = tjd + speed_intv;
                        } else {
                            t = tjd;
                        }
                        xp = xpos[i];
                        retc = jplplan(t, ipli, iflag, SwephData.NO_SAVE, xp, null, null,
                                       serr);
                        /* read error or corrupt file */
                        if (retc == SweConst.ERR) {
                            return(SweConst.ERR);
                        }
                        /* light-time-corrected moon for apparent node
                         * this makes a difference of several milliarcseconds with
                         * the node and 0.1" with the apogee.
                         * the simple formual 'x[j] -= dt * speed' should not be
                         * used here. the error would be greater than the advantage
                         * of computation speed. */
                        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0 && retc >= SweConst.OK) {
                            dt = Math.sqrt(sl.square_sum(xpos[i])) * SweConst.AUNIT /
                                SwephData.CLIGHT / 86400.0;
                            retc = jplplan(t-dt, ipli, iflag, SwephData.NO_SAVE, xpos[i],
                                           null, null, serr); /**/
                            /* read error or corrupt file */
                            if (retc == SweConst.ERR) {
                                return(SweConst.ERR);
                            }
                        }
                        /* jpl ephemeris not on disk, or date beyond ephemeris range */
                        if (retc == SwephData.NOT_AVAILABLE) {
                            iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
                            epheflag = SweConst.SEFLG_SWIEPH;
                            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                                serr.append(" \ntrying Swiss Eph; ");
                            }
                            break;
                        } else if (retc == SwephData.BEYOND_EPH_LIMITS) {
                            if (tjd > SwephData.MOSHLUEPH_START &&
                                tjd < SwephData.MOSHLUEPH_END) {
                                iflag = (iflag & ~SweConst.SEFLG_JPLEPH) |
                                    SweConst.SEFLG_MOSEPH;
                                epheflag = SweConst.SEFLG_MOSEPH;
                                if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                                    serr.append(" \nusing Moshier Eph; ");
                                }
                                break;
                            } else
                                return SweConst.ERR;
                        }
                        /* precession and nutation etc. */
                        retc = swi_plan_for_osc_elem(iflag|SweConst.SEFLG_SPEED, t, xpos[i]); /* retc is always ok */

                    }
                    break;
                case SweConst.SEFLG_SWIEPH:
                    speed_intv = SwephData.NODE_CALC_INTV;
                    for (i = istart; i <= 2; i++) {
                        if (i == 0) {
                            t = tjd - speed_intv;
                        } else if (i == 1) {
                            t = tjd + speed_intv;
                        } else {
                            t = tjd;
                        }
                        retc = swemoon(t, iflag | SweConst.SEFLG_SPEED, SwephData.NO_SAVE,
                                       xpos[i], serr);/**/
                        if (retc == SweConst.ERR) {
                            return(SweConst.ERR);
                        }
                        /* light-time-corrected moon for apparent node (~ 0.006") */
                        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0 && retc >= SweConst.OK) {
                            dt = Math.sqrt(sl.square_sum(xpos[i])) * SweConst.AUNIT /
                                SwephData.CLIGHT / 86400.0;
                            retc = swemoon(t-dt, iflag | SweConst.SEFLG_SPEED,
                                           SwephData.NO_SAVE, xpos[i], serr);/**/
                            if (retc == SweConst.ERR) {
                                return(SweConst.ERR);
                            }
                        }
                        if (retc == SwephData.NOT_AVAILABLE) {
                            if (tjd > SwephData.MOSHPLEPH_START &&
                                tjd < SwephData.MOSHPLEPH_END) {
                                iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
                                epheflag = SweConst.SEFLG_MOSEPH;
                                if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH) {
                                    serr.append(" \nusing Moshier eph.; ");
                                }
                                break;
                            } else
                                return SweConst.ERR;
                        }
                        /* precession and nutation etc. */
                        retc = swi_plan_for_osc_elem(iflag|SweConst.SEFLG_SPEED, t, xpos[i]); /* retc is always ok */
                    }
                    break;
                case SweConst.SEFLG_MOSEPH:
                    /* with moshier moon, we need a greater speed_intv, because here the
                     * node and apogee oscillate wildly within small intervals */
                    speed_intv = SwephData.NODE_CALC_INTV_MOSH;
                    for (i = istart; i <= 2; i++) {
                        if (i == 0) {
                            t = tjd - speed_intv;
                        } else if (i == 1) {
                            t = tjd + speed_intv;
                        } else {
                            t = tjd;
                        }
                        retc = sm.swi_moshmoon(t, SwephData.NO_SAVE, xpos[i], serr);/**/
                        if (retc == SweConst.ERR) {
                            return(retc);
                        }
                        /* precession and nutation etc. */
                        retc = swi_plan_for_osc_elem(iflag|SweConst.SEFLG_SPEED, t, xpos[i]); /* retc is always ok */

                    }
                    break;
                default:
                    break;
            }
        } while (retc == SwephData.NOT_AVAILABLE || retc == SwephData.BEYOND_EPH_LIMITS);
        //    goto three_positions;
        /*********************************************
         * node with speed                           *
         *********************************************/
        /* node is always needed, even if apogee is wanted */
        ndnp = swed.nddat[SwephData.SEI_TRUE_NODE];
        /* three nodes */
        for (i = istart; i <= 2; i++) {
            if (Math.abs(xpos[i][5]) < 1e-15) {
                xpos[i][5] = 1e-15;
            }
            fac = xpos[i][2] / xpos[i][5];
            sgn = xpos[i][5] / Math.abs(xpos[i][5]);
            for (j = 0; j <= 2; j++) {
                xx[i][j] = (xpos[i][j] - fac * xpos[i][j+3]) * sgn;
            }
        }
        /* now we have the correct direction of the node, the
         * intersection of the lunar plane and the ecliptic plane.
         * the distance is the distance of the point where the tangent
         * of the lunar motion penetrates the ecliptic plane.
         * this can be very large, e.g. j2415080.37372.
         * below, a new distance will be derived from the osculating
         * ellipse.
         */
        /* save position and speed */
        for (i = 0; i <= 2; i++) {
            ndnp.x[i] = xx[2][i];
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                b = (xx[1][i] - xx[0][i]) / 2;
                a = (xx[1][i] + xx[0][i]) / 2 - xx[2][i];
                ndnp.x[i+3] = (2 * a + b) / speed_intv;
            } else
                ndnp.x[i+3] = 0;
            ndnp.teval = tjd;
            ndnp.iephe = epheflag;
        }
        /************************************************************
         * apogee with speed                                        *
         * must be computed anyway to get the node's distance       *
         ************************************************************/
        ndap = swed.nddat[SwephData.SEI_OSCU_APOG];
        Gmsm = SwephData.GEOGCONST * (1 + 1 / SwephData.EARTH_MOON_MRAT) /
            SweConst.AUNIT/SweConst.AUNIT/SweConst.AUNIT*86400.0*86400.0;
        /* three apogees */
        for (i = istart; i <= 2; i++) {
            /* node */
            rxy =  Math.sqrt(xx[i][0] * xx[i][0] + xx[i][1] * xx[i][1]);
            cosnode = xx[i][0] / rxy;
            sinnode = xx[i][1] / rxy;
            /* inclination */
            sl.swi_cross_prod(xpos[i], 0, xpos[i], 3, xnorm, 0);
            rxy =  xnorm[0] * xnorm[0] + xnorm[1] * xnorm[1];
            c2 = (rxy + xnorm[2] * xnorm[2]);
            rxyz = Math.sqrt(c2);
            rxy = Math.sqrt(rxy);
            sinincl = rxy / rxyz;
            cosincl = Math.sqrt(1 - sinincl * sinincl);
            /* argument of latitude */
            cosu = xpos[i][0] * cosnode + xpos[i][1] * sinnode;
            sinu = xpos[i][2] / sinincl;
            uu = Math.atan2(sinu, cosu);
            /* semi-axis */
            rxyz = Math.sqrt(sl.square_sum(xpos[i]));
            v2 = sl.square_sum(xpos[i], 3);
            sema = 1 / (2 / rxyz - v2 / Gmsm);
            /* eccentricity */
            pp = c2 / Gmsm;
            ecce = Math.sqrt(1 - pp / sema);
            /* eccentric anomaly */
            cosE = 1 / ecce * (1 - rxyz / sema);
            sinE = 1 / ecce / Math.sqrt(sema * Gmsm) * dot_prod(xpos[i], xpos[i], 3);
            /* true anomaly */
            ny = 2 * Math.atan(Math.sqrt((1+ecce)/(1-ecce)) * sinE / (1 + cosE));
            /* distance of apogee from ascending node */
            xxa[i][0] = sl.swi_mod2PI(uu - ny + SwephData.PI);
            xxa[i][1] = 0;                      /* latitude */
            xxa[i][2] = sema * (1 + ecce);      /* distance */
            /* transformation to ecliptic coordinates */
            sl.swi_polcart(xxa[i], xxa[i]);
            sl.swi_coortrf2(xxa[i], xxa[i], -sinincl, cosincl);
            sl.swi_cartpol(xxa[i], xxa[i]);
            /* adding node, we get apogee in ecl. coord. */
            xxa[i][0] += Math.atan2(sinnode, cosnode);
            sl.swi_polcart(xxa[i], xxa[i]);
            /* new distance of node from orbital ellipse:
             * true anomaly of node: */
            ny = sl.swi_mod2PI(ny - uu);
            /* eccentric anomaly */
            cosE = Math.cos(2 * Math.atan(Math.tan(ny / 2) / Math.sqrt((1+ecce) / (1-ecce))));
            /* new distance */
            r[0] = sema * (1 - ecce * cosE);
            /* old node distance */
            r[1] = Math.sqrt(sl.square_sum(xx[i]));
            /* correct length of position vector */
            for (j = 0; j <= 2; j++) {
                xx[i][j] *= r[0] / r[1];
            }
        }
        /* save position and speed */
        for (i = 0; i <= 2; i++) {
            /* apogee */
            ndap.x[i] = xxa[2][i];
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                ndap.x[i+3] = (xxa[1][i] - xxa[0][i]) / speed_intv / 2;
            } else {
                ndap.x[i+3] = 0;
            }
            ndap.teval = tjd;
            ndap.iephe = epheflag;
            /* node */
            ndnp.x[i] = xx[2][i];
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                ndnp.x[i+3] = (xx[1][i] - xx[0][i]) / speed_intv / 2;/**/
            } else {
                ndnp.x[i+3] = 0;
            }
        }
        /**********************************************************************
         * precession and nutation have already been taken into account
         * because the computation is on the basis of lunar positions
         * that have gone through swi_plan_for_osc_elem.
         * light-time is already contained in lunar positions.
         * now compute polar and equatorial coordinates:
         **********************************************************************/
        double[] x=new double[6];
        for (j = 0; j <= 1; j++) {
            if (j == 0) {
                ndp = swed.nddat[SwephData.SEI_TRUE_NODE];
            } else {
                ndp = swed.nddat[SwephData.SEI_OSCU_APOG];
            }
            //  memset((void *) ndp.xreturn, 0, 24 * sizeof(double));
            for (int z=0; z<ndp.xreturn.length; z++) { ndp.xreturn[z]=0.0; }
            /* cartesian ecliptic */
            for (i = 0; i <= 5; i++) {
                ndp.xreturn[6+i] = ndp.x[i];
            }
            /* polar ecliptic */
            sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
            /* cartesian equatorial */
            sl.swi_coortrf2(ndp.xreturn, 6, ndp.xreturn, 18, -oe.seps, oe.ceps);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                sl.swi_coortrf2(ndp.xreturn, 9, ndp.xreturn, 21, -oe.seps, oe.ceps);
            }
            if ((iflag & SweConst.SEFLG_NONUT) == 0) {
                sl.swi_coortrf2(ndp.xreturn, 18, ndp.xreturn, 18, -swed.nut.snut,
                                swed.nut.cnut);
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    sl.swi_coortrf2(ndp.xreturn, 21, ndp.xreturn, 21, -swed.nut.snut,
                                    swed.nut.cnut);
                }
            }
            /* polar equatorial */
            sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
            ndp.xflgs = iflag;
            ndp.iephe = iflag & SweConst.SEFLG_EPHMASK;
            if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
                /* node and apogee are referred to t;
                 * the ecliptic position must be transformed to t0 */
                /* rigorous algorithm */
                if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0
                    || (swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
                    for (i = 0; i <= 5; i++) {
                        x[i] = ndp.xreturn[18+i];
                    }
                    /* remove nutation */
                    if ((iflag & SweConst.SEFLG_NONUT)==0) {
                        swi_nutate(x, 0, iflag, true);
                    }
                    /* precess to J2000 */
                    sl.swi_precess(x, tjd, SwephData.J_TO_J2000);
                    if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                        swi_precess_speed(x, tjd, SwephData.J_TO_J2000);
                    }
                    if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0) {
                        swi_trop_ra2sid_lon(x, ndp.xreturn, 6, ndp.xreturn, 18, iflag,
                                            null);
                        /* project onto solar system equator */
                    } else if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
                        swi_trop_ra2sid_lon_sosy(x, ndp.xreturn, 6, ndp.xreturn, 18, iflag,
                                                 null);
                    }
                    /* to polar */
                    sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
                    sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
                    /* traditional algorithm;
                     * this is a bit clumsy, but allows us to keep the
                     * sidereal code together */
                } else {
                    sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
                    ndp.xreturn[0] -= swe_get_ayanamsa(ndp.teval) * SwissData.DEGTORAD;
                    sl.swi_polcart_sp(ndp.xreturn, 0, ndp.xreturn, 6);
                }
            } else if ((iflag & SweConst.SEFLG_J2000)!=0) {
                /* node and apogee are referred to t;
                 * the ecliptic position must be transformed to J2000 */
                for (i = 0; i <= 5; i++) {
                    x[i] = ndp.xreturn[18+i];
                }
                /* precess to J2000 */
                sl.swi_precess(x, tjd, SwephData.J_TO_J2000);
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    swi_precess_speed(x, tjd, SwephData.J_TO_J2000);
                }
                for (i = 0; i <= 5; i++) {
                    ndp.xreturn[18+i] = x[i];
                }
                sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
                sl.swi_coortrf2(ndp.xreturn, 18, ndp.xreturn, 6, swed.oec2000.seps,
                                swed.oec2000.ceps);
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    sl.swi_coortrf2(ndp.xreturn, 21, ndp.xreturn, 9, swed.oec2000.seps,
                                    swed.oec2000.ceps);
                }
                sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
            }
            /**********************
             * radians to degrees *
             **********************/
            /*if (!(iflag & SEFLG_RADIANS)) {*/
            for (i = 0; i < 2; i++) {
                ndp.xreturn[i] *= SwissData.RADTODEG;              /* ecliptic */
                ndp.xreturn[i+3] *= SwissData.RADTODEG;
                ndp.xreturn[i+12] *= SwissData.RADTODEG;   /* equator */
                ndp.xreturn[i+15] *= SwissData.RADTODEG;
            }
            ndp.xreturn[0] = sl.swe_degnorm(ndp.xreturn[0]);
            ndp.xreturn[12] = sl.swe_degnorm(ndp.xreturn[12]);
            /*}*/
        }
        return SweConst.OK;
    }

    /* lunar osculating elements, i.e.
     */ 
    private int intp_apsides(double tjd, int ipl, int iflag, StringBuffer serr) {
        int i;
        int flg1, flg2;
        PlanData ndp;
        Epsilon oe;
        Nut nut;
        double speed_intv = 0.1;
        double t, dt;
        double xpos[][] = new double[3][6], xx[] = new double[6], x[] = new double[6];
        int speedf1, speedf2;
        oe = swed.oec;
        nut = swed.nut;
        ndp = swed.nddat[ipl];
        /* if same calculation was done before, return
         * if speed flag has been turned on, recompute */
        flg1 = iflag & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        flg2 = ndp.xflgs & ~SweConst.SEFLG_EQUATORIAL & ~SweConst.SEFLG_XYZ;
        speedf1 = ndp.xflgs & SweConst.SEFLG_SPEED;
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        if (tjd == ndp.teval 
            && tjd != 0 
            && flg1 == flg2
            && ((speedf2==0) || (speedf1!=0))) {
            ndp.xflgs = iflag;
            ndp.iephe = iflag & SweConst.SEFLG_MOSEPH;
            return SweConst.OK;
        }
        /*********************************************
         * now three apsides * 
         *********************************************/
        for (t = tjd - speed_intv, i = 0; i < 3; t += speed_intv, i++) {
            if ( ((iflag & SweConst.SEFLG_SPEED)==0) && i != 1) continue;
            sm.swi_intp_apsides(t, xpos[i], ipl);
        }
        /************************************************************
         * apsis with speed                                         * 
         ************************************************************/
        for (i = 0; i < 3; i++) {
            xx[i] = xpos[1][i];
            xx[i+3] = 0;
        }
        if ((iflag & SweConst.SEFLG_SPEED) != 0) {
            xx[3] = sl.swe_difrad2n(xpos[2][0], xpos[0][0]) / speed_intv / 2.0;
            xx[4] = (xpos[2][1] - xpos[0][1]) / speed_intv / 2.0;
            xx[5] = (xpos[2][2] - xpos[0][2]) / speed_intv / 2.0;
        }
        // memset((void *) ndp.xreturn, 0, 24 * sizeof(double));
        for(int p=0;p<24;p++) { ndp.xreturn[p]=0.; }
        /* ecliptic polar to cartesian */
        sl.swi_polcart_sp(xx, xx);
        /* light-time */
        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0) {
            dt = Math.sqrt(sl.square_sum(xx)) * SweConst.AUNIT / SwephData.CLIGHT / 86400.0;     
            for (i = 1; i < 3; i++)
                xx[i] -= dt * xx[i+3];
        }
        for (i = 0; i <= 5; i++) {
            ndp.xreturn[i+6] = xx[i];
        }
        /*printf("%.10f, %.10f, %.10f, %.10f\n", xx[0] /DEGTORAD, xx[1] / DEGTORAD, xx [2], xx[3] /DEGTORAD);*/
        /* equatorial cartesian */
        sl.swi_coortrf2(ndp.xreturn, 6, ndp.xreturn, 18, -oe.seps, oe.ceps);
        if ((iflag & SweConst.SEFLG_SPEED) != 0)
            sl.swi_coortrf2(ndp.xreturn, 9, ndp.xreturn, 21, -oe.seps, oe.ceps);
        ndp.teval = tjd;
        ndp.xflgs = iflag;
        ndp.iephe = iflag & SweConst.SEFLG_EPHMASK;
        if ((iflag & SweConst.SEFLG_SIDEREAL) != 0) {
            /* apogee is referred to t; 
             * the ecliptic position must be transformed to t0 */
            /* rigorous algorithm */
            if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0) != 0
                || (swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE) != 0) {
                for (i = 0; i <= 5; i++)
                    x[i] = ndp.xreturn[18+i];
                /* precess to J2000 */
                sl.swi_precess(x, tjd, SwephData.J_TO_J2000);
                if ((iflag & SweConst.SEFLG_SPEED) != 0)
                    swi_precess_speed(x, tjd, SwephData.J_TO_J2000);
                if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0) != 0) 
                    swi_trop_ra2sid_lon(x, ndp.xreturn, 6, ndp.xreturn, 18, iflag, null);
                /* project onto solar system equator */
                else if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE) != 0)
                    swi_trop_ra2sid_lon_sosy(x, ndp.xreturn, 6, ndp.xreturn, 18, iflag, null);
                /* to polar */
                sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
                sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
            } else {
                /* traditional algorithm */
                sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0); 
                ndp.xreturn[0] -= swe_get_ayanamsa(ndp.teval) * SwissData.DEGTORAD;
                sl.swi_polcart_sp(ndp.xreturn, 0, ndp.xreturn, 6); 
                sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
            }
        } else if ((iflag & SweConst.SEFLG_J2000) != 0) {
            /* node and apogee are referred to t; 
             * the ecliptic position must be transformed to J2000 */
            for (i = 0; i <= 5; i++)
                x[i] = ndp.xreturn[18+i];
            /* precess to J2000 */
            sl.swi_precess(x, tjd, SwephData.J_TO_J2000);
            if ((iflag & SweConst.SEFLG_SPEED) != 0)
                swi_precess_speed(x, tjd, SwephData.J_TO_J2000);
            for (i = 0; i <= 5; i++)
                ndp.xreturn[18+i] = x[i];
            sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
            sl.swi_coortrf2(ndp.xreturn, 18, ndp.xreturn, 6, swed.oec2000.seps, swed.oec2000.ceps);
            if ((iflag & SweConst.SEFLG_SPEED) != 0)
                sl.swi_coortrf2(ndp.xreturn, 21, ndp.xreturn, 9, swed.oec2000.seps, swed.oec2000.ceps);
            sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
        } else {
            /* tropical ecliptic positions */
            /* precession has already been taken into account, but not nutation */
            if ((iflag & SweConst.SEFLG_NONUT) == 0) {
                swi_nutate(ndp.xreturn, 18, iflag, false);
            }
            /* equatorial polar */
            sl.swi_cartpol_sp(ndp.xreturn, 18, ndp.xreturn, 12);
            /* ecliptic cartesian */
            sl.swi_coortrf2(ndp.xreturn, 18, ndp.xreturn, 6, oe.seps, oe.ceps);
            if ((iflag & SweConst.SEFLG_SPEED) != 0)
                sl.swi_coortrf2(ndp.xreturn, 21, ndp.xreturn, 9, oe.seps, oe.ceps);
            if ((iflag & SweConst.SEFLG_NONUT) == 0) {
                sl.swi_coortrf2(ndp.xreturn, 6, ndp.xreturn, 6, nut.snut, nut.cnut);
                if ((iflag & SweConst.SEFLG_SPEED) != 0)
                    sl.swi_coortrf2(ndp.xreturn, 9, ndp.xreturn, 9, nut.snut, nut.cnut);
            }
            /* ecliptic polar */
            sl.swi_cartpol_sp(ndp.xreturn, 6, ndp.xreturn, 0);
        }
        /********************** 
         * radians to degrees *
         **********************/
        /*if ((iflag & SweConst.SEFLG_RADIANS)==0) {*/
        for (i = 0; i < 2; i++) {
            ndp.xreturn[i] *= SwissData.RADTODEG;		/* ecliptic */
            ndp.xreturn[i+3] *= SwissData.RADTODEG;
            ndp.xreturn[i+12] *= SwissData.RADTODEG;	/* equator */
            ndp.xreturn[i+15] *= SwissData.RADTODEG;
        }
        ndp.xreturn[0] = sl.swe_degnorm(ndp.xreturn[0]);
        ndp.xreturn[12] = sl.swe_degnorm(ndp.xreturn[12]);
        /*}*/
        return SweConst.OK;
    }
  
    /* transforms the position of the moon in a way we can use it
     * for calculation of osculating node and apogee:
     * precession and nutation (attention to speed vector!)
     * according to flags
     * iflag        flags
     * tjd          time for which the element is computed
     *              i.e. date of ecliptic
     * xx           array equatorial cartesian position and speed
     * serr         error string
     */
    int swi_plan_for_osc_elem(int iflag, double tjd, double xx[]) {
        int i;
        double x[]=new double[6];
        Nut nuttmp=new Nut();
        Nut nutp = nuttmp;   /* dummy assign, to silence gcc warning */
        Epsilon oe = swed.oec;
        Epsilon oectmp=new Epsilon();
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS)==0 && swed.jpldenum >= 403) {
            sl.swi_bias(xx, iflag, false);
        }/**/
        /************************************************
         * precession, equator 2000 -> equator of date  *
         * attention: speed vector has to be rotated,   *
         * but daily precession 0.137" may not be added!*/
        sl.swi_precess(xx, tjd, SwephData.J2000_TO_J);
        sl.swi_precess(xx, 3, tjd, SwephData.J2000_TO_J);
        /* epsilon */
        if (tjd == swed.oec.teps) {
            oe = swed.oec;
        } else if (tjd == SwephData.J2000) {
            oe = swed.oec2000;
        } else {
            calc_epsilon(tjd, oectmp);
            oe = oectmp;
        }
        /************************************************
         * nutation                                     *
         * again: speed vector must be rotated, but not *
         * added 'speed' of nutation                    *
         ************************************************/
        if ((iflag & SweConst.SEFLG_NONUT) == 0) {
            if (tjd == swed.nut.tnut) {
                nutp = swed.nut;
            } else if (tjd == SwephData.J2000) {
                nutp = swed.nut2000;
            } else if (tjd == swed.nutv.tnut) {
                nutp = swed.nutv;
            } else {
                nutp = nuttmp;
                sl.swi_nutation(tjd, nutp.nutlo);
                nutp.tnut = tjd;
                nutp.snut = Math.sin(nutp.nutlo[1]);
                nutp.cnut = Math.cos(nutp.nutlo[1]);
                nut_matrix(nutp, oe);
            }
            for (i = 0; i <= 2; i++) {
                x[i] = xx[0] * nutp.matrix[0][i] +
                    xx[1] * nutp.matrix[1][i] +
                    xx[2] * nutp.matrix[2][i];
            }
            /* speed:
             * rotation only */
            for (i = 0; i <= 2; i++) {
                x[i+3] = xx[3] * nutp.matrix[0][i] +
                    xx[4] * nutp.matrix[1][i] +
                    xx[5] * nutp.matrix[2][i];
            }
            for (i = 0; i <= 5; i++) {
                xx[i] = x[i];
            }
        }
        /************************************************
         * transformation to ecliptic                   *
         ************************************************/
        sl.swi_coortrf2(xx, xx, oe.seps, oe.ceps);
        sl.swi_coortrf2(xx, 3, xx, 3, oe.seps, oe.ceps);
        if ((iflag & SweConst.SEFLG_NONUT) == 0) {
            sl.swi_coortrf2(xx, xx, nutp.snut, nutp.cnut);
            sl.swi_coortrf2(xx, 3, xx, 3, nutp.snut, nutp.cnut);
        }
        return SweConst.OK;
    }

    static final MeffEle eff_arr[] = {
        /*
         * r , m_eff for photon passing the sun at min distance r (fraction of Rsun)
         * the values where computed with sun_model.c, which is a classic
         * treatment of a photon passing a gravity field, multiplied by 2.
         * The sun mass distribution m(r) is from Michael Stix, The Sun, p. 47.
         */
        new MeffEle(1.000, 1.000000),
        new MeffEle(0.990, 0.999979),
        new MeffEle(0.980, 0.999940),
        new MeffEle(0.970, 0.999881),
        new MeffEle(0.960, 0.999811),
        new MeffEle(0.950, 0.999724),
        new MeffEle(0.940, 0.999622),
        new MeffEle(0.930, 0.999497),
        new MeffEle(0.920, 0.999354),
        new MeffEle(0.910, 0.999192),
        new MeffEle(0.900, 0.999000),
        new MeffEle(0.890, 0.998786),
        new MeffEle(0.880, 0.998535),
        new MeffEle(0.870, 0.998242),
        new MeffEle(0.860, 0.997919),
        new MeffEle(0.850, 0.997571),
        new MeffEle(0.840, 0.997198),
        new MeffEle(0.830, 0.996792),
        new MeffEle(0.820, 0.996316),
        new MeffEle(0.810, 0.995791),
        new MeffEle(0.800, 0.995226),
        new MeffEle(0.790, 0.994625),
        new MeffEle(0.780, 0.993991),
        new MeffEle(0.770, 0.993326),
        new MeffEle(0.760, 0.992598),
        new MeffEle(0.750, 0.991770),
        new MeffEle(0.740, 0.990873),
        new MeffEle(0.730, 0.989919),
        new MeffEle(0.720, 0.988912),
        new MeffEle(0.710, 0.987856),
        new MeffEle(0.700, 0.986755),
        new MeffEle(0.690, 0.985610),
        new MeffEle(0.680, 0.984398),
        new MeffEle(0.670, 0.982986),
        new MeffEle(0.660, 0.981437),
        new MeffEle(0.650, 0.979779),
        new MeffEle(0.640, 0.978024),
        new MeffEle(0.630, 0.976182),
        new MeffEle(0.620, 0.974256),
        new MeffEle(0.610, 0.972253),
        new MeffEle(0.600, 0.970174),
        new MeffEle(0.590, 0.968024),
        new MeffEle(0.580, 0.965594),
        new MeffEle(0.570, 0.962797),
        new MeffEle(0.560, 0.959758),
        new MeffEle(0.550, 0.956515),
        new MeffEle(0.540, 0.953088),
        new MeffEle(0.530, 0.949495),
        new MeffEle(0.520, 0.945741),
        new MeffEle(0.510, 0.941838),
        new MeffEle(0.500, 0.937790),
        new MeffEle(0.490, 0.933563),
        new MeffEle(0.480, 0.928668),
        new MeffEle(0.470, 0.923288),
        new MeffEle(0.460, 0.917527),
        new MeffEle(0.450, 0.911432),
        new MeffEle(0.440, 0.905035),
        new MeffEle(0.430, 0.898353),
        new MeffEle(0.420, 0.891022),
        new MeffEle(0.410, 0.882940),
        new MeffEle(0.400, 0.874312),
        new MeffEle(0.390, 0.865206),
        new MeffEle(0.380, 0.855423),
        new MeffEle(0.370, 0.844619),
        new MeffEle(0.360, 0.833074),
        new MeffEle(0.350, 0.820876),
        new MeffEle(0.340, 0.808031),
        new MeffEle(0.330, 0.793962),
        new MeffEle(0.320, 0.778931),
        new MeffEle(0.310, 0.763021),
        new MeffEle(0.300, 0.745815),
        new MeffEle(0.290, 0.727557),
        new MeffEle(0.280, 0.708234),
        new MeffEle(0.270, 0.687583),
        new MeffEle(0.260, 0.665741),
        new MeffEle(0.250, 0.642597),
        new MeffEle(0.240, 0.618252),
        new MeffEle(0.230, 0.592586),
        new MeffEle(0.220, 0.565747),
        new MeffEle(0.210, 0.537697),
        new MeffEle(0.200, 0.508554),
        new MeffEle(0.190, 0.478420),
        new MeffEle(0.180, 0.447322),
        new MeffEle(0.170, 0.415454),
        new MeffEle(0.160, 0.382892),
        new MeffEle(0.150, 0.349955),
        new MeffEle(0.140, 0.316691),
        new MeffEle(0.130, 0.283565),
        new MeffEle(0.120, 0.250431),
        new MeffEle(0.110, 0.218327),
        new MeffEle(0.100, 0.186794),
        new MeffEle(0.090, 0.156287),
        new MeffEle(0.080, 0.128421),
        new MeffEle(0.070, 0.102237),
        new MeffEle(0.060, 0.077393),
        new MeffEle(0.050, 0.054833),
        new MeffEle(0.040, 0.036361),
        new MeffEle(0.030, 0.020953),
        new MeffEle(0.020, 0.009645),
        new MeffEle(0.010, 0.002767),
        new MeffEle(0.000, 0.000000)
    };
    double meff(double r) {
        double f, m;
        int i;
        if (r <= 0) {
            return 0.0;
        } else if (r >= 1) {
            return 1.0;
        }
        for (i = 0; eff_arr[i].r > r; i++) {
            ; /* empty body */
        }
        f = (r - eff_arr[i-1].r) / (eff_arr[i].r - eff_arr[i-1].r);
        m = eff_arr[i-1].m + f * (eff_arr[i].m - eff_arr[i-1].m);
        return m;
    }

    // Only used with SEFLG_SPEED3
    void denormalize_positions(double[] x0, double[] x1, double[] x2) {
        int i;
        /* x*[0] = ecliptic longitude, x*[12] = rectascension */
        for (i = 0; i <= 12; i += 12) {
            if (x1[i] - x0[i] < -180) {
                x0[i] -= 360;
            }
            if (x1[i] - x0[i] > 180) {
                x0[i] += 360;
            }
            if (x1[i] - x2[i] < -180) {
                x2[i] -= 360;
            }
            if (x1[i] - x2[i] > 180) {
                x2[i] += 360;
            }
        }
    }

    // Only used with SEFLG_SPEED3
    void calc_speed(double[] x0, double[] x1, double[] x2, double dt) {
        int i, j, k;
        double a, b;
        for (j = 0; j <= 18; j += 6) {
            for (i = 0; i < 3; i++) {
                k = j + i;
                b = (x2[k] - x0[k]) / 2;
                a = (x2[k] + x0[k]) / 2 - x1[k];
                x1[k+3] = (2 * a + b) / dt;
            }
        }
    }

    void swi_check_ecliptic(double tjd) {
        if (swed.oec2000.teps != SwephData.J2000) {
            calc_epsilon(SwephData.J2000, swed.oec2000);
        }
        if (tjd == SwephData.J2000) {
            swed.oec.teps = swed.oec2000.teps;
            swed.oec.eps = swed.oec2000.eps;
            swed.oec.seps = swed.oec2000.seps;
            swed.oec.ceps = swed.oec2000.ceps;
            return;
        }
        if (swed.oec.teps != tjd || tjd == 0) {
            calc_epsilon(tjd, swed.oec);
        }
    }

    /* computes nutation, if it is wanted and has not yet been computed.
     * if speed flag has been turned on since last computation,
     * nutation is recomputed */
    int chck_nut_nutflag = 0;
    void swi_check_nutation(double tjd, int iflag) {
        int speedf1, speedf2;
        double t;
        speedf1 = chck_nut_nutflag & SweConst.SEFLG_SPEED;
        speedf2 = iflag & SweConst.SEFLG_SPEED;
        if ((iflag & SweConst.SEFLG_NONUT) == 0
            && (tjd != swed.nut.tnut || tjd == 0
                || ((speedf1==0) && (speedf2!=0)))) {
            sl.swi_nutation(tjd, swed.nut.nutlo);
            swed.nut.tnut = tjd;
            swed.nut.snut = Math.sin(swed.nut.nutlo[1]);
            swed.nut.cnut = Math.cos(swed.nut.nutlo[1]);
            chck_nut_nutflag = iflag;
            nut_matrix(swed.nut, swed.oec);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                /* once more for 'speed' of nutation, which is needed for
                 * planetary speeds */
                t = tjd - SwephData.NUT_SPEED_INTV;
                sl.swi_nutation(t, swed.nutv.nutlo);
                swed.nutv.tnut = t;
                swed.nutv.snut = Math.sin(swed.nutv.nutlo[1]);
                swed.nutv.cnut = Math.cos(swed.nutv.nutlo[1]);
                nut_matrix(swed.nutv, swed.oec);
            }
        }
    }

    int plaus_iflag(int iflag) {
        int epheflag = 0;
        /* if topocentric bit, turn helio- and barycentric bits off */
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            iflag = iflag & ~(SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR);
        }
        /* if heliocentric bit, turn aberration and deflection off */
        if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
            iflag |= SweConst.SEFLG_NOABERR | SweConst.SEFLG_NOGDEFL;
            /*iflag |= SEFLG_TRUEPOS;*/
        }
        /* same, if barycentric bit */
        if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
            iflag |= SweConst.SEFLG_NOABERR | SweConst.SEFLG_NOGDEFL;
            /*iflag |= SEFLG_TRUEPOS;*/
        }
        /* if no_precession bit is set, set also no_nutation bit */
        if ((iflag & SweConst.SEFLG_J2000)!=0) {
            iflag |= SweConst.SEFLG_NONUT;
        }
        /* if truepos is set, turn off grav. defl. and aberration */
        if ((iflag & SweConst.SEFLG_TRUEPOS)!=0) {
            iflag |= (SweConst.SEFLG_NOGDEFL | SweConst.SEFLG_NOABERR);
        }
        /* if sidereal bit is set, set also no_nutation bit */
        if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
            iflag |= SweConst.SEFLG_NONUT;
        }
        if ((iflag & SweConst.SEFLG_MOSEPH)!=0) {
            epheflag = SweConst.SEFLG_MOSEPH;
        }
        if ((iflag & SweConst.SEFLG_SWIEPH)!=0) {
            epheflag = SweConst.SEFLG_SWIEPH;
        }
        if ((iflag & SweConst.SEFLG_JPLEPH)!=0) {
            epheflag = SweConst.SEFLG_JPLEPH;
        }
        if (epheflag == 0) {
            epheflag = SweConst.SEFLG_DEFAULTEPH;
        }
        /* delete wrong ephe bits from flag */
        iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | epheflag;

        return iflag;
    }

    int swe_fixstar_found(StringBuffer serr, String s, StringBuffer star,
                          double tjd, int iflag, int iflgsave, int epheflag,
                          double[] xx) {
        double xpo[] = null;
        double ra_s, ra_pm, de_pm, ra, de, t, cosra, cosde, sinra, sinde;
        double ra_h, ra_m, de_d, de_m, de_s;
        String sde_d;
        double epoch, radv, parall, u;
        double x[]=new double[6];
        double xxsv[]=new double[6];
        double xobs[]=new double[6];
        int retc;
        PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
        PlanData psdp = swed.pldat[SwephData.SEI_SUNBARY];
        Epsilon oe = swed.oec2000;

        String[] cpos=new String[20];
        StringTokenizer tk=new StringTokenizer(s,",");
        int i=tk.countTokens();
        int n=0;
        while(tk.hasMoreTokens() && n<20) {
            cpos[n++]=tk.nextToken();
        }
        cpos[0]=cpos[0].trim();
        cpos[1]=cpos[1].trim();
        if (i < 13) {
            if (serr!=null) {
                serr.setLength(0);
                serr.append("data of star '"+cpos[0]+","+cpos[1]+"' incomplete");
            }
            return swe_fixstar_error(xx,SweConst.ERR);
        }
        // JAVA: Grrr: zumindest cpos[2] muss keine Zahl sein, aber es FAENGT
        // moeglicherweise mit einer Zahl AN!!!
        int idx=cpos[2].length();
        while(true) {
            try {
                epoch = Double.valueOf(cpos[2].substring(0,idx)).doubleValue();
                break;
            } catch (NumberFormatException nf) {
                idx--;
                if (idx==0) { epoch=0.; break; }
            }
        }
        ra_h = new Double(cpos[3]).doubleValue();
        ra_m = new Double(cpos[4]).doubleValue();
        ra_s = new Double(cpos[5]).doubleValue();
        de_d = new Double(cpos[6]).doubleValue();
        sde_d = cpos[6];
        de_m = new Double(cpos[7]).doubleValue();
        de_s = new Double(cpos[8]).doubleValue();
        ra_pm = new Double(cpos[9]).doubleValue();
        de_pm = new Double(cpos[10]).doubleValue();
        radv = new Double(cpos[11]).doubleValue();
        parall = new Double(cpos[12]).doubleValue();
        /* return trad. name, nomeclature name */
        if (cpos[0].length() > SweConst.SE_MAX_STNAME) {
            cpos[0]=cpos[0].substring(0,SweConst.SE_MAX_STNAME);
        }
        if (cpos[1].length() > SweConst.SE_MAX_STNAME-1) {
            cpos[1]=cpos[1].substring(0,SweConst.SE_MAX_STNAME-1);
        }
        star.setLength(0);
        star.append(cpos[0]+","+cpos[1]);
        /****************************************
         * position and speed (equinox)
         ****************************************/
        /* ra and de in degrees */
        ra = (ra_s / 3600.0 + ra_m / 60.0 + ra_h) * 15.0;
        if (sde_d.indexOf('-') < 0) {
            de = de_s / 3600.0 + de_m / 60.0 + de_d;
        } else {
            de = -de_s / 3600.0 - de_m / 60.0 + de_d;
        }
        /* speed in ra and de, degrees per century */
        ra_pm = ra_pm * 15 / 3600.0;
        de_pm /= 3600.0;
        /* parallax, degrees */
        if (parall > 1) {
            parall = (1 / parall / 3600);
        } else {
            parall /= 3600;
        }
        /* radial velocity in AU per century */
        radv *= SwephData.KM_S_TO_AU_CTY;
        /* radians */
        ra *= SwissData.DEGTORAD;
        de *= SwissData.DEGTORAD;
        ra_pm *= SwissData.DEGTORAD;
        de_pm *= SwissData.DEGTORAD;
        parall *= SwissData.DEGTORAD;
        x[0] = ra;
        x[1] = de;
        x[2] = 1;     /* -> unit vector */
        /* cartesian */
        sl.swi_polcart(x, x);
        /*space motion vector */
        cosra = Math.cos(ra);
        cosde = Math.cos(de);
        sinra = Math.sin(ra);
        sinde = Math.sin(de);
        x[3] = -ra_pm * cosde * sinra - de_pm * sinde * cosra
            + radv * parall * cosde * cosra;
        x[4] = ra_pm * cosde * cosra - de_pm * sinde * sinra
            + radv * parall * cosde * sinra;
        x[5] = de_pm * cosde + radv * parall * sinde;
        x[3] /= 36525;
        x[4] /= 36525;
        x[5] /= 36525;
        /******************************************
         * FK5
         ******************************************/
        if (epoch == 1950) {
            sl.swi_FK4_FK5(x, SwephData.B1950);
            sl.swi_precess(x, SwephData.B1950, SwephData.J_TO_J2000);
            sl.swi_precess(x, 3, SwephData.B1950, SwephData.J_TO_J2000);
        }
        /* FK5 to ICRS, if jpl ephemeris is referred to ICRS 
         * With data that are already ICRS, epoch = 0 */
        if (epoch != 0) {
            sl.swi_icrs2fk5(x, iflag, true);
            /* with ephemerides < DE403, we now convert to J2000 */
            if (swed.jpldenum < 403)
                sl.swi_bias(x, iflag, false);
        }
        /****************************************************
         * earth/sun
         * for parallax, light deflection, and aberration,
         ****************************************************/
        retc = main_planet(tjd, SwephData.SEI_EARTH, epheflag, iflag, serr);
        if (retc != SweConst.OK) {
            return swe_fixstar_error(xx,SweConst.ERR);
        }
        /* iflag (ephemeris bit) may have changed in main_planet() */
        iflag = swed.pldat[SwephData.SEI_EARTH].xflgs;
        /************************************
         * observer: geocenter or topocenter
         ************************************/
        /* if topocentric position is wanted  */
        if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            if (swed.topd.teval != pedp.teval
                || swed.topd.teval == 0) {
                if (swi_get_observer(pedp.teval, iflag, SwephData.DO_SAVE, xobs, serr)!=
                    SweConst.OK) {
                    return SweConst.ERR;
                }
            } else {
                for (i = 0; i <= 5; i++) {
                    xobs[i] = swed.topd.xobs[i];
                }
            }
            /* barycentric position of observer */
            for (i = 0; i <= 5; i++) {
                xobs[i] = xobs[i] + pedp.x[i];
            }
        } else {
            /* barycentric position of geocenter */
            for (i = 0; i <= 5; i++) {
                xobs[i] = pedp.x[i];
            }
        }
        /************************************
         * position and speed at tjd        *
         ************************************/
        if (epoch == 1950) {
            t= (tjd - SwephData.B1950);   /* days since 1950.0 */
        } else { /* epoch == 2000 */
            t= (tjd - SwephData.J2000);   /* days since 2000.0 */
        }
        /* for parallax */
        if ((iflag & SweConst.SEFLG_HELCTR)!=0 &&
            (iflag & SweConst.SEFLG_MOSEPH)!=0) {
            xpo = null;         /* no parallax, if moshier and heliocentric */
        } else if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
            xpo = psdp.x;
        } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
            xpo = null;         /* no parallax, if barycentric */
        } else {
            xpo = xobs;
        }
        if (xpo == null) {
            for (i = 0; i <= 2; i++) {
                x[i] += t * x[i+3];
            }
        } else {
            for (i = 0; i <= 2; i++) {
                x[i] += t * x[i+3] - parall * xpo[i];
                x[i+3] -= parall * xpo[i+3];
            }
        }
        /************************************
         * relativistic deflection of light *
         ************************************/
        for (i = 0; i <= 5; i++) {
            x[i] *= 10000;      /* great distance, to allow
                                 * algorithm used with planets */
        }
        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0 &&
            (iflag & SweConst.SEFLG_NOGDEFL) == 0) {
            swi_deflect_light(x, 0, 0, iflag & SweConst.SEFLG_SPEED);
        }
        /**********************************
         * 'annual' aberration of light   *
         * speed is incorrect !!!         *
         **********************************/
        if ((iflag & SweConst.SEFLG_TRUEPOS) == 0 &&
            (iflag & SweConst.SEFLG_NOABERR) == 0) {
            swi_aberr_light(x, xpo, iflag & SweConst.SEFLG_SPEED);
        }
        /* ICRS to J2000 */
        if ((iflag & SweConst.SEFLG_ICRS) == 0 && swed.jpldenum >= 403) {
            sl.swi_bias(x, iflag, false);
        }/**/
        /* save J2000 coordinates; required for sidereal positions */
        for (i = 0; i <= 5; i++) {
            xxsv[i] = x[i];
        }
        /************************************************
         * precession, equator 2000 -> equator of date *
         ************************************************/
        /*x[0] = -0.374018403; x[1] = -0.312548592; x[2] = -0.873168719;*/
        if ((iflag & SweConst.SEFLG_J2000) == 0) {
            sl.swi_precess(x, tjd, SwephData.J2000_TO_J);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                swi_precess_speed(x, tjd, SwephData.J2000_TO_J);
            }
            oe = swed.oec;
        } else {
            oe = swed.oec2000;
        }
        /************************************************
         * nutation                                     *
         ************************************************/
        if ((iflag & SweConst.SEFLG_NONUT) == 0) {
            swi_nutate(x, 0, 0, false);
        }
        /************************************************
         * unit vector (distance = 1)                   *
         ************************************************/
        u = Math.sqrt(sl.square_sum(x));
        for (i = 0; i <= 5; i++) {
            x[i] /= u;
        }
        u = Math.sqrt(sl.square_sum(xxsv));
        for (i = 0; i <= 5; i++) {
            xxsv[i] /= u;
        }
        /************************************************
         * set speed = 0, because not correct (aberration)
         ************************************************/
        for (i = 3; i <= 5; i++) {
            x[i] = xxsv[i] = 0;
        }
        /************************************************
         * transformation to ecliptic.                  *
         * with sidereal calc. this will be overwritten *
         * afterwards.                                  *
         ************************************************/
        if ((iflag & SweConst.SEFLG_EQUATORIAL) == 0) {
            sl.swi_coortrf2(x, x, oe.seps, oe.ceps);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                sl.swi_coortrf2(x, 3, x, 3, oe.seps, oe.ceps);
            }
            if ((iflag & SweConst.SEFLG_NONUT) == 0) {
                sl.swi_coortrf2(x, x, swed.nut.snut, swed.nut.cnut);
                if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                    sl.swi_coortrf2(x, 3, x, 3, swed.nut.snut, swed.nut.cnut);
                }
            }
        }
        /************************************
         * sidereal positions               *
         ************************************/
        if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
            /* rigorous algorithm */
            if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0) {
                if (swi_trop_ra2sid_lon(xxsv, x, xxsv, iflag, serr) != SweConst.OK) {
                    return SweConst.ERR;
                }
                if ((iflag & SweConst.SEFLG_EQUATORIAL)!=0) {
                    for (i = 0; i <= 5; i++) {
                        x[i] = xxsv[i];
                    }
                }
                /* project onto solar system equator */
            } else if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
                if (swi_trop_ra2sid_lon_sosy(xxsv, x, xxsv, iflag, serr) !=
                    SweConst.OK) {
                    return SweConst.ERR;
                }
                if ((iflag & SweConst.SEFLG_EQUATORIAL)!=0) {
                    for (i = 0; i <= 5; i++) {
                        x[i] = xxsv[i];
                    }
                }
                /* traditional algorithm */
            } else {
                sl.swi_cartpol_sp(x, x);
                x[0] -= swe_get_ayanamsa(tjd) * SwissData.DEGTORAD;
                sl.swi_polcart_sp(x, x);
            }
        }
        /************************************************
         * transformation to polar coordinates          *
         ************************************************/
        if ((iflag & SweConst.SEFLG_XYZ) == 0) {
            sl.swi_cartpol_sp(x, x);
        }
        /**********************
         * radians to degrees *
         **********************/
        if ((iflag & SweConst.SEFLG_RADIANS) == 0 &&
            (iflag & SweConst.SEFLG_XYZ) == 0) {
            for (i = 0; i < 2; i++) {
                x[i] *= SwissData.RADTODEG;
                x[i+3] *= SwissData.RADTODEG;
            }
        }
        for (i = 0; i <= 5; i++) {
            xx[i] = x[i];
        }
        /* if no ephemeris has been specified, do not return chosen ephemeris */
        if ((iflgsave & SweConst.SEFLG_EPHMASK) == 0) {
            iflag = iflag & ~SweConst.SEFLG_DEFAULTEPH;
        }
        iflag = iflag & ~SweConst.SEFLG_SPEED;
        return iflag;
    }

    int swe_fixstar_error(double[] xx, int retc) {
        for (int i = 0; i <= 5; i++) {
            xx[i] = 0;
        }
        return retc;
    }



    void swi_force_app_pos_etc() {
        int i;
        for (i = 0; i < SwephData.SEI_NPLANETS; i++) {
            swed.pldat[i].xflgs = -1;
        }
        for (i = 0; i < SwephData.SEI_NNODE_ETC; i++) {
            swed.nddat[i].xflgs = -1;
        }
        for (i = 0; i < SweConst.SE_NPLANETS; i++) {
            swed.savedat[i].tsave = 0;
            swed.savedat[i].iflgsave = -1;
        }
    }

    int swi_get_observer(double tjd, int iflag, boolean do_save, double xobs[],
                         StringBuffer serr) {
        int i;
        double sidt, delt, tjd_ut, eps, nut, nutlo[]=new double[2];
        double f = SwephData.EARTH_OBLATENESS;
        double re = SwephData.EARTH_RADIUS;
        double cosfi, sinfi, cc, ss, cosl, sinl, h;
        if (!swed.geopos_is_set) {
            if (serr != null) {
                serr.setLength(0);
                serr.append("geographic position has not been set");
            }
            return SweConst.ERR;
        }
        /* geocentric position of observer depends on sidereal time,
         * which depends on UT.
         * compute UT from ET. this UT will be slightly different
         * from the user's UT, but this difference is extremely small.
         */
        delt = SweDate.getDeltaT(tjd);
        tjd_ut = tjd - delt;
        if (swed.oec.teps == tjd && swed.nut.tnut == tjd) {
            eps = swed.oec.eps;
            nutlo[1] = swed.nut.nutlo[1];
            nutlo[0] = swed.nut.nutlo[0];
        } else {
            eps = sl.swi_epsiln(tjd);
            if ((iflag & SweConst.SEFLG_NONUT)==0) {
                sl.swi_nutation(tjd, nutlo);
            }
        }
        if ((iflag & SweConst.SEFLG_NONUT)!=0) {
            nut = 0;
        } else {
            eps += nutlo[1];
            nut = nutlo[0];
        }
        /* mean or apparent sidereal time, depending on whether or
         * not SEFLG_NONUT is set */
        sidt = sl.swe_sidtime0(tjd_ut, eps, nut);
        sidt *= 15;   /* in degrees */
        /* length of position and speed vectors;
         * the height above sea level must be taken into account.
         * with the moon, an altitude of 3000 m makes a difference
         * of about 2 arc seconds.
         * height is referred to the average sea level. however,
         * the spheroid (geoid), which is defined by the average
         * sea level (or rather by all points of same gravitational
         * potential), is of irregular shape and cannot easily
         * be taken into account. therefore, we refer height to
         * the surface of the ellipsoid. the resulting error
         * is below 500 m, i.e. 0.2 - 0.3 arc seconds with the moon.
         */
        cosfi = Math.cos(swed.topd.geolat * SwissData.DEGTORAD);
        sinfi = Math.sin(swed.topd.geolat * SwissData.DEGTORAD);
        cc= 1 / Math.sqrt(cosfi * cosfi + (1-f) * (1-f) * sinfi * sinfi);
        ss= (1-f) * (1-f) * cc;
        /* neglect polar motion (displacement of a few meters), as long as 
         * we use the earth ellipsoid */
        /* ... */
        /* add sidereal time */
        cosl = Math.cos((swed.topd.geolon + sidt) * SwissData.DEGTORAD);
        sinl = Math.sin((swed.topd.geolon + sidt) * SwissData.DEGTORAD);
        h = swed.topd.geoalt;
        xobs[0] = (re * cc + h) * cosfi * cosl;
        xobs[1] = (re * cc + h) * cosfi * sinl;
        xobs[2] = (re * ss + h) * sinfi;
        /* polar coordinates */
        sl.swi_cartpol(xobs, xobs);
        /* speed */
        xobs[3] = SwephData.EARTH_ROT_SPEED;
        xobs[4] = xobs[5] = 0;
        sl.swi_polcart_sp(xobs, xobs);
        /* to AUNIT */
        for (i = 0; i <= 5; i++) {
            xobs[i] /= SweConst.AUNIT;
        }
        /* subtract nutation, set backward flag */
        if ((iflag & SweConst.SEFLG_NONUT)==0) {
            sl.swi_coortrf2(xobs, xobs, -swed.nut.snut, swed.nut.cnut);
            if ((iflag & SweConst.SEFLG_SPEED)!=0) {
                sl.swi_coortrf2(xobs, 3, xobs, 3, -swed.nut.snut, swed.nut.cnut);
            }
            swi_nutate(xobs, 0, iflag, true);
        }
        /* precess to J2000 */
        sl.swi_precess(xobs, tjd, SwephData.J_TO_J2000);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
            swi_precess_speed(xobs, tjd, SwephData.J_TO_J2000);
        }
        /* neglect frame bias (displacement of 45cm) */
        /* ... */
        /* save */
        if (do_save) {
            for (i = 0; i <= 5; i++) {
                swed.topd.xobs[i] = xobs[i];
            }
            swed.topd.teval = tjd;
            swed.topd.tjd_ut = tjd_ut;  /* -> save area */
        }
        return SweConst.OK;
    }

    /* Equation of Time
     *
     * The function returns the difference between
     * local apparent and local mean time in days.
     * E = LAT - LMT
     * Input variable tjd is ET.
     * Algorithm according to Meeus, German, p. 190ff.
     */
    /**
     * Returns the difference between local apparent and local mean time in
     * days. E = LAT - LMT
     * @param tjd input date in julian days (ET)
     * @param serr buffer for error message on output
     * @return the difference
     */
    public int swe_time_equ(double tjd, DblObj E, StringBuffer serr) {
        double L0, dpsi, eps, x[]=new double[6], nutlo[]=new double[2];
        double tau = (tjd - SwephData.J2000) / 365250;
        double tau2 = tau * tau;
        double tau3 = tau * tau2;
        double tau4 = tau * tau3;
        double tau5 = tau * tau4;
        L0 = 280.4664567 + sl.swe_degnorm(tau * 360007.6982779)
            + tau2 * 0.03032028
            + tau3 * 1 / 49931
            - tau4 * 1 / 15299
            - tau5 * 1 / 1988000;
        sl.swi_nutation(tjd, nutlo);
        eps = (sl.swi_epsiln(tjd) + nutlo[1]) * SwissData.RADTODEG;
        dpsi = nutlo[0] * SwissData.RADTODEG;
        if (swe_calc(tjd, SweConst.SE_SUN, SweConst.SEFLG_EQUATORIAL, x, serr) ==
            SweConst.ERR) {
            return SweConst.ERR;
        }
        E.val = sl.swe_degnorm(L0 - 0.0057183 - x[0] + dpsi *
                               Math.cos(eps * SwissData.DEGTORAD));
        if (E.val > 180) {
            E.val -= 360;
        }
        E.val *= 4 / 1440.0;
        return SweConst.OK;
    }

    double dot_prod(double x[], double y[]) {
        ////#ifdef TRACE0
        //    Trace.level++;
        //    Trace.trace(Trace.level, "SwissEph.dot_prod(double[], double[])");
        ////#ifdef TRACE1
        //    Trace.printDblArr("x", x);
        //    Trace.printDblArr("y", y);
        ////#endif /* TRACE1 */
        //    Trace.level--;
        ////#endif /* TRACE0 */
        return x[0]*y[0]+x[1]*y[1]+x[2]*y[2];
    }
    double dot_prod(double x[], double y[], int yOffs) {
        ////#ifdef TRACE0
        //    Trace.level++;
        //    Trace.trace(Trace.level, "SwissEph.dot_prod(double[], double[], int)");
        ////#ifdef TRACE1
        //    Trace.printDblArr("x", x);
        //    Trace.printDblArr("y", y);
        //    DevNull.println("    yOffs: " + yOffs);
        ////#endif /* TRACE1 */
        //    Trace.level--;
        ////#endif /* TRACE0 */
        return x[0]*y[yOffs]+x[1]*y[1+yOffs]+x[2]*y[2+yOffs];
    }

    
} // Ende class SwissEph

class MeffEle {
    double r;
    double m;

    MeffEle(double r, double m) {
        ////#ifdef TRACE0
        //    Trace.level++;
        //    Trace.trace(Trace.level, "MeffEle(double, double)");
        ////#ifdef TRACE1
        //    DevNull.println("    r: " + Trace.fmtDbl(r) + "\n    m: " + Trace.fmtDbl(m));
        ////#endif /* TRACE1 */
        ////#endif /* TRACE0 */
        this.r=r; this.m=m;
        ////#ifdef TRACE0
        //    Trace.level--;
        ////#endif /* TRACE0 */
    }

}
