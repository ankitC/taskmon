package com.example.taskmonv2;

import java.util.ArrayList;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

/* Data Structure for keeping a record of process under observation */
public class Observer {

	private int pid;
	private double t;
	private GraphViewSeries dataSeries;
	private GraphViewSeries contextSeries;
	private ArrayList<Double> dataPoints;
	private ArrayList<Double> contextPoints;
	private ArrayList<Integer> contextState;
	private int prevContext;
	
	public ArrayList<Integer> getContextState() {
		return contextState;
	}


	public void setContextState(ArrayList<Integer> contextState) {
		this.contextState = contextState;
	}
	private int color;

	public ArrayList<Double> getContextPoints() {
		return contextPoints;
	}


	public void setContextPoints(ArrayList<Double> contextPoints) {
		this.contextPoints = contextPoints;
	}
	public static int colorIndex = 0;
	
	public Observer(int pid, double t){
		this.pid = pid;
		this.t = t;
		dataSeries = new GraphViewSeries(new GraphViewData[]{new GraphViewData(0, 0.0d)});
		contextSeries= new GraphViewSeries(new GraphViewData[]{new GraphViewData(0, 0.0d)});
		dataPoints= new ArrayList<Double>();
		contextPoints=new ArrayList<Double>();
		contextState=new ArrayList<Integer>();
		prevContext = 0;
		color = (colorIndex++%5);
	}
	
	
	public int getPrevContext() {
		return prevContext;
	}


	public void setPrevContext(int prevContext) {
		this.prevContext = prevContext;
	}


	public GraphViewSeries getContextSeries() {
		return contextSeries;
	}


	public void setContextSeries(GraphViewSeries contextSeries) {
		this.contextSeries = contextSeries;
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
