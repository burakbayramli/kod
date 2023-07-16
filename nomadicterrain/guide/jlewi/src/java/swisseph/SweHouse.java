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
* This class does all the calculations that are related to astrological
* houses.
* <P><I><B>You will find the complete documentation for the original
* SwissEphemeris package at <A HREF="http://www.astro.ch/swisseph/sweph_g.htm">
* http://www.astro.ch/swisseph/sweph_g.htm</A>. By far most of the information 
* there is directly valid for this port to Java as well.</B></I>
* @version 1.0.0a
*/
class SweHouse {

  static final double MILLIARCSEC=1.0 / 3600000.0;

  SwissLib sl=null;
  SwissEph sw=null;
  SwissData swed=null;

  /**
  * Constructs a new SweHouse object.
  */
  SweHouse() {
    sl   = new SwissLib();
    sw   = new SwissEph();
    swed = new SwissData();
  }

  /**
  * Constructs a new SweHouse object by using the given objects. If some or
  * all objects are null, they will be automatically instantiated here.
  * @param sl A SwissLib object that might be already available.
  * @param sw A SwissEph object that might be already available.
  * @param swed A SwissData object that might be already available.
  */
  SweHouse(SwissLib sl, SwissEph sw, SwissData swed) {
    this.sl   = sl;
    this.sw   = sw;
    this.swed = swed;
    if (this.sl   ==null) { this.sl   =new SwissLib(); }
    if (this.sw   ==null) { this.sw   =new SwissEph(); }
    if (this.swed ==null) { this.swed =new SwissData(); }
  }


  static final double VERY_SMALL=1E-10;

// Hmmm? Never used anywhere...
//  public double degtocs(double x) {
//    return sl.swe_d2l((x) * SwissData.DEG);
//  }

// Hmmm? Never used anywhere...
//  public double cstodeg(double x) {
//    return (double)((x) * SwissData.CS2DEG);
//  }

  private double sind(double x) {
    return Math.sin(x * SwissData.DEGTORAD);
  }
  private double cosd(double x) {
    return Math.cos(x * SwissData.DEGTORAD);
  }
  private double tand(double x) {
    return Math.tan(x * SwissData.DEGTORAD);
  }
  private double asind(double x) {
    return (Math.asin(x) * SwissData.RADTODEG);
  }
//Never used anywhere//  private double acosd(double x) {
//Never used anywhere//    return (Math.acos(x) * SwissData.RADTODEG);
//Never used anywhere//  }
  private double atand(double x) {
    return (Math.atan(x) * SwissData.RADTODEG);
  }
//Never used anywhere//  private double atan2d(double y, double x) {
//Never used anywhere//    return (Math.atan2(y, x) * SwissData.RADTODEG);
//Never used anywhere//  }



