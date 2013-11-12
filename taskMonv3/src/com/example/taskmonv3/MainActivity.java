package com.example.taskmonv3;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.achartengine.model.TimeSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int pid;
	private int cSec;
	private long cNsec;
	private int tSec;
	private long tNsec;
	private int prio;

	private EditText ePid;
	private EditText eCsec;
	private EditText eCnsec;
	private EditText eTsec;
	private EditText eTnsec;
	private EditText ePrio;

	public static LineGraph utilizationGraph;
	public static LineGraph contextGraph;

	private TimeSeries tempDataset;
	private XYSeriesRenderer tempRenderer = new XYSeriesRenderer();

	public static ConcurrentHashMap<Integer, Observer> pidMap = new ConcurrentHashMap<Integer, Observer>();
	public static int color[] = { Color.RED, Color.BLUE,	Color.GREEN, Color.BLACK, Color.GRAY};

	private BroadcastReceiver b;
	private AlarmManager am;
	private PendingIntent pi;

	public static double startTime;
	public static String debug = "TEAM11";
	public static long refreshInterval = 1000;

	public static boolean plotContext = true;
	public boolean timerStarted = false;

	public static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		System.loadLibrary("reservationFramework");

		context = getApplicationContext();
		utilizationGraph = new LineGraph("Utilization", context);
		contextGraph = new LineGraph("ContextSwitch", context);
		
		utilizationGraph.getmRenderer().setZoomEnabled(true, false);
		contextGraph.getmRenderer().setZoomEnabled(true, false);
		contextGraph.getmRenderer().setZoomInLimitX(0.0001);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.utilizationLinerLayout);
		layout.addView(MainActivity.utilizationGraph.getView(getApplicationContext()));

		LinearLayout contextLayout = (LinearLayout) findViewById(R.id.ContextLinerLayout);
		contextLayout.addView(MainActivity.contextGraph.getView(getApplicationContext()));

		if (!timerStarted) {
			this.startDataCollector(MainActivity.refreshInterval);
			Log.w(debug, "****Starting Service****");
			timerStarted = true;
			Log.w(debug, "****Registering CallbackReceiver*****");
			ResponseReceiver dataPointsReceiver = new ResponseReceiver();
			this.b = dataPointsReceiver;
			LocalBroadcastManager.getInstance(this).registerReceiver(dataPointsReceiver,	new IntentFilter("com.example.taskmonv3"));
			Log.w(debug, "****Finished registering receiver*****");
		}

		/* Setting Button Methods */
		findViewById(R.id.setReserveButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {

						ePid = ((EditText) findViewById(R.id.pid));
						eCsec = ((EditText) findViewById(R.id.cSec));
						eCnsec = ((EditText) findViewById(R.id.cNs));
						eTsec = ((EditText) findViewById(R.id.tSec));
						eTnsec = ((EditText) findViewById(R.id.tNs));
						ePrio = ((EditText) findViewById(R.id.prio));

						if (isEmpty(eCsec) || isEmpty(eCnsec) || isEmpty(ePid)
								|| isEmpty(ePrio) || isEmpty(eTnsec)
								|| isEmpty(eTsec)) {
							Toast.makeText(getApplicationContext(),	"Please enter all the fields",	Toast.LENGTH_SHORT).show();
							return;
						}

						pid = Integer.parseInt((ePid.getText().toString()));
						cSec = Integer.parseInt(eCsec.getText().toString());
						cNsec = Long.parseLong(eCnsec.getText().toString());
						tSec = Integer.parseInt(eTsec.getText().toString());
						tNsec = Long.parseLong(eTnsec.getText().toString());
						prio = Integer.parseInt(ePrio.getText().toString());

						int retVal = setReserve(pid, cSec, cNsec, tSec, tNsec,prio);
						if (retVal == 0) {
							Toast success = Toast.makeText(	getApplicationContext(),"Reservation Set on pid:" + pid,	Toast.LENGTH_SHORT);
							success.show();

							double t = ((double) tSec * 1000000000) + tNsec;
							Log.w(debug, "T set= " + t);
							if (!pidMap.containsKey(pid)) {
								Observer taskObserver = new Observer(pid, t);
								pidMap.put(pid, taskObserver);
							}
						} else {
							Toast failed = Toast.makeText(
									getApplicationContext(),	"Reservation could not be set, Retval ="+ retVal,	Toast.LENGTH_SHORT);
							failed.show();
						}
					}
				});

		/* Cancel Button Handler */
		findViewById(R.id.cancelReserveButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
			/*			Toast failed1 = Toast.makeText(
								getApplicationContext(),	"Calling Cancel Reserve =",	Toast.LENGTH_SHORT);
						failed1.show();*/
						ePid = ((EditText) findViewById(R.id.pid));

						if (isEmpty(ePid)) {
							Toast.makeText(getApplicationContext(), "Please enter all the pid.",	Toast.LENGTH_SHORT).show();
							return;
						}
						pid = Integer.parseInt((ePid.getText().toString()));
						if (MainActivity.pidMap.containsKey(pid)) {
							MainActivity.pidMap.remove(pid);
						}
						int retVal = cancelReserve(pid);
						if (retVal == 0) {
							Toast success = Toast.makeText(
									getApplicationContext(),	"Reservation Cancelled on pid:" + pid,	Toast.LENGTH_LONG);
							success.show();
						} else {
							Toast failed = Toast.makeText(
									getApplicationContext(),
									"Reservation could not be cleaned.",
									Toast.LENGTH_LONG);
							failed.show();
						}
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver {
		public ResponseReceiver() {
		}

		// Called when the BroadcastReceiver gets an Intent it's registered to
		// receive
		public void onReceive(Context context, Intent intent) {

			Log.w(debug, "****Received the broadCast*****");

			Iterator<Observer> itr = MainActivity.pidMap.values().iterator();
			// Log.w(debug, "****Going into while*****");


			while (itr.hasNext()) {
				Observer reservation = itr.next();
				/* Adding the series for the first time */
				if(!reservation.seriesAdded){
					MainActivity.utilizationGraph.getmDataset().addSeries(reservation.getUtilizationDataset());
					MainActivity.utilizationGraph.getmRenderer().addSeriesRenderer(reservation.getUtilizationRenderer());
					utilizationGraph.getView(MainActivity.context).repaint();

					MainActivity.contextGraph.getmDataset().addSeries(reservation.getContextDataset());
					MainActivity.contextGraph.getmRenderer().addSeriesRenderer(reservation.getContextRenderer());
					contextGraph.getView(MainActivity.context).repaint();
					reservation.seriesAdded = true;
				}
				/*  Find datapoints for plotting */
				for (int i = 0; i < reservation.getUtilizationX().size(); i++) {		
					//Log.w(debug, "PlotPoint:" + plotPoint + "    plotTime:"+ plotTime);

					reservation.getUtilizationDataset().add(reservation.getUtilizationX().get(i),reservation.getUtilizationY().get(i) );
					utilizationGraph.getView(MainActivity.context).repaint();
				}

				if(plotContext){

					//	reservation.getContextSeries().getStyle().color = color[reservation .getColor()];
					Log.w(MainActivity.debug,"Plotting Context now with "+ reservation.getContextX().size()+ " points");
					for(int j=0;  j<reservation.getContextX().size();  j++){

						//	Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t In Plotter Time:"+contextPoint);

						if(reservation.getContextY().get(j) ==0 ){
							
							reservation.getContextDataset().add(reservation.getContextX().get(j),1);
							//contextGraph.getView(MainActivity.context).repaint();
							reservation.getContextDataset().add(reservation.getContextX().get(j) + 0.00001, 0);
							contextGraph.getView(MainActivity.context).repaint();
							//reservation.setPrevContext(0); 
							Log.w(MainActivity.debug,"Plotting 0");
						}
						else{
						    reservation.getContextDataset().add(reservation.getContextX().get(j),0);
							//contextGraph.getView(MainActivity.context).repaint();
							reservation.getContextDataset().add(reservation.getContextX().get(j) + 0.00001, 1);
							contextGraph.getView(MainActivity.context).repaint();
							Log.w(MainActivity.debug,"Plotting 1");
							//reservation.setPrevContext(1);
						}
					}
				}
			}/* End of while itr.hasNext()*/
		}
	}

	/* Methods for service that polls the sysfs for data */

	/* Starting the service repetitively */
	private void startDataCollector(long refreshInterval) {
		Log.w(debug, "****StartingDataCollector*****");
		this.am = (AlarmManager) getApplicationContext().getSystemService(
				Context.ALARM_SERVICE);
		Intent i = new Intent(getApplicationContext(), DataCollector.class);
		this.pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
		this.am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), refreshInterval, this.pi);
	}

	/* De-register the listener and stop the alarm for server */
	protected void onDestroy() {
		super.onDestroy();
		Log.i(debug, "On Destroy .....");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(this.b);
		utilizationGraph.getmRenderer().removeAllRenderers();
		contextGraph.getmRenderer().removeAllRenderers();
		this.am.cancel(pi);
		Log.i(debug, "Finished onDestroy amd unregistering");
	}

	private boolean isEmpty(EditText etText) {
		return etText.getText().toString().trim().length() == 0;
	}

	/* Set and Cancel Reserve Native Methods for JNI */
	private static native int setReserve(int pid, int cSec, long cNsec,
			int tSec, long tNsec, int prio);

	private static native int cancelReserve(int pid);

}
