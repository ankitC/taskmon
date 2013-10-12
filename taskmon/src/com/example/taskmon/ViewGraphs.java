package com.example.taskmon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
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
	public static int index;
	public static int timeCounter=0 ;
	public static int color[] = {Color.BLACK, Color.BLUE, Color.RED, Color.CYAN, Color.GREEN};
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


				double time = System.currentTimeMillis() ;//- MainActivity.startTime;
				double dataPoint = 0;
				graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);  
				graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);

				//Map<String, ArrayList<String>> a = new LinkedHashMap<String, ArrayList<String>>();
				Iterator<Observer> itr = SetReserveActivity.pidMap.values().iterator();
				while (itr.hasNext()) {
					Observer reservation = itr.next();
					//Entry<Integer, Observer> entry = itr.next();

					//Observer reservation1=( (Entry<Integer, Observer>) itr).getValue();
					//Double t= SetReserveActivity.pidTMap.get(pid);
					
					if (((timeCounter)%(int)(reservation.getT()/1000000000)) == 0)
					{
						Log.w(debug,"Pid "+ reservation.getPid()+" time counter "+timeCounter+" T "+((int)(reservation.getT()/1000000000)));
						String filenameUtil = "/sys/rtes/tasks/" + reservation.getPid() + "/util";
						String filenameOverflow = "/sys/rtes/tasks/" + reservation.getPid() + "/overflow";
						String data = null;
						try 
						{

							BufferedReader brOverflow = new BufferedReader(new FileReader(
									filenameOverflow));

							BufferedReader brUtil = new BufferedReader(new FileReader(
									filenameUtil));
							data = brUtil.readLine();


							// Detecting Overflow
							if ((data == null) || (Integer.parseInt(brOverflow.readLine()) == 1)) 
							{
								System.out.println("Overflow detected with "+brOverflow.readLine());
								dataPoint = 0.0;
							}
							else
							{
								dataPoint = Double.parseDouble(data);

							}
							brOverflow.close();
							brUtil.close();
							double plotPoint = dataPoint / reservation.getT();
							//Log.w(debug, "datapoint:"+dataPoint+ " Cutrrent systime:"+System.currentTimeMillis());
							Log.w(debug, "C:"+dataPoint+"  T:"+reservation.getT()+" \n % plotpoint:" + plotPoint);
							//double time = System.currentTimeMillis() - MainActivity.startTime;
							reservation.getDataSeries().appendData(
									new GraphViewData(time/1000,plotPoint), true, 100);
							
							reservation.getDataSeries().getStyle().color = color[reservation.getColor()];
							
							ViewGraphs.graphView.addSeries(reservation.getDataSeries());
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
				}
				//Log.w(debug, "****Handler call back*****"+(SetReserveActivity.findMin()/1000000));
				handler.postDelayed(this, (long) (1000));
				timeCounter++;
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
