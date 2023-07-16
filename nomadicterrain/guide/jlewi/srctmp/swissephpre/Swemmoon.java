//#ifdef NO_RISE_TRANS
//#define ASTROLOGY
//#endif /* NO_RISE_TRANS */

//#ifdef TRACE1
//#define TRACE0
//#endif /* TRACE1 */

//#ifdef TRACE0
//#define ORIGINAL
//#endif /* TRACE0 */
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

class Swemmoon {

 /*
 * Expansions for the geocentric ecliptic longitude,
 * latitude, and distance of the Moon referred to the mean equinox
 * and ecliptic of date.
 *
 * This version of cmoon.c adjusts the ELP2000-85 analytical Lunar
 * theory of Chapront-Touze and Chapront to fit the Jet Propulsion
 * Laboratory's DE404 long ephemeris on the interval from 3000 B.C.
 * to 3000 A.D.
 *
 * The fit is much better in the remote past and future if
 * secular terms are included in the arguments of the oscillatory
 * perturbations.  Such adjustments cannot easily be incorporated
 * into the 1991 lunar tables.  In this program the traditional
 * literal arguments are used instead, with mean elements adjusted
 * for a best fit to the reference ephemeris.
 *
 * This program omits many oscillatory terms from the analytical
 * theory which, if they were included, would yield a much higher
 * accuracy for modern dates.  Detailed statistics of the precision
 * are given in the table below.  Comparing at 64-day intervals
 * over the period -3000 to +3000, the maximum discrepancies noted
 * were 7" longitude, 5" latitude, and 5 x 10^-8 au radius.
 * The expressions used for precession in this comparision were
 * those of Simon et al (1994).
 *
 * The adjusted coefficients were found by an unweighted least squares
 * fit to the numerical ephemeris in the mentioned test interval.
 * The approximation error increases rapidly outside this interval.
 * J. Chapront (1994) has described the basic fitting procedure.
 *
 * A major change from DE200 to DE404 is in the coefficient
 * of tidal acceleration of the Moon, which causes the Moon's
 * longitude to depart by about -0.9" per century squared
 * from DE200.  Uncertainty in this quantity continues to
 * be the limiting factor in long term projections of the Moon's
 * ephemeris.
 *
 * Since the Lunar theory is cast in the ecliptic of date, it makes
 * some difference what formula you use for precession.  The adjustment
 * to DE404 was carried out relative to the mean equinox and ecliptic
 * of date as defined in Williams (1994).  An earlier version of this
 * program used the precession given by Simon et al (1994).  The difference
 * between these two precession formulas amounts to about 12" in Lunar
 * longitude at 3000 B.C.
 *
 *    Maximum deviations between DE404 and this program
 *    in a set of 34274 samples spaced 64 days apart
 *
 *   Interval     Longitude  Latitude  Radius
 *   Julian Year   arc sec   arc sec   10^-8 au
 * -3000 to -2500    5.66      4.66     4.93
 * -2500 to -2000    5.49      3.98     4.56
 * -2000 to -1500    6.98      4.17     4.81
 * -1500 to -1000    5.74      3.53     4.87
 * -1000 to -500     5.95      3.42     4.67
 * -500 to     0     4.94      3.07     4.04
 *    0 to   500     4.42      2.65     4.55
 *  500 to  1000     5.68      3.30     3.99
 * 1000 to  1500     4.32      3.21     3.83
 * 1500 to  2000     2.70      2.69     3.71
 * 2000 to  2500     3.35      2.32     3.85
 * 2500 to  3000     4.62      2.39     4.11
 *
 *
 *
 * References:
 *
 *   James G. Williams, "Contributions to the Earth's obliquity rate,
 *   precession, and nutation,"  Astron. J. 108, 711-724 (1994)
 *
 *   DE403 and DE404 ephemerides by E. M. Standish, X. X. Newhall, and
 *   J. G. Williams are at the JPL computer site navigator.jpl.nasa.gov.
 *
 *   J. L. Simon, P. Bretagnon, J. Chapront, M. Chapront-Touze', G. Francou,
 *   and J. Laskar, "Numerical Expressions for precession formulae and
 *   mean elements for the Moon and the planets," Astronomy and Astrophysics
 *   282, 663-683 (1994)
 *
 *   P. Bretagnon and Francou, G., "Planetary theories in rectangular
 *   and spherical variables. VSOP87 solutions," Astronomy and
 *   Astrophysics 202, 309-315 (1988)
 *
 *   M. Chapront-Touze' and J. Chapront, "ELP2000-85: a semi-analytical
 *   lunar ephemeris adequate for historical times," Astronomy and
 *   Astrophysics 190, 342-352 (1988).
 *
 *   M. Chapront-Touze' and J. Chapront, _Lunar Tables and
 *   Programs from 4000 B.C. to A.D. 8000_, Willmann-Bell (1991)
 *
 *   J. Laskar, "Secular terms of classical planetary theories
 *   using the results of general theory," Astronomy and Astrophysics
 *   157, 59070 (1986)
 *
 *   S. L. Moshier, "Comparison of a 7000-year lunar ephemeris
 *   with analytical theory," Astronomy and Astrophysics 262,
 *   613-616 (1992)
 *
 *   J. Chapront, "Representation of planetary ephemerides by frequency
 *   analysis.  Application to the five outer planets,"  Astronomy and
 *   Astrophysics Suppl. Ser. 109, 181-192 (1994)
 *
 *
 * Entry swi_moshmoon2() returns the geometric position of the Moon
 * relative to the Earth.  Its calling procedure is as follows:
 *
 * double JD;       input Julian Ephemeris Date
 * double pol[3];   output ecliptic polar coordinatees in radians and au
 *                  pol[0] longitude, pol[1] latitude, pol[2] radius
 * swi_moshmoon2( JD, pol );
 *
 * - S. L. Moshier, August, 1991
 * DE200 fit: July, 1992
 * DE404 fit: October, 1995
 *
 * Dieter Koch: adaptation to SWISSEPH, April 1996
 * 18-feb-2006  replaced LP by SWELP because of name collision
 */

  SwissData swed;
  SwissLib sl;
//#ifdef ORIGINAL
  CFmt cv=new CFmt();
//#endif /* ORIGINAL */

  Swemmoon() {
    this(null,null);
// //#ifdef TRACE0
//     System.out.println(System.currentTimeMillis()+" Swemmoon()");
// //#endif /* TRACE0 */
  }

