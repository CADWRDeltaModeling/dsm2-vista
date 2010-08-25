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
package vista.graph;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Axis contains a TickLine for ticks, TickText for tick labels, TextLine for
 * axis labels.
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: Axis.java,v 1.1 2003/10/02 20:48:48 redwood Exp $
 */
public class Axis extends GEContainer implements FontResizable {
	/**
	 * debuggin'
	 */
	public static final boolean DEBUG = false;

	/**
	 * The axis is created by Plot only when a data coordinate range is
	 * available.
	 * 
	 * @see Plot
	 */
	public Axis(int position) {
		this(new AxisAttr(), position);
	}

	/**
	 * The axis is created by Plot only when a data coordinate range is
	 * available.
	 * 
	 * @see Plot
	 */
	public Axis(AxisAttr attributes, int position) {

		super(attributes);

		setPosition(position);

		setMediator(new AxisDataSetMediator(this));

		_tickLine = new TickLine((TickLineAttr) getAttributes(), null);

		GELineLayout layout = null;
		if (getAttributes()._orientation == GEAttr.HORIZONTAL) {
			layout = new GELineLayout(GELineLayout.VERTICAL,
					GELineLayout.CENTERED_BETWEEN_BOUNDS);
			setLayout(layout);

			_tickLine.setPosition(((AxisAttr) getAttributes())._position);

			if (((AxisAttr) getAttributes())._position == TickLineAttr.BOTTOM) {
				add(_tickLine);
			} else {
				add(_tickLine);
			}
		} else {
			layout = new GELineLayout(GELineLayout.HORIZONTAL,
					GELineLayout.CENTERED_BETWEEN_BOUNDS);
			setLayout(layout);
			_tickLine.setPosition(((AxisAttr) getAttributes())._position);

			if (((AxisAttr) getAttributes())._position == AxisAttr.LEFT) {
				add(_tickLine);
			} else {
				add(_tickLine);
			}
		}

		if (getPosition() == AxisAttr.TOP) {
			setName("Top");
		} else if (getPosition() == AxisAttr.BOTTOM) {
			setName("Bottom");
		} else if (getPosition() == AxisAttr.LEFT) {
			setName("Left");
		} else if (getPosition() == AxisAttr.RIGHT) {
			setName("Right");
		}
	}

	/**
	 * Sets the tick generator. One caveat is that it does not affect previous
	 * data sets that might have been added before this tick generator took
	 * affect. This may mean that the Plot object may have to call the
	 * setDCRange again for all the data sets attached to this Axis.
	 */
	public void setTickGenerator(TickGenerator tickGenerator) {
		_tickGenerator = tickGenerator;
		if (_mediator.hasDataSets())
			regenerateTicks();
	}

	/**
	 * Gets the reference to the currently used tick generator.
	 */
	public TickGenerator getTickGenerator() {
		return _tickGenerator;
	}

	/**
	 * This sets the data coordinate range for this axis. Usually obtained from
	 * the data set values. This should be called at least once before a drawing
	 * operation to set label and TickData objects to non-null values.
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 */
	public void setDCRange(double min, double max) {
		if (DEBUG)
			System.out.println("DCRange setting to min: " + min + ", max: "
					+ max);
		_tickGenerator.generate(min, max);
		TickData[] tdArray = _tickGenerator.getTickData();
		if (DEBUG)
			System.out.println("TickLine object " + _tickLine);
		_tickLine.setTickData(tdArray);

		if (DEBUG) {
			for (int i = 0; i < tdArray.length; i++) {
				System.out.println(tdArray[i]);
			}
		}

		removeTickText();

		for (int i = tdArray.length - 1; i >= 0; i--) {
			if (DEBUG)
				System.out.println("TickData " + i + " " + tdArray[i]);
			addTickLabels(tdArray[i]);
		}

		_axisScale = _tickLine.getScale();
		if (DEBUG)
			System.out.println("Axis scale: " + _axisScale);
		// set views range
		if (getOrientation() == AxisAttr.HORIZONTAL)
			getMediator().setCurveModelMinMax(_axisScale);
	}

	/**
	 * sets the label for this axis
	 */
	public void setAxisLabel(String s) {

		TextLine labelTextLine = new TextLine(new TextLineAttr(), s);
		labelTextLine.setName("Label");
		int position = ((AxisAttr) getAttributes())._position;

		if (position == AxisAttr.LEFT || position == AxisAttr.RIGHT) {
			TextLineAttr tla = (TextLineAttr) labelTextLine.getAttributes();
			if (GraphUtils.isJDK2()) {
				tla._orientation = tla.VERTICAL;
				tla._textArrangement = TextLineAttr.SIDE_BY_SIDE;
				drawFirst(labelTextLine);
			} else {
				tla._textArrangement = TextLineAttr.TOP_ON_TOP;
			}
		}

		if (position == AxisAttr.TOP || position == AxisAttr.LEFT) {
			add("Before", labelTextLine);
		} else
			add("After", labelTextLine);

		labelTextLine.setText(s);
	}

