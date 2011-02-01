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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import vista.db.dss.DSSUtil;
import vista.gui.VistaUtils;
import vista.set.DataReference;
import vista.set.DataReferenceMath;
import vista.set.DataReferenceScalarMathProxy;
import vista.set.DataReferenceVectorMathProxy;
import vista.set.Group;
import vista.set.Pathname;
import vista.set.ProxyFactory;
import vista.time.TimeInterval;

/**
 * A panel with buttons for doing math operations. Namely addition, subtraction,
 * division and multiplication. An "=" button is given to process the result of
 * the operations thus far. A label displays the expression.
 * 
 * @author Nicky Sandhu
 * @version $Id: MathOperationsPanel.java,v 1.1 2003/10/02 20:48:34 redwood Exp
 *          $
 */
public class MathOperationsPanel extends JPanel {
	/**
	 * Adds on a math operations panel for a certain group table.
	 */
	public MathOperationsPanel(GroupTable table) {
		_table = table;
		//
		JButton plusButton = new JButton("+");
		JButton minusButton = new JButton("-");
		JButton multiplyButton = new JButton("*");
		JButton divideButton = new JButton("/");
		JButton equalsButton = new JButton("=");
		plusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				plusPressed(evt);
			}
		});
		minusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				minusPressed(evt);
			}
		});
		divideButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dividePressed(evt);
			}
		});
		multiplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				multiplyPressed(evt);
			}
		});
		equalsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				equalsPressed(evt);
			}
		});
		//
		_numberContext = new JCheckBox("Use Number");
		_numberField = new JTextField(10);
		//
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(plusButton);
		buttonPanel.add(minusButton);
		buttonPanel.add(multiplyButton);
		buttonPanel.add(divideButton);
		buttonPanel.add(equalsButton);
		buttonPanel.add(_numberField);
		buttonPanel.add(_numberContext);
		// 
		JButton periodButton = new JButton("DO OPERATION");
		periodButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doPeriod(evt);
			}
		});
		_periodOperationChoice = new JComboBox();
		_periodOperationChoice.addItem(PAVG);
		_periodOperationChoice.addItem(PMIN);
		_periodOperationChoice.addItem(PMAX);
		_periodTimeIntervalField = new JComboBox();
		TimeInterval ti = null;
		ti = DSSUtil.getTimeFactory().getTimeIntervalInstance().create("15MIN");
		_periodTimeIntervalField.addItem(ti);
		ti = DSSUtil.getTimeFactory().getTimeIntervalInstance().create("1HOUR");
		_periodTimeIntervalField.addItem(ti);
		ti = DSSUtil.getTimeFactory().getTimeIntervalInstance().create("1DAY");
		_periodTimeIntervalField.addItem(ti);
		ti = DSSUtil.getTimeFactory().getTimeIntervalInstance().create("1MON");
		_periodTimeIntervalField.addItem(ti);
		ti = DSSUtil.getTimeFactory().getTimeIntervalInstance().create("1YEAR");
		_periodTimeIntervalField.addItem(ti);
		//
		JPanel periodPanel = new JPanel();
		periodPanel.setLayout(new FlowLayout());
		periodPanel.add(periodButton);
		periodPanel.add(_periodOperationChoice);
		periodPanel.add(_periodTimeIntervalField);
		//
		JButton mergeButton = new JButton("Merge References");
		mergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doMerge(evt);
			}
		});
		//
		JButton maButton = new JButton("Do Average");
		maButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doAverage(evt);
			}
		});
		JLabel flabel = new JLabel("Forward Length");
		JLabel blabel = new JLabel("Backward Length");
		_fField = new JTextField(5);
		_bField = new JTextField(5);
		JPanel maPanel2 = new JPanel();
		maPanel2.setLayout(new GridLayout(2, 2));
		maPanel2.add(flabel);
		maPanel2.add(blabel);
		maPanel2.add(_fField);
		maPanel2.add(_bField);
		JPanel maPanel = new JPanel();
		maPanel.setLayout(new BorderLayout());
		maPanel.add(maPanel2, BorderLayout.CENTER);
		maPanel.add(maButton, BorderLayout.SOUTH);
		//
		JPanel miscPanel = new JPanel();
		miscPanel.setLayout(new FlowLayout());
		miscPanel.add(mergeButton);
		miscPanel.add(periodPanel);
		//
		//
		JButton pairButton = new JButton("Pair references");
		JButton fillButton = new JButton("Fill 1 with 2");
		JButton rlButton = new JButton("Regress 1(x) with 2(y)");
		pairButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doPairing(evt);
			}
		});
		fillButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doLRFilling(evt);
			}
		});
		rlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doLR(evt);
			}
		});
		JPanel pairPanel = new JPanel();
		pairPanel.setLayout(new FlowLayout());
		pairPanel.add(rlButton);
		pairPanel.add(fillButton);
		pairPanel.add(pairButton);
		pairPanel.add(_pairTextField = new JTextField(10));
		//
		_expression = new JLabel("Math Expression > ");
		//
		JTabbedPane mainPane = new JTabbedPane(SwingConstants.TOP);
		mainPane.addTab("Math", buttonPanel);
		mainPane.addTab("Period Ops", miscPanel);
		mainPane.addTab("Filling", pairPanel);
		mainPane.addTab("Averaging", maPanel);
		mainPane.addTab("Conversion", new ConversionOperationsPanel(table));
		mainPane.addTab("Shifting", new ShiftingTimeOperationsPanel(table));
		//  
		setLayout(new BorderLayout());
		add(mainPane, BorderLayout.CENTER);
	}

	/**
   *
   */
	public void doAverage(ActionEvent evt) {
		String bstr = _bField.getText();
		String fstr = _fField.getText();
		int fL = 0, bL = 0;
		try {
			fL = new Integer(fstr.trim()).intValue();
			bL = new Integer(bstr.trim()).intValue();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage());
			return;
		}
		// get references
		DataReference[] refs = _table.getSelectedReferences();
		if (refs == null || refs.length == 0)
			return;
		for (int i = 0; i < refs.length; i++) {
			DataReference ref = refs[i];
			try {
				_table.addReferenceAtCurrentSelection(ProxyFactory
						.createMovingAverageProxy(ref, bL, fL));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	/**
   *
   */
	public void doLR(ActionEvent evt) {
		String str = _pairTextField.getText();
		StringTokenizer st = new StringTokenizer(str, "&");
		if (st.countTokens() != 2) {
			JOptionPane
					.showMessageDialog(this,
							"Enter x reference number followed by \"&\" and the y reference number");
			return;
		}
		// get reference #'s
		String t1 = st.nextToken();
		String t2 = st.nextToken();
		int x = 0;
		int y = 0;
		try {
			x = new Integer(t1.trim()).intValue();
			y = new Integer(t2.trim()).intValue();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage());
			return;
		}
		Group group = _table.getGroup();
		int nrefs = group.getNumberOfDataReferences();
		if (x <= 0 || x > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for x reference");
			return;
		}
		if (y <= 0 || y > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for y reference");
			return;
		}
		try {
			_table.addReferenceAtCurrentSelection(ProxyFactory
					.createRegressionLineProxy(group.getDataReference(x - 1),
							group.getDataReference(y - 1)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	/**
   *
   */
	public void doLRFilling(ActionEvent evt) {
		String str = _pairTextField.getText();
		StringTokenizer st = new StringTokenizer(str, "&");
		if (st.countTokens() != 2) {
			JOptionPane
					.showMessageDialog(this,
							"Enter x reference number followed by \"&\" and the y reference number");
			return;
		}
		// get reference #'s
		String t1 = st.nextToken();
		String t2 = st.nextToken();
		int x = 0;
		int y = 0;
		try {
			x = new Integer(t1.trim()).intValue();
			y = new Integer(t2.trim()).intValue();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage());
			return;
		}
		Group group = _table.getGroup();
		int nrefs = group.getNumberOfDataReferences();
		if (x <= 0 || x > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for x reference");
			return;
		}
		if (y <= 0 || y > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for y reference");
			return;
		}
		try {
			_table.addReferenceAtCurrentSelection(ProxyFactory
					.createLRFilledTimeSeriesProxy(group
							.getDataReference(x - 1), group
							.getDataReference(y - 1)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	/**
   *
   */
	public void doPairing(ActionEvent evt) {
		String str = _pairTextField.getText();
		StringTokenizer st = new StringTokenizer(str, "&");
		if (st.countTokens() != 2) {
			JOptionPane
					.showMessageDialog(this,
							"Enter x reference number followed by \"&\" and the y reference number");
			return;
		}
		// get reference #'s
		String t1 = st.nextToken();
		String t2 = st.nextToken();
		int x = 0;
		int y = 0;
		try {
			x = new Integer(t1.trim()).intValue();
			y = new Integer(t2.trim()).intValue();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage());
			return;
		}
		Group group = _table.getGroup();
		int nrefs = group.getNumberOfDataReferences();
		if (x <= 0 || x > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for x reference");
			return;
		}
		if (y <= 0 || y > group.getNumberOfDataReferences()) {
			JOptionPane.showMessageDialog(this,
					"Invalid reference number for y reference");
			return;
		}
		try {
			_table.addReferenceAtCurrentSelection(ProxyFactory
					.createPairedTimeSeriesProxy(group.getDataReference(x - 1),
							group.getDataReference(y - 1)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	/**
   *
   */
	public void doMerge(ActionEvent evt) {
		// get references
		DataReference[] refs = _table.getSelectedReferences();
		try {
			DataReference ref = ProxyFactory.createMergingProxy(refs);
			_table.addReferenceAtCurrentSelection(ref);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	/**
   *
   */
	public void doPeriod(ActionEvent evt) {
		// get period operation
		int opId = 0;
		if (_periodOperationChoice.getSelectedItem().equals(PAVG)) {
			opId = ProxyFactory.PERIOD_AVERAGE;
		} else if (_periodOperationChoice.getSelectedItem().equals(PMIN)) {
			opId = ProxyFactory.PERIOD_MIN;
		} else if (_periodOperationChoice.getSelectedItem().equals(PMAX)) {
			opId = ProxyFactory.PERIOD_MAX;
		} else {
			throw new IllegalArgumentException("Period operation "
					+ _periodOperationChoice.getSelectedItem() + " not valid");
		}
		// get time interval
		TimeInterval ti = (TimeInterval) _periodTimeIntervalField
				.getSelectedItem();
		// get references
		DataReference[] refs = _table.getSelectedReferences();
		for (int i = 0; i < refs.length; i++) {
			try {
				DataReference ref = ProxyFactory.createPeriodOperationProxy(
						opId, refs[i], ti);
				_table.addReferenceAtCurrentSelection(ref);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
		// get proxy and add to table
	}

	/**
	 * plus button pressed
	 */
	public void plusPressed(ActionEvent evt) {
		if (DEBUG)
			System.out.println("Plus pressed");
		if (_numberContext.isSelected()) {
			opNumber(DataReferenceMath.ADD);
		} else {
			plusReference();
		}
		setExpression(_ref);
	}

	/**
   *
   */
	public void minusPressed(ActionEvent evt) {
		if (DEBUG)
			System.out.println("Minus pressed");
		if (_numberContext.isSelected()) {
			opNumber(DataReferenceMath.SUB);
		} else {
			minusReference();
		}
		setExpression(_ref);
	}

	/**
   *
   */
	public void multiplyPressed(ActionEvent evt) {
		if (DEBUG)
			System.out.println("Multiply pressed");
		if (_numberContext.isSelected()) {
			opNumber(DataReferenceMath.MUL);
		} else {
			multiplyReference();
		}
		setExpression(_ref);
	}

	/**
   *
   */
	public void dividePressed(ActionEvent evt) {
		if (DEBUG)
			System.out.println("Divide pressed");
		if (_numberContext.isSelected()) {
			opNumber(DataReferenceMath.DIV);
		} else {
			divideReference();
		}
		setExpression(_ref);
	}

	/**
   *
   */
	public void equalsPressed(ActionEvent evt) {
		if (DEBUG)
			System.out.println("Equals pressed");
		if (_numberContext.isSelected()) {
			opNumber(-1);
		} else {
			equalsReference();
		}
		addReferenceToGroup();
	}

	/**
   *
   */
	private void addReferenceToGroup() {
		// add to group and reset reference to null for next operation
		_table.addReferenceAtCurrentSelection(_ref);
		_table.updateInfoPanel();
		_table.paintAll(_table.getGraphics());
		_expression.setText("Added new reference " + _ref.getPathname()
				+ " to group");
		_ref = null;
	}

	/**
	 * prepares to add next selected item to reference.
	 */
	private void plusReference() {
		DataReference ref = (DataReference) _table.getSelectedValue();
		if (ref == null)
			return;
		//
		if (_ref == null)
			_ref = ref;
		else
			_ref = new DataReferenceVectorMathProxy(_ref, ref, _previousOp);
		_previousOp = DataReferenceMath.ADD;
		if (DEBUG)
			System.out.println("Reference = " + _ref);
	}

	/**
   *
   */
	private void minusReference() {
		DataReference ref = (DataReference) _table.getSelectedValue();
		if (ref == null)
			return;
		//
		if (_ref == null)
			_ref = ref;
		else
			_ref = new DataReferenceVectorMathProxy(_ref, ref, _previousOp);
		_previousOp = DataReferenceMath.SUB;
	}

	/**
   *
   */
	private void divideReference() {
		DataReference ref = (DataReference) _table.getSelectedValue();
		if (ref == null)
			return;
		//
		if (_ref == null)
			_ref = ref;
		else
			_ref = new DataReferenceVectorMathProxy(_ref, ref, _previousOp);
		_previousOp = DataReferenceMath.DIV;
	}

	/**
   *
   */
	private void multiplyReference() {
		DataReference ref = (DataReference) _table.getSelectedValue();
		if (ref == null)
			return;
		//
		if (_ref == null)
			_ref = ref;
		else
			_ref = new DataReferenceVectorMathProxy(_ref, ref, _previousOp);
		_previousOp = DataReferenceMath.MUL;
	}

	/**
   *
   */
	public void equalsReference() {
		try {
			DataReference ref = (DataReference) _table.getSelectedValue();
			if (ref == null)
				return;
			//
			if (_ref == null)
				_ref = ref;
			else
				_ref = new DataReferenceVectorMathProxy(_ref, ref, _previousOp);
		} catch (Exception e) {
			VistaUtils.displayException(this, e);
		}
	}

	/**
	 * sets expression describing the operations thus far.
	 */
	private void setExpression(DataReference ref) {
		if (_ref == null)
			_expression.setText("");
		else
			_expression.setText(ref.getPathname().getPart(Pathname.B_PART));
		_expression.update(_expression.getGraphics());
	}

	/**
	 * operate on the previously selected reference and operation on the given
	 * number
	 */
	private void opNumber(int op) {
		Double d = getScalarFromField();
		if (d == null)
			throw new IllegalArgumentException(
					"Incorrect or missing number in text field");
		double scalar = d.doubleValue();
		DataReference ref = (DataReference) _table.getSelectedValue();
		if (ref == null)
			throw new IllegalArgumentException(
					"Invalid or no reference selected");
		else {
			if (DEBUG)
				System.out.println(ref + " + " + scalar + " = ");
			_ref = new DataReferenceScalarMathProxy(ref, scalar, op,
					DataReferenceMath.FIRST_FIRST);
			if (DEBUG)
				System.out.println(_ref);
			addReferenceToGroup();
		}
	}

	/**
	 * @returns a double object or null if conversion not possible.
	 */
	private Double getScalarFromField() {
		Double d = null;
		String text = _numberField.getText();
		// check for number not entered
		if (text.equals("")) {
			JOptionPane.showMessageDialog(this, "Number not entered");
			return d;
		}
		// check for incorrect number specification
		try {
			d = new Double(text);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Number incorrect ? --> "
					+ text);
			return null;
		}
		return d;
	}

	/**
   *
   */
	private GroupTable _table;
	/**
   *
   */
	private JLabel _expression;
	private JTextField _numberField;
	private JCheckBox _numberContext;
	/**
   *
   */
	private DataReference _ref;
	private int _previousOp;
	private static final boolean DEBUG = false;
	private JTextField _pairTextField, _bField, _fField;
	private static String PAVG = "PERIOD AVERAGE";
	private static String PMIN = "PERIOD MINIMUM";
	private static String PMAX = "PERIOD MAXIMUM";
	private JComboBox _periodOperationChoice;
	private JComboBox _periodTimeIntervalField;
}
