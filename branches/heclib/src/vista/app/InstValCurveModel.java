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

import vista.graph.AxisAttr;
import vista.graph.CurveDataModel;
import vista.graph.SimpleTickGenerator;
import vista.graph.TickGenerator;
import vista.graph.TimeTickGenerator;
import vista.set.DataSet;
import vista.set.DataSetElement;
import vista.set.DataSetIterator;
import vista.set.ElementFilter;
import vista.set.ElementFilterIterator;
import vista.set.TimeSeries;

/**
 * An interface containing the data required for drawing a Curve.
 * 
 * @author Nicky Sandhu
 * @version $Id: InstValCurveModel.java,v 1.1 2003/10/02 20:48:32 redwood Exp $
 */
public class InstValCurveModel implements CurveDataModel {
	private DataSet _ds;
	private DataSetIterator _dsi;
	private double _xmax, _xmin, _ymax, _ymin;
	private double _xvmax, _xvmin, _xprev;
	private int _xap, _yap;
	private String _legendName;
	private Object _obj;

	/**
   *
   */
	public InstValCurveModel(DataSet ds, ElementFilter f, int xAxisPosition,
			int yAxisPosition, String legend) {
		if (ds == null)
			throw new IllegalArgumentException("Null data set");
		_ds = ds;
		if (f == null) {
			_dsi = ds.getIterator();
		} else {
			_dsi = new ElementFilterIterator(ds.getIterator(), f);
		}
		DataSetElement dse = _dsi.getMaximum();
		_xmax = dse.getX();
		_xvmax = _xmax;
		_ymax = dse.getY();
		dse = _dsi.getMinimum();
		_xmin = dse.getX();
		_xvmin = _xmin;
		_ymin = dse.getY();
		_xap = xAxisPosition;
		_yap = yAxisPosition;
		_legendName = legend;
	}

	/**
   *
   */
	public InstValCurveModel(DataSet ds, ElementFilter f, int xap, int yap) {
		this(ds, f, xap, yap, ds.getName());
	}

	/**
   *
   */
	public InstValCurveModel(DataSet ds, ElementFilter f) {
		this(ds, f, AxisAttr.BOTTOM, AxisAttr.LEFT, ds.getName());
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public void setXViewMax(double max) {
		_xvmax = max;
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public void setXViewMin(double min) {
		_xvmin = min;
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public double getXMax() {
		return _xmax;
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public double getXMin() {
		return _xmin;
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public double getYMax() {
		return _ymax;
	}

	/**
	 * gets the maximum value for the x axis
	 */
	public double getYMin() {
		return _ymin;
	}

	/**
	 * gets the interpolation type of the data
	 */
	public int getInterpolationType() {
		return INST_VAL;
	}

	/**
	 * an object associated with this model.
	 */
	public Object getReferenceObject() {
		return _obj;
	}

	/**
	 * an object associated with this model.
	 */
	public void setReferenceObject(Object obj) {
		_obj = obj;
	}

	/**
	 * resets the data iterator to beginning of curve
	 */
	public void reset() {
		_dsi.resetIterator();
		if (_ds instanceof TimeSeries) {
			while (!_dsi.atEnd() && _dsi.getElement().getX() < _xvmin)
				_dsi.advance();
			_dsi.retreat();
			_xprev = _dsi.getElement().getX();
		} else {
		}
	}

	/**
	 * gets the next point
	 * 
	 * @param points
	 *            is an array wher points[0] contains the next x value and
	 *            points[1] contains the next y value
	 * @return an integer specifing movevment only or line drawing motion
	 */
	public int nextPoint(double[] points) {
		int lineType = LINE_TO;
		if (_dsi.hasSkipped() > 0)
			lineType = MOVE_TO;
		else
			lineType = LINE_TO;
		DataSetElement dse = _dsi.getElement();
		points[0] = dse.getX();
		points[1] = dse.getY();
		_xprev = dse.getX();
		_dsi.advance();
		return lineType;
	}

	/**
	 * @return true while has more points on curve
	 */
	public boolean hasMorePoints() {
		if (_dsi.atEnd())
			return false;
		if (_ds instanceof TimeSeries) {
			double xval = _dsi.getElement().getX();
			if (xval <= _xvmax)
				return true;
			else {
				if (xval > _xvmax && _xprev <= _xvmax)
					return true;
				else
					return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * gets teh legend text for this curve
	 */
	public String getLegendText() {
		return _legendName;
	}

	/**
	 * get the x axis position for this curve
	 */
	public int getXAxisPosition() {
		return _xap;
	}

	/**
	 * geth the y axis position for this curve
	 */
	public int getYAxisPosition() {
		return _yap;
	}

	/**
	 * get the tick generator for x axis
	 */
	public TickGenerator getXAxisTickGenerator() {
		if (_ds instanceof TimeSeries)
			return new TimeTickGenerator();
		else
			return new SimpleTickGenerator();
	}

	/**
	 * get the tick generator for the y axis
	 */
	public TickGenerator getYAxisTickGenerator() {
		return new SimpleTickGenerator();
	}

	/**
   *
   */
	public void setFilter(Object f) {
		if (f instanceof ElementFilter)
			_dsi = new ElementFilterIterator(_ds.getIterator(),
					(ElementFilter) f);
	}
}
