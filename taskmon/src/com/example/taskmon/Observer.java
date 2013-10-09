package com.example.taskmon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewData;
import android.os.Handler;


public class Observer implements Runnable {

	private int pid;
	public double t;
	private long period;
	public GraphViewSeries dataSeries;

	public Observer(int pid, double t){
		this.pid = pid;
		this.t = t;
		period = (long) (t/1000000);
		dataSeries = new GraphViewSeries(new GraphViewData[]{new GraphViewData(0, 0.0d)});
	}

	public void run(){

		//mTimer = new Runnable() {
		//@Override
		//public void run() {
		//while(true){
			String filename = "/sys/rtes/tasks/"+pid+"/util";
			String data = null;
			try{
				BufferedReader brn = new BufferedReader(new FileReader(filename));
				data = brn.readLine();

			}catch(Exception e){
				e.printStackTrace();
			}
			if(!(data == null)){
				double dataPoint = Double.parseDouble(data);
				dataSeries.appendData(new GraphViewData(System.currentTimeMillis()
						,dataPoint), true, 10);
				ViewGraphs.graphView.addSeries(dataSeries);
			//	handler.postDelayed(this, period);
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
		//}

		//};
		//handler.postDelayed(mTimer, 1000);


	}
	
	
	

}