  Swemmoon(SwissData swed, SwissLib sl) {
//#ifdef TRACE0
    Trace.level++;
    Trace.trace(Trace.level, "Swemmoon(SwissData, SwissLib)");
//#ifdef TRACE1
    System.out.println("    swed: " + swed + "\n    sl: " + sl);
//#endif /* TRACE1 */
//#endif /* TRACE0 */
    this.swed=swed;
    this.sl=sl;
    if (this.swed ==null) { this.swed =new SwissData(); }
    if (this.sl   ==null) { this.sl   =new SwissLib(); }
//#ifdef TRACE0
    Trace.level--;
//#endif /* TRACE0 */
  }


//#ifdef MOSH_MOON_200
  /* The following coefficients were calculated by a simultaneous least
   * squares fit between the analytical theory and the continued DE200
   * numerically integrated ephemeris from 9000 BC to 13000 AD.
   * See references to the array z[] later on in the program.
   * The 71 coefficients were estimated from 42,529 Lunar positions.
   */
  static final double z[] = {
  -1.225346551567e+001, /* F, t^2 */
  -1.096676093208e-003, /* F, t^3 */
  -2.165750777942e-006, /* F, t^4 */
  -2.790392351314e-009, /* F, t^5 */
   4.189032191814e-011, /* F, t^6 */
   4.474984866301e-013, /* F, t^7 */
   3.239398410335e+001, /* l, t^2 */
   5.185305877294e-002, /* l, t^3 */
  -2.536291235258e-004, /* l, t^4 */
  -2.506365935364e-008, /* l, t^5 */
   3.452144225877e-011, /* l, t^6 */
  -1.755312760154e-012, /* l, t^7 */
  -5.870522364514e+000, /* D, t^2 */
   6.493037519768e-003, /* D, t^3 */
  -3.702060118571e-005, /* D, t^4 */
   2.560078201452e-009, /* D, t^5 */
   2.555243317839e-011, /* D, t^6 */
  -3.207663637426e-013, /* D, t^7 */
  -4.776684245026e+000, /* L, t^2 */
   6.580112707824e-003, /* L, t^3 */
  -6.073960534117e-005, /* L, t^4 */
  -1.024222633731e-008, /* L, t^5 */
   2.235210987108e-010, /* L, t^6 */
   7.200592540556e-014, /* L, t^7 */
  -8.552017636339e+001, /* t^2 Math.cos(18V - 16E - l) */
  -2.055794304596e+002, /* t^2 Math.sin(18V - 16E - l) */
  -1.097555241866e+000, /* t^3 Math.cos(18V - 16E - l) */
   5.219423171002e-001, /* t^3 Math.sin(18V - 16E - l) */
   2.088802640755e-003, /* t^4 Math.cos(18V - 16E - l) */
   4.616541527921e-003, /* t^4 Math.sin(18V - 16E - l) */
   4.794930645807e+000, /* t^2 Math.cos(10V - 3E - l) */
  -4.595134364283e+001, /* t^2 Math.sin(10V - 3E - l) */
  -6.659812174691e-002, /* t^3 Math.cos(10V - 3E - l) */
  -2.570048828246e-001, /* t^3 Math.sin(10V - 3E - l) */
   6.229863046223e-004, /* t^4 Math.cos(10V - 3E - l) */
   5.504368344700e-003, /* t^4 Math.sin(10V - 3E - l) */
  -3.084830597278e+000, /* t^2 Math.cos(8V - 13E) */
  -1.000471012253e+001, /* t^2 Math.sin(8V - 13E) */
   6.590112074510e-002, /* t^3 Math.cos(8V - 13E) */
  -3.212573348278e-003, /* t^3 Math.sin(8V - 13E) */
   5.409038312567e-004, /* t^4 Math.cos(8V - 13E) */
   1.293377988163e-003, /* t^4 Math.sin(8V - 13E) */
   2.311794636111e+001, /* t^2 Math.cos(4E - 8M + 3J) */
  -3.157036220040e+000, /* t^2 Math.sin(4E - 8M + 3J) */
  -3.019293162417e+000, /* t^2 Math.cos(18V - 16E) */
  -9.211526858975e+000, /* t^2 Math.sin(18V - 16E) */
  -4.993704215784e-002, /* t^3 Math.cos(18V - 16E) */
   2.991187525454e-002, /* t^3 Math.sin(18V - 16E) */
  -3.827414182969e+000, /* t^2 Math.cos(18V - 16E - 2l) */
  -9.891527703219e+000, /* t^2 Math.sin(18V - 16E - 2l) */
  -5.322093802878e-002, /* t^3 Math.cos(18V - 16E - 2l) */
   3.164702647371e-002, /* t^3 Math.sin(18V - 16E - 2l) */
   7.713905234217e+000, /* t^2 Math.cos(2J - 5S) */
  -6.077986950734e+000, /* t^3 Math.sin(2J - 5S) */
  -1.278232501462e-001, /* t^2 Math.cos(L - F) */
   4.760967236383e-001, /* t^2 Math.sin(L - F) */
  -6.759005756460e-001, /* t^3 Math.sin(l') */
   1.655727996357e-003, /* t^4 Math.sin(l') */
   1.646526117252e-001, /* t^3 Math.sin(2D - l') */
  -4.167078100233e-004, /* t^4 Math.sin(2D - l') */
   2.067529538504e-001, /* t^3 Math.sin(2D - l' - l) */
  -5.219127398748e-004, /* t^4 Math.sin(2D - l' - l) */
  -1.526335222289e-001, /* t^3 Math.sin(l' - l) */
  -1.120545131358e-001, /* t^3 Math.sin(l' + l) */
   4.619472391553e-002, /* t^3 Math.sin(2D - 2l') */
   4.863621236157e-004, /* t^4 Math.sin(2D - 2l') */
  -4.280059182608e-002, /* t^3 Math.sin(2l') */
  -4.328378207833e-004, /* t^4 Math.sin(2l') */
  -8.371028286974e-003, /* t^3 Math.sin(2D - l) */
   4.089447328174e-002, /* t^3 Math.sin(2D - 2l' - l) */
  -1.238363006354e-002, /* t^3 Math.sin(2D + 2l' - l) */
  };
//#else
  /* The following coefficients were calculated by a simultaneous least
   * squares fit between the analytical theory and DE404 on the finite
   * interval from -3000 to +3000.
   * The coefficients were estimated from 34,247 Lunar positions.
   */
  static final double z[] = {
    /* The following are scaled in arc seconds, time in Julian centuries.
       They replace the corresponding terms in the mean elements.  */
    -1.312045233711e+01, /* F, t^2 */
    -1.138215912580e-03, /* F, t^3 */
    -9.646018347184e-06, /* F, t^4 */
     3.146734198839e+01, /* l, t^2 */
     4.768357585780e-02, /* l, t^3 */
    -3.421689790404e-04, /* l, t^4 */
    -6.847070905410e+00, /* D, t^2 */
    -5.834100476561e-03, /* D, t^3 */
    -2.905334122698e-04, /* D, t^4 */
    -5.663161722088e+00, /* L, t^2 */
     5.722859298199e-03, /* L, t^3 */
    -8.466472828815e-05, /* L, t^4 */
    /* The following longitude terms are in arc seconds times 10^5.  */
    -8.429817796435e+01, /* t^2 Math.cos(18V - 16E - l) */
    -2.072552484689e+02, /* t^2 Math.sin(18V - 16E - l) */
     7.876842214863e+00, /* t^2 Math.cos(10V - 3E - l) */
     1.836463749022e+00, /* t^2 Math.sin(10V - 3E - l) */
    -1.557471855361e+01, /* t^2 Math.cos(8V - 13E) */
    -2.006969124724e+01, /* t^2 Math.sin(8V - 13E) */
     2.152670284757e+01, /* t^2 Math.cos(4E - 8M + 3J) */
    -6.179946916139e+00, /* t^2 Math.sin(4E - 8M + 3J) */
    -9.070028191196e-01, /* t^2 Math.cos(18V - 16E) */
    -1.270848233038e+01, /* t^2 Math.sin(18V - 16E) */
    -2.145589319058e+00, /* t^2 Math.cos(2J - 5S) */
     1.381936399935e+01, /* t^2 Math.sin(2J - 5S) */
    -1.999840061168e+00, /* t^3 Math.sin(l') */
  };
//#endif  /* ! MOSH_MOON_200 */


