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

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Draws a default curve. It understands only linear and last value
 * interpolation types. Also both x and y coordinates are believed to be real
 * numbers
 * 
 * @author Nicky Sandhu
 * @version $Id: FlaggedCurve.java,v 1.1 2003/10/02 20:48:54 redwood Exp $
 */
public class FlaggedCurve extends Curve {
	private double[] _points;
	private Symbol rSymbol, qSymbol;
	/**
	 * debuggin'
	 */
	public static final boolean DEBUG = false;

	/**
   *
   */
	public FlaggedCurve(CurveAttr attributes, CurveDataModel cdm) {
		super(attributes, cdm);
		_points = new double[2];
		setName(cdm.getLegendText());
		setQuestionableSymbol(SymbolFactory.createSquare(false,
				attributes._foregroundColor, 2));
		setRejectSymbol(SymbolFactory.createCross(false,
				attributes._foregroundColor, 2));
	}

	/**
	 * sets the symbol to be drawn for questionable values
	 */
	public void setQuestionableSymbol(Symbol s) {
		qSymbol = s;
	}

	/**
	 * sets the symbol to be drawn for questionable values
	 */
	public void setRejectSymbol(Symbol s) {
		rSymbol = s;
	}

	/**
	 * Draws the data by scaling it and joining consecutive data points and/or
	 * plotting symbols for data points.
	 */
	protected void drawCurve() {
		CurveDataModel cdm = getModel();
		Axis xAxis = getXAxis();
		Axis yAxis = getYAxis();
		if (cdm == null)
			return;

		Graphics gc = getGraphics();
		Rectangle r = getBounds();

		CurveAttr attr = (CurveAttr) getAttributes();
		Symbol symbol = attr._symbol;
		Rectangle symbolBounds = new Rectangle(0, 0, 25, 25);
		Rectangle qSymbolBounds = new Rectangle(0, 0, 20, 20);
		Rectangle rSymbolBounds = new Rectangle(0, 0, 20, 20);

		if (DEBUG)
			System.out.println("Drawing curve");
		gc.setColor(attr._foregroundColor);

		int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		float thickness = attr._thickness;
		int lineType;
		int interType = cdm.getInterpolationType();
		Scale xScale = xAxis.getScale();
		Scale yScale = yAxis.getScale();
		// reset to beginning
		cdm.reset();
		if (!cdm.hasMorePoints())
			return; // empty data set

		lineType = cdm.nextPoint(_points);
		x1 = (int) xScale.scaleToUC(_points[0]);
		y1 = (int) yScale.scaleToUC(_points[1]);
		if (DEBUG)
			System.out.println("x1: " + x1 + ", y1: " + y1);
		int index = 0;
		int numMoveTo = 0; // a counter for number of move_to's in a sequence
		// loop throught the points
		while (cdm.hasMorePoints()) {
			lineType = cdm.nextPoint(_points);
			// while( !_dsi.atEnd() && (dse = _dsi.nextElement()) != null ){

			if (lineType == CurveDataModel.MOVE_TO) {
				x1 = (int) xScale.scaleToUC(_points[0]);
				y1 = (int) yScale.scaleToUC(_points[1]);
				if (DEBUG)
					System.out.println("x1: " + x1 + ", y1: " + y1);
				numMoveTo++;
			} else if (lineType == CurveDataModel.QUESTIONABLE_AT
					|| lineType == CurveDataModel.REJECT_AT) {
				x1 = (int) xScale.scaleToUC(_points[0]);
				y1 = (int) yScale.scaleToUC(_points[1]);
				if (DEBUG)
					System.out.println("x1: " + x1 + ", y1: " + y1);
				numMoveTo = 0;
			} else {
				numMoveTo = 0;
			}

			x2 = (int) xScale.scaleToUC(_points[0]);
			y2 = (int) yScale.scaleToUC(_points[1]);
			if (numMoveTo == 2)
				x2 = x1 + 3; // 2 MOVE_TO's in a row
			if (DEBUG)
				System.out.println("x2: " + x2 + ", y2: " + y2);

			// if (attr._drawLines && (r.contains(x1,y1) || r.contains(x2,y2)) )
			// {
			if (interType == CurveDataModel.INST_VAL
					|| interType == CurveDataModel.PER_VAL) {
				if (lineType == CurveDataModel.LINE_TO) {
					if (attr._drawLines
							&& (r.contains(x1, y1) || r.contains(x2, y2))) {
						if (thickness > 1)
							GraphUtils.drawThickLine(gc, x1, y1, x2, y2,
									thickness);
						else
							gc.drawLine(x1, y1, x2, y2);
					}
					// make sure first symbol gets drawn
					if (attr._drawSymbol && index % attr._dataPerSymbol == 0) {
						symbolBounds.x = x1;
						symbolBounds.y = y1;
						symbol.draw(gc, symbolBounds);
					}
				} else if (lineType == CurveDataModel.QUESTIONABLE_AT) {
					// draw questionable symbol
					qSymbolBounds.x = x1;
					qSymbolBounds.y = y1;
					qSymbol.draw(gc, qSymbolBounds);
				} else if (lineType == CurveDataModel.REJECT_AT) {
					// draw reject symbol
					rSymbolBounds.x = x1;
					rSymbolBounds.y = y1;
					rSymbol.draw(gc, rSymbolBounds);
				} else {
				}
			} else if (interType == CurveDataModel.LAST_VAL) {
				if (lineType == CurveDataModel.LINE_TO) {
					if (attr._drawLines
							&& (r.contains(x1, y1) || r.contains(x2, y2))) {
						if (thickness > 1) {
							GraphUtils.drawThickLine(gc, x1, y1, x1, y2,
									thickness);
							GraphUtils.drawThickLine(gc, x1, y2, x2, y2,
									thickness);
						} else {
							gc.drawLine(x1, y1, x1, y2);
							gc.drawLine(x1, y2, x2, y2);
						}
					}
					// make sure first symbol gets drawn
					if (attr._drawSymbol && index % attr._dataPerSymbol == 0) {
						symbolBounds.x = x1;
						symbolBounds.y = y1;
						symbol.draw(gc, symbolBounds);
					}
				} else if (lineType == CurveDataModel.QUESTIONABLE_AT) {
					// draw questionable symbol
					qSymbolBounds.x = x1;
					qSymbolBounds.y = y1;
					qSymbol.draw(gc, qSymbolBounds);
				} else if (lineType == CurveDataModel.REJECT_AT) {
					// draw reject symbol
					rSymbolBounds.x = x1;
					rSymbolBounds.y = y1;
					rSymbol.draw(gc, rSymbolBounds);
				} else {
				}
			} else {
				if (attr._drawLines
						&& (r.contains(x1, y1) || r.contains(x2, y2))) {
					if (thickness > 1)
						GraphUtils.drawThickLine(gc, x1, y1, x2, y2, thickness);
					else
						gc.drawLine(x1, y1, x2, y2);
				}
				// make sure first symbol gets drawn
				if (attr._drawSymbol && index % attr._dataPerSymbol == 0) {
					symbolBounds.x = x1;
					symbolBounds.y = y1;
					symbol.draw(gc, symbolBounds);
				}
			}
			x1 = x2;
			y1 = y2;
			index++;
		} // end loop
		// for symbols make sure the last symbol gets drawn.
		if (attr._drawSymbol && lineType == CurveDataModel.LINE_TO) {
			symbolBounds.x = x1;
			symbolBounds.y = y1;
			symbol.draw(gc, symbolBounds);
		}

	}
}
