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

class SwephJPL {
  static final int J_MERCURY =  0;
  static final int J_VENUS   =  1;
  static final int J_EARTH   =  2;
  static final int J_MARS    =  3;
  static final int J_JUPITER =  4;
  static final int J_SATURN  =  5;
  static final int J_URANUS  =  6;
  static final int J_NEPTUNE =  7;
  static final int J_PLUTO   =  8;
  static final int J_MOON    =  9;
  static final int J_SUN     = 10;
  static final int J_SBARY   = 11;
  static final int J_EMB     = 12;
  static final int J_NUT     = 13;
  static final int J_LIB     = 14;

  JplSave js=new JplSave();


  SwissEph  sw=null;
  SwissData swed=null;
  SwissLib  sl=null;



  SwephJPL(SwissEph sw, SwissData swed, SwissLib sl) {
    this.sw=sw;
    this.swed=swed;
    this.sl=sl;
    if (this.sw==null) { this.sw=new SwissEph(); }
    if (this.swed==null) { this.swed=new SwissData(); }
    if (this.sl==null) { this.sl=new SwissLib(); }
  }


  /*
   * This subroutine opens the file jplfname, with a phony record length,
   * reads the first record, and uses the info to compute ksize,
   * the number of single precision words in a record.
   * RETURN: ksize (record size of ephemeris data)
   * jplfptr is opened on return.
   */
  private int fsizer(StringBuffer serr) throws SwissephException {
    /* Local variables */
    int ncon;
    double emrat;
    int numde;
    double au, ss[]=new double[3];
    int i;
    int ksize;
    String ttl="";  // JAVA: Not used???
    try {
      // throws SwissephException, if null or maybe for other reasons:
      js.jplfptr = sw.swi_fopen(SwephData.SEI_FILE_PLANET, js.jplfname, js.jplfpath, serr);
      /* ttl = ephemeris title, e.g.
       * "JPL Planetary Ephemeris DE404/LE404
       *  Start Epoch: JED=   625296.5-3001 DEC 21 00:00:00
       *  Final Epoch: JED=  2817168.5 3001 JAN 17 00:00:00c */
//    fread((void *) &ttl[0], 1, 252, js->jplfptr);
      for(int m=0; m<252; m++) {
        ttl+=(char)js.jplfptr.readByte();
      }
    /* cnam = names of constants */
//    fread((void *) js->ch_cnam, 1, 6*400, js->jplfptr);
      for(int m=0; m<6*400; m++) {
        ttl+=(char)js.jplfptr.readByte();
      }
      /* ss[0] = start epoch of ephemeris
       * ss[1] = end epoch
       * ss[2] = segment size in days */
//    fread((void *) &ss[0], sizeof(double), 3, js->jplfptr);
      for(int m=0; m<3; m++) {
        ss[m]=js.jplfptr.readDouble();
      }
// Don't mind "reorder"...
//    /* reorder ? */
//    if (ss[2] < 1 || ss[2] > 200)
//      js.do_reorder = true;
//    else
//      js.do_reorder = false;
      for (i = 0; i < 3; i++)
        js.eh_ss[i] = ss[i];
//    if (js.do_reorder)
//      reorder((char *) &js->eh_ss[0], sizeof(double), 3);
      /* ncon = number of constants */
//    fread((void *) &ncon, sizeof(long), 1, js->jplfptr);
        ncon=js.jplfptr.readInt();
//    if (js->do_reorder)
//      reorder((char *) &ncon, sizeof(long), 1);
      /* au = astronomical unit */
//    fread((void *) &au, sizeof(double), 1, js->jplfptr);
      au=js.jplfptr.readDouble();
//    if (js->do_reorder)
//      reorder((char *) &au, sizeof(double), 1);
      /* emrat = earth moon mass ratio */
//    fread((void *) &emrat, sizeof(double), 1, js->jplfptr);
      emrat=js.jplfptr.readDouble();
//    if (js->do_reorder)
//      reorder((char *) &emrat, sizeof(double), 1);
      /* ipt[i+0]: coefficients of planet i start at buf[ipt[i+0]-1]
       * ipt[i+1]: number of coefficients (interpolation order - 1)
       * ipt[i+2]: number of intervals in segment */
//    fread((void *) &js->eh_ipt[0], sizeof(long), 36, js->jplfptr);
      for(int m=0; m<36; m++) {
        js.eh_ipt[m]=js.jplfptr.readInt();
      }
//    if (js->do_reorder)
//      reorder((char *) &js->eh_ipt[0], sizeof(long), 36);
      /* numde = number of jpl ephemeris "404" with de404 */
//    fread((void *) &numde, sizeof(long), 1, js->jplfptr);
        numde=js.jplfptr.readInt();
//    if (js->do_reorder)
//      reorder((char *) &numde, sizeof(long), 1);
      js.jplfptr.seek(0);
      /*  find the number of ephemeris coefficients from the pointers */
    } catch (java.io.IOException ioe) {
      throw new SwissephException(1./0., SwissephException.FILE_READ_ERROR,
          SweConst.ERR, ioe.getMessage());
    }
    switch (numde) {
      case 403:
      case 405:
        ksize = 2036;
        break;
      case 404:
      case 406:
        ksize = 1456;
        break;
      case 200:
        ksize = 1652;
        break;
      case 102:
        ksize = 1652;     /* de102 is filled with blanks to length of de200 */
        break;
      default:
        if (serr != null) {
          serr.setLength(0);
          serr.append("unknown numde value "+numde+";");
        }
        throw new SwissephException(1./0., SwissephException.DATA_FILE_ERROR,
            SweConst.ERR, serr);
    }
    return ksize;
  }

