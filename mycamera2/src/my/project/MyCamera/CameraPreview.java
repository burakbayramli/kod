package my.project.MyCamera;

import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.File;
import android.view.SurfaceHolder;
import android.os.Environment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.YuvImage;
import android.graphics.Rect;
import android.graphics.ImageFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;
import java.util.ArrayList;

public class CameraPreview implements SurfaceHolder.Callback,
				      Camera.PreviewCallback				      
{
    private Camera mCamera = null;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private String NowPictureFileName;
    private Boolean TakePicture = false;
    private ArrayList<byte[]> images = null;
    private ArrayList<String> orientations = null;
    private ArrayList<String> gps = null;
    public MyCamera mCameraActivity = null;
	
    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight)
    {
	PreviewSizeWidth = PreviewlayoutWidth;
    	PreviewSizeHeight = PreviewlayoutHeight;
    }
    
    private int frameCounter = 0;
    
    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) 
    {
	// onizleme sirasinda her kamera goruntusu (tek imaj olarak) bu
	// metota gecilir.

	// sayim yap, ki bazi goruntuleri almadan atabilelim
	frameCounter++;
	if (frameCounter < 10) return; // goruntuyu islemeden hemen geri don
	frameCounter = 0;
	
	// hafizaya ekle
	images.add(arg0);
	orientations.add(Float.toString(mCameraActivity.mOrientationAngles[0]) + " " +
			 Float.toString(mCameraActivity.mOrientationAngles[1]) + " " +
			 Float.toString(mCameraActivity.mOrientationAngles[2]));	
	if (gps != null) gps.add(mCameraActivity.gpsInfo);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) 
    {
	Parameters parameters;
		
	parameters = mCamera.getParameters();
	// Set the camera preview size
	parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
	// Set the take picture size, you can set the large size of the camera supported.
	parameters.setPictureSize(PreviewSizeWidth, PreviewSizeHeight);
	parameters.setPreviewFormat(ImageFormat.NV21);
				
	mCamera.setParameters(parameters);

	images = new ArrayList();
	orientations = new ArrayList<String>();
	gps = new ArrayList<String>();
	
	mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) 
    {
	mCamera = Camera.open();
	try {
	    // If did not set the SurfaceHolder, the preview area will be black.
	    mCamera.setPreviewDisplay(arg0);
	    mCamera.setPreviewCallback(this);
	} 
	catch (IOException e){
	    mCamera.release();
	    mCamera = null;
	}
    }

    
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) 
    {
	// program kapatilinca hafizada ne varsa diske yazilir.
	
	Log.d("cam","images size "+ Integer.toString(images.size()));
	
    	mCamera.setPreviewCallback(null);
	mCamera.stopPreview();
	mCamera.release();
	mCamera = null;
	
	final File path = new File("/storage/emulated/0/Bass");
	Log.d("cam","path "+ path);
		
	File fileo = new File(path, "orientations.txt");
	try
	    {
		fileo.createNewFile();
		FileOutputStream fOut = new FileOutputStream(fileo);
		OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
		for (String xx : orientations) {
		    myOutWriter.append(xx);
		    myOutWriter.append("\n");
		}
		myOutWriter.close();
		fOut.flush();
		fOut.close();
	    }
	catch (IOException e)
	    {
		Log.e("Exception", "File write failed: " + e.toString());
	    } 

	File fileg = new File(path, "gps.txt");
	try
	    {
		fileg.createNewFile();
		FileOutputStream fOut = new FileOutputStream(fileg);
		OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
		for (String xx : gps) {
		    myOutWriter.append(xx);
		    myOutWriter.append("\n");
		}
		myOutWriter.close();
		fOut.flush();
		fOut.close();
	    }
	catch (IOException e)
	    {
		Log.e("Exception", "File write failed: " + e.toString());
	    } 

	File filec = new File(path, "cam.txt");
	File files = new File(path, "sizes.txt");
	try
	    {
		filec.createNewFile();
		files.createNewFile();
		FileOutputStream fOut = new FileOutputStream(filec);
		FileOutputStream fOut2 = new FileOutputStream(files);
		OutputStreamWriter myOut2Writer = new OutputStreamWriter(fOut2);
		for (byte[] xx : images) {
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    YuvImage yuv = new YuvImage(xx, ImageFormat.NV21, PreviewSizeWidth, PreviewSizeHeight, null);
		    yuv.compressToJpeg(new Rect(0, 0, PreviewSizeWidth, PreviewSizeHeight), 50, out);
		    byte[] bytes = out.toByteArray();
		    fOut.write(bytes);
		    myOut2Writer.append("" + bytes.length);
		    myOut2Writer.append("\n");		    
		    Log.d("cam","single jpg size "+ bytes.length);
		}
		myOut2Writer.close();
		fOut.flush();
		fOut.close();
		fOut2.flush();
		fOut2.close();
	    }
	catch (IOException e)
	    {
		Log.e("Exception", "File write failed: " + e.toString());
	    } 
    }
}
