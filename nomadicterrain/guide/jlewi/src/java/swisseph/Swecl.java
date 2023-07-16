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

/**
* This class initiates the calculation of solar and lunar eclipses, of
* sun risetime and time of sunset, and lastly the calculation of the
* phenomena phase angle, phase, elongation of planet, apparent diameter
* of disc and apparent magnitude of the sun, moon, any planet or asteroid.<BR>
*
* <P><I><B>You will find the complete documentation for the original
* SwissEphemeris package at <A HREF="http://www.astro.ch/swisseph/sweph_g.htm">
* http://www.astro.ch/swisseph/sweph_g.htm</A>. By far most of the information 
* there is directly valid for this port to Java as well.</B></I>
*/
class Swecl {

  SwissEph  sw=null;
  SwissLib  sl=null;
  Swemmoon  sm=null;
  SwissData swed=null;


  private static final double DSUN=(1392000000.0 / SweConst.AUNIT);
  private static final double DMOON=(3476300.0 / SweConst.AUNIT);
  private static final double DEARTH=(6378140.0 * 2 / SweConst.AUNIT);
  private static final double RSUN=(DSUN / 2);
  private static final double RMOON=(DMOON / 2);
  private static final double REARTH=(DEARTH / 2);
  private static final int SEI_OCC_FAST=(16 * 1024);

  private static final double lnlog=Math.log(10);
  private double log10(double x) { return Math.log(x)/lnlog; }


  /**
  * Creates a new instance of this object.
  */
  Swecl() {
    this(null, null, null, null);
    sw=new SwissEph();
    sl=new SwissLib();
    sm=new Swemmoon();
    swed=new SwissData();
  }

  /**
  * Creates a new instance of this object and uses all the given
  * objects, as far as they are not null.
  * @param sw A SwissEph object that might already be available at the time
  * of creation of this object.
  * @param sl A SwissLib object that might already be available at the time
  * of creation of this object.
  * @param sm A Swemmoon object that might already be available at the time
  * of creation of this object.
  * @param swed A SwissData object that might already be available at the time
  * of creation of this object.
  */
  Swecl(SwissEph sw, SwissLib sl, Swemmoon sm, SwissData swed) {
    this.sw=sw;
    this.sl=sl;
    this.sm=sm;
    this.swed=swed;
    if (sw==null) { this.sw=new SwissEph(); }
    if (sl==null) { this.sl=new SwissLib(); }
    if (sm==null) { this.sm=new Swemmoon(); }
    if (swed==null) { this.swed=new SwissData(); }
  }

  /* Computes geographic location and type of solar eclipse
   * for a given tjd
   * iflag:        to indicate ephemeris to be used
   *                        (SEFLG_JPLEPH, SEFLG_SWIEPH, SEFLG_MOSEPH)
   *
   * Algorithms for the central line is taken from Montenbruck, pp. 179ff.,
   * with the exception, that we consider refraction for the maxima of
   * partial and noncentral eclipses.
   * Geographical positions are referred to sea level / the mean ellipsoid.
   *
   * Errors:
   * - from uncertainty of JPL-ephemerides (0.01 arcsec):
   *        about 40 meters
   * - from displacement of shadow points by atmospheric refraction:
   *      a few meters
   * - from deviation of the geoid from the ellipsoid
   *      a few meters
   * - from polar motion
   *      a few meters
   * For geographical locations that are interesting for observation,
   * the error is always < 100 m.
   * However, if the sun is close to the horizon,
   * all of these errors can grow up to a km or more.
   *
   * Function returns:
   * -1 (ERR)        on error (e.g. if swe_calc() for sun or moon fails)
   * 0                if there is no solar eclipse at tjd
   * SE_ECL_TOTAL
   * SE_ECL_ANNULAR
   * SE_ECL_TOTAL | SE_ECL_CENTRAL
   * SE_ECL_TOTAL | SE_ECL_NONCENTRAL
   * SE_ECL_ANNULAR | SE_ECL_CENTRAL
   * SE_ECL_ANNULAR | SE_ECL_NONCENTRAL
   * SE_ECL_PARTIAL
   *
   * geopos[0]:        geographic longitude of central line
   * geopos[1]:        geographic latitude of central line
   *
   * not implemented so far:
   *
   * geopos[2]:        geographic longitude of northern limit of umbra
   * geopos[3]:        geographic latitude of northern limit of umbra
   * geopos[4]:        geographic longitude of southern limit of umbra
   * geopos[5]:        geographic latitude of southern limit of umbra
   * geopos[6]:        geographic longitude of northern limit of penumbra
   * geopos[7]:        geographic latitude of northern limit of penumbra
   * geopos[8]:        geographic longitude of southern limit of penumbra
   * geopos[9]:        geographic latitude of southern limit of penumbra
   *
   * Attention: "northern" and "southern" limits of umbra do not
   * necessarily correspond to the northernmost or southernmost
   * geographic position, where the total, annular, or partial
   * phase is visible at a given time.
   * Imagine a situation in northern summer, when the sun illuminates
   * the northern polar circle. The southernmost point of the core
   * shadow may then touch the north pole, and therefore the
   * northernmost point will be more in the south.
   * Note also that with annular eclipses, the northern edge is
   * usually geographically the southern one. With annular-total
   * ones, the two lines cross, usually twice. The maximum is always
   * total in such cases.
   *
   * attr[0]        fraction of solar diameter covered by moon (magnitude)
   * attr[1]        ratio of lunar diameter to solar one
   * attr[2]        fraction of solar disc covered by moon (obscuration)
   * attr[3]      diameter of core shadow in km
   * attr[4]        azimuth of sun at tjd
   * attr[5]        true altitude of sun above horizon at tjd
   * attr[6]        apparent altitude of sun above horizon at tjd
   * attr[7]        angular distance of moon from sun in degrees
   *         declare as attr[20] at least !
   */
  /**
  * Computes the geographic location for a given time, where a solar
  * eclipse is central (or maximum for a non-central eclipse).
  * <P>Output parameters:<BLOCKQUOTE><P><CODE>
  * geopos[0]:&nbsp;&nbsp;&nbsp;geographic longitude of central line<BR>
  * geopos[1]:&nbsp;&nbsp;&nbsp;geographic latitude of central line<BR>
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
  * </CODE><P></BLOCKQUOTE>
  * <B>Attention: geopos must be a double[10], attr a double[20]!</B>
  * @param tjd_ut The Julian Day number in UT
  * @param ifl To indicate, which ephemeris to be used (SEFLG_JPLEPH,
  * SEFLG_SWIEPH, SEFLG_MOSEPH)
  * @param geopos An array[10], on return containing the geographic positions.
  * @param attr An array[20], on return containing the attributes of the
  * eclipse as above.
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails.
  * @return -1 (SweConst.ERR), if the calculation failed<BR>
  * 0 (SweConst.OK), if there is no solar eclipse at that time<BR>
  * SweConst.SE_ECL_TOTAL<BR>
  * SweConst.SE_ECL_ANNULAR<BR>
  * SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_CENTRAL<BR>
  * SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_NONCENTRAL<BR>
  * SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_CENTRAL<BR>
  * SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_NONCENTRAL<BR>
  * SweConst.SE_ECL_PARTIAL<BR>
  */
  int swe_sol_eclipse_where(double tjd_ut,
                            int ifl,
                            double[] geopos,
                            double[] attr,
                            StringBuffer serr) {
    int retflag, retflag2;
    double dcore[]=new double[10];
    ifl &= SweConst.SEFLG_EPHMASK;
    if ((retflag = eclipse_where(tjd_ut, SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) < 0) {
      return retflag;
    }
    if ((retflag2 = eclipse_how(tjd_ut, SweConst.SE_SUN, null, ifl, geopos[0], geopos[1], 0, attr, serr)) == SweConst.ERR) {
      return retflag2;
    }
    attr[3] = dcore[0];
    return retflag;
  }

  int swe_lun_occult_where(double tjd_ut,
                           int ipl,
                           StringBuffer starname,
                           int ifl,
                           double[] geopos,
                           double[] attr,
                           StringBuffer serr) {
    int retflag, retflag2;
    double dcore[]=new double[10];
    ifl &= SweConst.SEFLG_EPHMASK;
    if ((retflag = eclipse_where(tjd_ut, ipl, starname, ifl, geopos, dcore, serr)) < 0) {
      return retflag;
    }
    if ((retflag2 = eclipse_how(tjd_ut, ipl, starname, ifl, geopos[0], geopos[1], 0, attr, serr)) == SweConst.ERR) {
      return retflag2;
    }
    attr[3] = dcore[0];
    return retflag;
  }

  /* Used by several swe_sol_eclipse_ functions.
   * Like swe_sol_eclipse_where(), but instead of attr[0], it returns:
   *
   * dcore[0]:        core shadow width in km
   * dcore[2]:        distance of shadow axis from geocenter r0
   * dcore[3]:        diameter of core shadow on fundamental plane d0
   * dcore[4]:        diameter of half-shadow on fundamental plane D0
   */
  private int eclipse_where(double tjd_ut, int ipl, StringBuffer starname, int ifl,
                            double[] geopos, double[] dcore, StringBuffer serr) {
    int i;
    int retc = 0, niter = 0;
    double e[]=new double[6], et[]=new double[6], erm[]=new double[6],
           rm[]=new double[6], rs[]=new double[6], rmt[]=new double[6],
           rst[]=new double[6], xs[]=new double[6], xst[]=new double[6];
    double xssv[]=new double[16], x[]=new double[6];
    double lm[]=new double[6], ls[]=new double[6], lx[]=new double[6];
    double dsm, dsmt, d0, D0, s0, r0, d, s, dm;
    double de = 6378140.0 / SweConst.AUNIT;
    double earthobl = 1 - SwephData.EARTH_OBLATENESS;
    double deltat, tjd, sidt;
    double drad;
    double sinf1, sinf2, cosf1, cosf2;
    int iflag, iflag2;
    /* double ecce = Math.sqrt(2 * SwephData.EARTH_OBLATENESS - SwephData.EARTH_OBLATENESS * SwephData.EARTH_OBLATENESS); */
    boolean no_eclipse = false;
    Epsilon oe = swed.oec;
    for (i = 0; i < 10; i++)
      dcore[i] = 0;
    /* nutation need not be in lunar and solar positions,
     * if mean sidereal time will be used */
    iflag = SweConst.SEFLG_SPEED | SweConst.SEFLG_EQUATORIAL | ifl;
    iflag2 = iflag | SweConst.SEFLG_RADIANS;
    iflag  = iflag | SweConst.SEFLG_XYZ;
    deltat = SweDate.getDeltaT(tjd_ut);
    tjd = tjd_ut + deltat;
    /* moon in cartesian coordinates */
    if ((retc = sw.swe_calc(tjd, SweConst.SE_MOON, iflag, rm, serr)) == SweConst.ERR) {
      return retc;
    }
    /* moon in polar coordinates */
    if ((retc = sw.swe_calc(tjd, SweConst.SE_MOON, iflag2, lm, serr)) == SweConst.ERR)
      return retc;
    /* sun in cartesian coordinates */
    if ((retc = calc_planet_star(tjd, ipl, starname, iflag, rs, serr)) == SweConst.ERR)
      return retc;
    /* sun in polar coordinates */
    if ((retc = calc_planet_star(tjd, ipl, starname, iflag2, ls, serr)) == SweConst.ERR)
      return retc;
    /* save sun position */
    for (i = 0; i <= 2; i++)
      rst[i] = rs[i];
    /* save moon position */
    for (i = 0; i <= 2; i++)
      rmt[i] = rm[i];
    if ((iflag & SweConst.SEFLG_NONUT)!=0) {
      sidt = sl.swe_sidtime0(tjd_ut, oe.eps * SwissData.RADTODEG, 0) * 15 *
                                                            SwissData.DEGTORAD;
    } else {
      sidt = sl.swe_sidtime(tjd_ut) * 15 * SwissData.DEGTORAD;
    }
    /*
     * radius of planet disk in AU
     */
    if (starname != null && starname.length() > 0)
      drad = 0;
    else if (ipl < SwephData.NDIAM)
      drad = SwephData.pla_diam[ipl] / 2 / SweConst.AUNIT;
    else if (ipl > SweConst.SE_AST_OFFSET)
      drad = swed.ast_diam / 2 * 1000 / SweConst.AUNIT; /* km -> m -> AU */
    else
      drad = 0;
//iter_where:
    while(true) {
      for (i = 0; i <= 2; i++) {
        rs[i] = rst[i];
        rm[i] = rmt[i];
      }
      /* Account for oblateness of earth:
       * Instead of flattening the earth, we apply the
       * correction to the z coordinate of the moon and
       * the sun. This makes the calculation easier.
       */
      for (i = 0; i <= 2; i++)
        lx[i] = lm[i];
      sl.swi_polcart(lx, rm);
      rm[2] /= earthobl;
      /* distance of moon from geocenter */
      dm = Math.sqrt(sl.square_sum(rm));
      /* Account for oblateness of earth */
      for (i = 0; i <= 2; i++)
        lx[i] = ls[i];
      sl.swi_polcart(lx, rs);
      rs[2] /= earthobl;
      /* sun - moon vector */
      for (i = 0; i <= 2; i++) {
        e[i] = (rm[i] - rs[i]);
        et[i] = (rmt[i] - rst[i]);
      }
      /* distance sun - moon */
      dsm = Math.sqrt(sl.square_sum(e));
      dsmt = Math.sqrt(sl.square_sum(et));
      /* sun - moon unit vector */
      for (i = 0; i <= 2; i++) {
        e[i] /= dsm;
        et[i] /= dsmt;
        erm[i] = rm[i] / dm;
      }
      sinf1 = ((drad - RMOON) / dsm);
      cosf1 = Math.sqrt(1 - sinf1 * sinf1);
      sinf2 = ((drad + RMOON) / dsm);
      cosf2 = Math.sqrt(1 - sinf2 * sinf2);
      /* distance of moon from fundamental plane */
      s0 = -sw.dot_prod(rm, e);
      /* distance of shadow axis from geocenter */
      r0 = Math.sqrt(dm * dm - s0 * s0);
      /* diameter of core shadow on fundamental plane */
      d0 = (s0 / dsm * (drad * 2 - DMOON) - DMOON) / cosf1;
      /* diameter of half-shadow on fundamental plane */
      D0 = (s0 / dsm * (drad * 2 + DMOON) + DMOON) / cosf2;
      dcore[2] = r0;
      dcore[3] = d0;
      dcore[4] = D0;
      dcore[5] = cosf1;
      dcore[6] = cosf2;
      for (i = 2; i < 5; i++)
        dcore[i] *= SweConst.AUNIT / 1000.0;
      /**************************
       * central (total or annular) phase
       **************************/
      retc = 0;
      if (de * cosf1 >= r0) {
        retc |= SweConst.SE_ECL_CENTRAL;
      } else if (r0 <= de * cosf1 + Math.abs(d0) / 2) {
        retc |= SweConst.SE_ECL_NONCENTRAL;
      } else if (r0 <= de * cosf2 + D0 / 2) {
        retc |= (SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_NONCENTRAL);
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("no solar eclipse at tjd = "+tjd);
        }
        for (i = 0; i < 10; i++)
          geopos[i] = 0;
//    *dcore = 0;
        dcore[0] = 0;
        retc = 0;
        d = 0;
        no_eclipse = true;
        /*return retc;*/
      }
      /* distance of shadow point from fundamental plane */
      d = s0 * s0 + de * de - dm * dm;
      if (d > 0) {
        d = Math.sqrt(d);
      } else {
        d = 0;
      }
      /* distance of moon from shadow point on earth */
      s = s0 - d;
      /* next: geographic position of eclipse center.
       * if shadow axis does not touch the earth,
       * place on earth with maximum occultation is computed.
       */
      /* geographic position of eclipse center (maximum) */
      for (i = 0; i <= 2; i++)
        xs[i] = rm[i] + s * e[i];
      /* we need geographic position with correct z, as well */
      for (i = 0; i <= 2; i++)
        xst[i] = xs[i];
      xst[2] *= earthobl;
      sl.swi_cartpol(xst, xst);
      if (niter <= 0) {
        double cosfi = Math.cos(xst[1]);
        double sinfi = Math.sin(xst[1]);
        double eobl = SwephData.EARTH_OBLATENESS;
        double cc= 1 / Math.sqrt(cosfi * cosfi + (1-eobl) * (1-eobl) * sinfi * sinfi);
        double ss= (1-eobl) * (1-eobl) * cc;
        earthobl =  ss;
        niter++;
//      goto iter_where;
        continue;
      }
      sl.swi_polcart(xst, xst);
      /* to longitude and latitude */
      sl.swi_cartpol(xs, xs);
      /* measure from sidereal time at greenwich */
      xs[0] -= sidt;
      xs[0] *= SwissData.RADTODEG;
      xs[1] *= SwissData.RADTODEG;
      xs[0] = sl.swe_degnorm(xs[0]);
      /* west is negative */
      if (xs[0] > 180) {
        xs[0] -= 360;
      }
      xssv[0] = xs[0];
      xssv[1] = xs[1];
      geopos[0] = xs[0];
      geopos[1] = xs[1];
      /* diameter of core shadow:
       * first, distance moon - place of eclipse on earth */
      for (i = 0; i <= 2; i++)
        x[i] = rmt[i] - xst[i];
      s = Math.sqrt(sl.square_sum(x));
      /* diameter of core shadow at place of maximum eclipse */
      dcore[0] = (s / dsmt * ( drad * 2 - DMOON) - DMOON) * cosf1;
      dcore[0] *= SweConst.AUNIT / 1000.0;
      /* diameter of penumbra at place of maximum eclipse */
      dcore[1] = (s / dsmt * ( drad * 2 + DMOON) + DMOON) * cosf2;
      dcore[1] *= SweConst.AUNIT / 1000.0;
      if ((retc & SweConst.SE_ECL_PARTIAL)==0 && !no_eclipse) {
        if (dcore[0] > 0) {
          retc |= SweConst.SE_ECL_ANNULAR;
        } else {
          /*printf("total\n");*/
          retc |= SweConst.SE_ECL_TOTAL;
        }
      }
      break; // while (true) ... [goto iter_where]
    }
    return retc;
  }

  private int calc_planet_star(double tjd_et, int ipl, StringBuffer starname, int iflag, double[] x, StringBuffer serr) {
    int i;
    int retc = SweConst.OK;
    if (starname == null || starname.length() == 0) {
      retc = sw.swe_calc(tjd_et, ipl, iflag, x, serr);
    } else {
      if ((retc = sw.swe_fixstar(starname, tjd_et, iflag, x, serr)) ==
                                                                SweConst.OK) {
        /* fixstars have the standard distance 1.
         * in the occultation routines, this might lead to errors
         * if interpreted as AU distance. To avoid this, we make it very high.
         */
        if ((iflag & SweConst.SEFLG_XYZ)!=0) {
          for (i = 0; i < 3; i++)
            x[i] *= 100000000;
        } else {
          x[2] *= 100000000;
        }
      }
    }
    return retc;
  }

  /* Computes attributes of a solar eclipse for given tjd, geo. longitude,
   * geo. latitude, and geo. height.
   *
   * retflag        SE_ECL_TOTAL or SE_ECL_ANNULAR or SE_ECL_PARTIAL
   *              SE_ECL_NONCENTRAL
   *              if 0, no eclipse is visible at geogr. position.
   *
   * attr[0]        fraction of solar diameter covered by moon (magnitude)
   * attr[1]        ratio of lunar diameter to solar one
   * attr[2]        fraction of solar disc covered by moon (obscuration)
   * attr[3]      diameter of core shadow in km
   * attr[4]        azimuth of sun at tjd
   * attr[5]        true altitude of sun above horizon at tjd
   * attr[6]        apparent altitude of sun above horizon at tjd
   * attr[7]        elongation of moon in degrees
   *         declare as attr[20] at least !
   *
   */
  /**
  * Computes the attributes of a solar eclipse for a given Julian Day,
  * geographic longitude, latitude, and height.
  * <P><CODE>
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
  * </CODE><P><B>Attention: geopos must be a double[10], attr a double[20]!</B>
  * @param tjd_ut The Julian Day number in UT
  * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
  * SEFLG_SWIEPH or SEFLG_MOSEPH)
  * @param geopos A double[3] containing geographic longitude, latitude and
  * height in meters above sea level in this order.
  * @param attr An array[20], on return containing the attributes of the
  * eclipse as above
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails
  * @return -1 (SweConst.ERR), if the calculation failed<BR>
  * 0, if there is no solar eclipse at that time and location<BR>
  * SweConst.SE_ECL_TOTAL<BR>
  * SweConst.SE_ECL_ANNULAR<BR>
  * SweConst.SE_ECL_PARTIAL
  */
  int swe_sol_eclipse_how(double tjd_ut,
                          int ifl,
                          double[] geopos,
                          double[] attr,
                          StringBuffer serr) {
    int retflag, retflag2;
    double dcore[]=new double[10];
    double geopos2[]=new double[20];
    ifl &= SweConst.SEFLG_EPHMASK;
    if ((retflag = eclipse_how(tjd_ut, SweConst.SE_SUN, null, ifl, geopos[0],
                               geopos[1], geopos[2], attr, serr)) == SweConst.ERR) {
      return retflag;
    }
    if ((retflag2 = eclipse_where(tjd_ut, SweConst.SE_SUN, null, ifl, geopos2,
                                  dcore, serr)) == SweConst.ERR) {
      return retflag2;
    }
    if (retflag!=0) {
      retflag |= (retflag2 & (SweConst.SE_ECL_CENTRAL | SweConst.SE_ECL_NONCENTRAL));
    }
    attr[3] = dcore[0];
    return retflag;
  }

