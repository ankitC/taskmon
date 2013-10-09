package com.example.taskmon;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetReserveActivity extends Activity {

	private int pid;
	private int cSec;
	private long cNsec;
	private int tSec;
	private long tNsec;
	private int prio;

	public static LinkedHashMap<Integer, Observer> pidMap = new LinkedHashMap<Integer, Observer>();
	public static LinkedHashMap<Integer, Double> pidTMap = new LinkedHashMap<Integer, Double>();
	public static float timerPeriod;
	
	public static String debug = "TEAM11";

	private EditText ePid;
	private EditText eCsec;
	private EditText eCnsec;
	private EditText eTsec;
	private EditText eTnsec;
	private EditText ePrio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_reserve);

		System.loadLibrary("reservationFramework");

		ePid = ((EditText) findViewById(R.id.pidInputSet));
		eCsec = ((EditText) findViewById(R.id.inputCSeconds));
		eCnsec = ((EditText) findViewById(R.id.inputCns));
		eTsec = ((EditText) findViewById(R.id.inputTSeconds));
		eTnsec = ((EditText) findViewById(R.id.inputTns));
		ePrio = ((EditText) findViewById(R.id.inputPriority));

		Button setButton = (Button) findViewById(R.id.setButton);

		setButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pid = Integer.parseInt((ePid.getText().toString()));
				cSec = Integer.parseInt(eCsec.getText().toString());
				cNsec = Long.parseLong(eCnsec.getText().toString());
				tSec = Integer.parseInt(eTsec.getText().toString());
				tNsec = Long.parseLong(eTnsec.getText().toString());
				prio = Integer.parseInt(ePrio.getText().toString());

				int retVal = setReserve(pid, cSec, cNsec, tSec, tNsec, prio);
				if (retVal == 0) {
					Toast success = Toast.makeText(getApplicationContext(),
							"Reservation Set on pid:" + pid, Toast.LENGTH_LONG);
					success.show();
					
					double t = ((double)tSec*1000000000) + tNsec;
					Log.w(debug, "T set= "+ t);
					Observer taskObserver =new Observer(pid, t);
					
					pidMap.put(pid, taskObserver);
			
				} else {
					Toast failed = Toast.makeText(getApplicationContext(),
							"Reservation could not be set", Toast.LENGTH_LONG);
					failed.show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_reserve, menu);
		return true;
	}

	private static native int setReserve(int pid, int cSec, long cNsec,
			int tSec, long tNsec, int prio);
	
	public static Double findMin(){
		Collection<Double> min = pidTMap.values();
		Arrays.sort(min.toArray());
		Double[] sorted = (Double[]) min.toArray();
		return sorted[0];
	}

}