  /*
   *     This subroutine reads the jpl planetary ephemeris
   *     and gives the position and velocity of the point 'ntarg'
   *     with respect to 'ncent'.
   *     calling sequence parameters:
   *       et = d.p. julian ephemeris date at which interpolation
   *            is wanted.
   *       ** note the entry dpleph for a doubly-dimensioned time **
   *          the reason for this option is discussed in the
   *          subroutine state
   *     ntarg = integer number of 'target' point.
   *     ncent = integer number of center point.
   *            the numbering convention for 'ntarg' and 'ncent' is:
   *                0 = mercury           7 = neptune
   *                1 = venus             8 = pluto
   *                2 = earth             9 = moon
   *                3 = mars             10 = sun
   *                4 = jupiter          11 = solar-system barycenter
   *                5 = saturn           12 = earth-moon barycenter
   *                6 = uranus           13 = nutations (longitude and obliq)
   *                                     14 = librations, if on eph file
   *             (if nutations are wanted, set ntarg = 13. for librations,
   *              set ntarg = 14. set ncent=0.)
   *      rrd = output 6-word d.p. array containing position and velocity
   *            of point 'ntarg' relative to 'ncent'. the units are au and
   *            au/day. for librations the units are radians and radians
   *            per day. in the case of nutations the first four words of
   *            rrd will be set to nutations and rates, having units of
   *            radians and radians/day.
   *            The option is available to have the units in km and km/sec.
   *            For this, set do_km=TRUE (default FALSE).
   */
  int swi_pleph(double et, int ntarg, int ncent, double[] rrd,
                StringBuffer serr) throws SwissephException {
    int i, retc;
    int list[]=new int[12];
    double[] pv = js.pv;
    double[] pvsun = js.pvsun;
    for (i = 0; i < 6; ++i)
      rrd[i] = 0.0;
    if (ntarg == ncent) {
      return 0;
    }
    for (i = 0; i < 12; ++i)
      list[i] = 0;
    /*     check for nutation call */
    if (ntarg == J_NUT) {
      if (js.eh_ipt[34] > 0) {
        list[10] = 2;
        return(state(et, list, false, pv, pvsun, rrd, serr));
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("No nutations on the JPL ephemeris file;");
        }
        throw new SwissephException(et, SwissephException.UNDEFINED,
            SwephData.NOT_AVAILABLE, serr);
      }
    }
    if (ntarg == J_LIB) {
      if (js.eh_ipt[37] > 0) {
        list[11] = 2;
        // throws SwissephException by itself:
        retc = state(et, list, false, pv, pvsun, rrd, serr);
        for (i = 0; i < 6; ++i) {
          rrd[i] = pv[i + 60];
        }
        return 0;
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("No librations on the ephemeris file;");
        }
        throw new SwissephException(et, SwissephException.DATA_FILE_ERROR,
            SwephData.NOT_AVAILABLE, serr);
      }
    }
    /* set up proper entries in 'list' array for state call */
    if (ntarg < J_SUN) {
      list[ntarg] = 2;
    }
    if (ntarg == J_MOON)  /* Mooon needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ntarg == J_EARTH)         /* Earth needs Moon */ {
      list[J_MOON] = 2;
    }
    if (ntarg == J_EMB)   /* EMB needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ncent < J_SUN) {
      list[ncent] = 2;
    }
    if (ncent == J_MOON)  /* Mooon needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ncent == J_EARTH)         /* Earth needs Moon */ {
      list[J_MOON] = 2;
    }
    if (ncent == J_EMB)   /* EMB needs Earth */ {
      list[J_EARTH] = 2;
    }
    // throws SwissephException by itself:
    retc = state(et, list, true, pv, pvsun, rrd, serr);
    if (ntarg == J_SUN || ncent == J_SUN) {
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_SUN] = pvsun[i];
    }
    if (ntarg == J_SBARY || ncent == J_SBARY) {
      for (i = 0; i < 6; ++i) {
        pv[i + 6*J_SBARY] = 0.;
      }
    }
    if (ntarg == J_EMB || ncent == J_EMB) {
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_EMB] = pv[i + 6*J_EARTH];
    }
    if ((ntarg==J_EARTH && ncent==J_MOON) || (ntarg == J_MOON && ncent==J_EARTH)){
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_EARTH] = 0.;

    } else {
      if (list[J_EARTH] == 2) {
        for (i = 0; i < 6; ++i)
          pv[i + 6*J_EARTH] -= pv[i + 6*J_MOON] / (js.eh_emrat + 1.);
      }
      if (list[J_MOON] == 2) {
        for (i = 0; i < 6; ++i) {
          pv[i + 6*J_MOON] += pv[i + 6*J_EARTH];
        }
      }
    }
    for (i = 0; i < 6; ++i)
      rrd[i] = pv[i + ntarg * 6] - pv[i + ncent * 6];
    return SweConst.OK;
  }

  /*
   *  This subroutine differentiates and interpolates a
   *  set of chebyshev coefficients to give pos, vel, acc, and jerk
   *  calling sequence parameters:
   *    input:
   *     buf   1st location of array of d.p. chebyshev coefficients of position
   *        t   is dp fractional time in interval covered by
   *            coefficients at which interpolation is wanted, 0 <= t <= 1
   *     intv   is dp length of whole interval in input time units.
   *      ncf   number of coefficients per component
   *      ncm   number of components per set of coefficients
   *       na   number of sets of coefficients in full array
   *            (i.e., number of sub-intervals in full interval)
   *       ifl   int flag: =1 for positions only
   *                      =2 for pos and vel
   *                      =3 for pos, vel, and acc
   *                      =4 for pos, vel, acc, and jerk
   *    output:
   *      pv   d.p. interpolated quantities requested.
   *           assumed dimension is pv(ncm,fl).
   */
  /* Initialized data */
  int np_interp, nv_interp;
  int nac_interp;
  int njk_interp;
  double twot = 0.;
  private int interp(double[] buf, int bufOffs, double t, double intv,
                     int ncfin, int ncmin, int nain, int ifl, double[] pv,
                     int pvOffs) {
    /* Initialized data */
    double[] pc = js.pc;
    double[] vc = js.vc;
    double[] ac = js.ac;
    double[] jc = js.jc;
    int ncf = (int) ncfin;
    int ncm = (int) ncmin;
    int na = (int) nain;
    /* Local variables */
    double temp;
    int i, j, ni;
    double tc;
    double dt1, bma;
    double bma2, bma3;
    /*
     | get correct sub-interval number for this set of coefficients and then
     | get normalized chebyshev time within that subinterval.
     */
    if (t >= 0) {
      dt1 = Math.floor(t);
    } else {
      dt1 = -Math.floor(-t);
    }
    temp = na * t;
    ni = (int) (temp - dt1);
    /* tc is the normalized chebyshev time (-1 <= tc <= 1) */
    tc = ((temp % 1.0) + dt1) * 2. - 1.;
    /*
     *  check to see whether chebyshev time has changed,
     *  and compute new polynomial values if it has.
     *  (the element pc(2) is the value of t1(tc) and hence
     *  contains the value of tc on the previous call.)
     */
    if (tc != pc[1]) {
      np_interp = 2;
      nv_interp = 3;
      nac_interp = 4;
      njk_interp = 5;
      pc[1] = tc;
      twot = tc + tc;
    }
    /*
     *  be sure that at least 'ncf' polynomials have been evaluated
     *  and are stored in the array 'pc'.
     */
    if (np_interp < ncf) {
      for (i = np_interp; i < ncf; ++i)
        pc[i] = twot * pc[i - 1] - pc[i - 2];
      np_interp = ncf;
    }
    /*  interpolate to get position for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i] = 0.;
      for (j = ncf-1; j >= 0; --j)
        pv[pvOffs + i] += pc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
    }
    if (ifl <= 1) {
      return 0;
    }
    /*
     *       if velocity interpolation is wanted, be sure enough
     *       derivative polynomials have been generated and stored.
     */
    bma = (na + na) / intv;
    vc[2] = twot + twot;
    if (nv_interp < ncf) {
      for (i = nv_interp; i < ncf; ++i)
        vc[i] = twot * vc[i - 1] + pc[i - 1] + pc[i - 1] - vc[i - 2];
      nv_interp = ncf;
    }
    /*       interpolate to get velocity for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm] = 0.;
      for (j = ncf-1; j >= 1; --j)
        pv[pvOffs + i + ncm] += vc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm] *= bma;
    }
    if (ifl == 2) {
      return 0;
    }
    /*       check acceleration polynomial values, and */
    /*       re-do if necessary */
    bma2 = bma * bma;
    ac[3] = pc[1] * 24.;
    if (nac_interp < ncf) {
      nac_interp = ncf;
      for (i = nac_interp; i < ncf; ++i)
        ac[i] = twot * ac[i - 1] + vc[i - 1] * 4. - ac[i - 2];
    }
    /*       get acceleration for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm * 2] = 0.;
      for (j = ncf-1; j >= 2; --j)
        pv[pvOffs + i + ncm * 2] += ac[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm * 2] *= bma2;
    }
    if (ifl == 3) {
      return 0;
    }
    /*       check jerk polynomial values, and */
    /*       re-do if necessary */
    bma3 = bma * bma2;
    jc[4] = pc[1] * 192.;
    if (njk_interp < ncf) {
      njk_interp = ncf;
      for (i = njk_interp; i < ncf; ++i)
        jc[i] = twot * jc[i - 1] + ac[i - 1] * 6. - jc[i - 2];
    }
    /*       get jerk for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm * 3] = 0.;
      for (j = ncf-1; j >= 3; --j)
        pv[pvOffs + i + ncm * 3] += jc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm * 3] *= bma3;
    }
    return 0;
  }

  /*
   | ********** state ********************
   | this subroutine reads and interpolates the jpl planetary ephemeris file
   |  calling sequence parameters:
   |  input:
   |     et    dp julian ephemeris epoch at which interpolation is wanted.
   |     list  12-word integer array specifying what interpolation
   |           is wanted for each of the bodies on the file.
   |                      list(i)=0, no interpolation for body i
   |                             =1, position only
   |                             =2, position and velocity
   |            the designation of the astronomical bodies by i is:
   |                      i = 0: mercury
   |                        = 1: venus
   |                        = 2: earth-moon barycenter, NOT earth!
   |                        = 3: mars
   |                        = 4: jupiter
   |                        = 5: saturn
   |                        = 6: uranus
   |                        = 7: neptune
   |                        = 8: pluto
   |                        = 9: geocentric moon
   |                        =10: nutations in longitude and obliquity
   |                        =11: lunar librations (if on file)
   |            If called with list = NULL, only the header records are read and
   |            stored in the global areas.
   |  do_bary   short, if true, barycentric, if false, heliocentric.
   |              only the 9 planets 0..8 are affected by it.
   |  output:
   |       pv   dp 6 x 11 array that will contain requested interpolated
   |            quantities.  the body specified by list(i) will have its
   |            state in the array starting at pv(1,i).  (on any given
   |            call, only those words in 'pv' which are affected by the
   |            first 10 'list' entries (and by list(11) if librations are
   |            on the file) are set.  the rest of the 'pv' array
   |            is untouched.)  the order of components starting in
   |            pv is: x,y,z,dx,dy,dz.
   |            all output vectors are referenced to the earth mean
   |            equator and equinox of epoch. the moon state is always
   |            geocentric; the other nine states are either heliocentric
   |            or solar-system barycentric, depending on the setting of
   |            common flags (see below).
   |            lunar librations, if on file, are put into pv(k,10) if
   |            list(11) is 1 or 2.
   |    pvsun   dp 6-word array containing the barycentric position and
   |            velocity of the sun.
   |      nut   dp 4-word array that will contain nutations and rates,
   |            depending on the setting of list(10).  the order of
   |            quantities in nut is:
   |                     d psi  (nutation in longitude)
   |                     d epsilon (nutation in obliquity)
   |                     d psi dot
   |                     d epsilon dot
   |  globals used:
   |    do_km   logical flag defining physical units of the output states.
   |            TRUE = return km and km/sec, FALSE = return au and au/day
   |            default value = FALSE  (km determines time unit
   |            for nutations and librations.  angle unit is always radians.)
   */
  int irecsz_state;
  int nrl_state, lpt_state[]=new int[3], ncoeffs_state;
  private int state(double et, int[] list, boolean do_bary, double[] pv,
                    double[] pvsun, double[] nut, StringBuffer serr)
      throws SwissephException {
    int i, j, k;
    long flen;
    int nseg, nb;
    double[] buf = js.buf;
    double aufac=0., s, t=0., intv=0.;
    int nrecl, ksize;
    int nr;
    double et_mn, et_fr;
    int[] ipt = js.eh_ipt;
    String ch_ttl="";  // JAVA: Not used???
    boolean ferr=false;
    try {
      if (js.jplfptr == null ||
          (js.jplfptr.fp == null && js.jplfptr.sk == null)) {
        // fsizer() throws SwissephException
        ksize = fsizer(serr); /* the number of single precision words in a record */
        nrecl = 4;
        if (ksize < 0) {
          if (serr != null && ksize != SwephData.NOT_AVAILABLE) {
            serr.setLength(0);
            serr.append("fsizer does not work");/**/
          }
          throw new SwissephException(et, SwissephException.DATA_FILE_ERROR,  // Or is it some other error???
              (int)ksize, serr);
        }
        irecsz_state = nrecl * ksize;     /* record size in bytes */
        ncoeffs_state = ksize / 2;        /* # of coefficients, doubles */
        /* ttl = ephemeris title, e.g.
         * "JPL Planetary Ephemeris DE404/LE404
         *  Start Epoch: JED=   625296.5-3001 DEC 21 00:00:00
         *  Final Epoch: JED=  2817168.5 3001 JAN 17 00:00:00c */
//      fread((void *) &ch_ttl[0], 1, 252, js.jplfptr);
        for(int m=0;m<252;m++) {
          ch_ttl+=(char)js.jplfptr.readByte();
        }
        /* cnam = names of constants */
//      fread((void *) js.ch_cnam, 1, 2400, js.jplfptr);
        for(int m=0;m<2400;m++) {
          js.ch_cnam+=(char)js.jplfptr.readByte();
        }
        /* ss[0] = start epoch of ephemeris
         * ss[1] = end epoch
         * ss[2] = segment size in days */
//      fread((void *) &js.eh_ss[0], sizeof(double), 3, js.jplfptr);
        for(int m=0;m<3;m++) {
          js.eh_ss[m]=js.jplfptr.readDouble();
        }
//      if (js.do_reorder)
//        reorder((char *) &js.eh_ss[0], sizeof(double), 3);
        /* ncon = number of constants */
//      fread((void *) &js.eh_ncon, sizeof(long), 1, js.jplfptr);
        js.eh_ncon=js.jplfptr.readInt();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_ncon, sizeof(long), 1);
        /* au = astronomical unit */
//      fread((void *) &js.eh_au, sizeof(double), 1, js.jplfptr);
        js.eh_au=js.jplfptr.readDouble();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_au, sizeof(double), 1);
        /* emrat = earth moon mass ratio */
//      fread((void *) &js.eh_emrat, sizeof(double), 1, js.jplfptr);
        js.eh_emrat=js.jplfptr.readDouble();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_emrat, sizeof(double), 1);
        /* ipt[i+0]: coefficients of planet i start at buf[ipt[i+0]-1]
         * ipt[i+1]: number of coefficients (interpolation order - 1)
         * ipt[i+2]: number of intervals in segment */
//      fread((void *) &ipt[0], sizeof(long), 36, js.jplfptr);
        for(int m=0;m<36;m++) {
          ipt[m]=js.jplfptr.readInt();
        }
//      if (js.do_reorder)
//        reorder((char *) &ipt[0], sizeof(long), 36);
        /* numde = number of jpl ephemeris "404" with de404 */
//      fread((void *) &js.eh_denum, sizeof(long), 1, js.jplfptr);
        js.eh_denum=js.jplfptr.readInt();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_denum, sizeof(long), 1);
