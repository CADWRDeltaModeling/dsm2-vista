package vista.app;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import vista.set.IrregularTimeSeries;
import vista.set.MergingProxy;
import vista.set.RegularTimeSeries;
import vista.set.TimeSeries;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

public class TimeSeriesMerge {
	private TimeSeries merged;
	private int primaryTimeSeriesIndex;
	private ArrayList<TimeSeries> timeSeries;
	private TimeInterval mergeInterval;
	private TimeWindow mergeWindow;
	public TimeSeriesMerge(ArrayList<TimeSeries> timeSeries){
		this.timeSeries = timeSeries;
		if (!checkTimeSeriesAreMergable(timeSeries)){
			return;
		}
		for(TimeSeries ts: timeSeries){
			if (ts instanceof IrregularTimeSeries){
				merged = new IrregularTimeSeries(ts);
			}
		}
		primaryTimeSeriesIndex = specifyPrimaryTimeSeries(timeSeries);
		merged = doGraphicalMerge(timeSeries);
	}
	
	private TimeSeries doGraphicalMerge(ArrayList<TimeSeries> timeSeries) {
		return null;
	}

	private int specifyPrimaryTimeSeries(ArrayList<TimeSeries> timeSeries) {
		TimeSeries[] tsArray = new TimeSeries[timeSeries.size()];
		tsArray = timeSeries.toArray(tsArray);
		TimeSeries selected = (TimeSeries) JOptionPane.showInputDialog(null, "Select the primary time series for the merge", "Select Primary", JOptionPane.OK_CANCEL_OPTION, null, tsArray, tsArray[0]);
		int selectedIndex=0;
		if (selected==null){
			selectedIndex=0;
		}else{
			for(int i=0; i < tsArray.length; i++){
				if (selected==tsArray[i]){
					selectedIndex=i;
				}
			}
		}
		return selectedIndex;
	}
	/**
	 * returns the messages 
	 * @param timeSeries
	 * @return
	 */
	public boolean checkTimeSeriesAreMergable(ArrayList<TimeSeries> timeSeries) {
		if (timeSeries==null || timeSeries.size()==0){
			JOptionPane.showConfirmDialog(null, "Time Series are incompatible", "Time series merge", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (timeSeries.size() == 1){
			int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Only one time series to merge ?", "Time series merge", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (showConfirmDialog == JOptionPane.CANCEL_OPTION){
				return false;
			}
		}
		if (isAllRegular(timeSeries)){
			TimeInterval ti = null;
			for(TimeSeries ts: timeSeries){
				RegularTimeSeries rts = (RegularTimeSeries) ts;
				if (ti==null){
					ti = rts.getTimeInterval();
				} else {
					int compare = rts.getTimeInterval().compare(ti);
					if (compare != 0){
						String msg = "Time Series: " +rts.getName() + "with time interval: "+rts.getTimeInterval()+" does not have expected time interval "+ti+" as others in merge list";
						JOptionPane.showConfirmDialog(null, msg, "Time Series Merge", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			mergeInterval=ti;
		}
		mergeWindow = null;
		for(TimeSeries ts: timeSeries){
			TimeWindow tw = ts.getTimeWindow();
			if (mergeWindow==null){
				mergeWindow=TimeFactory.getInstance().createTimeWindow(tw.getStartTime(), tw.getEndTime());
			}else{
				mergeWindow = mergeWindow.union(tw);
			}
		}
		
		return true;
	}
	
	public boolean isAllRegular(ArrayList<TimeSeries> timeSeries){
		for(TimeSeries ts: timeSeries){
			if (!(ts instanceof RegularTimeSeries)){
				return false;
			}
		}
		return true;
	}

	public TimeSeries getMerged() {
		return merged;
	}

	public int getPrimaryTimeSeriesIndex() {
		return primaryTimeSeriesIndex;
	}

	public ArrayList<TimeSeries> getTimeSeries() {
		return timeSeries;
	}

}
