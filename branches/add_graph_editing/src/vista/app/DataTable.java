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
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import vista.db.dss.DSSUtil;
import vista.graph.Graph;
import vista.gui.ExcelAdapter;
import vista.gui.VistaUtils;
import vista.set.DataReference;
import vista.set.DataRetrievalException;
import vista.set.DataSet;
import vista.set.FlagUtils;
import vista.set.SetUtils;

/**
 * This is a table view on the DataSet.
 * 
 * @author Nicky Sandhu
 * @version $Id: DataTable.java,v 1.1 2003/10/02 20:48:27 redwood Exp $
 */
public class DataTable extends DefaultFrame {
	/**
    *
    */
	public DataTable(DataReference ref) {
		this(ref, true);
	}

	/**
	 * Construct a table
	 */
	public DataTable(DataReference ref, boolean visibleOnStart) {
		super();
		setIconImage(Toolkit.getDefaultToolkit().createImage(
				VistaUtils.getImageAsBytes("/vista/planning.gif")));
		_ref = ref;
		try {
			_dataModel = new DataSetTableModel(ref.getData());
		} catch (DataRetrievalException dre) {
			VistaUtils.displayException(this._table, dre);
		}
		_table = new JTable(_dataModel);
		_table.setGridColor(Color.blue);
		_table.setVisible(true);
		_table.getTableHeader().addMouseMotionListener(
				new TableHeaderToolTipRenderer(_table, ""));
		_table.setShowVerticalLines(true);
		_table.setShowHorizontalLines(true);
		new ExcelAdapter(_table);
		//
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(5, 1));
		infoPanel.add(new JLabel(ref.getServername()));
		infoPanel.add(new JLabel(ref.getFilename()));
		infoPanel.add(new JLabel(ref.getPathname().toString()));
		infoPanel.add(new JLabel("Number of data points: "
				+ _dataModel.getRowCount()));
		if (ref.getTimeWindow() != null) {
			infoPanel.add(new JLabel(ref.getTimeWindow().toString()));
		}
		//
		JPanel gotoPanel = new JPanel();
		gotoPanel.setLayout(new BorderLayout());
		gotoPanel.add(new JLabel("Goto row: "), BorderLayout.WEST);
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
		contentPane.add(infoPanel, BorderLayout.NORTH);
		contentPane.add(tablePanel, BorderLayout.CENTER);
		contentPane.add(gotoPanel, BorderLayout.SOUTH);
		//
		JMenuBar mbar = new JMenuBar();
		mbar.add(createDataMenu());
		if (isFlagged(_ref)) {
			mbar.add(createFlagMenu());
			// editor for flags
			JComboBox flagEditor = new JComboBox();
			flagEditor.addItem("                ");
			flagEditor.addItem(FlagUtils
					.getQualityFlagName(FlagUtils.UNSCREENED_FLAG));
			flagEditor.addItem(FlagUtils.getQualityFlagName(FlagUtils.OK_FLAG));
			flagEditor.addItem(FlagUtils
					.getQualityFlagName(FlagUtils.QUESTIONABLE_FLAG));
			flagEditor.addItem(FlagUtils
					.getQualityFlagName(FlagUtils.REJECT_FLAG));
			DefaultCellEditor dce = new DefaultCellEditor(flagEditor);
			_table.getColumn("Flag Value").setCellEditor(dce);

		}
		getRootPane().setJMenuBar(mbar);
		//
		this.setTitle(ref.getPathname().toString());
		// set size according to flag/ no flag display
		int columnCount = _dataModel.getColumnCount();
		setSize(150 * columnCount, 750);
		//
		this.setVisible(visibleOnStart);
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
	private final boolean isFlagged(DataReference ref) {
		boolean isFlagged = false;
		try {
			DataSet data = ref.getData();
			isFlagged = data.isFlagged();
		} catch (DataRetrievalException dre) {
			VistaUtils.displayException(this._table, dre);
		}
		return isFlagged;
	}