//      fread((void *) &lpt[0], sizeof(long), 3, js.jplfptr);
        for(int m=0;m<3;m++) {
          lpt_state[m]=js.jplfptr.readInt();
        }
//      if (js.do_reorder)
//        reorder((char *) &lpt[0], sizeof(long), 3);
        /* cval[]:  other constants in next record */
//      fseek(js.jplfptr, 1L * irecsz, 0);
        js.jplfptr.seek(1L * irecsz_state);
//      fread((void *) &js.eh_cval[0], sizeof(double), 400, js.jplfptr);
        for(int m=0;m<400;m++) {
          js.eh_cval[m]=js.jplfptr.readDouble();
        }
//      if (js.do_reorder)
//        reorder((char *) &js.eh_cval[0], sizeof(double), 400);
        js.jplfptr.seek(2L * irecsz_state);
        for (i = 0; i < 3; ++i)
          ipt[i + 36] = lpt_state[i];
        nrl_state = 0;
        /* is file length correct? */
        /* file length */
//      fseek(js.jplfptr, 0L, SEEK_END);
//      flen = ftell(js.jplfptr);
        flen=js.jplfptr.length();
        /* # of segments in file */
        nseg = (int) ((js.eh_ss[1] - js.eh_ss[0]) / js.eh_ss[2]);
        /* sum of all cheby coeffs of all planets and segments */
        for(i = 0, nb = 0; i < 13; i++) {
          k = 3;
          if (i == 11) {
            k = 2;
          }
          nb += (ipt[i*3+1] * ipt[i*3+2]) * k * nseg;
        }
        /* add start and end epochs of segments */
        nb += 2 * nseg;
        /* doubles to bytes */
        nb *= 8;
        /* add size of header and constants section */
        nb += 2 * ksize * nrecl;
        if (flen != nb
          /* some of our files are one record too long */
          && flen - nb != ksize * nrecl) {
          if (serr != null) {
            serr.setLength(0);
            serr.append("JPL ephemeris file is mutilated; length = "+flen+" instead of "+nb+".");
            if (serr.length() + js.jplfname.length() < SwissData.AS_MAXCH - 1) {
              serr.setLength(0); // Nanu???
              serr.append("JPL ephemeris file "+js.jplfname+" is mutilated; length = "+flen+" instead of "+nb+".");
            }
          }
          throw new SwissephException(et, SwissephException.FILE_READ_ERROR,
              SweConst.ERR, serr);
        }
      }
      if (list == null) {
        return 0;
      }
      s = et - .5;
      et_mn = Math.floor(s);
      et_fr = s - et_mn;    /* fraction of days since previous midnight */
      et_mn += .5;  /* midnight before epoch */
      /*       error return for epoch out of range */
      if (et < js.eh_ss[0] || et > js.eh_ss[1]) {
        if (serr != null) {
          serr.setLength(0);
          serr.append("jd "+et+" outside JPL eph. range "+js.eh_ss[0]+" .. "+js.eh_ss[1]+";");
        }
        throw new SwissephException(et, SwissephException.OUT_OF_TIME_RANGE,
            SwephData.BEYOND_EPH_LIMITS, serr);
      }
      /*       calculate record # and relative time in interval */
      nr = (int) ((et_mn - js.eh_ss[0]) / js.eh_ss[2]) + 2;
      if (et_mn == js.eh_ss[1]) {
        --nr;       /* end point of ephemeris, use last record */
      }
      t = (et_mn - ((nr - 2) * js.eh_ss[2] + js.eh_ss[0]) + et_fr) / js.eh_ss[2];
      /* read correct record if not in core */
      if (nr != nrl_state) {
        nrl_state = nr;
        js.jplfptr.seek(nr * irecsz_state);
        for (k = 1; k <= ncoeffs_state; ++k) {
//        if ( fread((void *) &buf[k - 1], sizeof(double), 1, js.jplfptr) != 1) {

          buf[k - 1]=js.jplfptr.readDouble();
//        }
//        if (js.do_reorder)
//          reorder((char *) &buf[k-1], sizeof(double), 1);
        }
      }
      if (js.do_km) {
        intv = js.eh_ss[2] * 86400.;
        aufac = 1.;
      } else {
        intv = js.eh_ss[2];
        aufac = 1. / js.eh_au;
      }
      /*   interpolate ssbary sun */
    } catch (java.io.EOFException ef) {
      ferr=true;
    } catch (java.io.IOException ie) {
      ferr=true;
    }
    if (ferr) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("Read error in JPL eph. at "+et+"\n");
      }
      throw new SwissephException(et, SwissephException.FILE_READ_ERROR,
          SweConst.ERR, serr);
    }
    interp(buf, (int) ipt[30] - 1, t, intv, ipt[31], 3, ipt[32], 2, pvsun, 0);
    for (i = 0; i < 6; ++i) {
      pvsun[i] *= aufac;
    }
    /*   check and interpolate whichever bodies are requested */
    for (i = 0; i < 10; ++i) {
      if (list[i] > 0) {
        interp(buf, (int) ipt[i * 3] - 1, t, intv, ipt[i * 3 + 1], 3,
               ipt[i * 3 + 2], list[i], pv, i * 6);
        for (j = 0; j < 6; ++j) {
          if (i < 9 && ! do_bary) {
            pv[j + i * 6] = pv[j + i * 6] * aufac - pvsun[j];
          } else {
            pv[j + i * 6] *= aufac;
          }
        }
      }
    }
    /*       do nutations if requested (and if on file) */
    if (list[10] > 0 && ipt[34] > 0) {
      interp(buf, (int) ipt[33] - 1, t, intv, ipt[34], 2, ipt[35],
               list[10], nut, 0);
    }
    /*       get librations if requested (and if on file) */
    if (list[11] > 0 && ipt[37] > 0) {
      interp(buf, (int) ipt[36] - 1, t, intv, ipt[37], 3, ipt[38], list[1],
              pv, 60);
    }
    return SweConst.OK;
  }

  /*
   *  this entry obtains the constants from the ephemeris file
   *  call state to initialize the ephemeris and read in the constants
   */
  private int read_const_jpl(double[] ss,  StringBuffer serr) throws SwissephException {
    int i;
    // throws SwissephException if !SweConst.OK:
    state(0.0, null, false, null, null, null, serr);

    for (i = 0; i < 3; i++)
      ss[i] = js.eh_ss[i];
    return SweConst.OK;
  }

