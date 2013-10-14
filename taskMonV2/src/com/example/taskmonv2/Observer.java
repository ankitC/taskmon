package com.example.taskmonv2;

import java.util.ArrayList;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

/* Data Structure for keeping a record of process under observation */
public class Observer {

	private int pid;
	private double t;
	private GraphViewSeries dataSeries;
	private ArrayList<Double> dataPoints;
	private int color;

	public static int colorIndex = 0;
	public Observer(int pid, double t){
		this.pid = pid;
		this.t = t;
		dataSeries = new GraphViewSeries(new GraphViewData[]{new GraphViewData(0, 0.0d)});
		dataPoints= new ArrayList<Double>();
		color = (colorIndex++%5);
	}
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public GraphViewSeries getDataSeries() {
		return dataSeries;
	}

	public void setDataSeries(GraphViewSeries dataSeries) {
		this.dataSeries = dataSeries;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}
	public ArrayList<Double> getDataPoints() {
		return dataPoints;
	}
	public void setDataPoints(ArrayList<Double> dataPoints) {
		this.dataPoints = dataPoints;
	}
}
