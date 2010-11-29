/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of
    Water Resources.

    VISTA : A VISualization Tool and Analyzer.
	Version 1.0beta
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

 */
package vista.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import vista.graph.AttributeSerializer;
import vista.graph.CoordinateDisplayInteractor;
import vista.graph.Curve;
import vista.graph.CurveDataModel;
import vista.graph.FontResizeInteractor;
import vista.graph.GECanvas;
import vista.graph.GEContainer;
import vista.graph.GETreeDialog;
import vista.graph.Graph;
import vista.graph.GraphFrameInterface;
import vista.graph.GraphProperties;
import vista.graph.GraphUtils;
import vista.graph.GraphicElement;
import vista.graph.ImageSerializer;
import vista.graph.Plot;
import vista.graph.PrintPreviewer;
import vista.graph.ZoomInteractor;
import vista.gui.VistaUtils;
import vista.set.DataReference;

/**
 * This class constructs a frame and provides the context with which to interact
 * with the Graph object. The Graph object itself is contained with the
 * GraphCanvas object
 * 
 * @see Graph
 * @see HEC.DSS.GraphCanvas
 * @author Nicky Sandhu
 * @version $Id: DataGraph.java,v 1.1 2003/10/02 20:48:25 redwood Exp $
 */
public class DataGraph extends JFrame implements GraphFrameInterface {
	/**
	 * for debuggin'
	 */
	public static boolean DEBUG = false;
	/**
	 * The component on which the graph is drawn.
	 */
	public GECanvas _gC = null;
	private JPanel _mainPanel;
	public static boolean LANDSCAPE_PRINTING = false;
	public static String PRINTER_NAME = "";
	private ZoomInteractor _zi;
	private FontResizeInteractor _ri;

	/**
	 * shows graph in a frame with frame title and shows it if isVisible is true
	 */
	public DataGraph(Graph graph, String frameTitle, boolean isVisible) {
		init(graph, isVisible, frameTitle);
	}

	/**
	 * Constructor
	 */
	public DataGraph(Graph graph, boolean isVisible) {
		init(graph, isVisible, "");
	}

	/**
	 * Constructor
	 */
	public DataGraph(Graph graph, String frameTitle) {
		init(graph, true, frameTitle);
	}

	/**
    *
    */
	public void cleanup() {
		if (DEBUG)
			System.out.println("Disposing of data graph");
		getContentPane().removeAll();
		if (DEBUG)
			System.out.println("Removed all components");
		if (_gC != null) {
			if (_mainPanel != null) {
				_mainPanel.removeAll();
				_mainPanel = null;
			}
			if (_zi != null) {
				_zi.releaseResources();
				_gC.removeMouseListener(_zi);
				_gC.removeMouseMotionListener(_zi);
				_gC.removeKeyListener(_zi);
				_zi = null;
			}
			if (_ri != null) {
				_ri.releaseResources();
				_gC.removeComponentListener(_ri);
				_ri = null;
			}
			try {
				_gC.finalize();
				_gC = null;
			} catch (Throwable exc) {
				exc.printStackTrace(System.err);
				throw new RuntimeException(exc.getMessage());
			}
		}
		if (DEBUG)
			System.out.println("Set _gC to null");
	}

	/**
   *
   */
	private void init(Graph graph, boolean isVisible, String frameTitle) {
		setTitle(frameTitle);
		setIconImage(Toolkit.getDefaultToolkit().createImage(
				VistaUtils.getImageAsBytes("/vista/planning.gif")));
		// add graph to canvas
		addGECanvas(graph);
		// set curve filters for the graph
		AppUtils.setCurveFilter(graph, AppUtils.getCurrentCurveFilter());
		// set up menus and their listeners
		JMenuBar mb = new JMenuBar();
		JMenu mainMenu = getMainMenu();
		JMenu displayMenu = getDisplayMenu();
		mb.add(mainMenu);
		mb.add(displayMenu);
		setJMenuBar(mb);
		// get attribute properties from graph properties file.
		String propertiesFile = "graphPropertiesFile";
		InputStream is = VistaUtils.getPropertyFileAsStream(propertiesFile);
		if (is == null)
			is = VistaUtils
					.getResourceAsStream("/vista/graph/demo1.properties");
		new AttributeSerializer(graph).load(is);
		//
		this.addWindowListener(new QuitListener());
		// position frame and show
		this.pack();
		Toolkit tk = getToolkit();
		Dimension screenSize = tk.getScreenSize();
		Dimension frameSize = getSize();
		this.setLocation(screenSize.width - frameSize.width, screenSize.height
				- frameSize.height);
		this.setVisible(isVisible);
		this.repaint();
	}

