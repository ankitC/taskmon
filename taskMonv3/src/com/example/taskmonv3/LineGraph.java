package com.example.taskmonv3;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import android.content.Context;
import android.graphics.Color;

public class LineGraph {

	private GraphicalView view;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
																					

	public LineGraph(String yTitle, Context context) {
		/*
		 * // Add single dataset to multiple dataset
		 * mDataset.addSeries(dataset); dataset= new TimeSeries(name); //
		 * Customization time for line 1! renderer.setColor(Color.WHITE);
		 * renderer.setPointStyle(PointStyle.SQUARE);
		 * renderer.setFillPoints(true);
		 */

		// Enable Zoom
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setXTitle("Time");
		mRenderer.setYTitle(yTitle);
		mRenderer.setYAxisMax(1);
		mRenderer.setYAxisMin(0);
		mRenderer.setGridColor(Color.BLUE);
		mRenderer.setShowGrid(true);
		mRenderer.setBackgroundColor(Color.WHITE);
		mRenderer.setLabelsColor(Color.MAGENTA);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setLabelsColor(Color.BLUE);
		mRenderer.setZoomInLimitY(1);
		mRenderer.setZoomInLimitX(0.5);
		mRenderer.setZoomEnabled(true, false);
	//	mRenderer.setScale(1);
		//mRenderer.setInScroll(true);
		mRenderer.setPanEnabled(true, false);
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		/*
		 * // Add single renderer to multiple renderer
		 * mRenderer.addSeriesRenderer(renderer);
		 */
	}

	public GraphicalView getView(Context context) {
		//view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		return view;
	}

	public XYMultipleSeriesDataset getmDataset() {
		return mDataset;
	}

	public void setmDataset(XYMultipleSeriesDataset mDataset) {
		this.mDataset = mDataset;
	}

	public XYMultipleSeriesRenderer getmRenderer() {
		return mRenderer;
	}

	public void setmRenderer(XYMultipleSeriesRenderer mRenderer) {
		this.mRenderer = mRenderer;
	}

}


