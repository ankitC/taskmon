package com.example.taskmonv4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DataCollector extends Service{

	/* Service for fetching the data points from the circular buffer to the app. Its a feeder function to your plotter */
	public  int  onStartCommand(Intent intent, int flags, int startID) {


		/* Finding the existing folder list from the sysfs  and making the map based on the folder values. */
		String folderName = "/sys/rtes/tasks/";
		File folder = new File(folderName);
		String[] pids =folder.list();
		Log.w(MainActivity.debug, "PIDs size:"+pids.length);
		double energyReading=0;

		MainActivity.pidMap.clear();
		if(pids != null){
			for (int i = 0; i < pids.length; i++){

				BufferedReader eReader;
				try{
					eReader = new BufferedReader(new FileReader(folderName + pids[i] + "/energy"));
					Log.w(MainActivity.debug, folderName + pids[i] + "/energy");
					energyReading = Double.parseDouble(eReader.readLine());
					eReader.close();
					Log.w(MainActivity.debug,"EnergyReading:"+energyReading );
					MainActivity.pidMap.put(Integer.parseInt(pids[i]), energyReading);
				}catch(Exception e){
					Log.d(MainActivity.debug,"Exception Occured while trying to read T due to "+e.getCause());
				}
			}
		}

		Log.w(MainActivity.debug,"FinishedServiceIteration");
		/* Return a broadcast if a value was read during this callback */
	//	if(flag){
			Intent returnIntent =  new Intent("com.example.taskmonv4");
			LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent);
//		}
		return Service.START_STICKY;
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
}
