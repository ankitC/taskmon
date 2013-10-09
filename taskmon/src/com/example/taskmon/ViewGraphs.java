package com.example.taskmon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map.Entry;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.LineGraphView;

public class ViewGraphs extends Activity {

	// public static double systime = System.currentTimeMillis();

	public static GraphView graphView;
	//private int pid = 2036;
	//private double t;
	private Runnable mTimer;
	private Handler handler = new Handler();
	//private long period;
	//private GraphViewSeries dataSeries;

	public String debug = "TEAM11";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_graphs);
		graphView = new LineGraphView(this, "Utilization Graphs");
		graphView.setViewPort(0, 10);
		graphView.setScrollable(true);
		graphView.scrollToEnd();
		graphView.setManualYAxis(true);
		graphView.setManualYAxisBounds(1, 0);
		//graphView.setScalable(true);
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		//dataSeries = new GraphViewSeries(new GraphViewData[]{new GraphViewData(0, 0.0d)});
		layout.addView(ViewGraphs.graphView);

		mTimer = new Runnable() {
			@Override
			public void run() {


				double time = System.currentTimeMillis() - MainActivity.startTime;
				for (Entry<Integer, Observer> entry : SetReserveActivity.pidMap.entrySet()) {
					int pid = entry.getKey();
					Observer reservation=entry.getValue();
					Double t= SetReserveActivity.pidTMap.get(pid);

					String filename = "/sys/rtes/tasks/" + pid + "/util";
					String data = null;
					try {
						BufferedReader brn = new BufferedReader(new FileReader(
								filename));
						data = brn.readLine();
						brn.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!(data == null)) {
						double dataPoint = Double.parseDouble(data);
						double plotPoint = dataPoint / reservation.t;
					//	Log.w(debug, "datapoint:"+dataPoint+ " Cutrrent systime:"+System.currentTimeMillis());
						Log.w(debug, "C:"+dataPoint+"  T:"+reservation.t+" plotpoint" + plotPoint);
						//double time = System.currentTimeMillis() - MainActivity.startTime;
						reservation.dataSeries.appendData(
								new GraphViewData(time/1000,plotPoint), true, 100);
						reservation.dataSeries.getStyle().color = Color.RED;
						ViewGraphs.graphView.addSeries(reservation.dataSeries);
					}
				}/*try {
					//Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				handler.postDelayed(this, 5000);
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_graphs, menu);
		return true;
	}

	protected void onResume() {
		super.onResume();


		handler.postDelayed(mTimer, 0);
	}

}
