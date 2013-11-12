package com.example.taskmonv3;

import java.util.ArrayList;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.util.Log;

/* Data Structure for keeping a record of process under observation */
public class Observer {

	private int pid;
	private double t;

	private TimeSeries utilizationDataset;
	private XYSeriesRenderer utilizationRenderer = new XYSeriesRenderer();

	private TimeSeries contextDataset;
	private XYSeriesRenderer contextRenderer = new XYSeriesRenderer();
	
	private ArrayList<Double> utilizationX;
	private ArrayList<Double> utilizationY;
	
	private ArrayList<Double> contextX;
	private ArrayList<Double> contextY;
	
	
	private int color;
	private double prevContext;
	public static int colorIndex = 0;
	public boolean seriesAdded;

	public Observer(int pid, double t) {
		this.pid = pid;
		this.t = t;
		Log.w(MainActivity.debug,"Creating Observer for Pid:"+pid);

		utilizationDataset = new TimeSeries(String.valueOf(pid));
		contextDataset = new TimeSeries(String.valueOf(pid));
		
		seriesAdded = false;

		prevContext = 0;
		color = (colorIndex++ % 5);

		utilizationRenderer.setPointStyle(PointStyle.SQUARE);
		utilizationRenderer.setFillPoints(true);
		utilizationRenderer.setColor(MainActivity.color[this.color]);

		contextRenderer.setPointStyle(PointStyle.SQUARE);
		contextRenderer.setFillPoints(true);
		contextRenderer.setColor(MainActivity.color[this.color]);
		
		utilizationX = new ArrayList<Double>();
		utilizationY = new ArrayList<Double>();
		
		contextX = new ArrayList<Double>();
		contextY = new ArrayList<Double>();
	}
	
	
	public ArrayList<Double> getUtilizationX() {
		return utilizationX;
	}

	public void setUtilizationX(ArrayList<Double> utilizationX) {
		this.utilizationX = utilizationX;
	}

	public ArrayList<Double> getUtilizationY() {
		return utilizationY;
	}

	public void setUtilizationY(ArrayList<Double> utilizationY) {
		this.utilizationY = utilizationY;
	}

	public ArrayList<Double> getContextX() {
		return contextX;
	}

	public void setContextX(ArrayList<Double> contextX) {
		this.contextX = contextX;
	}

	public ArrayList<Double> getContextY() {
		return contextY;
	}

	public void setContextY(ArrayList<Double> contextY) {
		this.contextY = contextY;
	}

	public TimeSeries getUtilizationDataset() {
		return utilizationDataset;
	}

	public void setUtilizationDataset(TimeSeries utilizationDataset) {
		this.utilizationDataset = utilizationDataset;
	}

	public XYSeriesRenderer getUtilizationRenderer() {
		return utilizationRenderer;
	}

	public void setUtilizationRenderer(XYSeriesRenderer utilizationRenderer) {
		this.utilizationRenderer = utilizationRenderer;
	}

	public TimeSeries getContextDataset() {
		return contextDataset;
	}

	public void setContextDataset(TimeSeries contextDataset) {
		this.contextDataset = contextDataset;
	}

	public XYSeriesRenderer getContextRenderer() {
		return contextRenderer;
	}

	public void setContextRenderer(XYSeriesRenderer contextRenderer) {
		this.contextRenderer = contextRenderer;
	}

	public TimeSeries getDataset() {
		return utilizationDataset;
	}

	public void setDataset(TimeSeries dataset) {
		this.utilizationDataset = dataset;
	}

	public XYSeriesRenderer getRenderer() {
		return utilizationRenderer;
	}

	public void setRenderer(XYSeriesRenderer renderer) {
		this.utilizationRenderer = renderer;
	}

	public double getPrevContext() {
		return prevContext;
	}

	public void setPrevContext(double prevContext) {
		this.prevContext = prevContext;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
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
}
