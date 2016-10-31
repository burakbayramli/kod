//#ifdef NO_RISE_TRANS
//#define ASTROLOGY
//#endif /* NO_RISE_TRANS */

//#ifdef TRACE1
//#define TRACE0
//#endif /* TRACE1 */
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

import java.io.*;
import java.net.*;


/**
* This class is meant to be a wrapper to some read functionality of the
* RandomAccessFile class. It adds the ability to read (and search and
* seek) in files using http://... access. This is needed to allow applets
* read access to files, in this case the Swiss Ephemeris and JPL data
* files.
*/
public class FilePtr {
  public static final String useragent="swisseph-java-1.70.03(00)";

  private static final int MAX_FAILURES=100;

  RandomAccessFile fp;
  Socket sk;
  InputStream is;
  BufferedOutputStream os;
  String fnamp;
  private long fpos=0;
  private String host;
  private int port;
  private int BUFSIZE=20;
  // Holds max. 1 BUFSIZE byte chunks of read data: startidx, endidx, data:
  private long[] startIdx=new long[1];  // long, as it holds the file pointer pos.
  private long[] endIdx=new long[1];
  private byte[][] data;
  private byte inbuf[];
  private int idx=0; // What to fill next.
  private long savedLength=-1;


  /**
  * Creates a new FilePtr instance. Well, the parameters are rather
  * &quot;funny&quot; for now, but there were reasons for it. I will
  * change it later (hopefully)...<br>
  * If you do not need to read randomly and you have access to the file
  * directly, you should use the BufferedInputStream etc. -classes, as
  * they are MUCH faster than the RandomAccessFile class that is used
  * here.
  */
  public FilePtr(RandomAccessFile fp,
                 Socket sk,
                 InputStream is,
                 BufferedOutputStream os,
                 String fnamp,
                 long fileLength,
                 int bufsize) throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr(RandomAccessFile, Socket, InputStream, BufferedOutputStream, String, long, int)");
////#ifdef TRACE1
//    System.out.println("    fp: " + fp + "\n    sk: " + sk + "\n    is: " + is + "\n    os: " + os + "\n    fnamp: " + fnamp + "\n    fileLength: " + fileLength + "\n    bufsize: " + bufsize);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    this.fp=fp;
    this.sk=sk;
    this.is=is;
    this.os=os;
    this.fnamp=fnamp;
    this.savedLength=fileLength;
    this.BUFSIZE=bufsize;
    data=new byte[BUFSIZE][1];
    inbuf=new byte[BUFSIZE];
    for (int i=0;i<data[0].length;i++) {
      startIdx[i]=-1; // Means: no data at this index.
    }
    if (fp==null) {
      try {
        URL u=new URL(fnamp);
        host=u.getHost();
        port=u.getPort();
        if (port<0) { port=80; } // Default port for http...
      } catch ( MalformedURLException me) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        throw new IOException("Malformed URL '"+fnamp+"'");
      }
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
  }




  /**
  * Reads one (signed) byte.
  * @return One signed 8 bit byte.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached <i>before</i> the
  * byte could be read.
  */
  public byte readByte() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readByte()");
////#endif /* TRACE0 */
    if (startIdx[idx]<0 || fpos<startIdx[idx] || fpos>endIdx[idx]) {
      readToBuffer();
    }
    fpos++;
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return data[(int)(fpos-1-startIdx[idx])][idx];
  }

  /**
  * Reads one <i>unsigned</i> byte.
  * @return One unsigned 8 bit byte as an integer.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached <i>before</i> the
  * byte could be read.
  */
  public int readUnsignedByte() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readUnsignedByte()");
//    Trace.level--;
////#endif /* TRACE0 */
    return ((int)readByte()) & 0xff;
  }


  /**
  * Reads a (signed) short value. This is a two byte read (16 bits) with
  * highest byte first.
  * @return A signed 2 byte (16 bit) value as a short.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached before the
  * 2 bytes could be read completely.
  */
  public short readShort() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readShort()");
//    Trace.level--;
////#endif /* TRACE0 */
    return (short)((readByte()<<8)+readUnsignedByte());
  }

  /**
  * Reads a (signed) integer value. This is a four byte read (32 bits) with
  * highest bytes first.
  * @return A signed 4 byte (32 bit) value as an integer.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached before the
  * 4 bytes could be read completely.
  */
  public int readInt() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readInt()");
//    Trace.level--;
////#endif /* TRACE0 */
    return (((int)readByte())<<24)+
           (((int)readUnsignedByte())<<16)+
           (((int)readUnsignedByte())<<8)+
           (int)readUnsignedByte();
  }

  /**
  * Reads a double value. This is an 8 byte read (64 bits) with highest
  * bytes first.
  * @return A double value.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached before the
  * 8 bytes could be read completely.
  */
  public double readDouble() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readDouble()");