	/**
   *
   */
	private JMenu createFlagMenu() {
		//
		JMenu flagMenu = new JMenu("Flag");
		JMenuItem markAsMissing = new JMenuItem("Mark selected as missing");
		JMenuItem markAsQuestionable = new JMenuItem(
				"Mark selected as questionable");
		JMenuItem markAsReject = new JMenuItem("Mark selected as reject");
		JMenuItem markAsOK = new JMenuItem("Mark selected as ok");
		JMenuItem markAsUS = new JMenuItem("Unmark selected");
		TableModel model = _table.getModel();
		DataSetTableModel dstm = null;
		if (model instanceof DataSetTableModel) {
			dstm = (DataSetTableModel) model;
		}
		JMenuItem flagOverride = null;
		if (dstm != null)
			flagOverride = new JCheckBoxMenuItem("Override flags ?", dstm
					.isFlagOveridden());
		flagMenu.add(markAsOK);
		flagMenu.add(markAsMissing);
		flagMenu.add(markAsQuestionable);
		flagMenu.add(markAsReject);
		flagMenu.add(markAsUS);
		if (flagOverride != null) {
			flagMenu.addSeparator();
			flagMenu.add(flagOverride);
		}
		// add listeners
		markAsMissing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				markAsMissing(evt);
			}
		});
		markAsQuestionable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				markAsQuestionable(evt);
			}
		});
		markAsReject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				markAsReject(evt);
			}
		});
		markAsOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				markAsOK(evt);
			}
		});
		markAsUS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				markAsUS(evt);
			}
		});
		if (flagOverride != null)
			flagOverride.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setFlagOverride(evt);
				}
			});
		return flagMenu;
	}

	/**
   *
   */
	private JMenu createDataMenu() {
		JMenu dataMenu = new JMenu("Data");
		JMenuItem showAsGraphItem = new JMenuItem("Show As Graph");
		JMenuItem showFlagsItem = new JCheckBoxMenuItem("Show Flags",
				isFlagged(_ref));
		JMenuItem showStatsItem = new JMenuItem("Show Attributes & Stats");
		JMenuItem editAttrItem = new JMenuItem("Edit Attributes");
		JMenu exportDataItem = new JMenu("Export Data to...");
		JMenuItem dssExport = new JMenuItem("DSS");
		JMenu txtMenu = new JMenu("Text");
		JMenuItem txtExport = new JMenuItem("DSS Format");
		JMenuItem txtNormalExport = new JMenuItem("Generic Format");
		JMenuItem txtTableExport = new JMenuItem("Table Format");
		txtMenu.add(txtExport);
		txtMenu.add(txtNormalExport);
		txtMenu.add(txtTableExport);
		exportDataItem.add(dssExport);
		exportDataItem.add(txtMenu);
		JMenuItem reloadItem = new JMenuItem("Reload Data");
		JMenuItem quitItem = new JMenuItem("Quit Window");
		dataMenu.add(showAsGraphItem);
		dataMenu.add(showFlagsItem);
		dataMenu.add(showStatsItem);
		dataMenu.add(editAttrItem);
		dataMenu.add(exportDataItem);
		dataMenu.addSeparator();
		dataMenu.add(reloadItem);
		dataMenu.add(quitItem);
		// add listeners
		showAsGraphItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showGraph(evt);
			}
		});
		showFlagsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				toggleFlagDisplay(evt);
			}
		});
		showStatsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showStatsDisplay(evt);
			}
		});
		editAttrItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				editAttr(evt);
			}
		});
		dssExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataToDSS(evt);
			}
		});
		txtExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportData(evt);
			}
		});
		txtNormalExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataGeneric(evt);
			}
		});
		txtTableExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportDataTable(evt);
			}
		});
		reloadItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				reloadData(evt);
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
   *
   */
	private void updateTable(int beginRow, int endRow) {
		_table.tableChanged(new TableModelEvent(_table.getModel(), beginRow,
				endRow));
		_table.repaint();
	}

	/**
   *
   */
	public void setFlagOverride(ActionEvent evt) {
		if (evt.getSource() instanceof JCheckBoxMenuItem) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) evt.getSource();
			((DataSetTableModel) _table.getModel()).setFlagOveridden(mi
					.isSelected());
		} else {
			return;
		}
	}

	/**
   *
   */
	public void markAsUS(ActionEvent evt) {
		int[] rows = _table.getSelectedRows();
		if (rows == null)
			return;
		if (rows.length <= 0)
			return;
		for (int i = 0; i < rows.length; i++) {
			_table.setValueAt(FlagUtils
					.getQualityFlagName(FlagUtils.UNSCREENED_FLAG), rows[i], 2);
		}
		updateTable(rows[0] - 1, rows[rows.length - 1] + 1);
	}

	/**
   *
   */
	public void markAsOK(ActionEvent evt) {
		int[] rows = _table.getSelectedRows();
		if (rows == null)
			return;
		if (rows.length <= 0)
			return;
		for (int i = 0; i < rows.length; i++) {
			_table.setValueAt(FlagUtils.getQualityFlagName(FlagUtils.OK_FLAG),
					rows[i], 2);
		}
		updateTable(rows[0] - 1, rows[rows.length - 1] + 1);
	}

	/**
   *
   */
	public void markAsMissing(ActionEvent evt) {
		int[] rows = _table.getSelectedRows();
		if (rows == null)
			return;
		if (rows.length <= 0)
			return;
		for (int i = 0; i < rows.length; i++) {
			Object value = _table.getValueAt(rows[i], 1);
			if (value.equals(DataSetTableModel.MV)
					|| value.equals(DataSetTableModel.MR)) {
				_table
						.setValueAt(FlagUtils
								.getQualityFlagName(FlagUtils.MISSING_FLAG),
								rows[i], 2);
			}
		}
		updateTable(rows[0] - 1, rows[rows.length - 1] + 1);
	}

	/**
   *
   */
	public void markAsQuestionable(ActionEvent evt) {
		int[] rows = _table.getSelectedRows();
		if (rows == null)
			return;
		if (rows.length <= 0)
			return;
		for (int i = 0; i < rows.length; i++) {
			_table.setValueAt(FlagUtils
					.getQualityFlagName(FlagUtils.QUESTIONABLE_FLAG), rows[i],
					2);
		}
		updateTable(rows[0] - 1, rows[rows.length - 1] + 1);
	}

	/**
   *
   */
	public void markAsReject(ActionEvent evt) {
		int[] rows = _table.getSelectedRows();
		if (rows == null)
			return;
		if (rows.length <= 0)
			return;
		for (int i = 0; i < rows.length; i++) {
			_table.setValueAt(FlagUtils
					.getQualityFlagName(FlagUtils.REJECT_FLAG), rows[i], 2);
		}
		updateTable(rows[0] - 1, rows[rows.length - 1] + 1);
	}

	/**
	 * quit window
	 */
	public void reloadData(ActionEvent evt) {
		_ref.reloadData();
		try {
			updateTable(0, _ref.getData().size());
		} catch (DataRetrievalException dre) {
			VistaUtils.displayException(this._table, dre);
		}
	}

	/**
	 * quit window
	 */
	public void quitWindow(ActionEvent evt) {
		this.setVisible(false);
		this._ref = null;
		this.dispose();
	}

	/**
	 * show graph
	 */
	public void showGraph(ActionEvent evt) {
		if (_graphFrame == null || (!_graphFrame.isVisible())) {
			GraphBuilder gb = new DefaultGraphBuilder();
			gb.addData(_ref);
			Graph[] graphs = gb.createGraphs();
			_graphFrame = new DataGraph(graphs[0], _ref.getName());
		}
	}

	/**
	 * show flag
	 */
	public void toggleFlagDisplay(ActionEvent evt) {
		if (_dataModel.isFlagDisplayed())
			_dataModel.setFlagDisplayed(false);
		else
			_dataModel.setFlagDisplayed(true);
	}

	/**
   *
   */
	public void editAttr(ActionEvent evt) {
		new DataSetAttrEditor(_ref.getData());
	}

	/**
	 * show flag
	 */
	public void showStatsDisplay(ActionEvent evt) {
		final JDialog dialog = new JDialog(this);
		JButton btn;
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(new StatsDisplayPanel(_ref.getData()),
				BorderLayout.CENTER);
		dialog.getContentPane()
				.add(btn = new JButton("OK"), BorderLayout.SOUTH);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		});
		dialog.setModal(false);
		dialog.pack();
		dialog.show();
	}

	/**
   *
   */
	public void exportData(ActionEvent evt) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "txt", "Text Format");
		if (saveFilename == null)
			return;
		try {
			DSSUtil.writeText(new DataReference[] { _ref }, saveFilename
					+ ".dss", saveFilename);
		} catch (IOException ioe) {
			VistaUtils.displayException(this, ioe);
		}
	}

	/**
	 * export data as seen in the data table
	 */
	public void exportDataGeneric(ActionEvent evt) {
		// get filename from dialog...
		String saveFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.SAVE, "txt", "DSS Text Format");
		if (saveFilename == null)
			return;
		SetUtils
				.write(_ref.getData(), saveFilename, _ref.getData().isFlagged());
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
	public void exportDataToDSS(ActionEvent evt) {
		// get filename from dialog...
		try {
			String saveFilename = VistaUtils.getFilenameFromDialog(this,
					FileDialog.SAVE, "dss", "DSS Format");
			if (saveFilename == null)
				return;
			saveFilename = VistaUtils.setExtension(saveFilename, "dss");
			DSSUtil.writeData(saveFilename, _ref.getPathname().toString(), _ref
					.getData());
		} catch (Exception ioe) {
			VistaUtils.displayException(this._table, ioe);
		}
	}

	/**
    *
    */
	public void dispose() {
		super.dispose();
		_table = null;
		_tableScrollPane = null;
		_graphFrame = null;
		_ref = null;
		_dataModel = null;
		_lineNumberField = null;
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
	private DataGraph _graphFrame;
	/**
	 * the data reference containing the data
	 */
	private DataReference _ref;
	/**
	 * the data set table model
	 */
	private DataSetTableModel _dataModel;
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
			double value = 0.0;
			try {
				value = new Double(field.getText()).doubleValue();
			} catch (NumberFormatException nfe) {
				String text = field.getText();
				Pattern pattern;
				Perl5Matcher matcher = new Perl5Matcher();
				try {
					pattern = new Perl5Compiler().compile(text,
							Perl5Compiler.CASE_INSENSITIVE_MASK);
				} catch (MalformedPatternException mpe) {
					throw new RuntimeException("Invalid Regular Expression "
							+ text);
				}
				int cpos = scrollBar.getValue();
				int nearestValue = (int) Math.round(cpos
						* _dataModel.getRowCount() / scrollBar.getMaximum());
				value = nearestValue;
				boolean gotMatch = false;
				boolean forwardSearch = true;
				int column = 0;
				while (!gotMatch) {
					if (nearestValue >= _dataModel.getRowCount()
							&& forwardSearch)
						break;
					if (nearestValue < 0 && !forwardSearch)
						break;
					Object obj = _dataModel.getValueAt(nearestValue, column);
					if (!(obj instanceof String)) {
						if (forwardSearch)
							nearestValue++;
						else
							nearestValue--;
						continue;
					}
					String ctxt = (String) obj;
					gotMatch = matcher.contains(ctxt, pattern);
					if (forwardSearch)
						nearestValue++;
					else
						nearestValue--;
				}
				if (gotMatch)
					value = nearestValue;
			}
			value = (value * scrollBar.getMaximum()) / _dataModel.getRowCount();
			scrollBar.setValue((int) value);
		}

		/**
		 * if vertical scrollbar is adjusted reflect the change in the field.
		 */
		public void adjustmentValueChanged(AdjustmentEvent evt) {
			// int value = (int) Math.round((1.0*evt.getValue()*
			// _dataModel.getRowCount())/
			// _tableScrollPane.getVerticalScrollBar().getMaximum());
			// _lineNumberField.setText( new Integer( value ).toString());
		}

		public void keyTyped(KeyEvent evt) {
		}

		public void keyReleased(KeyEvent evt) {
		}
	} // end of GotoListener
}
