package my.project.MyCamera;

import android.app.Application;
import java.util.*;
import android.speech.tts.TextToSpeech;

public class MyApp extends Application {

    public ArrayList<String[]> trip = new ArrayList<String[]>();

    public double or0;
    public double or1;
    public double or2;

    public double lat;
    public double lon;
    public double speed;
    public double acc;
    
    public TextToSpeech t1;

    public class ConcurrentTask implements Runnable {
	
	public MyApp parent = null;
	
	public void run() {
	    while (true) {
		try {
		    Thread.sleep(10000);
		    // System.out.println("Woken up");
		    // System.out.println(""+parent.or0 );
		    // System.out.println(""+parent.or1 );
		    // System.out.println(""+parent.or2 );

		    // String toSpeak = "Woken up motherfucka ";
		    // toSpeak += "orientation " + (int)parent.or0;
		    // parent.t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

		} catch (InterruptedException e) {
		    System.out.println("error concur" );
		}
	    }
	}
    }
    
    public Thread runner = null;
        
    public void startTrip() {
	if (runner != null) return;
	ConcurrentTask task = new ConcurrentTask();
	task.parent = this;
	runner = new Thread(task);
	runner.start();
    }
       
    
}
