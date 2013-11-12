package com.example.taskmonv3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
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
	public  int  onStartCommand(Intent intent, int flags, int startID) {


		/* Finding the existing folder list from the sysfs  and making the map based on the folder values. */
		String folderName = "/sys/rtes/tasks/";
		File folder = new File(folderName);
		String[] pids =folder.list();
		boolean flag = false;	
		Set<Integer> currentFolderList = new HashSet<Integer>();
		int pid;
		double t=0;

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
				else{
				/*	Observer o = MainActivity.pidMap.get(checked_pid);
					MainActivity.utilizationGraph.getmRenderer().removeSeriesRenderer(o.getUtilizationRenderer());
					MainActivity.utilizationGraph.getmDataset().removeSeries(o.getUtilizationDataset());
					MainActivity.utilizationGraph.getView(MainActivity.context).repaint();
					
					MainActivity.contextGraph.getmRenderer().removeSeriesRenderer(o.getContextRenderer());
					MainActivity.contextGraph.getmDataset().removeSeries(o.getContextDataset());
					MainActivity.contextGraph.getView(MainActivity.context).repaint();
					*/
					MainActivity.pidMap.remove(checked_pid);
				}
			}

			Iterator<Observer> observerItr =  MainActivity.pidMap.values().iterator();
			//	Log.w(SetReserveActivity.debug,"In Service\n Length = "+SetReserveActivity.pidMap.size());
			String utilData = null;
			String ctxData = null;
			String xData = null;
			String yData = null;



			/* Iterator to go through the list of reservations made by the app and find their values */
			while (observerItr.hasNext()) {	
				Observer reservation = observerItr.next();
				String filenameUtil = "/sys/rtes/tasks/" + reservation.getPid() + "/util";
				reservation.getUtilizationX().clear();
				reservation.getUtilizationY().clear();
				Log.w(MainActivity.debug,"Collecting Data Points");
				while(true){
					try {

						BufferedReader brUtil = new BufferedReader(new FileReader(filenameUtil));
						utilData = brUtil.readLine();	
						Log.w(MainActivity.debug,"Util:"+utilData);
						if(utilData == null){
							brUtil.close();
							break;
						}

						String splitted[] = utilData.split("\\s");
						yData = splitted[0];
						xData = splitted[1];

						double x = Double.parseDouble(xData);
						x = x/1E9;
						double y = Double.parseDouble(yData);
						y = y/reservation.getT();
						x = Double.parseDouble(new DecimalFormat("##.###")	.format((double) x ));
						y = Double.parseDouble(new DecimalFormat("##.##")	.format((double) y ));
						Log.w(MainActivity.debug,"Util X:"+x+"   Y:"+y);
						reservation.getUtilizationX().add(x);
						reservation.getUtilizationY().add(y);

						flag = true;
						brUtil.close();
					} catch (Exception e) {
						e.getCause();
						break;
					}
				}

				
				String filenameCtx = "/sys/rtes/tasks/" + reservation.getPid() + "/ctx";

				reservation.getContextX().clear();
				reservation.getContextY().clear();

				while(true){
					try{
						BufferedReader ctxUtil = new BufferedReader(new FileReader(filenameCtx));
						ctxData =ctxUtil.readLine();
						flag = true;
						if(ctxData == null){
							ctxUtil.close();
							break;
						}

						String splitted[] = ctxData.split("\\s");
						xData = splitted[0];
						yData = splitted[1];
						//Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t Time:"+time+" Status:"+status);
						Double x = Double.parseDouble(xData);
						x = x/1e9;
//						x = Double.parseDouble(new DecimalFormat("##.###")	.format((double) x ));
						x = Double.parseDouble(new DecimalFormat("##.######")	.format((double) x ));
						reservation.getContextX().add(x);
						Log.w(MainActivity.debug,"Context X:"+x+"   Y:"+yData);
						if(yData.equalsIgnoreCase("in")){
							reservation.getContextY().add((double) 1);
							Log.w(MainActivity.debug,"inside in");
						}
						else
							reservation.getContextY().add((double) 0);
						ctxUtil.close();
					}catch(Exception e){
						e.getCause();
						break;
					}
				}

			}/* While ends */
		}

		Log.w(MainActivity.debug,"FinishedServiceIteration");
		/* Return a broadcast if a value was read during this callback */
		if(flag){
			Intent returnIntent =  new Intent("com.example.taskmonv3");
			LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent);
		}
		return Service.START_STICKY;
	}



	public IBinder onBind(Intent intent) {
		return null;
	}
}