  private int eclipse_how(double tjd_ut, int ipl, StringBuffer starname,
                          int ifl, double geolon, double geolat, double geohgt,
                          double[] attr, StringBuffer serr) {
    int i;
    int retc = 0;
    double te;
    double xs[]=new double[6], xm[]=new double[6], ls[]=new double[6],
           lm[]=new double[6], x1[]=new double[6], x2[]=new double[6];
    double rmoon, rsun, rsplusrm, rsminusrm;
    double dctr;
    double drad;
    int iflag = SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_TOPOCTR | ifl;
    int iflagcart = iflag | SweConst.SEFLG_XYZ;
    double mdd, eps, sidt, armc, xh[]=new double[6], hmin_appr;
    double lsun, lmoon, lctr, lsunleft, a, b, sc1, sc2;
    for (i = 0; i < 10; i++)
      attr[i] = 0;
    te = tjd_ut + SweDate.getDeltaT(tjd_ut);
    sw.swe_set_topo(geolon, geolat, geohgt);
    if (calc_planet_star(te, ipl, starname, iflag, ls, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    if (sw.swe_calc(te, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    if (calc_planet_star(te, ipl, starname, iflagcart, xs, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    if (sw.swe_calc(te, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    /*
     * radius of planet disk in AU
     */
    if (starname != null && starname.length() > 0)
      drad = 0;
    else if (ipl < SwephData.NDIAM)
      drad = SwephData.pla_diam[ipl] / 2 / SweConst.AUNIT;
    else if (ipl > SweConst.SE_AST_OFFSET)
      drad = swed.ast_diam / 2 * 1000 / SweConst.AUNIT; /* km -> m -> AU */
    else
      drad = 0;
    /*
     * azimuth and altitude of sun or planet
     */
    eps = sl.swi_epsiln(te);
    if ((iflag & SweConst.SEFLG_NONUT)!=0) {
      sidt = sl.swe_sidtime0(tjd_ut, eps * SwissData.RADTODEG, 0) * 15;
    } else {
      sidt = sl.swe_sidtime(tjd_ut) * 15;
    }
    armc = sidt + geolon;
    mdd = sl.swe_degnorm(ls[0] - armc);
    xh[0] = sl.swe_degnorm(mdd - 90);
    xh[1] = ls[1];
    xh[2] = ls[2];
    sl.swe_cotrans(xh, 0, xh, 0, 90 - geolat);   /* azimuth from east, counterclock */
    /* eclipse description */
    rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
    rsun = Math.asin(drad / ls[2]) * SwissData.RADTODEG;
    rsplusrm = rsun + rmoon;
    rsminusrm = rsun - rmoon;
    for (i = 0; i < 3; i++) {
      x1[i] = xs[i] / ls[2];
      x2[i] = xm[i] / lm[2];
    }
    dctr = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
    /*
     * phase
     */
    if (dctr < rsminusrm) {
      retc = SweConst.SE_ECL_ANNULAR;
    } else if (dctr < Math.abs(rsminusrm)) {
      retc = SweConst.SE_ECL_TOTAL;
    } else if (dctr < rsplusrm) {
      retc = SweConst.SE_ECL_PARTIAL;
    } else {
      retc = 0;
      if (serr != null) {
        serr.setLength(0);
        serr.append("no solar eclipse at tjd = "+tjd_ut);
      }
    }
    /*
     * percentage of eclipse
     */
    /*
     * eclipse magnitude:
     * fraction of solar diameter covered by moon
     */
    lsun = Math.asin(rsun / 2 * SwissData.DEGTORAD) * 2;
    lmoon = Math.asin(rmoon / 2 * SwissData.DEGTORAD) * 2;
    lctr = Math.asin(dctr / 2 * SwissData.DEGTORAD) * 2;
    lsunleft = Math.asin((-dctr + rsun + rmoon) * SwissData.DEGTORAD / 2) * 2;
    if (lsun > 0)
      attr[0] = lsunleft / lsun / 2;
    else
      attr[0] = 100;
    /*
     * ratio of diameter of moon to that of sun
     */
    if (lsun > 0)
      attr[1] = lmoon / lsun;
    else
      attr[1] = 0;
    /*
     * obscuration:
     * fraction of solar disc obscured by moon
     */
    if (retc == 0 || lsun == 0) {
      attr[2] = 100;
    } else if (retc == SweConst.SE_ECL_TOTAL || retc == SweConst.SE_ECL_ANNULAR) {
      attr[2] = lmoon * lmoon / lsun / lsun;
    } else {
      a = 2 * lctr * lmoon;
      b = 2 * lctr * lsun;
      if (a < 1e-9) {
        attr[2] = lmoon * lmoon / lsun / lsun;
      } else {
        a = (lctr * lctr + lmoon * lmoon - lsun * lsun) / a;
        if (a > 1) a = 1;
        if (a < -1) a = -1;
        b = (lctr * lctr + lsun * lsun - lmoon * lmoon) / b;
        if (b > 1) b = 1;
        if (b < -1) b = -1;
        a = Math.acos(a);
        b = Math.acos(b);
        sc1 = a * lmoon * lmoon / 2;
        sc2 = b * lsun * lsun / 2;
        sc1 -= (Math.cos(a) * Math.sin(a)) * lmoon * lmoon / 2;
        sc2 -= (Math.cos(b) * Math.sin(b)) * lsun * lsun / 2;
        attr[2] = (sc1 + sc2) * 2 / SwephData.PI / lsun / lsun;
      }
    }
    attr[7] = dctr;
    /* approximate minimum height for visibility, considering
     * refraction and dip
     * 34.4556': refraction at horizon, from Bennets formulae
     * 1.75' / Math.sqrt(geohgt): dip of horizon
     * 0.37' / Math.sqrt(geohgt): refraction between horizon and observer */
    hmin_appr = -(34.4556 + (1.75 + 0.37) * Math.sqrt(geohgt)) / 60;
    if (xh[1] + rsun + Math.abs(hmin_appr) >= 0 && retc!=0) {
      retc |= SweConst.SE_ECL_VISIBLE;        /* eclipse visible */
    }
    attr[4] = sl.swe_degnorm(90 - xh[0]);   /* azimuth, from north, clockwise */
    attr[5] = xh[1]; /* height */
    return retc;
  }

  /* When is the next solar eclipse anywhere on earth?
   *
   * input parameters:
   *
   * tjd_start    start time for search (UT)
   * ifl          ephemeris to be used (SEFLG_SWIEPH, etc.)
   * ifltype      eclipse type to be searched (SE_ECL_TOTAL, etc.)
   *              0, if any type of eclipse is required
   *
   * return values:
   *
   * retflag      SE_ECL_TOTAL or SE_ECL_ANNULAR or SE_ECL_PARTIAL
   *              or SE_ECL_ANNULAR_TOTAL
   *              SE_ECL_CENTRAL
   *              SE_ECL_NONCENTRAL
   *
   * tret[0]      time of maximum eclipse
   * tret[1]      time, when eclipse takes place at local apparent noon
   * tret[2]      time of eclipse begin
   * tret[3]      time of eclipse end
   * tret[4]      time of totality begin
   * tret[5]      time of totality end
   * tret[6]      time of center line begin
   * tret[7]      time of center line end
   * tret[8]      time when annular-total eclipse becomes total
   *                 not implemented so far
   * tret[9]      time when annular-total eclipse becomes annular again
   *                 not implemented so far
   *         declare as tret[10] at least!
   *
   */
  /**
  * Computes the next solar eclipse anywhere on earth.
  * <P>tret is an output parameter with the following meaning:
  * <P><CODE>
  * tret[0]:&nbsp;&nbsp;&nbsp;time of maximum eclipse.<BR>
  * tret[1]:&nbsp;&nbsp;&nbsp;time, when the eclipse takes place at local
  * apparent noon.<BR>
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
  * </CODE><P><B>Attention: tret must be a double[10]!</B>
  * @param tjd_start The Julian Day number in UT, from when to start searching
  * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
  * SEFLG_SWIEPH or SEFLG_MOSEPH)
  * @param ifltype SweConst.SE_ECL_TOTAL for total eclipse or 0 for any eclipse
  * @param tret An array[10], on return containing the times of different
  * occasions of the eclipse as above
  * @param backward 1, if search should be done backwards
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails.
  * @return -1 (SweConst.ERR), if the calculation failed<BR>
  * SweConst.SE_ECL_TOTAL<BR>
  * SweConst.SE_ECL_ANNULAR<BR>
  * SweConst.SE_ECL_PARTIAL<BR>
  * SweConst.SE_ECL_ANNULAR_TOTAL<BR>in combination with:<BR>
  * SweConst.SE_ECL_CENTRAL<BR>
  * SweConst.SE_ECL_NONCENTRAL
  */
  int swe_sol_eclipse_when_glob(double tjd_start, int ifl, int ifltype,
                                double tret[], int backward,
                                StringBuffer serr) {
    int i, j, k, m, n, o, i1 = 0, i2 = 0;
    int retflag = 0, retflag2 = 0;
    double de = 6378.140, a;
    double t, tt, tjd, tjds, dt, dta, dtb;
    DblObj dtint=new DblObj();
    double T, T2, T3, T4, K, M, Mm;
    double E, Ff;
    double xs[]=new double[6], xm[]=new double[6],
           ls[]=new double[6], lm[]=new double[6];
    double rmoon, rsun, dcore[]=new double[10];
    double dc[]=new double[3];
    DblObj dctr=new DblObj();
    double twohr = 2.0 / 24.0;
    double tenmin = 10.0 / 24.0 / 60.0;
    DblObj dt1=new DblObj(), dt2=new DblObj();
    double geopos[]=new double[20], attr[]=new double[20];
    double dtstart, dtdiv;
    double xa[]=new double[6], xb[]=new double[6];
    int direction = 1;
    boolean dont_times = false;
    int iflag, iflagcart;
    ifl &= SweConst.SEFLG_EPHMASK;
    iflag = SweConst.SEFLG_EQUATORIAL | ifl;
    iflagcart = iflag | SweConst.SEFLG_XYZ;
    if (ifltype == (SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_CENTRAL)) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("central partial eclipses do not exist");
      }
      return SweConst.ERR;
    }
    if (ifltype == 0) {
      ifltype = SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_ANNULAR
             | SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_ANNULAR_TOTAL
             | SweConst.SE_ECL_NONCENTRAL | SweConst.SE_ECL_CENTRAL;
    }
    if (backward!=0) {
      direction = -1;
    }
    K = (int) ((tjd_start - SwephData.J2000) / 365.2425 * 12.3685);
    K -= direction;
//next_try:
    while(true) {
      retflag = 0;
      dont_times = false;
      for (i = 0; i <= 9; i++)
        tret[i] = 0;
      T = K / 1236.85;
      T2 = T * T; T3 = T2 * T; T4 = T3 * T;
      Ff = sl.swe_degnorm(160.7108 + 390.67050274 * K
                   - 0.0016341 * T2
                   - 0.00000227 * T3
                   + 0.000000011 * T4);
      if (Ff > 180) {
        Ff -= 180;
      }
      if (Ff > 21 && Ff < 159) {    /* no eclipse possible */
        K += direction;
        continue;
      }
      /* approximate time of geocentric maximum eclipse
       * formula from Meeus, German, p. 381 */
      tjd = 2451550.09765 + 29.530588853 * K
                          + 0.0001337 * T2
                          - 0.000000150 * T3
                          + 0.00000000073 * T4;
      M = sl.swe_degnorm(2.5534 + 29.10535669 * K
                          - 0.0000218 * T2
                          - 0.00000011 * T3);
      Mm = sl.swe_degnorm(201.5643 + 385.81693528 * K
                          + 0.1017438 * T2
                          + 0.00001239 * T3
                          + 0.000000058 * T4);
      E = 1 - 0.002516 * T - 0.0000074 * T2;
      M *= SwissData.DEGTORAD;
      Mm *= SwissData.DEGTORAD;
      tjd = tjd - 0.4075 * Math.sin(Mm)
                + 0.1721 * E * Math.sin(M);
      /*
       * time of maximum eclipse (if eclipse) =
       * minimum geocentric angle between sun and moon edges.
       * After this time has been determined, check
       * whether or not an eclipse is taking place with
       * the functions eclipse_where() and _how().
       */
      dtstart = 1;
      if (tjd < 2000000) {
        dtstart = 5;
      }
      dtdiv = 4;
      for (dt = dtstart;
           dt > 0.0001;
           dt /= dtdiv) {
        for (i = 0, t = tjd - dt; i <= 2; i++, t += dt) {
          if (sw.swe_calc(t, SweConst.SE_SUN, iflag, ls, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_SUN, iflagcart, xs, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          for (m = 0; m < 3; m++) {
            xa[m] = xs[m] / ls[2];
            xb[m] = xm[m] / lm[2];
          }
          dc[i] = Math.acos(sl.swi_dot_prod_unit(xa, xb)) * SwissData.RADTODEG;
          rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
          rsun = Math.asin(RSUN / ls[2]) * SwissData.RADTODEG;
          dc[i] -= (rmoon + rsun);
        }
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dctr);
        tjd += dtint.val + dt;
      }
      tjds = tjd = tjd - SweDate.getDeltaT(tjd);
      if ((retflag = eclipse_where(tjd, SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
        return retflag;
      }
      retflag2 = retflag;
        /* in extreme cases _where() returns no eclipse, where there is
         * actually a very small one, therefore call _how() with the
         * coordinates returned by _where(): */
      if ((retflag2 = eclipse_how(tjd, SweConst.SE_SUN, null, ifl, geopos[0], geopos[1], 0, attr,
                                                        serr)) == SweConst.ERR) {
        return retflag2;
      }
      if (retflag2 == 0) {
        K += direction;
        continue;
      }
      tret[0] = tjd;
      if ((backward!=0 && tret[0] >= tjd_start - 0.0001)
        || (backward==0 && tret[0] <= tjd_start + 0.0001)) {
        K += direction;
        continue;
      }
      /*
       * eclipse type, SE_ECL_TOTAL, _ANNULAR, etc.
       * SE_ECL_ANNULAR_TOTAL will be discovered later
       */
      if ((retflag = eclipse_where(tjd, SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
        return retflag;
      }
      if (retflag == 0) {   /* can happen with extremely small percentage */
        retflag = SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_NONCENTRAL;
        tret[4] = tret[5] = tjd;    /* fix this ???? */
        dont_times = true;
      }
      /*
       * check whether or not eclipse type found is wanted
       */
      /* non central eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_NONCENTRAL)==0 &&
                                   (retflag & SweConst.SE_ECL_NONCENTRAL)!=0) {
        K += direction;
        continue;
      }
      /* central eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_CENTRAL)==0 &&
                                      (retflag & SweConst.SE_ECL_CENTRAL)!=0) {
        K += direction;
        continue;
      }
      /* non annular eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_ANNULAR)==0 &&
                                      (retflag & SweConst.SE_ECL_ANNULAR)!=0) {
        K += direction;
        continue;
      }
      /* non partial eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_PARTIAL)==0 &&
                                      (retflag & SweConst.SE_ECL_PARTIAL)!=0) {
        K += direction;
        continue;
      }
      /* annular-total eclipse will be discovered later */
      if ((ifltype & (SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_ANNULAR_TOTAL))==0
                    && (retflag & SweConst.SE_ECL_TOTAL)!=0) {
        K += direction;
        continue;
      }
      if (dont_times) {
        break;
      }
      /*
       * n = 0: times of eclipse begin and end
       * n = 1: times of totality begin and end
       * n = 2: times of center line begin and end
       */
      if ((retflag & SweConst.SE_ECL_PARTIAL)!=0) {
        o = 0;
      } else if ((retflag & SweConst.SE_ECL_NONCENTRAL)!=0) {
        o = 1;
      }
      else
        o = 2;
      dta = twohr;
      dtb = tenmin;
      for (n = 0; n <= o; n++) {
        if (n == 0) {
          /*dc[1] = dcore[3] / 2 + de - dcore[1];*/
          i1 = 2; i2 = 3;
        } else if (n == 1) {
          if ((retflag & SweConst.SE_ECL_PARTIAL)!=0) {
            continue;
          }
          i1 = 4; i2 = 5;
        } else if (n == 2) {
          if ((retflag & SweConst.SE_ECL_NONCENTRAL)!=0) {
            continue;
          }
          i1 = 6; i2 = 7;
        }
        for (i = 0, t = tjd - dta; i <= 2; i += 1, t += dta) {
          if ((retflag2 = eclipse_where(t, SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
            return retflag2;
          }
          if (n == 0) {
            dc[i] = dcore[4] / 2 + de / dcore[5] - dcore[2];
          } else if (n == 1) {
            dc[i] = Math.abs(dcore[3]) / 2 + de / dcore[6] - dcore[2];
          } else if (n == 2) {
            dc[i] = de / dcore[6] - dcore[2];
          }
        }
        find_zero(dc[0], dc[1], dc[2], dta, dt1, dt2);
        tret[i1] = tjd + dt1.val + dta;
        tret[i2] = tjd + dt2.val + dta;
        for (m = 0, dt = dtb; m < 3; m++, dt /= 3) {
          for (j = i1; j <= i2; j += (i2 - i1)) {
            for (i = 0, t = tret[j] - dt; i < 2; i++, t += dt) {
              if ((retflag2 = eclipse_where(t, SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
                return retflag2;
              }
              if (n == 0) {
                dc[i] = dcore[4] / 2 + de / dcore[5] - dcore[2];
              } else if (n == 1) {
                dc[i] = Math.abs(dcore[3]) / 2 + de / dcore[6] - dcore[2];
              } else if (n == 2) {
                dc[i] = de / dcore[6] - dcore[2];
              }
            }
            dt1.val = dc[1] / ((dc[1] - dc[0]) / dt);
            tret[j] -= dt1.val;
          }
        }
      }
      /*
       * annular-total eclipses
       */
      if ((retflag & SweConst.SE_ECL_TOTAL)!=0) {
        if ((retflag2 = eclipse_where(tret[0], SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
          return retflag2;
        }
        dc[0] = dcore[0];
        if ((retflag2 = eclipse_where(tret[4], SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
          return retflag2;
        }
        dc[1] = dcore[0];
        if ((retflag2 = eclipse_where(tret[5], SweConst.SE_SUN, null, ifl, geopos, dcore, serr)) ==
                                                                 SweConst.ERR) {
          return retflag2;
        }
        dc[2] = dcore[0];
        /* the maximum is always total, and there is either one or
         * to times before and after, when the core shadow becomes
         * zero and totality changes into annularity or vice versa.
         */
        if (dc[0] * dc[1] < 0 || dc[0] * dc[2] < 0) {
          retflag |= SweConst.SE_ECL_ANNULAR_TOTAL;
          retflag &= ~SweConst.SE_ECL_TOTAL;
        }
      }
      /* if eclipse is given but not wanted: */
      if ((ifltype & SweConst.SE_ECL_TOTAL)==0 &&
          (retflag & SweConst.SE_ECL_TOTAL)!=0) {
        K += direction;
        continue;
      }
      /* if annular_total eclipse is given but not wanted: */
      if ((ifltype & SweConst.SE_ECL_ANNULAR_TOTAL)==0 &&
          (retflag & SweConst.SE_ECL_ANNULAR_TOTAL)!=0) {
        K += direction;
        continue;
      }
      /*
       * time of maximum eclipse at local apparent noon
       */
      /* first, find out, if there is a solar transit
       * between begin and end of eclipse */
      k = 2;
      for (i = 0; i < 2; i++) {
        j = i + k;
        tt = tret[j] + SweDate.getDeltaT(tret[j]);
        if (sw.swe_calc(tt, SweConst.SE_SUN, iflag, ls, serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
        if (sw.swe_calc(tt, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
        dc[i] = sl.swe_degnorm(ls[0] - lm[0]);
        if (dc[i] > 180) {
          dc[i] -= 360;
        }
      }
      if (dc[0] * dc[1] >= 0) {     /* no transit */
        tret[1] = 0;
      } else {
        tjd = tjds;
        dt = 0.1;
        dt1.val = (tret[3] - tret[2]) / 2.0;
        if (dt1.val < dt) {
          dt = dt1.val / 2.0;
        }
        for (j = 0;
            dt > 0.01;
            j++, dt /= 3) {
          for (i = 0, t = tjd; i <= 1; i++, t -= dt) {
            tt = t + SweDate.getDeltaT(t);
            if (sw.swe_calc(tt, SweConst.SE_SUN, iflag, ls, serr) ==
                                                                 SweConst.ERR) {
              return SweConst.ERR;
            }
            if (sw.swe_calc(tt, SweConst.SE_MOON, iflag, lm, serr) ==
                                                                 SweConst.ERR) {
              return SweConst.ERR;
            }
            dc[i] = sl.swe_degnorm(ls[0] - lm[0]);
            if (dc[i] > 180) {
              dc[i] -= 360;
            }
            if (dc[i] > 180) {
              dc[i] -= 360;
            }
          }
          a = (dc[1] - dc[0]) / dt;
          if (a < 1e-10) {
            break;
          }
          dt1.val = dc[0] / a;
          tjd += dt1.val;
        }
        tret[1] = tjd;
      }
      break;
    } // while (true)
    return retflag;
    /*
     * the time of maximum occultation is practically identical
     * with the time of maximum core shadow diameter.
     *
     * the time, when duration of totality is maximal,
     * is not an interesting computation either. Near the maximum
     * occulation, the time of totality can be the same by
     * a second for hundreds of kilometers (for 10 minutes
     * or more).
     *
     * for annular eclipses the maximum duration is close to the
     * beginning and the end of the center lines, where is also
     * the minimum of core shadow diameter.
     */
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
  int swe_lun_occult_when_glob(
       double tjd_start, int ipl, StringBuffer starname, int ifl, int ifltype,
       double[] tret, int backward, StringBuffer serr) {
    int i, j, k, m, n, o, i1=0, i2=0;
    int retflag = 0, retflag2 = 0;
    double de = 6378.140, a;
    double t, tt, tjd=0, tjds, dt, dta, dtb;
    DblObj dtint=new DblObj();
    double drad;
    double xs[]=new double[6], xm[]=new double[6], ls[]=new double[6], lm[]=new double[6];
    double rmoon, rsun, dcore[]=new double[10];
    double dc[]=new double[20];
    DblObj dctr=new DblObj();
    double twohr = 2.0 / 24.0;
    double tenmin = 10.0 / 24.0 / 60.0;
    DblObj dt1=new DblObj(), dt2=new DblObj();
    double dadd = 10, dadd2 = 6;
    int nstartpos = 10;
    double geopos[]=new double[20];
    double dtstart, dtdiv;
    int direction = 1;
    int iflag, iflagcart;
    boolean dont_times = false;
    int one_try = backward & SweConst.SE_ECL_ONE_TRY;
    ifl &= SweConst.SEFLG_EPHMASK;
    iflag = SweConst.SEFLG_EQUATORIAL | ifl;
    iflagcart = iflag | SweConst.SEFLG_XYZ;
    backward &= 1L;
    /*
     * initializations
     */
    if (ifltype == (SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_CENTRAL)) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("central partial eclipses do not exist");
      }
      return SweConst.ERR;
    }
    if (ifltype == 0)
      ifltype = SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_PARTIAL
             | SweConst.SE_ECL_ANNULAR_TOTAL | SweConst.SE_ECL_NONCENTRAL | SweConst.SE_ECL_CENTRAL;
    retflag = 0;
    dont_times = false;
    if ((backward & SEI_OCC_FAST) != 0)
      dont_times = true;
    for (i = 0; i <= 9; i++)
      tret[i] = 0;
    if (backward!=0)
      direction = -1;
    t = tjd_start - direction * 0.001;
    while(true) {
//next_try:
      for (i = 0; i < nstartpos; i++, t += direction * dadd2) {
        if (calc_planet_star(t, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
        if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
        dc[i] = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
        if (i > 1 && dc[i] > dc[i-1] && dc[i-2] > dc[i-1]) {
          tjd = t - direction * dadd2;
          break;
        } else if (i == nstartpos-1) {
          for (j = 0; j < nstartpos; j++)
            //DevNull.print(dc[j] + " ");
          System.err.println("problem planet");
          System.exit(0);
        }
      }
      /*
       * radius of planet disk in AU
       */
      if (starname != null && starname.length() > 0)
        drad = 0;
      else if (ipl < SwephData.NDIAM)
        drad = SwephData.pla_diam[ipl] / 2 / SweConst.AUNIT;
      else if (ipl > SweConst.SE_AST_OFFSET)
        drad = swed.ast_diam / 2 * 1000 / SweConst.AUNIT; /* km -> m -> AU */
      else
        drad = 0;
      /*
       * time of maximum eclipse (if eclipse) =
       * minimum geocentric angle between sun and moon edges.
       * After this time has been determined, check
       * whether or not an eclipse is taking place with
       * the functions eclipse_where() and _how().
       */
      dtstart = dadd2; /* originally 1 */
      dtdiv = 3;
      for (dt = dtstart;
           dt > 0.0001;
           dt /= dtdiv) {
        for (i = 0, t = tjd - dt; i <= 2; i++, t += dt) {
          if (calc_planet_star(t, ipl, starname, iflag, ls, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(t, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (calc_planet_star(t, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
          dc[i] = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
          rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
          rsun = Math.asin(drad / ls[2]) * SwissData.RADTODEG;
          dc[i] -= (rmoon + rsun);
        }
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dctr);
        tjd += dtint.val + dt;
      }
      tjd -= SweDate.getDeltaT(tjd);
      tjds = tjd;
      if ((retflag = eclipse_where(tjd, ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
        return retflag;
      retflag2 = retflag;
        /* in extreme cases _where() returns no eclipse, where there is
         * actually a very small one, therefore call _how() with the
         * coordinates returned by _where(): */
      /* if ((retflag2 = eclipse_how(tjd, ipl, starname, ifl, geopos[0], geopos[1], 0, attr, serr)) == SweConst.ERR)
        return retflag2; */
      if (retflag2 == 0) {
        /* only one try! */
        if (one_try!=0) {
          tret[0] = tjd;
          return 0;
        }
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      tret[0] = tjd;
      if ((backward!=0 && tret[0] >= tjd_start - 0.0001)
        || (backward==0 && tret[0] <= tjd_start + 0.0001)) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /*
       * eclipse type, SE_ECL_TOTAL, _ANNULAR, etc.
       * SE_ECL_ANNULAR_TOTAL will be discovered later
       */
      if ((retflag = eclipse_where(tjd, ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
        return retflag;
      if (retflag == 0) { /* can happen with extremely small percentage */
        retflag = SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_NONCENTRAL;
        tret[4] = tret[5] = tjd;  /* fix this ???? */
        retflag = SweConst.SE_ECL_PARTIAL | SweConst.SE_ECL_NONCENTRAL;
        tret[4] = tret[5] = tjd;  /* fix this ???? */
        dont_times = true;
      }
      /*
       * check whether or not eclipse type found is wanted
       */
      /* non central eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_NONCENTRAL)==0 && (retflag & SweConst.SE_ECL_NONCENTRAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /* central eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_CENTRAL)==0 && (retflag & SweConst.SE_ECL_CENTRAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /* non annular eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_ANNULAR)==0 && (retflag & SweConst.SE_ECL_ANNULAR)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /* non partial eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_PARTIAL)==0 && (retflag & SweConst.SE_ECL_PARTIAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /* annular-total eclipse will be discovered later */
      if ((ifltype & (SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_ANNULAR_TOTAL))==0 && (retflag & SweConst.SE_ECL_TOTAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      if (dont_times)
//        goto end_search_global;
        return retflag;
      /*
       * n = 0: times of eclipse begin and end
       * n = 1: times of totality begin and end
       * n = 2: times of center line begin and end
       */
      if ((retflag & SweConst.SE_ECL_PARTIAL)!=0)
        o = 0;
      else if ((retflag & SweConst.SE_ECL_NONCENTRAL)!=0)
        o = 1;
      else
        o = 2;
      dta = twohr;
      dtb = tenmin;
      for (n = 0; n <= o; n++) {
        if (n == 0) {
          /*dc[1] = dcore[3] / 2 + de - dcore[1];*/
          i1 = 2; i2 = 3;
        } else if (n == 1) {
          if ((retflag & SweConst.SE_ECL_PARTIAL)!=0)
            continue;
          i1 = 4; i2 = 5;
        } else if (n == 2) {
          if ((retflag & SweConst.SE_ECL_NONCENTRAL)!=0)
            continue;
          i1 = 6; i2 = 7;
        }
        for (i = 0, t = tjd - dta; i <= 2; i += 1, t += dta) {
          if ((retflag2 = eclipse_where(t, ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
            return retflag2;
          if (n == 0)
            dc[i] = dcore[4] / 2 + de / dcore[5] - dcore[2];
          else if (n == 1)
            dc[i] = Math.abs(dcore[3]) / 2 + de / dcore[6] - dcore[2];
          else if (n == 2)
            dc[i] = de / dcore[6] - dcore[2];
        }
        find_zero(dc[0], dc[1], dc[2], dta, dt1, dt2);
        tret[i1] = tjd + dt1.val + dta;
        tret[i2] = tjd + dt2.val + dta;
        for (m = 0, dt = dtb; m < 3; m++, dt /= 3) {
          for (j = i1; j <= i2; j += (i2 - i1)) {
            for (i = 0, t = tret[j] - dt; i < 2; i++, t += dt) {
              if ((retflag2 = eclipse_where(t, ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
                return retflag2;
              if (n == 0)
                dc[i] = dcore[4] / 2 + de / dcore[5] - dcore[2];
              else if (n == 1)
                dc[i] = Math.abs(dcore[3]) / 2 + de / dcore[6] - dcore[2];
              else if (n == 2)
                dc[i] = de / dcore[6] - dcore[2];
            }
            dt1.val = dc[1] / ((dc[1] - dc[0]) / dt);
            tret[j] -= dt1.val;
          }
        }
      }
      /*
       * annular-total eclipses
       */
      if ((retflag & SweConst.SE_ECL_TOTAL)!=0) {
        if ((retflag2 = eclipse_where(tret[0], ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
          return retflag2;
        dc[0] = dcore[0];
        if ((retflag2 = eclipse_where(tret[4], ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
          return retflag2;
        dc[1] = dcore[0];
        if ((retflag2 = eclipse_where(tret[5], ipl, starname, ifl, geopos, dcore, serr)) == SweConst.ERR)
          return retflag2;
        dc[2] = dcore[0];
        /* the maximum is always total, and there is either one or
         * to times before and after, when the core shadow becomes
         * zero and totality changes into annularity or vice versa.
         */
        if (dc[0] * dc[1] < 0 || dc[0] * dc[2] < 0) {
          retflag |= SweConst.SE_ECL_ANNULAR_TOTAL;
          retflag &= ~SweConst.SE_ECL_TOTAL;
        }
      }
      /* if eclipse is given but not wanted: */
      if ((ifltype & SweConst.SE_ECL_TOTAL)==0 && (retflag & SweConst.SE_ECL_TOTAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /* if annular_total eclipse is given but not wanted: */
      if ((ifltype & SweConst.SE_ECL_ANNULAR_TOTAL)==0 && (retflag & SweConst.SE_ECL_ANNULAR_TOTAL)!=0) {
        t= tjd + direction * dadd;
//    goto next_try;
        continue;
      }
      /*
       * time of maximum eclipse at local apparent noon
       */
      /* first, find out, if there is a solar transit
       * between begin and end of eclipse */
      k = 2;
      for (i = 0; i < 2; i++) {
        j = i + k;
        tt = tret[j] + SweDate.getDeltaT(tret[j]);
        if (calc_planet_star(tt, ipl, starname, iflag, ls, serr) == SweConst.ERR)
            return SweConst.ERR;
        if (sw.swe_calc(tt, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR)
            return SweConst.ERR;
        dc[i] = sl.swe_degnorm(ls[0] - lm[0]);
        if (dc[i] > 180)
          dc[i] -= 360;
      }
      if (dc[0] * dc[1] >= 0)     /* no transit */
        tret[1] = 0;
      else {
        tjd = tjds;
        dt = 0.1;
        dt1.val = (tret[3] - tret[2]) / 2.0;
        if (dt1.val < dt)
          dt = dt1.val / 2.0;
        for (j = 0;
             dt > 0.01;
             j++, dt /= 3) {
          for (i = 0, t = tjd; i <= 1; i++, t -= dt) {
            tt = t + SweDate.getDeltaT(t);
            if (calc_planet_star(tt, ipl, starname, iflag, ls, serr) == SweConst.ERR)
              return SweConst.ERR;
            if (sw.swe_calc(tt, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR)
              return SweConst.ERR;
            dc[i] = sl.swe_degnorm(ls[0] - lm[0]);
            if (dc[i] > 180)
              dc[i] -= 360;
            if (dc[i] > 180)
              dc[i] -= 360;
          }
          a = (dc[1] - dc[0]) / dt;
          if (a < 1e-10)
            break;
          dt1.val = dc[0] / a;
          tjd += dt1.val;
        }
        tret[1] = tjd;
      }
      break;
    }
//end_search_global:
    return retflag;
    /*
     * the time of maximum occultation is practically identical
     * with the time of maximum core shadow diameter.
     *
     * the time, when duration of totality is maximal,
     * is not an interesting computation either. Near the maximum
     * occulation, the time of totality can be the same by
     * a second for hundreds of kilometers (for 10 minutes
     * or more).
     *
     * for annular eclipses the maximum duration is close to the
     * beginning and the end of the center lines, where is also
     * the minimum of core shadow diameter.
     * beginning and the end of the center lines, where is also
     * the minimum of core shadow diameter.
     */
  }
 
 

  /* When is the next solar eclipse at a given geographical position?
   * Note the uncertainty of Delta T for the remote past and for
   * the future.
   *
   * retflag        SE_ECL_TOTAL or SE_ECL_ANNULAR or SE_ECL_PARTIAL
   *              SE_ECL_VISIBLE,
   *              SE_ECL_MAX_VISIBLE,
   *              SE_ECL_1ST_VISIBLE, SE_ECL_2ND_VISIBLE
   *              SE_ECL_3ST_VISIBLE, SE_ECL_4ND_VISIBLE
   *
   * tret[0]        time of maximum eclipse
   * tret[1]        time of first contact
   * tret[2]        time of second contact
   * tret[3]        time of third contact
   * tret[4]        time of forth contact
   * tret[5]        time of sun rise between first and forth contact
                          (not implemented so far)
   * tret[6]        time of sun set beween first and forth contact
                          (not implemented so far)
   *
   * attr[0]        fraction of solar diameter covered by moon (magnitude)
   * attr[1]        ratio of lunar diameter to solar one
   * attr[2]        fraction of solar disc covered by moon (obscuration)
   * attr[3]      diameter of core shadow in km
   * attr[4]        azimuth of sun at tjd
   * attr[5]        true altitude of sun above horizon at tjd
   * attr[6]        apparent altitude of sun above horizon at tjd
   * attr[7]        elongation of moon in degrees
   *         declare as attr[20] at least !
   */
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
  * @param geopos An array double[3] containing the longitude, latitude and
  * height of the geographic position
  * @param tret An array[7], on return containing the times of different
  * occasions of the eclipse as specified above
  * @param attr An array[20], on return containing different attributes of
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
  */
  int swe_sol_eclipse_when_loc(double tjd_start, int ifl,
                               double[] geopos, double[] tret,
                               double[] attr, int backward,
                               StringBuffer serr) {
    int retflag = 0, retflag2 = 0;
    double geopos2[]=new double[20], dcore[]=new double[10];
    ifl &= SweConst.SEFLG_EPHMASK;
    if ((retflag = eclipse_when_loc(tjd_start, ifl, geopos, tret, attr,
                                                        backward, serr)) <= 0) {
      return retflag;
    }
    /*
     * diameter of core shadow
     */
    if ((retflag2 = eclipse_where(tret[0], SweConst.SE_SUN, null, ifl, geopos2, dcore, serr)) ==
                                                                SweConst.ERR) {
      return retflag2;
    }
    retflag |= (retflag2 & SweConst.SE_ECL_NONCENTRAL);
    attr[3] = dcore[0];
    return retflag;
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
  int swe_lun_occult_when_loc(double tjd_start, int ipl, StringBuffer starname, int ifl,
       double[] geopos, double[] tret, double[] attr, int backward, StringBuffer serr) {
    int retflag = 0, retflag2 = 0;
    double geopos2[]=new double[20], dcore[]=new double[10];
    ifl &= SweConst.SEFLG_EPHMASK;
    if ((retflag = occult_when_loc(tjd_start, ipl, starname, ifl, geopos, tret, attr, backward, serr)) <= 0)
      return retflag;
    /*
     * diameter of core shadow
     */
    if ((retflag2 = eclipse_where(tret[0], ipl, starname, ifl, geopos2, dcore, serr)) == SweConst.ERR)
      return retflag2;
    retflag |= (retflag2 & SweConst.SE_ECL_NONCENTRAL);
    attr[3] = dcore[0];
    return retflag;
  }

  private int eclipse_when_loc(double tjd_start, int ifl, double[] geopos,
                               double[] tret, double[] attr, int backward,
                               StringBuffer serr) {
    int i, j, k, m;
    int retflag = 0;
    double t, tjd, dt, K, T, T2, T3, T4, F, M, Mm;
    DblObj dtint=new DblObj();
    double E, Ff, A1, Om;
    double xs[]=new double[6], xm[]=new double[6],
           ls[]=new double[6], lm[]=new double[6],
           x1[]=new double[6], x2[]=new double[6], dm, ds;
    double rmoon, rsun, rsplusrm, rsminusrm;
    double dc[]=new double[3], dctrmin;
    DblObj dctr=new DblObj();
    double twomin = 2.0 / 24.0 / 60.0;
    double tensec = 10.0 / 24.0 / 60.0 / 60.0;
    double twohr = 2.0 / 24.0;
    double tenmin = 10.0 / 24.0 / 60.0;
    DblObj dt1=new DblObj(), dt2=new DblObj();
    double dtdiv, dtstart;
    int iflag = SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_TOPOCTR | ifl;
    int iflagcart = iflag | SweConst.SEFLG_XYZ;
    sw.swe_set_topo(geopos[0], geopos[1], geopos[2]);
    K = (int) ((tjd_start - SwephData.J2000) / 365.2425 * 12.3685);
    if (backward!=0) {
      K++;
    } else {
      K--;
    }
//next_try:
    while (true) {
      T = K / 1236.85;
      T2 = T * T; T3 = T2 * T; T4 = T3 * T;
      Ff = F = sl.swe_degnorm(160.7108 + 390.67050274 * K
                   - 0.0016341 * T2
                   - 0.00000227 * T3
                   + 0.000000011 * T4);
      if (Ff > 180) {
        Ff -= 180;
      }
      if (Ff > 21 && Ff < 159) {         /* no eclipse possible */
        if (backward!=0) {
          K--;
        } else {
          K++;
        }
        continue;
      }
      /* approximate time of geocentric maximum eclipse.
       * formula from Meeus, German, p. 381 */
      tjd = 2451550.09765 + 29.530588853 * K
                          + 0.0001337 * T2
                          - 0.000000150 * T3
                          + 0.00000000073 * T4;
      M = sl.swe_degnorm(2.5534 + 29.10535669 * K
                          - 0.0000218 * T2
                          - 0.00000011 * T3);
      Mm = sl.swe_degnorm(201.5643 + 385.81693528 * K
                          + 0.1017438 * T2
                          + 0.00001239 * T3
                          + 0.000000058 * T4);
      Om = sl.swe_degnorm(124.7746 - 1.56375580 * K
                          + 0.0020691 * T2
                          + 0.00000215 * T3);
      E = 1 - 0.002516 * T - 0.0000074 * T2;
      A1 = sl.swe_degnorm(299.77 + 0.107408 * K - 0.009173 * T2);
      M *= SwissData.DEGTORAD;
      Mm *= SwissData.DEGTORAD;
      F *= SwissData.DEGTORAD;
      Om *= SwissData.DEGTORAD;
      A1 *= SwissData.DEGTORAD;
      tjd = tjd - 0.4075 * Math.sin(Mm)
                + 0.1721 * E * Math.sin(M);
      sw.swe_set_topo(geopos[0], geopos[1], geopos[2]);
      dtdiv = 2;
      dtstart = 0.5;
      if (tjd < 1900000) { /* because above formula is not good (delta t?) */
        dtstart = 2;
      }
      for (dt = dtstart;
           dt > 0.00001;
           dt /= dtdiv) {
        if (dt < 0.1) {
          dtdiv = 3;
        }
        for (i = 0, t = tjd - dt; i <= 2; i++, t += dt) {
          /* this takes some time, but is necessary to avoid
           * missing an eclipse */
          if (sw.swe_calc(t, SweConst.SE_SUN, iflagcart, xs, serr) ==
                                                                SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_SUN, iflag, ls, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) ==
                                                                SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          dm = Math.sqrt(sl.square_sum(xm));
          ds = Math.sqrt(sl.square_sum(xs));
          for (k = 0; k < 3; k++) {
            x1[k] = xs[k] / ds /*ls[2]*/;
            x2[k] = xm[k] / dm /*lm[2]*/;
          }
          dc[i] = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
        }
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dctr);
        tjd += dtint.val + dt;
      }
      if (sw.swe_calc(tjd, SweConst.SE_SUN, iflagcart, xs, serr) ==
                                                                 SweConst.ERR) {
        return SweConst.ERR;
      }
      if (sw.swe_calc(tjd, SweConst.SE_SUN, iflag, ls, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      if (sw.swe_calc(tjd, SweConst.SE_MOON, iflagcart, xm, serr) ==
                                                                 SweConst.ERR) {
        return SweConst.ERR;
      }
      if (sw.swe_calc(tjd, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      dctr.val = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
      rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
      rsun = Math.asin(RSUN / ls[2]) * SwissData.RADTODEG;
      rsplusrm = rsun + rmoon;
      rsminusrm = rsun - rmoon;
      if (dctr.val > rsplusrm) {
        if (backward!=0) {
          K--;
        } else {
          K++;
        }
        continue;
      }
      tret[0] = tjd - SweDate.getDeltaT(tjd);
      if ((backward!=0 && tret[0] >= tjd_start - 0.0001)
        || (backward==0 && tret[0] <= tjd_start + 0.0001)) {
        if (backward!=0) {
          K--;
        } else {
          K++;
        }
        continue;
      }
      if (dctr.val < rsminusrm) {
        retflag = SweConst.SE_ECL_ANNULAR;
      } else if (dctr.val < Math.abs(rsminusrm)) {
        retflag = SweConst.SE_ECL_TOTAL;
      } else if (dctr.val <= rsplusrm) {
        retflag = SweConst.SE_ECL_PARTIAL;
      }
      dctrmin = dctr.val;
      /* contacts 2 and 3 */
      if (dctr.val > Math.abs(rsminusrm)) {/* partial, no 2nd and 3rd contact */
        tret[2] = tret[3] = 0;
      } else {
        dc[1] = Math.abs(rsminusrm) - dctrmin;
        for (i = 0, t = tjd - twomin; i <= 2; i += 2, t = tjd + twomin) {
          if (sw.swe_calc(t, SweConst.SE_SUN, iflagcart, xs, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          dm = Math.sqrt(sl.square_sum(xm));
          ds = Math.sqrt(sl.square_sum(xs));
          rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
          rsun = Math.asin(RSUN / ds) * SwissData.RADTODEG;
          rsminusrm = rsun - rmoon;
          for (k = 0; k < 3; k++) {
            x1[k] = xs[k] / ds /*ls[2]*/;
            x2[k] = xm[k] / dm /*lm[2]*/;
          }
          dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
          dc[i] = Math.abs(rsminusrm) - dctr.val;
        }
        find_zero(dc[0], dc[1], dc[2], twomin, dt1, dt2);
        tret[2] = tjd + dt1.val + twomin;
        tret[3] = tjd + dt2.val + twomin;
        for (m = 0, dt = tensec; m < 2; m++, dt /= 10) {
          for (j = 2; j <= 3; j++) {
            if (sw.swe_calc(tret[j], SweConst.SE_SUN,
                            iflagcart | SweConst.SEFLG_SPEED, xs, serr) ==
                                                                 SweConst.ERR) {
              return SweConst.ERR;
            }
            if (sw.swe_calc(tret[j], SweConst.SE_MOON,
                            iflagcart | SweConst.SEFLG_SPEED, xm, serr) ==
                                                                 SweConst.ERR) {
              return SweConst.ERR;
            }
            for (i = 0; i < 2; i++) {
              if (i == 1) {
                for(k = 0; k < 3; k++) {
                  xs[k] -= xs[k+3] * dt;
                  xm[k] -= xm[k+3] * dt;
                }
              }
              dm = Math.sqrt(sl.square_sum(xm));
              ds = Math.sqrt(sl.square_sum(xs));
              rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
              rsun = Math.asin(RSUN / ds) * SwissData.RADTODEG;
              rsminusrm = rsun - rmoon;
              for (k = 0; k < 3; k++) {
                x1[k] = xs[k] / ds /*ls[2]*/;
                x2[k] = xm[k] / dm /*lm[2]*/;
              }
              dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) *
                                                             SwissData.RADTODEG;
              dc[i] = Math.abs(rsminusrm) - dctr.val;
            }
            dt1.val = -dc[0] / ((dc[0] - dc[1]) / dt);
            tret[j] += dt1.val;
          }
        }
        tret[2] -= SweDate.getDeltaT(tret[2]);
        tret[3] -= SweDate.getDeltaT(tret[3]);
      }
      /* contacts 1 and 4 */
      dc[1] = rsplusrm - dctrmin;
      for (i = 0, t = tjd - twohr; i <= 2; i += 2, t = tjd + twohr) {
        if (sw.swe_calc(t, SweConst.SE_SUN, iflagcart, xs, serr) ==
                                                                 SweConst.ERR) {
          return SweConst.ERR;
        }
        if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) ==
                                                                 SweConst.ERR) {
          return SweConst.ERR;
        }
        dm = Math.sqrt(sl.square_sum(xm));
        ds = Math.sqrt(sl.square_sum(xs));
        rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
        rsun = Math.asin(RSUN / ds) * SwissData.RADTODEG;
        rsplusrm = rsun + rmoon;
        for (k = 0; k < 3; k++) {
          x1[k] = xs[k] / ds /*ls[2]*/;
          x2[k] = xm[k] / dm /*lm[2]*/;
        }
        dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
        dc[i] = rsplusrm - dctr.val;
      }
      find_zero(dc[0], dc[1], dc[2], twohr, dt1, dt2);
      tret[1] = tjd + dt1.val + twohr;
      tret[4] = tjd + dt2.val + twohr;
      for (m = 0, dt = tenmin; m < 3; m++, dt /= 10) {
        for (j = 1; j <= 4; j += 3) {
          if (sw.swe_calc(tret[j], SweConst.SE_SUN,
                          iflagcart | SweConst.SEFLG_SPEED, xs, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(tret[j], SweConst.SE_MOON,
                          iflagcart | SweConst.SEFLG_SPEED, xm, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          for (i = 0; i < 2; i++) {
            if (i == 1) {
              for(k = 0; k < 3; k++) {
                xs[k] -= xs[k+3] * dt;
                xm[k] -= xm[k+3] * dt;
              }
            }
            dm = Math.sqrt(sl.square_sum(xm));
            ds = Math.sqrt(sl.square_sum(xs));
            rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
            rsun = Math.asin(RSUN / ds) * SwissData.RADTODEG;
            rsplusrm = rsun + rmoon;
            for (k = 0; k < 3; k++) {
              x1[k] = xs[k] / ds /*ls[2]*/;
              x2[k] = xm[k] / dm /*lm[2]*/;
            }
            dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
            dc[i] = Math.abs(rsplusrm) - dctr.val;
          }
          dt1.val = -dc[0] / ((dc[0] - dc[1]) / dt);
          tret[j] += dt1.val;
        }
      }
      tret[1] -= SweDate.getDeltaT(tret[1]);
      tret[4] -= SweDate.getDeltaT(tret[4]);
      /*
       * visibility of eclipse phases
       */
      for (i = 4; i >= 0; i--) {        /* attr for i = 0 must be kept !!! */
        if (tret[i] == 0) {
          continue;
        }
        if (eclipse_how(tret[i], SweConst.SE_SUN, null, ifl, geopos[0], geopos[1], geopos[2],
                          attr, serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
        /*if (retflag2 & SweConst.SE_ECL_VISIBLE) {} could be wrong for 1st/4th contact*/
        if (attr[5] > 0) {        /* this is save, sun above horizon */
          retflag |= SweConst.SE_ECL_VISIBLE;
          switch(i) {
          case 0: retflag |= SweConst.SE_ECL_MAX_VISIBLE; break;
          case 1: retflag |= SweConst.SE_ECL_1ST_VISIBLE; break;
          case 2: retflag |= SweConst.SE_ECL_2ND_VISIBLE; break;
          case 3: retflag |= SweConst.SE_ECL_3RD_VISIBLE; break;
          case 4: retflag |= SweConst.SE_ECL_4TH_VISIBLE; break;
          default:  break;
          }
        }
      }
      if ((retflag & SweConst.SE_ECL_VISIBLE)==0) {
        if (backward!=0) {
          K--;
        } else {
          K++;
        }
        continue;
      }
      break;
    } // while (true)
    return retflag;
  }

  private int occult_when_loc(double tjd_start, int ipl, StringBuffer starname,
      int ifl, double[] geopos, double[] tret, double[] attr,
      int backward, StringBuffer serr) {
    int i, j, k, m;
    int retflag = 0;
    double t, tjd, dt;
    DblObj dtint=new DblObj();
    double xs[]=new double[6], xm[]=new double[6], ls[]=new double[6], lm[]=new double[6], x1[]=new double[6], x2[]=new double[6], dm, ds;
    double rmoon, rsun, rsplusrm, rsminusrm;
    double dc[]=new double[20], dctrmin;
    DblObj dctr=new DblObj();
    double twomin = 2.0 / 24.0 / 60.0;
    double tensec = 10.0 / 24.0 / 60.0 / 60.0;
    double twohr = 2.0 / 24.0;
    double tenmin = 10.0 / 24.0 / 60.0;
    DblObj dt1=new DblObj(), dt2=new DblObj();
    double dtdiv, dtstart;
    double dadd2 = 6;
    int nstartpos = 10;
    double drad;
    int iflag = SweConst.SEFLG_TOPOCTR | ifl;
    int iflaggeo = iflag & ~SweConst.SEFLG_TOPOCTR;
    int iflagcart = iflag | SweConst.SEFLG_XYZ;
    int iflagcartgeo = iflaggeo | SweConst.SEFLG_XYZ;
    int direction = 1;
    int one_try = backward & SweConst.SE_ECL_ONE_TRY;
    boolean stop_after_this = false;
    backward &= 1L;
    retflag = 0;
    for (i = 0; i <= 9; i++)
      tret[i] = 0;
    if (backward!=0)
      direction = -1;
    t = tjd_start - direction * 0.1;
    tjd = tjd_start;
    while (true) {
//next_try:
      for (i = 0; i < nstartpos; i++, t += direction * dadd2) {
        if (calc_planet_star(t, ipl, starname, iflagcartgeo, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
        if (sw.swe_calc(t, SweConst.SE_MOON, iflagcartgeo, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
        dc[i] = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
        if (i > 1 && dc[i] > dc[i-1] && dc[i-2] > dc[i-1]) {
          tjd = t - direction*dadd2;
          break;
        } else if (i == nstartpos-1) {
          for (j = 0; j < nstartpos; j++)
              //DevNull.print(dc[j] + " ");
          DevNull.print("swe_lun_occult_when_loc(): problem planet\n");
          return SweConst.ERR;
        }
      }
      /*
       * radius of planet disk in AU
       */
      if (starname != null && starname.length() > 0)
        drad = 0;
      else if (ipl < SwephData.NDIAM)
        drad = SwephData.pla_diam[ipl] / 2 / SweConst.AUNIT;
      else if (ipl > SweConst.SE_AST_OFFSET)
        drad = swed.ast_diam / 2 * 1000 / SweConst.AUNIT; /* km -> m -> AU */
      else
        drad = 0;
      /* now find out, if there is an occultation at our geogr. location */
      dtdiv = 3;
      dtstart = dadd2; /* formerly 0.2 */
      for (dt = dtstart; 
           dt > 0.00001; 
           dt /= dtdiv) {
        if (dt < 0.01) 
          dtdiv = 3;
        for (i = 0, t = tjd - dt; i <= 2; i++, t += dt) {
          /* this takes some time, but is necessary to avoid
           * missing an eclipse */
          if (calc_planet_star(t, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (calc_planet_star(t, ipl, starname, iflag, ls, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(t, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (dt < 1 && Math.abs(ls[1] - lm[1]) > 2) {
            if (one_try != 0) {
              stop_after_this = true;
            } else {
              t = tjd + direction * 2;
//goto next_try;
              continue;
            }
          }
          dc[i] = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
          rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
          rsun = Math.asin(drad / ls[2]) * SwissData.RADTODEG;
          dc[i] -= (rmoon + rsun);
        }
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dctr);
        tjd += dtint.val + dt;
      }
      if (stop_after_this) { /* has one_try = TRUE */
        tret[0] = tjd;
        return 0;
      }
      if (calc_planet_star(tjd, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (calc_planet_star(tjd, ipl, starname, iflag, ls, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (sw.swe_calc(tjd, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (sw.swe_calc(tjd, SweConst.SE_MOON, iflag, lm, serr) == SweConst.ERR)
        return SweConst.ERR;
      dctr.val = Math.acos(sl.swi_dot_prod_unit(xs, xm)) * SwissData.RADTODEG;
      rmoon = Math.asin(RMOON / lm[2]) * SwissData.RADTODEG;
      rsun = Math.asin(drad / ls[2]) * SwissData.RADTODEG;
      rsplusrm = rsun + rmoon;
      rsminusrm = rsun - rmoon;
      if (dctr.val > rsplusrm) {
        if (one_try!=0) {
          tret[0] = tjd;
          return 0;
        }
        t = tjd + direction;
//    goto next_try;
        continue;
      }
      tret[0] = tjd - SweDate.getDeltaT(tjd);
      if ((backward!=0 && tret[0] >= tjd_start - 0.0001) 
        || (backward==0 && tret[0] <= tjd_start + 0.0001)) {
          t = tjd + direction;
//    goto next_try;
        continue;
      }
      if (dctr.val < rsminusrm)
        retflag = SweConst.SE_ECL_ANNULAR;
      else if (dctr.val < Math.abs(rsminusrm))
        retflag = SweConst.SE_ECL_TOTAL;
      else if (dctr.val <= rsplusrm)
        retflag = SweConst.SE_ECL_PARTIAL;
      dctrmin = dctr.val;
      /* contacts 2 and 3 */
      if (dctr.val > Math.abs(rsminusrm))  /* partial, no 2nd and 3rd contact */
        tret[2] = tret[3] = 0;
      else {
        dc[1] = Math.abs(rsminusrm) - dctrmin;
        for (i = 0, t = tjd - twomin; i <= 2; i += 2, t = tjd + twomin) {
          if (calc_planet_star(t, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
          dm = Math.sqrt(sl.square_sum(xm));
          ds = Math.sqrt(sl.square_sum(xs));
          rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
          rsun = Math.asin(drad / ds) * SwissData.RADTODEG;
          rsminusrm = rsun - rmoon;
          for (k = 0; k < 3; k++) {
            x1[k] = xs[k] / ds /*ls[2]*/;
            x2[k] = xm[k] / dm /*lm[2]*/;
          }
          dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
          dc[i] = Math.abs(rsminusrm) - dctr.val;
        }
        find_zero(dc[0], dc[1], dc[2], twomin, dt1, dt2);
        tret[2] = tjd + dt1.val + twomin;
        tret[3] = tjd + dt2.val + twomin;
        for (m = 0, dt = tensec; m < 2; m++, dt /= 10) {
          for (j = 2; j <= 3; j++) {
            if (calc_planet_star(tret[j], ipl, starname, iflagcart | SweConst.SEFLG_SPEED, xs, serr) == SweConst.ERR)
              return SweConst.ERR;
            if (sw.swe_calc(tret[j], SweConst.SE_MOON, iflagcart | SweConst.SEFLG_SPEED, xm, serr) == SweConst.ERR)
              return SweConst.ERR;
            for (i = 0; i < 2; i++) {
              if (i == 1) {
                for(k = 0; k < 3; k++) {
                  xs[k] -= xs[k+3] * dt;
                  xm[k] -= xm[k+3] * dt;
                }
              }
              dm = Math.sqrt(sl.square_sum(xm));
              ds = Math.sqrt(sl.square_sum(xs));
              rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
              rsun = Math.asin(drad / ds) * SwissData.RADTODEG;
              rsminusrm = rsun - rmoon;
              for (k = 0; k < 3; k++) {
                x1[k] = xs[k] / ds /*ls[2]*/;
                x2[k] = xm[k] / dm /*lm[2]*/;
              }
              dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
              dc[i] = Math.abs(rsminusrm) - dctr.val;
            }
            dt1.val = -dc[0] / ((dc[0] - dc[1]) / dt);
            tret[j] += dt1.val;
          }
        }
        tret[2] -= SweDate.getDeltaT(tret[2]);
        tret[3] -= SweDate.getDeltaT(tret[3]);
      }
      /* contacts 1 and 4 */
      dc[1] = rsplusrm - dctrmin;
      for (i = 0, t = tjd - twohr; i <= 2; i += 2, t = tjd + twohr) {
        if (calc_planet_star(t, ipl, starname, iflagcart, xs, serr) == SweConst.ERR)
          return SweConst.ERR;
        if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) == SweConst.ERR)
          return SweConst.ERR;
        dm = Math.sqrt(sl.square_sum(xm));
        ds = Math.sqrt(sl.square_sum(xs));
        rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
        rsun = Math.asin(drad / ds) * SwissData.RADTODEG;
        rsplusrm = rsun + rmoon;
        for (k = 0; k < 3; k++) {
          x1[k] = xs[k] / ds /*ls[2]*/;
          x2[k] = xm[k] / dm /*lm[2]*/;
        }
        dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
        dc[i] = rsplusrm - dctr.val;
      }
      find_zero(dc[0], dc[1], dc[2], twohr, dt1, dt2);
      tret[1] = tjd + dt1.val + twohr;
      tret[4] = tjd + dt2.val + twohr;
      for (m = 0, dt = tenmin; m < 3; m++, dt /= 10) {
        for (j = 1; j <= 4; j += 3) {
          if (calc_planet_star(tret[j], ipl, starname, iflagcart | SweConst.SEFLG_SPEED, xs, serr) == SweConst.ERR)
            return SweConst.ERR;
          if (sw.swe_calc(tret[j], SweConst.SE_MOON, iflagcart | SweConst.SEFLG_SPEED, xm, serr) == SweConst.ERR)
            return SweConst.ERR;
          for (i = 0; i < 2; i++) {
            if (i == 1) {
              for(k = 0; k < 3; k++) {
                xs[k] -= xs[k+3] * dt;
                xm[k] -= xm[k+3] * dt;
              }
            }
            dm = Math.sqrt(sl.square_sum(xm));
            ds = Math.sqrt(sl.square_sum(xs));
            rmoon = Math.asin(RMOON / dm) * SwissData.RADTODEG;
            rsun = Math.asin(drad / ds) * SwissData.RADTODEG;
            rsplusrm = rsun + rmoon;
            for (k = 0; k < 3; k++) {
              x1[k] = xs[k] / ds /*ls[2]*/;
              x2[k] = xm[k] / dm /*lm[2]*/;
            }
            dctr.val = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
            dc[i] = Math.abs(rsplusrm) - dctr.val;
          }
          dt1.val = -dc[0] / ((dc[0] - dc[1]) / dt);
          tret[j] += dt1.val;
        }
      }
      tret[1] -= SweDate.getDeltaT(tret[1]);
      tret[4] -= SweDate.getDeltaT(tret[4]);
      /*  
       * visibility of eclipse phases 
       */
      for (i = 4; i >= 0; i--) {	/* attr for i = 0 must be kept !!! */
        if (tret[i] == 0)
          continue;
        if (eclipse_how(tret[i], ipl, starname, ifl, geopos[0], geopos[1], geopos[2], 
    		attr, serr) == SweConst.ERR)
          return SweConst.ERR;
        /*if (retflag2 & SweConst.SE_ECL_VISIBLE) { could be wrong for 1st/4th contact } */
        if (attr[5] > 0) {	/* this is save, sun above horizon */
          retflag |= SweConst.SE_ECL_VISIBLE;
          switch(i) {
          case 0: retflag |= SweConst.SE_ECL_MAX_VISIBLE; break;
          case 1: retflag |= SweConst.SE_ECL_1ST_VISIBLE; break;
          case 2: retflag |= SweConst.SE_ECL_2ND_VISIBLE; break;
          case 3: retflag |= SweConst.SE_ECL_3RD_VISIBLE; break;
          case 4: retflag |= SweConst.SE_ECL_4TH_VISIBLE; break;
          default:  break;
          }
        }
      }
      if ((retflag & SweConst.SE_ECL_VISIBLE)==0) {
        t = tjd + direction;
//    goto next_try;
        continue;
      }
      break; // next_try
    } // while (true) .. [goto next_try]
    return retflag;
  }

  /*
   * swe_azalt()
   * Computes azimut and height, from either ecliptic or
   * equatorial coordinates
   *
   * input:
   *   tjd_ut
   *   iflag        either SE_ECL2HOR or SE_EQU2HOR
   *   geopos[3]    geograph. longitude, latitude, height above sea
   *   atpress      atmospheric pressure at geopos in millibars (hPa)
   *   attemp       atmospheric temperature in degrees C
   *   xin[2]       input coordinates polar, in degrees
   *
   * Horizontal coordinates are returned in
   *   xaz[3]       xaz[0] = azimuth
   *                xaz[1] = true altitude
   *                xaz[2] = apparent altitude
   *
   * If atpress is not given (= 0), the programm assumes 1013.25 mbar;
   * if a non-zero height above sea is given, atpress is estimated.
   *   geohgt       height of observer above sea (optional)
   */
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
  * @param geopos An array double[3] containing the longitude, latitude and
  * height of the geographic position
  * @param atpress atmospheric pressure in mBar (hPa). If it is 0, the pressure
  * will be estimated from geopos[2] and attemp.
  * @param attemp atmospheric temperature in degrees Celsius.
  * @param xin double[3] with a content depending on parameter calc_flag.
  * See there. xin[3] does not need to be defined.
  * @param xaz Output parameter: a double[3] returning values as specified
  * above.
  */
  void swe_azalt(double tjd_ut,
                 int calc_flag,
                 double[] geopos,
                 double atpress,
                 double attemp,
                 double[] xin,
                 double[] xaz) {
    int i;
    double x[]=new double[6], xra[]=new double[3];
    double armc = sl.swe_degnorm(sl.swe_sidtime(tjd_ut) * 15 + geopos[0]);
    double mdd, eps_true, tjd_et;
    for (i = 0; i < 2; i++)
      xra[i] = xin[i];
    xra[2] = 1;
    if (calc_flag == SweConst.SE_ECL2HOR) {
          tjd_et = tjd_ut + SweDate.getDeltaT(tjd_ut);
      sw.swe_calc(tjd_et, SweConst.SE_ECL_NUT, 0, x, null);
      eps_true = x[0];
          sl.swe_cotrans(xra, 0, xra, 0, -eps_true);
    }
    mdd = sl.swe_degnorm(xra[0] - armc);
    x[0] = sl.swe_degnorm(mdd - 90);
    x[1] = xra[1];
    x[2] = 1;
    /* azimuth from east, counterclock */
    sl.swe_cotrans(x, 0, x, 0, 90 - geopos[1]);
    /* azimuth from south to west */
    x[0] = sl.swe_degnorm(x[0] + 90);
    xaz[0] = 360 - x[0];
    xaz[1] = x[1];                /* true height */
    if (atpress == 0) {
      /* estimate atmospheric pressure */
      atpress = 1013.25 * Math.pow(1 - 0.0065 * geopos[2] / 288, 5.255);
    }
    xaz[2] = swe_refrac(x[1], atpress, attemp, SweConst.SE_TRUE_TO_APP);
  }

  /*
   * swe_azalt_rev()
   * computes either ecliptical or equatorial coordinates from
   * azimuth and true altitude in degrees.
   * For conversion between true and apparent altitude, there is
   * the function swe_refrac().
   *
   * input:
   *   tjd_ut
   *   iflag        either SE_HOR2ECL or SE_HOR2EQU
   *   xin[2]       azimut and true altitude, in degrees
   */
  /**
  * Computes either ecliptic or equatorial coordinates from azimuth and true
  * altitude. The true altitude might be gained from an apparent altitude by
  * calling swe_refrac.<P>xout is an output parameter containing the ecliptic
  * or equatorial coordinates, depending on the value of the parameter
  * calc_flag.
  * @param tjd_ut time and date in UT
  * @param calc_flag SweConst.SE_HOR2ECL or SweConst.SE_HOR2EQU
  * @param geopos An array double[3] containing the longitude, latitude and
  * height of the geographic position
  * @param xin double[2] with azimuth and true altitude of planet
  * @param xout Output parameter: a double[2] returning either ecliptic or
  * equatorial coordinates depending on parameter 'calc_flag'
  */
  void swe_azalt_rev(double tjd_ut,
                     int calc_flag,
                     double[] geopos,
                     double[] xin,
                     double[] xout) {
    int i;
    double x[]=new double[6], xaz[]=new double[3];
    double geolon = geopos[0];
    double geolat = geopos[1];
    double armc = sl.swe_degnorm(sl.swe_sidtime(tjd_ut) * 15 + geolon);
    double eps_true, tjd_et;
    for (i = 0; i < 2; i++)
      xaz[i] = xin[i];
    xaz[2] = 1;
    /* azimuth is from south, clockwise.
     * we need it from east, counterclock */
    xaz[0] = 360 - xaz[0];
    xaz[0] = sl.swe_degnorm(xaz[0] - 90);
    /* equatorial positions */
    sl.swe_cotrans(xaz, 0, xaz, 0, geolat - 90);
    xaz[0] = sl.swe_degnorm(xaz[0] + armc + 90);
    xout[0] = xaz[0];
    xout[1] = xaz[1];
    /* ecliptic positions */
    if (calc_flag == SweConst.SE_HOR2ECL) {
      tjd_et = tjd_ut + SweDate.getDeltaT(tjd_ut);
      sw.swe_calc(tjd_et, SweConst.SE_ECL_NUT, 0, x, null);
      eps_true = x[0];
      sl.swe_cotrans(xaz, 0, x, 0, eps_true);
      xout[0] = x[0];
      xout[1] = x[1];
    }
  }

  /* swe_refrac()
   * Transforms apparent to true altitude and vice-versa.
   * These formulae do not handle the case when the
   * sun is visible below the geometrical horizon
   * (from a mountain top or an air plane)
   * input:
   * double inalt;        * altitude of object in degrees *
   * double atpress;      * millibars (hectopascal) *
   * double attemp;       * degrees C *
   * int32  calc_flag;    * either SE_CALC_APP_TO_TRUE or
   *                      *        SE_CALC_TRUE_TO_APP
   */
  /**
  * Calculates the true altitude from the apparent altitude or vice versa.
  * @param inalt The true or apparent altitude to be converted
  * @param atpress Atmospheric pressure in mBar (hPa). If it is 0, the pressure
  * will be estimated from attemp on sea level.
  * @param attemp Atmospheric temperature in degrees Celsius.
  * @param calc_flag SweConst.SE_TRUE_TO_APP or SweConst.SE_APP_TO_TRUE
  * @return The converted altitude
  */
  double swe_refrac(double inalt, double atpress, double attemp,
                    int calc_flag) {
    double a, refr;
    double pt_factor = atpress / 1010.0 * 283.0 / (273.0 + attemp);
    double trualt, appalt;
    /* another algorithm, from Meeus, German, p. 114ff.
     */
    if (calc_flag == SweConst.SE_TRUE_TO_APP) {
      trualt = inalt;
      if (trualt > 15) {
        a = Math.tan((90 - trualt) * SwissData.DEGTORAD);
        refr = (58.276 * a - 0.0824 * a * a * a);
        refr *=  pt_factor / 3600.0;
      } else if (trualt > -5) {
        /* the following tan is not defined for a value
         * of trualt near -5.00158 and 89.89158 */
        a = trualt + 10.3 / (trualt + 5.11);
        if (a + 1e-10 >= 90) {
          refr = 0;
        } else {
          refr = 1.02 / Math.tan(a * SwissData.DEGTORAD);
        }
        refr *= pt_factor / 60.0;
      } else {
        refr = 0;
      }
      appalt = trualt;
      if (appalt + refr > 0) {
        appalt += refr;
      }
      return appalt;
    } else { // SE_TRUE_TO_APP
      appalt = inalt;
      if (appalt > 15) {
        a = Math.tan((90 - appalt) * SwissData.DEGTORAD);
        refr = (58.294 * a - 0.0668 * a * a * a);
        refr *=  pt_factor / 3600.0;
      } else {
        /* the following tan is not defined for a value
         * of inalt near -4.3285 and 89.9225 */
        a = appalt + 7.31 / (appalt + 4.4);
        if (a + 1e-10 >= 90) {
          refr = 0;
        } else {
          refr = 1.00 / Math.tan(a * SwissData.DEGTORAD);
          refr -= 0.06 * Math.sin(14.7 * refr + 13);
        }
        refr *= pt_factor / 60.0;
      }
      trualt = appalt;
      if (trualt - refr > 0) {
        trualt -= refr;
      }
      return trualt;
    }
  }

  /* Computes attributes of a lunar eclipse for given tjd and geopos
   *
   * retflag        SE_ECL_TOTAL or SE_ECL_PARTIAL
   *              SE_ECL_PENUMBRAL
   *              if 0, there is no eclipse
   *
   * attr[0]        umbral magnitude at tjd
   * attr[1]      penumbral magnitude
   * attr[7]        distance of moon from opposition in degrees
   *         declare as attr[20] at least !
   *
   */
  /**
  * Computes the attributes of a lunar eclipse for a given Julian Day,
  * geographic longitude, latitude, and height.
  * <BLOCKQUOTE><P><CODE>
  * attr[0]:&nbsp;&nbsp;&nbsp;umbral magnitude at tjd<BR>
  * (magnitude)<BR>
  * attr[1]:&nbsp;&nbsp;&nbsp;penumbral magnitude<BR>
  * attr[4]:&nbsp;&nbsp;&nbsp;azimuth of moon at tjd. <I>Not yet
  * implemented.</I><BR>
  * attr[5]:&nbsp;&nbsp;&nbsp;true altitude of moon above horizon at tjd.
  * <I>Not yet implemented.</I><BR>
  * attr[6]:&nbsp;&nbsp;&nbsp;apparent altitude of moon above horizon at tjd.
  * <I>Not yet implemented.</I><BR>
  * attr[7]:&nbsp;&nbsp;&nbsp;distance of moon from opposition in degrees
  * </CODE><P></BLOCKQUOTE><B>Attention: attr must be a double[20]!</B>
  * @param tjd_ut The Julian Day number in UT
  * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
  * SEFLG_SWIEPH or SEFLG_MOSEPH)
  * @param geopos A double[3] containing geographic longitude, latitude and
  * height in meters above sea level in this order.
  * @param attr An array[20], on return containing the attributes of the
  * eclipse as above
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails
  * @return -1 (SweConst.ERR), if the calculation failed<BR>
  * 0, if there is no lunar eclipse at that time and location<BR>
  * SweConst.SE_ECL_TOTAL<BR>
  * SweConst.SE_ECL_PENUMBRAL<BR>
  * SweConst.SE_ECL_PARTIAL
  */
  int swe_lun_eclipse_how(double tjd_ut,
                          int ifl,
                          double[] geopos,
                          double[] attr,
                          StringBuffer serr) {
    double dcore[]=new double[10];
    /* attention: geopos[] is not used so far; may be null */
    // geopos[0] = geopos[0]; /* to shut up mint */
    ifl = ifl & ~SweConst.SEFLG_TOPOCTR;
    return lun_eclipse_how(tjd_ut, ifl, attr, dcore, serr);
  }

  /*
   * attr[]:         see swe_lun_eclipse_how()
   *
   * dcore[0]:        distance of shadow axis from geocenter r0
   * dcore[1]:        diameter of core shadow on fundamental plane d0
   * dcore[2]:        diameter of half-shadow on fundamental plane D0
   */
  private int lun_eclipse_how(double tjd_ut,
                              int ifl,
                              double[] attr,
                              double[] dcore,
                              StringBuffer serr) {
    int i;
    int retc = 0;
    double e[]=new double[6], rm[]=new double[6], rs[]=new double[6];
    double dsm, d0, D0, s0, r0, ds, dm;
    double dctr, x1[]=new double[6], x2[]=new double[6];
    double f1, f2;
    double deltat, tjd;
    double cosf1, cosf2;
    int iflag;
    for (i = 0; i < 10; i++)
      dcore[i] = 0;
    for (i = 0; i < 20; i++)
      attr[i] = 0;
    /* nutation need not be in lunar and solar positions,
     * if mean sidereal time will be used */
    iflag = SweConst.SEFLG_SPEED | SweConst.SEFLG_EQUATORIAL | ifl;
    iflag  = iflag | SweConst.SEFLG_XYZ;
    deltat = SweDate.getDeltaT(tjd_ut);
    tjd = tjd_ut + deltat;
    /* moon in cartesian coordinates */
    if (sw.swe_calc(tjd, SweConst.SE_MOON, iflag, rm, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    /* distance of moon from geocenter */
    dm = Math.sqrt(sl.square_sum(rm));
    /* sun in cartesian coordinates */
    if (sw.swe_calc(tjd, SweConst.SE_SUN, iflag, rs, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    /* distance of sun from geocenter */
    ds = Math.sqrt(sl.square_sum(rs));
    for (i = 0; i < 3; i++) {
      x1[i] = rs[i] / ds;
      x2[i] = rm[i] / dm;
    }
    dctr = Math.acos(sl.swi_dot_prod_unit(x1, x2)) * SwissData.RADTODEG;
    /* selenocentric sun */
    for (i = 0; i <= 2; i++)
      rs[i] -= rm[i];
    /* selenocentric earth */
    for (i = 0; i <= 2; i++)
      rm[i] = -rm[i];
    /* sun - earth vector */
    for (i = 0; i <= 2; i++)
      e[i] = (rm[i] - rs[i]);
    /* distance sun - earth */
    dsm = Math.sqrt(sl.square_sum(e));
    /* sun - earth unit vector */
    for (i = 0; i <= 2; i++)
      e[i] /= dsm;
    f1 = ((RSUN - REARTH) / dsm);
    cosf1 = Math.sqrt(1 - f1 * f1);
    f2 = ((RSUN + REARTH) / dsm);
    cosf2 = Math.sqrt(1 - f2 * f2);
    /* distance of earth from fundamental plane */
    s0 = -sw.dot_prod(rm, e);
    /* distance of shadow axis from selenocenter */
    r0 = Math.sqrt(dm * dm - s0 * s0);
    /* diameter of core shadow on fundamental plane */
    d0 = Math.abs(s0 / dsm * (DSUN - DEARTH) - DEARTH) * (1 + 1.0 / 50) / cosf1;
           /* one 50th is added for effect of atmosphere, AA98, L4 */
    /* diameter of half-shadow on fundamental plane */
    D0 = (s0 / dsm * (DSUN + DEARTH) + DEARTH) * (1 + 1.0 / 50) / cosf2;
    d0 /= cosf1;
    D0 /= cosf2;
    dcore[0] = r0;
    dcore[1] = d0;
    dcore[2] = D0;
    dcore[3] = cosf1;
    dcore[4] = cosf2;
    /**************************
     * phase and umbral magnitude
     **************************/
    retc = 0;
    if (d0 / 2 >= r0 + RMOON / cosf1) {
      retc = SweConst.SE_ECL_TOTAL;
      attr[0] = (d0 / 2 - r0 + RMOON) / DMOON;
    } else if (d0 / 2 >= r0 - RMOON / cosf1) {
      retc = SweConst.SE_ECL_PARTIAL;
      attr[0] = (d0 / 2 - r0 + RMOON) / DMOON;
    } else if (D0 / 2 >= r0 - RMOON / cosf2) {
      retc = SweConst.SE_ECL_PENUMBRAL;
      attr[0] = 0;
    } else {
      if (serr != null) {
        serr.setLength(0);
        serr.append("no lunar eclipse at tjd = "+tjd);
      }
    }
    /**************************
     * penumbral magnitude
     **************************/
    attr[1] = (D0 / 2 - r0 + RMOON) / DMOON;
    if (retc != 0) {
      attr[7] = 180 - Math.abs(dctr);
    }
    return retc;
  }

  /* When is the next lunar eclipse?
   *
   * retflag        SE_ECL_TOTAL or SE_ECL_PENUMBRAL or SE_ECL_PARTIAL
   *
   * tret[0]        time of maximum eclipse
   * tret[1]
   * tret[2]        time of partial phase begin (indices consistent with solar eclipses)
   * tret[3]        time of partial phase end
   * tret[4]        time of totality begin
   * tret[5]        time of totality end
   * tret[6]        time of penumbral phase begin
   * tret[7]        time of penumbral phase end
   */
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
  * @param tret An array[10], on return containing the times of different
  * occasions of the eclipse as above
  * @param backward true, if search should be done backwards
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails.
  * @return -1 (SweConst.ERR), if the calculation failed<BR>
  * SweConst.SE_ECL_TOTAL<BR>
  * SweConst.SE_ECL_ANNULAR<BR>
  * SweConst.SE_ECL_PARTIAL<BR>
  * SweConst.SE_ECL_ANNULAR_TOTAL<BR>in combination with:<BR>
  * SweConst.SE_ECL_CENTRAL<BR>
  * SweConst.SE_ECL_NONCENTRAL
  */
  int swe_lun_eclipse_when(double tjd_start, int ifl, int ifltype,
                           double[] tret, int backward,
                           StringBuffer serr) {
    int i, j, m, n, o, i1 = 0, i2 = 0;
    int retflag = 0, retflag2 = 0;
    double t, tjd, dt, dta, dtb;
    DblObj dtint=new DblObj();
    double T, T2, T3, T4, K, F, M, Mm;
    double E, Ff, F1, A1, Om;
    double xs[]=new double[6], xm[]=new double[6], dm, ds;
    double rsun, rearth, dcore[]=new double[10];
    double dc[]=new double[3];
    DblObj dctr=new DblObj();
    double twohr = 2.0 / 24.0;
    double tenmin = 10.0 / 24.0 / 60.0;
    DblObj dt1=new DblObj(), dt2=new DblObj();
    double kk;
    double attr[]=new double[20];
    double dtstart, dtdiv;
    double xa[]=new double[6], xb[]=new double[6];
    int direction = 1;
    int iflag;
    int iflagcart;
    ifl &= SweConst.SEFLG_EPHMASK;
    iflag = SweConst.SEFLG_EQUATORIAL | ifl;
    iflagcart = iflag | SweConst.SEFLG_XYZ;
    if (ifltype == 0) {
      ifltype = SweConst.SE_ECL_TOTAL | SweConst.SE_ECL_PENUMBRAL |
                SweConst.SE_ECL_PARTIAL;
    }
    if (backward!=0) {
      direction = -1;
    }
    K = (int) ((tjd_start - SwephData.J2000) / 365.2425 * 12.3685);
    K -= direction;
//next_try:
    while (true) {
      retflag = 0;
      for (i = 0; i <= 9; i++)
        tret[i] = 0;
      kk = K + 0.5;
      T = kk / 1236.85;
      T2 = T * T; T3 = T2 * T; T4 = T3 * T;
      Ff = F = sl.swe_degnorm(160.7108 + 390.67050274 * kk
                   - 0.0016341 * T2
                   - 0.00000227 * T3
                   + 0.000000011 * T4);
      if (Ff > 180) {
        Ff -= 180;
      }
      if (Ff > 21 && Ff < 159) {         /* no eclipse possible */
        K += direction;
        continue;
      }
      /* approximate time of geocentric maximum eclipse
       * formula from Meeus, German, p. 381 */
      tjd = 2451550.09765 + 29.530588853 * kk
                          + 0.0001337 * T2
                          - 0.000000150 * T3
                          + 0.00000000073 * T4;
      M = sl.swe_degnorm(2.5534 + 29.10535669 * kk
                          - 0.0000218 * T2
                          - 0.00000011 * T3);
      Mm = sl.swe_degnorm(201.5643 + 385.81693528 * kk
                          + 0.1017438 * T2
                          + 0.00001239 * T3
                          + 0.000000058 * T4);
      Om = sl.swe_degnorm(124.7746 - 1.56375580 * kk
                          + 0.0020691 * T2
                          + 0.00000215 * T3);
      E = 1 - 0.002516 * T - 0.0000074 * T2;
      A1 = sl.swe_degnorm(299.77 + 0.107408 * kk - 0.009173 * T2);
      M *= SwissData.DEGTORAD;
      Mm *= SwissData.DEGTORAD;
      F *= SwissData.DEGTORAD;
      Om *= SwissData.DEGTORAD;
      F1 = F - 0.02665 * Math.sin(Om) * SwissData.DEGTORAD;
      A1 *= SwissData.DEGTORAD;
      tjd = tjd - 0.4075 * Math.sin(Mm)
                + 0.1721 * E * Math.sin(M)
                + 0.0161 * Math.sin(2 * Mm)
                - 0.0097 * Math.sin(2 * F1)
                + 0.0073 * E * Math.sin(Mm - M)
                - 0.0050 * E * Math.sin(Mm + M)
                - 0.0023 * Math.sin(Mm - 2 * F1)
                + 0.0021 * E * Math.sin(2 * M)
                + 0.0012 * Math.sin(Mm + 2 * F1)
                + 0.0006 * E * Math.sin(2 * Mm + M)
                - 0.0004 * Math.sin(3 * Mm)
                - 0.0003 * E * Math.sin(M + 2 * F1)
                + 0.0003 * Math.sin(A1)
                - 0.0002 * E * Math.sin(M - 2 * F1)
                - 0.0002 * E * Math.sin(2 * Mm - M)
                - 0.0002 * Math.sin(Om);
      /*
       * precise computation:
       * time of maximum eclipse (if eclipse) =
       * minimum selenocentric angle between sun and earth edges.
       * After this time has been determined, check
       * whether or not an eclipse is taking place with
       * the function lun_eclipse_how().
       */
      dtstart = 0.1;
      if (tjd < 2000000) {
        dtstart = 5;
      }
      dtdiv = 4;
      for (j = 0, dt = dtstart;
           dt > 0.001;
           j++, dt /= dtdiv) {
        for (i = 0, t = tjd - dt; i <= 2; i++, t += dt) {
          if (sw.swe_calc(t, SweConst.SE_SUN, iflagcart, xs, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          if (sw.swe_calc(t, SweConst.SE_MOON, iflagcart, xm, serr) ==
                                                                 SweConst.ERR) {
            return SweConst.ERR;
          }
          for (m = 0; m < 3; m++) {
            xs[m] -= xm[m];        /* selenocentric sun */
            xm[m] = -xm[m];        /* selenocentric earth */
          }
          ds = Math.sqrt(sl.square_sum(xs));
          dm = Math.sqrt(sl.square_sum(xm));
          for (m = 0; m < 3; m++) {
            xa[m] = xs[m] / ds;
            xb[m] = xm[m] / dm;
          }
          dc[i] = Math.acos(sl.swi_dot_prod_unit(xa, xb)) * SwissData.RADTODEG;
          rearth = Math.asin(REARTH / dm) * SwissData.RADTODEG;
          rsun = Math.asin(RSUN / ds) * SwissData.RADTODEG;
          dc[i] -= (rearth + rsun);
        }
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dctr);
        tjd += dtint.val + dt;
      }
      tjd = tjd - SweDate.getDeltaT(tjd);
      if ((retflag = swe_lun_eclipse_how(tjd, ifl, null, attr, serr)) ==
                                                                 SweConst.ERR) {
        return retflag;
      }
      if (retflag == 0) {
        K += direction;
        continue;
      }
      tret[0] = tjd;
      if ((backward!=0 && tret[0] >= tjd_start - 0.0001)
        || (backward==0 && tret[0] <= tjd_start + 0.0001)) {
        K += direction;
        continue;
      }
      /*
       * check whether or not eclipse type found is wanted
       */
      /* non penumbral eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_PENUMBRAL)==0 &&
          (retflag & SweConst.SE_ECL_PENUMBRAL)!=0) {
        K += direction;
        continue;
      }
      /* non partial eclipse is wanted: */
      if ((ifltype & SweConst.SE_ECL_PARTIAL)==0 &&
          (retflag & SweConst.SE_ECL_PARTIAL)!=0) {
        K += direction;
        continue;
      }
      /* annular-total eclipse will be discovered later */
      if ((ifltype & (SweConst.SE_ECL_TOTAL))==0 &&
          (retflag & SweConst.SE_ECL_TOTAL)!=0) {
        K += direction;
        continue;
      }
      /*
       * n = 0: times of eclipse begin and end
       * n = 1: times of totality begin and end
       * n = 2: times of center line begin and end
       */
      if ((retflag & SweConst.SE_ECL_PENUMBRAL)!=0) {
        o = 0;
      } else if ((retflag & SweConst.SE_ECL_PARTIAL)!=0) {
        o = 1;
      } else {
        o = 2;
      }
      dta = twohr;
      dtb = tenmin;
      for (n = 0; n <= o; n++) {
        if (n == 0) {
          i1 = 6; i2 = 7;
        } else if (n == 1) {
          i1 = 2; i2 = 3;
        } else if (n == 2) {
          i1 = 4; i2 = 5;
        }
        for (i = 0, t = tjd - dta; i <= 2; i += 1, t += dta) {
          if ((retflag2 = lun_eclipse_how(t, ifl, attr, dcore, serr)) ==
                                                                 SweConst.ERR) {
            return retflag2;
          }
          if (n == 0) {
            dc[i] = dcore[2] / 2 + RMOON / dcore[4] - dcore[0];
          } else if (n == 1) {
            dc[i] = dcore[1] / 2 + RMOON / dcore[3] - dcore[0];
          } else if (n == 2) {
            dc[i] = dcore[1] / 2 - RMOON / dcore[3] - dcore[0];
          }
        }
        find_zero(dc[0], dc[1], dc[2], dta, dt1, dt2);
        dtb = (dt1.val + dta) / 2;
        tret[i1] = tjd + dt1.val + dta;
        tret[i2] = tjd + dt2.val + dta;
        for (m = 0, dt = dtb / 2; m < 3; m++, dt /= 2) {
          for (j = i1; j <= i2; j += (i2 - i1)) {
            for (i = 0, t = tret[j] - dt; i < 2; i++, t += dt) {
              if ((retflag2 = lun_eclipse_how(t, ifl, attr, dcore, serr)) ==
                                                                 SweConst.ERR) {
                return retflag2;
              }
              if (n == 0) {
                dc[i] = dcore[2] / 2 + RMOON / dcore[4] - dcore[0];
              } else if (n == 1) {
                dc[i] = dcore[1] / 2 + RMOON / dcore[3] - dcore[0];
              } else if (n == 2) {
                dc[i] = dcore[1] / 2 - RMOON / dcore[3] - dcore[0];
              }
            }
            dt1.val = dc[1] / ((dc[1] - dc[0]) / dt);
            tret[j] -= dt1.val;
          }
        }
      }
      break;
    } // while (true)
    return retflag;
  }

  /*
   * function calculates planetary phenomena
   *
   * attr[0] = phase angle (earth-planet-sun)
   * attr[1] = phase (illumined fraction of disc)
   * attr[2] = elongation of planet
   * attr[3] = apparent diameter of disc
   * attr[4] = apparent magnitude
   *         declare as attr[20] at least !
   *
   * Note: the lunar magnitude is quite a complicated thing,
   * but our algorithm is very simple.
   * The phase of the moon, its distance from the earth and
   * the sun is considered, but no other factors.
   *
   */
  private static final double EULER=2.718281828459;
  private static final int NMAG_ELEM=SweConst.SE_VESTA + 1;

  private static final double mag_elem[][] = {
                  /* DTV-Atlas Astronomie, p. 32 */
                  {-26.86, 0, 0, 0},
                  {-12.55, 0, 0, 0},
                  /* IAU 1986 */
                  {-0.42, 3.80, -2.73, 2.00},
                  {-4.40, 0.09, 2.39, -0.65},
                  {- 1.52, 1.60, 0, 0},   /* Mars */
                  {- 9.40, 0.5, 0, 0},    /* Jupiter */
                  {- 8.88, -2.60, 1.25, 0.044},   /* Saturn */
                  {- 7.19, 0.0, 0, 0},    /* Uranus */
                  {- 6.87, 0.0, 0, 0},    /* Neptune */
                  {- 1.00, 0.0, 0, 0},    /* Pluto */
                  {99, 0, 0, 0},          /* nodes and apogees */
                  {99, 0, 0, 0},
                  {99, 0, 0, 0},
                  {99, 0, 0, 0},
                  {99, 0, 0, 0},          /* Earth */
                  /* from Bowell data base */
                  {6.5, 0.15, 0, 0},      /* Chiron */
                  {7.0, 0.15, 0, 0},      /* Pholus */
                  {3.34, 0.12, 0, 0},     /* Ceres */
                  {4.13, 0.11, 0, 0},     /* Pallas */
                  {5.33, 0.32, 0, 0},     /* Juno */
                  {3.20, 0.32, 0, 0},     /* Vesta */
                  };

  /**
  * Computes phase, phase angel, elongation, apparent diameter and apparent
  * magnitude for sun, moon, all planets and asteroids. This method is
  * identical to swe_pheno_ut() with the one exception that the time
  * has to be given in ET (Ephemeris Time or Dynamical Time). You
  * would get ET by adding deltaT to the UT, e.g.,
  * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
  * See <A HREF="Swecl.html#swe_pheno_ut(double, int, int, double[], java.lang.StringBuffer)">swe_pheno_ut(...)</A> for missing information.
  */
  int swe_pheno(double tjd, int ipl, int iflag, double[] attr,
                StringBuffer serr) {
    int i;
    double xx[]=new double[6], xx2[]=new double[6], xxs[]=new double[6],
           lbr[]=new double[6], lbr2[]=new double[6], dt = 0, dsm, dd;
    double fac;
    double T, in, om, sinB, u1, u2, du;
    double ph1, ph2, me[]=new double[2];
    int iflagp;
    for (i = 0; i < 20; i++)
      attr[i] = 0;
    /* Ceres - Vesta must be SE_CERES etc., not 10001 etc. */
    if (ipl > SweConst.SE_AST_OFFSET && ipl <= SweConst.SE_AST_OFFSET + 4) {
      ipl = ipl - SweConst.SE_AST_OFFSET - 1 + SweConst.SE_CERES;
    }
    iflag = iflag & (SweConst.SEFLG_EPHMASK |
                     SweConst.SEFLG_TRUEPOS |
                     SweConst.SEFLG_J2000 |
                     SweConst.SEFLG_NONUT |
                     SweConst.SEFLG_NOGDEFL |
                     SweConst.SEFLG_NOABERR |
                     SweConst.SEFLG_TOPOCTR);
    iflagp = iflag & (SweConst.SEFLG_EPHMASK |
                     SweConst.SEFLG_TRUEPOS |
                     SweConst.SEFLG_J2000 |
                     SweConst.SEFLG_NONUT |
                     SweConst.SEFLG_NOABERR);
    iflagp |= SweConst.SEFLG_HELCTR;
    /*
     * geocentric planet
     */
    if (sw.swe_calc(tjd, ipl, iflag | SweConst.SEFLG_XYZ, xx, serr) ==
                                                                 SweConst.ERR) {
      return SweConst.ERR;
    }
    if (sw.swe_calc(tjd, ipl, iflag, lbr, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    /* if moon, we need sun as well, for magnitude */
    if (ipl == SweConst.SE_MOON) {
      if (sw.swe_calc(tjd, SweConst.SE_SUN, iflag | SweConst.SEFLG_XYZ,
                      xxs, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
    }
    if (ipl != SweConst.SE_SUN && ipl != SweConst.SE_EARTH &&
      ipl != SweConst.SE_MEAN_NODE && ipl != SweConst.SE_TRUE_NODE &&
      ipl != SweConst.SE_MEAN_APOG && ipl != SweConst.SE_OSCU_APOG) {
      /*
       * light time planet - earth
       */
      dt = lbr[2] * SweConst.AUNIT / SwephData.CLIGHT / 86400.0;
      if ((iflag & SweConst.SEFLG_TRUEPOS)!=0) {
        dt = 0;
      }
      /*
       * heliocentric planet at tjd - dt
       */
      if (sw.swe_calc(tjd - dt, ipl, iflagp | SweConst.SEFLG_XYZ, xx2, serr) ==
                                                                 SweConst.ERR) {
        return SweConst.ERR;
      }
      if (sw.swe_calc(tjd - dt, ipl, iflagp, lbr2, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      /*
       * phase angle
       */
      attr[0] = Math.acos(sl.swi_dot_prod_unit(xx, xx2)) * SwissData.RADTODEG;
      /*
       * phase
       */
      attr[1] = (1 + Math.cos(attr[0] * SwissData.DEGTORAD)) / 2;
    }
    /*
     * apparent diameter of disk
     */
    if (ipl < SwephData.NDIAM) {
      dd = SwephData.pla_diam[ipl];
    } else if (ipl > SweConst.SE_AST_OFFSET) {
      dd = swed.ast_diam * 1000;        /* km -> m */
    } else {
      dd = 0;
    }
    if (lbr[2] < dd / 2 / SweConst.AUNIT) {
      attr[3] = 180;  /* assume position on surface of earth */
    } else {
      attr[3] = Math.asin(dd / 2 / SweConst.AUNIT / lbr[2]) * 2 * SwissData.RADTODEG;
    }
    /*
     * apparent magnitude
     */
    if (ipl > SweConst.SE_AST_OFFSET ||
        (ipl < NMAG_ELEM && mag_elem[ipl][0] < 99)) {
      if (ipl == SweConst.SE_SUN) {
        /* ratio apparent diameter : average diameter */
        fac = attr[3] / (Math.asin(SwephData.pla_diam[SweConst.SE_SUN] / 2.0 /
                                            SweConst.AUNIT) * 2 * SwissData.RADTODEG);
        fac *= fac;
        attr[4] = mag_elem[ipl][0] - 2.5 * log10(fac);
      } else if (ipl == SweConst.SE_MOON) {
        /* ratio apparent diameter : average diameter */
        fac = attr[3] / (Math.asin(SwephData.pla_diam[SweConst.SE_MOON] / 2.0 /
                                            384400000.0) * 2 * SwissData.RADTODEG);
        /* distance sun - moon */
        for (i = 0; i < 3; i++)
          xxs[i] -= xx[i];
        dsm = Math.sqrt(sl.square_sum(xxs));
        /* account for phase and distance of moon: */
        fac *= fac * attr[1];
        /* account for distance of sun from moon: */
        fac *= dsm * dsm;
        attr[4] = mag_elem[ipl][0] - 2.5 * log10(fac);
      } else if (ipl == SweConst.SE_SATURN) {
        /* rings are considered according to Meeus, German, p. 329ff. */
        T = (tjd - dt - SwephData.J2000) / 36525.0;
        in = (28.075216 - 0.012998 * T + 0.000004 * T * T) * SwissData.DEGTORAD;
        om = (169.508470 + 1.394681 * T + 0.000412 * T * T) * SwissData.DEGTORAD;
        sinB = Math.abs(Math.sin(in) * Math.cos(lbr[1] * SwissData.DEGTORAD)
                      * Math.sin(lbr[0] * SwissData.DEGTORAD - om)
                      - Math.cos(in) * Math.sin(lbr[1] * SwissData.DEGTORAD));
        u1 = Math.atan2(Math.sin(in) * Math.tan(lbr2[1] * SwissData.DEGTORAD)
                               + Math.cos(in) * Math.sin(lbr2[0] *
                                                          SwissData.DEGTORAD - om),
                          Math.cos(lbr2[0] * SwissData.DEGTORAD - om)) *
                                                                 SwissData.RADTODEG;
        u2 = Math.atan2(Math.sin(in) * Math.tan(lbr[1] * SwissData.DEGTORAD)
                        + Math.cos(in) * Math.sin(lbr[0] * SwissData.DEGTORAD - om),
                          Math.cos(lbr[0] * SwissData.DEGTORAD - om)) *
                                                                 SwissData.RADTODEG;
        du = sl.swe_degnorm(u1 - u2);
        if (du > 10) {
          du = 360 - du;
        }
        attr[4] = 5 * log10(lbr2[2] * lbr[2])
                    + mag_elem[ipl][1] * sinB
                    + mag_elem[ipl][2] * sinB * sinB
                    + mag_elem[ipl][3] * du
                    + mag_elem[ipl][0];
      } else if (ipl < SweConst.SE_CHIRON) {
        attr[4] = 5 * log10(lbr2[2] * lbr[2])
                    + mag_elem[ipl][1] * attr[0] /100.0
                    + mag_elem[ipl][2] * attr[0] * attr[0] / 10000.0
                    + mag_elem[ipl][3] * attr[0] * attr[0] * attr[0] / 1000000.0
                    + mag_elem[ipl][0];
      } else if (ipl < NMAG_ELEM || ipl > SweConst.SE_AST_OFFSET) {/*asteroids*/
        ph1 = Math.pow(EULER, -3.33 *
                        Math.pow(Math.tan(attr[0] * SwissData.DEGTORAD / 2), 0.63));
        ph2 = Math.pow(EULER, -1.87 *
                        Math.pow(Math.tan(attr[0] * SwissData.DEGTORAD / 2), 1.22));
        if (ipl < NMAG_ELEM) {    /* main asteroids */
          me[0] = mag_elem[ipl][0];
          me[1] = mag_elem[ipl][1];
        } else if (ipl == SweConst.SE_AST_OFFSET + 1566) {
                    /* Icarus has elements from JPL database */
                  me[0] = 16.9;
                  me[1] = 0.15;
        } else {      /* other asteroids */
          me[0] = swed.ast_H;
          me[1] = swed.ast_G;
        }
        attr[4] = 5 * log10(lbr2[2] * lbr[2])
            + me[0]
            - 2.5 * log10((1 - me[1]) * ph1 + me[1] * ph2);
      } else { /* ficticious bodies */
        attr[4] = 0;
      }
    }
    if (ipl != SweConst.SE_SUN && ipl != SweConst.SE_EARTH) {
      /*
       * elongation of planet
       */
      if (sw.swe_calc(tjd, SweConst.SE_SUN, iflag | SweConst.SEFLG_XYZ,
                      xx2, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      if (sw.swe_calc(tjd, SweConst.SE_SUN, iflag, lbr2, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      attr[2] = Math.acos(sl.swi_dot_prod_unit(xx, xx2)) * SwissData.RADTODEG;
    }
    return SweConst.OK;
  }

  /**
  * Computes phase, phase angel, elongation, apparent diameter and apparent
  * magnitude for sun, moon, all planets and asteroids.
  * @param tjd_ut The Julian Day number in UT (Universal Time).
  * @param ipl The body number to be calculated. See class
  * <A HREF="SweConst.html">SweConst</A> for a list of bodies
  * <P>attr is an output parameter with the following meaning:
  * <P><CODE>
  * attr[0]:&nbsp;&nbsp;&nbsp;phase angle (earth-planet-sun).<BR>
  * attr[1]:&nbsp;&nbsp;&nbsp;phase (illumined fraction of disc).<BR>
  * attr[2]:&nbsp;&nbsp;&nbsp;elongation of planet.<BR>
  * attr[3]:&nbsp;&nbsp;&nbsp;apparent diameter of disc.<BR>
  * attr[4]:&nbsp;&nbsp;&nbsp;apparent magnitude.<BR>
  * </CODE><P><B>Attention: attr must be a double[20]!</B>
  * @param iflag Which ephemeris is to be used (SEFLG_JPLEPH, SEFLG_SWIEPH,
  * SEFLG_MOSEPH). Also allowable flags: SEFLG_TRUEPOS, SEFLG_HELCTR.
  * @param attr A double[20] in which the result is returned. See above for more
  * details.
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails.
  * @return SweConst.OK (0) or SweConst.ERR (-1)
  * @see swisseph.SweConst#OK
  * @see swisseph.SweConst#ERR
  * @see swisseph.SweConst#SEFLG_JPLEPH
  * @see swisseph.SweConst#SEFLG_SWIEPH
  * @see swisseph.SweConst#SEFLG_MOSEPH
  * @see swisseph.SweConst#SEFLG_TRUEPOS
  * @see swisseph.SweConst#SEFLG_HELCTR
  */
  int swe_pheno_ut(double tjd_ut, int ipl, int iflag, double[] attr,
                   StringBuffer serr) {
    return swe_pheno(tjd_ut + SweDate.getDeltaT(tjd_ut), ipl, iflag, attr, serr);
  }

  private int find_maximum(double y00, double y11, double y2, double dx,
                           DblObj dxret, DblObj yret) {
    double a, b, c, x, y;
    c = y11;
    b = (y2 - y00) / 2.0;
    a = (y2 + y00) / 2.0 - c;
    x = -b / 2 / a;
    y = (4 * a * c - b * b) / 4 / a;
    dxret.val = (x - 1) * dx;
    if (yret != null) {
      yret.val = y;
    }
    return SweConst.OK;
  }

  private int find_zero(double y00, double y11, double y2, double dx,
                        DblObj dxret, DblObj dxret2) {
    double a, b, c, x1, x2;
    c = y11;
    b = (y2 - y00) / 2.0;
    a = (y2 + y00) / 2.0 - c;
    if (b * b - 4 * a * c < 0) {
      return SweConst.ERR;
    }
    x1 = (-b + Math.sqrt(b * b - 4 * a * c)) / 2 / a;
    x2 = (-b - Math.sqrt(b * b - 4 * a * c)) / 2 / a;
      dxret.val = (x1 - 1) * dx;
      dxret2.val = (x2 - 1) * dx;
    return SweConst.OK;
  }

  /* rise, set, and meridian transits of sun, moon, planets, and stars
   *
   * tjd_ut       universal time from when on search ought to start
   * ipl          planet number, neglected, if Starname is given
   * starname     pointer to string. if a planet, not a star, is
   *              wanted, starname must be null or ""
   * epheflag     used for ephemeris only
   * rsmi         SE_CALC_RISE, SE_CALC_SET, SE_CALC_MTRANSIT, SE_CALC_ITRANSIT
   *              | SE_BIT_DISC_CENTER      for rises of disc center of body
   *              | SE_BIT_NO_REFRACTION    to neglect refraction
   * geopos       array of doubles for geogr. long., lat. and height above sea
   * atpress      atmospheric pressure
   * attemp       atmospheric temperature
   *
   * return variables:
   * tret         time of rise, set, meridian transits
   * serr[256]        error string
   * function return value -2 means that the body does not rise or set */
  /**
  * Calculates the times of rising, setting and meridian transits for all
  * planets, asteroids, the moon, and the fixed stars.
  * @param tjd_ut The Julian Day number in UT, from when to start searching
  * @param ipl Planet number, if times for planet or moon are to be calculated.
  * @param starname The name of the star, if times for a star should be
  * calculated. It has to be null or the empty string otherwise!
  * @param ifl To indicate, which ephemeris should be used (SEFLG_JPLEPH,
  * SEFLG_SWIEPH or SEFLG_MOSEPH)
  * @param rsmi Specification, what type of calculation is wanted
  * (SE_CALC_RISE, SE_CALC_SET, SE_CALC_MTRANSIT, SE_CALC_ITRANSIT) plus
  * optionally SE_BIT_DISC_CENTER, when the rise time of the disc center
  * of the body is requested and or SE_BIT_NO_REFRACTION for calculation
  * without refraction effects). The calculation method defaults to
  * SE_CALC_RISE.
  * @param geopos An array double[3] containing the longitude, latitude and
  * height of the observer
  * @param atpress atmospheric pressure in mBar (hPa). If it is 0, the pressure
  * will be estimated from geopos[2] and attemp (1013.25 mbar for sea level).
  * When calculating MTRANSIT or ITRANSIT, this parameter is not used.
  * @param attemp atmospheric temperature in degrees Celsius. When
  * calculating MTRANSIT or ITRANSIT, this parameter is not used.
  * @param tret Return value containing the time of rise or whatever was
  * requested
  * @param serr A StringBuffer containing a warning or error message, if
  * something fails 
  * @return SweConst.OK (0) or SweConst.ERR (-1)
  * @see swisseph.SweConst#OK
  * @see swisseph.SweConst#ERR
  * @see swisseph.SweConst#SEFLG_JPLEPH
  * @see swisseph.SweConst#SEFLG_SWIEPH
  * @see swisseph.SweConst#SEFLG_MOSEPH
  * @see swisseph.SweConst#SE_CALC_RISE
  * @see swisseph.SweConst#SE_CALC_SET
  * @see swisseph.SweConst#SE_CALC_MTRANSIT
  * @see swisseph.SweConst#SE_CALC_ITRANSIT
  * @see swisseph.SweConst#SE_BIT_DISC_CENTER
  * @see swisseph.SweConst#SE_BIT_NO_REFRACTION
  * @see swisseph.DblObj
  */
  int swe_rise_trans(double tjd_ut, int ipl, StringBuffer starname,
                     int epheflag, int rsmi, double[] geopos,
                     double atpress, double attemp,
                     DblObj tret, StringBuffer serr) {
    int i, j, k, ii, calc_culm, nculm = -1;
    double tjd_et = tjd_ut + SweDate.getDeltaT(tjd_ut);
    double xc[]=new double[6], xh[][]=new double[20][6], ah[]=new double[6],
           aha;
    double tculm[]=new double[4], tcu, tc[]=new double[20], h[]=new double[20],
           t2[]=new double[6], dc[]=new double[6];
    DblObj dtint=new DblObj();
    DblObj dx=new DblObj();
    double rdi, dd = 0;
    int iflag = epheflag;
    int jmax = 14;
    double t, te, tt, dt, twohrs = 1.0 / 12.0;
    boolean do_fixstar = (starname != null && starname.length() > 0);
    // xh[0][0] = 0; /* to shut up mint */
    iflag &= SweConst.SEFLG_EPHMASK;
    tret.val = 0;
    iflag |= (SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_TOPOCTR);
    sw.swe_set_topo(geopos[0], geopos[1], geopos[2]);
    if ((rsmi & (SweConst.SE_CALC_MTRANSIT | SweConst.SE_CALC_ITRANSIT))!=0) {
      return calc_mer_trans(tjd_ut, ipl, epheflag, rsmi,
                  geopos, starname,
                  tret, serr);
    }
    if ((rsmi & ( SweConst.SE_CALC_RISE | SweConst.SE_CALC_SET))==0) {
      rsmi |= SweConst.SE_CALC_RISE;
    }
    /* find culmination points within 28 hours from t0 - twohrs.
     * culminations are required in case there are maxima or minima
     * in height slightly above or below the horizon.
     * we do not use meridian transits, because in polar regions
     * the culmination points may considerably deviate from
     * transits. also, there are cases where the moon rises in the
     * western half of the sky for a short time.
     */
    if (do_fixstar) {
      if (sw.swe_fixstar(starname, tjd_et, iflag, xc, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
    }
    for (ii = 0, t = tjd_ut - twohrs; ii <= jmax; ii++, t += twohrs) {
      tc[ii] = t;
      if (!do_fixstar) {
        te = t + SweDate.getDeltaT(t);
        if (sw.swe_calc(te, ipl, iflag, xc, serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
      }
      /* diameter of object in km */
      if (ii == 0) {
        if (do_fixstar) {
          dd = 0;
        } else
               if ((rsmi & SweConst.SE_BIT_DISC_CENTER)!=0) {
          dd = 0;
        } else if (ipl < SwephData.NDIAM) {
          dd = SwephData.pla_diam[ipl];
        } else if (ipl > SweConst.SE_AST_OFFSET) {
          dd = swed.ast_diam * 1000;        /* km -> m */
        } else {
          dd = 0;
        }
      }
      /* apparent radius of disc */
      rdi = Math.asin(dd / 2 / SweConst.AUNIT / xc[2]) * SwissData.RADTODEG;
      /* true height of center of body */
      swe_azalt(t, SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, xh[ii]);
      /* true height of uppermost point of body */
      xh[ii][1] += rdi;
      /* apparent height of uppermost point of body */
      if ((rsmi & SweConst.SE_BIT_NO_REFRACTION)!=0) {
        h[ii] = xh[ii][1];
      } else {
        swe_azalt_rev(t, SweConst.SE_HOR2EQU, geopos, xh[ii], xc);
        swe_azalt(t, SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, xh[ii]);
        h[ii] = xh[ii][2];
      }
      calc_culm = 0;
      if (ii > 1) {
        dc[0] = xh[ii-2][1];
        dc[1] = xh[ii-1][1];
        dc[2] = xh[ii][1];
        if (dc[1] > dc[0] && dc[1] > dc[2]) {
          calc_culm = 1;
        }
        if (dc[1] < dc[0] && dc[1] < dc[2]) {
          calc_culm = 2;
        }
      }
      if (calc_culm!=0) {
        dt = twohrs;
        tcu = t - dt;
        find_maximum(dc[0], dc[1], dc[2], dt, dtint, dx);
        tcu += dtint.val + dt;
        dt /= 3;
        for (; dt > 0.0001; dt /= 3) {
          for (i = 0, tt = tcu - dt; i < 3; tt += dt, i++) {
            te = tt + SweDate.getDeltaT(tt);
            if (!do_fixstar) {
              if (sw.swe_calc(te, ipl, iflag, xc, serr) == SweConst.ERR) {
                return SweConst.ERR;
              }
            }
            swe_azalt(tt, SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, ah);
            dc[i] = ah[1];
          }
          find_maximum(dc[0], dc[1], dc[2], dt, dtint, dx);
          tcu += dtint.val + dt;
        }
        nculm++;
        tculm[nculm] = tcu;
      }
    }
    /* note: there can be a rise or set on the poles, even if
     * there is no culmination. So, we must not leave here
     * in any case. */
    /* insert culminations into array of heights */
    for (i = 0; i <= nculm; i++) {
      for (j = 1; j <= jmax; j++) {
        if (tculm[i] < tc[j]) {
          for (k = jmax; k >= j; k--) {
            tc[k+1] = tc[k];
            h[k+1] = h[k];
          }
          tc[j] = tculm[i];
          if (!do_fixstar) {
            te = tc[j] + SweDate.getDeltaT(tc[j]);
            if (sw.swe_calc(te, ipl, iflag, xc, serr) == SweConst.ERR) {
              return SweConst.ERR;
            }
          }
          /* apparent radius of disc */
          rdi = Math.asin(dd / 2 / SweConst.AUNIT / xc[2]) * SwissData.RADTODEG;
          /* true height of center of body */
          swe_azalt(tc[j], SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, ah);
          /* true height of uppermost point of body */
          ah[1] += rdi;
          /* apparent height of uppermost point of body */
          if ((rsmi & SweConst.SE_BIT_NO_REFRACTION)!=0) {
            h[j] = ah[1];
          } else {
            swe_azalt_rev(tc[j], SweConst.SE_HOR2EQU, geopos, ah, xc);
            swe_azalt(tc[j], SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, ah);
            h[j] = ah[2];
          }
          jmax++;
          break;
        }
      }
    }
    tret.val = 0;
    /* find points with zero height.
     * binary search */
    for (ii = 1; ii <= jmax; ii++) {
      if (h[ii-1] * h[ii] >= 0) {
        continue;
      }
      if (h[ii-1] < h[ii] && ((rsmi & SweConst.SE_CALC_RISE) == 0)) {
        continue;
      }
      if (h[ii-1] > h[ii] && ((rsmi & SweConst.SE_CALC_SET) == 0)) {
        continue;
      }
      dc[0] = h[ii-1];
      dc[1] = h[ii];
      t2[0] = tc[ii-1];
      t2[1] = tc[ii];
      for (i = 0; i < 20; i++) {
        t = (t2[0] + t2[1]) / 2;
        if (!do_fixstar) {
          te = t + SweDate.getDeltaT(t);
          if (sw.swe_calc(te, ipl, iflag, xc, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
        }
        /* apparent radius of disc */
        rdi = Math.asin(dd / 2 / SweConst.AUNIT / xc[2]) * SwissData.RADTODEG;
        /* true height of center of body */
        swe_azalt(t, SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, ah);
        /* true height of uppermost point of body */
        ah[1] += rdi;
        /* apparent height of uppermost point of body */
        if ((rsmi & SweConst.SE_BIT_NO_REFRACTION)!=0) {
          aha = ah[1];
        } else {
          swe_azalt_rev(t, SweConst.SE_HOR2EQU, geopos, ah, xc);
          swe_azalt(t, SweConst.SE_EQU2HOR, geopos, atpress, attemp, xc, ah);
          aha = ah[2];
        }
        if (aha * dc[0] <= 0) {
          dc[1] = aha;
          t2[1] = t;
        } else {
          dc[0] = aha;
          t2[0] = t;
        }
      }
      if (t > tjd_ut) {
       tret.val = t;
       return SweConst.OK;
      }
    }
    if (serr!=null)
      serr.append("rise or set not found for planet ").append(ipl);
    return -2; /* no t of rise or set found */
  }

  private int calc_mer_trans(double tjd_ut, int ipl, int epheflag, int rsmi,
                             double[] geopos, StringBuffer starname,
                             DblObj tret, StringBuffer serr) {
    int i;
    double tjd_et = tjd_ut + SweDate.getDeltaT(tjd_ut);
    double armc, armc0, arxc, x0[]=new double[6], x[]=new double[6], t, te;
    double mdd;
    int iflag = epheflag;
    boolean do_fixstar = (starname != null && starname.length() > 0);
    iflag &= SweConst.SEFLG_EPHMASK;
    tret.val = 0;
    iflag |= (SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_TOPOCTR);
    armc0 = sl.swe_sidtime(tjd_ut) + geopos[0] / 15;
    if (armc0 >= 24) {
      armc0 -= 24;
    }
    if (armc0 < 0) {
      armc0 += 24;
    }
    armc0 *= 15;
    if (do_fixstar) {
      if (sw.swe_fixstar(starname, tjd_et, iflag, x0, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
    } else {
      if (sw.swe_calc(tjd_et, ipl, iflag, x0, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
    }
    /*
     * meridian transits
     */
      x[0] = x0[0];
      x[1] = x0[1];
      t = tjd_ut;
      arxc = armc0;
      if ((rsmi & SweConst.SE_CALC_ITRANSIT)!=0) {
        arxc = sl.swe_degnorm(arxc + 180);
      }
      for (i = 0; i < 4; i++) {
        mdd = sl.swe_degnorm(x[0] - arxc);
        if (i > 0 && mdd > 180) {
          mdd -= 360;
        }
        t += mdd / 361;
        armc = sl.swe_sidtime(t) + geopos[0] / 15;
        if (armc >= 24) {
          armc -= 24;
        }
        if (armc < 0) {
          armc += 24;
        }
        armc *= 15;
        arxc = armc;
        if ((rsmi & SweConst.SE_CALC_ITRANSIT)!=0) {
          arxc = sl.swe_degnorm(arxc + 180);
        }
        if (!do_fixstar) {
  //        te = t + swe_deltat(t);
          te = t + SweDate.getDeltaT(t);
          if (sw.swe_calc(te, ipl, iflag, x, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
        }
      }
    tret.val = t;
    return SweConst.OK;
  }

  /*
  Nodes and apsides of planets and moon

  Planetary nodes can be defined in three different ways:
  a) They can be understood as a direction or as an axis
    defined by the intersection line of two orbital planes.
    E.g., the nodes of Mars are defined by the intersection
    line of Mars' orbital plane with the ecliptic (= the
    Earths orbit heliocentrically or the solar orbit
    geocentrically). However, as Michael Erlewine points
    out in his elaborate web page on this topic
    (http://thenewage.com/resources/articles/interface.html),
    planetary nodes can be defined for any couple of
    planets. E.g. there is also an intersection line for the
    two orbital planes of Mars and Saturn.
    Because such lines are, in principle, infinite, the
    heliocentric and the geocentric positions of the
    planetary nodes will be the same. There are astrologers
    that use such heliocentric planetary nodes in geocentric
    charts.
    The ascending and the descending node will, in this
    case, be in precise opposition.

  b) The planetary nodes can also be understood in a
    different way, not as an axis, but as the two points on a
    planetary orbit that are located precisely on the
    intersection line of the two planes.
    This second definition makes no difference for the moon or
    for heliocentric positions of planets, but it does so for
    geocentric positions. There are two possibilities for
    geocentric planetary nodes based on this definition.
    1) The common solution is that the points on the
      planets orbit are transformed to the geocenter. The
      two points will not be in opposition anymore, or
      they will only roughly be so with the outer planets. The
      advantage of these nodes is that when a planet is in
      conjunction with its node, then its ecliptic latitude
      will be zero. This is not true when a planet is in
      geocentric conjunction with its heliocentric node.
      (And neither is it always true for the inner planets,
      i.e. Mercury and Venus.)
    2) The second possibility that nobody seems to have
      thought of so far: One may compute the points of
      the earth's orbit that lie exactly on another planet's
      orbital plane and transform it to the geocenter. The two
      points will always be in an approximate square.

  c) Third, the planetary nodes could be defined as the
    intersection points of the plane defined by their
    momentary geocentric position and motion with the
    plane of the ecliptic. Such points would move very fast
    around the planetary stations. Here again, as in b)1),
    the planet would cross the ecliptic and its ecliptic
    latitude would be 0 exactly when it were in
    conjunction with one of its nodes.

  The Swiss Ephemeris supports the solutions a) and b) 1).

  Possible definitions for apsides

  a) The planetary apsides can be defined as the perihelion and
    aphelion points on a planetary orbit. For a
    geocentric chart, these points could be transformed
    from the heliocenter to the geocenter.
  b) However, one might consider these points as
    astrologically relevant axes rather than as points on a
    planetary orbit. Again, this would allow heliocentric
    positions in a geocentric chart.

  Note: For the "Dark Moon" or "Lilith", which I usually
  define as the lunar apogee, some astrologers give a
  different definition. They understand it as the second focal
  point of the moon's orbital ellipse. This definition does not
  make a difference for geocentric positions, because the
  apogee and the second focus are in exactly the same geocentric
  direction. However, it makes a difference with topocentric
  positions, because the two points do not have same distance.
  Analogous "black planets" have been proposed: they would be the
  second focal points of the planets' orbital ellipses. The
  heliocentric positions of these "black planets" are identical
  with the heliocentric positions of the aphelia, but geocentric
  positions are not identical, because the focal points are
  much closer to the sun than the aphelia.

  The Swiss Ephemeris allows to compute the "black planets" as well.

  Mean positions

  Mean nodes and apsides can be computed for the Moon, the
  Earth and the planets Mercury - Neptune. They are taken
  from the planetary theory VSOP87. Mean points can not be
  calculated for Pluto and the asteroids, because there is no
  planetary theory for them.

  Osculating nodes and apsides

  Nodes and apsides can also be derived from the osculating
  orbital elements of a body, the paramaters that define an
  ideal unperturbed elliptic (two-body) orbit.
  For astrology, note that this is a simplification and
  idealization.
  Problem with Neptune: Neptune's orbit around the sun does not
  have much in common with an ellipse. There are often two
  perihelia and two aphelia within one revolution. As a result,
  there is a wild oscillation of the osculating perihelion (and
  aphelion).
  In actuality, Neptune's orbit is not heliocentric orbit at all.
  The twofold perihelia and aphelia are an effect of the motion of
  the sun about the solar system barycenter. This motion is
  much faster than the motion of Neptune, and Neptune
  cannot react on such fast displacements of the Sun. As a
  result, Neptune seems to move around the barycenter (or a
  mean sun) rather than around the true sun. In fact,
  Neptune's orbit around the barycenter is therefore closer to
  an ellipse than the his orbit around the sun. The same
  statement is also true for Saturn, Uranus and Pluto, but not
  for Jupiter and the inner planets.

  This fundamental problem about osculating ellipses of
  planetary orbits does of course not only affect the apsides
  but also the nodes.

  Two solutions can be thought of for this problem:
  1) The one would be to interpolate between actual
    passages of the planets through their nodes and
    apsides. However, this works only well with Mercury.
    With all other planets, the supporting points are too far
    apart as to make an accurate interpolation possible.
    This solution is not implemented, here.
  2) The other solution is to compute the apsides of the
    orbit around the barycenter rather than around the sun.
    This procedure makes sense for planets beyond Jupiter,
    it comes closer to the mean apsides and nodes for
    planets that have such points defined. For all other
    transsaturnian planets and asteroids, this solution yields
    a kind of "mean" nodes and apsides. On the other hand,
    the barycentric ellipse does not make any sense for
    inner planets and Jupiter.

  The Swiss Ephemeris supports solution 2) for planets and
  asteroids beyond Jupiter.

  Anyway, neither the heliocentric nor the barycentric ellipse
  is a perfect representation of the nature of a planetary orbit,
  and it will not yield the degree of precision that today's
  astrology is used to.
  The best choice of method will probably be:
  - For Mercury - Neptune: mean nodes and apsides
  - For asteroids that belong to the inner asteroid belt:
  osculating nodes/apsides from a heliocentric ellipse
  - For Pluto and outer asteroids: osculating nodes/apsides
  from a barycentric ellipse

  The Moon is a special case: A "lunar true node" makes
  more sense, because it can be defined without the idea of an
  ellipse, e.g. as the intersection axis of the momentary lunar
  orbital plane with the ecliptic. Or it can be said that the
  momentary motion of the moon points to one of the two
  ecliptic points that are called the "true nodes".  So, these
  points make sense. With planetary nodes, the situation is
  somewhat different, at least if we make a difference
  between heliocentric and geocentric positions. If so, the
  planetary nodes are points on a heliocentric orbital ellipse,
  which are transformed to the geocenter. An ellipse is
  required here, because a solar distance is required. In
  contrast to the planetary nodes, the lunar node does not
  require a distance, therefore manages without the idea of an
  ellipse and does not share its weaknesses.
  On the other hand, the lunar apsides DO require the idea of
  an ellipse. And because the lunar ellipse is actually
  extremely distorted, even more than any other celestial
  ellipse, the "true Lilith" (apogee), for which printed
  ephemerides are available, does not make any sense at all.
  (See the chapter on the lunar node and apogee.)

  Special case: the Earth

  The Earth is another special case. Instead of the motion of
  the Earth herself, the heliocentric motion of the Earth-
  Moon-Barycenter (EMB) is used to determine the
  osculating perihelion.
  There is no node of the earth orbit itself. However, there is
  an axis around which the earth's orbital plane slowly rotates
  due to planetary precession. The position points of this axis
  are not calculated by the Swiss Ephemeris.

  Special case: the Sun

  In addition to the Earth (EMB) apsides, the function
  computes so-to-say "apsides" of the sun, i.e. points on the
  orbit of the Sun where it is closest to and where it is farthest
  from the Earth. These points form an opposition and are
  used by some astrologers, e.g. by the Dutch astrologer
  George Bode or the Swiss astrologer Liduina Schmed. The
  perigee, located at about 13 Capricorn, is called the
  "Black Sun", the other one, in Cancer, the "Diamond".
  So, for a complete set of apsides, one ought to calculate
  them for the Sun and the Earth and all other planets.

  The modes of the Swiss Ephemeris function
  swe_nod_aps()

  The  function swe_nod_aps() can be run in the following
  modes:
  1) Mean positions are given for nodes and apsides of Sun,
    Moon, Earth, and the up to Neptune. Osculating
    positions are given with Pluto and all asteroids. This is
    the default mode.
  2) Osculating positions are returned for nodes and apsides
    of all planets.
  3) Same as 2), but for planets and asteroids beyond
    Jupiter, a barycentric ellipse is used.
  4) Same as 1), but for Pluto and asteroids beyond Jupiter,
    a barycentric ellipse is used.

  In all of these modes, the second focal point of the ellipse
  can be computed instead of the aphelion.
  Like the planetary function swe_calc(), swe_nod_aps() is
  able to return geocentric, topocentric, heliocentric, or
  barycentric position.
   *
   * tjd_ut         julian day, ephemeris time
   * ipl                 planet number
   * iflag         as usual, SEFLG_HELCTR, etc.
   * xnasc         an array of 6 doubles: ascending node
   * xndsc         an array of 6 doubles: ascending node
   * xperi         an array of 6 doubles: perihelion
   * xaphe         an array of 6 doubles: aphelion
   * method        see below
   * serr          error message
   *
   * method        can have the following values:
   *               - 0 or SE_NODBIT_MEAN. MEAN positions are given for
   *                 nodes and apsides of Sun, Moon, Earth, and the
   *                 planets up to Neptune. Osculating positions are
   *                 given with Pluto and all asteroids.
   *               - SE_NODBIT_OSCU. Osculating positions are given
   *                 for all nodes and apsides.
   *               - SE_NODBIT_OSCU_BAR. Osculating nodes and apsides
   *                 are computed from barycentric ellipses, for planets
   *                 beyond Jupiter, but from heliocentric ones for
   *                 ones for Jupiter and inner planets.
   *               - SE_NODBIT_MEAN and SE_NODBIT_OSCU_BAR can be combined.
   *                 The program behaves the same way as with simple
   *                 SE_NODBIT_MEAN, but uses barycentric ellipses for
   *                 planets beyond Neptune and asteroids beyond Jupiter.
   *               - SE_NODBIT_FOCAL can be combined with any of the other
   *                 bits. The second focal points of the ellipses will
   *                 be returned instead of the aphelia.
   */
  /* mean elements for Mercury - Neptune from VSOP87 (mean equinox of date) */
  private static final double el_node[][] = new double[][]
    {{ 48.330893,  1.1861890,  0.00017587,  0.000000211,}, /* Mercury */
    { 76.679920,  0.9011190,  0.00040665, -0.000000080,}, /* Venus   */
    {  0       ,  0        ,  0         ,  0          ,}, /* Earth   */
    { 49.558093,  0.7720923,  0.00001605,  0.000002325,}, /* Mars    */
    {100.464441,  1.0209550,  0.00040117,  0.000000569,}, /* Jupiter */
    {113.665524,  0.8770970, -0.00012067, -0.000002380,}, /* Saturn  */
    { 74.005947,  0.5211258,  0.00133982,  0.000018516,}, /* Uranus  */
    {131.784057,  1.1022057,  0.00026006, -0.000000636,}, /* Neptune */
    };
  private static final double el_peri[][] = new double[][]
    {{ 77.456119,  1.5564775,  0.00029589,  0.000000056,}, /* Mercury */
    {131.563707,  1.4022188, -0.00107337, -0.000005315,}, /* Venus   */
    {102.937348,  1.7195269,  0.00045962,  0.000000499,}, /* Earth   */
    {336.060234,  1.8410331,  0.00013515,  0.000000318,}, /* Mars    */
    { 14.331309,  1.6126668,  0.00103127, -0.000004569,}, /* Jupiter */
    { 93.056787,  1.9637694,  0.00083757,  0.000004899,}, /* Saturn  */
    {173.005159,  1.4863784,  0.00021450,  0.000000433,}, /* Uranus  */
    { 48.123691,  1.4262677,  0.00037918, -0.000000003,}, /* Neptune */
    };
  private static final double el_incl[][] = new double[][]
    {{  7.004986,  0.0018215, -0.00001809,  0.000000053,}, /* Mercury */
    {  3.394662,  0.0010037, -0.00000088, -0.000000007,}, /* Venus   */
    {  0,         0,          0,           0          ,}, /* Earth   */
    {  1.849726, -0.0006010,  0.00001276, -0.000000006,}, /* Mars    */
    {  1.303270, -0.0054966,  0.00000465, -0.000000004,}, /* Jupiter */
    {  2.488878, -0.0037363, -0.00001516,  0.000000089,}, /* Saturn  */
    {  0.773196,  0.0007744,  0.00003749, -0.000000092,}, /* Uranus  */
    {  1.769952, -0.0093082, -0.00000708,  0.000000028,}, /* Neptune */
    };
  private static final double el_ecce[][] = new double[][]
    {{  0.20563175,  0.000020406, -0.0000000284, -0.00000000017,}, /* Mercury */
    {  0.00677188, -0.000047766,  0.0000000975,  0.00000000044,}, /* Venus   */
    {  0.01670862, -0.000042037, -0.0000001236,  0.00000000004,}, /* Earth   */
    {  0.09340062,  0.000090483, -0.0000000806, -0.00000000035,}, /* Mars    */
    {  0.04849485,  0.000163244, -0.0000004719, -0.00000000197,}, /* Jupiter */
    {  0.05550862, -0.000346818, -0.0000006456,  0.00000000338,}, /* Saturn  */
    {  0.04629590, -0.000027337,  0.0000000790,  0.00000000025,}, /* Uranus  */
    {  0.00898809,  0.000006408, -0.0000000008, -0.00000000005,}, /* Neptune */
    };
  private static final double el_sema[][] = new double[][]
    {{  0.387098310,  0.0,  0.0,  0.0,}, /* Mercury */
    {  0.723329820,  0.0,  0.0,  0.0,}, /* Venus   */
    {  1.000001018,  0.0,  0.0,  0.0,}, /* Earth   */
    {  1.523679342,  0.0,  0.0,  0.0,}, /* Mars    */
    {  5.202603191,  0.0000001913,  0.0,  0.0,}, /* Jupiter */
    {  9.554909596,  0.0000021389,  0.0,  0.0,}, /* Saturn  */
    { 19.218446062, -0.0000000372,  0.00000000098,  0.0,}, /* Uranus  */
    { 30.110386869, -0.0000001663,  0.00000000069,  0.0,}, /* Neptune */
    };
  /* Ratios of mass of Sun to masses of the planets */
  private static final double plmass[] = new double[] {
      6023600,        /* Mercury */
       408523.5,      /* Venus */
       328900.5,      /* Earth and Moon */
      3098710,        /* Mars */
         1047.350,    /* Jupiter */
         3498.0,      /* Saturn */
        22960,        /* Uranus */
        19314,        /* Neptune */
    130000000,        /* Pluto */
  };
  private static final int ipl_to_elem[] = new int[]
                                {2, 0, 0, 1, 3, 4, 5, 6, 7, 0, 0, 0, 0, 0, 2,};

  /**
  * Computes planetary nodes and apsides (perihelia, aphelia, second focal
  * points of the orbital ellipses). This method is identical to
  * swe_nod_aps_ut() with the one exception that the time has to be given
  * in ET (Ephemeris Time or Dynamical Time). You would get ET by adding
  * deltaT to the UT, e.g.,
  * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
  * See <A HREF="Swecl.html#swe_nod_aps_ut(double, int, int, int, double[], double[], double[], double[], java.lang.StringBuffer)">swe_nod_aps_ut(...)</A> for missing information.
  */
  int swe_nod_aps(double tjd_et, int ipl, int iflag,
                  int  method,
                  double[] xnasc, double[] xndsc,
                  double[] xperi, double[] xaphe,
                  StringBuffer serr) {
    int ij, i, j;
    int iplx;
    int ipli;
    int istart, iend;
    int iflJ2000;
    double plm;
    double t = (tjd_et - SwephData.J2000) / 36525, dt;
    double x[]=new double[6], xx[]=new double[24], xp[],
           xobs[]=new double[6], x2000[]=new double[6];
    int xpOffs=0;
    double xpos[][]=new double[3][6], xnorm[]=new double[6];
    double xposm[]=new double[6];
    double xn[][]=new double[3][6], xs[][]=new double[3][6];
    double xq[][]=new double[3][6], xa[][]=new double[3][6];
    double xobs2[]=new double[6], x2[]=new double[6];
    double[] xna, xnd, xpe, xap;
    final int xndOffs = 6, xpeOffs = 12, xapOffs = 18;
    double incl, sema, ecce, parg, ea, vincl, vsema, vecce, pargx, eax;
    PlanData pedp = swed.pldat[SwephData.SEI_EARTH];
    PlanData psbdp = swed.pldat[SwephData.SEI_SUNBARY];
    PlanData pldat=new PlanData();
    double[] xsun = psbdp.x;
    double[] xear = pedp.x;
    double[] ep;
    double Gmsm, dzmin;
    double rxy, rxyz, fac, sgn;
    double sinnode, cosnode, sinincl, cosincl, sinu, cosu, sinE, cosE, cosE2;
    double uu, ny, ny2, c2, v2, pp, ro, ro2, rn, rn2;
    Epsilon oe;
    boolean is_true_nodaps = false;
    boolean do_aberr = (iflag &
                        (SweConst.SEFLG_TRUEPOS | SweConst.SEFLG_NOABERR))==0;
    boolean do_defl = (iflag & SweConst.SEFLG_TRUEPOS)==0 &&
                      (iflag & SweConst.SEFLG_NOGDEFL)==0;
    boolean do_focal_point = (method & SweConst.SE_NODBIT_FOPOINT) != 0;
    boolean ellipse_is_bary = false;
    int iflg0;
    xna = xx; 
    xnd = xx; // xndOffs = 6; 
    xpe = xx; // xpeOffs = 12; 
    xap = xx; // xapOffs = 18;
    // xpos[0][0] = 0; /* to shut up mint */
    /* to get control over the save area: */
    sw.swi_force_app_pos_etc();
    method %= SweConst.SE_NODBIT_FOPOINT;
    ipli = ipl;
    if (ipl == SweConst.SE_SUN) {
      ipli = SweConst.SE_EARTH;
    }
    if (ipl == SweConst.SE_MOON) {
      do_defl = false;
      if ((iflag & SweConst.SEFLG_HELCTR)==0) {
        do_aberr = false;
      }
    }
    iflg0 = (iflag & (SweConst.SEFLG_EPHMASK|SweConst.SEFLG_NONUT)) |
            SweConst.SEFLG_SPEED | SweConst.SEFLG_TRUEPOS;
    if (ipli != SweConst.SE_MOON) {
      iflg0 |= SweConst.SEFLG_HELCTR;
    }
    if (ipl == SweConst.SE_MEAN_NODE || ipl == SweConst.SE_TRUE_NODE ||
            ipl == SweConst.SE_MEAN_APOG || ipl == SweConst.SE_OSCU_APOG ||
            ipl < 0 ||
            (ipl >= SweConst.SE_NPLANETS && ipl <= SweConst.SE_AST_OFFSET)) {
           /*(ipl >= SE_FICT_OFFSET && ipl - SE_FICT_OFFSET < SE_NFICT_ELEM)) */
      if (serr != null) {
        serr.setLength(0);
        serr.append("nodes/apsides for planet "+ipl+
                    " are not implemented");
      }
      if (xnasc != null) {
        for (i = 0; i <= 5; i++)
          xnasc[i] = 0;
      }
      if (xndsc != null) {
        for (i = 0; i <= 5; i++)
          xndsc[i] = 0;
      }
      if (xaphe != null) {
        for (i = 0; i <= 5; i++)
          xaphe[i] = 0;
      }
      if (xperi != null) {
        for (i = 0; i <= 5; i++)
          xperi[i] = 0;
      }
      return SweConst.ERR;
    }
    for (i = 0; i < 24; i++)
      xx[i] = 0;
    /***************************************
     * mean nodes and apsides
     ***************************************/
    /* mean points only for Sun - Neptune */
    if ((method == 0 || (method & SweConst.SE_NODBIT_MEAN)!=0) &&
          ((ipl >= SweConst.SE_SUN && ipl <= SweConst.SE_NEPTUNE) ||
                                                    ipl == SweConst.SE_EARTH)) {
      if (ipl == SweConst.SE_MOON) {
//      sm.swi_mean_lunar_elements(tjd_et, &xna[0], &xna[3], &xpe[0], &xpe[3]);
        DblObj xna0=new DblObj(); xna0.val=xna[0];
        DblObj xna3=new DblObj(); xna3.val=xna[3];
        DblObj xpe0=new DblObj(); xpe0.val=xpe[0+xpeOffs];
        DblObj xpe3=new DblObj(); xpe3.val=xpe[3+xpeOffs];
        sm.swi_mean_lunar_elements(tjd_et, xna0, xna3, xpe0, xpe3);
        xna[0]=xna0.val;
        xna[3]=xna3.val;
        xpe[0+xpeOffs]=xpe0.val;
        xpe[3+xpeOffs]=xpe3.val;
        incl = SwephData.MOON_MEAN_INCL;
        vincl = 0;
        ecce = SwephData.MOON_MEAN_ECC;
        vecce = 0;
        sema = SwephData.MOON_MEAN_DIST / SweConst.AUNIT;
        vsema = 0;
      } else {
        iplx = ipl_to_elem[ipl];
        ep = el_incl[iplx];
        incl = ep[0] + ep[1] * t + ep[2] * t * t + ep[3] * t * t * t;
        vincl = ep[1] / 36525;
        ep = el_sema[iplx];
        sema = ep[0] + ep[1] * t + ep[2] * t * t + ep[3] * t * t * t;
        vsema = ep[1] / 36525;
        ep = el_ecce[iplx];
        ecce = ep[0] + ep[1] * t + ep[2] * t * t + ep[3] * t * t * t;
        vecce = ep[1] / 36525;
        ep = el_node[iplx];
        /* ascending node */
        xna[0] = ep[0] + ep[1] * t + ep[2] * t * t + ep[3] * t * t * t;
        xna[3] = ep[1] / 36525;
        /* perihelion */
        ep = el_peri[iplx];
        xpe[0+xpeOffs] = ep[0] + ep[1] * t + ep[2] * t * t + ep[3] * t * t * t;
        xpe[3+xpeOffs] = ep[1] / 36525;
      }
      /* descending node */
      xnd[0+xndOffs] = sl.swe_degnorm(xna[0] + 180);
      xnd[3+xndOffs] = xna[3];
      /* angular distance of perihelion from node */
      parg = xpe[0+xpeOffs] = sl.swe_degnorm(xpe[0+xpeOffs] - xna[0]);
      pargx = xpe[3+xpeOffs] = sl.swe_degnorm(xpe[0+xpeOffs] + xpe[3+xpeOffs]  - xna[3]);
      /* transform from orbital plane to mean ecliptic of date */
      sl.swe_cotrans(xpe, xpeOffs, xpe, xpeOffs, -incl);
      /* xpe+3 is aux. position, not speed!!! */
      sl.swe_cotrans(xpe, 3+xpeOffs, xpe, 3+xpeOffs, -incl-vincl);
      /* add node again */
      xpe[0+xpeOffs] = sl.swe_degnorm(xpe[0+xpeOffs] + xna[0]);
      /* xpe+3 is aux. position, not speed!!! */
      xpe[3+xpeOffs] = sl.swe_degnorm(xpe[3+xpeOffs] + xna[0] + xna[3]);
      /* speed */
      xpe[3+xpeOffs] = sl.swe_degnorm(xpe[3+xpeOffs] - xpe[0+xpeOffs]);
      /* heliocentric distance of perihelion and aphelion */
      xpe[2+xpeOffs] = sema * (1 - ecce);
      xpe[5+xpeOffs] = (sema + vsema) * (1 - ecce - vecce) - xpe[2+xpeOffs];
      /* aphelion */
      xap[0+xapOffs] = sl.swe_degnorm(xpe[xpeOffs] + 180);
      xap[1+xapOffs] = -xpe[1+xpeOffs];
      xap[3+xapOffs] = xpe[3+xpeOffs];
      xap[4+xapOffs] = -xpe[4+xpeOffs];
      if (do_focal_point) {
        xap[2+xapOffs] = sema * ecce * 2;
        xap[5+xapOffs] = (sema + vsema) * (ecce + vecce) * 2 - xap[2+xapOffs];
      } else {
        xap[2+xapOffs] = sema * (1 + ecce);
        xap[5+xapOffs] = (sema + vsema) * (1 + ecce + vecce) - xap[2+xapOffs];
      }
      /* heliocentric distance of nodes */
      ea = Math.atan(Math.tan(-parg * SwissData.DEGTORAD / 2) *
                                              Math.sqrt((1-ecce)/(1+ecce))) * 2;
      eax = Math.atan(Math.tan(-pargx * SwissData.DEGTORAD / 2) *
                                  Math.sqrt((1-ecce-vecce)/(1+ecce+vecce))) * 2;
      xna[2] = sema * (Math.cos(ea) - ecce) / Math.cos(parg * SwissData.DEGTORAD);
      xna[5] = (sema+vsema) * (Math.cos(eax) - ecce - vecce) /
                                                Math.cos(pargx * SwissData.DEGTORAD);
      xna[5] -= xna[2];
      ea = Math.atan(Math.tan((180 - parg) * SwissData.DEGTORAD / 2) *
                                              Math.sqrt((1-ecce)/(1+ecce))) * 2;
      eax = Math.atan(Math.tan((180 - pargx) * SwissData.DEGTORAD / 2) *
                                  Math.sqrt((1-ecce-vecce)/(1+ecce+vecce))) * 2;
      xnd[2+xndOffs] = sema * (Math.cos(ea) - ecce) / Math.cos((180 - parg) * SwissData.DEGTORAD);
      xnd[5+xndOffs] = (sema+vsema) * (Math.cos(eax) - ecce - vecce) /
                                             Math.cos((180 - pargx) * SwissData.DEGTORAD);
      xnd[5+xndOffs] -= xnd[2+xndOffs];
      /* no light-time correction because speed is extremely small */
      for (i = 0, xp = xx, xpOffs = 0; i < 4; i++, xpOffs += 6) {
        /* to cartesian coordinates */
        xp[0+xpOffs] *= SwissData.DEGTORAD;
        xp[1+xpOffs] *= SwissData.DEGTORAD;
        xp[3+xpOffs] *= SwissData.DEGTORAD;
        xp[4+xpOffs] *= SwissData.DEGTORAD;
        sl.swi_polcart_sp(xp, xpOffs, xp, xpOffs);
      }
    /***************************************
     * "true" or osculating nodes and apsides
     ***************************************/
    } else {
      /* first, we need a heliocentric distance of the planet */
      if (sw.swe_calc(tjd_et, ipli, iflg0, x, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
      iflJ2000 = (iflag & SweConst.SEFLG_EPHMASK)|
                 SweConst.SEFLG_J2000|
                 SweConst.SEFLG_EQUATORIAL|
                 SweConst.SEFLG_XYZ|
                 SweConst.SEFLG_TRUEPOS|
                 SweConst.SEFLG_NONUT|
                 SweConst.SEFLG_SPEED;
      ellipse_is_bary = false;
      if (ipli != SweConst.SE_MOON) {
        if ((method & SweConst.SE_NODBIT_OSCU_BAR)!=0 && x[2] > 6) {
          iflJ2000 |= SweConst.SEFLG_BARYCTR; /* only planets beyond Jupiter */
          ellipse_is_bary = true;
        } else {
          iflJ2000 |= SweConst.SEFLG_HELCTR;
        }
      }
      /* we need three positions and three speeds
       * for three nodes/apsides. from the three node positions,
       * the speed of the node will be computed. */
      if (ipli == SweConst.SE_MOON) {
        dt = SwephData.NODE_CALC_INTV;
        dzmin = 1e-15;
        Gmsm = SwephData.GEOGCONST * (1 + 1 / SwephData.EARTH_MOON_MRAT) /
                            SweConst.AUNIT/SweConst.AUNIT/SweConst.AUNIT*86400.0*86400.0;
      } else {
        if ((ipli >= SweConst.SE_MERCURY && ipli <= SweConst.SE_PLUTO) ||
                                                   ipli == SweConst.SE_EARTH) {
          plm = 1 / plmass[ipl_to_elem[ipl]];
        } else {
          plm = 0;
        }
        dt = SwephData.NODE_CALC_INTV * 10 * x[2];
        dzmin = 1e-15 * dt / SwephData.NODE_CALC_INTV;
        Gmsm = SwephData.HELGRAVCONST * (1 + plm) /
                            SweConst.AUNIT/SweConst.AUNIT/SweConst.AUNIT*86400.0*86400.0;
      }
      if ((iflag & SweConst.SEFLG_SPEED)!=0) {
        istart = 0;
        iend = 2;
      } else {
        istart = iend = 0;
        dt = 0;
      }
      for (i = istart, t = tjd_et - dt; i <= iend; i++, t += dt) {
        if (istart == iend) {
          t = tjd_et;
        }
        if (sw.swe_calc(t, ipli, iflJ2000, xpos[i], serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
        /* the EMB is used instead of the earth */
        if (ipli == SweConst.SE_EARTH) {
          if (sw.swe_calc(t,
                       SweConst.SE_MOON,
                       iflJ2000 & ~(SweConst.SEFLG_BARYCTR|SweConst.SEFLG_HELCTR),
                       xposm, serr) == SweConst.ERR) {
            return SweConst.ERR;
          }
          for (j = 0; j <= 2; j++)
            xpos[i][j] += xposm[j] / (SwephData.EARTH_MOON_MRAT + 1.0);
        }
        sw.swi_plan_for_osc_elem(iflg0, t, xpos[i]);
      }
      for (i = istart; i <= iend; i++) {
        if (Math.abs(xpos[i][5]) < dzmin) {
          xpos[i][5] = dzmin;
        }
        fac = xpos[i][2] / xpos[i][5];
        sgn = xpos[i][5] / Math.abs(xpos[i][5]);
        for (j = 0; j <= 2; j++) {
          xn[i][j] = (xpos[i][j] - fac * xpos[i][j+3]) * sgn;
          xs[i][j] = -xn[i][j];
        }
      }
      for (i = istart; i <= iend; i++) {
        /* node */
        rxy =  Math.sqrt(xn[i][0] * xn[i][0] + xn[i][1] * xn[i][1]);
        cosnode = xn[i][0] / rxy;
        sinnode = xn[i][1] / rxy;
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
        sinE = 1 / ecce / Math.sqrt(sema * Gmsm) *
                                        sw.dot_prod(xpos[i], xpos[i], 3);
        /* true anomaly */
        ny = 2 * Math.atan(Math.sqrt((1+ecce)/(1-ecce)) * sinE / (1 + cosE));
        /* distance of perihelion from ascending node */
        xq[i][0] = sl.swi_mod2PI(uu - ny);
        xq[i][1] = 0;                        /* latitude */
        xq[i][2] = sema * (1 - ecce);        /* distance of perihelion */
        /* transformation to ecliptic coordinates */
        sl.swi_polcart(xq[i], xq[i]);
        sl.swi_coortrf2(xq[i], xq[i], -sinincl, cosincl);
        sl.swi_cartpol(xq[i], xq[i]);
        /* adding node, we get perihelion in ecl. coord. */
        xq[i][0] += Math.atan2(sinnode, cosnode);
        xa[i][0] = sl.swi_mod2PI(xq[i][0] + SwephData.PI);
        xa[i][1] = -xq[i][1];
        if (do_focal_point) {
          xa[i][2] = sema * ecce * 2;        /* distance of aphelion */
        } else {
          xa[i][2] = sema * (1 + ecce);        /* distance of aphelion */
        }
        sl.swi_polcart(xq[i], xq[i]);
        sl.swi_polcart(xa[i], xa[i]);
        /* new distance of node from orbital ellipse:
         * true anomaly of node: */
        ny = sl.swi_mod2PI(ny - uu);
        ny2 = sl.swi_mod2PI(ny + SwephData.PI);
        /* eccentric anomaly */
        cosE = Math.cos(2 * Math.atan(Math.tan(ny / 2) /
                                             Math.sqrt((1+ecce) / (1-ecce))));
        cosE2 = Math.cos(2 * Math.atan(Math.tan(ny2 / 2) /
                                             Math.sqrt((1+ecce) / (1-ecce))));
        /* new distance */
        rn = sema * (1 - ecce * cosE);
        rn2 = sema * (1 - ecce * cosE2);
        /* old node distance */
        ro = Math.sqrt(sl.square_sum(xn[i]));
        ro2 = Math.sqrt(sl.square_sum(xs[i]));
        /* correct length of position vector */
        for (j = 0; j <= 2; j++) {
          xn[i][j] *= rn / ro;
          xs[i][j] *= rn2 / ro2;
        }
      }
      for (i = 0; i <= 2; i++) {
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
          xpe[i+xpeOffs] = xq[1][i];
          xpe[i+3+xpeOffs] = (xq[2][i] - xq[0][i]) / dt / 2;
          xap[i+xapOffs] = xa[1][i];
          xap[i+3+xapOffs] = (xa[2][i] - xa[0][i]) / dt / 2;
          xna[i] = xn[1][i];
          xna[i+3] = (xn[2][i] - xn[0][i]) / dt / 2;
          xnd[i+xndOffs] = xs[1][i];
          xnd[i+3+xndOffs] = (xs[2][i] - xs[0][i]) / dt / 2;
        } else {
          xpe[i+xpeOffs] = xq[0][i];
          xpe[i+3+xpeOffs] = 0;
          xap[i+xapOffs] = xa[0][i];
          xap[i+3+xapOffs] = 0;
          xna[i] = xn[0][i];
          xna[i+3] = 0;
          xnd[i+xndOffs] = xs[0][i];
          xnd[i+3+xndOffs] = 0;
        }
      }
      is_true_nodaps = true;
    }
    /* to set the variables required in the save area,
     * i.e. ecliptic, nutation, barycentric sun, earth
     * we compute the planet */
    if (ipli == SweConst.SE_MOON &&
        (iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR))!=0) {
      sw.swi_force_app_pos_etc();
      if (sw.swe_calc(tjd_et, SweConst.SE_SUN, iflg0, x, serr) == SweConst.ERR) {
        return SweConst.ERR;
      }
    } else {
      if (sw.swe_calc(tjd_et, ipli,
                   iflg0 | (iflag & SweConst.SEFLG_TOPOCTR), x, serr) ==
                                                                SweConst.ERR) {
        return SweConst.ERR;
      }
    }
    /***********************
     * position of observer
     ***********************/
    if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
      /* geocentric position of observer */
      if (sw.swi_get_observer(tjd_et, iflag, false, xobs, serr) != SweConst.OK) {
        return SweConst.ERR;
      }
      /*for (i = 0; i <= 5; i++)
        xobs[i] = swed.topd.xobs[i];*/
    } else {
      for (i = 0; i <= 5; i++)
        xobs[i] = 0;
    }
    if ((iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR))!=0) {
      if ((iflag & SweConst.SEFLG_HELCTR)!=0 &&
          (iflag & SweConst.SEFLG_MOSEPH)==0) {
        for (i = 0; i <= 5; i++)
          xobs[i] = xsun[i];
      }
    } else if (ipl == SweConst.SE_SUN && (iflag & SweConst.SEFLG_MOSEPH)==0) {
      for (i = 0; i <= 5; i++)
        xobs[i] = xsun[i];
    } else {
      /* barycentric position of observer */
      for (i = 0; i <= 5; i++)
        xobs[i] += xear[i];
    }
    /* ecliptic obliqity */
    if ((iflag & SweConst.SEFLG_J2000)!=0) {
      oe = swed.oec2000;
    } else {
      oe = swed.oec;
    }
    /*************************************************
     * conversions shared by mean and osculating points
     *************************************************/
    for (ij = 0, xp = xx, xpOffs = 0; ij < 4; ij++, xpOffs += 6) {
      /* no nodes for earth */
      if (ipli == SweConst.SE_EARTH && ij <= 1) {
        for (i = 0; i <= 5; i++)
              xp[i+xpOffs] = 0;
        continue;
      }
      /*********************
       * to equator
       *********************/
      if (is_true_nodaps && (iflag & SweConst.SEFLG_NONUT)==0) {
        sl.swi_coortrf2(xp, xpOffs, xp, xpOffs, -swed.nut.snut, swed.nut.cnut);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
          sl.swi_coortrf2(xp, 3+xpOffs, xp, 3+xpOffs, -swed.nut.snut, swed.nut.cnut);
        }
      }
      sl.swi_coortrf2(xp, xpOffs, xp, xpOffs, -oe.seps, oe.ceps);
      sl.swi_coortrf2(xp, 3+xpOffs, xp, 3+xpOffs, -oe.seps, oe.ceps);
      if (is_true_nodaps) {
        /****************************
         * to mean ecliptic of date
         ****************************/
        if ((iflag & SweConst.SEFLG_NONUT)==0) {
          sw.swi_nutate(xp, xpOffs, iflag, true);
        }
      }
      /*********************
       * to J2000
       *********************/
      sl.swi_precess(xp, xpOffs, tjd_et, SwephData.J_TO_J2000);
      if ((iflag & SweConst.SEFLG_SPEED)!=0) {
        sw.swi_precess_speed(xp, xpOffs, tjd_et, SwephData.J_TO_J2000);
      }
      /*********************
       * to barycenter
       *********************/
      if (ipli == SweConst.SE_MOON) {
        for (i = 0; i <= 5; i++)
          xp[i+xpOffs] += xear[i];
      } else {
        if ((iflag & SweConst.SEFLG_MOSEPH)==0 && !ellipse_is_bary) {
          for (j = 0; j <= 5; j++)
            xp[j+xpOffs] += xsun[j];
        }
      }
      /*********************
       * to correct center
       *********************/
      for (j = 0; j <= 5; j++)
        xp[j+xpOffs] -= xobs[j];
          /* geocentric perigee/apogee of sun */
      if (ipl == SweConst.SE_SUN &&
          (iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR))==0) {
        for (j = 0; j <= 5; j++)
          xp[j+xpOffs] = -xp[j+xpOffs];
      }
      /*********************
       * light deflection
       *********************/
      dt = Math.sqrt(sl.square_sum(xp, xpOffs)) * SweConst.AUNIT / SwephData.CLIGHT / 86400.0;
      if (do_defl) {
        sw.swi_deflect_light(xp, xpOffs, dt, iflag);
      }
      /*********************
       * aberration
       *********************/
      if (do_aberr) {
        sw.swi_aberr_light(xp, xpOffs, xobs, iflag);
        /*
         * Apparent speed is also influenced by
         * the difference of speed of the earth between t and t-dt.
         * Neglecting this would result in an error of several 0.1"
         */
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
          /* get barycentric sun and earth for t-dt into save area */
          if (sw.swe_calc(tjd_et - dt, ipli,
                       iflg0 | (iflag & SweConst.SEFLG_TOPOCTR), x2, serr) ==
                                                                SweConst.ERR) {
            return SweConst.ERR;
          }
          if ((iflag & SweConst.SEFLG_TOPOCTR)!=0) {
            /* geocentric position of observer */
            /* if (sw.swi_get_observer(tjd_et - dt, iflag, false, xobs, serr) != SweConst.OK)
              return SweConst.ERR;*/
            for (i = 0; i <= 5; i++)
              xobs2[i] = swed.topd.xobs[i];
          } else {
            for (i = 0; i <= 5; i++)
              xobs2[i] = 0;
          }
          if ((iflag & (SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR))!=0) {
            if ((iflag & SweConst.SEFLG_HELCTR)!=0 &&
                (iflag & SweConst.SEFLG_MOSEPH)==0) {
              for (i = 0; i <= 5; i++)
                xobs2[i] = xsun[i];
            }
          } else if (ipl == SweConst.SE_SUN && (iflag & SweConst.SEFLG_MOSEPH)==0) {
            for (i = 0; i <= 5; i++)
              xobs2[i] = xsun[i];
          } else {
            /* barycentric position of observer */
            for (i = 0; i <= 5; i++)
              xobs2[i] += xear[i];
          }
          for (i = 3; i <= 5; i++)
            xp[i+xpOffs] += xobs[i] - xobs2[i];
          /* The above call of swe_calc() has destroyed the
           * parts of the save area
           * (i.e. bary sun, earth nutation matrix!).
           * to restore it:
           */
          if (sw.swe_calc(tjd_et, SweConst.SE_SUN,
                       iflg0 | (iflag & SweConst.SEFLG_TOPOCTR), x2, serr) ==
                                                                SweConst.ERR) {
            return SweConst.ERR;
          }
        }
      }
      /*********************
       * precession
       *********************/
      /* save J2000 coordinates; required for sidereal positions */
      for (j = 0; j <= 5; j++)
        x2000[j] = xp[j+xpOffs];
      if ((iflag & SweConst.SEFLG_J2000)==0) {
        sl.swi_precess(xp, xpOffs, tjd_et, SwephData.J2000_TO_J);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
          sw.swi_precess_speed(xp, xpOffs, tjd_et, SwephData.J2000_TO_J);
        }
      }
      /*********************
       * nutation
       *********************/
      if ((iflag & SweConst.SEFLG_NONUT)==0) {
        sw.swi_nutate(xp, xpOffs, iflag, false);
      }
      /* now we have equatorial cartesian coordinates; keep them */
      for (j = 0; j <= 5; j++)
        pldat.xreturn[18+j] = xp[j+xpOffs];
      /************************************************
       * transformation to ecliptic.                  *
       * with sidereal calc. this will be overwritten *
       * afterwards.                                  *
       ************************************************/
      sl.swi_coortrf2(xp, xpOffs, xp, xpOffs, oe.seps, oe.ceps);
      if ((iflag & SweConst.SEFLG_SPEED)!=0) {
        sl.swi_coortrf2(xp, 3+xpOffs, xp, 3+xpOffs, oe.seps, oe.ceps);
      }
      if ((iflag & SweConst.SEFLG_NONUT)==0) {
        sl.swi_coortrf2(xp, xpOffs, xp, xpOffs, swed.nut.snut, swed.nut.cnut);
        if ((iflag & SweConst.SEFLG_SPEED)!=0) {
          sl.swi_coortrf2(xp, 3+xpOffs, xp, 3+xpOffs,
                          swed.nut.snut, swed.nut.cnut);
        }
      }
        /* now we have ecliptic cartesian coordinates */
        for (j = 0; j <= 5; j++)
          pldat.xreturn[6+j] = xp[j+xpOffs];
      /************************************
       * sidereal positions               *
       ************************************/
      if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
        /* project onto ecliptic t0 */
        if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0) {
          if (sw.swi_trop_ra2sid_lon(x2000, pldat.xreturn, 6, pldat.xreturn, 18, iflag, serr) != SweConst.OK) {
            return SweConst.ERR;
          }
        /* project onto solar system equator */
        } else if ((swed.sidd.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
          if (sw.swi_trop_ra2sid_lon_sosy(x2000, pldat.xreturn, 6, pldat.xreturn, 18, iflag, serr) != SweConst.OK) {
            return SweConst.ERR;
        }
        } else {
        /* traditional algorithm */
        sl.swi_cartpol_sp(pldat.xreturn, 6, pldat.xreturn, 0);
        pldat.xreturn[0] -= sw.swe_get_ayanamsa(tjd_et) * SwissData.DEGTORAD;
        sl.swi_polcart_sp(pldat.xreturn, 0, pldat.xreturn, 6);
        }
      }
      if ((iflag & SweConst.SEFLG_XYZ)!=0 &&
          (iflag & SweConst.SEFLG_EQUATORIAL)!=0) {
        for (j = 0; j <= 5; j++)
          xp[j+xpOffs] = pldat.xreturn[18+j];
        continue;
      }
      if ((iflag & SweConst.SEFLG_XYZ)!=0) {
        for (j = 0; j <= 5; j++)
          xp[j+xpOffs] = pldat.xreturn[6+j];
        continue;
      }
      /************************************************
       * transformation to polar coordinates          *
       ************************************************/
      sl.swi_cartpol_sp(pldat.xreturn, 18, pldat.xreturn, 12);
      sl.swi_cartpol_sp(pldat.xreturn, 6, pldat.xreturn, 0);
      /**********************
       * radians to degrees *
       **********************/
      for (j = 0; j < 2; j++) {
        pldat.xreturn[j] *= SwissData.RADTODEG;                /* ecliptic */
        pldat.xreturn[j+3] *= SwissData.RADTODEG;
        pldat.xreturn[j+12] *= SwissData.RADTODEG;        /* equator */
        pldat.xreturn[j+15] *= SwissData.RADTODEG;
      }
      if ((iflag & SweConst.SEFLG_EQUATORIAL)!=0) {
        for (j = 0; j <= 5; j++)
          xp[j+xpOffs] = pldat.xreturn[12+j];
        continue;
      } else {
        for (j = 0; j <= 5; j++)
          xp[j+xpOffs] = pldat.xreturn[j];
        continue;
      }
    }
    for (i = 0; i <= 5; i++) {
      if (i > 2 && (iflag & SweConst.SEFLG_SPEED)==0) {
        xna[i] = xnd[i+xndOffs] = xpe[i+xpeOffs] = xap[i+xapOffs] = 0;
      }
      if (xnasc != null) {
        xnasc[i] = xna[i];
      }
      if (xndsc != null) {
        xndsc[i] = xnd[i+xndOffs];
      }
      if (xperi != null) {
        xperi[i] = xpe[i+xpeOffs];
      }
      if (xaphe != null) {
        xaphe[i] = xap[i+xapOffs];
      }
    }
    return SweConst.OK;
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
  * @see Swecl.html#swe_nod_aps(double, int, int, int, double[], double[], double[], double[], java.lang.StringBuffer)
  * @see swisseph.SweConst#OK
  * @see swisseph.SweConst#ERR
  * @see swisseph.SweConst#SE_NODBIT_MEAN
  * @see swisseph.SweConst#SE_NODBIT_OSCU
  * @see swisseph.SweConst#SE_NODBIT_OSCU_BAR
  * @see swisseph.SweConst#SE_NODBIT_FOPOINT
  */
  int swe_nod_aps_ut(double tjd_ut, int ipl, int iflag,
                     int  method,
                     double[] xnasc, double[] xndsc,
                     double[] xperi, double[] xaphe,
                     StringBuffer serr) {
    return swe_nod_aps(tjd_ut + SweDate.getDeltaT(tjd_ut),
                        ipl, iflag, method, xnasc, xndsc, xperi, xaphe,
                        serr);
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
  int swe_gauquelin_sector(double t_ut, int ipl, StringBuffer starname, int iflag, int imeth, double[] geopos, double atpress, double attemp, DblObj dgsect, StringBuffer serr) {
    DblObj dtmp=new DblObj();
    boolean rise_found = true;
    boolean set_found = true;
    int retval;
    double tret[]=new double[3];
    double t_et, t;
    double x0[]=new double[6];
    double eps, nutlo[]=new double[2], armc;
    int epheflag = iflag & SweConst.SEFLG_EPHMASK;
    boolean do_fixstar = (starname != null && starname.length() > 0);
    int risemeth = SweConst.SE_BIT_DISC_CENTER;
    boolean above_horizon = false;
    /* 
     * geometrically from ecl. longitude and latitude 
     */
    if ((imeth & 2)==0) {
      t_et = t_ut + SweDate.getDeltaT(t_ut);
      eps = sl.swi_epsiln(t_et) * SwissData.RADTODEG;
      sl.swi_nutation(t_et, nutlo);
      nutlo[0] *= SwissData.RADTODEG;
      nutlo[1] *= SwissData.RADTODEG;
      armc = sl.swe_degnorm(sl.swe_sidtime0(t_ut, eps + nutlo[1], nutlo[0]) * 15 + geopos[0]);
      if (do_fixstar) {
        if (sw.swe_fixstar(starname, t_et, iflag, x0, serr) == SweConst.ERR)
  	return SweConst.ERR;
      } else {
        if (sw.swe_calc(t_et, ipl, iflag, x0, serr) == SweConst.ERR)
  	return SweConst.ERR;
      }
      if ((imeth & 1)!=0) 
        x0[1] = 0;
      dgsect.val = sw.swe_house_pos(armc, geopos[1], eps + nutlo[1], 'G', x0, null);
      return SweConst.OK;
    }
    /* 
     * from rise and set times
     */
    if (imeth == 2) 
      risemeth |= SweConst.SE_BIT_NO_REFRACTION;
    /* find the next rising time of the planet or star */
    dtmp.val=tret[0];
    retval = swe_rise_trans(t_ut, ipl, starname, epheflag,
                            SweConst.SE_CALC_RISE|risemeth, geopos, atpress, attemp,
                            dtmp, serr);
    tret[0]=dtmp.val;
    if (retval == SweConst.ERR) {
      return SweConst.ERR; 
    } else if (retval == -2) {
      /* actually, we could return ERR here. However, we
       * keep this variable, in case we implement an algorithm
       * for Gauquelin sector positions of circumpolar bodies.
       * As with the Ludwig Otto procedure with Placidus, one 
       * could replace missing rises or sets by meridian transits,
       * although there are cases where even this is not possible.
       * Sometimes a body both appears and disappears on the western 
       * part of the horizon. Using true culminations rather than meridan
       * transits would not help in any case either, because there are
       * cases where a body does not have a culmination within days,
       * e.g. the sun near the poles.
       */
      rise_found = false;    
    }
    /* find the next setting time of the planet or star */
    dtmp.val=tret[1];
    retval = swe_rise_trans(t_ut, ipl, starname, epheflag,
                            SweConst.SE_CALC_SET|risemeth, geopos, atpress, attemp,
                            dtmp, serr);
    tret[1]=dtmp.val;
    if (retval == SweConst.ERR) {
      return SweConst.ERR; 
    } else if (retval == -2) {
      set_found = false;
    }
    if (tret[0] < tret[1] && rise_found == true) {
      above_horizon = false;
      /* find last set */
      t = t_ut - 1.2;
      if (set_found) t = tret[1] - 1.2;
      set_found = true;
      dtmp.val=tret[1];
      retval = swe_rise_trans(t, ipl, starname, epheflag,
                            SweConst.SE_CALC_SET|risemeth, geopos, atpress, attemp,
                            dtmp, serr);
      tret[1]=dtmp.val;
      if (retval == SweConst.ERR) {
        return SweConst.ERR; 
      } else if (retval == -2) {
        set_found = false;
      }
    } else if (tret[0] >= tret[1] && set_found == true) {
      above_horizon = true;
      /* find last rise */
      t = t_ut - 1.2;
      if (rise_found) t = tret[0] - 1.2;
      rise_found = true;
      dtmp.val=tret[0];
      retval = swe_rise_trans(t, ipl, starname, epheflag,
                            SweConst.SE_CALC_RISE|risemeth, geopos, atpress, attemp,
                            dtmp, serr);
      tret[0]=dtmp.val;
      if (retval == SweConst.ERR) {
        return SweConst.ERR; 
      } else if (retval == -2) {
        rise_found = false;
      }
    }
    if (rise_found && set_found) {
      if (above_horizon) {
        dgsect.val = (t_ut - tret[0]) / (tret[1] - tret[0]) * 18 + 1;
      } else {
        dgsect.val = (t_ut - tret[1]) / (tret[0] - tret[1]) * 18 + 19;
      }
      return SweConst.OK;
    } else {
      dgsect.val = 0;
      if (serr!=null)
        serr.append("rise or set not found for planet ").append(ipl);
      return SweConst.ERR;
    }
  }
} // End of class Swecl