	/**
	 * gets the label of the axis
	 */
	public String getAxisLabel() {
		return (_labelTextLine == null) ? "" : _labelTextLine.getText();
	}

	/**
	 * sets the position
	 */
	public void setPosition(int p) {
		((AxisAttr) getAttributes())._position = p;
		if (((AxisAttr) getAttributes())._position == AxisAttr.TOP
				|| ((AxisAttr) getAttributes())._position == AxisAttr.BOTTOM)
			getAttributes()._orientation = AxisAttr.HORIZONTAL;
		if (((AxisAttr) getAttributes())._position == AxisAttr.LEFT
				|| ((AxisAttr) getAttributes())._position == AxisAttr.RIGHT)
			getAttributes()._orientation = AxisAttr.VERTICAL;
	}

	/**
	 * gets the position of axis top,left,bottom or right?
	 */
	public int getPosition() {
		return ((AxisAttr) getAttributes())._position;
	}

	/**
	 * gets the orientation of the axis: horizontal or vertical?
	 */
	public int getOrientation() {
		return getAttributes()._orientation;
	}

	/**
	 * Delegates to the TickLine object to get the scaling
	 * 
	 * @return The Scale object
	 */
	public Scale getScale() {
		return _axisScale;
	}

	/**
	 * Gets the major tick information
	 * 
	 * @return The TickData object for major Ticks
	 */
	public TickData getMajorTickData() {
		TickData[] tdArray = _tickLine.getTickData();
		return tdArray[0];
	}

	/**
	 * adds the tick labels if available
	 */
	private void addTickLabels(TickData tickData) {
		String[] labels = tickData.getLabels();
		if (labels == null)
			return;
		TickTextAttr ttxa = ((AxisAttr) getAttributes())
				.getTickTextAttributes();
		if (DEBUG)
			System.out.println("TickTextAttr " + ttxa + " Font " + ttxa._font);
		TickText tickText = new TickText(ttxa, null, null);
		if (getAttributes()._orientation == GEAttr.HORIZONTAL) {
			tickText.setLayout(new GETickLayout(GETickLayout.HORIZONTAL,
					GETickLayout.CENTERED_ON_BOUNDS, null));
			if (((AxisAttr) getAttributes())._position == AxisAttr.BOTTOM) {
				insertAt(indexOf(_tickLine) + 1, tickText);
			} else {
				insertAt(indexOf(_tickLine), tickText);
			}
		} else {
			tickText.setLayout(new GETickLayout(GETickLayout.VERTICAL,
					GETickLayout.CENTERED_ON_BOUNDS, null));
			if (((AxisAttr) getAttributes())._position == AxisAttr.LEFT) {
				insertAt(indexOf(_tickLine), tickText);
			} else {
				insertAt(indexOf(_tickLine) + 1, tickText);
			}
		}
		tickText.setLabels(labels);

		if (tickText.getLayout() instanceof GETickLayout)
			((GETickLayout) tickText.getLayout()).setTickData(tickData);
	}

	/**
	 * removes all elements of class TickText. This operation is needed at the
	 * time when range is changed and a new set of labels needs to be generated.
	 */
	private void removeTickText() {
		int count = getElementCount();
		for (int i = 0; i < count; i++) {
			GraphicElement ge = (GraphicElement) getElement(i);
			if (ge instanceof TickText) {
				remove(ge);
				count = getElementCount();
				i = 0;
			}
		}
	}

	/**
	 * regenerates ticks. May be used after tick generator object is reset to
	 * something other than the default
	 */
	public void regenerateTicks() {
		double min = _mediator.getMinimum();
		double max = _mediator.getMaximum();

		if (min == max) {
			min = min - 0.5;
			max = max + 0.5;
		}
		setDCRange(min, max);
	}

	/**
	 * sets font by ratio
	 */
	public void setFontByRatio(double fontResizeRatio) {
		for (Enumeration iterator = getIterator(); iterator.hasMoreElements();) {
			GraphicElement ge = (GraphicElement) iterator.nextElement();
			if (ge instanceof FontResizable)
				((FontResizable) ge).setFontByRatio(fontResizeRatio);
		}
	}

	/**
   *
   */
	public void setMediator(AxisDataSetMediator mediator) {
		_mediator = mediator;
	}

	/**
   *
   */
	protected void attachCurve(Curve c) {
		_mediator.attach(c.getModel());
		if (getOrientation() == AxisAttr.HORIZONTAL)
			setTickGenerator(c.getModel().getXAxisTickGenerator());
		else
			setTickGenerator(c.getModel().getYAxisTickGenerator());
		regenerateTicks();
	}

	/**
   *
   */
	protected void detachCurve(Curve c) {
		_mediator.detach(c.getModel());
		regenerateTicks();
	}

