package vista.graph.test;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import junit.framework.TestCase;
import vista.app.DefaultGraphBuilder;
import vista.graph.Axis;
import vista.graph.Curve;
import vista.graph.GECanvas;
import vista.graph.Graph;
import vista.graph.RangeActor;
import vista.graph.RangeSelector;
import vista.set.DataReference;
import vista.set.DefaultReference;
import vista.set.RegularTimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;

public class TestGraph {

	public static void main(String[] args) {
		TestGraph test = new TestGraph();
		test.testSimpleGraph();
	}

	public void testSimpleGraph() {
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
		RangeSelector rangeSelector = new RangeSelector(canvas, curve,
				new RangeActor() {

					@Override
					public void selectedRange(int minX, int maxX, int minY,
							int maxY) {
						System.out.println("Selected range: " + minX + " to "
								+ maxX);
						Axis xAxis = curve.getXAxis();
						double dmin = xAxis.getScale().scaleToDC(minX);
						double dmax = xAxis.getScale().scaleToDC(maxX);
						System.out.println("Selected range: " + dmin + " to "
								+ dmax);
						Time tmin = TimeFactory.getInstance().createTime(
								Math.round(dmin));
						Time tmax = TimeFactory.getInstance().createTime(
								Math.round(dmax));
						System.out.println("Selected range: " + tmin + " to "
								+ tmax);
						System.out.println("Selected range: " + minY + " to "
								+ maxY);
						Axis yAxis = curve.getYAxis();
						double ymin = yAxis.getScale().scaleToDC(minY);
						double ymax = yAxis.getScale().scaleToDC(maxY);
						System.out.println("Selected Y range: " + ymin + " to "
								+ ymax);
					}
				});
		final JFrame fr = new JFrame();
		fr.getContentPane().add(canvas);
		fr.pack();
		fr.setVisible(true);
		fr.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				fr.dispose();
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}
}
