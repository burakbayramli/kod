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

public class SwissData {

  /**
  * The character to be used as the degree character. Only textmode
  * applications (and probably awt applications in Java 1.1 and below?)
  * will require to differentiate between different characters, awt&nbsp;/
  * swing components of Java 1.2 and above will use the unicode encoding
  * always!
  */
  public String ODEGREE_CHAR=""+'\u00b0'; // Unicode degree character 176
                                          // Identical in most ISO-8859 sets


  public static final String ayanamsa_name[] = {
     "Fagan/Bradley",
     "Lahiri",
     "De Luce",
     "Raman",
     "Ushashashi",
     "Krishnamurti",
     "Djwhal Khul",
     "Yukteshwar",
     "J.N. Bhasin",
     "Babylonian/Kugler 1",
     "Babylonian/Kugler 2",
     "Babylonian/Kugler 3",
     "Babylonian/Huber",
     "Babylonian/Eta Piscium",
     "Babylonian/Aldebaran = 15 Tau",
     "Hipparchos",
     "Sassanian",
     "Galact. Center = 0 Sag",
     "J2000",
     "J1900",
     "B1950",
  };

//////////////////////////////////////////////////////////////////////////////
// sweodef.h: ////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
  static final double M_PI=3.14159265358979323846;

  public static final int AS_MAXCH=256; // used for string declarations,
                                        // allowing 255 char+\0

  static final double DEGTORAD=0.0174532925199433;
  static final double RADTODEG=57.2957795130823;

  static final int DEG=360000;  // degree expressed in centiseconds
  static final int DEG7_30=2700000;	// 7.5 degrees
  static final int DEG15=15 * DEG;
  static final int DEG24=24 * DEG;
  static final int DEG30=30 * DEG;
  static final int DEG60=60 * DEG;
  static final int DEG90=90 * DEG;
  static final int DEG120=120 * DEG;
  static final int DEG150=150 * DEG;
  static final int DEG180=180 * DEG;
  static final int DEG270=270 * DEG;
  static final int DEG360=360 * DEG;

  static final double CSTORAD=4.84813681109536E-08; // centisec to rad:
                                                    // pi / 180 /3600/100
  static final double RADTOCS=2.06264806247096E+07; // rad to centisec
                                                    // 180*3600*100/pi

  static final double CS2DEG=1.0/360000.0;	     // centisec to degree

  static final String BFILE_R_ACCESS="r";  // open binary file for reading
  static final String BFILE_RW_ACCESS="r+";// open binary file for writing and reading
  static final String BFILE_W_CREATE="w";  // create/open binary file for write
  static final String BFILE_A_ACCESS="a+"; // create/open binary file for append
  static final String FILE_R_ACCESS="r";   // open text file for reading
  static final String FILE_RW_ACCESS="r+"; // open text file for writing and reading
  static final String FILE_W_CREATE="w";   // create/open text file for write
  static final String FILE_A_ACCESS="a+";  // create/open text file for append
  static final int O_BINARY=0;	           // for open(), not defined in Unix
  static final int OPEN_MODE=0666;         // default file creation mode
  public String DIR_GLUE;              // glue string for directory/file
  public static final String PATH_SEPARATOR=";:"; // semicolon or colon may be used


//////////////////////////////////////////////////////////////////////////////
// swephexp.h: ///////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
  public static final int SE_NSIDM_PREDEF         =21;

//  static final int SE_MAX_STNAME=20;    // maximum size of fixstar name;
//                                        // the parameter star in swe_fixstar
//					// must allow twice this space for
//				        // the returned star name.
//

  static final int pnoext2int[] = {SwephData.SEI_SUN, SwephData.SEI_MOON,
    SwephData.SEI_MERCURY, SwephData.SEI_VENUS, SwephData.SEI_MARS,
    SwephData.SEI_JUPITER, SwephData.SEI_SATURN, SwephData.SEI_URANUS,
    SwephData.SEI_NEPTUNE, SwephData.SEI_PLUTO, 0, 0, 0, 0, SwephData.SEI_EARTH,
    SwephData.SEI_CHIRON, SwephData.SEI_PHOLUS, SwephData.SEI_CERES,
    SwephData.SEI_PALLAS, SwephData.SEI_JUNO, SwephData.SEI_VESTA, };

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
  boolean ephe_path_is_set=false;
  boolean jpl_file_is_open=false;
  FilePtr fixfp=null;
  String ephepath;
  String jplfnam;
  int jpldenum;
  boolean geopos_is_set=false;
  boolean ayana_is_set=false;

  FileData fidat[] = new FileData[SwephData.SEI_NEPHFILES];
  GenConst gcdat;
  PlanData pldat[] = new PlanData[SwephData.SEI_NPLANETS];
  PlanData nddat[] = new PlanData[SwephData.SEI_NNODE_ETC];
  SavePositions savedat[] = new SavePositions[SweConst.SE_NPLANETS+1];
  Epsilon oec, oec2000;
  Nut nut, nut2000, nutv;
  TopoData topd;
  SidData sidd;
  String astelem;
  double ast_G, ast_H, ast_diam;
  int i_saved_planet_name;
  String saved_planet_name;

  /**
  * Constructs a new SwissData object.
  */
  public SwissData() {
    // File separator character:
    DIR_GLUE = System.getProperty("file.separator");

    try {
      // ODEGREE_CHAR:
      String cp=System.getProperties().getProperty("file.encoding");
      if (cp!=null) {
        if (cp.toUpperCase().startsWith("CP")) {
          try {
            int cpn=Integer.parseInt(cp.substring(2));
            if (cpn>=437 && cpn<870) {
              ODEGREE_CHAR=""+'\u00f8';
            }
          } catch (NumberFormatException nfe) {
          }
          ODEGREE_CHAR=""+'\u00f8'; // DOS degree character 248
        } else if (cp.toUpperCase().startsWith("ISO-8859-")) {
          ODEGREE_CHAR=""+'\u00b0'; // Latin1 degree character 176
        } else if (cp.toUpperCase().startsWith("UTF")) {
          ODEGREE_CHAR=""+'\u00b0'; // Default Unicode...
        }
      }
    } catch (SecurityException ase) {
      if (DIR_GLUE.equals("/")) {
        ODEGREE_CHAR=""+'\u00b0'; // Latin1 degree character 176
      } else if (DIR_GLUE.equals("\\")) {
        ODEGREE_CHAR=""+'\u00f8'; // DOS degree character 248
      } else {
        ODEGREE_CHAR=""+'\u00b0'; // Default Unicode...
      }
    }
    // Macintoshs (prior to MacOS X) should use '\u00a1', but how to
    // identify more reasonably? Has the Mac an appropriate text window anyway?
    if (DIR_GLUE.equals(":")) {
      ODEGREE_CHAR=""+'\u00a1';
    }


    int i;
    for(i=0;i<SwephData.SEI_NEPHFILES;i++){ fidat[i] = new FileData(); }
    gcdat = new GenConst();
    for(i=0;i<SwephData.SEI_NPLANETS;i++){ pldat[i] = new PlanData(); }
    for(i=0;i<SwephData.SEI_NNODE_ETC;i++){ nddat[i] = new PlanData(); }
    for(i=0;i<SweConst.SE_NPLANETS+1;i++){ savedat[i] = new SavePositions(); }
    oec = new Epsilon();
    oec2000 = new Epsilon();
    nut = new Nut();
    nut2000 = new Nut();
    nutv = new Nut();
    topd = new TopoData();
    sidd = new SidData();
  }

}
