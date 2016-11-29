package my.project.MyCamera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.graphics.drawable.*;
import android.graphics.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import android.view.Display;


public class ViewImage extends Activity {

    public static String base_uri = "/storage/emulated/0/Bass/";

    ArrayList<String[]> lls = null;

    public static String city;

    public static String base_file() { return city + "_map_"; }
    
    public static byte[] getAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int iRead;
        int off;
        while (is.available() > 0) {
            iRead = is.read(buf, 0, buf.length);
            if (iRead > 0) bs.write(buf, 0, iRead);
        }
        return bs.toByteArray();
    }
    
    public static void writeTempImage(String uri) {
	try {	    
	    ZipFile  zipFile =  new  ZipFile (base_uri + city + ".zip");
	    ZipEntry e = zipFile.getEntry(uri)	;
            File f = new File(base_uri + "out.png");
            InputStream is = zipFile.getInputStream(e);
            FileOutputStream fos = new FileOutputStream(f);
            byte[] rbuf = getAllBytesFromStream(is);
            fos.write(rbuf, 0, rbuf.length);
            is.close();
            fos.close();
	    zipFile.close();	    
	} catch (IOException e) {
	    System.out.println("error");
	}

    }	
    
    public void initMapFiles() {
	lls = new ArrayList<String[]>();    
	try {	    
	    ZipFile  zipFile = new ZipFile (base_uri + city + ".zip");
	    Log.d("cam", "before initMap " + base_uri + city + ".zip");
	    Enumeration <?  extends   ZipEntry > entries = zipFile.entries();
	    while (entries.hasMoreElements()){
		ZipEntry  entry = entries.nextElement();
		Geo g = ViewImage.latLonFromFileName(entry.toString());
		String [] tmp = new String[2];
		tmp[0] = ""+g.lat; tmp[1] = ""+g.lon;
		lls.add(tmp);
	    }
	    zipFile.close();
	} catch (IOException e) {
	    System.out.println("error");
	}
	

	Log.d("cam", "map file size "+lls.size());
	
    }

    public String mapFile(double x, double y) {
	String uri = "";
	final double[] dists = new double[lls.size()];
	final Integer[] idxs = getMapsClosestToGeoSorted(x, y);
	Log.d("cam","base_file"+base_file());
	uri = base_file() +
	    lls.get(idxs[0])[0].replace(".","_") + "_" +
	    lls.get(idxs[0])[1].replace(".","_");
	uri = uri + ".png";
	Log.d("cam",""+uri);
	return uri;
    }

    public Integer[] getMapsClosestToGeoSorted(double lat, double lon)
    { 
	// GEO adresine en yakin haritayi bul
	final double[] dists = new double[lls.size()];
	final Integer[] idxs = new Integer[lls.size()];
        for (int i=0;i<lls.size();i++){
	    idxs[i] = i; String[] ll = lls.get(i);
	    // Oklit mesafesini hesapla
	    dists[i]=Math.sqrt(Math.pow((lat-Float.parseFloat(ll[0])),2) +
			       Math.pow((lon-Float.parseFloat(ll[1])),2));
	}
	// Tum uzakliklari sirala, en yakin tabii ki en ustte olacak
	// Siralama teknigi degere gore ama id'ler uzerinden yapiliyor
	// (o sebeple altta id uzerinden degeri aliyoruz) ki boylece
	// dizindeki en yakin ID hangisi hemen anlayabiliyoruz.
	// Ayrica siralama kullanmanin bir faydasi ileride sag,sol,vs.
	// haritasini ararken en yakin haritalardan aramaya
	// baslayabilmek. "En yakin" dizinde sifirinci tabii ki.
	Arrays.sort(idxs, new Comparator<Integer>() {
		@Override public int compare(final Integer o1, final Integer o2) {
		    return Double.compare(dists[o1], dists[o2]);
		}
	    });
	return idxs;
    }
    
    
    public static class Geo {
	public double lat;
	public double lon;
    }

    public static Geo latLonFromFileName(String s) {
	Pattern r = Pattern.compile("(-*\\d+)_(\\d+)_(-*\\d+)_(\\d+)");
	Matcher m = r.matcher(s);
	if (m.find( )) {
	    double lat = Double.parseDouble(m.group(1) + "." + m.group(2));
	    double lon = Double.parseDouble(m.group(3) + "." + m.group(4));
	    Geo g = new Geo();
	    g.lat = lat;
	    g.lon = lon;
	    return g;
	}else {
	    System.out.println("NO MATCH");
	    return null;
	}	
    }

    public double lastMapCenterLat;
    public double lastMapCenterLon;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
	// default
	double lat = 0;
	double lon = 0;
        String uri = "";
        if (extras != null) {
            lat = Double.parseDouble(extras.getString("latitude"));
            lon = Double.parseDouble(extras.getString("longitude"));
            city = extras.getString("city");
	    if (lat==0 && lon==0) {
		lat = 52.511736; // test vals
		lon = 13.375345; // test vals
	    }
            Log.d("cam",""+lat + " " + lon);	    
            Log.d("cam","city "+city);
        }
	initMapFiles();
	uri = mapFile(lat, lon);	
	Log.d("cam",""+uri);	    

	Geo imageCenterGeo = latLonFromFileName(uri);
	lastMapCenterLat = imageCenterGeo.lat;
	lastMapCenterLon = imageCenterGeo.lon;

        TouchImageView image = new TouchImageView(this);
	image.parent = this;	
        image.setMaxHeight(800);
        image.setMaxWidth(800);
	writeTempImage(uri);
	Drawable res2 = new BitmapDrawable(getResources(), base_uri + "/out.png");
	Drawable res3 = res2;
	try {
	    res3 = mark(res2, lat, lon, imageCenterGeo.lat, imageCenterGeo.lon, Color.RED);
	} catch (Exception e) {
	    Log.d("cam", "Error");
	}
        image.setImageDrawable(res3);

        LinearLayout ll = new LinearLayout(this);
        ll.addView(image);
        setContentView(ll);
    }

    
    private Drawable mark(Drawable d, double lat, double lon, double latcen, double loncen, int to)
    {
	// harita icine nokta koy
	Bitmap src = ((BitmapDrawable) d).getBitmap();

	// bu degerler lat,lon ve pixel degerleri arasinda bir gecis
	// sagliyor, deneme-yanilma ile bulundular.
	double SCALEX = 23000;
	double SCALEY = -35000;
	
	int x=100;int y=100;
	if (lat > 0) {
	    double dy = (lat-latcen)*SCALEY;
	    double dx = (lon-loncen)*SCALEX;
	    x = (int)(src.getWidth()/2 + dx);
	    y = (int)(src.getHeight()/2 + dy);
	}
	Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
	bitmap.setPixel(x, y, to);
	bitmap.setPixel(x, y+1, to);bitmap.setPixel(x+1, y, to);
	bitmap.setPixel(x-1, y, to);bitmap.setPixel(x, y-1, to);
	bitmap.setPixel(x-1, y+1, to);bitmap.setPixel(x+1, y-1, to);
	
	return new BitmapDrawable(bitmap);
    }    
    
}
