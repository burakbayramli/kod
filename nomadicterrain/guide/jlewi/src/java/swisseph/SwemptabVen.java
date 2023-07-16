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

class SwemptabVen {
  /*
  First date in file = 1228000.50
  Number of records = 397276.0
  Days per record = 4.0
        Julian Years      Lon    Lat    Rad
   -1349.9 to  -1000.0:   0.23   0.15   0.10
   -1000.0 to   -500.0:   0.25   0.15   0.10
    -500.0 to      0.0:   0.20   0.13   0.09
       0.0 to    500.0:   0.16   0.11   0.08
     500.0 to   1000.0:   0.19   0.09   0.08
    1000.0 to   1500.0:   0.16   0.09   0.08
    1500.0 to   2000.0:   0.21   0.12   0.08
    2000.0 to   2500.0:   0.28   0.14   0.09
    2500.0 to   3000.0:   0.30   0.15   0.10
    3000.0 to   3000.8:  0.116  0.062  0.058
  */
  static final double ventabl[] = {
           9.08078,          55.42416, 21066413644.98911,      655127.20186,

           0.00329,           0.10408,

           0.00268,          -0.01908,

           0.00653,           0.00183,

           0.15083,          -0.21997,

           6.08596,           2.34841,           3.70668,          -0.22740,
          -2.29376,          -1.46741,

          -0.03840,           0.01242,

           0.00176,           0.00913,

           0.00121,          -0.01222,

          -1.22624,           0.65264,          -1.15974,          -1.28172,
           1.00656,          -0.66266,

           0.01560,          -0.00654,           0.00896,           0.00069,

           0.21649,          -0.01786,

           0.01239,           0.00255,

           0.00084,          -0.06086,

          -0.00041,           0.00887,

           0.13453,          -0.20013,           0.08234,           0.01575,

           0.00658,          -0.00214,

           0.00254,           0.00857,

          -0.01047,          -0.00519,

           0.63215,          -0.40914,           0.34271,          -1.53258,

           0.00038,          -0.01437,

          -0.02599,          -2.27805,          -0.36873,          -1.01799,
          -0.36798,           1.41356,

          -0.08167,           0.01368,           0.20676,           0.06807,

           0.02282,          -0.04691,

           0.30308,          -0.20218,           0.24785,           0.27522,

           0.00197,          -0.00499,

           1.43909,          -0.46154,           0.93459,           2.99583,
          -3.43274,           0.05672,

          -0.06586,           0.12467,           0.02505,          -0.08433,

           0.00743,           0.00174,

          -0.04013,           0.17715,

          -0.00603,          -0.01024,

           0.01542,          -0.02378,

           0.00676,           0.00002,

          -0.00168,          -4.89487,

           0.02393,          -0.03064,

           0.00090,           0.00977,

           0.01223,           0.00381,

           0.28135,          -0.09158,           0.18550,           0.58372,
          -0.67437,           0.01409,

          -0.25404,          -0.06863,

           0.06763,          -0.02939,

          -0.00009,          -0.04888,

           0.01718,          -0.00978,

          -0.01945,           0.08847,

          -0.00135,         -11.29920,

           0.01689,          -0.04756,

           0.02075,          -0.01667,

           0.01397,           0.00443,

          -0.28437,           0.07600,           0.17996,          -0.44326,

           0.29356,           1.41869,          -1.58617,           0.03206,

           0.00229,          -0.00753,

          -0.03076,          -2.96766,

           0.00245,           0.00697,

           0.01063,          -0.02468,

          -0.00351,          -0.18179,

          -0.01088,           0.00380,

           0.00496,           0.02072,

          -0.12890,           0.16719,          -0.06820,          -0.03234,

         -60.36135,         -11.74485,         -11.03752,          -3.80145,
         -21.33955,        -284.54495,        -763.43839,         248.50823,
        1493.02775,        1288.79621,       -2091.10921,       -1851.15420,

          -0.00922,           0.06233,

           0.00004,           0.00785,

           0.10363,          -0.16770,           0.45497,           0.24051,
          -0.28057,           0.61126,

          -0.02057,           0.00010,

           0.00561,           0.01994,

           0.01416,          -0.00442,

           0.03073,          -0.14961,

          -0.06272,           0.08301,

           0.02040,           7.12824,

          -0.00453,          -0.01815,

           0.00004,          -0.00013,

          -0.03593,          -0.18147,           0.20353,          -0.00683,

           0.00003,           0.06226,

          -0.00443,           0.00257,

           0.03194,           0.03254,

           0.00282,          -0.01401,

           0.00422,           1.03169,

          -0.00169,          -0.00591,

          -0.00307,           0.00540,

           0.05511,           0.00347,

           0.07896,           0.06583,

           0.00783,           0.01926,

           0.03109,           0.15967,

           0.00343,           0.88734,

           0.01047,           0.32054,

           0.00814,           0.00051,

           0.02474,           0.00047,

           0.00052,           0.03763,

         -57.06618,          20.34614,         -45.06541,        -115.20465,
         136.46887,         -84.67046,          92.93308,         160.44644,

          -0.00020,          -0.00082,

           0.02496,           0.00279,

           0.00849,           0.00195,

          -0.05013,          -0.04331,

          -0.00136,           0.14491,

          -0.00183,          -0.00406,

           0.01163,           0.00093,

          -0.00604,          -0.00680,

          -0.00036,           0.06861,

          -0.00450,          -0.00969,

           0.00171,           0.00979,

          -0.00152,           0.03929,

           0.00631,           0.00048,

          -0.00709,          -0.00864,

           1.51002,          -0.24657,           1.27338,           2.64699,
          -2.40990,          -0.57413,

          -0.00023,           0.03528,

           0.00268,           0.00522,

          -0.00010,           0.01933,

          -0.00006,           0.01100,

           0.06313,          -0.09939,           0.08571,           0.03206,

          -0.00004,           0.00645,

  };
  static final double ventabb[] = {
         -23.91858,          31.44154,          25.93273,         -67.68643,

          -0.00171,           0.00123,

           0.00001,          -0.00018,

          -0.00005,           0.00018,

          -0.00001,           0.00019,

           0.00733,           0.00030,          -0.00038,           0.00011,
           0.00181,           0.00120,

           0.00010,           0.00002,

          -0.00012,           0.00002,

           0.00021,           0.00004,

          -0.00403,           0.00101,           0.00342,          -0.00328,
           0.01564,           0.01212,

           0.00011,           0.00010,          -0.00002,          -0.00004,

          -0.00524,           0.00079,

           0.00011,           0.00002,

          -0.00001,           0.00003,

           0.00001,           0.00000,

           0.00108,           0.00035,           0.00003,           0.00064,

          -0.00000,          -0.00002,

          -0.00069,           0.00031,

           0.00020,           0.00003,

           0.00768,           0.03697,          -0.07906,           0.01673,

          -0.00003,          -0.00001,

          -0.00198,          -0.01045,           0.01761,          -0.00803,
          -0.00751,           0.04199,

           0.00280,          -0.00213,          -0.00482,          -0.00209,

          -0.01077,           0.00715,

           0.00048,          -0.00004,           0.00199,           0.00237,

           0.00017,          -0.00032,

          -0.07513,          -0.00658,          -0.04213,           0.16065,
           0.27661,           0.06515,

           0.02156,          -0.08144,          -0.23994,          -0.05674,

           0.00167,           0.00069,

           0.00244,          -0.01247,

          -0.00100,           0.00036,

           0.00240,           0.00012,

           0.00010,           0.00018,

           0.00208,          -0.00098,

          -0.00217,           0.00707,

          -0.00338,           0.01260,

          -0.00127,          -0.00039,

          -0.03516,          -0.00544,          -0.01746,           0.08258,
           0.10633,           0.02523,

           0.00077,          -0.00214,

          -0.02335,           0.00976,

          -0.00019,           0.00003,

           0.00041,           0.00039,

           0.00199,          -0.01098,

           0.00813,          -0.00853,

           0.02230,           0.00349,

          -0.02250,           0.08119,

          -0.00214,          -0.00052,

          -0.00220,           0.15216,           0.17152,           0.08051,

          -0.01561,           0.27727,           0.25837,           0.07021,

          -0.00005,          -0.00000,

          -0.02692,          -0.00047,

          -0.00007,          -0.00016,

           0.01072,           0.01418,

          -0.00076,           0.00379,

          -0.00807,           0.03463,

          -0.05199,           0.06680,

          -0.00622,           0.00787,           0.00672,           0.00453,

         -10.69951,         -67.43445,        -183.55956,         -37.87932,
        -102.30497,        -780.40465,        2572.21990,        -446.97798,
        1665.42632,        5698.61327,      -11889.66501,        2814.93799,

           0.03204,          -0.09479,

           0.00014,          -0.00001,

          -0.04118,          -0.04562,           0.03435,          -0.05878,
           0.01700,           0.02566,

          -0.00121,           0.00170,

           0.02390,           0.00403,

           0.04629,           0.01896,

          -0.00521,           0.03215,

          -0.01051,           0.00696,

          -0.01332,          -0.08937,

          -0.00469,          -0.00751,

           0.00016,          -0.00035,

           0.00492,          -0.03930,          -0.04742,          -0.01013,

           0.00065,           0.00021,

          -0.00006,           0.00017,

           0.06768,          -0.01558,

          -0.00055,           0.00322,

          -0.00287,          -0.01656,

           0.00061,          -0.00041,

           0.00030,           0.00047,

          -0.01436,          -0.00148,

           0.30302,          -0.05511,

          -0.00020,          -0.00005,

           0.00042,          -0.00025,

           0.01270,           0.00458,

          -0.00593,          -0.04480,

           0.00005,          -0.00008,

           0.08457,          -0.01569,

           0.00062,           0.00018,

           9.79942,          -2.48836,           4.17423,           6.72044,
         -63.33456,          34.63597,          39.11878,         -72.89581,

          -0.00066,           0.00036,

          -0.00045,          -0.00062,

          -0.00287,          -0.00118,

          -0.21879,           0.03947,

           0.00086,           0.00671,

          -0.00113,           0.00122,

          -0.00193,          -0.00029,

          -0.03612,           0.00635,

           0.00024,           0.00207,

          -0.00273,           0.00443,

          -0.00055,           0.00030,

          -0.00451,           0.00175,

          -0.00110,          -0.00015,

          -0.02608,           0.00480,

           2.16555,          -0.70419,           1.74648,           0.97514,
          -1.15360,           1.73688,

           0.00004,           0.00105,

           0.00187,          -0.00311,

           0.00005,           0.00055,

           0.00004,           0.00032,

          -0.04629,           0.02292,          -0.00363,          -0.03807,

           0.00002,           0.00020,

  };
  static final double ventabr[] = {
          -0.24459,           3.72698,          -6.67281,           5.24378,

           0.00030,           0.00003,

          -0.00002,          -0.00000,

          -0.00000,           0.00001,

           0.00032,           0.00021,

          -0.00326,           0.01002,           0.00067,           0.00653,
           0.00243,          -0.00417,

          -0.00004,          -0.00010,

          -0.00002,          -0.00001,

           0.00004,          -0.00002,

          -0.00638,          -0.01453,           0.01458,          -0.01235,
           0.00755,           0.01030,

           0.00006,           0.00014,           0.00000,           0.00009,

           0.00063,           0.00176,

           0.00003,          -0.00022,

           0.00112,           0.00001,

          -0.00014,          -0.00001,

           0.00485,           0.00322,          -0.00035,           0.00198,

           0.00004,           0.00013,

          -0.00015,          -0.00003,

           0.00011,          -0.00025,

           0.00634,           0.02207,           0.04620,           0.00160,

           0.00045,           0.00001,

          -0.11563,           0.00643,          -0.05947,           0.02018,
           0.07704,           0.01574,

          -0.00090,          -0.00471,          -0.00322,           0.01104,

           0.00265,          -0.00038,

           0.01395,           0.02165,          -0.01948,           0.01713,

          -0.00057,          -0.00019,

           0.04889,           0.13403,          -0.28327,           0.10597,
          -0.02325,          -0.35829,

           0.01171,          -0.00904,           0.00747,           0.02546,

           0.00029,          -0.00190,

          -0.03408,          -0.00703,

           0.00176,          -0.00109,

           0.00463,           0.00293,

           0.00000,           0.00148,

           1.06691,          -0.00054,

          -0.00935,          -0.00790,

           0.00552,          -0.00084,

          -0.00100,           0.00336,

           0.02874,           0.08604,          -0.17876,           0.05973,
          -0.00720,          -0.21195,

           0.02134,          -0.07980,

           0.01500,           0.01398,

           0.01758,          -0.00004,

           0.00371,           0.00650,

          -0.03375,          -0.00723,

           4.65465,          -0.00040,

           0.02040,           0.00707,

          -0.00727,          -0.01144,

          -0.00196,           0.00620,

          -0.03396,          -0.12904,           0.20160,           0.08092,

          -0.67045,           0.14014,          -0.01571,          -0.75141,

           0.00361,           0.00110,

           1.42165,          -0.01499,

          -0.00334,           0.00117,

           0.01187,           0.00507,

           0.08935,          -0.00174,

          -0.00211,          -0.00525,

           0.01035,          -0.00252,

          -0.08355,          -0.06442,           0.01616,          -0.03409,

           5.55241,         -30.62428,           2.03824,          -6.26978,
         143.07279,         -10.24734,        -125.25411,        -380.85360,
        -644.78411,         745.02852,         926.70000,       -1045.09820,

          -0.03124,          -0.00465,

          -0.00396,           0.00002,

           0.08518,           0.05248,          -0.12178,           0.23023,
          -0.30943,          -0.14208,

          -0.00005,          -0.01054,

          -0.00894,           0.00233,

          -0.00173,          -0.00768,

           0.07881,           0.01633,

          -0.04463,          -0.03347,

          -3.92991,           0.00945,

           0.01524,          -0.00422,

          -0.00011,          -0.00005,

           0.10842,          -0.02126,           0.00349,           0.12097,

          -0.03752,           0.00001,

          -0.00156,          -0.00270,

          -0.01520,           0.01349,

           0.00895,           0.00186,

          -0.67751,           0.00180,

           0.00516,          -0.00151,

          -0.00365,          -0.00210,

          -0.00276,           0.03793,

          -0.02637,           0.03235,

          -0.01343,           0.00541,

          -0.11270,           0.02169,

          -0.63365,           0.00122,

          -0.24329,           0.00428,

          -0.00040,           0.00586,

           0.00581,           0.01112,

          -0.02731,           0.00008,

          -2.69091,           0.42729,           2.78805,           3.43849,
          -0.87998,          -6.62373,           0.56882,           4.69370,

           0.00005,          -0.00008,

          -0.00181,           0.01767,

          -0.00168,           0.00660,

           0.01802,          -0.01836,

          -0.11245,          -0.00061,

           0.00199,          -0.00070,

          -0.00076,           0.00919,

           0.00311,          -0.00165,

          -0.05650,          -0.00018,

           0.00121,          -0.00069,

          -0.00803,           0.00146,

          -0.03260,          -0.00072,

          -0.00042,           0.00524,

           0.00464,          -0.00339,

          -0.06203,          -0.00278,           0.04145,           0.02871,
          -0.01962,          -0.01362,

          -0.03040,          -0.00010,

           0.00085,          -0.00001,

          -0.01712,          -0.00006,

          -0.00996,          -0.00003,

          -0.00029,           0.00026,           0.00016,          -0.00005,

          -0.00594,          -0.00003,

  };