  /* housasp.c
   * cusps are returned in double cusp[13],
   *                           or cusp[37] with house system 'G':
   * cusp[1...12]        houses 1 - 12
   * additional points are returned in ascmc[10].
   * ascmc[0] = ascendant
   * ascmc[1] = mc
   * ascmc[2] = armc
   * ascmc[3] = vertex
   * ascmc[4] = equasc            * "equatorial ascendant" *
   * ascmc[5] = coasc1            * "co-ascendant" (W. Koch) *
   * ascmc[6] = coasc2            * "co-ascendant" (M. Munkasey) *
   * ascmc[7] = polasc            * "polar ascendant" (M. Munkasey) *
   */
  /**
  /**
  * Calculates the house positions and other vital points. The possible
  * house systems (parameter &quot;hsys&quot;) are:<P><CODE><BLOCKQUOTE>
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
  * The parameter ascmc is defined as double[10] and will return the
  * following points:<P><CODE><BLOCKQUOTE>
  * ascmc[0] = ascendant<BR>
  * ascmc[1] = mc<BR>
  * ascmc[2] = armc (sidereal time)<BR>
  * ascmc[3] = vertex<BR>
  * ascmc[4] = equatorial ascendant<BR>
  * ascmc[5] = co-ascendant (Walter Koch)<BR>
  * ascmc[6] = co-ascendant (Michael Munkasey)<BR>
  * ascmc[7] = polar ascendant (Michael Munkasey)<BR>
  * ascmc[8] = reserved for future use<BR>
  * ascmc[9] = reserved for future use
  * </BLOCKQUOTE></CODE>
  * 
  * As Koch and Placidus don't work in the polar circle, the calculation is
  * done in that case by swapping MC/IC so that MC is always before AC in
  * the zodiac. Then the quadrants are divided into 3 equal parts.<P>
  * Calculates the house positions and other vital points.<p>
  * <b>Note:</b>This routine is identical to the routine swe_houses_ex() in
  * the original C code, and <b>not</b> to swe_houses()!<p>
  * @param tjd_ut The Julian Day number in UT
  * @param iflag An additional flag for calculation, e.g. 0 or SEFLG_SIDEREAL
  * and / or SEFLG_RADIANS.
  * @param geolat The latitude on earth, for which the calculation has to be
  * done.
  * @param geolong The longitude on earth, for which the calculation has to be
  * done.
  * @param hsys The house system as a character given as an integer.
  * @param cusp The house cusps are returned here in cusp[1...12] for
  * the house 1 to 12. It has to be a double[13].
  * @param ascmc The special points like ascendent etc. are returned here.
  * It has to be a double[10].
  * @see swisseph.SweConst#SEFLG_RADIANS
  * @see swisseph.SweConst#SEFLG_SIDEREAL
  * @see swisseph.SwissEph#swe_set_sid_mode(int, double, double)
  */
  int swe_houses(double tjd_ut,
                 int iflag,
                 double geolat,
                 double geolon,
                 int hsys,
                 double[] cusp,
                 double[] ascmc) {
    return swe_houses(tjd_ut, iflag, geolat, geolon, hsys, cusp, ascmc, 0);
  }
  int swe_houses(double tjd_ut,
                 int iflag,
                 double geolat,
                 double geolon,
                 int hsys,
                 double[] cusp,
                 double[] ascmc,
                 int aOffs) {
    int i, retc = 0;
    double armc, eps_mean, nutlo[]=new double[2];
    double tjde = tjd_ut + SweDate.getDeltaT(tjd_ut);
    SidData sip = swed.sidd;
    int ito;
    if (Character.toUpperCase((char)hsys) == 'G') {
      ito = 36;
    } else {
      ito = 12;
    }
    if ((iflag & SweConst.SEFLG_SIDEREAL)!=0 && !swed.ayana_is_set) {
      sw.swe_set_sid_mode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
    }
    eps_mean = sl.swi_epsiln(tjde) * SwissData.RADTODEG;
    sl.swi_nutation(tjde, nutlo);
    for (i = 0; i < 2; i++)
      nutlo[i] *= SwissData.RADTODEG;
      /*houses_to_sidereal(tjde, geolat, hsys, eps, cusp, ascmc, iflag);*/
    armc = sl.swe_degnorm(sl.swe_sidtime0(tjd_ut, eps_mean + nutlo[1], nutlo[0]) * 15 + geolon);
    if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
      if ((sip.sid_mode & SweConst.SE_SIDBIT_ECL_T0)!=0) {
        retc = sidereal_houses_ecl_t0(tjde, armc, eps_mean + nutlo[1], nutlo, geolat, hsys, cusp, ascmc, aOffs);
      } else if ((sip.sid_mode & SweConst.SE_SIDBIT_SSY_PLANE)!=0) {
        retc = sidereal_houses_ssypl(tjde, armc, eps_mean + nutlo[1], nutlo, geolat, hsys, cusp, ascmc, aOffs);
      } else {
        retc = sidereal_houses_trad(tjde, armc, eps_mean + nutlo[1], nutlo[0], geolat, hsys, cusp, ascmc, aOffs);
      }
    } else {
      retc = swe_houses_armc(armc, geolat, eps_mean + nutlo[1], hsys, cusp, ascmc, aOffs);
    }
    if ((iflag & SweConst.SEFLG_RADIANS)!=0) {
      for (i = 1; i <= ito; i++)
        cusp[i] *= SwissData.DEGTORAD;
      for (i = 0; i < SweConst.SE_NASCMC; i++)
        ascmc[i+aOffs] *= SwissData.DEGTORAD;
    }
    return retc;
  }

  /*
   * houses to sidereal
   * ------------------
   * there are two methods:
   * a) the traditional one
   *    houses are computed tropically, then nutation and the ayanamsa
   *    are subtracted.
   * b) the projection on the ecliptic of t0
   *    The house computation is then as follows:
   *
   * Be t the birth date and t0 the epoch at which ayanamsa = 0.
   * 1. Compute the angle between the mean ecliptic at t0 and
   *    the true equator at t.
   *    The intersection point of these two circles we call the
   *    "auxiliary vernal point", and the angle between them the
   *    "auxiliary obliquity".
   * 2. Compute the distance of the auxiliary vernal point from the
   *    vernal point at t. (this is a section on the equator)
   * 3. subtract this value from the armc of t = aux. armc.
   * 4. Compute the axes and houses for this aux. armc and aux. obliquity.
   * 5. Compute the distance between the auxiliary vernal point and the
   *    vernal point at t0 (this is the ayanamsa at t, measured on the
   *    ecliptic of t0)
   * 6. subtract this distance from all house cusps.
   * 7. subtract ayanamsa_t0 from all house cusps.
   */
  private int sidereal_houses_ecl_t0(double tjde,
                                     double armc,
                                     double eps,
                                     double[] nutlo,
                                     double lat,
                                     int hsys,
                                     double[] cusp,
                                     double[] ascmc,
                                     int aOffs) {
    int i, j, retc = SweConst.OK;
    double x[]=new double[6], xvpx[]=new double[6], x2[]=new double[6], epst0,
           xnorm[]=new double[6];
    double rxy, rxyz, c2, epsx, sgn, fac, dvpx, dvpxe;
    double armcx;
    SidData sip = swed.sidd;
    int ito;
    if (Character.toUpperCase((char)hsys) == 'G') {
      ito = 36;
    } else {
      ito = 12;
    }
    /* epsilon at t0 */
    epst0 = sl.swi_epsiln(sip.t0);
    /* cartesian coordinates of an imaginary moving body on the
     * the mean ecliptic of t0; we take the vernal point: */
    x[0] = x[4] = 1;
    x[1] = x[2] = x[3] = x[5] = 0;
    /* to equator */
    sl.swi_coortrf(x, x, -epst0);
    sl.swi_coortrf(x, 3, x, 3, -epst0);
    /* to tjd_et */
    sl.swi_precess(x, sip.t0, SwephData.J_TO_J2000);
    sl.swi_precess(x, tjde, SwephData.J2000_TO_J);
    sl.swi_precess(x, 3, sip.t0, SwephData.J_TO_J2000);
    sl.swi_precess(x, 3, tjde, SwephData.J2000_TO_J);
    /* to true equator of tjd_et */
    sl.swi_coortrf(x, x, (eps - nutlo[1]) * SwissData.DEGTORAD);
    sl.swi_coortrf(x, 3, x, 3, (eps - nutlo[1]) * SwissData.DEGTORAD);
    sl.swi_cartpol_sp(x, 0, x, 0);
    x[0] += nutlo[0] * SwissData.DEGTORAD;
    sl.swi_polcart_sp(x, x);
    sl.swi_coortrf(x, x, -eps * SwissData.DEGTORAD);
    sl.swi_coortrf(x, 3, x, 3, -eps * SwissData.DEGTORAD);
    /* now, we have the moving point precessed to tjd_et.
     * next, we compute the auxiliary epsilon: */
    sl.swi_cross_prod(x, 0, x, 3, xnorm, 0);
    rxy =  xnorm[0] * xnorm[0] + xnorm[1] * xnorm[1];
    c2 = (rxy + xnorm[2] * xnorm[2]);
    rxyz = Math.sqrt(c2);
    rxy = Math.sqrt(rxy);
    epsx = Math.asin(rxy / rxyz) * SwissData.RADTODEG;           /* 1a */
    /* auxiliary vernal point */
    if (Math.abs(x[5]) < 1e-15) {
      x[5] = 1e-15;
    }
    fac = x[2] / x[5];
    sgn = x[5] / Math.abs(x[5]);
    for (j = 0; j <= 2; j++)
      xvpx[j] = (x[j] - fac * x[j+3]) * sgn;      /* 1b */
    /* distance of the auxiliary vernal point from
     * the zero point at tjd_et (a section on the equator): */
    sl.swi_cartpol(xvpx, x2);
    dvpx = x2[0] * SwissData.RADTODEG;                      /* 2 */
    /* auxiliary armc */
    armcx = sl.swe_degnorm(armc - dvpx);        /* 3 */
    /* compute axes and houses: */
    retc = swe_houses_armc(armcx, lat, epsx, hsys, cusp, ascmc, aOffs);  /* 4 */
    /* distance between auxiliary vernal point and
     * vernal point of t0 (a section on the sidereal plane) */
    dvpxe = Math.acos(sl.swi_dot_prod_unit(x, xvpx)) * SwissData.RADTODEG;  /* 5 */
    if (tjde < sip.t0) {
      dvpxe = -dvpxe;
    }
    for (i = 1; i <= ito; i++)                     /* 6, 7 */
      cusp[i] = sl.swe_degnorm(cusp[i] - dvpxe - sip.ayan_t0);
    for (i = 0; i <= SweConst.SE_NASCMC; i++)
      ascmc[aOffs+i] = sl.swe_degnorm(ascmc[aOffs+i] - dvpxe - sip.ayan_t0);
    return retc;
  }

  /*
   * Be t the birth date and t0 the epoch at which ayanamsa = 0.
   * 1. Compute the angle between the solar system rotation plane and
   *    the true equator at t.
   *    The intersection point of these two circles we call the
   *    "auxiliary vernal point", and the angle between them the
   *    "auxiliary obliquity".
   * 2. Compute the distance of the auxiliary vernal point from the
   *    zero point at t. (this is a section on the equator)
   * 3. subtract this value from the armc of t = aux. armc.
   * 4. Compute the axes and houses for this aux. armc and aux. obliquity.
   * 5. Compute the distance between the auxiliary vernal point at t
   *    and the zero point of the solar system plane J2000
   *    (a section measured on the solar system plane)
   * 6. subtract this distance from all house cusps.
   * 7. compute the ayanamsa of J2000 on the solar system plane,
   *    referred to t0
   * 8. subtract ayanamsa_t0 from all house cusps.
   * 9. subtract ayanamsa_2000 from all house cusps.
   */
  private int sidereal_houses_ssypl(double tjde,
                                    double armc,
                                    double eps,
                                    double[] nutlo,
                                    double lat,
                                    int hsys,
                                    double[] cusp,
                                    double[] ascmc,
                                    int aOffs) {
    int i, j, retc = SweConst.OK;
    double x[]=new double[6], x0[]=new double[6], xvpx[]=new double[6],
           x2[]=new double[6], epst0, xnorm[]=new double[6];
    double rxy, rxyz, c2, epsx, eps2000, sgn, fac, dvpx, dvpxe, x00;
    double armcx;
    SidData sip = swed.sidd;
    int ito;
    if (Character.toUpperCase((char)hsys) == 'G') {
      ito = 36;
    } else {
      ito = 12;
    }
    /* epsilon at t0 */
    epst0 = sl.swi_epsiln(sip.t0);
    eps2000 = sl.swi_epsiln(SwephData.J2000);
    /* cartesian coordinates of the zero point on the
     * the solar system rotation plane */
    x[0] = x[4] = 1;
    x[1] = x[2] = x[3] = x[5] = 0;
    /* to ecliptic 2000 */
    sl.swi_coortrf(x, x, -SwephData.SSY_PLANE_INCL);
    sl.swi_coortrf(x, 3, x, 3, -SwephData.SSY_PLANE_INCL);
    sl.swi_cartpol_sp(x, 0, x, 0);
    x[0] += SwephData.SSY_PLANE_NODE_E2000;
    sl.swi_polcart_sp(x, x);
    /* to equator 2000 */
    sl.swi_coortrf(x, x, -eps2000);
    sl.swi_coortrf(x, 3, x, 3, -eps2000);
    /* to mean equator of t */
    sl.swi_precess(x, tjde, SwephData.J2000_TO_J);
    sl.swi_precess(x, 3, tjde, SwephData.J2000_TO_J);
    /* to true equator of t */
    sl.swi_coortrf(x, x, (eps - nutlo[1]) * SwissData.DEGTORAD);
    sl.swi_coortrf(x, 3, x, 3, (eps - nutlo[1]) * SwissData.DEGTORAD);
    sl.swi_cartpol_sp(x, 0, x, 0);
    x[0] += nutlo[0] * SwissData.DEGTORAD;
    sl.swi_polcart_sp(x, x);
    sl.swi_coortrf(x, x, -eps * SwissData.DEGTORAD);
    sl.swi_coortrf(x, 3, x, 3, -eps * SwissData.DEGTORAD);
    /* now, we have the moving point precessed to tjd_et.
     * next, we compute the auxiliary epsilon: */
    sl.swi_cross_prod(x, 0, x, 3, xnorm, 0);
    rxy =  xnorm[0] * xnorm[0] + xnorm[1] * xnorm[1];
    c2 = (rxy + xnorm[2] * xnorm[2]);
    rxyz = Math.sqrt(c2);
    rxy = Math.sqrt(rxy);
    epsx = Math.asin(rxy / rxyz) * SwissData.RADTODEG;           /* 1a */
    /* auxiliary vernal point */
    if (Math.abs(x[5]) < 1e-15) {
      x[5] = 1e-15;
    }
    fac = x[2] / x[5];
    sgn = x[5] / Math.abs(x[5]);
    for (j = 0; j <= 2; j++)
      xvpx[j] = (x[j] - fac * x[j+3]) * sgn;      /* 1b */
    /* distance of the auxiliary vernal point from
     * mean vernal point at tjd_et (a section on the equator): */
    sl.swi_cartpol(xvpx, x2);
    dvpx = x2[0] * SwissData.RADTODEG;                      /* 2 */
    /* auxiliary armc */
    armcx = sl.swe_degnorm(armc - dvpx);        /* 3 */
    /* compute axes and houses: */
    retc = swe_houses_armc(armcx, lat, epsx, hsys, cusp, ascmc, aOffs);  /* 4 */
    /* distance between the auxiliary vernal point at t and
     * the sidereal zero point of 2000 at t
     * (a section on the sidereal plane).
     */
    dvpxe = Math.acos(sl.swi_dot_prod_unit(x, xvpx)) * SwissData.RADTODEG;  /* 5 */
                  /* (always positive for dates after 5400 bc) */
    dvpxe -= SwephData.SSY_PLANE_NODE * SwissData.RADTODEG;
    /* ayanamsa between t0 and J2000, measured on solar system plane: */
    /* position of zero point of t0 */
    x0[0] = 1;
    x0[1] = x0[2] = 0;
    /* zero point of t0 in J2000 system */
    if (sip.t0 != SwephData.J2000) {
      sl.swi_precess(x0, sip.t0, SwephData.J_TO_J2000);
    }
    /* zero point to ecliptic 2000 */
    sl.swi_coortrf(x0, x0, eps2000);
    /* to solar system plane */
    sl.swi_cartpol(x0, x0);
    x0[0] -= SwephData.SSY_PLANE_NODE_E2000;
    sl.swi_polcart(x0, x0);
    sl.swi_coortrf(x0, x0, SwephData.SSY_PLANE_INCL);
    sl.swi_cartpol(x0, x0);
    x0[0] += SwephData.SSY_PLANE_NODE;
    x00 = x0[0] * SwissData.RADTODEG;                       /* 7 */
    for (i = 1; i <= ito; i++)                     /* 6, 8, 9 */
      cusp[i] = sl.swe_degnorm(cusp[i] - dvpxe - sip.ayan_t0 - x00);
    for (i = 0; i <= SweConst.SE_NASCMC; i++)
      ascmc[aOffs+i] = sl.swe_degnorm(ascmc[aOffs+i] - dvpxe - sip.ayan_t0 - x00);
    return retc;
  }

  /* common simplified procedure */
  private int sidereal_houses_trad(double tjde,
                                   double armc,
                                   double eps,
                                   double nutl,
                                   double lat,
                                   int hsys,
                                   double[] cusp,
                                   double[] ascmc,
                                   int aOffs) {
    int i, retc = SweConst.OK;
    double ay;
    int ito;
    if (Character.toUpperCase((char)hsys) == 'G') {
      ito = 36;
    } else {
      ito = 12;
    }
    retc = swe_houses_armc(armc, lat, eps, hsys, cusp, ascmc, aOffs);
    ay = sw.swe_get_ayanamsa(tjde);
    for (i = 1; i <= ito; i++)
      cusp[i] = sl.swe_degnorm(cusp[i] - ay - nutl);
    for (i = 0; i < SweConst.SE_NASCMC; i++) {
      if (i == 2) /* armc */ {
        continue;
      }
      ascmc[aOffs+i] = sl.swe_degnorm(ascmc[aOffs+i] - ay - nutl);
    }
    return retc;
  }

  /*
   * this function is required for very special computations
   * where no date is given for house calculation,
   * e.g. for composite charts or progressive charts.
   * cusps are returned in double cusp[13],
   *                           or cusp[37] with house system 'G'.
   * cusp[1...12] houses 1 - 12
   * additional points are returned in ascmc[10].
   * ascmc[0] = ascendant
   * ascmc[1] = mc
   * ascmc[2] = armc
   * ascmc[3] = vertex
   * ascmc[4] = equasc            * "equatorial ascendant" *
   * ascmc[5] = coasc1            * "co-ascendant" (Walter Koch) *
   * ascmc[6] = coasc2            * "co-ascendant" (Michael Munkasey) *
   * ascmc[7] = polasc            * "polar ascendant" (Michael Munkasey) *
   */
  /**
  * Calculates the house positions and other vital points. You would use
  * this method instead of swe_houses, if you do not have a date available,
  * but just the ARMC (==sidereal time).
  * @param armc The ARMC (==sidereal time)
  * @param geolat The latitude on earth, for which the calculation has to be
  * done.
  * @param eps The ecliptic obliquity (e.g. xx[0] of swe_calc(...))
  * @param hsys The house system as a character given as an integer.
  * @param cusp The house cusps are returned here in cusp[1...12] for
  * the house 1 to 12.
  * @param ascmc The special points like ascendent etc. are returned here.
  * @see #swe_houses(double, int, double, double, int, double[], double[])
  * @see swisseph.SwissEph#swe_calc
  * @return SweConst.OK (==0) or SweConst.ERR (==-1), if an error occured.
  */
  int swe_houses_armc(double armc,
                      double geolat,
                      double eps,
                      int hsys,
                      double cusp[],
                      double ascmc[],
                      int aOffs) {
    Houses h=new Houses();
    int i, retc = 0;
    int ito;
    if (Character.toUpperCase((char)hsys) == 'G') {
      ito = 36;
    } else {
      ito = 12;
    }
    armc = sl.swe_degnorm(armc);
    retc = CalcH(armc,
                 geolat,
                 eps,
                 (char)hsys, 2, h);
    cusp[0] = 0;
    for (i = 1; i <= ito; i++) {
      cusp[i] = h.cusp[i];
    }
    ascmc[aOffs+0] = h.ac;        /* Asc */
    ascmc[aOffs+1] = h.mc;        /* Mid */
    ascmc[aOffs+2] = armc;
    ascmc[aOffs+3] = h.vertex;
    ascmc[aOffs+4] = h.equasc;
    ascmc[aOffs+5] = h.coasc1;  /* "co-ascendant" (Walter Koch) */
    ascmc[aOffs+6] = h.coasc2;  /* "co-ascendant" (Michael Munkasey) */
    ascmc[aOffs+7] = h.polasc;  /* "polar ascendant" (Michael Munkasey) */
    for (i = SweConst.SE_NASCMC; i < 10; i++)
      ascmc[aOffs+i] = 0;
    return retc;
  }

  private int CalcH(double th, double fi, double ekl, char hsy,
                    int iteration_count, Houses hsp )
  /* *********************************************************
   *  Arguments: th = sidereal time (angle 0..360 degrees
   *             hsy = letter code for house system;
   *                   A  equal
   *                   E  equal
   *                   B  Alcabitius
   *                   C  Campanus
   *                   H  horizon / azimut
   *                   K  Koch
   *                   O  Porphyry
   *                   P  Placidus
   *                   R  Regiomontanus
   *                   V  equal Vehlow
   *                   X  axial rotation system/ Meridian houses
   *                   G  36 Gauquelin sectors
   *                   U  Krusinski
   *             fi = geographic latitude
   *             ekl = obliquity of the ecliptic
   *             iteration_count = number of iterations in
   *             Placidus calculation; can be 1 or 2.
   * *********************************************************
   *  Koch and Placidus don't work in the polar circle.
   *  We swap MC/IC so that MC is always before AC in the zodiac
   *  We than divide the quadrants into 3 equal parts.
   * *********************************************************
   *  All angles are expressed in degrees.
   *  Special trigonometric functions sind, cosd etc. are
   *  implemented for arguments in degrees.
   ***********************************************************/
  {
    double tane, tanfi, cosfi, tant, sina, cosa, th2;
    double a, c, f, fh1, fh2, xh1, xh2, rectasc, ad3, acmc, vemc;
    int i, ih, ih2, retc = SweConst.OK;
    double sine, cose;
    double x[] = new double[3], krHorizonLon; /* BK 14.02.2006 */
    cose  = cosd(ekl);
    sine  = sind(ekl);
    tane  = tand(ekl);
    /* north and south poles */
    if (Math.abs(Math.abs(fi) - 90) < VERY_SMALL) {
      if (fi < 0) {
        fi = -90 + VERY_SMALL;
      } else {
        fi = 90 - VERY_SMALL;
      }
    }
    tanfi = tand(fi);
    /* mc */
    if (Math.abs(th - 90) > VERY_SMALL
      && Math.abs(th - 270) > VERY_SMALL) {
      tant = tand(th);
      hsp.mc = atand(tant / cose);
      if (th > 90 && th <= 270) {
        hsp.mc = sl.swe_degnorm(hsp.mc + 180);
      }
    } else {
      if (Math.abs(th - 90) <= VERY_SMALL) {
        hsp.mc = 90;
      } else {
        hsp.mc = 270;
      }
    } /*  if */
    hsp.mc = sl.swe_degnorm(hsp.mc);
    /* ascendant */
    hsp.ac = Asc1 (th + 90, fi, sine, cose);
    hsp.cusp[1] = hsp.ac;
    hsp.cusp[10] = hsp.mc;
    hsy=Character.toUpperCase(hsy);
    switch (hsy) {
      case (int)'A':   /* equal houses */
      case (int)'E':
        /*
         * within polar circle we swap AC/DC if AC is on wrong side
         */
        acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
        if (acmc < 0) {
          hsp.ac = sl.swe_degnorm(hsp.ac + 180);
          hsp.cusp[1] = hsp.ac;
        }
        for (i = 2; i <=12; i++)
          hsp.cusp [i] = sl.swe_degnorm(hsp.cusp [1] + (i-1) * 30);
        break;
      case 'C': /* Campanus houses and Horizon or Azimut system */
      case 'H':
        if (hsy == 'H') {
          if (fi > 0) {
            fi = 90 - fi;
          } else {
            fi = -90 - fi;
          }
          /* equator */
          if (Math.abs(Math.abs(fi) - 90) < VERY_SMALL) {
            if (fi < 0) {
              fi = -90 + VERY_SMALL;
            } else {
              fi = 90 - VERY_SMALL;
            }
          }
          th = sl.swe_degnorm(th + 180);
        }
        fh1 = asind(sind (fi) / 2);
        fh2 = asind(Math.sqrt (3.0) / 2 * sind(fi));
        cosfi = cosd(fi);
        if (Math.abs(cosfi) == 0) {        /* '==' should be save! */
          if (fi > 0) {
            xh1 = xh2 = 90; /* cosfi = VERY_SMALL; */
          } else {
            xh1 = xh2 = 270; /* cosfi = -VERY_SMALL; */
          }
        } else {
          xh1 = atand(Math.sqrt (3.0) / cosfi);
          xh2 = atand(1 / Math.sqrt (3.0) / cosfi);
        }
        hsp.cusp [11] = Asc1 (th + 90 - xh1, fh1, sine, cose);
        hsp.cusp [12] = Asc1 (th + 90 - xh2, fh2, sine, cose);
        if (hsy == 'H') {
          hsp.cusp [1] = Asc1 (th + 90, fi, sine, cose);
        }
        hsp.cusp [2] = Asc1 (th + 90 + xh2, fh2, sine, cose);
        hsp.cusp [3] = Asc1 (th + 90 + xh1, fh1, sine, cose);
        /* within polar circle, when mc sinks below horizon and
         * ascendant changes to western hemisphere, all cusps
         * must be added 180 degrees.
         * houses will be in clockwise direction */
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
          if (acmc < 0) {
            hsp.ac = sl.swe_degnorm(hsp.ac + 180);
            hsp.mc = sl.swe_degnorm(hsp.mc + 180);
            for (i = 1; i <= 12; i++)
              hsp.cusp[i] = sl.swe_degnorm(hsp.cusp[i] + 180);
          }
        }
        if (hsy == 'H') {
          for (i = 1; i <= 3; i++)
            hsp.cusp[i] = sl.swe_degnorm(hsp.cusp[i] + 180);
          for (i = 11; i <= 12; i++)
            hsp.cusp[i] = sl.swe_degnorm(hsp.cusp[i] + 180);
          /* restore fi and th */
          if (fi > 0) {
            fi = 90 - fi;
          } else {
            fi = -90 - fi;
          }
          th = sl.swe_degnorm(th + 180);
        }
        break;
      case (int)'K': /* Koch houses */
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          retc = SweConst.ERR;
//          goto porphyry;
          makePorphyry(hsp);
          break;
        }
        sina = sind(hsp.mc) * sine / cosd(fi);      /* always << 1,
                                          because fi < polar circle */
        cosa = Math.sqrt(1 - sina * sina);          /* always >> 0 */
        c = atand(tanfi / cosa);
        ad3 = asind(sind(c) * sina) / 3.0;
        hsp.cusp [11] = Asc1 (th + 30 - 2 * ad3, fi, sine, cose);
        hsp.cusp [12] = Asc1 (th + 60 - ad3, fi, sine, cose);
        hsp.cusp [2] = Asc1 (th + 120 + ad3, fi, sine, cose);
        hsp.cusp [3] = Asc1 (th + 150 + 2 * ad3, fi, sine, cose);
        break;
      case (int)'O':   /* Porphyry houses */
