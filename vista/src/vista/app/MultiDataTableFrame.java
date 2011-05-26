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
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import vista.db.dss.DSSUtil;
import vista.graph.Graph;
import vista.gui.ExcelAdapter;
import vista.gui.VistaUtils;
import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.SetUtils;
import vista.set.TimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;

/**
 * This is a table view on the DataSet.
 * 
 * @author Nicky Sandhu
 * @version $Id: MultiDataTable.java,v 1.1 2003/10/02 20:48:35 redwood Exp $
 */
public class MultiDataTableFrame extends DefaultFrame {
	private DataReference[] _refs;

	/**
    *
    */
	public MultiDataTableFrame(DataReference[] refs) {
		this(refs, true);
	}

	/**
	 * Construct a table
	 */
	public MultiDataTableFrame(DataReference[] refs, boolean visibleOnStart) {
		super();
		setIconImage(Toolkit.getDefaultToolkit().createImage(
				VistaUtils.getImageAsBytes("/vista/planning.gif")));
		// check to make sure we only have time series here. If we don't drop
		// non-time series and continue
		Vector tsarray = new Vector();
		Vector refarray = new Vector();
		for (int i = 0; i < refs.length; i++) {
			DataSet ds = null;
			try {
				ds = refs[i].getData();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"A null data set in given table references");
			}
			if (ds != null && ds instanceof TimeSeries) {
				tsarray.addElement(ds);
				refarray.addElement(refs[i]);
			}
		}
		// check that we have atleast some data
		if (tsarray.size() == 0)
			throw new IllegalArgumentException(
					"No data for requested references");
		TimeSeries[] tsArray = new TimeSeries[tsarray.size()];
		tsarray.copyInto(tsArray);
		//
		_refs = new DataReference[tsarray.size()];
		refarray.copyInto(_refs);
		// create table model
		_dataModel = new MultiDataSetTableModel(tsArray);
		_table = new JTable(_dataModel);
		_table.setGridColor(Color.blue);
		_table.setVisible(true);
		_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		_table.getColumn(_table.getColumnName(0)).setPreferredWidth(150);
		new ExcelAdapter(_table);
		for (int i = 1; i < _table.getColumnCount(); i++) {
			_table.getColumn(_table.getColumnName(i)).setPreferredWidth(80);
		}
		//
		JPanel gotoPanel = new JPanel();
		gotoPanel.setLayout(new BorderLayout());
		gotoPanel.add(new JLabel("Goto Time: "), BorderLayout.WEST);
		_lineNumberField = new JTextField(40);
		gotoPanel.add(_lineNumberField, BorderLayout.CENTER);
		GotoListener l1 = new GotoListener();
		_lineNumberField.addKeyListener(l1);
		//
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(_tableScrollPane = new JScrollPane(_table));
		_tableScrollPane.getVerticalScrollBar().addAdjustmentListener(l1);
		// add components...
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(tablePanel, BorderLayout.CENTER);
		contentPane.add(gotoPanel, BorderLayout.SOUTH);
		//
		_table.getTableHeader().addMouseMotionListener(
				new TableHeaderToolTipRenderer(_table, ""));
		_table.setShowVerticalLines(true);
		_table.setShowHorizontalLines(true);
		//
		JMenuBar mbar = new JMenuBar();
		mbar.add(createDataMenu());
		getRootPane().setJMenuBar(mbar);
		//
		setTitle("Multi-Data Table");
		pack();
		int columnCount = _dataModel.getColumnCount();
		int screenWidth = getToolkit().getScreenSize().width;
		setSize(
				(int) Math.min(screenWidth, _table.getPreferredSize().width) + 50,
				750 - 50);
		setVisible(visibleOnStart);
	}

	/**
	 * gets the complete table after construction.
	 */
	public JTable getTable() {
		return _table;
	}

	/**
   *
   */
	private JMenu createDataMenu() {
		JMenu dataMenu = new JMenu("Data");
		JMenuItem showAsGraphItem = new JMenuItem("Show As Graph");
		JMenu exportDataItem = new JMenu("Export Data to...");
		JMenuItem dssExport = new JMenuItem("DSS");
		JMenuItem dssExportWithoutFlags = new JMenuItem("DSS w/o flags");
		JMenu txtMenu = new JMenu("Text");
		JMenuItem txtExport = new JMenuItem("DSS Format");
		JMenuItem txtNormalExport = new JMenuItem("Generic Format");
		JMenuItem txtTableExport = new JMenuItem("Table Format");
		JCheckBoxMenuItem showFlagsItem = new JCheckBoxMenuItem("Show Flags");
		txtMenu.add(txtExport);
		txtMenu.add(txtNormalExport);
		txtMenu.add(txtTableExport);
		exportDataItem.add(dssExport);
		exportDataItem.add(dssExportWithoutFlags);
		exportDataItem.add(txtMenu);
		JMenuItem quitItem = new JMenuItem("Quit Window");
		dataMenu.add(showAsGraphItem);
		dataMenu.addSeparator();
		dataMenu.add(showFlagsItem);
		dataMenu.addSeparator();
		dataMenu.add(exportDataItem);
		dataMenu.addSeparator();
		dataMenu.add(quitItem);
		// add listeners
		showAsGraphItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showGraph(evt);
			}
		});
		dssExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataToDSS(evt,true);
			}
		});
		dssExportWithoutFlags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataToDSS(evt,false);
			}
		});
		txtExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportData(evt);
			}
		});
		txtNormalExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataNormal(evt);
			}
		});
		txtTableExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataTable(evt);
			}
		});
		showFlagsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showFlags(evt);
			}
		});
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				quitWindow(evt);
			}
		});
		return dataMenu;
	}

	/**
	 * updates table from begin row to end row
	 */
	private void updateTable(int beginRow, int endRow) {
		_table.tableChanged(new TableModelEvent(_table.getModel(), beginRow,
				endRow));
		_table.repaint();
	}

	/**
    *
    */
	public void showFlags(ActionEvent evt) {
		MultiDataSetTableModel model = (MultiDataSetTableModel) _table
				.getModel();
		model.setFlagDisplayed(!model.isFlagDisplayed());
		_table.tableChanged(new TableModelEvent(model,
				TableModelEvent.HEADER_ROW));
	}

	/**
	 * quit window
	 */
	public void quitWindow(ActionEvent evt) {
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * show graph
	 */
	public void showGraph(ActionEvent evt) {
		if (_graphFrame == null || (!_graphFrame.isVisible())) {
			GraphBuilder gb = new DefaultGraphBuilder();
			for (int i = 0; i < _refs.length; i++)
				gb.addData(_refs[i]);
			Graph[] graphs = gb.createGraphs();
			for (int i = 0; i < graphs.length; i++)
				_graphFrame = new DataGraphFrame(graphs[0], "");
		}
	}

	/**
	 * export data as seen in the data table
	 */
	public void exportData(ActionEvent evt) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "txt", "Text Format");
		if (saveFilename == null)
			return;
		try {
			DSSUtil.writeText(_refs, saveFilename + ".dss", saveFilename);
		} catch (IOException ioe) {
			VistaUtils.displayException(this, ioe);
		}
	}

	/**
	 * export data as seen in the data table
	 */
	public void exportDataNormal(ActionEvent evt) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "txt", "DSS Text Format");
		if (saveFilename == null)
			return;
		DataSet[] dataSets = new DataSet[_refs.length];

		for (int i = 0; i < _refs.length; i++) {
			DataReference ref = _refs[i];
			if (ref == null)
				continue;
			dataSets[i] = ref.getData();
		}
		SetUtils.write(dataSets, saveFilename, true);
	}

	/**
	 * export data as seen in the data table
	 */
	public void exportDataTable(ActionEvent evt) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "txt", "Text Format");
		if (saveFilename == null)
			return;
		AppUtils.dumpToText(getTable().getModel(), saveFilename);
	}

	/**
	 * export data as seen in the data table to dss format
	 */
	public void exportDataToDSS(ActionEvent evt, boolean withFlags) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "dss", "DSS Format");
		if (saveFilename == null)
			return;
		saveFilename = VistaUtils.setExtension(saveFilename, "dss");
		try {
			for (int i = 0; i < _refs.length; i++) {
				DataReference ref = _refs[i];
				DSSUtil.writeData(saveFilename, ref.getPathname().toString(),
						SetUtils.convertFlagsToValues((TimeSeries) ref.getData()), withFlags);
			}
		} catch (Exception ioe) {
			VistaUtils.displayException(this._table, ioe);
		}
	}

	/**
	 * the scroll pane
	 */
	private JScrollPane _tableScrollPane;
	/**
	 * the table
	 */
	private JTable _table;
	/**
	 * the graph fram
	 */
	private DataGraphFrame _graphFrame;
	/**
	 * the data set table model
	 */
	private MultiDataSetTableModel _dataModel;
	/**
	 * the goto line number field
	 */
	private JTextField _lineNumberField;

	/**
   *
   */
	private class GotoListener implements KeyListener, AdjustmentListener {
		/**
		 * if enter key is pressed in goto field, goto row number
		 */
		public void keyPressed(KeyEvent evt) {
			if (evt.getKeyCode() != KeyEvent.VK_ENTER)
				return;
			JTextField field = (JTextField) evt.getSource();
			JScrollBar scrollBar = _tableScrollPane.getVerticalScrollBar();
			TimeFactory tf = TimeFactory.getInstance();
			Time sTime = null;
			try {
				sTime = tf.createTime(field.getText());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Exception"
						+ e.getMessage() + " parsing time from: "
						+ field.getText());
				return;
			}
			getIndexAtTime(sTime);
			double value = getIndexAtTime(sTime);
			value = (value * scrollBar.getMaximum()) / _dataModel.getRowCount();
			// System.out.println(value);
			scrollBar.setValue((int) value);
		}

		/**
		 * a simple binary search
		 */
		private int getIndexAtTime(Time sTime) {
			TableModel model = getTable().getModel();
			Time tm = null;
			int rcmax = model.getRowCount();
			int rcmin = 0;
			int sl = (rcmax - rcmin) / 2;
			TimeFactory tf = TimeFactory.getInstance();
			while (sl != 0) {
				int mid = rcmin + sl / 2;
				String str = (String) model.getValueAt(mid, 0);
				tm = tf.createTime(str);
				if (tm.compare(sTime) > 0) {// value in upper half
					rcmax = mid;
				} else if (tm.compare(sTime) < 0) { // value in lower half
					rcmin = mid;
				} else {
					return mid;
				}
				sl = (rcmax - rcmin) / 2;
			}
			return rcmin;
		}

		/**
		 * if vertical scrollbar is adjusted reflect the change in the field.
		 */
		public void adjustmentValueChanged(AdjustmentEvent evt) {
			int value = (int) Math.round((1.0 * evt.getValue() * _dataModel
					.getRowCount())
					/ _tableScrollPane.getVerticalScrollBar().getMaximum());
			_lineNumberField.setText(new Integer(value).toString());
		}

		public void keyTyped(KeyEvent evt) {
		}

		public void keyReleased(KeyEvent evt) {
		}
	} // end of GotoListener
}