  /* Perturbation tables
   */
  static final int NLR=118;
  static final short LR[]={
  /*
                 Longitude    Radius
   D  l' l  F    1"  .0001"  1km  .0001km */

   0, 0, 1, 0, 22639, 5858,-20905,-3550,
   2, 0,-1, 0,  4586, 4383, -3699,-1109,
   2, 0, 0, 0,  2369, 9139, -2955,-9676,
   0, 0, 2, 0,   769,  257,  -569,-9251,
   0, 1, 0, 0,  -666,-4171,    48, 8883,
   0, 0, 0, 2,  -411,-5957,    -3,-1483,
   2, 0,-2, 0,   211, 6556,   246, 1585,
   2,-1,-1, 0,   205, 4358,  -152,-1377,
   2, 0, 1, 0,   191, 9562,  -170,-7331,
   2,-1, 0, 0,   164, 7285,  -204,-5860,
   0, 1,-1, 0,  -147,-3213,  -129,-6201,
   1, 0, 0, 0,  -124,-9881,   108, 7427,
   0, 1, 1, 0,  -109,-3803,   104, 7552,
   2, 0, 0,-2,    55, 1771,    10, 3211,
   0, 0, 1, 2,   -45, -996,     0,    0,
   0, 0, 1,-2,    39, 5333,    79, 6606,
   4, 0,-1, 0,    38, 4298,   -34,-7825,
   0, 0, 3, 0,    36, 1238,   -23,-2104,
   4, 0,-2, 0,    30, 7726,   -21,-6363,
   2, 1,-1, 0,   -28,-3971,    24, 2085,
   2, 1, 0, 0,   -24,-3582,    30, 8238,
   1, 0,-1, 0,   -18,-5847,    -8,-3791,
   1, 1, 0, 0,    17, 9545,   -16,-6747,
   2,-1, 1, 0,    14, 5303,   -12,-8314,
   2, 0, 2, 0,    14, 3797,   -10,-4448,
   4, 0, 0, 0,    13, 8991,   -11,-6500,
   2, 0,-3, 0,    13, 1941,    14, 4027,
   0, 1,-2, 0,    -9,-6791,    -7,  -27,
   2, 0,-1, 2,    -9,-3659,     0, 7740,
   2,-1,-2, 0,     8, 6055,    10,  562,
   1, 0, 1, 0,    -8,-4531,     6, 3220,
   2,-2, 0, 0,     8,  502,    -9,-8845,
   0, 1, 2, 0,    -7,-6302,     5, 7509,
   0, 2, 0, 0,    -7,-4475,     1,  657,
   2,-2,-1, 0,     7, 3712,    -4,-9501,
   2, 0, 1,-2,    -6,-3832,     4, 1311,
   2, 0, 0, 2,    -5,-7416,     0,    0,
   4,-1,-1, 0,     4, 3740,    -3,-9580,
   0, 0, 2, 2,    -3,-9976,     0,    0,
   3, 0,-1, 0,    -3,-2097,     3, 2582,
   2, 1, 1, 0,    -2,-9145,     2, 6164,
   4,-1,-2, 0,     2, 7319,    -1,-8970,
   0, 2,-1, 0,    -2,-5679,    -2,-1171,
   2, 2,-1, 0,    -2,-5212,     2, 3536,
   2, 1,-2, 0,     2, 4889,     0, 1437,
   2,-1, 0,-2,     2, 1461,     0, 6571,
   4, 0, 1, 0,     1, 9777,    -1,-4226,
   0, 0, 4, 0,     1, 9337,    -1,-1169,
   4,-1, 0, 0,     1, 8708,    -1,-5714,
   1, 0,-2, 0,    -1,-7530,    -1,-7385,
   2, 1, 0,-2,    -1,-4372,     0,-1357,
   0, 0, 2,-2,    -1,-3726,    -4,-4212,
   1, 1, 1, 0,     1, 2618,     0,-9333,
   3, 0,-2, 0,    -1,-2241,     0, 8624,
   4, 0,-3, 0,     1, 1868,     0,-5142,
   2,-1, 2, 0,     1, 1770,     0,-8488,
   0, 2, 1, 0,    -1,-1617,     1, 1655,
   1, 1,-1, 0,     1,  777,     0, 8512,
   2, 0, 3, 0,     1,  595,     0,-6697,
   2, 0, 1, 2,     0,-9902,     0,    0,
   2, 0,-4, 0,     0, 9483,     0, 7785,
   2,-2, 1, 0,     0, 7517,     0,-6575,
   0, 1,-3, 0,     0,-6694,     0,-4224,
   4, 1,-1, 0,     0,-6352,     0, 5788,
   1, 0, 2, 0,     0,-5840,     0, 3785,
   1, 0, 0,-2,     0,-5833,     0,-7956,
   6, 0,-2, 0,     0, 5716,     0,-4225,
   2, 0,-2,-2,     0,-5606,     0, 4726,
   1,-1, 0, 0,     0,-5569,     0, 4976,
   0, 1, 3, 0,     0,-5459,     0, 3551,
   2, 0,-2, 2,     0,-5357,     0, 7740,
   2, 0,-1,-2,     0, 1790,     8, 7516,
   3, 0, 0, 0,     0, 4042,    -1,-4189,
   2,-1,-3, 0,     0, 4784,     0, 4950,
   2,-1, 3, 0,     0,  932,     0, -585,
   2, 0, 2,-2,     0,-4538,     0, 2840,
   2,-1,-1, 2,     0,-4262,     0,  373,
   0, 0, 0, 4,     0, 4203,     0,    0,
   0, 1, 0, 2,     0, 4134,     0,-1580,
   6, 0,-1, 0,     0, 3945,     0,-2866,
   2,-1, 0, 2,     0,-3821,     0,    0,
   2,-1, 1,-2,     0,-3745,     0, 2094,
   4, 1,-2, 0,     0,-3576,     0, 2370,
   1, 1,-2, 0,     0, 3497,     0, 3323,
   2,-3, 0, 0,     0, 3398,     0,-4107,
   0, 0, 3, 2,     0,-3286,     0,    0,
   4,-2,-1, 0,     0,-3087,     0,-2790,
   0, 1,-1,-2,     0, 3015,     0,    0,
   4, 0,-1,-2,     0, 3009,     0,-3218,
   2,-2,-2, 0,     0, 2942,     0, 3430,
   6, 0,-3, 0,     0, 2925,     0,-1832,
   2, 1, 2, 0,     0,-2902,     0, 2125,
   4, 1, 0, 0,     0,-2891,     0, 2445,
   4,-1, 1, 0,     0, 2825,     0,-2029,
   3, 1,-1, 0,     0, 2737,     0,-2126,
   0, 1, 1, 2,     0, 2634,     0,    0,
   1, 0, 0, 2,     0, 2543,     0,    0,
   3, 0, 0,-2,     0,-2530,     0, 2010,
   2, 2,-2, 0,     0,-2499,     0,-1089,
   2,-3,-1, 0,     0, 2469,     0,-1481,
   3,-1,-1, 0,     0,-2314,     0, 2556,
   4, 0, 2, 0,     0, 2185,     0,-1392,
   4, 0,-1, 2,     0,-2013,     0, 0,
   0, 2,-2, 0,     0,-1931,     0, 0,
   2, 2, 0, 0,     0,-1858,     0, 0,
   2, 1,-3, 0,     0, 1762,     0, 0,
   4, 0,-2, 2,     0,-1698,     0, 0,
   4,-2,-2, 0,     0, 1578,     0,-1083,
   4,-2, 0, 0,     0, 1522,     0,-1281,
   3, 1, 0, 0,     0, 1499,     0,-1077,
   1,-1,-1, 0,     0,-1364,     0, 1141,
   1,-3, 0, 0,     0,-1281,     0, 0,
   6, 0, 0, 0,     0, 1261,     0, -859,
   2, 0, 2, 2,     0,-1239,     0, 0,
   1,-1, 1, 0,     0,-1207,     0, 1100,
   0, 0, 5, 0,     0, 1110,     0, -589,
   0, 3, 0, 0,     0,-1013,     0,  213,
   4,-1,-3, 0,     0,  998,     0, 0,
  };


//#ifdef MOSH_MOON_200
  static final int NMB=56;
  static final short MB[]={
  /*
                 Latitude
   D  l' l  F    1"  .0001" */

   0, 0, 0, 1,18461, 2387,
   0, 0, 1, 1, 1010, 1671,
   0, 0, 1,-1,  999, 6936,
   2, 0, 0,-1,  623, 6524,
   2, 0,-1, 1,  199, 4837,
   2, 0,-1,-1,  166, 5741,
   2, 0, 0, 1,  117, 2607,
   0, 0, 2, 1,   61, 9120,
   2, 0, 1,-1,   33, 3572,
   0, 0, 2,-1,   31, 7597,
   2,-1, 0,-1,   29, 5766,
   2, 0,-2,-1,   15, 5663,
   2, 0, 1, 1,   15, 1216,
   2, 1, 0,-1,  -12, -941,
   2,-1,-1, 1,    8, 8681,
   2,-1, 0, 1,    7, 9586,
   2,-1,-1,-1,    7, 4346,
   0, 1,-1,-1,   -6,-7314,
   4, 0,-1,-1,    6, 5796,
   0, 1, 0, 1,   -6,-4601,
   0, 0, 0, 3,   -6,-2965,
   0, 1,-1, 1,   -5,-6324,
   1, 0, 0, 1,   -5,-3684,
   0, 1, 1, 1,   -5,-3113,
   0, 1, 1,-1,   -5, -759,
   0, 1, 0,-1,   -4,-8396,
   1, 0, 0,-1,   -4,-8057,
   0, 0, 3, 1,    3, 9841,
   4, 0, 0,-1,    3, 6745,
   4, 0,-1, 1,    2, 9985,
   0, 0, 1,-3,    2, 7986,
   4, 0,-2, 1,    2, 4139,
   2, 0, 0,-3,    2, 1863,
   2, 0, 2,-1,    2, 1462,
   2,-1, 1,-1,    1, 7660,
   2, 0,-2, 1,   -1,-6244,
   0, 0, 3,-1,    1, 5813,
   2, 0, 2, 1,    1, 5198,
   2, 0,-3,-1,    1, 5156,
   2, 1,-1, 1,   -1,-3178,
   2, 1, 0, 1,   -1,-2643,
   4, 0, 0, 1,    1, 1919,
   2,-1, 1, 1,    1, 1346,
   2,-2, 0,-1,    1,  859,
   0, 0, 1, 3,   -1, -194,
   2, 1, 1,-1,    0,-8227,
   1, 1, 0,-1,    0, 8042,
   1, 1, 0, 1,    0, 8026,
   0, 1,-2,-1,    0,-7932,
   2, 1,-1,-1,    0,-7910,
   1, 0, 1, 1,    0,-6674,
   2,-1,-2,-1,    0, 6502,
   0, 1, 2, 1,    0,-6388,
   4, 0,-2,-1,    0, 6337,
   4,-1,-1,-1,    0, 5958,
   1, 0, 1,-1,    0,-5889,
  };
//#else
  static final int NMB=77;
  static final short MB[]={
  /*
                 Latitude
   D  l' l  F    1"  .0001" */

   0, 0, 0, 1,18461, 2387,
   0, 0, 1, 1, 1010, 1671,
   0, 0, 1,-1,  999, 6936,
   2, 0, 0,-1,  623, 6524,
   2, 0,-1, 1,  199, 4837,
   2, 0,-1,-1,  166, 5741,
   2, 0, 0, 1,  117, 2607,
   0, 0, 2, 1,   61, 9120,
   2, 0, 1,-1,   33, 3572,
   0, 0, 2,-1,   31, 7597,
   2,-1, 0,-1,   29, 5766,
   2, 0,-2,-1,   15, 5663,
   2, 0, 1, 1,   15, 1216,
   2, 1, 0,-1,  -12, -941,
   2,-1,-1, 1,    8, 8681,
   2,-1, 0, 1,    7, 9586,
   2,-1,-1,-1,    7, 4346,
   0, 1,-1,-1,   -6,-7314,
   4, 0,-1,-1,    6, 5796,
   0, 1, 0, 1,   -6,-4601,
   0, 0, 0, 3,   -6,-2965,
   0, 1,-1, 1,   -5,-6324,
   1, 0, 0, 1,   -5,-3684,
   0, 1, 1, 1,   -5,-3113,
   0, 1, 1,-1,   -5, -759,
   0, 1, 0,-1,   -4,-8396,
   1, 0, 0,-1,   -4,-8057,
   0, 0, 3, 1,    3, 9841,
   4, 0, 0,-1,    3, 6745,
   4, 0,-1, 1,    2, 9985,
   0, 0, 1,-3,    2, 7986,
   4, 0,-2, 1,    2, 4139,
   2, 0, 0,-3,    2, 1863,
   2, 0, 2,-1,    2, 1462,
   2,-1, 1,-1,    1, 7660,
   2, 0,-2, 1,   -1,-6244,
   0, 0, 3,-1,    1, 5813,
   2, 0, 2, 1,    1, 5198,
   2, 0,-3,-1,    1, 5156,
   2, 1,-1, 1,   -1,-3178,
   2, 1, 0, 1,   -1,-2643,
   4, 0, 0, 1,    1, 1919,
   2,-1, 1, 1,    1, 1346,
   2,-2, 0,-1,    1,  859,
   0, 0, 1, 3,   -1, -194,
   2, 1, 1,-1,    0,-8227,
   1, 1, 0,-1,    0, 8042,
   1, 1, 0, 1,    0, 8026,
   0, 1,-2,-1,    0,-7932,
   2, 1,-1,-1,    0,-7910,
   1, 0, 1, 1,    0,-6674,
   2,-1,-2,-1,    0, 6502,
   0, 1, 2, 1,    0,-6388,
   4, 0,-2,-1,    0, 6337,
   4,-1,-1,-1,    0, 5958,
   1, 0, 1,-1,    0,-5889,
   4, 0, 1,-1,    0, 4734,
   1, 0,-1,-1,    0,-4299,
   4,-1, 0,-1,    0, 4149,
   2,-2, 0, 1,    0, 3835,
   3, 0, 0,-1,    0,-3518,
   4,-1,-1, 1,    0, 3388,
   2, 0,-1,-3,    0, 3291,
   2,-2,-1, 1,    0, 3147,
   0, 1, 2,-1,    0,-3129,
   3, 0,-1,-1,    0,-3052,
   0, 1,-2, 1,    0,-3013,
   2, 0, 1,-3,    0,-2912,
   2,-2,-1,-1,    0, 2686,
   0, 0, 4, 1,    0, 2633,
   2, 0,-3, 1,    0, 2541,
   2, 0,-1, 3,    0,-2448,
   2, 1, 1, 1,    0,-2370,
   4,-1,-2, 1,    0, 2138,
   4, 0, 1, 1,    0, 2126,
   3, 0,-1, 1,    0,-2059,
   4, 1,-1,-1,    0,-1719,
  };
//#endif  /* ! MOSH_MOON_200 */