	/**
	 * adds GraphicElement canvas
	 */
	private void addGECanvas(Graph graph) {
		// create a graphic element canvas for the graph
		_gC = new GECanvas(graph);
		// add graph canvas to frame and set its listeners
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(_gC, BorderLayout.CENTER);
		//
		addInteractors(graph);
	}

	/**
	 * adds toolbar to frame
	 */
	public void addToolBar(JToolBar tb) {
		_mainPanel = new JPanel();
		_mainPanel.setLayout(new BorderLayout());
		_mainPanel.add(_gC, BorderLayout.CENTER);
		this.getContentPane().removeAll();
		this.getContentPane().add(tb, BorderLayout.NORTH);
		this.getContentPane().add(_mainPanel);
	}

	/**
	 * sets graph in canvas
	 */
	public void setGraph(Graph graph) {
		new AttributeSerializer(graph).load("graphPropertiesFile");
		_gC.setGraphicElement(graph);
		addInteractors(graph);
		_gC.redoNextPaint();
		_gC.paint(_gC.getGraphics());
	}

	/**
	 * adds interactors to graph
	 */
	private void addInteractors(Graph graph) {
		if (_zi != null) {
			_gC.removeMouseListener(_zi);
			_gC.removeMouseMotionListener(_zi);
			_gC.removeKeyListener(_zi);
		}
		if (_ri != null) {
			_gC.removeComponentListener(_ri);
		}
		// zoom interactor
		_zi = new ZoomInteractor(_gC);
		if (graph.getAttributes()._backgroundColor == Color.black)
			((ZoomInteractor) _zi).setZoomRectangleColor(Color.white);
		_gC.addMouseListener(_zi);
		_gC.addMouseMotionListener(_zi);
		// resize interactor
		_ri = new FontResizeInteractor(_gC);
		_gC.addComponentListener(_ri);
	}

	/**
   *
   */
	public void doPrint() {
		if (GraphUtils.isJDK2()) {
			try {
				String methodName = "print2d";
				Class[] params = { Class.forName("java.lang.String"),
						Boolean.TYPE, Class.forName("vista.graph.GECanvas") };
				Class cl2d = Class.forName("vista.graph.Print2D");
				Method m = cl2d.getDeclaredMethod(methodName, params);
				m.invoke(null, new Object[] { PRINTER_NAME,
						new Boolean(LANDSCAPE_PRINTING), _gC });
			} catch (Exception exc) {
				exc.printStackTrace(System.err);
				throw new RuntimeException("Nested Exception: "
						+ exc.getMessage());
			}

		} else {
			doPrint(PRINTER_NAME, LANDSCAPE_PRINTING);
		}
	}

	/**
	 * does printing to file or printer using java core classes.
	 */
	public void doPrint(String printer, boolean landscape) {
		// set size to 8.5 X 11 inches == 21.25 cm X 27.5 cm
		// Dimension pSize = getSize();
		int resolution = 72; // in pixels per inch
		// f.setSize((int) 8.5*resolution, 11*resolution);
		// landscape
		// setSize( 11*resolution, (int) 8.5*resolution);
		Properties props = new Properties();
		props.put("awt.print.printer", printer);
		if (landscape)
			props.put("awt.print.orientation", "landscape");
		PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(this,
				"GraphCanvas Print Job", props);
		boolean bufferStatus = _gC.getDoubleBuffered();
		if (pj != null) {
			Graphics pg = pj.getGraphics();
			pg.translate(10, 10);
			try {
				_gC.setDoubleBuffered(false);
				_gC.paintAll(pg);
			} finally {
				pg.dispose();
				_gC.setDoubleBuffered(bufferStatus);
			}
			pj.end();
		}
		// this.setSize(pSize.width, pSize.height);
		this.repaint();
	}

	/**
	 * does printing to file or printer using java core classes.
	 */
	public void doPrintPreview() {
		new PrintPreviewer(_gC.getGraphicElement());
	}

	/**
	 * Outputs plot to gif file.
	 */

