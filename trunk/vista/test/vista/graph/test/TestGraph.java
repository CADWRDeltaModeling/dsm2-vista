package vista.graph.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import junit.framework.TestCase;
import vista.app.DefaultGraphBuilder;
import vista.app.GraphFrame;
import vista.graph.Axis;
import vista.graph.Curve;
import vista.graph.GECanvas;
import vista.graph.GEContainer;
import vista.graph.Graph;
import vista.graph.RangeActor;
import vista.graph.RangeSelector;
import vista.set.DataReference;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.DefaultReference;
import vista.set.RegularTimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;

public class TestGraph {

	public static void main(String[] args) {
		TestGraph test = new TestGraph();
		test.testGraph();
	}

	public void testGraph() {
		double[] y = new double[100];
		for (int i = 0; i < y.length; i++) {
			y[i] = Math.sin(i / Math.PI);
		}
		RegularTimeSeries rts1 = new RegularTimeSeries("test1",
				"01JAN1990 0100", "1HOUR", y);
		DataSetAttr attr1 = new DataSetAttr(DataType.REGULAR_TIME_SERIES, "TIME", "CFS", "", "");
		rts1.setAttributes(attr1);
		DataReference ref1 = new DefaultReference(rts1);
		double[] y2 = new double[100];
		for (int i = 0; i < y2.length; i++) {
			y2[i] = Math.cos(i / Math.PI);
		}
		RegularTimeSeries rts2 = new RegularTimeSeries("test2",
				"01JAN1990 0100", "1HOUR", y2);
		DataSetAttr attr2 = new DataSetAttr(DataType.REGULAR_TIME_SERIES, "TIME", "FT", "", "");
		rts2.setAttributes(attr2);
		DataReference ref2 = new DefaultReference(rts2);
		DefaultGraphBuilder builder = new DefaultGraphBuilder();
		builder.addData(ref1);
		builder.addData(ref2);
		Graph[] graphs = builder.createGraphs();
		graphs[0].setBackgroundColor(Color.white);
		final Curve curve = graphs[0].getPlot().getCurve(0);
		GraphFrame fr = new GraphFrame(graphs[0], "Test Graph");
	}
}
