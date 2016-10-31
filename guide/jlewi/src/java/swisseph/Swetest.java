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

/**
 * A class to test (probably) all of the swiss ephemeris routines with
 * (probably) all possible options. See parameter -h for infos about
 * all the parameter switches.<P>
 */
public class Swetest {

    public String globalResult = ""; // OH YEAH BABY
    
    static final String infocmd0 = "\n"+
        "  Swetest computes a complete set of geocentric planetary positions,\n"+
        "  for a given date or a sequence of dates.\n"+
        "  Input can either be a date or an absolute julian day number.\n"+
        "  0:00 (midnight).\n"+
        "  With the proper options, swetest can be used to output a printed\n"+
        "  ephemeris and transfer the data into other programs like spreadsheets\n"+
        "  for graphical display.\n"+
        "\n";
    static final String infocmd1 = "\n"+
        "  Command line options:\n"+
        "        -ay..   ayanamsa, with number of method, e.g. ay0 for Fagan/Bradley\n"+
        "        -pSEQ   planet sequence to be computed.\n"+
        "                See the letter coding below.\n"+
        "        -nN     output data for N consecutive days; if no -n option\n"+
        "                is given, the default is 1. If the option -n without a\n"+
        "                number is given, the default is 20.\n"+
        "        -sN     timestep N days, default 1. This option is only meaningful\n"+
        "                when combined with option -n.\n"+
        "        -edirPATH change the directory of the ephemeris files\n"+
        "        -dX     differential ephemeris: print differential ephemeris between\n"+
        "                body X and each body in list given by -p\n"+
        "                example: -p2 -d0 -fJl -n366 -b1.1.1992 prints the longitude\n"+
        "                distance between SUN (planet 0) and MERCURY (planet 2)\n"+
        "                for a full year starting at 1 Jan 1992.\n"+
        "        -DX     midpoint ephemeris, works the same way as the differential\n"+
        "                mode -d described above, but outputs the midpoint position.\n"+
        "        -hel    compute heliocentric positions\n"+
        "        -bary   compute barycentric positions (bar. earth instead of node)\n"+
        "        -sid..    sidereal, with number of method; 'sid0' for Fagan/Bradley\n"+
        "        -sidt0..  sidereal, projection on ecliptic of t0\n"+
        "        -sidsp..  sidereal, projection on solar system plane\n"+
        "        -topo[long,lat,elev]\n"+
        "                topocentric positions. The longitude, latitude (degrees with\n"+
        "                DECIMAL fraction) and elevation (meters) can be given, with\n"+
        "                commas separated, + for east and north. If none are given,\n"+
        "                Z\u00fcrich is used: 8.55,47.38,400\n"+
        "        -house[long,lat,hsys]\n"+
        "                include house cusps. The longitude, latitude (degrees with\n"+
        "                DECIMAL fraction) and house system letter can be given, with\n"+
        "                commas separated, + for east and north. If none are given,\n"+
        "                Greenwich UK and Placidus is used: 0.00,51.50,p.\n"+
        "                The output lists 12 house cusps, Asc, MC, ARMC and Vertex.\n"+
        "                Houses can only be computed if option -ut is given.\n";
    static final String infocmd2 = ""+
        "        -hsy[hsys]	\n"+
        "                house system to be used (for house positions of planets)\n"+
        "                for long, lat, hsys, see -house\n"+
        "        -geopos[long,lat,elev]\n"+
        "                Geographic position. Can be used for azimuth and altitude\n"+
        "                or topocentric or house cups calculations.\n"+
        "                The longitude, latitude (degrees with DECIMAL fraction)\n"+
        "                and elevation (meters) can be given, with\n"+
        "                commas separated, + for east and north. If none are given,\n"+
        "                Z?rich is used: 8.55,47.38,400\n"+
        "        -head   don\'t print the header before the planet data. This option\n"+
        "                is useful when you want to paste the output into a\n"+
        "                spreadsheet for displaying graphical ephemeris.\n"+
        "        -bDATE  begin date; e.g. -b1.1.1992 if\n"+
        "                Note: the date format is day month year (European style).\n"+
        "        -bj...  begin date as an absolute Julian day number; e.g. -bj2415020.5\n"+
        "        -j...   same as -bj\n"+
        "        -fSEQ   use SEQ as format sequence for the output columns;\n"+
        "                default is PLBRS.\n"+
        "        -tHH.MMSS  input time (ephemeris time)\n"+
        "        -ut     input date is universal time\n"+
        "        -utHH:MM:SS input time\n"+
        "        -utHH.MMSS input time\n";
    static final String infocmd3 = ""+
        "        -eswe   swiss ephemeris\n"+
        "        -ejpl   jpl ephemeris (DE406), or with ephemeris file name\n"+
        "                -ejplde200.eph\n"+
        "        -emos   moshier ephemeris\n"+
        "        -true   true positions\n"+
        "        -noaberr        no aberration\n"+
        "        -nodefl no gravitational light deflection\n"+
        "        -j2000  no precession (i.e. J2000 positions)\n"+
        "        -icrs   ICRS (positions in the Internat. Celestial Reference System)\n"+
        "        -nonut  no nutation\n"+
        "        -speed  high precision speed\n"+
        "        -speed3 'low' precision speed from 3 positions\n"+
        "                do not use this option. -speed parameter is faster and preciser\n"+
        "        -testaa96       test example in AA 96, B37,\n"+
        "                        i.e. venus, j2450442.5, DE200.\n"+
        "                        attention: use precession IAU1976\n"+
        "                        and nutation 1980 (s. swephlib.h)\n"+
        "        -testaa95\n"+
        "        -testaa97\n"+
        "        -roundsec       round to seconds\n"+
        "        -roundmin       round to minutes\n"+
        "\n"+
        "        -?, -h  display whole info\n"+
        "        -hcmd   display commands\n"+
        "        -hplan  display planet numbers\n"+
        "        -hform  display format characters\n"+
        "        -hdate  display input date format\n"+
        "        +head   header before every step (with -s..)\n"+
        "        -gPPP   use PPP as gap between output columns; default is a single\n"+
        "                blank.  -g followed by white space sets the\n"+
        "                gap to the TAB character; which is useful for data entry\n"+
        "                into spreadsheets.\n"+
        "        -iXX    force iflag to value XX\n"+
        "        -hexamp  display examples\n";
    static final String infocmd4 =
        "         -solecl solar eclipse\n"+
        "                 output 1st line:\n"+
        "                   eclipse date,\n"+
        "                   time of maximum,\n"+
        "                   core shadow width (negative with total eclipses),\n"+
        "                   fraction of solar diameter that is eclipsed\n"+
        "                 output 2nd line:\n"+
        "                   start and end times for partial and total phase\n"+
        "                 output 3rd line:\n"+
        "                   geographical longitude and latitude of maximum eclipse,\n"+
        "                   totality duration at that geographical position,\n"+
        "                 output with -local, see below.\n"+
        "         -occult occultation of planet or star by the moon. Use -p to\n"+
        "                 specify planet (-pf -xfAldebaran for stars)\n"+
        "                 output format same as with -solecl\n"+
        "         -lunecl lunar eclipse\n"+
        "                 output 1st line:\n"+
        "                   eclipse date,\n"+
        "                   time of maximum,\n"+
        "                 output 2nd line:\n"+
        "                   6 contacts for start and end of penumbral, partial, and\n"+
        "                   total phase\n"+
        "         -local  only with -solecl or -occult, if the next event of this\n"+
        "                 kind is wanted for a given geogr. position.\n"+
        "                 Use -geopos[long,lat,elev] to specify that position.\n"+
        "                 If -local is not set, the program\n"+
        "                 searches for the next event anywhere on earth.\n"+
        "                 output 1st line:\n"+
        "                   eclipse date,\n"+
        "                   time of maximum,\n"+
        "                   fraction of solar diameter that is eclipsed\n"+
        "                 output 2nd line:\n"+
        "                   local eclipse duration,\n"+
        "                   local four contacts,\n";
    static final String infocmd5 =
        "         -total  total eclipse (only with -solecl, -lunecl)\n"+
        "         -partial partial eclipse (only with -solecl, -lunecl)\n"+
        "         -annular annular eclipse (only with -solecl)\n"+
        "         -anntot annular-total eclipse (only with -solecl)\n"+
        "         -penumbral penumbral lunar eclipse (only with -lunecl)\n"+
        "         -central central eclipse (only with -solecl, nonlocal)\n"+
        "         -noncentral non-central eclipse (only with -solecl, nonlocal)\n"+
        "         -rise   rising and setting of a planet or star.\n"+
        "                 Use -geopos[long,lat,elev] to specify geographical position.\n"+
        "         -norefrac   neglect refraction (with option -rise)\n"+
        "         -disccenter find rise of disc center (with option -rise)\n"+
        "         -hindu      hindu version of sunrise (with option -rise)\n"+
        "         -metr   southern and northern meridian transit of a planet of star\n"+
        "                 Use -geopos[long,lat,elev] to specify geographical position.\n"+
        "         -bwd    search backward\n";
    /* characters still available:
       bcegijklruvxy
    */
    static final String infoplan = "\n"+
        "  Planet selection letters:\n"+
        "        d (default) main factors 0123456789mtABCcg\n"+
        "        p main factors as above, plus main asteroids DEFGHI\n"+
        "        h ficticious factors J..X\n"+
        "        a all factors\n"+
        "        (the letters above can only appear as a single letter)\n\n"+
        "        0 Sun (character zero)\n"+
        "        1 Moon (character 1)\n"+
        "        2 Mercury\n"+
        "        ....\n"+
        "        9 Pluto\n"+
        "        m mean lunar node\n"+
        "        t true lunar node\n"+
        "        n nutation\n"+
        "        q delta t\n"+
        "        o obliquity of ecliptic\n"+
        "        A mean lunar apogee (Lilith, Black Moon)\n"+
        "        B osculating lunar apogee\n"+  // True Lilith
        "        c intp. lunar apogee\n"+
        "        g intp. lunar perigee\n"+
        "        C Earth\n"+
        "        D Chiron\n"+
        "        E Pholus\n"+
        "        F Ceres\n"+
        "        G Pallas\n"+
        "        H Juno\n"+
        "        I Vesta\n"+
        "        J Cupido\n"+
        "        K Hades\n"+
        "        L Zeus\n"+
        "        M Kronos\n"+
        "        N Apollon\n"+
        "        O Admetos\n"+
        "        P Vulkanus\n"+
        "        Q Poseidon\n"+
        "        R Isis (Sevin)\n"+
        "        S Nibiru (Sitchin)\n"+
        "        T Harrington\n"+
        "        U Leverrier's Neptune\n"+
        "        V Adams' Neptune\n"+
        "        W Lowell's Pluto\n"+
        "        X Pickering's Pluto\n"+
        "        Y Vulcan\n"+
        "        Z White Moon\n"+
        "        w Waldemath's dark Moon\n"+
        "        f fixed star, with name or number given in -xf option\n"+
        "        z hypothetical body, with number given in -xz\n"+
        "        s minor planet, with MPC number given in -xs\n"+
        "        e print a line of labels\n"+
        "          \n";
    /* characters still available
       cgjv
    */
    static final String infoform = "\n"+
        "  Output format SEQ letters:\n"+
        "  In the standard setting five columns of coordinates are printed with\n"+
        "  the default format PLBRS. You can change the default by providing an\n"+
        "  option like -fCCCC where CCCC is your sequence of columns.\n"+
        "  The coding of the sequence is like this:\n"+
        "        y year\n"+
        "        Y year.fraction_of_year\n"+
        "        p planet index\n"+
        "        P planet name\n"+
        "        J absolute juldate\n"+
        "        T date formatted like 23.02.1992\n"+
        "        t date formatted like 920223 for 1992 february 23\n"+
        "        L longitude in degree ddd\u00b0mm'ss\"\n"+
        "        l longitude decimal\n"+
        "        Z longitude ddsignmm'ss\"\n"+
        "        S speed in longitude in degree ddd:mm:ss per day\n"+
        "        SS speed for all values specified in fmt\n"+
        "        s speed longitude decimal (degrees/day)\n"+
        "        ss speed for all values specified in fmt\n"+
        "        B latitude degree\n"+
        "        b latitude decimal\n"+
        "        R distance decimal in AU\n"+
        "        r distance decimal in AU, Moon in seconds parallax\n"+
        "          relative distance (1000=nearest, 0=furthest)\n"+
        "        A Rectascension in hh:mm:ss\n"+
        "        a rectascension hours decimal\n"+
        "        D Declination degree\n"+
        "        d declination decimal\n"+
        "        I Azimuth degree\n"+
        "        i Azimuth decimal\n"+
        "        H Height degree\n"+
        "        h Height decimal\n"+
        "        K Height (with refraction) degree\n"+
        "        k Height (with refraction) decimal\n"+
        "        G house position in degrees\n"+
        "        g house position in degrees decimal\n"+
        "        j house number 1.0 - 12.99999\n"+
        "        X x-, y-, and z-coordinates ecliptical\n"+
        "        x x-, y-, and z-coordinates equatorial\n"+
        "        U unit vector ecliptical\n"+
        "        u unit vector equatorial\n"+
        "        Q l, b, r, dl, db, dr, a, d, da, dd\n"+
        "        n mean values: ascending, descending node (Me - Ne) decimal degree\n"+
        "        N osculating values: ascending, descending node\n"+
        "        f mean values for longitude: perihel, aphel, second focal point\n"+
        "        F oscul. values for longitude: perihel, aphel, second focal point\n"+
        "        + phase angle\n"+
        "        - phase\n"+
        "        * elongation\n"+
        "        / apparent diameter of disc (without refraction)\n"+
        "        = magnitude\n";

