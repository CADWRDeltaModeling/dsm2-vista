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

public class TestRectangularSelect {

	public static void main(String[] args) {
		TestRectangularSelect test = new TestRectangularSelect();
		test.testRectangularSelect();
	}

	public void testRectangularSelect() {
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
		final JFrame fr = new JFrame();
		final RangeSelector rangeSelector = new RangeSelector(canvas, curve,
				new RangeActor() {

					@Override
					public void selectedRange(int minX, int maxX, int minY,
							int maxY) {
						String msg = "Selected range (screen horizontal): " + minX + " to "
								+ maxX;
						msg+="\n";
						Axis xAxis = curve.getXAxis();
						double dmin = xAxis.getScale().scaleToDC(minX);
						double dmax = xAxis.getScale().scaleToDC(maxX);
						msg += "Selected range (x axis): " + dmin + " to "
								+ dmax;
						msg+="\n";
						Time tmin = TimeFactory.getInstance().createTime(
								Math.round(dmin));
						Time tmax = TimeFactory.getInstance().createTime(
								Math.round(dmax));
						msg += "Selected range (time axis): " + tmin + " to "
								+ tmax;
						msg+="\n";
						msg += "Selected range (screen vertical): " + minY + " to "
								+ maxY;
						msg+="\n";
						Axis yAxis = curve.getYAxis();
						double ymin = yAxis.getScale().scaleToDC(minY);
						double ymax = yAxis.getScale().scaleToDC(maxY);
						msg += "Selected Y range (y axis): " + ymin + " to "
								+ ymax;
						JOptionPane.showMessageDialog(fr, msg, "Range Selected", JOptionPane.INFORMATION_MESSAGE);
						fr.invalidate();
					}
				});
		JPanel mainPanel = new JPanel();
		fr.getContentPane().add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(canvas, BorderLayout.CENTER);
		JButton selectButton = new JButton("Select");
		selectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rangeSelector.selectRange();
			}
		});
		mainPanel.add(selectButton, BorderLayout.PAGE_END);
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