//porphyry:
        makePorphyry(hsp);
        break;
      case (int)'R':   /* Regiomontanus houses */
        fh1 = atand (tanfi * 0.5);
        fh2 = atand (tanfi * cosd(30));
        hsp.cusp [11] =  Asc1 (30 + th, fh1, sine, cose);
        hsp.cusp [12] =  Asc1 (60 + th, fh2, sine, cose);
        hsp.cusp [2] =  Asc1 (120 + th, fh2, sine, cose);
        hsp.cusp [3] =  Asc1 (150 + th, fh1, sine, cose);
        /* within polar circle, when mc sinks below horizon and
         * ascendant changes to western hemisphere, all cusps
         * must be added 180 degrees.
         * houses will be in clockwise direction */
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
          if (acmc < 0) {
            hsp.ac = sl.swe_degnorm(hsp.ac + 180);
            hsp.mc = sl.swe_degnorm(hsp.mc + 180);
            for (i = 1; i <= 12; i++)
              hsp.cusp[i] = sl.swe_degnorm(hsp.cusp[i] + 180);
          }
        }
        break;
      case (int)'T':   /* 'topocentric' houses */
        fh1 = atand (tanfi / 3.0);
        fh2 = atand (tanfi * 2.0 / 3.0);
        hsp.cusp [11] =  Asc1 (30 + th, fh1, sine, cose);
        hsp.cusp [12] =  Asc1 (60 + th, fh2, sine, cose);
        hsp.cusp [2] =  Asc1 (120 + th, fh2, sine, cose);
        hsp.cusp [3] =  Asc1 (150 + th, fh1, sine, cose);
        /* within polar circle, when mc sinks below horizon and
         * ascendant changes to western hemisphere, all cusps
         * must be added 180 degrees.
         * houses will be in clockwise direction */
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
          if (acmc < 0) {
            hsp.ac = sl.swe_degnorm(hsp.ac + 180);
            hsp.mc = sl.swe_degnorm(hsp.mc + 180);
            for (i = 1; i <= 12; i++)
              hsp.cusp[i] = sl.swe_degnorm(hsp.cusp[i] + 180);
          }
        }
        break;
      case 'V':   /* equal houses after Vehlow */
        /*
         * within polar circle we swap AC/DC if AC is on wrong side
         */
        acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
        if (acmc < 0) {
          hsp.ac = sl.swe_degnorm(hsp.ac + 180);
          hsp.cusp[1] = hsp.ac;
        }
        hsp.cusp [1] = sl.swe_degnorm(hsp.ac - 15);
        for (i = 2; i <=12; i++)
          hsp.cusp [i] = sl.swe_degnorm(hsp.cusp [1] + (i-1) * 30);
        break;
      case (int)'X': {
        /*
         * Meridian or axial rotation system:
         * ecliptic points whose rectascensions
         * are armc + n * 30
         */
        int j;
        double a2 = th;
        for (i = 1; i <= 12; i++) {
          j = i + 10;
          if (j > 12) {
            j -= 12;
          }
          a2 = sl.swe_degnorm(a2 + 30);
          if (Math.abs(a2 - 90) > VERY_SMALL
            && Math.abs(a2 - 270) > VERY_SMALL) {
            tant = tand(a2);
            hsp.cusp[j] = atand(tant / cose);
            if (a2 > 90 && a2 <= 270) {
              hsp.cusp[j] = sl.swe_degnorm(hsp.cusp[j] + 180);
            }
          } else {
            if (Math.abs(a2 - 90) <= VERY_SMALL) {
              hsp.cusp[j] = 90;
            } else {
              hsp.cusp[j] = 270;
            }
          } /*  if */
          hsp.cusp[j] = sl.swe_degnorm(hsp.cusp[j]);
        }
        break;
        }
      case (int)'M': {
        /*
         * Morinus
         * points of the equator (armc + n * 30) are transformed
         * into the ecliptic coordinate system
         */
        int j;
        double am = th;
        double xm[] = new double[3];
        for (i = 1; i <= 12; i++) {
          j = i + 10;
          if (j > 12) j -= 12;
          am = sl.swe_degnorm(am + 30);
          xm[0] = am;
          xm[1] = 0;
          sl.swe_cotrans(xm, 0, xm, 0, ekl);
          hsp.cusp[j] = xm[0];
        }
        break;
        }
      case (int)'B': { /* Alcabitius */
        /* created by Alois 17-sep-2000, followed example in Matrix
           electrical library. The code reproduces the example!
           See http://www.astro.com/cgi/adict.cgi query: alcabitius
           in the resuotl page, see program code example.
           I think the Alcabitius code in Walter Pullen's Astrolog 5.40
           is wrong, because he remains in RA and forgets the transform to
           the ecliptic. */
        double dek, r, sna, sda, sn3, sd3;
        acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
        if (acmc < 0) {
          hsp.ac = sl.swe_degnorm(hsp.ac + 180);
          hsp.cusp[1] = hsp.ac;
          acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
        }
        dek = asind(sind(hsp.ac) * sine);        /* declination of Ascendant */
        /* must treat the case fi == 90 or -90 */
        r = -tanfi * tand(dek);
        /* must treat the case of abs(r) > 1; probably does not happen
         * because dek becomes smaller when fi is large, as ac is close to
         * zero Aries/Libra in that case.
         */
        sda = Math.acos(r) * SwissData.RADTODEG; /* semidiurnal arc, measured on equator */
        sna = 180 - sda;          /* complement, seminocturnal arc */
        sd3 = sda / 3;
        sn3 = sna / 3;
        rectasc = sl.swe_degnorm(th + sd3);            /* cusp 11 */
        /* project rectasc onto eclipitic with pole height 0, i.e. along the
        declination circle */
        hsp.cusp [11] = Asc1 (rectasc, 0, sine, cose);
        rectasc = sl.swe_degnorm(th + 2 * sd3);        /* cusp 12 */
        hsp.cusp [12] = Asc1 (rectasc, 0, sine, cose);
        rectasc = sl.swe_degnorm(th + 180 - 2 * sn3);  /* cusp 2 */
        hsp.cusp [2] = Asc1 (rectasc, 0, sine, cose);
        rectasc = sl.swe_degnorm(th + 180 -  sn3);     /* cusp 3 */
        hsp.cusp [3] = Asc1 (rectasc, 0, sine, cose);
        }
        break;
      case (int)'G': {   /* 36 Gauquelin sectors */
        for (i = 1; i <= 36; i++) {
          hsp.cusp[i] = 0;
        }
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          retc = SweConst.ERR;
          // goto porphyry;
          makePorphyry(hsp);
        }
        /*************** forth/second quarter ***************/
        /* note: Gauquelin sectors are counted in clockwise direction */
        a = asind(tand(fi) * tane);
        for (ih = 2; ih <= 9; ih++) {
          ih2 = 10 - ih;
          fh1 = atand(sind(a * ih2 / 9) / tane);
          rectasc = sl.swe_degnorm((90 / 9) * ih2 + th);
          tant = tand(asind(sine * sind(Asc1 (rectasc, fh1, sine, cose))));
          if (Math.abs(tant) < VERY_SMALL) {
            hsp.cusp[ih] = rectasc;
          } else {
            /* pole height */
            f = atand(sind(asind(tanfi * tant) * ih2 / 9)  /tant);
            hsp.cusp [ih] = Asc1 (rectasc, f, sine, cose);
            for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp[ih])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp[ih] = rectasc;
              break;
            }
            /* pole height */
            f = atand(sind(asind(tanfi * tant) * ih2 / 9) / tant);
            hsp.cusp[ih] = Asc1 (rectasc, f, sine, cose);
            }
          }
          hsp.cusp[ih+18] = sl.swe_degnorm(hsp.cusp[ih] + 180);
        }
        /*************** first/third quarter ***************/
        for (ih = 29; ih <= 36; ih++) {
          ih2 = ih - 28;
          fh1 = atand(sind(a * ih2 / 9) / tane);
          rectasc = sl.swe_degnorm(180 - ih2 * 90 / 9 + th);
          tant = tand(asind(sine * sind(Asc1 (rectasc, fh1, sine, cose))));
          if (Math.abs(tant) < VERY_SMALL) {
            hsp.cusp[ih] = rectasc;
          } else {
            f = atand(sind(asind(tanfi * tant) * ih2 / 9) / tant);
            /*  pole height */
            hsp.cusp[ih] = Asc1 (rectasc, f, sine, cose);
            for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp[ih])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp[ih] = rectasc;
              break;
            }
            f = atand(sind(asind(tanfi * tant) * ih2 / 9) / tant);
            /*  pole height */
            hsp.cusp[ih] = Asc1 (rectasc, f, sine, cose);
            }
          }
          hsp.cusp[ih-18] = sl.swe_degnorm(hsp.cusp[ih] + 180);
        }
        hsp.cusp[1] = hsp.ac;
        hsp.cusp[10] = hsp.mc;
        hsp.cusp[19] = sl.swe_degnorm(hsp.ac + 180);
        hsp.cusp[28] = sl.swe_degnorm(hsp.mc + 180);
        break;
        }
      case 'U': /* Krusinski */
        /*
         * The following code was written by Bogdan Krusinski in 2006.
         * bogdan@astrologia.pl
         *
         * Definition:
         * "Krusinski - house system based on the great circle passing through 
         * ascendant and zenith. This circle is divided into 12 equal parts 
         * (1st cusp is ascendent, 10th cusp is zenith), then the resulting 
         * points are projected onto the ecliptic through meridian circles.
         * The house cusps in space are half-circles perpendicular to the equator
         * and running from the north to the south celestial pole through the
         * resulting cusp points on the house circle. The points where they 
         * cross the ecliptic mark the ecliptic house cusps."
         *
         * Description of the algorithm:
         * Transform into great circle running through Asc and zenit (where arc 
         * between Asc and zenith is always 90 deg), and then return with 
         * house cusps into ecliptic. Eg. solve trigonometrical triangle 
         * with three transformations and two rotations starting from ecliptic. 
         * House cusps in space are meridian circles. 
         *
         * Notes:
         * 1. In this definition we assume MC on ecliptic as point where
         *    half-meridian (from north to south pole) cuts ecliptic,
         *    so MC may be below horizon in arctic regions.
         * 2. Houses could be calculated in all latitudes except the poles 
         *    themselves (-90,90) and points on arctic circle in cases where 
         *    ecliptic is equal to horizon and then ascendant is undefined. 
         *    But ascendant when 'horizon=ecliptic' could be deduced as limes 
         *    from both sides of that point and houses with that provision can 
         *    be computed also there.
         *
         * Starting values for calculations:
         *	   - Asc ecliptic longitude
         *	   - right ascension of MC (RAMC)
         *	   - geographic latitude.
         */
        /*
         * within polar circle we swap AC/DC if AC is on wrong side
         */
        acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
        if (acmc < 0) {
          hsp.ac = sl.swe_degnorm(hsp.ac + 180);
        }
        /* A0. Start point - ecliptic coords of ascendant */
        x[0] = hsp.ac; /* Asc longitude   */
        x[1] = 0.0;     /* Asc declination */
        x[2] = 1.0;     /* Radius to test validity of subsequent transformations. */
        sl.swe_cotrans(x, x, -ekl);      /* A1. Transform into equatorial coords */
        x[0] = x[0] - (th-90);        /* A2. Rotate                           */
        sl.swe_cotrans(x, x, -(90-fi));  /* A3. Transform into horizontal coords */
        krHorizonLon = x[0];          /* ...save asc lon on horizon to get back later with house cusp */
        x[0] = x[0] - x[0];           /* A4. Rotate                           */
        sl.swe_cotrans(x, x, -90);       /* A5. Transform into this house system great circle (asc-zenith) */
        /* As it is house circle now, simple add 30 deg increments... */
        for(i = 0; i < 6; i++) {
          /* B0. Set 'n-th' house cusp. 
           *     Note that IC/MC are also calculated here to check 
           *     if really this is the asc-zenith great circle. */
          x[0] = 30.0*i;
          x[1] = 0.0;
          sl.swe_cotrans(x, x, 90);                 /* B1. Transform back into horizontal coords */
          x[0] = x[0] + krHorizonLon;            /* B2. Rotate back.                          */
          sl.swe_cotrans(x, x, 90-fi);              /* B3. Transform back into equatorial coords */
          x[0] = sl.swe_degnorm(x[0] + (th-90));    /* B4. Rotate back -> RA of house cusp as result. */
          /* B5. Where's this house cusp on ecliptic? */
          /* ... so last but not least - get ecliptic longitude of house cusp: */
          hsp.cusp[i+1] = atand(tand(x[0])/cosd(ekl));
          if (x[0] > 90 && x[0] <= 270)
            hsp.cusp[i+1] = sl.swe_degnorm(hsp.cusp[i+1] + 180);
          hsp.cusp[i+1] = sl.swe_degnorm(hsp.cusp[i+1]);
          hsp.cusp[i+7] = sl.swe_degnorm(hsp.cusp[i+1]+180);
        }
        break;
      default:    /* Placidus houses */
        if (hsy != 'P') {
          System.err.println("swe_houses: make Placidus, unknown key "+hsy);
        }
        if (Math.abs(fi) >= 90 - ekl) {  /* within polar circle */
          retc = SweConst.ERR;
//          goto porphyry;
          makePorphyry(hsp);
          break;
        }
        a = asind(tand(fi) * tane);
        fh1 = atand(sind(a / 3) / tane);
        fh2 = atand(sind(a * 2 / 3) / tane);
        /* ************  house 11 ******************** */
        rectasc = sl.swe_degnorm(30 + th);
        tant = tand(asind(sine * sind(Asc1 (rectasc, fh1, sine, cose))));
        if (Math.abs(tant) < VERY_SMALL) {
          hsp.cusp [11] = rectasc;
        } else {
          /* pole height */
          f = atand(sind(asind(tanfi * tant) / 3)  /tant);
          hsp.cusp [11] = Asc1 (rectasc, f, sine, cose);
          for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp [11])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp [11] = rectasc;
              break;
            }
            /* pole height */
            f = atand(sind(asind(tanfi * tant) / 3) / tant);
            hsp.cusp [11] = Asc1 (rectasc, f, sine, cose);
          }
        }
        /* ************  house 12 ******************** */
        rectasc = sl.swe_degnorm(60 + th);
        tant = tand(asind(sine*sind(Asc1 (rectasc,  fh2, sine, cose))));
        if (Math.abs(tant) < VERY_SMALL) {
          hsp.cusp [12] = rectasc;
        } else {
          f = atand(sind(asind(tanfi * tant) / 1.5) / tant);
          /*  pole height */
          hsp.cusp [12] = Asc1 (rectasc, f, sine, cose);
          for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp [12])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp [12] = rectasc;
              break;
            }
            f = atand(sind(asind(tanfi * tant) / 1.5) / tant);
            /*  pole height */
            hsp.cusp [12] = Asc1 (rectasc, f, sine, cose);
          }
        }
        /* ************  house  2 ******************** */
        rectasc = sl.swe_degnorm(120 + th);
        tant = tand(asind(sine * sind(Asc1 (rectasc, fh2, sine, cose))));
        if (Math.abs(tant) < VERY_SMALL) {
          hsp.cusp [2] = rectasc;
        } else {
          f = atand(sind(asind(tanfi * tant) / 1.5) / tant);
          /*  pole height */
          hsp.cusp [2] = Asc1 (rectasc, f, sine, cose);
          for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp [2])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp [2] = rectasc;
              break;
            }
            f = atand(sind(asind(tanfi * tant) / 1.5) / tant);
            /*  pole height */
            hsp.cusp [2] = Asc1 (rectasc, f, sine, cose);
          }
        }
        /* ************  house  3 ******************** */
        rectasc = sl.swe_degnorm(150 + th);
        tant = tand(asind(sine * sind(Asc1 (rectasc, fh1, sine, cose))));
        if (Math.abs(tant) < VERY_SMALL) {
          hsp.cusp [3] = rectasc;
        } else {
          f = atand(sind(asind(tanfi * tant) / 3) / tant);
          /*  pole height */
          hsp.cusp [3] = Asc1(rectasc, f, sine, cose);
          for (i = 1; i <= iteration_count; i++) {
            tant = tand(asind(sine * sind(hsp.cusp [3])));
            if (Math.abs(tant) < VERY_SMALL) {
              hsp.cusp [3] = rectasc;
              break;
            }
            f = atand(sind(asind(tanfi * tant) / 3) / tant);
            /*  pole height */
            hsp.cusp [3] = Asc1 (rectasc, f, sine, cose);
          }
        }
        break;
    } /* end switch */
    if (hsy != 'G') {
      hsp.cusp [4] = sl.swe_degnorm(hsp.cusp [10] + 180);
      hsp.cusp [5] = sl.swe_degnorm(hsp.cusp [11] + 180);
      hsp.cusp [6] = sl.swe_degnorm(hsp.cusp [12] + 180);
      hsp.cusp [7] = sl.swe_degnorm(hsp.cusp [1] + 180);
      hsp.cusp [8] = sl.swe_degnorm(hsp.cusp [2] + 180);
      hsp.cusp [9] = sl.swe_degnorm(hsp.cusp [3] + 180);
    }
    /* vertex */
    if (fi >= 0) {
      f = 90 - fi;
    } else {
      f = -90 - fi;
    }
    hsp.vertex = Asc1 (th - 90, f, sine, cose);
    /* with tropical latitudes, the vertex behaves strange,
     * in a similar way as the ascendant within the polar
     * circle. we keep it always on the western hemisphere.*/
    if (Math.abs(fi) <= ekl) {
      vemc = sl.swe_difdeg2n(hsp.vertex, hsp.mc);
      if (vemc > 0) {
        hsp.vertex = sl.swe_degnorm(hsp.vertex + 180);
      }
    }
    /*
     * some strange points:
     */
    /* equasc (equatorial ascendant) */
    th2 = sl.swe_degnorm(th + 90);
    if (Math.abs(th2 - 90) > VERY_SMALL
      && Math.abs(th2 - 270) > VERY_SMALL) {
      tant = tand(th2);
      hsp.equasc = atand(tant / cose);
      if (th2 > 90 && th2 <= 270) {
        hsp.equasc = sl.swe_degnorm(hsp.equasc + 180);
      }
    } else {
      if (Math.abs(th2 - 90) <= VERY_SMALL) {
        hsp.equasc = 90;
      } else {
        hsp.equasc = 270;
      }
    } /*  if */
    hsp.equasc = sl.swe_degnorm(hsp.equasc);
    /* "co-ascendant" W. Koch */
    hsp.coasc1 = sl.swe_degnorm(Asc1 (th - 90, fi, sine, cose) + 180);
    /* "co-ascendant" M. Munkasey */
    if (fi >= 0) {
      hsp.coasc2 = Asc1 (th + 90, 90 - fi, sine, cose);
    } else /* southern hemisphere */ {
      hsp.coasc2 = Asc1 (th + 90, -90 - fi, sine, cose);
    }
    /* "polar ascendant" M. Munkasey */
    hsp.polasc = Asc1 (th - 90, fi, sine, cose);
    return retc;
  } /* procedure houses */

  /**
  * This is just a wrapping function to deal with the <CODE>goto</CODE>'s in
  * the original C-Code.
  */
  private void makePorphyry(Houses hsp) {
    /*
     * within polar circle we swap AC/DC if AC is on wrong side
     */
    double acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
    if (acmc < 0) {
      hsp.ac = sl.swe_degnorm(hsp.ac + 180);
      hsp.cusp[1] = hsp.ac;
      acmc = sl.swe_difdeg2n(hsp.ac, hsp.mc);
    }
    hsp.cusp [2] = sl.swe_degnorm(hsp.ac + (180 - acmc) / 3);
    hsp.cusp [3] = sl.swe_degnorm(hsp.ac + (180 - acmc) / 3 * 2);
    hsp.cusp [11] = sl.swe_degnorm(hsp.mc + acmc / 3);
    hsp.cusp [12] = sl.swe_degnorm(hsp.mc + acmc / 3 * 2);
  }

  /******************************/
  private double Asc1 (double x1, double f, double sine, double cose) {
    int n;
    double ass;
    x1 = sl.swe_degnorm(x1);
    n  = (int) ((x1 / 90) + 1);
    if (n == 1) {
      ass = ( Asc2 (x1, f, sine, cose));
    } else if (n == 2) {
      ass = (180 - Asc2 (180 - x1, - f, sine, cose));
    } else if (n == 3) {
      ass = (180 + Asc2 (x1 - 180, - f, sine, cose));
    } else {
      ass = (360 - Asc2 (360- x1,  f, sine, cose));
    }
    ass = sl.swe_degnorm(ass);
    if (Math.abs(ass - 90) < VERY_SMALL)        /* rounding, e.g.: if */ {
      ass = 90;                           /* fi = 0 & st = 0, ac = 89.999... */
    }
    if (Math.abs(ass - 180) < VERY_SMALL) {
      ass = 180;
    }
    if (Math.abs(ass - 270) < VERY_SMALL)        /* rounding, e.g.: if */ {
      ass = 270;                          /* fi = 0 & st = 0, ac = 89.999... */
    }
    if (Math.abs(ass - 360) < VERY_SMALL) {
      ass = 0;
    }
    return ass;
  }  /* Asc1 */

  private double Asc2 (double x, double f, double sine, double cose) {
    int n;
    double ass, sinx;
    ass = - tand(f) * sine + cose * cosd(x);
    if (Math.abs(ass) < VERY_SMALL) {
      ass = 0;
    }
    sinx = sind(x);
    if (Math.abs(sinx) < VERY_SMALL) {
      sinx = 0;
    }
    if (sinx == 0) {
      if (ass < 0) {
        ass = -VERY_SMALL;
      } else {
        ass = VERY_SMALL;
      }
    } else if (ass == 0) {
      if (sinx < 0) {
        ass = -90;
      } else {
        ass = 90;
      }
    } else {
      ass = atand(sinx / ass);
    }
    if (ass < 0) {
      ass = 180 + ass;
    }
    return (ass);
  } /* Asc2 */


  /* computes the house position of a planet or another point,
   * in degrees: 0 - 30 = 1st house, 30 - 60 = 2nd house, etc.
   * armc         sidereal time in degrees
   * geolat       geographic latitude
   * eps          true ecliptic obliquity
   * hsys         house system character
   * xpin         array of 6 doubles:
   *              only the first two of them are used: ecl. long., lat.
   * serr         error message area
   *
   * house position is returned by function.
   *
   * sidereal house positions:
   *
   * tropical and sidereal house positions of planets are always identical
   * if the traditional method of computing sidereal positions (subtracting
   * the ayanamsha from tropical in order to get sidereal positions) is used.
   *
   * if the sidereal plane is not identical to the ecliptic of date,
   * sidereal and tropical house positions are identical with
   * house systems that are independent on the ecliptic such as:
   * - Campanus
   * - Regiomontanus
   * - Placidus
   * - Azimuth/Horizon
   * - Axial rotation system
   * - "topocentric" system
   *
   * in all these cases call swe_house_pos() with TROPICAL planetary positions.
   *
   * but there are different house positions for ecliptic-dependent systems
   * such as:
   * - equal
   * - Porphyry
   * - Koch
<  * - Krusinski
   *
   * for these cases there is no function.
   */
  /**
  * The function returns a value between 1.0 and 12.999999, indicating in
  * which house a planet is and how far from its cusp it is. With Koch houses,
  * the function sometimes returns 0, if the computation was not possible.
  * @param armc The ARMC (sidereal time)
  * @param geolat The latitude
  * @param eps The ecliptic obliquity (e.g. xx[0] of swe_calc(...))
  * @param hsys The house system. See swe_houses(...) for a description
  * of the possible houses.
  * @param xpin A double[6] containing the ecliptic longitude and latitude of
  * the planet in degrees in xpin[0] and xpin[1]. The other xpin[] values are
  * not used, but the array has to be this size! The values must describe
  * tropical positions.
  * @param serr StringBuffer to contain any error messages or warnings
  * @return A value between 1.0 and 12.999999, indicating in which house a
  * planet is and how far from its cusp it is. Koch may return 0, if the
  * calculation was not possible.
  * #swe_houses
  */
  double swe_house_pos(double armc, double geolat, double eps,
                       int hsys, double xpin[], StringBuffer serr) {
    double xp[]=new double[6], xeq[]=new double[6], ra, de, mdd, mdn, sad, san;
    double hpos, sinad, ad, a, admc, adp, samc, demc, asc, mc, acmc, tant;
    double fh, ra0, tanfi, fac;
    double x[] = new double[3], xasc[] = new double[3], raep, raaz, oblaz, xtemp; /* BK 21.02.2006 */
    double sine = sind(eps);
    double cose = cosd(eps);
    boolean is_above_hor = false;
    if (serr != null) { serr.setLength(0); }
    hsys = Character.toUpperCase((char)hsys);
    xeq[0] = xpin[0];
    xeq[1] = xpin[1];
    xeq[2] = 1;
    sl.swe_cotrans(xpin, 0, xeq, 0, -eps);
    ra = xeq[0];
    de = xeq[1];
    mdd = sl.swe_degnorm(ra - armc);
    mdn = sl.swe_degnorm(mdd + 180);
    if (mdd >= 180) {
      mdd -= 360;
    }
    if (mdn >= 180) {
      mdn -= 360;
    }
    /* xp[0] will contain the house position, a value between 0 and 360 */
    switch(hsys) {
      case (int)'A':
      case (int)'E':
      case (int)'V':
        asc = Asc1 (sl.swe_degnorm(armc + 90), geolat, sine, cose);
        demc = atand(sind(armc) * tand(eps));
        if (geolat >= 0 && 90 - geolat + demc < 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        if (geolat < 0 && -90 - geolat + demc > 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        xp[0] = sl.swe_degnorm(xpin[0] - asc);
        if (hsys == 'V') {
          xp[0] = sl.swe_degnorm(xp[0] + 15);
        }
        /* to make sure that a call with a house cusp position returns
         * a value within the house, 0.001" is added */
        xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        hpos = xp[0] / 30.0 + 1;
      break;
      case 'O':
        asc = Asc1 (sl.swe_degnorm(armc + 90), geolat, sine, cose);
        demc = atand(sind(armc) * tand(eps));
        /* mc */
        if (Math.abs(armc - 90) > VERY_SMALL
                && Math.abs(armc - 270) > VERY_SMALL) {
          tant = tand(armc);
          mc = sl.swe_degnorm(atand(tant / cose));
          if (armc > 90 && armc <= 270) {
            mc = sl.swe_degnorm(mc + 180);
          }
        } else {
          if (Math.abs(armc - 90) <= VERY_SMALL) {
            mc = 90;
          } else {
            mc = 270;
          }
        }
        /* while MC is always south,
         * Asc must always be in eastern hemisphere */
        if (geolat >= 0 && 90 - geolat + demc < 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        if (geolat < 0 && -90 - geolat + demc > 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        xp[0] = sl.swe_degnorm(xpin[0] - asc);
        /* to make sure that a call with a house cusp position returns
         * a value within the house, 0.001" is added */
        xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        if (xp[0] < 180) {
          hpos = 1;
        } else {
          hpos = 7;
          xp[0] -= 180;
        }
        acmc = sl.swe_difdeg2n(asc, mc);
        if (xp[0] < 180 - acmc) {
          hpos += xp[0] * 3 / (180 - acmc);
        } else {
          hpos += 3 + (xp[0] - 180 + acmc) * 3 / acmc;
        }
      break;
      case 'X': /* Merdidian or axial rotation system */
        hpos = sl.swe_degnorm(mdd - 90) / 30.0 + 1;
      break;
      case (int)'M': { /* Morinus */
        double am = xpin[0];
        if (Math.abs(am - 90) > VERY_SMALL
          && Math.abs(am - 270) > VERY_SMALL) {
          tant = tand(am);
          hpos = atand(tant / cose);
          if (am > 90 && am <= 270) {
            hpos = sl.swe_degnorm(hpos + 180);
          }
        } else {
          if (Math.abs(am - 90) <= VERY_SMALL) {
            hpos = 90;
          } else {
            hpos = 270;
          }
        } /*  if */
        hpos = sl.swe_degnorm(hpos - armc - 90);
        hpos = hpos / 30.0 + 1;
      }
      break;
      case (int)'K':
       demc = atand(sind(armc) * tand(eps));
       /* if body is within circumpolar region, error */
       if (90 - Math.abs(geolat) <= Math.abs(de)) {
         if (serr != null) {
           serr.append("no Koch house position, because planet is circumpolar.");
         }
         xp[0] = 0;
         hpos = 0;        /* Error */
       } else if (90 - Math.abs(geolat) <= Math.abs(demc)) {
         if (serr != null) {
           serr.append("no Koch house position, because mc is circumpolar.");
         }
         xp[0] = 0;
         hpos = 0;        /* Error */
        } else {
          admc = asind(tand(eps) * tand(geolat) * sind(armc));
          adp = asind(tand(geolat) * tand(de));
            samc = 90 + admc;
          if (mdd >= 0) {        /* east */
            xp[0] = sl.swe_degnorm(((mdd - adp + admc) / samc - 1) * 90);
          } else {
            xp[0] = sl.swe_degnorm(((mdd + 180 + adp + admc) / samc + 1) * 90);
          }
          /* to make sure that a call with a house cusp position returns
           * a value within the house, 0.001" is added */
          xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
          hpos = xp[0] / 30.0 + 1;
        }
        break;
      case (int)'C':
        xeq[0] = sl.swe_degnorm(mdd - 90);
        sl.swe_cotrans(xeq, 0, xp, 0, -geolat);
        /* to make sure that a call with a house cusp position returns
         * a value within the house, 0.001" is added */
        xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        hpos = xp[0] / 30.0 + 1;
        break;
      case 'U': /* Krusinski */
        /* Purpose: find point where planet's house circle (meridian)
         * cuts house plane, giving exact planet's house position.
         * Input data: ramc, geolat, asc.
         */
        asc = Asc1 (sl.swe_degnorm(armc + 90), geolat, sine, cose);
        demc = atand(sind(armc) * tand(eps));
        /* while MC is always south, 
         * Asc must always be in eastern hemisphere */
        if (geolat >= 0 && 90 - geolat + demc < 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        if (geolat < 0 && -90 - geolat + demc > 0) {
          asc = sl.swe_degnorm(asc + 180);
        }
        /*
         * Descr: find the house plane 'asc-zenith' - where it intersects 
         * with equator and at what angle, and then simple find arc 
         * from asc on that plane to planet's meridian intersection 
         * with this plane.
         */
        /* I. find plane of 'asc-zenith' great circle relative to equator: 
         *   solve spherical triangle 'EP-asc-intersection of house circle with equator' */
        /* Ia. Find intersection of house plane with equator: */
        x[0] = asc; x[1] = 0.0; x[2] = 1.0;          /* 1. Start with ascendent on ecliptic     */
        sl.swe_cotrans(x, x, -eps);                     /* 2. Transform asc into equatorial coords */
        raep = sl.swe_degnorm(armc + 90);               /* 3. RA of east point                     */
        x[0] = sl.swe_degnorm(raep - x[0]);             /* 4. Rotation - found arc raas-raep      */
        sl.swe_cotrans(x, x, -(90-geolat));             /* 5. Transform into horizontal coords - arc EP-asc on horizon */
        xtemp = atand(tand(x[0])/cosd((90-geolat))); /* 6. Rotation from horizon on circle perpendicular to equator */
        if (x[0] > 90 && x[0] <= 270)
        xtemp = sl.swe_degnorm(xtemp + 180);
        x[0] = sl.swe_degnorm(xtemp);        
        raaz = sl.swe_degnorm(raep - x[0]); /* result: RA of intersection 'asc-zenith' great circle with equator */
        /* Ib. Find obliquity to equator of 'asc-zenith' house plane: */
        x[0] = raaz; x[1] = 0.0; 
        x[0] = sl.swe_degnorm(raep - x[0]);  /* 1. Rotate start point relative to EP   */
        sl.swe_cotrans(x, x, -(90-geolat));  /* 2. Transform into horizontal coords    */
        x[1] = x[1] + 90;                 /* 3. Add 90 deg do decl - so get the point on house plane most distant from equ. */
        sl.swe_cotrans(x, x, 90-geolat);     /* 4. Rotate back to equator              */
        oblaz = x[1];                     /* 5. Obliquity of house plane to equator */
        /* II. Next find asc and planet position on house plane, 
         *     so to find relative distance of planet from 
         *     coords beginning. */
        /* IIa. Asc on house plane relative to intersection 
         *      of equator with 'asc-zenith' plane. */
        xasc[0] = asc; xasc[1] = 0.0; xasc[2] = 1.0;
        sl.swe_cotrans(xasc, xasc, -eps);
        xasc[0] = sl.swe_degnorm(xasc[0] - raaz);
        xtemp = atand(tand(xasc[0])/cosd(oblaz));
        if (xasc[0] > 90 && xasc[0] <= 270)
        xtemp = sl.swe_degnorm(xtemp + 180);
        xasc[0] = sl.swe_degnorm(xtemp);
        /* IIb. Planet on house plane relative to intersection 
         *      of equator with 'asc-zenith' plane */
        xp[0] = sl.swe_degnorm(xeq[0] - raaz);        /* Rotate on equator  */
        xtemp = atand(tand(xp[0])/cosd(oblaz));    /* Find arc on house plane from equator */
        if (xp[0] > 90 && xp[0] <= 270)
          xtemp = sl.swe_degnorm(xtemp + 180);
        xp[0] = sl.swe_degnorm(xtemp);
        xp[0] = sl.swe_degnorm(xp[0]-xasc[0]); /* find arc between asc and planet, and get planet house position  */
        /* IIc. Distance from planet to house plane on declination circle: */
        x[0] = xeq[0];
        x[1] = xeq[1];
        sl.swe_cotrans(x, x, oblaz);
        xp[1] = xeq[1] - x[1]; /* How many degrees is the point on declination circle from house circle */
        /* to make sure that a call with a house cusp position returns
         * a value within the house, 0.001" is added */
        xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        hpos = xp[0] / 30.0 + 1;
        break;
      case (int)'H':
        xeq[0] = sl.swe_degnorm(mdd - 90);
        sl.swe_cotrans(xeq, 0, xp, 0, 90 - geolat);
        /* to make sure that a call with a house cusp position returns
         * a value within the house, 0.001" is added */
        xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        hpos = xp[0] / 30.0 + 1;
        break;
      case (int)'R':
        if (Math.abs(mdd) < VERY_SMALL) {
          xp[0] = 270;
        } else if (180 - Math.abs(mdd) < VERY_SMALL) {
          xp[0] = 90;
        } else {
          if (90 - Math.abs(geolat) < VERY_SMALL) {
            if (geolat > 0) {
              geolat = 90 - VERY_SMALL;
            } else {
              geolat = -90 + VERY_SMALL;
            }
          }
          if (90 - Math.abs(de) < VERY_SMALL) {
            if (de > 0) {
              de = 90 - VERY_SMALL;
            } else {
              de = -90 + VERY_SMALL;
            }
          }
          a = tand(geolat) * tand(de) + cosd(mdd);
          xp[0] = sl.swe_degnorm(atand(-a / sind(mdd)));
          if (mdd < 0) {
            xp[0] += 180;
          }
          xp[0] = sl.swe_degnorm(xp[0]);
          /* to make sure that a call with a house cusp position returns
           * a value within the house, 0.001" is added */
          xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        }
        hpos = xp[0] / 30.0 + 1;
        break;
      case (int)'T':
        mdd = sl.swe_degnorm(mdd);
        if (de > 90 - VERY_SMALL) {
          de = 90 - VERY_SMALL;
        }
        if (de < -90 + VERY_SMALL) {
          de = -90 + VERY_SMALL;
        }
        sinad = tand(de) * tand(geolat);
        ad = asind(sinad);
        a = sinad + cosd(mdd);
        if (a >= 0) {
          is_above_hor = true;
        }
        /* mirror everything below the horizon to the opposite point
         * above the horizon */
        if (!is_above_hor) {
          ra = sl.swe_degnorm(ra + 180);
          de = -de;
          mdd = sl.swe_degnorm(mdd + 180);
        }
        /* mirror everything on western hemisphere to eastern hemisphere */
        if (mdd > 180) {
          ra = sl.swe_degnorm(armc - mdd);
        }
        /* binary search for "topocentric" position line of body */
        tanfi = tand(geolat);
        fh = geolat;
        ra0 = sl.swe_degnorm(armc + 90);
        xp[1] = 1;
        xeq[1] = de;
        fac = 2;
        while (Math.abs(xp[1]) > 0.000001) {
          if (xp[1] > 0) {
            fh = atand(tand(fh) - tanfi / fac);
            ra0 -= 90 / fac;
          } else {
            fh = atand(tand(fh) + tanfi / fac);
            ra0 += 90 / fac;
          }
          xeq[0] = sl.swe_degnorm(ra - ra0);
          sl.swe_cotrans(xeq, 0, xp, 0, 90 - fh);
          fac *= 2;
        }
        hpos = sl.swe_degnorm(ra0 - armc);
        /* mirror back to west */
        if (mdd > 180) {
          hpos = sl.swe_degnorm(-hpos);
        }
        /* mirror back to below horizon */
        if (!is_above_hor) {
          hpos = sl.swe_degnorm(hpos + 180);
        }
        hpos = sl.swe_degnorm(hpos - 90) / 30 + 1;
        break;
      case (int)'P':
      case (int)'G':
      default:
         /* circumpolar region */
        if (90 - Math.abs(de) <= Math.abs(geolat)) {
          if (de * geolat < 0) {
            xp[0] = sl.swe_degnorm(90 + mdn / 2);
          } else {
            xp[0] = sl.swe_degnorm(270 + mdd / 2);
          }
          if (serr != null) {
            serr.append("Otto Ludwig procedure within circumpolar regions.");
          }
        } else {
          sinad = tand(de) * tand(geolat);
          ad = asind(sinad);
          a = sinad + cosd(mdd);
          if (a >= 0) {
            is_above_hor = true;
          }
          sad = 90 + ad;
          san = 90 - ad;
          if (is_above_hor) {
            xp[0] =  (mdd / sad + 3) * 90;
          } else {
            xp[0] = (mdn / san + 1) * 90;
          }
          /* to make sure that a call with a house cusp position returns
           * a value within the house, 0.001" is added */
          xp[0] = sl.swe_degnorm(xp[0] + MILLIARCSEC);
        }
        if ((char)hsys == 'G') {
          xp[0] = 360 - xp[0]; /* Gauquelin sectors are in clockwise direction */
          hpos = xp[0] / 10.0 + 1;
        } else {
          hpos = xp[0] / 30.0 + 1;
        }
      break;
    }
    return hpos;
  }
}