    static final String infodate = "\n"+
        "  Date entry:\n"+
        "  In the interactive mode, when you are asked for a start date,\n"+
        "  you can enter data in one of the following formats:\n"+
        "\n"+
        "        1.2.1991        three integers separated by a nondigit character for\n"+
        "                        day month year. Dates are interpreted as Gregorian\n"+
        "                        after 4.10.1582 and as Julian Calender before.\n"+
        "                        Time is always set to midnight.\n"+
        "                        If the three letters jul are appended to the date,\n"+
        "                        the Julian calendar is used even after 1582.\n"+
        "                        If the four letters greg are appended to the date,\n"+
        "                        the Gregorian calendar is used even before 1582.\n"+
        "\n"+
        "        j2400123.67     the letter j followed by a real number, for\n"+
        "                        the absolute Julian daynumber of the start date.\n"+
        "                        Fraction .5 indicates midnight, fraction .0\n"+
        "                        indicates noon, other times of the day can be\n"+
        "                        chosen accordingly.\n"+
        "\n"+
        "        <RETURN>        repeat the last entry\n"+
        "        \n"+
        "        .               stop the program\n"+
        "\n"+
        "        +20             advance the date by 20 days\n"+
        "\n"+
        "        -10             go back in time 10 days\n";
    static final String infoexamp = "\n"+
        "\n"+
        "  Examples:\n"+
        "\n"+
        "    java Swetest -p2 -b1.12.1900 -n15 -s2\n"+
        "	ephemeris of Mercury (-p2) starting on 1 Dec 1900,\n"+
        "	15 positions (-n15) in two-day steps (-s2)\n"+
        "\n"+
        "    java Swetest -p2 -b1.12.1900 -n15 -s2 -fTZ -roundsec -g, -head\n"+
        "	same, but output format =  date and zodiacal position (-fTZ),\n"+
        "	separated by comma (-g,) and rounded to seconds (-roundsec),\n"+
        "	without header (-head).\n"+
        "\n"+
        "    java Swetest -ps -xs433 -b1.12.1900\n"+
        "	position of asteroid 433 Eros (-ps -xs433)\n"+
        "\n"+
        "    java Swetest -pf -xfAldebaran -b1.1.2000\n"+
        "     position of fixed star Aldebaran\n"+
        "\n"+
        "    java Swetest -p1 -d0 -b1.12.1900 -n10 -fPTl -head\n"+
        "	angular distance of moon (-p1) from sun (-d0) for 10\n"+
        "	consecutive days (-n10).\n"+
        "\n"+
        "    java Swetest -p6 -DD -b1.12.1900 -n100 -s5 -fPTZ -head -roundmin\n"+
        "     Midpoints between Saturn (-p6) and Chiron (-DD) for 100\n"+
        "     consecutive steps (-n100) with 5-day steps (-s5) with\n"+
        "     longitude in degree-sign format (-f..Z) rounded to minutes (-roundmin)\n"+
        "\n"+
        "    java Swetest -b5.1.2002 -p -house12.05,49.50,k -ut12:30\n"+
        "        Koch houses for a location in Germany at a given date and time\n";
    /**************************************************************/

    SwissData swed = new SwissData();
    SwissLib  sl   = new SwissLib();
    SwissEph  sw   = new SwissEph();
    SweDate   sd   = null;
    CFmt      f    = new CFmt();

    static final double J2000=2451545.0;  /* 2000 January 1.5 */
    public double square_sum(double x[]) { return x[0]*x[0]+x[1]*x[1]+x[2]*x[2]; }
    public static final int SEFLG_EPHMASK=SweConst.SEFLG_JPLEPH|
        SweConst.SEFLG_SWIEPH|
        SweConst.SEFLG_MOSEPH;

    static final int BIT_ROUND_SEC=1;
    static final int BIT_ROUND_MIN=2;
    static final int BIT_ZODIAC=4;
    static final String PLSEL_D="0123456789mtA";
    static final String PLSEL_P="0123456789mtA"+
        "BCcg"+
        "DEFGHI";
    static final String PLSEL_H="JKLMNOPQRSTUVWXYZw";
    static final String PLSEL_A="0123456789mtABCcgDEFGHIJKLMNOPQRSTUVWXYZw";

    static final char DIFF_DIFF='d';
    static final char DIFF_MIDP='D';
    static final int MODE_HOUSE=1;
    static final int MODE_LABEL=2;

    String se_pname;
    public static final String[] zod_nam = new String[]
        {"ar", "ta", "ge", "cn", "le", "vi",
         "li", "sc", "sa", "cp", "aq", "pi"};

    String star = "algol", star2;
    String sastno = "433";
    String shyp = "1";

    /* globals shared between main() and print_line() */
    String fmt = "PLBRS";
    String gap = " ";
    double t, te, tut, jut = 0;
    int jmon, jday, jyear;
    int ipl = SweConst.SE_SUN, ipldiff = SweConst.SE_SUN, nhouses = 12;
    String spnam, spnam2="";
    StringBuffer serr=new StringBuffer(swed.AS_MAXCH);
    StringBuffer serr_save=new StringBuffer(swed.AS_MAXCH);
    StringBuffer serr_warn=new StringBuffer(swed.AS_MAXCH);
    boolean gregflag = SweDate.SE_GREG_CAL;
    int diff_mode = 0;
    boolean universal_time = false;
    int round_flag = 0;
    double x[]=new double[6], x2[]=new double[6], xequ[]=new double[6],
        xcart[]=new double[6], xcartq[]=new double[6],
        xobl[]=new double[6], xaz[]=new double[6], xt[]=new double[6],
        geopos[]=new double[3], hpos, hpos2, armc, xsv[]=new double[6];
    DblObj hposj=new DblObj();
    int hpos_meth = 0;
    double attr[]=new double[20], tret[]=new double[20];
    int iflag = 0, iflag2;              /* external flag: helio, geo... */
    static final String hs_nam[] = {"undef",
                                    "Ascendant", "MC", "ARMC", "Vertex"};
    int smod = 0;
    int direction = 1;
    boolean direction_flag = false;
    double tjd = 2415020.5;
    int nstep = 1, istep;
    int search_flag = 0;
    String sout;
    int whicheph = SweConst.SEFLG_SWIEPH;
    String psp;
    int p=0; // Index for psp
    boolean norefrac = false;
    boolean disccenter = false;

    static final int SMOD_LUNAR    =1;
    static final int SMOD_HOW      =2;       /* an option for Lunar */
    static final int SMOD_SOLAR    =4;
    static final int SMOD_LOCAL    =8;       /* an option for Solar */
    static final int SMOD_TOTAL    =16;
    static final int SMOD_ANNULAR  =32;      /* count as penumbral for Lunar */
    static final int SMOD_ANNTOT   =64;
    static final int SMOD_PARTIAL  =128;
    static final int SMOD_PENUMBRAL=256;
    static final int SMOD_ALL      =(SMOD_TOTAL| SMOD_ANNULAR|SMOD_PARTIAL|SMOD_ANNTOT);
    static final int SMOD_OCCULT   =512;
    static final int SMOD_RISE     =1024;
    static final int SMOD_METR     =2048;
    static final int SMOD_HOCAL    =4096;


    static final int ECL_LUN_PENUMBRAL     =1;       /* eclipse types for hocal list */
    static final int ECL_LUN_PARTIAL       =2;
    static final int ECL_LUN_TOTAL         =3;
    static final int ECL_SOL_PARTIAL       =4;
    static final int ECL_SOL_ANNULAR       =5;
    static final int ECL_SOL_TOTAL         =6;

    String SE_EPHE_PATH="";

    /**
     * This class is not to be instantiated, it is to be run via the main method.
     */
    public Swetest() { }

    /**
     * Use the parameter -h to get infos about the available options.
     */
    public static void main(String argv[]) {
        Swetest swt=new Swetest();
        System.exit(swt.main_start(argv));
    }

