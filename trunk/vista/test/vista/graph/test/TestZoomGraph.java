package vista.graph.test;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import vista.app.DataGraphFrame;
import vista.app.DefaultGraphBuilder;
import vista.graph.Curve;
import vista.graph.GECanvas;
import vista.graph.Graph;
import vista.set.DataReference;
import vista.set.DefaultReference;
import vista.set.RegularTimeSeries;

public class TestZoomGraph {

	public static void main(String[] args) {
		TestZoomGraph test = new TestZoomGraph();
		test.testZoom();
	}

	public void testZoom() {
		double[] y = new double[100];
		for (int i = 0; i < y.length; i++) {
			y[i] = Math.sin(i / Math.PI);
		}
		RegularTimeSeries rts = new RegularTimeSeries("test1",
				"01JAN1990 0100", "1HOUR", y);
		DataReference ref = new DefaultReference(rts);
		DefaultGraphBuilder builder = new DefaultGraphBuilder();
		builder.addData(ref);
		Graph[] graphs = builder.createGraphs();
		graphs[0].setBackgroundColor(Color.white);
		final Curve curve = graphs[0].getPlot().getCurve(0);
		GECanvas canvas = new GECanvas(graphs[0]);
		final DataGraphFrame fr = new DataGraphFrame(graphs[0], "Test Zoom");
		fr.pack();
		fr.setVisible(true);
		fr.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				fr.dispose();
				System.exit(0);
			}
		});
	}
}
