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

class PlanData {
  double x[]=new double[6]; /* position and speed vectors equatorial J2000 */
  double xreturn[]=new double[24]; /* return positions:
                         * xreturn+0    ecliptic polar coordinates
                         * xreturn+6    ecliptic cartesian coordinates
                         * xreturn+12   equatorial polar coordinates
                         * xreturn+18   equatorial cartesian coordinates
                         */

  /* the following data are read from file only once, immediately after
   * file has been opened */
  int ibdy=0;           /* internal body number */
  int iflg=0;          /* contains several bit flags describing the data:
                         * SEI_FLG_HELIO: true if helio, false if bary
                         * SEI_FLG_ROTATE: TRUE if coefficients are referred
                         *      to coordinate system of orbital plane
                         * SEI_FLG_ELLIPSE: TRUE if reference ellipse */
  int ncoe=0;           /* # of coefficients of ephemeris polynomial,
                           is polynomial order + 1  */
  /* where is the segment index on the file */
  long lndx0=0;         /* file position of begin of planet's index */
  int nndx=0;           /* number of index entries on file: computed */
  double tfstart=0;     /* file contains ephemeris for tfstart thru tfend */
  double tfend=0;       /*      for this particular planet !!!            */
  double dseg=0;        /* segment size (days covered by a polynomial)  */
  /* orbital elements: */
  double telem=0;       /* epoch of elements */
  double prot=0;
  double qrot=0;
  double dprot=0;
  double dqrot=0;
  double rmax=0;        /* normalisation factor of cheby coefficients */
  /* in addition, if reference ellipse is used: */
  double peri=0;
  double dperi=0;
////  double *refep;        /* pointer to cheby coeffs of reference ellipse,
////                         * size of data is 2 x ncoe */
  double refep[]=null;  /* pointer to cheby coeffs of reference ellipse,
                         * size of data is 2 x ncoe */
  /* unpacked segment information, only updated when a segment is read: */
  double tseg0=0, tseg1=0;  /* start and end jd of current segment */
////  double *segp;         /* pointer to unpacked cheby coeffs of segment;
////                         * the size is 3 x ncoe */
  double segp[]=null;   /* pointer to unpacked cheby coeffs of segment;
                         * the size is 3 x ncoe */
  int neval=0;          /* how many coefficients to evaluate. this may
                         * be less than ncoe */
  /* result of most recent data evaluation for this body: */
  double teval=0;       /* time for which previous computation was made */
  int iephe=0;          /* which ephemeris was used */
  int xflgs=0;         /* hel., light-time, aberr., prec. flags etc. */

  PlanData() {
    for(int i=0;i<x.length;i++) { x[i]=0.; } // Really?
    for(int i=0;i<x.length;i++) { xreturn[i]=0.; } // Really?
  }


  void clearData() {
    int j;

    ibdy=0;
    iflg=0;
    ncoe=0;
    lndx0=0;
    nndx=0;
    tfstart=0.0;
    tfend=0.0;
    dseg=0.0;
    telem=0.0;
    prot=0.0;
    qrot=0.0;
    dprot=0.0;
    dqrot=0.0;
    rmax=0.0;
    peri=0.0;
    dperi=0.0;
    tseg0=0.0;
    tseg1=0.0;
    neval=0;
    teval=0.0;
    iephe=0;
    xflgs=0;
    for(j=0; j<x.length; j++) { x[j]=0.0; }
    for(j=0; j<xreturn.length; j++) { xreturn[j]=0.0; }
    refep=null;
    segp=null;
  }
}
