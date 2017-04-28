package gov.ca.hec;
import java.util.HashMap;

import hec.heclib.dss.HecDss;
import hec.hecmath.HecMath;
import hec.hecmath.TimeSeriesMath;
import hec.io.DataContainer;
import hec.io.TimeSeriesContainer;

public class ComparePlots {
	
	public static void main(String[] args) throws Exception{
	    // starttime and endtime in HEC Time format
	    String stime = "05JAN1990 0000";
	    String etime = "30JAN2005 2400";
	    //Files corresponding to the pathnames in plotSet structure
	    String [] files = new String[]{"Z:/delta/dsm2_v812/studies/extended/output/historical_extended_hist_ext1.dss",
	             "Z:/delta/dsm2_v812/studies/extended/output/historical_extended_hist_ext1_original.dss",
	             "Z:/delta/dsm2_v812/timeseries/hist_19902012.dss"};
	    // a plot will be generated for each plotSet identified by a name and an array of pathnames
	    HashMap<String, String[]> plotSet = new HashMap<String, String[]>();
	    plotSet.put("Martinez Stage",new String[]{"/*/RSAC054/STAGE/*/*/*441/", "/*/RSAC054/STAGE/*/*/*441/","/*/RSAC054/STAGE/*/*/*/"});
	    plotSet.put("Martinez EC", new String[]{"/*/RSAC054/EC/*/*/*/","/*/RSAC054/EC/*/*/*/","/*/RSAC054/EC/*/*/*CORRECTED/"} );
	    //set to True for doing daily averages and False for not
	    boolean doAverage=true;
	    //set to True for differences and False for not
	    boolean doDiffToFirst=true;
	    runCompare(files, plotSet, stime, etime, doAverage, doDiffToFirst);
		// read input file
		// plot data as instructed in it
	}

	public static void doCompare(String[] paths, HecDss[] dssfiles, String title, boolean doAverage, boolean diffToFirst) throws Exception{
	    DataContainer[] data = new DataContainer[paths.length];
	    for(int i=0; i < paths.length; i++){
	    	DataContainer d = HecUtils.getMatching(dssfiles[i], paths[i], null);
	        if (doAverage){
	        	d=HecUtils.average(d,"1DAY");
	        }
	        data[i]=d;
	    }
	    if (diffToFirst){
	        for(int i=0; i < paths.length; i++){
	            HecMath diff=new TimeSeriesMath((TimeSeriesContainer) data[i]).subtract(new TimeSeriesMath((TimeSeriesContainer) data[0]));
	            diff.getData().location=data[i].location+"-DIFF";
	            data[i]=diff.getData();
	        }
	    }
	    HecPlotUtils.plot(data, title);
	}
	
	public static void runCompare(String [] files, HashMap<String, String[]> plotSet, String stime, String etime, boolean doAverage, boolean doDiffToFirst) throws Exception{
	    HecDss[] dssfiles = new HecDss[files.length];
	    int count = 0;
	    for (String f: files){
	        HecDss d = HecUtils.openDSS(f);
	        d.setTimeWindow(stime, etime);
	        dssfiles[count++]=d;
	    }

	    for (String l : plotSet.keySet()){
	        String[] paths = plotSet.get(l);
	        doCompare(paths,dssfiles,l,doAverage,doDiffToFirst);
	    }
	    for (HecDss d : dssfiles ){
	        HecUtils.closeDSS(d);
	    }
	    System.out.println("END");
	}	    

}
