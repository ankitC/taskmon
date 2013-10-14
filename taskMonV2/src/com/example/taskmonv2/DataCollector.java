package com.example.taskmonv2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DataCollector extends Service{
/* Service for fetching the data points from the circular buffer to the app. Its a feeder function to your plotter */

	@Override
	public  int  onStartCommand(Intent intent, int flags, int startID) {

		Iterator<Observer> observerItr =  MainActivity.pidMap.values().iterator();
		//	Log.w(SetReserveActivity.debug,"In Service\n Length = "+SetReserveActivity.pidMap.size());
		Log.w(MainActivity.debug,"In Service");
		boolean flag = false;	
		
		/* Iterator to go through the list of reservations made by the app and find their values */
		while (observerItr.hasNext()) {	
			Observer reservation = observerItr.next();
			//	Log.w(SetReserveActivity.debug,"Pid "+ reservation.getPid()+" time counter "+ViewGraphs.timeCounter+" T "+((int)(reservation.getT()/1000000000)));
			String filenameUtil = "/sys/rtes/tasks/" + reservation.getPid() + "/util";
			String data = null;
			reservation.getDataPoints().clear();
			while(true){
				try {

					BufferedReader brUtil = new BufferedReader(new FileReader(filenameUtil));
					data = brUtil.readLine();	
					Log.w(MainActivity.debug,"InService: Data="+data);

					if(data == null)
						break;

					reservation.getDataPoints().add(new Double(Double.parseDouble(data)));
					flag = true;
	
					brUtil.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.getCause();
				}
			}
		}

		Log.w(MainActivity.debug,"FinishedServiceIteration");
		/* Return a broadcast if a value was read during this callback */
		if(flag){
			Intent returnIntent =  new Intent("com.example.taskmonv2");
			LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent);
		}
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