//  void reorder(char *x, int size, int number) {
//    int i, j;
//    char s[8];
//    char *sp1 = x;
//    char *sp2 = &s[0];
//    for (i = 0; i < number; i++) {
//      for (j = 0; j < size; j++)
//        *(sp2 + j) = *(sp1 + size - j - 1);
//      for (j = 0; j < size; j++)
//        *(sp1 + j) = *(sp2 + j);
//      sp1 += size;
//    }
//  }

  void swi_close_jpl_file() {
    if (js != null) {
      try {
        if (js.jplfptr != null) {
          js.jplfptr.close();
        }
      } catch (java.io.IOException e) {
      }
      if (js.jplfname != null) {
        js.jplfname = null;
      }
      if (js.jplfpath != null) {
        js.jplfpath = null;
      }
      js = null;
    }
  }

  int swi_open_jpl_file(double[] ss, String fname, String fpath,
                                StringBuffer serr) throws SwissephException {
    int retc = SweConst.OK;
    /* if open, return */
    if (js != null && js.jplfptr != null) {
      return SweConst.OK;
    }
    js=new JplSave();
/*
    if ((js = (struct jpl_save *) CALLOC(1, sizeof(struct jpl_save))) == null
      || (js.jplfname = MALLOC(strlen(fname)+1)) == null
      || (js.jplfpath = MALLOC(strlen(fpath)+1)) == null
      ) {
      if (serr != null)
        strcpy(serr, "error in malloc() with JPL ephemeris.");
      return SweConst.ERR;
    }
*/
    js.jplfname=fname;
    js.jplfpath=fpath;
    try {
      retc = read_const_jpl(ss, serr);
    } catch (SwissephException se) {
      swi_close_jpl_file();
      throw se;
    }

    /* intializations for function interpol() */
    js.pc[0] = 1;
    js.pc[1] = 2;
    js.vc[1] = 1;
    js.ac[2] = 4;
    js.jc[3] = 24;

    return retc;
  }

  int swi_get_jpl_denum() {
    return js.eh_denum;
  }

  double[] getJPLRange(String fname) throws SwissephException {
    double start=0./0., end=0./0.;
    FilePtr fp = null;
    try {
      fp = sw.swi_fopen(SwephData.SEI_FILE_PLANET, fname, swed.ephepath, null);
      fp.seek(252+6*400);
      start = fp.readDouble();
      end = fp.readDouble();
    } catch (SwissephException e) {
      throw e;
    } catch (Exception e) {
    }
    try { fp.close(); } catch (Exception e) { }
    return new double[]{start, end};
  }
}


class JplSave {
  String jplfname=null;
  String jplfpath=null;
  FilePtr jplfptr=null;
//  boolean do_reorder;
  double eh_cval[]=new double[400];
  double eh_ss[]=new double[3], eh_au, eh_emrat;
  int eh_denum, eh_ncon, eh_ipt[]=new int[39];
  String ch_cnam="";
  double pv[]=new double[78];
  double pvsun[]=new double[6];
  double buf[]=new double[1500];
  double pc[]=new double[18], vc[]=new double[18],
         ac[]=new double[18], jc[]=new double[18];
  boolean do_km;
}