  static final int NLRT=38;
  static final short LRT[]={
  /*
  Multiply by T
                 Longitude    Radius
   D  l' l  F   .1"  .00001" .1km  .00001km */

   0, 1, 0, 0,    16, 7680,    -1,-2302,
   2,-1,-1, 0,    -5,-1642,     3, 8245,
   2,-1, 0, 0,    -4,-1383,     5, 1395,
   0, 1,-1, 0,     3, 7115,     3, 2654,
   0, 1, 1, 0,     2, 7560,    -2,-6396,
   2, 1,-1, 0,     0, 7118,     0,-6068,
   2, 1, 0, 0,     0, 6128,     0,-7754,
   1, 1, 0, 0,     0,-4516,     0, 4194,
   2,-2, 0, 0,     0,-4048,     0, 4970,
   0, 2, 0, 0,     0, 3747,     0, -540,
   2,-2,-1, 0,     0,-3707,     0, 2490,
   2,-1, 1, 0,     0,-3649,     0, 3222,
   0, 1,-2, 0,     0, 2438,     0, 1760,
   2,-1,-2, 0,     0,-2165,     0,-2530,
   0, 1, 2, 0,     0, 1923,     0,-1450,
   0, 2,-1, 0,     0, 1292,     0, 1070,
   2, 2,-1, 0,     0, 1271,     0,-6070,
   4,-1,-1, 0,     0,-1098,     0,  990,
   2, 0, 0, 0,     0, 1073,     0,-1360,
   2, 0,-1, 0,     0,  839,     0, -630,
   2, 1, 1, 0,     0,  734,     0, -660,
   4,-1,-2, 0,     0, -688,     0,  480,
   2, 1,-2, 0,     0, -630,     0,    0,
   0, 2, 1, 0,     0,  587,     0, -590,
   2,-1, 0,-2,     0, -540,     0, -170,
   4,-1, 0, 0,     0, -468,     0,  390,
   2,-2, 1, 0,     0, -378,     0,  330,
   2, 1, 0,-2,     0,  364,     0,    0,
   1, 1, 1, 0,     0, -317,     0,  240,
   2,-1, 2, 0,     0, -295,     0,  210,
   1, 1,-1, 0,     0, -270,     0, -210,
   2,-3, 0, 0,     0, -256,     0,  310,
   2,-3,-1, 0,     0, -187,     0,  110,
   0, 1,-3, 0,     0,  169,     0,  110,
   4, 1,-1, 0,     0,  158,     0, -150,
   4,-2,-1, 0,     0, -155,     0,  140,
   0, 0, 1, 0,     0,  155,     0, -250,
   2,-2,-2, 0,     0, -148,     0, -170,
  };

  static final int NBT=16;
  static final short BT[]={
  /*
  Multiply by T
               Latitude
   D  l' l  F  .00001"  */

   2,-1, 0,-1, -7430,
   2, 1, 0,-1,  3043,
   2,-1,-1, 1, -2229,
   2,-1, 0, 1, -1999,
   2,-1,-1,-1, -1869,
   0, 1,-1,-1,  1696,
   0, 1, 0, 1,  1623,
   0, 1,-1, 1,  1418,
   0, 1, 1, 1,  1339,
   0, 1, 1,-1,  1278,
   0, 1, 0,-1,  1217,
   2,-2, 0,-1,  -547,
   2,-1, 1,-1,  -443,
   2, 1,-1, 1,   331,
   2, 1, 0, 1,   317,
   2, 0, 0,-1,   295,
  };

  static final int NLRT2=25;
  static final short LRT2[]={
  /*
  Multiply by T^2
             Longitude    Radius
   D  l' l  F  .00001" .00001km   */

   0, 1, 0, 0,  487,   -36,
   2,-1,-1, 0, -150,   111,
   2,-1, 0, 0, -120,   149,
   0, 1,-1, 0,  108,    95,
   0, 1, 1, 0,   80,   -77,
   2, 1,-1, 0,   21,   -18,
   2, 1, 0, 0,   20,   -23,
   1, 1, 0, 0,  -13,    12,
   2,-2, 0, 0,  -12,    14,
   2,-1, 1, 0,  -11,     9,
   2,-2,-1, 0,  -11,     7,
   0, 2, 0, 0,   11,     0,
   2,-1,-2, 0,   -6,    -7,
   0, 1,-2, 0,    7,     5,
   0, 1, 2, 0,    6,    -4,
   2, 2,-1, 0,    5,    -3,
   0, 2,-1, 0,    5,     3,
   4,-1,-1, 0,   -3,     3,
   2, 0, 0, 0,    3,    -4,
   4,-1,-2, 0,   -2,     0,
   2, 1,-2, 0,   -2,     0,
   2,-1, 0,-2,   -2,     0,
   2, 1, 1, 0,    2,    -2,
   2, 0,-1, 0,    2,     0,
   0, 2, 1, 0,    2,     0,
  };

  static final int NBT2=12;
  static final short BT2[]={
  /*
  Multiply by T^2
             Latitiude
   D  l' l  F  .00001" */

   2,-1, 0,-1,  -22,
   2, 1, 0,-1,    9,
   2,-1, 0, 1,   -6,
   2,-1,-1, 1,   -6,
   2,-1,-1,-1,   -5,
   0, 1, 0, 1,    5,
   0, 1,-1,-1,    5,
   0, 1, 1, 1,    4,
   0, 1, 1,-1,    4,
   0, 1, 0,-1,    4,
   0, 1,-1, 1,    4,
   2,-2, 0,-1,   -2,
  };

  /* The following times are set up by update() and refer
   * to the same instant.  The distinction between them
   * is required by altaz().
   */
  double ss[][]=new double[5][8];
  double cc[][]=new double[5][8];

  double l;                /* Moon's ecliptic longitude */
  double B;                /* Ecliptic latitude */

  double moonpol[]=new double[3];

  /* Orbit calculation begins.
   */
  double SWELP;
  double M;
  double MP;
  double D;
  double NF;
  double T;
  double T2;

  static double T3;
  static double T4;
  static double f;
  static double g;
  static double Ve;
  static double Ea;
  static double Ma;
  static double Ju;
  static double Sa;
  static double cg;
  static double sg;
  static double l1;
  static double l2;
  static double l3;
  static double l4;

  /* Calculate geometric coordinates of Moon
   * without light time or nutation correction.
   */
  int swi_moshmoon2(double J, double[] pol) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_moshmoon2(double, double[])");
//#endif /* TRACE0 */
    int i;
    T = (J-SwephData.J2000)/36525.0;
    T2 = T*T;
    mean_elements();
    mean_elements_pl();
    moon1();
    moon2();
    moon3();
    moon4();
    for( i=0; i<3; i++ )
      pol[i] = moonpol[i];
    return(0);
  }

  /* Moshier's moom
   * tjd          julian day
   * xpm          array of 6 doubles for moon's position and speed vectors
   * serr         pointer to error string
   */
  int swi_moshmoon(double tjd, boolean do_save, double[] xpmret,
                   StringBuffer serr) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_moshmoon(double, boolean, double[], StringBuffer)");
//#endif /* TRACE0 */
    int i;
    double a, b, x1[]=new double[6], x2[]=new double[6], t;
    double xx[]=new double[6], xpm[];
    PlanData pdp = swed.pldat[SwephData.SEI_MOON];
    String s;
    if (do_save) {
      xpm = pdp.x;
    } else {
      xpm = xx;
    }
    /* allow 0.2 day tolerance so that true node interval fits in */
    if (tjd < SwephData.MOSHLUEPH_START - 0.2 || tjd > SwephData.MOSHLUEPH_END + 0.2) {
      if (serr != null) {
//#ifdef ORIGINAL
        s="jd "+cv.fmt("%f",tjd)+" outside Moshier's Moon range "+
          cv.fmt("%.2f",SwephData.MOSHLUEPH_START)+" .. "+
          cv.fmt("%.2f",SwephData.MOSHLUEPH_END)+" ";
//#else
        s="jd "+tjd+" outside Moshier's Moon range "+
          SwephData.MOSHLUEPH_START+" .. "+
          SwephData.MOSHLUEPH_END+" ";
//#endif /* ORIGINAL */
        if (serr.length() + s.length() < SwissData.AS_MAXCH) {
          serr.append(s);
        }
      }
      return(SweConst.ERR);
    }
    /* if moon has already been computed */
    if (tjd == pdp.teval && pdp.iephe == SweConst.SEFLG_MOSEPH) {
      if (xpmret != null) {
        for (i = 0; i <= 5; i++) {
          xpmret[i] = pdp.x[i];
        }
      }
      return(SweConst.OK);
    }
    /* else compute moon */
    swi_moshmoon2(tjd, xpm);
    if (do_save) {
      pdp.teval = tjd;
      pdp.xflgs = -1;
      pdp.iephe = SweConst.SEFLG_MOSEPH;
    }
    /* Moshier moon is referred to ecliptic of date. But we need
     * equatorial positions for several reasons.
     * e.g. computation of earth from emb and moon
     *                  of heliocentric moon
     * Besides, this helps to keep the program structure simpler
     */
    ecldat_equ2000(tjd, xpm);
    /* speed */
    /* from 2 other positions. */
    /* one would be good enough for computation of osculating node,
     * but not for osculating apogee */
    t = tjd + SwephData.MOON_SPEED_INTV;
    swi_moshmoon2(t, x1);
    ecldat_equ2000(t, x1);
    t = tjd - SwephData.MOON_SPEED_INTV;
    swi_moshmoon2(t, x2);
    ecldat_equ2000(t, x2);
    for (i = 0; i <= 2; i++) {
//#if 0
//    xpm[i+3] = (x1[i] - x2[i]) / SwephData.MOON_SPEED_INTV / 2;
//#else
      b = (x1[i] - x2[i]) / 2;
      a = (x1[i] + x2[i]) / 2 - xpm[i];
      xpm[i+3] = (2 * a + b) / SwephData.MOON_SPEED_INTV;
//#endif /* 0 */
    }
    if (xpmret != null) {
      for (i = 0; i <= 5; i++) {
        xpmret[i] = xpm[i];
      }
    }
    return(SweConst.OK);
  }


//#ifdef MOSH_MOON_200
  private void moon1() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.moon1()");
