package vista.app;

import junit.framework.TestCase;
import vista.db.dss.DSSUtil;
import vista.graph.Curve;
import vista.graph.Graph;
import vista.set.RegularTimeSeries;

public class TestReorderTable extends TestCase{
	public void testReorderTable() throws Exception{
		TestMerging merging = new TestMerging();
		RegularTimeSeries rts1 = merging.createMergeTimeSeries1();
		RegularTimeSeries rts2 = merging.createMergeTimeSeries2();
		Graph[] graphs = merging.createGraphs(DSSUtil.createDataReference("", "", rts1.getName(), rts1),DSSUtil.createDataReference("", "", rts2.getName(), rts2));
		Graph g = graphs[0];
		int n = g.getPlot().getNumberOfCurves();
		Curve[] curves = new Curve[n];
		for(int i=0; i < curves.length; i++){
			curves[i] = g.getPlot().getCurve(i);
		}
		ReorderMergingCurvesDialog table = new ReorderMergingCurvesDialog(null,curves);
		Curve[] curves2 = table.getCurves();
		System.out.println(curves2);
	}
}