////#endif /* TRACE0 */
    long ldb=(((long)readUnsignedByte())<<56)+
             (((long)readUnsignedByte())<<48)+
             (((long)readUnsignedByte())<<40)+
             (((long)readUnsignedByte())<<32)+
             (((long)readUnsignedByte())<<24)+
             (((long)readUnsignedByte())<<16)+
             (((long)readUnsignedByte())<<8)+
             (long)readUnsignedByte();
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return Double.longBitsToDouble(ldb);
  }

//#ifndef ASTROLOGY
  /**
  * Reads a complete line from the file. The line is seen to end on a
  * newline character ('\n') or on an end of file. The returned String
  * will include the newline character if any.
  * 
  * @return A String containing a complete line. The line may be 0 characters
  * long.
  * @throws IOException if an I/O error occurs.
  * @throws EOFException if the end of file is reached before even one
  * character (byte) could be read.
  */
  public String readLine() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readLine()");
////#endif /* TRACE0 */
    String sout="";
    try {
      char ch;
      while ((ch=(char)readUnsignedByte())!='\n') {
        sout+=ch;
      }
      sout+=ch;
    } catch (EOFException e) {
      if (sout.length()==0) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        throw e;
      }
    }

////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return sout;
  }
//#endif /* ASTROLOGY */

  /**
  * Closes the connection to the file. This includes any Streams as well
  * as the Socket and the RandomAccessFile from the constructor.
  * @throws IOException if an I/O error occurs.
  */
  public void close() throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.close()");
////#endif /* TRACE0 */
    try {
      fnamp="";
      if (fp!=null) { fp.close(); }
      fp=null;
      if (sk!=null) { sk.close(); }
      sk=null;
      is=null;
      os=null;
    } catch (IOException ie) {
      try {
        if (sk!=null) { sk.close(); }
        sk=null;
        is=null;
        os=null;
      } catch (IOException ies) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        throw ies;
      }
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      throw ie;
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
  }

  /**
  * Returns the current position of the file pointer.
  * @return the current position of the file pointer.
  */
  public long getFilePointer() {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.getFilePointer()");
//    Trace.level--;
////#endif /* TRACE0 */
    return fpos;
  }

  /**
  * Returns the length of the file.
  * @return the length of the file in bytes.
  * @throws IOException if an I/O error occurs.
  */
  public long length() throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.length()");
////#endif /* TRACE0 */
    if (fp!=null && savedLength<0) { savedLength=fp.length(); }
    if (fp!=null || savedLength>=0) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
      return savedLength;
    }

    long len=0;
    String s="HEAD "+fnamp+" HTTP/1.1\r\n"+
             "User-Agent: "+useragent+"\r\n"+
             "Host: "+host+":"+port+"\r\n\r\n";
    String sout = "";
    int failures = 0;
    int rc = 0;
    while (true) {
      try {
        URLwrite(os,s);
        sout=URLread(is);
      } catch (IOException ioe) {
        if (++failures>=MAX_FAILURES) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new IOException("(java.net.SocketException) "+ioe.getMessage());
        }
        reconnect();
        continue;
      }
      rc=checkHeader(sout);
      if (rc<0) { // What has happened? Invalid header?
        if (++failures>=MAX_FAILURES) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new IOException("Failed to read a valid / complete header.");
        }
        reconnect();
        continue;
      }
      break;
    }
    int idx=sout.indexOf("Content-Length:");
    if (rc==200 && sout.indexOf("Content-Length:") >= 0) {
      sout=sout.substring(idx+"Content-Length:".length());
      sout=sout.substring(0,sout.indexOf("\n")).trim();
      len=Long.parseLong(sout);
    } else {
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      throw new IOException("Can't determine length of (HTTP-)file '"+fnamp+
                            "'. HTTP error code: "+rc);
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return len;
  }

  /**
  * Positions the file pointer.
  * @param pos the new position in the file. The position is seen zero based.
  */
  public void seek(long pos) {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.seek()");
//    Trace.level--;
////#endif /* TRACE0 */
    fpos=pos;
  }

  /**
  * Repositions the file pointer by skipping some bytes from the current
  * position.
  * @param count How many bytes to skip.
  * @throws EOFException if the new position would be beyond the end of the
  * file.
  * @throws IOException if an I/O error occurs.
  */
  void skipBytes(int count) throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.skipBytes(int)");
////#ifdef TRACE1
//    System.out.println("    count: " + count);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    if (fpos+count>=length()) {
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      throw new EOFException("Filepointer position "+(fpos+count)+" exceeds "+
                             "file length by "+(fpos+count-length()+1)+
                             " byte(s).");
    }
    fpos+=count;
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
  }


// RFC 2068: Response = Status-Line
//                      * ( general-header
//                        | response-header
//                        | entity-header )
//                     CRLF
//                     [ message-body ]
//
// Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
//
// Status-Code should (has to?) be 206
//


  private String URLread(InputStream is) throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.URLread(InputStream)");