//#endif /* TRACE0 */
    double a;

    sscc( 0, SwephData.STR*D, 6 );
    sscc( 1, SwephData.STR*M,  4 );
    sscc( 2, SwephData.STR*MP, 4 );
    sscc( 3, SwephData.STR*NF, 4 );

    moonpol[0] = 0.0;
    moonpol[1] = 0.0;
    moonpol[2] = 0.0;

    /* terms in T^2, scale 1.0 = 10^-5" */
    chewm( LRT2, NLRT2, 4, 2, moonpol );
    chewm( BT2, NBT2, 4, 4, moonpol );

    f = 18 * Ve - 16 * Ea;

    g = SwephData.STR*(f - MP );  /* 18V - 16E - l */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l = 6.367278 * cg + 12.747036 * sg;  /* t^0 */
    l1 = 23123.70 * cg - 10570.02 * sg;  /* t^1 */
    l2 = z[24] * cg + z[25] * sg;        /* t^2 */
    l3 = z[26] * cg + z[27] * sg;        /* t^3 */
    l4 = z[28] * cg + z[29] * sg;        /* t^4 */
    moonpol[2] += 5.01 * cg + 2.72 * sg;

    g = SwephData.STR * (10.*Ve - 3.*Ea - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.253102 * cg + 0.503359 * sg;
    l1 += 1258.46 * cg + 707.29 * sg;
    l2 += z[30] * cg + z[31] * sg;
    l3 += z[32] * cg + z[33] * sg;
    l4 += z[34] * cg + z[35] * sg;

    g = SwephData.STR*(8.*Ve - 13.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.187231 * cg - 0.127481 * sg;
    l1 += -319.87 * cg - 18.34 * sg;
    l2 += z[36] * cg + z[37] * sg;
    l3 += z[38] * cg + z[39] * sg;
    l4 += z[40] * cg + z[41] * sg;

    a = 4.0*Ea - 8.0*Ma + 3.0*Ju;
    g = SwephData.STR * a;
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.866287 * cg + 0.248192 * sg;
    l1 += 41.87 * cg + 1053.97 * sg;
    l2 += z[42] * cg + z[43] * sg;

    g = SwephData.STR*(a - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.165009 * cg + 0.044176 * sg;
    l1 += 4.67 * cg + 201.55 * sg;


    g = SwephData.STR*f;  /* 18V - 16E */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.330401 * cg + 0.661362 * sg;
    l1 += 1202.67 * cg - 555.59 * sg;
    l2 += z[44] * cg + z[45] * sg;
    l3 += z[46] * cg + z[47] * sg;

    g = SwephData.STR*(f - 2.0*MP );  /* 18V - 16E - 2l */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.352185 * cg + 0.705041 * sg;
    l1 += 1283.59 * cg - 586.43 * sg;
    l2 += z[48] * cg + z[49] * sg;
    l3 += z[50] * cg + z[51] * sg;

    g = SwephData.STR * (2.0*Ju - 5.0*Sa);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.034700 * cg + 0.160041 * sg;
    l2 += z[52] * cg + z[53] * sg;

    g = SwephData.STR * (SWELP - NF);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.000116 * cg + 7.063040 * sg;
    l1 +=  298.8 * sg;
    l2 += z[54] * cg + z[55] * sg;


    /* T^3 terms */
    sg = Math.sin( SwephData.STR * M );
    l3 +=  z[56] * sg;
    l4 +=  z[57] * sg;

    g = SwephData.STR * (2.0*D - M);
    sg = Math.sin(g);
    cg = Math.cos(g);
    l3 +=  z[58] * sg;
    l4 +=  z[59] * sg;
    moonpol[2] +=  -0.2655 * cg * T;

    g = g - SwephData.STR * MP;
    sg = Math.sin(g);
    l3 +=  z[60] * sg;
    l4 +=  z[61] * sg;

    g = SwephData.STR * (M - MP);
    l3 +=  z[62] * Math.sin( g );
    moonpol[2] +=  -0.1568 * Math.cos( g ) * T;

    g = SwephData.STR * (M + MP);
    l3 +=  z[63] * Math.sin( g );
    moonpol[2] +=  0.1309 * Math.cos( g ) * T;

    g = SwephData.STR * 2.0 * (D - M);
    sg = Math.sin(g);
    l3 +=  z[64] * sg;
    l4 +=  z[65] * sg;

    g = SwephData.STR * 2.0 * M;
    sg = Math.sin(g);
    l3 +=  z[66] * sg;
    l4 +=  z[67] * sg;

    g = SwephData.STR * (2.0*D - MP);
    sg = Math.sin(g);
    l3 +=  z[68] * sg;

    g = SwephData.STR * (2.0*(D - M) - MP);
    sg = Math.sin(g);
    l3 +=  z[69] * sg;

    g = SwephData.STR * (2.0*(D + M) - MP);
    sg = Math.sin(g);
    cg = Math.cos(g);
    l3 +=  z[70] * sg;
    moonpol[2] +=   0.5568 * cg * T;

    l2 += moonpol[0];

    g = SwephData.STR*(2.0*D - M - MP);
    moonpol[2] +=  -0.1910 * Math.cos( g ) * T;


    moonpol[1] *= T;
    moonpol[2] *= T;

    /* terms in T */
    moonpol[0] = 0.0;
    chewm( BT, NBT, 4, 4, moonpol );
    chewm( LRT, NLRT, 4, 1, moonpol );
    g = SwephData.STR*(f - MP - NF - 2355767.6); /* 18V - 16E - l - F */
    moonpol[1] +=  -1127. * Math.sin(g);
    g = SwephData.STR*(f - MP + NF - 235353.6); /* 18V - 16E - l + F */
    moonpol[1] +=  -1123. * Math.sin(g);
    g = SwephData.STR*(Ea + D + 51987.6);
    moonpol[1] +=  1303. * Math.sin(g);
    g = SwephData.STR*SWELP;
    moonpol[1] +=  342. * Math.sin(g);


    g = SwephData.STR*(2.*Ve - 3.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l +=  -0.343550 * cg - 0.000276 * sg;
    l1 +=  105.90 * cg + 336.53 * sg;

    g = SwephData.STR*(f - 2.*D); /* 18V - 16E - 2D */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.074668 * cg + 0.149501 * sg;
    l1 += 271.77 * cg - 124.20 * sg;

    g = SwephData.STR*(f - 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.073444 * cg + 0.147094 * sg;
    l1 += 265.24 * cg - 121.16 * sg;

    g = SwephData.STR*(f + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.072844 * cg + 0.145829 * sg;
    l1 += 265.18 * cg - 121.29 * sg;

    g = SwephData.STR*(f + 2.*(D - MP));
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.070201 * cg + 0.140542 * sg;
    l1 += 255.36 * cg - 116.79 * sg;

    g = SwephData.STR*(Ea + D - NF);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.288209 * cg - 0.025901 * sg;
    l1 += -63.51 * cg - 240.14 * sg;

    g = SwephData.STR*(2.*Ea - 3.*Ju + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.077865 * cg + 0.438460 * sg;
    l1 += 210.57 * cg + 124.84 * sg;

    g = SwephData.STR*(Ea - 2.*Ma);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.216579 * cg + 0.241702 * sg;
    l1 += 197.67 * cg + 125.23 * sg;

    g = SwephData.STR*(a + MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.165009 * cg + 0.044176 * sg;
    l1 += 4.67 * cg + 201.55 * sg;

    g = SwephData.STR*(a + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.133533 * cg + 0.041116 * sg;
    l1 +=  6.95 * cg + 187.07 * sg;

    g = SwephData.STR*(a - 2.*D + MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.133430 * cg + 0.041079 * sg;
    l1 +=  6.28 * cg + 169.08 * sg;

    g = SwephData.STR*(3.*Ve - 4.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.175074 * cg + 0.003035 * sg;
    l1 +=  49.17 * cg + 150.57 * sg;

    g = SwephData.STR*(2.*(Ea + D - MP) - 3.*Ju + 213534.);
    l1 +=  158.4 * Math.sin(g);
    l1 += moonpol[0];

    a = 0.1 * T; /* set amplitude scale of 1.0 = 10^-4 arcsec */
    moonpol[1] *= a;
    moonpol[2] *= a;
  }
//#else
  private void moon1() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.moon1()");
//#endif /* TRACE0 */
    double a;

    sscc( 0, SwephData.STR*D, 6 );
    sscc( 1, SwephData.STR*M,  4 );
    sscc( 2, SwephData.STR*MP, 4 );
    sscc( 3, SwephData.STR*NF, 4 );
    moonpol[0] = 0.0;
    moonpol[1] = 0.0;
    moonpol[2] = 0.0;
    /* terms in T^2, scale 1.0 = 10^-5" */
    chewm( LRT2, NLRT2, 4, 2, moonpol );
    chewm( BT2, NBT2, 4, 4, moonpol );
    f = 18 * Ve - 16 * Ea;
    g = SwephData.STR*(f - MP );  /* 18V - 16E - l */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l = 6.367278 * cg + 12.747036 * sg;  /* t^0 */
    l1 = 23123.70 * cg - 10570.02 * sg;  /* t^1 */
    l2 = z[12] * cg + z[13] * sg;        /* t^2 */
    moonpol[2] += 5.01 * cg + 2.72 * sg;
    g = SwephData.STR * (10.*Ve - 3.*Ea - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.253102 * cg + 0.503359 * sg;
    l1 += 1258.46 * cg + 707.29 * sg;
    l2 += z[14] * cg + z[15] * sg;
    g = SwephData.STR*(8.*Ve - 13.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.187231 * cg - 0.127481 * sg;
    l1 += -319.87 * cg - 18.34 * sg;
    l2 += z[16] * cg + z[17] * sg;
    a = 4.0*Ea - 8.0*Ma + 3.0*Ju;
    g = SwephData.STR * a;
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.866287 * cg + 0.248192 * sg;
    l1 += 41.87 * cg + 1053.97 * sg;
    l2 += z[18] * cg + z[19] * sg;
    g = SwephData.STR*(a - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.165009 * cg + 0.044176 * sg;
    l1 += 4.67 * cg + 201.55 * sg;
    g = SwephData.STR*f;  /* 18V - 16E */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.330401 * cg + 0.661362 * sg;
    l1 += 1202.67 * cg - 555.59 * sg;
    l2 += z[20] * cg + z[21] * sg;
    g = SwephData.STR*(f - 2.0*MP );  /* 18V - 16E - 2l */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.352185 * cg + 0.705041 * sg;
    l1 += 1283.59 * cg - 586.43 * sg;
    g = SwephData.STR * (2.0*Ju - 5.0*Sa);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.034700 * cg + 0.160041 * sg;
    l2 += z[22] * cg + z[23] * sg;
    g = SwephData.STR * (SWELP - NF);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.000116 * cg + 7.063040 * sg;
    l1 +=  298.8 * sg;
    /* T^3 terms */
    sg = Math.sin( SwephData.STR * M );
    /* l3 +=  z[24] * sg;                   moshier! l3 not initialized! */
    l3 =  z[24] * sg;
    l4 = 0;
    g = SwephData.STR * (2.0*D - M);
    sg = Math.sin(g);
    cg = Math.cos(g);
    moonpol[2] +=  -0.2655 * cg * T;
    g = SwephData.STR * (M - MP);
    moonpol[2] +=  -0.1568 * Math.cos( g ) * T;
    g = SwephData.STR * (M + MP);
    moonpol[2] +=  0.1309 * Math.cos( g ) * T;
    g = SwephData.STR * (2.0*(D + M) - MP);
    sg = Math.sin(g);
    cg = Math.cos(g);
    moonpol[2] +=   0.5568 * cg * T;
    l2 += moonpol[0];
    g = SwephData.STR*(2.0*D - M - MP);
    moonpol[2] +=  -0.1910 * Math.cos( g ) * T;
    moonpol[1] *= T;
    moonpol[2] *= T;
    /* terms in T */
    moonpol[0] = 0.0;
    chewm( BT, NBT, 4, 4, moonpol );
    chewm( LRT, NLRT, 4, 1, moonpol );
    g = SwephData.STR*(f - MP - NF - 2355767.6); /* 18V - 16E - l - F */
    moonpol[1] +=  -1127. * Math.sin(g);
    g = SwephData.STR*(f - MP + NF - 235353.6); /* 18V - 16E - l + F */
    moonpol[1] +=  -1123. * Math.sin(g);
    g = SwephData.STR*(Ea + D + 51987.6);
    moonpol[1] +=  1303. * Math.sin(g);
    g = SwephData.STR*SWELP;
    moonpol[1] +=  342. * Math.sin(g);
    g = SwephData.STR*(2.*Ve - 3.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l +=  -0.343550 * cg - 0.000276 * sg;
    l1 +=  105.90 * cg + 336.53 * sg;
    g = SwephData.STR*(f - 2.*D); /* 18V - 16E - 2D */
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.074668 * cg + 0.149501 * sg;
    l1 += 271.77 * cg - 124.20 * sg;
    g = SwephData.STR*(f - 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.073444 * cg + 0.147094 * sg;
    l1 += 265.24 * cg - 121.16 * sg;
    g = SwephData.STR*(f + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.072844 * cg + 0.145829 * sg;
    l1 += 265.18 * cg - 121.29 * sg;
    g = SwephData.STR*(f + 2.*(D - MP));
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.070201 * cg + 0.140542 * sg;
    l1 += 255.36 * cg - 116.79 * sg;
    g = SwephData.STR*(Ea + D - NF);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.288209 * cg - 0.025901 * sg;
    l1 += -63.51 * cg - 240.14 * sg;
    g = SwephData.STR*(2.*Ea - 3.*Ju + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += 0.077865 * cg + 0.438460 * sg;
    l1 += 210.57 * cg + 124.84 * sg;
    g = SwephData.STR*(Ea - 2.*Ma);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.216579 * cg + 0.241702 * sg;
    l1 += 197.67 * cg + 125.23 * sg;
    g = SwephData.STR*(a + MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.165009 * cg + 0.044176 * sg;
    l1 += 4.67 * cg + 201.55 * sg;
    g = SwephData.STR*(a + 2.*D - MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.133533 * cg + 0.041116 * sg;
    l1 +=  6.95 * cg + 187.07 * sg;
    g = SwephData.STR*(a - 2.*D + MP);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.133430 * cg + 0.041079 * sg;
    l1 +=  6.28 * cg + 169.08 * sg;
    g = SwephData.STR*(3.*Ve - 4.*Ea);
    cg = Math.cos(g);
    sg = Math.sin(g);
    l += -0.175074 * cg + 0.003035 * sg;
    l1 +=  49.17 * cg + 150.57 * sg;
    g = SwephData.STR*(2.*(Ea + D - MP) - 3.*Ju + 213534.);
    l1 +=  158.4 * Math.sin(g);
    l1 += moonpol[0];
    a = 0.1 * T; /* set amplitude scale of 1.0 = 10^-4 arcsec */
    moonpol[1] *= a;
    moonpol[2] *= a;
  }
//#endif  /* MOSH_MOON_200 */

  void moon2() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.moon2()");
//#endif /* TRACE0 */
    /* terms in T^0 */
    g = SwephData.STR*(2*(Ea-Ju+D)-MP+648431.172);
    l += 1.14307 * Math.sin(g);
    g = SwephData.STR*(Ve-Ea+648035.568);
    l += 0.82155 * Math.sin(g);
    g = SwephData.STR*(3*(Ve-Ea)+2*D-MP+647933.184);
    l += 0.64371 * Math.sin(g);
    g = SwephData.STR*(Ea-Ju+4424.04);
    l += 0.63880 * Math.sin(g);
    g = SwephData.STR*(SWELP + MP - NF + 4.68);
    l += 0.49331 * Math.sin(g);
    g = SwephData.STR*(SWELP - MP - NF + 4.68);
    l += 0.4914 * Math.sin(g);
    g = SwephData.STR*(SWELP+NF+2.52);
    l += 0.36061 * Math.sin(g);
    g = SwephData.STR*(2.*Ve - 2.*Ea + 736.2);
    l += 0.30154 * Math.sin(g);
    g = SwephData.STR*(2.*Ea - 3.*Ju + 2.*D - 2.*MP + 36138.2);
    l += 0.28282 * Math.sin(g);
    g = SwephData.STR*(2.*Ea - 2.*Ju + 2.*D - 2.*MP + 311.0);
    l += 0.24516 * Math.sin(g);
    g = SwephData.STR*(Ea - Ju - 2.*D + MP + 6275.88);
    l += 0.21117 * Math.sin(g);
    g = SwephData.STR*(2.*(Ea - Ma) - 846.36);
    l += 0.19444 * Math.sin(g);
    g = SwephData.STR*(2.*(Ea - Ju) + 1569.96);
    l -= 0.18457 * Math.sin(g);
    g = SwephData.STR*(2.*(Ea - Ju) - MP - 55.8);
    l += 0.18256 * Math.sin(g);
    g = SwephData.STR*(Ea - Ju - 2.*D + 6490.08);
    l += 0.16499 * Math.sin(g);
    g = SwephData.STR*(Ea - 2.*Ju - 212378.4);
    l += 0.16427 * Math.sin(g);
    g = SwephData.STR*(2.*(Ve - Ea - D) + MP + 1122.48);
    l += 0.16088 * Math.sin(g);
    g = SwephData.STR*(Ve - Ea - MP + 32.04);
    l -= 0.15350 * Math.sin(g);
    g = SwephData.STR*(Ea - Ju - MP + 4488.88);
    l += 0.14346 * Math.sin(g);
    g = SwephData.STR*(2.*(Ve - Ea + D) - MP - 8.64);
    l += 0.13594 * Math.sin(g);
    g = SwephData.STR*(2.*(Ve - Ea - D) + 1319.76);
    l += 0.13432 * Math.sin(g);
    g = SwephData.STR*(Ve - Ea - 2.*D + MP - 56.16);
    l -= 0.13122 * Math.sin(g);
    g = SwephData.STR*(Ve - Ea + MP + 54.36);
    l -= 0.12722 * Math.sin(g);
    g = SwephData.STR*(3.*(Ve - Ea) - MP + 433.8);
    l += 0.12539 * Math.sin(g);
    g = SwephData.STR*(Ea - Ju + MP + 4002.12);
    l += 0.10994 * Math.sin(g);
    g = SwephData.STR*(20.*Ve - 21.*Ea - 2.*D + MP - 317511.72);
    l += 0.10652 * Math.sin(g);
    g = SwephData.STR*(26.*Ve - 29.*Ea - MP + 270002.52);
    l += 0.10490 * Math.sin(g);
    g = SwephData.STR*(3.*Ve - 4.*Ea + D - MP - 322765.56);
    l += 0.10386 * Math.sin(g);
    g = SwephData.STR*(SWELP+648002.556);
    B =  8.04508 * Math.sin(g);
    g = SwephData.STR*(Ea+D+996048.252);
    B += 1.51021 * Math.sin(g);
    g = SwephData.STR*(f - MP + NF + 95554.332);
    B += 0.63037 * Math.sin(g);
    g = SwephData.STR*(f - MP - NF + 95553.792);
    B += 0.63014 * Math.sin(g);
    g = SwephData.STR*(SWELP - MP + 2.9);
    B +=  0.45587 * Math.sin(g);
    g = SwephData.STR*(SWELP + MP + 2.5);
    B +=  -0.41573 * Math.sin(g);
    g = SwephData.STR*(SWELP - 2.0*NF + 3.2);
    B +=  0.32623 * Math.sin(g);
    g = SwephData.STR*(SWELP - 2.0*D + 2.5);
    B +=  0.29855 * Math.sin(g);
  }

  void moon3() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.moon3()");
//#endif /* TRACE0 */
    /* terms in T^0 */
    moonpol[0] = 0.0;
    chewm( LR, NLR, 4, 1, moonpol );
    chewm( MB, NMB, 4, 3, moonpol );
    l += (((l4 * T + l3) * T + l2) * T + l1) * T * 1.0e-5;
    moonpol[0] = SWELP + l + 1.0e-4 * moonpol[0];
    moonpol[1] = 1.0e-4 * moonpol[1] + B;
    moonpol[2] = 1.0e-4 * moonpol[2] + 385000.52899; /* kilometers */
  }


  /* Compute final ecliptic polar coordinates
   */
  void moon4() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.moon4()");
//#endif /* TRACE0 */
    moonpol[2] /= SweConst.AUNIT / 1000;
    moonpol[0] = SwephData.STR * mods3600( moonpol[0] );
    moonpol[1] = SwephData.STR * moonpol[1];
    B = moonpol[1];
  }

  /* mean lunar node
   * J            julian day
   * pol          return array for position and velocity
   *              (polar coordinates of ecliptic of date)
   */
  int swi_mean_node(double J, double pol[], StringBuffer serr) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_mean_node(double, double[], StringBuffer)");
//#endif /* TRACE0 */
    return swi_mean_node(J, pol, 0, serr);
  }
  int swi_mean_node(double J, double pol[], int offs, StringBuffer serr) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_mean_node(double, double[], int, StringBuffer)");
//#endif /* TRACE0 */
//#if 0
//  double a, b, c;
//#endif /* 0 */
    String s;
    T = (J-SwephData.J2000)/36525.0;
    T2 = T*T;
    T3 = T*T2;
    T4 = T2*T2;
    /* with elements from swi_moshmoon2(), which are fitted to jpl-ephemeris */
    if (J < SwephData.MOSHNDEPH_START || J > SwephData.MOSHNDEPH_END) {
      if (serr != null) {
//#ifdef ORIGINAL
        s="jd "+cv.fmt("%f",J)+" outside mean node range "+
                cv.fmt("%.2f",SwephData.MOSHNDEPH_START)+" .. "+
                cv.fmt("%.2f",SwephData.MOSHNDEPH_END)+" ";
//#else
        s="jd "+J+" outside mean node range "+
                SwephData.MOSHNDEPH_START+" .. "+
                SwephData.MOSHNDEPH_END+" ";
//#endif /* ORIGINAL */
        serr.append(s);
      }
      return SweConst.ERR;
    }
    mean_elements();
    /* longitude */
    pol[offs] = sl.swi_mod2PI((SWELP - NF) * SwephData.STR);
    /* latitude */
    pol[offs+1] = 0.0;
    /* distance */
    pol[offs+2] = SwephData.MOON_MEAN_DIST / SweConst.AUNIT; /* or should it be derived from mean
                                      * orbital ellipse? */
//#if 0
//    a = pol[0];
//    /* Chapront, according to Meeus, German, p. 339 */
//    pol[0] = 125.0445550 - 1934.1361849 * T + 0.0020762 * T2 +
//             T3 / 467410 - T4 / 60616000;
//    pol[0] = sl.swi_mod2PI(pol[0] * SwissData.DEGTORAD);
//    c = pol[0];
//    System.out.print("mean node\n");
//    System.out.println("moshier de404 - chapront "+
//                      cv.fmt("%f",(a-c) * SwissData.RADTODEG * 3600)+"\"");
//#endif /* 0 */
    return SweConst.OK;
  }

  /* mean lunar apogee ('dark moon', 'lilith')
   * J            julian day
   * pol          return array for position
   *              (polar coordinates of ecliptic of date)
   * serr         error return string
   */
  int swi_mean_apog(double J, double pol[], StringBuffer serr) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_mean_apog(double, double[], StringBuffer)");
//#endif /* TRACE0 */
    return swi_mean_apog(J, pol, 0, serr);
  }
  int swi_mean_apog(double J, double pol[], int offs, StringBuffer serr) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_mean_apog(double, double[], int, StringBuffer)");
//#endif /* TRACE0 */
//#if 0
//    int i;
//    double a, b;
//    double x[]=new double[3];
//#endif /* 0 */
    double node;
    String s;
    T = (J-SwephData.J2000)/36525.0;
    T2 = T*T;
    T3 = T*T2;
    T4 = T2*T2;
    /* with elements from swi_moshmoon2(), which are fitted to jpl-ephemeris */
    if (J < SwephData.MOSHNDEPH_START || J > SwephData.MOSHNDEPH_END) {
      if (serr != null) {
//#ifdef ORIGINAL
        s="jd "+cv.fmt("%f",J)+" outside mean apogee range "+
                cv.fmt("%.2f",SwephData.MOSHNDEPH_START)+" .. "+
                cv.fmt("%.2f",SwephData.MOSHNDEPH_END)+" ";
//#else
        s="jd "+J+" outside mean apogee range "+
                SwephData.MOSHNDEPH_START+" .. "+
                SwephData.MOSHNDEPH_END+" ";
//#endif /* ORIGINAL */
        if (serr.length()+s.length() < SwissData.AS_MAXCH) {
          serr.append(s);
        }
      }
      return SweConst.ERR;
    }
    mean_elements();
//#if 0
//    a = pol[0];
//    /* Chapront, according to Meeus, German, p. 339 */
//    pol[0] = 83.3532430 + 4069.0137111 * T - 0.0103238 * T2
//             - T3 / 80053 + T4 / 18999000;
//    pol[0] = sl.swi_mod2PI(pol[0] * SwissData.DEGTORAD + SwephData.PI);
//    b = pol[0];
//    System.out.print("mean apogee\n");
//    System.out.println("moshier de404 - chapront "+
//            cv.fmt("%f",(a-b) * SwissData.RADTODEG * 3600)+"\"");
//#endif /* 0 */
    pol[offs] = sl.swi_mod2PI((SWELP - MP) * SwephData.STR + SwephData.PI);
    pol[offs+1] = 0;
    pol[offs+2] = SwephData.MOON_MEAN_DIST * (1 + SwephData.MOON_MEAN_ECC) /
                                                 SweConst.AUNIT; /* apogee */
//#if 0
//    pol[2] = 2 * SwephData.MOON_MEAN_ECC * SwephData.MOON_MEAN_DIST / SweConst.AUNIT;
//                                                            /* 2nd focus */
//#endif /* 0 */
    /* Lilith or Dark Moon is either the empty focal point of the mean
     * lunar ellipse or, for some people, its apogee ("aphelion").
     * This is 180 degrees from the perigee.
     *
     * Since the lunar orbit is not in the ecliptic, the apogee must be
     * projected onto the ecliptic.
     * Joelle de Gravelaine has in her book "Lilith der schwarze Mond"
     * (Astrodata, 1990) an ephemeris which gives noon (12.00) positions
     * but does not project them onto the ecliptic.
     * This results in a mistake of several arc minutes.
     *
     * There is also another problem. The other focal point doesn't
     * coincide with the geocenter but with the barycenter of the
     * earth-moon-system. The difference is about 4700 km. If one
     * took this into account, it would result in an oscillation
     * of the Black Moon. If defined as the apogee, this oscillation
     * would be about +/- 40 arcmin.
     * If defined as the second focus, the effect is very large:
     * +/- 6 deg!
     * We neglect this influence.
     */
    /* apogee is now projected onto ecliptic */
    node = (SWELP - NF) * SwephData.STR;
    pol[offs] = sl.swi_mod2PI(pol[offs] - node);
    sl.swi_polcart(pol, offs, pol, offs);
    sl.swi_coortrf(pol, offs, pol, offs, -SwephData.MOON_MEAN_INCL * SwissData.DEGTORAD);
    sl.swi_cartpol(pol, offs, pol, offs);
    pol[offs] = sl.swi_mod2PI(pol[offs] + node);
//#if 0
    /* speed */
    mean_elements(T-PLAN_SPEED_INTV, &SWELP, &MP, &NF, &M, &D);
    pol[3] = swi_mod2PI((SWELP - MP) * SwephData.STR + SwephData.PI);
    pol[4] = 0;
    pol[5] = MOON_MEAN_DIST * (1 + MOON_MEAN_ECC) / AUNIT; /* apogee */
//#if 0
    pol[2] = 2 * MOON_MEAN_ECC * MOON_MEAN_DIST / AUNIT; /* 2nd focus */
//#endif /* 0 */
    node = (SWELP - NF) * SwephData.STR;
    pol[3] = swi_mod2PI(pol[3] - node);
    swi_polcart(pol+3, pol+3);
    swi_coortrf(pol+3, pol+3, -MOON_MEAN_INCL * DEGTORAD);
    swi_cartpol(pol+3, pol+3);
    pol[3] = swi_mod2PI(pol[3] + node);
    for (i = 0; i <= 2; i++)
      pol[3+i] = pol[i] - pol[3+i];
    pol[3] = swi_mod2PI(pol[3]);
//#endif /* 0 */
    return SweConst.OK;
  }

  /* Program to step through the perturbation table
   */
  void chewm(short[] pt, int nlines, int nangles, int typflg, double[] ans ) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.chewm(short[], int, int, int, double[])");
//#endif /* TRACE0 */
    int npt=0;
    int i, j, k, k1, m;
    double cu, su, cv, sv, ff;
    for( i=0; i<nlines; i++ ) {
      k1 = 0;
      sv = 0.0;
      cv = 0.0;
      for( m=0; m<nangles; m++ ) {
        j = pt[npt++]; /* multiple angle factor */
        if( j!=0 ) {
          k = j;
          if( j < 0 ) {
            k = -k; /* make angle factor > 0 */
          }
          /* sin, cos (k*angle) from lookup table */
          su = ss[m][k-1];
          cu = cc[m][k-1];
          if( j < 0 ) {
            su = -su; /* negative angle factor */
          }
          if( k1 == 0 ) {
            /* Set sin, cos of first angle. */
            sv = su;
            cv = cu;
            k1 = 1;
          }
          else {
            /* Combine angles by trigonometry. */
            ff =  su*cv + cu*sv;
            cv = cu*cv - su*sv;
            sv = ff;
          }
        }
      }
      /* Accumulate
       */
      switch( typflg ) {
      /* large longitude and radius */
      case 1:
        j = pt[npt++];
        k = pt[npt++];
        ans[0] += (10000.0 * j  + k) * sv;
        j = pt[npt++];
        k = pt[npt++];
        if( k!=0 ) {
          ans[2] += (10000.0 * j  + k) * cv;
        }
        break;
      /* longitude and radius */
      case 2:
        j = pt[npt++];
        k = pt[npt++];
        ans[0] += j * sv;
        ans[2] += k * cv;
        break;
      /* large latitude */
      case 3:
        j = pt[npt++];
        k = pt[npt++];
        ans[1] += ( 10000.0*j + k)*sv;
        break;
      /* latitude */
      case 4:
        j = pt[npt++];
        ans[1] += j * sv;
        break;
      }
    }
  }

  /* Prepare lookup table of sin and cos ( i*Lj )
   * for required multiple angles
   */
  void sscc(int k, double arg, int n ) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.sscc(int, double, int)");
//#endif /* TRACE0 */
    double cu, su, cv, sv, s;
    int i;
    su = Math.sin(arg);
    cu = Math.cos(arg);
    ss[k][0] = su;                        /* sin(L) */
    cc[k][0] = cu;                        /* cos(L) */
    sv = 2.0*su*cu;
    cv = cu*cu - su*su;
    ss[k][1] = sv;                        /* sin(2L) */
    cc[k][1] = cv;
    for( i=2; i<n; i++ ) {
      s =  su*cv + cu*sv;
      cv = cu*cv - su*sv;
      sv = s;
      ss[k][i] = sv;              /* sin( i+1 L ) */
      cc[k][i] = cv;
    }
  }

  /* converts from polar coordinates of ecliptic of date
   *          to   cartesian coordinates of equator 2000
   * tjd          date
   * x            array of position
   */
  void ecldat_equ2000(double tjd, double[] xpm) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.ecldat_equ2000(double, double[])");
//#endif /* TRACE0 */
    /* cartesian */
    sl.swi_polcart(xpm, xpm);
    /* equatorial */
    sl.swi_coortrf2(xpm, xpm, -swed.oec.seps, swed.oec.ceps);
    /* j2000 */
    sl.swi_precess(xpm, tjd, SwephData.J_TO_J2000);/**/
  }

  /* Reduce arc seconds modulo 360 degrees
   * answer in arc seconds
   */
  double mods3600(double x) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.mods3600(double)");
//#endif /* TRACE0 */
    double lx;
    lx = x;
    lx = lx - 1296000.0 * Math.floor( lx/1296000.0 );
    return( lx );
  }


//#ifndef ASTROLOGY
  void swi_mean_lunar_elements(double tjd,
                               DblObj node, DblObj dnode,
                               DblObj peri, DblObj dperi) {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.swi_mean_lunar_elements(double, DblObj, DblObj, DblObj, DblObj)");
//#endif /* TRACE0 */
    T = (tjd - SwephData.J2000) / 36525.0;
    T2 = T*T;
    mean_elements();
    node.val = sl.swe_degnorm((SWELP - NF) * SwephData.STR * SwissData.RADTODEG);
    peri.val = sl.swe_degnorm((SWELP - MP) * SwephData.STR * SwissData.RADTODEG);
    T -= 1.0 / 36525;
    mean_elements();
    dnode.val = sl.swe_degnorm(node.val - (SWELP-NF) * SwephData.STR * SwissData.RADTODEG);
    dnode.val -= 360;
    dperi.val = sl.swe_degnorm(peri.val - (SWELP-MP) * SwephData.STR * SwissData.RADTODEG);
  }
//#endif /* ASTROLOGY */

  void mean_elements() {
//#ifdef TRACE0
    System.out.println(System.currentTimeMillis()+" Swemmoon.mean_elements()");
//#endif /* TRACE0 */
    double fracT = T%1.;
    /* Mean anomaly of sun = l' (J. Laskar) */
    M =  mods3600(129600000.0 * fracT - 3418.961646 * T +  1287104.76154);
    M += ((((((((
      1.62e-20 * T
    - 1.0390e-17 ) * T
    - 3.83508e-15 ) * T
    + 4.237343e-13 ) * T
    + 8.8555011e-11 ) * T
    - 4.77258489e-8 ) * T
    - 1.1297037031e-5 ) * T
    + 1.4732069041e-4 ) * T
    - 0.552891801772 ) * T2;
//#ifdef MOSH_MOON_200
    /* Mean distance of moon from its ascending node = F */
    NF = mods3600( 1739527263.0983 * T + 335779.55755 );
    /* Mean anomaly of moon = l */
    MP = mods3600( 1717915923.4728 * T +  485868.28096 );
    /* Mean elongation of moon = D */
    D = mods3600( 1602961601.4603 * T + 1072260.73512 );
    /* Mean longitude of moon */
    SWELP = mods3600( 1732564372.83264 * T +  785939.95571 );
    /* Higher degree secular terms found by least squares fit */
    NF += (((((z[5] *T+z[4] )*T + z[3] )*T + z[2] )*T + z[1] )*T + z[0] )*T2;
    MP += (((((z[11]*T+z[10])*T + z[9] )*T + z[8] )*T + z[7] )*T + z[6] )*T2;
    D  += (((((z[17]*T+z[16])*T + z[15])*T + z[14])*T + z[13])*T + z[12])*T2;
    SWELP += (((((z[23]*T+z[22])*T + z[21])*T + z[20])*T + z[19])*T + z[18])*T2;
//#else
    /* Mean distance of moon from its ascending node = F */
    /*NF = mods3600((1739527263.0983 - 2.079419901760e-01) * T +335779.55755);*/
    NF = mods3600(1739232000.0 * fracT + 295263.0983 * T -
                  2.079419901760e-01 * T + 335779.55755);
    /* Mean anomaly of moon = l */
    MP = mods3600(1717200000.0 * fracT + 715923.4728 * T -
                  2.035946368532e-01 * T + 485868.28096);
    /* Mean elongation of moon = D */
    D = mods3600(1601856000.0 * fracT + 1105601.4603 * T +
                 3.962893294503e-01 * T + 1072260.73512);
    /* Mean longitude of moon, referred to the mean ecliptic and equinox of date */
    SWELP = mods3600(1731456000.0 * fracT + 1108372.83264 * T - 6.784914260953e-01 * T +  785939.95571);
    /* Higher degree secular terms found by least squares fit */
    NF += ((z[2]*T + z[1])*T + z[0])*T2;
    MP += ((z[5]*T + z[4])*T + z[3])*T2;
    D  += ((z[8]*T + z[7])*T + z[6])*T2;
    SWELP += ((z[11]*T + z[10])*T + z[9])*T2;
//#endif /* MOSH_MOON_200 */
    /* sensitivity of mean elements
     *    delta argument = scale factor times delta amplitude (arcsec)
     * cos l  9.0019 = mean eccentricity
     * cos 2D 43.6
     * cos F  11.2 (latitude term)
     */
  }
  
  void mean_elements_pl() {
    /* Mean longitudes of planets (Laskar, Bretagnon) */
    Ve = mods3600( 210664136.4335482 * T + 655127.283046 );
    Ve += ((((((((
      -9.36e-023 * T
     - 1.95e-20 ) * T
     + 6.097e-18 ) * T
     + 4.43201e-15 ) * T
     + 2.509418e-13 ) * T
     - 3.0622898e-10 ) * T
     - 2.26602516e-9 ) * T
     - 1.4244812531e-5 ) * T
     + 0.005871373088 ) * T2;
    Ea = mods3600( 129597742.26669231  * T +  361679.214649 );
    Ea += (((((((( -1.16e-22 * T
     + 2.976e-19 ) * T
     + 2.8460e-17 ) * T
     - 1.08402e-14 ) * T
     - 1.226182e-12 ) * T
     + 1.7228268e-10 ) * T
     + 1.515912254e-7 ) * T
     + 8.863982531e-6 ) * T
     - 2.0199859001e-2 ) * T2;
    Ma = mods3600(  68905077.59284 * T + 1279559.78866 );
    Ma += (-1.043e-5*T + 9.38012e-3)*T2;
    Ju = mods3600( 10925660.428608 * T +  123665.342120 );
    Ju += (1.543273e-5*T - 3.06037836351e-1)*T2;
    Sa = mods3600( 4399609.65932 * T + 180278.89694 );
    Sa += (( 4.475946e-8*T - 6.874806E-5 ) * T + 7.56161437443E-1)*T2;
  }
  
  /* Calculate geometric coordinates of true interpolated Moon apsides
   */
  int swi_intp_apsides(double J, double[] pol, int ipli) {
    double dd;
    double rsv[] = new double[3];
    double sNF, sD, sLP, sMP, sM, sVe, sEa, sMa, sJu, sSa, fM, fVe, fEa, fMa, fJu, fSa, cMP, zMP, fNF, fD, fLP;
    double dMP, mLP, mNF, mD, mMP;
    int i, ii, iii, niter = 4;    /* niter: silence compiler warning */
    ii=1;
    zMP=27.55454988;
    fNF = 27.212220817/zMP;/**/
    fD  = 29.530588835/zMP;/**/
    fLP = 27.321582/zMP;/**/
    fM  = 365.2596359/zMP;
    fVe = 224.7008001/zMP;
    fEa = 365.2563629/zMP;
    fMa = 686.9798519/zMP;
    fJu = 4332.589348/zMP;
    fSa = 10759.22722/zMP;
    T = (J-SwephData.J2000)/36525.0;
    T2 = T*T;
    T4 = T2*T2;
    mean_elements();
    mean_elements_pl();
    sNF = NF;
    sD  = D;
    sLP = SWELP;
    sMP = MP;
    sM  = M ;
    sVe = Ve;
    sEa = Ea;
    sMa = Ma;
    sJu = Ju;
    sSa = Sa;
    sNF = mods3600(NF);
    sD  = mods3600(D);
    sLP = mods3600(SWELP);
    sMP = mods3600(MP);
    if (ipli == SwephData.SEI_INTP_PERG) {MP = 0.0; niter = 5;}
    if (ipli == SwephData.SEI_INTP_APOG) {MP = 648000.0; niter = 4;}
    cMP = 0;
    dd = 18000.0;
    for (iii= 0; iii<=niter; iii++) {/**/
      dMP = sMP - MP;
      mLP = sLP - dMP;
      mNF = sNF - dMP;
      mD  = sD  - dMP;
      mMP = sMP - dMP;
      for (ii = 0; ii <=2; ii++) {/**/
        MP = mMP + (ii-1)*dd;       /**/
        NF = mNF + (ii-1)*dd/fNF;
        D  = mD  + (ii-1)*dd/fD;
        SWELP = mLP + (ii-1)*dd/fLP;
        M  = sM  + (ii-1)*dd/fM ;
        Ve = sVe + (ii-1)*dd/fVe;
        Ea = sEa + (ii-1)*dd/fEa;
        Ma = sMa + (ii-1)*dd/fMa;
        Ju = sJu + (ii-1)*dd/fJu;
        Sa = sSa + (ii-1)*dd/fSa;
        moon1();
        moon2();
        moon3();
        moon4();
        if (ii==1) {
          for( i=0; i<3; i++ ) pol[i] = moonpol[i];
        }
        rsv[ii] = moonpol[2];
      }
      cMP = (1.5*rsv[0] - 2*rsv[1] + 0.5*rsv[2]) / (rsv[0] + rsv[2] - 2*rsv[1]);/**/
      cMP *= dd;
      cMP = cMP - dd;
      mMP += cMP;
      MP = mMP;
      dd /= 10;
    }
    return(0);
  }

} // End of class Swemmoon