	/**
   *
   */
	protected int getNumberAttached() {
		return _mediator.getNumberOfCurves();
	}

	/**
   *
   */
	public AxisDataSetMediator getMediator() {
		return _mediator;
	}

	/**
	 * Returns its properties in a Properties object. These are saved on disk.
	 * 
	 * @param prefixTag
	 *            A tag to assign the context for these properties e.g. if these
	 *            properties belong to Axis class then prefixTag will be "Axis."
	 */
	public void toProperties(Properties p, String prefixTag) {
		String localTag = getPrefixTag(prefixTag);
		if (p == null)
			return;
		AxisAttr attr = (AxisAttr) getAttributes();

		p.put(localTag + "plotMajorTickLabels", new Boolean(
				attr._plotMajorTickLabels).toString());
		p.put(localTag + "plotAxisLabel", new Boolean(attr._plotAxisLabel)
				.toString());

		super.toProperties(p, localTag);
	}

	/**
	 * determines the prefix tag for properties
	 */
	public String getPrefixTag(String prefixTag) {
		return prefixTag + getName() + "Axis.";
	}

	/**
	 * initializes attributes and state from Properties object.
	 */
	public void fromProperties(Properties p, String prefixTag) {

		String localTag = getPrefixTag(prefixTag);
		AxisAttr attr = (AxisAttr) getAttributes();

		String property = p.getProperty(localTag + "plotMajorTickLabels");
		if (property != null) {
			attr._plotMajorTickLabels = new Boolean(property).booleanValue();
		}
		property = p.getProperty(localTag + "plotAxisLabel");
		if (property != null) {
			attr._plotAxisLabel = new Boolean(property).booleanValue();
		}
		super.fromProperties(p, getPrefixTag(prefixTag));
	}

	/**
   *
   */
	public void setPlotMajorTickLabels(boolean b) {
		((AxisAttr) getAttributes())._plotMajorTickLabels = b;
	}

	/**
   *
   */
	public boolean getPlotMajorTickLabels() {
		return ((AxisAttr) getAttributes())._plotMajorTickLabels;
	}

	/**
   *
   */
	public void setPlotAxisLabel(boolean b) {
		((AxisAttr) getAttributes())._plotAxisLabel = b;
	}

	/**
   *
   */
	public boolean getPlotAxisLabel() {
		return ((AxisAttr) getAttributes())._plotAxisLabel;
	}

	/**
   *
   */
	public void setPercentMajorTickLength(double d) {
		((AxisAttr) getAttributes())._percentMajorTickLength = d;
	}

	/**
   *
   */
	public double getPercentMajorTickLength() {
		return ((AxisAttr) getAttributes())._percentMajorTickLength;
	}

	/**
   *
   */
	public void setPercentMajor2TickLength(double d) {
		((AxisAttr) getAttributes())._percentMajor2TickLength = d;
	}

	/**
   *
   */
	public double getPercentMajor2TickLength() {
		return ((AxisAttr) getAttributes())._percentMajor2TickLength;
	}

	/**
   *
   */
	public void setPercentMinorTickLength(double d) {
		((AxisAttr) getAttributes())._percentMinorTickLength = d;
	}

	/**
   *
   */
	public double getPercentMinorTickLength() {
		return ((AxisAttr) getAttributes())._percentMinorTickLength;
	}

	/**
   *
   */
	public void setPlotMajorTicks(boolean b) {
		((AxisAttr) getAttributes())._plotMajorTicks = b;
	}

	/**
   *
   */
	public boolean getPlotMajorTicks() {
		return ((AxisAttr) getAttributes())._plotMajorTicks;
	}

	/**
   *
   */
	public void setPlotMinorTicks(boolean b) {
		((AxisAttr) getAttributes())._plotMinorTicks = b;
	}

	/**
   *
   */
	public boolean getPlotMinorTicks() {
		return ((AxisAttr) getAttributes())._plotMinorTicks;
	}

	/**
   *
   */
	public boolean getPlotLine() {
		return ((AxisAttr) getAttributes())._plotLine;
	}

	/**
   *
   */
	public void setPlotLine(boolean b) {
		((AxisAttr) getAttributes())._plotLine = b;
	}

	/**
	 * create dialog panel for this element.
	 */
	public GEDialogPanel createDialogPanel() {
		return new AxisDialogPanel(this);
	}

	/**
	 * The major and minor tick element
	 */
	private TickLine _tickLine;
	/**
	 * The axis label element.
	 */
	private TextLine _labelTextLine;
	/**
	 * The generator for ticks given a min-max range.
	 */
	private TickGenerator _tickGenerator = new SimpleTickGenerator();
	/**
	 * Stores the current scaling object
	 */
	private Scale _axisScale;
	/**
	 * Mediates information flow between data sets and axis
	 */
	private AxisDataSetMediator _mediator = null;
}
