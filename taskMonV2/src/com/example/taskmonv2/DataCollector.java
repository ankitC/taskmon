package com.example.taskmonv2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DataCollector extends Service{
	/* Service for fetching the data points from the circular buffer to the app. Its a feeder function to your plotter */

	@Override
	public  int  onStartCommand(Intent intent, int flags, int startID) {

		/* 
		 * Finding the existing folder list from the sysfs
		 * and making the map based on the folder values.
		 */
		String folderName = "/sys/rtes/tasks/";
		File folder = new File(folderName);
		String[] pids =folder.list();
		boolean flag = false;	
		Set<Integer> currentFolderList = new HashSet<Integer>();
		int pid;
		double t=0;

		synchronized (MainActivity.pidMap) {

			if(pids != null){
				Log.w(MainActivity.debug,"Folders Length = "+pids.length);
				for(int i =0; i<pids.length;i++){

					pid=Integer.parseInt(pids[i]);
					currentFolderList.add(pid);

					String tFile = folderName + pid+ "/tval";
					BufferedReader tReader;
					try{
						tReader = new BufferedReader(new FileReader(tFile));
						t = Double.parseDouble(tReader.readLine());
						tReader.close();
					}catch(Exception e){
						Log.d(MainActivity.debug,"Exception Occured while trying to read T due to "+e.getCause());
					}

					if(!MainActivity.pidMap.containsKey(pid)){
						/*  Do the math for T */
						Observer o = new Observer(pid, t);
						MainActivity.pidMap.put(pid, o);
						Log.w(MainActivity.debug,"Key Found");
					}
					else
						MainActivity.pidMap.get(pid).setT(t);
				}

				/*
				 * When a task reservation is cancelled the folders will no longer be
				 * present in the sysfs. In that case, we identify those folders and 
				 * remove them from the Map.
				 */  
				Set<Integer> keysFromMap = MainActivity.pidMap.keySet();

				final Iterator <Integer>mapIterator = keysFromMap.iterator();
				/* If difference was found, remove them from the Map */		
				while(mapIterator.hasNext()){
					int checked_pid = mapIterator.next();
					if(currentFolderList.contains(checked_pid))
						continue;
					else
						MainActivity.pidMap.remove(checked_pid);
				}

				Iterator<Observer> observerItr =  MainActivity.pidMap.values().iterator();
				//	Log.w(SetReserveActivity.debug,"In Service\n Length = "+SetReserveActivity.pidMap.size());

				/* Iterator to go through the list of reservations made by the app and find their values */
				while (observerItr.hasNext()) {	
					Observer reservation = observerItr.next();
					String filenameUtil = "/sys/rtes/tasks/" + reservation.getPid() + "/util";
					String data = null;
					reservation.getDataPoints().clear();
					while(true){
						try {

							BufferedReader brUtil = new BufferedReader(new FileReader(filenameUtil));
							data = brUtil.readLine();	

							if(data == null)
								break;

							reservation.getDataPoints().add(new Double(Double.parseDouble(data)));
							flag = true;
							Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t C:"+ Double.parseDouble(data));
							brUtil.close();
						} catch (Exception e) {
							e.getCause();
						}
					}

					String filenameCtx = "/sys/rtes/tasks/" + reservation.getPid() + "/ctx";
					String status = null;
					String time = null;
					String ctxLine = null;
					
					reservation.getContextPoints().clear();
					reservation.getContextState().clear();

					while(true){
						try{
							BufferedReader ctxUtil = new BufferedReader(new FileReader(filenameCtx));
							ctxLine =ctxUtil.readLine();
							flag = true;
							if(ctxLine == null)
								break;
					
							String splitted[] = ctxLine.split("\\s");
							time = splitted[0];
							status = splitted[1];
							//Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t Time:"+time+" Status:"+status);
							reservation.getContextPoints().add(new Double(Double.parseDouble(time)));
							if(status.equalsIgnoreCase("in")){
								reservation.getContextState().add(new Integer(1));
							}
							else
								reservation.getContextState().add(new Integer(0));

							//reservation.get
						}catch(Exception e){
							e.getCause();
					}
				}

			}/* While ends */
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

public IBinder onBind(Intent intent) {
	return null;
}
}
