package com.example.taskmonv2;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.LineGraphView;

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

	private BroadcastReceiver b;
	private AlarmManager am;
	private PendingIntent pi;

	public static double startTime;
	public static String debug = "TEAM11";
	public static long refreshInterval = 1000;

	public static LinkedHashMap<Integer, Observer> pidMap = new LinkedHashMap<Integer, Observer>();
	public static GraphView graphView;
	public static GraphView contextView;
	public static int color[] = { Color.GRAY, Color.RED, Color.BLUE,	Color.GREEN, Color.BLACK, };

	public boolean timerStarted = false;
	public boolean initialized = false;
	public boolean referenceTimeSet = false;
	public boolean plotContext = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.loadLibrary("reservationFramework");

		if (!referenceTimeSet) {
			startTime = System.currentTimeMillis();
			referenceTimeSet = true;
		}

		graphView = new LineGraphView(this, "Utilization Graphs");
		graphView.setViewPort(0, 10);
		graphView.setScrollable(true);
		//	graphView.scrollToEnd();
		graphView.setManualYAxis(true);
		graphView.setManualYAxisBounds(1, 0);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setNumHorizontalLabels(6);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(40);
		//graphView.getGraphViewStyle().setTextSize(10);

		contextView = new LineGraphView(this, "Context Switch Graphs");
		contextView.setViewPort(0, 1);
		contextView.setScrollable(true);
		//	contextView.scrollToEnd();
		contextView.setManualYAxis(true);
		contextView.setManualYAxisBounds(1, 0);
		contextView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		contextView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		contextView.getGraphViewStyle().setNumHorizontalLabels(2);
		contextView.getGraphViewStyle().setVerticalLabelsWidth(2);


		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(MainActivity.graphView);

		LinearLayout contextLayout = (LinearLayout) findViewById(R.id.contextgraph);
		contextLayout.addView(MainActivity.contextView);

		/* Starting the timer */
		if(!timerStarted){
			this.startDataCollector(MainActivity.refreshInterval);
			Log.w(debug, "****Starting Service****");
			timerStarted=true;
			Log.w(debug, "****Registering CallbackReceiver*****");
			ResponseReceiver dataPointsReceiver = new ResponseReceiver();
			this.b = dataPointsReceiver;
			LocalBroadcastManager.getInstance(this).registerReceiver(dataPointsReceiver, new IntentFilter("com.example.taskmonv2"));
			Log.w(debug, "****Finished registering receiver*****");
		}
		initialized= true;

		/* The Set button Handler */
		findViewById(R.id.setReserveButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						boolean invalid = false;
						ePid = ((EditText) findViewById(R.id.pid));
						eCsec = ((EditText) findViewById(R.id.cSec));
						eCnsec = ((EditText) findViewById(R.id.cNs));
						eTsec = ((EditText) findViewById(R.id.tSec));
						eTnsec = ((EditText) findViewById(R.id.tNs));
						ePrio = ((EditText) findViewById(R.id.prio));

						if(isEmpty(eCsec)|| isEmpty(eCnsec)||isEmpty(ePid)||isEmpty(ePrio)||isEmpty(eTnsec)||isEmpty(eTsec))
						{
							Toast.makeText(getApplicationContext(), "Please enter all the fields", Toast.LENGTH_SHORT).show();
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
							Toast success = Toast.makeText(
									getApplicationContext(),	"Reservation Set on pid:" + pid, Toast.LENGTH_SHORT);
							success.show();

							double t = ((double) tSec * 1000000000) + tNsec;
							Log.w(debug, "T set= " + t);
							if (!pidMap.containsKey(pid)) {
								Observer taskObserver = new Observer(pid, t);
								pidMap.put(pid, taskObserver);
							}
						} else {
							Toast failed = Toast.makeText(getApplicationContext(),"Reservation could not be set",	Toast.LENGTH_SHORT);
							failed.show();
						}
					}
				});
		/* The Cancel Button Handler */
		findViewById(R.id.cancelReserveButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {

						ePid = ((EditText) findViewById(R.id.pid));

						if(isEmpty(ePid)){
							Toast.makeText(getApplicationContext(), "Please enter all the pid.", Toast.LENGTH_SHORT).show();

							return;
						}
						pid = Integer.parseInt((ePid.getText().toString()));
						if (MainActivity.pidMap.containsKey(pid)) {
							MainActivity.pidMap.remove(pid);
						}
						int retVal = cancelReserve(pid);
						if (retVal == 0) {
							Toast success = Toast.makeText(getApplicationContext(),"Reservation Cancelled on pid:" + pid,Toast.LENGTH_LONG);
							success.show();
						} else {
							Toast failed = Toast.makeText(getApplicationContext(),"Reservation could not be cleaned.",Toast.LENGTH_LONG);
							failed.show();
						}
					}

				});
	}

	private boolean isEmpty(EditText etText) {
		return etText.getText().toString().trim().length() == 0;
	}


	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver {
		public ResponseReceiver() {
		}

		// Called when the BroadcastReceiver gets an Intent it's registered to
		// receive
		public void onReceive(Context context, Intent intent) {
			Log.w(debug, "****Received the broadCast*****");

			double time = System.currentTimeMillis() - MainActivity.startTime;
			Iterator<Observer> itr = MainActivity.pidMap.values().iterator();
			// Log.w(debug, "****Going into while*****");
			synchronized (MainActivity.pidMap) {

				while (itr.hasNext()) {
					Observer reservation = itr.next();
					reservation.getDataSeries().getStyle().color = color[reservation .getColor()];
					/*  Find datapoints for plotting */
					for (int i = 0; i < reservation.getDataPoints().size(); i++) {
						double plotPoint = (double) reservation.getDataPoints().get(i) / reservation.getT();

						double plotTime = time/1000 + (double) (MainActivity.refreshInterval * i)/ reservation.getDataPoints().size();
						plotTime = Double.parseDouble(new DecimalFormat("##.##")	.format((double) plotTime ));

						Log.w(debug, "PlotPoint:" + plotPoint + "    plotTime:"+ plotTime);
						reservation.getDataSeries().appendData(new GraphViewData(plotTime, plotPoint), true, 1000);
					}
					MainActivity.graphView.addSeries(reservation.getDataSeries());

					if(plotContext){
						reservation.getContextSeries().getStyle().color = color[reservation .getColor()];
						for(int j=0;  j<reservation.getContextPoints().size();  j++){
							double contextState = reservation.getContextState().get(j);
							double contextPoint = reservation.getContextPoints().get(j);
							//	Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t In Plotter Time:"+contextPoint);
							contextPoint = contextPoint/(1000000);
							//	contextPoint = contextPoint - MainActivity.startTime;
							Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\tSystime="+System.currentTimeMillis());
							contextPoint = contextPoint/10000;
							contextPoint=Double.parseDouble(new DecimalFormat("##.##").format((double)contextPoint));
							Log.w(MainActivity.debug,"Pid "+ reservation.getPid()+"\t In Plotter Time:"+contextPoint+" Status:"+contextState);
							if(contextState ==0 ){
								reservation.getContextSeries().appendData(new GraphViewData(contextPoint, reservation.getPrevContext()), true, 1000);
								reservation.getContextSeries().appendData(new GraphViewData(contextPoint, 0), true, 1000);
								reservation.setPrevContext(0);
							}
							else{
								reservation.getContextSeries().appendData(new GraphViewData(contextPoint, reservation.getPrevContext()), true, 1000);
								reservation.getContextSeries().appendData(new GraphViewData(contextPoint, 1), true, 1000);
								reservation.setPrevContext(1);
							}
						}
						MainActivity.contextView.addSeries(reservation.getContextSeries());
					}
				}/* End of while itr.hasNext()*/
			}
		}
	}
	/* Starting the service repetitively */
	private void startDataCollector(long refreshInterval){
		Log.w(debug, "****StartingDataCollector*****");
		this.am=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(getApplicationContext(), DataCollector.class);
		this.pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
		this.am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), refreshInterval ,this. pi); // Millisec * Second * Minute
	}

	/* Set and Cancel Reserve Native Methods for JNI */
	private static native int setReserve(int pid, int cSec, long cNsec,
			int tSec, long tNsec, int prio);

	private static native int cancelReserve(int pid);

	/* De-register the listener and stop the alarm for server*/
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(debug, "On Destroy .....");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(this.b);
		this.am.cancel(pi);
		Log.i(debug, "Finished onDestroy amd unregistering");
	}
}
