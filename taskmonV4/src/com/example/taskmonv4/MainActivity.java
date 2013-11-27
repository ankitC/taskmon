package com.example.taskmonv4;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

	public static ConcurrentHashMap <Integer , Double> pidMap = new ConcurrentHashMap<Integer, Double>() ;
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

		if (!timerStarted) {
			this.startDataCollector(MainActivity.refreshInterval);
			Log.w(debug, "****Starting Service****");
			timerStarted = true;
			Log.w(debug, "****Registering CallbackReceiver*****");
			ResponseReceiver dataPointsReceiver = new ResponseReceiver();
			this.b = dataPointsReceiver;
			LocalBroadcastManager.getInstance(this).registerReceiver(dataPointsReceiver,	new IntentFilter("com.example.taskmonv4"));
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

						if (isEmpty(eCsec) || isEmpty(eCnsec) || isEmpty(ePid)|| isEmpty(ePrio) || isEmpty(eTnsec)|| isEmpty(eTsec)) {
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
						} else {
							Toast failed = Toast.makeText(getApplicationContext(),	"Reservation could not be set, Retval ="+ retVal,	Toast.LENGTH_SHORT);
							failed.show();
						}
					}
				});

		/* Cancel Button Handler */
		findViewById(R.id.cancelReserveButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
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
							Toast failed = Toast.makeText(getApplicationContext(),	"Reservation could not be cleaned.",	Toast.LENGTH_LONG);
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

			int i=1;
			Log.w(debug, "****Received the broadCast*****");
			TableLayout table = (TableLayout)findViewById(R.id.the_table);
			table.removeAllViews();
			
			TableRow headerRow = new TableRow(context);
			TextView headerTv = new TextView(context);
			headerTv.setText("No.\t\t\tPID\t\t\tEnergy(x10^-12 J)");
			headerTv.setGravity(Gravity.LEFT);
			headerRow.addView(headerTv);
			table.addView(headerRow);
			
			Iterator <Integer> pidIterator = pidMap.keySet().iterator();

			while(pidIterator.hasNext()){

				int pidEntry = pidIterator.next();
				TableRow row = new TableRow(context);

				TextView tv = new TextView(context);

				tv.setText(Integer.toString(i) + "\t\t\t\t" +pidEntry +"\t\t\t" + pidMap.get(pidEntry));
				tv.setHighlightColor(Color.RED);
				tv.setTextColor(Color.RED);
				tv.setGravity(Gravity.LEFT);
				row.addView(tv);
				table.addView(row);
				i++;
			}
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
