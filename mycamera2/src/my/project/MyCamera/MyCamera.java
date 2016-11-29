package my.project.MyCamera;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.GpsSatellite;
import android.location.Location;
import android.content.Intent;
import android.util.Log;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MyCamera extends Activity implements SensorEventListener
{
    private CameraPreview camPreview; 
    private FrameLayout mainLayout;
    private int PreviewSizeWidth = 320;
    private int PreviewSizeHeight= 240;    
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private SensorManager mSensorManager;
    public float[] mAccelerometerReading = new float[3];
    public float[] mMagnetometerReading = new float[3];
    public float[] mRotationMatrix = new float[9];
    public float[] mOrientationAngles = new float[3];

    public String gpsInfo = null;
    public double latitude;
    public double longitude;
    LocationManager locationManager = null;

    private PrisLocationListener mLocationListener;

    TextView textView = null;
    EditText textEdit = null;

    TextToSpeech t1;

    public static double round(double d, double precision){
	double p = Math.pow(10,precision);
	return Math.round(d*p) / p;
    }
    
    public static String round(double d){
	String s = ""+d;
	if (s.length() > 4) return s.substring(0,5);
	else return s;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);     
	mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);	
	mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);	
	mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);	

	PrisLocationListener locationListener = new PrisLocationListener();
	locationListener.parent = this;
	HandlerThread t = new HandlerThread("LocationHandlerThread");
	t.start();
	locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, t.getLooper());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
			     WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.main);

	textView = (TextView)findViewById(R.id.txt02);
	textEdit = (EditText)findViewById(R.id.edit);
        
    	SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight);
	camPreview.mCameraActivity = this;
        
        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mainLayout = (FrameLayout) findViewById(R.id.frameLayout1);
        mainLayout.addView(camView, new LayoutParams(PreviewSizeWidth, PreviewSizeHeight));

	t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
		@Override
		public void onInit(int status) {
		    if(status != TextToSpeech.ERROR) {
			t1.setLanguage(Locale.UK);
		    }
		}
	    });

	//textEdit.setText("berlin");
	textEdit.setText("istanbul");
    }

    @Override 
    public boolean onTouchEvent(MotionEvent event) 
    { 
    	return true;
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    System.arraycopy(event.values, 0, mAccelerometerReading,
			     0, mAccelerometerReading.length);
	}
	else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
	    System.arraycopy(event.values, 0, mMagnetometerReading,
			     0, mMagnetometerReading.length);
	}
	else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
	    System.arraycopy(event.values, 0, mOrientationAngles,
			     0, mOrientationAngles.length);
	}
	//double or0 = round(mOrientationAngles[0],2);
	//double or1 = round(mOrientationAngles[1],2);
	//double or2 = round(mOrientationAngles[2],2);
	double or0 = mOrientationAngles[0];
	double or1 = mOrientationAngles[1];
	double or2 = mOrientationAngles[2];
	String direction = "";
	if (or0 > 270 && or0 <= 360 ) direction = "EAST";
	else if (or0 > 180 && or0 <= 270 ) direction = "NORTH";
	else if (or0 > 90 && or0 <= 180 ) direction = "WEST";
	else if (or0 > 0 && or0 <= 90 ) direction = "SOUTH";
	String disp =
	    "Orientation:" + round(or0) + " " + round(or1) + " " + round(or2) + "\n" +
	    "Heading:" + direction + "\n" +
	    "GPS:" + gpsInfo;
	textView.setText(disp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {	
    }
   
    public static class PrisLocationListener implements LocationListener{

	private static final String TAG = PrisLocationListener.class.getSimpleName();
	public MyCamera parent = null;

	@Override
	public void onLocationChanged(Location arg0) {
	    // double lat = round(arg0.getLatitude(),3);
	    // double lon = round(arg0.getLongitude(),3);
	    // double speed = round(arg0.getSpeed(),3);
	    // double acc = round(arg0.getAccuracy(),3);
	    // double alt = round(arg0.getAltitude(),3);

	    double lat = arg0.getLatitude();
	    double lon = arg0.getLongitude();
	    double speed = arg0.getSpeed();
	    double acc = arg0.getAccuracy();
	    double alt = arg0.getAltitude();
	    
	    String s =
		round(lat) + "," + round(lon) + "," +
		round(speed) + "," + round(acc) + "," + round(alt) ;
	    parent.latitude = arg0.getLatitude();
	    parent.longitude = arg0.getLongitude();
	    parent.gpsInfo = s;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
    }

    public void viewImage(View view) {
	//String toSpeak = "Yo";
	//t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);	    
	Intent intent = new Intent(this, ViewImage.class);
	Log.d("cam",""+latitude);
	Log.d("cam",""+longitude);
	intent.putExtra("latitude", ""+latitude);
	intent.putExtra("longitude", ""+longitude);
	intent.putExtra("city", textEdit.getText().toString());
        startActivity(intent);
    }    
}