	public void outputGif() {
		String filename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, ".gif", "GIF Files");
		if (filename != null) {
			Thread serializerThread = new Thread(new ImageSerializer(filename,
					_gC, ImageSerializer.GIF), "Gif serializer");
			serializerThread.setPriority(Thread.MIN_PRIORITY);
			serializerThread.run();
		}
	}

	/**
	 * Outputs plot to ps file.
	 */
	public void outputPS() {
		String filename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, ".ps", "Postscript Files");
		if (filename != null) {
			boolean bufferStatus = _gC.getDoubleBuffered();
			_gC.setDoubleBuffered(false);
			Thread serializerThread = new Thread(new ImageSerializer(filename,
					_gC, ImageSerializer.PS), "Post-script serializer");
			serializerThread.setPriority(Thread.MIN_PRIORITY);
			serializerThread.run();
			_gC.setDoubleBuffered(bufferStatus);
		}
	}

	/**
	 * Outputs plot to jpeg file.
	 */
	public void outputJpeg() {
		String filename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, ".jpg", "Jpeg Files");
		if (filename != null) {
			Thread serializerThread = new Thread(new ImageSerializer(filename,
					_gC, ImageSerializer.JPEG), "Jpeg serializer");
			serializerThread.setPriority(Thread.MIN_PRIORITY);
			serializerThread.run();
		}
	}

	/**
	 * Outputs plot to PPM file.
	 */
	public void outputPPM() {
		String filename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, ".ppm", "PPM Files");
		if (filename != null) {
			Thread serializerThread = new Thread(new ImageSerializer(filename,
					_gC, ImageSerializer.PPM), "PPM serializer");
			serializerThread.setPriority(Thread.MIN_PRIORITY);
			serializerThread.run();
		}
	}

	/**
	 * gets the reference to the graph canvas
	 */
	public GECanvas getCanvas() {
		return _gC;
	}

	/**
	 * creates a graph attribute editing dialog
	 */
	private class EditListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new GETreeDialog(DataGraph.this, getCanvas());
		}
	}

	/**
	 * prints current graph
	 */
	private class PrintListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			doPrint();
		}
	}

	/**
	 * Loads/saves attributes
	 */
	private class AttrListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			if (s instanceof JMenuItem) {
				JMenuItem mi = (JMenuItem) s;
				String label = mi.getText();
				if (label.indexOf("Load") >= 0) {
					loadAttributes();
				} else if (label.indexOf("Save") >= 0) {
					saveAttributes();
				} else {
					throw new RuntimeException(
							"Unknown menu tried to save/load attributes!");
				}
			}
		}

		/**
		 * loads attributes
		 */
		private void loadAttributes() {
			if (_attrS == null)
				_attrS = new AttributeSerializer((Graph) _gC
						.getGraphicElement());
			if (_attrS.loadAttributes()) {
				_gC.redoNextPaint();
				_gC.repaint();
			}
		}

		/**
   *
   */
		private void saveAttributes() {
			if (_attrS == null)
				_attrS = new AttributeSerializer((Graph) _gC
						.getGraphicElement());
			_attrS.saveAttributes();
		}

		/**
   *
   */
		private AttributeSerializer _attrS = null;
	}

	/**
	 * quits on quit command
	 */
	protected class QuitListener extends WindowAdapter implements
			ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (DEBUG)
				System.out.println("Quit event");
			dispose();
			cleanup();
		}

		public void windowClosed(WindowEvent evt) {
			if (DEBUG)
				System.out.println("Window closed event");
			cleanup();
			// dispose();
		}

		public void windowClosing(WindowEvent evt) {
			if (DEBUG)
				System.out.println("Window closing event");
			cleanup();
			// dispose();
		}
	}

	/**
	 * saves this graph to various file formats
	 */
	protected class SaveAsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			if (!(obj instanceof JMenuItem))
				return;
			JMenuItem mItem = (JMenuItem) e.getSource();
			String label = mItem.getText();

			if (label.indexOf("gif") >= 0) {
				outputGif();
			} else

			if (label.indexOf("post") >= 0) {
				outputPS();
			} else if (label.indexOf("ppm") >= 0) {
				outputPPM();
			} else if (label.indexOf("jpeg") >= 0) {
				outputJpeg();
			} else {
				throw new RuntimeException("Unknown kind of format?: " + mItem);
			}
		}

	}

	/**
	 * 
	 * 
	 * @author Nicky Sandhu
	 * @version $Id: DataGraph.java,v 1.1 2003/10/02 20:48:25 redwood Exp $
	 */
	private class DisplayCoordinateListener implements ActionListener {
		private CoordinateDisplayInteractor _cdi;

		public void actionPerformed(ActionEvent evt) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) evt.getSource();
			if (mi.isSelected()) {
				_cdi = new CoordinateDisplayInteractor(getCanvas());
				getCanvas().addMouseMotionListener(_cdi);
			} else {
				if (_cdi != null)
					getCanvas().removeMouseMotionListener(_cdi);
				_cdi.doneDisplaying();
			}
		}
	} // end of Displa....

	/**
	 * 
	 * 
	 * @author Nicky Sandhu
	 * @version $Id: DataGraph.java,v 1.1 2003/10/02 20:48:25 redwood Exp $
	 */
	private class FontResizeListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) evt.getSource();
			if (_ri != null)
				((FontResizeInteractor) _ri).setDoResize(mi.isSelected());
		}
	}

	/**
	 * 
	 * 
	 * @author Nicky Sandhu
	 * @version $Id: DataGraph.java,v 1.1 2003/10/02 20:48:25 redwood Exp $
	 */
	class DisplayFlagListener implements ActionListener {
		JMenuItem qItem, gItem, rItem, sItem, uItem;

		/**
		 * 
         */
		public void addQuestionableMenuItem(JMenuItem q) {
			qItem = q;
			qItem.addActionListener(this);
		}

		public void addGoodMenuItem(JMenuItem g) {
			gItem = g;
			gItem.addActionListener(this);
		}

		public void addRejectMenuItem(JMenuItem r) {
			rItem = r;
			rItem.addActionListener(this);
		}

		public void addUnscreenedMenuItem(JMenuItem us) {
			uItem = us;
			uItem.addActionListener(this);
		}

		/**
      *
      */
		public void actionPerformed(ActionEvent evt) {
			GraphicElement ge = _gC.getGraphicElement();
			if (!(ge instanceof Graph))
				return;
			Graph graph = (Graph) ge;
			if (qItem.isSelected())
				GraphProperties.properties.put("displayQuestionable", "true");
			else
				GraphProperties.properties.put("displayQuestionable", "false");
			if (gItem.isSelected())
				GraphProperties.properties.put("displayGood", "true");
			else
				GraphProperties.properties.put("displayGood", "false");
			if (rItem.isSelected())
				GraphProperties.properties.put("displayReject", "true");
			else
				GraphProperties.properties.put("displayReject", "false");
			if (uItem.isSelected())
				GraphProperties.properties.put("displayUnscreened", "true");
			else
				GraphProperties.properties.put("displayUnscreened", "false");
			AppUtils.setCurveFilter(graph, AppUtils.getCurrentCurveFilter());
			_gC.redoNextPaint();
			_gC.paint(_gC.getGraphics());
		}
	}

	/**
    *
    */
	class FlagEditorListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Graph graph = (Graph) _gC.getGraphicElement();
			GEContainer curveContainer = graph.getPlot().getCurveContainer();
			GraphicElement[] curves = curveContainer.getElements(Curve.class);
			if (curves == null || curves[0] == null)
				throw new RuntimeException("No curves in selection");
			Curve curve = (Curve) curves[0];
			new vista.app.FlagEditor(_gC, curve, true);
		}
	}

	/**
	 * shows the data in the graph as a table
	 */
	public void showAsTable(ActionEvent evt) {
		Graph graph = (Graph) _gC.getGraphicElement();
		GraphicElement[] plots = GraphUtils.getElements(graph, Plot.class);
		if (plots == null)
			return;
		DataReference[] refArray = null;
		for (int i = 0; i < plots.length; i++) {
			Plot plot = (Plot) plots[i];
			GraphicElement[] curves = GraphUtils.getElements(plot, Curve.class);
			if (curves == null)
				continue;
			refArray = new DataReference[curves.length];
			for (int k = 0; k < curves.length; k++) {
				refArray[k] = (DataReference) ((Curve) curves[k]).getModel()
						.getReferenceObject();
			}
		}
		if (refArray.length == 1)
			new DataTable(refArray[0]);
		else
			new MultiDataTable(refArray);
	}

	/**
   *
   */
	public void reloadData(ActionEvent evt) {
		Graph graph = (Graph) _gC.getGraphicElement();
		GraphicElement[] plots = GraphUtils.getElements(graph, Plot.class);
		if (plots == null)
			return;
		for (int i = 0; i < plots.length; i++) {
			Plot plot = (Plot) plots[i];
			GraphicElement[] curves = GraphUtils.getElements(plot, Curve.class);
			if (curves == null)
				continue;
			for (int k = 0; k < curves.length; k++) {
				Curve crv = (Curve) curves[k];
				CurveDataModel cdm = crv.getModel();
				DataReference ref = (DataReference) cdm.getReferenceObject();
				ref.reloadData();
				crv.setModel(cdm);
			}
		}
		_gC.redoNextPaint();
		repaint();
	}

	/**
 *
 */
	private JMenu getMainMenu() {
		// !! start main menu
		JMenu mainMenu = new JMenu("Graph");
		//
		JMenuItem reloadItem = new JMenuItem("Reload data");
		JMenuItem showAsTableItem = new JMenuItem("Show as Table");
		reloadItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				reloadData(evt);
			}
		});
		showAsTableItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showAsTable(evt);
			}
		});
		// !! start print menu...
		JMenuItem printItem = new JMenuItem("Print...");
		printItem.addActionListener(new PrintListener());
		JMenuItem ppItem = new JMenuItem("Print Preview");
		ppItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doPrintPreview();
			}
		});
		// @ end print menu...
		// !! start save as menu....
		JMenuItem save2gif = new JMenuItem("gif");
		JMenuItem save2ps = new JMenuItem("postscript");
		JMenuItem save2ppm = new JMenuItem("ppm");
		JMenuItem save2jpeg = new JMenuItem("jpeg");

		ActionListener sal = new SaveAsListener();
		save2jpeg.addActionListener(sal);
		save2ppm.addActionListener(sal);
		save2gif.addActionListener(sal);
		save2ps.addActionListener(sal);

		JMenu saveAsMenu = new JMenu("Save As ...");
		saveAsMenu.add(save2gif);
		saveAsMenu.add(save2ps);
		saveAsMenu.add(save2ppm);
		saveAsMenu.add(save2jpeg);
		// @ end save as menu
		// !!
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new QuitListener());
		// @
		JMenuItem loadAttrItem = new JMenuItem("Load Attributes");
		loadAttrItem.addActionListener(new AttrListener());
		JMenuItem saveAttrItem = new JMenuItem("Save Attributes");
		saveAttrItem.addActionListener(new AttrListener());

		mainMenu.add(showAsTableItem);
		mainMenu.add(reloadItem);
		mainMenu.addSeparator();
		mainMenu.add(ppItem);
		mainMenu.addSeparator();
		mainMenu.add(printItem);
		mainMenu.addSeparator();
		mainMenu.add(saveAsMenu);
		mainMenu.addSeparator();
		mainMenu.add(loadAttrItem);
		mainMenu.add(saveAttrItem);
		mainMenu.addSeparator();
		mainMenu.add(quitItem);
		return mainMenu;
	}

	/**
 *
 */
	private JMenu getDisplayMenu() {
		JMenu displayMenu = new JMenu("Display");
		JMenuItem graphEdit = new JMenuItem("Edit Graph Display...");
		graphEdit.addActionListener(new EditListener());
		JMenuItem flagEdit = new JMenuItem("Edit...");
		flagEdit.addActionListener(new FlagEditorListener());
		//
		JMenuItem displayLocationItem = new JCheckBoxMenuItem(
				"Display Co-ordinates");
		displayLocationItem.addActionListener(new DisplayCoordinateListener());
		JMenuItem fontResizeItem = new JCheckBoxMenuItem(
				"Set Font Resize By Ratio");
		fontResizeItem.addActionListener(new FontResizeListener());
		fontResizeItem.setSelected(true);
		//
		DisplayFlagListener fdl = new DisplayFlagListener();
		JCheckBoxMenuItem goodItem = new JCheckBoxMenuItem("Display Good");
		JCheckBoxMenuItem questionableItem = new JCheckBoxMenuItem(
				"Display Questionable");
		JCheckBoxMenuItem rejectItem = new JCheckBoxMenuItem("Display Reject");
		JCheckBoxMenuItem unscreenedItem = new JCheckBoxMenuItem(
				"Display Unscreened");
		if (GraphProperties.properties.get("displayGood").equals("true"))
			goodItem.setSelected(true);
		if (GraphProperties.properties.get("displayQuestionable")
				.equals("true"))
			questionableItem.setSelected(true);
		if (GraphProperties.properties.get("displayReject").equals("true"))
			rejectItem.setSelected(true);
		if (GraphProperties.properties.get("displayUnscreened").equals("true"))
			unscreenedItem.setSelected(true);
		//
		fdl.addGoodMenuItem(goodItem);
		fdl.addQuestionableMenuItem(questionableItem);
		fdl.addRejectMenuItem(rejectItem);
		fdl.addUnscreenedMenuItem(unscreenedItem);
		JMenu flagDisplayMenu = new JMenu("Display...");
		flagDisplayMenu.add(goodItem);
		flagDisplayMenu.add(questionableItem);
		flagDisplayMenu.add(rejectItem);
		flagDisplayMenu.add(unscreenedItem);
		//
		JMenu flagMenu = new JMenu("Flag");
		flagMenu.add(flagEdit);
		flagMenu.add(flagDisplayMenu);
		//
		displayMenu.add(flagMenu);
		displayMenu.addSeparator();
		displayMenu.add(displayLocationItem);
		displayMenu.add(fontResizeItem);
		displayMenu.addSeparator();
		displayMenu.add(graphEdit);
		return displayMenu;
	}
}