  static byte venargs[] = {
  (byte)0,  (byte)3,
  (byte)2,  (byte)2,  (byte)5, (byte)-5,  (byte)6,  (byte)0,
  (byte)3,  (byte)2,  (byte)2,  (byte)1,  (byte)3, (byte)-8,  (byte)4,  (byte)0,
  (byte)3,  (byte)5,  (byte)1,(byte)-14,  (byte)2,  (byte)2,  (byte)3,  (byte)0,
  (byte)3,  (byte)3,  (byte)2, (byte)-7,  (byte)3,  (byte)4,  (byte)4,  (byte)0,
  (byte)2,  (byte)8,  (byte)2,(byte)-13,  (byte)3,  (byte)2,
  (byte)3,  (byte)6,  (byte)2,(byte)-10,  (byte)3,  (byte)3,  (byte)5,  (byte)0,
  (byte)1,  (byte)1,  (byte)7,  (byte)0,
  (byte)2,  (byte)1,  (byte)5, (byte)-2,  (byte)6,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-3,  (byte)4,  (byte)2,
  (byte)2,  (byte)2,  (byte)5, (byte)-4,  (byte)6,  (byte)1,
  (byte)1,  (byte)1,  (byte)6,  (byte)0,
  (byte)3,  (byte)3,  (byte)2, (byte)-5,  (byte)3,  (byte)1,  (byte)5,  (byte)0,
  (byte)3,  (byte)3,  (byte)2, (byte)-5,  (byte)3,  (byte)2,  (byte)5,  (byte)0,
  (byte)2,  (byte)1,  (byte)5, (byte)-1,  (byte)6,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-6,  (byte)4,  (byte)1,
  (byte)2,  (byte)2,  (byte)5, (byte)-3,  (byte)6,  (byte)0,
  (byte)1,  (byte)2,  (byte)6,  (byte)0,
  (byte)2,  (byte)3,  (byte)5, (byte)-5,  (byte)6,  (byte)0,
  (byte)1,  (byte)1,  (byte)5,  (byte)1,
  (byte)2,  (byte)2,  (byte)5, (byte)-2,  (byte)6,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-5,  (byte)3,  (byte)2,
  (byte)2,  (byte)5,  (byte)2, (byte)-8,  (byte)3,  (byte)1,
  (byte)1,  (byte)2,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)1, (byte)-5,  (byte)2,  (byte)1,
  (byte)2,  (byte)6,  (byte)2,(byte)-10,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-3,  (byte)3,  (byte)2,
  (byte)2,  (byte)1,  (byte)2, (byte)-2,  (byte)3,  (byte)1,
  (byte)2,  (byte)4,  (byte)2, (byte)-7,  (byte)3,  (byte)0,
  (byte)2,  (byte)4,  (byte)2, (byte)-6,  (byte)3,  (byte)0,
  (byte)1,  (byte)1,  (byte)4,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-2,  (byte)4,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-5,  (byte)4,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-1,  (byte)3,  (byte)0,
  (byte)2,  (byte)1,  (byte)1, (byte)-3,  (byte)2,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-4,  (byte)3,  (byte)0,
  (byte)2,  (byte)6,  (byte)2, (byte)-9,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-4,  (byte)3,  (byte)2,
  (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)2,  (byte)0,
  (byte)1,  (byte)1,  (byte)3,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-1,  (byte)4,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-4,  (byte)4,  (byte)0,
  (byte)2,  (byte)5,  (byte)2, (byte)-7,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-2,  (byte)3,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-3,  (byte)5,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-3,  (byte)3,  (byte)0,
  (byte)2,  (byte)7,  (byte)2,(byte)-10,  (byte)3,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-2,  (byte)5,  (byte)1,
  (byte)2,  (byte)4,  (byte)2, (byte)-5,  (byte)3,  (byte)1,
  (byte)3,  (byte)1,  (byte)2,  (byte)1,  (byte)5, (byte)-5,  (byte)6,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-1,  (byte)5,  (byte)0,
  (byte)3,  (byte)1,  (byte)2, (byte)-3,  (byte)5,  (byte)5,  (byte)6,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-2,  (byte)6,  (byte)0,
  (byte)2,  (byte)1,  (byte)2, (byte)-1,  (byte)6,  (byte)0,
  (byte)1,  (byte)3,  (byte)4,  (byte)0,
  (byte)2,  (byte)7,  (byte)2,(byte)-13,  (byte)3,  (byte)0,
  (byte)3,  (byte)1,  (byte)2,  (byte)2,  (byte)5, (byte)-5,  (byte)6,  (byte)1,
  (byte)1,  (byte)1,  (byte)2,  (byte)5,
  (byte)2,  (byte)9,  (byte)2,(byte)-13,  (byte)3,  (byte)0,
  (byte)3,  (byte)1,  (byte)2,  (byte)1,  (byte)5, (byte)-2,  (byte)6,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-3,  (byte)4,  (byte)2,
  (byte)2,  (byte)3,  (byte)2, (byte)-6,  (byte)4,  (byte)0,
  (byte)2,  (byte)1,  (byte)2,  (byte)1,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-5,  (byte)3,  (byte)0,
  (byte)2,  (byte)6,  (byte)2, (byte)-8,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)1, (byte)-4,  (byte)2,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-3,  (byte)3,  (byte)0,
  (byte)1,  (byte)2,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-7,  (byte)3,  (byte)0,
  (byte)2,  (byte)5,  (byte)2, (byte)-6,  (byte)3,  (byte)1,
  (byte)2,  (byte)2,  (byte)2, (byte)-2,  (byte)4,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-5,  (byte)4,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-1,  (byte)3,  (byte)0,
  (byte)2,  (byte)7,  (byte)2, (byte)-9,  (byte)3,  (byte)0,
  (byte)2,  (byte)4,  (byte)2, (byte)-4,  (byte)3,  (byte)0,
  (byte)2,  (byte)1,  (byte)2,  (byte)1,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-4,  (byte)4,  (byte)0,
  (byte)2,  (byte)6,  (byte)2, (byte)-7,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-2,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-4,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-3,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-2,  (byte)5,  (byte)0,
  (byte)2,  (byte)5,  (byte)2, (byte)-5,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-3,  (byte)6,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-1,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)2, (byte)-2,  (byte)6,  (byte)0,
  (byte)1,  (byte)2,  (byte)2,  (byte)3,
  (byte)2,  (byte)2,  (byte)2,  (byte)1,  (byte)5,  (byte)0,
  (byte)2,  (byte)7,  (byte)2, (byte)-8,  (byte)3,  (byte)0,
  (byte)2,  (byte)2,  (byte)1, (byte)-3,  (byte)2,  (byte)0,
  (byte)2,  (byte)4,  (byte)2, (byte)-3,  (byte)3,  (byte)0,
  (byte)2,  (byte)6,  (byte)2, (byte)-6,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-1,  (byte)3,  (byte)0,
  (byte)2,  (byte)8,  (byte)2, (byte)-9,  (byte)3,  (byte)0,
  (byte)2,  (byte)5,  (byte)2, (byte)-4,  (byte)3,  (byte)0,
  (byte)2,  (byte)7,  (byte)2, (byte)-7,  (byte)3,  (byte)0,
  (byte)2,  (byte)4,  (byte)2, (byte)-2,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-4,  (byte)5,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-3,  (byte)5,  (byte)0,
  (byte)2,  (byte)9,  (byte)2,(byte)-10,  (byte)3,  (byte)0,
  (byte)2,  (byte)3,  (byte)2, (byte)-2,  (byte)5,  (byte)0,
  (byte)1,  (byte)3,  (byte)2,  (byte)2,
  (byte)2,  (byte)8,  (byte)2, (byte)-8,  (byte)3,  (byte)0,
  (byte)2,  (byte)5,  (byte)2, (byte)-3,  (byte)3,  (byte)0,
  (byte)2,  (byte)9,  (byte)2, (byte)-9,  (byte)3,  (byte)0,
  (byte)2, (byte)10,  (byte)2,(byte)-10,  (byte)3,  (byte)0,
  (byte)1,  (byte)4,  (byte)2,  (byte)1,
  (byte)2, (byte)11,  (byte)2,(byte)-11,  (byte)3,  (byte)0,
 (byte)-1
  };
  /* Total terms = 108, small = 107 */
  static Plantbl ven404 = new Plantbl(
                               new short[]{5, 14, 13,  8,  4,  5,  1,  0,  0},
                               (short)5,
                               venargs,
                               ventabl,
                               ventabb,
                               ventabr,
                               7.2332982000000001e-01
                              );
}