////#ifdef TRACE1
//    System.out.println("    is: " + is);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    StringBuffer sret=new StringBuffer("");
    int av = is.read();
    if (av == -1) {
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      throw new IOException("No bytes available.");
    }
    sret.append((char)av);
    while (is.available()>0) {
      sret.append((char)is.read());
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return sret.toString();
  }

  private void URLwrite(BufferedOutputStream os, String s) throws IOException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.URLwrite(BufferedOutputStream, String)");
////#ifdef TRACE1
//    System.out.println("    os: " + os + " \n    s: " + s);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    for(int n=0; n<s.length(); n++) {
      os.write((byte)s.charAt(n));
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    os.flush();
  }

  // Returns the data part of the html response in String s
  private String htmlStrip(String s) {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.htmlStrip(String)");
////#ifdef TRACE1
//    System.out.println("    s: " + s);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    int idx=s.indexOf("\r\n\r\n");
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    if (idx>=0) {
      return s.substring(idx+4);
    }
    return "";
  }

  // Returns the http return code or -1, if not available
  private int checkHeader(String s) {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.checkHeader(String)");
////#ifdef TRACE1
//    System.out.println("    s: " + s);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    try {
      int ix1=s.indexOf(" ");
      int ix2=s.indexOf(" ",ix1+1);
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      if (ix1<0 || ix2<0 || ix1<8 || ix1+4!=ix2) { return -1; }
      return Integer.parseInt(s.substring(ix1+1,ix2));
    } catch (NumberFormatException nf) {
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      return -1;
    }
  }


  // Reads a chunk of data to the buffer data[][idx]
  private void readToBuffer() throws IOException, EOFException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.trace(Trace.level, "FilePtr.readToBuffer()");
////#endif /* TRACE0 */
    // Directly reading a file:
    if (fp!=null) { 
      fp.seek(fpos);
      int cnt=fp.read(inbuf);
// Probably, RandomAccessFile.read(byte[n]) performes n read operations???
      if (cnt==-1) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        throw new EOFException("Filepointer position "+fpos+" exceeds file"+
                               " length by "+(fpos-length()+1)+" byte(s).");
      }
      for(int n=0;n<cnt;n++) {
        data[n][idx]=inbuf[n];
      } 
      startIdx[idx]=fpos;
      endIdx[idx]=fpos+cnt-1;
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      return;
    }

    // Reading via http:
    if (fpos>=length()) {
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      throw new EOFException("Filepointer position "+fpos+" exceeds file "+
                             "length by "+(fpos-length()+1)+" byte(s).");
    }
    String s="GET "+fnamp+" HTTP/1.1\r\n"+
             "User-Agent: "+useragent+"\r\n"+
             "Host: "+host+":"+port+"\r\n"+
             "Range: bytes="+fpos+"-"+
                                Math.min(length()-1,fpos+BUFSIZE-1)+"\r\n\r\n";
    String sout="";
    int slen=0;
    int failures=0;
    while (true) {
      try {
        URLwrite(os,s);
        sout=URLread(is);
      } catch (IOException ioe) {
        if (++failures>=MAX_FAILURES) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new IOException("(java.net.SocketException) "+ioe.getMessage());
        }
        reconnect();
        continue;
      }
      int rc=checkHeader(sout);
      if (rc<0) { // What has happened?
        if (++failures>=MAX_FAILURES) { // Too many failures in a row, abort:
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new IOException("Failed to read successfully from address\n'"+
                                fnamp+"'. The http reply from the server was "+
                                sout.length()+
                                " bytes long and it's content is:\n\n"+sout);
        }
        continue;
      }
      sout=htmlStrip(sout); // keep data part of http response only
      slen=sout.length();
      if ((rc!=200 /* OK */ && rc!=206 /* Partial content */) ||
          slen>BUFSIZE ||
          (slen<BUFSIZE && savedLength>=0 && fpos+slen != savedLength)) {
        if (++failures>=MAX_FAILURES) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new IOException("HTTP read failed with HTTP response "+rc+
                                ". Read "+slen+" bytes, requested "+BUFSIZE+
                                " bytes.");
        }
        continue;
      }
      if (slen==0) { // How is this to happen???
        if (++failures>=MAX_FAILURES) {
////#ifdef TRACE0
//          Trace.level--;
////#endif /* TRACE0 */
          throw new EOFException("Filepointer position "+fpos+" exceeds file "+
                                 "length by "+(fpos-length()+1)+" byte(s).");
        }
        continue;
      }
      break;
    }
    startIdx[idx]=fpos;
    endIdx[idx]=fpos+slen-1;
    for(int n=0;n<slen;n++) {
      data[n][idx]=(byte)sout.charAt(n);
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
  }

  private void reconnect() throws IOException {
////#ifdef TRACE0
//    System.out.println(System.currentTimeMillis()+" FilePtr.reconnect()");
////#endif /* TRACE0 */
System.err.println("reconnecting...");
    sk.close();
    sk=new Socket(host,port);
    sk.setSoTimeout(5000);
    is=sk.getInputStream();
    os=new BufferedOutputStream(sk.getOutputStream());
  }
}