    public int main_start(String[] argv) {
        String saves="";
        String s1, s2;
        String sp, spsave, sp2;
        String spno;
        String plsel = PLSEL_D;
        int i, j, n, iflag_f = -1, iflgt;
        int line_count, line_limit = 3200000;
        double daya;
        double top_long = 0; /* Greenwich, UK */
        double top_lat = 51.5;
        double top_elev = 0;
        boolean have_geopos = false;
        int ihsy = (int)'p';
        boolean do_houses = false;
        String ephepath;
        String fname;
        String sdate;
        String begindate = null;
        long iflgret;
        boolean with_header = true;
        boolean with_header_always = false;
        boolean do_ayanamsa = false;
        int sid_mode = SweConst.SE_SIDM_FAGAN_BRADLEY;
        double t2, tstep = 1, thour = 0;
        double delt;
        serr.setLength(0); serr_save.setLength(0); serr_warn.setLength(0);
        saves = "";

        sd=new SweDate(tjd,gregflag);

        ephepath="";
        fname=SweConst.SE_FNAME_DE406;
        for (i = 0; i < argv.length; i++) {
            if (argv[i].startsWith("-DSE_EPHE_PATH")) {
                if (++i<argv.length) {
                    SE_EPHE_PATH=argv[i];
                }
            } else if (argv[i].startsWith("-ut")) {
                universal_time = true;
                if (argv[i].length() > 3) {
                    s1=argv[i].substring(3);
                    if (s1.indexOf(':')>=0) {
                        s1=s1.substring(0,s1.indexOf(':'))+"."+s1.substring(s1.indexOf(':')+1);
                        if (s1.indexOf(':')>=0) {
                            s1=s1.substring(0,s1.indexOf(':'))+s1.substring(s1.indexOf(':')+1);
                        }
                    }
                    thour = Double.valueOf(s1).doubleValue();
                    /* h.mmss -> decimal */
                    // Allowing for negative times: this is different from the C code!
                    t = (thour%1.0) * 100 + (thour<0?-1e-10:1e-10);
                    j = (int) t;
                    t = (t%1.0) * 100 + 1e-10;
                    thour = (int) thour + j / 60.0 + t / 3600.0;
                }
            } else if (argv[i].startsWith("-head")) {
                with_header = false;
            } else if (argv[i].startsWith("+head")) {
                with_header_always = true;
            } else if (argv[i].equals("-j2000")) {
                iflag |= SweConst.SEFLG_J2000;
            } else if (argv[i].equals("-icrs")) {
                iflag |= SweConst.SEFLG_ICRS;
            } else if (argv[i].startsWith("-ay")) {
                do_ayanamsa = true;
                sid_mode=0;
                if (argv[i].length()>3) {
                    sid_mode=Integer.parseInt(argv[i].substring(3));
                }
                sw.swe_set_sid_mode(sid_mode, 0, 0);
            } else if (argv[i].startsWith("-sidt0")) {
                iflag |= SweConst.SEFLG_SIDEREAL;
                //      sid_mode = atol(argv[i]+6);
                sid_mode=0;
                if (argv[i].length()>6) {
                    sid_mode=Integer.parseInt(argv[i].substring(6));
                }
                if (sid_mode == 0)
                    sid_mode = SweConst.SE_SIDM_FAGAN_BRADLEY;
                sid_mode |= SweConst.SE_SIDBIT_ECL_T0;
                sw.swe_set_sid_mode(sid_mode, 0, 0);
            } else if (argv[i].startsWith("-sidsp")) {
                iflag |= SweConst.SEFLG_SIDEREAL;
                //      sid_mode = atol(argv[i]+6);
                sid_mode=0;
                if (argv[i].length()>6) {
                    sid_mode=Integer.parseInt(argv[i].substring(6));
                }
                if (sid_mode == 0)
                    sid_mode = SweConst.SE_SIDM_FAGAN_BRADLEY;
                sid_mode |= SweConst.SE_SIDBIT_SSY_PLANE;
                sw.swe_set_sid_mode(sid_mode, 0, 0);
            } else if (argv[i].startsWith("-sid")) {
                iflag |= SweConst.SEFLG_SIDEREAL;
                //      sid_mode = atol(argv[i]+4);
                sid_mode=0;
                if (argv[i].length()>4) {
                    try {
                        sid_mode=Integer.parseInt(argv[i].substring(4));
                    } catch (NumberFormatException nf) {
                        // Anything not being a number will be considered '0'
                    }
                }
                if (sid_mode > 0)
                    sw.swe_set_sid_mode(sid_mode, 0, 0);
            } else if (argv[i].startsWith("-j")) {
                begindate = argv[i].substring(1);
            } else if (argv[i].startsWith("-ejpl")) {
                whicheph = SweConst.SEFLG_JPLEPH;
                if (argv[i].length()>5)
                    fname=argv[i].substring(5);
            } else if (argv[i].startsWith("-edir")) {
                if (argv[i].length() > 5)
                    ephepath=argv[i].substring(5);
            } else if (argv[i].startsWith("-eswe")) {
                whicheph = SweConst.SEFLG_SWIEPH;
            } else if (argv[i].startsWith("-emos")) {
                whicheph = SweConst.SEFLG_MOSEPH;
            } else if (argv[i].equals("-hel")) {
                iflag |= SweConst.SEFLG_HELCTR;
            } else if (argv[i].equals("-bary")) {
                iflag |= SweConst.SEFLG_BARYCTR;
            } else if (argv[i].startsWith("-house")) {
                sout="";
                boolean hLonIsSet=false;
                boolean hLatIsSet=false;
                // sscanf(argv[i] + 6, "%lf,%lf,%s", &top_long, &top_lat, sout);
                try {
                    String h=argv[i].substring(6);
                    int idx=h.indexOf(',');
                    String hf=h.substring(0,idx);
                    top_long=Double.valueOf(hf).doubleValue();
                    hLonIsSet=true;
                    h=h.substring(idx+1);
                    idx=h.indexOf(',');
                    if (idx<0) { idx=h.length(); }
                    hf=h.substring(0,idx);
                    top_lat=Double.valueOf(hf).doubleValue();
                    hLatIsSet=true;
                    sout=h.substring(idx+1);
                } catch (NumberFormatException nfe) {
                    if (!hLonIsSet) { top_long=0; } else { top_lat=0; }
                } catch (StringIndexOutOfBoundsException aie) {
                    if (!hLonIsSet) { top_long=0; } else if (!hLatIsSet) { top_lat=0; }
                }
                top_elev = 0;
                if (sout.length()>0) { ihsy = sout.charAt(0); }
                do_houses = true;
                have_geopos = true;
            } else if (argv[i].startsWith("-hsy")) {
                if (argv[i].length()<5) {
                    ihsy = 'p';
                } else {
                    ihsy = argv[i].charAt(4);
                }
                if (argv[i].length()>5) {
                    hpos_meth = Integer.parseInt(argv[i].substring(5));
                }
                have_geopos = true;
            } else if (argv[i].startsWith("-topo") ||
                       argv[i].startsWith("-geopos")) {
                int plen=(argv[i].startsWith("-topo")?5:7);
                if (plen==5) { iflag |= SweConst.SEFLG_TOPOCTR; }
                //      sscanf(argv[i] + 5, "%lf,%lf,%lf", &top_long, &top_lat, &top_elev);
                if (argv[i].length()>plen) {
                    String fl=argv[i].substring(plen);
                    top_long=new Double(fl.substring(0,fl.indexOf(','))).doubleValue();
                    fl=fl.substring(fl.indexOf(',')+1);
                    top_lat=new Double(fl.substring(0,fl.indexOf(','))).doubleValue();
                    fl=fl.substring(fl.indexOf(',')+1);
                    top_elev=new Double(fl).doubleValue();
                }
                have_geopos = true;
            } else if (argv[i].equals("-true")) {
                iflag |= SweConst.SEFLG_TRUEPOS;
            } else if (argv[i].equals("-noaberr")) {
                iflag |= SweConst.SEFLG_NOABERR;
            } else if (argv[i].equals("-nodefl")) {
                iflag |= SweConst.SEFLG_NOGDEFL;
            } else if (argv[i].equals("-nonut")) {
                iflag |= SweConst.SEFLG_NONUT;
            } else if (argv[i].equals("-speed3")) {
                iflag |= SweConst.SEFLG_SPEED3;
            } else if (argv[i].equals("-speed")) {
                iflag |= SweConst.SEFLG_SPEED;
            } else if (argv[i].startsWith("-testaa")) {
                whicheph = SweConst.SEFLG_JPLEPH;
                fname=SweConst.SE_FNAME_DE200;
                if (argv[i].substring(7).equals("95"))
                    begindate = "j2449975.5";
                if (argv[i].substring(7).equals("96"))
                    begindate = "j2450442.5";
                if (argv[i].substring(7).equals("97"))
                    begindate = "j2450482.5";
                fmt = "PADRu";
                universal_time = false;
                plsel="3";
            } else if (argv[i].equals("-lunecl")) {
                smod |= SMOD_LUNAR;
                smod &= ~SMOD_SOLAR;
            } else if (argv[i].equals("-solecl")) {
                smod |= SMOD_SOLAR;
                search_flag |= SweConst.SE_ECL_CENTRAL;
                search_flag |= SweConst.SE_ECL_NONCENTRAL;
                have_geopos = true;
            } else if (argv[i].equals("-occult")) {
                smod |= SMOD_OCCULT;
                smod &= ~SMOD_SOLAR;
                smod &= ~SMOD_LUNAR;
                search_flag |= SweConst.SE_ECL_CENTRAL;
                search_flag |= SweConst.SE_ECL_NONCENTRAL;
                have_geopos = true;
            } else if (argv[i].equals("-hocal")) {
                /* used to create a listing for inclusion in hocal.c source code */
                smod |= SMOD_HOCAL;
            } else if (argv[i].equals("-how")) {
                smod |= SMOD_HOW;
            } else if (argv[i].equals("-total")) {
                smod |= SMOD_TOTAL;
            } else if (argv[i].equals("-annular")) {
                smod |= SMOD_ANNULAR;
            } else if (argv[i].equals("-anntot")) {
                smod |= SMOD_ANNTOT;
            } else if (argv[i].equals("-partial")) {
                smod |= SMOD_PARTIAL;
            } else if (argv[i].equals("-penumbral")) {
                smod |= SMOD_PENUMBRAL;
            } else if (argv[i].equals("-noncentral")) {
                search_flag &= ~SweConst.SE_ECL_CENTRAL;
                search_flag |= SweConst.SE_ECL_NONCENTRAL;
            } else if (argv[i].equals("-central")) {
                search_flag &= ~SweConst.SE_ECL_NONCENTRAL;
                search_flag |= SweConst.SE_ECL_CENTRAL;
            } else if (argv[i].equals("-local")) {
                smod |= SMOD_LOCAL;
            } else if (argv[i].equals("-rise")) {
                smod |= SMOD_RISE;
                smod &= ~SMOD_SOLAR;
                have_geopos = true;
            } else if (argv[i].equals("-norefrac")) {
                norefrac = true;
            } else if (argv[i].equals("-disccenter")) {
                disccenter = true;
            } else if (argv[i].equals("-hindu")) {
                norefrac = true;
                disccenter = true;
            } else if (argv[i].equals("-metr")) {
                smod |= SMOD_METR;
                smod &= ~SMOD_SOLAR;
                have_geopos = true;
            } else if (argv[i].equals("-bwd")) {
                direction = -1;
                direction_flag = true;
            } else if (argv[i].startsWith("-p")) {
                spno="";
                if (argv[i].length()>2) {
                    spno = argv[i].substring(2);
                    switch ((int)spno.charAt(0)) {
                        case (int)'d':
                            /*
                              case (int)'\0':
                              case (int)' ':
                            */
                            plsel = PLSEL_D; break;
                        case (int)'p':  plsel = PLSEL_P; break;
                        case (int)'h':  plsel = PLSEL_H; break;
                        case (int)'a':  plsel = PLSEL_A; break;
                        default:   plsel = spno;
                    }
                } else {
                    // We need at least one char in Java, to be able to use existing code
                    plsel = " ";
                }
            } else if (argv[i].startsWith("-xs")) {
                /* number of asteroid */
                sastno="0";
                if (argv[i].length()>3) {
                    sastno=argv[i].substring(3);
                }
            } else if (argv[i].startsWith("-xf")) {
                /* name or number of fixed star */
                star="";
                if (argv[i].length()>3) {
                    star=argv[i].substring(3);
                }
            } else if (argv[i].startsWith("-xz")) {
                /* number of hypothetical body */
                if (argv[i].length()>3) {
                    shyp=argv[i].substring(3);
                }
            } else if (argv[i].startsWith("-x")) {
                /* name or number of fixed star */
                star="";
                if (argv[i].length()>2) {
                    star=argv[i].substring(2);
                }
            } else if (argv[i].startsWith("-n")) {
                nstep=0;
                if (argv[i].length()>2) {
                    nstep = Integer.parseInt(argv[i].substring(2));
                }
                if (nstep==0) {
                    nstep=20;
                }
            } else if (argv[i].startsWith("-i")) {
                if (iflag_f<0) iflag_f = 0;
                if (argv[i].length()>2) {
                    iflag_f = Integer.parseInt(argv[i].substring(2));
                }
                if ((iflag_f & SweConst.SEFLG_XYZ)!=0)
                    fmt = "PX";
            } else if (argv[i].startsWith("-s")) {
                tstep=0;
                if (argv[i].length()>2) {
                    tstep = new Double(argv[i].substring(2)).doubleValue();
                }
            } else if (argv[i].startsWith("-b")) {
                begindate="";
                if (argv[i].length()>2) {
                    begindate = argv[i].substring(2);
                }
            } else if (argv[i].startsWith("-f")) {
                fmt="";
                if (argv[i].length()>2) {
                    fmt = argv[i].substring(2);
                }
            } else if (argv[i].startsWith("-g")) {
                gap = "\t";
                if (argv[i].length()>2) {
                    gap = argv[i].substring(2);
                }
            } else if (argv[i].startsWith("-d")
                       || argv[i].startsWith("-D")) {
                diff_mode = (int)argv[i].charAt(1); /* 'd' or 'D' */
                ipldiff=-1;
                if (argv[i].length()>2) {
                    sp = argv[i].substring(2);
                    ipldiff = letter_to_ipl((int) sp.charAt(0));
                }
                if (ipldiff <0) ipldiff = SweConst.SE_SUN;
                spnam2=sw.swe_get_planet_name(ipldiff);
            } else if (argv[i].equals("-roundsec")) {
                round_flag |= BIT_ROUND_SEC;
            } else if (argv[i].equals("-roundmin")) {
                round_flag |= BIT_ROUND_MIN;
            } else if (argv[i].startsWith("-t")) {
                if (argv[i].length() > 2) {
                    s1=argv[i].substring(2);
                    if (s1.indexOf(':')>=0) {
                        s1=s1.substring(0,s1.indexOf(':'))+"."+s1.substring(s1.indexOf(':')+1);
                        if (s1.indexOf(':')>=0) {
                            s1=s1.substring(0,s1.indexOf(':'))+s1.substring(s1.indexOf(':')+1);
                        }
                    }
                    thour = Double.valueOf(s1).doubleValue();
                    /* h.mmss -> decimal */
                    // Allowing for negative times: this is different from the C code!
                    t = (thour%1.) * 100 + (thour<0?-1e-10:1e-10);
                    j = (int) t;
                    t = (t%1.) * 100 + 1e-10;
                    thour = (int) thour + j / 60.0 + t / 3600.0;
                }
            } else if (argv[i].startsWith("-h")
                       || argv[i].startsWith("-?")) {
                sp=" ";
                if (argv[i].length()>2) {
                    sp = argv[i].substring(2);
                }
                if (sp.charAt(0) == 'c' || sp.charAt(0) == ' ') {
                    DevNull.print(infocmd0);
                    DevNull.print(infocmd1);
                    DevNull.print(infocmd2);
                    DevNull.print(infocmd3);
                    DevNull.print(infocmd4);
                    DevNull.print(infocmd5);
                }
                if (sp.charAt(0) == 'p' || sp.charAt(0) == ' ')
                    DevNull.print(infoplan);
                if (sp.charAt(0) == 'f' || sp.charAt(0) == ' ')
                    DevNull.print(infoform);
                if (sp.charAt(0) == 'd' || sp.charAt(0) == ' ')
                    DevNull.print(infodate);
                if (sp.charAt(0) == 'e' || sp.charAt(0) == ' ')
                    DevNull.print(infoexamp);
                //      goto end_main;
                /* close open files and free allocated space */
                sw.swe_close();
                return SweConst.OK;
            } else {
                sout="illegal option "+argv[i]+"\n";
                DevNull.print(sout);
                System.exit(1);
            }
        }
        if ((smod & (SMOD_OCCULT | SMOD_RISE | SMOD_METR)) != 0) {
            ipl = letter_to_ipl((int)plsel.charAt(0));
            if (plsel.charAt(0) == 'f')
                ipl = SweConst.SE_FIXSTAR;
            else
                star = "";
            if ((smod & SMOD_OCCULT)!=0 && ipl == 1)
                ipl = 2; /* no occultation of moon by moon */
        }
        geopos[0] = top_long;
        geopos[1] = top_lat;
        geopos[2] = top_elev;
        sw.swe_set_topo(top_long, top_lat, top_elev);
        if (with_header) {
            DevNull.print("swetest ");
            for (i = 0; i < argv.length; i++) {
                DevNull.print(argv[i]);
                DevNull.print(" ");
            }
        }
        iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | whicheph;
        if (fmt.indexOf("S")>=0 || fmt.indexOf("s")>=0 || fmt.indexOf("Q")>=0)
            iflag |= SweConst.SEFLG_SPEED;
        String argv0=System.getProperties().getProperty("user.dir");
        if (ephepath.length()>0)
            sw.swe_set_ephe_path(ephepath);
        else if (make_ephemeris_path(iflag, argv0) == SweConst.ERR) {
            iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | SweConst.SEFLG_MOSEPH;
            whicheph = SweConst.SEFLG_MOSEPH;
        }
        if ((whicheph & SweConst.SEFLG_JPLEPH)!=0)
            sw.swe_set_jpl_file(fname);
        while (true) {
            serr.setLength(0); serr_save.setLength(0); serr_warn.setLength(0);
            if (begindate == null) {
                DevNull.print("\nDate ?");
                sdate = "";
                try {
                    InputStreamReader in=new InputStreamReader(System.in);
                    BufferedReader bin=new BufferedReader(in);
                    sdate=bin.readLine();
                } catch (IOException ie) {
                    DevNull.println(ie.getMessage());
                }
            } else {
                sdate=begindate;
                begindate = ".";  /* to exit afterwards */
            }
            if (sdate.equals("-bary")) {
                iflag = iflag & ~SweConst.SEFLG_HELCTR;
                iflag |= SweConst.SEFLG_BARYCTR;
                sdate = "";
            } else if (sdate.equals("-hel")) {
                iflag = iflag & ~SweConst.SEFLG_BARYCTR;
                iflag |= SweConst.SEFLG_HELCTR;
                sdate = "";
            } else if (sdate.equals("-geo")) {
                iflag = iflag & ~SweConst.SEFLG_BARYCTR;
                iflag = iflag & ~SweConst.SEFLG_HELCTR;
                sdate = "";
            } else if (sdate.equals("-ejpl")) {
                iflag &= ~SweConst.SEFLG_EPHMASK;
                iflag |= SweConst.SEFLG_JPLEPH;
                sdate = "";
            } else if (sdate.equals("-eswe")) {
                iflag &= ~SweConst.SEFLG_EPHMASK;
                iflag |= SweConst.SEFLG_SWIEPH;
                sdate = "";
            } else if (sdate.equals("-emos")) {
                iflag &= ~SweConst.SEFLG_EPHMASK;
                iflag |= SweConst.SEFLG_MOSEPH;
                sdate = "";
            } else if (sdate.startsWith("-xs")) {
                sastno=sdate.substring(3);
                sdate = "";
            }
            sp = sdate;
            spsave = sp;
            if (sp.length()>0 && sp.charAt(0) == '.') {
                sw.swe_close();
                return SweConst.OK;
            } else if (sp.length() == 0) {
                sdate=saves;
                sp=sdate;
            } else {
                saves=sdate;
            }
            if (sp.length()>0 && sp.charAt(0) == 'j') {   /* it's a day number */
                if (sp.indexOf(',') >= 0)
                    sp=sp.substring(0,sp.indexOf(','))+'.'+sp.substring(sp.indexOf(',')+1);
                tjd = new Double(sp.substring(1)).doubleValue();
                if (tjd < 2299160.5)
                    gregflag = SweDate.SE_JUL_CAL;
                else
                    gregflag = SweDate.SE_GREG_CAL;
                if (sp.indexOf("jul") >= 0)
                    gregflag = SweDate.SE_JUL_CAL;
                else if (sp.indexOf("greg") >= 0)
                    gregflag = SweDate.SE_GREG_CAL;
                sd.setJulDay(tjd);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
            } else if (sp.length()>0 && sp.charAt(0) == '+') {
                n=0;
                if (sp.length()>1) {
                    n = Integer.parseInt(sp.substring(1));
                }
                if (n == 0) n = 1;
                tjd += n;
                //      swe_revjul(tjd, gregflag, &jyear, &jmon, &jday, &jut);
                sd.setJulDay(tjd);
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
            } else if (sp.length()>0 && sp.charAt(0) == '-') {
                n = Integer.parseInt(sp);
                if (n == 0) n = -1;
                tjd += n;
                //      swe_revjul(tjd, gregflag, &jyear, &jmon, &jday, &jut);
                sd.setJulDay(tjd);
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
            } else {
                //      if (sscanf (sp, "%d%*c%d%*c%d", &jday,&jmon,&jyear) < 1) exit(1);
                jday=jmon=jyear=0;
                boolean neg=false;
                i=0;
                try {
                    neg=sp.charAt(0)=='-';
                    if (neg) { i++; }
                    while (Character.isDigit(sp.charAt(i))) {
                        jday=jday*10+Character.digit(sp.charAt(i++),10);
                    }
                    if (neg) { jday=-jday; neg=false; }

                    while (i<sp.length() &&
                           !Character.isDigit(sp.charAt(i)) && sp.charAt(i)!='-') { i++; }
                    neg=sp.charAt(i)=='-';
                    if (neg) { i++; }
                    while (i<sp.length() && Character.isDigit(sp.charAt(i))) {
                        jmon=jmon*10+Character.digit(sp.charAt(i++),10);
                    }
                    if (neg) { jmon=-jmon; neg=false; }

                    while (i<sp.length() &&
                           !Character.isDigit(sp.charAt(i)) && sp.charAt(i)!='-') { i++; }
                    neg=sp.charAt(i)=='-';
                    if (neg) { i++; }
                    while (i<sp.length() && Character.isDigit(sp.charAt(i))) {
                        jyear=jyear*10+Character.digit(sp.charAt(i++),10);
                    }
                    if (neg) { jyear=-jyear; }
                } catch (StringIndexOutOfBoundsException sob) {
                    System.exit(1);
                }
                if (jyear * 10000 + jmon * 100 + jday < 15821015)
                    gregflag = SweDate.SE_JUL_CAL;
                else
                    gregflag = SweDate.SE_GREG_CAL;
                if (sp.indexOf("jul") >= 0)
                    gregflag = SweDate.SE_JUL_CAL;
                else if (sp.indexOf("greg") >= 0)
                    gregflag = SweDate.SE_GREG_CAL;
                jut = 0;
                sd.setDate(jyear,jmon,jday,jut);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_DATE); // Keep Date!
                tjd = sd.getJulDay();
                tjd += thour / 24;
                sd.setJulDay(tjd);
            }
            line_count = 0;
            if (smod > 0) {
                do_eclipse_etc();
                sw.swe_close();
                return SweConst.OK;
            }
            for (t = tjd, istep = 1; istep <= nstep; t += tstep, istep++) {
                if (t < 2299160.5)
                    gregflag = SweDate.SE_JUL_CAL;
                else
                    gregflag = SweDate.SE_GREG_CAL;
                if (sp.indexOf("jul") >= 0)
                    gregflag = SweDate.SE_JUL_CAL;
                else if (spsave.indexOf("greg") >= 0)
                    gregflag = SweDate.SE_GREG_CAL;
                //      swe_revjul(t, gregflag, &jyear, &jmon, &jday, &jut);
                sd.setJulDay(t);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
                if (with_header) {
                    sout="\ndate (dmy) "+jday+"."+jmon+"."+jyear;
                    DevNull.print(sout);
                    if (gregflag)
                        DevNull.print(" greg.");
                    else
                        DevNull.print(" jul.");
                    t2 = t + 0.5;
                    t2 += 0.5 / 86400; /* round to second */
                    t2 = (t2 - (int) t2) * 24;
                    sout="  "+f.fmt("% 2d",(int) t2)+":";
                    DevNull.print(sout);
                    t2 = (t2 - (int) t2) * 60;
                    sout=f.fmt("%02d",(int)t2)+":";
                    DevNull.print(sout);
                    t2 = (t2 - (int) t2) * 60;
                    sout=f.fmt("%02d",(int) t2);
                    DevNull.print(sout);
                    if (universal_time)
                        DevNull.print(" UT");
                    else
                        DevNull.print(" ET");
                }
                delt = sd.getDeltaT(t);
                if (universal_time) {
                    if (with_header) {
                        sout="\nUT: "+f.fmt("%f",t);
                        DevNull.print(sout);
                    }
                    if (with_header) {
                        sout="     delta t: "+f.fmt("%f",delt * 86400.0)+" sec";
                        DevNull.print(sout);
                    }
                    te = t + delt;
                    tut = t;
                } else {
                    te = t;
                    tut = t - delt;
                }
                iflgret = sw.swe_calc(te, SweConst.SE_ECL_NUT, iflag, xobl, serr);
                if (with_header) {
                    sout="\nET: "+f.fmt("%f",te);
                    DevNull.print(sout);
                    if ((iflag & SweConst.SEFLG_SIDEREAL)!=0) {
                        daya = sw.swe_get_ayanamsa(te);
                        sout="   ayanamsa = "+dms(daya, round_flag);
                        DevNull.print(sout);
                    }
                    if (have_geopos) {
                        DevNull.print("\ngeo. long "+f.fmt("%f",geopos[0])+
                                         ", lat "+f.fmt("%f",geopos[1])+
                                         ", alt "+f.fmt("%f",geopos[2]));
                    }
                    if (iflag_f >=0)
                        iflag = iflag_f;
                    if (plsel.indexOf('o') < 0) {
                        sout="\n"+f.fmt("%-15s","Epsilon (true)")+" "+dms(xobl[0],round_flag);
                        DevNull.print(sout);
                    }
                    if (plsel.indexOf('n') < 0) {
                        s1=dms(xobl[2], round_flag);
                        s2=dms(xobl[3], round_flag);
                        //            sout="\nNutation        "+s1+gap+s2;
                        sout="\n"+f.fmt("%-15s","Nutation")+" "+s1+gap+s2;
                        DevNull.print(sout);
                    }
                    DevNull.print("\n");

                    if (do_houses) {
                        if (!universal_time) {
                            do_houses = false;
                            DevNull.print("option -house requires option -ut for "+
                                             "Universal Time\n");
                        } else {
                            s1 = dms(top_long, round_flag);
                            s2 = dms(top_lat, round_flag);
                            sout = "Houses system "+(char)ihsy+" for long="+s1+
                                ", lat="+s2+"\n";
                            DevNull.print(sout);
                        }     
                    }         
                }
                if (with_header && !with_header_always)
                    with_header = false;
                if (do_ayanamsa) {
                    daya = sw.swe_get_ayanamsa(te);
                    DevNull.print("Ayanamsa"+gap+dms(daya, round_flag)+"\n");
                    continue;
                }
                if (plsel.indexOf('e')>=0) {
                    print_line(MODE_LABEL);
                }
                for (psp = plsel, p=0; p<plsel.length(); p++) {
                    if (psp.charAt(p) == 'e') { continue; }
                    ipl = letter_to_ipl((int) psp.charAt(p));
                    if (psp.charAt(p) == 'f') {
                        ipl = SweConst.SE_FIXSTAR;
                    } else if (psp.charAt(p) == 's') {
                        try {
                            // Well, no: sastno may be appended a letter like '10s'!!!
                            //              ipl = Integer.parseInt(sastno) + 10000;
                            ipl = sl.atoi(sastno) + 10000;
                        } catch (NumberFormatException ne) {
                            ipl = SweConst.ERR;
                        }
                    } else if (psp.charAt(p) == 'z') {
                        try {
                            ipl = Integer.parseInt(shyp) + SweConst.SE_FICT_OFFSET_1;
                        } catch (NumberFormatException ne) {
                            ipl = SweConst.ERR;
                        }
                    }
                    if (ipl == -2) {
                        DevNull.print("illegal parameter -p"+plsel+"\n");
                        System.exit(1);
                    }
                    if ((iflag & SweConst.SEFLG_HELCTR)!=0) {
                        if (ipl == SweConst.SE_SUN
                            || ipl == SweConst.SE_MEAN_NODE
                            || ipl == SweConst.SE_TRUE_NODE
                            || ipl == SweConst.SE_MEAN_APOG
                            || ipl == SweConst.SE_OSCU_APOG)
                            continue;
                    } else if ((iflag & SweConst.SEFLG_BARYCTR)!=0) {
                        if (ipl == SweConst.SE_MEAN_NODE || ipl == SweConst.SE_TRUE_NODE
                            || ipl == SweConst.SE_MEAN_APOG
                            || ipl == SweConst.SE_OSCU_APOG)
                            continue;
                    } else          /* geocentric */
                        if (ipl == SweConst.SE_EARTH)
                            continue;
                    /* ecliptic position */
                    if (iflag_f >=0)
                        iflag = iflag_f;
                    if (ipl == SweConst.SE_FIXSTAR) {
                        StringBuffer sstar=new StringBuffer(star);
                        iflgret = sw.swe_fixstar(sstar, te, iflag, x, serr);
                        star=sstar.toString();
                        se_pname=star;
                    } else {
                        iflgret = sw.swe_calc(te, ipl, iflag, x, serr);
                        /* phase, magnitude, etc. */
                        if (iflgret != SweConst.ERR && (fmt.indexOf("+")>=0 ||
                                                        fmt.indexOf("-")>=0 || fmt.indexOf("*")>=0 ||
                                                        fmt.indexOf("/")>=0 || fmt.indexOf("=")>=0)) {
                            iflgret = sw.swe_pheno(te, ipl, iflag, attr, serr);
                        }
                        se_pname=sw.swe_get_planet_name(ipl);
                    }
                    if (psp.charAt(p) == 'q') {/* delta t */
                        x[0] = sd.getDeltaT(te) * 86400;
                        x[1] = x[2] = x[3] = 0;
                        se_pname = "Delta T";
                    }
                    if (psp.charAt(p) == 'o') {/* ecliptic is wanted, remove nutation */
                        x[2] = x[3] = 0;
                        se_pname="Ecl. Obl.";
                    }
                    if (psp.charAt(p) == 'n') {/* nutation is wanted, remove ecliptic */
                        x[0] = x[2];
                        x[1] = x[3];
                        x[2] = x[3] = 0;
                        se_pname="Nutation";
                    }
                    if (iflgret < 0) {
                        if (!serr.toString().equals(serr_save.toString())
                            && (ipl == SweConst.SE_SUN || ipl == SweConst.SE_MOON
                                || ipl == SweConst.SE_MEAN_NODE
                                || ipl == SweConst.SE_TRUE_NODE || ipl == SweConst.SE_CHIRON
                                || ipl == SweConst.SE_PHOLUS || ipl == SweConst.SE_CUPIDO
                                || ipl >= SweConst.SE_AST_OFFSET
                                || ipl == SweConst.SE_FIXSTAR)) {
                            sout="error: "+serr.toString()+"\n";
                            DevNull.print(sout);
                        }
                        serr_save=new StringBuffer(serr.toString());
                    } else if (serr.length()>0 && serr_warn.length()==0) {
                        if (serr.toString().indexOf("'seorbel.txt' not found")<0) {
                            serr_warn=new StringBuffer(serr.toString());
                        }
                    }
                    if (diff_mode != 0) {
                        iflgret = sw.swe_calc(te, ipldiff, iflag, x2, serr);
                        if (iflgret < 0) {
                            sout="error: "+serr.toString()+"\n";
                            DevNull.print(sout);
                        }
                        if (diff_mode == DIFF_DIFF) {
                            for (i = 1; i < 6; i++)
                                x[i] -= x2[i];
                            if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                x[0] = sl.swe_difdeg2n(x[0], x2[0]);
                            else
                                x[0] = sl.swe_difrad2n(x[0], x2[0]);
                        } else {      /* DIFF_MIDP */
                            for (i = 1; i < 6; i++)
                                x[i] = (x[i] + x2[i]) / 2;
                            if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                x[0] = sl.swe_deg_midp(x[0], x2[0]);
                            else
                                x[0] = sl.swe_rad_midp(x[0], x2[0]);
                        }
                    }
                    /* equator position */
                    //        if (strpbrk(fmt, "aADdQ") != null) { ... }
                    if (fmt.indexOf("a")>=0 || fmt.indexOf("A")>=0 ||
                        fmt.indexOf("D")>=0 || fmt.indexOf("d")>=0 ||
                        fmt.indexOf("Q")>=0) {
                        iflag2 = iflag | SweConst.SEFLG_EQUATORIAL;
                        if (ipl == SweConst.SE_FIXSTAR) {
                            StringBuffer sstar=new StringBuffer(star);
                            iflgret = sw.swe_fixstar(sstar, te, iflag2, xequ, serr);
                            star=sstar.toString();
                        } else {
                            iflgret = sw.swe_calc(te, ipl, iflag2, xequ, serr);
                        }
                        if (diff_mode != 0) {
                            iflgret = sw.swe_calc(te, ipldiff, iflag2, x2, serr);
                            if (diff_mode == DIFF_DIFF) {
                                for (i = 1; i < 6; i++)
                                    xequ[i] -= x2[i];
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    xequ[0] = sl.swe_difdeg2n(xequ[0], x2[0]);
                                else
                                    xequ[0] = sl.swe_difrad2n(xequ[0], x2[0]);
                            } else {    /* DIFF_MIDP */
                                for (i = 1; i < 6; i++)
                                    xequ[i] = (xequ[i] + x2[i]) / 2;
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    xequ[0] = sl.swe_deg_midp(xequ[0], x2[0]);
                                else
                                    xequ[0] = sl.swe_rad_midp(xequ[0], x2[0]);
                            }
                        }
                    }
                    /* azimuth and height */
                    //        if (strpbrk(fmt, "IiHhKk") != NULL) { ... }
                    if (fmt.indexOf("I")>=0 || fmt.indexOf("i")>=0 ||
                        fmt.indexOf("H")>=0 || fmt.indexOf("h")>=0 ||
                        fmt.indexOf("K")>=0 || fmt.indexOf("k")>=0) {
                        /* first, get topocentric equatorial positions */
                        iflgt = whicheph | SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_TOPOCTR;
                        if (ipl == SweConst.SE_FIXSTAR) {
                            StringBuffer sstar=new StringBuffer(star);
                            iflgret = sw.swe_fixstar(sstar, te, iflgt, xt, serr);
                            star=sstar.toString();
                        } else {
                            iflgret = sw.swe_calc(te, ipl, iflgt, xt, serr);
                        }
                        /* to azimuth/height */
                        sw.swe_azalt(tut, SweConst.SE_EQU2HOR, geopos, 1013.25, 10, xt, xaz);
                        if (diff_mode!=0) {
                            iflgret = sw.swe_calc(te, ipldiff, iflgt, xt, serr);
                            sw.swe_azalt(tut, SweConst.SE_EQU2HOR, geopos, 1013.25, 10, xt, x2);
                            if (diff_mode == DIFF_DIFF) {
                                for (i = 1; i < 3; i++)
                                    xaz[i] -= x2[i];
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    xaz[0] = sl.swe_difdeg2n(xaz[0], x2[0]);
                                else
                                    xaz[0] = sl.swe_difrad2n(xaz[0], x2[0]);
                            } else {    /* DIFF_MIDP */
                                for (i = 1; i < 3; i++)
                                    xaz[i] = (xaz[i] + x2[i]) / 2;
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    xaz[0] = sl.swe_deg_midp(xaz[0], x2[0]);
                                else
                                    xaz[0] = sl.swe_rad_midp(xaz[0], x2[0]);
                            }
                        }
                    }
                    /* ecliptic cartesian position */
                    //        if (strpbrk(fmt, "XU") != null) { ... }
                    if (fmt.indexOf("X")>=0 || fmt.indexOf("U")>=0) {
                        iflag2 = iflag | SweConst.SEFLG_XYZ;
                        if (ipl == SweConst.SE_FIXSTAR) {
                            StringBuffer sstar=new StringBuffer(star);
                            iflgret = sw.swe_fixstar(sstar, te, iflag2, xcart, serr);
                            star=sstar.toString();
                        } else {
                            iflgret = sw.swe_calc(te, ipl, iflag2, xcart, serr);
                        }
                        if (diff_mode != 0) {
                            iflgret = sw.swe_calc(te, ipldiff, iflag2, x2, serr);
                            if (diff_mode == DIFF_DIFF) {
                                for (i = 0; i < 6; i++)
                                    xcart[i] -= x2[i];
                            } else {
                                xcart[i] = (xcart[i] + x2[i]) / 2;
                            }
                        }
                    }
                    /* equator cartesian position */
                    //        if (strpbrk(fmt, "xu") != null) { ... }
                    if (fmt.indexOf("x")>=0 || fmt.indexOf("u")>=0) {
                        iflag2 = iflag | SweConst.SEFLG_XYZ | SweConst.SEFLG_EQUATORIAL;
                        if (ipl == SweConst.SE_FIXSTAR) {
                            StringBuffer sstar=new StringBuffer(star);
                            iflgret = sw.swe_fixstar(sstar, te, iflag2, xcartq, serr);
                            star=sstar.toString();
                        } else {
                            iflgret = sw.swe_calc(te, ipl, iflag2, xcartq, serr);
                        }
                        if (diff_mode != 0) {
                            iflgret = sw.swe_calc(te, ipldiff, iflag2, x2, serr);
                            if (diff_mode == DIFF_DIFF) {
                                for (i = 0; i < 6; i++)
                                    xcartq[i] -= x2[i];
                            } else {
                                xcartq[i] = (xcart[i] + x2[i]) / 2;
                            }
                        }
                    }
                    /* house position */
                    //          if (strpbrk(fmt, "gGj") != NULL) { ... }
                    if (fmt.indexOf("g")>=0 || fmt.indexOf("G")>=0 || fmt.indexOf("j")>=0) {
                        armc = sl.swe_degnorm(sl.swe_sidtime(tut) * 15 + geopos[0]);
                        for (i = 0; i < 6; i++)
                            xsv[i] = x[i];
                        if (hpos_meth == 1)
                            xsv[1] = 0;
                        if (ipl == SweConst.SE_FIXSTAR)
                            star2=star;
                        else
                            star2 = "";
                        if (hpos_meth >= 2 && Character.toLowerCase((char)ihsy) == 'g') {
                            StringBuffer sstar2=new StringBuffer(star2);
                            sw.swe_gauquelin_sector(tut, ipl, sstar2, iflag, hpos_meth, geopos, 0, 0, hposj, serr);
                            star2=sstar2.toString();
                        } else {
                            hposj.val = sw.swe_house_pos(armc, geopos[1], xobl[0], ihsy, xsv, serr);
                        }
                        if (Character.toLowerCase((char)ihsy) == 'g')
                            hpos = (hposj.val - 1) * 10;
                        else
                            hpos = (hposj.val - 1) * 30;
                        if (diff_mode!=0) {
                            for (i = 0; i < 6; i++)
                                xsv[i] = x2[i];
                            if (hpos_meth == 1)
                                xsv[1] = 0;
                            hpos2 = sw.swe_house_pos(armc, geopos[1], xobl[0], ihsy, xsv, serr);
                            if (Character.toLowerCase((char)ihsy) == 'g')
                                hpos2 = (hpos2 - 1) * 10;
                            else
                                hpos2 = (hpos2 - 1) * 30;
                            if (diff_mode == DIFF_DIFF) {
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    hpos = sl.swe_difdeg2n(hpos, hpos2);
                                else
                                    hpos = sl.swe_difrad2n(hpos, hpos2);
                            } else {    /* DIFF_MIDP */
                                if ((iflag & SweConst.SEFLG_RADIANS) == 0)
                                    hpos = sl.swe_deg_midp(hpos, hpos2);
                                else
                                    hpos = sl.swe_rad_midp(hpos, hpos2);
                            }
                        }
                    }
                    spnam=se_pname;
                    print_line(0);
                    line_count++;
                    if (line_count >= line_limit) {
                        sout="****** line count "+line_limit+" was exceeded\n";
                        DevNull.print(sout);
                        break;
                    }
                }         /* for psp */
                if (do_houses) {
                    double cusp[]=new double[100];
                    int iofs;
                    if (Character.toLowerCase((char)ihsy) == 'g')
                        nhouses = 36;
                    iofs = nhouses + 1;
                    iflgret = sw.swe_houses(t,iflag, top_lat, top_long, ihsy, cusp, cusp, iofs);
                    if (iflgret < 0) {
                        if (!serr.toString().equals(serr_save.toString())) {
                            sout="error: "+serr.toString()+"\n";
                            DevNull.print(sout);
                        }
                        serr_save=new StringBuffer(serr.toString());
                    } else {
                        for (ipl = 1; ipl < iofs+4; ipl++) {
                            x[0] = cusp[ipl];
                            x[1] = 0;   /* latitude */
                            x[2] = 1.0; /* pseudo radius vector */
                            //              if (strpbrk(fmt, "aADdQ") != NULL)
                            if (fmt.indexOf('a')>=0 || fmt.indexOf('A')>=0 ||
                                fmt.indexOf('D')>=0 || fmt.indexOf('d')>=0 ||
                                fmt.indexOf('Q')>=0) {
                                sl.swe_cotrans(x, 0, xequ, 0, -xobl[0]);
                            }
                            print_line(MODE_HOUSE);
                            line_count++;
                        }
                    }
                }   
                if (line_count >= line_limit)
                    break;
            }           /* for tjd */
            if (serr_warn.length() != 0) {
                DevNull.print("\nwarning: ");
                DevNull.print(serr_warn.toString());
                DevNull.print("\n");
            }
        }             /* while 1 */
    }

    /*
     * The string fmt contains a sequence of format specifiers;
     * each character in fmt creates a column, the columns are
     * sparated by the gap string.
     */
    int print_line(int mode) {
        String sp, sp2, sout;
        double t2, ju2 = 0;
        double y_frac;
        double ar, sinp;
        boolean is_house = ((mode & MODE_HOUSE) != 0);
        boolean is_label = ((mode & MODE_LABEL) != 0);
        int iflgret;
        int c=0;
        for (sp = fmt; c<fmt.length(); c++) {
            if (is_house && "bBsSrRxXuUQnNfF+-*/=".indexOf(sp.charAt(c))>=0) {
                continue;
            }
            if (c != 0)
                DevNull.print(gap);
            switch((int)sp.charAt(c)) {
                case (int)'y':
                    if (is_label) { DevNull.println("year"); break; }
                    sout=""+jyear;
                    DevNull.print(sout);
                    break;
                case (int)'Y':
                    if (is_label) { DevNull.println("year"); break; }
                    t2 = sd.getJulDay(jyear,1,1,ju2,gregflag);
                    y_frac = (t - t2) / 365.0;
                    sout=f.fmt("%.2f",jyear + y_frac);
                    DevNull.print(sout);
                    break;
                case (int)'p':
                    if (is_label) { DevNull.println("obj.nr"); break; }
                    if (!is_house && diff_mode == DIFF_DIFF)
                        sout=""+ipl+"-"+ipldiff;
                    else if (!is_house && diff_mode == DIFF_MIDP)
                        sout=""+ipl+"/"+ipldiff;
                    else
                        sout=""+ipl;
                    DevNull.print(sout);
                    break;
                case (int)'P':
                    if (is_label) { DevNull.println("name           "); break; }
                    if (is_house) {
                        if (ipl <= nhouses)
                            sout="house "+f.fmt("%2d",ipl)+"       ";
                        else
                            sout=f.fmt("%-15s",hs_nam[ipl - nhouses]);
                    } else if (diff_mode == DIFF_DIFF)
                        sout=f.fmt("%.3s",spnam)+"-"+f.fmt("%.3s",spnam2);
                    else if (diff_mode == DIFF_MIDP)
                        sout=f.fmt("%.3s",spnam)+"/"+f.fmt("%.3s",spnam2);
                    else {
                        sout=f.fmt("%-15s",spnam);
                    }
                    DevNull.print(sout);
                    break;
                case (int)'J':
                    if (is_label) { DevNull.println("julday"); break; }
                    y_frac = (t - (int)(t)) * 100;
                    if ((int)(y_frac) != y_frac)
                        sout=f.fmt("%.5f", t);
                    else
                        sout=f.fmt("%.2f", t);
                    DevNull.print(sout);
                    break;
                case (int)'T':
                    if (is_label) { DevNull.println("date"); break; }
                    sout=f.fmt("%02d",jday)+"."+f.fmt("%02d",jmon)+"."+jyear;
                    if (jut != 0) {
                        int h, m, s;
                        s = (int) (jut * 3600 + 0.5);
                        h = (int) (s / 3600.0);
                        m = (int) ((s % 3600) / 60.0);
                        s %= 60;
                        sout+=" "+f.fmt("%d",h)+":"+f.fmt("%02d",m)+":"+
                            f.fmt("%02d",s);
                        if (universal_time)
                            sout+=" UT";
                        else
                            sout+=" ET";
                    }
                    DevNull.print(sout);
                    break;
                case (int)'t':
                    if (is_label) { DevNull.println("date"); break; }
                    sout=f.fmt("%02d",jyear % 100)+f.fmt("%02d",jmon)+
                        f.fmt("%02d",jday);
                    DevNull.print(sout);
                    break;
                case (int)'L':
                    if (is_label) { DevNull.println("long."); break; }
                    if (p >= psp.length() || (p < psp.length() && psp.charAt(p) != 'q')) { /* != delta t */
                        DevNull.print(dms(x[0], round_flag));
                        break;
                    }
                    // Fall through else...
                case (int)'l':
                    if (is_label && sp.charAt(c) != 'l') { DevNull.println("long"); break; }
                    sout=f.fmt("%# 11.7f", x[0]);
                    DevNull.print(sout);
                    break;
                case (int)'G':
                    if (is_label) { DevNull.println("housPos"); break; }
                    DevNull.print(dms(hpos, round_flag));
                    break;
                case (int)'g':
                    if (is_label) { DevNull.println("housPos"); break; }
                    sout=f.fmt("%# 11.7f", hpos);
                    DevNull.print(sout);
                    break;
                case (int)'j':
                    if (is_label) { DevNull.println("houseNr"); break; }
                    sout=f.fmt("%# 11.7f", hposj.val);
                    DevNull.print(sout);
                    break;
                case (int)'Z':
                    if (is_label) { DevNull.println("long"); break; }
                    DevNull.print(dms(x[0], round_flag|BIT_ZODIAC));
                    break;
                case (int)'S':
                case (int)'s':
                    if (fmt.indexOf("X")>=0 || fmt.indexOf("U")>=0 ||
                        fmt.indexOf("x")>=0 || fmt.indexOf("u")>=0 ||
                        (sp.length()>c+1 &&
                         (sp.charAt(c+1) == 'S' || sp.charAt(c+1) == 's'))) {
                        int c2=0;
                        for (sp2 = fmt; c2<fmt.length(); c2++) {
                            if (c2 != 0)
                                DevNull.print(gap);
                            switch((int)sp2.charAt(c2)) {
                                case (int)'L':   /* speed! */
                                case (int)'Z':   /* speed! */
                                    if (is_label) { DevNull.println("lon/day"); break; }
                                    DevNull.print(dms(x[3], round_flag));
                                    break;
                                case (int)'l':   /* speed! */
                                    if (is_label) { DevNull.println("lon/day"); break; }
                                    sout=f.fmt("%11.7f", x[3]);
                                    DevNull.print(sout);
                                    break;
                                case (int)'B':   /* speed! */
                                    if (is_label) { DevNull.println("lat/day"); break; }
                                    DevNull.print(dms(x[4], round_flag));
                                    break;
                                case (int)'b':   /* speed! */
                                    if (is_label) { DevNull.println("lat/day"); break; }
                                    sout=f.fmt("%11.7f", x[4]);
                                    DevNull.print(sout);
                                    break;
                                case (int)'A':   /* speed! */
                                    if (is_label) { DevNull.println("RA/day"); break; }
                                    DevNull.print(dms(xequ[3]/15,
                                                         round_flag|SweConst.SEFLG_EQUATORIAL));
                                    break;
                                case (int)'a':   /* speed! */
                                    if (is_label) { DevNull.println("RA/day"); break; }
                                    sout=f.fmt("%11.7f", xequ[3]);
                                    DevNull.print(sout);
                                    break;
                                case (int)'D':   /* speed! */
                                    if (is_label) { DevNull.println("dcl/day"); break; }
                                    DevNull.print(dms(xequ[4], round_flag));
                                    break;
                                case (int)'d':   /* speed! */
                                    if (is_label) { DevNull.println("dcl/day"); break; }
                                    sout=f.fmt("%11.7f", xequ[4]);
                                    DevNull.print(sout);
                                    break;
                                case (int)'R':   /* speed! */
                                case (int)'r':   /* speed! */
                                    if (is_label) { DevNull.println("AU/day"); break; }
                                    sout=f.fmt("%# 14.9f", x[5]);
                                    DevNull.print(sout);
                                    break;
                                case (int)'U':   /* speed! */
                                case (int)'X':   /* speed! */
                                    if (is_label) { DevNull.println("speed_0"+gap+"speed_1"+gap+"speed_2"); break; }
                                    if (sp.charAt(c) =='U')
                                        ar = Math.sqrt(square_sum(xcart));
                                    else
                                        ar = 1;
                                    sout=f.fmt("%# 14.9f", xcart[3]/ar)+gap;
                                    DevNull.print(sout);
                                    sout=f.fmt("%# 14.9f", xcart[4]/ar)+gap;
                                    DevNull.print(sout);
                                    sout=f.fmt("%# 14.9f", xcart[5]/ar);
                                    DevNull.print(sout);
                                    break;
                                case (int)'u':   /* speed! */
                                case (int)'x':   /* speed! */
                                    if (is_label) { DevNull.println("speed_0"+gap+"speed_1"+gap+"speed_2"); break; }
                                    if (sp.charAt(c) =='u')
                                        ar = Math.sqrt(square_sum(xcartq));
                                    else
                                        ar = 1;
                                    sout=f.fmt("%# 14.9f", xcartq[3]/ar)+gap;
                                    DevNull.print(sout);
                                    sout=f.fmt("%# 14.9f", xcartq[4]/ar)+gap;
                                    DevNull.print(sout);
                                    sout=f.fmt("%# 14.9f", xcartq[5]/ar);
                                    DevNull.print(sout);
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (sp.charAt(c+1) == 'S' || sp.charAt(c+1) == 's')
                            c++;
                    } else if (sp.charAt(c) == 'S') {
                        if (is_label) { DevNull.println("deg/day"); break; }
                        DevNull.print(dms(x[3], round_flag));
                    } else {
                        if (is_label) { DevNull.println("deg/day"); break; }
                        DevNull.print(f.fmt("%# 11.7f", x[3]));
                    }
                    break;
                case (int)'B':
                    if (is_label) { DevNull.println("lat"); break; }
                    DevNull.print(dms(x[1], round_flag));
                    break;
                case (int)'b':
                    if (is_label) { DevNull.println("lat"); break; }
                    sout=f.fmt("%# 11.7f", x[1]);
                    DevNull.print(sout);
                    break;
                case (int)'A':     /* rectascensio */
                    if (is_label) { DevNull.println("RA"); break; }
                    DevNull.print(dms(xequ[0]/15,
                                         round_flag|SweConst.SEFLG_EQUATORIAL));
                    break;
                case (int)'a':     /* rectascensio */
                    if (is_label) { DevNull.println("RA"); break; }
                    sout=f.fmt("%# 11.7f", xequ[0]);
                    DevNull.print(sout);
                    break;
                case (int)'D':     /* declination */
                    if (is_label) { DevNull.println("decl"); break; }
                    DevNull.print(dms(xequ[1], round_flag));
                    break;
                case (int)'d':     /* declination */
                    if (is_label) { DevNull.println("decl"); break; }
                    sout=f.fmt("%# 11.7f", xequ[1]);
                    DevNull.print(sout);
                    break;
                case 'I':     /* azimuth */
                    if (is_label) { DevNull.println("azimuth"); break; }
                    DevNull.print(dms(xaz[0], round_flag));
                    break;
                case 'i':     /* azimuth */
                    if (is_label) { DevNull.println("azimuth"); break; }
                    sout=f.fmt("%# 11.7f", xaz[0]);
                    DevNull.print(sout);
                    break;
                case 'H':     /* height */
                    if (is_label) { DevNull.println("height"); break; }
                    DevNull.print(dms(xaz[1], round_flag));
                    break;
                case 'h':     /* height */
                    if (is_label) { DevNull.println("height"); break; }
                    sout=f.fmt("%# 11.7f", xaz[1]);
                    DevNull.print(sout);
                    break;
                case 'K':     /* height (apparent) */
                    if (is_label) { DevNull.println("hgtApp"); break; }
                    DevNull.print(dms(xaz[2], round_flag));
                    break;
                case 'k':     /* height (apparent) */
                    if (is_label) { DevNull.println("hgtApp"); break; }
                    sout=f.fmt("%# 11.7f", xaz[2]);
                    DevNull.print(sout);
                    break;
                case (int)'R':
                    if (is_label) { DevNull.println("distAU"); break; }
                    sout=f.fmt("%# 14.9f", x[2]);
                    DevNull.print(sout);
                    break;
                case (int)'r':
                    if (is_label) { DevNull.println("dist"); break; }
                    if ( ipl == SweConst.SE_MOON ) { /* for moon print parallax */
                        sinp = 8.794 / x[2];    /* in seconds of arc */
                        ar = sinp * (1 + sinp * sinp * 3.917402e-12);
                        /* the factor is 1 / (3600^2 * (180/pi)^2 * 6) */
                        sout=f.fmt("%# 13.5f",ar)+"\"";
                    } else {
                        sout=f.fmt("%# 14.9f", x[2]);
                    }
                    DevNull.print(sout);
                    break;
                case (int)'U':
                case (int)'X':
                    if (sp.charAt(c) =='U')
                        ar = Math.sqrt(square_sum(xcart));
                    else
                        ar = 1;
                    sout=f.fmt("%# 14.9f", xcart[0]/ar)+gap;
                    DevNull.print(sout);
                    sout=f.fmt("%# 14.9f", xcart[1]/ar)+gap;
                    DevNull.print(sout);
                    sout=f.fmt("%# 14.9f", xcart[2]/ar);
                    DevNull.print(sout);
                    break;
                case (int)'u':
                case (int)'x':
                    if (is_label) { DevNull.println("x0"+gap+"x1"+gap+"x2"); break; }
                    if (sp.charAt(c) =='u')
                        ar = Math.sqrt(square_sum(xcartq));
                    else
                        ar = 1;
                    sout=f.fmt("%# 14.9f", xcartq[0]/ar)+gap;
                    DevNull.print(sout);
                    sout=f.fmt("%# 14.9f", xcartq[1]/ar)+gap;
                    DevNull.print(sout);
                    sout=f.fmt("%# 14.9f", xcartq[2]/ar);
                    DevNull.print(sout);
                    break;
                case (int)'Q':
                    if (is_label) { DevNull.println("Q"); break; }
                    //                sout=f.fmt("%-15s", spnam);
                    sout=(spnam+"               ").substring(0,Math.max(15,spnam.length()));
                    DevNull.print(sout);
                    DevNull.print(dms(x[0], round_flag));
                    DevNull.print(dms(x[1], round_flag));
                    sout="  "+f.fmt("%# 14.9f", x[2]);
                    DevNull.print(sout);
                    DevNull.print(dms(x[3], round_flag));
                    DevNull.print(dms(x[4], round_flag));
                    sout="  "+f.fmt("%# 14.9f", x[5])+"\n";
                    DevNull.print(sout);
                    sout="               "+dms(xequ[0], round_flag);
                    DevNull.print(sout);
                    DevNull.print(dms(xequ[1], round_flag));
                    sout="                "+dms(xequ[3], round_flag);
                    DevNull.print(sout);
                    DevNull.print(dms(xequ[4], round_flag));
                    break;
                case 'N':
                case 'n': {
                    double xasc[]=new double[6], xdsc[]=new double[6];
                    int imeth = (Character.isLowerCase(sp.charAt(c)))?
                        SweConst.SE_NODBIT_MEAN:SweConst.SE_NODBIT_OSCU;
                    iflgret = sw.swe_nod_aps(te, ipl, iflag, imeth, xasc,
                                             xdsc, null, null, serr);
                    if (iflgret >= 0 &&
                        (ipl <= SweConst.SE_NEPTUNE || sp.charAt(c) == 'N') ) {
                        if (is_label) { DevNull.println("nodAsc"+gap+"nodDesc"); break; }
                        DevNull.print(f.fmt("%# 11.7f",xasc[0]));
                        DevNull.print(gap);
                        DevNull.print(f.fmt("%# 11.7f",xdsc[0]));
                    }
                };
                    break;
                case 'F':
                case 'f':
                    if (!is_house) {
                        double xfoc[]=new double[6], xaph[]=new double[6],
                            xper[]=new double[6];
                        int imeth = (Character.isLowerCase(sp.charAt(c)))?
                            SweConst.SE_NODBIT_MEAN:SweConst.SE_NODBIT_OSCU;
                        iflgret = sw.swe_nod_aps(te, ipl, iflag, imeth, null, null,
                                                 xper, xaph, serr);
                        if (iflgret >= 0 && (ipl <= SweConst.SE_NEPTUNE ||
                                             sp.charAt(c) == 'F') ) {
                            if (is_label) { DevNull.println("peri"+gap+"apo"); break; }
                            DevNull.print(f.fmt("%# 11.7f", xper[0]));
                            DevNull.print(gap);
                            DevNull.print(f.fmt("%# 11.7f", xaph[0]));
                        }
                        imeth |= SweConst.SE_NODBIT_FOPOINT;
                        iflgret = sw.swe_nod_aps(te, ipl, iflag, imeth, null, null,
                                                 xper, xfoc, serr);
                        if (iflgret >= 0 && (ipl <= SweConst.SE_NEPTUNE ||
                                             sp.charAt(c) == 'F') ) {
                            if (is_label) { DevNull.println(gap+"focus"); break; }
                            DevNull.print(gap);
                            DevNull.print(f.fmt("%# 11.7f", xfoc[0]));
                        }
                    };
                    break;
                case '+':
                    if (is_house) break;
                    if (is_label) { DevNull.println("phase"); break; }
                    DevNull.print(dms(attr[0], round_flag));
                    break;
                case '-':
                    if (is_label) { DevNull.println("phase"); break; }
                    if (is_house) break;
                    DevNull.print("  "+f.fmt("%# 14.9f", attr[1]));
                    break;
                case '*':
                    if (is_label) { DevNull.println("elong"); break; }
                    if (is_house) break;
                    DevNull.print(dms(attr[2], round_flag));
                    break;
                case '/':
                    if (is_label) { DevNull.println("diamet"); break; }
                    if (is_house) break;
                    DevNull.print(dms(attr[3], round_flag));
                    break;
                case '=':
                    if (is_label) { DevNull.println("magn"); break; }
                    if (is_house) break;
                    DevNull.print("  "+f.fmt("%# 6.1f", attr[4])+"m");
                    break;
            }     /* switch */
        }       /* for sp */
        DevNull.print("\n");
        return SweConst.OK;
    }

    private String dms(double xv, int iflag) {
        int izod;
        int k, kdeg, kmin, ksec;
        String c = swed.ODEGREE_CHAR;
        String s1;
        String s;
        int sgn;
        s = "";
        if ((iflag & SweConst.SEFLG_EQUATORIAL)!=0)
            c = "h";
        if (xv < 0) {
            xv = -xv;
            sgn = -1;
        } else
            sgn = 1;
        if ((iflag & BIT_ROUND_MIN)!=0)
            xv = sl.swe_degnorm(xv + 0.5/60);
        if ((iflag & BIT_ROUND_SEC)!=0)
            xv = sl.swe_degnorm(xv + 0.5/3600);
        if ((iflag & BIT_ZODIAC)!=0) {
            izod = (int) (xv / 30)%12;
            xv%=30.;
            kdeg = (int) xv;
            s=f.fmt("%2ld",kdeg)+" "+zod_nam[izod]+" ";
        } else {
            kdeg = (int) xv;
            s=" "+f.fmt("%3ld", kdeg)+c;
        }
        xv -= kdeg;
        xv *= 60;
        kmin = (int) xv;
        if ((iflag & BIT_ZODIAC)!=0 && (iflag & BIT_ROUND_MIN)!=0)
            s1=f.fmt("%2ld", kmin);
        else
            s1=f.fmt("%2ld", kmin)+"'";
        s+=s1;
        if ((iflag & BIT_ROUND_MIN)!=0)
            return return_dms(sgn,s);
        xv -= kmin;
        xv *= 60;
        ksec = (int) xv;
        if ((iflag & BIT_ROUND_SEC)!=0)
            s1=f.fmt("%2ld", ksec)+"\"";
        else
            s1=f.fmt("%2ld", ksec);
        s+=s1;
        if ((iflag & BIT_ROUND_SEC)!=0)
            return return_dms(sgn,s);
        xv -= ksec;
        k = (int) (xv * 10000);
        s1="."+f.fmt("%04ld", k);
        s+=s1;
        return return_dms(sgn,s);
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

        // such a hack that I am ashamed, but I say fuck it, I don't want to try
        // to figure out what this code does, the interface is very very
        // confusing, and I am no astrologist, astrologer, nor I want to become
        // one. Never spent such a long time to get one fucking number out of a
        // system, I swear to God.        
        globalResult = s;
        
        return(s);
    }

    private int letter_to_ipl(int letter) {
        if (letter >= (int)'0' && letter <= (int)'9')
            return letter - (int)'0' + SweConst.SE_SUN;
        if (letter >= (int)'A' && letter <= (int)'I')
            return letter - (int)'A' + SweConst.SE_MEAN_APOG;
        if (letter >= (int)'J' && letter <= (int)'Z')
            return letter - (int)'J' + SweConst.SE_CUPIDO;
        switch (letter) {
            case (int)'m': return SweConst.SE_MEAN_NODE;
            case (int)'c': return SweConst.SE_INTP_APOG;
            case (int)'g': return SweConst.SE_INTP_PERG;
            case (int)'n':
            case (int)'o': return SweConst.SE_ECL_NUT;
            case (int)'t': return SweConst.SE_TRUE_NODE;
            case (int)'f': return SweConst.SE_FIXSTAR;
            case (int)'w': return SweConst.SE_WALDEMATH;
            case 'e': /* swetest: a line of labels */
            case 'q': /* swetest: delta t */
            case 's': /* swetest: an asteroid, with number given in -xs[number] */
            case 'z': /* swetest: a fictitious body, number given in -xz[number] */
            case 'd': /* swetest: default (main) factors 0123456789mtABC */
            case 'p': /* swetest: main factors ('d') plus main asteroids DEFGHI */
            case 'h': /* swetest: fictitious factors JKLMNOPQRSTUVWXYZw */
            case 'a': /* swetest: all factors, like 'p'+'h' */
                return -1;
        }
        return -2;
    }


    private int do_eclipse_etc() {
        DblObj dtmp=new DblObj();
        StringBuffer sstar;
        int ii, i;
        boolean has_found = false;
        double t_ut, dt;
        int ecl_type = 0, eclflag;
        String s1, s2;
        int rsmi = 0;
        if ((smod & (SMOD_LUNAR | SMOD_SOLAR | SMOD_OCCULT))!=0)  {
            if ((smod & SMOD_ALL) == 0) /* no selective eclipse type set, set all */
                smod |= SMOD_ALL;
            if ((smod & SMOD_TOTAL)!=0) search_flag |= SweConst.SE_ECL_TOTAL;
            if ((smod & SMOD_ANNULAR)!=0) search_flag |= SweConst.SE_ECL_ANNULAR | SweConst.SE_ECL_PENUMBRAL;
            if ((smod & SMOD_PARTIAL)!=0) search_flag |= SweConst.SE_ECL_PARTIAL;
            if ((smod & SMOD_ANNTOT)!=0) search_flag |= SweConst.SE_ECL_ANNULAR_TOTAL;
        }
        /* 
         * for local eclipses: set geographic position of observer 
         */
        if ((smod & (SMOD_LOCAL|SMOD_RISE|SMOD_METR))!=0)
            sw.swe_set_topo(geopos[0], geopos[1], geopos[2]); 
        t_ut = tjd;
        do_printf("\n");
        for (ii = 0; ii < nstep; ii++) {
            sout = "";
            if ((smod & SMOD_LUNAR)!=0 && (smod & SMOD_HOW)!=0) {
                if ((eclflag = sw.swe_lun_eclipse_how(t_ut, whicheph, geopos, attr, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } else {
                    ecl_type = 0;
                    if ((eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                        sout="total lunar eclipse: "+f.fmt("%f",attr[0])+" o/o\n";
                        ecl_type = ECL_LUN_TOTAL;
                    } else if ((eclflag & SweConst.SE_ECL_PARTIAL)!=0)  {
                        sout="partial lunar eclipse: "+f.fmt("%f",attr[0])+" o/o\n";
                        ecl_type = ECL_LUN_PARTIAL;
                    } else if ((eclflag & SweConst.SE_ECL_PENUMBRAL)!=0)  {
                        sout="penumbral lunar eclipse: "+f.fmt("%f",attr[0])+" o/o\n";
                        ecl_type = ECL_LUN_PENUMBRAL;
                    } else {
                        sout="no lunar eclipse\n";
                    }
                    do_printf(sout);
                }
            }
            if ((smod & SMOD_LUNAR)!=0 && (smod & SMOD_HOW)==0) {
                if ((eclflag = sw.swe_lun_eclipse_when(t_ut, whicheph, search_flag, 
                                                       tret, direction_flag?-1:0, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } 
                t_ut = tret[0];
                if ((eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                    sout="total   ";
                    ecl_type = ECL_LUN_TOTAL;
                }
                if ((eclflag & SweConst.SE_ECL_PENUMBRAL)!=0) {
                    sout="penumb. ";
                    ecl_type = ECL_LUN_PENUMBRAL;
                }
                if ((eclflag & SweConst.SE_ECL_PARTIAL)!=0) {
                    sout="partial ";
                    ecl_type = ECL_LUN_PARTIAL;
                }
                sout+="lunar eclipse ";
                //        swe_revjul(t_ut, gregflag, &jyear, &jmon, &jday, &jut);
                sd.setJulDay(t_ut);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
                if ((eclflag = sw.swe_lun_eclipse_how(t_ut, whicheph, geopos, attr, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                }
                /* eclipse times, penumbral, partial, total begin and end */
                sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+
                    f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\t"+f.fmt("%f",attr[0])+
                    " o/o\n";
                sout += "  "+hms_from_tjd(tret[6])+" "; 
                if (tret[2] != 0)
                    sout += hms_from_tjd(tret[2])+" "; 
                else
                    sout+="   -         ";
                if (tret[4] != 0)
                    sout += hms_from_tjd(tret[4])+" "; 
                else
                    sout+="   -         ";
                if (tret[5] != 0)
                    sout += hms_from_tjd(tret[5]) + " "; 
                else
                    sout+="   -         ";
                if (tret[3] != 0)
                    sout += hms_from_tjd(tret[3]) + " "; 
                else
                    sout+="   -         ";
                sout += hms_from_tjd(tret[7]) + "\n"; 
                if ((smod & SMOD_HOCAL)!=0) {
                    IntObj ihou=new IntObj(), imin=new IntObj(), isec=new IntObj(), isgn=new IntObj();
                    DblObj dfrc=new DblObj();
                    sl.swe_split_deg(jut, SweConst.SE_SPLIT_DEG_ROUND_MIN, ihou, imin, isec, dfrc, isgn);
                    sout="\""+f.fmt("%04d",jyear)+" "+f.fmt("%02d",jmon)+" "+f.fmt("%02d",jday)+" "+f.fmt("%02d",ihou.val)+"."+f.fmt("%02d",imin.val)+" "+f.fmt("%d",ecl_type)+"\",\n";
                } 
                do_printf(sout);
            }
            if ((smod & SMOD_SOLAR)!=0 && (smod & SMOD_LOCAL)!=0) {
                if ((eclflag = sw.swe_sol_eclipse_when_loc(t_ut, whicheph, geopos, tret, attr, direction_flag?-1:0, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } else { 
                    has_found = false;
                    t_ut = tret[0];
                    if ((smod & SMOD_TOTAL)!=0 && (eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                        sout="total   ";
                        has_found = true;
                        ecl_type = ECL_SOL_TOTAL;
                    }
                    if ((smod & SMOD_ANNULAR)!=0 && (eclflag & SweConst.SE_ECL_ANNULAR)!=0) {
                        sout="annular ";
                        has_found = true;
                        ecl_type = ECL_SOL_ANNULAR;
                    }
                    if ((smod & SMOD_PARTIAL)!=0 && (eclflag & SweConst.SE_ECL_PARTIAL)!=0) {
                        sout="partial ";
                        has_found = true;
                        ecl_type = ECL_SOL_PARTIAL;
                    }
                    if (!has_found) {
                        ii--;
                    } else {
                        i = sw.swe_calc(t_ut + sd.getDeltaT(t_ut), SweConst.SE_ECL_NUT, 0, x, serr);
                        sd.setJulDay(t_ut);
                        sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                        jyear=sd.getYear();
                        jmon=sd.getMonth();
                        jday=sd.getDay();
                        jut=sd.getHour();
                        sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\t"+f.fmt("%f",attr[0])+"o/o\n";
                        dt = (tret[3] - tret[2]) * 24 * 60;
                        sout += "\t"+(int)dt+" min "+f.fmt("%4.2f",(dt%1.) * 60)+" sec\t";
                        if ((eclflag & SweConst.SE_ECL_1ST_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[1]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_2ND_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[2]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_3RD_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[3]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_4TH_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[4]) + " "; 
                        else
                            sout+="   -         ";
                        sout+="\n";
                        do_printf(sout);
                    }
                }
            }   /* endif search_local */
            if ((smod & SMOD_OCCULT)!=0 && (smod & SMOD_LOCAL)!=0) {
                sstar=new StringBuffer(star);
                if ((eclflag = sw.swe_lun_occult_when_loc(t_ut, ipl, sstar, whicheph, geopos, tret, attr, direction_flag?-1:0, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } else {
                    star=sstar.toString();
                    has_found = false;
                    t_ut = tret[0];
                    if ((smod & SMOD_TOTAL)!=0 && (eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                        sout="total   ";
                        has_found = true;
                        ecl_type = ECL_SOL_TOTAL;
                    }
                    if ((smod & SMOD_ANNULAR)!=0 && (eclflag & SweConst.SE_ECL_ANNULAR)!=0) {
                        sout="annular ";
                        has_found = true;
                        ecl_type = ECL_SOL_ANNULAR;
                    }
                    if ((smod & SMOD_PARTIAL)!=0 && (eclflag & SweConst.SE_ECL_PARTIAL)!=0) {
                        sout="partial ";
                        has_found = true;
                        ecl_type = ECL_SOL_PARTIAL;
                    }
                    if (!has_found) {
                        ii--;
                    } else {
                        i = sw.swe_calc(t_ut + sd.getDeltaT(t_ut), SweConst.SE_ECL_NUT, 0, x, serr);
                        sd.setJulDay(t_ut);
                        sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                        jyear=sd.getYear();
                        jmon=sd.getMonth();
                        jday=sd.getDay();
                        jut=sd.getHour();
                        sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\t"+f.fmt("%f",attr[0])+"o/o\n";
                        dt = (tret[3] - tret[2]) * 24 * 60;
                        sout += "\t"+f.fmt("%d",(int) dt)+" min "+f.fmt("%4.2f",(dt%1.)*60)+" sec\t";
                        if ((eclflag & SweConst.SE_ECL_1ST_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[1]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_2ND_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[2]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_3RD_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[3]) + " "; 
                        else
                            sout+="   -         ";
                        if ((eclflag & SweConst.SE_ECL_4TH_VISIBLE)!=0)
                            sout += hms_from_tjd(tret[4]) + " "; 
                        else
                            sout+="   -         ";
                        sout+="\n";
                        do_printf(sout);
                    }
                }
            }   /* endif search_local */
            if ((smod & SMOD_SOLAR)!=0 && (smod & SMOD_LOCAL)==0) {
                if ((eclflag = sw.swe_sol_eclipse_when_glob(t_ut, whicheph, search_flag,
                                                            tret, direction_flag?-1:0, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } 
                t_ut = tret[0];
                if ((eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                    sout="total   ";
                    ecl_type = ECL_SOL_TOTAL;
                }
                if ((eclflag & SweConst.SE_ECL_ANNULAR)!=0) {
                    sout="annular ";
                    ecl_type = ECL_SOL_ANNULAR;
                }
                if ((eclflag & SweConst.SE_ECL_ANNULAR_TOTAL)!=0) {
                    sout="ann-tot ";
                    ecl_type = ECL_SOL_ANNULAR;        /* by Alois: what is this ? */
                }
                if ((eclflag & SweConst.SE_ECL_PARTIAL)!=0) {
                    sout="partial ";
                    ecl_type = ECL_SOL_PARTIAL;
                }
                if ((eclflag & SweConst.SE_ECL_NONCENTRAL)!=0 && (eclflag & SweConst.SE_ECL_PARTIAL)==0)
                    sout+="non-central ";
                sw.swe_sol_eclipse_where(t_ut, whicheph, geopos, attr, serr);
                //      swe_revjul(t_ut, gregflag, &jyear, &jmon, &jday, &jut);
                sd.setJulDay(t_ut);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
                sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\t"+f.fmt("%f",attr[3])+" km\t"+f.fmt("%f",attr[0])+" o/o\n";
                sout += "\t" + hms_from_tjd(tret[2]) + " "; 
                if (tret[4] != 0)
                    sout += hms_from_tjd(tret[4]) + " "; 
                else
                    sout+="   -         ";
                if (tret[5] != 0)
                    sout += hms_from_tjd(tret[5]) + " "; 
                else
                    sout+="   -         ";
                sout += hms_from_tjd(tret[3]) + "\n"; 
                s1=dms(geopos[0], BIT_ROUND_MIN);
                s2=dms(geopos[1], BIT_ROUND_MIN);
                sout += "\t"+s1+"\t"+s2;
                if ((eclflag & SweConst.SE_ECL_PARTIAL)==0 && (eclflag & SweConst.SE_ECL_NONCENTRAL)==0) {
                    if ((eclflag = sw.swe_sol_eclipse_when_loc(t_ut - 10, whicheph, geopos, tret, attr, 0, serr)) == SweConst.ERR) {
                        do_printf(serr);
                        System.exit(0);
                    }
                    if (Math.abs(tret[0] - t_ut) > 1) 
                        do_printf("when_loc returns wrong date\n");
                    dt = (tret[3] - tret[2]) * 24 * 60;
                    sout += "\t"+(int)dt+" min "+f.fmt("%4.2f",(dt%1.) * 60.)+" sec\t";
                }
                sout+="\n";
                if ((smod & SMOD_HOCAL)!=0) {
                    IntObj ihou=new IntObj(), imin=new IntObj(), isec=new IntObj(), isgn=new IntObj();
                    DblObj dfrc=new DblObj();
                    sl.swe_split_deg(jut, SweConst.SE_SPLIT_DEG_ROUND_MIN, ihou, imin, isec, dfrc, isgn);
                    sout="\""+f.fmt("%04d",jyear)+" "+f.fmt("%02d",jmon)+" "+f.fmt("%02d",jday)+" "+f.fmt("%02d",ihou.val)+"."+f.fmt("%02d",imin.val)+" "+f.fmt("%d",ecl_type)+"\",\n";
                } 
                do_printf(sout);
            }
            if ((smod & SMOD_OCCULT)!=0 && (smod & SMOD_LOCAL)==0) {
                /* * global search for eclipses */
                sstar=new StringBuffer(star);
                if ((eclflag = sw.swe_lun_occult_when_glob(t_ut, ipl, sstar, whicheph, search_flag, tret, direction_flag?-1:0, serr)) == SweConst.ERR) {
                    do_printf(serr);
                    System.exit(0);
                } 
                star=sstar.toString();
                t_ut = tret[0];
                if ((eclflag & SweConst.SE_ECL_TOTAL)!=0) {
                    sout="total   ";
                    ecl_type = ECL_SOL_TOTAL;
                }
                if ((eclflag & SweConst.SE_ECL_ANNULAR)!=0) {
                    sout="annular ";
                    ecl_type = ECL_SOL_ANNULAR;
                }
                if ((eclflag & SweConst.SE_ECL_ANNULAR_TOTAL)!=0) {
                    sout="ann-tot ";
                    ecl_type = ECL_SOL_ANNULAR;        /* by Alois: what is this ? */
                }
                if ((eclflag & SweConst.SE_ECL_PARTIAL)!=0) {
                    sout="partial ";
                    ecl_type = ECL_SOL_PARTIAL;
                }
                if ((eclflag & SweConst.SE_ECL_NONCENTRAL)!=0 && (eclflag & SweConst.SE_ECL_PARTIAL)==0)
                    sout+="non-central ";
                sstar=new StringBuffer(star);
                sw.swe_lun_occult_where(t_ut, ipl, sstar, whicheph, geopos, attr, serr);
                star=sstar.toString();
                sd.setJulDay(t_ut);
                sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                jyear=sd.getYear();
                jmon=sd.getMonth();
                jday=sd.getDay();
                jut=sd.getHour();
                sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\t"+f.fmt("%f",attr[3])+" km\t"+f.fmt("%f",attr[0])+" o/o\n";
                sout += "\t" + hms_from_tjd(tret[2]) + " "; 
                if (tret[4] != 0)
                    sout += hms_from_tjd(tret[4]) + " "; 
                else
                    sout+="   -         ";
                if (tret[5] != 0)
                    sout += hms_from_tjd(tret[5]) + " "; 
                else
                    sout+="   -         ";
                sout += hms_from_tjd(tret[3]) + "\n"; 
                s1=dms(geopos[0], BIT_ROUND_MIN);
                s2=dms(geopos[1], BIT_ROUND_MIN);
                sout += "\t"+s1+"\t"+s2;
                if ((eclflag & SweConst.SE_ECL_PARTIAL)==0 && (eclflag & SweConst.SE_ECL_NONCENTRAL)==0) {
                    sstar=new StringBuffer(star);
                    if ((eclflag = sw.swe_lun_occult_when_loc(t_ut - 10, ipl, sstar, whicheph, geopos, tret, attr, 0, serr)) == SweConst.ERR) {
                        do_printf(serr);
                        System.exit(0);
                    }
                    star=sstar.toString();
                    if (Math.abs(tret[0] - t_ut) > 1) 
                        do_printf("when_loc returns wrong date\n");
                    dt = (tret[3] - tret[2]) * 24 * 60;
                    sout += "\t"+(int)dt+" min "+f.fmt("%4.2f",(dt%1.)*60)+" sec\t";
                } 
                sout+="\n";
                if ((smod & SMOD_HOCAL)!=0) {
                    IntObj ihou=new IntObj(), imin=new IntObj(), isec=new IntObj(), isgn=new IntObj();
                    DblObj dfrc=new DblObj();
                    sl.swe_split_deg(jut, SweConst.SE_SPLIT_DEG_ROUND_MIN, ihou, imin, isec, dfrc, isgn);
                    sout="\""+f.fmt("%04d",jyear)+" "+f.fmt("%02d",jmon)+" "+f.fmt("%02d",jday)+" "+f.fmt("%02d",ihou.val)+"."+f.fmt("%02d",imin.val)+" "+f.fmt("%d",ecl_type)+"\",\n";
                } 
                do_printf(sout);
            }
            if ((smod & SMOD_RISE)!=0) {
                dtmp.val=tret[0];
                sstar=new StringBuffer(star);
                rsmi = SweConst.SE_CALC_RISE;
                if (norefrac) rsmi |= SweConst.SE_BIT_NO_REFRACTION;
                if (disccenter) rsmi |= SweConst.SE_BIT_DISC_CENTER;
                if (sw.swe_rise_trans(t_ut, ipl, sstar, whicheph, rsmi, geopos, 1013.25, 10, dtmp, serr) !=
                    SweConst.OK) {
                    do_printf(serr);
                    System.exit(0);
                } 
                star=sstar.toString();
                tret[0]=dtmp.val;
                dtmp.val=tret[1];
                sstar=new StringBuffer(star);
                rsmi = SweConst.SE_CALC_SET;
                if (norefrac) rsmi |= SweConst.SE_BIT_NO_REFRACTION;
                if (disccenter) rsmi |= SweConst.SE_BIT_DISC_CENTER;
                if (sw.swe_rise_trans(t_ut, ipl, sstar, whicheph, rsmi, geopos, 1013.25, 10, dtmp, serr) !=
                    SweConst.OK) {
                    do_printf(serr);
                    System.exit(0);
                } 
                star=sstar.toString();
                tret[1]=dtmp.val;
                sout="rise     ";
                if (tret[0] == 0 || tret[0] > tret[1]) {
                    sout+="         -                     ";
                } else {
                    sd.setJulDay(tret[0]);
                    sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                    jyear=sd.getYear();
                    jmon=sd.getMonth();
                    jday=sd.getDay();
                    jut=sd.getHour();
                    sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"    ";
                }
                sout+="set      ";
                if (tret[1] == 0) {
                    sout+="         -                     \n";
                } else {
                    sd.setJulDay(tret[1]);
                    sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                    jyear=sd.getYear();
                    jmon=sd.getMonth();
                    jday=sd.getDay();
                    jut=sd.getHour();
                    sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\n"; 
                }
                do_printf(sout);
            }
            if ((smod & SMOD_METR)!=0) {
                dtmp.val=tret[0];
                sstar=new StringBuffer(star);
                if (sw.swe_rise_trans(t_ut, ipl, sstar, whicheph, SweConst.SE_CALC_MTRANSIT, geopos, 1013.25, 10, dtmp, serr) != SweConst.OK) {
                    do_printf(serr);
                    System.exit(0);
                } 
                star=sstar.toString();
                tret[0]=dtmp.val;
                dtmp.val=tret[1];
                sstar=new StringBuffer(star);
                if (sw.swe_rise_trans(t_ut, ipl, sstar, whicheph, SweConst.SE_CALC_ITRANSIT, geopos, 1013.25, 10, dtmp, serr) != SweConst.OK) {
                    do_printf(serr);
                    System.exit(0);
                }
                star=sstar.toString();
                tret[1]=dtmp.val;
                sout="mtransit ";
                if (tret[0] == 0) {
                    sout+="         -                     ";
                } else {
                    sd.setJulDay(tret[0]);
                    sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                    jyear=sd.getYear();
                    jmon=sd.getMonth();
                    jday=sd.getDay();
                    jut=sd.getHour();
                    sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"    "; 
                }
                sout+="itransit ";
                if (tret[1] == 0) {
                    sout+="         -                     \n";
                } else {
                    sd.setJulDay(tret[1]);
                    sd.setCalendarType(gregflag,SweDate.SE_KEEP_JD); // Keep JulDay!
                    jyear=sd.getYear();
                    jmon=sd.getMonth();
                    jday=sd.getDay();
                    jut=sd.getHour();
                    sout += f.fmt("%2d",jday)+"."+f.fmt("%2d",jmon)+"."+f.fmt("%4d",jyear)+"\t"+hms(jut,0)+"\n"; 
                }
                do_printf(sout);
            }
            if ((smod & (SMOD_RISE | SMOD_METR))!=0 && tret[1] > 0)
                t_ut = tret[1] + 0.1;
            else
                t_ut += direction;
        }
        if (serr_warn.length() > 0) {
            do_printf("\nwarning: ");
            do_printf(serr_warn);
            do_printf("\n");
        }
        return SweConst.OK;
    }
  
    private String hms_from_tjd(double x) {
        String s;
        s=hms(((x + 1000000.5)%1.) * 24, 0)+" ";
        return s;
    }
  
    private String hms(double x, int iflag) {
        String s;
        int sp;
        String c = swed.ODEGREE_CHAR;
        x += 0.5 / 36000.0; /* round to 0.1 sec */
        s=dms(x, iflag);
        sp = s.indexOf(c);
        if (sp >= 0) {
            s = s.substring(0,sp) + ":" + s.substring(sp+c.length());
            s = s.replace('\'',':');
            s = s.substring(0,s.lastIndexOf(':')+5);
        }
        return s;
    }
  
    static void do_printf(StringBuffer info) {
        DevNull.print(info.toString());
    }
    static void do_printf(String info) {
        DevNull.print(info);
    }

    /* make_ephemeris_path().
     * ephemeris path includes
     *   current working directory
     *   + program directory
     *   + default path from swephexp.h on current drive
     *   +                              on program drive
     *   +                              on drive C:
     */
    private int make_ephemeris_path(int iflg, String argv0) {
        String path="", s="";
        int sp;
        String dirglue = swed.DIR_GLUE;
        int pathlen = 0;
        /* moshier needs no ephemeris path */
        if ((iflg & SweConst.SEFLG_MOSEPH)!=0)
            return SweConst.OK;
        /* current working directory */
        path="."+swed.PATH_SEPARATOR.charAt(0);
        /* program directory */
        sp = argv0.lastIndexOf(dirglue);
        if (sp >= 0) {
            pathlen = sp;
            if (path.length() + pathlen < swed.AS_MAXCH-1) {
                s=argv0.substring(0,pathlen);
                path=path+s+swed.PATH_SEPARATOR.charAt(0);
            }
        }
        if (path.length() + pathlen < swed.AS_MAXCH-1)
            path+=SweConst.SE_EPHE_PATH;
        return SweConst.OK;
    }


} // End of class Swetest
